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

import java.util.ArrayList;
import java.util.List;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.Organism;

/**
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VIPSMobileCropCategory {
    private List<VIPSMobileCrop> kulturer;
    private List<Organism> crops;
    private Integer varselKulturKategoriId;
    private String varselKulturKategoriNavn;
    private Integer ordering;
    
    public VIPSMobileCropCategory(CropCategory cc, List<Organism> organisms, String locale){
        this.varselKulturKategoriId = cc.getCropCategoryId();
        this.varselKulturKategoriNavn = cc.getLocalName(locale);
        this.ordering = cc.getCropCategoryId();
        this.kulturer = new ArrayList<>();
        organisms.stream().forEach(organism -> {
                this.kulturer.add(new VIPSMobileCrop(organism, locale));
            });
    }

    /**
     * @return the kulturer
     */
    public List<VIPSMobileCrop> getKulturer() {
        return kulturer;
    }

    /**
     * @param kulturer the kulturer to set
     */
    public void setKulturer(List<VIPSMobileCrop> kulturer) {
        this.kulturer = kulturer;
    }

    /**
     * @return the varselKulturKategoriId
     */
    public Integer getVarselKulturKategoriId() {
        return varselKulturKategoriId;
    }

    /**
     * @param varselKulturKategoriId the varselKulturKategoriId to set
     */
    public void setVarselKulturKategoriId(Integer varselKulturKategoriId) {
        this.varselKulturKategoriId = varselKulturKategoriId;
    }

    /**
     * @return the varselKulturKategoriNavn
     */
    public String getVarselKulturKategoriNavn() {
        return varselKulturKategoriNavn;
    }

    /**
     * @param varselKulturKategoriNavn the varselKulturKategoriNavn to set
     */
    public void setVarselKulturKategoriNavn(String varselKulturKategoriNavn) {
        this.varselKulturKategoriNavn = varselKulturKategoriNavn;
    }

    /**
     * @return the ordering
     */
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * @param ordering the ordering to set
     */
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }
}
