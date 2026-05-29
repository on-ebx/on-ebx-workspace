package com.orchestranetworks.ps.deepcopy;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 * Manage the internal structure of a primary key for Deep Copy pkey resolution purposes.
 * This Object is identified by ebx5 ObjectKey "", and is passed from the deep copy service to the SetPrimaryKeyDataModifier 
 * module, which applies the primary key update to one or more fields in the target copy.
 */
public class PrimaryKeyDataSpec
{
	private AdaptationTable tableObj;
	private PrimaryKey sourcePkey; // optional ebx5 format, for string representation
	private BeanSpec[] beanSpecs;
	public BeanSpec[] getBeanSpecs()
	{
		return beanSpecs;
	}

	public AdaptationTable getTableObj()
	{
		return tableObj;
	}

	public PrimaryKeyDataSpec(Adaptation aSourceRecord, String rootName)
	{
		tableObj = aSourceRecord.getContainerTable();
		Path[] pkeyFieldPaths = tableObj.getPrimaryKeySpec();
		beanSpecs = new BeanSpec[pkeyFieldPaths.length];
		sourcePkey = tableObj.computePrimaryKey(aSourceRecord);
		for (int i = 0; i < pkeyFieldPaths.length; i++)
		{
			beanSpecs[i] = new BeanSpec(
				Path.SELF.add(pkeyFieldPaths[i]), // convert absolute paths to relative
				aSourceRecord);
		}
	}

	/**
	 * Test whether this primary key already exists in the table.
	 * @param aContext Readable context for the current table.
	 */
	public boolean isNewPrimaryKey(ValueContext aContext)
	{
		Object[] newPkeyValues = new Object[beanSpecs.length];
		for (int i = 0; i < newPkeyValues.length; i++)
		{
			newPkeyValues[i] = beanSpecs[i].getPkeyFieldValue();
		}
		PrimaryKey targetPkey = tableObj.computePrimaryKey(newPkeyValues);
		Adaptation existingRecord = tableObj.lookupAdaptationByPrimaryKey(targetPkey);
		return (existingRecord == null);
	}

	public void updateTargetPrimaryKey(ValueContextForUpdate aContext)
	{
		for (int i = 0; i < beanSpecs.length; i++)
		{
			BeanSpec bean = beanSpecs[i];
			aContext.setValue(bean.getPkeyFieldValue(), bean.getPkeyFieldPath());
		}
	}

	String errorInfo()
	{
		return String.format(
			"[Primary Key Spec] Error for PKey %s in table %s",
			sourcePkey,
			tableObj.toString());
	}

	public static class BeanSpec
	{
		private Path pkeyFieldPath; // key fields for PKey, discovered from source table
		private Object pkeyFieldValue; // values of each PKey, default from source record, final
										// value applied to target
		private String pkeyBeanName;
		private SchemaTypeName pkeyBeanType;
		private SchemaNode childNode;

		public SchemaNode getChildNode()
		{
			return childNode;
		}
		public void setChildNode(SchemaNode childNode)
		{
			this.childNode = childNode;
		}
		public Path getPkeyFieldPath()
		{
			return pkeyFieldPath;
		}
		public Object getPkeyFieldValue()
		{
			return pkeyFieldValue;
		}
		public void setPkeyFieldValue(Object value)
		{
			pkeyFieldValue = value;
		}
		public String getPkeyBeanName()
		{
			return pkeyBeanName;
		}
		public SchemaTypeName getPkeyBeanType()
		{
			return pkeyBeanType;
		}

		public BeanSpec(Path path, Adaptation aSourceRecord)
		{
			pkeyFieldPath = path;
			pkeyFieldValue = aSourceRecord.get(pkeyFieldPath);
			SchemaNode recNode = aSourceRecord.getSchemaNode();
			childNode = recNode.getNode(pkeyFieldPath);
			pkeyBeanName = childNode.getLabel(Locale.getDefault());
			pkeyBeanType = childNode.getXsTypeName();
		}

		public Path getSimplePath()
		{
			return Path.SELF.add(pkeyFieldPath.getLastStep());
		}
	}
}
