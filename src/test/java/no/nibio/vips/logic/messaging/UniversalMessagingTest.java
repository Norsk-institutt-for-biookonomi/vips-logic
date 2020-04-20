/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import no.nibio.vips.logic.util.RESTAuthenticator;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author treinar
 */
public class UniversalMessagingTest {
    
    public UniversalMessagingTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    
    //@Test
    public void deserializeMessageLocalVersionTest()
    {
        System.out.println("deserializeMessageLocalVersionTest");
        try
        {
            String jsonText = "[{\"locale\":\"no\",\"msgSubject\":\"Bladveps funnet på Kyrksæterøra\",\"msgLeadParagraph\":\"Se opp for svermende veps med brodder\",\"msgBody\":\"Lorem ipsum Dolores Sanctum Nobliatarus Factorum des nomine in sanctus spiritu benedictus qui venit dies irae Lorem ipsum Dolores Sanctum Nobliatarus Factorum des nomine in sanctus spiritu benedictus qui venit dies irae Lorem ipsum Dolores Sanctum Nobliatarus Factorum des nomine in sanctus spiritu benedictus qui venit dies irae\",\"msgDownloadUrl\":\"http://www.nibio.no/\"}]";

            ObjectMapper mapper = new ObjectMapper();
            //mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            //mapper.configure(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS,true);
            List<MessageLocalVersion> result = mapper.readValue(jsonText, new TypeReference<List<MessageLocalVersion>>(){});
        }
        catch(Exception e)
        {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    
    //@Test
    public void serviceTest()
    {
        System.out.println("serviceTest");
        String result = null;
        try
        {
            UniversalMessage um = new UniversalMessage(
                    "nb",
                    "Bladveps funnet på Kyrksæterøra",
                    "Se opp for svermende veps med brodder",
                    "Lorem ipsum Dolores Sanctum Nobliatarus Factorum des nomine in sanctus spiritu benedictus qui venit dies irae Lorem ipsum Dolores Sanctum Nobliatarus Factorum des nomine in sanctus spiritu benedictus qui venit dies irae Lorem ipsum Dolores Sanctum Nobliatarus Factorum des nomine in sanctus spiritu benedictus qui venit dies irae",
                    "http://www.nibio.no/"
            );
            
            um.addMessageLocalVersion(
                    "en", 
                    "Leaf wasp found in Kyrksæterøra", 
                    "Look out for swarming wasps and their dangerous sting", 
                    "Yesterday, all my troubles seemed so far away. Now it looks as though they're here to stay.", 
                    "http://www.nibio.no/"
            );
            
            
            String[][] recipients = {
                //{"1","Tor-Einar Skog","Mail","tor-einar.skog@nibio.no","nb","true"}
                {"2","Tor-Einar Skog","Mail","tor.einar.skog@gmail.com","en","false"},
                //{"3","Tor-Einar Skog","Mail","tor.einar.skog@gmail.com","bs","false"},
                {"1","Tor-Einar Skog","Sms","004791303819","nb","true"}
                //{"2","Lars Aksel Opsahl", "Mail","lars.opsahl@nibio.no"}
            };

            List<MessageRecipient> rList = new ArrayList<>();
            for(String[] recipient:recipients)
            {
                rList.add(new MessageRecipient(
                        recipient[0],
                        recipient[1],
                        recipient[2],
                        recipient[3],
                        recipient[4],
                        recipient[5].equals("true")
                    )
                );
            }
            um.setDistributionList(rList);

            ResteasyClient client = new ResteasyClientBuilder().build();
            //client.register(new RESTAuthenticator("user", "userPass"));
            client.register(new RESTAuthenticator("VIPSLogic", "plmoknijbuhv000"));
            //ResteasyWebTarget target = client.target("http://kart13utv.ad.skogoglandskap.no:8080");
            ResteasyWebTarget target = client.target("http://localhost:8080");
            //ResteasyWebTarget target = client.target("http://logic.testvips2.nibio.no");
            UniversalMessagingServiceClient umClient = target.proxy(UniversalMessagingServiceClient.class);
            
        
            //System.out.println(new ObjectMapper().writeValueAsString(um));
            Response r = umClient.sendMessage(new ObjectMapper().writeValueAsString(um));
            
            
            result = r.readEntity(String.class);
            System.out.println(result);
            assertEquals(200,r.getStatus());
            //Response r = umClient.sendMessage(um);
        }
        catch(IOException | IllegalArgumentException | NullPointerException e)
        {
            //e.printStackTrace();
            fail(e.getMessage());
        }
        assertNotNull(result);
    }
}
