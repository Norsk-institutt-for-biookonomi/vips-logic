package no.nibio.vips.logic.entity.rest;

import java.util.ArrayList;
import java.util.List;

public class AppNotificationPrefsDto {

    private Boolean forecastStatusEnabled = Boolean.TRUE;
    private List<Long> forecastConfigurationIds = new ArrayList<>();
    private Boolean messageEnabled = Boolean.FALSE;
    private List<Integer> messageCropCategoryIds = new ArrayList<>();

    public boolean isForecastStatusEnabled() {
        return forecastStatusEnabled == null || forecastStatusEnabled;
    }

    public void setForecastStatusEnabled(Boolean forecastStatusEnabled) {
        this.forecastStatusEnabled = forecastStatusEnabled;
    }

    public List<Long> getForecastConfigurationIds() {
        return forecastConfigurationIds == null ? new ArrayList<>() : forecastConfigurationIds;
    }

    public void setForecastConfigurationIds(List<Long> forecastConfigurationIds) {
        this.forecastConfigurationIds = forecastConfigurationIds;
    }

    public boolean isMessageEnabled() {
        return messageEnabled != null && messageEnabled;
    }

    public void setMessageEnabled(Boolean messageEnabled) {
        this.messageEnabled = messageEnabled;
    }

    public List<Integer> getMessageCropCategoryIds() {
        return messageCropCategoryIds == null ? new ArrayList<>() : messageCropCategoryIds;
    }

    public void setMessageCropCategoryIds(List<Integer> messageCropCategoryIds) {
        this.messageCropCategoryIds = messageCropCategoryIds;
    }
}
