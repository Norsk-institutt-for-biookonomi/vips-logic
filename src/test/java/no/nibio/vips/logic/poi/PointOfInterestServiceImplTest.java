package no.nibio.vips.logic.poi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestType;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PointOfInterestServiceImplTest {

    @Mock
    private PointOfInterestRepository pointOfInterestRepository;

    @Mock
    private ResourceBundle i18nBundle;

    private PointOfInterestServiceImpl pointOfInterestService;

    @BeforeEach
    public void setUp() {
        pointOfInterestService = new PointOfInterestServiceImpl(pointOfInterestRepository);
    }

    @Test
    public void getPoisKmlUsesPoiQueryNotWeatherStationQuery() {
        Organization organization = organization(1);
        PointOfInterest poi = poi(10, "Field");
        when(pointOfInterestRepository.findOrganizationById(1)).thenReturn(organization);
        when(pointOfInterestRepository.findPoisByOrganization(organization)).thenReturn(List.of(poi));

        Kml kml = pointOfInterestService.getPoisKml(1, null, null, "logic.example", i18nBundle);

        assertEquals(List.of("10"), placemarkIds(kml));
        verify(pointOfInterestRepository).findOrganizationById(1);
        verify(pointOfInterestRepository).findPoisByOrganization(organization);
        verifyNoMoreInteractions(pointOfInterestRepository);
    }

    @Test
    public void getWeatherStationsKmlUsesWeatherStationQueriesAndSkipsPrivate() {
        Organization organization = organization(1);
        organization.setOrganizationSet(Set.of());
        PointOfInterestWeatherStation publicStation = weatherStation(11, "Public", false);
        PointOfInterestWeatherStation privateStation = weatherStation(12, "Private", true);
        when(i18nBundle.getString("dataSourceName")).thenReturn("Data source");
        when(pointOfInterestRepository.findOrganizationById(1)).thenReturn(organization);
        when(pointOfInterestRepository.findWeatherstationsByActivityAndOrganization(organization, true))
                .thenReturn(List.of(publicStation, privateStation));

        Kml kml = pointOfInterestService.getWeatherStationsKml(1, null, null, "logic.example", i18nBundle);

        assertEquals(List.of("11"), placemarkIds(kml));
        verify(pointOfInterestRepository).findWeatherstationsByActivityAndOrganization(organization, true);
    }

    @Test
    public void getPoisForOrganizationMinusOneLoadsTopLevelOrganizations() {
        Organization organization = organization(3);
        PointOfInterest poi = poi(20, "Org3");
        when(pointOfInterestRepository.findAllTopLevelOrganizations()).thenReturn(List.of(organization));
        when(pointOfInterestRepository.findPoisByOrganization(organization)).thenReturn(List.of(poi));

        Kml kml = pointOfInterestService.getPoisForOrganization(
                -1, null, null, "logic.example", i18nBundle, null);

        assertEquals(List.of("20"), placemarkIds(kml));
        verify(pointOfInterestRepository).findAllTopLevelOrganizations();
    }

    @Test
    public void getPoisKmlExcludesRequestedPoi() {
        Organization organization = organization(1);
        when(pointOfInterestRepository.findOrganizationById(1)).thenReturn(organization);
        when(pointOfInterestRepository.findPoisByOrganization(organization))
                .thenReturn(List.of(poi(10, "Keep"), poi(11, "Skip")));

        Kml kml = pointOfInterestService.getPoisKml(1, 11, null, "logic.example", i18nBundle);

        assertEquals(List.of("10"), placemarkIds(kml));
    }

    @Test
    public void getWeatherStationsKmlReturnsEmptyWhenOrganizationMissing() {
        when(pointOfInterestRepository.findOrganizationById(99)).thenReturn(null);

        Kml kml = pointOfInterestService.getWeatherStationsKml(99, null, null, "logic.example", i18nBundle);

        assertTrue(placemarkIds(kml).isEmpty());
        verify(pointOfInterestRepository).findOrganizationById(99);
        verifyNoMoreInteractions(pointOfInterestRepository);
    }

    @Test
    public void getWeatherStationsKmlUsesWeatherStationType() {
        Organization organization = organization(1);
        organization.setOrganizationSet(Set.of());
        when(pointOfInterestRepository.findOrganizationById(1)).thenReturn(organization);
        when(pointOfInterestRepository.findWeatherstationsByActivityAndOrganization(organization, true))
                .thenReturn(List.of());

        pointOfInterestService.getPoisForOrganization(
                1, null, null, "logic.example", i18nBundle,
                PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION);

        verify(pointOfInterestRepository).findWeatherstationsByActivityAndOrganization(organization, true);
        verify(pointOfInterestRepository).findOrganizationById(1);
        verifyNoMoreInteractions(pointOfInterestRepository);
    }

    private Organization organization(Integer id) {
        Organization organization = new Organization();
        organization.setOrganizationId(id);
        return organization;
    }

    private PointOfInterest poi(Integer id, String name) {
        PointOfInterest poi = new PointOfInterest();
        poi.setPointOfInterestId(id);
        poi.setName(name);
        poi.setGisGeom(new GeometryFactory().createPoint(new Coordinate(10.0, 60.0)));
        return poi;
    }

    private PointOfInterestWeatherStation weatherStation(Integer id, String name, boolean isPrivate) {
        WeatherStationDataSource dataSource = new WeatherStationDataSource();
        dataSource.setName("Met");
        dataSource.setUri("https://example.test");
        dataSource.setInfoUriExpression("");
        PointOfInterestWeatherStation station = new PointOfInterestWeatherStation();
        station.setPointOfInterestId(id);
        station.setName(name);
        station.setIsPrivate(isPrivate);
        station.setWeatherStationRemoteId("WS1");
        station.setWeatherStationDataSourceId(dataSource);
        station.setGisGeom(new GeometryFactory().createPoint(new Coordinate(10.0, 60.0)));
        return station;
    }

    private List<String> placemarkIds(Kml kml) {
        Document document = (Document) kml.getFeature();
        List<Feature> features = document.getFeature();
        if (features == null) {
            return List.of();
        }
        return features.stream()
                .filter(Placemark.class::isInstance)
                .map(Feature::getId)
                .toList();
    }
}
