package no.nibio.vips.logic.organism.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.ArrayList;
import java.util.List;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.organism.OrganismRepository;

@ApplicationScoped
public class JpaOrganismRepository implements OrganismRepository {

    private final EntityManager em;

    @Inject
    public JpaOrganismRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Organism> findRoots() {
        return em.createNativeQuery(
                        "SELECT * FROM Organism o WHERE o.parent_organism_id IS NULL",
                        Organism.class)
                .getResultList();
    }

    @Override
    public List<Organism> findByParentId(Integer parentOrganismId) {
        return em.createNamedQuery("Organism.findByParentOrganismId", Organism.class)
                .setParameter("parentOrganismId", parentOrganismId)
                .getResultList();
    }

    @Override
    public List<Organism> findAllCrops() {
        return em.createNamedQuery("Organism.findAllCrops", Organism.class).getResultList();
    }

    @Override
    public List<Organism> findAllPests() {
        return em.createNamedQuery("Organism.findAllPests", Organism.class).getResultList();
    }

    @Override
    public List<Organism> findByLatinNames(List<String> latinNames) {
        return em.createNamedQuery("Organism.findByLatinNames", Organism.class)
                .setParameter("latinNames", latinNames)
                .getResultList();
    }

    @Override
    public List<Organism> findByLocalNames(List<String> localNames, String locale) {
        return em.createNamedQuery("Organism.findByLocalNames", Organism.class)
                .setParameter("localNames", localNames)
                .setParameter("locale", locale)
                .getResultList();
    }

    @Override
    public List<Organism> findPestsByCropOrganismId(Integer cropOrganismId) {
        return em.createNativeQuery(
                        "SELECT DISTINCT * FROM public.organism WHERE organism_id = ANY ((SELECT pest_organism_ids FROM public.crop_pest where crop_organism_id = :cropOrganismId)\\:\\:int[])",
                        Organism.class)
                .setParameter("cropOrganismId", cropOrganismId)
                .getResultList();
    }

    @Override
    public Organism findById(Integer organismId) {
        return em.find(Organism.class, organismId);
    }

    @Override
    public List<CropPest> findAllCropPests() {
        return em.createNamedQuery("CropPest.findAll", CropPest.class).getResultList();
    }

    @Override
    public CropPest findCropPestByCropOrganismId(Integer cropOrganismId) {
        try {
            return em.createNamedQuery("CropPest.findByCropOrganismId", CropPest.class)
                    .setParameter("cropOrganismId", cropOrganismId)
                    .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public List<CropCategory> findCropCategoriesByOrganizationId(Integer organizationId) {
        try {
            return em.createNamedQuery("CropCategory.findByOrganizationId", CropCategory.class)
                    .setParameter("organizationId", organizationId)
                    .getResultList();
        } catch (NoResultException ex) {
            return new ArrayList<>();
        }
    }
}
