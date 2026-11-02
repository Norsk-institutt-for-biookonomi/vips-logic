package no.nibio.vips.logic.messaging;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(name = "user_device", schema = "messaging")
@NamedQueries({
    @NamedQuery(name = "UserDevice.findByFcmToken", query = "SELECT d FROM UserDevice d WHERE d.fcmToken = :fcmToken"),
    @NamedQuery(name = "UserDevice.deleteByUserId", query = "DELETE FROM UserDevice d WHERE d.userId = :userId"),
    @NamedQuery(name = "UserDevice.deleteByFcmToken", query = "DELETE FROM UserDevice d WHERE d.fcmToken = :fcmToken")
})
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_device_id")
    private Integer userDeviceId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @Column(name = "platform", nullable = false)
    private String platform;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    public Integer getUserDeviceId() {
        return userDeviceId;
    }

    public void setUserDeviceId(Integer userDeviceId) {
        this.userDeviceId = userDeviceId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
