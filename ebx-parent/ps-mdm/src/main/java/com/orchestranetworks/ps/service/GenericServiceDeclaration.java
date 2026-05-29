package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.userservice.declaration.*;
import com.orchestranetworks.userservice.permission.*;

public class GenericServiceDeclaration<S extends EntitySelection, U extends ActivationContext<S>>
	implements UserServiceDeclaration<S, U>
{
	private final ServiceKey serviceKey;
	private final Class<? extends UserService<S>> implementation;
	private final String title;
	private final String description;
	private final String confirmBeforeLaunchMessage; // if null, there will be no confirm before launch
	public static final String DEFAULT_CONFIRM = "<default-confirm>"; // use this value as the confirmBeforeLaunchMessage to get the default confirmation message 
	private boolean enabled = false; // when "true", then the service will be enabled for all users by default
										// -- otherwise will be controlled thru dataset/advanced services based on Profile (default)

	public GenericServiceDeclaration(
		ServiceKey aServiceKey,
		Class<? extends UserService<S>> implementation,
		String aTitle,
		String aDescription,
		String aConfirmBeforeLaunchMessage)
	{
		this(aServiceKey, implementation, aTitle, aDescription, aConfirmBeforeLaunchMessage, true);
	}

	public GenericServiceDeclaration(
		ServiceKey aServiceKey,
		Class<? extends UserService<S>> implementation,
		String aTitle,
		String aDescription,
		String aConfirmBeforeLaunchMessage,
		boolean enabled)
	{
		this.serviceKey = aServiceKey;
		this.implementation = implementation;
		this.title = aTitle;
		this.description = aDescription;
		this.confirmBeforeLaunchMessage = DEFAULT_CONFIRM.equals(aConfirmBeforeLaunchMessage)
			? defaultConfirm(aTitle)
			: aConfirmBeforeLaunchMessage;
		this.enabled = enabled;
	}

	private static String defaultConfirm(String aTitle)
	{
		return aTitle + "?";
	}

	@Override
	public final ServiceKey getServiceKey()
	{
		return this.serviceKey;
	}

	public final String getTitle()
	{
		return this.title;
	}

	public final String getDescription()
	{
		return this.description;
	}

	public final String getConfirmBeforeLaunchMessage()
	{
		return this.confirmBeforeLaunchMessage;
	}

	@Override
	public void defineProperties(UserServicePropertiesDefinitionContext aDefinition)
	{
		if (title != null)
		{
			aDefinition.setLabel(title);
		}
		if (description != null)
		{
			aDefinition.setDescription(description);
		}
		if (confirmBeforeLaunchMessage != null)
		{
			aDefinition.setConfirmationMessageBeforeLaunch(confirmBeforeLaunchMessage);
		}
	}

	@Override
	public void declareWebComponent(WebComponentDeclarationContext aDeclaration)
	{
		aDeclaration.setAvailableAsPerspectiveAction(true);
		aDeclaration.setAvailableAsWorkflowUserTask(true, true);
	}

	@Override
	public UserService<S> createUserService()
	{
		if (implementation != null)
		{
			try
			{
				return implementation.getDeclaredConstructor().newInstance();
			}
			catch (ReflectiveOperationException e)
			{
				throw new IllegalStateException("Unable to instantiate service implementation", e);
			}
		}
		else
		{
			throw new IllegalStateException(
				"If no service class is provided, must override createUserService");
		}
	}

	@Override
	public void defineActivation(U aContext)
	{
		if (!enabled)
			aContext.setDefaultPermission(UserServicePermission.getDisabled());
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

}
