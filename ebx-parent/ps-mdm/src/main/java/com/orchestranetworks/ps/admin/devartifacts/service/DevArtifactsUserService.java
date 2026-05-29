package com.orchestranetworks.ps.admin.devartifacts.service;

import java.util.*;

import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.admin.devartifacts.config.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.impl.*;
import com.orchestranetworks.ps.admin.devartifacts.util.*;
import com.orchestranetworks.ps.service.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.base.*;
import com.orchestranetworks.ui.form.*;
import com.orchestranetworks.ui.form.widget.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;

/**
 * An abstract User Service for presenting a form to the user, allowing them to configure the
 * import/export before launching it
 */
public abstract class DevArtifactsUserService<S extends DataspaceEntitySelection>
	extends
	AbstractFileDownloadUserService<S>
	implements DevArtifactsConstants
{
	// This service defines a single object named "input".
	private final static ObjectKey _InputObjectKey = ObjectKey.forName("input");
	private final Path envCopyPath = Path.SELF
		.add(Path.parse(DevArtifactsBase.PARAM_ENVIRONMENT_COPY));

	private boolean errorOccurred;

	protected DevArtifactsConfig config;

	private DevArtifactsBase impl;
	private DevArtifactsConfigFactory configFactory;
	private Map<String, String[]> paramMap;

	protected DevArtifactsUserService(
		DevArtifactsBase impl,
		DevArtifactsConfigFactory configFactory,
		Map<String, String[]> paramMap)
	{
		this.impl = impl;
		this.configFactory = configFactory;
		this.paramMap = paramMap;
	}

	@Override
	public void writeInputPane(UserServicePaneContext paneContext, UserServicePaneWriter aWriter)
	{
		String info = getInformation();
		if (info != null)
		{
			aWriter.add_cr("<div style='margin: 5px'>");
			aWriter.add_cr("  <p>" + info + "</p>");
			aWriter.add_cr("</div>");
		}
		writeForm(paneContext, aWriter, _InputObjectKey);
	}

	protected String getInformation()
	{
		return null;
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<S> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		if (aContext.isInitialDisplay())
		{
			try
			{
				config = configFactory
					.createConfig(aContext.getRepository(), aContext.getSession(), paramMap);
			}
			catch (OperationException ex)
			{
				String msg = "Error creating config.";
				DevArtifactsUtil.getLog().error(msg, ex);
				throw new RuntimeException(ex);
			}
			impl.setConfig(config);
			BeanDefinition def = context.defineObject(aBuilder, _InputObjectKey);
			defineElement(
				def,
				envCopyPath,
				"Environment Copy",
				SchemaTypeName.XS_BOOLEAN,
				Boolean.valueOf(config.isEnvironmentCopy()));
			addExtraInputs(aContext, def, _InputObjectKey);
		}
	}

	protected void addExtraInputs(
		UserServiceSetupObjectContext<S> aContext,
		BeanDefinition def,
		ObjectKey key)
	{
		// do nothing
	}

	protected List<Path> addInformationFields(BeanDefinition def)
	{
		return null;
	}

	@Override
	public void landService()
	{
		// Show an alert saying service complete, but only if an error didn't occur
		// and we're not showing a download link.
		if (!(errorOccurred || shouldWriteDownloadLinks()))
		{
			alert(SERVICE_COMPLETE_MSG);
		}
		super.landService();
	}

	@Override
	protected void writeNode(UserServicePaneWriter aWriter, SchemaNode node, boolean top)
	{
		Step lastStep = node.getPathInAdaptation().getLastStep();
		String paramStr = lastStep.format();
		if (DevArtifactsBase.PARAM_ENVIRONMENT_COPY.equals(paramStr))
		{
			writeCheckBox(aWriter, lastStep.toSelfPath(), paramStr, null);
		}
		else
		{
			super.writeNode(aWriter, node, top);
		}
	}

	@Override
	public void execute(Session session) throws OperationException
	{
		errorOccurred = false;
		Repository repo = context.getRequest().getRepository();
		try
		{
			impl.clearErrorCount();
			configFactory.updateConfig(config, repo, session, paramMap);
			doExecute(repo, session);
			DevArtifactsUtil.getLog().info("**** Dev Artifacts processing complete.");
		}
		catch (OperationException ex)
		{
			String msg = "Error processing artifacts.";
			DevArtifactsUtil.getLog().error(LOG_PREFIX + " " + msg, ex);
			alert(msg + " " + ex.getMessage());
			errorOccurred = true;
			return;
		}
	}

	protected void doExecute(Repository repo, Session session) throws OperationException
	{
		if (config.isEnvironmentCopy())
		{
			impl.processArtifacts(repo, session, true);
		}
		// Note that for imports, this will do nothing when env copy is true,
		// but for exports, we want to additionally export the normal artifacts
		impl.processArtifacts(repo, session, false);

		impl.postProcess(repo, session);

		if (impl.getErrorCount() > 0)
		{
			throw OperationException.createError(impl.getDevArtifactsOutcome());
		}
	}

	protected static void writeCheckBox(
		UserServicePaneWriter aWriter,
		Path path,
		String rowId,
		String jsFunction)
	{
		UICheckBox checkBox = aWriter.newCheckBox(path);
		if (jsFunction != null)
		{
			checkBox.setActionOnAfterValueChanged(JsFunctionCall.on(jsFunction));
		}
		UIFormRow formRow = aWriter.newFormRow(path);
		if (rowId != null)
		{
			formRow.setRowId(rowId);
		}
		aWriter.addFormRow(formRow, checkBox);
	}

	@Override
	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		for (ObjectKey key : context.getObjectKeys())
		{
			readValuesIntoParamMap(fromContext, key, null);
		}
		return super.readValues(fromContext);
	}

	private void readValuesIntoParamMap(
		UserServiceObjectContext fromContext,
		ObjectKey key,
		SchemaNode node)
	{
		SchemaNode readFromNode = (node == null) ? fromContext.getValueContext(key).getNode()
			: node;
		if (readFromNode.isComplex())
		{
			SchemaNode[] children = readFromNode.getNodeChildren();
			for (SchemaNode childNode : children)
			{
				readValuesIntoParamMap(fromContext, key, childNode);
			}
		}
		else
		{
			Path path = Path.SELF.add(readFromNode.getPathInAdaptation().getSubPath(1));
			paramMap.put(
				path.getLastStep().format(),
				new String[] { String.valueOf(fromContext.getValueContext(key, path).getValue()) });
		}
	}
}