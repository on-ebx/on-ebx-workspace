package com.orchestranetworks.ps.scripttask;

import java.util.Iterator;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationHome;
import com.orchestranetworks.addon.dint.DataIntegrationException;
import com.orchestranetworks.addon.dint.DataIntegrationExecutionResults;
import com.orchestranetworks.addon.dint.DataIntegrationExecutor;
import com.orchestranetworks.addon.dint.TableMappingResult;
import com.orchestranetworks.addon.dint.template.SQLImportTemplateSpec;
import com.orchestranetworks.instance.Repository;
import com.orchestranetworks.ps.util.AdaptationUtil;
import com.orchestranetworks.ps.util.ExceptionUtils;
import com.orchestranetworks.service.LoggingCategory;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.workflow.ScriptTaskBean;
import com.orchestranetworks.workflow.ScriptTaskBeanContext;

/**
 * This task requires that you add the following xml in the module.xml
 * 
 * <pre>{@code
 		<bean className="com.orchestranetworks.ps.scripttask.DataIntegrationImportSQLScriptTask">
		    <documentation xml:lang="en-US">
			<label>Data Integration Import SQL - using template </label>
			<description>
			    Script Task to Import SQL with a saved import SQL template using Data Integration add-on
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

public class DataIntegrationImportSQLScriptTask extends ScriptTaskBean
{
	private String dataSpaceName;
	private String dataSetName;
	private String templateId;

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		try
		{
			LoggingCategory.getWorkflow().info(
					"DataIntegrationImportSQLScriptTask started for {template id = " + templateId + ", target dataSpace = " + dataSpaceName + ", dataset = " + dataSetName + "}");

			Repository repository = context.getRepository();
			AdaptationHome dataSpace = AdaptationUtil.getDataSpaceOrThrowOperationException(repository, dataSpaceName);
			Adaptation dataSet = AdaptationUtil.getDataSetOrThrowOperationException(dataSpace, dataSetName);
			
			SQLImportTemplateSpec sqlImportTemplateSpec = new SQLImportTemplateSpec(templateId, context.getSession());
			
			sqlImportTemplateSpec.setDataset(dataSet);
			DataIntegrationExecutionResults sqlImportResults = DataIntegrationExecutor.getInstance().execute(sqlImportTemplateSpec);

			for (Iterator<TableMappingResult> iterator = sqlImportResults.get(); iterator.hasNext();)
			{
				TableMappingResult result = (TableMappingResult) iterator.next();
				LoggingCategory.getWorkflow()
					.info(
						result.getTargetTable().getLabel() + " in dataspace " + dataSpace + " - " + "[Processed = " + result.getNumberOfProcessedRecords() + ",Inserted = "
							+ result.getNumberOfInsertedRecords() + ",Updated = " + result.getNumberOfUpdatedRecords() + ",Deleted = " + result.getNumberOfDeletedRecords() + ",Unchanged = "
							+ result.getNumberOfUnchangedRecords() + ",Failed = " + result.getNumberOfFailedRecords() + "]");
			}

			LoggingCategory.getWorkflow().info("DataIntegrationImportSQLScriptTask completed....");

		}
		catch (DataIntegrationException e)
		{
			LoggingCategory.getWorkflow().error("DataIntegrationImportSQLScriptTask could not complete successfully. Exception : " + ExceptionUtils.getStackTraceAsString(e));
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

	public String getTemplateId()
	{
		return templateId;
	}

	public void setTemplateId(String templateId)
	{
		this.templateId = templateId;
	}

}
