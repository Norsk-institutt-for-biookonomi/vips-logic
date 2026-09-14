package no.nibio.vips.logic.organism.jpa;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.util.List;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JpaOrganismRepositoryTest {

    private static final String CROP_PESTS_SQL =
            "SELECT DISTINCT * FROM public.organism WHERE organism_id = ANY ((SELECT pest_organism_ids FROM public.crop_pest where crop_organism_id = :cropOrganismId)\\:\\:int[])";

    @Mock
    private EntityManager em;

    @Mock
    private Query nativeQuery;

    @Mock
    private TypedQuery<Organism> organismQuery;

    @Mock
    private TypedQuery<CropPest> cropPestQuery;

    @Mock
    private TypedQuery<CropCategory> cropCategoryQuery;

    private JpaOrganismRepository repository;

    @Before
    public void setUp() {
        repository = new JpaOrganismRepository(em);
    }

    @Test
    public void findRootsUsesNativeQuery() {
        List<Organism> roots = List.of(new Organism());
        when(em.createNativeQuery(
                        "SELECT * FROM Organism o WHERE o.parent_organism_id IS NULL",
                        Organism.class))
                .thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(roots);

        assertSame(roots, repository.findRoots());
    }

    @Test
    public void findByParentIdUsesNamedQuery() {
        List<Organism> children = List.of(new Organism());
        when(em.createNamedQuery("Organism.findByParentOrganismId", Organism.class)).thenReturn(organismQuery);
        when(organismQuery.setParameter("parentOrganismId", 7)).thenReturn(organismQuery);
        when(organismQuery.getResultList()).thenReturn(children);

        assertSame(children, repository.findByParentId(7));
        verify(organismQuery).setParameter("parentOrganismId", 7);
    }

    @Test
    public void findAllCropsUsesNamedQuery() {
        List<Organism> crops = List.of(new Organism());
        when(em.createNamedQuery("Organism.findAllCrops", Organism.class)).thenReturn(organismQuery);
        when(organismQuery.getResultList()).thenReturn(crops);

        assertSame(crops, repository.findAllCrops());
    }

    @Test
    public void findAllPestsUsesNamedQuery() {
        List<Organism> pests = List.of(new Organism());
        when(em.createNamedQuery("Organism.findAllPests", Organism.class)).thenReturn(organismQuery);
        when(organismQuery.getResultList()).thenReturn(pests);

        assertSame(pests, repository.findAllPests());
    }

    @Test
    public void findByLatinNamesUsesNamedQuery() {
        List<String> names = List.of("Triticum aestivum");
        List<Organism> matches = List.of(new Organism());
        when(em.createNamedQuery("Organism.findByLatinNames", Organism.class)).thenReturn(organismQuery);
        when(organismQuery.setParameter("latinNames", names)).thenReturn(organismQuery);
        when(organismQuery.getResultList()).thenReturn(matches);

        assertSame(matches, repository.findByLatinNames(names));
        verify(organismQuery).setParameter("latinNames", names);
    }

    @Test
    public void findByLocalNamesUsesNamedQuery() {
        List<String> names = List.of("hvete");
        List<Organism> matches = List.of(new Organism());
        when(em.createNamedQuery("Organism.findByLocalNames", Organism.class)).thenReturn(organismQuery);
        when(organismQuery.setParameter("localNames", names)).thenReturn(organismQuery);
        when(organismQuery.setParameter("locale", "nb")).thenReturn(organismQuery);
        when(organismQuery.getResultList()).thenReturn(matches);

        assertSame(matches, repository.findByLocalNames(names, "nb"));
        verify(organismQuery).setParameter("localNames", names);
        verify(organismQuery).setParameter("locale", "nb");
    }

    @Test
    public void findPestsByCropOrganismIdUsesNativeQuery() {
        List<Organism> pests = List.of(new Organism());
        when(em.createNativeQuery(CROP_PESTS_SQL, Organism.class)).thenReturn(nativeQuery);
        when(nativeQuery.setParameter("cropOrganismId", 15)).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(pests);

        assertSame(pests, repository.findPestsByCropOrganismId(15));
        verify(nativeQuery).setParameter("cropOrganismId", 15);
    }

    @Test
    public void findByIdUsesEntityManager() {
        Organism organism = new Organism();
        when(em.find(Organism.class, 9)).thenReturn(organism);

        assertSame(organism, repository.findById(9));
    }

    @Test
    public void findAllCropPestsUsesNamedQuery() {
        List<CropPest> cropPests = List.of(new CropPest(1));
        when(em.createNamedQuery("CropPest.findAll", CropPest.class)).thenReturn(cropPestQuery);
        when(cropPestQuery.getResultList()).thenReturn(cropPests);

        assertSame(cropPests, repository.findAllCropPests());
    }

    @Test
    public void findCropPestByCropOrganismIdUsesNamedQuery() {
        CropPest cropPest = new CropPest(15);
        when(em.createNamedQuery("CropPest.findByCropOrganismId", CropPest.class)).thenReturn(cropPestQuery);
        when(cropPestQuery.setParameter("cropOrganismId", 15)).thenReturn(cropPestQuery);
        when(cropPestQuery.getSingleResult()).thenReturn(cropPest);

        assertSame(cropPest, repository.findCropPestByCropOrganismId(15));
    }

    @Test
    public void findCropCategoriesByOrganizationIdUsesNamedQuery() {
        List<CropCategory> categories = List.of(new CropCategory());
        when(em.createNamedQuery("CropCategory.findByOrganizationId", CropCategory.class))
                .thenReturn(cropCategoryQuery);
        when(cropCategoryQuery.setParameter("organizationId", 1)).thenReturn(cropCategoryQuery);
        when(cropCategoryQuery.getResultList()).thenReturn(categories);

        assertSame(categories, repository.findCropCategoriesByOrganizationId(1));
    }
}
