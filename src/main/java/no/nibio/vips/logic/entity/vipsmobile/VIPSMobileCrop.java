/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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
