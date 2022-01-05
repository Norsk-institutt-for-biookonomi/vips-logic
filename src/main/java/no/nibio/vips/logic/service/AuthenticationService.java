/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import javax.ejb.EJB;
import no.nibio.vips.logic.controller.session.UserBean;

import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;

/**
 * Authentication services
 * @copyright 2021-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/auth")
public class AuthenticationService {
    
    @EJB
    UserBean userBean;
	
    @Context
    private HttpServletRequest httpServletRequest;
	
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
		Map<String,String> creds = new HashMap();
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
	public Response getUserByUuidInAuthorizationHeader()
	{
		String uuidStr = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
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
