package no.nibio.vips.logic.forecast;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.util.Date;
import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.dto.ForecastConfigurationDto;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.VipsLogicUser;

public interface ForecastService {

    boolean isUserAuthorizedForForecastConfiguration(Long forecastConfigurationId, String userUUID);

    List<ForecastResultDto> getForecastResultDtos(Long forecastConfigurationId);

    List<ForecastResultDto> getForecastResultDtos(Long forecastConfigurationId, Integer latestDays);

    List<ForecastResult> getForecastResults(Long forecastConfigurationId);

    String getForecastResultsCsv(Long forecastConfigurationId);

    List<ForecastResult> getForecastResults(Long forecastConfigurationId, Date dateStart, Date dateEnd);

    List<ForecastConfiguration> getForecastConfigurationSummaries(
            Integer organizationId, String includeOrganizationIds, String userUUID);

    List<ForecastConfiguration> getPrivateForecastConfigurationSummaries(VipsLogicUser user);

    ForecastConfiguration getForecastConfiguration(Long forecastConfigurationId);

    ForecastConfigurationDto getForecastConfigurationDto(Long forecastConfigurationId);

    List<ForecastConfiguration> getForecastConfigurationsForModel(String modelId, Integer year);

    List<ForecastConfiguration> getPrivateForecastConfigurationsForUser(Integer userId);

    List<ForecastConfiguration> getForecastConfigurations(List<Integer> organizationIds, Date from, Date to);

    List<ForecastConfiguration> getActiveForecastConfigurationsWithIncludeOrganizations(
            Integer organizationId, String includeOrganizationIds, Date from, Date to);

    List<ForecastConfiguration> getForecastConfigurationsForOrganization(
            Integer organizationId, List<Integer> cropOrganismIds, Date from, Date to);

    List<ForecastConfiguration> getForecastConfigurationsForUserAndDate(Integer userId, Date from, Date to);

    List<ForecastConfiguration> getForecastConfigurationsForUserAndCropsAndDate(
            Integer userId, List<Integer> cropOrganismIds, Date from, Date to);

    List<ForecastModelConfiguration> getForecastModelConfigurations(Long forecastConfigurationId);

    Kml getForecastsAggregateKml(
            Integer organizationId, List<Integer> cropCategoryIds, String userUUID);

    Kml getForecastsAggregateKmlForOrganizations(
            List<Integer> organizationIds, List<Integer> cropCategoryIds, String userUUID);

    Map<String, Object> getLatestForecastResultsForPoi(Integer poiId);

    void updateForecastCaches();

    ModelInformation getModelInformation(String modelId);
}
