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

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Dto for transferring forecast result data to the client. 
 * 
 * @copyright 2026 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ForecastResultDto implements Comparable<ForecastResultDto>{
    private Long forecastConfigurationId, forecastResultId;
    private Instant validTimeStart, validTimeEnd;
    private Long warningStatus;
    private String keys;
    private String allValues;

    public ForecastResultDto(Long forecastConfigurationId, Long forecastResultId, Instant validTimeStart, Instant validTimeEnd, Long warningStatus, String allValues) {
        this.forecastConfigurationId = forecastConfigurationId;
        this.forecastResultId = forecastResultId;
        this.validTimeStart = validTimeStart;
        this.validTimeEnd = validTimeEnd;
        this.warningStatus = warningStatus;
        this.allValues = allValues;
    }
    
    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }
    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }
    public Long getForecastResultId() {
        return forecastResultId;
    }
    public void setForecastResultId(Long forecastResultId) {
        this.forecastResultId = forecastResultId;
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
    public Long getWarningStatus() {
        return warningStatus;
    }
    public void setWarningStatus(Long warningStatus) {
        this.warningStatus = warningStatus;
    }

    @JsonProperty("allValues")
    public Map<String,String> getAllValuesAsMap()
    {
        try {
            return new ObjectMapper().readValue(this.allValues, new TypeReference<HashMap<String,String>>(){});
        } catch (IOException ex) {

            return new HashMap<>();
        }
 
    }

    @JsonIgnore
    public String getAllValues() {
        return allValues;
    }
    public void setAllValues(String allValues) {
        this.allValues = allValues;
    }

    public Set<String> getKeys() {
        return this.getAllValuesAsMap().keySet();
    }
    
    @Override
    public int compareTo(ForecastResultDto other) {
        return this.validTimeStart.compareTo(other.validTimeStart);
    }
}
