package com.orchestranetworks.ps.service;

import com.orchestranetworks.hierarchy.HierarchyNode;
import com.orchestranetworks.service.ServiceKey;
import com.orchestranetworks.ui.selection.HierarchyNodeEntitySelection;
import com.orchestranetworks.userservice.declaration.ActivationContextOnHierarchyNode;
import com.orchestranetworks.userservice.declaration.UserServiceDeclaration;
import com.orchestranetworks.userservice.permission.ServicePermissionRuleContext;
import com.orchestranetworks.userservice.permission.UserServicePermission;

public class ShowNodeDeclaration extends
GenericServiceDeclaration<HierarchyNodeEntitySelection, ActivationContextOnHierarchyNode>
implements UserServiceDeclaration.OnHierarchyNode

{

	public ShowNodeDeclaration(String moduleName)
	{
		super(
			moduleName == null ? ServiceKey.forName("ShowNode")
				: ServiceKey.forModuleServiceName(moduleName, "ShowNode"),
			ShowNodeService.class,
			"Open Join Record",
			null,
			null);
	}

	@Override
	public void defineActivation(ActivationContextOnHierarchyNode aContext) {
		super.defineActivation(aContext);
		aContext.setPermissionRule(this::getPermission);
	}
	
	private UserServicePermission getPermission(ServicePermissionRuleContext<HierarchyNodeEntitySelection> context) {
		HierarchyNodeEntitySelection sel = context.getEntitySelection();
		HierarchyNode node = sel != null ? sel.getHierarchyNode() : null;
		if (node != null && node.getJoinOccurrence() != null)
			return UserServicePermission.getEnabled();
		return UserServicePermission.getDisabled();
	}
}