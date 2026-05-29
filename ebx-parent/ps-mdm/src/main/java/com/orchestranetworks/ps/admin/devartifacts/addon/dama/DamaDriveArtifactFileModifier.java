package com.orchestranetworks.ps.admin.devartifacts.addon.dama;

import java.math.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.admin.devartifacts.constants.*;
import com.orchestranetworks.ps.admin.devartifacts.modifier.*;
import com.orchestranetworks.ps.util.addon.*;

/**
 * Modifies a Drive record of the DAMA add-on. The drive path can differ between environments, so on export, this will
 * strip out the common prefix and on import, re-insert it. The prefix is stored in the configuration.
 * 
 * This also does special handling of the max usable space field. That can differ between environments, so if the
 * record already exists on import, it will keep the value.
 */
public class DamaDriveArtifactFileModifier extends ArtifactFileModifier
{
	private static final String UUID_FIELD_NAME = AddonDamaAdminUtil.ADDON_DAMA_DRIVE_UUID_PATH
		.getLastStep()
		.format();
	private static final String PHYSICAL_ROOT_PATH_FIELD_NAME = AddonDamaAdminUtil.ADDON_DAMA_DRIVE_PHYSICAL_ROOT_PATH_PATH
		.getLastStep()
		.format();
	private static final String PHYSICAL_USED_SPACE_FIELD_NAME = AddonDamaAdminUtil.ADDON_DAMA_DRIVE_PHYSICAL_USED_SPACE_PATH
		.getLastStep()
		.format();
	private static final String MAX_USABLE_SPACE_FIELD_NAME = AddonDamaAdminUtil.ADDON_DAMA_DRIVE_MAX_USABLE_SPACE_PATH
		.getLastStep()
		.format();

	private String pathPrefix;
	Map<String, Adaptation> existingRecordMap;
	private Adaptation currentDriveRecord;
	private boolean builtIn;

	/**
	 * Create the modifier
	 * 
	 * @param pathPrefix the prefix to use for the physical root path
	 * @param driveTable the Drive table
	 * @param driveRecordsPredicate the predicate to use to collect the pre-existing records.
	 *                              This is only needed for an import, so should be set to <code>null</code> for an export.
	 */
	public DamaDriveArtifactFileModifier(
		String pathPrefix,
		AdaptationTable driveTable,
		String driveRecordsPredicate)
	{
		this.pathPrefix = pathPrefix;

		// If a predicate was specified, then collect all existing records that match the predicate into a map.
		// This needs to be done up front because once the modifyImport is invoked, the records will be deleted
		// temporarily as part of the pseudo replace mode import, and won't be able to look them up at that point.
		if (driveRecordsPredicate != null)
		{
			existingRecordMap = new HashMap<>();
			RequestResult requestResult = driveTable.createRequestResult(driveRecordsPredicate);
			try
			{
				for (Adaptation driveRecord; (driveRecord = requestResult
					.nextAdaptation()) != null;)
				{
					existingRecordMap
						.put(driveRecord.getOccurrencePrimaryKey().format(), driveRecord);
				}
			}
			finally
			{
				requestResult.close();
			}
		}
	}

	@Override
	public List<String> modifyExport(String line)
	{
		List<String> returnVal = null;
		if (containsStartTag(line, UUID_FIELD_NAME))
		{
			String uuid = getValue(line, UUID_FIELD_NAME);
			// We need to store the fact that this is a built-in record
			builtIn = (uuid != null
				&& uuid.startsWith(DevArtifactsConstants.ADDON_BUILT_IN_RECORD_PREFIX));
		}
		// Replace max used space with an empty string.
		// This isn't a valid decimal value, but will be replaced on import.
		else if (containsStartTag(line, MAX_USABLE_SPACE_FIELD_NAME))
		{
			returnVal = Arrays.asList(replaceValue(line, MAX_USABLE_SPACE_FIELD_NAME, ""));
		}
		// Do basically the same for physical used space as we did for max usable space
		else if (containsStartTag(line, PHYSICAL_USED_SPACE_FIELD_NAME))
		{
			returnVal = Arrays.asList(replaceValue(line, PHYSICAL_USED_SPACE_FIELD_NAME, ""));
		}
		// If we're on the path field
		else if (containsStartTag(line, PHYSICAL_ROOT_PATH_FIELD_NAME))
		{
			// If it's a built-in record (as stored earlier when we encountered the UUID)
			// then clear out the path altogether, because we'll want to keep the entire
			// path that is in the target environment
			if (builtIn)
			{
				returnVal = Arrays.asList(replaceValue(line, PHYSICAL_ROOT_PATH_FIELD_NAME, ""));
			}
			// Otherwise, we need to strip the prefix off of the path
			else
			{
				String physicalRootPath = getValue(line, PHYSICAL_ROOT_PATH_FIELD_NAME);
				if (physicalRootPath != null && physicalRootPath.startsWith(pathPrefix))
				{
					int pathPrefixLen = pathPrefix.length();
					String modifiedPhysicalRootPath;
					if (physicalRootPath.length() > pathPrefixLen)
					{
						modifiedPhysicalRootPath = physicalRootPath.substring(pathPrefixLen);
					}
					else
					{
						modifiedPhysicalRootPath = "";
					}
					returnVal = Arrays.asList(
						replaceValue(
							line,
							PHYSICAL_ROOT_PATH_FIELD_NAME,
							modifiedPhysicalRootPath));
				}
			}
		}
		return returnVal;
	}

	@Override
	public List<String> modifyImport(String line)
	{
		List<String> returnVal = null;
		// For UUID field, need to store which existing record we're currently on, and whether this is a built-in record
		if (containsStartTag(line, UUID_FIELD_NAME))
		{
			String uuid = getValue(line, UUID_FIELD_NAME);
			if (uuid == null)
			{
				currentDriveRecord = null;
				builtIn = false;
			}
			else
			{
				currentDriveRecord = existingRecordMap.get(uuid);
				builtIn = uuid.startsWith(DevArtifactsConstants.ADDON_BUILT_IN_RECORD_PREFIX);
			}
		}
		// For max usable space, if there's an existing record, then replace the value with the value from
		// the existing record. We only want the value from the file when there is no existing record already.
		else if (containsStartTag(line, MAX_USABLE_SPACE_FIELD_NAME))
		{
			if (currentDriveRecord != null)
			{
				BigDecimal currentMaxUsableSpace = (BigDecimal) currentDriveRecord
					.get(AddonDamaAdminUtil.ADDON_DAMA_DRIVE_MAX_USABLE_SPACE_PATH);
				// If the current record doesn't have a value, then remove the line altogether.
				// (Shouldn't really happen since that would be invalid but if it's already invalid, just leave it invalid.)
				if (currentMaxUsableSpace == null)
				{
					returnVal = new ArrayList<>();
				}
				else
				{
					returnVal = Arrays.asList(
						replaceValue(
							line,
							MAX_USABLE_SPACE_FIELD_NAME,
							currentMaxUsableSpace.toPlainString()));
				}
			}
		}
		// Do basically the same for physical used space as we did for max usable space
		else if (containsStartTag(line, PHYSICAL_USED_SPACE_FIELD_NAME))
		{
			// If there's no current drive record, then just remove the line altogether
			// so it defaults to not defined
			if (currentDriveRecord == null)
			{
				returnVal = new ArrayList<>();
			}
			else
			{
				BigDecimal currentPhysicalUsedSpace = (BigDecimal) currentDriveRecord
					.get(AddonDamaAdminUtil.ADDON_DAMA_DRIVE_PHYSICAL_USED_SPACE_PATH);
				// If the current record doesn't have a value, then remove the line altogether.
				if (currentPhysicalUsedSpace == null)
				{
					returnVal = new ArrayList<>();
				}
				else
				{
					returnVal = Arrays.asList(
						replaceValue(
							line,
							PHYSICAL_USED_SPACE_FIELD_NAME,
							currentPhysicalUsedSpace.toPlainString()));
				}
			}
		}
		// For the path, if it's built-in, keep the existing path and ignore what's in the import file.
		// It will only use the import file's value when creating the record.
		// If it's not built-in, then prepend the prefix to the path.
		else if (containsStartTag(line, PHYSICAL_ROOT_PATH_FIELD_NAME))
		{
			String physicalRootPath = getValue(line, PHYSICAL_ROOT_PATH_FIELD_NAME);
			if (builtIn)
			{
				if (currentDriveRecord != null)
				{
					String currentPhysicalRootPath = currentDriveRecord
						.getString(AddonDamaAdminUtil.ADDON_DAMA_DRIVE_PHYSICAL_ROOT_PATH_PATH);
					String newPhysicalRootPath = (currentPhysicalRootPath == null) ? ""
						: currentPhysicalRootPath;
					returnVal = Arrays.asList(
						replaceValue(line, PHYSICAL_ROOT_PATH_FIELD_NAME, newPhysicalRootPath));
				}
			}
			else
			{
				StringBuilder bldr = new StringBuilder(pathPrefix);
				if (physicalRootPath != null)
				{
					bldr.append(physicalRootPath);
				}
				returnVal = Arrays
					.asList(replaceValue(line, PHYSICAL_ROOT_PATH_FIELD_NAME, bldr.toString()));
			}
		}
		return returnVal;
	}
}
