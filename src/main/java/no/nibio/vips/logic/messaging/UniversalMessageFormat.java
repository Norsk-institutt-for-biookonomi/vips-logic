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

package no.nibio.vips.logic.messaging;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "universal_message_format", schema = "messaging")
@NamedQueries({
    @NamedQuery(name = "UniversalMessageFormat.findAll", query = "SELECT u FROM UniversalMessageFormat u")
})
public class UniversalMessageFormat implements Serializable{
    
    public static final Integer FORMAT_EMAIL = 1;
    public static final Integer FORMAT_SMS = 2;
    @Id
    @Column(name = "universal_message_format_id")
    private Integer universalMessageFormatId;
    @Column(name = "format_name")
    private String formatName;
    

    /**
     * @return the universalMessageFormatId
     */
    public Integer getUniversalMessageFormatId() {
        return universalMessageFormatId;
    }

    /**
     * @param universalMessageFormatId the universalMessageFormatId to set
     */
    public void setUniversalMessageFormatId(Integer universalMessageFormatId) {
        this.universalMessageFormatId = universalMessageFormatId;
    }

    /**
     * @return the formatName
     */
    public String getFormatName() {
        return formatName;
    }

    /**
     * @param formatName the formatName to set
     */
    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }
}
