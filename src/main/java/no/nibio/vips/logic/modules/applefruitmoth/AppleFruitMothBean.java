/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
import java.util.Date;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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
    
    public List<ObservationSite> getObservationSitesWithClusterCountLastUpdate(){
        List<ObservationSite> sites = this.getObservationSites();
        Query qu = em.createNativeQuery("SELECT max(last_updated) FROM applefruitmoth.observation_site_point_season_data osd WHERE osd.observation_site_point_id IN (SELECT observation_site_point_id FROM applefruitmoth.observation_site_point WHERE observation_site_id = :observationSiteId)");
        sites.stream().forEach(site->{
            qu.setParameter("observationSiteId", site.getObservationSiteId());
            try
            {
                Date lastUpdated = (Date) qu.getSingleResult();
                site.setLastUpdatedClusterCount(lastUpdated);
            }
            catch(NoResultException ex){}
        });
        
        return sites;
    }

    public ObservationSite getObservationSite(Integer observationSiteId) {
        return em.find(ObservationSite.class, observationSiteId);
    }

    public Kml getObservationSitesKml(Integer season, Integer selectedObservationSiteId, String serverName, Boolean onlyPubliclyAvailable) {
        onlyPubliclyAvailable = onlyPubliclyAvailable != null ? onlyPubliclyAvailable : true;
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
            if(onlyPubliclyAvailable && !site.getPubliclyAvailable())
            {
                continue;
            }
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
