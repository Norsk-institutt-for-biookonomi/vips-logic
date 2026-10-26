package no.nibio.vips.logic.poi.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.poi.PointOfInterestRepository;

@ApplicationScoped
public class JpaPointOfInterestRepository implements PointOfInterestRepository {

    private final EntityManager em;

    @Inject
    public JpaPointOfInterestRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Organization> findAllTopLevelOrganizations() {
        return em.createNamedQuery("Organization.findAllTopLevelOrganizations", Organization.class)
                .getResultList();
    }

    @Override
    public Organization findOrganizationById(Integer organizationId) {
        return em.find(Organization.class, organizationId);
    }

    @Override
    public List<PointOfInterestWeatherStation> findWeatherstationsByActivityAndOrganization(
            Organization organization, Boolean active) {
        return em.createNamedQuery(
                        "PointOfInterestWeatherStation.findByActivityAndOrganizationId",
                        PointOfInterestWeatherStation.class)
                .setParameter("organizationId", organization)
                .setParameter("active", active)
                .getResultList();
    }

    @Override
    public List<PointOfInterestWeatherStation> findWeatherstationsByOrganization(Organization organization) {
        return em.createNamedQuery(
                        "PointOfInterestWeatherStation.findByOrganizationId",
                        PointOfInterestWeatherStation.class)
                .setParameter("organizationId", organization)
                .getResultList();
    }

    @Override
    public List<PointOfInterest> findPoisByOrganization(Organization organization) {
        return em.createNamedQuery("PointOfInterest.findByOrganizationId", PointOfInterest.class)
                .setParameter("organizationId", organization)
                .getResultList();
    }
}
