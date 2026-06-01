package no.nibio.vips.logic.dto;

import java.time.Instant;

public class ForecastConfigurationDto {

    private Long forecastConfigurationId;
    private Integer vipsLogicUserId;
    private Boolean isPrivate;
    private PointOfInterestDto locationPointOfInterestId, weatherStationPointOfInterestId;
    private String modelId, timeZone;
    private Instant dateStart, dateEnd;

    
    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }
    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }
    public Integer getVipsLogicUserId() {
        return vipsLogicUserId;
    }
    public void setVipsLogicUserId(Integer vipsLogicUserId) {
        this.vipsLogicUserId = vipsLogicUserId;
    }
    public PointOfInterestDto getLocationPointOfInterestId() {
        return locationPointOfInterestId;
    }
    public void setLocationPointOfInterestId(PointOfInterestDto locationPointOfInterestId) {
        this.locationPointOfInterestId = locationPointOfInterestId;
    }
    public PointOfInterestDto getWeatherStationPointOfInterestId() {
        return weatherStationPointOfInterestId;
    }
    public void setWeatherStationPointOfInterestId(PointOfInterestDto weatherStationPointOfInterestId) {
        this.weatherStationPointOfInterestId = weatherStationPointOfInterestId;
    }
    public String getModelId() {
        return modelId;
    }
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }
    public String getTimeZone() {
        return timeZone;
    }
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public Instant getDateStart() {
        return dateStart;
    }
    public void setDateStart(Instant dateStart) {
        this.dateStart = dateStart;
    }
    public Instant getDateEnd() {
        return dateEnd;
    }
    public void setDateEnd(Instant dateEnd) {
        this.dateEnd = dateEnd;
    }
    public Boolean getIsPrivate() {
        return isPrivate;
    }
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
