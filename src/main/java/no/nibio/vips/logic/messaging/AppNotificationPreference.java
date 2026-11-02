package no.nibio.vips.logic.messaging;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "app_notification_preference", schema = "messaging")
public class AppNotificationPreference {

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "forecast_status_enabled", nullable = false)
    private boolean forecastStatusEnabled = true;

    @Type(ListArrayType.class)
    @Column(name = "forecast_configuration_ids", columnDefinition = "bigint[]")
    private List<Long> forecastConfigurationIds = new ArrayList<>();

    @Column(name = "message_enabled", nullable = false)
    private boolean messageEnabled = false;

    @Type(ListArrayType.class)
    @Column(name = "message_crop_category_ids", columnDefinition = "integer[]")
    private List<Integer> messageCropCategoryIds = new ArrayList<>();

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isForecastStatusEnabled() {
        return forecastStatusEnabled;
    }

    public void setForecastStatusEnabled(boolean forecastStatusEnabled) {
        this.forecastStatusEnabled = forecastStatusEnabled;
    }

    public List<Long> getForecastConfigurationIds() {
        return forecastConfigurationIds;
    }

    public void setForecastConfigurationIds(List<Long> forecastConfigurationIds) {
        this.forecastConfigurationIds = forecastConfigurationIds;
    }

    public boolean isMessageEnabled() {
        return messageEnabled;
    }

    public void setMessageEnabled(boolean messageEnabled) {
        this.messageEnabled = messageEnabled;
    }

    public List<Integer> getMessageCropCategoryIds() {
        return messageCropCategoryIds;
    }

    public void setMessageCropCategoryIds(List<Integer> messageCropCategoryIds) {
        this.messageCropCategoryIds = messageCropCategoryIds;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
