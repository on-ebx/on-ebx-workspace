package com.orchestranetworks.ps.util;

import java.util.*;

import org.json.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.schema.instance.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.comparison.*;

public class JSONUtil
{
	/** Return a json representation of a complex object */
	public static String jsonFor(GenericComplexObject obj, SchemaNode complexNode)
	{
		try
		{
			return jsonForComplexObject(obj, complexNode).toString();
		}
		catch (JSONException e)
		{
			StringBuilder sb = new StringBuilder("{");
			SchemaNode[] children = complexNode.getNodeChildren();
			boolean first = true;
			for (SchemaNode child : children)
			{
				Step step = child.getPathInAdaptation().getLastStep();
				if (!first)
					sb.append(", ");
				else
					first = false;
				sb.append("\"").append(step.format()).append("\": ");
				sb.append("\"").append(obj.getValueAt(step)).append("\"");
			}
			sb.append("}");
			return sb.toString();
		}
	}

	public static JSONObject jsonForHome(AdaptationHome home, boolean resolved) throws JSONException
	{
		JSONObject obj = new JSONObject();
		obj.put("homeKey", home.getKey().format());
		List<Adaptation> datasets = home.findAllRoots();
		JSONArray array = new JSONArray();
		for (Adaptation adaptation : datasets)
		{
			array.put(jsonObjectForAdaptation(adaptation, resolved));
		}
		obj.put("datasets", array);
		return obj;
	}

	public static JSONObject jsonObjectForAdaptation(Adaptation adaptation, boolean resolved)
		throws JSONException
	{
		JSONObject obj = new JSONObject();
		if (adaptation.isRootAdaptation())
			obj.put("name", adaptation.getAdaptationName().getStringName());
		SchemaNode node = adaptation.getSchemaNode();
		buildJson(node, adaptation, obj, resolved);
		return obj;
	}

	private static void buildJson(
		SchemaNode node,
		Adaptation adaptation,
		JSONObject obj,
		boolean resolved)
		throws JSONException
	{
		for (SchemaNode childNode : node.getNodeChildren())
		{
			Path path = childNode.getPathInAdaptation();
			if (childNode.isTableNode())
			{
				obj.append(
					path.format(),
					jsonForTable(adaptation.getTable(childNode.getPathInSchema()), resolved));
			}
			else if (childNode.isTerminalValue())
			{
				if (resolved || !childNode.isValueFunction())
					obj.append(
						path.format(),
						jsonForValue(adaptation.getValueWithoutResolution(path), childNode));
			}
			else if (childNode.isComplex())
			{
				buildJson(childNode, adaptation, obj, resolved);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object jsonForValue(Object value, SchemaNode node) throws JSONException
	{
		if (value instanceof Collection)
		{
			JSONArray array = new JSONArray();
			for (Object element : (Collection) value)
			{
				array.put(jsonForValue(element, node));
			}
			return array;
		}
		else if (value instanceof Number || value instanceof Boolean || value instanceof String)
		{
			return value;
		}
		else if (value instanceof GenericComplexObject)
		{
			return jsonForComplexObject((GenericComplexObject) value, node);
		}
		if (node.isComplex())
			return new JSONObject(value);
		if (AdaptationValue.INHERIT_VALUE.equals(value))
			return value.toString();
		return node.formatToXsString(value);
	}

	public static JSONObject jsonForComplexObject(GenericComplexObject value, SchemaNode node)
		throws JSONException
	{
		JSONObject obj = new JSONObject();
		SchemaNode[] children = node.getNodeChildren();
		for (SchemaNode child : children)
		{
			Step step = child.getPathInAdaptation().getLastStep();
			obj.put(step.format(), jsonForValue(value.getValueAt(step), child));
		}
		return obj;
	}

	public static Object jsonForTable(AdaptationTable table, boolean resolved) throws JSONException
	{
		JSONArray jsonArray = new JSONArray();
		RequestResult rr = table.createRequest().execute();
		try
		{
			Adaptation record;
			while ((record = rr.nextAdaptation()) != null)
			{
				jsonArray.put(jsonObjectForAdaptation(record, resolved));
			}
		}
		finally
		{
			rr.close();
		}
		return null;
	}

	public static JSONObject jsonForDiff(DifferenceBetweenOccurrences diff, CompareOptions opts)
		throws JSONException
	{
		JSONObject result = new JSONObject();
		result.put(
			"record-" + opts.getRightLabel(),
			jsonObjectForAdaptation(diff.getOccurrenceOnRight(), opts.isResolvedMode()));
		result.put(
			"record-" + opts.getLeftLabel(),
			jsonObjectForAdaptation(diff.getOccurrenceOnLeft(), opts.isResolvedMode()));
		return result;
	}

	public static JSONObject jsonForDiff(DifferenceBetweenInstances diff, CompareOptions opts)
		throws JSONException
	{
		JSONObject result = new JSONObject();
		for (DifferenceBetweenTables tableDiff : diff.getDeltaTables())
		{
			result.put(
				tableDiff.getLeft().getTablePath().format(),
				jsonForTableDiff(tableDiff, opts));
		}
		return result;
	}

	private static JSONObject jsonForTableDiff(
		DifferenceBetweenTables tableDiff,
		CompareOptions opts)
		throws JSONException
	{
		JSONObject result = new JSONObject();
		JSONArray right = new JSONArray();
		for (ExtraOccurrenceOnRight extra : tableDiff.getExtraOccurrencesOnRight())
		{
			right.put(jsonObjectForAdaptation(extra.getExtraOccurrence(), opts.isResolvedMode()));
		}
		result.put(opts.getRightOnlyLabel(), right);
		JSONArray left = new JSONArray();
		for (ExtraOccurrenceOnLeft extra : tableDiff.getExtraOccurrencesOnLeft())
		{
			left.put(jsonObjectForAdaptation(extra.getExtraOccurrence(), opts.isResolvedMode()));
		}
		result.put(opts.getLeftOnlyLabel(), left);
		JSONArray deltas = new JSONArray();
		for (DifferenceBetweenOccurrences delta : tableDiff.getDeltaOccurrences())
		{
			deltas.put(jsonForDiff(delta, opts));
		}
		result.put("deltas", deltas);
		return result;
	}

	/**
	 * @param home - a data space to compare to it's parent
	 * @param opts - Compare option see CompareOptions
	 * @return - JSONObject that contain the differences.
	 * @throws JSONException
	 */
	public static JSONObject getJsonDiffs(AdaptationHome home, CompareOptions opts)
		throws JSONException
	{
		AdaptationHome rightHome = home.getParent();
		return getJsonDiffs(home, rightHome, opts);
	}

	/**
	 * @param home - a source data space
	 * @param rightHome - a target data space
	 * @param opts - Compare option see CompareOptions
	 * @return - JSONObject that contain the differences.
	 * @throws JSONException
	 */
	public static JSONObject getJsonDiffs(
		AdaptationHome home,
		AdaptationHome rightHome,
		CompareOptions opts)
		throws JSONException
	{
		JSONObject result = new JSONObject();
		DifferenceBetweenHomes diff = DifferenceHelper
			.compareHomes(home, rightHome, opts.resolvedMode);
		for (DifferenceBetweenInstances datasetDiff : diff.getDeltaInstances())
		{
			result.put(
				datasetDiff.getInstanceOnLeft().getAdaptationName().getStringName(),
				jsonForDiff(datasetDiff, opts));
		}
		return result;
	}

	public static class CompareOptions
	{
		private String leftLabel = "left";
		private String rightLabel = "right";
		private String leftOnlyLabel = "onlyInLeft";
		private String rightOnlyLabel = "onlyInRight";
		private boolean resolvedMode = false;
		public String getLeftLabel()
		{
			return leftLabel;
		}
		public void setLeftLabel(String leftLabel)
		{
			this.leftLabel = leftLabel;
		}
		public String getRightLabel()
		{
			return rightLabel;
		}
		public void setRightLabel(String rightLabel)
		{
			this.rightLabel = rightLabel;
		}
		public boolean isResolvedMode()
		{
			return resolvedMode;
		}
		public void setResolvedMode(boolean resolvedMode)
		{
			this.resolvedMode = resolvedMode;
		}
		public String getLeftOnlyLabel()
		{
			return leftOnlyLabel;
		}
		public void setLeftOnlyLabel(String leftOnlyLabel)
		{
			this.leftOnlyLabel = leftOnlyLabel;
		}
		public String getRightOnlyLabel()
		{
			return rightOnlyLabel;
		}
		public void setRightOnlyLabel(String rightOnlyLabel)
		{
			this.rightOnlyLabel = rightOnlyLabel;
		}
	}
}
