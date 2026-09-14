package no.nibio.vips.logic.organism;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ibm.icu.util.ULocale;
import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.observationdata.ObservationDataBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrganismServiceImplTest {

    @Mock
    private OrganismRepository organismRepository;

    @Mock
    private UserBean userBean;

    @Mock
    private ObservationDataBean observationDataBean;

    private OrganismServiceImpl organismService;

    @Before
    public void setUp() {
        organismService = new OrganismServiceImpl(organismRepository, userBean, observationDataBean);
    }

    @Test
    public void getSubTreeNestsChildrenFromRepository() {
        Organism root = organism(1);
        Organism child = organism(2);
        when(organismRepository.findRoots()).thenReturn(List.of(root));
        when(organismRepository.findByParentId(1)).thenReturn(List.of(child));
        when(organismRepository.findByParentId(2)).thenReturn(List.of());

        List<Organism> tree = organismService.getSubTree(null);

        assertEquals(1, tree.size());
        assertSame(root, tree.get(0));
        assertEquals(1, root.getChildOrganisms().size());
        assertSame(child, root.getChildOrganisms().get(0));
        assertTrue(child.getChildOrganisms().isEmpty());
    }

    @Test
    public void findAllCropsDelegatesToRepository() {
        List<Organism> crops = List.of(organism(10));
        when(organismRepository.findAllCrops()).thenReturn(crops);

        assertSame(crops, organismService.findAllCrops());
    }

    @Test
    public void findByLatinNamesDelegatesToRepository() {
        List<String> names = List.of("Triticum aestivum");
        List<Organism> matches = List.of(organism(11));
        when(organismRepository.findByLatinNames(names)).thenReturn(matches);

        assertSame(matches, organismService.findByLatinNames(names));
    }

    @Test
    public void findByLocalNamesDelegatesToRepository() {
        List<String> names = List.of("hvete");
        List<Organism> matches = List.of(organism(12));
        when(organismRepository.findByLocalNames(names, "nb")).thenReturn(matches);

        assertSame(matches, organismService.findByLocalNames(names, "nb"));
    }

    @Test
    public void getCropPestsMappedKeysByCropOrganismId() {
        CropPest cropPest = new CropPest(42);
        when(organismRepository.findAllCropPests()).thenReturn(List.of(cropPest));

        Map<String, CropPest> mapped = organismService.getCropPestsMapped();

        assertEquals(1, mapped.size());
        assertSame(cropPest, mapped.get("42"));
    }

    @Test
    public void getCropPestRecursiveReturnsPrimaryMatch() {
        CropPest cropPest = new CropPest(15);
        cropPest.setIncludeAllChildCrops(false);
        when(organismRepository.findCropPestByCropOrganismId(15)).thenReturn(cropPest);

        assertSame(cropPest, organismService.getCropPestRecursive(15, true));
    }

    @Test
    public void getCropPestRecursiveWalksParentWhenIncludeAllChildCrops() {
        CropPest parentCropPest = new CropPest(1);
        parentCropPest.setIncludeAllChildCrops(true);
        Organism child = organism(15);
        child.setParentOrganismId(1);
        when(organismRepository.findCropPestByCropOrganismId(15)).thenReturn(null);
        when(organismRepository.findById(15)).thenReturn(child);
        when(organismRepository.findCropPestByCropOrganismId(1)).thenReturn(parentCropPest);

        assertSame(parentCropPest, organismService.getCropPestRecursive(15, true));
    }

    @Test
    public void getCropPestRecursiveReturnsNullAtRootWithoutMatch() {
        Organism root = organism(1);
        when(organismRepository.findCropPestByCropOrganismId(1)).thenReturn(null);
        when(organismRepository.findById(1)).thenReturn(root);

        assertNull(organismService.getCropPestRecursive(1, true));
    }

    @Test
    public void getPestListReturnsAllPestsWhenCropNotSpecified() {
        List<Organism> pests = List.of(organism(20));
        when(organismRepository.findAllPests()).thenReturn(pests);

        assertSame(pests, organismService.getPestList(null, null, null));
        verifyNoInteractions(userBean, observationDataBean);
    }

    @Test
    public void getPestListFiltersByCrop() {
        List<Organism> pests = List.of(organism(21));
        when(organismRepository.findPestsByCropOrganismId(15)).thenReturn(pests);

        assertSame(pests, organismService.getPestList(15, null, null));
    }

    @Test
    public void getPestListDecoratesWithSchemaUsingOrganizationLocaleWhenUnauthenticated() {
        List<Organism> pests = List.of(organism(22));
        List<Organism> decorated = List.of(organism(22));
        Organization organization = new Organization();
        organization.setDefaultLocale("nb");
        when(organismRepository.findAllPests()).thenReturn(pests);
        when(userBean.getUserFromAuthorizationHeader(null)).thenReturn(null);
        when(userBean.getOrganization(1)).thenReturn(organization);
        when(observationDataBean.decoratePestsWithOrganismDataSchema(pests, 1, new ULocale("nb")))
                .thenReturn(decorated);

        assertSame(decorated, organismService.getPestList(null, 1, null));
        verify(observationDataBean).decoratePestsWithOrganismDataSchema(pests, 1, new ULocale("nb"));
    }

    @Test
    public void getPestListDecoratesWithSchemaUsingUserOrganizationLocale() {
        List<Organism> pests = List.of(organism(23));
        List<Organism> decorated = List.of(organism(23));
        Organization organization = new Organization();
        organization.setDefaultLocale("en");
        VipsLogicUser user = new VipsLogicUser();
        user.setOrganizationId(organization);
        when(organismRepository.findAllPests()).thenReturn(pests);
        when(userBean.getUserFromAuthorizationHeader("user-uuid")).thenReturn(user);
        when(observationDataBean.decoratePestsWithOrganismDataSchema(pests, 1, new ULocale("en")))
                .thenReturn(decorated);

        assertSame(decorated, organismService.getPestList(null, 1, "user-uuid"));
    }

    private Organism organism(Integer id) {
        Organism organism = new Organism();
        organism.setOrganismId(id);
        return organism;
    }
}
