package com.orchestranetworks.ps.service;

import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.lang3.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.schema.definition.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;

/**
 * Export data set to an excel workbook. Give details of tables and fields and
 * recurse into reachable data sets.
 */
public class GenerateDataDictionary extends AbstractFileDownloadUserService<DatasetEntitySelection>
	implements DBMappingPaths
{
	private File file;
	private static final String[] ELEMENTS_HEADERS = new String[] { "Data Set", "Table", "Group",
			"Field Order", "Field", "Label", "Description", "PK", "Unique", "FK", "CA", "Type",
			"Max Length", "Min Occurs", "Max Occurs", "Enumeration", "Other Facets",
			"Default Value", "Auto-Increment", "Inherited", "Information", "History", "Replication",
			"DB Column", "Hidden" };
	private static final String[] TABLES_HEADERS = new String[] { "Data Set", "Table", "Label",
			"Primary Key", "Description", "Constraints", "Information", "DB Table" };
	private static final String[] COMPLEX_TYPES_HEADERS = new String[] { "Name", "Label", "Field",
			"Description", "Type", "Max Length", "Min Occurs", "Max Occurs", "Enumeration",
			"Other Facets", "Default Value", "Auto-Increment" };
	private static final String[] SIMPLE_TYPES_HEADERS = new String[] { "Name", "Label",
			"Description", "BaseType", "Max Length", "Min Occurs", "Max Occurs", "Enumeration",
			"Other Facets", "Default Value", "Auto-Increment" };

	private Adaptation dbMappingDataSet;

	private void createFile(Adaptation dataSet) throws OperationException
	{
		String label = dataSet.getLabel(Locale.getDefault());
		label = StringUtils.substringBefore(label, " Data Set");
		try
		{
			this.file = File.createTempFile(label + " Data Dictionary", ".xlsx");
		}
		catch (IOException e)
		{
			throw OperationException.createError("Failed to create file for data dictionary", e);
		}
	}

	protected XSSFWorkbook generateLibrary(Adaptation dataSet) throws OperationException
	{
		this.dbMappingDataSet = AdminUtil
			.getDatabaseMappingDataSet(dataSet.getHome().getRepository());
		XSSFWorkbook workbook = new XSSFWorkbook();
		Sheet tables = workbook.createSheet("Tables");
		writeHeaders(tables, TABLES_HEADERS);
		Sheet elements = workbook.createSheet("Elements");
		writeHeaders(elements, ELEMENTS_HEADERS);
		Sheet ctypes = workbook.createSheet("Complex Types");
		writeHeaders(ctypes, COMPLEX_TYPES_HEADERS);
		Sheet stypes = workbook.createSheet("Simple Types");
		writeHeaders(stypes, SIMPLE_TYPES_HEADERS);
		Set<Adaptation> dataSets = new LinkedHashSet<>();
		dataSets.add(dataSet);
		Locale locale = context.getSession().getLocale();
		Set<Adaptation> processedDataSets = new HashSet<>();
		Map<SchemaTypeName, DataSetSchemaNode> sharedTypes = new LinkedHashMap<>();
		while (hasDataSetToProcess(dataSets, processedDataSets))
		{
			List<Adaptation> temp = new ArrayList<>(dataSets);
			dataSets.clear();
			for (Adaptation ds : temp)
			{
				processDataSet(
					ds,
					dataSets,
					processedDataSets,
					elements,
					tables,
					sharedTypes,
					locale);
			}
		}
		writeTypes(sharedTypes, ctypes, stypes, locale);
		return workbook;
	}

	private void writeHeaders(Sheet sheet, String[] headers)
	{
		// write the header row
		Row headerRow = sheet.createRow(0);
		Workbook wb = sheet.getWorkbook();
		Font font = wb.createFont();
		font.setBold(true);
		CellStyle style = wb.createCellStyle();
		style.setFont(font);
		for (int i = 0; i < headers.length; i++)
		{
			Cell cell = headerRow.createCell(i);
			cell.setCellStyle(style);
			cell.setCellValue(headers[i]);
		}
	}

	private void processDataSet(
		Adaptation dataSet,
		Set<Adaptation> dataSets,
		Set<Adaptation> processedDataSets,
		Sheet elements,
		Sheet tables,
		Map<SchemaTypeName, DataSetSchemaNode> sharedTypes,
		Locale locale)
	{
		if (!processedDataSets.add(dataSet))
			return;
		List<AdaptationTable> tableNodes = AdaptationUtil.getAllTables(dataSet);
		Map<AdaptationTable, String> tableNames = AdaptationUtil
			.deriveTableNames(tableNodes, "/", context.getSession().getLocale());
		String dataSetName = dataSet.getLabel(Locale.getDefault());
		writeElements(elements, dataSetName, tableNodes, dataSets, sharedTypes, tableNames, locale);
		writeTables(tables, dataSetName, tableNodes, tableNames, locale);
	}

	private static boolean hasDataSetToProcess(
		Set<Adaptation> dataSets,
		Set<Adaptation> processedDataSets)
	{
		return !processedDataSets.containsAll(dataSets);
	}

	private static void writeTypes(
		Map<SchemaTypeName, DataSetSchemaNode> types,
		Sheet ctypes,
		Sheet stypes,
		Locale locale)
	{
		int crow = 1;
		int srow = 1;
		for (Map.Entry<SchemaTypeName, DataSetSchemaNode> entry : types.entrySet())
		{
			DataSetSchemaNode dsnode = entry.getValue();
			SchemaNode node = dsnode.schemaNode;
			if (node instanceof TNode)
			{
				Type type = ((TNode) node).getType();
				if (type.isComplexType())
				{
					// populate complex type sheet
					ComplexType ctype = (ComplexType) type;
					List<Path> fields = ctype.getSubTypePathList();
					for (Path path : fields)
					{
						Row row = ctypes.createRow(crow++);
						/*
						 * private static final String[] COMPLEX_TYPES_HEADERS =
						 * new String[] { "Name", "Label", "Field",
						 * "Description", "Type", "Max Length", "Min Occurs",
						 * "Max Occurs", "Enumeration", "Default Value",
						 * "Auto-Increment" };
						 */
						int col = 0;
						addCell(row, col++, ctype.getTypeName().getName()); // typename
						addCell(row, col++, ctype.getLabel().formatMessage(locale)); // label
						addCell(row, col++, path.format()); // field
						SchemaNode fieldNode = node.getNode(Path.SELF.add(path));
						addCell(row, col++, fieldNode.getDescription(locale)); // description
						addCommonFieldColumns(
							row,
							col,
							getType(fieldNode, dsnode.dataSet, locale),
							fieldNode,
							node.getTableNode());
					}
				}
				else
				{
					// populate simple type sheet
					Row row = stypes.createRow(srow++);
					/*
					 * private static final String[] SIMPLE_TYPES_HEADERS = new
					 * String[] { "Name", "Label", "Description", "BaseType",
					 * "Max Length", "Min Occurs", "Max Occurs", "Enumeration"
					 * };
					 */
					int col = 0;
					addCell(row, col++, type.getTypeName().getName()); // typename
					addCell(row, col++, type.getLabel().formatMessage(locale)); // label
					SchemaNode fieldNode = node;
					addCell(row, col++, fieldNode.getDescription(locale)); // description
					addCommonFieldColumns(
						row,
						col,
						type.getBaseTypeName().getName(),
						fieldNode,
						null);
				}
			}
		}
	}

	private static int addCommonFieldColumns(
		Row row,
		int col,
		String type,
		SchemaNode fieldNode,
		SchemaNode tableNode)
	{
		addCell(row, col++, type); // basetype
		SchemaFacetMaxLength maxLen = fieldNode.getFacetMaxLength();
		addCell(row, col++, maxLen != null ? maxLen.getValue() : ""); // maxLength
		addCell(row, col++, fieldNode.getMinOccurs()); // minOccurs
		int maxOccurs = fieldNode.getMaxOccurs();
		if (maxOccurs == Integer.MAX_VALUE || maxOccurs == 0)
			addCell(row, col++, "unbounded"); // maxOccurs
		else
			addCell(row, col++, maxOccurs); // maxOccurs
		addCell(row, col++, getEnumeration(fieldNode)); // enumeration
		addCell(row, col++, getOtherFacets(fieldNode)); // other facets
		addCell(row, col++, getDefaultValue(fieldNode)); // default value
		addCell(row, col++, getAutoIncrement(type, fieldNode)); // auto-increment?
		return col;
	}

	@SuppressWarnings("rawtypes")
	private static Object getDefaultValue(SchemaNode fieldNode)
	{
		Object defaultValue = fieldNode.getDefaultValue();
		if (defaultValue instanceof Collection && ((Collection) defaultValue).isEmpty())
			return null;
		return defaultValue;
	}

	private static Boolean getAutoIncrement(String type, SchemaNode fieldNode)
	{
		if ("Integer".equals(type))
			return ((TNode) fieldNode).isAutoIncrement();
		else
			return null;
	}

	private void writeTables(
		Sheet tablesSheet,
		String dataSetName,
		List<AdaptationTable> tables,
		Map<AdaptationTable, String> tableNames,
		Locale locale)
	{
		int rowNum = tablesSheet.getLastRowNum();
		for (AdaptationTable table : tables)
		{
			Row row = tablesSheet.createRow(++rowNum);
			writeTable(row, dataSetName, table, tableNames, locale);
		}
	}

	private void writeTable(
		Row row,
		String dataSetName,
		AdaptationTable table,
		Map<AdaptationTable, String> tableNames,
		Locale locale)
	{
		/*
		 * private static final String[] TABLES_HEADERS = new String[] {
		 * "Data Set", "Table", "Label", "Primary Key", "Description",
		 * "Constraints", "Information", "DB Table" };
		 */
		int col = 0;
		SchemaNode tableNode = table.getTableNode();
		addCell(row, col++, dataSetName); // data set
		addCell(row, col++, table.getTablePath().format()); // table
		addCell(row, col++, tableNames.get(table)); // label
		addCell(row, col++, getKeySpec(table)); // PK
		addCell(row, col++, tableNode.getDescription(locale)); // description
		addCell(row, col++, getOtherFacets(tableNode)); // constraints
		addCell(row, col++, getRules(tableNode)); // information
		addCell(row, col++, getDBTable(table)); // DB Table
	}

	private String getDBTable(AdaptationTable table)
	{
		Adaptation dbMappingTableRecord = getDBMappingTable(table);
		if (dbMappingTableRecord == null)
			return null;
		Adaptation replica = CollectionUtils
			.getFirstOrNull(AdaptationUtil.getLinkedRecordList(dbMappingTableRecord, DBM_REPLICAS));
		if (replica == null)
			return null;
		return replica.getString(Path.parse("./tableNameInDB"));
	}

	private Adaptation getDBMappingTable(AdaptationTable table)
	{
		AdaptationTable dbDMTable = dbMappingDataSet.getTable(DB_MAPPING_DM_PATH);
		String dmName = table.getContainerAdaptation()
			.getSchemaLocation()
			.toStringInfo(Locale.getDefault())
			.replace("Module:", "module:");
		Adaptation dmRecord = dbDMTable.lookupFirstRecordMatchingPredicate(
			AdaptationUtil.createQueryPredicate(
				dbDMTable.getTableOccurrenceRootNode(),
				DBM_DM_PATH,
				dmName,
				null));
		if (dmRecord == null)
			return null;
		AdaptationTable dbMappingTable = dbMappingDataSet.getTable(DB_MAPPING_TABLE_PATH);
		return dbMappingTable.lookupFirstRecordMatchingPredicate(
			AdaptationUtil.createQueryPredicate(
				dbMappingTable.getTableOccurrenceRootNode(),
				new Path[] { DBM_TABLE_PATH, DBM_DATA_MODEL },
				new Object[] { table.getTablePath().format(),
						dmRecord.getOccurrencePrimaryKey().format() },
				null));
	}

	private Map<Path, String> getDBColumns(AdaptationTable table)
	{
		Adaptation dbMappingTableRecord = getDBMappingTable(table);
		if (dbMappingTableRecord == null)
			return Collections.emptyMap();
		List<Adaptation> columns = AdaptationUtil
			.getLinkedRecordList(dbMappingTableRecord, DBM_COLUMNS);
		if (columns == null)
			return Collections.emptyMap();
		Map<Path, String> result = new HashMap<>();
		for (Adaptation column : columns)
		{
			String fieldPath = column.getString(DBM_COLUMN_PATH);
			if (fieldPath.endsWith(";"))
				fieldPath = StringUtils.substringBefore(fieldPath, ";");
			result.put(Path.parse(fieldPath), column.getString(DB_COLUMN_NAME));
		}
		List<Adaptation> lists = AdaptationUtil
			.getLinkedRecordList(dbMappingTableRecord, DBM_LISTS);
		if (lists != null)
		{
			for (Adaptation list : lists)
			{
				result.put(Path.parse(list.getString(DBM_LIST_PATH)), getListDBName(list));
			}
		}
		return result;
	}

	private String getListDBName(Adaptation list)
	{
		Adaptation auxTable = AdaptationUtil.followFK(list, DBM_AUX_TABLE_PATH);
		Adaptation column = CollectionUtils
			.getFirstOrNull(AdaptationUtil.getLinkedRecordList(auxTable, DBM_COLUMNS));
		if (column != null)
		{
			Adaptation replica = CollectionUtils
				.getFirstOrNull(AdaptationUtil.getLinkedRecordList(auxTable, DBM_REPLICAS));
			String prefix = "";
			if (replica != null)
				prefix = replica.getString(DB_TABLE_NAME) + ":";
			return prefix + column.getString(DB_COLUMN_NAME);
		}
		return null;
	}

	private static String getKeySpec(AdaptationTable table)
	{
		StringBuilder sb = new StringBuilder();
		for (Path path : table.getPrimaryKeySpec())
		{
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(path.format());
		}
		return sb.toString();
	}

	private void writeElements(
		Sheet elements,
		String dataSetName,
		List<AdaptationTable> tables,
		Set<Adaptation> dataSets,
		Map<SchemaTypeName, DataSetSchemaNode> sharedTypes,
		Map<AdaptationTable, String> tableNames,
		Locale locale)
	{
		for (AdaptationTable table : tables)
		{
			writeElementsForTable(
				elements,
				dataSetName,
				table,
				dataSets,
				sharedTypes,
				tableNames,
				locale);
		}
	}

	private void writeElementsForTable(
		Sheet sheet,
		String dataSetName,
		AdaptationTable table,
		Set<Adaptation> dataSets,
		Map<SchemaTypeName, DataSetSchemaNode> sharedTypes,
		Map<AdaptationTable, String> tableNames,
		Locale locale)
	{
		List<SchemaNode> fields = new ArrayList<>();
		collectFields(
			fields,
			table.getTableNode().getTableOccurrenceRootNode().getNodeChildren(),
			table.getContainerAdaptation(),
			sharedTypes);
		Map<Path, String> dbColumns = getDBColumns(table);
		int rowNum = sheet.getLastRowNum();
		for (int i = 0; i < fields.size(); i++)
		{
			SchemaNode field = fields.get(i);
			Row row = sheet.createRow(++rowNum);
			writeElementsForField(
				row,
				dataSetName,
				table,
				field,
				i + 1,
				isPK(table, field),
				dataSets,
				tableNames,
				dbColumns,
				locale);
		}
	}

	private static boolean isPK(AdaptationTable table, SchemaNode field)
	{
		Path[] keyPaths = table.getPrimaryKeySpec();
		Path fieldPath = field.getPathInAdaptation();
		for (Path path : keyPaths)
		{
			if (fieldPath.equals(path))
				return true;
		}
		return false;
	}

	private static void collectFields(
		List<SchemaNode> fields,
		SchemaNode[] children,
		Adaptation dataSet,
		Map<SchemaTypeName, DataSetSchemaNode> sharedTypes)
	{
		for (SchemaNode child : children)
		{
			SchemaTypeName stn = child.getXsTypeName();
			if (stn != null && stn.getBuiltInTypeLabel() == null && !sharedTypes.containsKey(stn))
			{
				sharedTypes.put(stn, new DataSetSchemaNode(child, dataSet));
			}
			if (child.isComplex())
			{
				SchemaNode[] subchildren = child.getNodeChildren();
				collectFields(fields, subchildren, dataSet, sharedTypes);
			}
			else if (child.isTerminalValue() || child.isAssociationNode())
			{
				fields.add(child);
			}
		}
	}

	private static void writeElementsForField(
		Row row,
		String dataSetName,
		AdaptationTable table,
		SchemaNode fieldNode,
		int index,
		boolean isPK,
		Set<Adaptation> dataSets,
		Map<AdaptationTable, String> tableNames,
		Map<Path, String> dbColumns,
		Locale locale)
	{
		/*
		 * private static final String[] ELEMENTS_HEADERS = new String[] {
		 * "Data Set", "Table", "Group", "Field Order", "Field", "Label",
		 * "Description", "PK", "Unique", "FK", "CA", "Type", "Max Length",
		 * "Min Occurs", "Max Occurs", "Enumeration", "Other Facets",
		 * "Default Value", "Inherited", "Information", "History",
		 * "Replication", "Hidden" };
		 */
		int col = 0;
		TNode tnode = fieldNode instanceof TNode ? (TNode) fieldNode : null;
		Path path = fieldNode.getPathInAdaptation();
		addCell(row, col++, dataSetName); // data set
		addCell(row, col++, tableNames.get(table)); // table
		String group = null;
		if (path.getSize() > 1)
		{
			Path groupPath = path.getPathWithoutLastStep();
			TNode groupNode = tnode != null ? tnode.getParent() : null;
			group = getGroup(groupPath, groupNode, locale);
		}
		addCell(row, col++, group); // group
		addCell(row, col++, index); // field order
		addCell(row, col++, path.format()); // field path
		addCell(row, col++, fieldNode.getLabel(locale)); // label
		addCell(row, col++, fieldNode.getDescription(locale)); // description
		addCell(row, col++, isPK); // pk
		addCell(row, col++, tnode != null && tnode.isPartOfUniquenessConstraint(), false); // unique
		SchemaFacetTableRef ref = fieldNode.getFacetOnTableReference();
		if (ref != null)
		{
			AdaptationReference container = ref != null ? ref.getContainerReference() : null;
			if (container != null)
			{
				HomeKey branch = ref.getContainerHome();
				AdaptationHome dataSpace = table.getContainerAdaptation().getHome();
				if (branch != null)
				{
					dataSpace = dataSpace.getRepository().lookupHome(branch);
				}
				Adaptation dataSet = dataSpace.findAdaptationOrNull(container);
				if (dataSet != null)
					dataSets.add(dataSet);
			}
		}
		addCell(row, col++, ref != null, false); // fk
		addCell(row, col++, (fieldNode.isValueFunction() && !fieldNode.isAssociationNode()), false); // ca
		col = addCommonFieldColumns(
			row,
			col,
			getType(fieldNode, table.getContainerAdaptation(), locale),
			fieldNode,
			null);
		SchemaInheritanceProperties ip = fieldNode.getInheritanceProperties();
		String inode = null;
		if (ip != null)
		{
			SchemaNode in = ip.getInheritedNode();
			SchemaNode itn = in.getTableNode();
			inode = itn.getLabel(locale) + "/" + in.getLabel(locale);
		}
		addCell(row, col++, inode); // inherited
		addCell(row, col++, getRules(fieldNode)); // information
		addCell(row, col++, !fieldNode.isHistoryDisabled(), false); // history
		addCell(row, col++, tnode == null || !tnode.isReplicationDisabled(), false); // replication
		addCell(row, col++, dbColumns.get(fieldNode.getPathInAdaptation())); // db column
		SchemaNodeDefaultView dvp = fieldNode.getDefaultViewProperties();
		boolean hidden = dvp != null ? dvp.isHidden() : false;
		addCell(row, col++, hidden, false); // hidden
	}

	private static Object getOtherFacets(final SchemaNode fieldNode)
	{
		StringBuilder sb = new StringBuilder();
		for (Iterator<SchemaFacet> iterator = fieldNode.getFacets(); iterator.hasNext();)
		{
			SchemaFacet facet = iterator.next();
			if (facet.isFacetMaxLength() || facet.isEnumerationList())
				continue; // covered as a primary column
			if (facet.isFacetMinLength())
				sb.append("Length >= ").append(((SchemaFacetMinLength) facet).getValue()).append(
					"; ");
			else if (facet.isFacetLength())
				sb.append("Length = ").append(((SchemaFacetLength) facet).getValue()).append("; ");
			else if (facet.isFacetTotalDigits())
				sb.append("TotalDigits <= ")
					.append(((SchemaFacetTotalDigits) facet).getTotalDigits())
					.append("; ");
			else if (facet.isFacetFractionDigits())
				sb.append("FractionDigits <= ")
					.append(((SchemaFacetFractionDigits) facet).getFractionDigits())
					.append("; ");
			else if (facet.isFacetPattern())
				sb.append("Pattern = ")
					.append(((SchemaFacetPattern) facet).getPatternString())
					.append("; ");
			else if (facet.isFacetBoundaryMinInclusive())
				sb.append("Value >= ").append(getBound((SchemaFacetBoundary) facet)).append("; ");
			else if (facet.isFacetBoundaryMinExclusive())
				sb.append("Value > ").append(getBound((SchemaFacetBoundary) facet)).append("; ");
			else if (facet.isFacetBoundaryMaxInclusive())
				sb.append("Value <= ").append(getBound((SchemaFacetBoundary) facet)).append("; ");
			else if (facet.isFacetBoundaryMaxExclusive())
				sb.append("Value < ").append(getBound((SchemaFacetBoundary) facet)).append("; ");
			else
			{
				String facetString = facet.toString();

				// In this section, replaced checking for "FacetGeneric[" which was used in EBX5, with "constraint[" which is now used in EBX6
				// 
				// if (!"FacetOnMandatoryField[]".equals(facetString)  
				// commented out the above line for now -- not sure exactly what this was checking for in EBX5 or what the equivalent is in EBX6
				if (!"constraint[com.orchestranetworks.cce.validation.constraint.RtcFacetUniqueOnTable]"
					.equals(facetString)
					// this is replaces the EBX5 com.orchestranetworks.cce.validation.constraint.j class in EBX6
					&& !Strings.CI.startsWith(facetString, "constraint[com.onwbp."))
				{
					facetString = StringUtils.substringBefore(
						StringUtils.substringAfter(facetString, "constraint["),
						"]");
					if (!StringUtils.isEmpty(facetString))
						sb.append(facetString).append("; ");
				}
			}
		}
		return sb.toString();
	}

	private static String getBound(SchemaFacetBoundary boundary)
	{
		Object bound = boundary.getBound();
		if (bound != null)
		{
			if (bound instanceof Date)
				return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
					.format((Date) bound);
			return bound.toString();
		}
		Path path = boundary.getPath();
		return path != null ? path.format() : "?";
	}

	private static String getGroup(Path groupPath, TNode groupNode, Locale locale)
	{
		if (groupNode != null)
		{
			TNode[] path = new TNode[groupPath.getSize()];
			for (int i = path.length; --i >= 0;)
			{
				path[i] = groupNode;
				groupNode = groupNode.getParent();
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < path.length; i++)
			{
				if (i > 0)
					sb.append("/");
				sb.append(path[i].getLabel(locale));
			}
			return sb.toString();
		}
		else
			return groupPath.format();
	}

	private static Object getRules(SchemaNode node)
	{
		SchemaNodeInformation info = node.getInformation();
		if (info != null)
			return info.getInformation();
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static String getEnumeration(SchemaNode fieldNode)
	{
		SchemaFacetEnumeration enumeration = fieldNode.getFacetEnumeration();
		if (enumeration != null)
		{
			List values = enumeration.getValues();
			if (values != null) // static enumeration
			{
				return values.toString();
			}
		}
		return "";
	}

	private static String getType(SchemaNode fieldNode, Adaptation dataSet, Locale locale)
	{
		String baseName;
		SchemaNode tableNode = AdaptationUtil.getTableNodeForRelated(fieldNode, dataSet);
		if (tableNode != null)
		{
			baseName = tableNode.getLabel(locale);
		}
		else
		{
			SchemaTypeName typeName = fieldNode.getXsTypeName();
			UserMessage builtIn = typeName != null ? typeName.getBuiltInTypeLabel() : null;
			if (builtIn != null)
			{
				baseName = builtIn.formatMessage(locale);
			}
			else
			{
				baseName = typeName != null ? typeName.toString() : "String";
			}
		}
		if (fieldNode.getMaxOccurs() != 1)
		{
			return "List<" + baseName + ">";
		}
		else
			return baseName;
	}

	private static void addCell(Row row, int index, Boolean value, boolean showFalse)
	{
		String svalue = null;
		if (value.booleanValue())
			svalue = "Yes";
		else if (showFalse)
			svalue = "No";
		addCell(row, index, svalue);
	}

	private static void addCell(Row row, int index, Object value)
	{
		Cell cell = row.createCell(index);
		if (value instanceof Number)
			cell.setCellValue(((Number) value).doubleValue());
		else if (value instanceof Boolean)
			cell.setCellValue(((Boolean) value).booleanValue() ? "Yes" : "No");
		else
			cell.setCellValue(value == null ? "" : value.toString());
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		Adaptation dataSet = context.getEntitySelection().getDataset();
		XSSFWorkbook workbook = generateLibrary(dataSet);
		createFile(dataSet);
		saveWorkbook(workbook, file);
	}

	private static class DataSetSchemaNode
	{
		private final SchemaNode schemaNode;
		private final Adaptation dataSet;
		public DataSetSchemaNode(SchemaNode schemaNode, Adaptation dataSet)
		{
			super();
			this.schemaNode = schemaNode;
			this.dataSet = dataSet;
		}
	}

	@Override
	public List<File> getFiles()
	{
		return file == null ? null : Collections.singletonList(file);
	}
}
