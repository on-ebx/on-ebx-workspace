package com.orchestranetworks.ps.ranges;

import java.math.*;
import java.util.*;

import com.orchestranetworks.ps.util.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.info.*;

public class RangeConfig
{
	private String commonValuePathsString;
	protected List<Path> commonValuePaths;
	private Path minValuePath;
	private Path maxValuePath;
	private boolean minInclusive = true;
	private boolean maxInclusive = false;
	private boolean checkOverlaps = true;
	protected boolean checkGaps = false;
	private boolean usePadding = false;
	private String padding;
	protected BigDecimal paddingNum = BigDecimal.valueOf(0);
	private final boolean maxRequired;
	private boolean sortOnMax;

	public RangeConfig()
	{
		this(true);
	}

	public RangeConfig(boolean maxRequired)
	{
		this.maxRequired = maxRequired;
	}

	public Path getCommonValuePath()
	{
		return CollectionUtils.getFirstOrNull(commonValuePaths);
	}

	public void setCommonValuePath(Path commonValuePath)
	{
		this.commonValuePaths = Collections.singletonList(commonValuePath);
	}

	public String getCommonValuePathsString()
	{
		return commonValuePathsString;
	}

	public List<Path> getCommonValuePaths()
	{
		return commonValuePaths;
	}

	public void setCommonValuePathsString(String commonValuePathsString)
	{
		this.commonValuePathsString = commonValuePathsString;
	}

	public Path getMinValuePath()
	{
		return minValuePath;
	}

	public void setMinValuePath(Path minValuePath)
	{
		this.minValuePath = minValuePath;
	}

	public Path getMaxValuePath()
	{
		return maxValuePath;
	}

	public void setMaxValuePath(Path maxValuePath)
	{
		this.maxValuePath = maxValuePath;
	}

	public boolean isMinInclusive()
	{
		return minInclusive;
	}

	public void setMinInclusive(boolean minInclusive)
	{
		this.minInclusive = minInclusive;
	}

	public boolean isMaxInclusive()
	{
		return maxInclusive;
	}

	public void setMaxInclusive(boolean maxInclusive)
	{
		this.maxInclusive = maxInclusive;
	}

	public boolean isCheckOverlaps()
	{
		return checkOverlaps;
	}

	public void setCheckOverlaps(boolean checkOverlaps)
	{
		this.checkOverlaps = checkOverlaps;
	}

	public boolean isCheckGaps()
	{
		return checkGaps;
	}

	public void setCheckGaps(boolean checkGaps)
	{
		this.checkGaps = checkGaps;
	}

	public boolean isUsePadding()
	{
		return usePadding;
	}

	public void setUsePadding(boolean usePadding)
	{
		this.usePadding = usePadding;
	}

	public String getPadding()
	{
		return padding;
	}

	public BigDecimal getPaddingNum()
	{
		return paddingNum;
	}

	public void setPadding(String padding)
	{
		this.padding = padding;
	}

	public boolean isSortOnMax()
	{
		return sortOnMax;
	}

	public void setSortOnMax(boolean sortOnMax)
	{
		this.sortOnMax = sortOnMax;
	}

	public void setup(SchemaNodeContext context)
	{
		SchemaNode parentNode = context.getSchemaNode();
		if ( !maxRequired)
			parentNode = parentNode.getNode(Path.PARENT);
		if (commonValuePathsString != null)
		{
			commonValuePaths = PathUtils.convertStringToPathList(commonValuePathsString, null);
			for (Path path : commonValuePaths)
			{
				PathUtils.setupFieldNode(context, parentNode, path, "commonValue", true, false);
			}
		}
		SchemaNode minNode = PathUtils.setupFieldNode(
			context,
			parentNode,
			minValuePath,
			"minValue",
			true,
			false);
		SchemaTypeName minType = minNode.getXsTypeName();
		SchemaNode maxNode = PathUtils.setupFieldNode(
			context,
			parentNode,
			maxValuePath,
			"maxValue",
			maxRequired,
			false);
		SchemaTypeName maxType = maxNode != null ? maxNode.getXsTypeName() : null;
		if (maxValuePath != null
			&& (minType == null || maxType == null || !minType.equals(maxType)))
		{
			context.addError("Nodes " + minValuePath.format() + " and " + maxValuePath.format()
					+ " must be matching comparable types");
		}
		else if (usePadding)
		{
			if (padding == null)
			{
				//determine the default padding based on the datatype
				if (SchemaTypeName.XS_DATE.equals(minType))
					paddingNum = RangeUtils.ONE_DAY_IN_MILLIS;
				else if (SchemaTypeName.XS_DATETIME.equals(minType))
					paddingNum = RangeUtils.ONE_SECOND_IN_MILLIS;
				else if (SchemaTypeName.XS_INT.equals(minType)
					|| SchemaTypeName.XS_INTEGER.equals(minType))
					paddingNum = RangeUtils.ONE;
				else if (SchemaTypeName.XS_DECIMAL.equals(minType))
				{
					SchemaFacetFractionDigits scaleFacet = maxNode == null ? null
						: maxNode.getFacetFractionDigits();
					if (scaleFacet != null)
					{
						int scale = scaleFacet.getFractionDigits();
						StringBuilder numString = new StringBuilder("0.");
						for (int i = 1; i < scale; i++)
						{
							numString.append("0");
						}
						numString.append("1");
						paddingNum = new BigDecimal(numString.toString());
					}
					else
					{
						paddingNum = RangeUtils.ONE_HUNDREDTH;
					}
				}
				else
				{
					context.addError("Range support with padding is limited to date, dateTime, and numeric ranges");
				}
			}
			else
			{
				try
				{
					paddingNum = new BigDecimal(padding);
				}
				catch (NumberFormatException e)
				{
					context.addError("Padding should be a numeric value");
				}
			}
		}
		if ((minInclusive == maxInclusive) && minInclusive && !usePadding)
		{
			context.addError(
				"When constraining a series of ranges, best practice is to us [) or (] so edges can match");
		}
	}

}
