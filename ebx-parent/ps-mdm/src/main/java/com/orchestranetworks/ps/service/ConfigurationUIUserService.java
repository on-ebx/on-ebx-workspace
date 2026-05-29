package com.orchestranetworks.ps.service;

import java.util.*;
import java.util.Map.*;

import org.apache.commons.configuration2.*;
import org.apache.commons.lang3.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.ps.ui.annotations.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.form.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

//public abstract class AbstractUserService<S extends EntitySelection> implements UserService<S>

public class ConfigurationUIUserService<S extends DataspaceEntitySelection>
	extends
	AbstractUserService<DataspaceEntitySelection>
{

	private ObjectKey _objectKey = null;

	private Map<Path, FieldMetadata> metadata = new LinkedHashMap<>();

	private Class<?> beanClass;

	private String fileName;

	private PropertiesFileListener configurationPropertiesFileListner;
	private PropertiesFileListener ebxPropertiesFileListner;

	public ConfigurationUIUserService(Class<?> beanClass)
	{
		this.beanClass = beanClass;
		ConfigurationType configuration = ConfigurationAnnotationUtils
			.getTypeAnnotation(beanClass, ConfigurationType.class);

		this.fileName = System.getProperty(configuration.systemPropertyName());
		if (StringUtils.isEmpty(fileName))
		{
			this.fileName = System.getProperty(CommonConstants.EBX_PROPERTIES_SYSTEM_PROPERTY_NAME);
		}
		this.configurationPropertiesFileListner = PropertiesFileListener.getPropertyLoader(this.fileName);
		this.ebxPropertiesFileListner = PropertiesFileListener.getPropertyLoader(CommonConstants.DEFAULT_EBX_PROPERTIES_FILE_NAME);
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<DataspaceEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (aContext.isInitialDisplay())
		{
			String className = beanClass.getSimpleName();
			_objectKey = ObjectKey.forName(className);

			BeanDefinition def = ConfigurationAnnotationUtils
				.generateBeanElement(metadata, beanClass, aBuilder, configurationPropertiesFileListner);
			aBuilder.registerBean(_objectKey, def);
		}

	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext,
		UserServiceDisplayConfigurator aConfigurator)
	{
		validateServiceConfiguration(aContext,aConfigurator);
		String title = ConfigurationAnnotationUtils
			.getTypeAnnotation(beanClass, ConfigurationType.class)
			.nameInMenu();
		aConfigurator.setTitle(title);
		aConfigurator.setLeftButtons(aConfigurator.newSaveButton(this::save), aConfigurator.newCancelButton());

//		aConfigurator.setRightButtons(
//			aConfigurator.newSubmitButton("Test Connection", this::testConnection),
//			aConfigurator.newSaveButton(this::save));

		aConfigurator.setContent(this::writePane);

	}

	private void writePane(UserServicePaneContext aContext, UserServicePaneWriter aWriter)
	{
		aWriter.setCurrentObject(_objectKey);

		aWriter.startTableFormRow();

		generateUI(aWriter, metadata);

		aWriter.endTableFormRow();
	}

	private UserServiceEventOutcome save(UserServiceEventContext anEventContext)
	{
		if (configurationPropertiesFileListner == null)
		{
			return null;
		}
		ValueContextForInputValidation vc = anEventContext.getValueContext(_objectKey);
		Set<Entry<Path, FieldMetadata>> fields = metadata.entrySet();
		Configuration configuration = configurationPropertiesFileListner.getConfiguration();
		for (Entry<Path, FieldMetadata> entry : fields)
		{
			Path path = entry.getKey();
			FieldMetadata fieldData = entry.getValue();

			Object newValue = vc.getValue(path);
			String propertyname = fieldData.getAnootationProperty().propertyName();
			if (configuration.containsKey(propertyname) && newValue == null)
			{
				configuration.clearProperty(propertyname);
				continue;
			}
			configuration.setProperty(propertyname, newValue);
		}
		configurationPropertiesFileListner.saveStoredProperties();
		return null;
	}

	protected UserServiceEventOutcome testConnection(UserServiceEventContext anEventContext)
	{
		anEventContext.addWarning("The test method is not implemented for this service.");
		return null;
	}

	@Override
	public void execute(Session session) throws OperationException
	{
	}

	private void generateUI(UserServicePaneWriter aWriter, Map<Path, FieldMetadata> fieldsMetadata)
	{
		Set<Entry<Path, FieldMetadata>> fields = fieldsMetadata.entrySet();
		for (Entry<Path, FieldMetadata> entry : fields)
		{
			Path fieldPath = entry.getKey();
			FieldMetadata fieldMatadata = entry.getValue();
			if (fieldMatadata.isGroup())
			{
				UIFormGroup groupForm = new UIFormGroupImpl(fieldPath);
				Map<Path, FieldMetadata> children = fieldMatadata.getFileds();
				aWriter.addFormGroup(groupForm);
				generateUI(aWriter, children);
				aWriter.endFormGroup();
			}
			else
			{
				aWriter.addFormRow(fieldPath);
			}
		}

	}
	
	private void validateServiceConfiguration(UserServiceSetupDisplayContext<DataspaceEntitySelection> aContext, UserServiceDisplayConfigurator aConfigurator) {
		ConfigurationType annotation = ConfigurationAnnotationUtils
		.getTypeAnnotation(beanClass, ConfigurationType.class);
		if (StringUtils.isEmpty(annotation.requiredPropertyName())) {
			return;
		}
		String propertyValue = ebxPropertiesFileListner.getProperty(annotation.requiredPropertyName());
		if (propertyValue != null) {
			propertyValue = propertyValue.trim();
		}
		String[] propertiesPossibleValues =  annotation.requiredPropertyValue().split(",");
		boolean valueFound = false;
		for (int i = 0; i < propertiesPossibleValues.length; i++)
		{
			String possibleValue = propertiesPossibleValues[i]; 
			if (possibleValue != null) {
				possibleValue = possibleValue.trim();
			}
			if (Strings.CI.equals(propertyValue, possibleValue)) {
				valueFound = true;
				break;
			}
		}
		if (!valueFound) {
			aContext.addWarning("\"" + annotation.requiredPropertyName() + "\" needs to be set in ebx.properties with the value of \"" +annotation.requiredPropertyValue()+ "\" for this configuration to work properly.");
		}
	}

}
