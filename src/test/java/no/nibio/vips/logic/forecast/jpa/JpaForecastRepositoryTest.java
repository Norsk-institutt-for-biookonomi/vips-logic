package no.nibio.vips.logic.forecast.jpa;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaForecastRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query query;

    @Mock
    private TypedQuery<ForecastConfiguration> configurationQuery;

    @Mock
    private TypedQuery<ModelInformation> modelQuery;

    private JpaForecastRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new JpaForecastRepository(em);
    }

    @Test
    public void findResultDtosByConfigurationIdUsesNativeQuery() {
        List<ForecastResultDto> dtos = List.of(new ForecastResultDto(7L, 1L, null, null, 0L, "{}"));
        when(em.createNativeQuery(JpaForecastRepository.RESULT_DTOS_SQL, ForecastResultDto.class)).thenReturn(query);
        when(query.setParameter("forecastConfigurationId", 7L)).thenReturn(query);
        when(query.getResultList()).thenReturn(dtos);

        assertSame(dtos, repository.findResultDtosByConfigurationId(7L));
        verify(query).setParameter("forecastConfigurationId", 7L);
    }

    @Test
    public void findResultsByConfigurationIdUsesNamedQuery() {
        List<ForecastResult> results = List.of(new ForecastResult());
        when(em.createNamedQuery("ForecastResult.findByForecastConfigurationId")).thenReturn(query);
        when(query.setParameter("forecastConfigurationId", 7L)).thenReturn(query);
        when(query.getResultList()).thenReturn(results);

        assertSame(results, repository.findResultsByConfigurationId(7L));
    }

    @Test
    public void findConfigurationByIdReturnsNullWhenMissing() {
        when(em.createNamedQuery("ForecastConfiguration.findByForecastConfigurationId", ForecastConfiguration.class))
                .thenReturn(configurationQuery);
        when(configurationQuery.setParameter("forecastConfigurationId", 9L)).thenReturn(configurationQuery);
        when(configurationQuery.getSingleResult()).thenThrow(new NoResultException());

        assertNull(repository.findConfigurationById(9L));
    }

    @Test
    public void findPrivateByUserIdUsesNamedQuery() {
        VipsLogicUser user = new VipsLogicUser();
        List<ForecastConfiguration> configs = List.of(new ForecastConfiguration());
        when(em.find(VipsLogicUser.class, 4)).thenReturn(user);
        when(em.createNamedQuery("ForecastConfiguration.findPrivateByVipsLogicUserId")).thenReturn(query);
        when(query.setParameter("vipsLogicUserId", user)).thenReturn(query);
        when(query.getResultList()).thenReturn(configs);

        assertSame(configs, repository.findPrivateByUserId(4));
    }

    @Test
    public void findByOrganizationIdsAndDateReturnsEmptyWhenNoUsers() {
        when(em.createNamedQuery("VipsLogicUser.findByOrganizationIds")).thenReturn(query);
        when(query.setParameter("organizationIds", List.of(1))).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of());

        assertTrue(repository.findByOrganizationIdsAndDate(List.of(1), new Date(), new Date()).isEmpty());
    }

    @Test
    public void findModelConfigurationsByConfigurationIdUsesNamedQuery() {
        List<ForecastModelConfiguration> configs = List.of(new ForecastModelConfiguration());
        when(em.createNamedQuery("ForecastModelConfiguration.findByForecastConfigurationId")).thenReturn(query);
        when(query.setParameter("forecastConfigurationId", 3L)).thenReturn(query);
        when(query.getResultList()).thenReturn(configs);

        assertSame(configs, repository.findModelConfigurationsByConfigurationId(3L));
    }

    @Test
    public void findModelInformationByIdUsesNamedQuery() {
        ModelInformation model = new ModelInformation();
        when(em.createNamedQuery("ModelInformation.findByModelId", ModelInformation.class)).thenReturn(modelQuery);
        when(modelQuery.setParameter("modelId", "PSILARTEMP")).thenReturn(modelQuery);
        when(modelQuery.getSingleResult()).thenReturn(model);

        assertSame(model, repository.findModelInformationById("PSILARTEMP"));
    }
}
