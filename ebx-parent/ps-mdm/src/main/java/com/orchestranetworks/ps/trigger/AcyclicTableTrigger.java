package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.trigger.TriggerActionValidator.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.ps.util.functional.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 *  IMPORTANT NOTE: This class will now enforce application of User Permissions during any kind of Imports (either native EBX Imports or the Data Exchange add-on Imports)
 *  -- BE AWARE that in any of your Table Trigger Subclasses, you should always be using <ValueContext.setValueEnablingPrivilegeForNode> instead of <<ValueContext.setValue> 
 *       when setting fields that the end-user might not have permission to update
 */


/**
 * Prevents creations and modifications of the parent field when the parent hierarchy contains a reference
 * to this record. This can be used in conjunction with a {@link com.orchestranetworks.ps.constraint.AcyclicConstraint}
 * or a {@link com.orchestranetworks.ps.tablereffilter.AcyclicTableRefFilter}.
 * The constraint and filter essentially do the same thing, but when importing data, it will get imported anyway even though there
 * are errors. In cases where this could cause a serious issue (like an infinite loop), this trigger can be used as a
 * precaution against that. When the parent is retrieved via a link table, you should put the trigger on both the link and the
 * target tables, configuring the paths appropriately (since you can't guarantee which order they'll load the tables in).
 */
public class AcyclicTableTrigger extends BaseTableTriggerEnforcingPermissions
{
	private AcyclicConfig config = new AcyclicConfig();
	private UnaryFunction<ValueChanges, Boolean> hasAncestryChanged;

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);
		config.setup(context);
		// Create a default hasAncestryChanged function that simply sees if the fields represented
		// by the parent or child path have changed
		hasAncestryChanged = new UnaryFunction<ValueChanges, Boolean>()
		{
			@Override
			public Boolean evaluate(ValueChanges changes)
			{
				List<Path> parentPath = config.getParentPath();
				if (parentPath.isEmpty() || changes.getChange(parentPath.get(0)) != null)
				{
					return Boolean.TRUE;
				}
				Path childPath = config.getChildPath();
				return Boolean.valueOf(childPath != null && changes.getChange(childPath) != null);
			}
		};
	}

	@Override
	protected TriggerActionValidator createTriggerActionValidator(TriggerAction triggerAction)
	{
		return new TriggerActionValidator()
		{
			@Override
			public UserMessage validateTriggerAction(
				Session session,
				ValueContext valueContext,
				ValueChanges valueChanges,
				TriggerAction action)
				throws OperationException
			{
				// Don't need to do anything for deletes.
				// TODO: For creates, we can't follow associated records currently so we handle that in handleAfterCreate. See comments there.
				if (action == TriggerAction.DELETE || action == TriggerAction.CREATE)
				{
					return null;
				}
				// If it's a modify and the parent or child was changed then detect a cycle
				if (Boolean.TRUE.equals(hasAncestryChanged.evaluate(valueChanges)))
				{
					Path childPath = getChildPath();
					Adaptation record = childPath == null
						? AdaptationUtil.getRecordForValueContext(valueContext)
						: AdaptationUtil.followFK(valueContext, childPath);
					if (record != null)
					{
						String cycle = AdaptationUtil.detectAncestorCycle(
							record,
							valueContext,
							getGetParents(),
							getGetRecordLabel());
						if (cycle != null)
						{
							return UserMessage.createError(config.createCheckContextMessage(cycle));
						}
					}
				}
				return null;
			}
		};
	}

	// TODO: We need to check for cycle after create rather than before, because we can't follow associated links
	//       from a record prior to it being saved. Ideally we'd have that capability and implement this in handleBeforeCreate
	//       (or in the trigger action validator which is called by handleBeforeCreate). Awaiting response from support on
	//       that request.
	@Override
	public void handleAfterCreate(AfterCreateOccurrenceContext context) throws OperationException
	{
		super.handleAfterCreate(context);

		Adaptation record = context.getAdaptationOccurrence();
		String cycle = AdaptationUtil
			.detectAncestorCycle(record, null, getGetParents(), getGetRecordLabel());
		if (cycle != null)
		{
			throw OperationException.createError(config.createCheckContextMessage(cycle));
		}
	}

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		revalidatePaths();
		super.handleBeforeCreate(context);
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		revalidatePaths();
		super.handleBeforeModify(context);
	}

	@Override
	public void handleBeforeDelete(BeforeDeleteOccurrenceContext context) throws OperationException
	{
		revalidatePaths();
		super.handleBeforeDelete(context);
	}

	// The paths can't be guaranteed to be validated on setup so we validate them in handleBefore methods,
	// if they haven't been validated already
	private void revalidatePaths()
	{
		if (config.isPathsValidationIndeterminate())
		{
			String msg = config.revalidatePaths();
			if (msg != null)
			{
				throw new IllegalStateException("Error in setup of table trigger: " + msg);
			}
		}
	}

	public String getParentPath()
	{
		return config.getPathsToParentString();
	}

	public void setParentPath(String parentPath)
	{
		config.setPathsToParentString(parentPath);
	}

	public BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getGetParents()
	{
		return config.getGetParents();
	}

	public void setGetParents(BinaryFunction<Adaptation, ValueContext, List<Adaptation>> getParents)
	{
		config.setGetParents(getParents);
	}

	public UnaryFunction<Adaptation, String> getGetRecordLabel()
	{
		return config.getGetRecordLabel();
	}

	public void setGetRecordLabel(UnaryFunction<Adaptation, String> getRecordLabel)
	{
		config.setGetRecordLabel(getRecordLabel);
	}

	public UnaryFunction<ValueChanges, Boolean> getHasAncestryChanged()
	{
		return hasAncestryChanged;
	}

	public void setHasAncestryChanged(UnaryFunction<ValueChanges, Boolean> hasAncestryChanged)
	{
		this.hasAncestryChanged = hasAncestryChanged;
	}

	public Path getChildPath()
	{
		return config.getChildPath();
	}

	public void setChildPath(Path childPath)
	{
		config.setChildPath(childPath);
	}
}
