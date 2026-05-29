package com.orchestranetworks.ps.servicepermission;

import java.util.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.permission.*;

/**
 * EBX only allows one service permission rule to be put on a node for each service key.
 * This class allows you to specify multiple service permission rules per node and utilizes
 * a {@link CompoundServicePermissionRule} when more than one is set.
 * 
 * Since the compound rule needs to be typed appropriately, you need to pass in the specific class to use
 * when instantiating it.
 * 
 * You can configure what separator is used to separate multiple messages. If none supplied, will use the
 * compound rule's default.
 */
// TODO: Should we add a setServicePermissionRuleOnNodeAndAllDescendants to be complete? Don't have a need for it at the time.
// TODO: Is there a better way to handle instantiating the compound rule? I don't want to have just an enum because I see it being
//       useful to be able to specify your own subclass, but this is kind of ugly.
public class ServicePermissionRulesManager
{
	/**
	 * An enumeration that specifies the types of compound rules available, and for each what the specific class is.
	 * These are provided for convenience, for use with the
	 * {@link ServicePermissionRulesManager#setServicePermissionRuleOnNode(Path, ServiceKey, ServicePermissionRule, CompoundRuleEntityType)}
	 * method, but they don't have to be used. One can instead invoke
	 * {@link ServicePermissionRulesManager#setServicePermissionRuleOnNode(Path, ServiceKey, ServicePermissionRule, Class)}
	 * instead, passing in the specific class to instantiate a compound rule for.
	 */
	public enum CompoundRuleEntityType {
		/** Represents a CompoundAssociationServicePermissionRule */
		ASSOCIATION(CompoundAssociationServicePermissionRule.class),

		/** Represents a CompoundServicePermissionRule */
		DATASPACE(CompoundServicePermissionRule.class),

		/** Represents a CompoundRecordServicePermissionRule */
		RECORD(CompoundRecordServicePermissionRule.class),

		/** Represents a CompoundTableViewServicePermissionRule */
		TABLE_VIEW(CompoundTableViewServicePermissionRule.class);

		private Class<?> clazz;

		private CompoundRuleEntityType(Class<?> clazz)
		{
			this.clazz = clazz;
		}

		/**
		 * Get the class for the compound rule associated with this enumerated value
		 * 
		 * @return the class
		 */
		public Class<?> getClassForEntityType()
		{
			return clazz;
		}
	}

	// Key = path of the node to put the rules on
	// Value = Map of:
	//         Key = service key to put the rules on
	//         Value = list of service permission rules
	private Map<Path, Map<ServiceKey, List<ServicePermissionRule<?>>>> permissionRuleMap = new HashMap<>();
	private SchemaExtensionsContext context;
	private String messageSeparator;

	/**
	 * Create the manager using the default message separator.
	 * This is equivalent to calling {@link #ServicePermissionRulesManager(SchemaExtensionsContext, String)
	 * with <code>null</code>.
	 * 
	 * @param context the context
	 */
	public ServicePermissionRulesManager(SchemaExtensionsContext context)
	{
		this(context, null);
	}

	/**
	 * Create the manager with the given message separator. If the separator is <code>null</code>,
	 * then the default separator will be used.
	 * 
	 * @param context the context
	 * @param messageSeparator the separator, or <code>null</code>
	 */
	public ServicePermissionRulesManager(SchemaExtensionsContext context, String messageSeparator)
	{
		this.context = context;
		this.messageSeparator = messageSeparator;
	}

	/**
	 * This invokes {@link #setServicePermissionRuleOnNode(Path, ServiceKey, ServicePermissionRule, Class)},
	 * passing in the class for a specified enumerated {@link CompoundRuleEntityType}. It can be more convenient
	 * than passing in the class, but otherwise is equivalent. See that method for more details.
	 * 
	 * @param path the path for the node to put the rule on
	 * @param serviceKey the service key to put the rule on
	 * @param rule the rule
	 * @param compoundRuleEntityType a predefined type that represents a {@link CompoundServicePermissionRule} or a subclass
	 * @throws OperationException if an error occurs setting the rule
	 */
	public void setServicePermissionRuleOnNode(
		Path path,
		ServiceKey serviceKey,
		ServicePermissionRule<?> rule,
		CompoundRuleEntityType compoundRuleEntityType)
		throws OperationException
	{
		setServicePermissionRuleOnNode(
			path,
			serviceKey,
			rule,
			compoundRuleEntityType.getClassForEntityType());
	}

	/**
	 * Sets a rule on a node for a service key, and if there's already a rule, wrap them in a compound rule.
	 * This method purposely has signature similar to
	 * {@link SchemaExtensionsContext#setServicePermissionRuleOnNode(Path, ServiceKey, ServicePermissionRule)}
	 * to make it easier to plug in. However, in addition you need to specify the class for the compound rule.
	 * 
	 * Care must be taken with the compound rule class, because it's possible to call this method for multiple rules
	 * on the same node & service key, yet supply different compound rule classes, or supply compound rule classes
	 * that aren't appropriate for the rules supplied.
	 * 
	 * @param path the path for the node to put the rule on
	 * @param serviceKey the service key to put the rule on
	 * @param rule the rule
	 * @param compoundRuleClass a class to use to instantiate the compound rule
	 * @throws OperationException if an error occurs setting the rule
	 */
	@SuppressWarnings("unchecked")
	public void setServicePermissionRuleOnNode(
		Path path,
		ServiceKey serviceKey,
		ServicePermissionRule<?> rule,
		Class<?> compoundRuleClass)
		throws OperationException
	{
		// Get the map of service key / rules for the path. If null, create an empty map.
		Map<ServiceKey, List<ServicePermissionRule<?>>> map = permissionRuleMap.get(path);
		if (map == null)
		{
			map = new HashMap<>();
			permissionRuleMap.put(path, map);
		}
		// Get the list of rules for the service key from the map. If null, create an empty list.
		List<ServicePermissionRule<?>> list = map.get(serviceKey);
		if (list == null)
		{
			list = new ArrayList<>();
			map.put(serviceKey, list);
		}
		// Add the rule to the list
		list.add(rule);

		// If there are rules already stored, then it means we'll need a compound rule
		if (list.size() > 1)
		{
			// Instantiate the rule
			CompoundServicePermissionRule<DataspaceEntitySelection> compoundRule;
			try
			{
				compoundRule = (CompoundServicePermissionRule<DataspaceEntitySelection>) compoundRuleClass
					.getDeclaredConstructor()
					.newInstance();
			}
			catch (ReflectiveOperationException ex)
			{
				throw OperationException.createError("Error creating Compound Rule.", ex);
			}

			if (messageSeparator != null)
			{
				compoundRule.setMessageSeparator(messageSeparator);
			}

			// Loop through the stored rules and add them to the compound rule
			for (ServicePermissionRule<?> storedRule : list)
			{
				compoundRule
					.appendRule((ServicePermissionRule<DataspaceEntitySelection>) storedRule);
			}
			context.setServicePermissionRuleOnNode(path, serviceKey, compoundRule);
		}
		// Otherwise this is the only one so just set it
		else
		{
			context.setServicePermissionRuleOnNode(path, serviceKey, rule);
		}
	}

	public String getMessageSeparator()
	{
		return messageSeparator;
	}

	public void setMessageSeparator(String messageSeparator)
	{
		this.messageSeparator = messageSeparator;
	}
}
