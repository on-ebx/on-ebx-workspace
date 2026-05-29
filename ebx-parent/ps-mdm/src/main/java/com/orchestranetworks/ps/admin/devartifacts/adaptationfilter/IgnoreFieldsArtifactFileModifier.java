package com.orchestranetworks.ps.admin.devartifacts.adaptationfilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.modifier.*;
import com.orchestranetworks.schema.*;

/**
 * Allows one to specify certain fields to ignore the value of when processing with Dev Artifacts.
 * On export, it will clear out the value of those fields and on import, it will maintain whatever the
 * current value is in EBX for those fields. If it's a new record, it will set the value to whatever is
 * specified as the default value (which can be <code>null</code>).
 * 
 * You must specify a field to be the unique field for the record. It can be the primary key field, but doesn't
 * have to, as long as it uniquely identifies a record in the table. It only supports one field being the
 * unique field, rather than a combination of fields.
 * 
 * A current restriction is that there can't be another table or group (complex type) within the same file that
 * is named the same. This is because it looks for the tag with the table's name to know when it encounters a new
 * record, so if there's another tag with that same name, it won't work.
 */
public class IgnoreFieldsArtifactFileModifier extends ArtifactFileModifier
{
	private String uniqueFieldName;
	private Path[] ignoreFieldPaths;
	private String[] ignoreFieldNames;
	private SchemaNode[] ignoreFieldNodes;
	private Object[] defaultValues;
	private AdaptationTable table;
	private String tableFieldName;
	private boolean insideRecord;
	private Adaptation currentRecord;

	/**
	 * Construct the artifact file modifier
	 * 
	 * @param uniqueFieldPath The path of the field that's unique
	 * @param ignoreFieldPaths An array of fields to ignore (if only one just create an array of one)
	 * @param defaultValues The values to default when it's a new record (must match one to one with the <code>ignoreFieldPaths</code>)
	 * @param table the table
	 */
	public IgnoreFieldsArtifactFileModifier(
		Path uniqueFieldPath,
		Path[] ignoreFieldPaths,
		Object[] defaultValues,
		AdaptationTable table)
	{
		this.table = table;
		// Save the names of the various fields because that's what the tag will be in the XML
		tableFieldName = table.getTablePath().getLastStep().format();
		SchemaNode rootNode = table.getTableOccurrenceRootNode();

		this.uniqueFieldName = uniqueFieldPath.getLastStep().format();

		this.ignoreFieldPaths = ignoreFieldPaths;
		ignoreFieldNames = new String[ignoreFieldPaths.length];
		ignoreFieldNodes = new SchemaNode[ignoreFieldPaths.length];
		for (int i = 0; i < ignoreFieldPaths.length; i++)
		{
			ignoreFieldNames[i] = ignoreFieldPaths[i].getLastStep().format();
			ignoreFieldNodes[i] = rootNode.getNode(uniqueFieldPath);
		}

		this.defaultValues = defaultValues;
	}

	@Override
	public List<String> modifyExport(String line)
	{
		// For each specified field, clear out the value but keep the tag so we can replace the value on import
		List<String> returnVal = null;
		for (int i = 0; returnVal == null && i < ignoreFieldNames.length; i++)
		{
			String ignoreFieldName = ignoreFieldNames[i];
			if (containsStartTag(line, ignoreFieldName))
			{
				returnVal = Arrays.asList(replaceValue(line, ignoreFieldName, ""));
			}
		}
		return returnVal;
	}

	@Override
	public List<String> modifyImport(String line)
	{
		// If we've started a record
		if (insideRecord)
		{
			// If we found the end tag for the record, then clear these fields so we know
			// next time this gets called, we're no longer inside that group
			if (containsEndTag(line, tableFieldName))
			{
				insideRecord = false;
				currentRecord = null;
			}
			else
			{
				String uniqueValue = getValue(line, uniqueFieldName);
				if (uniqueValue == null)
				{
					List<String> returnVal = null;
					for (int i = 0; returnVal == null && i < ignoreFieldNames.length; i++)
					{
						String ignoreFieldName = ignoreFieldNames[i];
						// If we found a field to ignore inside this record
						if (containsStartTag(line, ignoreFieldName))
						{
							Object value;
							// If there is no existing record (i.e. this is a new record in the imported file)
							// then default the value
							if (currentRecord == null)
							{
								value = defaultValues[i];
							}
							// Otherwise, get the current value from the existing record and fill that in so it is maintained
							else
							{
								value = currentRecord.get(ignoreFieldPaths[i]);
								// If current value is null then don't include this line at all
								if (value == null)
								{
									returnVal = new ArrayList<>();
								}
							}
							if (returnVal == null)
							{
								String valueStr = (value == null) ? ""
									: ignoreFieldNodes[i].formatToXsString(value);
								returnVal = Arrays
									.asList(replaceValue(line, ignoreFieldName, valueStr));
							}
						}
					}
					return returnVal;
				}
				// If this line contains the unique field, then set the current record
				// so that next time this gets called, we know which record to use
				currentRecord = table
					.lookupAdaptationByPrimaryKey(PrimaryKey.parseString(uniqueValue));
			}
		}
		// If we found a start tag for the table, then mark that we're inside of
		// that group for next time this gets called
		else if (containsStartTag(line, tableFieldName))
		{
			insideRecord = true;
		}
		// If nothing was replaced, then return null to indicate that no modification is to occur for this line
		return null;
	}
}
