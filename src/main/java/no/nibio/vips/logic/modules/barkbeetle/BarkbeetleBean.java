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

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.LabelStyle;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Units;
import de.micromata.opengis.kml.v_2_2_0.Vec2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.EJB;

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

import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.messaging.MessageRecipient;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.messaging.UniversalMessage;
import no.nibio.vips.logic.messaging.UniversalMessageFormat;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SimpleMailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@LocalBean
@Stateless
public class BarkbeetleBean {

    private static Logger LOGGER = LoggerFactory.getLogger(BarkbeetleBean.class);

    @PersistenceContext(unitName = "VIPSLogic-PU")
    EntityManager em;

    public static final List<Integer> TRAP_EMPTYING_WEEKS = Arrays.asList(new Integer[]{21, 24, 28, 33});
    public static final Locale NORGE_MITT_NORGE = new Locale("nb", "NO");

    @EJB
    UserBean userBean;

    @EJB
    MessagingBean messagingBean;

    public static BarkbeetleBean getEJBInstance() {
        try {
            InitialContext ic = new InitialContext();
            BarkbeetleBean retVal = (BarkbeetleBean) ic.lookup(SessionControllerGetter.getJndiPath(BarkbeetleBean.class));

            return retVal;
        } catch (NamingException ne) {
            System.out.println("Could not find " + BarkbeetleBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(BarkbeetleBean.class));
            return null;
        }
    }

    /**
     * Get the list of trapsites for the given season
     *
     * @param season
     * @return
     */
    public List<SeasonTrapsite> getSeasonTrapsites(Integer season) {
        return em.createNamedQuery("SeasonTrapsite.findBySeason")
                .setParameter("season", season)
                .getResultList();
    }

    /**
     * Get the list of trapsites for the given season
     *
     * @param season
     * @return
     */
    public List<SeasonTrapsite> getSeasonTrapsiteCandidates(Integer season) {
        return em.createNamedQuery("SeasonTrapsite.findUnactivatedBySeason")
                .setParameter("season", season)
                .getResultList();
    }

    /**
     * Get a specific trapsite
     *
     * @param seasonTrapsiteId
     * @return
     */
    public SeasonTrapsite getSeasonTrapsite(Integer seasonTrapsiteId) {
        return em.find(SeasonTrapsite.class, seasonTrapsiteId);
    }

    public SeasonTrapsite storeSeasonTrapsite(SeasonTrapsite seasonTrapsite) {
        if (seasonTrapsite.getSeasonTrapsiteId() == null) {
            em.persist(seasonTrapsite);
            return seasonTrapsite;
        } else {
            return em.merge(seasonTrapsite);
        }
    }

    public List<TrapsiteType> getTrapsiteTypes() {
        return em.createNamedQuery("TrapsiteType.findAll").getResultList();
    }

    public TrapsiteType getTrapsiteType(Integer trapsiteTypeId) {
        return em.find(TrapsiteType.class, trapsiteTypeId);
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
     *
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
     *
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

        for (int i = 0; i <= 5; i++) {

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
                            + "<li>Eier: " + trap.getOwnerName() + (trap.getOwnerPhone() == null || trap.getOwnerPhone().isBlank() ? "" : ", tlf: " + trap.getOwnerPhone()) + "</li>"
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

    public Boolean deleteSeasonTrapsite(Integer seasonTrapsiteId) {
        SeasonTrapsite stToDelete = em.find(SeasonTrapsite.class, seasonTrapsiteId);
        if (stToDelete == null) {
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
     * This effectively makes a copy of all trap sites of one season to another.
     * Registrations are not kept
     *
     * @param fromSeason
     * @param toSeason
     */
    public void copySeasonTrapsites(Integer fromSeason, Integer toSeason) {
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

    public String getCountyAverageCsv(Integer season) {
        Boolean DEBUG = false;
        Integer[] weeks = {21, 24, 28, 33};
        List<SeasonTrapsite> seasonTrapsites = this.getSeasonTrapsites(season);
        String[] heading = {
            "Fylke",
            "21_avg",
            "21_min",
            "21_max",
            "24_avg",
            "24_min",
            "24_max",
            "24_akk_avg",
            "24_akk_min",
            "24_akk_max",
            "28_avg",
            "28_min",
            "28_max",
            "28_akk_avg",
            "28_akk_min",
            "28_akk_max",
            "33_avg",
            "33_min",
            "33_max",
            "33_akk_avg",
            "33_akk_min",
            "33_akk_max"
        };

        String csv = String.join(",", heading) + "\n";

        // Group by county
        Map<String, List<SeasonTrapsite>> sitesByCounty = new HashMap<>();

        Map<String, Map<Integer, List<Long>>> countyWeekData = new HashMap<>();
        Integer latestRegisterWeek = 0;
        for (SeasonTrapsite st : seasonTrapsites) {
            // Hard coded merging of two counties (Oslo and Akershus)
            String countyName = st.getCounty2012Name().toLowerCase().equals("oslo") || st.getCounty2012Name().toLowerCase().equals("akershus")
                    ? "oslo og akershus"
                    : st.getCounty2012Name().toLowerCase();
            List<SeasonTrapsite> sitesForCounty = sitesByCounty.get(countyName);
            if (sitesForCounty == null) {
                sitesForCounty = new ArrayList<>();
            }
            sitesForCounty.add(st);
            sitesByCounty.put(countyName, sitesForCounty);
            for (TrapsiteRegistration reg : st.getTrapsiteRegistrationCollection()) {
                latestRegisterWeek = Math.max(latestRegisterWeek, reg.getTrapsiteRegistrationPK().getWeek());
            }
        }

        if (DEBUG) {
            System.out.println("Latest week of registration: " + latestRegisterWeek);
        }

        for (String county : sitesByCounty.keySet()) {
            if (DEBUG) {
                System.out.println(county);
            }
            // Organizing the data
            Map<Integer, List<Long>> weekTrapsiteResults = new HashMap<>();
            Map<Integer, List<Long>> accWeekTrapsiteResults = new HashMap<>();
            for (Integer week : weeks) {
                List<Long> data = new ArrayList<>();
                weekTrapsiteResults.put(week, data);
                List<Long> accData = new ArrayList<>();
                accWeekTrapsiteResults.put(week, accData);
            }

            for (SeasonTrapsite st : sitesByCounty.get(county)) {
                List<TrapsiteRegistration> regs = new ArrayList<>(st.getTrapsiteRegistrationCollection());
                // TODO: Only exclude those who are missing earlier weeks than its own maximum!!!
                // First check: Does it have registrations for all weeks including the latest?
                Set<Integer> siteWeeks = regs.stream().map(r -> r.getTrapsiteRegistrationPK().getWeek()).collect(Collectors.toSet());
                // Skipping sites without registrations
                if (siteWeeks.size() == 0) {
                    continue;
                }

                // Get the max week of the set
                Integer maxWeek = siteWeeks.stream().mapToInt(Integer::intValue).max().getAsInt();
                // Count backwards from it and down to 21
                boolean hasContinuousWeeks = true;
                for (int i = weeks.length - 1; i >= 0; i--) {
                    if (weeks[i] <= maxWeek && !siteWeeks.contains(weeks[i])) {
                        hasContinuousWeeks = false;
                    }
                }
                if (!hasContinuousWeeks) {
                    continue;
                }

                // Check for null values too
                boolean hasWeekWithMissingValue = false;
                Collections.reverse(regs);
                for (TrapsiteRegistration reg : regs) {
                    if (reg.getRegistrationAverage() == null) {
                        // We accept that the last x registrations are empty, but not anyone before the last
                        // non-empty registration object
                        if (reg.getTrapsiteRegistrationPK().getWeek() == maxWeek && maxWeek >= 21) {
                            maxWeek--;
                        } else {
                            hasWeekWithMissingValue = true;
                            break;
                        }
                    }
                }

                if (hasWeekWithMissingValue) {
                    continue;
                }

                // All tests passed, start aggregating
                Long acc24 = 0l;
                Long acc28 = 0l;
                Long acc33 = 0l;

                for (TrapsiteRegistration reg : regs) {
                    if (reg.getRegistrationAverage() != null) {
                        Integer week = reg.getTrapsiteRegistrationPK().getWeek();
                        weekTrapsiteResults.get(week).add(Math.round(reg.getRegistrationAverage()));
                        switch (week) {
                            case 21:
                                acc24 += Math.round(reg.getRegistrationAverage());
                                acc28 += Math.round(reg.getRegistrationAverage());
                                acc33 += Math.round(reg.getRegistrationAverage());
                                break;
                            case 24:
                                acc24 += Math.round(reg.getRegistrationAverage());
                                acc28 += Math.round(reg.getRegistrationAverage());
                                acc33 += Math.round(reg.getRegistrationAverage());
                                break;
                            case 28:
                                acc28 += Math.round(reg.getRegistrationAverage());
                                acc33 += Math.round(reg.getRegistrationAverage());
                                break;
                            case 33:
                                acc33 += Math.round(reg.getRegistrationAverage());
                                break;
                            default:
                                break;
                        }

                    }

                }

                // Adding accumulations
                // Only if all previous weeks have registrations
                if (maxWeek >= 24) {
                    accWeekTrapsiteResults.get(24).add(acc24);
                }
                if (maxWeek >= 28) {
                    accWeekTrapsiteResults.get(28).add(acc28);
                }
                if (maxWeek == 33) {
                    accWeekTrapsiteResults.get(33).add(acc33);
                }
            }

            csv += county;
            for (Integer week : weeks) {
                if (DEBUG && weekTrapsiteResults.get(week) != null && weekTrapsiteResults.get(week).size() > 0) {
                    System.out.println("Uke " + week + ": " + weekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).sum() + "/" + weekTrapsiteResults.get(week).size() + "=" + Math.round(weekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).sum() / weekTrapsiteResults.get(week).size()));
                }
                csv += "," + (weekTrapsiteResults.get(week) != null && weekTrapsiteResults.get(week).size() > 0 ? Math.round(weekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).sum() / weekTrapsiteResults.get(week).size()) : "")
                        + "," + (weekTrapsiteResults.get(week) != null && weekTrapsiteResults.get(week).size() > 0 ? weekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).min().getAsLong() : "")
                        + "," + (weekTrapsiteResults.get(week) != null && weekTrapsiteResults.get(week).size() > 0 ? weekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).max().getAsLong() : "");
                if (week >= 24) {
                    if (weekTrapsiteResults.get(week) != null && weekTrapsiteResults.get(week).size() > 0) {
                        if (DEBUG && weekTrapsiteResults.get(week) != null && weekTrapsiteResults.get(week).size() > 0) {
                            System.out.println("AKKUMULERT Uke " + week + ": " + accWeekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).sum() + "/" + accWeekTrapsiteResults.get(week).size() + "=" + Math.round(accWeekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).sum() / accWeekTrapsiteResults.get(week).size()));
                        }
                        csv += "," + Math.round(accWeekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).sum() / accWeekTrapsiteResults.get(week).size())
                                + "," + accWeekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).min().getAsLong()
                                + "," + accWeekTrapsiteResults.get(week).stream().mapToLong(Long::longValue).max().getAsLong();
                    } else {
                        csv += ",,,";
                    }
                }
            }
            csv += "\n";
        }

        return csv;
    }

    public String getSeasonTrapsitesReportCsv(Integer season) {
        Integer[] weeks = {21, 24, 28, 33};
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

        String csv = String.join(";", heading) + "\n";
        for (SeasonTrapsite st : seasonTrapsites) {
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
            for (TrapsiteRegistration r : regs) {
                String[] stDataWeek = {
                    r.getTrapsiteRegistrationPK().getWeek().toString(),
                    r.getDateRegistration().toString(), // TODO Format
                    r.getTrap1() != null ? r.getTrap1().toString() : "M",
                    r.getTrap2() != null ? r.getTrap2().toString() : "M",
                    r.getTrap3() != null ? r.getTrap3().toString() : "M",
                    r.getTrap4() != null ? r.getTrap4().toString() : "M",
                    r.getRegistrationAverage() != null ? r.getRegistrationAverage().toString() : "",
                    r.getObservedAttacksDescription() != null && ! r.getObservedAttacksDescription().isBlank() ? r.getObservedAttacksDescription().replaceAll("\\r|\\n", ";") : ""
                };
                regsPerWeek.put(r.getTrapsiteRegistrationPK().getWeek(), stDataWeek);
            }

            String[][] trapSiteLines = new String[4][23];
            Integer counter = 0;
            for (Integer week : weeks) {
                List<String> trapSiteLine = new ArrayList(Arrays.asList(week.equals(21) ? stInfo : emptyCells));
                trapSiteLine.addAll(Arrays.asList(regsPerWeek.get(week) != null ? regsPerWeek.get(week) : new String[8]));
                csv += String.join(";", trapSiteLine.toArray(new String[0]));
                csv += "\n"; // observedAttacksDescription has been moved to week-by-week lines
            }

        }

        return csv;
    }

    public String getLatestBivoltCalculations(Integer season) {
        List<SeasonTrapsite> seasonTrapsites = this.getSeasonTrapsites(season);
        String[] heading = {
            "Fylke",
            "Fylkesnr (2012)",
            "Kommune",
            "Kommunenr (2012)",
            "Eier",
            "Siste værdatadato",
            "gen1_maturity_rate",
            "gen1_doy",
            "gen2_maturity_rate",
            "gen2_doy"
        };

        String csv = String.join(",", heading) + "\n";
        for (SeasonTrapsite st : seasonTrapsites) {
            csv += st.getCounty2012Name()
                    + "," + st.getCounty2012No()
                    + "," + st.getMunicipality2012Name()
                    + "," + st.getMunicipality2012No()
                    + "," + st.getOwnerName();
            if (st.getBivolt() != null) {
                csv += "," + st.getBivolt().getDateLastWeatherData()
                        + "," + st.getBivolt().getGen1MaturityRate()
                        + "," + st.getBivolt().getGen1MatureDoy()
                        + "," + st.getBivolt().getGen2MaturityRate()
                        + "," + st.getBivolt().getGen2MatureDoy();
            } else {
                csv += ",,,,";
            }
            csv += "\n";
        }
        return csv;
    }

    public org.locationtech.jts.geom.Coordinate getUTMCoordinate(Double longitude, Double latitude) {
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

    /**
     * For a given date, returns the trapsites that are behind on registering
     * data
     *
     * @param date
     * @return
     */
    public List<SeasonTrapsite> getSeasonTrapsitesBehindOnData(Date currentDate) {
        Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
        cal.setTime(currentDate);
        Integer season = cal.get(Calendar.YEAR);
        Integer week = cal.get(Calendar.WEEK_OF_YEAR);
        List<SeasonTrapsite> retVal = this.getSeasonTrapsites(season).stream()
                .filter(trapsite -> {
                    for (TrapsiteRegistration tr : trapsite.getTrapsiteRegistrationCollection()) {
                        if (tr.getTrapsiteRegistrationPK().getWeek() >= week) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        return retVal;
    }

    /**
     * First reminder is to be sent Thursday before the registration week
     *
     * @param currentDate
     * @return
     */
    public Boolean isTimeToSendFirstReminder(Date currentDate) {
        Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
        cal.setTime(currentDate);
        Integer weekAfter = cal.get(Calendar.WEEK_OF_YEAR) + 1;
        Boolean isThursday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY;
        return BarkbeetleBean.TRAP_EMPTYING_WEEKS.contains(weekAfter) && isThursday;
    }

    /**
     * Second reminder is to be sent Wednesday in the registration week
     *
     * @param currentDate
     * @return
     */
    public Boolean isTimeToSendSecondReminder(Date currentDate) {
        Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
        cal.setTime(currentDate);
        Integer week = cal.get(Calendar.WEEK_OF_YEAR);
        Boolean isWednesday = cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY;
        return BarkbeetleBean.TRAP_EMPTYING_WEEKS.contains(week) && isWednesday;
    }

    public void checkAndSendTrapEmptyingReminder(Date currentDate) {

        /*
        VipsLogicUser testRecipient = new VipsLogicUser();
        testRecipient.setUserId(1);
        testRecipient.setEmail("tor-einar.skog@nibio.no");
        testRecipient.setPhone("91303819");
        testRecipient.setApprovesSmsBilling(false);
        testRecipient.setFreeSms(true);
        testRecipient.setPreferredLocale("nb");
        */

        // Is it time to send the first reminder?
        if (this.isTimeToSendFirstReminder(currentDate)) {
            String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
            SimpleMailSender mailSender = new SimpleMailSender(smtpServer);

            Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
            cal.setTime(currentDate);

            String heading = "Barkbilleregistrering: Første påminnelse for uke " + (cal.get(Calendar.WEEK_OF_YEAR) + 1);

            // Get all trapsites for this season
            List<SeasonTrapsite> allSeasonTrapsites = this.getSeasonTrapsites(cal.get(Calendar.YEAR));
            for (SeasonTrapsite st : allSeasonTrapsites) {

                String message = this.getIndividualFirstReminderText(st, cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR) + 1);
                VipsLogicUser recipient = st.getUserId();
                this.sendReminder(List.of(recipient), heading, message);
            }

            // We also send a reminder to the county contacts
            Organization NIBIO = this.userBean.getOrganization(1); // !!! This is hard coded!!!
            List<VipsLogicUser> countyContacts = this.userBean.findOrganizationUsersByRole(NIBIO, VipsLogicRole.BARKBEETLE_COUNTY_ADMIN);

            //LOGGER.debug("RecipientList would have been: " + (countyContacts.stream().map(cc -> cc.getEmail()).collect((Collectors.joining(",")))));
            String message = this.getCountyContactReminderText(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR) + 1);
            this.sendReminder(countyContacts, heading, message); 

        } // Is it time to send the second reminder?
        else if (this.isTimeToSendSecondReminder(currentDate)) {
            String smtpServer = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
            SimpleMailSender mailSender = new SimpleMailSender(smtpServer);
            Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
            cal.setTime(currentDate);
            //System.out.println("We think that " + currentDate + " is in week " + cal.get(Calendar.WEEK_OF_YEAR));
            Integer season = cal.get(Calendar.YEAR);
            Integer week = cal.get(Calendar.WEEK_OF_YEAR);
            String heading = "Barkbilleregistrering: Andre påminnelse for uke " + cal.get(Calendar.WEEK_OF_YEAR);
            // Get all trapsites behind on registering
            List<SeasonTrapsite> sitesBehindOnData = this.getSeasonTrapsitesBehindOnData(currentDate);
            for (SeasonTrapsite st : sitesBehindOnData) {
                String message = this.getIndividualSecondReminderText(st, season, week);
                VipsLogicUser recipient = st.getUserId();
                this.sendReminder(List.of(recipient), heading, message);
            }
        } else {
            LOGGER.info("checkAndSendTrapEmptyingReminder(" + currentDate + "): Nothing to do.");
        }
    }

    final String firstRegistrantReminderTpl = "For din fellelokalitet "
            + "i %s fylke, %s kommune, skal tømming og registrering utføres mandag %s eller tirsdag %s "
            + "neste uke. Frist for innlegging av data er onsdag %s. For ytterligere instruksjoner, les mer her: %s";

    public String getIndividualFirstReminderText(SeasonTrapsite seasonTrapsite, Integer season, Integer week) {
        Map<Integer, Date> weekdaysInWeek = this.getWeekdaysInWeek(season, week);
        SimpleDateFormat format = new SimpleDateFormat("d. MMM", BarkbeetleBean.NORGE_MITT_NORGE);
        return String.format(firstRegistrantReminderTpl, seasonTrapsite.getCountyName(), seasonTrapsite.getMunicipalityName(),
                format.format(weekdaysInWeek.get(Calendar.MONDAY)), format.format(weekdaysInWeek.get(Calendar.TUESDAY)), format.format(weekdaysInWeek.get(Calendar.WEDNESDAY)),
                String.format("https://logic.vips.nibio.no/images/modules/barkbeetle/%s", BarkbeetleController.FILENAME_INSTRUKS_REGISTRANTER)
        );
    }

    final String secondReminderTpl = "Vi er inne i tømmeuke %s. For din fellelokalitet "
            + "i %s fylke, %s kommune, skulle tømming og registrering vært utført mandag %s eller tirsdag %s "
            + "denne uke. Frist for innlegging av data var onsdag %s. Vennligst ferdigstill tømming og registrering "
            + "så raskt som overhodet mulig. For ytterligere instruksjoner, les mer her: %s";

    public String getIndividualSecondReminderText(SeasonTrapsite seasonTrapsite, Integer season, Integer week) {
        Map<Integer, Date> weekdaysInWeek = this.getWeekdaysInWeek(season, week);
        SimpleDateFormat format = new SimpleDateFormat("d. MMM", BarkbeetleBean.NORGE_MITT_NORGE);
        return String.format(secondReminderTpl, week, seasonTrapsite.getCountyName(), seasonTrapsite.getMunicipalityName(),
                format.format(weekdaysInWeek.get(Calendar.MONDAY)), format.format(weekdaysInWeek.get(Calendar.TUESDAY)), format.format(weekdaysInWeek.get(Calendar.WEDNESDAY)),
                String.format("https://logic.vips.nibio.no/images/modules/barkbeetle/%s", BarkbeetleController.FILENAME_INSTRUKS_REGISTRANTER)
        );
    }

    final String countyContactReminderTpl = "Tømming og registrering utføres "
            + "mandag %s eller tirsdag %s neste uke. Frist for innlegging av data er onsdag %s. "
            + "Sjekk her for å se hvem som (ikke) har lagt inn data i portalen: %s";

    public String getCountyContactReminderText(Integer season, Integer week) {
        Map<Integer, Date> weekdaysInWeek = this.getWeekdaysInWeek(season, week);
        SimpleDateFormat format = new SimpleDateFormat("d. MMM", BarkbeetleBean.NORGE_MITT_NORGE);
        return String.format(countyContactReminderTpl,
                format.format(weekdaysInWeek.get(Calendar.MONDAY)), format.format(weekdaysInWeek.get(Calendar.TUESDAY)), format.format(weekdaysInWeek.get(Calendar.WEDNESDAY)),
                String.format("https://logic.vips.nibio.no/barkbeetle?action=listSeasonTrapsitesStatus&season=%s", season)
        );
    }

    public Map<Integer, Date> getWeekdaysInWeek(Integer season, Integer week) {
        Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
        cal.set(Calendar.YEAR, season);
        cal.set(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        Date mondayInWeek = cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
        Date tuesdayInWeek = cal.getTime();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        Date wednesdayInWeek = cal.getTime();

        Map<Integer, Date> retVal = new HashMap<>();
        retVal.put(Calendar.MONDAY, mondayInWeek);
        retVal.put(Calendar.TUESDAY, tuesdayInWeek);
        retVal.put(Calendar.WEDNESDAY, wednesdayInWeek);

        return retVal;
    }

    /**
     * Using the VIPS messaging system
     *
     * @param users
     * @param heading
     * @param message
     */
    public void sendReminder(List<VipsLogicUser> users, String heading, String message) {

        UniversalMessage uMessage = new UniversalMessage();
        // The notificationSettingsLink is relevant to regular VIPS notifications, not
        // these barkbeetle reminders, so omitting this here.
        uMessage.setIncludeNotificationSettingsLink(false);
        Calendar cal = Calendar.getInstance(BarkbeetleBean.NORGE_MITT_NORGE);
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 2);
        uMessage.setExpiresAt(cal.getTime());
        uMessage.addMessageLocalVersion("nb", heading, message, "", null);
        List<MessageRecipient> recipients = users.stream().map(user -> {
            MessageRecipient recipient = new MessageRecipient();
            UniversalMessageFormat umf = messagingBean.getUniversalMessageFormat(
                    (user.isApprovesSmsBilling() || user.isFreeSms()) && !user.getPhone().trim().isBlank()
                    ? UniversalMessageFormat.FORMAT_SMS
                    : UniversalMessageFormat.FORMAT_EMAIL
            );
            recipient.setType(umf.getFormatName());
            recipient.setMsgDeliveryAddress(
                    umf.getUniversalMessageFormatId().equals(UniversalMessageFormat.FORMAT_SMS)
                    ? user.getPhone()
                    : user.getEmail()
            );
            recipient.setFreeSms(user.isFreeSms());
            recipient.setPreferredLocale(user.getPreferredLocale());
            return recipient;
        })
                .collect(Collectors.toList());
        uMessage.setDistributionList(recipients);
        messagingBean.sendUniversalMessage(uMessage);
    }

}
