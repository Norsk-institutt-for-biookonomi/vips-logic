/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.gis.CoordinateXYZ;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXYZM;
import org.locationtech.jts.geom.Point;

/**
 * @copyright 2020 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class BarkbeetleController extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.BARKBEETLE_ADMIN, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN, VipsLogicRole.BARKBEETLE_REGISTRATOR, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
        {
            request.setAttribute("user",user);
            request.setAttribute("userIsAdmin",SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.BARKBEETLE_ADMIN, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN, VipsLogicRole.SUPERUSER));
            Integer season = request.getParameter("season") != null?  Integer.valueOf(request.getParameter("season"))
                        : LocalDate.ofInstant(SystemTime.getSystemTime().toInstant(),ZoneId.of("Europe/Oslo")).getYear();
            String action = request.getParameter("action");
            if(action == null)
            {
                List<SeasonTrapsite> seasonTrapSites = BarkbeetleBean.getInstance().getSeasonTrapsites(season);
                request.setAttribute("season", season);
                request.setAttribute("seasonTrapsites", seasonTrapSites);
                request.getRequestDispatcher("/modules/barkbeetle/barkbeetleSeasonTrapsiteList.ftl").forward(request, response);
            }
            else if(action.equals("editSeasonTrapsite"))
            {
                if(!SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.BARKBEETLE_ADMIN, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
                request.setAttribute("trapsiteRegistrators", SessionControllerGetter.getUserBean().getUsersByVipsLogicRoles(new Integer[]{VipsLogicRole.BARKBEETLE_ADMIN, VipsLogicRole.BARKBEETLE_REGISTRATOR, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN}));
                SeasonTrapsite trapsite = request.getParameter("seasonTrapsiteId") != null ? 
                        BarkbeetleBean.getInstance().getSeasonTrapsite(Integer.valueOf(request.getParameter("seasonTrapsiteId")))
                        : new SeasonTrapsite();
                
                List<TrapsiteType> trapsiteTypes = BarkbeetleBean.getInstance().getTrapsiteTypes();
                request.setAttribute("season", season);
                request.setAttribute("seasonTrapsite", trapsite);
                request.setAttribute("seasonTrapsiteTypes", trapsiteTypes);
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/modules/barkbeetle/barkbeetleSeasonTrapsiteForm.ftl").forward(request, response);
            }
            else if(action.equals("seasonTrapsiteFormSubmit"))
            {
                if(!SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.BARKBEETLE_ADMIN, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
                try
                {
                    FormValidation formValidation = FormValidator.validateForm("modules/barkbeetle/seasonTrapsiteForm", request, getServletContext());
                    if(formValidation.isValid())
                    {
                        SeasonTrapsite trapsite = (request.getParameter("seasonTrapsiteId") != null && Integer.valueOf(request.getParameter("seasonTrapsiteId")) > 0) ? 
                                BarkbeetleBean.getInstance().getSeasonTrapsite(Integer.valueOf(request.getParameter("seasonTrapsiteId")))
                                : new SeasonTrapsite();
                        trapsite.setSeason(formValidation.getFormField("season").getValueAsInteger());
                        trapsite.setTrapsiteType(BarkbeetleBean.getInstance().getTrapsiteType(formValidation.getFormField("trapsiteTypeId").getValueAsInteger()));
                        
                        trapsite.setCountyNo(formValidation.getFormField("countyNo").isEmpty() ? null : formValidation.getFormField("countyNo").getValueAsInteger());
                        trapsite.setCountyName(formValidation.getFormField("countyName").getWebValue());
                        trapsite.setMunicipalityNo(formValidation.getFormField("municipalityNo").isEmpty() ? null : formValidation.getFormField("municipalityNo").getValueAsInteger());
                        trapsite.setMunicipalityName(formValidation.getFormField("municipalityName").getWebValue());
                        trapsite.setPropertyNo(formValidation.getFormField("propertyNo").isEmpty() ? null : formValidation.getFormField("propertyNo").getValueAsInteger());
                        trapsite.setPropertySectionNo(formValidation.getFormField("propertySectionNo").isEmpty() ? null : formValidation.getFormField("propertySectionNo").getValueAsInteger());
                        trapsite.setOwnerName(formValidation.getFormField("ownerName").getWebValue());
                        trapsite.setOwnerPhone(formValidation.getFormField("ownerPhone").getWebValue());
                        trapsite.setDateInstalled(formValidation.getFormField("dateInstalled").isEmpty() ? null : formValidation.getFormField("dateInstalled").getValueAsDate());
                        trapsite.setInstallationRemarks(formValidation.getFormField("installationRemarks").getWebValue());
                        trapsite.setUserId(SessionControllerGetter.getUserBean().getVipsLogicUser(formValidation.getFormField("userId").getValueAsInteger()));
                        
                        // Handling the GIS
                        Point p2d = formValidation.getFormField("gisGeom").getValueAsPointWGS84();
                        CoordinateXYZ coordinate = new CoordinateXYZ(p2d.getX(), p2d.getY(),0.0);
                        try
                        {
                            Double altitude = formValidation.getFormField("altitude").getValueAsDouble();
                            coordinate = new CoordinateXYZ(p2d.getX(), p2d.getY(),altitude);
                        }
                        catch(NullPointerException | NumberFormatException ex) {}
                        GISUtil gisUtil = new GISUtil();
                        Point p3d = gisUtil.createPointWGS84(coordinate);
                        trapsite.setGisGeom(p3d);
                        
                        trapsite = BarkbeetleBean.getInstance().storeSeasonTrapsite(trapsite);
                        
                        // Redirect to form
                        response.sendRedirect(Globals.PROTOCOL + "://"
                                + ServletUtil.getServerName(request)
                                + "/barkbeetle?messageKey=storeOK&action=editSeasonTrapsite&seasonTrapsiteId=" + trapsite.getSeasonTrapsiteId()
                        );
                    }
                    else
                    {
                        response.sendError(500, formValidation.getValidationMessages());
                    }
                }
                catch(FormValidationException ex)
                {
                    response.sendError(500, ExceptionUtil.getStackTrace(ex));
                }
            }
            else if(action.equals("registerData"))
            {
                SeasonTrapsite trapsite = request.getParameter("seasonTrapsiteId") != null ? 
                        BarkbeetleBean.getInstance().getSeasonTrapsite(Integer.valueOf(request.getParameter("seasonTrapsiteId")))
                        : null;
                if(trapsite != null)
                {
                    request.setAttribute("userIsAdmin",SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.BARKBEETLE_ADMIN, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN, VipsLogicRole.SUPERUSER));
                    request.setAttribute("seasonTrapsite", trapsite);
                    Set<Integer> missingWeeks = new HashSet(trapsite.getTrapsiteType().getTrapsiteTypeId().equals(TrapsiteType.TRAPSITE_TYPE_STANDARD)?
                            Arrays.asList(new Integer[]{21,24,28,33})
                            : Arrays.asList(new Integer[]{21,24,28,33,37}));
                    //request.setAttribute("weeks", weeks);
                    request.setAttribute("traps", trapsite.getTrapsiteType().getTrapsiteTypeId().equals(TrapsiteType.TRAPSITE_TYPE_STANDARD)? 4 : 2 );
                    List<TrapsiteRegistration> trapsiteReg = new ArrayList(trapsite.getTrapsiteRegistrationCollection());
                    request.setAttribute("firstTimeReg", trapsiteReg.isEmpty());
                    
                    // Finding which registrations are present, removing them from "missing" collection
                    trapsiteReg.forEach(r -> {
                        missingWeeks.remove(r.getTrapsiteRegistrationPK().getWeek());
                    });
                    // Adding the missing
                    trapsiteReg.addAll(missingWeeks.stream().map(w->{
                        TrapsiteRegistration tr = new TrapsiteRegistration();
                        tr.setTrapsiteRegistrationPK(new TrapsiteRegistrationPK(trapsite.getSeasonTrapsiteId(), w));
                        return tr;
                    }).collect(Collectors.toList()));
                    Collections.sort(trapsiteReg);
                    request.setAttribute("trapsiteRegistrations", trapsiteReg);
                    // Setting the current week - last week for enabled registrations (future reg. not allowed)
                    WeekFields weekFields = WeekFields.of(Locale.getDefault());
                    request.setAttribute("currentWeek", LocalDate.ofInstant(SystemTime.getSystemTime().toInstant(),ZoneId.of("Europe/Oslo")).get(weekFields.weekOfWeekBasedYear()));
                    //request.setAttribute("currentWeek", 32); // TEST!
                    request.setAttribute("registrationStatusTypes", BarkbeetleBean.getInstance().getRegistrationStatusTypes());
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.getRequestDispatcher("/modules/barkbeetle/barkbeetleTrapsiteRegistrationForm.ftl").forward(request, response);
                }
                else
                {
                    response.sendError(Response.Status.BAD_REQUEST.getStatusCode(),"Kan ikke registrere data, fordi fellelokalitet med id=" + request.getParameter("seasonTrapsiteId") + " ikke ble funnet.");
                }
            }
            else if(action.equals("trapsiteRegistrationFormSubmit"))
            {
                SeasonTrapsite trapsite = request.getParameter("seasonTrapsiteId") != null ? 
                        BarkbeetleBean.getInstance().getSeasonTrapsite(Integer.valueOf(request.getParameter("seasonTrapsiteId")))
                        : null;
                
                if(trapsite != null)
                {
                    try
                    {
                        // Build all objects, send to bean for storage
                        // If date is set, the row is valid for storage
                        List<TrapsiteRegistration> registrations = new ArrayList();
                        request.getParameterNames().asIterator().forEachRemaining(parameterName->{
                            if(parameterName.contains("dateRegistration") && !request.getParameter(parameterName).isBlank())
                            {
                                Integer week = Integer.valueOf(parameterName.split("_")[0]);
                                TrapsiteRegistration tr = new TrapsiteRegistration();
                                tr.setTrapsiteRegistrationPK(new TrapsiteRegistrationPK(trapsite.getSeasonTrapsiteId(), week));
                                tr.setDateRegistration(Date.from(LocalDate.parse(request.getParameter(parameterName)).atStartOfDay(ZoneId.systemDefault()).toInstant()));
                                String trap1 = request.getParameter(week + "_trap1");
                                tr.setTrap1(!trap1.equals("M") && ! trap1.isBlank() ? Integer.valueOf(trap1) : null);
                                String trap2 = request.getParameter(week + "_trap2");
                                tr.setTrap2(!trap2.equals("M") && ! trap2.isBlank() ? Integer.valueOf(trap2) : null);
                                String trap3 = request.getParameter(week + "_trap3");
                                tr.setTrap3(!trap3.equals("M") && ! trap3.isBlank() ? Integer.valueOf(trap3) : null);
                                String trap4 = request.getParameter(week + "_trap4");
                                tr.setTrap4(!trap4.equals("M") && ! trap4.isBlank() ? Integer.valueOf(trap4) : null);

                                tr.setTrap1Remarks(request.getParameter(week + "_trap1Remarks"));
                                tr.setTrap2Remarks(request.getParameter(week + "_trap2Remarks"));
                                tr.setTrap3Remarks(request.getParameter(week + "_trap3Remarks"));
                                tr.setTrap4Remarks(request.getParameter("_trap4Remarks"));
                                
                                String registrationStatusTypeIdFieldValue = request.getParameter(week + "_registrationStatusTypeId");
                                if(registrationStatusTypeIdFieldValue != null && ! registrationStatusTypeIdFieldValue.isBlank())
                                {
                                    tr.setRegistrationStatusTypeId(Integer.valueOf(registrationStatusTypeIdFieldValue));
                                }
                                else
                                {
                                    tr.setRegistrationStatusTypeId(RegistrationStatusType.NOT_EVALUATED); // Default not evaluated
                                }
                                
                                registrations.add(tr);
                            }
                        });
                        BarkbeetleBean.getInstance().storeTrapsiteRegistrations(trapsite.getSeasonTrapsiteId(), registrations);
                        // Redirect to form
                        response.sendRedirect(Globals.PROTOCOL + "://"
                                + ServletUtil.getServerName(request)
                                + "/barkbeetle?messageKey=storeOK&action=registerData&seasonTrapsiteId=" + trapsite.getSeasonTrapsiteId()
                        );
                    }
                    catch(NumberFormatException ex)
                    {
                        response.sendError(500, ex.getMessage());
                    }
                    
                }
                else
                {
                    response.sendError(Response.Status.BAD_REQUEST.getStatusCode(),"Kan ikke registrere data, fordi fellelokalitet med id=" + request.getParameter("seasonTrapsiteId") + " ikke ble funnet.");
                }
            }
        }
        else
        {
            response.sendError(403,"Access not authorized"); // HTTP Forbidden
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
