package no.nibio.vips.logic.organism;

import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;

@ApplicationScoped
@Path("organism")
public class OrganismResource {

    private final OrganismService organismService;

    @Inject
    public OrganismResource(OrganismService organismService) {
        this.organismService = organismService;
    }

    @GET
    @Path("list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Organism[].class)
    public Response getOrganismList() {
        return Response.ok().entity(organismService.getSubTree(null)).build();
    }

    @GET
    @Path("search/latinnames")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organism[].class)
    public Response findOrganismsByLatinNames(@QueryParam("keywords") String keywords) {
        List<String> latinNames = Arrays.asList(keywords.split(","));
        return Response.ok().entity(organismService.findByLatinNames(latinNames)).build();
    }

    @GET
    @Path("search/localnames/{locale}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organism[].class)
    public Response findOrganismsByLocalNames(
            @PathParam("locale") String locale,
            @QueryParam("keywords") String keywords) {
        List<String> localNames = Arrays.asList(keywords.split(",")).stream()
                .map(String::trim)
                .collect(Collectors.toList());
        return Response.ok().entity(organismService.findByLocalNames(localNames, locale)).build();
    }

    @GET
    @Path("crop/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Organism[].class)
    public Response getCropOrganismList() {
        return Response.ok().entity(organismService.findAllCrops()).build();
    }

    @GET
    @Path("pest/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Organism[].class)
    public Response getPestOrganismList(
            @Context HttpHeaders httpHeaders,
            @QueryParam("cropOrganismId") Integer cropOrganismId,
            @QueryParam("organizationId") Integer organizationId) {
        String authorizationHeader = httpHeaders != null
                ? httpHeaders.getHeaderString(HttpHeaders.AUTHORIZATION)
                : null;
        return Response.ok()
                .entity(organismService.getPestList(cropOrganismId, organizationId, authorizationHeader))
                .build();
    }

    @GET
    @Path("crop/pest/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getCropPestList() {
        return Response.ok().entity(organismService.getCropPestsMapped()).build();
    }

    @GET
    @Path("croppest/{cropOrganismId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(CropPest.class)
    public Response getCropPest(@PathParam("cropOrganismId") Integer cropOrganismId) {
        CropPest cropPest = organismService.getCropPestRecursive(cropOrganismId, true);
        if (cropPest != null) {
            return Response.ok().entity(cropPest).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("cropcategory/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(CropCategory[].class)
    public Response getCropCategories(@PathParam("organizationId") Integer organizationId) {
        if (organizationId != null) {
            return Response.ok().entity(organismService.getCropCategories(organizationId)).build();
        }
        return Response.noContent().build();
    }
}
