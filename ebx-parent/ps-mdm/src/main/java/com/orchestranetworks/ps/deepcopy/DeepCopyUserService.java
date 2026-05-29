/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.deepcopy.PrimaryKeyDataSpec.BeanSpec;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 */
public class DeepCopyUserService extends AbstractUserService<RecordEntitySelection>
{
	public static final String SERVICE_NAME = "DeepCopyUserService";
	public static final ObjectKey DEEP_COPY_INPUT_CONTEXT = ObjectKey.forName("deepCopyInput");
	public static ServiceKey getServiceKey()
	{
		return ServiceKey.forName(SERVICE_NAME);
	}

	private PrimaryKeyDataSpec pkeySpec;
	private boolean inputKeys = false;
	private DeepCopyDataModifier modifier;
	private final DeepCopyImpl impl;

	public DeepCopyUserService(DeepCopyConfigFactory configFactory)
	{
		this(configFactory, null, false, false);
	}

	public DeepCopyUserService(
		DeepCopyConfigFactory configFactory,
		DeepCopyDataModifier modifier,
		boolean openNewRecord,
		boolean inputKeys)
	{
		this.impl = new DeepCopyImpl(configFactory, openNewRecord);
		this.modifier = modifier;
		this.inputKeys = inputKeys;
	}

	protected DeepCopyDataModifier createDataModifier()
	{
		if (inputKeys)
		{
			DeepCopyDataModifier newMod = new SetPrimaryKeyDataModifier(pkeySpec);
			if (this.modifier == null)
				return newMod;
			return new CompositeDataModifier().addModifier(this.modifier).addModifier(newMod);
		}
		return this.modifier != null ? this.modifier : new DefaultDeepCopyDataModifier();
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		Adaptation record = getRecordToCopy();
		impl.execute(session, record, createDataModifier());
	}

	// Default Behavior it to copy the selected record
	protected Adaptation getRecordToCopy()
	{
		return context.getEntitySelection().getRecord();
	}

	// This wasn't being called from anywhere - commenting out for now
	//
	//	protected String getRedirectURL(Adaptation createdRecord)
	//	{
	//		// TODO: Doesn't work when called from association. Need feedback from engineering.
	//		if (impl.isOpenNewRecord())
	//			return context.getLocator().getURLForSelection(createdRecord);
	//		return context.getLocator().getURLForEndingService();
	//	}

	@Override
	public void landService()
	{
		Adaptation target = impl.getTargetRecord();
		if (impl.isOpenNewRecord() && target != null)
		{
			String url = context.getWriter().getURLForSelection(target);
			context.getWriter().addJS_cr("window.location.href='" + url + "';");
		}
		else
			super.landService();
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<RecordEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (!inputKeys)
		{
			super.setupObjectContext(aContext, aBuilder);
			return;
		}
		initContext(aContext);
		if (aContext.isInitialDisplay())
		{
			Adaptation record = aContext.getEntitySelection().getRecord();
			pkeySpec = new PrimaryKeyDataSpec(record, DEEP_COPY_INPUT_CONTEXT.getName());

			BeanSpec[] spec = pkeySpec.getBeanSpecs();
			BeanDefinition def = context.defineObject(aBuilder, DEEP_COPY_INPUT_CONTEXT);

			ValueContext vc = record.createValueContext();
			for (int i = 0; i < spec.length; ++i)
			{
				BeanSpec bean = spec[i];
				BeanElement element = defineElement(def, bean.getChildNode(), vc);
				element.setDefaultValue(bean.getPkeyFieldValue());
			}
		}
	}

	@Override
	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		if (inputKeys)
		{
			BeanSpec[] spec = pkeySpec.getBeanSpecs();
			for (int i = 0; i < spec.length; ++i)
			{
				BeanSpec bean = spec[i];
				bean.setPkeyFieldValue(fromContext.getValueContext(
					DEEP_COPY_INPUT_CONTEXT,
					bean.getSimplePath()).getValue()); // was bean.getPkeyFieldPath())
			}
		}
		return super.readValues(fromContext);
	}

	@Override
	public void validate(UserServiceValidateContext<RecordEntitySelection> aContext)
	{
		super.validate(aContext);
		if (inputKeys)
		{
			readValues(aContext);
			if (!pkeySpec.isNewPrimaryKey(aContext.getValueContext(DEEP_COPY_INPUT_CONTEXT)))
			{
				aContext.addError("The entered values are not unique. Please revise and try again.");
			}
		}
	}

	public DeepCopyImpl getImpl()
	{
		return impl;
	}
}
