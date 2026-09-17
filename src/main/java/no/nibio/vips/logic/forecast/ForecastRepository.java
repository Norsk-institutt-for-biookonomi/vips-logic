package no.nibio.vips.logic.forecast;

import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.ModelInformation;

public interface ForecastRepository {

    List<ForecastResultDto> findResultDtosByConfigurationId(Long forecastConfigurationId);

    List<ForecastResult> findResultsByConfigurationId(Long forecastConfigurationId);

    List<ForecastResult> findResultsByConfigurationIdAndPeriod(
            Long forecastConfigurationId, Date timeStart, Date timeEnd);

    ForecastConfiguration findConfigurationById(Long forecastConfigurationId);

    List<ForecastConfiguration> findPrivateByUserId(Integer userId);

    List<ForecastConfiguration> findByUserAndDate(Integer userId, Date from, Date to);

    List<ForecastConfiguration> findByUserCropsAndDate(
            Integer userId, List<Integer> cropOrganismIds, Date from, Date to);

    List<ForecastConfiguration> findByOrganizationIdsAndDate(
            List<Integer> organizationIds, Date from, Date to);

    List<ForecastModelConfiguration> findModelConfigurationsByConfigurationId(Long forecastConfigurationId);

    ModelInformation findModelInformationById(String modelId);

    List<ForecastConfiguration> findByModelIdAndYear(String modelId, Integer year);
}
