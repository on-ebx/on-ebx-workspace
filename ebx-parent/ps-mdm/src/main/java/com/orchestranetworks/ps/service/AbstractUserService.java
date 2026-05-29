package com.orchestranetworks.ps.service;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.onwbp.org.apache.commons.io.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.dynamic.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.selection.*;
import com.orchestranetworks.userservice.*;
import com.orchestranetworks.workflow.*;

/**
 * An abstract user service may be used as a base class for most user services.
 * There are two basic flavors to most user services; those that use an input
 * form and those that perform some action on a selection without displaying
 * anything. There are also services that generate files to download. This class
 * intends to cater to all of these use cases.
 */
public abstract class AbstractUserService<S extends EntitySelection> implements UserService<S>
{
	protected final Context<S> context = new Context<>();
	protected boolean submitted = false;
	protected boolean writeResultCalled = false;

	public void landService()
	{
		String url = context.getLocator().getURLForEndingService();
		landAtUrl(url);
	}

	public final void landAtUrl(String url)
	{
		UserServiceWriter writer = context.getWriter();
		writer.addJS_cr("window.location.href='" + url + "';");
	}

	public final void landAtUrlInParent(String url)
	{
		UserServiceWriter writer = context.getWriter();
		writer.addJS_cr("window.parent.location.href='" + url + "';");
	}

	public void writeDownloadLink(final File file)
	{
		writeDownloadLink(null, file);
	}

	public void writeDownloadLink(String label, final File file)
	{
		UserServiceWriter aWriter = context.getWriter();
		aWriter.add("<div ");
		aWriter.addSafeAttribute("class", UICSSClasses.CONTAINER_WITH_TEXT_PADDING);
		aWriter.add(">");

		String downloadURL = getDownloadURL(aWriter, file);
		aWriter.add((label != null ? label : "") + "<a ");
		aWriter.addSafeAttribute("href", downloadURL);
		aWriter.add(">Click here to download " + file.getName() + "</a>");

		aWriter.add("</div>");
	}

	public static String getDownloadURL(UserServiceWriter aWriter, final File file)
	{
		return aWriter.getURLForGetRequest(new UserServiceGetRequest()
		{
			@Override
			public void processRequest(
				UserServiceGetContext aContext,
				UserServiceGetResponse aResponse)
			{
				aResponse.setHeader("Content-type", "application/octet-stream");
				aResponse.setHeader(
					"Content-Disposition",
					"attachment; filename=\"" + file.getName() + "\"");
				OutputStream os = aResponse.getOutputStream();
				InputStream is = null;
				try
				{
					is = new FileInputStream(file);
					IOUtils.copy(is, os);
				}
				catch (IOException e)
				{

				}
				finally
				{
					IOUtils.closeQuietly(is);
					IOUtils.closeQuietly(os);
				}
			}
		});
	}

	public void execute(ScriptTaskContext sContext) throws OperationException
	{
		Session session = sContext.getSession();
		Repository repo = sContext.getRepository();
		init(sContext, repo);
		execute(session);
		finish(sContext, repo);
	}

	public void init(DataContext dataContext, Repository repo)
	{
		// init local variables using dataContext
	}

	public void finish(DataContext dataContext, Repository repo)
	{
		// set any out parameters
	}

	public abstract void execute(Session session) throws OperationException;

	/**
	 * Convenience method for checking if an http parameter's value is true. The
	 * parameter map contains an array of strings for each parameter's value,
	 * but for the boolean parameters we know it will only contain one value
	 * with either "false" or "true".
	 *
	 * @param paramValues
	 *            the array of values. See
	 *            {@link javax.servlet.ServletRequest#getParameterMap()}.
	 */
	protected static boolean isBooleanParamSet(String[] paramValues)
	{
		return paramValues != null && paramValues.length != 0 && paramValues[0].equals("true");
	}

	protected void alert(String message)
	{
		context.getWriter().addJS("alert('" + message + "');");
	}

	public static String appendServiceParameters(
		String componentUri,
		Map<String, String> parameters)
	{
		for (Map.Entry<String, String> entry : parameters.entrySet())
		{
			componentUri += '&' + entry.getKey() + '=' + entry.getValue();
		}
		return componentUri;
	}

	public void displayInFrameWithCloseButton(String frameId, String componentUri)
	{
		displayInFrameWithCloseButton(
			frameId,
			componentUri,
			context.getLocator().getURLForEndingService());
	}

	public void displayInFrameWithCloseButton(String frameId, String componentUri, String cancelUri)
	{
		displayInFrame(frameId, componentUri, cancelUri, true);
	}

	public void displayInFrameWithoutCloseButton(String frameId, String componentUri)
	{
		displayInFrame(frameId, componentUri, context.getLocator().getURLForEndingService(), false);
	}

	public void displayInFrame(
		String frameId,
		String componentUri,
		String cancelUri,
		boolean withCloseButton)
	{
		UserServiceWriter writer = context.getWriter();
		writer.addJS_addResizeWorkspaceListener("resizeFrame");
		if (withCloseButton)
		{
			UIButtonSpecJSAction cancelButtonSpec = new UIButtonSpecJSAction(
				UserMessage.createInfo("Close"),
				"cancelForm();");
			writer.addButtonJavaScript(cancelButtonSpec);
		}
		writer.add_cr(
			"<iframe id='" + frameId + "' name='" + frameId + "' src='" + componentUri
				+ "' src='' style='border: none; width:100%'></iframe>");
		// Setting the url directly in the iframe declaration causes 2 subsessions to be created,
		// and some unpredictable behavior. So it should be set via js code after creating the
		// iframe.
		writer.addJS_cr("document.getElementById('" + frameId + "').src='" + componentUri + "';");
		writer.addJS_cr("function resizeFrame(size)");
		writer.addJS_cr("{");
		writer.addJS_cr("  var frameElement = document.getElementById('" + frameId + "');");
		writer.addJS_cr("  frameElement.style.height = size.h + 'px';");
		writer.addJS_cr("}");
		writer.addJS_cr("function cancelForm()");
		writer.addJS_cr("{");
		writer.addJS_cr("  window.location.href='" + cancelUri + "';");
		writer.addJS_cr("}");
	}

	@Override
	public UserServiceEventOutcome processEventOutcome(
		UserServiceProcessEventOutcomeContext<S> aContext,
		UserServiceEventOutcome anEventOutcome)
	{
		return anEventOutcome;
	}

	@Override
	public void setupDisplay(
		UserServiceSetupDisplayContext<S> aContext,
		UserServiceDisplayConfigurator aConfigurator)
	{
		// by default, we add this as a pane
		context.setLocator(aConfigurator);
		if (!submitted)
		{
			aConfigurator.setLeftButtons(aConfigurator.newCancelButton());

			UIButtonSpecSubmit nextButton = aConfigurator.newNextButton(this::readValues);
			aConfigurator.setRightButtons(nextButton);
			aConfigurator.setContent(this::writeInputPane);
		}
		else
		{
			aConfigurator.setContent(this::writeResultPane);
		}
	}

	protected void writeInputPane(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter)
	{
		context.setRequest(aPaneContext);
		context.setWriter(aWriter);
		List<ObjectKey> keys = context.objectKeys;
		for (ObjectKey key : keys)
		{
			writeForm(aPaneContext, aWriter, key);
		}
	}

	protected void writeResultPane(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter)
	{
		if (writeResultCalled)
			return;
		writeResultCalled = true;
		context.setRequest(aPaneContext);
		context.setWriter(aWriter);
		try
		{
			init(null, aPaneContext.getRepository());
			execute(aPaneContext.getSession());
		}
		catch (OperationException e)
		{
			LoggingCategory.getKernel().error(e.getMessage(), e);
			alert(e.getMessage());
		}
		landService();
	}

	protected UserServiceEventOutcome readValues(UserServiceObjectContext fromContext)
	{
		this.submitted = true;
		return null;
	}

	@Override
	public void setupObjectContext(
		UserServiceSetupObjectContext<S> aContext,
		UserServiceObjectContextBuilder aBuilder)
	{
		initContext(aContext);
		// when overriding this method, it's best to call initContext in the beginning and then
		// super at the end
		if (context.objectKeys.isEmpty())
			submitted = true;
	}

	protected void initContext(UserServiceSetupObjectContext<S> aContext)
	{
		context.setEntitySelection(aContext.getEntitySelection());
	}

	/**
	 * When using this user service in another user service that may be composing multiple services, or using this one as step,
	 * it is sometimes convenient to superimpose the delegating service context here.  For example, a delegator may construct this service,
	 * call delegateFrom(this) and then call execute in its execute method.
	 * @param delegating
	 */
	public void delegateFrom(AbstractUserService<? extends S> delegating)
	{
		context.copy(delegating.context);
	}

	@Override
	public void validate(UserServiceValidateContext<S> aContext)
	{
	}

	public static class Context<S>
	{
		private UserServiceRequest request;
		private UserServiceWriter writer;
		private UserServiceResourceLocator locator;
		private S entitySelection;
		private final List<ObjectKey> objectKeys = new ArrayList<>();

		public List<ObjectKey> getObjectKeys()
		{
			return objectKeys;
		}

		public S getEntitySelection()
		{
			return entitySelection;
		}

		public void setEntitySelection(S entitySelection)
		{
			this.entitySelection = entitySelection;
		}

		public UserServiceRequest getRequest()
		{
			return request;
		}

		public void setRequest(UserServiceRequest request)
		{
			this.request = request;
		}

		public UserServiceWriter getWriter()
		{
			return writer;
		}

		public void setWriter(UserServiceWriter writer)
		{
			this.writer = writer;
		}

		public Session getSession()
		{
			return request.getSession();
		}

		public UserServiceResourceLocator getLocator()
		{
			return locator;
		}

		public void setLocator(UserServiceResourceLocator locator)
		{
			this.locator = locator;
		}

		public BeanDefinition defineObject(UserServiceObjectContextBuilder aBuilder, ObjectKey key)
		{
			BeanDefinition def = aBuilder.createBeanDefinition();
			aBuilder.registerBean(key, def);
			objectKeys.add(key);
			return def;
		}

		@Deprecated
		public UserServicePaneContext getPaneContext()
		{
			return request instanceof UserServicePaneContext ? (UserServicePaneContext) request
				: null;
		}

		@Deprecated
		public UserServicePaneWriter getPaneWriter()
		{
			return writer instanceof UserServicePaneWriter ? (UserServicePaneWriter) writer : null;
		}

		@Deprecated
		public UserServiceDisplayConfigurator getConfigurator()
		{
			return locator instanceof UserServiceDisplayConfigurator
				? (UserServiceDisplayConfigurator) locator
				: null;
		}

		public void copy(Context<? extends S> otherContext)
		{
			this.entitySelection = otherContext.entitySelection;
			this.locator = otherContext.locator;
			this.writer = otherContext.writer;
			this.request = otherContext.request;
		}

	}

	public BeanElement defineElement(
		BeanDefinition def,
		Path path,
		String label,
		SchemaTypeName type,
		Object defaultValue)
	{
		BeanElement element = def.createElement(path, type);
		element.setLabel(label);
		element.setDefaultValue(defaultValue);
		return element;
	}

	public BeanElement defineElement(BeanDefinition def, SchemaNode copyFrom, ValueContext vc)
	{
		BeanElement element = def
			.createElement(getSimplePath(copyFrom.getPathInAdaptation()), copyFrom.getXsTypeName());
		element.setLabel(copyFrom.getLabel(Locale.getDefault()));
		element.setDefaultValue(copyFrom.getDefaultValue());
		element.setMaxOccurs(copyFrom.getMaxOccurs());
		element.setMinOccurs(copyFrom.getMinOccurs());
		Iterator<SchemaFacet> facetIter = copyFrom.getFacets();
		while (facetIter.hasNext())
		{
			SchemaFacet facet = facetIter.next();
			copyFacet(element, facet, vc);
		}
		return element;
	}

	protected Path getSimplePath(Path path)
	{
		return Path.SELF.add(path.getLastStep());
	}

	private static void copyFacet(BeanElement element, SchemaFacet facet, ValueContext vc)
	{
		if (facet instanceof SchemaFacetTableRef)
			element.addFacetTableRef(((SchemaFacetTableRef) facet).getTable(vc));
		else if (facet instanceof SchemaFacetEnumeration)
			element.addFacetEnumeration(((SchemaFacetEnumeration) facet).getNomenclature());
		else if (facet instanceof SchemaFacetMaxLength)
			element.addFacetMaxLength(((SchemaFacetMaxLength) facet).getValue().intValue());
		else if (facet instanceof SchemaFacetMinLength)
			element.addFacetMinLength(((SchemaFacetMinLength) facet).getValue().intValue());
		else if (facet instanceof SchemaFacetLength)
			element.addFacetLength(((SchemaFacetLength) facet).getValue().intValue());
		else if (facet instanceof SchemaFacetFractionDigits)
			element.addFacetFractionDigits(((SchemaFacetFractionDigits) facet).getFractionDigits());
		else if (facet instanceof SchemaFacetTotalDigits)
			element.addFacetTotalDigits(((SchemaFacetTotalDigits) facet).getTotalDigits());
		else if (facet instanceof SchemaFacetPattern)
			element.addFacetPattern(((SchemaFacetPattern) facet).getPatternString());
	}

	/**
	 * Given an object context created using the context method, layout the
	 * default form
	 *
	 * @param aPaneContext
	 * @param aWriter
	 * @param objectkey
	 */
	protected void writeForm(
		UserServicePaneContext aPaneContext,
		UserServicePaneWriter aWriter,
		ObjectKey objectKey)
	{
		aWriter.setCurrentObject(objectKey);
		ValueContext vc = aPaneContext.getValueContext(objectKey);
		SchemaNode node = vc.getNode();
		aWriter.startTableFormRow();
		writeNode(aWriter, node, true);
		aWriter.endTableFormRow();
	}

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
			aWriter.addFormRow(path);
		}
	}

	public static List<Adaptation> getSelectedRecords(
		TableViewEntitySelection selection,
		boolean all)
	{
		Request selected = selection.getSelectedRecords();
		if (selected == null && all)
			selected = selection.getAllRecords();
		return selected == null ? new ArrayList<>() : AdaptationUtil.getRecords(selected.execute());
	}
}
