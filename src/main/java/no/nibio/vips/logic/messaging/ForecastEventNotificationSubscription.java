/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.messaging;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import no.nibio.vips.logic.util.IntegerArrayUserType;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "forecast_event_notification_subscription", schema = "messaging")
public class ForecastEventNotificationSubscription {

    @Id
    @Basic(optional = false)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "universal_message_format_id")
    private Integer universalMessageFormatId;
    
    @Convert(converter = IntegerArrayUserType.class)
    @Column(name = "weather_station_ids")
    private Integer[] weatherStationIds;
    
    @Convert(converter = IntegerArrayUserType.class)
    @Column(name = "crop_category_ids")
    private Integer[] cropCategoryIds;

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * @return the universalMessageFormatId
     */
    public Integer getUniversalMessageFormatId() {
        return universalMessageFormatId;
    }

    /**
     * @param universalMessageFormatId the universalMessageFormatId to set
     */
    public void setUniversalMessageFormatId(Integer universalMessageFormatId) {
        this.universalMessageFormatId = universalMessageFormatId;
    }

    /**
     * @return the weatherStationIds
     */
    public Integer[] getWeatherStationIds() {
        return weatherStationIds;
    }

    /**
     * @param weatherStationIds the weatherStationIds to set
     */
    public void setWeatherStationIds(Integer[] weatherStationIds) {
        this.weatherStationIds = weatherStationIds;
    }
    
    /**
     * Convenience method
     * @param weatherStationIds 
     */
    public void setWeatherStationIds(List<Integer> weatherStationIds)
    {
        this.weatherStationIds = weatherStationIds.toArray(new Integer[weatherStationIds.size()]);
    }

    /**
     * @return the cropCategoryIds
     */
    public Integer[] getCropCategoryIds() {
        return cropCategoryIds;
    }

    /**
     * @param cropCategoryIds the cropCategoryIds to set
     */
    public void setCropCategoryIds(Integer[] cropCategoryIds) {
        this.cropCategoryIds = cropCategoryIds;
    }
    
    public void setCropCategoryIds(List<Integer> cropCategoryIds)
    {
        this.cropCategoryIds = cropCategoryIds.toArray(new Integer[cropCategoryIds.size()]);
    }
}
