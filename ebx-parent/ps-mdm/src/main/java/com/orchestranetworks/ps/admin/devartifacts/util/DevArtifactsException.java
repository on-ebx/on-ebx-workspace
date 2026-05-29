package com.orchestranetworks.ps.admin.devartifacts.util;

/**
 * An exception used by Dev Artifacts that can store the artifact associated with the error
 */
public class DevArtifactsException extends Exception
{
	private static final long serialVersionUID = 1L;

	private String artifact;

	public DevArtifactsException(String artifact)
	{
		this(artifact, null, null);
	}

	public DevArtifactsException(String artifact, String message)
	{
		this(artifact, message, null);
	}

	public DevArtifactsException(String artifact, Throwable cause)
	{
		this(artifact, null, cause);
	}

	public DevArtifactsException(String artifact, String message, Throwable cause)
	{
		super(createMessage(artifact, message, cause), cause);
		this.artifact = artifact;
	}

	private static String createMessage(String artifact, String message, Throwable cause)
	{
		if (message == null)
		{
			return (cause == null) ? null : cause.getMessage();
		}
		return message;
	}

	public String getArtifact()
	{
		return artifact;
	}
}
