package no.nibio.vips.logic.forecast.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.forecast.ForecastRepository;

@ApplicationScoped
public class JpaForecastRepository implements ForecastRepository {

    public static final String RESULT_DTOS_SQL =
            "SELECT CAST(forecast_configuration_id AS bigint), CAST(forecast_result_id AS bigint),valid_time_start, valid_time_end, CAST(warning_status AS bigint), all_values FROM forecast_result WHERE forecast_configuration_id = :forecastConfigurationId";

    private final EntityManager em;

    @Inject
    public JpaForecastRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastResultDto> findResultDtosByConfigurationId(Long forecastConfigurationId) {
        Query q = em.createNativeQuery(RESULT_DTOS_SQL, ForecastResultDto.class);
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastResult> findResultsByConfigurationId(Long forecastConfigurationId) {
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationId");
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastResult> findResultsByConfigurationIdAndPeriod(
            Long forecastConfigurationId, Date timeStart, Date timeEnd) {
        Query q = em.createNamedQuery("ForecastResult.findByForecastConfigurationIdAndPeriod");
        q.setParameter("forecastConfigurationId", forecastConfigurationId);
        q.setParameter("timeStart", timeStart);
        q.setParameter("timeEnd", timeEnd);
        try {
            return q.getResultList();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public ForecastConfiguration findConfigurationById(Long forecastConfigurationId) {
        try {
            return em.createNamedQuery("ForecastConfiguration.findByForecastConfigurationId", ForecastConfiguration.class)
                    .setParameter("forecastConfigurationId", forecastConfigurationId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastConfiguration> findPrivateByUserId(Integer userId) {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findPrivateByVipsLogicUserId");
        q.setParameter("vipsLogicUserId", user);
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastConfiguration> findByUserAndDate(Integer userId, Date from, Date to) {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdAndDate");
        q.setParameter("vipsLogicUserId", user);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastConfiguration> findByUserCropsAndDate(
            Integer userId, List<Integer> cropOrganismIds, Date from, Date to) {
        VipsLogicUser user = em.find(VipsLogicUser.class, userId);
        Query q = em.createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdAndCropOrganismIdsAndDate");
        q.setParameter("vipsLogicUserId", user);
        q.setParameter("cropOrganismIds", cropOrganismIds);
        q.setParameter("from", from);
        q.setParameter("to", to);
        return q.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastConfiguration> findByOrganizationIdsAndDate(
            List<Integer> organizationIds, Date from, Date to) {
        List<VipsLogicUser> organizationUsers = em
                .createNamedQuery("VipsLogicUser.findByOrganizationIds")
                .setParameter("organizationIds", organizationIds)
                .getResultList();

        if (!organizationUsers.isEmpty()) {
            return em
                    .createNamedQuery("ForecastConfiguration.findByVipsLogicUserIdsAndDate")
                    .setParameter("vipsLogicUserIds", organizationUsers)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getResultList();
        }
        return new ArrayList<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastModelConfiguration> findModelConfigurationsByConfigurationId(Long forecastConfigurationId) {
        return em.createNamedQuery("ForecastModelConfiguration.findByForecastConfigurationId")
                .setParameter("forecastConfigurationId", forecastConfigurationId)
                .getResultList();
    }

    @Override
    public ModelInformation findModelInformationById(String modelId) {
        try {
            return em.createNamedQuery("ModelInformation.findByModelId", ModelInformation.class)
                    .setParameter("modelId", modelId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ForecastConfiguration> findByModelIdAndYear(String modelId, Integer year) {
        return em.createNamedQuery("ForecastConfiguration.findByModelIdAndYear")
                .setParameter("modelId", modelId)
                .setParameter("year", year)
                .getResultList();
    }
}
