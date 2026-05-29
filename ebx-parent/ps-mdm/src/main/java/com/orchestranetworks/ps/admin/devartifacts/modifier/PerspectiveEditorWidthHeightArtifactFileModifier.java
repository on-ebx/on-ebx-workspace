package com.orchestranetworks.ps.admin.devartifacts.modifier;

import java.util.*;

import com.orchestranetworks.ps.util.*;

/**
 * EBX 6.1.4-HF-3 introduced an issue where the Editor Height and Width fields in the ergonomic policy group
 * of perspectives were removed from the UI and replaced with Editor Min Height, Max Height, Min Width, and Max Width.
 * However, the original fields weren't actually removed, they were just hidden in the UI, so they still appear in
 * the exported XML. Then when an import is done, an error results because we don't have permission to update those
 * hidden fields.
 * 
 * If EBX is updated to fix this issue, then this process won't be necessary, but in the meantime, this avoids the issue.
 * It removes the old fields from the exported XML.
 */
public class PerspectiveEditorWidthHeightArtifactFileModifier extends ArtifactFileModifier
{
	private static final String HTML_EDITOR_HEIGHT_FIELD_NAME = AdminUtil
		.getPerspectivesErgonomicsHtmlEditorHeightPath()
		.getLastStep()
		.format();
	private static final String HTML_EDITOR_WIDTH_FIELD_NAME = AdminUtil
		.getPerspectivesErgonomicsHtmlEditorWidthPath()
		.getLastStep()
		.format();

	@Override
	public List<String> modifyExport(String line)
	{
		// If it's the older width or height field, then don't include the line in the export
		if (containsStartTag(line, HTML_EDITOR_WIDTH_FIELD_NAME) || containsStartTag(line, HTML_EDITOR_HEIGHT_FIELD_NAME))
		{
			return new ArrayList<>();
		}
		// Otherwise no special processing is needed
		return null;
	}

	@Override
	public List<String> modifyImport(String line)
	{
		// No special processing is needed for imports, since the fields were removed on export
		return null;
	}
}
