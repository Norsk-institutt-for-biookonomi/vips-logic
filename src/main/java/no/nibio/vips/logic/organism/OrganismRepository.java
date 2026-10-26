package no.nibio.vips.logic.organism;

import java.util.List;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;

public interface OrganismRepository {

    List<Organism> findRoots();

    List<Organism> findByParentId(Integer parentOrganismId);

    List<Organism> findAllCrops();

    List<Organism> findAllPests();

    List<Organism> findByLatinNames(List<String> latinNames);

    List<Organism> findByLocalNames(List<String> localNames, String locale);

    List<Organism> findPestsByCropOrganismId(Integer cropOrganismId);

    Organism findById(Integer organismId);

    List<CropPest> findAllCropPests();

    CropPest findCropPestByCropOrganismId(Integer cropOrganismId);

    List<CropCategory> findCropCategoriesByOrganizationId(Integer organizationId);
}
