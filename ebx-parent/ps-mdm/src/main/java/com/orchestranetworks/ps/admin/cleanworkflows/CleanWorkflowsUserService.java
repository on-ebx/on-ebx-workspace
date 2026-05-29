/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.admin.cleanworkflows;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * A service that terminates and cleans workflows and closes child data spaces.
 */
public class CleanWorkflowsUserService extends AbstractUserService<DataspaceEntitySelection>
{
	private static final ObjectKey _InputObjectKey = ObjectKey.forName("input");
	private static final Path INCLUDE_COMPLETED_PATH = Path.parse("./includeCompleted");
	private static final Path INCLUDE_ACTIVE_PATH = Path.parse("./includeActive");
	private static final Path CREATED_BEFORE_DATE_PATH = Path.parse("./createdBeforeDate");
	private static final Path CREATED_BEFORE_NUM_OF_DAYS_PATH = Path
		.parse("./createdBeforeNumOfDays");
	private static final String INCLUDE_COMPLETED_LABEL = "Include Completed?";
	private static final String INCLUDE_ACTIVE_LABEL = "Include Active?";
	private static final String CREATED_BEFORE_DATE_LABEL = "Created Before Date";
	private static final String CREATED_BEFORE_NUM_OF_DAYS_LABEL = "Created Before Num Of Days";

	private CleanWorkflowsConfigFactory configFactory;
	protected CleanWorkflowsConfig config;
	protected CleanWorkflowsImpl impl = new CleanWorkflowsImpl();

	/**
	 * Create the service using the default folder and file, defined by
	 * {@link CleanWorkflowsPropertyFileHelper#DEFAULT_PROPERTIES_FOLDER} and
	 * {@link CleanWorkflowsPropertyFileHelper#DEFAULT_PROPERTIES_FILE}.
	 * This utilizes a {@link DefaultCleanWorkflowsConfigFactory}.
	 */
	public CleanWorkflowsUserService()
	{
		this(
			CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FOLDER,
			CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FILE);
	}

	/**
	 * Create the service using the default folder defined by
	 * {@link CleanWorkflowsPropertyFileHelper#DEFAULT_PROPERTIES_FOLDER}
	 * and the given properties file.
	 * This utilizes a {@link DefaultCleanWorkflowsConfigFactory}.
	 *
	 * @param propertiesFile the properties file
	 */
	public CleanWorkflowsUserService(String propertiesFile)
	{
		this(CleanWorkflowsPropertyFileHelper.DEFAULT_PROPERTIES_FOLDER, propertiesFile);
	}

	/**
	 * Create the service using the given folder and properties file.
	 * This utilizes a {@link DefaultCleanWorkflowsConfigFactory}.
	 *
	 * @param propertiesFolder the folder
	 * @param propertiesFile the properties file
	 */
	public CleanWorkflowsUserService(String propertiesFolder, String propertiesFile)
	{
		this(new DefaultCleanWorkflowsConfigFactory(propertiesFolder, propertiesFile));
	}

	/**
	 * Create the service using the given factory to create the configuration
	 *
	 * @param configFactory the factory
	 */
	public CleanWorkflowsUserService(CleanWorkflowsConfigFactory configFactory)
	{
		this.configFactory = configFactory;
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		Repository repo = context.getEntitySelection().getDataspace().getRepository();

		// Make sure only admins can execute since this service can have serious consequences.
		// Also, it utilizes things that usually only an administrator has the ability to do.
		if (!session.isUserInRole(Role.ADMINISTRATOR))
		{
			throw OperationException
				.createError("User doesn't have permission to execute service.");
		}

		impl.execute(session, repo, config);
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<DataspaceEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (aContext.isInitialDisplay())
		{
			try
			{
				config = configFactory
					.createConfig(aContext.getRepository(), aContext.getSession());
			}
			catch (OperationException ex)
			{
				config = null;
				LoggingCategory.getKernel()
					.error("Error creating Clean Workflows Configuration.", ex);
			}
			if (config != null)
			{
				BeanDefinition def = context.defineObject(aBuilder, _InputObjectKey);
				BeanElement includeCompletedElement = defineElement(
					def,
					INCLUDE_COMPLETED_PATH,
					INCLUDE_COMPLETED_LABEL,
					SchemaTypeName.XS_BOOLEAN,
					Boolean.valueOf(config.isIncludeCompleted()));
				includeCompletedElement.setMinOccurs(1);
				BeanElement includeActiveElement = defineElement(
					def,
					INCLUDE_ACTIVE_PATH,
					INCLUDE_ACTIVE_LABEL,
					SchemaTypeName.XS_BOOLEAN,
					Boolean.valueOf(config.isIncludeActive()));
				includeActiveElement.setMinOccurs(1);
				defineElement(
					def,
					CREATED_BEFORE_DATE_PATH,
					CREATED_BEFORE_DATE_LABEL,
					SchemaTypeName.XS_DATETIME,
					config.getCreatedBeforeDate());
				defineElement(
					def,
					CREATED_BEFORE_NUM_OF_DAYS_PATH,
					CREATED_BEFORE_NUM_OF_DAYS_LABEL,
					SchemaTypeName.XS_INTEGER,
					config.getCreatedBeforeNumOfDays());
			}
		}
		super.setupObjectContext(aContext, aBuilder);
	}

	@Override
	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		Boolean includeCompleted = (Boolean) fromContext
			.getValueContext(_InputObjectKey, INCLUDE_COMPLETED_PATH)
			.getValue();
		config.setIncludeActive(includeCompleted.booleanValue());
		Boolean includeActive = (Boolean) fromContext
			.getValueContext(_InputObjectKey, INCLUDE_ACTIVE_PATH)
			.getValue();
		config.setIncludeActive(includeActive.booleanValue());
		Date createdBeforeDate = (Date) fromContext
			.getValueContext(_InputObjectKey, CREATED_BEFORE_DATE_PATH)
			.getValue();
		config.setCreatedBeforeDate(createdBeforeDate);
		Integer createdBeforeNumOfDays = (Integer) fromContext
			.getValueContext(_InputObjectKey, CREATED_BEFORE_NUM_OF_DAYS_PATH)
			.getValue();
		config.setCreatedBeforeNumOfDays(createdBeforeNumOfDays);
		return super.readValues(fromContext);
	}

	@Override
	public void landService()
	{
		alert("Service complete.");
		super.landService();
	}

	@Override
	public void validate(UserServiceValidateContext<DataspaceEntitySelection> aContext)
	{
		Date createdBeforeDate = (Date) aContext
			.getValueContext(_InputObjectKey, CREATED_BEFORE_DATE_PATH)
			.getValue();
		Integer createdBeforeNumOfDays = (Integer) aContext
			.getValueContext(_InputObjectKey, CREATED_BEFORE_NUM_OF_DAYS_PATH)
			.getValue();
		if (createdBeforeDate == null && createdBeforeNumOfDays == null)
		{
			aContext.addError(
				"Either " + CREATED_BEFORE_DATE_LABEL + " or " + CREATED_BEFORE_NUM_OF_DAYS_LABEL
					+ " must be specified.");
		}

		Boolean includeCompleted = (Boolean) aContext
			.getValueContext(_InputObjectKey, INCLUDE_COMPLETED_PATH)
			.getValue();
		Boolean includeActive = (Boolean) aContext
			.getValueContext(_InputObjectKey, INCLUDE_ACTIVE_PATH)
			.getValue();
		if (!Boolean.TRUE.equals(includeCompleted) && !Boolean.TRUE.equals(includeActive))
		{
			aContext.addError(
				"Either " + INCLUDE_COMPLETED_LABEL + " or " + INCLUDE_ACTIVE_LABEL
					+ " must be true.");
		}

		super.validate(aContext);
	}

	public CleanWorkflowsImpl getImpl()
	{
		return this.impl;
	}

	public void setImpl(CleanWorkflowsImpl impl)
	{
		this.impl = impl;
	}
}
