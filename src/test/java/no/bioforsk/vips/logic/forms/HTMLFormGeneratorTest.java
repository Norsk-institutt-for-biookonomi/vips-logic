/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import no.bioforsk.web.forms.HTMLFormGenerator;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
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
public class HTMLFormGeneratorTest {
    
    public HTMLFormGeneratorTest() {
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

    @Test
    public void testGetHTMLForm() {
        try {
            String language="no";
            HTMLFormGenerator t = new HTMLFormGenerator();
            assertNotNull(t);
            InputStream in = this.getClass().getResourceAsStream("/HTMLFormGeneratorTest/forecastConfigurationForm.json");
            InputStreamReader reader = new InputStreamReader(in,"UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getJsonFactory();
            JsonParser parser = factory.createJsonParser(reader);
            JsonNode formDefinition = mapper.readTree(parser);
            InputStream in2 = this.getClass().getResourceAsStream("/HTMLFormGeneratorTest/forecastConfigurationForm_result.html");
            InputStreamReader reader2 = new InputStreamReader(in2, "UTF-8");
            java.util.Scanner s = new java.util.Scanner(reader2).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            //System.out.println("expectedResult=\n" + result);
            String HTMLForm = t.getHTMLFormFields(formDefinition,"forecastConfigurationForm",language);
            //System.out.println("HTMLForm=\n" + HTMLForm);
            assertEquals(result,HTMLForm);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }
}
