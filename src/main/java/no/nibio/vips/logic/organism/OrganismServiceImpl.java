package no.nibio.vips.logic.organism;

import com.ibm.icu.util.ULocale;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.observationdata.ObservationDataBean;

@ApplicationScoped
@Transactional
public class OrganismServiceImpl implements OrganismService {

    private final OrganismRepository organismRepository;
    private final UserBean userBean;
    private final ObservationDataBean observationDataBean;

    @Inject
    public OrganismServiceImpl(
            OrganismRepository organismRepository,
            UserBean userBean,
            ObservationDataBean observationDataBean) {
        this.organismRepository = organismRepository;
        this.userBean = userBean;
        this.observationDataBean = observationDataBean;
    }

    @Override
    public List<Organism> getSubTree(Integer parentOrganismId) {
        List<Organism> organisms = parentOrganismId == null
                ? organismRepository.findRoots()
                : organismRepository.findByParentId(parentOrganismId);
        for (Organism organism : organisms) {
            organism.setChildOrganisms(getSubTree(organism.getOrganismId()));
        }
        return organisms;
    }

    @Override
    public List<Organism> findAllCrops() {
        return organismRepository.findAllCrops();
    }

    @Override
    public List<Organism> findAllPests() {
        return organismRepository.findAllPests();
    }

    @Override
    public List<Organism> findPestsByCropOrganismId(Integer cropOrganismId) {
        return organismRepository.findPestsByCropOrganismId(cropOrganismId);
    }

    @Override
    public List<Organism> getPestList(Integer cropOrganismId, Integer organizationId, String authorizationHeader) {
        List<Organism> organismList = cropOrganismId == null
                ? findAllPests()
                : findPestsByCropOrganismId(cropOrganismId);
        if (organizationId != null) {
            VipsLogicUser user = userBean.getUserFromAuthorizationHeader(authorizationHeader);
            ULocale locale = new ULocale(
                    user != null ? user.getOrganizationId().getDefaultLocale()
                            : userBean.getOrganization(organizationId).getDefaultLocale());
            organismList = observationDataBean.decoratePestsWithOrganismDataSchema(
                    organismList, organizationId, locale);
        }
        return organismList;
    }

    @Override
    public List<Organism> findByLatinNames(List<String> latinNames) {
        return organismRepository.findByLatinNames(latinNames);
    }

    @Override
    public List<Organism> findByLocalNames(List<String> localNames, String locale) {
        return organismRepository.findByLocalNames(localNames, locale);
    }

    @Override
    public Map<String, CropPest> getCropPestsMapped() {
        List<CropPest> cropPests = organismRepository.findAllCropPests();
        Map<String, CropPest> mapped = new HashMap<>();
        cropPests.forEach(cropPest -> mapped.put(String.valueOf(cropPest.getCropOrganismId()), cropPest));
        return mapped;
    }

    @Override
    public CropPest getCropPestRecursive(Integer cropOrganismId, boolean isPrimaryCrop) {
        CropPest cropPest = organismRepository.findCropPestByCropOrganismId(cropOrganismId);
        if (cropPest != null && (isPrimaryCrop || cropPest.getIncludeAllChildCrops())) {
            return cropPest;
        }
        Organism organism = organismRepository.findById(cropOrganismId);
        if (organism.getParentOrganismId() != null) {
            return getCropPestRecursive(organism.getParentOrganismId(), false);
        }
        return null;
    }

    @Override
    public List<CropCategory> getCropCategories(Integer organizationId) {
        return organismRepository.findCropCategoriesByOrganizationId(organizationId);
    }
}
