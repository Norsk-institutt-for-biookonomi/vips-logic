package no.nibio.vips.logic.poi;

import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;

public interface PointOfInterestRepository {

    List<Organization> findAllTopLevelOrganizations();

    Organization findOrganizationById(Integer organizationId);

    List<PointOfInterestWeatherStation> findWeatherstationsByActivityAndOrganization(
            Organization organization, Boolean active);

    List<PointOfInterestWeatherStation> findWeatherstationsByOrganization(Organization organization);

    List<PointOfInterest> findPoisByOrganization(Organization organization);
}
