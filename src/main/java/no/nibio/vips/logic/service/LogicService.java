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

package no.nibio.vips.logic.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import org.apache.commons.validator.routines.EmailValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import com.webcohesion.enunciate.metadata.Facet;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NonUniqueResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.controller.session.DeleteUserException;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.PointOfInterestType;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.CSVPrintUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.vips.util.SolarRadiationUtil;
import no.nibio.web.forms.FormValidationException;

/**
 * @copyright 2013-2023 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Path("")
public class LogicService {

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    UserBean userBean;
    @Inject
    PointOfInterestBean pointOfInterestBean;
    

    /**
     * Check if a proposed password meets the requirements configured by Passay
     * @param password
     * @return 
     */
    @GET
    @Path("evaluatepassword/{password}")
    @Produces("text/plain;charset=UTF-8")
    @Facet("restricted")
    public Response evaluatePassord(@PathParam("password") String password)
    {
        ULocale currentLocale = SessionLocaleUtil.getCurrentLocale(httpServletRequest);
        try
        {
            // Invalid passwords always cause a PasswordValidationException to be thrown
            Boolean isPasswordValid = userBean.isPasswordValid(password, currentLocale);
            return Response.ok().entity(isPasswordValid).build();
            
        }
        catch(PasswordValidationException ex)
        {
            return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }

    /**
     * Get a list of weather stations for a given organization
     * @param excludeWeatherStationId Exclude this weather station from the KML
     * @param highlightWeatherStationId Show highlight icon for this weather station
     * @param organizationId
     * @return a KML with weather stations for an organization
     */
    @GET
    @Path("weatherstations/kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getWeatherStations(
            @QueryParam("excludeWeatherStationId") Integer excludeWeatherStationId, 
            @QueryParam("highlightWeatherStationId") Integer highlightWeatherStationId, 
            @PathParam("organizationId") Integer organizationId
    )
    {
        Kml retVal = pointOfInterestBean.getPoisForOrganization(organizationId, excludeWeatherStationId, highlightWeatherStationId, Globals.SERVER_NAME, SessionLocaleUtil.getI18nBundle(httpServletRequest), PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION);
        return Response.ok().entity(retVal).build();
    }
    
    /**
     * Get a KML list of locations (pois) for a given organization
     * @param excludePoiId 
     * @param highlightPoiId use this if you want to highlight a specific POI. Should be 
     * used in conjunction with excludePoiId
     * @param organizationId
     * @return KML
     */
    @GET
    @Path("pois/kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getPois(
            @QueryParam("excludePoiId") Integer excludePoiId, 
            @QueryParam("highlightPoiId") Integer highlightPoiId, 
            @PathParam("organizationId") Integer organizationId
    )
    {
        Kml retVal = pointOfInterestBean.getPoisForOrganization(organizationId, excludePoiId, highlightPoiId, Globals.SERVER_NAME, SessionLocaleUtil.getI18nBundle(httpServletRequest), null);
        return Response.ok().entity(retVal).build();
    }
    

    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return 
     */
    @GET
    @Path("weather/calculation/solarradiation/json")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getCalculatedSolarRadiationAtLocationAndTimeJSON(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude, 
            @QueryParam("startTime") String startTimeStr, 
            @QueryParam("endTime") String endTimeStr, 
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("logIntervalId") Integer logIntervalId
    )
    {
        try
        {

            List<WeatherObservation> radiationValues = getCalculatedSolarRadiationAtLocationAndTime (
                    latitude,
                    longitude,
                    startTimeStr,
                    endTimeStr,
                    timeZoneStr,
                    logIntervalId
            );
            return Response.ok().entity(radiationValues).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex).build();
        }
    }
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return 
     */
    @GET
    @Path("weather/calculation/solarradiation/csv")
    @Produces("text/csv;charset=UTF-8")
    @Facet("restricted")
    public Response getCalculatedSolarRadiationAtLocationAndTimeCSV(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude, 
            @QueryParam("startTime") String startTimeStr, 
            @QueryParam("endTime") String endTimeStr, 
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("logIntervalId") Integer logIntervalId
    )
    {
        try
        {

            List<WeatherObservation> radiationValues = getCalculatedSolarRadiationAtLocationAndTime (
                    latitude,
                    longitude,
                    startTimeStr,
                    endTimeStr,
                    timeZoneStr,
                    logIntervalId
            );
            //return Response.ok().entity(radiationValues).build();
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            return Response.ok(new CSVPrintUtil().printWeatherObservations(radiationValues, timeZone, "\t")).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex).build();
        }
    }
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return
     * @throws ParseException 
     */
    private List<WeatherObservation> getCalculatedSolarRadiationAtLocationAndTime (
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId
    ) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            Date startTime = format.parse(startTimeStr);
            Date endTime = format.parse(endTimeStr);
            logIntervalId = logIntervalId == null ? WeatherObservation.LOG_INTERVAL_ID_1H : logIntervalId;
            SolarRadiationUtil srUtil = new SolarRadiationUtil();
            return srUtil.getCalculatedSolarRadiation(latitude, longitude, startTime, endTime, timeZone, logIntervalId);
    }
    

    /**
     * TODO: Should only be available for trusted clients (like VIPSWeb)
     * @param userUUID
     * @return 
     * @ignore
     */
    @GET
    @Path("user/uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getVipsLogicUserByUUID(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
            if(user != null)
            {
                return Response.ok().entity(user).build();
            }
            else
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch(IllegalArgumentException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * TODO: Must be authenticated or not??
     * @param userUUID
     * @return 
     * @ignore
     */
    @DELETE
    @Path("user/uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response deleteVipsLogicUserUUID(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            userBean.deleteUserUuid(uUUID);
            return Response.ok().build();
        }
        catch(IllegalArgumentException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    /**
     * Registers a user and grants limited access to certain functionalities in the VIPSLogic system:
     * <ul>
     *     <li>Adding observations - default not approved</li>
     *     <li>Adding POIs (Points Of Interest)</li>
     * </ul>
     * The user must be approved
     * @param userInfoBody
     * @return
     */
    @POST
    @Path("user/register")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response registerNewLimitedUser(String userInfoBody)
    {

        try {
            HashMap<String, Object> userInfo = new ObjectMapper().readValue(userInfoBody, new TypeReference<HashMap<String, Object>>() {
            });
            // Input control
            List<String> errorMessages = new ArrayList<>();
            // Email

            String email = ((String) userInfo.get("email")).toLowerCase();
            // Set?
            if(email == null || email.isBlank())
            {
                errorMessages.add("Email must be set");
            }
            // Must be valid email
            else if(!EmailValidator.getInstance().isValid(email))
            {
                errorMessages.add(email + " is not a valid email address");
            }
            else
            {
                // Must be unique
                Boolean emailAlreadyInUse = false;
                try {
                    VipsLogicUser foundUser = userBean.getUserByEmail(email);
                    emailAlreadyInUse = (foundUser != null);
                } catch (NonUniqueResultException ex) {
                    emailAlreadyInUse = true;
                }
                if (emailAlreadyInUse) {
                    errorMessages.add("Email " + email + " is already in use");
                }
            }

            // Username
            String username = (String) userInfo.get("username");
            // Set?
            if(username == null || username.isBlank())
            {
                errorMessages.add("Username must be set");
            }
            else
            {
                // Existing username?
                Boolean usernameExists = false;
                try
                {
                    VipsLogicUser foundUser = userBean.getUser(username, UserAuthenticationType.TYPE_PASSWORD);
                    usernameExists = (foundUser != null);
                }
                catch(NonUniqueResultException ex)
                {
                    usernameExists = true;
                }
                if(usernameExists)
                {
                    errorMessages.add("Username " + username  + " already exists");
                }
            }

            // First name
            String firstName = (String) userInfo.get("firstName");
            if(firstName == null || firstName.isBlank())
            {
                errorMessages.add("First name must be set");
            }

            // Last name
            String lastName = (String) userInfo.get("lastName");
            if(lastName == null || lastName.isBlank())
            {
                errorMessages.add("Last name must be set");
            }


            // Password
            String password = (String) userInfo.get("password");
            if(password == null || password.isBlank())
            {
                errorMessages.add("Password must be set");
            }

            if(errorMessages.size() > 0)
            {
                Map<String, List<String>> errorMsg = Map.of("errorMessages",errorMessages);
                return Response.status(Status.BAD_REQUEST).entity(errorMsg).build();
            }

            VipsLogicUser user = new VipsLogicUser();
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(email.trim());
            user.setPhoneCountryCode((String) userInfo.get("phoneCountryCode"));
            user.setPreferredLocale((String) userInfo.get("preferredLocale"));
            user.setOrganizationId(userBean.getOrganization((Integer) userInfo.get("organizationId")));
            user.setApprovalApplication("Registered in app");
            user.setUserStatusId(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION);
            // Add observer role
            user.setVipsLogicRoles(Set.of(userBean.getVipsLogicRole(VipsLogicRole.OBSERVER)));
            // Set user authentication
            UserAuthenticationType uat = userBean.createUserAuthenticationTypeInstance(UserAuthenticationType.TYPE_PASSWORD);
            UserAuthentication ua = new UserAuthentication();
            ua.setUserAuthenticationType(uat);
            ua.setUsername(username.trim());
            ua.setPassword(userBean.getMD5EncryptedString(password.trim()));
            userBean.storeUserFirstTime(user, ua);
            userBean.sendUserEmailVerification(user, SessionLocaleUtil.getI18nBundle(httpServletRequest), ServletUtil.getServerName(httpServletRequest));

            return Response.status(Status.OK).entity(user).build();
        }
        catch(FormValidationException | IOException ex)
        {
            return Response.status(Status.BAD_REQUEST).entity("INPUT ERROR: " + ex.getMessage()).build();
        }
    }

    /**
     * Allows a user to delete their account
     * @param keepData if true, move all data to default user.
     * @return
     */
    @DELETE
    @Path("user/deleteme")
    public Response deleteMe(@QueryParam("keepData") Boolean keepData) {
        // Authentication
        // Either valid UUID or session
        VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
        if(user == null)
        {
            user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        }
        if(user == null)
        {
            return Response.status(Status.UNAUTHORIZED).entity("You are not authorized to perform this operation").build();
        }

        // If it's an archive user, do NOT delete it!
        if(user.getOrganizationId().getArchiveUser() != null && user.getOrganizationId().getArchiveUser().getUserId().equals(user.getUserId()))
        {
            return Response.status(Status.BAD_REQUEST).entity("User is an archive user for organization " + user.getOrganizationId().getOrganizationName() + ". Can't delete it").build();
        }
        try {
            if (keepData != null && keepData) {
                // Get default user for organization
                VipsLogicUser archiveUser = user.getOrganizationId().getArchiveUser();
                if (archiveUser == null) {
                    return Response.status(Status.BAD_REQUEST).entity("Your organization " + user.getOrganizationId().getOrganizationName() + " has not defined a default user for archiving your data. Please contact your systems administrator to fix this.").build();
                }
                userBean.transferUserResources(user, archiveUser);
            } else {
                userBean.deleteUserResources(user);
            }
            // Delete the user
            userBean.deleteUser(user);
            return Response.status(Status.NO_CONTENT).build();
        }
        catch(DeleteUserException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }
    
}
