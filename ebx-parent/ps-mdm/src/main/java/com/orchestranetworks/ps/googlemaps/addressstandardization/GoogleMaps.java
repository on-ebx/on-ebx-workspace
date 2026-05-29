package com.orchestranetworks.ps.googlemaps.addressstandardization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;

import com.onwbp.base.text.UserMessage;
import com.orchestranetworks.ps.googlemaps.addressstandardization.GoogleMapsConstant.Events;
import com.orchestranetworks.ui.UIButtonSpecJSAction;
import com.orchestranetworks.ui.UIComponentWriter;

public final class GoogleMaps {

	private final String apiKey;
	private GoogleMapsAddressFieldMapping mapping;
	private GoogleMapsOptions options = new GoogleMapsOptions();
	private boolean variableGoogleMapsInitListAlreadyAdded = false;
	private boolean scriptGoogleMapsAlreadyAdded = false;
	private boolean functionGoogleMapsInitAlreadyAdded = false;
	private boolean functionGeolocateAlreadyAdded = false;
	private boolean functionFillInAddressAlreadyAdded = false;
	private boolean functionSetValueAlreadyAdded = false;
	private boolean functionResetAlreadyAdded = false;
	private boolean functionGetValueAlreadyAdded = false;
	private boolean functionStandardizeAlreadyAdded = false;
	private boolean functionUpdateMapAlreadyAdded = false;
	private boolean functionReplaceMarkerAlreadyAdded = false;
	private boolean functionCreateGoogleMapsItemAlreadyAdded = false;
	private boolean functionCreateMarkerAlreadyAdded = false;
	private boolean functionGetGeoCodeAlreadyAdded = false;
	private boolean functionAddMarkersFromItemsAlreadyAdded = false;

	private final static String JS_FUNCNAME_ADD_MARKERS_FROM_ITEMS = JSPrefix.PREFIX + "addMarkersFromItems";
	private final static String JS_FUNCNAME_INIT_GOOGLE_MAP = JSPrefix.PREFIX + "initGoogleMap";
	private final static String JS_FUNCNAME_INIT_GOOGLE_AUTOCOMPLETE = JSPrefix.PREFIX + "initGoogleMapAutocomplete";
	private final static String JS_FUNCNAME_RESET_AUTO_COMPLETE_FIELDS = JSPrefix.PREFIX + "resetAutocompleteInputs";

	private final static String JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION = "addressComponentsDefinition";
	private final static String JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INTERNAL_NAME = "internalName";
	private final static String JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_TYPE = "type";
	private final static String JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID = "inputId";
	private final static String JS_VARNAME_SET_EBX_VALUE_TYPE = "type";
	private final static String JS_VARNAME_GET_EBX_VALUE_TYPE = "type";
	private final static String JS_VARNAME_SET_EBX_VALUE_VALUE = "value";
	private final static String JS_VARNAME_EVENT_ARGUMENT_IN_LISTENER = "event";
	private final static String JS_VARNAME_EMPTY = "''";

	private final static String JS_VARNAME_PREFIX_AUTOCOMPLETE = "autocomplete";
	private final static String JS_VARNAME_PREFIX_MAP = "map";
	private final static String JS_VARNAME_PREFIX_ITEMS = "items";
	private final static String JS_VARNAME_PREFIX_MAPDIV = "mapDiv";
	private final static String JS_VARNAME_PREFIX_INFOWINDOW = "infowindow";
	private final static String JS_VARNAME_PREFIX_BOUNDS = "bounds";
	private final static String JS_VARNAME_PREFIX_MARKERS = "markers";

	private final static String JS_VARNAME_INIT_FUNC_LIST = "googleMapsInitFuncList";
	private final static String JS_VARNAME_NOT_DEFINED = "[not defined]";
	private final static String JS_VARNAME_EBX_NULL = "ebx:null";

	private final static String JS_VARNAME_NUMBER = "number";
	private final static String JS_VARNAME_STREET = "street";
	private final static String JS_VARNAME_POSTALCODE = "postalCode";
	private final static String JS_VARNAME_LOCALITY = "locality";
	private final static String JS_VARNAME_REGION = "administrative_area_level_1";
	private final static String JS_VARNAME_COUNTRY = "country";
	private final static String JS_VARNAME_LAT = "lat";
	private final static String JS_VARNAME_LONG = "long";
	private final static String JS_VARNAME_FORMATTED_ADDRESS = "formatted_address";
	private final static String JS_VARNAME_PREMISE = "premise";
	private final static String JS_VARNAME_SUB_PREMISE = "subpremise";
	private final static String JS_VARNAME_ADMINISTRATIVE_AREA_2 = "administrative_area_level_2";
	private final static String JS_VARNAME_POSTAL_TOWN = "postal_town";
	private final static String JS_VARNAME_PLUS_CODE = "plus_code";
	private final static String JS_VARNAME_INTERSECTION = "intersection";
	private final static String JS_VARNAME_COLLOQUIAL_AREA = "colloquial_area";
	private final static String JS_VARNAME_NEIGHBORHOOD = "neighborhood";
	private final static String JS_VARNAME_ADMINISTRATIVE_AREA_3 = "administrative_area_level_3";
	private final static String JS_VARNAME_ADMINISTRATIVE_AREA_4 = "administrative_area_level_4";
	private final static String JS_VARNAME_ADMINISTRATIVE_AREA_5 = "administrative_area_level_5";
	private final static String JS_VARNAME_POSTAL_CODE_SUFFIX = "postal_code_suffix";
	private final static String JS_VARNAME_POST_BOX = "post_box";
	private final static String JS_VARNAME_POI = "point_of_interest";
	private final static String JS_VARNAME_SUBLOCALITY = "sublocality";
	private final static String JS_VARNAME_SUBLOCALITY_1 = "sublocality_level_1";
	private final static String JS_VARNAME_SUBLOCALITY_2 = "sublocality_level_2";
	private final static String JS_VARNAME_SUBLOCALITY_3 = "sublocality_level_3";
	private final static String JS_VARNAME_SUBLOCALITY_4 = "sublocality_level_4";
	private final static String JS_VARNAME_SUBLOCALITY_5 = "sublocality_level_5";

	private final static String SUBLOCALITY_FORMATTER = "', '";
	private final static String POSTALCODE_FORMATTER = "'-'";
	private final static String AUTOCOMPLETE_FIELD_DEFAULT_STYLE = "height:40px; background-color:#F0F0F0; ";
	private final static String GOOGLE_MAPS_SCRIPT_URL = "https://maps.googleapis.com/maps/api/js?libraries=places";

	private static HashMap<String, String> getGoogleMapsAddressComponents() {
		HashMap<String, String> components = new HashMap<String, String>();
		components.put(GoogleMapsConstant.AddressComponents.STREET_NUMBER, GoogleMaps.JS_VARNAME_NUMBER);
		components.put(GoogleMapsConstant.AddressComponents.ROUTE, GoogleMaps.JS_VARNAME_STREET);
		components.put(GoogleMapsConstant.AddressComponents.LOCALITY, GoogleMaps.JS_VARNAME_LOCALITY);
		components.put(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_1, GoogleMaps.JS_VARNAME_REGION);
		components.put(GoogleMapsConstant.AddressComponents.FORMATTED_ADDRESS, GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS);
		components.put(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_2,
				GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_2);
		components.put(GoogleMapsConstant.AddressComponents.POSTAL_TOWN, GoogleMaps.JS_VARNAME_POSTAL_TOWN);
		components.put(GoogleMapsConstant.AddressComponents.PREMISE, GoogleMaps.JS_VARNAME_PREMISE);
		components.put(GoogleMapsConstant.AddressComponents.INTERSECTION, GoogleMaps.JS_VARNAME_INTERSECTION);
		components.put(GoogleMapsConstant.AddressComponents.COLLOQUIAL_AREA, GoogleMaps.JS_VARNAME_COLLOQUIAL_AREA);
		components.put(GoogleMapsConstant.AddressComponents.NEIGHBORHOOD, GoogleMaps.JS_VARNAME_NEIGHBORHOOD);
		components.put(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_3,
				GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_3);
		components.put(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_4,
				GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_4);
		components.put(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_5,
				GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_5);
		components.put(GoogleMapsConstant.AddressComponents.POSTAL_CODE_SUFFIX,
				GoogleMaps.JS_VARNAME_POSTAL_CODE_SUFFIX);
		components.put(GoogleMapsConstant.AddressComponents.POST_BOX, GoogleMaps.JS_VARNAME_POST_BOX);
		components.put(GoogleMapsConstant.AddressComponents.SUB_PREMISE, GoogleMaps.JS_VARNAME_SUB_PREMISE);
		components.put(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL, GoogleMaps.JS_VARNAME_SUBLOCALITY);
		components.put(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_1, GoogleMaps.JS_VARNAME_SUBLOCALITY_1);
		components.put(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_2, GoogleMaps.JS_VARNAME_SUBLOCALITY_2);
		components.put(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_3, GoogleMaps.JS_VARNAME_SUBLOCALITY_3);
		components.put(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_4, GoogleMaps.JS_VARNAME_SUBLOCALITY_4);
		components.put(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_5, GoogleMaps.JS_VARNAME_SUBLOCALITY_5);
		components.put(GoogleMapsConstant.AddressComponents.PLUS_CODE, GoogleMaps.JS_VARNAME_PLUS_CODE);
		components.put(GoogleMapsConstant.AddressComponents.COUNTRY, GoogleMaps.JS_VARNAME_COUNTRY);
		components.put(GoogleMapsConstant.AddressComponents.POSTAL_CODE, GoogleMaps.JS_VARNAME_POSTALCODE);
		components.put(GoogleMapsConstant.AddressComponents.LAT, GoogleMaps.JS_VARNAME_LAT);
		components.put(GoogleMapsConstant.AddressComponents.LONG, GoogleMaps.JS_VARNAME_LONG);

		return components;
	}

	public static String getFuncNameInitAutocomplete(final String pMapId) {
		return GoogleMaps.JS_FUNCNAME_INIT_GOOGLE_AUTOCOMPLETE + "_" + pMapId;
	}

	public static String getFuncNameInitMap(final String pMapId) {
		return GoogleMaps.JS_FUNCNAME_INIT_GOOGLE_MAP + "_" + pMapId;
	}

	public static String getVarNameAutocomplete(final String pAutocompleteId) {
		return GoogleMaps.JS_VARNAME_PREFIX_AUTOCOMPLETE + "_" + pAutocompleteId;
	}

	public static String getVarNameBounds(final String pMapId) {
		return GoogleMaps.JS_VARNAME_PREFIX_BOUNDS + "_" + pMapId;
	}

	public static String getVarNameInfoWindow(final String pMapId) {
		return GoogleMaps.JS_VARNAME_PREFIX_INFOWINDOW + "_" + pMapId;
	}

	public static String getVarNameItems(final String pMapId) {
		return GoogleMaps.JS_VARNAME_PREFIX_ITEMS + "_" + pMapId;
	}

	public static String getVarNameMap(final String pMapId) {
		return GoogleMaps.JS_VARNAME_PREFIX_MAP + "_" + pMapId;
	}

	public static String getVarNameMapDiv(final String pMapId) {
		return GoogleMaps.JS_VARNAME_PREFIX_MAPDIV + "_" + pMapId;
	}

	public static String getVarNameMarkers(final String pMapId) {
		return GoogleMaps.JS_VARNAME_PREFIX_MARKERS + "_" + pMapId;
	}

	private static String getRandomID() {
		String id = UUID.randomUUID().toString();
		return id.substring(0, 5);
	}

	public GoogleMaps(final String pAPIKey) {
		this.apiKey = pAPIKey;
	}

	public GoogleMaps(final String pAPIKey, final GoogleMapsAddressFieldMapping mapping) {
		this.apiKey = pAPIKey;
		this.mapping = mapping;
	}

	public GoogleMaps(final String pAPIKey, final GoogleMapsAddressFieldMapping mapping,
			final GoogleMapsOptions options) {
		this.apiKey = pAPIKey;
		this.mapping = mapping;
		this.options = options;
	}

	public GoogleMaps(final String pAPIKey, final GoogleMapsOptions options) {
		this.apiKey = pAPIKey;
		this.options = options;
	}

	public String insertAutocompleteAddressField(final UIComponentWriter pWriter, final String pPlaceholder,
			final String pStyle, final String pMapId, final boolean regionFKPresent) {
		String id = GoogleMaps.getRandomID();
		this.insertAutocompleteAddressField(pWriter, pPlaceholder, pStyle, pMapId, id, null, regionFKPresent);
		return id;
	}

	public String insertAutocompleteAddressField(final UIComponentWriter pWriter, final String pPlaceholder,
			final String pStyle, final String pMapId, final GoogleMapsOptions pCustomOptions, final boolean regionFKPresent) {
		String id = GoogleMaps.getRandomID();
		this.insertAutocompleteAddressField(pWriter, pPlaceholder, pStyle, pMapId, id, pCustomOptions, regionFKPresent);
		return id;
	}

	public void insertAutocompleteAddressField(final UIComponentWriter pWriter, final String pPlaceholder,
			final String pStyle, final String pMapId, final String pFieldId, boolean regionFKPresent) {
		this.insertAutocompleteAddressField(pWriter, pPlaceholder, pStyle, pMapId, pFieldId, null, regionFKPresent);
	}

	public void insertAutocompleteAddressField(final UIComponentWriter pWriter, final String pPlaceholder,
			final String pStyle, final String pMapId, final String pFieldId, GoogleMapsOptions pCustomOptions, final boolean regionFKPresent) {
		if (pCustomOptions == null) {
			pCustomOptions = this.getOptions();
		}
		Locale locale = pCustomOptions.getLocale();
		this.insertAutocompleteInputField(pWriter, pPlaceholder, pStyle, pFieldId);
		this.insertAutocompleteHiddenInputs(pWriter, pFieldId);
		this.addJsFunctionsForAutocomplete(pWriter, pMapId, pFieldId, pCustomOptions, regionFKPresent);
		this.insertGoogleMapsScript(pWriter, locale);
	}

	public String insertMap(final UIComponentWriter pWriter, final GoogleMapsAddressValue pAddressValue,
			final String pCustomStyle) {
		String iframeMapId = GoogleMaps.getRandomID();
		this.insertMap(pWriter, pAddressValue, pCustomStyle, null, iframeMapId);
		return iframeMapId;
	}

	public String insertMap(final UIComponentWriter pWriter, final GoogleMapsAddressValue pAddressValue,
			final String pCustomStyle, final GoogleMapsOptions pCustomOptions) {
		String iframeMapId = GoogleMaps.getRandomID();
		this.insertMap(pWriter, pAddressValue, pCustomStyle, pCustomOptions, iframeMapId);
		return iframeMapId;
	}

	public void insertMap(final UIComponentWriter pWriter, final GoogleMapsAddressValue pAddressValue,
			final String pCustomStyle, final GoogleMapsOptions pCustomOptions, final String pMapId) {
		List<GoogleMapsItem> items = new ArrayList<GoogleMapsItem>();
		if (pAddressValue != null) {
			items.add(this.createGoogleMapsItemFromAddressValue(pAddressValue));
		}
		this.insertMapWithMarkers(pWriter, items, pCustomStyle, pCustomOptions, pMapId);
	}

	public void insertMap(final UIComponentWriter pWriter, final GoogleMapsAddressValue pAddressValue,
			final String pCustomStyle, final String pMapId) {
		this.insertMap(pWriter, pAddressValue, pCustomStyle, null, pMapId);
	}

	public String insertMapWithMarkers(final UIComponentWriter pWriter, final List<GoogleMapsItem> pItems,
			final String pCustomStyle) {
		String id = GoogleMaps.getRandomID();
		this.insertMapWithMarkers(pWriter, pItems, pCustomStyle, null, id);
		return id;
	}

	public String insertMapWithMarkers(final UIComponentWriter pWriter, final List<GoogleMapsItem> pItems,
			final String pCustomStyle, final GoogleMapsOptions pCustomOptions) {
		String id = GoogleMaps.getRandomID();
		this.insertMapWithMarkers(pWriter, pItems, pCustomStyle, pCustomOptions, id);
		return id;
	}

	public void insertMapWithMarkers(final UIComponentWriter pWriter, final List<GoogleMapsItem> pItems,
			final String pCustomStyle, GoogleMapsOptions pCustomOptions, final String pMapId) {
		if (pCustomOptions == null) {
			pCustomOptions = this.getOptions();
		}
		Locale locale = pCustomOptions.getLocale();
		this.insertMapDiv(pWriter, pCustomStyle, pMapId);
		this.addVariableMapItems(pWriter, pItems, pMapId);
		this.addJsFunctionForMapsWithMakers(pWriter, pMapId, pCustomOptions);
		this.insertGoogleMapsScript(pWriter, locale);
	}

	public void insertMapWithMarkers(final UIComponentWriter pWriter, final List<GoogleMapsItem> pItems,
			final String pCustomStyle, final String pMapId) {
		this.insertMapWithMarkers(pWriter, pItems, pCustomStyle, null, pMapId);
	}

	public String insertStandardizeAddressButton(final UIComponentWriter pWriter, final UserMessage pLabel,
			final String pAutocompleteFieldId) {
		String id = GoogleMaps.getRandomID();
		this.insertStandardizeAddressButton(pWriter, pLabel, pAutocompleteFieldId, id);
		return id;
	}

	public void insertStandardizeAddressButton(final UIComponentWriter pWriter, final UserMessage pLabel,
			final String pAutocompleteFieldId, final String pButtonId) {
		String standardizeFunctionName = this.addFunctionStandardize(pWriter);
		String buttonCommand = standardizeFunctionName + "('" + pAutocompleteFieldId + "')";

		UIButtonSpecJSAction buttonSpec = new UIButtonSpecJSAction(pLabel, buttonCommand);
		buttonSpec.setId(pButtonId);
		buttonSpec.setCssClass("ebx_Button");
		pWriter.addButtonJavaScript(buttonSpec);
	}

	public void setAddressMapping(final GoogleMapsAddressFieldMapping mapping) {
		this.mapping = mapping;
	}

	public void setOptions(final GoogleMapsOptions options) {
		this.options = options;
	}

	private String addFunctionAddMarkersFromItems(final UIComponentWriter pWriter) {
		String functionName = GoogleMaps.JS_FUNCNAME_ADD_MARKERS_FROM_ITEMS;
		if (this.functionAddMarkersFromItemsAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(map, items, markersArray, infowindow, done) {");

		pWriter.addJS_cr("    var bounds = new google.maps.LatLngBounds();");
		pWriter.addJS_cr("    var itemCounter = 0; var nbItems =0;");

		pWriter.addJS_cr("    if(items) nbItems = items.length;");
		pWriter.addJS_cr("    for (var i = 0; i < nbItems; i++) {");
		pWriter.addJS_cr("        var location = items[i];");
		pWriter.addJS_cr("        (function(item) {");
		pWriter.addJS_cr("            if (item." + GoogleMapsItem.JsObject.ADDRESSVALUE + ") {");
		pWriter.addJS_cr("                if (item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.LAT + " && item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.LONG + ") {");
		pWriter.addJS_cr("                    var latLng = new google.maps.LatLng(item."
				+ GoogleMapsItem.JsObject.ADDRESSVALUE + "." + GoogleMapsItem.JsObject.AddressValue.LAT + ", item."
				+ GoogleMapsItem.JsObject.ADDRESSVALUE + "." + GoogleMapsItem.JsObject.AddressValue.LONG + ", false);");
		pWriter.addJS_cr("                    " + GoogleMapsConstant.JS_FUNCNAME_CREATE_MARKER + "(map, latLng, item."
				+ GoogleMapsItem.JsObject.ICON + ", infowindow, item." + GoogleMapsItem.JsObject.CONTENT
				+ ", markersArray);");
		pWriter.addJS_cr("                    bounds.extend(latLng);");
		pWriter.addJS_cr("                    itemCounter++;");
		pWriter.addJS_cr("                    if (itemCounter === nbItems) {");
		pWriter.addJS_cr("                        done(bounds);");
		pWriter.addJS_cr("                    }");
		pWriter.addJS_cr("                } else if (item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.CONCATENATED_ADDRESS + ") {");
		pWriter.addJS_cr("                    " + GoogleMapsConstant.JS_FUNCNAME_GET_GEOCODE + "(item."
				+ GoogleMapsItem.JsObject.ADDRESSVALUE + "." + GoogleMapsItem.JsObject.AddressValue.CONCATENATED_ADDRESS
				+ ", function(results, status) {");
		pWriter.addJS_cr("                        if (status === google.maps.GeocoderStatus.OK) {");
		pWriter.addJS_cr("                            var latLng = results[0].geometry.location;");
		pWriter.addJS_cr("                            " + GoogleMapsConstant.JS_FUNCNAME_CREATE_MARKER
				+ "(map, latLng, item." + GoogleMapsItem.JsObject.ICON + ", infowindow, item."
				+ GoogleMapsItem.JsObject.CONTENT + ", markersArray);");
		pWriter.addJS_cr("                            bounds.extend(latLng);");
		pWriter.addJS_cr("                        }");
		pWriter.addJS_cr("                        itemCounter++;");
		pWriter.addJS_cr("                        if (itemCounter === nbItems) {");
		pWriter.addJS_cr("                            done(bounds);");
		pWriter.addJS_cr("                        }");
		pWriter.addJS_cr("                    });");
		pWriter.addJS_cr("                }");
		pWriter.addJS_cr("            }");
		pWriter.addJS_cr("        })(location);");
		pWriter.addJS_cr("    }");

		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionAddMarkersFromItemsAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionCreateGoogleMapsItem(final UIComponentWriter pWriter,
			final GoogleMapsOptions pCustomOptions) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_CREATE_GOOGLE_MAPS_ITEM;
		if (this.functionCreateGoogleMapsItemAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(pk, label, latLng, address, content, icon) {");
		pWriter.addJS_cr("    var item = {};");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.PK + " = pk;");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.LABEL + " = label;");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.CONTENT + " = content;");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.ICON + " = icon;");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.ADDRESSVALUE + " = {};");
		pWriter.addJS_cr("    if (address) {");
		pWriter.addJS_cr("        address.forEach(function(component) {");

		pWriter.addJS_cr("            if(component.types.indexOf('" + GoogleMapsConstant.AddressComponents.STREET_NUMBER
				+ "') >= 0) {");
		pWriter.addJS_cr("                item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.NUMBER + " = component."
				+ pCustomOptions.getAddressComponentType(GoogleMapsConstant.AddressComponents.STREET_NUMBER) + ";");

		pWriter.addJS_cr("            } else if(component.types.indexOf('" + GoogleMapsConstant.AddressComponents.ROUTE
				+ "') >= 0) {");
		pWriter.addJS_cr("                item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.STREET + " = component."
				+ pCustomOptions.getAddressComponentType(GoogleMapsConstant.AddressComponents.ROUTE) + ";");

		pWriter.addJS_cr("            } else if(component.types.indexOf('"
				+ GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_1 + "') >= 0) {");
		pWriter.addJS_cr(
				"                item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
						+ GoogleMapsItem.JsObject.AddressValue.REGION + " = component." + pCustomOptions
								.getAddressComponentType(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_1)
						+ ";");

		pWriter.addJS_cr("            } else if(component.types.indexOf('"
				+ GoogleMapsConstant.AddressComponents.POSTAL_CODE + "') >= 0) {");
		pWriter.addJS_cr("                item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.ZIPCODE + " = component."
				+ pCustomOptions.getAddressComponentType(GoogleMapsConstant.AddressComponents.POSTAL_CODE) + ";");

		pWriter.addJS_cr("            } else if(component.types.indexOf('"
				+ GoogleMapsConstant.AddressComponents.LOCALITY + "') >= 0) {");
		pWriter.addJS_cr("                item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.CITY + " = component."
				+ pCustomOptions.getAddressComponentType(GoogleMapsConstant.AddressComponents.LOCALITY) + ";");

		pWriter.addJS_cr("            } else if(component.types.indexOf('"
				+ GoogleMapsConstant.AddressComponents.COUNTRY + "') >= 0) {");
		pWriter.addJS_cr("                item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.COUNTRY + " = component."
				+ pCustomOptions.getAddressComponentType(GoogleMapsConstant.AddressComponents.COUNTRY) + ";");
		pWriter.addJS_cr("            }");

		pWriter.addJS_cr("        });");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.CONCATENATED_ADDRESS + " = null;");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.LAT + " = latLng.lat();");
		pWriter.addJS_cr("    item." + GoogleMapsItem.JsObject.ADDRESSVALUE + "."
				+ GoogleMapsItem.JsObject.AddressValue.LONG + " = latLng.lng();");
		pWriter.addJS_cr("    return item;");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionCreateGoogleMapsItemAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionCreateMarker(final UIComponentWriter pWriter, final GoogleMapsOptions pCustomOptions) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_CREATE_MARKER;
		if (this.functionCreateMarkerAlreadyAdded) {
			return functionName;
		}

		String markerVarName = "marker";

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(map, latLng, icon, infowindow, content, markersArray) {");
		pWriter.addJS_cr("    var " + markerVarName + " = new google.maps.Marker({");
		pWriter.addJS_cr("        map: map,");
		pWriter.addJS_cr("        position: latLng,");
		pWriter.addJS_cr("        icon: icon || '" + pCustomOptions.getDefaultIcon() + "'");
		pWriter.addJS_cr("    });");
		pWriter.addJS_cr("    if (content && infowindow) {");

		String eventFunctionContent = "";
		eventFunctionContent += "infowindow.setContent(content);";
		eventFunctionContent += "infowindow.open(map, " + markerVarName + ");";
		this.addGoogleMapsEventListener(pWriter, markerVarName, Events.CLICK, eventFunctionContent);

		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("    if (markersArray) {");
		pWriter.addJS_cr("        markersArray.push(" + markerVarName + ");");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("    return " + markerVarName + ";");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionCreateMarkerAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionFillInAddress(final UIComponentWriter pWriter, final boolean regionFKPresent) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_FILL_IN_ADDRESS;
		if (this.functionFillInAddressAlreadyAdded) {
			return functionName;
		}

		this.addFunctionSetValue(pWriter, regionFKPresent);
		this.addFunctionReset(pWriter);

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(placeResult) {");
		pWriter.addJS_cr("    for (var component in " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + ") {");
		pWriter.addJS_cr("        var inputId = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION
				+ "[component].inputId;");
		pWriter.addJS_cr("        var componentElement = document.getElementById(inputId);");
		pWriter.addJS_cr("        if (componentElement) {");
		pWriter.addJS_cr("            componentElement.value = '';");
		pWriter.addJS_cr("        }");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("    " + GoogleMaps.JS_FUNCNAME_RESET_AUTO_COMPLETE_FIELDS + "();");

		pWriter.addJS_cr("    var filledComponent = []; var nbAddressComponents = 0; var regionIndex = 0; var countryIndex = 0; var countryPresent = 0; var regionPresent = 0;");
		pWriter.addJS_cr();
		pWriter.addJS_cr("    if(placeResult.address_components) nbAddressComponents = placeResult.address_components.length;");
		pWriter.addJS_cr("    for (var i = 0; i < nbAddressComponents; i++) {");
		pWriter.addJS_cr("    	var addressType = placeResult.address_components[i].types[0];");
		// if street is available skip plus_code
		pWriter.addJS_cr("    	if(filledComponent && filledComponent.length>0 && filledComponent.includes('"
				+ GoogleMaps.JS_VARNAME_STREET + "')) {");
		pWriter.addJS_cr("    		var a1 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_PLUS_CODE + "');");
		pWriter.addJS_cr("    		if(a1==0) {");
		pWriter.addJS_cr("    			continue;");
		pWriter.addJS_cr("   		}" + "}");

		// if locality is present skip populating postal town and administrative_level_3
		pWriter.addJS_cr("    	if(filledComponent && filledComponent.length>0 && filledComponent.includes('"
				+ GoogleMaps.JS_VARNAME_LOCALITY + "')) {");
		pWriter.addJS_cr("    		var a2 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_POSTAL_TOWN + "');");
		pWriter.addJS_cr("    		if(a2==0) {");
		pWriter.addJS_cr("    			continue;");
		pWriter.addJS_cr("    		var a3 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_3 + "');");
		pWriter.addJS_cr("    		if(a3==0) {");
		pWriter.addJS_cr("    			continue; }");
		pWriter.addJS_cr("   		}" + "}");
		// if ADMINISTRATIVE_AREA_2(county) is present skip populating sub administrative areas
		pWriter.addJS_cr("    	if(filledComponent && filledComponent.length>0 && filledComponent.includes('"
				+ GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_2 + "')) {");
		pWriter.addJS_cr("    		var a4 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_4 + "');");
		pWriter.addJS_cr("    		if(a4==0) {");
		pWriter.addJS_cr("    			continue; }");
		pWriter.addJS_cr("    		var a5 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_5 + "');");
		pWriter.addJS_cr("    		if(a5==0) {");
		pWriter.addJS_cr("    			continue; }");
		pWriter.addJS_cr("   	}");
		// if neighborhood is present skip populating poi
		pWriter.addJS_cr("    	if(filledComponent && filledComponent.length>0 && filledComponent.includes('"
				+ GoogleMaps.JS_VARNAME_NEIGHBORHOOD + "')) {");
		pWriter.addJS_cr("    		var a7 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_POI + "');");
		pWriter.addJS_cr("    		if(a7==0) {");
		pWriter.addJS_cr("    			continue;");
		pWriter.addJS_cr("   		}" + "}");
		// skip populating region until country gets populated
		if(regionFKPresent) {
			pWriter.addJS_cr("    	if(filledComponent && filledComponent.length>0 && !filledComponent.includes('"
					+ GoogleMaps.JS_VARNAME_COUNTRY + "')) {");
			pWriter.addJS_cr("    		var a8 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_REGION + "');");
			pWriter.addJS_cr("    		if(a8==0) {");
			pWriter.addJS_cr("    			regionIndex = i;");
			pWriter.addJS_cr("    			regionPresent = 1;");
			pWriter.addJS_cr("    			continue;");
			pWriter.addJS_cr("   		}" + "}");
			
			pWriter.addJS_cr("    		var a9 = addressType.localeCompare('" + GoogleMaps.JS_VARNAME_COUNTRY + "');");
			pWriter.addJS_cr("    		if(a9==0) {");
			pWriter.addJS_cr("    			countryIndex = i;");
			pWriter.addJS_cr("    			countryPresent = 1;");
			pWriter.addJS_cr("   		}");
		}
		//
		pWriter.addJS_cr(
				"        var componentDef = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + "[addressType];");
		pWriter.addJS_cr("        if (componentDef) {");
		pWriter.addJS_cr("            var valueType = componentDef."
				+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_TYPE + ";");
		pWriter.addJS_cr("            var componentValue = placeResult.address_components[i][valueType];");

		pWriter.addJS_cr("            document.getElementById(componentDef."
				+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID + ").value = componentValue;");
		pWriter.addJS_cr("            var internalName = componentDef.internalName;");
		pWriter.addJS_cr(
				"            " + GoogleMapsConstant.JS_FUNCNAME_SET_EBX_VALUE + "(internalName, componentValue);");
		pWriter.addJS_cr("            filledComponent.push(addressType);");
		pWriter.addJS_cr("       }");
		pWriter.addJS_cr("    }");
		
		
		if(regionFKPresent) {
		//set Region now
			pWriter.addJS_cr("        if (countryPresent==1 && regionPresent==1) {");
			pWriter.addJS_cr("        	var countryDef = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + "['"+GoogleMaps.JS_VARNAME_COUNTRY+"'];");
			pWriter.addJS_cr("        	var regionDef = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + "['"+GoogleMaps.JS_VARNAME_REGION+"'];");
			pWriter.addJS_cr("          if (countryDef && regionDef) {");
			pWriter.addJS_cr("            var countryType = countryDef."
					+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_TYPE + ";");
			pWriter.addJS_cr("            var countryValue = placeResult.address_components[countryIndex][countryType];");
			pWriter.addJS_cr("            var regionType = regionDef."
					+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_TYPE + ";");
			pWriter.addJS_cr("            var regionValue = placeResult.address_components[regionIndex][regionType];");
	
			pWriter.addJS_cr("            document.getElementById(regionDef."
					+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID + ").value = countryValue+'-'+regionValue;");
			pWriter.addJS_cr("            var regionName = regionDef.internalName;");
			pWriter.addJS_cr(
					"            " + GoogleMapsConstant.JS_FUNCNAME_SET_EBX_VALUE + "(regionName, regionValue);");
			pWriter.addJS_cr("            filledComponent.push('"+GoogleMaps.JS_VARNAME_REGION+"');");
			pWriter.addJS_cr("        	}");
			pWriter.addJS_cr("        }");
		}
		//
		
		
		
		
		pWriter.addJS_cr();
		pWriter.addJS_cr("    var latDef = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + "['"
				+ GoogleMapsConstant.AddressComponents.LAT + "'];");
		pWriter.addJS_cr("    if (latDef) {");
		pWriter.addJS_cr("        " + GoogleMaps.JS_VARNAME_LAT + " = placeResult.geometry.location.lat();");
		pWriter.addJS_cr(
				"        document.getElementById(latDef." + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID
						+ ").value = " + GoogleMaps.JS_VARNAME_LAT + ";");
		pWriter.addJS_cr("        " + GoogleMapsConstant.JS_FUNCNAME_SET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_LAT
				+ "', " + GoogleMaps.JS_VARNAME_LAT + ");");
		pWriter.addJS_cr("        filledComponent.push('" + GoogleMaps.JS_VARNAME_LAT + "');");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr();
		pWriter.addJS_cr("    var longDef = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + "['"
				+ GoogleMapsConstant.AddressComponents.LONG + "'];");
		pWriter.addJS_cr("    if (longDef) {");
		pWriter.addJS_cr("        " + GoogleMaps.JS_VARNAME_LONG + " = placeResult.geometry.location.lng();");
		pWriter.addJS_cr("        document.getElementById(longDef."
				+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID + ").value = "
				+ GoogleMaps.JS_VARNAME_LONG + ";");
		pWriter.addJS_cr("        " + GoogleMapsConstant.JS_FUNCNAME_SET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_LONG
				+ "', " + GoogleMaps.JS_VARNAME_LONG + ");");
		pWriter.addJS_cr("        filledComponent.push('" + GoogleMaps.JS_VARNAME_LONG + "');");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr();

		pWriter.addJS_cr("    var formattedAdd = " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + "['"
				+ GoogleMapsConstant.AddressComponents.FORMATTED_ADDRESS + "'];");
		pWriter.addJS_cr("    if (formattedAdd) {");
		pWriter.addJS_cr("        " + GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS + " = placeResult.formatted_address;");
		pWriter.addJS_cr("        document.getElementById(formattedAdd."
				+ GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID + ").value = "
				+ GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS + ";");
		pWriter.addJS_cr("        " + GoogleMapsConstant.JS_FUNCNAME_SET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS + "', " + GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS + ");");
		pWriter.addJS_cr("        filledComponent.push('" + GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS + "');");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr();
		pWriter.addJS_cr();
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionFillInAddressAlreadyAdded = true;
		return functionName;
	}

	@SuppressWarnings("unused")
	private String addFunctionGeolocate(final UIComponentWriter pWriter) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_GEOLOCATE;
		if (this.functionGeolocateAlreadyAdded) {
			return functionName;
		}
		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "() {");
		pWriter.addJS_cr("    if (navigator.geolocation) {");
		pWriter.addJS_cr("        navigator.geolocation.getCurrentPosition(function(position) {");
		pWriter.addJS_cr("			var lat = position.coords.latitude;");
		pWriter.addJS_cr("			var long = position.coords.longitude;");
		pWriter.addJS_cr("			return new google.maps.LatLng(lat, long);");
		pWriter.addJS_cr("        });");
		pWriter.addJS_cr("    } else {");
		pWriter.addJS_cr("        return null;");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionGeolocateAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionGetGeoCode(final UIComponentWriter pWriter) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_GET_GEOCODE;
		if (this.functionGetGeoCodeAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(address, callback) {");
		pWriter.addJS_cr("    var geocoder = new google.maps.Geocoder();");
		pWriter.addJS_cr("    geocoder.geocode({address: address}, function(results, status) {");
		pWriter.addJS_cr("        callback(results, status);");
		pWriter.addJS_cr("    });");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionGetGeoCodeAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionGetValue(final UIComponentWriter pWriter) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE;
		if (this.functionGetValueAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(" + GoogleMaps.JS_VARNAME_GET_EBX_VALUE_TYPE + ") {");

		if (this.mapping != null) {
			this.addGetInputValues(pWriter);
		} else {
			pWriter.addJS_cr("    return null;");
		}

		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionGetValueAlreadyAdded = true;
		return functionName;
	}

	private void addFunctionInitializeAutocomplete(final UIComponentWriter pWriter, final String pAutocompleteId,
			final String pMapId, final GoogleMapsOptions pCustomOptions) {
		String initAutocompleteFuncName = GoogleMaps.getFuncNameInitAutocomplete(pMapId);

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + initAutocompleteFuncName + "() {");

		pWriter.addJS_cr("    if(!google) {");
		pWriter.addJS_cr("        return;");
		pWriter.addJS_cr("    }");

		String mapVarname = GoogleMaps.getVarNameMap(pMapId);
		String markersArrayVarName = GoogleMaps.getVarNameMarkers(pMapId);
		String autocompleteVar = GoogleMaps.getVarNameAutocomplete(pAutocompleteId);

		pWriter.addJS_cr(autocompleteVar + " = new google.maps.places.Autocomplete(");
		pWriter.addJS_cr("    document.getElementById('" + pAutocompleteId + "'), { types: ['geocode'] }");
		pWriter.addJS_cr(");");

		String placeChangedEventFunction = "var newPlace = " + GoogleMaps.getVarNameAutocomplete(pAutocompleteId)
				+ ".getPlace();";
		placeChangedEventFunction += GoogleMapsConstant.JS_FUNCNAME_FILL_IN_ADDRESS + "(newPlace);";
		placeChangedEventFunction += GoogleMapsConstant.JS_FUNCNAME_UPDATE_MAP + "(" + mapVarname + ", newPlace, "
				+ pCustomOptions.getZoomSingleMarker() + ");";
		placeChangedEventFunction += GoogleMapsConstant.JS_FUNCNAME_REPLACE_MARKER + "(" + mapVarname + ", "
				+ markersArrayVarName + ", newPlace);";

		this.addGoogleMapsEventListener(pWriter, GoogleMaps.getVarNameAutocomplete(pAutocompleteId),
				Events.PLACE_CHANGED, placeChangedEventFunction);

		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		pWriter.addJS_cr();

		if (!this.variableGoogleMapsInitListAlreadyAdded) {
			this.addVariableInitFuncList(pWriter);
		}
		pWriter.addJS_cr();
		pWriter.addJS_cr(GoogleMaps.JS_VARNAME_INIT_FUNC_LIST + ".push(" + initAutocompleteFuncName + ");");
		pWriter.addJS_cr();
	}

	private void addFunctionInitializeGoogleMaps(final UIComponentWriter pWriter) {
		if (!this.functionGoogleMapsInitAlreadyAdded) {
			pWriter.addJS_cr();
			pWriter.addJS_cr("function " + GoogleMaps.JS_FUNCNAME_INIT_GOOGLE_MAP + "() {");
			pWriter.addJS_cr("    " + GoogleMaps.JS_VARNAME_INIT_FUNC_LIST + ".forEach(function(initFunction) {");
			pWriter.addJS_cr("        initFunction.call();");
			pWriter.addJS_cr("    });");
			pWriter.addJS_cr("}");
			pWriter.addJS_cr();

			this.functionGoogleMapsInitAlreadyAdded = true;
		}
	}

	private void addFunctionInitializeMapWithMarkers(final UIComponentWriter pWriter, final String pMapId,
			final GoogleMapsOptions pCustomOptions) {
		String optionsObject = this.getMapOptionsJsObject(pCustomOptions);
		String mapDivVarName = GoogleMaps.getVarNameMapDiv(pMapId);
		String mapVarName = GoogleMaps.getVarNameMap(pMapId);
		String itemsVarName = GoogleMaps.getVarNameItems(pMapId);
		String markersArrayVarName = GoogleMaps.getVarNameMarkers(pMapId);
		String infoWindowVarName = GoogleMaps.getVarNameInfoWindow(pMapId);
		String initMapFuncName = GoogleMaps.getFuncNameInitMap(pMapId);
		String boundsVarName = GoogleMaps.getVarNameBounds(pMapId);

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + initMapFuncName + "() {");
		pWriter.addJS_cr();
		pWriter.addJS_cr("    if(!google) {");
		pWriter.addJS_cr("        return;");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr();
		pWriter.addJS_cr("    " + mapVarName + " = new google.maps.Map(" + mapDivVarName + ");");
		pWriter.addJS_cr("    " + infoWindowVarName + " = new google.maps.InfoWindow(");
		pWriter.addJS_cr("        " + pCustomOptions.getInfoWindowJsObject());
		pWriter.addJS_cr("    );");
		pWriter.addJS_cr("    " + boundsVarName + " = new google.maps.LatLngBounds();");
		pWriter.addJS_cr();
		pWriter.addJS_cr("    var options = " + optionsObject + ";");
		pWriter.addJS_cr("    " + mapVarName + ".setOptions(options);");
		pWriter.addJS_cr("    " + GoogleMaps.JS_FUNCNAME_ADD_MARKERS_FROM_ITEMS + "(" + mapVarName + ", " + itemsVarName
				+ ", " + markersArrayVarName + ", " + infoWindowVarName + ", function(bounds){");
		pWriter.addJS_cr("        " + mapVarName + ".fitBounds(bounds);");
		if (pCustomOptions.getZoomSingleMarker() != null) {
			pWriter.addJS_cr("        if (" + itemsVarName + ".length === 1 ) {");
			pWriter.addJS_cr("            " + mapVarName + ".setZoom(" + pCustomOptions.getZoomSingleMarker() + ");");
			pWriter.addJS_cr("        }");
		}
		pWriter.addJS_cr("    });");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		if (!this.variableGoogleMapsInitListAlreadyAdded) {
			this.addVariableInitFuncList(pWriter);
		}
		pWriter.addJS_cr();
		pWriter.addJS_cr(GoogleMaps.JS_VARNAME_INIT_FUNC_LIST + ".push(" + initMapFuncName + ");");
		pWriter.addJS_cr();
	}

	private String addFunctionReplaceMarker(final UIComponentWriter pWriter) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_REPLACE_MARKER;
		if (this.functionReplaceMarkerAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(map, markersArray, placeResult) {");
		pWriter.addJS_cr("    if (markersArray) {");
		pWriter.addJS_cr("        for (var i = 0; i < markersArray.length; i++) {");
		pWriter.addJS_cr("            markersArray[i].setMap(null);");
		pWriter.addJS_cr("        }");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("    var latLng = placeResult.geometry.location;");
		pWriter.addJS_cr("    " + GoogleMapsConstant.JS_FUNCNAME_CREATE_MARKER
				+ "(map, latLng, null, null, null, markersArray);");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionReplaceMarkerAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionSetValue(final UIComponentWriter pWriter, final boolean regionFKPresent) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_SET_EBX_VALUE;
		if (this.functionSetValueAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(" + GoogleMaps.JS_VARNAME_SET_EBX_VALUE_TYPE + ", "
				+ GoogleMaps.JS_VARNAME_SET_EBX_VALUE_VALUE + ") {");

		if (this.mapping != null) {
			this.addSetInputValues(pWriter, regionFKPresent);
		}

		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionSetValueAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionStandardize(final UIComponentWriter pWriter) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_STANDARDIZE;
		if (this.functionStandardizeAlreadyAdded) {
			return functionName;
		}

		this.addFunctionGetValue(pWriter);

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(autocompleteId) {");
		pWriter.addJS_cr("	var myAddress = '';");

		pWriter.addJS_cr();
		pWriter.addJS_cr(
				"if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_NUMBER + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_NUMBER + "') + ' ';");
		// get rid of sub premise if present
		pWriter.addJS_cr("if(myAddress.length==0 && " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_PREMISE + "')) {");
		pWriter.addJS_cr("	var premise = " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_PREMISE + "');");
		pWriter.addJS_cr("	var sub = premise.indexOf(" + GoogleMaps.POSTALCODE_FORMATTER + ");");
		pWriter.addJS_cr("	if(sub!=-1) { ");
		pWriter.addJS_cr("		myAddress += premise.substring(0, sub)+ ' ';");
		pWriter.addJS_cr("	} else myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_PREMISE + "') + ' ';");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr("if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_STREET + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_STREET + "') + ' ';");
		pWriter.addJS_cr("if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_INTERSECTION + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_INTERSECTION + "') + ' ';");
		pWriter.addJS_cr("if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_NEIGHBORHOOD + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_NEIGHBORHOOD + "') + ' ';");
		pWriter.addJS_cr("if(myAddress.length==0 && " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_SUBLOCALITY + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_SUBLOCALITY + "') + ' ';");
		pWriter.addJS_cr(
				"if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_LOCALITY + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_LOCALITY + "') + ' ';");
		// consider country in case of partial address
		pWriter.addJS_cr("if(myAddress.length==0 && " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_2 + "'))");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_2 + "') + ' ';");
		pWriter.addJS_cr(
				"if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_REGION + "')) {");
		pWriter.addJS_cr("	var state = " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_REGION + "');");
		pWriter.addJS_cr("  var m = state.localeCompare('" + GoogleMaps.JS_VARNAME_NOT_DEFINED + "')");
		pWriter.addJS_cr("  var n = state.startsWith('" + GoogleMaps.JS_VARNAME_EBX_NULL + "')");
		pWriter.addJS_cr("    		if(m!=0 && !n) {");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_REGION + "') + ' ';");
		pWriter.addJS_cr("}" + "}");
		// get rid of suffix if present
		pWriter.addJS_cr("if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_POSTALCODE
				+ "')) {");
		pWriter.addJS_cr("	var postalcode = " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_POSTALCODE + "');");
		pWriter.addJS_cr("	var suffix = postalcode.indexOf(" + GoogleMaps.POSTALCODE_FORMATTER + ");");
		pWriter.addJS_cr("	if(suffix!=-1) { ");
		pWriter.addJS_cr("		myAddress += postalcode.substring(0, suffix)+ ' ';");
		pWriter.addJS_cr("	} else myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_POSTALCODE + "') + ' ';");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr(
				"if(" + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('" + GoogleMaps.JS_VARNAME_COUNTRY + "')) {");
		pWriter.addJS_cr("	var country = " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_COUNTRY + "');");
		pWriter.addJS_cr("  var m = country.localeCompare('" + GoogleMaps.JS_VARNAME_NOT_DEFINED + "')");
		pWriter.addJS_cr("  var n = country.startsWith('" + GoogleMaps.JS_VARNAME_EBX_NULL + "')");
		pWriter.addJS_cr("    		if(m!=0 && !n) {");
		pWriter.addJS_cr("	myAddress += " + GoogleMapsConstant.JS_FUNCNAME_GET_EBX_VALUE + "('"
				+ GoogleMaps.JS_VARNAME_COUNTRY + "');");
		pWriter.addJS_cr("}" + "}");
		pWriter.addJS_cr();
		pWriter.addJS_cr("    var autocompleteField = document.getElementById(autocompleteId);");
		pWriter.addJS_cr("    if (autocompleteField) {");
		pWriter.addJS_cr("        autocompleteField.value =  myAddress;");
		pWriter.addJS_cr("        autocompleteField.focus();");
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionStandardizeAlreadyAdded = true;
		return functionName;
	}

	private String addFunctionUpdateMap(final UIComponentWriter pWriter) {
		String functionName = GoogleMapsConstant.JS_FUNCNAME_UPDATE_MAP;
		if (this.functionUpdateMapAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("function " + functionName + "(map, placeResult, zoom) {");
		pWriter.addJS_cr("    var latLng = placeResult.geometry.location;");
		pWriter.addJS_cr("    var bounds = new google.maps.LatLngBounds().extend(latLng);");
		pWriter.addJS_cr("    map.fitBounds(bounds);");
		pWriter.addJS_cr("    map.setZoom(zoom);");
		pWriter.addJS_cr("}");
		pWriter.addJS_cr();

		this.functionUpdateMapAlreadyAdded = true;
		return functionName;
	}

	private void addGetInputValues(final UIComponentWriter pWriter) {
		pWriter.addJS_cr();
		pWriter.addJS_cr("    switch (" + GoogleMaps.JS_VARNAME_GET_EBX_VALUE_TYPE + ") {");
		pWriter.addJS_cr();

		if (this.mapping.getNumberId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_NUMBER, this.mapping.getNumberId(), false);
		}

		if (this.mapping.getStreetId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_STREET, this.mapping.getStreetId(), false);
		}

		if (this.mapping.getPremiseId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_PREMISE, this.mapping.getPremiseId(),
					false);
		}

		if (this.mapping.getIntersectionId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_INTERSECTION,
					this.mapping.getIntersectionId(), false);
		}

		if (this.mapping.getNeighborhoodId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_NEIGHBORHOOD,
					this.mapping.getNeighborhoodId(), false);
		}

		if (this.mapping.getSubLocalityId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY,
					this.mapping.getSubLocalityId(), false);
		}

		if (this.mapping.getLocalityId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_LOCALITY, this.mapping.getLocalityId(),
					false);
		}

		if (this.mapping.getPostalCodeId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_POSTALCODE, this.mapping.getPostalCodeId(),
					false);
		}

		if (this.mapping.getSubAdministrativeAreaId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_2,
					this.mapping.getSubAdministrativeAreaId(), false);
		}

		if (this.mapping.getRegionId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_REGION, this.mapping.getRegionId(), true);
		}
		if (this.mapping.getCountryId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_COUNTRY, this.mapping.getCountryId(), true);
		}

		if (this.mapping.getLatitudeId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_LAT, this.mapping.getLatitudeId(), false);
		}

		if (this.mapping.getLongitudeId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_LONG, this.mapping.getLongitudeId(), false);
		}

		if (this.mapping.getFormattedAddressId() != null) {
			this.addGetInputValuesSwitchCase(pWriter, GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS,
					this.mapping.getFormattedAddressId(), false);
		}

		pWriter.addJS_cr("    }");
		pWriter.addJS_cr();
	}

	private void addGetInputValuesSwitchCase(final UIComponentWriter pWriter, final String pTypeName,
			final String pElementId, boolean isFK) {
		pWriter.addJS_cr("        case '" + pTypeName + "':");

		// FK case
		if (isFK) {
			pWriter.addJS_cr(" var Fk =  document.getElementsByName('" + pElementId + "');");
			pWriter.addJS_cr(" if(Fk && Fk.length>0) {");
			pWriter.addJS_cr("   return Fk[0].value;");
			pWriter.addJS_cr(" } else ");
		}
		// FK case ends
		pWriter.addJS_cr("            return document.getElementById('" + pElementId + "').value;");
		pWriter.addJS_cr("break;");
	}

	private void addGoogleMapsEventListener(final UIComponentWriter pWriter, final String pHtmlElementVar,
			final String pEvent, final String pFunctionContent) {
		pWriter.addJS_cr();
		pWriter.addJS("google.maps.event.addListener(").addJS(pHtmlElementVar + ", ").addJS("'" + pEvent + "'" + ", ")
				.addJS_cr(" function(" + GoogleMaps.JS_VARNAME_EVENT_ARGUMENT_IN_LISTENER + ") {");
		pWriter.addJS_cr(pFunctionContent);
		pWriter.addJS_cr("});");
		pWriter.addJS_cr();
	}

	private void addJsFunctionForMapsWithMakers(final UIComponentWriter pWriter, final String pMapId,
			final GoogleMapsOptions pCustomOptions) {
		this.addVariableMap(pWriter, pMapId);
		this.addVariableMarkers(pWriter, pMapId);
		this.addVariableInfoWindow(pWriter, pMapId, pCustomOptions);
		this.addVariableBounds(pWriter, pMapId);
		this.addVariableInitFuncList(pWriter);
		this.addFunctionCreateMarker(pWriter, pCustomOptions);
		this.addFunctionCreateGoogleMapsItem(pWriter, pCustomOptions);
		this.addFunctionGetGeoCode(pWriter);
		this.addFunctionAddMarkersFromItems(pWriter);
		this.addFunctionInitializeMapWithMarkers(pWriter, pMapId, pCustomOptions);
		this.addFunctionInitializeGoogleMaps(pWriter);
	}

	private void addJsFunctionsForAutocomplete(final UIComponentWriter pWriter, final String pMapId,
			final String pAutocompleteId, final GoogleMapsOptions pCustomOptions, final boolean regionFKPresent) {
		this.addVariableAutocomplete(pWriter, pAutocompleteId);
		this.addVariableAddressComponentsDefinition(pWriter, pAutocompleteId, pCustomOptions);
		this.addVariableInitFuncList(pWriter);
		this.addFunctionReplaceMarker(pWriter);
		this.addFunctionUpdateMap(pWriter);
		this.addFunctionFillInAddress(pWriter, regionFKPresent);
		this.addFunctionInitializeAutocomplete(pWriter, pAutocompleteId, pMapId, pCustomOptions);
		this.addFunctionInitializeGoogleMaps(pWriter);
	}

	private void addSetInputValues(final UIComponentWriter pWriter, final boolean regionFKPresent) {
		pWriter.addJS_cr();
		pWriter.addJS_cr("    switch (" + GoogleMaps.JS_VARNAME_SET_EBX_VALUE_TYPE + ") {");
		pWriter.addJS_cr();

		if (this.mapping.getNumberId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_NUMBER, this.mapping.getNumberId(), false,
					false, null, null, regionFKPresent);
		}
		if (this.mapping.getStreetId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_STREET, this.mapping.getStreetId(), false,
					false, null, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_PLUS_CODE, this.mapping.getStreetId(), false,
					false, null, null, regionFKPresent);
		}
		if (this.mapping.getPremiseId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_PREMISE, this.mapping.getPremiseId(), true,
					false, GoogleMaps.POSTALCODE_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUB_PREMISE, this.mapping.getPremiseId(),
					true, false, GoogleMaps.POSTALCODE_FORMATTER, null, regionFKPresent);
		}
		if (this.mapping.getIntersectionId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_INTERSECTION,
					this.mapping.getIntersectionId(), false, false, null, null, regionFKPresent);
		}
		if (this.mapping.getNeighborhoodId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_NEIGHBORHOOD,
					this.mapping.getNeighborhoodId(), false, false, null, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_POI, this.mapping.getNeighborhoodId(), false,
					false, null, null, regionFKPresent);
		}
		if (this.mapping.getSubLocalityId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY, this.mapping.getSubLocalityId(),
					true, false, GoogleMaps.SUBLOCALITY_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY_1,
					this.mapping.getSubLocalityId(), true, false, GoogleMaps.SUBLOCALITY_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY_2,
					this.mapping.getSubLocalityId(), true, false, GoogleMaps.SUBLOCALITY_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY_3,
					this.mapping.getSubLocalityId(), true, false, GoogleMaps.SUBLOCALITY_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY_4,
					this.mapping.getSubLocalityId(), true, false, GoogleMaps.SUBLOCALITY_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_SUBLOCALITY_5,
					this.mapping.getSubLocalityId(), true, false, GoogleMaps.SUBLOCALITY_FORMATTER, null, regionFKPresent);
		}
		if (this.mapping.getPostalCodeId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_POSTALCODE, this.mapping.getPostalCodeId(),
					true, false, GoogleMaps.POSTALCODE_FORMATTER, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_POSTAL_CODE_SUFFIX,
					this.mapping.getPostalCodeId(), true, false, GoogleMaps.POSTALCODE_FORMATTER, null, regionFKPresent);
		}
		if (this.mapping.getLocalityId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_LOCALITY, this.mapping.getLocalityId(),
					false, false, null, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_POSTAL_TOWN, this.mapping.getLocalityId(),
					false, false, null, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_3,
					this.mapping.getLocalityId(), false, false, null, null, regionFKPresent);
		}
		if (this.mapping.getSubAdministrativeAreaId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_2,
					this.mapping.getSubAdministrativeAreaId(), false, false, null, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_4,
					this.mapping.getSubAdministrativeAreaId(), false, false, null, null, regionFKPresent);
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_ADMINISTRATIVE_AREA_5,
					this.mapping.getSubAdministrativeAreaId(), false, false, null, null, regionFKPresent);
		}
		if (this.mapping.getCountryId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_COUNTRY, this.mapping.getCountryId(), false,
					true, null, null, regionFKPresent);
		}
		if (this.mapping.getRegionId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_REGION, this.mapping.getRegionId(), false,
					true, null, this.mapping.getCountryId(), regionFKPresent);
		}
		if (this.mapping.getLatitudeId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_LAT, this.mapping.getLatitudeId(), false,
					false, null, null, regionFKPresent);
		}
		if (this.mapping.getLongitudeId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_LONG, this.mapping.getLongitudeId(), false,
					false, null, null, regionFKPresent);
		}
		if (this.mapping.getFormattedAddressId() != null) {
			this.addSetInputValueSwitchCase(pWriter, GoogleMaps.JS_VARNAME_FORMATTED_ADDRESS,
					this.mapping.getFormattedAddressId(), false, false, null, null, regionFKPresent);
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("    }");
		pWriter.addJS_cr();

	}

	private void addSetInputValueSwitchCase(final UIComponentWriter pWriter, final String pTypeName,
			final String pElementId, final boolean multivalue, final boolean isFK, final String formatter, final String oElementId, final boolean regionFKPresent) {
		pWriter.addJS_cr("        case '" + pTypeName + "':");
		if (multivalue && formatter != null) {
			pWriter.addJS_cr("   var ps_v = document.getElementById('" + pElementId + "').value;  ");
			pWriter.addJS_cr("   if(ps_v) {");
			pWriter.addJS_cr("   document.getElementById('" + pElementId + "').value = ps_v + " + formatter + "+"
					+ GoogleMaps.JS_VARNAME_SET_EBX_VALUE_VALUE + ";");
			pWriter.addJS_cr("   } else {  document.getElementById('" + pElementId + "').value =  "
					+ GoogleMaps.JS_VARNAME_SET_EBX_VALUE_VALUE + "; }");
		} else {
			pWriter.addJS_cr("   document.getElementById('" + pElementId + "').value =  "
					+ GoogleMaps.JS_VARNAME_SET_EBX_VALUE_VALUE + ";");
		}
		// FK case
		if (isFK) {
			//region follows ISO 3166-1 alpha-2 <country-region> format
			if(oElementId!=null && regionFKPresent) {
				pWriter.addJS_cr(" var dependentFk =  document.getElementsByName('" + oElementId + "');");
				pWriter.addJS_cr(" if(dependentFk && dependentFk.length>0) {");
				pWriter.addJS_cr(" var Fk =  document.getElementsByName('" + pElementId + "');");
				pWriter.addJS_cr(" if(Fk && Fk.length>0) {");
				pWriter.addJS_cr("   Fk[0].value = dependentFk[0].value+'-'+" + GoogleMaps.JS_VARNAME_SET_EBX_VALUE_VALUE + ";");
				pWriter.addJS_cr(" } }");
			} else {
				pWriter.addJS_cr(" var Fk =  document.getElementsByName('" + pElementId + "');");
				pWriter.addJS_cr(" if(Fk && Fk.length>0) {");
				pWriter.addJS_cr("   Fk[0].value =  " + GoogleMaps.JS_VARNAME_SET_EBX_VALUE_VALUE + ";");
				pWriter.addJS_cr(" }");
			}
		}
		// FK case ends
		pWriter.addJS_cr("            break;");
	}

	private void addVariableAddressComponentsDefinition(final UIComponentWriter pWriter, final String pAutocompleteId,
			final GoogleMapsOptions pCustomOptions) {
		pWriter.addJS_cr();
		pWriter.addJS_cr("var " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION + " = {");

		HashMap<String, String> addressComponents = GoogleMaps.getGoogleMapsAddressComponents();
		int nbComponents = addressComponents.size();
		int counter = 0;
		Iterator<Entry<String, String>> iterator = addressComponents.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> component = iterator.next();
			String googleMapsComponentName = component.getKey();
			String internalJsName = component.getValue();

			String inputId = this.getAddressComponentInputId(googleMapsComponentName, pAutocompleteId);

			pWriter.addJS("    ");
			pWriter.addJS_cr(googleMapsComponentName + ":");
			pWriter.addJS_cr("        {");
			pWriter.addJS_cr("            " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INTERNAL_NAME + ": '"
					+ internalJsName + "',");
			pWriter.addJS_cr("            " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_TYPE + ": '"
					+ pCustomOptions.getAddressComponentType(googleMapsComponentName) + "',");
			pWriter.addJS_cr("            " + GoogleMaps.JS_VARNAME_ADDRESS_COMPONENTS_DEFINITION_INPUT_ID + ": '"
					+ inputId + "'");
			pWriter.addJS("        }");

			counter++;
			if (counter < nbComponents) {
				pWriter.addJS_cr(",");
			}

		}

		pWriter.addJS_cr();
		pWriter.addJS_cr("};");
		pWriter.addJS_cr();
	}

	private void addVariableAutocomplete(final UIComponentWriter pWriter, final String pAutocompleteId) {
		String autocompleteVar = GoogleMaps.getVarNameAutocomplete(pAutocompleteId);
		pWriter.addJS_cr();
		pWriter.addJS_cr("var " + autocompleteVar + ";");
		pWriter.addJS_cr();
	}

	private void addVariableBounds(final UIComponentWriter pWriter, final String pMapId) {
		String boundsVarName = GoogleMaps.getVarNameBounds(pMapId);
		pWriter.addJS_cr();
		pWriter.addJS_cr("var " + boundsVarName + ";");
		pWriter.addJS_cr();
	}

	private void addVariableInfoWindow(final UIComponentWriter pWriter, final String pMapId,
			final GoogleMapsOptions pCustomOptions) {
		String infoWindowVarName = GoogleMaps.getVarNameInfoWindow(pMapId);
		pWriter.addJS_cr();
		pWriter.addJS_cr("var " + infoWindowVarName + ";");
		pWriter.addJS_cr();
	}

	private void addVariableInitFuncList(final UIComponentWriter pWriter) {
		if (!this.variableGoogleMapsInitListAlreadyAdded) {
			pWriter.addJS_cr();
			pWriter.addJS_cr("var " + GoogleMaps.JS_VARNAME_INIT_FUNC_LIST + " = [];");
			pWriter.addJS_cr();
			this.variableGoogleMapsInitListAlreadyAdded = true;
		}
	}

	private void addVariableMap(final UIComponentWriter pWriter, final String pMapId) {
		String mapVarName = GoogleMaps.getVarNameMap(pMapId);
		String mapDivVarName = GoogleMaps.getVarNameMapDiv(pMapId);

		pWriter.addJS_cr();
		pWriter.addJS_cr("var " + mapDivVarName + " = document.getElementById('" + pMapId + "');");
		pWriter.addJS_cr("var " + mapVarName + ";");
		pWriter.addJS_cr();
	}

	private void addVariableMapItems(final UIComponentWriter pWriter, final List<GoogleMapsItem> pItems,
			final String pMapId) {
		String varName = GoogleMaps.getVarNameItems(pMapId);

		pWriter.addJS_cr();
		if (pItems == null || pItems.isEmpty()) {
			pWriter.addJS_cr("var " + varName + " = [];");
		} else {
			pWriter.addJS_cr("var " + varName + " = [");

			int nbItems = pItems.size();
			for (int i = 0; i < nbItems; i++) {
				GoogleMapsItem item = pItems.get(i);

				pWriter.addJS_cr();
				pWriter.addJS_cr();
				pWriter.addJS("    " + item.getJsObject());
				if (i < nbItems - 1) {
					pWriter.addJS_cr(",");
				} else {
					pWriter.addJS_cr();
				}

			}
			pWriter.addJS_cr();
			pWriter.addJS_cr("];");
		}
		pWriter.addJS_cr();
	}

	private void addVariableMarkers(final UIComponentWriter pWriter, final String pMapId) {
		String markerVarName = GoogleMaps.getVarNameMarkers(pMapId);

		pWriter.addJS_cr();
		pWriter.addJS_cr("var " + markerVarName + " = [];");
		pWriter.addJS_cr();
	}

	private GoogleMapsItem createGoogleMapsItemFromAddressValue(final GoogleMapsAddressValue pAddressValue) {
		return new GoogleMapsItem(pAddressValue);
	}

	private String getAddressComponentInputId(final String pAddressType, final String pAutocompleteId) {
		return pAddressType + "_" + pAutocompleteId;
	}

	private String getMapOptionsJsObject(final GoogleMapsOptions pCustomOptions) {
		if (pCustomOptions != null) {
			return pCustomOptions.getMapsJsObject();
		} else if (this.options != null) {
			return this.options.getMapsJsObject();
		} else {
			return "{}";
		}
	}

	private GoogleMapsOptions getOptions() {
		if (this.options == null) {
			return new GoogleMapsOptions();
		}
		return this.options;
	}

	private void insertAutocompleteHiddenInputs(final UIComponentWriter pWriter, final String pAutocompleteId) {
		pWriter.add_cr();
		HashMap<String, String> addressComponents = GoogleMaps.getGoogleMapsAddressComponents();

		Iterator<Entry<String, String>> iterator = addressComponents.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> component = iterator.next();
			String googleMapsComponentName = component.getKey();
			String inputId = this.getAddressComponentInputId(googleMapsComponentName, pAutocompleteId);
			this.insertHiddenInput(pWriter, inputId);
		}

		pWriter.add_cr();
	}

	private String addFunctionReset(final UIComponentWriter pWriter) {
		String functionName = GoogleMaps.JS_FUNCNAME_RESET_AUTO_COMPLETE_FIELDS;
		if (this.functionResetAlreadyAdded) {
			return functionName;
		}

		pWriter.addJS_cr();
		pWriter.addJS_cr(" function " + functionName + "() {");

		pWriter.addJS_cr("   document.getElementById('" + mapping.getNumberId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getStreetId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getPremiseId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getIntersectionId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getNeighborhoodId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getSubLocalityId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getLocalityId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getSubAdministrativeAreaId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getRegionId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getPostalCodeId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getCountryId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getLatitudeId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getLongitudeId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr("   document.getElementById('" + mapping.getFormattedAddressId() + "').value =  "
				+ GoogleMaps.JS_VARNAME_EMPTY + ";");
		// check if state is FK node
		pWriter.addJS_cr(" var stateFk =  document.getElementsByName('" + mapping.getRegionId() + "');");
		pWriter.addJS_cr(" if(stateFk && stateFk.length>0)    stateFk[0].value = " + GoogleMaps.JS_VARNAME_EMPTY + ";");
		// check if country is FK node
		pWriter.addJS_cr(" var countryFk =  document.getElementsByName('" + mapping.getCountryId() + "');");
		pWriter.addJS_cr(
				" if(countryFk && countryFk.length>0)    countryFk[0].value = " + GoogleMaps.JS_VARNAME_EMPTY + ";");
		pWriter.addJS_cr(" }");
		pWriter.addJS_cr();
		this.functionResetAlreadyAdded = true;
		return functionName;
	}

	private void insertAutocompleteInputField(final UIComponentWriter pWriter, final String pPlaceholder,
			final String pStyle, final String pFieldId) {
		String defaultStyle = GoogleMaps.AUTOCOMPLETE_FIELD_DEFAULT_STYLE;
		pWriter.add("<input id=\"" + pFieldId + "\" placeholder=\"" + pPlaceholder + "\" type=\"text\" style=\""
				+ defaultStyle + pStyle + "\"></input>");
	}

	private void insertGoogleMapsScript(final UIComponentWriter pWriter, final Locale pLocale) {
		String scriptLanguage = null;
		String region = null;
		Locale locale = pWriter.getLocale();
		if (pLocale != null) {
			locale = pLocale;
		}
		if (!locale.getCountry().isEmpty()) {
			region = "region=" + locale.getCountry();
		}
		if (!locale.getLanguage().isEmpty()) {
			scriptLanguage = "language=" + locale.getLanguage();
		}

		if (!this.scriptGoogleMapsAlreadyAdded) {
			pWriter.addJS_cr("(function () {");
			pWriter.addJS_cr("    var e = document.createElement('script');");
			pWriter.addJS("    e.src=\"" + GoogleMaps.GOOGLE_MAPS_SCRIPT_URL);
			pWriter.addJS("&key=" + this.apiKey);
			if (scriptLanguage != null) {
				pWriter.addJS("&" + scriptLanguage);
			}
			if (region != null) {
				pWriter.addJS("&" + region);
			}
			pWriter.addJS("&callback=" + GoogleMaps.JS_FUNCNAME_INIT_GOOGLE_MAP);
			pWriter.addJS_cr("\";");
			pWriter.addJS_cr("    e.async = true");
			pWriter.addJS_cr("    document.body.appendChild(e);");
			pWriter.addJS_cr("}());");

			this.scriptGoogleMapsAlreadyAdded = true;
		}
	}

	private void insertHiddenInput(final UIComponentWriter pWriter, final String pId) {
		pWriter.add_cr("<input type=\"hidden\" id=\"" + pId + "\" >");
	}

	private void insertMapDiv(final UIComponentWriter pWriter, String pCustomStyle, final String pMapId) {
		if (pCustomStyle == null) {
			pCustomStyle = "height:100%;width:100%;";
		}
		pWriter.add("<div id=\"" + pMapId + "\" style=\"" + pCustomStyle + "\"></div>");
	}
}
