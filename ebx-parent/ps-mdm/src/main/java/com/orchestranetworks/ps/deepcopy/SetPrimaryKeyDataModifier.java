/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;

public class SetPrimaryKeyDataModifier implements DeepCopyDataModifier
{
	private PrimaryKeyDataSpec pkeySpec;

	/**
	 * Instantiate the Data Modifier with the Primary Key specification.
	 * The PrimaryKeyDataSpec object must have its internal pkeyValue fields updated with the desired
	 * target record primary key prior to invocation of the modifyDuplicateRecordContext() method.
	 * @param aKeySpec The PrimaryKeyDataSpec containing the metadata for the primary key field update.
	 */
	public SetPrimaryKeyDataModifier(PrimaryKeyDataSpec aKeySpec)
	{
		pkeySpec = aKeySpec;
	}

	@Override
	public void modifyDuplicateRecordContext(
		ValueContextForUpdate context,
		Adaptation origRecord,
		DeepCopyConfig config,
		Session session)
	{
		if (pkeySpec.getTableObj().getTablePath().equals(
			origRecord.getContainerTable().getTablePath()))
		{
			pkeySpec.updateTargetPrimaryKey(context);
		}
	}

}
