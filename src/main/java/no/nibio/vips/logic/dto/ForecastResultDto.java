/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.dto;

import java.time.Instant;

/**
 * Dto for transferring forecast result data to the client. 
 * 
 * @copyright 2026 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ForecastResultDto {
    private Long forecastConfigurationId;
    private Instant validTimeStart, validTimeEnd;
    private Integer warningStatus;
    private String allValues;
    
    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }
    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }
    public Instant getValidTimeStart() {
        return validTimeStart;
    }
    public void setValidTimeStart(Instant validTimeStart) {
        this.validTimeStart = validTimeStart;
    }
    public Instant getValidTimeEnd() {
        return validTimeEnd;
    }
    public void setValidTimeEnd(Instant validTimeEnd) {
        this.validTimeEnd = validTimeEnd;
    }
    public Integer getWarningStatus() {
        return warningStatus;
    }
    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }
    public String getAllValues() {
        return allValues;
    }
    public void setAllValues(String allValues) {
        this.allValues = allValues;
    }
}
