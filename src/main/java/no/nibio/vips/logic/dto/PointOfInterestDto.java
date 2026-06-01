package no.nibio.vips.logic.dto;

import java.util.List;

public class PointOfInterestDto {
    private Long pointOfInterestId;
    private String name;
    private Double latitude, longitude;
    private List<PointOfInterestExternalResourceDto> externalResources;

    public Long getPointOfInterestId() {
        return pointOfInterestId;
    }
    public void setPointOfInterestId(Long pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public List<PointOfInterestExternalResourceDto> getExternalResources() {
        return externalResources;
    }
    public void setExternalResources(List<PointOfInterestExternalResourceDto> externalResources) {
        this.externalResources = externalResources;
    }
}
