package com.orchestranetworks.ps.util;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;

public class AdaptationToJAXB
{
	private Set<Class<?>> classes = new HashSet<>();
	private String packageName;

	public AdaptationToJAXB(String packageName)
	{
		this.packageName = packageName;
	}

	public Object generateJAXB(Adaptation adaptation) throws Exception
	{
		String typeName = getTypeName(adaptation);
		List<SchemaNode> properties = getProperties(adaptation);

		// instantiate the jaxb object
		Class<?> jaxbClass = ClassLoaderUtils.loadClass(packageName.concat(".".concat(typeName)));
		classes.add(jaxbClass);
		Object jaxbObject = jaxbClass.getDeclaredConstructor().newInstance();

		populateObject(adaptation, properties, jaxbObject);
		return null;
	}

	private void populateObject(
		Adaptation adaptation,
		List<SchemaNode> properties,
		Object jaxbObject)
	{
		for (SchemaNode property : properties)
		{
			String type = getType(property);
			/*Object value = */getPropertyValue(adaptation, property);
			switch (type)
			{
			case "String":

				break;

			default:
				break;
			}
		}

	}

	private String getType(SchemaNode property)
	{
		List<String> types = SchemaUtils.getTypesForNode(property);
		return types.get(0);
	}

	private List<SchemaNode> getProperties(Adaptation adaptation)
	{
		SchemaNode[] properties = adaptation.getSchemaNode().getNodeChildren();
		return Arrays.asList(properties);
	}

	private String getTypeName(Adaptation adaptation)
	{
		return adaptation.getContainerTable().getTablePath().getLastStep().format();
	}

	private Object getPropertyValue(Adaptation adaptation, SchemaNode property)
	{
		return adaptation.get(property);

	}

}
