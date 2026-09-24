package no.nibio.vips.logic.poi;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Data;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestType;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.Globals;

@ApplicationScoped
@Transactional
public class PointOfInterestServiceImpl implements PointOfInterestService {

    private final PointOfInterestRepository pointOfInterestRepository;

    @Inject
    public PointOfInterestServiceImpl(PointOfInterestRepository pointOfInterestRepository) {
        this.pointOfInterestRepository = pointOfInterestRepository;
    }

    @Override
    public Kml getWeatherStationsKml(
            Integer organizationId,
            Integer excludeWeatherStationId,
            Integer highlightWeatherStationId,
            String serverName,
            ResourceBundle i18nBundle) {
        return getPoisForOrganization(
                organizationId,
                excludeWeatherStationId,
                highlightWeatherStationId,
                serverName,
                i18nBundle,
                PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION);
    }

    @Override
    public Kml getPoisKml(
            Integer organizationId,
            Integer excludePoiId,
            Integer highlightPoiId,
            String serverName,
            ResourceBundle i18nBundle) {
        return getPoisForOrganization(
                organizationId, excludePoiId, highlightPoiId, serverName, i18nBundle, null);
    }

    @Override
    public Kml getPoisForOrganization(
            Integer organizationId,
            Integer excludePoiId,
            Integer highlightPoiId,
            String serverName,
            ResourceBundle i18nBundle,
            Integer pointOfInterestTypeId) {
        String iconPath = Globals.PROTOCOL + "://" + serverName + "/public/images/";

        List<PointOfInterest> pois = new ArrayList<>();
        if(organizationId.equals(-1))
        {
            List<Organization> organizations = pointOfInterestRepository.findAllTopLevelOrganizations();
            for(Organization organization:organizations)
            {
                if(pointOfInterestTypeId != null && pointOfInterestTypeId == PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION)
                {
                    pois.addAll(getWeatherstationsForOrganization(organization,true, false));
                }
                else
                {
                    pois.addAll(pointOfInterestRepository.findPoisByOrganization(organization));
                }
            }
        }
        else
        {
            if(pointOfInterestTypeId != null && pointOfInterestTypeId == PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION)
            {
                pois.addAll(getWeatherstationsForOrganization(pointOfInterestRepository.findOrganizationById(organizationId),true, false));
            }
            else
            {
                pois.addAll(pointOfInterestRepository.findPoisByOrganization(pointOfInterestRepository.findOrganizationById(organizationId)));
            }
        }


        // Initialization
        final Kml kml = KmlFactory.createKml();
        final Document document = kml.createAndSetDocument()
        .withName("Weather stations").withDescription("Weather stations for ");

        document.createAndAddStyle()
            .withId("weatherstation_icon")
        .createAndSetIconStyle()
                .withScale(0.55)
                .createAndSetIcon()
                    .withHref(iconPath + "dot_blue.png");

        document.createAndAddStyle()
            .withId("weatherstation_icon_highlighted")
        .createAndSetIconStyle()
                .withScale(0.55)
                .createAndSetIcon()
                    .withHref(iconPath + "Map_pin_icon_green_small.png");



        String description = "";
        GISEntityUtil gisUtil = new GISEntityUtil();
        for(PointOfInterest poi:pois)
        {
            if(excludePoiId != null && excludePoiId.equals(poi.getPointOfInterestId()))
            {
                continue;
            }

            String styleUrl = "#weatherstation_icon"
                            +   (highlightPoiId != null && highlightPoiId.equals(poi.getPointOfInterestId()) ? "_highlighted" :"");

            String infoUriValue = "";
            if(poi instanceof PointOfInterestWeatherStation)
            {
                description = i18nBundle.getString("dataSourceName") +
                                ": <a href=\"" + ((PointOfInterestWeatherStation) poi).getWeatherStationDataSourceId().getUri() + "\" target=\"new\">"
                                +  ((PointOfInterestWeatherStation) poi).getWeatherStationDataSourceId().getName()
                                + "</a>";
                String infoUriExpression = ((PointOfInterestWeatherStation) poi).getWeatherStationDataSourceId().getInfoUriExpression();
                if(!infoUriExpression.isEmpty())
                {
                    infoUriValue = String.format(infoUriExpression, ((PointOfInterestWeatherStation) poi).getWeatherStationRemoteId());
                }
            }
            Data infoUri = new Data(infoUriValue);
            infoUri.setName("infoUri");
            List<Data> dataList = new ArrayList<>();
            dataList.add(infoUri);
            ExtendedData extendedData = document.createAndSetExtendedData()
                    .withData(dataList);

            final Placemark placemark = document.createAndAddPlacemark()
            .withName(poi.getName())
            .withDescription(description)
            .withStyleUrl(styleUrl)
            .withId(poi.getPointOfInterestId().toString())
            .withExtendedData(extendedData);


            final Point point = placemark.createAndSetPoint();
            List<Coordinate> coord = point.createAndSetCoordinates();
            coord.add(gisUtil.getKMLCoordinateFromJTSCoordinate(poi.getGisGeom().getCoordinate()));
        }
        return kml;
    }

    private List<PointOfInterestWeatherStation> getWeatherstationsForOrganization(
            Organization organization, Boolean active, Boolean includePrivate) {

        if(organization == null)
        {
            return new ArrayList<>();
        }
        // Avoid nulls
        active = active == null ? true : active;

        List<PointOfInterestWeatherStation> retVal = pointOfInterestRepository
                .findWeatherstationsByActivityAndOrganization(organization, active);
        for(Organization subdivision:organization.getOrganizationSet())
        {
            retVal.addAll(pointOfInterestRepository.findWeatherstationsByOrganization(subdivision));
        }

        // Only public weather stations in this list UNLESS explicitly stated otherwise
        return (includePrivate != null && includePrivate) ? retVal: retVal.stream().filter(poi-> ! poi.getIsPrivate()).collect(Collectors.toList());
    }
}
