package com.orchestranetworks.ps.service;

import java.awt.Color;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import org.apache.poi.hssf.util.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.schema.instance.*;
import com.orchestranetworks.addon.dama.models.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.interactions.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.comparison.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.workflow.*;

public class GenerateCompareReport<S extends DataspaceEntitySelection>
	extends
	AbstractFileDownloadUserService<S>
{
	/**
	 * Defines the levels at which the report will be generated.
	 * These aren't applicable when configuring via a script task because in that case, you simply
	 * won't pass in parameters for the levels you don't want.
	 */
	public enum CompareReportLevel {
		/**
		 * Always should generate a report for whatever the service is defined on. (i.e. if defined on a table, it's a table compare).
		 * This is the default.
		 */
		DERIVED,

		/** Always generate a data space compare report */
		DATA_SPACE,

		/** Always generate a data set compare report */
		DATA_SET,

		/** Always generate a table compare report */
		TABLE,

		/** Always generate a record compare report */
		RECORD
	}

	public static final String RIGHT_DATASPACE_PARAM = "rightDataSpace";
	public static final String LEFT_DATASPACE_PARAM = "leftDataSpace";
	public static final String DATASET_PARAM = "dataSet";
	public static final String TABLE_PARAM = "table";
	private static final String DIFF_LOG_MSG = "Reporting difference in table {0} between left record {1} and right record {2} due to {3}";
	private CompareReportLevel compareReportLevel = CompareReportLevel.DERIVED;
	private AdaptationName dataSetName;
	private Path tablePath;
	private AdaptationHome rightDataSpace;
	private AdaptationHome leftDataSpace;
	private boolean isResolvedMode = false;
	private boolean useNodeLabels = true;
	private File file;
	private Map<Path, TableConfig> tableConfigs = new HashMap<>();
	private Adaptation leftRecord;
	private Adaptation rightRecord;
	private CellStyle ldiffCellStyle;
	private CellStyle rdiffCellStyle;
	private CellStyle hiddenCellStyle;
	private CellStyle extraRightCellStyle;
	private CellStyle extraLeftCellStyle;
	private CellStyle leftCellStyle;
	private CellStyle rightCellStyle;
	private CellStyle headerCellStyle;
	private Color extraRightColor = new Color(255, 255, 225);
	private Color extraLeftColor = new Color(240, 210, 210);
	private Color leftColor = new Color(225, 225, 255);
	private Color rightColor = new Color(155, 140, 235);
	private static final UnaryFunction<Adaptation, String> DEFAULT_EXTRA_LEFT_LABEL = new UnaryFunction<Adaptation, String>()
	{
		@Override
		public String evaluate(Adaptation object)
		{
			return "REMOVED";
		}
	};
	private static final UnaryFunction<Adaptation, String> DEFAULT_EXTRA_RIGHT_LABEL = new UnaryFunction<Adaptation, String>()
	{
		@Override
		public String evaluate(Adaptation object)
		{
			return "ADDED";
		}
	};
	private static final UnaryFunction<Adaptation, String> DEFAULT_LEFT_LABEL = new UnaryFunction<Adaptation, String>()
	{
		@Override
		public String evaluate(Adaptation object)
		{
			return "BEFORE";
		}
	};
	private static final UnaryFunction<Adaptation, String> DEFAULT_RIGHT_LABEL = new UnaryFunction<Adaptation, String>()
	{
		@Override
		public String evaluate(Adaptation object)
		{
			return "AFTER";
		}
	};
	private UnaryFunction<Adaptation, String> extraLeftLabel = DEFAULT_EXTRA_LEFT_LABEL;
	private UnaryFunction<Adaptation, String> extraRightLabel = DEFAULT_EXTRA_RIGHT_LABEL;
	private UnaryFunction<Adaptation, String> leftLabel = DEFAULT_LEFT_LABEL;
	private UnaryFunction<Adaptation, String> rightLabel = DEFAULT_RIGHT_LABEL;

	public GenerateCompareReport()
	{
		this(null, null, null, null, false, true);
	}

	public GenerateCompareReport(AdaptationHome leftDataSpace, AdaptationHome rightDataSpace)
	{
		this(leftDataSpace, rightDataSpace, null, null, false, true);
	}

	public GenerateCompareReport(Adaptation leftRecord, Adaptation rightRecord)
	{
		this(
			leftRecord == null ? null : leftRecord.getHome(),
			rightRecord == null ? null : rightRecord.getHome(),
			null,
			null,
			false,
			true);
		this.leftRecord = leftRecord;
		this.rightRecord = rightRecord;
	}

	public GenerateCompareReport(
		AdaptationHome leftDataSpace,
		AdaptationHome rightDataSpace,
		String dataSetName,
		Path tablePath,
		boolean isResolvedMode,
		boolean useNodeLabels)
	{
		super();
		this.leftDataSpace = leftDataSpace;
		this.rightDataSpace = rightDataSpace;
		this.dataSetName = dataSetName != null ? AdaptationName.forName(dataSetName) : null;
		this.tablePath = tablePath;
		this.isResolvedMode = isResolvedMode;
		this.useNodeLabels = useNodeLabels;
	}

	public Adaptation getLeftRecord()
	{
		return leftRecord;
	}

	public void setLeftRecord(Adaptation leftRecord)
	{
		this.leftRecord = leftRecord;
		if (leftDataSpace == null && leftRecord != null)
			leftDataSpace = leftRecord.getHome();
	}

	public Adaptation getRightRecord()
	{
		return rightRecord;
	}

	public void setRightRecord(Adaptation rightRecord)
	{
		this.rightRecord = rightRecord;
		if (rightDataSpace == null && rightRecord != null)
			rightDataSpace = rightRecord.getHome();
	}

	public GenerateCompareReport<S> addTableConfig(TableConfig tableConfig)
	{
		this.tableConfigs.put(tableConfig.getTablePath(), tableConfig);
		return this;
	}

	public void addTableConfigs(Collection<TableConfig> configs)
	{
		for (TableConfig tableConfig : configs)
		{
			addTableConfig(tableConfig);
		}
	}

	public void setExtraRightColor(String colorHex)
	{
		this.extraRightColor = colorForHex(colorHex);
	}

	public void setExtraLeftColor(String colorHex)
	{
		this.extraLeftColor = colorForHex(colorHex);
	}

	public void setLeftColor(String colorHex)
	{
		this.leftColor = colorForHex(colorHex);
	}

	public void setRightColor(String colorHex)
	{
		this.rightColor = colorForHex(colorHex);
	}

	private static java.awt.Color colorForHex(String hex)
	{
		return new java.awt.Color(
			Integer.valueOf(hex.substring(1, 3), 16).intValue(),
			Integer.valueOf(hex.substring(3, 5), 16).intValue(),
			Integer.valueOf(hex.substring(5, 7), 16).intValue());
	}

	public void setExtraRightLabel(UnaryFunction<Adaptation, String> label)
	{
		this.extraRightLabel = label;
	}

	public void setExtraLeftLabel(UnaryFunction<Adaptation, String> label)
	{
		this.extraLeftLabel = label;
	}

	public void setLeftLabel(UnaryFunction<Adaptation, String> label)
	{
		this.leftLabel = label;
	}

	public void setRightLabel(UnaryFunction<Adaptation, String> label)
	{
		this.rightLabel = label;
	}

	@Override
	public void landService()
	{
		if (file != null)
			super.landService();
		else
		{
			alert("No differences");
			super.landService();
		}
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		// if this is being executed as a workflow UI Service, then set the Accept button to enabled by default
		SessionInteraction interaction = session.getInteraction(true);
		if (interaction != null)
		{
			interaction.complete(null);
		}

		try
		{
			Workbook workbook = new XSSFWorkbook();
			Set<String> sheetNames = new HashSet<>();
			setCellStyles(workbook);

			SessionPermissions permissions = session.getPermissions();
			if (leftRecord != null && rightRecord != null)
			{
				compareRecords(workbook, sheetNames, permissions, session.getLocale());
			}
			else if (dataSetName != null)
			{
				Adaptation leftDataSet = leftDataSpace.findAdaptationOrNull(dataSetName);
				Adaptation rightDataSet = rightDataSpace.findAdaptationOrNull(dataSetName);
				if (tablePath != null && !tablePath.isRoot())
				{
					AdaptationTable leftTable = leftDataSet.getTable(tablePath);
					AdaptationTable rightTable = rightDataSet.getTable(tablePath);
					TableConfig config = tableConfigs.get(tablePath);
					compareTable(
						workbook,
						sheetNames,
						leftTable,
						rightTable,
						config,
						null,
						permissions,
						null);
				}
				else
				{
					compareDataSet(
						workbook,
						sheetNames,
						leftDataSet,
						rightDataSet,
						null,
						permissions,
						session.getLocale());
				}
			}
			else
			{
				compareDataSpace(
					workbook,
					sheetNames,
					leftDataSpace,
					rightDataSpace,
					permissions,
					session.getLocale());
			}
			if (workbook.getNumberOfSheets() > 0)
			{
				//Old way 
				//file = File.createTempFile("CompareReport", ".xlsx");

				//new way
				// *** CodeQL remediation fix java nio package is more secure
				java.nio.file.Path temp = java.nio.file.Files
					.createTempFile("CompareReport", ".xlsx");

				file = new File(
					System.getProperty("java.io.tmpdir"),
					temp.getFileName().toString());

				file.deleteOnExit();
				saveWorkbook(workbook, file);
			}
		}
		catch (Exception e)
		{
			throw OperationException.createError("Error creating comparison report", e);
		}
	}
	private void compareRecords(
		Workbook workbook,
		Set<String> sheetNames,
		SessionPermissions permissions,
		Locale locale)
	{
		// DifferenceBetweenOccurrences diff = DifferenceHelper
		// .compareOccurrences(recordOnLeft, recordOnRight, isResolvedMode);
		// should we add a tab with the root differences?
		Adaptation leftDataSet = leftRecord.getContainer();
		Adaptation rightDataSet = rightRecord.getContainer();
		Map<AdaptationTable, String> tableNames = getTableNames(rightDataSet, locale);
		AccessPermission ap = permissions.getAdaptationAccessPermission(rightDataSet);
		if (ap.isHidden())
			return;
		for (TableConfig tableConfig : tableConfigs.values())
		{
			compareAssociatedTable(
				workbook,
				sheetNames,
				leftDataSet,
				rightDataSet,
				tableConfig,
				permissions,
				tableNames);
		}
	}

	private void setCellStyles(Workbook workbook)
	{
		IndexedColorMap icm = ((XSSFWorkbook) workbook).getStylesSource().getIndexedColors();
		XSSFCellStyle hiddenCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.hiddenCellStyle = hiddenCellStyle;
		hiddenCellStyle.setFillPattern(FillPatternType.THICK_FORWARD_DIAG);
		hiddenCellStyle
			.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
		XSSFCellStyle headerCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.headerCellStyle = headerCellStyle;
		Font font = workbook.createFont();
		font.setBold(true);
		headerCellStyle.setFont(font);
		headerCellStyle
			.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
		headerCellStyle.setBorderBottom(BorderStyle.DOUBLE);
		XSSFCellStyle extraRightCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.extraRightCellStyle = extraRightCellStyle;
		extraRightCellStyle.setFillForegroundColor(new XSSFColor(extraRightColor, icm));
		extraRightCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		XSSFCellStyle extraLeftCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.extraLeftCellStyle = extraLeftCellStyle;
		extraLeftCellStyle.setFillForegroundColor(new XSSFColor(extraLeftColor, icm));
		extraLeftCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		XSSFCellStyle leftCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.leftCellStyle = leftCellStyle;
		leftCellStyle.setFillForegroundColor(new XSSFColor(leftColor, icm));
		leftCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		XSSFCellStyle rightCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.rightCellStyle = rightCellStyle;
		rightCellStyle.setFillForegroundColor(new XSSFColor(rightColor, icm));
		rightCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		rightCellStyle.setBorderBottom(BorderStyle.THIN);
		XSSFCellStyle ldiffCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.ldiffCellStyle = ldiffCellStyle;
		ldiffCellStyle.cloneStyleFrom(leftCellStyle);
		font = workbook.createFont();
		font.setItalic(true);
		font.setColor(Font.COLOR_RED);
		ldiffCellStyle.setFont(font);
		XSSFCellStyle rdiffCellStyle = (XSSFCellStyle) workbook.createCellStyle();
		this.rdiffCellStyle = rdiffCellStyle;
		rdiffCellStyle.cloneStyleFrom(ldiffCellStyle);
		rdiffCellStyle.setFillForegroundColor(new XSSFColor(rightColor, icm));
		rdiffCellStyle.setBorderBottom(BorderStyle.THIN);
	}

	private void compareDataSpace(
		Workbook workbook,
		Set<String> sheetNames,
		AdaptationHome leftDataSpace,
		AdaptationHome rightDataSpace,
		SessionPermissions permissions,
		Locale locale)
	{
		DifferenceBetweenHomes diff = DifferenceHelper
			.compareHomes(leftDataSpace, rightDataSpace, isResolvedMode);
		List<DifferenceBetweenInstances> dataSetDiffs = diff.getDeltaInstances();
		for (DifferenceBetweenInstances differenceBetweenInstances : dataSetDiffs)
		{
			compareDataSet(
				workbook,
				sheetNames,
				null,
				null,
				differenceBetweenInstances,
				permissions,
				locale);
		}
	}

	private void compareDataSet(
		Workbook workbook,
		Set<String> sheetNames,
		Adaptation leftDataSet,
		Adaptation rightDataSet,
		DifferenceBetweenInstances diff,
		SessionPermissions permissions,
		Locale locale)
	{
		if (diff != null)
		{
			leftDataSet = diff.getInstanceOnLeft();
			rightDataSet = diff.getInstanceOnRight();
		}
		Map<AdaptationTable, String> tableNames = getTableNames(rightDataSet, locale);
		AccessPermission ap = permissions.getAdaptationAccessPermission(rightDataSet);
		if (ap.isHidden())
			return;
		if (diff == null)
			diff = DifferenceHelper.compareInstances(leftDataSet, rightDataSet, isResolvedMode);
		List<DifferenceBetweenTables> tableDiffs = diff.getDeltaTables();
		for (DifferenceBetweenTables differenceBetweenTables : tableDiffs)
		{
			compareTable(
				workbook,
				sheetNames,
				null,
				null,
				null,
				differenceBetweenTables,
				permissions,
				tableNames);
		}
	}

	private Map<AdaptationTable, String> getTableNames(Adaptation dataSet, Locale locale)
	{
		List<AdaptationTable> alltables = AdaptationUtil.getAllTables(dataSet);
		List<AdaptationTable> tables = alltables;
		if (!tableConfigs.isEmpty())
		{
			tables = new ArrayList<>();
			for (Path path : tableConfigs.keySet())
			{
				tables.add(dataSet.getTable(path));
			}
			Collections.sort(tables, new CollectionUtils.CompareByExampleList<>(alltables, true));
		}
		return AdaptationUtil.deriveTableNames(tables, "-", locale);
	}

	private void compareTable(
		Workbook workbook,
		Set<String> sheetNames,
		AdaptationTable leftTable,
		AdaptationTable rightTable,
		TableConfig tableConfig,
		DifferenceBetweenTables diff,
		SessionPermissions permissions,
		Map<AdaptationTable, String> tableNames)
	{
		if (diff != null)
		{
			leftTable = diff.getLeft();
			rightTable = diff.getRight();
			tableConfig = tableConfigs.get(rightTable.getTablePath());
		}
		SchemaNode tableNode = rightTable.getTableNode();
		Adaptation dataSet = rightTable.getContainerAdaptation();
		AccessPermission ap = permissions.getNodeAccessPermission(tableNode, dataSet);
		if (ap.isHidden())
			return;
		if (diff == null)
			diff = DifferenceHelper.compareAdaptationTables(
				leftTable,
				rightTable,
				isResolvedMode || (tableConfig != null && tableConfig.isNeedsResolution()));
		if (diff.isEmpty())
			return;
		List<Adaptation> extraOnRight = new ArrayList<>();
		List<ExtraOccurrenceOnRight> extras = diff.getExtraOccurrencesOnRight();
		for (ExtraOccurrenceOnRight extra : extras)
		{
			extraOnRight.add(extra.getExtraAdaptationOnRight());
		}
		List<Adaptation> extraOnLeft = new ArrayList<>();
		List<ExtraOccurrenceOnLeft> extrasOnLeft = diff.getExtraOccurrencesOnLeft();
		for (ExtraOccurrenceOnLeft extra : extrasOnLeft)
		{
			extraOnLeft.add(extra.getExtraAdaptationOnLeft());
		}
		List<DifferenceBetweenOccurrences> diffs = diff.getDeltaOccurrences();
		if (extraOnRight.isEmpty() && extraOnLeft.isEmpty()
			&& !hasDifference(diffs, tableConfig, isResolvedMode))
			return;

		List<SchemaNode> visibleColumns = getVisibleColumns(
			tableNode,
			permissions,
			dataSet,
			isResolvedMode);
		String label = tableNames != null ? tableNames.get(rightTable) : null;
		if (label == null)
			label = tableNode.getLabel(Locale.getDefault());
		label = getUniqueLabel(sheetNames, label, dataSet);
		Sheet sheet = workbook.createSheet(label);
		writeHeader(sheet, visibleColumns);
		int rowNum = writeExtraRight(
			sheet,
			tableNode,
			permissions,
			tableConfig,
			extraOnRight,
			visibleColumns);
		rowNum = writeExtraLeft(
			sheet,
			tableNode,
			permissions,
			tableConfig,
			extraOnLeft,
			visibleColumns,
			rowNum);
		writeUpdated(sheet, tableNode, permissions, tableConfig, diffs, visibleColumns, rowNum);
		formatSheet(sheet, visibleColumns.size(), getFreezeCol(tableNode, tableConfig));
	}

	private String getUniqueLabel(Set<String> sheetNames, String label, Adaptation dataSet)
	{
		// Excel has a limit on sheet name of 31 characters. Try to abbreviate the prefix.
		String newLabel = label;
		if (label.length() > 30)
			newLabel = label.substring(0, 30);
		if (sheetNames.add(newLabel))
			return newLabel;
		int i = 2;
		while (!sheetNames.add(newLabel))
		{
			newLabel = newLabel.substring(0, 29) + i;
			i++;
		}
		return newLabel;
	}

	private static boolean hasDifference(
		List<DifferenceBetweenOccurrences> diffs,
		TableConfig tableConfig,
		boolean resolvedMode)
	{
		if (tableConfig == null)
			return !diffs.isEmpty();
		for (DifferenceBetweenOccurrences diff : diffs)
		{
			if (tableConfig.isDifferent(diff, resolvedMode))
				return true;
		}
		return false;
	}

	private int getFreezeCol(SchemaNode tableNode, TableConfig tableConfig)
	{
		int result = 1;
		if (tableConfig == null)
		{
			result += tableNode.getTablePrimaryKeyNodes().length;
		}
		else
		{
			if (tableConfig.getPathToRoot() != null)
				result++;
			result += tableConfig.getMatchOnFields().size();
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void compareAssociatedTable(
		Workbook workbook,
		Set<String> sheetNames,
		Adaptation leftDataSet,
		Adaptation rightDataSet,
		TableConfig tableConfig,
		SessionPermissions permissions,
		Map<AdaptationTable, String> tableNames)
	{
		AdaptationTable rightTable = rightDataSet.getTable(tableConfig.getTablePath());
		AdaptationTable leftTable = leftDataSet.getTable(tableConfig.getTablePath());
		SchemaNode tableNode = rightTable.getTableNode();
		AccessPermission ap = permissions.getNodeAccessPermission(tableNode, rightDataSet);
		if (ap.isHidden())
			return;
		String label = tableNames != null ? tableNames.get(rightTable) : null;
		if (label == null)
			label = rightTable.getTableNode().getLabel(Locale.getDefault());

		String leftKey = leftRecord.getOccurrencePrimaryKey().format();
		String pred = AdaptationUtil.createQueryPredicate(
			tableNode.getTableOccurrenceRootNode(),
			tableConfig.getPathToRoot(),
			leftKey,
			null);
		if (tableConfig.getFilter() != null)
			pred = pred + " and (" + tableConfig.getFilter() + ")";

		List<SchemaNode> visibleColumns = getVisibleColumns(
			tableNode,
			permissions,
			rightDataSet,
			isResolvedMode);
		RequestResult rleft = leftTable.createRequestResult(pred);
		List<Adaptation> relatedLeft;
		try
		{
			relatedLeft = AdaptationUtil.getRecords(rleft);
		}
		finally
		{
			rleft.close();
		}
		String rightKey = rightRecord.getOccurrencePrimaryKey().format();
		RequestResult rright = rightTable.createRequestResult(pred.replace(leftKey, rightKey));
		List<Adaptation> relatedRight;
		try
		{
			relatedRight = AdaptationUtil.getRecords(rright);
		}
		finally
		{
			rright.close();
		}
		List[] diff = computeTableDiffs(relatedLeft, relatedRight, tableConfig);
		List<Adaptation> extraRight = diff[1];
		List<Adaptation> extraLeft = diff[0];
		List<DifferenceBetweenOccurrences> diffs = diff[2];
		if (extraRight.isEmpty() && extraLeft.isEmpty() && diffs.isEmpty())
			return;
		Sheet sheet = workbook.createSheet(label);
		writeHeader(sheet, visibleColumns);
		int rowNum = writeExtraRight(
			sheet,
			tableNode,
			permissions,
			tableConfig,
			extraRight,
			visibleColumns);
		rowNum = writeExtraLeft(
			sheet,
			tableNode,
			permissions,
			tableConfig,
			extraLeft,
			visibleColumns,
			rowNum);
		writeUpdated(sheet, tableNode, permissions, tableConfig, diffs, visibleColumns, rowNum);
		formatSheet(sheet, visibleColumns.size(), getFreezeCol(tableNode, tableConfig));
	}

	@SuppressWarnings("rawtypes")
	private List[] computeTableDiffs(
		List<Adaptation> relatedLeft,
		List<Adaptation> relatedRight,
		TableConfig tableConfig)
	{
		List<Adaptation> extraLeft = new ArrayList<>();
		List<DifferenceBetweenOccurrences> diffs = new ArrayList<>();
		Map<Object, Adaptation> rightSorted = sortRecords(tableConfig, relatedRight);
		for (int i = 0; i < relatedLeft.size(); i++)
		{
			Adaptation left = relatedLeft.get(i);
			Object key = getMatchKey(tableConfig, left);
			Adaptation rightMatch = rightSorted.remove(key);
			if (rightMatch == null)
				extraLeft.add(left);
			else
			{
				DifferenceBetweenOccurrences diff = DifferenceHelper.compareOccurrences(
					left,
					rightMatch,
					isResolvedMode || tableConfig.isNeedsResolution());
				if (tableConfig.isDifferent(diff, isResolvedMode))
					diffs.add(diff);
			}
		}
		List<Adaptation> extraRight = new ArrayList<>(rightSorted.values());
		return new List[] { extraLeft, extraRight, diffs };
	}

	private Map<Object, Adaptation> sortRecords(TableConfig tableConfig, List<Adaptation> records)
	{
		Map<Object, Adaptation> result = new LinkedHashMap<>();
		for (Adaptation record : records)
		{
			Object key = getMatchKey(tableConfig, record);
			result.put(key, record);
		}
		return result;
	}

	private static Object getMatchKey(TableConfig tableConfig, Adaptation record)
	{
		if (tableConfig != null)
			return tableConfig.getMatchKey(record);
		Path[] keySpec = record.getContainerTable().getPrimaryKeySpec();
		Object[] vals = new Object[keySpec.length];
		for (int i = 0; i < keySpec.length; i++)
		{
			Path path = keySpec[i];
			vals[i] = record.get(path);
		}
		return Arrays.asList(vals);
	}

	private List<SchemaNode> getVisibleColumns(
		SchemaNode tableNode,
		SessionPermissions permissions,
		Adaptation dataSet,
		boolean resolvedMode)

	{
		TableConfig tableConfig = tableConfigs.get(tableNode.getPathInSchema());
		List<SchemaNode> nodes = new ArrayList<>();
		collectFields(
			nodes,
			Arrays.asList(tableNode.getTablePrimaryKeyNodes()),
			tableNode.getTableOccurrenceRootNode().getNodeChildren(),
			permissions,
			dataSet,
			resolvedMode,
			tableConfig);
		if (tableConfig != null)
			nodes = tableConfig.filterAndSort(tableNode, nodes);
		return nodes;
	}
	private void writeHeader(Sheet sheet, List<SchemaNode> nodes)
	{
		Row header = sheet.createRow(0);
		for (int i = 0; i < nodes.size(); i++)
		{
			SchemaNode field = nodes.get(i);
			Cell cell = header.createCell(i + 1);
			cell.setCellStyle(headerCellStyle);
			cell.setCellValue(field.getLabel(Locale.getDefault()));
		}
	}

	private int writeExtraRight(
		Sheet sheet,
		SchemaNode tableNode,
		SessionPermissions permissions,
		TableConfig tableConfig,
		List<Adaptation> records,
		List<SchemaNode> visibleColumns)
	{
		return writeExtra(
			sheet,
			tableNode,
			permissions,
			tableConfig,
			records,
			visibleColumns,
			extraRightLabel,
			extraRightCellStyle,
			1);
	}

	private int writeExtra(
		Sheet sheet,
		SchemaNode tableNode,
		SessionPermissions permissions,
		TableConfig tableConfig,
		List<Adaptation> records,
		List<SchemaNode> visibleColumns,
		UnaryFunction<Adaptation, String> label,
		CellStyle cellStyle,
		int rowNum)
	{
		Locale locale = Locale.getDefault();
		for (Adaptation record : records)
		{
			AccessPermission ap = permissions.getAdaptationAccessPermission(record);
			if (ap.isHidden())
				continue;
			ValueContext recordContext = record.createValueContext();
			Row row = sheet.createRow(rowNum);
			int colCount = 0;
			Cell cell = row.createCell(colCount++);
			cell.setCellValue(label.evaluate(record));
			cell.setCellStyle(cellStyle);
			for (SchemaNode node : visibleColumns)
			{
				cell = row.createCell(colCount++);
				ap = permissions.getNodeAccessPermission(node, record);
				if (ap.isHidden())
					cell.setCellStyle(hiddenCellStyle);
				else
				{
					cell.setCellValue(
						getValue(
							record,
							node,
							useNodeLabels,
							recordContext,
							locale,
							isResolvedMode,
							tableConfig));
					cell.setCellStyle(cellStyle);
				}
			}
			rowNum++;
		}
		return rowNum;
	}

	private int writeExtraLeft(
		Sheet sheet,
		SchemaNode tableNode,
		SessionPermissions permissions,
		TableConfig tableConfig,
		List<Adaptation> removed,
		List<SchemaNode> visibleColumns,
		int startRowNum)
	{
		return writeExtra(
			sheet,
			tableNode,
			permissions,
			tableConfig,
			removed,
			visibleColumns,
			extraLeftLabel,
			extraLeftCellStyle,
			startRowNum);
	}

	private void writeUpdated(
		Sheet sheet,
		SchemaNode tableNode,
		SessionPermissions permissions,
		TableConfig tableConfig,
		List<DifferenceBetweenOccurrences> updates,
		List<SchemaNode> visibleColumns,
		int startRowNum)
	{
		int rowNum = startRowNum;
		Locale locale = Locale.getDefault();
		for (DifferenceBetweenOccurrences update : updates)
		{
			Adaptation leftRecord = update.getOccurrenceOnLeft();
			AccessPermission lap = permissions.getAdaptationAccessPermission(leftRecord);
			Adaptation rightRecord = update.getOccurrenceOnRight();
			AccessPermission rap = permissions.getAdaptationAccessPermission(rightRecord);
			if (lap.isHidden() && rap.isHidden())
				continue;
			ValueContext leftRecordContext = leftRecord.createValueContext();
			ValueContext rightRecordContext = rightRecord.createValueContext();
			int colCount = 0;
			Row leftRow = sheet.createRow(rowNum++);
			Cell lcell = leftRow.createCell(colCount);
			lcell.setCellValue(leftLabel.evaluate(leftRecord));
			lcell.setCellStyle(leftCellStyle);
			Row rightRow = sheet.createRow(rowNum++);
			Cell rcell = rightRow.createCell(colCount);
			rcell.setCellValue(rightLabel.evaluate(rightRecord));
			rcell.setCellStyle(rightCellStyle);
			colCount++;
			for (SchemaNode node : visibleColumns)
			{
				lcell = leftRow.createCell(colCount);
				rcell = rightRow.createCell(colCount);
				String leftValue = null;
				boolean leftHidden = false;
				if (lap.isHidden()
					|| permissions.getNodeAccessPermission(node, leftRecord).isHidden())
				{
					lcell.setCellStyle(hiddenCellStyle);
					leftHidden = true;
				}
				else
				{
					leftValue = getValue(
						leftRecord,
						node,
						useNodeLabels,
						leftRecordContext,
						locale,
						isResolvedMode,
						tableConfig);
					lcell.setCellValue(leftValue);
					lcell.setCellStyle(leftCellStyle);
				}
				String rightValue = null;
				boolean rightHidden = false;
				if (rap.isHidden()
					|| permissions.getNodeAccessPermission(node, rightRecord).isHidden())
				{
					rcell.setCellStyle(hiddenCellStyle);
					rightHidden = true;
				}
				else
				{
					rightValue = getValue(
						rightRecord,
						node,
						useNodeLabels,
						rightRecordContext,
						locale,
						isResolvedMode,
						tableConfig);
					rcell.setCellValue(rightValue);
					rcell.setCellStyle(rightCellStyle);
				}
				if ((tableConfig == null || tableConfig.isDifferent(
					Path.SELF.add(node.getPathInAdaptation()),
					leftRecord,
					rightRecord,
					isResolvedMode)) && !Objects.equals(leftValue, rightValue))
				{
					if (!leftHidden)
					{
						lcell.setCellStyle(ldiffCellStyle);
					}
					if (!rightHidden)
					{
						rcell.setCellStyle(rdiffCellStyle);
					}
				}
				colCount++;
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static String getValue(
		Adaptation record,
		SchemaNode node,
		boolean useNodeLabels,
		ValueContext recordContext,
		Locale locale,
		boolean resolvedMode,
		TableConfig tableConfig)
	{
		Object rawValue;
		if (resolvedMode || (tableConfig != null
			&& tableConfig.shouldAlwaysShow(Path.SELF.add(node.getPathInAdaptation()))))
			rawValue = record.get(node);
		else
			rawValue = record.getValueWithoutResolution(node.getPathInAdaptation());

		// TODO: Eventually should be enhanced to handle Complex Types that are Terminal Nodes (i.e. Jave Beans)
		if (rawValue instanceof List)
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Object val : (List) rawValue)
			{
				if (!first)
					sb.append("\r\n");
				first = false;
				if (val instanceof GenericComplexObject)
				{
					sb.append(JSONUtil.jsonFor((GenericComplexObject) val, node));
				}
				else if (val instanceof MediaType)
				{
					sb.append(((MediaType) val).getAttachment());
				}
				else
				{
					sb.append(node.displayOccurrence(val, useNodeLabels, recordContext, locale));
				}
			}
			return sb.toString();
		}
		else
		{
			if (AdaptationValue.INHERIT_VALUE.equals(rawValue))
			{
				return "<inherited>";
			}
			if (rawValue instanceof MediaType)
			{
				return ((MediaType) rawValue).getAttachment();
			}
			return node.displayOccurrence(rawValue, useNodeLabels, recordContext, locale);
		}
	}

	private static void collectFields(
		List<SchemaNode> fields,
		List<SchemaNode> tablePrimaryKeyNodes,
		SchemaNode[] children,
		SessionPermissions permissions,
		Adaptation dataSet,
		boolean resolvedMode,
		TableConfig tableConfig)
	{
		for (SchemaNode child : children)
		{
			if (child.isComplex())
			{
				if (child.getMaxOccurs() == 1)
				{
					collectFields(
						fields,
						tablePrimaryKeyNodes,
						child.getNodeChildren(),
						permissions,
						dataSet,
						resolvedMode,
						tableConfig);
				}
				else if (shouldAddField(
					child,
					tablePrimaryKeyNodes,
					permissions,
					dataSet,
					resolvedMode,
					tableConfig))
					fields.add(child);
			}
			else if (child.isTerminalValue() && !child.isAssociationNode() && !child.isSelectNode())
			{
				if (shouldAddField(
					child,
					tablePrimaryKeyNodes,
					permissions,
					dataSet,
					resolvedMode,
					tableConfig))
					fields.add(child);
			}
		}
	}

	private static boolean shouldAddField(
		SchemaNode child,
		List<SchemaNode> tablePrimaryKeyNodes,
		SessionPermissions permissions,
		Adaptation dataSet,
		boolean resolvedMode,
		TableConfig tableConfig)
	{
		Path path = Path.SELF.add(child.getPathInAdaptation());
		boolean alwaysShow = tablePrimaryKeyNodes.contains(child)
			|| (tableConfig != null && tableConfig.shouldAlwaysShow(path));
		if (!alwaysShow)
		{
			boolean ignore = false;
			SchemaNodeDefaultView dv = child.getDefaultViewProperties();
			if (dv != null && dv.isHidden())
			{
				ignore = true;
			}
			else if (child.isAutoIncrement())
			{
				ignore = true;
			}
			if (ignore)
			{
				if (tableConfig != null && !tableConfig.initialized)
					tableConfig.addFieldToIgnore(path);
				return false;
			}
			else if (permissions.getNodeAccessPermission(child, dataSet).isHidden())
			{
				return false;
			}
			else if (!resolvedMode && child.isValueFunction())
			{
				return false;
			}
		}
		else if (tableConfig != null && child.isValueFunction()
			&& tableConfig.ignoredFieldsToShow.contains(path))
		{
			tableConfig.setNeedsResolution(true);
		}
		return true;
	}

	@Override
	public void init(DataContext dataContext, Repository repo)
	{
		super.init(dataContext, repo);
		S selection = context != null ? context.getEntitySelection() : null;
		if (selection != null)
		{
			if (rightDataSpace == null)
				rightDataSpace = selection.getDataspace();
			if (leftDataSpace == null)
				leftDataSpace = rightDataSpace.getParent();
			// Only continue to gather configuration if we're not explicitly generating a data space report
			if (compareReportLevel != CompareReportLevel.DATA_SPACE)
			{
				if (dataSetName == null && selection instanceof DatasetEntitySelection)
					dataSetName = ((DatasetEntitySelection) selection).getDataset()
						.getAdaptationName();
				// Only continue to gather configuration if we're not explicitly generating a data set report
				if (compareReportLevel != CompareReportLevel.DATA_SET)
				{
					if (tablePath == null && selection instanceof TableEntitySelection)
						tablePath = ((TableEntitySelection) selection).getTable().getTablePath();
					// Only continue to gather configuration if we're not explicitly generating a table report
					if (compareReportLevel != CompareReportLevel.TABLE)
					{
						if (leftRecord == null && rightRecord == null)
						{
							if (selection instanceof TableViewEntitySelection)
							{
								List<Adaptation> pair = AdaptationUtil.getRecords(
									((TableViewEntitySelection) selection).getSelectedRecords()
										.execute());
								if (pair.size() == 2)
								{
									leftRecord = pair.get(0);
									rightRecord = pair.get(1);
								}
							}
							else if (selection instanceof RecordEntitySelection)
							{
								rightRecord = ((RecordEntitySelection) selection).getRecord();
								leftRecord = AdaptationUtil
									.getRecordFromOtherDataSpace(rightRecord, leftDataSpace);
							}
						}
					}
				}
			}
		}
		else if (dataContext != null)
		{
			if (leftDataSpace == null && dataContext.isVariableDefined(LEFT_DATASPACE_PARAM))
			{
				String dataSpaceId = dataContext.getVariableString(LEFT_DATASPACE_PARAM);
				if (dataSpaceId != null)
				{
					leftDataSpace = repo.lookupHome(HomeKey.parse(dataSpaceId));
				}
			}
			if (rightDataSpace == null && dataContext.isVariableDefined(RIGHT_DATASPACE_PARAM))
			{
				String dataSpaceId = dataContext.getVariableString(RIGHT_DATASPACE_PARAM);
				if (dataSpaceId != null)
				{
					rightDataSpace = repo.lookupHome(HomeKey.parse(dataSpaceId));
				}
			}
			if (dataSetName == null && dataContext.isVariableDefined(DATASET_PARAM))
			{
				String dsName = dataContext.getVariableString(DATASET_PARAM);
				dataSetName = dsName != null ? AdaptationName.forName(dsName) : null;
			}
			if (tablePath == null && dataContext.isVariableDefined(TABLE_PARAM))
			{
				String tableName = dataContext.getVariableString(TABLE_PARAM);
				tablePath = tableName != null ? Path.parse(tableName) : null;
			}
		}
	}
	public File getFile()
	{
		return file;
	}

	public static class TableConfig
	{
		private final Path tablePath;
		private final Path pathToRoot;
		private final List<Path> matchOnFields;
		private String filter;
		private final List<Path> fieldsToIgnore = new ArrayList<>();
		private final List<Path> ignoredFieldsToShow = new ArrayList<>();
		private boolean initialized = false;
		private boolean needsResolution = false;

		public TableConfig(Path tablePath, Path pathToRoot, List<Path> matchOnFields)
		{
			super();
			this.tablePath = tablePath;
			this.pathToRoot = pathToRoot;
			if (matchOnFields == null)
				this.matchOnFields = Collections.<Path> emptyList();
			else
				this.matchOnFields = matchOnFields;
		}
		public Path getPathToRoot()
		{
			return pathToRoot;
		}
		public Path getTablePath()
		{
			return tablePath;
		}
		public List<Path> getMatchOnFields()
		{
			return matchOnFields;
		}
		public String getFilter()
		{
			return filter;
		}
		public void setFilter(String filter)
		{
			this.filter = filter;
		}
		public List<Path> getFieldsToIgnore()
		{
			return fieldsToIgnore;
		}
		public TableConfig addFieldToIgnore(Path fieldsToIgnore)
		{
			this.fieldsToIgnore.add(fieldsToIgnore);
			return this;
		}
		public List<Path> getIgnoredFieldsToShow()
		{
			return ignoredFieldsToShow;
		}
		public TableConfig addIgnoredFieldToShow(Path field)
		{
			this.ignoredFieldsToShow.add(field);
			return this;
		}
		public boolean isNeedsResolution()
		{
			return needsResolution;
		}
		public void setNeedsResolution(boolean needsResolution)
		{
			this.needsResolution = needsResolution;
		}
		public boolean shouldAlwaysShow(Path path)
		{
			if (path.equals(pathToRoot))
				return true;
			if (matchOnFields.contains(path))
				return true;
			if (ignoredFieldsToShow.contains(path))
				return true;
			return false;
		}
		private List<SchemaNode> filterAndSort(SchemaNode rootNode, List<SchemaNode> nodes)
		{
			this.initialized = true;
			List<SchemaNode> result = new ArrayList<>();
			if (pathToRoot != null)
				result.add(rootNode.getNode(pathToRoot));
			for (Path matchPath : matchOnFields)
			{
				result.add(rootNode.getNode(matchPath));
			}
			for (SchemaNode schemaNode : nodes)
			{
				Path path = Path.SELF.add(schemaNode.getPathInAdaptation());
				if (!path.equals(pathToRoot) && !matchOnFields.contains(path)
					&& (!fieldsToIgnore.contains(path) || ignoredFieldsToShow.contains(path)))
					result.add(schemaNode);
			}
			return result;
		}
		/**
		 * Return a unique key on which you can sort and match on a record. Best practice is that
		 * match fields are part of uniqueness constraints so that there will not be more than one
		 * match.
		 * 
		 * @param record
		 *            record to be sorted
		 * @return Object that uniquely identifies the record
		 */
		public Object getMatchKey(Adaptation record)
		{
			if (matchOnFields.isEmpty())
				return GenerateCompareReport.getMatchKey(null, record);
			Object[] result = new Object[matchOnFields.size()];
			for (int i = 0; i < matchOnFields.size(); i++)
			{
				Path path = matchOnFields.get(i);
				result[i] = record.get(path);
			}
			return Arrays.asList(result);
		}
		/**
		 * Given a difference between two records, process whether or not to include in report based on
		 * potential field differences to ignore.
		 * @param diff the actual difference between the two records
		 * @param resolvedMode the service setting for resolved mode
		 * @return true if this is a difference to include
		 */
		public boolean isDifferent(DifferenceBetweenOccurrences diff, boolean resolvedMode)
		{
			if (diff.isEmpty())
				return false;
			List<NodeDifference> diffs = diff.getNodeDifferences();
			Adaptation leftRecord = diff.getOccurrenceOnLeft();
			Adaptation rightRecord = diff.getOccurrenceOnRight();
			for (NodeDifference nodeDifference : diffs)
			{
				Path path = Path.SELF.add(nodeDifference.getLeftValuePath());
				if (isDifferent(path, leftRecord, rightRecord, resolvedMode))
				{
					Locale locale = Locale.getDefault();
					LoggingCategory.getKernel()
						.debug(
							MessageFormat.format(
								DIFF_LOG_MSG,
								leftRecord.getContainerTable().getTableNode().getLabel(locale),
								leftRecord.getLabel(locale),
								rightRecord.getLabel(locale),
								nodeDifference.toString()));
					return true;
				}
			}
			return false;
		}
		/**
		 * Given a node difference -- the difference for a particular field when comparing two records -- answer if this
		 * difference is one to include.
		 * @param path the field on which there is a difference
		 * @param leftRecord record on left
		 * @param rightRecord record on right
		 * @param resolvedMode the service setting for resolved mode
		 * @return true if the difference should be included
		 */
		public boolean isDifferent(
			Path path,
			Adaptation leftRecord,
			Adaptation rightRecord,
			boolean resolvedMode)
		{
			if (path.equals(pathToRoot))
				return false;
			if (ignoredFieldsToShow.contains(path))
				return true;
			if (fieldsToIgnore.contains(path))
				return false;
			// is we need to use resolved mode, but didn't mean to include all computed fields...
			if (!resolvedMode && needsResolution
				&& leftRecord.getSchemaNode().getNode(path).isValueFunction())
				return false;
			return true;
		}
	}

	@Override
	public List<File> getFiles()
	{
		return file == null ? null : Collections.singletonList(getFile());
	}

	public CompareReportLevel getCompareReportLevel()
	{
		return compareReportLevel;
	}

	public void setCompareReportLevel(CompareReportLevel compareReportLevel)
	{
		this.compareReportLevel = compareReportLevel;
	}
}
