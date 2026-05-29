package com.orchestranetworks.ps.uibeaneditor;

import java.util.*;

import org.apache.commons.lang3.*;

import com.orchestranetworks.schema.*;
import com.orchestranetworks.schema.relationships.*;
import com.orchestranetworks.ui.*;

/**
 */
@Deprecated
public class RelationshipSelector extends UIBeanEditor
{

	@Override
	public void addForDisplay(final UIResponseContext pContext)
	{
		pContext.addWidget(Path.SELF);
	}

	@Override
	public void addForEdit(final UIResponseContext pContext)
	{
		List<SchemaNode> directFKs = new ArrayList<>();
		List<ReverseRelationship> reverseFKs = new ArrayList<>();
		List<ReverseRelationshipInterDataset> interDatasetReverseFKs = new ArrayList<>();
		List<ReverseRelationshipIntraDataset> intraDatasetReverseFKs = new ArrayList<>();

		SchemaNode node = pContext.getNode();
		SchemaNodeRelationships relationships = node.getRelationships();
		Iterable<ExplicitRelationship> explicitRelationships = relationships
			.getExplicitRelationships();
		for (ExplicitRelationship explicitRelationship : explicitRelationships)
		{
			if (!explicitRelationship.getSchemaNodeExplicitRelationship().isTableRefLink())
			{
				continue;
			}
			directFKs.add(explicitRelationship.getOwnerNode());
		}

		Iterable<ReverseRelationship> reverseRelationships = relationships
			.getReverseRelationships();
		for (ReverseRelationship reverseRelationship : reverseRelationships)
		{
			if (!reverseRelationship.getExplicitRelationship()
				.getSchemaNodeExplicitRelationship()
				.isTableRefLink())
			{
				continue;
			}
			reverseFKs.add(reverseRelationship);
		}

		Iterable<ReverseRelationshipIntraDataset> intraDatasetReverseRelationships = relationships
			.getIntraDatasetReverseRelationships();
		for (ReverseRelationshipIntraDataset reverseRelationshipIntraDataset : intraDatasetReverseRelationships)
		{
			if (!reverseRelationshipIntraDataset.getExplicitRelationship()
				.getSchemaNodeExplicitRelationship()
				.isTableRefLink())
			{
				continue;
			}
			intraDatasetReverseFKs.add(reverseRelationshipIntraDataset);
		}

		Iterable<ReverseRelationshipInterDataset> interDatasetReverseRelationships = relationships
			.getInterDatasetReverseRelationships();
		for (ReverseRelationshipInterDataset reverseRelationshipInterDataset : interDatasetReverseRelationships)
		{
			if (!reverseRelationshipInterDataset.getExplicitRelationship()
				.getSchemaNodeExplicitRelationship()
				.isTableRefLink())
			{
				continue;
			}
			interDatasetReverseFKs.add(reverseRelationshipInterDataset);
		}

		this.addModeSelector(pContext);

		pContext.add("<div style='padding-left:20px'>");

		this.addTableSelector(pContext);

		pContext.add("<div id='ts_fk_" + pContext.getWebName() + "'>");
		pContext.add("<span>Foreign key</span><br />");
		pContext.addWidget(Path.parse("./foreignKey"));
		pContext.add("</div>");

		pContext.add("<div id='ts_join_fk_" + pContext.getWebName() + "' style='display:none;'>");
		pContext.add("<span>Join foreign key</span><br />");
		pContext.addWidget(Path.parse("./joinForeignKey"));
		pContext.add("</div>");

		pContext
			.add("<div id='ts_join_filter_" + pContext.getWebName() + "' style='display:none;'>");
		pContext.add("<span>Filter on join table</span><br />");
		pContext.addWidget(Path.parse("./joinTableFilter"));
		pContext.add("</div>");

		pContext.add("</div>");

	}

	private void addModeSelector(final UIResponseContext pContext)
	{
		pContext
			.add("<div id='rs_mode_" + pContext.getWebName() + "' style='padding-bottom:5px;'>");

		String name = "mode_" + pContext.getWebName();
		String onChangeFnName = "refreshRelationshipSelector_" + pContext.getWebName() + "(this)";

		this.addRadioButton(
			pContext,
			"direct_" + pContext.getWebName(),
			name,
			onChangeFnName,
			"Direct");
		this.addRadioButton(
			pContext,
			"indirect_" + pContext.getWebName(),
			name,
			onChangeFnName,
			"Indirect");
		this.addRadioButton(
			pContext,
			"join_" + pContext.getWebName(),
			name,
			onChangeFnName,
			"Over a join table");

		pContext.add("</div>");

		if (!StringUtils.isBlank((String) pContext.getValue(Path.parse("./joinForeignKey"))))
		{
			pContext
				.addJS_cr("document.getElementById('join_" + pContext.getWebName() + "').click();");
		}
		else if (!StringUtils.isBlank((String) pContext.getValue(Path.parse("./table"))))
		{
			pContext.addJS_cr(
				"document.getElementById('indirect_" + pContext.getWebName() + "').click();");
		}
		else
		{
			pContext.addJS_cr(
				"document.getElementById('direct_" + pContext.getWebName() + "').click();");
		}

		pContext
			.addJS_cr("function refreshRelationshipSelector_" + pContext.getWebName() + "(radio){");
		pContext.addJS_cr("if(radio.checked){");
		pContext.addJS_cr("switch(radio.value){");
		pContext.addJS_cr("case 'direct_" + pContext.getWebName() + "' :");
		pContext.addJS_cr(
			"document.getElementById('rs_table_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_join_fk_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_join_filter_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr("break;");
		pContext.addJS_cr("case 'indirect_" + pContext.getWebName() + "' :");
		pContext.addJS_cr(
			"document.getElementById('rs_table_" + pContext.getWebName() + "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_join_fk_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_join_filter_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr("break;");
		pContext.addJS_cr("case 'join_" + pContext.getWebName() + "' :");
		pContext.addJS_cr(
			"document.getElementById('rs_table_" + pContext.getWebName() + "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_join_fk_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_join_filter_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr("break;");
		pContext.addJS_cr("}");
		pContext.addJS_cr("}");
		pContext.addJS_cr("}");
	}

	private void addRadioButton(
		final UIResponseContext pContext,
		final String pID,
		final String pName,
		final String pOnChangeFnName,
		final String pLabel)
	{
		pContext.add(
			"<input type='radio' id='" + pID + "' name='" + pName + "' value='" + pID
				+ "' onchange='" + pOnChangeFnName + "'/>");
		pContext.add("<label for='" + pID + "'>" + pLabel + "</label>");
	}

	private void addTableSelector(final UIResponseContext pContext)
	{
		pContext.add("<div id='rs_table_" + pContext.getWebName() + "' style='display:none;'>");

		pContext
			.add("<div id='rs_select_" + pContext.getWebName() + "' style='padding-bottom:5px;'>");

		String name = "select_" + pContext.getWebName();
		String onChangeFnName = "refreshTableSelector_" + pContext.getWebName() + "(this)";

		this.addRadioButton(
			pContext,
			"intra_dataset_" + pContext.getWebName(),
			name,
			onChangeFnName,
			"Same data set");
		this.addRadioButton(
			pContext,
			"intra_home_" + pContext.getWebName(),
			name,
			onChangeFnName,
			"Other data set");
		this.addRadioButton(
			pContext,
			"inter_home_" + pContext.getWebName(),
			name,
			onChangeFnName,
			"Other data space");

		pContext.add("</div>");

		pContext.add("<div id='ts_dataspace_" + pContext.getWebName() + "' style='display:none;'>");
		pContext.add("<span>Dataspace</span><br />");
		pContext.addWidget(Path.parse("./dataspace"));
		pContext.add("</div>");

		pContext.add("<div id='ts_dataset_" + pContext.getWebName() + "' style='display:none;'>");
		pContext.add("<span>Dataset</span><br />");
		pContext.addWidget(Path.parse("./dataset"));
		pContext.add("</div>");

		pContext.add("<div id='ts_table_same_dataset_" + pContext.getWebName() + "'>");
		pContext.add("<span>Table</span><br />");
		pContext.addWidget(Path.parse("./tableInSameDataset"));
		pContext.add("</div>");

		pContext.add("<div id='ts_table_" + pContext.getWebName() + "' style='display:none;'>");
		pContext.add("<span>Table</span><br />");
		pContext.addWidget(Path.parse("./table"));
		pContext.add("</div>");

		pContext.add("</div>");

		if (!StringUtils.isBlank((String) pContext.getValue(Path.parse("./dataspace"))))
		{
			pContext.addJS_cr(
				"document.getElementById('inter_home_" + pContext.getWebName() + "').click();");
		}
		else if (!StringUtils.isBlank((String) pContext.getValue(Path.parse("./dataset"))))
		{
			pContext.addJS_cr(
				"document.getElementById('intra_home_" + pContext.getWebName() + "').click();");
		}
		else
		{
			pContext.addJS_cr(
				"document.getElementById('intra_dataset_" + pContext.getWebName() + "').click();");
		}

		pContext.addJS_cr("function refreshTableSelector_" + pContext.getWebName() + "(radio){");
		pContext.addJS_cr("if(radio.checked){");
		pContext.addJS_cr("switch(radio.value){");
		pContext.addJS_cr("case 'intra_dataset_" + pContext.getWebName() + "' :");
		pContext.addJS_cr(
			"document.getElementById('ts_dataspace_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_dataset_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_table_same_dataset_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_table_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr("break;");
		pContext.addJS_cr("case 'intra_home_" + pContext.getWebName() + "' :");
		pContext.addJS_cr(
			"document.getElementById('ts_dataspace_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_dataset_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_table_same_dataset_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_table_" + pContext.getWebName() + "').style.display='';");
		pContext.addJS_cr("break;");
		pContext.addJS_cr("case 'inter_home_" + pContext.getWebName() + "' :");
		pContext.addJS_cr(
			"document.getElementById('ts_dataspace_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_dataset_" + pContext.getWebName()
				+ "').style.display='';");
		pContext.addJS_cr(
			"document.getElementById('ts_table_same_dataset_" + pContext.getWebName()
				+ "').style.display='none';");
		pContext.addJS_cr(
			"document.getElementById('ts_table_" + pContext.getWebName() + "').style.display='';");
		pContext.addJS_cr("break;");
		pContext.addJS_cr("}");
		pContext.addJS_cr("}");
		pContext.addJS_cr("}");
	}

	@Override
	public void validateInput(final UIRequestContext pContext)
	{
		String mode = pContext.getOptionalRequestParameterValue("mode_" + pContext.getWebName());
		if (!mode.equals("join_" + pContext.getWebName()))
		{
			pContext.getValueContext(Path.parse("./joinForeignKey")).setNewValue(null);
			pContext.getValueContext(Path.parse("./joinTableFilter")).setNewValue(null);
			if (!mode.equals("indirect_" + pContext.getWebName()))
			{
				pContext.getValueContext(Path.parse("./dataspace")).setNewValue(null);
				pContext.getValueContext(Path.parse("./dataset")).setNewValue(null);
				pContext.getValueContext(Path.parse("./table")).setNewValue(null);
			}
		}

		String select = pContext
			.getOptionalRequestParameterValue("select_" + pContext.getWebName());
		if (!select.equals("inter_home_" + pContext.getWebName()))
		{
			pContext.getValueContext(Path.parse("./dataspace")).setNewValue(null);
			if (!select.equals("intra_home_" + pContext.getWebName()))
			{
				pContext.getValueContext(Path.parse("./dataset")).setNewValue(null);
			}
		}
	}
}
