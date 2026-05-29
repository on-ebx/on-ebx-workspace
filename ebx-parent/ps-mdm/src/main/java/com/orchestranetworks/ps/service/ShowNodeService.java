package com.orchestranetworks.ps.service;

import com.orchestranetworks.hierarchy.HierarchyNode;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Session;
import com.orchestranetworks.ui.UIHttpManagerComponent;
import com.orchestranetworks.ui.UIHttpManagerComponent.Scope;
import com.orchestranetworks.ui.selection.HierarchyNodeEntitySelection;

public class ShowNodeService extends AbstractUserService<HierarchyNodeEntitySelection> {
	private String url;
	@Override
	public void landService() {
		if (url == null)
		{
			super.landService();
			return;
		}

		displayInFrameWithCloseButton("view-node-frame", url);
	}

	@Override
	public void execute(Session session) throws OperationException {
		HierarchyNode node = context.getEntitySelection().getHierarchyNode();
		UIHttpManagerComponent managerComponent = context.getWriter()
			.createWebComponentForSubSession();
		managerComponent.selectInstanceOrOccurrence(node.getJoinOccurrence());
		managerComponent.setScope(Scope.NODE);
		url = managerComponent.getURIWithParameters();
	}

}
