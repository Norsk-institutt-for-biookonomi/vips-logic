/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import no.nibio.vips.logic.controller.session.UserBean;

import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;

/**
 * Authentication services
 * @copyright 2021-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Path("auth")
public class AuthenticationService {
    
    @Inject
    UserBean userBean;
	/**
	 * Authenticates user
	 * Example input
     * <pre>
     * {
        "username": "foo",
        "password": "bar"
      }
     </pre>
	 * @responseExample application/json {"success":"true", "UUID": "01bce95b-0004-4567-a4a2-3c184954fc15"}
	 * @responseExample application/json {"success":"false"}
	 */
	@POST
	@Path("login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(JsonNode credentials) {
		// Get username and password from Json
		String username = credentials.get("username").asText();
		String password = credentials.get("password").asText();
		Map<String,String> creds = new HashMap<>();
        creds.put("username", username);
        creds.put("password", password);
		// Authenticate 
        VipsLogicUser user = userBean.authenticateUser(creds);
		// Return success (true/false) and UUID if success
        ObjectMapper objectMapper = new ObjectMapper();
        
        ObjectNode result = objectMapper.createObjectNode();
        if(user != null)
        {
        	UserUuid uuid = userBean.createAndPersistUserUuid(user);
        	result.put("success", true);
        	result.put("UUID", uuid.getUserUuidPK().getUserUuid().toString());
        }
        else
        {
        	result.put("success", false);
        }
        
        return Response.status(user != null ? Status.CREATED : Status.NOT_FOUND).entity(result).build();
	}
	
	/**
	 * Provide the UUID as the contents of the Authorization header
	 * @return The VIPSLogic user associated with this uuid, or 404 if not found
	 */
	@GET
	@Path("uuid")
	@Produces(MediaType.APPLICATION_JSON)
	@TypeHint(VipsLogicUser.class)
	public Response getUserByUuidInAuthorizationHeader(@Context HttpHeaders headers)
	{
		String uuidStr = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
		UUID uuid = UUID.fromString(uuidStr);
		VipsLogicUser user = userBean.findVipsLogicUser(uuid);
		if(user != null)
		{
			// Also, renew the uuid by default length
			userBean.renewUserUuid(uuid);
			return Response.ok().entity(user).build();			
		}
		else
		{
			return Response.status(Status.NOT_FOUND).build();
		}
	}
}
