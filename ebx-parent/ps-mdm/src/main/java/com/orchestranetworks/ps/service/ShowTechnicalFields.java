package com.orchestranetworks.ps.service;

import com.onwbp.adaptation.Adaptation;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.SchemaTypeName;
import com.orchestranetworks.schema.dynamic.BeanDefinition;
import com.orchestranetworks.service.OperationException;
import com.orchestranetworks.service.Session;
import com.orchestranetworks.ui.form.widget.UIWidget;
import com.orchestranetworks.ui.selection.RecordEntitySelection;
import com.orchestranetworks.userservice.ObjectKey;
import com.orchestranetworks.userservice.UserServiceDisplayConfigurator;
import com.orchestranetworks.userservice.UserServiceObjectContextBuilder;
import com.orchestranetworks.userservice.UserServicePaneWriter;
import com.orchestranetworks.userservice.UserServiceSetupDisplayContext;
import com.orchestranetworks.userservice.UserServiceSetupObjectContext;

public class ShowTechnicalFields extends AbstractUserService<RecordEntitySelection>
{
	private static final ObjectKey _ResultObjectKey = ObjectKey.forName("result");

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<RecordEntitySelection> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (aContext.isInitialDisplay())
		{
			RecordEntitySelection selection = aContext.getEntitySelection();
			Adaptation record = selection.getRecord();
			BeanDefinition def = context.defineObject(aBuilder, _ResultObjectKey);
			defineElement(
				def,
				Path.parse("./created"),
				"Created",
				SchemaTypeName.XS_DATETIME,
				record.getTimeOfCreation());
			defineElement(
				def,
				Path.parse("./createdBy"),
				"Created By",
				SchemaTypeName.XS_STRING,
				record.getCreator().getLabel());
			defineElement(
				def,
				Path.parse("./lastModified"),
				"Last Modified",
				SchemaTypeName.XS_DATETIME,
				record.getTimeOfLastModification());
			defineElement(
				def,
				Path.parse("./lastModifiedBy"),
				"Last Modified By",
				SchemaTypeName.XS_STRING,
				record.getLastUser().getLabel());
		}
		super.setupObjectContext(aContext, aBuilder);
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		//there really isn't anything to do after initial display
	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<RecordEntitySelection> aContext,
		UserServiceDisplayConfigurator aConfigurator)
	{
		// by default, we add this as a pane
		context.setLocator(aConfigurator);
		if (!submitted)
		{
			aConfigurator.setLeftButtons(aConfigurator.newCancelButton());
			aConfigurator.setContent(this::writeInputPane);
			submitted = true;
		}
	}

	@Override
	protected void writeNode(UserServicePaneWriter aWriter, SchemaNode node, boolean top)
	{
		Path path = Path.SELF.add(node.getPathInAdaptation().getSubPath(1));
		if (node.isComplex())
		{
			if (!top)
				aWriter.startFormGroup(path);
			SchemaNode[] children = node.getNodeChildren();
			for (SchemaNode childNode : children)
			{
				writeNode(aWriter, childNode, false);
			}
			if (!top)
				aWriter.endFormGroup();
		}
		else
		{
			aWriter.startFormRow(path);
			UIWidget widget = aWriter.newBestMatching(path);
			widget.setEditorDisabled(true);
			aWriter.addWidget(widget);
			aWriter.endFormRow();
		}
	}

}
