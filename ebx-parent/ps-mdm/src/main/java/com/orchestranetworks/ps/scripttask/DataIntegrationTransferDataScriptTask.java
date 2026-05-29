package com.orchestranetworks.ps.scripttask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.misc.*;
import com.orchestranetworks.addon.dint.*;
import com.orchestranetworks.addon.dint.template.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.ExceptionUtils;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * <pre>{@code
		
		<bean
			className="com.orchestranetworks.ps.scripttask.DataIntegrationTransferDataScriptTask">
			<documentation xml:lang="en-US">
				<label>Data Integration - Transfer Data using Template Name</label>
				<description>
					Script Task to Transfer data between tables for a given template Name using Data Integration
				</description>
			</documentation>
			<properties>
				<property name="templateName" input="true">
					<documentation xml:lang="en-US">
						<label>Template Name</label>
						<description>The user friendly Template name as defined in the Data Exchange (New) configuration</description>
					</documentation>
				</property>
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
				 <property name="sourceXPathFilter" input="true">
		              <documentation xml:lang="en-US">
		                  <label>Source XPath Filter</label>
		                  <description>
		                     Source XPath filter to allow transfer of selected records from source to target. for eg. /root/table[./field='a']
		                     
		                     <b>Note</b> :  This feature is available only from EBX 6.1.0 core (and DINT 6.1.0) version
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

			</properties>
		</bean>
		
		
		}</pre>
 * @author TIBCO
 *
 */
public class DataIntegrationTransferDataScriptTask extends ScriptTaskBean
{

	private String templateName; // this is the template name (not UUID), you can get from admin page.

	private String dataSpaceName;
	private String dataSetName;

	private String targetDataSpaceName;
	private String targetDataSetName;

	/**
	 * @since DINT 6.1.0.
	 */
	private String sourceXPathFilter;

	@Override
	public void executeScript(ScriptTaskBeanContext context) throws OperationException
	{
		try
		{
			LoggingCategory.getWorkflow()
				.info(
					"DataIntegrationTransferDataScriptTask started for {template name = "
						+ templateName + ", source dataSpace = " + dataSpaceName + ", dataset = "
						+ dataSetName + ", target dataSpace = " + targetDataSpaceName
						+ ", target dataset = " + targetDataSetName + ", sourceXPathFilter = "
						+ sourceXPathFilter + "}");

			Repository repository = context.getRepository();
			AdaptationHome dataSpace = AdaptationUtil
				.getDataSpaceOrThrowOperationException(repository, dataSpaceName);
			Adaptation dataSet = AdaptationUtil
				.getDataSetOrThrowOperationException(dataSpace, dataSetName);

			AdaptationHome targetDataSpace = AdaptationUtil
				.getDataSpaceOrThrowOperationException(repository, targetDataSpaceName);
			Adaptation targetDataSet = AdaptationUtil
				.getDataSetOrThrowOperationException(targetDataSpace, targetDataSetName);

			EBXTransferTemplateSpec templateSpec = new EBXTransferTemplateSpec(
				TemplateConfig.forDataset(templateName, dataSet),
				context.getSession());

			templateSpec.setSourceDataset(dataSet);

			if (StringUtils.isNotEmpty(sourceXPathFilter))
			{

				String[] sourceXPathFilterList = sourceXPathFilter
					.split(DEFAULT_TABLE_PAIR_DELIMITER);

				for (String sourceXPathFilterString : sourceXPathFilterList)
				{
					Request request = XPathExpressionHelper
						.createRequestForXPath(dataSet, sourceXPathFilterString);
					templateSpec.setRequest(
						XPathExpressionHelper.getTablePathForXPath(sourceXPathFilterString),
						request);
				}
			}

			List<Adaptation> targetDataSets = new ArrayList<Adaptation>();
			targetDataSets.add(targetDataSet);

			templateSpec.addTargetDatasets(targetDataSets);
			DataIntegrationExecutionResults transferResults = DataIntegrationExecutor.getInstance()
				.execute(templateSpec);

			List<String> transferSummaryList = new ArrayList<String>();
			if (null != transferResults)
			{
				Iterator<TableMappingResult> results = (Iterator<TableMappingResult>) transferResults
					.get();

				transferSummaryList = new ArrayList<String>();
				while (results.hasNext())
				{
					TableMappingResult transferResult = (TableMappingResult) results.next();

					String transferSummary = "{Summary of transfer [" + "Source table: "
						+ transferResult.getSourceTable().getPath().format() + " ->  Target table: "
						+ transferResult.getTargetTable().getPath().format() + "] : "
						+ " Processed = " + transferResult.getNumberOfProcessedRecords()
						+ ", Created = " + transferResult.getNumberOfInsertedRecords()
						+ ", Updated = " + transferResult.getNumberOfUpdatedRecords()
						+ ", Deleted = " + transferResult.getNumberOfDeletedRecords()
						+ ", Unchanged = " + transferResult.getNumberOfUnchangedRecords()
						+ ", Failed = " + transferResult.getNumberOfFailedRecords() + "}";

					LoggingCategory.getWorkflow().info(transferSummary);
					transferSummaryList.add(transferSummary);

				}
			}

			LoggingCategory.getWorkflow()
				.info("DataIntegrationTransferDataScriptTask completed....");

		}
		catch (DataIntegrationException e)
		{
			LoggingCategory.getWorkflow()
				.error(
					"DataIntegrationImportSQLScriptTask could not complete successfully. Exception : "
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

	public String getSourceXPathFilter()
	{
		return sourceXPathFilter;
	}

	public void setSourceXPathFilter(String sourceXPathFilter)
	{
		this.sourceXPathFilter = sourceXPathFilter;
	}

	protected static final String DEFAULT_TABLE_PAIR_DELIMITER = ";";

	public String getTemplateName()
	{
		return templateName;
	}

	public void setTemplateName(String templateName)
	{
		this.templateName = templateName;
	}

}
