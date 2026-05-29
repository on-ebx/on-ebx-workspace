/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.deepcopy;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.service.*;

public class CompositeDataModifier implements DeepCopyDataModifier
{
	private List<DeepCopyDataModifier> modifiers = new ArrayList<>();

	@Override
	public void modifyDuplicateRecordContext(
		ValueContextForUpdate context,
		Adaptation origRecord,
		DeepCopyConfig config,
		Session session)
	{
		for (DeepCopyDataModifier modifier : modifiers)
		{
			modifier.modifyDuplicateRecordContext(context, origRecord, config, session);
		}
	}

	public CompositeDataModifier addModifier(DeepCopyDataModifier modifier)
	{
		modifiers.add(modifier);
		return this;
	}

}
