package com.orchestranetworks.ps.scripttask;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * 
 * Set the label of a data space.
 * <pre>{@code
   <bean className="com.orchestranetworks.ps.workflow.scripttask.SetDataSpaceLabelScript">
           <documentation xml:lang="en-US">
                <label>Set the label of a data space</label>
                <description>
                  Set the label of a data space
                </description>
            </documentation>
            <properties>
                <property name="branch" input="true">
                    <documentation xml:lang="en-US">
                        <label>Data space</label>
                        <description>
                           The data space where the value to set is located
                        </description>
                    </documentation>
                </property>
                  <property name="label" input="true">
                    <documentation xml:lang="en-US">
                        <label>Label</label>
                        <description>
                           The new label to set
                        </description>
                    </documentation>
                </property>
            </properties>
        </bean>
 * }</pre>
 * @author MCH
 */
public class SetDataSpaceLabelScript extends ScriptTaskBean
{

	/** The label. */
	private String label;

	/** The data space. */
	private String branch;

	/*
	 * @see
	 * com.orchestranetworks.workflow.ScriptTaskBean#executeScript(com.orchestranetworks.workflow.
	 * ScriptTaskBeanContext)
	 */
	@Override
	public void executeScript(final ScriptTaskBeanContext pContext) throws OperationException
	{
		AdaptationHome home = AdaptationUtil.getDataSpaceOrThrowOperationException(
			pContext.getRepository(),
			this.branch);
		pContext.getRepository().setDocumentationLabel(
			home,
			this.label,
			Locale.getDefault(),
			pContext.getSession());
	}

	/**
	 * Gets the data space.
	 *
	 * @return the data space
	 */
	public String getDataSpace()
	{
		return this.branch;
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	public String getLabel()
	{
		return this.label;
	}

	/**
	 * Sets the data space.
	 *
	 * @param dataSpace the new data space
	 */
	public void setDataSpace(final String dataSpace)
	{
		this.branch = dataSpace;
	}

	/**
	 * Sets the label.
	 *
	 * @param label the new label
	 */
	public void setLabel(final String label)
	{
		this.label = label;
	}
}
