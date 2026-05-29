/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.multiselect;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.ui.form.widget.*;

/**
 */
public class MultiSelectWidgetFactory implements UIWidgetFactory<MultiSelectCustomWidget>
{
	private int joinTableParentPKPosition = 0;
	private Path joinTableFKPath;
	private String selectLabel = "Select";
	@SuppressWarnings("deprecation")
	private String selectionServiceName = MultiSelectRecordSelectionService.DEFAULT_SERVICE_NAME;
	private boolean allowOutsideWorkflow = false;
	private boolean allowWithoutCreate = false;

	public boolean isAllowWithoutCreate()
	{
		return allowWithoutCreate;
	}

	public void setAllowWithoutCreate(boolean allowWithoutCreate)
	{
		this.allowWithoutCreate = allowWithoutCreate;
	}

	public boolean isAllowOutsideWorkflow()
	{
		return allowOutsideWorkflow;
	}

	public void setAllowOutsideWorkflow(boolean allowOutsideWorkflow)
	{
		this.allowOutsideWorkflow = allowOutsideWorkflow;
	}

	public int getJoinTableParentPKPosition()
	{
		return this.joinTableParentPKPosition;
	}

	public void setJoinTableParentPKPosition(int joinTableParentPKPosition)
	{
		this.joinTableParentPKPosition = joinTableParentPKPosition;
	}

	public Path getJoinTableFKPath()
	{
		return this.joinTableFKPath;
	}

	public void setJoinTableFKPath(Path joinTableFKPath)
	{
		this.joinTableFKPath = joinTableFKPath;
	}

	public String getSelectLabel()
	{
		return this.selectLabel;
	}

	public void setSelectLabel(String selectLabel)
	{
		this.selectLabel = selectLabel;
	}

	public String getSelectionServiceName()
	{
		return this.selectionServiceName;
	}

	public void setSelectionServiceName(String selectionServiceName)
	{
		this.selectionServiceName = selectionServiceName;
	}

	@Override
	public MultiSelectCustomWidget newInstance(WidgetFactoryContext arg0)
	{
		return new MultiSelectCustomWidget(this, arg0);
	}

	@Override
	public void setup(WidgetFactorySetupContext arg0)
	{
	}
}
