package no.nibio.vips.logic.organism;

import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;

public interface OrganismService {

    List<Organism> getSubTree(Integer parentOrganismId);

    List<Organism> findAllCrops();

    List<Organism> findAllPests();

    List<Organism> findPestsByCropOrganismId(Integer cropOrganismId);

    List<Organism> getPestList(Integer cropOrganismId, Integer organizationId, String authorizationHeader);

    List<Organism> findByLatinNames(List<String> latinNames);

    List<Organism> findByLocalNames(List<String> localNames, String locale);

    Map<String, CropPest> getCropPestsMapped();

    CropPest getCropPestRecursive(Integer cropOrganismId, boolean isPrimaryCrop);

    List<CropCategory> getCropCategories(Integer organizationId);
}
