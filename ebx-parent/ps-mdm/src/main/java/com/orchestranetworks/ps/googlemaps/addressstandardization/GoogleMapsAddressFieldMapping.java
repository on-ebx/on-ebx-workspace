package com.orchestranetworks.ps.googlemaps.addressstandardization;

import com.orchestranetworks.schema.Path;

public final class GoogleMapsAddressFieldMapping {
	private String numberId;
	private String streetId;
	private String premiseId;
	private String intersectionId;
	private String neighborhoodId;
	private String subLocalityId;
	private String localityId;
	private String subAdministrativeAreaId;
	private String regionId;
	private String postalCodeId;
	private String countryId;
	private String latitudeId;
	private String longitudeId;
	private String formattedAddressId;
	private Path numberPath;
	private Path streetPath;
	private Path premisePath;
	private Path intersectionPath;
	private Path neighborhoodPath;
	private Path subLocalityPath;
	private Path localityPath;
	private Path subAdministrativeAreaPath;
	private Path regionPath;
	private Path postalCodePath;
	private Path countryPath;
	private Path latitudePath;
	private Path longitudePath;
	private Path formattedAddressPath;
	private Path addressStatusPath;
	
	public Path getNumberPath() {
		return numberPath;
	}

	public void setNumberPath(Path numberPath) {
		this.numberPath = numberPath;
	}

	public Path getStreetPath() {
		return streetPath;
	}

	public void setStreetPath(Path streetPath) {
		this.streetPath = streetPath;
	}

	public Path getPremisePath() {
		return premisePath;
	}

	public void setPremisePath(Path premisePath) {
		this.premisePath = premisePath;
	}

	public Path getIntersectionPath() {
		return intersectionPath;
	}

	public void setIntersectionPath(Path intersectionPath) {
		this.intersectionPath = intersectionPath;
	}

	public Path getNeighborhoodPath() {
		return neighborhoodPath;
	}

	public void setNeighborhoodPath(Path neighborhoodPath) {
		this.neighborhoodPath = neighborhoodPath;
	}

	public Path getSubLocalityPath() {
		return subLocalityPath;
	}

	public void setSubLocalityPath(Path subLocalityPath) {
		this.subLocalityPath = subLocalityPath;
	}

	public Path getLocalityPath() {
		return localityPath;
	}

	public void setLocalityPath(Path localityPath) {
		this.localityPath = localityPath;
	}

	public Path getSubAdministrativeAreaPath() {
		return subAdministrativeAreaPath;
	}

	public void setSubAdministrativeAreaPath(Path subAdministrativeAreaPath) {
		this.subAdministrativeAreaPath = subAdministrativeAreaPath;
	}

	public Path getRegionPath() {
		return regionPath;
	}

	public void setRegionPath(Path regionPath) {
		this.regionPath = regionPath;
	}

	public Path getPostalCodePath() {
		return postalCodePath;
	}

	public void setPostalCodePath(Path postalCodePath) {
		this.postalCodePath = postalCodePath;
	}

	public Path getCountryPath() {
		return countryPath;
	}

	public void setCountryPath(Path countryPath) {
		this.countryPath = countryPath;
	}

	public Path getLatitudePath() {
		return latitudePath;
	}

	public void setLatitudePath(Path latitudePath) {
		this.latitudePath = latitudePath;
	}

	public Path getLongitudePath() {
		return longitudePath;
	}

	public void setLongitudePath(Path longitudePath) {
		this.longitudePath = longitudePath;
	}

	public Path getFormattedAddressPath() {
		return formattedAddressPath;
	}

	public void setFormattedAddressPath(Path formattedAddressPath) {
		this.formattedAddressPath = formattedAddressPath;
	}

	public GoogleMapsAddressFieldMapping() {
	}

	public GoogleMapsAddressFieldMapping(final Path numberPath, final Path streetPath, final Path addressLine2Path,
			final Path zipcodePath, final Path cityPath, final Path regionPath, final Path countryPath,
			final Path latitudePath, final Path longitudePath, final Path formattedAddressPath,
			final Path addressStatusPath) {

	}

	public Path getAddressStatusPath() {
		return this.addressStatusPath;
	}

	public void setAddressStatusPath(final Path addressStatusPath) {
		this.addressStatusPath = addressStatusPath;
	}

	public String getNumberId() {
		return numberId;
	}

	public void setNumberId(String numberId) {
		this.numberId = numberId;
	}

	public String getStreetId() {
		return streetId;
	}

	public void setStreetId(String streetId) {
		this.streetId = streetId;
	}

	public String getPremiseId() {
		return premiseId;
	}

	public void setPremiseId(String premiseId) {
		this.premiseId = premiseId;
	}

	public String getIntersectionId() {
		return intersectionId;
	}

	public void setIntersectionId(String intersectionId) {
		this.intersectionId = intersectionId;
	}

	public String getNeighborhoodId() {
		return neighborhoodId;
	}

	public void setNeighborhoodId(String neighborhoodId) {
		this.neighborhoodId = neighborhoodId;
	}

	public String getSubLocalityId() {
		return subLocalityId;
	}

	public void setSubLocalityId(String subLocalityId) {
		this.subLocalityId = subLocalityId;
	}

	public String getLocalityId() {
		return localityId;
	}

	public void setLocalityId(String localityId) {
		this.localityId = localityId;
	}

	public String getSubAdministrativeAreaId() {
		return subAdministrativeAreaId;
	}

	public void setSubAdministrativeAreaId(String subAdministrativeAreaId) {
		this.subAdministrativeAreaId = subAdministrativeAreaId;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getPostalCodeId() {
		return postalCodeId;
	}

	public void setPostalCodeId(String postalCodeId) {
		this.postalCodeId = postalCodeId;
	}

	public String getCountryId() {
		return countryId;
	}

	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}

	public String getLatitudeId() {
		return latitudeId;
	}

	public void setLatitudeId(String latitudeId) {
		this.latitudeId = latitudeId;
	}

	public String getLongitudeId() {
		return longitudeId;
	}

	public void setLongitudeId(String longitudeId) {
		this.longitudeId = longitudeId;
	}

	public String getFormattedAddressId() {
		return formattedAddressId;
	}

	public void setFormattedAddressId(String formattedAddressId) {
		this.formattedAddressId = formattedAddressId;
	}
}
