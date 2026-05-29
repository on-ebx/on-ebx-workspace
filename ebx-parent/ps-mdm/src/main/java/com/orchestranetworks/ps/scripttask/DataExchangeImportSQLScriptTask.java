package com.orchestranetworks.ps.scripttask;

import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.addon.dataexchange.*;
import com.orchestranetworks.addon.dex.*;
import com.orchestranetworks.addon.dex.DataExchangeSpec;
import com.orchestranetworks.addon.dex.common.generation.*;
import com.orchestranetworks.addon.dex.configuration.*;
import com.orchestranetworks.addon.dex.mapping.*;
import com.orchestranetworks.addon.dex.mapping.TableMapping;
import com.orchestranetworks.addon.dex.result.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * This task requires that you add the following xml in the module.xml
 * <pre>{@code
 		<bean className="com.orchestranetworks.ps.scripttask.DataExchangeImportSQLScriptTask">
		    <documentation xml:lang="en-US">
			<label>Data Exchange Import SQL</label>
			<description>
			    Script Task to Import SQL with the given parameters using Data Exchange
			</description>
		    </documentation>
		    <properties>
			<property name="dataSpaceName" input="true">
			    <documentation xml:lang="en-US">
				<label>Data space</label>
				<description>
				   The data space where the Import SQL service is executed
				</description>
			    </documentation>
			</property>
			<property name="dataSetName" input="true">
			    <documentation xml:lang="en-US">
				<label>Data set</label>
				<description>
				    The data set where the Import SQL service is executed
				</description>
			    </documentation>
			</property>
			<property name="tableXPath" input="true">
			    <documentation xml:lang="en-US">
				 <label>Table XPath</label>
				<description>
				   Table XPath on which the Import SQL service is executed
				</description>
			    </documentation>
			</property>
			<property name="sqlDataSourceName" input="true">
				<documentation xml:lang="en-US">
				<label>SQL Data Source Name</label>
				<description>
				   Name of the SQL data source defined in context.xml
				</description>
			    </documentation>
			</property>
			
			<property name="schemaName" input="true">
				<documentation xml:lang="en-US">
				<label>Schema Name</label>
				<description>
				   Name of the schema from where the data needs to be imported
				</description>
			    </documentation>
			</property>
			<property name="sqlTableOrViewName" input="true">
			    <documentation xml:lang="en-US">
				<label>SQL Table or View Name</label>
				<description>
				   The name of the SQL table or view from where the data needs to be imported
				</description>
			    </documentation>
			</property>
			<property name="sqlTableOrViewPredicate" input="true">
			    <documentation xml:lang="en-US">
				<label>SQL Table or View Predicate</label>
				<description>
				   The SQL table or view predicate to apply on the data that needs to be imported
				</description>
			    </documentation>
			</property>
			<property name="importMode" input="true">
			    <documentation xml:lang="en-US">
				<label>Import Mode</label>
				<description>
				   Defines the mode for import. option are "UPDATE_AND_INSERT" (default), "INSERT_ONLY", "UPDATE_ONLY", "REPLACE_ALL_CONTENT", "UNKNOWN".
				</description>
			    </documentation>
			</property>
			<property name="forceImport" input="true">
			    <documentation xml:lang="en-US">
				<label>Force Import</label>
				<description>
				   Specify "true" to disable all triggers and constraints during import. Otherwise, all triggers and constraints are enabled.
				</description>
			    </documentation>
			</property>
			
			
		    </properties>
		</bean>
 * }</pre>
 * @author davidda
 */

public class DataExchangeImportSQLScriptTask extends ScriptTaskBean
{
	private String sqlDataSourceName;
	private String dataSpaceName;
	private String dataSetName;
	private String tableXPath;
	private String schemaName;
	private String sqlTableOrViewName;
	private String sqlTableOrViewPredicate;
	private ImportMode importModeEnum = ImportMode.UPDATE_AND_INSERT;
	private boolean forceImport = false;

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
			AdaptationTable table = dataSet.getTable(Path.parse(tableXPath));

			LoggingCategory.getKernel().debug(
				"[Import SQL] Performed on dataspace: " + dataSpace.getKey().format()
					+ " and dataset: " + dataSet.getAdaptationName().getStringName());

			DataExchangeSpec spec = new DataExchangeSpec();

			EBXTableGeneration ebxTableGen = TableGenerationFactory.getEBXTableGeneration();
			Set<Path> ebxTablePaths = new LinkedHashSet<>();
			ebxTablePaths.add(Path.parse(tableXPath));
			List<EBXTable> ebxTables = ebxTableGen
				.generateTables(dataSet, ebxTablePaths, context.getSession())
				.getTables();

			if (ebxTables == null || ebxTables.isEmpty())
			{
				throw OperationException.createError("Cannot find EBX table!");
			}

			SQLTableGeneration sqlTableGen = TableGenerationFactory.getSQLTableGeneration();
			Set<String> sqlTableNames = new LinkedHashSet<>();
			sqlTableNames.add(getSqlTableOrViewName());
			List<SQLTable> sqlTables = sqlTableGen
				.generateTables(
					getSqlDataSourceName(),
					getSchemaName(),
					null,
					sqlTableNames,
					ServiceType.SQL_IMPORT)
				.getTables();

			SQLTable sqlTable = new SQLTable(
				getSchemaName(),
				getSqlTableOrViewName(),
				sqlTables.get(0).getFields());

			SQLImportConfigurationSpec importSpec = null;
			// if the predicate is empty string or null we don't set it
			if (StringUtils.isEmpty(getSqlTableOrViewPredicate()))
			{
				importSpec = new SQLImportConfigurationSpec(
					table,
					getSqlDataSourceName(),
					sqlTable,
					context.getSession());
			}
			else
			{
				TableFilter tableFilter = new TableFilter();
				tableFilter.setPredicate(getSqlTableOrViewPredicate());
				importSpec = new SQLImportConfigurationSpec(
					table,
					getSqlDataSourceName(),
					sqlTable,
					tableFilter,
					context.getSession());
			}

			importSpec.setImportMode(getImportModeEnum());
			importSpec.setImportForced(isForceImport());
			importSpec.setEmptyOrNullPrimaryKeyChecked(false);
			importSpec.setEmptyOrNullValueIgnored(false);

			@SuppressWarnings("rawtypes")
			Map<TableMapping, FieldMapperDefinition> tableMappingDef = new LinkedHashMap<>();
			TableMapping<SQLField, EBXField> tableMapping = new TableMapping<>(
				sqlTables.get(0),
				ebxTables.get(0));
			tableMappingDef.put(tableMapping, FieldMapperDefinition.MAP_NAME_WITH_INSENSITIVE);
			importSpec.setFieldMapperDefinitions(tableMappingDef);

			@SuppressWarnings("rawtypes")
			ApplicationMapping applicationMapping = ApplicationMappingHelperFactory
				.getApplicationMappingForSQLImportHelper()
				.getApplicationMapping(importSpec);
			spec.setApplicationMapping(applicationMapping);
			spec.setConfigurationSpec(importSpec);

			DataExchangeService service = DataExchangeServiceFactory.getDataExchangeService();

			DataExchangeResult result = service.execute(spec);

			StringBuilder resultMessage = new StringBuilder();
			for (Iterator<UserMessage> iterator = result.getErrorMessages(); iterator.hasNext();)
			{
				UserMessage userMessage = iterator.next();
				resultMessage.append(userMessage.formatMessage(Locale.getDefault())).append("\n");
			}
			if (resultMessage.length() > 0)
			{
				throw OperationException.createError(resultMessage.toString());
			}

			for (Iterator<UserMessage> iterator = result.getWarningMessages(); iterator.hasNext();)
			{
				UserMessage userMessage = iterator.next();
				LoggingCategory.getKernel().warn(userMessage);
			}

		}
		catch (DataExchangeException e)
		{
			throw OperationException.createError(e);
		}
	}
	public String getSqlDataSourceName()
	{
		return sqlDataSourceName;
	}

	public void setSqlDataSourceName(String datasourceName)
	{
		this.sqlDataSourceName = datasourceName;
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
		return tableXPath;
	}

	public void setTableXPath(String tableXPath)
	{
		this.tableXPath = tableXPath;
	}

	public String getSchemaName()
	{
		return schemaName;
	}

	public void setSchemaName(String schemaName)
	{
		this.schemaName = schemaName;
	}

	public String getSqlTableOrViewName()
	{
		return sqlTableOrViewName;
	}

	public void setSqlTableOrViewName(String sqlTableOrViewName)
	{
		this.sqlTableOrViewName = sqlTableOrViewName;
	}

	public String getSqlTableOrViewPredicate()
	{
		return sqlTableOrViewPredicate;
	}

	public void setSqlTableOrViewPredicate(String sqlTableOrViewPredicate)
	{
		this.sqlTableOrViewPredicate = sqlTableOrViewPredicate;
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

	public boolean isForceImport()
	{
		return forceImport;
	}
	public void setForceImport(boolean forceImport)
	{
		this.forceImport = forceImport;
	}

}
