package com.orchestranetworks.ps.admin.devartifacts.modifier;

import java.net.*;
import java.util.*;

import com.onwbp.adaptation.*;
import com.orchestranetworks.ps.util.*;

/**
 * Modifies a perspective artifact to remove the logos on export and keep whatever the value is already on import
 */
public class PerspectiveLogoArtifactFileModifier extends ArtifactFileModifier
{
	private static final String LOGO_URL_SVG_FIELD_NAME = AdminUtil
		.getPerspectivesColorsLogoUrlSvgPath()
		.getLastStep()
		.format();
	private static final String LOGO_URL_PNG_FIELD_NAME = AdminUtil
		.getPerspectivesColorsLogoUrlPngPath()
		.getLastStep()
		.format();
	private static final String LOGO_URL_GIF_JPG_FIELD_NAME = AdminUtil
		.getPerspectivesColorsLogoUrlGifJpgPath()
		.getLastStep()
		.format();

	private Adaptation perspectivesDataSet;

	public PerspectiveLogoArtifactFileModifier(Adaptation perspectivesDataSet)
	{
		this.perspectivesDataSet = perspectivesDataSet;
	}

	@Override
	public List<String> modifyExport(String line)
	{
		if (containsStartTag(line, LOGO_URL_SVG_FIELD_NAME))
		{
			return Arrays.asList(replaceValue(line, LOGO_URL_SVG_FIELD_NAME, ""));
		}
		if (containsStartTag(line, LOGO_URL_PNG_FIELD_NAME))
		{
			return Arrays.asList(replaceValue(line, LOGO_URL_PNG_FIELD_NAME, ""));
		}
		if (containsStartTag(line, LOGO_URL_GIF_JPG_FIELD_NAME))
		{
			return Arrays.asList(replaceValue(line, LOGO_URL_GIF_JPG_FIELD_NAME, ""));
		}
		return null;
	}

	@Override
	public List<String> modifyImport(String line)
	{
		if (containsStartTag(line, LOGO_URL_SVG_FIELD_NAME))
		{
			URI logo = (URI) perspectivesDataSet
				.get(AdminUtil.getPerspectivesColorsLogoUrlSvgPath());
			if (logo != null)
			{
				return Arrays.asList(replaceValue(line, LOGO_URL_SVG_FIELD_NAME, logo.toString()));
			}
		}
		else if (containsStartTag(line, LOGO_URL_PNG_FIELD_NAME))
		{
			URI logo = (URI) perspectivesDataSet
				.get(AdminUtil.getPerspectivesColorsLogoUrlPngPath());
			if (logo != null)
			{
				return Arrays.asList(replaceValue(line, LOGO_URL_PNG_FIELD_NAME, logo.toString()));
			}
		}
		else if (containsStartTag(line, LOGO_URL_GIF_JPG_FIELD_NAME))
		{
			URI logo = (URI) perspectivesDataSet
				.get(AdminUtil.getPerspectivesColorsLogoUrlGifJpgPath());
			if (logo != null)
			{
				return Arrays
					.asList(replaceValue(line, LOGO_URL_GIF_JPG_FIELD_NAME, logo.toString()));
			}
		}
		return null;
	}
}
