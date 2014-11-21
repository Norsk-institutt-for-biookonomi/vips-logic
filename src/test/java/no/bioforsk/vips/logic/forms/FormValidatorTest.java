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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidationException;
import no.bioforsk.web.forms.FormValidator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class FormValidatorTest {
    
    public FormValidatorTest() {
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
    
    @Test
    public void testPointWGS84Validation()
    {
        String testFormDef = "{\"fields\": [\n" +

            "        {\n" +
            "            \"name\" : \"location\",\n" +
            "            \"dataType\" : \"POINT_WGS84\",\n" +
            "            \"required\" : true\n" +
            "        }]}";
        try {
            JsonNode def = FormValidator.getFormDefinition(testFormDef);
            ResourceBundle resourceBundle = ResourceBundle.getBundle("no.bioforsk.vips.logic.i18n.vipslogictexts", Locale.ENGLISH);
            Map<String,String[]> parameterMap = new HashMap<>();
            String[][] validPoints = {{"34.2,45.9"},{"34,45"}};
            String[][] invalidPoints = {{"blablabla"},{"3,4,7"}};
            FormValidation validation;
            for(String[] validPoint:validPoints)
            {
                parameterMap.put("location",validPoint);
                validation = FormValidator.validateForm(def, parameterMap, resourceBundle);
                assertTrue(validation.isValid());
            }
            for(String[] invalidPoint:invalidPoints)
            {
                parameterMap.put("location",invalidPoint);
                validation = FormValidator.validateForm(def, parameterMap, resourceBundle);
                assertFalse(validation.isValid());
            }
            
        } catch (IOException | FormValidationException ex) {
            fail(ex.getMessage());
        }
    }
}
