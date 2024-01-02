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

package no.nibio.vips.logic.service;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbDateFormat;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import java.util.logging.Level;  
import java.util.logging.Logger; 

/**
 * Add this to no.nibio.vips.logic.VIPSLogicApplication if you want all
 * dates to be serialized as ISO formatted date strings
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
public class JSONBConfig implements ContextResolver<javax.json.bind.Jsonb>
{
    //private final ObjectMapper objectMapper;
    private final Jsonb jsonB;  
    private Logger log; 

    public JSONBConfig() throws Exception
    {
        log = Logger.getLogger(JacksonConfig.class.getName());  
        log.log(Level.INFO,"Created JSONBConfig");  
        //System.out.println("This is what you should expect");
        JsonbConfig config = new JsonbConfig();  
        config.setProperty(JsonbConfig.DATE_FORMAT, JsonbDateFormat.DEFAULT_FORMAT);  
        jsonB = JsonbBuilder.create(config);
    }

    @Override
    public Jsonb getContext(Class objectType)
    {
        log.log(Level.INFO,"Retrieved Objectmapper "+objectType.toGenericString());  
        return jsonB;
    }
 }