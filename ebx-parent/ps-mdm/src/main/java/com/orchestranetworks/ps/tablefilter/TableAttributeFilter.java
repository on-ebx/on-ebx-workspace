package com.orchestranetworks.ps.tablefilter;

import java.util.*;

import com.onwbp.adaptation.*;
import com.onwbp.base.text.*;
import com.orchestranetworks.schema.*;
import com.orchestranetworks.service.*;
import com.orchestranetworks.ui.*;

/**
 * Custom UI search filter that allows the user to specify a field to filter the table records on. 
 * The UI Search filter allows the user to filter on any number of values entered into the TextArea.
 * 
 * @author Orchestra Networks Professional Services
 *
 */
public class TableAttributeFilter extends UITableFilter 
{
	/**
	 * Filter Criteria field name used to retrieve the search criteria from the TableAttributeFilter.
	 */
	private static final String PARAM_ATTRIBUTE_FILTER_FIELD_NAME = "criteria";
	
	/**
	 * Label for the Criteria Field name.
	 */
	protected String filterFieldLabel;

	/**
	 * Maintains the values retrieved the the TableAttributeFilter's search criteria field.
	 */
	protected String filterValues = "";
	
	
	/**
	 * Attribute path that overrides the Default Attribute Path. 
	 */
	private Path filterOnAttributePath ;

	/**
	 * Keeps track of the filter criteria entered by user. Used also to maintain the value in the filter field. 
	 */
	private String filterCriteria = ""; 
	
	
	/**
	 * Constructor. Makes sure to initialize the attributePath to a Default Path. The Default Path can be
	 * overridden by calling the setAttributePath() method. 
	 */
	public TableAttributeFilter()
	{
		filterOnAttributePath = this.getDefaultFilterOnAttributePath();
		filterFieldLabel = this.getDefaultFilterFieldLabel();
	}
	
	/**
	 * Retrieves the current Path that should be used to filter by.
	 * 
	 * @return An instance of Path that defines the criteria field that the path is based on. 
	 */
	public Path getFilterOnAttributePath() 
	{
		return filterOnAttributePath;
	}

	public void setFilterOnAttributePath(Path attributePath) 
	{
		this.filterOnAttributePath = attributePath;
	}
	
	public String getFilterFieldLabel() 
	{
		return filterFieldLabel;
	}

	public void setFilterFieldLabel(String criteriaFieldLabel) 
	{
		this.filterFieldLabel = criteriaFieldLabel;
	}
	
	/**
	 * A subclass should override to return the default attribute path that should be used by the filter.
	 * 
	 * @return Path The Path of the attribute to filter on.
	 */
	protected Path getDefaultFilterOnAttributePath()
	{
		return null;
	}
	
	/** 
	 * A subclass should override to return the default Filter Label used by the field and the filter title.
	 * 
	 * @return String representing the filter label that is used for the field and filter title.
	 */
	protected String getDefaultFilterFieldLabel()
	{
		return "Default Label (Please Configure)";
	}

	@Override
	public void addForEdit(UITableFilterResponseContext ctx) 
	{

		if(this.getFilterOnAttributePath() == null)
			LoggingCategory.getKernel().error("filterOnAttributePath was never configured with a Path to a field. The TableAttributeFilter will not filter.");
		
		ctx.add("<div class=\"ebx_SearchFilter\">");
		ctx.add("<div class=\"ebx_Criteria\">");
		ctx.add("<div>" + this.getFilterFieldLabel() + ":</div>");
		
		this.writeSearchField(ctx);
		
		ctx.add("</div>");
		ctx.add("</div>");
	}
	
	/**
	 * Depending on the configuration of the Table Filter determine the type of search criteria 
	 * field to write. 
	 * 
	 * @param ctx
	 */
	protected void writeSearchField(UITableFilterResponseContext ctx)
	{
		writeTextAreaCriteriaField(ctx);
	}
	
	/**
	 * Write a text area field to capture user's search criteria. 
	 * 
	 * @param ctx
	 */
	public void writeTextAreaCriteriaField(UITableFilterResponseContext ctx)
	{
		ctx.add("<textarea name=\"" + 
				  PARAM_ATTRIBUTE_FILTER_FIELD_NAME + 
				   "\" id=\"" + 
				  PARAM_ATTRIBUTE_FILTER_FIELD_NAME + 
				   "\" class=\"ebx_APV\"  value=\""+ 
				  filterCriteria  + 
				   "\" rows=\"10\" cols=\"10\" style=\"max-width: 100%;max-height: 100%;resize: vertical;\">" +
				  filterCriteria +
				  "</textarea>");	
	}
	
	@Override
	public void handleApply(UITableFilterRequestContext ctx) 
	{
		// Build a table filter class and tell EBX to apply it		
		AdaptationFilter filter = new RecordFilter(ctx);
		ctx.setTableFilter(filter);
		
		// Set the filterCriteria value so the Table Filter contains the previous result.
		this.filterCriteria = ctx.getParameter(PARAM_ATTRIBUTE_FILTER_FIELD_NAME);
	}
	
	/**
	 * Return the label that this search will have in the EBX UI
	 */
	@Override
	public UserMessage getLabel() 
	{
		return UserMessage.createInfo(this.getFilterFieldLabel() + " Search");
	}
	
	/**
	 * Inner class that filters records according to user entry. 
	 *
	 */
	private class RecordFilter implements AdaptationFilter 
	{
		/** 
		 * Configure a new Record Filter from cafe numbers entered by the user.
		 * 
		 * @param ctx A filter request context containing the user selections.
		 */
        public RecordFilter(UITableFilterRequestContext ctx) 
        {
        	filterValues = ctx.getParameter(PARAM_ATTRIBUTE_FILTER_FIELD_NAME);	        
		}
		
        /**
         * Decide whether a record should be displayed in the search results.
         * 
         * @return true if a record is contained within the entry.
         */
        @Override
		public boolean accept(Adaptation record) 
        {
			Object recordAttributeValue = record.get(filterOnAttributePath);
			if (recordAttributeValue == null)
			{
				return false;
			}
        	
        	// split on newline, carriage return, line feed, space, pipe, comma, semicolon
        	String[] parsedValues = filterValues.split("[\n\r\f \\|,;]"); 
			return Arrays.asList(parsedValues).contains(recordAttributeValue.toString());
        	
		}
	}
}
