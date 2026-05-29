package com.orchestranetworks.ps.uibeaneditor;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;
import com.orchestranetworks.ui.form.widget.*;

@Deprecated
public class ReadOnlyUIBeanEditor extends UIBeanEditor
{
	protected static final String SEPARATOR = ",";

	private String editorRoles;
	private boolean neverEditable;
	private boolean neverValidate;

	@Override
	public void addForDisplay(UIResponseContext context)
	{
		doAddForDisplay(context);
	}

	@Override
	public void addForEdit(UIResponseContext context)
	{
		boolean readOnly = isReadOnly(context);
		if (readOnly)
		{
			doAddForDisplay(context);
		}
		else
		{
			doAddForEdit(context);
		}
	}

	@Override
	public void addForDisplayInTable(UIResponseContext context)
	{
		doAddForDisplay(context);
	}

	@Override
	public boolean shallValidateInput(UIRequestContext context)
	{
		return super.shallValidateInput(context) && !isNeverValidate() && !isReadOnly(context);
	}

	/**
	 * Get a comma-separated list of roles that can edit
	 * 
	 * @return the roles
	 */
	public String getEditorRoles()
	{
		return this.editorRoles;
	}

	/**
	 * Set a comma-separated list of roles that can edit
	 * 
	 * @param editorRoles the roles
	 */
	public void setEditorRoles(String editorRoles)
	{
		this.editorRoles = editorRoles;
	}

	/**
	 * Get whether this is never editable. If never editable, no one can edit it even if their
	 * roles are specified as editable.
	 * 
	 * @return whether this is never editable
	 */
	public boolean isNeverEditable()
	{
		return this.neverEditable;
	}

	/**
	 * Set whether this is never editable. If never editable, no one can edit it even if their
	 * roles are specified as editable.
	 * 
	 * @param neverEditable whether this is never editable
	 */
	public void setNeverEditable(boolean neverEditable)
	{
		this.neverEditable = neverEditable;
	}

	/**
	 * Get whether this never validates. By default, the editor doesn't validate when it's read only and otherwise it validates.
	 * This will allow you to specify that it should never validate regardless of if it's editable.
	 * 
	 * @return whether this never validates
	 */
	public boolean isNeverValidate()
	{
		return this.neverValidate;
	}

	/**
	 * Set whether this never validates. By default, the editor doesn't validate when it's read only and otherwise it validates.
	 * This will allow you to specify that it should never validate regardless of if it's editable.
	 * 
	 * @param neverValidate whether this never validates
	 */
	public void setNeverValidate(boolean neverValidate)
	{
		this.neverValidate = neverValidate;
	}

	/**
	 * Determine if the editor should be read-only
	 * 
	 * @param context the context
	 * @return whether the editor should be read-only
	 */
	protected boolean isReadOnly(UIContext context)
	{
		return isNeverEditable() || !isUserAlwaysReadWrite(context.getSession());
	}

	/**
	 * Checks if a user is always read/write, based on the specified <code>editorRoles</code>
	 *
	 * @param session the session
	 * @return whether the user is always read/write
	 */
	protected boolean isUserAlwaysReadWrite(Session session)
	{
		String editorRolesToUse = (editorRoles == null) ? getDefaultEditorRoles() : editorRoles;
		if (editorRolesToUse != null)
		{
			String[] editorRolesToUseArr = editorRolesToUse.split(SEPARATOR);
			for (String editorRole : editorRolesToUseArr)
			{
				String roleName = editorRole.trim();
				if (!"".equals(roleName))
				{
					Role role = "administrator".equals(roleName) ? Role.ADMINISTRATOR
						: Role.forSpecificRole(roleName);
					if (session.isUserInRole(role))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Define the default roles to use if none are configured on the bean.
	 * By default, it is Tech Admin but this can be overwritten by subclasses.
	 */
	protected String getDefaultEditorRoles()
	{
		return CommonConstants.ROLE_TECH_ADMIN;
	}

	/**
	 * Adds the editor when it's read-only.
	 * Adds the default editor unless overridden.
	 */
	protected void doAddForDisplay(UIResponseContext context)
	{
		UIWidget widget = context.newBestMatching(Path.SELF);
		widget.setEditorDisabled(true);
		context.addWidget(widget);
	}

	/**
	 * Adds the editor when it's not read-only.
	 * Adds the default editor unless overridden.
	 */
	protected void doAddForEdit(UIResponseContext context)
	{
		context.addWidget(Path.SELF);
	}
}
