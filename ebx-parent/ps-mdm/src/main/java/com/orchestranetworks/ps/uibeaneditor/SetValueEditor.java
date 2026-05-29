package com.orchestranetworks.ps.uibeaneditor;

import com.orchestranetworks.ps.ui.ajax.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.base.*;
import com.orchestranetworks.ui.form.widget.*;

// TODO write an article on this
/**
 * 
 *         This editor aims to set the value of a field according to the current
 *         value.
 * 
 *         It is designed to be used on an foreign key to set a simple string,
 *         integer or boolean <br>
 *         at location pathToTarget with value at location pathToSource (in
 *         record referenced by the FK).
 * 
 * <pre>{@code
 *         <osd:uiBean class="com.orchestranetworks.ps.ui.bean.SetValueEditor">
 *         	<pathToSource>${absolute.path.from.referenced.record.root}</pathToSource>
 *         	<pathToTarget>${relative.path.from.current.node}</pathToTarget>
 *         </osd:uiBean>
 * 
 *         This editor relies on an AJAX component that must be declared as
 *         follow in the data model :
 *}</pre>
 * 
 * <pre>{@code
 *         <xs:complexType name="SetValueAjaxComponent">
 *         	<xs:annotation>
 *         		<xs:appinfo>
 *         			<osd:ajaxComponent class="com.orchestranetworks.ps.ui.ajax.SetValueAjaxComponent"/>
 *         		</xs:appinfo>
 *         	</xs:annotation>
 *         </xs:complexType>
 *}</pre>
 *         
 * @author MCH
 * @see SetValueAjaxComponent
 */
@Deprecated
public class SetValueEditor extends UIBeanEditor
{

	/** The path to target. */
	private String pathToTarget;

	/** The path to source. */
	private String pathToSource;

	/*
	 * @see com.orchestranetworks.ui.UIBeanEditor#addForDisplay(com.orchestranetworks.ui.
	 * UIResponseContext)
	 */
	@Override
	public void addForDisplay(final UIResponseContext pContext)
	{
		pContext.addUIBestMatchingComponent(Path.SELF, "");
	}

	/*
	 * @see
	 * com.orchestranetworks.ui.UIBeanEditor#addForEdit(com.orchestranetworks.ui.UIResponseContext)
	 */
	@Override
	public void addForEdit(final UIResponseContext pContext)
	{
		UIComboBox widget = pContext.newComboBox(Path.SELF);
		String onChangeFunctionName = UIUtils.getUniqueJSFunctionName(pContext, "onChange");
		this.addJsFunctions(pContext, onChangeFunctionName);
		widget.setActionOnAfterValueChanged(JsFunctionCall.on(onChangeFunctionName));
		pContext.addWidget(widget);
	}

	/**
	 * Adds the js functions.
	 *
	 * @param pContext the context
	 * @param pOnChangeFunctionName the on change function name
	 */
	private void addJsFunctions(
		final UIResponseContext pContext,
		final String pOnChangeFunctionName)
	{
		this.addOnChangeJsFunction(pContext, pOnChangeFunctionName);
		UIUtils.addJSValueGetter(pContext);
		UIUtils.addJSValueSetter(pContext, Path.parse(this.pathToTarget));
	}

	/**
	 * Adds the on change js function.
	 *
	 * @param pContext the context
	 * @param pOnChangeFunctionName the on change function name
	 */
	private void addOnChangeJsFunction(
		final UIResponseContext pContext,
		final String pOnChangeFunctionName)
	{
		String ajaxPrototypeName = UIUtils.getUniqueJSFunctionName(pContext, "SetValueHandler");
		SchemaNode targetNode = pContext.getNode(Path.parse(this.pathToTarget));
		String ajaxSuccess = UIUtils.getValueSetterName(targetNode) + "(responseContent);";
		UIUtils.addJSAjaxComponent(pContext, ajaxPrototypeName, ajaxSuccess, null);

		String url = pContext.getURLForAjaxComponent(SetValueAjaxComponent.AJAX_COMP_NAME);
		url += "&" + SetValueAjaxComponent.VALUE_PARAM + "='+value+'";
		url += "&" + SetValueAjaxComponent.NODE_ID_PARAM + "="
			+ UIUtils.getUniqueWebIdentifierForCurrentNode(pContext);
		url += "&" + SetValueAjaxComponent.PATH_SOURCE_PARAM + "=" + this.pathToSource;

		pContext.addJS_cr("function " + pOnChangeFunctionName + "(){");
		pContext.addJS_cr("var value = " + UIUtils.getValueGetterName(pContext) + "().key;");
		UIUtils.addJSAjaxCall(pContext, url, ajaxPrototypeName);
		pContext.addJS_cr("};");
	}

	/**
	 * Gets the path to source.
	 *
	 * @return the path to source
	 */
	public String getPathToSource()
	{
		return this.pathToSource;
	}

	/**
	 * Gets the path to target.
	 *
	 * @return the path to target
	 */
	public String getPathToTarget()
	{
		return this.pathToTarget;
	}

	/**
	 * Sets the path to source.
	 *
	 * @param pathToSource the new path to source
	 */
	public void setPathToSource(final String pathToSource)
	{
		this.pathToSource = pathToSource;
	}

	/**
	 * Sets the path to target.
	 *
	 * @param pathToTarget the new path to target
	 */
	public void setPathToTarget(final String pathToTarget)
	{
		this.pathToTarget = pathToTarget;
	}
}
