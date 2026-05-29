package com.orchestranetworks.ps.uibeaneditor;

import java.util.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.*;

/**
 * 
 *         Fix a multi valued field to a given number of elements.
 *         
 * <pre>{@code
 *  	<osd:uiBean class="com.orchestranetworks.ps.ui.bean.FixedListEditor">
 *         	<numberOfElements>...</numberOfElements>
 *  	</osd:uiBean>
 *  }</pre>
 * @author MCH
 */
@Deprecated
public class FixedListEditor extends UIBeanEditor
{

	private int numberOfElements;

	@Override
	public void addForDisplay(final UIResponseContext pContext)
	{
		pContext.addUIBestMatchingComponent(Path.SELF, "");
	}

	@Override
	public void addForEdit(final UIResponseContext pContext)
	{
		pContext.addUIBestMatchingComponent(Path.SELF, "");
	}

	@Override
	public void addList(final UIResponseContext pContext)
	{
		for (int i = 0; i < this.numberOfElements; i++)
		{
			pContext.addUIBestMatchingComponent(
				Path.PARENT.add(pContext.getPathInAdaptation().addIndex(i)),
				"");
		}
	}

	@Override
	public void validateInputList(final UIRequestContext pContext)
	{
		List<Object> list = new ArrayList<>();
		for (int i = 0; i < 2; i++)
		{
			Object value = pContext
				.getValue(Path.PARENT.add(pContext.getPathInAdaptation().addIndex(i)));
			list.add(value);
		}
		Collections.reverse(list);
		pContext.getValueContext().setNewValue(list);
	}
}
