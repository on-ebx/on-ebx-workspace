package com.orchestranetworks.ps.ui.annotations;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;

import org.apache.commons.lang3.*;

import com.onwbp.base.schema.definition.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.userservice.*;

public class ConfigurationAnnotationUtils
{
	public static BeanDefinition generateBeanElement(
		Map<Path, FieldMetadata> medata,
		Class<?> beanClass,
		UserServiceObjectContextBuilder aBuilder,
		PropertiesFileListener propertiesListner)
	{
		BeanDefinition def = aBuilder.createBeanDefinition();
		Field[] fields = beanClass.getFields();

		Arrays.sort(fields, 0, fields.length, new Comparator<Field>()
		{
			@Override
			public int compare(Field field1, Field field2)
			{
				Property field1Annotation = field1.getAnnotation(Property.class);
				Property field2Annotation = field2.getAnnotation(Property.class);
				if (field1Annotation == null || field2Annotation == null)
				{
					return 0;
				}
				if (field1Annotation.order() > field2Annotation.order())
				{
					return 1;
				}
				else
				{
					return -1;
				}
			}
		});

		for (int i = 0; i < fields.length; i++)
		{
			getMetadaata(aBuilder, medata, null, def, fields[i], propertiesListner);
		}

		return def;
	}

	public static void getMetadaata(
		UserServiceObjectContextBuilder aBuilder,
		Map<Path, FieldMetadata> metadata,
		Path prefix,
		BeanDefinition def,
		Field field,
		PropertiesFileListener propertiesListner)
	{
		Class<Property> clazz = Property.class;
		Annotation annotation = field.getAnnotation(clazz);
		Property property = null;
		if (annotation instanceof Property)
		{
			property = (Property) annotation;
		}
		if (property == null)
		{
			return;
		}
		if (isPrimitive(property))
		{
			Path elementPath = prefix != null ? prefix.add(Path.parse(field.getName()))
				: Path.parse(field.getName());

			SchemaTypeName propertyType = getSchemaType(property.dataType());

			BeanElement element = def.createElement(elementPath, propertyType);

			element.setDescription(property.description());
			element.setLabel(property.label());
			element.setMinOccurs(property.minAccure());
			element.setMaxOccurs(property.maxAccure());

			if (!StringUtils.isEmpty(property.possibleValuesClassName()))
			{
				element.addFacetEnumeration(getEnumerationList(property.possibleValuesClassName()));
			}

			if (!StringUtils.isEmpty(property.propertyName()))
			{
				element.setDefaultValue(getValue(propertiesListner, property));
			}

			FieldMetadata fieldMetadat = new FieldMetadata();
			fieldMetadat.setElement(element);
			fieldMetadat.setAnootationProperty(property);
			fieldMetadat.setPath(elementPath);
			//			fieldMetadat.setGroup(!isPrimitive(field));
			metadata.put(elementPath, fieldMetadat);
		}
		else
		{
			Path elementPath = prefix != null ? prefix.add(Path.parse(field.getName()))
				: Path.parse(field.getName());
			BeanElement element = def
				.createElement(elementPath, TypeName.forName(property.dataType()));
			element.setDescription(property.description());
			element.setMinOccurs(property.minAccure());
			element.setMaxOccurs(property.maxAccure());
			FieldMetadata fieldMetadat = new FieldMetadata();
			fieldMetadat.setElement(element);
			fieldMetadat.setPath(elementPath);
			fieldMetadat.setGroup(isPrimitive(property));
			Map<Path, FieldMetadata> childMetadata = new HashMap<>();
			generateBeanElement(childMetadata, field.getType(), aBuilder, propertiesListner);
			fieldMetadat.setFileds(childMetadata);
			metadata.put(elementPath, fieldMetadat);
		}
	}

	private static Nomenclature<String> getEnumerationList(String possibleValuesClassName)
	{
		PossibleValues possibleValues = (PossibleValues) com.orchestranetworks.ps.util.ClassLoaderUtils
			.newInstance(possibleValuesClassName);
		Nomenclature<String> enumList = new Nomenclature<>();
		List<EnumerationValue> values = possibleValues.getValues();
		for (EnumerationValue value : values)
		{
			enumList.addItemValue(value.getStoredValue(), value.getDisplayValue());
		}

		return enumList;
	}

	private static Object getValue(PropertiesFileListener propertiesListner, Property property)
	{
		String dataType = property.dataType();
		String propertyName = property.propertyName();
		Object value = null;

		if (DataType.STRING.equals(dataType))
		{
			value = propertiesListner.getProperty(propertyName);
		}
		else if (DataType.DATE.equals(dataType))
		{
			value = propertiesListner.getDateProperty(propertyName);
		}
		else if (DataType.DATETIME.equals(dataType))
		{
			value = propertiesListner.getDateProperty(propertyName);
		}
		else if (DataType.BOOLEAN.equals(dataType))
		{
			value = propertiesListner.getBooleanProperty(propertyName);
		}
		else if (DataType.INTEGER.equals(dataType) || DataType.INT.equals(dataType))
		{
			value = propertiesListner.getIntegerProperty(propertyName);
		}
		else if (DataType.DECIMAL.equals(dataType))
		{
			value = propertiesListner.getDecimalProperty(propertyName);
		}
		else if (DataType.LONG.equals(dataType))
		{
			value = propertiesListner.getLongProperty(propertyName);
		}
		else if (DataType.PASSWORD.equals(dataType))
		{
			value = propertiesListner.getDecryptValue(propertyName);
		}
		if (value == null)
		{
			value = property.defaultValue();
			if ("".equals(value))
			{
				value = null;
			}
		}
		return value;

	}

	private static SchemaTypeName getSchemaType(String dataType)
	{
		if (DataType.STRING.equals(dataType))
		{
			return SchemaTypeName.XS_STRING;
		}
		else if (DataType.DATE.equals(dataType))
		{
			return SchemaTypeName.XS_DATE;
		}
		else if (DataType.DATETIME.equals(dataType))
		{
			return SchemaTypeName.XS_DATETIME;
		}
		else if (DataType.BOOLEAN.equals(dataType))
		{
			return SchemaTypeName.XS_BOOLEAN;
		}
		else if (DataType.INTEGER.equals(dataType) || DataType.INT.equals(dataType))
		{
			return SchemaTypeName.XS_INTEGER;
		}
		else if (DataType.DECIMAL.equals(dataType) || DataType.LONG.equals(dataType))
		{
			return SchemaTypeName.XS_DECIMAL;
		}
		else if (DataType.PASSWORD.equals(dataType))
		{
			return SchemaTypeName.OSD_PASSWORD;
		}
		return SchemaTypeName.XS_STRING;
	}

	private static boolean isPrimitive(Property property)
	{
		String type = property.dataType();
		return DataType.STRING.equals(type) || DataType.INTEGER.equals(type)
			|| DataType.INT.equals(type) || DataType.LONG.equals(type)
			|| DataType.DECIMAL.equals(type) || DataType.DATE.equals(type)
			|| DataType.DATETIME.equals(type) || DataType.BOOLEAN.equals(type)
			|| DataType.PASSWORD.equals(type);
	}

	public static ConfigurationType getTypeAnnotation(
		Class<?> beanClass,
		Class<ConfigurationType> annotationClass)
	{
		return beanClass.getAnnotation(annotationClass);

	}

}
