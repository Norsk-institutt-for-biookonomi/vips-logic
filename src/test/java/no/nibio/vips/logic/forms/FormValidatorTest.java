/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.forms;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
            ResourceBundle resourceBundle = ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts", Locale.ENGLISH);
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
