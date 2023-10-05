/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.modules.barkbeetle;

import java.util.Calendar;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ONLY POJO LEVEL TESTS!
 * 
 * @author treinar
 */
public class BarkbeetleBeanTest {
    
    public BarkbeetleBeanTest() {
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


    /**
     * Test of isTimeToSendFirstReminder method, of class BarkbeetleBean.
     * They are year specific, hard to test in other ways without not really testing
     */
    //@Test
    public void testIsTimeToSendFirstReminder() throws Exception {
        System.out.println("isTimeToSendFirstReminder");
        
        // Week 21
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.DATE, 19);
        Date currentDate = cal.getTime();
        System.out.println("currentDate=" + currentDate);
        BarkbeetleBean instance = new BarkbeetleBean();
        Boolean result = instance.isTimeToSendFirstReminder(currentDate);
        assertTrue(result);
        
        cal.set(Calendar.DATE, 20);
        currentDate = cal.getTime();
        result = instance.isTimeToSendFirstReminder(currentDate);
        assertFalse(result);
        
        // Week 24
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DATE, 9);
        currentDate = cal.getTime();
        assertTrue(instance.isTimeToSendFirstReminder(currentDate));
        
        // Week 28
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DATE, 7);
        currentDate = cal.getTime();
        assertTrue(instance.isTimeToSendFirstReminder(currentDate));
        
        // Week 33
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        cal.set(Calendar.DATE, 11);
        currentDate = cal.getTime();
        assertTrue(instance.isTimeToSendFirstReminder(currentDate));
        
    }

    /**
     * Test of isTimeToSendSecondReminder method, of class BarkbeetleBean.
     * They are year specific, hard to test in other ways without not really testing
     */
    @Test
    public void testIsTimeToSendSecondReminder() throws Exception {
        System.out.println("isTimeToSendSecondReminder");
        
        // Week 21
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2023);
        cal.set(Calendar.MONTH, Calendar.MAY);
        cal.set(Calendar.DATE, 24);
        Date currentDate = cal.getTime();
        System.out.println("currentDate=" + currentDate);
        BarkbeetleBean instance = new BarkbeetleBean();
        Boolean result = instance.isTimeToSendSecondReminder(currentDate);
        assertTrue(result);
        
        cal.set(Calendar.DATE, 23);
        currentDate = cal.getTime();
        result = instance.isTimeToSendSecondReminder(currentDate);
        assertFalse(result);
        
        // Week 24
        cal.set(Calendar.MONTH, Calendar.JUNE);
        cal.set(Calendar.DATE, 14);
        currentDate = cal.getTime();
        assertTrue(instance.isTimeToSendSecondReminder(currentDate));
        
        // Week 28
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DATE, 12);
        currentDate = cal.getTime();
        assertTrue(instance.isTimeToSendSecondReminder(currentDate));
        
        // Week 33
        cal.set(Calendar.MONTH, Calendar.AUGUST);
        cal.set(Calendar.DATE, 16);
        currentDate = cal.getTime();
        assertTrue(instance.isTimeToSendSecondReminder(currentDate));
        
    }
    
    @Test
    public void testGetIndividualFirstReminderText() throws Exception
    {
        System.out.println("getIndividualFirstReminderText");
        SeasonTrapsite s = new SeasonTrapsite();
        s.setCountyName("Vestfold og Telemark");
        s.setMunicipalityName("Porsgrunn");
        
        BarkbeetleBean instance = new BarkbeetleBean();
        
        String result = instance.getIndividualFirstReminderText(s, 2023, 21);
        System.out.println(result);
        assertNotNull(result);
    }
    
    @Test
    public void testGetIndividualSecondReminderText() throws Exception
    {
        System.out.println("getIndividualSecondReminderText");
        SeasonTrapsite s = new SeasonTrapsite();
        s.setCountyName("Vestfold og Telemark");
        s.setMunicipalityName("Porsgrunn");
        BarkbeetleBean instance = new BarkbeetleBean();
        
        String result = instance.getIndividualSecondReminderText(s, 2023, 21);
        System.out.println(result);
        assertNotNull(result);
    }
    
    @Test
    public void testCountyContactReminderText() throws Exception
    {
        System.out.println("getCountyContactReminderText");

        BarkbeetleBean instance = new BarkbeetleBean();
        
        String result = instance.getCountyContactReminderText(2022, 21);
        System.out.println(result);
        assertNotNull(result);
    }
    
}
