package com.orchestranetworks.ps.googlemaps.addressstandardization;

public final class GoogleMapsConstant {
	public final class AddressComponents {
		public final class Type {
			public final static String LONG_NAME = "long_name";
			public final static String SHORT_NAME = "short_name";

			private Type() {
			}
		}

		public final static String STREET_NUMBER = "street_number";// street number
		public final static String ROUTE = "route";
		public final static String LOCALITY = "locality";// city or town
		public final static String ADMINISTRATIVE_AREA_1 = "administrative_area_level_1";// state ISO 3166-2
		public final static String COUNTRY = "country";// country ISO 3166-1
		public final static String POSTAL_CODE = "postal_code";// zip code
		public final static String LAT = "lat";
		public final static String LONG = "long";
		public final static String FORMATTED_ADDRESS = "formatted_address";
		public final static String PREMISE = "premise";// building name
		public final static String ADMINISTRATIVE_AREA_2 = "administrative_area_level_2";// county
		public final static String POSTAL_TOWN = "postal_town";// city/town of UK

		public final static String PLUS_CODE = "plus_code";
		public final static String ROOM = "room";
		public final static String INTERSECTION = "intersection";
		public final static String COLLOQUIAL_AREA = "colloquial_area";
		public final static String NEIGHBORHOOD = "neighborhood";
		public final static String ADMINISTRATIVE_AREA_3 = "administrative_area_level_3";
		public final static String ADMINISTRATIVE_AREA_4 = "administrative_area_level_4";
		public final static String ADMINISTRATIVE_AREA_5 = "administrative_area_level_5";
		public final static String POSTAL_CODE_SUFFIX = "postal_code_suffix";
		public final static String POST_BOX = "post_box";
		public final static String SUB_PREMISE = "subpremise";
		public final static String POI = "point_of_interest";
		public final static String SUBLOCALITY_LEVEL = "sublocality";
		public final static String SUBLOCALITY_LEVEL_1 = "sublocality_level_1";
		public final static String SUBLOCALITY_LEVEL_2 = "sublocality_level_2";
		public final static String SUBLOCALITY_LEVEL_3 = "sublocality_level_3";
		public final static String SUBLOCALITY_LEVEL_4 = "sublocality_level_4";
		public final static String SUBLOCALITY_LEVEL_5 = "sublocality_level_5";

		private AddressComponents() {
		}
	}

	public final class ControlPosition {
		public final static String BOTTOM_CENTER = "google.maps.ControlPosition.BOTTOM_CENTER";
		public final static String BOTTOM_LEFT = "google.maps.ControlPosition.BOTTOM_LEFT";
		public final static String BOTTOM_RIGHT = "google.maps.ControlPosition.BOTTOM_RIGHT";
		public final static String LEFT_BOTTOM = "google.maps.ControlPosition.LEFT_BOTTOM";
		public final static String LEFT_CENTER = "google.maps.ControlPosition.LEFT_CENTER";
		public final static String LEFT_TOP = "google.maps.ControlPosition.LEFT_TOP";
		public final static String RIGHT_BOTTOM = "google.maps.ControlPosition.RIGHT_BOTTOM";
		public final static String RIGHT_CENTER = "google.maps.ControlPosition.RIGHT_CENTER";
		public final static String RIGHT_TOP = "google.maps.ControlPosition.RIGHT_TOP";
		public final static String TOP_CENTER = "google.maps.ControlPosition.TOP_CENTER";
		public final static String TOP_LEFT = "google.maps.ControlPosition.TOP_LEFT";
		public final static String TOP_RIGHT = "google.maps.ControlPosition.TOP_RIGHT";

		private ControlPosition() {
		}
	}

	public final class Events {
		public final static String PLACE_CHANGED = "place_changed";
		public final static String CLICK = "click";
		public final static String DRAG_END = "dragend";

		private Events() {
		}
	}

	public final class Icons {
		public final static String RED = "http://maps.google.com/mapfiles/ms/icons/red-dot.png";
		public final static String BLUE = "http://maps.google.com/mapfiles/ms/icons/blue-dot.png";
		public final static String GREEN = "http://maps.google.com/mapfiles/ms/icons/green-dot.png";
		public final static String ORANGE = "http://maps.google.com/mapfiles/ms/icons/orange-dot.png";
		public final static String PURPLE = "http://maps.google.com/mapfiles/ms/icons/purple-dot.png";
		public final static String PINK = "http://maps.google.com/mapfiles/ms/icons/pink-dot.png";
		public final static String YELLOW = "http://maps.google.com/mapfiles/ms/icons/yellow-dot.png";

		private Icons() {
		}
	}

	public final class MapType {
		public final static String HYBRID = "google.maps.MapTypeId.HYBRID";
		public final static String ROADMAP = "google.maps.MapTypeId.ROADMAP";
		public final static String SATELLITE = "google.maps.MapTypeId.SATELLITE";
		public final static String TERRAIN = "google.maps.MapTypeId.TERRAIN";

		private MapType() {
		}
	}

	public final class Zoom {
		public final static String LEVEL_1 = "1";
		public final static String LEVEL_2 = "2";
		public final static String LEVEL_3 = "3";
		public final static String LEVEL_4 = "4";
		public final static String LEVEL_5 = "5";
		public final static String LEVEL_6 = "6";
		public final static String LEVEL_7 = "7";
		public final static String LEVEL_8 = "8";
		public final static String LEVEL_9 = "9";
		public final static String LEVEL_10 = "10";
		public final static String LEVEL_11 = "11";
		public final static String LEVEL_12 = "12";
		public final static String LEVEL_13 = "13";
		public final static String LEVEL_14 = "14";
		public final static String LEVEL_15 = "15";
		public final static String LEVEL_16 = "16";
		public final static String LEVEL_17 = "17";
		public final static String LEVEL_18 = "18";
		public final static String LEVEL_19 = "19";
		public final static String LEVEL_20 = "20";
		public final static String LEVEL_21 = "21";
		public final static String MIN = Zoom.LEVEL_1;
		public final static String MAX = Zoom.LEVEL_21;
		public final static String DEFAULT = Zoom.LEVEL_2;
		public final static String DEFAULT_SINGLE_MARKER = Zoom.LEVEL_17;

		private Zoom() {
		}
	}

	public final class ZoomControlStyle {
		/** The Constant DEFAULT with value google.maps.ZoomControlStyle.DEFAULT. */
		public final static String DEFAULT = "google.maps.ZoomControlStyle.DEFAULT";
		/** The Constant LARGE with value google.maps.ZoomControlStyle.LARGE. */
		public final static String LARGE = "google.maps.ZoomControlStyle.LARGE";
		/** The Constant SMALL with value google.maps.ZoomControlStyle.SMALL. */
		public final static String SMALL = "google.maps.ZoomControlStyle.SMALL";

		private ZoomControlStyle() {
		}
	}

	public final static String JS_FUNCNAME_STANDARDIZE = JSPrefix.PREFIX + "standardize";
	public final static String JS_FUNCNAME_FILL_IN_ADDRESS = JSPrefix.PREFIX + "fillInAddress";
	public final static String JS_FUNCNAME_SET_EBX_VALUE = JSPrefix.PREFIX + "setEBXValue";
	public final static String JS_FUNCNAME_GET_EBX_VALUE = JSPrefix.PREFIX + "getEBXValue";
	public final static String JS_FUNCNAME_GEOLOCATE = JSPrefix.PREFIX + "geolocate";
	public final static String JS_FUNCNAME_UPDATE_MAP = JSPrefix.PREFIX + "updateMap";
	public final static String JS_FUNCNAME_REPLACE_MARKER = JSPrefix.PREFIX + "replaceMarker";
	public final static String JS_FUNCNAME_CREATE_MARKER = JSPrefix.PREFIX + "createMarker";
	public final static String JS_FUNCNAME_CREATE_GOOGLE_MAPS_ITEM = JSPrefix.PREFIX + "createGoogleMapsItem";
	public final static String JS_FUNCNAME_GET_GEOCODE = JSPrefix.PREFIX + "getGeoCode";
	public final static String JS_FUNCNAME_ADD_MARKERS_FROM_ITEMS = JSPrefix.PREFIX + "addMarkersFromItems";
}