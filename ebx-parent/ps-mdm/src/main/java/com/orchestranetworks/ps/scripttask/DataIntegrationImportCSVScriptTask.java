package com.orchestranetworks.ps.scripttask;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.addon.dint.*;
import com.orchestranetworks.addon.dint.template.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * This task requires that you add the following xml in the module.xml
 * 
 * <pre>{@code
 		<bean className="com.orchestranetworks.ps.scripttask.DataIntegrationImportCSVScriptTask">
		    <documentation xml:lang="en-US">
			<label>Data Integration Import CSV - using template </label>
			<description>
			    Script Task to Import data with a saved CSV template using Data Integration add-on
			</description>
		    </documentation>
		    <properties>
			<property name="dataSpaceName" input="true">
			    <documentation xml:lang="en-US">
				<label>Data space</label>
				<description>
				   The Target data space into where the Import CSV service is to be executed
				</description>
			    </documentation>
			</property>
			<property name="dataSetName" input="true">
			    <documentation xml:lang="en-US">
				<label>Data set</label>
				<description>
				    The target data set where the mport CSV service is to be executed
				</description>
			    </documentation>
			</property>
			<property name="filePath" input="true">
			    <documentation xml:lang="en-US">
				<label>File Path</label>
				<description>
				   The absolute file path for the file to be imported.
				</description>
			    </documentation>
			</property>
			<property name="templateId" input="true">
			    <documentation xml:lang="en-US">
				<label>Template Id</label>
				<description>
				   The Template UUID as defined in the Data Exchange (New) configuration
				</description>
			    </documentation>
			</property>
			
		    </properties>
		</bean>
 * }</pre>
 * @author TIBCO
 */

public class DataIntegrationImportCSVScriptTask extends ScriptTaskBean
{
	private String dataSpaceName;
	private String dataSetName;
	private String filePath;
	private String templateId;

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		try
		{
			LoggingCategory.getWorkflow()
				.info(
					"DataIntegrationImportCSVScriptTask started for {template id = " + templateId
						+ ", filePath = " + filePath + " source dataSpace = " + dataSpaceName
						+ ", dataset = " + dataSetName + "}");

			AdaptationHome dataSpace = AdaptationUtil
				.getDataSpaceOrThrowOperationException(context.getRepository(), dataSpaceName);

			Adaptation dataSet = AdaptationUtil
				.getDataSetOrThrowOperationException(dataSpace, dataSetName);

			File sourceFile = new File(filePath);

			
		CSVImportTemplateSpec templateSpec = new CSVImportTemplateSpec(
				templateId,
				sourceFile,
				context.getSession());

			templateSpec.setDataset(dataSet); // target dataset
			DataIntegrationExecutionResults importResults = DataIntegrationExecutor.getInstance()
				.execute(templateSpec);

			for (Iterator<TableMappingResult> iterator = importResults.get(); iterator.hasNext();)
			{
				TableMappingResult result = iterator.next();
				LoggingCategory.getWorkflow()
					.info(
						result.getTargetTable().getLabel() + " in dataspace " + dataSpace + " - "
							+ "[Processed = " + result.getNumberOfProcessedRecords()
							+ ",Inserted = " + result.getNumberOfInsertedRecords() + ",Updated = "
							+ result.getNumberOfUpdatedRecords() + ",Deleted = "
							+ result.getNumberOfDeletedRecords() + ",Unchanged = "
							+ result.getNumberOfUnchangedRecords() + ",Failed = "
							+ result.getNumberOfFailedRecords() + "]");
			}

			LoggingCategory.getWorkflow().info("DataIntegrationImportCSVScriptTask completed....");

		}
		catch (DataIntegrationException e)
		{
			LoggingCategory.getWorkflow()
				.error(
					"DataIntegrationImportCSVScriptTask could not complete successfully. Exception : "
						+ ExceptionUtils.getStackTraceAsString(e));
			throw OperationException.createError(e);
		}
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
	public String getFilePath()
	{
		return filePath;
	}

	public void setFilePath(String filePath)
	{
		this.filePath = filePath;
	}

	public String getTemplateId()
	{
		return templateId;
	}

	public void setTemplateId(String templateId)
	{
		this.templateId = templateId;
	}

}
