/*
 * Copyright (c) Orchestra Networks 2000-2006. All rights reserved.
 */
package com.orchestranetworks.ps.service;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.instance.*;
@Deprecated
public class HomeCollector
{
	private static final Comparator<AdaptationHome> HomeLabelComparator = new Comparator<AdaptationHome>()
	{
		@Override
		public int compare(AdaptationHome o1, AdaptationHome o2)
		{
			String l1 = o1.getLabel(Locale.getDefault());
			String l2 = o2.getLabel(Locale.getDefault());
			return l1.compareTo(l2);
		}
	};

	public static List<AdaptationHome> getAllHomes(
		Repository aRepository,
		boolean includeBranch,
		boolean includeVersion)
	{
		List<AdaptationHome> result = new ArrayList<>();
		List<AdaptationHome> initSnapshots = aRepository.getReferenceBranch().getVersionChildren();
		for (AdaptationHome initSnapshot : initSnapshots)
		{
			collectHomes(initSnapshot, result, includeBranch, includeVersion);
		}
		Collections.sort(result, HomeLabelComparator);
		return result;
	}

	public static void collectHomes(
		AdaptationHome aHome,
		List<AdaptationHome> homes,
		boolean includeBranch,
		boolean includeVersion)
	{
		if (aHome == null)
			return;

		if (aHome.isOpenBranch())
		{
			if (includeBranch)
				homes.add(aHome);
			if (includeVersion && !aHome.isBranchReference())
				homes.add(aHome.getParent());
		}

		if (aHome.isOpenVersion() && !aHome.isInitialVersion())
		{
			if (includeVersion)
				homes.add(aHome);
		}

		if (includeBranch)
			for (AdaptationHome child : aHome.getBranchChildren())
			{
				collectHomes(child, homes, includeBranch, includeVersion);
			}

		if (includeVersion)
			for (AdaptationHome child : aHome.getVersionChildren())
			{
				collectHomes(child, homes, includeBranch, includeVersion);
			}
	}
}
