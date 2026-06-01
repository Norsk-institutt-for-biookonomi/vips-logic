package no.nibio.vips.logic.dto;

public class PointOfInterestExternalResourceDto {

    private Long externalResourceId;
    private String externalResourceUri;

    public Long getExternalResourceId() {
        return externalResourceId;
    }
    public void setExternalResourceId(Long externalResourceId) {
        this.externalResourceId = externalResourceId;
    }
    
    public String getExternalResourceUri() {
        return externalResourceUri;
    }
    public void setExternalResourceUri(String externalResourceUri) {
        this.externalResourceUri = externalResourceUri;
    }

}
