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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import no.nibio.vips.gis.GISUtil;
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
     * Get the list of trapsites for the given season
     * @param season
     * @return 
     */
    public List<SeasonTrapsite> getSeasonTrapsiteCandidates(Integer season){
        return em.createNamedQuery("SeasonTrapsite.findUnactivatedBySeason")
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

    /**
     * Replaces the old set with a new one
     * @param seasonTrapsiteId
     * @param registrations
     * @return
     */
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

    public Integer getFirstAvailableSeason() {
        return (Integer) em.createNativeQuery("SELECT MIN(season) FROM barkbeetle.season_trapsite;").getSingleResult();
    }
    
    /**
     * This effectively makes a copy of all trap sites of one season to another. Registrations are not kept
     * @param fromSeason
     * @param toSeason
     */
    public void copySeasonTrapsites(Integer fromSeason, Integer toSeason)
    {
    	em.createNativeQuery("INSERT INTO barkbeetle.season_trapsite("
    			+ "season,"
    			+ "gis_geom,"
    			+ "trapsite_type,"
    			+ "county_no,"
    			+ "county_name,"
    			+ "municipality_no,"
    			+ "municipality_name,"
    			+ "county2012_no,"
    			+ "county2012_name,"
    			+ "municipality2012_no,"
    			+ "municipality2012_name,"
    			+ "user_id,"
    			+ "activated"
    			+ ")\n"
    			+ "SELECT "
    			+ ":toSeason,"
    			+ "gis_geom,"
    			+ "trapsite_type,"
    			+ "county_no,"
    			+ "county_name,"
    			+ "municipality_no,"
    			+ "municipality_name,"
    			+ "county2012_no,"
    			+ "county2012_name,"
    			+ "municipality2012_no,"
    			+ "municipality2012_name,"
    			+ "user_id,"
    			+ "FALSE \n"
    			+ "FROM barkbeetle.season_trapsite "
    			+ "WHERE season=:fromSeason"
    			)
    	.setParameter("fromSeason", fromSeason)
    	.setParameter("toSeason", toSeason)
    	.executeUpdate();
    	
    }

	public void setMaintenanceCompleted(Integer seasonTrapsiteId, boolean isMaintenanceCompleted) {
		SeasonTrapsite site = em.find(SeasonTrapsite.class, seasonTrapsiteId);
		site.setMaintenanceCompleted(isMaintenanceCompleted);
	}
	
	public String getCountyAverageCsv(Integer season)
	{
		Integer[] weeks = {21,24,28,33};
		List<SeasonTrapsite> seasonTrapsites = this.getSeasonTrapsites(season);
		String[] heading = {
				"Fylke",
				"21_avg",
				"21_min",
				"21_max",
				"24_avg",
				"24_min",
				"24_max",
				"24_tot_avg",
				"24_tot_min",
				"24_tot_max",
				"28_avg",
				"28_min",
				"28_max",
				"28_tot_avg",
				"28_tot_min",
				"28_tot_max",
				"33_avg",
				"33_min",
				"33_max",
				"33_tot_avg",
				"33_tot_min",
				"33_tot_max"
		};
		
		String csv = String.join(",", heading) + "\n";
		
		// Group by county
		Map<String, List<SeasonTrapsite>> sitesByCounty = new HashMap<>();
		
		for(SeasonTrapsite st:seasonTrapsites)
		{
			// Hard coded merging of two counties (Oslo and Akershus)
			String countyName = st.getCounty2012Name().toLowerCase().equals("oslo") || st.getCounty2012Name().toLowerCase().equals("akershus") ?
					"oslo og akershus"
					: st.getCounty2012Name().toLowerCase();
			List<SeasonTrapsite> sitesForCounty = sitesByCounty.get(countyName);
			if(sitesForCounty == null)
			{
				sitesForCounty = new ArrayList<>();
			}
			sitesForCounty.add(st);
			sitesByCounty.put(countyName,sitesForCounty);
		}
		
		for(String county:sitesByCounty.keySet())
		{
			Map<Integer,Double> countyWeekMin = new HashMap<>();
			Map<Integer,Double> countyWeekMax = new HashMap<>();
			Map<Integer,Double> countyWeekSum = new HashMap<>();
			Map<Integer,Integer> countyWeekSampleSize = new HashMap<>();
			
			// Max and min are aggregated from site avg (NOT from single BEKA traps)
			for(SeasonTrapsite st: sitesByCounty.get(county))
			{
				Collection<TrapsiteRegistration> regs = st.getTrapsiteRegistrationCollection();
				for(TrapsiteRegistration reg:regs)
				{
					if(reg.getRegistrationAverage() != null)
					{
						Integer week = reg.getTrapsiteRegistrationPK().getWeek();
						// Max
						countyWeekMax.put(week,countyWeekMax.get(week) != null ? Math.max(countyWeekMax.get(week), reg.getRegistrationAverage()): reg.getRegistrationAverage());
						// Min
						countyWeekMin.put(week,countyWeekMin.get(week) != null ? Math.min(countyWeekMin.get(week), reg.getRegistrationAverage()): reg.getRegistrationAverage());
						// Add to sum and sample size
						countyWeekSum.put(week, countyWeekSum.get(week) != null ? countyWeekSum.get(week) + reg.getRegistrationAverage() : reg.getRegistrationAverage());
						countyWeekSampleSize.put(week, countyWeekSampleSize.get(week) != null ? countyWeekSampleSize.get(week) + 1 : 1);
					}
				}
				
			}
			
			csv += county;
			// Hopefully I'll be able to restructure this to avoid all this repetition....ugh
			for(Integer week : weeks)
			{
				csv += "," + (countyWeekSum.get(week) != null ? Math.round(countyWeekSum.get(week) / countyWeekSampleSize.get(week)) : "")
					+  "," + (countyWeekMin.get(week) != null ? Math.round(countyWeekMin.get(week)) : "")
					+  "," + (countyWeekMax.get(week) != null ? Math.round(countyWeekMax.get(week)) : "");
				if(week == 24)
				{
					if(countyWeekSum.get(week) != null)
					{
						csv += "," + Math.round(countyWeekSum.get(21) + countyWeekSum.get(24)) / (countyWeekSampleSize.get(21) + countyWeekSampleSize.get(24))
						     + "," + Math.round(Math.min(countyWeekMin.get(21), countyWeekMin.get(24)))
						     + "," + Math.round(Math.max(countyWeekMin.get(21), countyWeekMax.get(24)));
					}
					else
					{
						csv += ",,,";
					}
				}
				if(week == 28)
				{
					if(countyWeekSum.get(week) != null)
					{
						csv += "," + Math.round(countyWeekSum.get(21) + countyWeekSum.get(24) + countyWeekSum.get(28)) / (countyWeekSampleSize.get(21) + countyWeekSampleSize.get(24) + countyWeekSampleSize.get(28))
						     + "," + Math.round(Math.min(countyWeekMin.get(21), Math.min(countyWeekMin.get(24),countyWeekMin.get(28))))
						     + "," + Math.round(Math.max(countyWeekMin.get(21), Math.max(countyWeekMax.get(24),countyWeekMax.get(28))));
					}
					else
					{
						csv += ",,,";
					}
				}
				if(week == 33)
				{
					if(countyWeekSum.get(week) != null)
					{
						csv += "," + Math.round(countyWeekSum.get(21) + countyWeekSum.get(24) + countyWeekSum.get(28) + countyWeekSum.get(33)) / (countyWeekSampleSize.get(21) + countyWeekSampleSize.get(24) + countyWeekSampleSize.get(28) + countyWeekSampleSize.get(33))
						     + "," + Math.round(Math.min(countyWeekMin.get(21), Math.min(countyWeekMin.get(24),Math.min(countyWeekMin.get(28),countyWeekMin.get(33)))))
						     + "," + Math.round(Math.max(countyWeekMin.get(21), Math.max(countyWeekMax.get(24),Math.max(countyWeekMax.get(28),countyWeekMax.get(33)))));
					}
					else
					{
						csv += ",,,";
					}
				}
			}
			csv += "\n";
		}
		
		
		return csv;
	}
	
	public String getSeasonTrapsitesReportCsv(Integer season)
	{
		Integer[] weeks = {21,24,28,33};
		List<SeasonTrapsite> seasonTrapsites = this.getSeasonTrapsites(season);
		String[] heading = {
				"Fylke",
				"Fylkesnr",
				"Kommune",
				"Kommunenr",
				"Registrant",
				"Tel-registrant",
				"Epost-registrant",
				"Eier",
				"Tel-eier",
				"Høyde o.h.",
				"UTM32N",
				"UTM32E",
				"Breddegrad",
				"Lengdegrad",
				"Uke",
				"Dato",
				"BEKA1",
				"BEKA2",
				"BEKA3",
				"BEKA4",
				"SNITT",
				"Angrep"
		};
		
		String csv = String.join(",", heading) + "\n";
		for(SeasonTrapsite st:seasonTrapsites) {
			org.locationtech.jts.geom.Coordinate UMT32coord = this.getUTMCoordinate(st.getLongitude(), st.getLatitude());
			String[] stInfo = {
					
					st.getCounty2012Name(),
					st.getCounty2012No() != null ? st.getCounty2012No().toString() : "",
					st.getMunicipality2012Name(),
					st.getMunicipality2012No() != null ? st.getMunicipality2012No().toString() : "",
					st.getUserId().getFirstName() + " " + st.getUserId().getLastName(),
					st.getUserId().getPhone(),
					st.getUserId().getEmail(),
					st.getOwnerName(),
					st.getOwnerPhone() != null ? st.getOwnerPhone() : "",
					st.getAltitude() != null ? st.getAltitude().toString() : "",
					UMT32coord != null ? String.valueOf(UMT32coord.getY()) : "",
					UMT32coord != null ? String.valueOf(UMT32coord.getX()) : "",
					st.getLatitude().toString(),
					st.getLongitude().toString()
			};
			String[] emptyCells = new String[14];
			Arrays.fill(emptyCells, "");
			Collection<TrapsiteRegistration> regs = st.getTrapsiteRegistrationCollection();
			Map<Integer, String[]> regsPerWeek = new HashMap<>();
			for(TrapsiteRegistration r:regs)
			{
				String[] stDataWeek = {
						r.getTrapsiteRegistrationPK().getWeek().toString(),
						r.getDateRegistration().toString(), // TODO Format
						r.getTrap1() != null ? r.getTrap1().toString() : "M",
						r.getTrap2() != null ? r.getTrap2().toString() : "M",
						r.getTrap3() != null ? r.getTrap3().toString() : "M",
						r.getTrap4() != null ? r.getTrap4().toString() : "M",
						r.getRegistrationAverage() != null ? r.getRegistrationAverage().toString() : ""
				};
				regsPerWeek.put(r.getTrapsiteRegistrationPK().getWeek(), stDataWeek);
			}
		
			String[][] trapSiteLines = new String[4][23];
			Integer counter = 0;
			for(Integer week:weeks)
			{
				List<String> trapSiteLine = new ArrayList(Arrays.asList(week.equals(21) ? stInfo:emptyCells)); 
				trapSiteLine.addAll(Arrays.asList(regsPerWeek.get(week) != null ? regsPerWeek.get(week) : new String[7]));
				csv += String.join(",",trapSiteLine.toArray(new String[0]));
				csv += "," + (week.equals(21) ? 
						st.getObservedAttacksDescription() != null ? st.getObservedAttacksDescription() : "" 
						: "") + "\n";
			}
			
		}
		
		return csv;
	}
	
	public org.locationtech.jts.geom.Coordinate getUTMCoordinate(Double longitude, Double latitude)
	{
		try {
			CoordinateReferenceSystem fromCRS = CRS.decode("EPSG:4326");
	        CoordinateReferenceSystem toCRS = CRS.decode("EPSG:2077");
	        MathTransform transform = CRS.findMathTransform(fromCRS, toCRS, true);
	        // For some reason the x/y coordinates have to be switched to make this work. Beats me!
	        return (org.locationtech.jts.geom.Coordinate) JTS.transform(new org.locationtech.jts.geom.Coordinate(latitude, longitude), null, transform);
		} catch (FactoryException | TransformException e) {
			e.printStackTrace();
			return null;
		}
	}
}
