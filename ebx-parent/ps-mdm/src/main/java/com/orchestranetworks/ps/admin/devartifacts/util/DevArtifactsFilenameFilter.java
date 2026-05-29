package com.orchestranetworks.ps.admin.devartifacts.util;

import java.io.*;

/**
 * A filename filter that is constructed with a prefix and a suffix and accepts
 * files whose names start with the prefix and end with the suffix.
 */
public class DevArtifactsFilenameFilter implements FilenameFilter
{
	private String prefix;
	private String suffix;

	public DevArtifactsFilenameFilter(String prefix, String suffix)
	{
		this.prefix = prefix;
		this.suffix = suffix;
	}

	@Override
	public boolean accept(File dir, String name)
	{
		return (prefix == null || name.startsWith(prefix))
			&& (suffix == null || name.endsWith(suffix));
	}

	public String getPrefix()
	{
		return this.prefix;
	}

	public void setPrefix(String prefix)
	{
		this.prefix = prefix;
	}

	public String getSuffix()
	{
		return this.suffix;
	}

	public void setSuffix(String suffix)
	{
		this.suffix = suffix;
	}
}
