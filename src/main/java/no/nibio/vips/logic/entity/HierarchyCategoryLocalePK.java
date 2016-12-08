/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
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
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class HierarchyCategoryLocalePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "hierarchy_category_id")
    private int hierarchyCategoryId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "locale")
    private String locale;

    public HierarchyCategoryLocalePK() {
    }

    public HierarchyCategoryLocalePK(int hierarchyCategoryId, String locale) {
        this.hierarchyCategoryId = hierarchyCategoryId;
        this.locale = locale;
    }

    public int getHierarchyCategoryId() {
        return hierarchyCategoryId;
    }

    public void setHierarchyCategoryId(int hierarchyCategoryId) {
        this.hierarchyCategoryId = hierarchyCategoryId;
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
        hash += (int) hierarchyCategoryId;
        hash += (locale != null ? locale.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HierarchyCategoryLocalePK)) {
            return false;
        }
        HierarchyCategoryLocalePK other = (HierarchyCategoryLocalePK) object;
        if (this.hierarchyCategoryId != other.hierarchyCategoryId) {
            return false;
        }
        if ((this.locale == null && other.locale != null) || (this.locale != null && !this.locale.equals(other.locale))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.HierarchyCategoryLocalePK[ hierarchyCategoryId=" + hierarchyCategoryId + ", locale=" + locale + " ]";
    }

}
