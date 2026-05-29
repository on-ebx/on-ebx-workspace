package com.orchestranetworks.ps.util;

import java.io.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.*;

/**
 * Utility class to support UI developments
 *
 */
public class UIUtils
{

	/**
	 * Adds the css link.
	 *
	 * @author MCH
	 *
	 * Add a link tag to import a stylesheet from the current module
	 *
	 * @param pWriter the writer
	 * @param pLocation            The location of the stylesheet from /www/<locale>/stylesheets
	 */
	public static void addCssLink(final UIComponentWriter pWriter, final String pLocation)
	{
		String location = pWriter.getURLForResource(ResourceType.STYLESHEET, pLocation);
		pWriter.add("<link rel=\"stylesheet\" href=\"" + location + "\" > ");
	}

	/**
	 * Adds the ebx private date widget.
	 *
	 * @author MCH
	 * @param pId            used to build the identifier of the the different element that
	 *            compose the widget.
	 * @param pWriter the writer
	 * @param pCalendar the calendar
	 * @return the name of the getter
	 * @deprecated             Add a the standard EBX date Widget in a UIFormRow (must be).
	 *             This method is deprecated because using EBX private API. It
	 *             is not guarantee to work in the future. It must be replaced
	 *             by an other implementation being close to the standard EBX
	 *             one.
	 */
	@Deprecated
	public static void addEBXPrivateDateWidget(
		final String pId,
		final UIComponentWriter pWriter,
		final Calendar pCalendar)
	{
		String dayId = pId + "_day";
		String monthId = pId + "_month";
		String yearId = pId + "_year";

		String day = "";
		String month = "";
		String year = "";
		if (pCalendar != null)
		{
			day = pCalendar.get(Calendar.DAY_OF_MONTH) + "";
			month = pCalendar.get(Calendar.MONTH) + 1 + "";
			year = pCalendar.get(Calendar.YEAR) + "";
		}

		UIButtonSpecJSAction dateButton = new UIButtonSpecJSAction(
			UserMessage.createInfo("Open cal"),
			"EBX_Form.openCalendar('" + pId + "_year','" + pId + "_month','" + pId + "_day','" + pId
				+ "');");
		dateButton.setButtonIcon(UIButtonIcon.CALENDAR);
		dateButton.setButtonLayout(UIButtonLayout.ICON_ONLY);
		dateButton.setCssClass("ebx_Button ebx_IconButton ebx_Calendar");
		pWriter.add("<span id=\"" + pId + "_dateInputs\" class=\"ebx_InputsDateContainer\">");
		pWriter.add(
			"<input type=\"text\" tabindex=\"1\" maxlength=\"2\" size=\"2\" style=\"width: 3ex;text-align: right;\""
				+ " value=\"" + day + "\" name=\"" + dayId + "\" id=\"" + dayId + "\"/>");
		pWriter.add(
			"/<input type=\"text\" tabindex=\"2\" maxlength=\"2\" size=\"2\" style=\"width: 3ex;text-align: right;\" value=\""
				+ month + "\" name=\"" + monthId + "\" id=\"" + monthId + "\" />");
		pWriter.add(
			"/<input type=\"text\" tabindex=\"3\" maxlength=\"4\" size=\"4\" style=\"width: 5ex;text-align: right;\" value=\""
				+ year + "\" name=\"" + yearId + "\" id=\"" + yearId + "\"/ >");
		pWriter.add("</span>");
		pWriter.addButtonJavaScript(dateButton);
	}

	/**
	 * Include an external stylesheet.
	 *
	 * @author MCH
	 * @param pWriter the writer
	 * @param pLocation the location of the external stylesheet
	 */
	public static void addExternalCssLink(final UIComponentWriter pWriter, final String pLocation)
	{
		pWriter.add("<link rel=\"stylesheet\" href=\"" + pLocation + "\" > ");
	}

	/**
	 * Adds the external js library.
	 *
	 * @author MCH
	 *
	 *         Add a script tag to import a javascript file.
	 * @param pWriter the writer
	 * @param pURL            The URL of the javascript library
	 */
	public static void addExternalJSLibrary(final UIComponentWriter pWriter, final String pURL)
	{
		pWriter.add("<script src=\"" + pURL + "\" type=\"text/javascript\"></script>");
	}

	public static void addJQueryFromExternal(
		final UIComponentWriter pWriter,
		final String pLocation)
	{
		pWriter.add("<script>");
		pWriter.add("if (typeof jQuery == 'undefined') { ");
		pWriter.add("var xhrObj = new XMLHttpRequest();");
		pWriter.add("xhrObj.open('GET', \"" + pLocation + "\", false);");
		pWriter.add("xhrObj.send('');");
		pWriter.add("var se = document.createElement('script');");
		pWriter.add("se.type = \"text/javascript\";");
		pWriter.add("se.text = xhrObj.responseText;");
		pWriter.add("document.getElementsByTagName('head')[0].appendChild(se);");
		pWriter.add("}");
		pWriter.add("</script>");
	}

	/**
	 * Adds the js ajax call.
	 *
	 * @author MCH
	 *
	 *         Add a call to the javascript function based on the EBX Ajax
	 *         prototype to call an Ajax Component.
	 * @param pContext the service context
	 * @param pURL url of the ajax component to call
	 * @param pAjaxFunctionName the name of the ajax function
	 * @param string
	 * @throws UnsupportedEncodingException
	 * @deprecated This uses deprecated APIs {@link ServiceContext} and {@link UIServiceComponentWriter}
	 */
	@Deprecated
	public static void addJSAjaxCall(
		final ServiceContext pContext,
		final String pURL,
		final String pParameter,
		final String pAjaxFunctionName)
		throws UnsupportedEncodingException
	{
		UIServiceComponentWriter writer = pContext.getUIComponentWriter();
		writer.addJS_cr("var handler = new " + pAjaxFunctionName + "();");
		writer.addJS_cr("handler.sendRequest('" + pURL + "&'+" + pParameter + ");");
	}

	/**
	 * Adds the js ajax call.
	 *
	 * @author MCH
	 *
	 *         Add a call to the javascript function based on the EBX Ajax
	 *         prototype to call an Ajax Component.
	 * @param pContext the service context
	 * @param pURL url of the ajax component to call
	 * @param pAjaxFunctionName the name of the ajax function
	 * @param string
	 * @throws UnsupportedEncodingException
	 */
	public static void addJSAjaxCall(
		final UIComponentWriter pWriter,
		final String pURL,
		final String pParameter,
		final String pAjaxFunctionName)
		throws UnsupportedEncodingException
	{
		pWriter.addJS_cr("var handler = new " + pAjaxFunctionName + "();");
		pWriter.addJS_cr("handler.sendRequest('" + pURL + "&'+" + pParameter + ");");
	}

	/**
	 * Adds the js ajax call.
	 *
	 * @author MCH
	 *
	 *         Add a call to the javascript function based on the EBX Ajax
	 *         prototype to call an Ajax Component.
	 * @param pContext the context
	 * @param pURL            url of the ajax component to call
	 * @param pAjaxFunctionName            the name of the ajax function
	 */
	@Deprecated
	public static void addJSAjaxCall(
		final UIResponseContext pContext,
		final String pURL,
		final String pAjaxFunctionName)
	{
		pContext.addJS_cr("var handler = new " + pAjaxFunctionName + "();");
		pContext.addJS_cr("handler.sendRequest('" + pURL + "');");
	}

	/**
	 * Adds the js ajax component.
	 *
	 * @author MCH
	 *
	 *         Add a javascript function based on the EBX Ajax prototype to call
	 *         an Ajax Component Facade to the javascript framework offered by
	 *         EBX
	 * @param pWriter the writer
	 * @param pAjaxFunctionName            the name of the ajax function
	 * @param pAjaxSuccessJS            the javascript to execute if the ajax call succeed
	 * @param pAjaxFailedJS            the javascript to execute if the ajax call failed
	 */
	public static void addJSAjaxComponent(
		final UIComponentWriter pWriter,
		final String pAjaxFunctionName,
		final String pAjaxSuccessJS,
		final String pAjaxFailedJS)
	{
		pWriter.addJS_cr(pAjaxFunctionName + " = function(){");
		{
			pWriter.addJS_cr("this.handleAjaxResponseSuccess = function(responseContent){");
			if (pAjaxSuccessJS != null)
			{
				pWriter.addJS_cr(pAjaxSuccessJS);
			}
			pWriter.addJS_cr("};");
		}
		{
			pWriter.addJS_cr("this.handleAjaxResponseFailed = function(responseContent){");
			if (pAjaxFailedJS != null)
			{
				pWriter.addJS_cr(pAjaxFailedJS);
			}
			pWriter.addJS_cr("};");
		}
		pWriter.addJS_cr("};");

		pWriter.addJS_cr(pAjaxFunctionName + ".prototype = new EBX_AJAXResponseHandler();");
	}

	/**
	 * Adds the js library.
	 *
	 * @author MCH
	 *
	 *         Add a script tag to import a javascript file.
	 * @param pWriter the writer
	 * @param pLocation            The location of the javascript library from
	 *            /www/<locale>/jscripts
	 */
	public static void addJSLibrary(final UIComponentWriter pWriter, final String pLocation)
	{
		String location = pWriter.getURLForResource(ResourceType.JSCRIPT, pLocation);
		pWriter.add("<script src=\"" + location + "\" type=\"text/javascript\"></script>");
	}

	/**
	 * Include un javascript file in the current page head.<br>
	 *
	 * @param pWriter
	 *            : The sUIResponse writer.
	 * @param pFileName
	 *            : The JavaScript file name.
	 */
	@Deprecated
	public static void addJSLibraryInHTMLHead(
		final UIResponseContext pWriter,
		final String pFileName)
	{
		final StringBuilder js = new StringBuilder();
		js.append("var fileJS=document.createElement(\"script\");");
		js.append("fileJS.setAttribute(\"type\", \"text/javaScript\");");
		js.append("fileJS.setAttribute(\"src\", '" + pFileName + "');");
		js.append("document.getElementsByTagName(\"head\")[0].appendChild(fileJS);");
		pWriter.addJS(js.toString());
	}

	/**
	 * Adds the js value getter.
	 *
	 * @author MCH
	 *
	 *         Add a javascript method to get the value of a node. Translate the
	 *         java method from public EBX API into a Javascript method and add
	 *         it to a response context.
	 * @param pContext the context
	 * @return the name of the getter
	 */
	@Deprecated
	public static String addJSValueGetter(final UIResponseContext pContext)
	{
		String valueGetterName = UIUtils.getValueGetterName(pContext);
		pContext.addJS_cr("function " + valueGetterName + "(){");
		pContext.addJS("return ");
		pContext.addJS_getNodeValue(Path.SELF);
		pContext.addJS_cr(";");
		pContext.addJS_cr("}");
		return valueGetterName;
	}

	/**
	 * Adds the js value setter.
	 *
	 * @author MCH
	 *
	 *         Add a javascript method to set the value of a node. Translate the
	 *         java method from public EBX API into a Javascript method and add
	 *         it to a response context.
	 * @param pContext the context
	 * @param pPath            path to the node to set
	 * @return the name of the setter
	 */
	@Deprecated
	public static String addJSValueSetter(final UIResponseContext pContext, final Path pPath)
	{
		SchemaNode node = pContext.getNode(pPath);
		String valueSetterName = UIUtils.getValueSetterName(node);
		pContext.addJS_cr("function " + valueSetterName + "(value){");
		pContext.addJS_cr();
		pContext.addJS_setNodeValue("value", pPath);
		pContext.addJS_cr(";");
		pContext.addJS_cr("}");
		return valueSetterName;
	}

	/**
	 * Get the current adaptation of a UIForm. A dataset if in creation mode, a record otherwise.
	 *
	 * @author MCH
	 *
	 * @param pContext a UIFormContext
	 * @return a record or a dataset
	 */
	public static Adaptation getCurrentAdaptation(final UIFormContext pContext)
	{
		Adaptation curentAdaptation = pContext.getCurrentRecord();
		if (curentAdaptation == null)
		{
			curentAdaptation = pContext.getCurrentDataSet();
		}
		return curentAdaptation;
	}

	/**
	 * Get the current adaptation of a UIFormRequestContext. A dataset if in creation mode, a record otherwise.
	 *
	 * @author MCH
	 *
	 * @param pContext a UIFormRequestContext
	 * @return a record or a dataset
	 */
	public static Adaptation getCurrentAdaptation(final UIFormRequestContext pContext)
	{
		Adaptation dataset = pContext.getCurrentDataSet();
		if (pContext.isCreatingRecord())
		{
			return dataset;
		}
		AdaptationTable table = dataset.getTable(pContext.getTableNode().getPathInSchema());
		return table.lookupAdaptationByPrimaryKey(pContext.getValueContext(Path.SELF));
	}

	/**
	 * Gets the field label.
	 *
	 * @author US-Team
	 *
	 * Gets the label for a field
	 * @param pAdaptation the record or data set (for cases where you want a data set level field)
	 * @param pFieldPath the path of the field within the given adaptation
	 * @param pSession the user's session
	 * @param pIncludingGroupLabels Include the labels of the parent group(s) of the field
	 * @return the label
	 */
	public static String getFieldLabel(
		final Adaptation pAdaptation,
		final Path pFieldPath,
		final Session pSession,
		final boolean pIncludingGroupLabels)
	{
		SchemaNode node = pAdaptation.getSchemaNode().getNode(pFieldPath);
		StringBuilder bldr = new StringBuilder();
		Locale locale = pSession.getLocale();
		bldr.append(node.getLabel(locale));
		if (pIncludingGroupLabels)
		{
			Path tablePath = node.getTableNode().getPathInSchema();
			// Loop through the parents until you get to a table node and for each group add its
			// label
			for (SchemaNode parentNode = node; (parentNode = parentNode
				.getNode(Path.PARENT)) != null && !tablePath.equals(parentNode.getPathInSchema());)
			{
				bldr.insert(0, " / ");
				bldr.insert(0, parentNode.getLabel(locale));
			}
		}
		return bldr.toString();
	}

	/**
	 * Gets the node path from unique web identier.
	 *
	 * @author MCH
	 *
	 *         Retrieve the xpath of a given node from its unique web identifier
	 *         generated by getUniqueWebIdentifierForNode
	 * @param pNodeId            a schema node unique identifier
	 * @return the schema node's xpath
	 * @see getUniqueWebIdentifierForNode
	 */
	public static Path getNodePathFromUniqueWebIdentier(final String pNodeId)
	{
		return Path.parse(pNodeId.replaceAll("___", "/"));
	}

	/**
	 * Gets the unique js function name.
	 *
	 * @author MCH
	 * @param pContext the context
	 * @param pFunctionName the function name
	 * @return a unique function identifier
	 */
	@Deprecated
	public static String getUniqueJSFunctionName(
		final UIResponseContext pContext,
		final String pFunctionName)
	{
		String id = UIUtils.getUniqueWebIdentifierForCurrentNode(pContext);
		return id + "__" + pFunctionName;
	}

	/**
	 * Gets the unique js function or var name.
	 *
	 * @author BKL
	 * @param pNode the node
	 * @param pName the function or var name
	 * @return a unique function identifier
	 */
	public static String getUniqueJSName(final SchemaNode pNode, final String pName)
	{
		String id = UIUtils.getUniqueWebIdentifierForNode(pNode);
		return id + "__" + pName;
	}

	/**
	 * Gets the unique js function or var name.
	 *
	 * @author BKL
	 * @param pfieldId the node id
	 * @param pName the function or var name
	 * @return a unique function identifier
	 */
	public static String getUniqueJSName(final String pfieldId, final String pName)
	{
		return pfieldId + "__" + pName;
	}

	/**
	 * Gets the unique web identifier for current node.
	 *
	 * @author MCH
	 * @param pContext            a UIResponseContext instance.
	 * @return a unique identifier for the current node.
	 * @see getUniqueWebIdentifierForNode
	 */
	@Deprecated
	public static String getUniqueWebIdentifierForCurrentNode(final UIResponseContext pContext)
	{
		SchemaNode node = pContext.getNode();
		return UIUtils.getUniqueWebIdentifierForNode(node);
	}

	/**
	 * Gets the unique web identifier for node.
	 *
	 * @author MCH
	 *
	 *         Generate a unique web identifier based on the schema node xpath.
	 *         The character '/' is replaced by 3 '_'. The same construction
	 *         with 2 '_' is already used by EBX.
	 *
	 *         UIResponseContext.getWebName() can be used as a unique identifier
	 *         but cannot be used from a UIForm and is not well supported
	 *         anymore.
	 * @param pNode            a a schema node.
	 * @return a unique identifier for the node in parameter.
	 */
	public static String getUniqueWebIdentifierForNode(final SchemaNode pNode)
	{
		String id = pNode.getPathInAdaptation().format().replace("/", "___");
		return id.replace(".", "");
	}

	/**
	 * Gets the unique web identifier for a record.
	 *
	 * @author MCH
	 *
	 *         Generate a unique web identifier based on the record primary key.
	 *         The character '|' is replaced by 3 '_'. The same construction
	 *         with 2 '_' is already used by EBX.
	 *
	 *         UIResponseContext.getWebName() can be used as a unique identifier
	 *         but cannot be used from a UIForm and is not well supported
	 *         anymore.
	 * @param pRecord an Adaptation corresponding to a record.
	 * @return a unique identifier for the record in parameter.
	 */
	public static String getUniqueWebIdentifierForRecord(final Adaptation pRecord)
	{
		return pRecord.getOccurrencePrimaryKey().format().replace("|", "___");
	}

	/**
	 * Gets the value getter name.
	 *
	 * @author MCH
	 * @param pContext the context
	 * @return the name of the getter created with addJSValueGetter
	 * @see addJSValueSetter
	 */
	@Deprecated
	public static String getValueGetterName(final UIResponseContext pContext)
	{
		String id = UIUtils.getUniqueWebIdentifierForCurrentNode(pContext);
		return "get" + id + "Value";
	}

	/**
	 * Gets the value setter name.
	 *
	 * @author MCH
	 * @param pNode the node
	 * @return the name of the setter created with addJSValueSetter
	 * @see addJSValueSetter
	 */
	public static String getValueSetterName(final SchemaNode pNode)
	{
		String id = UIUtils.getUniqueWebIdentifierForNode(pNode);
		return "set" + id + "Value";
	}

	/**
	 * Verify if a field is writable
	 *
	 * @author MCH
	 * @param pNode a node
	 * @param pContext a form context
	 * @return true if the field is not writable.
	 */
	public static boolean isNodeReadOnlyOrHidden(
		final SchemaNode pNode,
		final UIFormContext pContext)
	{
		Adaptation adaptation = pContext.getCurrentRecord();
		if (adaptation == null)
		{
			adaptation = pContext.getCurrentDataSet();
		}
		return !pContext.getSession()
			.getPermissions()
			.getNodeAccessPermission(pNode, adaptation)
			.isReadWrite();
	}
}
