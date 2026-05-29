package com.orchestranetworks.ps.googlemaps.widget;

import java.util.Locale;

import com.onwbp.adaptation.Adaptation;
import com.onwbp.adaptation.AdaptationTable;
import com.onwbp.base.text.UserMessage;
import com.orchestranetworks.instance.ValueContext;
import com.orchestranetworks.ps.googlemaps.addressstandardization.GoogleMaps;
import com.orchestranetworks.ps.googlemaps.addressstandardization.GoogleMapsAddressFieldMapping;
import com.orchestranetworks.ps.googlemaps.addressstandardization.GoogleMapsAddressValue;
import com.orchestranetworks.ps.googlemaps.message.Messages;
import com.orchestranetworks.schema.Path;
import com.orchestranetworks.schema.SchemaNode;
import com.orchestranetworks.schema.info.SchemaNodeDefaultView;
import com.orchestranetworks.ui.form.widget.UISimpleCustomWidget;
import com.orchestranetworks.ui.form.widget.WidgetDisplayContext;
import com.orchestranetworks.ui.form.widget.WidgetFactoryContext;
import com.orchestranetworks.ui.form.widget.WidgetWriter;

/**
 * This is a ready to use custom widget to display a complex group of fields
 * standardizing address using google api 
 * Feature: 
 * Google Auto-completion,
 * Dynamic Map population, 
 * Standardization Refer
 * Documentation: company-mdm/doc/GoogleMapsAddressStandardizationWidgetSetting.docx
 * 
 * @author noiritabera
 *
 */
public class AddressStandardizationWidget extends UISimpleCustomWidget {
	String google_api_key;
	SchemaNode widget_group_node;
	String CONST_WIDGET_GROUP_NODE;
	final String CONST_TAB = "tab";

	public AddressStandardizationWidget(final WidgetFactoryContext context) {
		super(context);
	}

	public AddressStandardizationWidget(final WidgetFactoryContext context, String google_api_key,
			SchemaNode widge_Node) {
		this(context);
		this.google_api_key = google_api_key;
		this.widget_group_node = widge_Node;
		this.CONST_WIDGET_GROUP_NODE = context.getPath().format()
				.substring(context.getPath().format().lastIndexOf("/") + 1);
	}

	@Override
	public void write(final WidgetWriter writer, final WidgetDisplayContext context) {

		Locale locale = writer.getLocale();
		if (context.isDisplayedInTable()) {
			writer.addWidget(Path.parse("formattedAddress"));
		} else if (context.isDisplayedInForm()) {
			SchemaNodeDefaultView detail = this.widget_group_node.getDefaultViewProperties();
			if (detail != null) {
				String displayMode = detail.getDisplayMode();
				boolean isTab = CONST_TAB.equals(displayMode);
				ValueContext valueContext = context.getValueContext();
				Adaptation dataset = context.getValueContext().getAdaptationInstance();
				boolean readWrite = false;

				if (valueContext == null) {
					readWrite = writer.getSession().getPermissions().getAdaptationAccessPermission(dataset)
							.isReadWrite();
				} else {
					AdaptationTable table = valueContext.getAdaptationTable();
					readWrite = writer.getSession().getPermissions()
							.getTableActionPermissionToCreateRootOccurrence(table).isEnabled();
				}
				GoogleMapsAddressFieldMapping mapping = new AddressAreaConstant().getGoogleMapsMapping();

				// Instantiate a new GoogleMaps object
				GoogleMaps googleMaps = new GoogleMaps(this.google_api_key, mapping);
				String mapId = "LocationMap";
				String autocompleteFieldId = "LocationAutocompleteField";
				// Left side
				boolean fkNodePresent = false;
				boolean regionFKPresent = false;
				String leftDivStyle = "";
				String autoCompDivStyle = "";
				String rightDivStyle = "";
				if (readWrite) {
					SchemaNode[] children = this.widget_group_node.getNodeChildren();
					for (SchemaNode child : children) {
						if (child.getFacetOnTableReference() != null) {
							fkNodePresent = true;
							if(child.getPathInAdaptation().format().contains("stateProvince")) {
								regionFKPresent = true;
								break;
							}
							//break;
						}
					}
					if (fkNodePresent) {
						if (isTab) {
							writer.add(
									"<div style=\"width:1120px; padding: 10px 10px 25px 20px; margin-right: 15px; border-radius:0 0 4pm 4px; display:flex; flex-direction:row; border:1px solid #E0E0E0\">");
							autoCompDivStyle += "width:91%; margin:10px 0px 30px;";
						} else {
							writer.add(
									"<div style=\"width:1120px; padding: 10px; margin-right: 15px; border-radius:0 0 4pm 4px; display:flex; flex-direction:row; border:1px solid #E0E0E0\">");
							autoCompDivStyle += "width:89%; margin:10px 0px 30px 20px;";
						}
						leftDivStyle += "min-width:55%; padding-top:10px; bottom:0;";
						rightDivStyle += "display:flex; flex-direction:column; width:45%; padding:15px 10px 5px;";
					} else {
						if (isTab) {
							writer.add(
									"<div style=\"width:1050px; padding: 10px 10px 25px 20px; margin-right: 15px; border-radius:0 0 4pm 4px; display:flex; flex-direction:row; border:1px solid #E0E0E0\">");
							autoCompDivStyle += "width:98%; margin:10px 0px 30px;";
						} else {
							writer.add(
									"<div style=\"width:1020px; padding: 10px; border-radius:0 0 4pm 4px; margin-right: 15px; display:flex; flex-direction:row; border:1px solid #E0E0E0\">");
							autoCompDivStyle += "width:96%; margin:10px 15px 30px;";
						}
						leftDivStyle += "min-width:55%; padding-top:10px; bottom:0;";
						rightDivStyle += "display:flex; flex-direction:column; width:45%; padding:15px 10px 5px;";
					}
				} else {
					if (isTab) {
						writer.add(
								"<div style=\"width:1050px; padding: 15px; margin-right: 15px; border-radius:0 0 4pm 4px; display:flex; flex-direction:row; border:1px solid #E0E0E0\">");
					} else {
						writer.add(
								"<div style=\"width:1050px; padding: 15px 15px 15px 0px; margin-right: 15px; border-radius:0 0 4pm 4px; display:flex; flex-direction:row; border:1px solid #E0E0E0\">");
					}
					leftDivStyle += "min-width:55%; padding:10px; bottom:0;";
					rightDivStyle += "width:45%; display:flex; flex-direction:column; padding:10px;";
				}
				writer.add("<div style=\"" + leftDivStyle + "\">");
				if (readWrite) {
					String placeholder = Messages.get(this.getClass(), locale, "addressFieldPlaceholder");
					googleMaps.insertAutocompleteAddressField(writer, placeholder, autoCompDivStyle, mapId,
							autocompleteFieldId, regionFKPresent);
				}
				SchemaNode[] children = this.widget_group_node.getNodeChildren();
				for (SchemaNode child : children) {
					String path = child.getPathInAdaptation().format()
							.substring(child.getPathInAdaptation().format().lastIndexOf("/") + 1);
					writer.addFormRow(Path.parse(path));
				}
				if (readWrite) {
					writer.add("<div>&nbsp;</div>");
					// writer.startFormRow(new UIFormLabelSpec(""));
					UserMessage standardizeButtonLabel = Messages.getInfo(this.getClass(),
							"addressStandardizeButtonLabel");
					googleMaps.insertStandardizeAddressButton(writer, standardizeButtonLabel, autocompleteFieldId);
					// writer.endFormRow();
				}
				writer.add("</div>");
				writer.add("<div style=\"" + rightDivStyle + "\">");
				GoogleMapsAddressValue addressValue = GoogleMapsAddressValue.createAddressValue(valueContext, mapping,
						locale);
				if (readWrite) {
					googleMaps.insertMap(writer, addressValue, "width:100%; height:93%;", mapId);
				} else {
					googleMaps.insertMap(writer, addressValue, "width:100%; height:95%;", mapId);
				}
				writer.add("</div>");
				writer.add("</div>");
			}
		}
	}

	public class AddressAreaConstant {
		AddressAreaConstant() {
		}

		public final String WIDGET_FIELD_PREFIX = "___40_cfvAO__" + CONST_WIDGET_GROUP_NODE + "__";
		public final String WIDGET_NUMBER_ID = WIDGET_FIELD_PREFIX + "number";
		public final String WIDGET_STREET_ID = WIDGET_FIELD_PREFIX + "street";
		public final String WIDGET_PREMISE_ID = WIDGET_FIELD_PREFIX + "premise";
		public final String WIDGET_INTERSECTION_ID = WIDGET_FIELD_PREFIX + "intersection";
		public final String WIDGET_NEIGHBORHOOD_ID = WIDGET_FIELD_PREFIX + "neighborhood";
		public final String WIDGET_SUBLOCALITY_ID = WIDGET_FIELD_PREFIX + "subLocality";
		public final String WIDGET_POSTALCODE_ID = WIDGET_FIELD_PREFIX + "postalCode";
		public final String WIDGET_LOCALITY_ID = WIDGET_FIELD_PREFIX + "city";
		public final String WIDGET_SUBADMINISTRATIVEAREA_ID = WIDGET_FIELD_PREFIX + "county";
		public final String WIDGET_REGION_ID = WIDGET_FIELD_PREFIX + "stateProvince";
		public final String WIDGET_COUNTRY_ID = WIDGET_FIELD_PREFIX + "country";
		public final String WIDGET_LAT_ID = WIDGET_FIELD_PREFIX + "latitude";
		public final String WIDGET_LONG_ID = WIDGET_FIELD_PREFIX + "longitude";
		public final String WIDGET_FORMATTED_ADDRESS_ID = WIDGET_FIELD_PREFIX + "formattedAddress";

		public GoogleMapsAddressFieldMapping getGoogleMapsMapping() {
			GoogleMapsAddressFieldMapping mapping = new GoogleMapsAddressFieldMapping();
			mapping.setNumberPath(widget_group_node.getPathInAdaptation().add(Path.parse("number")));
			mapping.setStreetPath(widget_group_node.getPathInAdaptation().add(Path.parse("street")));
			mapping.setPremisePath(widget_group_node.getPathInAdaptation().add(Path.parse("premise")));
			mapping.setIntersectionPath(widget_group_node.getPathInAdaptation().add(Path.parse("intersection")));
			mapping.setNeighborhoodPath(widget_group_node.getPathInAdaptation().add(Path.parse("neighborhood")));
			mapping.setSubLocalityPath(widget_group_node.getPathInAdaptation().add(Path.parse("subLocality")));
			mapping.setLocalityPath(widget_group_node.getPathInAdaptation().add(Path.parse("city")));
			mapping.setSubAdministrativeAreaPath(
					widget_group_node.getPathInAdaptation().add(Path.parse("county")));
			mapping.setRegionPath(widget_group_node.getPathInAdaptation().add(Path.parse("stateProvince")));
			mapping.setPostalCodePath(widget_group_node.getPathInAdaptation().add(Path.parse("postalCode")));
			mapping.setCountryPath(widget_group_node.getPathInAdaptation().add(Path.parse("country")));
			mapping.setLatitudePath(widget_group_node.getPathInAdaptation().add(Path.parse("latitude")));
			mapping.setLongitudePath(widget_group_node.getPathInAdaptation().add(Path.parse("longitude")));
			mapping.setFormattedAddressPath(
					widget_group_node.getPathInAdaptation().add(Path.parse("formattedAddress")));
			mapping.setNumberId(WIDGET_NUMBER_ID);
			mapping.setStreetId(WIDGET_STREET_ID);
			mapping.setPremiseId(WIDGET_PREMISE_ID);
			mapping.setIntersectionId(WIDGET_INTERSECTION_ID);
			mapping.setNeighborhoodId(WIDGET_NEIGHBORHOOD_ID);
			mapping.setSubLocalityId(WIDGET_SUBLOCALITY_ID);
			mapping.setLocalityId(WIDGET_LOCALITY_ID);
			mapping.setSubAdministrativeAreaId(WIDGET_SUBADMINISTRATIVEAREA_ID);
			mapping.setRegionId(WIDGET_REGION_ID);
			mapping.setPostalCodeId(WIDGET_POSTALCODE_ID);
			mapping.setCountryId(WIDGET_COUNTRY_ID);
			mapping.setLatitudeId(WIDGET_LAT_ID);
			mapping.setLongitudeId(WIDGET_LONG_ID);
			mapping.setFormattedAddressId(WIDGET_FORMATTED_ADDRESS_ID);
			return mapping;
		}
	}
}
