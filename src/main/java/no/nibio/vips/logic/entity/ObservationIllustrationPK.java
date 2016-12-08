/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class ObservationIllustrationPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "observation_id")
    private int observationId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2047)
    @Column(name = "file_name")
    private String fileName;

    public ObservationIllustrationPK() {
    }

    public ObservationIllustrationPK(int observationId, String fileName) {
        this.observationId = observationId;
        this.fileName = fileName;
    }

    public int getObservationId() {
        return observationId;
    }

    public void setObservationId(int observationId) {
        this.observationId = observationId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) observationId;
        hash += (fileName != null ? fileName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationIllustrationPK)) {
            return false;
        }
        ObservationIllustrationPK other = (ObservationIllustrationPK) object;
        if (this.observationId != other.observationId) {
            return false;
        }
        if ((this.fileName == null && other.fileName != null) || (this.fileName != null && !this.fileName.equals(other.fileName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ObservationIllustrationPK[ observationId=" + observationId + ", fileName=" + fileName + " ]";
    }

}
