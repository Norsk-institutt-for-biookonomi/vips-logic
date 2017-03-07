/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.entity.vipsmobile;

import no.nibio.vips.logic.entity.Organism;

/**
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VIPSMobileCrop {

    private Integer kulturId;
    private String kulturNavn;
    public VIPSMobileCrop(Organism organism, String locale){
        this.kulturId = organism.getOrganismId();
        this.kulturNavn = organism.getLocalName(locale);
    }

    /**
     * @return the kulturId
     */
    public Integer getKulturId() {
        return kulturId;
    }

    /**
     * @param kulturId the kulturId to set
     */
    public void setKulturId(Integer kulturId) {
        this.kulturId = kulturId;
    }

    /**
     * @return the kulturNavn
     */
    public String getKulturNavn() {
        return kulturNavn;
    }

    /**
     * @param kulturNavn the kulturNavn to set
     */
    public void setKulturNavn(String kulturNavn) {
        this.kulturNavn = kulturNavn;
    }
}
