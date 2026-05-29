package com.orchestranetworks.ps.widget;

import com.orchestranetworks.ps.constants.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.service.directory.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 * A base widget factory that other widget factories can extend from in order to inherit some commonly used functionality.
 * Currently this includes the ability to make the editor read only, and allow for certain roles to ignore the read only
 * setting. It also allows you to specify that it never validates the input.
 * 
 * By default, this is editable and does not offer any different behavior unless the various parameters are set.
 */
public class BaseUICustomWidgetFactory<T extends UICustomWidget> implements UIWidgetFactory<T>
{
	private static final String EDITOR_ROLES_SEPARATOR = ",";

	private boolean readOnly;
	private boolean neverEditable;
	private String editorRoles = getDefaultEditorRoles();
	private boolean neverValidate;

	protected boolean listWidget;

	@SuppressWarnings("unchecked")
	@Override
	public T newInstance(WidgetFactoryContext context)
	{
		// Rather than have two separate factories, this is one factory that will return
		// either a list widget or simple widget based on this flag.
		if (listWidget)
		{
			return (T) new BaseUIListCustomWidget(context, this);
		}
		return (T) new BaseUISimpleCustomWidget(context, this);
	}

	@Override
	public void setup(WidgetFactorySetupContext context)
	{
		if (!readOnly && neverEditable)
		{
			context.addError("neverEditable can only be true when the editor is read only.");
		}
		listWidget = (context.getSchemaNode().getMaxOccurs() > 1);
	}

	/**
	 * Get whether the user belongs to one of the editor roles specified
	 * 
	 * @param session the session
	 * @return whether the user belongs to any of the roles
	 */
	protected boolean isUserInEditorRoles(Session session)
	{
		if (editorRoles != null)
		{
			DirectoryHandler dirHandler = session.getDirectory();
			UserReference user = session.getUserReference();
			String[] editorRolesToUseArr = editorRoles.split(EDITOR_ROLES_SEPARATOR);
			for (String editorRole : editorRolesToUseArr)
			{
				String roleName = editorRole.trim();
				if (!"".equals(roleName))
				{
					Role role = "administrator".equals(roleName) ? Role.ADMINISTRATOR
						: Role.forSpecificRole(roleName);
					if (dirHandler.isUserInRole(user, role))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Define the default roles to use if none are configured on the bean. By
	 * default, it is Tech Admin but this can be overwritten by subclasses.
	 */
	protected String getDefaultEditorRoles()
	{
		return CommonConstants.ROLE_TECH_ADMIN;
	}

	/**
	 * Whether to really validate the widget. This is called by both the list and simple widgets.
	 * 
	 * @return whether to validate
	 */
	protected boolean shouldValidateWidget()
	{
		// Only validate if it's not set to never validate and it's not never editable
		return !isNeverValidate() && !isNeverEditable();
	}

	/**
	 * @see {@link #setReadOnly(boolean)}
	 */
	public boolean isReadOnly()
	{
		return readOnly;
	}

	/**
	 * Set whether the widget is in read only mode
	 * 
	 * @param readOnly whether the widget is in read only mode
	 */
	public void setReadOnly(boolean readOnly)
	{
		this.readOnly = readOnly;
	}

	/**
	 * @see {@link #setNeverEditable(boolean)}
	 */
	public boolean isNeverEditable()
	{
		return neverEditable;
	}

	/**
	 * Set whether this is never editable. If never editable, no one can edit it
	 * even if their roles are specified as editable.
	 * Only applicable when <code>readOnly</code> is <code>true</code>.
	 * 
	 * @param neverEditable whether the widget is never editable
	 */
	public void setNeverEditable(boolean neverEditable)
	{
		this.neverEditable = neverEditable;
	}

	/**
	 * @see {@link #setEditorRoles(String)}
	 */
	public String getEditorRoles()
	{
		return this.editorRoles;
	}

	/**
	 * Set a comma-separated list of roles that can edit, if the <code>readOnly</code> is <code>true</code>
	 * and <code>neverEditable</code> is <code>false</code>.
	 * 
	 * @param editorRoles the roles
	 */
	public void setEditorRoles(String editorRoles)
	{
		this.editorRoles = editorRoles;
	}

	/**
	 * @see {@link #setNeverValidate(boolean)}
	 */
	public boolean isNeverValidate()
	{
		return this.neverValidate;
	}

	/**
	 * Set whether this never validates. By default, the editor doesn't validate
	 * when it's read only and otherwise it validates. This will allow you to
	 * specify that it should never validate regardless of if it's editable.
	 * 
	 * @param neverValidate whether this never validates
	 */
	public void setNeverValidate(boolean neverValidate)
	{
		this.neverValidate = neverValidate;
	}
}
