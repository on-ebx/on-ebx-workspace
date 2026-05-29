/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.procedure;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;

/**
 */
public abstract class CopyComputedValuesProcedure implements Procedure
{
	private static final String DEFAULT_CHILD_DATA_SPACE_LABEL_PREFIX = "Copy Computed Values at ";
	private static final String DEFAULT_PERMISSIONS_TEMPLATE_DATA_SPACE_NAME = "CopyComputedValuesPermissions";

	private String dataSetName;
	// These aren't used by the procedure, but can be used by the service / script task executing
	// the procedure
	private String childDataSpaceLabelPrefix;
	private String permissionsTemplateDataSpaceName;

	// Table paths for which trigger should be enabled
	protected List<Path> triggerEnabledTablePaths = new ArrayList<>();

	protected CopyComputedValuesProcedure()
	{
		this(null);
	}

	protected CopyComputedValuesProcedure(String dataSetName)
	{
		this(
			dataSetName,
			DEFAULT_CHILD_DATA_SPACE_LABEL_PREFIX,
			DEFAULT_PERMISSIONS_TEMPLATE_DATA_SPACE_NAME);
	}

	protected CopyComputedValuesProcedure(
		String dataSetName,
		String childDataSpaceLabelPrefix,
		String permissionsTemplateDataSpaceName)
	{
		this.dataSetName = dataSetName;
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
	}

	/**
	 * Get the map of fields to copy
	 * 
	 * @return a map of fields to copy, where key = path of the table, value = (a map of key = field to copy from, value = field to copy to)
	 */
	protected abstract Map<Path, Map<Path, Path>> getFieldMapping();

	@Override
	public void execute(ProcedureContext pContext) throws Exception
	{
		Adaptation dataSet = pContext.getAdaptationHome()
			.findAdaptationOrNull(AdaptationName.forName(dataSetName));
		Map<Path, Map<Path, Path>> tableFieldMap = getFieldMapping();

		for (Map.Entry<Path, Map<Path, Path>> entry : tableFieldMap.entrySet())
		{
			Path tablePath = entry.getKey();
			Map<Path, Path> fieldMap = entry.getValue();
			boolean disableTrigger = true;
			if (triggerEnabledTablePaths.contains(tablePath))
			{
				disableTrigger = false;
			}

			AdaptationTable table = dataSet.getTable(tablePath);
			RequestResult reqRes = table.createRequestResult(null);
			try
			{
				Adaptation record;
				ModifyValuesProcedure mvp = new ModifyValuesProcedure();
				mvp.setAllPrivileges(true);
				mvp.setTriggerActivation(!disableTrigger);
				while ((record = reqRes.nextAdaptation()) != null)
				{
					Map<Path, Object> pathValueMap = createPathValueMap(record, fieldMap);
					if (!pathValueMap.isEmpty())
					{
						// Disable trigger activation because not needed here
						mvp.setAdaptation(record);
						mvp.setPathValueMap(pathValueMap);
						mvp.execute(pContext);
					}
				}
			}
			finally
			{
				reqRes.close();
			}
		}
	}

	private Map<Path, Object> createPathValueMap(Adaptation record, Map<Path, Path> fieldMap)
	{
		Map<Path, Object> pathValueMap = new HashMap<>();
		for (Map.Entry<Path, Path> entry : fieldMap.entrySet())
		{
			Path fromPath = entry.getKey();
			Path toPath = entry.getValue();
			Object fromValue = record.get(fromPath);
			Object toValue = record.get(toPath);
			if (!Objects.equals(fromValue, toValue))
			{
				pathValueMap.put(toPath, fromValue);
			}
		}
		return pathValueMap;
	}

	public String getDataSetName()
	{
		return this.dataSetName;
	}

	public void setDataSetName(String dataSetName)
	{
		this.dataSetName = dataSetName;
	}

	public String getChildDataSpaceLabelPrefix()
	{
		return this.childDataSpaceLabelPrefix;
	}

	public void setChildDataSpaceLabelPrefix(String childDataSpaceLabelPrefix)
	{
		this.childDataSpaceLabelPrefix = childDataSpaceLabelPrefix;
	}

	public String getPermissionsTemplateDataSpaceName()
	{
		return this.permissionsTemplateDataSpaceName;
	}

	public void setPermissionsTemplateDataSpaceName(String permissionsTemplateDataSpaceName)
	{
		this.permissionsTemplateDataSpaceName = permissionsTemplateDataSpaceName;
	}
}
