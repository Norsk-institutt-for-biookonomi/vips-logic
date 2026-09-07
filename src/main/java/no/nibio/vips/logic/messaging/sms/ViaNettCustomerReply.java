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

package no.nibio.vips.logic.messaging.sms;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@XmlRootElement(name="ack")
@XmlAccessorType(XmlAccessType.FIELD)
public class ViaNettCustomerReply {

    @XmlAttribute
    private String refno;
    
    @XmlAttribute
    private String errorcode;
    
    @XmlAttribute(name="response_msg")
    private String response_msg;
    
    @XmlAttribute(name="response_pricegroup")
    private String response_pricegroup;
    
    @XmlAttribute(name = "response_fromalpha")
    private String response_fromalpha;

    /**
     * @return the refno
     */
    public String getRefno() {
        return refno;
    }

    /**
     * @param refno the refno to set
     */
    public void setRefno(String refno) {
        this.refno = refno;
    }

    /**
     * @return the errorcode
     */
    public String getErrorcode() {
        return errorcode;
    }

    /**
     * @param errorcode the errorcode to set
     */
    public void setErrorcode(String errorcode) {
        this.errorcode = errorcode;
    }

    /**
     * @return the response_msg
     */
    public String getResponse_msg() {
        return response_msg;
    }

    /**
     * @param response_msg the response_msg to set
     */
    public void setResponse_msg(String response_msg) {
        this.response_msg = response_msg;
    }

    /**
     * @return the response_pricegroup
     */
    public String getResponse_pricegroup() {
        return response_pricegroup;
    }

    /**
     * @param response_pricegroup the response_pricegroup to set
     */
    public void setResponse_pricegroup(String response_pricegroup) {
        this.response_pricegroup = response_pricegroup;
    }

    /**
     * @return the response_fromalpha
     */
    public String getResponse_fromalpha() {
        return response_fromalpha;
    }

    /**
     * @param response_fromalpha the response_fromalpha to set
     */
    public void setResponse_fromalpha(String response_fromalpha) {
        this.response_fromalpha = response_fromalpha;
    }
}
