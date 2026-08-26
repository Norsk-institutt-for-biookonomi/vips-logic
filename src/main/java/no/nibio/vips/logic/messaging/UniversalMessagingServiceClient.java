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

package no.nibio.vips.logic.messaging;

import com.webcohesion.enunciate.metadata.Facet;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 * @copyright 2015 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Facet("restricted")
@RegisterRestClient(configKey = "universal-messaging")
@Path("/")
public interface UniversalMessagingServiceClient {

   @POST
   @Path("vips_msg_ws_01/rest/send_vips_msg")
   @Consumes("application/json;charset=UTF-8")
   @Produces("application/json;charset=UTF-8")
   public Response sendMessage(UniversalMessage universalMessage);
   
   @POST
   @Path("vips_msg_ws_01/rest/send_vips_msg")
   @Consumes("application/json;charset=UTF-8")
   @Produces("application/json;charset=UTF-8")
   public Response sendMessage(String universalMessage);
}
