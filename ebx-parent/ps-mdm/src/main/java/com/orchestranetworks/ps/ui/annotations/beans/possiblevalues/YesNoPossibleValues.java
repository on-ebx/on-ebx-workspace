package com.orchestranetworks.ps.ui.annotations.beans.possiblevalues;

import com.orchestranetworks.ps.ui.annotations.*;

public class YesNoPossibleValues extends PossibleValues
{

	@Override
	public void initValues()
	{
		add(EnumerationValue.valueOf("Yes", "true"));
		add(EnumerationValue.valueOf("No", "false"));

	}

}