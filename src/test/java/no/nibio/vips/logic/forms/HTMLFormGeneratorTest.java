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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import no.nibio.web.forms.HTMLFormGenerator;
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
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(reader);
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
