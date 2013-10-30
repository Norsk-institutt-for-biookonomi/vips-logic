package no.bioforsk.vips.logic.scheduling.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "forecast_configuration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastConfiguration.findAll", query = "SELECT f FROM ForecastConfiguration f"),
    @NamedQuery(name = "ForecastConfiguration.findByForecastConfigurationId", query = "SELECT f FROM ForecastConfiguration f WHERE f.forecastConfigurationId = :forecastConfigurationId"),
    @NamedQuery(name = "ForecastConfiguration.findByModelId", query = "SELECT f FROM ForecastConfiguration f WHERE f.modelId = :modelId"),
    @NamedQuery(name = "ForecastConfiguration.findByDateStart", query = "SELECT f FROM ForecastConfiguration f WHERE f.dateStart = :dateStart"),
    @NamedQuery(name = "ForecastConfiguration.findByDateEnd", query = "SELECT f FROM ForecastConfiguration f WHERE f.dateEnd = :dateEnd"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsCoreUserId", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsCoreUserId = :vipsCoreUserId")})
public class ForecastConfiguration implements Serializable {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "forecastConfiguration")
    private Set<ForecastModelConfiguration> forecastModelConfigurationSet;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "forecast_configuration_id")
    private Integer forecastConfigurationId;
    @Size(max = 10)
    @Column(name = "model_id")
    private String modelId;
    @Column(name = "date_start")
    @Temporal(TemporalType.DATE)
    private Date dateStart;
    @Column(name = "date_end")
    @Temporal(TemporalType.DATE)
    private Date dateEnd;
    @Column(name = "vips_core_user_id")
    private Integer vipsCoreUserId;
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id")
    @ManyToOne
    private PointOfInterest pointOfInterestId;

    public ForecastConfiguration() {
    }

    public ForecastConfiguration(Integer forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public Integer getForecastConfigurationId() {
        return forecastConfigurationId;
    }

    public void setForecastConfigurationId(Integer forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public Integer getVipsCoreUserId() {
        return vipsCoreUserId;
    }

    public void setVipsCoreUserId(Integer vipsCoreUserId) {
        this.vipsCoreUserId = vipsCoreUserId;
    }

    public PointOfInterest getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(PointOfInterest pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastConfigurationId != null ? forecastConfigurationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastConfiguration)) {
            return false;
        }
        ForecastConfiguration other = (ForecastConfiguration) object;
        if ((this.forecastConfigurationId == null && other.forecastConfigurationId != null) || (this.forecastConfigurationId != null && !this.forecastConfigurationId.equals(other.forecastConfigurationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.scheduling.model.ForecastConfiguration[ forecastConfigurationId=" + forecastConfigurationId + " ]";
    }

    @XmlTransient
    @JsonIgnore
    public Set<ForecastModelConfiguration> getForecastModelConfigurationSet() {
        return forecastModelConfigurationSet;
    }

    public void setForecastModelConfigurationSet(Set<ForecastModelConfiguration> forecastModelConfigurationSet) {
        this.forecastModelConfigurationSet = forecastModelConfigurationSet;
    }
    
    /**
     * Util method to get a specific parameter value
     * @param modelConfigParameter
     * @return 
     */
    public String getForecastModelConfigurationValue(String modelConfigParameter)
    {
        for(ForecastModelConfiguration conf:this.forecastModelConfigurationSet)
        {
            if(conf.getForecastModelConfigurationPK().getModelConfigParameter().equals(modelConfigParameter))
            {
                return conf.getParameterValue();
            }
        }
        return null;
    }

}
