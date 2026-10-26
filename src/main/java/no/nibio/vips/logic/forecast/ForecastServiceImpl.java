package no.nibio.vips.logic.forecast;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.dto.ForecastConfigurationDto;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Transactional
public class ForecastServiceImpl implements ForecastService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastServiceImpl.class);

    private final ForecastRepository forecastRepository;
    private final ForecastBean forecastBean;
    private final UserBean userBean;
    private final OrganismBean organismBean;

    @Inject
    public ForecastServiceImpl(
            ForecastRepository forecastRepository,
            ForecastBean forecastBean,
            UserBean userBean,
            OrganismBean organismBean) {
        this.forecastRepository = forecastRepository;
        this.forecastBean = forecastBean;
        this.userBean = userBean;
        this.organismBean = organismBean;
    }

    @Override
    public boolean isUserAuthorizedForForecastConfiguration(Long forecastConfigurationId, String userUUID) {
        ForecastConfigurationDto fc = forecastBean.getForecastConfigurationDto(forecastConfigurationId);
        if (fc == null) {
            return true;
        }
        if (fc.getIsPrivate()) {
            if (userUUID == null) {
                return false;
            }
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
            if (user == null || !user.getUserId().equals(fc.getVipsLogicUserId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<ForecastResultDto> getForecastResultDtos(Long forecastConfigurationId) {
        List<ForecastResultDto> results = forecastRepository.findResultDtosByConfigurationId(forecastConfigurationId);
        if (results == null) {
            return new ArrayList<>();
        }
        return results;
    }

    @Override
    public List<ForecastResultDto> getForecastResultDtos(Long forecastConfigurationId, Integer latestDays) {
        List<ForecastResultDto> allResults = forecastRepository.findResultDtosByConfigurationId(forecastConfigurationId);
        if (allResults == null || allResults.isEmpty()) {
            return new ArrayList<>();
        }
        Collections.sort(allResults);
        ForecastResultDto mostRecentForecastResult = allResults.get(allResults.size() - 1);
        Instant latestDaysLimit = mostRecentForecastResult.getValidTimeStart().minus(Duration.ofDays(latestDays));
        return allResults.stream()
                .filter(result -> result.getValidTimeStart().isAfter(latestDaysLimit))
                .collect(Collectors.toList());
    }

    @Override
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId) {
        return forecastRepository.findResultsByConfigurationId(forecastConfigurationId);
    }

    @Override
    public String getForecastResultsCsv(Long forecastConfigurationId) {
        List<ForecastResult> results = forecastRepository.findResultsByConfigurationId(forecastConfigurationId);
        if (results == null || results.isEmpty()) {
            return "";
        }
        List<String> parameters = new ArrayList<>();
        parameters.add("Valid time start");
        parameters.add("Valid time end");
        parameters.add("Warning status");
        results.get(0).getKeys().stream().forEach(key -> {
            parameters.add(key);
        });

        String csvOutput = String.join(",", parameters) + "\n";
        csvOutput += results.stream().map(result -> {
            String line = result.getValidTimeStart() + "," + result.getValidTimeEnd() + "," + result.getWarningStatus();
            Map<String, String> valueMap = result.getAllValuesAsMap();
            for (int i = 3; i < parameters.size(); i++) {
                line += "," + valueMap.get(parameters.get(i));
            }
            return line;
        }).collect(Collectors.joining("\n"));
        return csvOutput;
    }

    @Override
    public List<ForecastResult> getForecastResults(Long forecastConfigurationId, Date dateStart, Date dateEnd) {
        return forecastRepository.findResultsByConfigurationIdAndPeriod(forecastConfigurationId, dateStart, dateEnd);
    }

    @Override
    public List<ForecastConfiguration> getForecastConfigurationSummaries(
            Integer organizationId, String includeOrganizationIds, String userUUID) {
        VipsLogicUser user = findUserOrNull(userUUID);
        List<ForecastConfiguration> summaries = forecastBean.getForecastConfigurationSummaries(organizationId, user);

        if (includeOrganizationIds != null) {
            String[] includeOrgIdStrs = includeOrganizationIds.split(",");
            for (String orgId : includeOrgIdStrs) {
                try {
                    Integer includeOrgId = Integer.valueOf(orgId);
                    if (includeOrgId.equals(organizationId)) {
                        continue;
                    }
                    summaries.addAll(forecastBean.getForecastConfigurationSummaries(includeOrgId, user));
                } catch (NumberFormatException ex) {
                }
            }
        }
        return summaries;
    }

    @Override
    public List<ForecastConfiguration> getPrivateForecastConfigurationSummaries(VipsLogicUser user) {
        return forecastBean.getPrivateForecastConfigurationSummaries(user);
    }

    @Override
    public ForecastConfiguration getForecastConfiguration(Long forecastConfigurationId) {
        return forecastRepository.findConfigurationById(forecastConfigurationId);
    }

    @Override
    public ForecastConfigurationDto getForecastConfigurationDto(Long forecastConfigurationId) {
        LOGGER.debug("Getting forecast configuration DTO for id " + forecastConfigurationId + " at " + SystemTime.getSystemTime());
        ForecastConfigurationDto forecastConfigurationDto = forecastBean.getForecastConfigurationDto(forecastConfigurationId);
        LOGGER.debug("Retrieved forecast configuration DTO for id " + forecastConfigurationId + " at " + SystemTime.getSystemTime());
        return forecastConfigurationDto;
    }

    @Override
    public List<ForecastConfiguration> getForecastConfigurationsForModel(String modelId, Integer year) {
        return forecastRepository.findByModelIdAndYear(modelId, year);
    }

    @Override
    public List<ForecastConfiguration> getPrivateForecastConfigurationsForUser(Integer userId) {
        return forecastRepository.findPrivateByUserId(userId);
    }

    @Override
    public List<ForecastConfiguration> getForecastConfigurations(List<Integer> organizationIds, Date from, Date to) {
        return forecastRepository.findByOrganizationIdsAndDate(organizationIds, from, to);
    }

    @Override
    public List<ForecastConfiguration> getActiveForecastConfigurationsWithIncludeOrganizations(
            Integer organizationId, String includeOrganizationIds, Date from, Date to) {
        if (from == null || to == null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(SystemTime.getSystemTime());
            cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
            from = cal.getTime();
            cal.set(cal.get(Calendar.YEAR), Calendar.DECEMBER, 31, 23, 0, 0);
            to = cal.getTime();
        }

        List<Integer> orgIds = new ArrayList<>();
        orgIds.add(organizationId);
        if (includeOrganizationIds != null) {
            String[] includeOrgIdStrs = includeOrganizationIds.split(",");
            for (String orgIdStr : includeOrgIdStrs) {
                try {
                    Integer includeOrgId = Integer.valueOf(orgIdStr.trim());
                    if (includeOrgId.equals(organizationId)) {
                        continue;
                    }
                    orgIds.add(includeOrgId);
                } catch (NumberFormatException ex) {
                }
            }
        }
        return forecastRepository.findByOrganizationIdsAndDate(orgIds, from, to);
    }

    @Override
    public List<ForecastConfiguration> getForecastConfigurationsForOrganization(
            Integer organizationId, List<Integer> cropOrganismIds, Date from, Date to) {
        Date resolvedFrom = from;
        Date resolvedTo = to;
        if (resolvedFrom == null || resolvedTo == null) {
            resolvedTo = SystemTime.getSystemTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(resolvedTo);
            cal.add(Calendar.MONTH, -4);
            resolvedFrom = cal.getTime();
        }

        List<VipsLogicUser> organizationUsers = userBean.getUsersByOrganization(organizationId);
        List<ForecastConfiguration> forecasts = new ArrayList<>();

        for (VipsLogicUser user : organizationUsers) {
            Integer userId = user.getUserId();
            List<ForecastConfiguration> result = cropOrganismIds != null && !cropOrganismIds.isEmpty()
                    ? forecastRepository.findByUserCropsAndDate(userId, cropOrganismIds, resolvedFrom, resolvedTo)
                    : forecastRepository.findByUserAndDate(userId, resolvedFrom, resolvedTo);
            if (forecasts == null) {
                forecasts = result;
            } else {
                forecasts.addAll(result);
            }
        }

        return forecasts.stream()
                .filter(forecast -> forecast.getForecastConfigurationId() > 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<ForecastConfiguration> getForecastConfigurationsForUserAndDate(Integer userId, Date from, Date to) {
        return forecastRepository.findByUserAndDate(userId, from, to);
    }

    @Override
    public List<ForecastConfiguration> getForecastConfigurationsForUserAndCropsAndDate(
            Integer userId, List<Integer> cropOrganismIds, Date from, Date to) {
        return forecastRepository.findByUserCropsAndDate(userId, cropOrganismIds, from, to);
    }

    @Override
    public List<ForecastModelConfiguration> getForecastModelConfigurations(Long forecastConfigurationId) {
        return forecastRepository.findModelConfigurationsByConfigurationId(forecastConfigurationId);
    }

    @Override
    public Kml getForecastsAggregateKml(Integer organizationId, List<Integer> cropCategoryIds, String userUUID) {
        if (cropCategoryIds == null || cropCategoryIds.isEmpty()) {
            return null;
        }
        VipsLogicUser viewUser = findUserIgnoringIllegalUuid(userUUID);
        List<Integer> organizationIds = new ArrayList<>();
        organizationIds.add(organizationId);
        List<Integer> cropOrganismIds = organismBean.getCropCategoryOrganismIds(cropCategoryIds);
        return forecastBean.getForecastsAggregateKml(
                organizationIds, cropOrganismIds, SystemTime.getSystemTime(), Globals.SERVER_NAME, viewUser);
    }

    @Override
    public Kml getForecastsAggregateKmlForOrganizations(
            List<Integer> organizationIds, List<Integer> cropCategoryIds, String userUUID) {
        if (cropCategoryIds == null || cropCategoryIds.isEmpty()) {
            return null;
        }
        VipsLogicUser viewUser = findUserIgnoringIllegalUuid(userUUID);
        List<Integer> cropOrganismIds = organismBean.getCropCategoryOrganismIds(cropCategoryIds);
        return forecastBean.getForecastsAggregateKml(
                organizationIds, cropOrganismIds, SystemTime.getSystemTime(), Globals.SERVER_NAME, viewUser);
    }

    @Override
    public Map<String, Object> getLatestForecastResultsForPoi(Integer poiId) {
        return forecastBean.getLatestForecastResultsForPoi(poiId);
    }

    @Override
    public void updateForecastCaches() {
        forecastBean.updateForecastResultCacheTable();
        forecastBean.updateForecastSummaryTable(SystemTime.getSystemTime());
    }

    @Override
    public ModelInformation getModelInformation(String modelId) {
        return forecastRepository.findModelInformationById(modelId);
    }

    private VipsLogicUser findUserOrNull(String userUUID) {
        try {
            UUID uUUID = UUID.fromString(userUUID);
            return userBean.findVipsLogicUser(uUUID);
        } catch (NullPointerException | IllegalArgumentException ex) {
            return null;
        }
    }

    private VipsLogicUser findUserIgnoringIllegalUuid(String userUUID) {
        if (userUUID == null) {
            return null;
        }
        try {
            UUID uUUID = UUID.fromString(userUUID);
            return userBean.findVipsLogicUser(uUUID);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
