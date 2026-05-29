package com.orchestranetworks.ps.service;

import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

public class UserServiceNoUI<S extends EntitySelection> implements UserServiceExtended<S>
{
	private final AbstractUserService<S> impl;
	public UserServiceNoUI(AbstractUserService<S> impl)
	{
		this.impl = impl;
	}

	@Override
	public UserServiceEventOutcome processEventOutcome(
		UserServiceProcessEventOutcomeContext<S> outcomeContext,
		UserServiceEventOutcome outcome)
	{
		return outcome;
	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<S> arg0,
		UserServiceDisplayConfigurator arg1)
	{
		// Do nothing
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<S> arg0,
		UserServiceObjectContextBuilder arg1)
	{
		// Do nothing
	}

	@Override
	public void validate(UserServiceValidateContext<S> arg0)
	{
		// Do nothing
	}

	@Override
	public UserServiceEventOutcome initialize(UserServiceInitializeContext<S> aContext)
	{
		impl.context.setEntitySelection(aContext.getEntitySelection());
		impl.context.setRequest(aContext);
		impl.context.setLocator(aContext);
		try
		{
			impl.init(null, aContext.getRepository());
			impl.execute(aContext.getSession());
		}
		catch (OperationException e)
		{
			aContext.addError(e.getMessage());
		}
		return getOutcome();
	}

	/** Override this if nextClose would cause an issue, like it might for a delete service... */
	public UserServiceEventOutcome getOutcome()
	{
		return UserServiceNext.nextClose();
	}

}
