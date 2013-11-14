package no.bioforsk.vips.logic.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import no.bioforsk.vips.entity.Result;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "forecast_result")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastResult.findAll", query = "SELECT f FROM ForecastResult f ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByForecastResultId", query = "SELECT f FROM ForecastResult f WHERE f.forecastResultId = :forecastResultId ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByResultValidTime", query = "SELECT f FROM ForecastResult f WHERE f.resultValidTime = :resultValidTime ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByWarningStatus", query = "SELECT f FROM ForecastResult f WHERE f.warningStatus = :warningStatus ORDER BY f.resultValidTime ASC")})
public class ForecastResult implements Serializable, Comparable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "forecast_result_id")
    private Integer forecastResultId;
    @Column(name = "result_valid_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resultValidTime;
    @Column(name = "warning_status")
    private Integer warningStatus;
    @Column(name = "all_values")
    private String allValues;

    public ForecastResult() {
    }
    
    public ForecastResult(Result result){
        try {
            this.allValues = new ObjectMapper().writeValueAsString(result.getAllValues());
            this.resultValidTime = result.getResultValidTime();
            this.warningStatus = result.getWarningStatus();
        } catch (IOException ex) {
            Logger.getLogger(ForecastResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ForecastResult(Integer forecastResultId) {
        this.forecastResultId = forecastResultId;
    }

    public Integer getForecastResultId() {
        return forecastResultId;
    }

    public void setForecastResultId(Integer forecastResultId) {
        this.forecastResultId = forecastResultId;
    }

    public Date getResultValidTime() {
        return resultValidTime;
    }

    public void setResultValidTime(Date resultValidTime) {
        this.resultValidTime = resultValidTime;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    @JsonIgnore
    public String getAllValues() {
        return allValues;
    }

    public void setAllValues(String allValues) {
        this.allValues = allValues;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastResultId != null ? forecastResultId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastResult)) {
            return false;
        }
        ForecastResult other = (ForecastResult) object;
        if ((this.forecastResultId == null && other.forecastResultId != null) || (this.forecastResultId != null && !this.forecastResultId.equals(other.forecastResultId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.ForecastResult[ forecastResultId=" + forecastResultId + " ]";
    }

    /**
     * Converting from JSON format
     * @return 
     */
    @Transient
    @JsonProperty("allValues")
    public Map<String,String> getAllValuesAsMap()
    {
        try {
            return new ObjectMapper().readValue(this.allValues, new TypeReference<HashMap<String,String>>(){});
        } catch (IOException ex) {
            Logger.getLogger(ForecastResult.class.getName()).log(Level.SEVERE, null, ex);
            return new HashMap<>();
        }
 
    }
    
    /**
     * Converting to JSON string
     * @param values 
     */
    @Transient 
    public void setAllValues(Map<String,String> values)
    {
        try {
            this.allValues = new ObjectMapper().writeValueAsString(values);
            System.out.println("this.allvalues=" + this.allValues);
        } catch (IOException ex) {
            Logger.getLogger(ForecastResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int compareTo(Object t) {
        return this.compareTo((ForecastResult)t);
    }
    
    public int compareTo(ForecastResult other)
    {
        return this.getResultValidTime().compareTo(other.getResultValidTime());
    }
}
