/*
 * Copyright Orchestra Networks 2000-2012. All rights reserved.
 */
package com.orchestranetworks.ps.trigger;

import java.util.*;

import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;
import com.orchestranetworks.schema.trigger.*;
import com.orchestranetworks.service.*;

/**
 * This trigger sets the audit fields for the standard Base Audit Persisted group
 * (create date/time, created by, last update date/time, last updated by).
 * 
 * It is coded assuming the standard field names but they can also be set if it is different.
 * If using the default behavior, this should be set on the create date/time field, but it will
 * navigate to the other fields (that should be at the same level in the schema) to set those as well.
 * This way, there's one trigger invocation handling all the audit fields, and it can be set within the
 * reusable group rather than on every table that includes the group.
 * 
 * However, you can also set the 4 field paths if not using the standard Base Audit Persisted group.
 * In this case, it's assumed you are setting it at the table level and it will navigate to the
 * fields using the paths specified as parameters.
 */
public class BaseAuditPersistedFieldsTableTrigger extends BaseTableTriggerEnforcingPermissions
{
	private static final Path CREATED_BY_PATH = Path.parse("../createdBy");
	private static final Path LAST_UPDATE_DATE_TIME_PATH = Path.parse("../lastUpdateDateTime");
	private static final Path LAST_UPDATED_BY_PATH = Path.parse("../lastUpdatedBy");

	private Path createDateTimePath;
	private Path createdByPath;
	private Path lastUpdateDateTimePath;
	private Path lastUpdatedByPath;

	private SchemaNode createDateTimeNode;
	private SchemaNode createdByNode;
	private SchemaNode lastUpdateDateTimeNode;
	private SchemaNode lastUpdatedByNode;

	private Integer createdByMaxLength;
	private Integer lastUpdatedByMaxLength;

	@Override
	public void handleBeforeCreate(BeforeCreateOccurrenceContext context) throws OperationException
	{
		super.handleBeforeCreate(context);

		ValueContextForUpdate vc = context.getOccurrenceContextForUpdate();

		Date now = getCurrentDateTime();
		if (createDateTimeNode != null)
		{
			vc.setValueEnablingPrivilegeForNode(now, createDateTimeNode.getPathInAdaptation());
		}
		if (lastUpdateDateTimeNode != null)
		{
			vc.setValueEnablingPrivilegeForNode(now, lastUpdateDateTimeNode.getPathInAdaptation());
		}

		String user = context.getSession().getUserReference().getUserId();
		if (createdByNode != null)
		{
			vc.setValueEnablingPrivilegeForNode(
				getTrimmedUser(createdByMaxLength, user),
				createdByNode.getPathInAdaptation());
		}
		if (lastUpdatedByNode != null)
		{
			vc.setValueEnablingPrivilegeForNode(
				getTrimmedUser(lastUpdatedByMaxLength, user),
				lastUpdatedByNode.getPathInAdaptation());
		}
	}

	@Override
	public void handleBeforeModify(BeforeModifyOccurrenceContext context) throws OperationException
	{
		super.handleBeforeModify(context);

		ValueContextForUpdate vc = context.getOccurrenceContextForUpdate();

		if (lastUpdateDateTimeNode != null)
		{
			Date now = getCurrentDateTime();
			vc.setValueEnablingPrivilegeForNode(now, lastUpdateDateTimeNode.getPathInAdaptation());
		}

		String user = context.getSession().getUserReference().getUserId();
		if (lastUpdatedByNode != null)
		{
			vc.setValueEnablingPrivilegeForNode(
				getTrimmedUser(lastUpdatedByMaxLength, user),
				lastUpdatedByNode.getPathInAdaptation());
		}
	}

	@Override
	public void setup(TriggerSetupContext context)
	{
		super.setup(context);

		if (createDateTimePath == null && createdByPath == null && lastUpdateDateTimePath == null
			&& lastUpdatedByPath == null)
		{
			createDateTimeNode = context.getSchemaNode();
			createdByNode = createDateTimeNode.getNode(CREATED_BY_PATH);
			lastUpdateDateTimeNode = createDateTimeNode.getNode(LAST_UPDATE_DATE_TIME_PATH);
			lastUpdatedByNode = createDateTimeNode.getNode(LAST_UPDATED_BY_PATH);
		}
		else
		{
			createDateTimeNode = PathUtils
				.setupFieldNode(context, createDateTimePath, "createDateTimePath", false, false);
			createdByNode = PathUtils
				.setupFieldNode(context, createdByPath, "createdByPath", false, false);
			lastUpdateDateTimeNode = PathUtils.setupFieldNode(
				context,
				lastUpdateDateTimePath,
				"lastUpdateDateTimePath",
				false,
				false);
			lastUpdatedByNode = PathUtils
				.setupFieldNode(context, lastUpdatedByPath, "lastUpdatedByPath", false, false);
		}

		SchemaFacetMaxLength schemaFacetMaxLen = createdByNode.getFacetMaxLength();
		if (schemaFacetMaxLen != null)
		{
			createdByMaxLength = schemaFacetMaxLen.getValue();
		}
		schemaFacetMaxLen = lastUpdatedByNode.getFacetMaxLength();
		if (schemaFacetMaxLen != null)
		{
			lastUpdatedByMaxLength = schemaFacetMaxLen.getValue();
		}
	}

	private static String getTrimmedUser(Integer maxLength, String user)
	{
		String trimmedUser;
		if (maxLength == null)
		{
			trimmedUser = user;
		}
		else
		{
			int maxLen = maxLength.intValue();
			if (user.length() > maxLen)
			{
				trimmedUser = user.substring(0, maxLen);
			}
			else
			{
				trimmedUser = user;
			}
		}
		return trimmedUser;
	}

	private static Date getCurrentDateTime()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public Path getCreateDateTimePath()
	{
		return createDateTimePath;
	}

	public void setCreateDateTimePath(Path createDateTimePath)
	{
		this.createDateTimePath = createDateTimePath;
	}

	public Path getCreatedByPath()
	{
		return createdByPath;
	}

	public void setCreatedByPath(Path createdByPath)
	{
		this.createdByPath = createdByPath;
	}

	public Path getLastUpdateDateTimePath()
	{
		return lastUpdateDateTimePath;
	}

	public void setLastUpdateDateTimePath(Path lastUpdateDateTimePath)
	{
		this.lastUpdateDateTimePath = lastUpdateDateTimePath;
	}

	public Path getLastUpdatedByPath()
	{
		return lastUpdatedByPath;
	}

	public void setLastUpdatedByPath(Path lastUpdatedByPath)
	{
		this.lastUpdatedByPath = lastUpdatedByPath;
	}
}
