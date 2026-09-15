package no.nibio.vips.logic.organism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrganismResourceTest {

    @Mock
    private OrganismService organismService;

    @Mock
    private HttpHeaders httpHeaders;

    private OrganismResource organismResource;

    @BeforeEach
    public void setUp() {
        organismResource = new OrganismResource(organismService);
    }

    @Test
    public void getOrganismListReturnsOkWithTree() {
        List<Organism> organisms = List.of(organism(1));
        when(organismService.getSubTree(null)).thenReturn(organisms);

        Response response = organismResource.getOrganismList();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(organisms, response.getEntity());
    }

    @Test
    public void findByLatinNamesSplitsKeywordsAndReturnsOk() {
        List<Organism> organisms = List.of(organism(2));
        when(organismService.findByLatinNames(List.of("Triticum aestivum", "Hordeum vulgare")))
                .thenReturn(organisms);

        Response response = organismResource.findOrganismsByLatinNames("Triticum aestivum,Hordeum vulgare");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(organisms, response.getEntity());
    }

    @Test
    public void findByLocalNamesTrimsKeywordsAndReturnsOk() {
        List<Organism> organisms = List.of(organism(4));
        when(organismService.findByLocalNames(List.of("hvete", "bygg"), "nb")).thenReturn(organisms);

        Response response = organismResource.findOrganismsByLocalNames("nb", "hvete, bygg");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(organisms, response.getEntity());
    }

    @Test
    public void getCropListReturnsOk() {
        List<Organism> crops = List.of(organism(3));
        when(organismService.findAllCrops()).thenReturn(crops);

        Response response = organismResource.getCropOrganismList();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(crops, response.getEntity());
    }

    @Test
    public void getPestListPassesOptionalFiltersAndAuthHeader() {
        List<Organism> pests = List.of(organism(5));
        when(httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION)).thenReturn("user-uuid");
        when(organismService.getPestList(15, 1, "user-uuid")).thenReturn(pests);

        Response response = organismResource.getPestOrganismList(httpHeaders, 15, 1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(pests, response.getEntity());
    }

    @Test
    public void getCropPestListReturnsOk() {
        Map<String, CropPest> mapped = Map.of("1", new CropPest(1));
        when(organismService.getCropPestsMapped()).thenReturn(mapped);

        Response response = organismResource.getCropPestList();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(mapped, response.getEntity());
        assertTrue(((Map<?, ?>) response.getEntity()).containsKey("1"));
    }

    @Test
    public void getCropPestReturnsOkWhenFound() {
        CropPest cropPest = new CropPest(15);
        when(organismService.getCropPestRecursive(15, true)).thenReturn(cropPest);

        Response response = organismResource.getCropPest(15);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(cropPest, response.getEntity());
    }

    @Test
    public void getCropPestReturnsNoContentWhenMissing() {
        when(organismService.getCropPestRecursive(99, true)).thenReturn(null);

        Response response = organismResource.getCropPest(99);

        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public void getCropCategoriesReturnsOk() {
        List<CropCategory> categories = List.of(new CropCategory());
        when(organismService.getCropCategories(1)).thenReturn(categories);

        Response response = organismResource.getCropCategories(1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(categories, response.getEntity());
    }

    private Organism organism(Integer id) {
        Organism organism = new Organism();
        organism.setOrganismId(id);
        return organism;
    }
}
