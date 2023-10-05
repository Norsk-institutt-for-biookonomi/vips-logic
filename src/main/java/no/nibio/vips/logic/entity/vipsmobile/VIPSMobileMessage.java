/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VIPSMobileMessage implements Comparable{
    
    private int meldingId;
    private String tittel;
    private String ingress;
    private String tekst;
    private Date registrertDato;
    private Date gyldigTilDato;
    private int meldingType;
    private String bildeURL;
    private String bildetekst;
    
    public VIPSMobileMessage (
            int meldingId,
            String tittel,
            String ingress,
            String tekst,
            Date registrertDato,
            Date gyldigTilDato,
            int meldingType,
            String bildeURL,
            String bildetekst
    )
    {
        this.meldingId = meldingId;
        this.tittel = tittel;
        this.ingress = ingress;
        this.tekst = tekst;
        this.registrertDato = registrertDato;
        this.gyldigTilDato = gyldigTilDato;
        this.meldingType = meldingType;
        this.bildeURL = bildeURL;
        this.bildetekst = bildetekst;
    }

    /**
     * @return the meldingId
     */
    public int getMeldingId() {
        return meldingId;
    }

    /**
     * @param meldingId the meldingId to set
     */
    public void setMeldingId(int meldingId) {
        this.meldingId = meldingId;
    }

    /**
     * @return the tittel
     */
    public String getTittel() {
        return tittel;
    }

    /**
     * @param tittel the tittel to set
     */
    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    /**
     * @return the ingress
     */
    public String getIngress() {
        return ingress;
    }

    /**
     * @param ingress the ingress to set
     */
    public void setIngress(String ingress) {
        this.ingress = ingress;
    }

    /**
     * @return the tekst
     */
    public String getTekst() {
        return tekst;
    }

    /**
     * @param tekst the tekst to set
     */
    public void setTekst(String tekst) {
        this.tekst = tekst;
    }

    /**
     * @return the registrertDato
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    public Date getRegistrertDato() {
        return registrertDato;
    }

    /**
     * @param registrertDato the registrertDato to set
     */
    public void setRegistrertDato(Date registrertDato) {
        this.registrertDato = registrertDato;
    }

    /**
     * @return the gyldigTilDato
     */
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
    public Date getGyldigTilDato() {
        return gyldigTilDato;
    }

    /**
     * @param gyldigTilDato the gyldigTilDato to set
     */
    public void setGyldigTilDato(Date gyldigTilDato) {
        this.gyldigTilDato = gyldigTilDato;
    }

    /**
     * @return the meldingType
     */
    public int getMeldingType() {
        return meldingType;
    }

    /**
     * @param meldingType the meldingType to set
     */
    public void setMeldingType(int meldingType) {
        this.meldingType = meldingType;
    }

    /**
     * @return the bildeURL
     */
    public String getBildeURL() {
        return bildeURL;
    }

    /**
     * @param bildeURL the bildeURL to set
     */
    public void setBildeURL(String bildeURL) {
        this.bildeURL = bildeURL;
    }

    /**
     * @return the bildetekst
     */
    public String getBildetekst() {
        return bildetekst;
    }

    /**
     * @param bildetekst the bildetekst to set
     */
    public void setBildetekst(String bildetekst) {
        this.bildetekst = bildetekst;
    }
    
    public boolean getLeaf()
    {
        return true;
    }

    @Override
    public int compareTo(Object t) {
        return this.getRegistrertDato().compareTo(((VIPSMobileMessage)t).getRegistrertDato());
    }

}
