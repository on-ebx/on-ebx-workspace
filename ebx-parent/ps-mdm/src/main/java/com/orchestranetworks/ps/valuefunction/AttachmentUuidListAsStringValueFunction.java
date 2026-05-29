package com.orchestranetworks.ps.valuefunction;

import com.orchestranetworks.addon.dama.models.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.functional.*;

// Use this Value Function to return a List of UUIDs for a repeating attribute containing a list of DAMA Add-on Attachments
public class AttachmentUuidListAsStringValueFunction extends ListAsStringValueFunction
{

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.valuefunction.ListAsStringValueFunction#getToStringFunction()
	 */
	@Override
	protected UnaryFunction<Object, String> getToStringFunction()
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see com.orchestranetworks.ps.valuefunction.ListAsStringValueFunction#convertToString(java.lang.Object, com.orchestranetworks.instance.ValueContext)
	 */
	@Override
	protected String convertToString(Object object, ValueContext valueContext)
	{
		if (object instanceof MediaType)
		{
			return ((MediaType) object).getAttachment();
		}
		return null;
	}

}
