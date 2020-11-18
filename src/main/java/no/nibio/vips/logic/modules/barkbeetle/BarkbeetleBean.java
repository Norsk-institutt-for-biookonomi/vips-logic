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

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import java.util.Calendar;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@LocalBean
@Stateless
public class BarkbeetleBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    /**
     * Get the list of trapsites for the given season
     * @param season
     * @return 
     */
    public List<SeasonTrapsite> getSeasonTrapsites(Integer season){
        return em.createNamedQuery("SeasonTrapsite.findBySeason")
                .setParameter("season", season)
                .getResultList();
    }
    
    /**
     * Get a specific trapsite
     * @param seasonTrapsiteId
     * @return 
     */
    public SeasonTrapsite getSeasonTrapsite(Integer seasonTrapsiteId)
    {
        return em.find(SeasonTrapsite.class, seasonTrapsiteId);
    }
    
    public SeasonTrapsite storeSeasonTrapsite(SeasonTrapsite seasonTrapsite)
    {
        if(seasonTrapsite.getSeasonTrapsiteId() == null)
        {
            em.persist(seasonTrapsite);
            return seasonTrapsite;
        }
        else
        {
            return em.merge(seasonTrapsite);
        }
    }
    
    public List<TrapsiteType> getTrapsiteTypes()
    {
        return em.createNamedQuery("TrapsiteType.findAll").getResultList();
    }
    
    public TrapsiteType getTrapsiteType(Integer trapsiteTypeId)
    {
        return em.find(TrapsiteType.class, trapsiteTypeId);
    }
    
    /**
     * Util method for getting this bean through JNDI
     * @return 
     */
    public static BarkbeetleBean getInstance()
    {
        try
        {
            InitialContext ic = new InitialContext();
            BarkbeetleBean retVal = (BarkbeetleBean) ic.lookup(SessionControllerGetter.getJndiPath(BarkbeetleBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + BarkbeetleBean.class.getSimpleName());
            return null;
        }
    }

    public List<TrapsiteRegistration> getRegistrationsForSite(SeasonTrapsite trapsite) {
        return em.createNamedQuery("TrapsiteRegistration.findBySeasonTrapsiteId")
                .setParameter("seasonTrapsiteId", trapsite.getSeasonTrapsiteId())
                .getResultList();
    }

    public List<RegistrationStatusType> getRegistrationStatusTypes() {
        return em.createNamedQuery("RegistrationStatusType.findAll").getResultList();
    }

    public SeasonTrapsite storeTrapsiteRegistrations(Integer seasonTrapsiteId, List<TrapsiteRegistration> registrations) {
        SeasonTrapsite trapsite = em.find(SeasonTrapsite.class, seasonTrapsiteId);
        trapsite.setTrapsiteRegistrationCollection(registrations);
        return em.merge(trapsite);
    }

    public Kml getSeasonTrapsitesKml(Integer season, Integer excludeSeasonTrapsiteId, String serverName) {
        String iconPath = Globals.PROTOCOL + "://" + serverName + "/images/modules/barkbeetle/";
        List<SeasonTrapsite> traps = this.getSeasonTrapsites(season);
        // Initialization
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
        .withName("Barkbillefellelokaliteter").withDescription("Barkbillefellelokaliteter for sesongen " + season);
        
        document.createAndAddStyle()
            .withId("trapsite_icon")
        .createAndSetIconStyle()
                .withScale(0.55)
                .createAndSetIcon()
                    .withHref(iconPath + "dot_blue.png");
        
        document.createAndAddStyle()
            .withId("trapsite_icon_highlighted")
        .createAndSetIconStyle()
                .withScale(0.55)
                .createAndSetIcon()
                    .withHref(iconPath + "trapsite.png");
        
        String styleUrl = "#trapsite_icon";
        GISEntityUtil gisUtil = new GISEntityUtil();
        traps.stream()
            .filter(trap -> !(excludeSeasonTrapsiteId != null && excludeSeasonTrapsiteId.equals(trap.getSeasonTrapsiteId())))
            .forEachOrdered(trap -> {
                final Placemark placemark = document.createAndAddPlacemark()
                        .withName(trap.getCountyName() + "/" + trap.getMunicipalityName())
                        .withDescription(
                                "<ul>"
                                + "<li>Eier: " + trap.getOwnerName() + ", tlf: " + trap.getOwnerPhone() + "</li>"
                                + "<li>Registrant: <a href='mailto:" + trap.getUserId().getEmail() + "'>" + trap.getUserId().getFirstName() + " " + trap.getUserId().getLastName() + "</a>, tlf: " + trap.getUserId().getPhone() + "</li>"
                                + "</ul>")
                        .withStyleUrl(styleUrl)
                        .withId(trap.getSeasonTrapsiteId().toString());

                final de.micromata.opengis.kml.v_2_2_0.Point point = placemark.createAndSetPoint();
                List<Coordinate> coord = point.createAndSetCoordinates();
                coord.add(gisUtil.getKMLCoordinateFromJTSCoordinate(trap.getGisGeom().getCoordinate()));
            });
        return kml;
    }
    
    /**
     * Returns the season's trapsites with their risk status included
     * @param season
     * @param serverName // For returning paths to the icons
     * @return 
     */
    public Kml getSeasonTrapsitesStatusKml(Integer season, String serverName) {
        String iconPath = Globals.PROTOCOL + "://" + serverName + "/images/modules/barkbeetle/";
        List<SeasonTrapsite> traps = this.getSeasonTrapsites(season);
        // Initialization
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
        .withName("Barkbillefellelokaliteter").withDescription("Barkbillefellelokaliteter for sesongen " + season);
        // Setting the icon anchor (bottom, middle of icon. Aligns with pinpoint)
        final Vec2 hotspot = new Vec2()
                .withX(0.5)
                .withXunits(Units.FRACTION)
                .withY(0)
                .withYunits(Units.FRACTION);
        
        LabelStyle noLabel = new LabelStyle().withScale(0.0);
        
        for(int i=0;i<=5;i++)
        {
            
            document.createAndAddStyle()
                .withId("status_type_" + i)
                .withLabelStyle(noLabel)
            .createAndSetIconStyle()
                    .withScale(1)
                    .withHotSpot(hotspot)
                    .createAndSetIcon()
                        .withHref(iconPath + "station_icon_status_" + i + ".png"); 
        }
        
        
        
        String styleUrl = "#status_type_";
        GISEntityUtil gisUtil = new GISEntityUtil();
        traps.forEach(trap -> {
            final Placemark placemark = document.createAndAddPlacemark()
                    .withName(trap.getCountyName() + "/" + trap.getMunicipalityName())
                    .withDescription(
                            "<ul>"
                            + "<li>Eier: " + trap.getOwnerName() + (trap.getOwnerPhone() == null || trap.getOwnerPhone().isBlank() ? "" : ", tlf: " + trap.getOwnerPhone() ) + "</li>"
                            + "<li>Registrant: <a href='mailto:" + trap.getUserId().getEmail() + "'>" + trap.getUserId().getFirstName() + " " + trap.getUserId().getLastName() + "</a>, tlf: " + trap.getUserId().getPhone() + "</li>"
                            + "</ul>")
                    .withStyleUrl(styleUrl + trap.getWarningStatus())
                    .withId(trap.getSeasonTrapsiteId().toString());
            
            final de.micromata.opengis.kml.v_2_2_0.Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(gisUtil.getKMLCoordinateFromJTSCoordinate(trap.getGisGeom().getCoordinate()));
        });
        return kml;
    }
    
    public Boolean deleteSeasonTrapsite(Integer seasonTrapsiteId)
    {
        SeasonTrapsite stToDelete = em.find(SeasonTrapsite.class, seasonTrapsiteId);
        if(stToDelete == null)
        {
            return false;
        }
        // Need to manually delete bivolt model calculations for this trap site
        em.createNativeQuery("DELETE FROM barkbeetle.season_trapsite_bivolt WHERE season_trapsite_id = :seasonTrapsiteId")
                .setParameter("seasonTrapsiteId", seasonTrapsiteId)
                .executeUpdate();
        em.remove(stToDelete);
        return true;
    }
}
