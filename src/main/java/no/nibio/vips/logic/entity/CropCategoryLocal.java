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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "crop_category_local")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CropCategoryLocal.findAll", query = "SELECT c FROM CropCategoryLocal c"),
    @NamedQuery(name = "CropCategoryLocal.findByCropCategoryId", query = "SELECT c FROM CropCategoryLocal c WHERE c.cropCategoryLocalPK.cropCategoryId = :cropCategoryId"),
    @NamedQuery(name = "CropCategoryLocal.findByLocale", query = "SELECT c FROM CropCategoryLocal c WHERE c.cropCategoryLocalPK.locale = :locale"),
    @NamedQuery(name = "CropCategoryLocal.findByLocalName", query = "SELECT c FROM CropCategoryLocal c WHERE c.localName = :localName")})
public class CropCategoryLocal implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected CropCategoryLocalPK cropCategoryLocalPK;
    @Size(max = 255)
    @Column(name = "local_name")
    private String localName;
    
    // Easy fix for mapping from CropCategory
    @Column(name="crop_category_id", insertable = false, updatable = false)
    private Integer cropCategoryId;

    public CropCategoryLocal() {
    }

    public CropCategoryLocal(CropCategoryLocalPK cropCategoryLocalPK) {
        this.cropCategoryLocalPK = cropCategoryLocalPK;
    }

    public CropCategoryLocal(int cropCategoryId, String locale) {
        this.cropCategoryLocalPK = new CropCategoryLocalPK(cropCategoryId, locale);
    }

    public CropCategoryLocalPK getCropCategoryLocalPK() {
        return cropCategoryLocalPK;
    }

    public void setCropCategoryLocalPK(CropCategoryLocalPK cropCategoryLocalPK) {
        this.cropCategoryLocalPK = cropCategoryLocalPK;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cropCategoryLocalPK != null ? cropCategoryLocalPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CropCategoryLocal)) {
            return false;
        }
        CropCategoryLocal other = (CropCategoryLocal) object;
        if ((this.cropCategoryLocalPK == null && other.cropCategoryLocalPK != null) || (this.cropCategoryLocalPK != null && !this.cropCategoryLocalPK.equals(other.cropCategoryLocalPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.CropCategoryLocal[ cropCategoryLocalPK=" + cropCategoryLocalPK + " ]";
    }

    /**
     * @return the cropCategoryId
     */
    public Integer getCropCategoryId() {
        return cropCategoryId;
    }

    /**
     * @param cropCategoryId the cropCategoryId to set
     */
    public void setCropCategoryId(Integer cropCategoryId) {
        this.cropCategoryId = cropCategoryId;
    }

}
