package com.orchestranetworks.ps.accessrule;

import com.onwbp.adaptation.Adaptation;
import com.orchestranetworks.ps.constants.CommonConstants;
import com.orchestranetworks.ps.util.AdaptationUtil;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.service.AccessPermission;
import com.orchestranetworks.service.AccessRule;
import com.orchestranetworks.service.Role;
import com.orchestranetworks.service.Session;
/**
 * Restricts read/write access to records created within the current workflow, i.e. to those that are new
 * in the current child dataset.
 * Similar to ReadWriteForNewRecordOnlyAccessRule, except it retains read/write status even after the user
 * hits the Save button.  It tests whether the given record existed in the original snapshot of the child 
 * dataspace.  If the record is new in the child dataspace it continues in read/write mode until merged 
 * into the parent dataspace.
 * 
 * @author dhentchel
 *
 */
public class ReadWriteForRecordCreatedInChildDataspace implements AccessRule 
{
	/** The role giving permission despite the rule */
	private Role permissiveRole;

	protected AccessPermission notNewRecordPermission;

	public ReadWriteForRecordCreatedInChildDataspace()
	{
		this(AccessPermission.getReadOnly(), CommonConstants.TECH_ADMIN);
	}

	public ReadWriteForRecordCreatedInChildDataspace(final AccessPermission notNewRecordPermission)
	{
		this(notNewRecordPermission, CommonConstants.TECH_ADMIN);
	}

	public ReadWriteForRecordCreatedInChildDataspace(final Role permissiveRole)
	{
		this(AccessPermission.getReadOnly(), permissiveRole);
	}

	public ReadWriteForRecordCreatedInChildDataspace(
		final AccessPermission notNewRecordPermission,
		final Role permissiveRole)
	{
		this.notNewRecordPermission = notNewRecordPermission;
		this.permissiveRole = permissiveRole;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.orchestranetworks.service.AccessRule#getPermission(com.onwbp.adaptation
	 * .Adaptation, com.orchestranetworks.service.Session,
	 * com.orchestranetworks.schema.SchemaNode)
	 */
	@Override
	public AccessPermission getPermission(
		final Adaptation pAdaptation,
		final Session pSession,
		final SchemaNode pNode)
	{
		// If it's a new record or viewing on a table (is schema instance for both cases),
		// or if it's history, or if you're in a permissive role, then return RW
		if (pAdaptation.isSchemaInstance() || pAdaptation.isHistory() || isUserAlwaysReadWrite(pSession))
		{
			return AccessPermission.getReadWrite();
		}

		if (AdaptationUtil.getRecordFromInitialSnapshot(pAdaptation) == null)
		{
			return AccessPermission.getReadWrite();
		}
		return this.notNewRecordPermission;
	}

	/**
	 * Checks if a permissive role was specified, and if so if the user is in that role,
	 * but can be overridden to specify any additional logic based on the session that
	 * should determine if a user is always read/write (e.g. tracking info).
	 *
	 * @param session the user session
	 * @return whether the user's always read/write
	 */
	protected boolean isUserAlwaysReadWrite(Session session)
	{
		return this.permissiveRole != null && session.isUserInRole(this.permissiveRole);
	}

	public Role getPermissiveRole()
	{
		return this.permissiveRole;
	}

	public void setPermissiveRole(final Role permissiveRole)
	{
		this.permissiveRole = permissiveRole;
	}

	
}
