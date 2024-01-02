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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class CropCategoryLocalPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "crop_category_id")
    private int cropCategoryId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "locale")
    private String locale;

    public CropCategoryLocalPK() {
    }

    public CropCategoryLocalPK(int cropCategoryId, String locale) {
        this.cropCategoryId = cropCategoryId;
        this.locale = locale;
    }

    public int getCropCategoryId() {
        return cropCategoryId;
    }

    public void setCropCategoryId(int cropCategoryId) {
        this.cropCategoryId = cropCategoryId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) cropCategoryId;
        hash += (locale != null ? locale.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CropCategoryLocalPK)) {
            return false;
        }
        CropCategoryLocalPK other = (CropCategoryLocalPK) object;
        if (this.cropCategoryId != other.cropCategoryId) {
            return false;
        }
        if ((this.locale == null && other.locale != null) || (this.locale != null && !this.locale.equals(other.locale))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.CropCategoryLocalPK[ cropCategoryId=" + cropCategoryId + ", locale=" + locale + " ]";
    }

}
