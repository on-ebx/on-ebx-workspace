package com.orchestranetworks.ps.scripttask;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.addon.dataexchange.transformation.*;
import com.orchestranetworks.addon.dex.*;
import com.orchestranetworks.addon.dex.common.generation.*;
import com.orchestranetworks.addon.dex.configuration.*;
import com.orchestranetworks.addon.dex.mapping.*;
import com.orchestranetworks.addon.dex.result.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * <pre>{@code
		<bean className="com.orchestranetworks.ps.scripttask.DataExchangeTransferDataScriptTask">
		    <documentation xml:lang="en-US">
				<label>Data Exchange Transfer Data</label>
				<description>
					Script Task to Transfer data between tables with the given parameters using Data Exchange
				</description>
			</documentation>
			<properties>
		          <property name="dataSpaceName" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Source Dataspace id</label>
		                  <description>
		                     Source dataspace. 
		                  </description>
		              </documentation>
		          </property>
		          <property name="dataSetName" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Source Dataset</label>
		                  <description>
		                     Source Dataset
		                  </description>
		              </documentation>
		          </property>
		          
		          <property name="targetDataSpaceName" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Target Dataspace id</label>
		                  <description>
		                     Target Dataspace
		                  </description>
		              </documentation>
		          </property>
		          <property name="targetDataSetName" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Target Dataset id</label>
		                  <description>
		                     Target Dataset
		                  </description>
		              </documentation>
		          </property>
		          <property name="sourceTargetTablePaths" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Source, Target tables path</label>
		                  <description>
		                     Comma delimited for Source and Target Table path and semicolon delimiter for each pair		
		                     /root/sourceTable1{filters},/root/targetTable1;/root/sourceTable2,/root/targetTable2
		                  </description>
		              </documentation>
		          </property>
		          
		          <property name="sourceApplicationLogicalName" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Source application logical name</label>
		                  <description>
		                     Name of an application to be used as Source from the Data Exchange configuration.
		                  </description>
		              </documentation>
		          </property>
		          <property name="targetApplicationLogicalName" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Target application logical name</label>
		                  <description>
		                     Name of an application to be used as Target from the Data Exchange configuration.
		                  </description>
		              </documentation>
		          </property>
		        <property name="importMode" input="true">
		            <documentation xml:lang="en-US">
		                <label>Transfer operation mode</label>
						<description>
						   Defines the mode for transferring the data. option are "UPDATE_AND_INSERT" (default), "INSERT_ONLY", "UPDATE_ONLY", "REPLACE_ALL_CONTENT", "UNKNOWN". 
						</description>
		            </documentation>
		        </property>          
		        <property name="emptyOrNullValueIgnored" input="true">
		              <documentation xml:lang="en-US">
		                <label>Ignore the empty or null values</label>
						<description>
						   The existing record is not updated with null and empty values from the source table (Default is True). 
						</description>
		             </documentation>
		        </property>          
		      </properties>
		</bean>
		
		}</pre>
 * @author mickaelgermemont, noiritabera
 *
 */
public class DataExchangeTransferDataScriptTask extends ScriptTaskBean
{
	private String sourceApplicationLogicalName;
	private String targetApplicationLogicalName;
	private String dataSpaceName;
	private String dataSetName;
	private Boolean emptyOrNullValueIgnored;
	private String targetDataSpaceName;
	private String targetDataSetName;

	/**
	 * example: 
	 * /root/sourceTable1{is_valid = 'true' AND sourceSystem = 'EIDR'},/root/targetTable1;/root/sourceTable2,/root/targetTable2
	 */
	private String sourceTargetTablePaths;

	private String delimiter = DEFAULT_DELIMITER;
	private String tablePairDelimiter = DEFAULT_TABLE_PAIR_DELIMITER;
	private ImportMode importModeEnum = ImportMode.UPDATE_AND_INSERT;

	private static final String DEFAULT_DELIMITER = ",";
	private static final String DEFAULT_TABLE_PAIR_DELIMITER = ";";
	public static final String[] SOURCE_PREDICATE_BRACKETS = { "{", "}" };

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		try
		{
			Repository repository = context.getRepository();
			AdaptationHome dataSpace = AdaptationUtil
				.getDataSpaceOrThrowOperationException(repository, dataSpaceName);
			Adaptation dataSet = AdaptationUtil
				.getDataSetOrThrowOperationException(dataSpace, dataSetName);

			AdaptationHome targetDataSpace = AdaptationUtil
				.getDataSpaceOrThrowOperationException(repository, targetDataSpaceName);
			Adaptation targetDataSet = AdaptationUtil
				.getDataSetOrThrowOperationException(targetDataSpace, targetDataSetName);

			ArrayList<SourceTargetSpec> sourceTargetSpecList = buildSourceTargetTableMap(
				sourceTargetTablePaths);
			//Staging Source Tables
			List<EBXTable> sourceEBXTables = new ArrayList<EBXTable>();
			Map<EBXTable, TableFilter> sourceTableFilters = new HashMap<EBXTable, TableFilter>();
			TableMappingList<EBXField, EBXField> tableMappingList = new TableMappingList<>();

			for (SourceTargetSpec spec : sourceTargetSpecList)
			{
				AdaptationTable stgSourceTable = dataSet
					.getTable(Path.parse(spec.getSourceTableName()));
				EBXTable stgEbxTable = new EBXTable(stgSourceTable);
				sourceEBXTables.add(stgEbxTable);

				TableFilter tableFilter = new TableFilter();
				String predicate = spec.getPredicate();
				if (predicate != null && predicate.length() > 0)
				{
					tableFilter.setPredicate(predicate);
				}
				sourceTableFilters.put(stgEbxTable, tableFilter);

				//Target Table wherein data to be transformed
				AdaptationTable targetTable = targetDataSet
					.getTable(Path.parse(spec.getTargetTableName()));
				EBXTable targetEbxTable = new EBXTable(targetTable);
				TableMapping<EBXField, EBXField> tableMapping = new TableMapping<>(
					stgEbxTable,
					targetEbxTable);
				tableMappingList.add(tableMapping);
			}
			TransferConfigurationSpec transferConfig = new TransferConfigurationSpec(
				dataSet,
				sourceEBXTables,
				sourceTableFilters,
				targetDataSet,
				context.getSession());
			transferConfig.setImportMode(importModeEnum);
			if (emptyOrNullValueIgnored != null)
			{
				transferConfig.setEmptyOrNullValueIgnored(emptyOrNullValueIgnored);
			}
			CommonApplication sourceApplication = new CommonApplication(
				getSourceApplicationLogicalName(),
				ApplicationType.EBX);
			CommonApplication targetApplication = new CommonApplication(
				getTargetApplicationLogicalName(),
				ApplicationType.EBX);

			ApplicationMapping<EBXField, EBXField> applicationMapping = ApplicationMappingHelperFactory
				.getApplicationMappingForTransferHelper()
				.getApplicationMapping(
					transferConfig,
					sourceApplication,
					targetApplication,
					tableMappingList);
			DataExchangeSpec dataExchangeSpec = new DataExchangeSpec();
			dataExchangeSpec.setApplicationMapping(applicationMapping);
			dataExchangeSpec.setConfigurationSpec(transferConfig);
			DataExchangeResult transferResults = DataExchangeServiceFactory.getDataExchangeService()
				.execute(dataExchangeSpec);
			StringBuilder resultMessage = new StringBuilder();

			for (@SuppressWarnings("unchecked")
			Iterator<TransferResult> iterator = (Iterator<TransferResult>) transferResults
				.getResults(); iterator.hasNext();)
			{
				Result result = iterator.next();
				// iterate the errors and build a string with them. later throw it an an operational exception
				for (UserMessage userMessage : result.getErrorMessages().values())
				{
					resultMessage.append(userMessage.formatMessage(Locale.getDefault()))
						.append("\n");
				}
				// iterating the warnings and logging them to the kernel log.
				for (Iterator<UserMessage> iterator2 = result.getWarningMessages(); iterator2
					.hasNext();)
				{
					UserMessage warningMessage = iterator2.next();
					LoggingCategory.getKernel().warn(warningMessage);
				}
			}

			if (resultMessage.length() > 0)
			{
				throw OperationException.createError(resultMessage.toString());
			}
		}
		catch (Exception e)
		{
			throw OperationException.createError(e);
		}
	}

	public ArrayList<SourceTargetSpec> buildSourceTargetTableMap(String sourceTargetTablePaths)
		throws OperationException
	{
		ArrayList<SourceTargetSpec> sourceTargetSpecList = new ArrayList<SourceTargetSpec>();

		String[] sourceSpecs = sourceTargetTablePaths.split(DEFAULT_TABLE_PAIR_DELIMITER);
		for (String sourceSpec : sourceSpecs)
		{
			String sourceTableName = null;
			String predicate = null;
			String targetTableName = null;
			int openBracket = sourceSpec.indexOf(SOURCE_PREDICATE_BRACKETS[0]);
			if (openBracket >= 0)
			{
				int closeBracket = sourceSpec.indexOf(SOURCE_PREDICATE_BRACKETS[1], openBracket);
				if (openBracket == 0 || closeBracket < openBracket)
					throw OperationException.createError("Unmatched predicate brackets");
				int targetDelim = sourceSpec.indexOf(DEFAULT_DELIMITER, closeBracket);
				if (targetDelim < closeBracket + 1)
					throw OperationException.createError("Missing source/target delimiter");
				sourceTableName = sourceSpec.substring(0, openBracket).trim();
				predicate = sourceSpec.substring(openBracket + 1, closeBracket).trim();
				targetTableName = sourceSpec.substring(targetDelim + 1).trim();
			}
			else
			{
				String[] parts = sourceSpec.split(DEFAULT_DELIMITER, 2);
				sourceTableName = parts[0].trim();
				targetTableName = parts[1].trim();
			}
			if (sourceTableName == null || sourceTableName.length() < 1 || targetTableName == null
				|| targetTableName.length() < 1)
				throw OperationException.createError("Source and target tables required");
			SourceTargetSpec spec = new SourceTargetSpec(
				sourceTableName,
				predicate,
				targetTableName);
			sourceTargetSpecList.add(spec);
		}
		return sourceTargetSpecList;
	}

	public String getSourceApplicationLogicalName()
	{
		return sourceApplicationLogicalName;
	}

	public void setSourceApplicationLogicalName(String sourceApplicationLogicalName)
	{
		this.sourceApplicationLogicalName = sourceApplicationLogicalName;
	}

	public String getTargetApplicationLogicalName()
	{
		return targetApplicationLogicalName;
	}

	public void setTargetApplicationLogicalName(String targetApplicationLogicalName)
	{
		this.targetApplicationLogicalName = targetApplicationLogicalName;
	}

	public String getDataSpaceName()
	{
		return dataSpaceName;
	}

	public void setDataSpaceName(String dataSpaceName)
	{
		this.dataSpaceName = dataSpaceName;
	}

	public String getDataSetName()
	{
		return dataSetName;
	}

	public void setDataSetName(String dataSetName)
	{
		this.dataSetName = dataSetName;
	}

	public String getTableXPath()
	{
		// in order to not break existing integrations
		return "";
	}

	public void setTableXPath(String tableXPath)
	{
		// in order to not break existing integrations
	}

	public String getTargetDataSpaceName()
	{
		return targetDataSpaceName;
	}

	public void setTargetDataSpaceName(String targetDataSpaceName)
	{
		this.targetDataSpaceName = targetDataSpaceName;
	}

	public String getTargetDataSetName()
	{
		return targetDataSetName;
	}

	public void setTargetDataSetName(String targetDataSetName)
	{
		this.targetDataSetName = targetDataSetName;
	}

	public String getSourceTargetTablePaths()
	{
		return sourceTargetTablePaths;
	}

	public void setSourceTargetTablePaths(String sourceTargetTablePaths)
	{
		this.sourceTargetTablePaths = sourceTargetTablePaths;
	}

	public String getDelimiter()
	{
		return delimiter;
	}

	public void setDelimiter(String delimiter)
	{
		this.delimiter = delimiter;
	}

	public String getTablePairDelimiter()
	{
		return tablePairDelimiter;
	}

	public void setTablePairDelimiter(String tablePairDelimiter)
	{
		this.tablePairDelimiter = tablePairDelimiter;
	}

	public String getImportMode()
	{
		return getImportModeEnum().getValue();
	}

	public void setImportMode(String importMode)
	{
		if (!StringUtils.isEmpty(importMode))
		{
			this.importModeEnum = ImportMode.valueOf(importMode);
		}

	}

	public ImportMode getImportModeEnum()
	{
		return importModeEnum;
	}

	public Boolean getEmptyOrNullValueIgnored()
	{
		return emptyOrNullValueIgnored;
	}

	public void setEmptyOrNullValueIgnored(Boolean emptyOrNullValueIgnored)
	{
		this.emptyOrNullValueIgnored = emptyOrNullValueIgnored;
	}

	/**
	 * Holder object for a source/target mapping specification.
	 * Correlates a Source table entry with a predicate (can be null) and a table.
	 */
	class SourceTargetSpec
	{
		private String sourceTableName;
		private String predicate;
		private String targetTableName;
		public SourceTargetSpec(String sourceTableName, String predicate, String targetTableName)
		{
			super();
			this.sourceTableName = sourceTableName;
			this.predicate = predicate;
			this.targetTableName = targetTableName;
		}

		public String getSourceTableName()
		{
			return sourceTableName;
		}
		public String getPredicate()
		{
			return predicate;
		}
		public String getTargetTableName()
		{
			return targetTableName;
		}

	}

}
