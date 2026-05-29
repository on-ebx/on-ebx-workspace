package com.orchestranetworks.ps.service;

import java.io.*;
import java.util.*;

import com.onwbp.base.text.*;
import com.onwbp.org.apache.commons.io.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.userservice.*;

public class UserServiceUtilities
{
	public static void writeDowloadLink(
		UserServiceRequest aContext,
		UserServiceWriter aWriter,
		final File file)
	{
		aWriter.add("<div ");
		aWriter.addSafeAttribute("class", UICSSClasses.CONTAINER_WITH_TEXT_PADDING);
		aWriter.add(">");

		String downloadURL = aWriter.getURLForGetRequest(new UserServiceGetRequest()
		{
			@Override
			public void processRequest(
				UserServiceGetContext aContext,
				UserServiceGetResponse aResponse)
			{
				aResponse.setContentType("text/plain;charset=utf-8");
				aResponse.setHeader(
					"Content-Disposition",
					"attachment; filename=\"" + file.getName() + "\"");
				PrintWriter writer = new PrintWriter(aResponse.getOutputStream());
				try
				{
					IOUtils.copy(new FileReader(file), writer);
				}
				catch (IOException e)
				{

				}
				finally
				{
					writer.close();
				}
			}
		});

		aWriter.add("<a ");
		aWriter.addSafeAttribute("href", downloadURL);
		aWriter.add(">Click here to download " + file.getName() + "</a>");

		aWriter.add("</div>");
	}

	public static void landService(UserServiceWriter writer, String url)
	{
		writer.addJS_cr("window.location.href='" + url + "';");
	}

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
	public static boolean isBooleanParamSet(String[] paramValues)
	{
		return paramValues != null && paramValues.length != 0 && paramValues[0].equals("true");
	}

	public static void alert(UserServicePaneWriter writer, String message)
	{
		writer.addJS("alert('" + message + "');");
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

	public static void displayInFrameWithCloseButton(
		UserServiceResourceLocator locator,
		UserServiceWriter writer,
		String frameId,
		String componentUri)
	{
		writer.addJS_addResizeWorkspaceListener("resizeFrame");
		UIButtonSpecJSAction cancelButtonSpec = new UIButtonSpecJSAction(
			UserMessage.createInfo("Close"),
			"cancelForm();");
		writer.addButtonJavaScript(cancelButtonSpec);
		writer.add_cr(
			"<iframe id='" + frameId + "' name='" + frameId
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
		writer.addJS_cr("  window.location.href='" + locator.getURLForEndingService() + "';");
		writer.addJS_cr("}");
	}

}
