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

import com.webcohesion.enunciate.metadata.Facet;
import jakarta.ejb.EJB;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.VipsLogicUser;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/sms")
@Facet("restricted")
public class SMSHandlingService {
    
    @EJB
    UserBean userBean;

    /**
     * Sample of incoming request: /rest/sms/vianett/customer_request
     * ?refno=25145540&now=20160219112102&requesttype=mo&sourceaddr=4791303819&destinationaddr=1963
     * &replypathid=0&prefix=TESTNIBIO&message=STOP&retrycount=0&operator=1
     * &referenceid=CPA2162fc5f-626e-414b-aa5b-ba88a98a5939&username=*****&password=***** (username and password hidden deliberately)
     * 
     * @param username
     * @param password
     * @param refno
     * @param message
     * @param sourceaddr The client's phone number
     * @return 
     */
    @GET
    @Path("vianett/customer_request")
    @Produces("application/xml;charset=UTF-8")
    public ViaNettCustomerReply handleCustomerRequest(
            @QueryParam("username") String username,
            @QueryParam("password") String password,
            @QueryParam("refno") String refno,
            @QueryParam("message") String message,
            @QueryParam("sourceaddr") String sourceaddr
    )
    {
        ViaNettCustomerReply reply = new ViaNettCustomerReply();
        reply.setRefno(refno);
        reply.setErrorcode("0");
        if(message !=null && sourceaddr != null && username!=null && username.equals("vianett") && password != null && password.equals("plantevern2016"))
        {
            VipsLogicUser user = userBean.getUserByPhoneNumber(sourceaddr);
            if(user != null)
            {
                if(message.trim().toUpperCase().equals("STOP"))
                {
                    user.setApprovesSmsBilling(false);
                    reply.setResponse_msg("Confirmation: Sending and billing of SMS messages from VIPS is stopped.");
                }
                else if(message.trim().toUpperCase().equals("START"))
                {
                    user.setApprovesSmsBilling(true);
                    reply.setResponse_msg("Confirmation: Sending and billing of SMS messages from VIPS is started.");
                }
                userBean.storeUser(user);
                reply.setResponse_fromalpha("Vips");
                reply.setResponse_pricegroup("0");
            }
        }
        
        return reply;
    }
}
