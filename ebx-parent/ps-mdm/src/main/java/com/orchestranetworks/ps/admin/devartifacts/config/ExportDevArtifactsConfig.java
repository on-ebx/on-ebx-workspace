package com.orchestranetworks.ps.admin.devartifacts.config;

/**
 * A config for exporting dev artifacts
 */
public class ExportDevArtifactsConfig extends DevArtifactsConfig
{
	private boolean enableDownloadToLocal;
	private boolean downloadToLocal;

	public boolean isEnableDownloadToLocal()
	{
		return enableDownloadToLocal;
	}

	public void setEnableDownloadToLocal(boolean enableDownloadToLocal)
	{
		this.enableDownloadToLocal = enableDownloadToLocal;
	}

	public boolean isDownloadToLocal()
	{
		return downloadToLocal;
	}

	public void setDownloadToLocal(boolean downloadToLocal)
	{
		this.downloadToLocal = downloadToLocal;
	}
}
