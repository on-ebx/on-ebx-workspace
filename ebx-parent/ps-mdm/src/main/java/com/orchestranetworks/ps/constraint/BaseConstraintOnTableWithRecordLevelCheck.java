/*
 * Copyright Orchestra Networks 2000-2016. All rights reserved.
 */
package com.orchestranetworks.ps.constraint;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.instance.*;
import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;

/**
 * This abstract class simplifies implementation of a <code>ConstraintOnTableWithRecordLevelCheck</code> by
 * requiring subclasses only to implement a <code>checkValueContext</code> method that returns a String. 
 * When this result is not null, this is recorded as a validation error, and otherwise, the record it valid
 * with respect to this constraint.
 * The <code>checkTable</code> method is implemented to iterate over all records;
 * the <code>checkRecord</code> method is implemented to check a single record using the checkValueContext
 * method.  Optionally, one can override <code>getNodeForRecord</code> to associate violations of this
 * constraint with a particular field of the record (to determine which value will appear in red in the UI).
 * Also, optionally, one can override <code>getSeverity</code> to determine error/warning etc.
 * Configure instances of this constraint with a semicolon-separated list of paths representing fields that
 * this constraint is sensitive to (dependencies).  If one of those field dependencies is an association,
 * this constraint will, in fact, depend on changes to the related table.  Note well that we assume that all
 * association field will use an association table in the same data model, as is the documented best practice.
 *
 * 		<severity>F=Fatal, E=Error (default), W=Warning, I=Information</severity>
 * 		<recordLevelSeverity>can optionally provide a different severity level for the Record Level check</recordLevelSeverity>
 * 		<allowFormSubmissionWithError>will allow a record to be saved even if this constraint reports an error</allowFormSubmissionWithError>
 * 
 */
public abstract class BaseConstraintOnTableWithRecordLevelCheck
	implements ConstraintOnTableWithRecordLevelCheck
{
	//"\u2022 " displays a bullet in the UI
	public static final String BULLET = "\u2022 ";

	private String dependencyPaths;
	private String severity;
	private Severity defaultSeverity = Severity.ERROR;
	private String recordLevelSeverity;
	private Severity defaultRecordLevelSeverity;
	private boolean allowFormSubmissionWithError = false;
	private Path recordValidationFieldPath;
	private SchemaNode recordValidationNode;

	public String getDependencyPaths()
	{
		return dependencyPaths;
	}

	public void setDependencyPaths(String dependencyPaths)
	{
		this.dependencyPaths = dependencyPaths;
	}

	@Override
	public void setup(ConstraintContextOnTable context)
	{
		if (dependencyPaths != null)
		{
			List<Path> paths = PathUtils.convertStringToPathList(dependencyPaths, null);
			for (Path path : paths)
			{
				PathUtils.setupFieldNode(context, path, null, true);
			}
		}
		if (severity != null)
		{
			this.defaultSeverity = Severity.parseFlag(severity);
		}
		if (recordLevelSeverity != null)
		{
			this.defaultRecordLevelSeverity = Severity.parseFlag(recordLevelSeverity);
		}
		if (recordValidationFieldPath != null)
		{
			this.recordValidationNode = PathUtils
				.setupFieldNode(context, recordValidationFieldPath, "recordLevelFieldPath", false);
		}

		if (allowFormSubmissionWithError)
		{
			context.setBlocksCommitToNever();
		}
	}

	@Override
	public void checkTable(ValueContextForValidationOnTable context)
	{
		AdaptationTable table = context.getTable();
		RequestResult reqRes = table.createRequestResult(null);
		try
		{
			for (Adaptation record; (record = reqRes.nextAdaptation()) != null;)
			{
				ValueContext recordContext = record.createValueContext();
				String msg = checkValueContext(recordContext);
				if (msg != null)
				{
					context.addMessage(
						record,
						AdaptationUtil.createUserMessage(
							record,
							msg,
							getSeverityFromTableLevel(recordContext)));
				}
			}
		}
		finally
		{
			reqRes.close();
		}
	}

	@Override
	public void checkRecord(ValueContextForValidationOnRecord context)
	{
		ValueContext recordContext = context.getRecord();

		String msg = checkValueContext(recordContext);
		if (msg != null)
		{
			UserMessage userMsg = AdaptationUtil
				.createUserMessage(msg, getSeverityFromRecordLevel(recordContext));
			SchemaNode node = getNodeForRecordValidation(recordContext);
			if (node == null)
			{
				context.addMessage(userMsg);
			}
			else
			{
				context.addMessage(node, userMsg);
			}
		}
	}

	/**
	 * Check a record and return an error if the constraint is violated.
	 * Only one message may be returned per record.
	 * 
	 * @param recordContext the <code>ValueContext</code> of the record
	 * @return the error message, or <code>null</code> if no constraint was violated.
	 */
	protected abstract String checkValueContext(ValueContext recordContext);

	/**
	 * Get the node to associate with the record-level validation error. If <code>null</code>, it's not associated with a node.
	 * By default, returns the node associated with <code>recordValidationFieldPath</code>, if specified but can be overridden
	 * if there is some more complex logic.
	 * 
	 * @param recordContext the record's context
	 * @return the node, or <code>null</code>
	 */
	protected SchemaNode getNodeForRecordValidation(ValueContext recordContext)
	{
		return recordValidationNode;
	}

	/**
	 * Get the severity associated with violations of this constraint.
	 * 
	 * @param recordContext the record's context
	 * @return the severity
	 */
	protected Severity getSeverity(ValueContext recordContext)
	{
		return defaultSeverity;
	}

	protected Severity getSeverityFromRecordLevel(ValueContext recordContext)
	{
		return (defaultRecordLevelSeverity != null ? defaultRecordLevelSeverity
			: getSeverity(recordContext));
	}

	protected Severity getSeverityFromTableLevel(ValueContext recordContext)
	{
		return getSeverity(recordContext);
	}

	public String getSeverity()
	{
		return severity;
	}

	public void setSeverity(String severity)
	{
		this.severity = severity;
	}

	public String getRecordLevelSeverity()
	{
		return recordLevelSeverity;
	}

	public void setRecordLevelSeverity(String recordLevelSeverity)
	{
		this.recordLevelSeverity = recordLevelSeverity;
	}

	public Path getRecordValidationFieldPath()
	{
		return recordValidationFieldPath;
	}

	public void setRecordValidationFieldPath(Path recordValidationFieldPath)
	{
		this.recordValidationFieldPath = recordValidationFieldPath;
	}

	public boolean isAllowFormSubmissionWithError()
	{
		return allowFormSubmissionWithError;
	}

	public void setAllowFormSubmissionWithError(boolean allowFormSubmissionWithError)
	{
		this.allowFormSubmissionWithError = allowFormSubmissionWithError;
	}
}
