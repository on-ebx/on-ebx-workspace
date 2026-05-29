package com.orchestranetworks.ps.scripttask;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.procedure.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * 
 * Set the value of a node in a record.
 * 
 * <pre>{@code
	<bean className="com.orchestranetworks.ps.workflow.scripttask.SetValueScript">
        <documentation xml:lang="en-US">
            <label>Set value</label>
            <description>
                Set a value to a field
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
            <property name="xpath" input="true">
                <documentation xml:lang="en-US">
                     <label>XPath</label>
                    <description>
                       XPath to the record where the value to set is located
                    </description>
                </documentation>
            </property>
            <property name="path" input="true">
            	<documentation xml:lang="en-US">
                    <label>Path</label>
                    <description>
                       Path to the field to set
                    </description>
                </documentation>
            </property>
            <property name="value" input="true">
                <documentation xml:lang="en-US">
                    <label>Value</label>
                    <description>
                       Value to set in the selected field
                    </description>
                </documentation>
            </property>
        </properties>
    </bean>
 * } </pre>
 * @author MCH
 */

public class SetValueScriptTask extends ScriptTaskBean
{
	private String dataspace;
	private String dataset;
	private String xpath;
	private String path;
	private String value;

	@Override
	public void executeScript(ScriptTaskBeanContext aContext) throws OperationException
	{
		final Adaptation record = AdaptationUtil.getOneRecordOrThrowOperationException(
			aContext.getRepository(),
			dataspace,
			dataset,
			xpath);

		final AdaptationHome home = AdaptationUtil.getDataSpaceOrThrowOperationException(
			aContext.getRepository(),
			dataspace);

		SchemaNode node = null;

		try
		{
			node = record.getSchemaNode().getNode(Path.parse(path));
		}
		catch (Exception ex)
		{
			throw OperationException.createError(ex.getMessage());
		}
		final Procedure proc = new SetValueFromStringProcedure(record, node, value);
		ProcedureExecutor.executeProcedure(proc, aContext.getSession(), home);
	}
	public String getDataspace()
	{
		return this.dataspace;
	}

	public void setDataspace(String dataspace)
	{
		this.dataspace = dataspace;
	}

	public String getDataset()
	{
		return this.dataset;
	}

	public void setDataset(String dataset)
	{
		this.dataset = dataset;
	}

	public String getXpath()
	{
		return this.xpath;
	}

	public void setXpath(String xpath)
	{
		this.xpath = xpath;
	}

	public String getPath()
	{
		return this.path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

}
