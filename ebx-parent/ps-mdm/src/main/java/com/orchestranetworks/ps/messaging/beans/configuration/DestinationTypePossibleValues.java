package com.orchestranetworks.ps.messaging.beans.configuration;

import com.orchestranetworks.ps.ui.annotations.*;

public class DestinationTypePossibleValues extends PossibleValues
{

	@Override
	public void initValues()
	{
		add(EnumerationValue.valueOf("topic"));
		add(EnumerationValue.valueOf("queue"));
	}

}
