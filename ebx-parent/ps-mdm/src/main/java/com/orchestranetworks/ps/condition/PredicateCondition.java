package com.orchestranetworks.ps.condition;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.workflow.*;

/**
 * Configured with a name of a dataspace, a dataset, xpath for identifying a single record and an xpath predicate,
 * this condition bean will return true if the record matches the predicate.
 */
/**
 * 
 * Verify an xpath condition on a record.
 * 
 * <pre>{@code
 * 		<bean className="com.orchestranetworks.ps.condition.PredicateCondition">
 *           <documentation xml:lang="en-US">
 *               <label>Predicate is true?</label>
 *               <description>
 *                   Test if a XPath predicate is true
 *               </description>
 *           </documentation>
 *           <properties>
 *               <property name="dataspace" input="true">
 *                   <documentation xml:lang="en-US">
 *                       <label>Data space</label>
 *                       <description>
 *                          The data space where to apply the predicate
 *                       </description>
 *                   </documentation>
 *               </property>
 *               <property name="dataset" input="true">
 *                   <documentation xml:lang="en-US">
 *                       <label>Data set</label>
 *                       <description>
 *                           The data where to apply the predicate
 *                       </description>
 *                   </documentation>
 *              </property>
 *              <property name="xpath" input="true">
 *                   <documentation xml:lang="en-US">
 *                        <label>Records XPath expression</label>
 *                       <description>
 *                          XPath to the record where to apply the predicate
 *                       </description>
 *                   </documentation>
 *               </property>
 *               <property name="predicate" input="true">
 *               	<documentation xml:lang="en-US">
 *                       <label>Predicate</label>
 *                       <description>
 *                       	Predicate to test
 *                       </description>
 *                   </documentation>
 *               </property>
 *           </properties>
 *       </bean>
 * 
 * }</pre>
 * 
 * @author MCH
 */

public class PredicateCondition extends ConditionBean
{
	private String dataspace;
	private String dataset;
	private String xpath;
	private String predicate;

	@Override
	public final boolean evaluateCondition(ConditionBeanContext aContext) throws OperationException
	{
		final Adaptation record = AdaptationUtil.getOneRecordOrThrowOperationException(
			aContext.getRepository(),
			dataspace,
			dataset,
			xpath);
		try
		{
			return record.matches(predicate);
		}
		catch (Exception ex)
		{
			throw OperationException.createError(ex.getMessage());
		}
	}

	public final String getDataset()
	{
		return this.dataset;
	}

	public final String getDataspace()
	{
		return this.dataspace;
	}

	public final String getPredicate()
	{
		return this.predicate;
	}

	public final String getXpath()
	{
		return this.xpath;
	}

	public final void setDataset(String dataset)
	{
		this.dataset = dataset;
	}

	public final void setDataspace(String dataspace)
	{
		this.dataspace = dataspace;
	}

	public final void setPredicate(String predicate)
	{
		this.predicate = predicate;
	}

	public final void setXpath(String xpath)
	{
		this.xpath = xpath;
	}
}
