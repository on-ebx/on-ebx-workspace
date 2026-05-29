package com.orchestranetworks.ps.uilabelrenderer;

import com.orchestranetworks.ui.*;

/**
 * Renders a record in a hierarchy with a custom icon instead of the default icon.
 * <code>iconType</code> indicates how it is specified:
 * <ul>
 * 
 * <li><code>none</code>: No icon is specified, meaning the default icon is removed and none is shown.</li>
 * 
 * <li><code>class</code>: The css class of built-in EBX icon is specified in the <code>icon</code> parameter,
 * i.e. those used as the "standard" ones in perspective menu items. You can tell the class of the icon by
 * setting it on a perspective, exporting the perspective, and looking at the XML. For example, the flag icon
 * is <code>fa-flag</code>.</li>
 * 
 * <li><code>file</code>: A file that contains the icon to use is specified in the <code>icon</code> parameter.
 * You would specify this similar to how you would specify an "image URL" for a perspective menu item.</li>
 * 
 * </ul>
 * If <code>iconType</code> is <code>null</code>, then the default icon will be used. In that case, this class
 * wouldn't be providing much benefit, but it might still be useful if you are extending it and in some condition,
 * don't wish a custom icon.
 * 
 * The default label for the record will be used. If something different is desired,
 * {@link #displayText(UILabelRendererForHierarchyContext)} can be overridden.
 */
public class CustomIconUILabelRendererForHierarchy implements UILabelRendererForHierarchy
{
	public static final String ICON_TYPE_NONE = "none";
	public static final String ICON_TYPE_CLASS = "class";
	public static final String ICON_TYPE_FILE = "file";

	private String iconType;
	private String icon;

	@Override
	public void displayLabel(UILabelRendererForHierarchyContext context)
	{
		displayIcon(context);
		displayText(context);
	}

	/**
	 * Display the icon (or no icon if none is specified)
	 * 
	 * @param context the context
	 */
	protected void displayIcon(UILabelRendererForHierarchyContext context)
	{
		if (iconType != null)
		{
			context.setDefaultIconDisplayed(false);

			if (!context.isHTMLForbidden())
			{
				if (ICON_TYPE_CLASS.equals(iconType))
				{
					context.add("<span class='ebx_Icon fa ").addSafeInnerHTML(icon).add(
						"'></span>&nbsp;");
				}
				else if (ICON_TYPE_FILE.equals(iconType))
				{
					context.add("<img src='").addSafeInnerHTML(icon).add("'></img>&nbsp;");
				}
			}
		}
	}

	/**
	 * Display the text
	 * 
	 * @param context the context
	 */
	protected void displayText(UILabelRendererForHierarchyContext context)
	{
		context.add(context.getLabelFromDefaultPattern());
	}

	public String getIconType()
	{
		return iconType;
	}

	public void setIconType(String iconType)
	{
		this.iconType = iconType;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}
}
