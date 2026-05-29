package com.orchestranetworks.ps.messaging;

import com.onwbp.adaptation.*;

public class TransformationContext
{
	private AdaptationHome dataSpace;
	private Adaptation dataSet;
	private AdaptationTable table;

	public AdaptationHome getDataSpace()
	{
		return dataSpace;
	}
	public void setDataSpace(AdaptationHome dataSpace)
	{
		this.dataSpace = dataSpace;
	}
	public Adaptation getDataSet()
	{
		return dataSet;
	}
	public void setDataSet(Adaptation dataSet)
	{
		this.dataSet = dataSet;
	}
	public AdaptationTable getTable()
	{
		return table;
	}
	public void setTable(AdaptationTable table)
	{
		this.table = table;
	}

}
