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

package no.nibio.vips.observationdata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/observationdata")
public class ObservationDataService {

    @GET
    @Path("schema/{organizationId}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getSchema(){
        // Try to find schema for given organism/organization
        // If not found, return standard nominator/denominator (unit) form 
        return Response.ok().entity(this.getStandardSchema()).build();
    }
    
    @GET
    @Path("model/{organizationId}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getModel(){
        // Try to find schema for given organism/organization
        // If not found, return standard nominator/denominator (unit) form 
        return Response.ok().entity(this.getStandardModel()).build();
    }
    
    private String getStandardSchema(){
        return "{"
                + "\"number\":{\"title\":\"Number\"},"
                + "\"unit\":{\"title\":\"Unit\"}"
                + "}";
    }
    
    private String getStandardModel(){
        return "{"
                + "\"number\":0,"
                + "\"unit\":\"Number\""
                + "}";
    }
}
