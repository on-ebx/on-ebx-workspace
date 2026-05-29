package com.orchestranetworks.ps.googlemaps.addressstandardization;

import java.util.Locale;

public final class GoogleMapsOptions {
	private final Double defaultCenterLat = Double.valueOf(23.3100192);
	private final Double defaultCenterLong = Double.valueOf(-1.8458414);
	private Double initialCenterLat = this.defaultCenterLat;
	private Double initialCenterLong = this.defaultCenterLong;
	private boolean disableControls = false;
	private boolean draggable = true;
	private boolean draggableMarker = false;
	private String mapType = GoogleMapsConstant.MapType.ROADMAP;
	private boolean mapTypeControl = false;
	private String mapTypeControlPosition = "null";
	private String zoomSingleMarker = GoogleMapsConstant.Zoom.DEFAULT_SINGLE_MARKER;
	private String defaultZoom = GoogleMapsConstant.Zoom.DEFAULT;
	private String minZoom = GoogleMapsConstant.Zoom.MIN;
	private String maxZoom = GoogleMapsConstant.Zoom.MAX;
	private boolean zoomControl = true;
	private String zoomControlPosition = "null";
	private String zoomControlStyle = "null";
	private boolean overviewMapControl = false;
	private boolean panControl = false;
	private String panControlPosition = "null";
	private boolean rotateControl = false;
	private String rotateControlPosition = "null";
	private boolean scaleControl = false;
	private boolean scrollwheel = true;
	private boolean streetViewControl = false;
	private String streetViewControlPosition = "null";
	private String defaultIcon;
	private Integer infoWindowMaxWidth;
	//address component
	private String numberAddressComponentType;
	private String streetAddressComponentType;
	private String zipCodeAddressComponentType;
	private String postalcodeprefixAddressComponentType;
	private String cityAddressComponentType;
	private String postal_townAddressComponentType;
	private String regionAddressComponentType;
	private String countryAddressComponentType;
	private String premiseAddressComponentType;
	private String subpremiseAddressComponentType;
	private String administrative_area_level_2AddressComponentType;
	private String administrative_area_level_3AddressComponentType;
	private String administrative_area_level_4AddressComponentType;
	private String administrative_area_level_5AddressComponentType;
	private String intersectionAddressComponentType;
	private String colloquial_areaAddressComponentType;
	private String postboxAddressComponentType;
	private String neighborhoodAddressComponentType;
	private String sublocalityAddressComponentType;
	private String sublocality1AddressComponentType;
	private String sublocality2AddressComponentType;
	private String sublocality3AddressComponentType;
	private String sublocality4AddressComponentType;
	private String sublocality5AddressComponentType;
	private String plusCodeAddressComponentType;
	
	protected String getAddressComponentType(final String addressComponent) {
		if (addressComponent == null) {
			return "";
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.STREET_NUMBER)) {
			return this.getNumberAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.POSTAL_CODE_SUFFIX)) {
			return this.getPostalcodeprefixAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.COLLOQUIAL_AREA)) {
			return this.getColloquial_areaAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUB_PREMISE)) {
			return this.getSubpremiseAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.INTERSECTION)) {
			return this.getIntersectionAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.POST_BOX)) {
			return this.getPostboxAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.ROUTE)) {
			return this.getStreetAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_1)) {
			return this.getRegionAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_2)) {
			return this.getAdministrative_area_level_2AddressComponentType();
		}else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_3)) {
				return this.getAdministrative_area_level_3AddressComponentType();
		}else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_4)) {
			return this.getAdministrative_area_level_3AddressComponentType();
		}else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.ADMINISTRATIVE_AREA_5)) {
			return this.getAdministrative_area_level_3AddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.POSTAL_CODE)) {
			return this.getZipCodeAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.LOCALITY)) {
			return this.getCityAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.COUNTRY)) {
			return this.getCountryAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.PREMISE)) {
			return this.getPremiseAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.POSTAL_TOWN)) {
			return this.getPostal_townAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.NEIGHBORHOOD)) {
			return this.getNeighborhoodAddressComponentType();
		}else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL)) {
			return this.getSublocalityAddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_1)) {
			return this.getSublocality1AddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_2)) {
			return this.getSublocality2AddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_3)) {
			return this.getSublocality3AddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_4)) {
			return this.getSublocality4AddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.SUBLOCALITY_LEVEL_5)) {
			return this.getSublocality5AddressComponentType();
		} else if (addressComponent.equals(GoogleMapsConstant.AddressComponents.PLUS_CODE)) {
			return this.getPlusCodeAddressComponentType();
		} else {
			return "";
		}
	}
	public String getPostalcodeprefixAddressComponentType() {
		if (this.postalcodeprefixAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return postalcodeprefixAddressComponentType;
	}

	public void setPostalcodeprefixAddressComponentType(String postalcodeprefixAddressComponentType) {
		this.postalcodeprefixAddressComponentType = postalcodeprefixAddressComponentType;
	}

	public String getSubpremiseAddressComponentType() {
		if (this.subpremiseAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return subpremiseAddressComponentType;
	}

	public void setSubpremiseAddressComponentType(String subpremiseAddressComponentType) {
		this.subpremiseAddressComponentType = subpremiseAddressComponentType;
	}

	public String getAdministrative_area_level_3AddressComponentType() {
		if (this.administrative_area_level_3AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return administrative_area_level_3AddressComponentType;
	}

	public void setAdministrative_area_level_3AddressComponentType(String administrative_area_level_3AddressComponentType) {
		this.administrative_area_level_3AddressComponentType = administrative_area_level_3AddressComponentType;
	}

	public String getAdministrative_area_level_4AddressComponentType() {
		if (this.administrative_area_level_4AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return administrative_area_level_4AddressComponentType;
	}

	public void setAdministrative_area_level_4AddressComponentType(String administrative_area_level_4AddressComponentType) {
		this.administrative_area_level_4AddressComponentType = administrative_area_level_4AddressComponentType;
	}

	public String getAdministrative_area_level_5AddressComponentType() {
		if (this.administrative_area_level_5AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return administrative_area_level_5AddressComponentType;
	}

	public void setAdministrative_area_level_5AddressComponentType(String administrative_area_level_5AddressComponentType) {
		this.administrative_area_level_5AddressComponentType = administrative_area_level_5AddressComponentType;
	}

	public String getIntersectionAddressComponentType() {
		if (this.intersectionAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return intersectionAddressComponentType;
	}

	public void setIntersectionAddressComponentType(String intersectionAddressComponentType) {
		this.intersectionAddressComponentType = intersectionAddressComponentType;
	}

	public String getColloquial_areaAddressComponentType() {
		if (this.colloquial_areaAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return colloquial_areaAddressComponentType;
	}

	public void setColloquial_areaAddressComponentType(String colloquial_areaAddressComponentType) {
		this.colloquial_areaAddressComponentType = colloquial_areaAddressComponentType;
	}

	public String getPostboxAddressComponentType() {
		if (this.postboxAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return postboxAddressComponentType;
	}

	public void setPostboxAddressComponentType(String postboxAddressComponentType) {
		this.postboxAddressComponentType = postboxAddressComponentType;
	}

	public String getSublocalityAddressComponentType() {
		if (this.sublocalityAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return sublocalityAddressComponentType;
	}

	public void setSublocalityAddressComponentType(String sublocalityAddressComponentType) {
		this.sublocalityAddressComponentType = sublocalityAddressComponentType;
	}

	public String getSublocality2AddressComponentType() {
		if (this.sublocality2AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return sublocality2AddressComponentType;
	}

	public void setSublocality2AddressComponentType(String sublocality2AddressComponentType) {
		this.sublocality2AddressComponentType = sublocality2AddressComponentType;
	}

	public String getSublocality3AddressComponentType() {
		if (this.sublocality3AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return sublocality3AddressComponentType;
	}

	public void setSublocality3AddressComponentType(String sublocality3AddressComponentType) {
		this.sublocality3AddressComponentType = sublocality3AddressComponentType;
	}

	public String getSublocality4AddressComponentType() {
		if (this.sublocality4AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return sublocality4AddressComponentType;
	}

	public void setSublocality4AddressComponentType(String sublocality4AddressComponentType) {
		this.sublocality4AddressComponentType = sublocality4AddressComponentType;
	}

	public String getSublocality5AddressComponentType() {
		if (this.sublocality5AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return sublocality5AddressComponentType;
	}

	public void setSublocality5AddressComponentType(String sublocality5AddressComponentType) {
		this.sublocality5AddressComponentType = sublocality5AddressComponentType;
	}

	public String getPlusCodeAddressComponentType() {
		if (this.plusCodeAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return plusCodeAddressComponentType;
	}

	public void setPlusCodeAddressComponentType(String plusCodeAddressComponentType) {
		this.plusCodeAddressComponentType = plusCodeAddressComponentType;
	}

	public String getPremiseAddressComponentType() {
		if (this.premiseAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.premiseAddressComponentType;
	}

	public void setPremiseAddressComponentType(String premiseAddressComponentType) {
		this.premiseAddressComponentType = premiseAddressComponentType;
	}

	public String getAdministrative_area_level_2AddressComponentType() {
		if (this.administrative_area_level_2AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.administrative_area_level_2AddressComponentType;
	}

	public void setAdministrative_area_level_2AddressComponentType(
			String administrative_area_level_2AddressComponentType) {
		this.administrative_area_level_2AddressComponentType = administrative_area_level_2AddressComponentType;
	}

	public String getPostal_townAddressComponentType() {
		if (this.postal_townAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.postal_townAddressComponentType;
	}

	public void setPostal_townAddressComponentType(String postal_townAddressComponentType) {
		this.postal_townAddressComponentType = postal_townAddressComponentType;
	}

	private Locale locale = null;

	public GoogleMapsOptions() {
	}

	public String getCityAddressComponentType() {
		if (this.cityAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.cityAddressComponentType;
	}

	public String getCountryAddressComponentType() {
		if (this.countryAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.SHORT_NAME;
		}
		return this.countryAddressComponentType;
	}

	public String getDefaultIcon() {
		if (this.defaultIcon == null) {
			return GoogleMapsConstant.Icons.GREEN;
		}
		return this.defaultIcon;
	}

	public String getDefaultZoom() {
		return this.defaultZoom;
	}

	public Integer getInfoWindowMaxWidth() {
		if (this.infoWindowMaxWidth == null) {
			return Integer.valueOf(1000);
		}
		return this.infoWindowMaxWidth;
	}

	public Double getInitialCenterLat() {
		if (this.initialCenterLat == null) {
			return this.defaultCenterLat;
		}
		return this.initialCenterLat;
	}

	public Double getInitialCenterLong() {
		if (this.initialCenterLong == null) {
			return this.defaultCenterLong;
		}
		return this.initialCenterLong;
	}

	public Locale getLocale() {
		return this.locale;
	}

	public String getMapType() {
		return this.mapType;
	}

	public String getMapTypeControlPosition() {
		return this.mapTypeControlPosition;
	}

	public String getMaxZoom() {
		return this.maxZoom;
	}

	public String getMinZoom() {
		return this.minZoom;
	}

	public String getNumberAddressComponentType() {
		if (this.numberAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.numberAddressComponentType;
	}

	public String getNeighborhoodAddressComponentType() {
		if (this.neighborhoodAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.neighborhoodAddressComponentType;
	}

	public void setNeighborhoodAddressComponentType(String neighborhoodAddressComponentType) {
		this.neighborhoodAddressComponentType = neighborhoodAddressComponentType;
	}

	public String getSublocality1AddressComponentType() {
		if (this.sublocality1AddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.sublocality1AddressComponentType;
	}

	public void setSublocality1AddressComponentType(String sublocality1AddressComponentType) {
		this.sublocality1AddressComponentType = sublocality1AddressComponentType;
	}

	public String getPanControlPosition() {
		return this.panControlPosition;
	}

	public String getRegionAddressComponentType() {
		if (this.regionAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.SHORT_NAME;
		}
		return this.regionAddressComponentType;
	}

	public String getStreetAddressComponentType() {
		if (this.streetAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.streetAddressComponentType;
	}

	public String getStreetViewControlPosition() {
		return this.streetViewControlPosition;
	}

	public String getZipCodeAddressComponentType() {
		if (this.zipCodeAddressComponentType == null) {
			return GoogleMapsConstant.AddressComponents.Type.LONG_NAME;
		}
		return this.zipCodeAddressComponentType;
	}

	public String getZoomControlPosition() {
		return this.zoomControlPosition;
	}

	public String getZoomControlStyle() {
		return this.zoomControlStyle;
	}

	public String getZoomSingleMarker() {
		if (this.zoomSingleMarker == null) {
			return GoogleMapsConstant.Zoom.DEFAULT_SINGLE_MARKER;
		}
		return this.zoomSingleMarker;
	}

	public boolean hasMapTypeControl() {
		return this.mapTypeControl;
	}

	public boolean hasPanControl() {
		return this.panControl;
	}

	public boolean hasStreetViewControl() {
		return this.streetViewControl;
	}

	public boolean hasZoomControl() {
		return this.zoomControl;
	}

	public boolean isDisableControls() {
		return this.disableControls;
	}

	public boolean isDraggable() {
		return this.draggable;
	}

	public boolean isDraggableMarker() {
		return this.draggableMarker;
	}

	public boolean isScrollwheel() {
		return this.scrollwheel;
	}

	public void setCityAddressComponentType(final String cityAddressComponentType) {
		this.cityAddressComponentType = cityAddressComponentType;
	}

	public void setCountryAddressComponentType(final String countryAddressComponentType) {
		this.countryAddressComponentType = countryAddressComponentType;
	}

	public void setDefaultIcon(final String defaultIcon) {
		this.defaultIcon = defaultIcon;
	}

	public void setDefaultZoom(final String defaultZoom) {
		this.defaultZoom = defaultZoom;
	}

	public void setDisableControls(final boolean disableControls) {
		this.disableControls = disableControls;
	}

	public void setDraggable(final boolean draggable) {
		this.draggable = draggable;
	}

	public void setDraggableMarker(final boolean draggableMarker) {
		this.draggableMarker = draggableMarker;
	}

	public void setInfoWindowMaxWidth(final Integer infoWindowMaxWidth) {
		this.infoWindowMaxWidth = infoWindowMaxWidth;
	}

	public void setInitialCenterLat(final Double initialCenterLat) {
		this.initialCenterLat = initialCenterLat;
	}

	public void setInitialCenterLong(final Double initialCenterLong) {
		this.initialCenterLong = initialCenterLong;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	public void setMapType(final String mapType) {
		this.mapType = mapType;
	}

	public void setMapTypeControl(final boolean mapTypeControl) {
		this.mapTypeControl = mapTypeControl;
	}

	public void setMapTypeControlPosition(final String mapTypeControlPosition) {
		this.mapTypeControlPosition = mapTypeControlPosition;
	}

	public void setMaxZoom(final String maxZoom) {
		this.maxZoom = maxZoom;
	}

	public void setMinZoom(final String minZoom) {
		this.minZoom = minZoom;
	}

	public void setNumberAddressComponentType(final String numberAddressComponentType) {
		this.numberAddressComponentType = numberAddressComponentType;
	}

	public void setPanControl(final boolean panControl) {
		this.panControl = panControl;
	}

	public void setPanControlPosition(final String panControlPosition) {
		this.panControlPosition = panControlPosition;
	}

	public void setRegionAddressComponentType(final String regionAddressComponentType) {
		this.regionAddressComponentType = regionAddressComponentType;
	}

	public void setScrollwheel(final boolean scrollwheel) {
		this.scrollwheel = scrollwheel;
	}

	public void setStreetAddressComponentType(final String streetAddressComponentType) {
		this.streetAddressComponentType = streetAddressComponentType;
	}

	public void setStreetViewControl(final boolean streetViewControl) {
		this.streetViewControl = streetViewControl;
	}

	public void setStreetViewControlPosition(final String streetViewControlPosition) {
		this.streetViewControlPosition = streetViewControlPosition;
	}

	public void setZipCodeAddressComponentType(final String zipCodeAddressComponentType) {
		this.zipCodeAddressComponentType = zipCodeAddressComponentType;
	}

	public void setZoomControl(final boolean zoomControl) {
		this.zoomControl = zoomControl;
	}

	public void setZoomControlPosition(final String zoomControlPosition) {
		this.zoomControlPosition = zoomControlPosition;
	}

	public void setZoomControlStyle(final String zoomControlStyle) {
		this.zoomControlStyle = zoomControlStyle;
	}

	public void setZoomSingleMarker(final String zoomSingleMarker) {
		this.zoomSingleMarker = zoomSingleMarker;
	}

	private String getCenter() {
		Double centerLat = this.getInitialCenterLat();
		Double centerLong = this.getInitialCenterLong();
		String center = "new google.maps.LatLng(" + String.valueOf(centerLat) + ", " + String.valueOf(centerLong) + ")";

		return center;
	}

	protected String buildInfoWindowJsObjectContent() {
		String jsObjectContent = "";
		jsObjectContent += JsObjectName.InfoWindow.MaxWidth.JS_NAME + ": "
				+ String.valueOf(this.getInfoWindowMaxWidth().intValue());
		return jsObjectContent;
	}

	protected String buildMapsJsObjectContent() {
		String jsObjectContent = "";

		jsObjectContent += JsObjectName.Center.JS_NAME + ": " + this.getCenter() + ", ";
		jsObjectContent += JsObjectName.DisableDefaultUI.JS_NAME + ": " + this.isDisableControls() + ", ";
		jsObjectContent += JsObjectName.Draggable.JS_NAME + ": " + this.isDraggable() + ", ";
		jsObjectContent += JsObjectName.MapType.JS_NAME + ": " + this.getMapType() + ", ";
		jsObjectContent += JsObjectName.MapTypeControl.JS_NAME + ": " + this.hasMapTypeControl() + ", ";
		jsObjectContent += JsObjectName.MapTypeControlOptions.JS_NAME + ":{";
		jsObjectContent += JsObjectName.MapTypeControlOptions.Position.JS_NAME + ": "
				+ this.getMapTypeControlPosition();
		jsObjectContent += "}, ";
		jsObjectContent += JsObjectName.Zoom.JS_NAME + ": " + this.getDefaultZoom() + ", ";
		jsObjectContent += JsObjectName.MaxZoom.JS_NAME + ": " + this.getMaxZoom() + ", ";
		jsObjectContent += JsObjectName.MinZoom.JS_NAME + ": " + this.getMinZoom() + ", ";
		jsObjectContent += JsObjectName.ZoomControl.JS_NAME + ": " + this.hasZoomControl() + ", ";
		jsObjectContent += JsObjectName.ZoomControlOptions.JS_NAME + ": {";
		jsObjectContent += JsObjectName.ZoomControlOptions.Position.JS_NAME + ": " + this.getZoomControlPosition()
				+ ", ";
		jsObjectContent += JsObjectName.ZoomControlOptions.Style.JS_NAME + ": " + this.getZoomControlStyle();
		jsObjectContent += "}, ";
		jsObjectContent += JsObjectName.OverviewMapControl.JS_NAME + ": " + this.hasOverviewMapControl() + ", ";
		jsObjectContent += JsObjectName.PanControl.JS_NAME + ": " + this.hasPanControl() + ", ";
		jsObjectContent += JsObjectName.PanControlOptions.JS_NAME + ": {";
		jsObjectContent += JsObjectName.PanControlOptions.Position.JS_NAME + ": " + this.getPanControlPosition();
		jsObjectContent += "}, ";
		jsObjectContent += JsObjectName.RotateControl.JS_NAME + ": " + this.hasRotateControl() + ", ";
		jsObjectContent += JsObjectName.RotateControlOptions.JS_NAME + ": {";
		jsObjectContent += JsObjectName.RotateControlOptions.Position.JS_NAME + ": " + this.getRotateControlPosition();
		jsObjectContent += "}, ";
		jsObjectContent += JsObjectName.ScaleControl.JS_NAME + ": " + this.hasScaleControl() + ", ";
		jsObjectContent += JsObjectName.Scrollwheel.JS_NAME + ": " + this.isScrollwheel() + ", ";
		jsObjectContent += JsObjectName.StreetViewControl.JS_NAME + ": " + this.hasStreetViewControl() + ", ";
		jsObjectContent += JsObjectName.StreetViewControlOptions.JS_NAME + ": {";
		jsObjectContent += JsObjectName.StreetViewControlOptions.Position.JS_NAME + ": "
				+ this.getStreetViewControlPosition();
		jsObjectContent += "}";

		return jsObjectContent;
	}

	protected String getInfoWindowJsObject() {
		String jsObject = "{";
		jsObject += this.buildInfoWindowJsObjectContent();
		jsObject += "}";
		return jsObject;
	}

	protected String getMapsJsObject() {
		String jsObject = "{";
		jsObject += this.buildMapsJsObjectContent();
		jsObject += "}";
		return jsObject;
	}

	protected String getRotateControlPosition() {
		return this.rotateControlPosition;
	}

	protected boolean hasOverviewMapControl() {
		return this.overviewMapControl;
	}

	protected boolean hasRotateControl() {
		return this.rotateControl;
	}

	protected boolean hasScaleControl() {
		return this.scaleControl;
	}

	protected void setOverviewMapControl(final boolean overviewMapControl) {
		this.overviewMapControl = overviewMapControl;
	}

	protected void setRotateControl(final boolean rotateControl) {
		this.rotateControl = rotateControl;
	}

	protected void setRotateControlPosition(final String rotateControlPosition) {
		this.rotateControlPosition = rotateControlPosition;
	}

	protected void setScaleControl(final boolean scaleControl) {
		this.scaleControl = scaleControl;
	}
	protected static class JsObjectName {
		protected static class Center {
			protected static final String JS_NAME = "center";
		}

		protected static class DisableDefaultUI {
			protected static final String JS_NAME = "disableDefaultUI";
		}

		protected static class Draggable {
			protected static final String JS_NAME = "draggable";
		}

		protected static class InfoWindow {
			protected static class MaxWidth {
				protected static final String JS_NAME = "maxWidth";
			}
		}

		protected static class MapType {
			protected static final String JS_NAME = "mapTypeId";
		}

		protected static class MapTypeControl {
			protected static final String JS_NAME = "mapTypeControl";
		}

		protected static class MapTypeControlOptions {
			protected static class Position {
				protected static final String JS_NAME = "position";
			}

			protected static final String JS_NAME = "mapTypeControlOptions";
		}

		protected static class MaxZoom {
			protected static final String JS_NAME = "maxZoom";
		}

		protected static class MinZoom {
			protected static final String JS_NAME = "minZoom";
		}

		protected static class OverviewMapControl {
			protected static final String JS_NAME = "overviewMapControl";
		}

		protected static class PanControl {
			protected static final String JS_NAME = "panControl";
		}

		protected static class PanControlOptions {
			protected static class Position {
				protected static final String JS_NAME = "position";
			}

			protected static final String JS_NAME = "panControlOptions";
		}

		protected static class RotateControl {
			protected static final String JS_NAME = "rotateControl";
		}

		protected static class RotateControlOptions {
			protected static class Position {
				protected static final String JS_NAME = "position";
			}

			protected static final String JS_NAME = "rotateControlOptions";
		}

		protected static class ScaleControl {
			protected static final String JS_NAME = "scaleControl";
		}

		protected static class Scrollwheel {
			protected static final String JS_NAME = "scrollwheel";
		}

		protected static class StreetViewControl {
			protected static final String JS_NAME = "streetViewControl";
		}

		protected static class StreetViewControlOptions {
			protected static class Position {
				protected static final String JS_NAME = "position";
			}

			protected static final String JS_NAME = "streetViewControlOptions";
		}

		protected static class Zoom {
			protected static final String JS_NAME = "zoom";
		}

		protected static class ZoomControl {
			protected static final String JS_NAME = "zoomControl";
		}

		protected static class ZoomControlOptions {
			protected static class Position {
				protected static final String JS_NAME = "position";
			}

			protected static class Style {
				protected static final String JS_NAME = "style";
			}

			protected static final String JS_NAME = "zoomControlOptions";
		}
	}

}
