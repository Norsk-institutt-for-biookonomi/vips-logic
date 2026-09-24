package no.nibio.vips.logic.poi.jpa;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaPointOfInterestRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Organization> organizationQuery;

    @Mock
    private TypedQuery<PointOfInterest> poiQuery;

    @Mock
    private TypedQuery<PointOfInterestWeatherStation> weatherStationQuery;

    private JpaPointOfInterestRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new JpaPointOfInterestRepository(em);
    }

    @Test
    public void findAllTopLevelOrganizationsUsesNamedQuery() {
        List<Organization> organizations = List.of(new Organization());
        when(em.createNamedQuery("Organization.findAllTopLevelOrganizations", Organization.class))
                .thenReturn(organizationQuery);
        when(organizationQuery.getResultList()).thenReturn(organizations);

        assertSame(organizations, repository.findAllTopLevelOrganizations());
        verify(em).createNamedQuery("Organization.findAllTopLevelOrganizations", Organization.class);
    }

    @Test
    public void findOrganizationByIdUsesEntityManagerFind() {
        Organization organization = new Organization();
        when(em.find(Organization.class, 1)).thenReturn(organization);

        assertSame(organization, repository.findOrganizationById(1));
        verify(em).find(Organization.class, 1);
    }

    @Test
    public void findPoisByOrganizationUsesNamedQuery() {
        Organization organization = new Organization();
        List<PointOfInterest> pois = List.of(new PointOfInterest());
        when(em.createNamedQuery("PointOfInterest.findByOrganizationId", PointOfInterest.class)).thenReturn(poiQuery);
        when(poiQuery.setParameter("organizationId", organization)).thenReturn(poiQuery);
        when(poiQuery.getResultList()).thenReturn(pois);

        assertSame(pois, repository.findPoisByOrganization(organization));
        verify(em).createNamedQuery("PointOfInterest.findByOrganizationId", PointOfInterest.class);
    }

    @Test
    public void findWeatherstationsByActivityAndOrganizationUsesNamedQuery() {
        Organization organization = new Organization();
        List<PointOfInterestWeatherStation> stations = List.of(new PointOfInterestWeatherStation());
        when(em.createNamedQuery(
                        "PointOfInterestWeatherStation.findByActivityAndOrganizationId",
                        PointOfInterestWeatherStation.class))
                .thenReturn(weatherStationQuery);
        when(weatherStationQuery.setParameter("organizationId", organization)).thenReturn(weatherStationQuery);
        when(weatherStationQuery.setParameter("active", true)).thenReturn(weatherStationQuery);
        when(weatherStationQuery.getResultList()).thenReturn(stations);

        assertSame(stations, repository.findWeatherstationsByActivityAndOrganization(organization, true));
        verify(em).createNamedQuery(
                "PointOfInterestWeatherStation.findByActivityAndOrganizationId",
                PointOfInterestWeatherStation.class);
    }

    @Test
    public void findWeatherstationsByOrganizationUsesNamedQuery() {
        Organization organization = new Organization();
        List<PointOfInterestWeatherStation> stations = List.of(new PointOfInterestWeatherStation());
        when(em.createNamedQuery(
                        "PointOfInterestWeatherStation.findByOrganizationId",
                        PointOfInterestWeatherStation.class))
                .thenReturn(weatherStationQuery);
        when(weatherStationQuery.setParameter("organizationId", organization)).thenReturn(weatherStationQuery);
        when(weatherStationQuery.getResultList()).thenReturn(stations);

        assertSame(stations, repository.findWeatherstationsByOrganization(organization));
        verify(em).createNamedQuery(
                "PointOfInterestWeatherStation.findByOrganizationId", PointOfInterestWeatherStation.class);
    }
}
