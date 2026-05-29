/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.Adaptation;
import com.orchestranetworks.ps.util.AdaptationUtil;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * Extract a value from a record field and assign it to a Data Context output variable.
 * 
 * <pre>{@code
  		<bean className="com.orchestranetworks.ps.scripttask.SetContextFromRecordScriptTask">
            <documentation xml:lang="en-US">
                <label>Set Context from Record value</label>
                <description>
                    Extract a value from a record field and assign it to a Data Context output
                </description>
            </documentation>
            <properties>
                <property name="dataspace" input="true">
                    <documentation xml:lang="en-US">
                        <label>Data space</label>
                        <description>
                           The data space where the value to set is located
                        </description>
                    </documentation>
                </property>
                <property name="dataset" input="true">
                    <documentation xml:lang="en-US">
                        <label>Data set</label>
                        <description>
                            The data set where the value to set is located
                        </description>
                    </documentation>
                </property>
                <property name="record" input="true">
                    <documentation xml:lang="en-US">
                         <label>Record XPath</label>
                        <description>
                           XPath to the record containing the value to extract
                        </description>
                    </documentation>
                </property>
                <property name="field" input="true">
                    <documentation xml:lang="en-US">
                         <label>XPath to Field within Record</label>
                        <description>
                           XPath within the record to the field containing the value
                        </description>
                    </documentation>
                </property>
                <property name="outputValue" output="true">
                    <documentation xml:lang="en-US">
                        <label>Output Value</label>
                        <description>
                           Value to set in the selected field
                        </description>
                    </documentation>
                </property>
            </properties>
        </bean>
 * } </pre>
 */

public class SetContextFromRecordScriptTask extends ScriptTaskBean
{
	private String dataspace;
	private String dataset;
	private String record;
	private String field;
	private String outputValue;

	@Override
	public void executeScript(ScriptTaskBeanContext aContext) throws OperationException
	{
		final Adaptation adaptation = AdaptationUtil.getOneRecordOrThrowOperationException(
			aContext.getRepository(),
			dataspace,
			dataset,
			record);
		this.outputValue = adaptation.getString(Path.parse(field));
	}

	public String getDataspace()
	{
		return dataspace;
	}

	public void setDataspace(String dataspace)
	{
		this.dataspace = dataspace;
	}

	public String getDataset()
	{
		return dataset;
	}

	public void setDataset(String dataset)
	{
		this.dataset = dataset;
	}

	public String getRecord()
	{
		return record;
	}

	public void setRecord(String record)
	{
		this.record = record;
	}

	public String getField()
	{
		return field;
	}

	public void setField(String field)
	{
		this.field = field;
	}

	public String getOutputValue()
	{
		return this.outputValue;
	}

	public void setOutputValue(String outputValue)
	{
		this.outputValue = outputValue;
	}
}
