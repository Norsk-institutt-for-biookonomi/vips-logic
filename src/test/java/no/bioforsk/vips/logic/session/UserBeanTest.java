/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.session;

import java.util.Locale;
import java.util.ResourceBundle;
import no.bioforsk.vips.logic.controller.session.UserBean;
import no.bioforsk.vips.logic.util.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author treinar
 */
public class UserBeanTest {

    
    public UserBeanTest() {
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
    
    // This is simply to read the output without having to send the bleeding mail every time
    // Uncomment the System.out.println(result); and run the test
    @Test
    public void sendUserEmailVerificationBodyTextTest()
    {
        ResourceBundle i18nBundle = ResourceBundle.getBundle("no.bioforsk.vips.logic.i18n.vipslogictexts",new Locale("no"));
        UserBean uBean = new UserBean();
        String result = uBean.getUserEmailVerificationBody(i18nBundle, "logic.vips.bioforsk.no",StringUtils.generateRandomAlphanumericString(30));
        //System.out.println(result);
        assert(true);
    }

    
}
