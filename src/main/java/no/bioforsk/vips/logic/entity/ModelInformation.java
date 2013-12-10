package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "model_information")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ModelInformation.findAll", query = "SELECT m FROM ModelInformation m"),
    @NamedQuery(name = "ModelInformation.findByModelId", query = "SELECT m FROM ModelInformation m WHERE m.modelId = :modelId"),
    @NamedQuery(name = "ModelInformation.findByDateFirstRegistered", query = "SELECT m FROM ModelInformation m WHERE m.dateFirstRegistered = :dateFirstRegistered"),
    @NamedQuery(name = "ModelInformation.findByDateLastRegistered", query = "SELECT m FROM ModelInformation m WHERE m.dateLastRegistered = :dateLastRegistered"),
    @NamedQuery(name = "ModelInformation.findByDefaultName", query = "SELECT m FROM ModelInformation m WHERE m.defaultName = :defaultName"),
    @NamedQuery(name = "ModelInformation.findByCopyrightHolder", query = "SELECT m FROM ModelInformation m WHERE m.copyrightHolder = :copyrightHolder"),
    @NamedQuery(name = "ModelInformation.findByLicense", query = "SELECT m FROM ModelInformation m WHERE m.license = :license"),
    @NamedQuery(name = "ModelInformation.findByDefaultDescription", query = "SELECT m FROM ModelInformation m WHERE m.defaultDescription = :defaultDescription"),
    @NamedQuery(name = "ModelInformation.findBySampleConfig", query = "SELECT m FROM ModelInformation m WHERE m.sampleConfig = :sampleConfig"),
    @NamedQuery(name = "ModelInformation.findByUsage", query = "SELECT m FROM ModelInformation m WHERE m.usage = :usage")})
public class ModelInformation implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "model_id")
    private String modelId;
    @Column(name = "date_first_registered")
    @Temporal(TemporalType.DATE)
    private Date dateFirstRegistered;
    @Column(name = "date_last_registered")
    @Temporal(TemporalType.DATE)
    private Date dateLastRegistered;
    @Size(max = 63)
    @Column(name = "default_name")
    private String defaultName;
    @Size(max = 255)
    @Column(name = "copyright_holder")
    private String copyrightHolder;
    @Size(max = 2147483647)
    @Column(name = "license")
    private String license;
    @Size(max = 2147483647)
    @Column(name = "default_description")
    private String defaultDescription;
    @Size(max = 2147483647)
    @Column(name = "sample_config")
    private String sampleConfig;
    @Size(max = 2147483647)
    @Column(name = "usage")
    private String usage;

    public ModelInformation() {
    }

    public ModelInformation(String modelId) {
        this.modelId = modelId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Date getDateFirstRegistered() {
        return dateFirstRegistered;
    }

    public void setDateFirstRegistered(Date dateFirstRegistered) {
        this.dateFirstRegistered = dateFirstRegistered;
    }

    public Date getDateLastRegistered() {
        return dateLastRegistered;
    }

    public void setDateLastRegistered(Date dateLastRegistered) {
        this.dateLastRegistered = dateLastRegistered;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getCopyrightHolder() {
        return copyrightHolder;
    }

    public void setCopyrightHolder(String copyrightHolder) {
        this.copyrightHolder = copyrightHolder;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getSampleConfig() {
        return sampleConfig;
    }

    public void setSampleConfig(String sampleConfig) {
        this.sampleConfig = sampleConfig;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (modelId != null ? modelId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ModelInformation)) {
            return false;
        }
        ModelInformation other = (ModelInformation) object;
        if ((this.modelId == null && other.modelId != null) || (this.modelId != null && !this.modelId.equals(other.modelId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.ModelInformation[ modelId=" + modelId + " ]";
    }

}
