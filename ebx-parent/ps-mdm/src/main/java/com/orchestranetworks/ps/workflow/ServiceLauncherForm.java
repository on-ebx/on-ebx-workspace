/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.workflow;

import java.text.*;
import java.util.*;

import com.onwbp.base.text.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

import jakarta.servlet.http.*;

/**
 * Abstract class that handles creating the basics of a <code>ServiceLauncherForm</code>.
 * This class also provides utility methods to help add fields and defining JavaScript Functions
 * for canceling the form (called via Cancel button) and validating the form when
 * required fields are specified (called via Submit button).
 * <p>
 * Expectation is that the child class will write the body of the form by implementing the
 * {@link #writeForm(ServiceContext)} abstract method.
 * <p>
 * The utility methods that create UI Form input fields(for example, try to maintain the same look and feel
 * as all EBX GUI elements. This form also provides the ability to mark an input element as
 * mandatory, and display an error message if the field is not provided a value on the form.
 * <p>
 * <strong>Note:</strong>
 * There is a better way to add error messages. Currently work is being done to improve the Error Message
 * to allow for the EBX Icon Error image to appear next to the error message.
 *
 * @see
 */
@Deprecated
public abstract class ServiceLauncherForm
{
	/**
	 * The function name that must be called when the Submit button is selected.
	 */
	protected static final String FORM_VALIDITY_FUNCTION_NAME = "checkFormValidity";

	/**
	 * Part of the Error Message div tag id string used to write error messages on the Form.
	 */
	private static final String ERROR_MESSAGE_ID = "ErrorMessage";

	/**
	 * The Form Validation Function. This String will be used to generate the error message.
	 * <p>
	 * In the end the generated Form Validation Function should generate a similar function as
	 * displayed below:
	 * <pre><code>
	 * function checkFormValidity(formEl) {
	 *     var NameVar = formEl.elements["workflowInstanceName"].value;
	 *     var isValid = true;
	 *
	 *     if (NameVar == null || NameVar == "") {
	 *         alert("Field 'Name' is mandatory.");
	 *
	 *         document.getElementById("NameErrorMessage").innerHTML = "&nbsp&nbsp&nbsp&nbspField 'Name' is mandatory.";
	 *         isValid = false;
	 *     }
	 *
	 *     return isValid;
	 * }
	 * </pre></code>
	 */
	protected static final String FORM_VALIDITY_FUNCTION = "function checkFormValidity(formEl) '{'"
		+ "    var isValid = true;" + " " + "    {0}" + "  " + "    return isValid;" + "'}'";

	/**
	 * The Form Validation Function Body. This String will be used to generate each validation check
	 * for each required field.
	 * <p>
	 * <strong>Note:</strong>Use of single apostrophes around curly braces are in place to properly format
	 * this message using the MessageFormat.format() call.
	 */
	protected static final String FORM_VALIDITY_FUNCTION_BODY = "    var {0}Var = formEl.elements[\"{0}\"].value;"
		+ "    if ({0}Var == null || {0}Var == \"\") '{'"
		+ "        alert(\"Field ''{1}'' is mandatory.\"); "
		+ "        document.getElementById(\"{2}\").innerHTML = \"&nbsp&nbsp&nbsp&nbspField ''{1}'' is mandatory.\";"
		+ "        isValid = false;" + "     '}' " + " ";

	/**
	 * The <code>cancelForm()</code> JS Function signature.
	 */
	protected static final String FORM_CANCEL_FUNCTION = "function cancelForm()" + "'{'" + "   {0}"
		+ "'}'";

	/**
	 * The specific JS Function call (command) that is associated with the Cancel button.
	 */
	protected static final String FORM_CANCEL_FUNCTION_CALL = "cancelForm();";

	/**
	 * A HashMap to maintain a mapping of Required Fields. The Expectation is that
	 * each entry is represented by the tag's <code>id</code> attribute, as the key,
	 * and a FieldEntry instance containing the field name and field's label, as the value.
	 */
	protected Map<String, String> requiredFields = new HashMap<>();

	/**
	 * Used to determine whether or not the Submit button should be suppressed.
	 * <p>
	 * <strong>Note:</strong> There is not an equivalent flag for the Cancel button.
	 * The Cancel button should always be provided to give the user the ability
	 * to exit out of the Workflow.
	 */
	protected boolean suppressSubmitButton = false;

	protected abstract String getServletName();

	/**
	 * Implementation should define the fields that will be created for the UIForm that
	 * will be presented to the user.
	 *
	 * @param sContext	ServiceContext used to write to the UIForm.
	 *
	 * @throws OperationException
	 */
	protected abstract void writeForm(ServiceContext sContext) throws OperationException;

	/**
	 * Return true indicating that the service form does not want an alert presented
	 * when a modification has been made, and the user selects Submit or Cancel.
	 * <p>
	 * It is up to the child service to override this method to change the expectation
	 * for that service(i.e. show the alert).
	 *
	 * @return true, unless overriden by child.
	 */
	protected boolean isLostModificationDetectionDisabled()
	{
		return true;
	}

	public void execute(HttpServletRequest request) throws OperationException
	{
		ServiceContext sContext = ServiceContext.getServiceContext(request);

		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		UIFormSpec formSpec = createFormSpec(sContext);

		formSpec.setDetectionOfLostModificationDisabled(isLostModificationDetectionDisabled());

		writer.startForm(formSpec);
		try
		{
			writeForm(sContext);
		}
		finally
		{
			writer.endForm();
		}

		writeFormButtonsJS(sContext);
	}

	/**
	 * Creates the UIFormSpec with the Cancel and Submit buttons to the form. If the Workflow
	 * is configured to suppress the submit button, then only the Cancel button will be created.
	 *
	 * @param sContext SerciceContext to write the UIFormSpec.
	 *
	 * @return The UIFormSpec.
	 *
	 * @see #suppressSubmitButton(boolean)
	 */
	protected UIFormSpec createFormSpec(ServiceContext sContext)
	{
		UIFormSpec formSpec = new UIFormSpec();
		formSpec.setURLForAction(sContext.getURLForIncludingResource(getServletName()));

		UIButtonSpec cancelButtonSpec = new UIButtonSpecJSAction(
			UserMessage.createInfo("Cancel"),
			FORM_CANCEL_FUNCTION_CALL);
		formSpec.addActionBackInBottomBar(cancelButtonSpec);

		if (!suppressSubmitButton)
		{
			UIButtonSpec submitButtonSpec = new UIButtonSpecSubmit(
				UserMessage.createInfo("Submit"),
				"submit",
				"submit");

			formSpec.setJsFnNameToCallBeforeSubmit(FORM_VALIDITY_FUNCTION_NAME);
			formSpec.addActionForwardInBottomBar(submitButtonSpec);
		}

		return formSpec;
	}

	/**
	 * Generates the <code>checkFormValidity()</code> JS Function that is used to execute a
	 * check on required fields. If no fields were set to be required then this method will
	 * return right away.
	 *
	 * @param sContext ServiceContext used to write the JS Function.
	 */
	protected void writeFormValidationFunction(ServiceContext sContext)
	{
		// Nothing to see here folks, move it along
		if (requiredFields.isEmpty())
			return;

		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		String jsFunctionBody = generateFormValidationFunctionBody(sContext);

		String jsFunction = MessageFormat.format(FORM_VALIDITY_FUNCTION, jsFunctionBody);

		writer.addJS_cr(jsFunction);
	}

	/**
	 * Generate the JS Validation Function. Uses the requiredFields
	 * @return
	 */
	protected String generateFormValidationFunctionBody(ServiceContext sContext)
	{
		StringBuilder jsFunctionBody = new StringBuilder();
		for (Map.Entry<String, String> requiredField : requiredFields.entrySet())
		{

			jsFunctionBody.append(
				MessageFormat.format(
				FORM_VALIDITY_FUNCTION_BODY,
					requiredField.getKey(), // Fill in the Field's id, used both for Var and element
											// lookup
				requiredField.getValue(), // Fill in the Field's label for the required field.
					generateErrorMsgId(requiredField.getKey()))); // Fill in the Error Message Div
																	// Id, so validation function
																	// can find it.
		}
		return jsFunctionBody.toString();
	}

	/**
	 * Provides the option of suppressing the Submit button, if an error condition exists.
	 * This was put in placed to allow the child to hide the submit on the form, if there
	 * was an error condition that would cause the launcher to fail if user submitted the
	 * form.
	 *
	 * @param suppress True to suppress the Submit button.
	 */
	protected void suppressSubmitButton(boolean suppress)
	{
		this.suppressSubmitButton = suppress;
	}

	/**
	 * Writes the <code>cancelForm()</code> JS Function that is used to cleanly cancel the form.
	 * This writes a function that redirects the screen on cancel.
	 * <p>
	 * <strong>Note:</strong> If the <code>cancelForm()</code> functionality needs to be modified
	 * for a child, then the child implementation should override the <code>generateFormCancelFunctionBody()</code>
	 * method. this method will adhere to the JS Function signature that is maintained in the <code>createFormSpec()</code>.
	 *
	 * @param sContext ServiceContext used to write the JS Function.
	 *
	 * @see com.orchestranetworks.service.ServiceContext#getURLForEndingService()
	 * @see #generateFormCancelFunctionBody(ServiceContext)
	 */
	protected void writeFormCancelFunction(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		// On cancel, just redirect the screen
		writer.addJS_cr(
			MessageFormat
				.format(FORM_CANCEL_FUNCTION, this.generateFormCancelFunctionBody(sContext)));
		writer.addJS_cr();

	}

	/**
	 * Generates the body of the <code>cancelForm()</code> JS Function. If a child class would
	 * like to modify the functionality of the <code>cancelForm()</code> function, then it is
	 * recommended to override this method.
	 *
	 * @param sContext ServiceContext used to write the JS Function to the Form.
	 *
	 * @return A String representing the functional body of the <code>cancelForm()</code> JS
	 * 		   Function that is associated with the Cancel button.
	 */
	protected String generateFormCancelFunctionBody(ServiceContext sContext)
	{
		return "window.location.href='" + sContext.getURLForEndingService() + "';";
	}

	protected void writeFormButtonsJS(ServiceContext sContext)
	{
		writeFormCancelFunction(sContext);
		writeFormValidationFunction(sContext);
	}

	protected void writeHiddenInput(ServiceContext sContext, String id, String value)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.add("<input type='hidden' id='" + id + "' name='" + id + "'");
		if (value != null)
		{
			writer.add(" value='" + value + "'");
		}
		writer.add_cr("/>");
	}

	protected void startFieldList(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.add_cr("<table class=\"ebx_FieldList\"><tbody>");
	}

	protected void endFieldList(ServiceContext sContext)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer
			.add_cr("<tr class=\"ebx_FieldListBedrock\"><td></td><td></td><td></td><td></td></tr>");
		writer.add_cr("</tbody></table>");
	}

	/**
	 * Writes a Input Text Field to the Form. This method attempts to keep the fields aligned with other
	 * input fields. This implementation takes into account how to render the field based on whether
	 * the field is required or not.
	 *
	 * @param sContext	ServiceContext used to write to the UIForm.
	 * @param fieldLabel A String representing the label that accompanies the input field. This is used
	 * 					 to help with defining a user readable error message, if the field is required.
	 * @param id		 A String representing the input text field id attribute. Also used to keep track
	 * 					 of required fields.
	 * @param name		 A String representing the input text field name attribute.
	 * @param maxLength	 A String representing the length of the input text field.
	 * @param initialValue	A String representing the initial value of the input text field.
	 * @param isRequired	A boolean used to determine if the input text field should be required by the system. If <code>true</code>
	 * 						the id and fieldLable will be used in the creation of the validation JS function. If <code>false</code>
	 * 						the method will make sure that this field will align properly with other input fields that may be required.
	 */
	protected void writeInputTextField(
		ServiceContext sContext,
		String fieldLabel,
		String id,
		String name,
		String maxLength,
		String initialValue,
		boolean isRequired)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		// If required mark the field as such, if not put two spaces in its place to align fields
		// Note: Aligning with spaces are not ideal. We should try to replace the else with either
		//       a different css class, or fine tune the form to optionally use a table tag.
		//
		// TODO: Decide on proper alignment implementation for non-required fields. Right now using
		// spaces.

		if (isRequired)
		{
			writer.add_cr("<span class=\"ebx_MandatoryFlag\">*</span></td>");
			setRequiredFieldId(id, fieldLabel);
		}
		else
			writer.add_cr("<span class=\"ebx_MandatoryFlag\">&nbsp&nbsp</span>");

		writer.add_cr(
			"<input type=\"text\" class=\"ebx_APV\" name=\"" + name + "\" id=\"" + id
			+ "\" maxlength=\"" + maxLength + "\"value=\"" + initialValue + "\">");

		if (isRequired)
			writer.add_cr("<div class=\"ebx_Error\" id=\"" + generateErrorMsgId(id) + "\"></div>");

	}
	
	/**
	 * Writes a Input Number Field to the Form. This method attempts to keep the fields aligned with other 
	 * input fields. This implementation takes into account how to render the field based on whether 
	 * the field is required or not. 
	 * 
	 * @param sContext	ServiceContext used to write to the UIForm. 
	 * @param fieldLabel A String representing the label that accompanies the input field. This is used
	 * 					 to help with defining a user readable error message, if the field is required.
	 * @param id		 A String representing the input number field id attribute. Also used to keep track
	 * 					 of required fields. 
	 * @param name		 A String representing the input number field name attribute. 
	 * @param initialValue	A String representing the initial value of the input number field.
	 * @param minValue	 A String representing the minimum value of the input number field 
	 * @param maxValue	 A String representing the maximum value of the input number field
	 * @param isRequired	A boolean used to determine if the input number field should be required by the system. If <code>true</code>
	 * 						the id and fieldLable will be used in the creation of the validation JS function. If <code>false</code>
	 * 						the method will make sure that this field will align properly with other input fields that may be required. 
	 */
	protected void writeInputNumberField(
		ServiceContext sContext,
		String fieldLabel,
		String id,
		String name,
		String initialValue,
		String minValue,
		String maxValue,
		boolean isRequired)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		// If required, mark the field as such, if not put two spaces in its place to align fields 
		// Note: Aligning with spaces are not ideal. We should try to replace the else with either
		//       a different css class, or fine tune the form to optionally use a table tag. 
		//
		// TODO: Decide on proper alignment implementation for non-required fields. Right now using spaces.

		if (isRequired)
		{
			writer.add_cr("<span class=\"ebx_MandatoryFlag\">*</span></td>");
			setRequiredFieldId(id, fieldLabel);
		}
		else
			writer.add_cr("<span class=\"ebx_MandatoryFlag\">&nbsp&nbsp</span>");
		// the "number" field restricts all decimal values by default except .0 (e.g. 2.0, 3.0, etc..)
		// hence the onkeypress javascript to restrict values from 0-9.
		if(isRequired)
			writer.add_cr("<input type=\"number\" class=\"ebx_APV\" name=\"" + name + "\" id=\"" + id
					+ "\" min=\"" + minValue + "\" max=\"" + maxValue + "\" value=\"" + initialValue
					+ "\" onkeypress=\"return event.charCode >= 48 && event.charCode <= 57\" required>");
		else
			writer.add_cr("<input type=\"number\" class=\"ebx_APV\" name=\"" + name + "\" id=\"" + id
					+ "\" min=\"" + minValue + "\" max=\"" + maxValue + "\" value=\"" + initialValue
					+ "\" onkeypress=\"return event.charCode >= 48 && event.charCode <= 57\">");

		// Since we are using the default validation and error message provided by the "required" attribute of the "number" type field, 
		// we do not need to generate ebx error message.
		//	if (isRequired)
		//		writer.add_cr("<div class=\"ebx_Error\" id=\"" + generateErrorMsgId(id) + "\"></div>");

	}

	/**
	 * Writes an Input Text Area to the Form. This method attempts to keep the fields aligned with other
	 * input fields. This implementation takes into account how to render the field based on whether
	 * the field is required or not.
	 *
	 * @param sContext	ServiceContext used to write to the UIForm.
	 * @param fieldLabel A String representing the label that accompanies the text area field. This is used
	 * 					 to help with defining a user readable error message, if the field is required.
	 * @param id		 A String representing the input text area id attribute. Also used to keep track
	 * 					 of required fields.
	 * @param name		 A String representing the input text area name attribute.
	 * @param maxLength	 A String representing the length of the input text area.
	 * @param columnSize A String representing the text area column size.
	 * @param rowSize	 A String representing the text area row size.
	 * @param initialValue	A String representing the initial value of the input text area.
	 * @param isRequired	A boolean used to determine if the input text area should be required by the system. If <code>true</code>
	 * 						the id and fieldLabel will be used in the creation of the validation JS function. If <code>false</code>
	 * 						the method will make sure that this field will align properly with other input fields that may be required.
	 */
	protected void writeInputTextAreaField(
		ServiceContext sContext,
		String fieldLabel,
		String id,
		String name,
		String maxLength,
		String initialValue,
		String columnSize,
		String rowSize,
		boolean isRequired)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();

		// If required mark the field as such, if not put two spaces in its place to align fields
		// Note: Aligning with spaces are not ideal. We should try to replace the else with either
		//       a different css class, or fine tune the form to optionally use a table tag.
		//
		// TODO: Implement a proper alignment implementation for non-required fields. Right now
		// using spaces.
		//
		//
		if (isRequired)
		{
			writer.add_cr("<span class=\"ebx_MandatoryFlag\">*</span></td>");
			setRequiredFieldId(id, fieldLabel);
		}
		else
			writer.add_cr("<span class=\"ebx_MandatoryFlag\">&nbsp&nbsp</span>");

		writer.add_cr(
			"<textarea class=\"ebx_APV\" name=\"" + name + "\" id=\"" + id + "\" cols='"
			+ columnSize + "' rows='" + rowSize + "' " + "maxlength=\"" + maxLength + "\">"
			+ initialValue + "</textarea>");

		if (isRequired)
			writer.add_cr("<div class=\"ebx_Error\" id=\"" + generateErrorMsgId(id) + "\"></div>");
	}

	/**
	 * Adds a Error Message Div tag to the form with the EBX Error Icon.
	 *
	 * @param sContext ServiceContext used to write the error to the UIForm.
	 * @param id	A String representing the id attribute for the error div tag.
	 * @param errorMessage	The error message to write to the UIForm.
	 */
	protected void writeErrorMessage(ServiceContext sContext, String id, String errorMessage)
	{
		UIServiceComponentWriter writer = sContext.getUIComponentWriter();
		writer.startFormRow(new UIFormLabelSpec("    "));
		writer.add_cr("<div class=\"ebx_MessageContainer\"><div class=\"ebx_IconError\">");
		writer.add_cr(
			"<div class=\"ebx_Error\" id=\"" + this.generateErrorMsgId(id) + "\">" + errorMessage
				+ "</div></div></div>");
		writer.endFormRow();
	}

	/**
	 * Adds the id and field label passed in, to a Map representing Required Fields
	 * on the form. The expectation is that the id (used as a key in the map) represents
	 * an actual field on the form, and the fieldLabel (maintained as the value)
	 * represents the user friendly label.
	 * <p>
	 * <strong>Note:</strong> If the id passed in does not reflect an id on the
	 * form of a specific field, then it will be ignored. There is no verification
	 * that an id actually exists on the form.
	 *
	 * @param id A String representing a input fields "id". This value will be used
	 *           to create a JS validation function, that will be called upon form
	 *           Submit.
	 * <p>
	 * @param fieldLabel A String representing the user friendly field Label. This
	 * 					 will be used to create a readable alert and error message.
	 */
	protected void setRequiredFieldId(String id, String fieldLabel)
	{
		requiredFields.put(id, fieldLabel);
	}

	/**
	 * Generates the expected id value for Error Message div tags.
	 *
	 * @param prefix String that will be used to prefix the Error Message id value,
	 * 				 to make it a unique within the checkFormValidity() JS Function.
	 *
	 * @return String representing the id value for an Error Message div tag.
	 */
	protected String generateErrorMsgId(String prefix)
	{
		String errorMsgId = new String(prefix + ERROR_MESSAGE_ID);
		return errorMsgId;
	}

}