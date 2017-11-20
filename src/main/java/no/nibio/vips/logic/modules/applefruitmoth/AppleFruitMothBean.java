/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.applefruitmoth;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.nibio.vips.logic.util.Globals;

/**
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@LocalBean
@Stateless
public class AppleFruitMothBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    
    public List<ObservationSite> getObservationSites()
    {
        List<ObservationSite> sites = em.createNamedQuery("ObservationSite.findAll").getResultList();
        return sites;
    }

    public ObservationSite getObservationSite(Integer observationSiteId) {
        return em.find(ObservationSite.class, observationSiteId);
    }

    public Kml getObservationSitesKml(Integer season, Integer selectedObservationSiteId, String serverName) {
        String iconPath = Globals.PROTOCOL + "://" + serverName + "/public/images/";
        // Initialization
        final Vec2 hotspot = new Vec2()
                .withX(0.5)
                .withXunits(Units.FRACTION)
                .withY(0)
                .withYunits(Units.FRACTION);
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
                .withName("Rognebærmøllstasjoner").withDescription("Oversikt over varslingssesongen " + season);
        
        // Adding icons for warning statuses
        document.createAndAddStyle()
            .withId("warning_type_0")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_0.png");
        
        document.createAndAddStyle()
            .withId("selected_warning_type_0")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "selectedstation_icon_status_0.png");
        
        document.createAndAddStyle()
            .withId("warning_type_1")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_1.png");
        
        document.createAndAddStyle()
            .withId("selected_warning_type_1")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "selectedstation_icon_status_1.png");
        
        document.createAndAddStyle()
            .withId("warning_type_2")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_2.png");
        
        document.createAndAddStyle()
            .withId("selected_warning_type_2")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "selectedstation_icon_status_2.png");
        
        document.createAndAddStyle()
            .withId("warning_type_3")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_3.png");
        
        document.createAndAddStyle()
            .withId("selected_warning_type_3")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "selectedstation_icon_status_3.png");
        
        document.createAndAddStyle()
            .withId("warning_type_4")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "station_icon_status_4.png");
        
        document.createAndAddStyle()
            .withId("selected_warning_type_4")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "selectedstation_icon_status_4.png");
        
        List<ObservationSite> sites = this.getObservationSites();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for(ObservationSite site:sites)
        {
            Integer warningStatus = 0;
            String remarks = "Varsel ikke oppdatert";
            ObservationSiteSeasonCommonData commonData = site.getCommonDataForSeason(season);
            if(commonData != null)
            {
                warningStatus = commonData.getWarningStatus();
                String lastUpdatedStr = "";
                if(commonData.getLastUpdated() != null)
                {
                    lastUpdatedStr = "[" + format.format(commonData.getLastUpdated()) + "] ";
                }
                remarks = lastUpdatedStr;
            }
            
            Data infoUri = new Data(String.valueOf(warningStatus));
            infoUri.setName("warningStatus");
            List<Data> dataList = new ArrayList<>();
            dataList.add(infoUri);
            Data siteName = new Data(site.getObservationSiteName());
            siteName.setName("siteName");
            dataList.add(siteName);
            
            ExtendedData extendedData = document.createAndSetExtendedData()
                    .withData(dataList);
            
            final Placemark placemark = document.createAndAddPlacemark()
                    //.withName(site.getObservationSiteName())
                    .withDescription(remarks)
                    .withStyleUrl("#" + (site.getObservationSiteId().equals(selectedObservationSiteId) ? "selected_":"") + "warning_type_" + warningStatus)
                    .withId(site.getObservationSiteId().toString())
                    .withExtendedData(extendedData);
            
            
            
            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(new Coordinate(
                    site.getGisId().getGisGeom().getCoordinate().x,
                    site.getGisId().getGisGeom().getCoordinate().y,
                    0
                )
            );
            
        }
        return kml;
    }
    
    /*
    public Kml getObservationSitePointsKml(Integer observationSiteId, String serverName) {
        
        ObservationSite observationSite = em.find(ObservationSite.class, observationSiteId);
        
        String iconPath = Globals.PROTOCOL + "://" + serverName + "/public/images/";
        // Initialization
        final Vec2 hotspot = new Vec2()
                .withX(0.5)
                .withXunits(Units.FRACTION)
                .withY(0)
                .withYunits(Units.FRACTION);
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
                .withName("Rognebærmøllstasjon-punkter").withDescription("Oversikt over punkter for rognebærmøllstasjonen " + observationSite.getObservationSiteName());
        
        // Adding icons for warning statuses
        document.createAndAddStyle()
            .withId("observation_site_point_icon")
        .createAndSetIconStyle()
                .withScale(1)
                .withHotSpot(hotspot)
                .createAndSetIcon()
                    .withHref(iconPath + "dot_blue.png");
        
        Set<ObservationSitePoint> sitePoints = observationSite.getObservationSitePointSet();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        for(ObservationSitePoint sitePoint:sitePoints)
        {
            
            final Placemark placemark = document.createAndAddPlacemark()
                    .withName(sitePoint.getPointName())
                    .withStyleUrl("#observation_site_point_icon")
                    .withId(sitePoint.getObservationSitePointId().toString());
            
            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(new Coordinate(
                    sitePoint.getGisId().getGisGeom().getCoordinate().x,
                    sitePoint.getGisId().getGisGeom().getCoordinate().y,
                    0
            ));
            
        }
        return kml;
    }*/

    public ObservationSite storeObservationSite(ObservationSite observationSite) {
        return em.merge(observationSite);
    }

    public ObservationSitePoint storeObservationSitePoint(ObservationSitePoint sitePoint)
    {
        return em.merge(sitePoint);
    }
    
    public ObservationSitePointSeasonData storeObservationSitePointSeasonData(ObservationSitePointSeasonData seasonData)
    {
        return em.merge(seasonData);
    }

    public void storeObservationSitePointSeasonCommonData(ObservationSiteSeasonCommonData seasonCommonData) {
        em.merge(seasonCommonData);
    }
}
