/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.controller.session;

import com.ibm.icu.util.ULocale;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.DbUpdate;
import no.nibio.vips.logic.entity.ExternalResource;
import no.nibio.vips.logic.entity.ExternalResourceType;
import no.nibio.vips.logic.entity.HierarchyCategory;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.OrganismExternalResource;
import no.nibio.vips.logic.entity.OrganismLocale;
import no.nibio.vips.logic.entity.OrganismLocalePK;
import no.nibio.vips.logic.util.HierarchyCategoryLocaleNames;

/**
 * @copyright 2014-2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Stateless
public class OrganismBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;

   
    /**
     * 
     * @param parentOrganismId Id of the parent organism you start the tree from. If NULL, all top nodes are selected
     * @return All child nodes of given organism, organized in tree
     */
    public List<Organism> getOrganismSubTree(Integer parentOrganismId){
        List<Organism> retVal;
        if(parentOrganismId == null)
        {
            retVal = em.createNativeQuery("SELECT * FROM Organism o WHERE o.parent_organism_id IS NULL", Organism.class).getResultList();
        }
        else
        {
            retVal = em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", parentOrganismId).getResultList();
        }
         
        for(Organism organism : retVal)
        {
            organism.setChildOrganisms(this.getOrganismSubTree(organism.getOrganismId()));
        }
        return retVal;
        
    }
    
    /**
     * 
     * @param organismId
     * @return the requested organism and its children AND their children again
     */
    public Organism getOrganismAndChildrenTwoLevels(Integer organismId)
    {
        Organism retVal = em.find(Organism.class, organismId);
        retVal.setChildOrganisms(em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", organismId).getResultList());
        // Iterate and set children of next level (to decide if the child is deletable)
        for(Organism child: retVal.getChildOrganisms())
        {
            List<Organism> grandChildren = em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", child.getOrganismId()).getResultList();
            child.setChildOrganisms(grandChildren);
        }
        return retVal;
    }
    
    /**
     * 
     * @return top level organisms and direct children
     */
    public List<Organism> getTopLevelOrganisms()
    {
        List<Organism> retVal = em.createNativeQuery("SELECT * FROM Organism o WHERE o.parent_organism_id IS NULL AND o.organism_id > 0", Organism.class).getResultList();
        for(Organism organism: retVal)
        {
            List<Organism> children = em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", organism.getOrganismId()).getResultList();
            organism.setChildOrganisms(children);
        }
        return retVal;
    }
    
    public List<Organism> getCropChildren(Organism crop)
    {
        List<Organism> children = em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", crop.getOrganismId()).getResultList();
        List<Organism> grandChildren = new ArrayList();
        children.stream().forEach(childCrop -> grandChildren.addAll(this.getCropChildren(childCrop)));
        children.addAll(grandChildren);
        return children;
    }

    public List<ExternalResource> getUnusedExternalResourcesForOrganism(Organism organism) {
        StringBuilder sql = new StringBuilder()
                .append("SELECT * FROM external_resource ")
                .append("WHERE external_resource_type_id=")
                .append(ExternalResourceType.ORGANISM_DATABASE).append(" \n");
        if(organism.getOrganismExternalResourceSet() == null || organism.getOrganismExternalResourceSet().isEmpty())
        {
            return em.createNativeQuery(sql.toString(), ExternalResource.class).getResultList();
        }
        else
        {
            sql.append("AND external_resource_id NOT IN (:externalResourceIds)");
            List<Integer> ids = new ArrayList<>();
            for(OrganismExternalResource ore : organism.getOrganismExternalResourceSet())
            {
                ids.add(ore.getExternalResource().getExternalResourceId());
            }
            
            Query q = em.createNativeQuery(sql.toString(), ExternalResource.class);
            q.setParameter("externalResourceIds", ids);
            return q.getResultList();
        }
    }

    public Organism storeOrganismWithLocalName(Organism organism, String localName, ULocale currentLocale) {
        // Ensure that parentOrganismId = -1 => null
        if(organism.getParentOrganismId().equals(-1))
        {
            organism.setParentOrganismId(null);
        }
        organism = em.merge(organism);
        // Adding local name only if it is set
        if(!localName.isEmpty())
        {
            //SessionControllerGetter.getOrganismBean().setOrganismLocalName(organism, localName, currentLocale);
            this.setOrganismLocalName(organism, localName, currentLocale);
        }
        // If local name form field is not set, but it already has a local name, delete it
        else if(organism.getOrganismLocale(currentLocale.getLanguage()) != null)
        {
            //SessionControllerGetter.getOrganismBean().removeOrganismLocalName(organism, currentLocale);
            this.removeOrganismLocalName(organism, currentLocale);
        }
        this.notifyUpdateOfOrganisms();
        return organism;
    }

    public void storeOrganismExternalResource(OrganismExternalResource organismExternalResource) {
        em.merge(organismExternalResource);
    }

    public HierarchyCategoryLocaleNames getHierarchyCategoryNames(ULocale locale) {
        Map<Integer, String> map = new HashMap<>();
        for(HierarchyCategory cat:em.createNamedQuery("HierarchyCategory.findAll", HierarchyCategory.class).getResultList())
        {
            map.put(cat.getHierarchyCategoryId(), cat.getLocalName(locale.getLanguage()));
        }
        
        HierarchyCategoryLocaleNames retVal = new HierarchyCategoryLocaleNames();
        retVal.setNameMap(map);
        return retVal;
    }

    /**
     * Removes an organism from database. Does this only if it has no children
     * @param organismId 
     */
    public boolean deleteOrganism(Integer organismId) {
        List<Organism> children = em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", organismId).getResultList();
        if(children == null || children.isEmpty())
        {
            Organism organism = em.find(Organism.class, organismId);
            em.remove(organism);
            this.notifyUpdateOfOrganisms();
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Adds local name in given locale to organism
     * @param organism
     * @param localName
     * @param currentLocale 
     */
    public void setOrganismLocalName(Organism organism, String localName, ULocale currentLocale) {
        if(organism.getOrganismLocale(currentLocale.getLanguage()) == null)
        {
            OrganismLocalePK pk = new OrganismLocalePK(organism.getOrganismId(), currentLocale.getLanguage());
            OrganismLocale oLocale = new OrganismLocale(pk);
            oLocale.setLocalName(localName);
            organism.getOrganismLocaleSet().add(oLocale);
        }
        else
        {
            OrganismLocale oLocale = organism.getOrganismLocale(currentLocale.getLanguage());
            oLocale.setLocalName(localName);
        }
    }
    
    public void removeOrganismLocalName(Organism organism, ULocale currentLocale)
    {
        OrganismLocale oLocale = organism.getOrganismLocale(currentLocale.getLanguage());
        if(oLocale != null)
        {
            organism.getOrganismLocaleSet().remove(oLocale);
        }
    }
    
    /**
     * 
     * @param latinName Latin name of the species (assuming that latin name is unique for species)
     * @return children of the specified species
     */
    public List<Organism> getSpeciesChildren(String latinName)
    {
        HierarchyCategory speciesCat = em.createNamedQuery("HierarchyCategory.findByDefaultName", HierarchyCategory.class)
                .setParameter("defaultName", "Species")
                .getSingleResult();
        try
        {
            List<Organism> tmp = em.createNamedQuery("Organism.findByLatinName")
                    .setParameter("latinName", latinName)
                    .getResultList();
            for(Organism org : tmp)
            {
                if(org.getHierarchyCategoryId().equals(speciesCat.getHierarchyCategoryId()))
                {
                    return em.createNamedQuery("Organism.findByParentOrganismId")
                            .setParameter("parentOrganismId", org.getOrganismId())
                            .getResultList();
                }
            }
        }
        catch(NoResultException ex)
        {
            return null;
        }
        
        return null;
    }
    
    public List<Organism> getAllCrops()
    {
        return em.createNamedQuery("Organism.findAllCrops").getResultList();
    }
    
    public List<Organism> getAllPests()
    {
        return em.createNamedQuery("Organism.findAllPests").getResultList();
    }
    
    public Map<Integer, Organism> getAllCropsMapped()
    {
        List<Organism> cropList = this.getAllCrops();
        Map<Integer, Organism> cropMap = new HashMap<>();
        cropList.forEach((crop) -> {
            cropMap.put(crop.getOrganismId(), crop);
        });
        return cropMap;
    }
    
    public Map<String, Organism> getAllPestsMapped()
    {
        List<Organism> pestList = this.getAllPests();
        Map<String, Organism> pestMap = new HashMap<>();
        pestList.forEach((pest) -> {
            pestMap.put(String.valueOf(pest.getOrganismId()), pest);
        });
        return pestMap;
    }

    public CropPest getCropPest(Integer cropOrganismId) {
        try
        {
            return em.createNamedQuery("CropPest.findByCropOrganismId", CropPest.class)
                    .setParameter("cropOrganismId", cropOrganismId)
                    .getSingleResult();
        }
        catch(NoResultException ex)
        {
            return null;
        }
    }
    
    public Map<String, CropPest> getCropPestsMapped()
    {
        List<CropPest> cropPests = em.createNamedQuery("CropPest.findAll").getResultList();
        Map<String, CropPest> retVal = new HashMap<>();
        cropPests.forEach((cPest) -> {
            retVal.put(String.valueOf(cPest.getCropOrganismId()), cPest);
        });
        return retVal;
    }
    
    /**
     * 
     * @param pestOrganismId
     * @return the crops associated with this pest (in the public.crop_pest database
     */
    public List<Organism> getPestCrops(Integer pestOrganismId)
    {
        /*Integer[] pestOrganismIds = {pestOrganismId};
        List<CropPest> cropPests = em.createNamedQuery("CropPest.findByPestOrganismId")
                .setParameter("pestOrganismId", pestOrganismId)
                .getResultList();
        */
        List<CropPest> cropPests = em.createNativeQuery("SELECT * FROM public.crop_pest where :pestOrganismId = ANY(pest_organism_ids)", CropPest.class)
                .setParameter("pestOrganismId", pestOrganismId)
                .getResultList();
        
        /*System.out.println("PestId=" + pestOrganismId);
        Syem.out.println("cropPests.size()=" + cropPests.size());*/
        
        // Using map to avoid potential duplicates
        Map<Integer, Organism> pestCrops = new HashMap();
        if(cropPests != null)
        {
            cropPests.stream().forEach(cropPest->{
                Organism crop = em.find(Organism.class, cropPest.getCropOrganismId());
                pestCrops.put(crop.getOrganismId(), crop);
                if(cropPest.getIncludeAllChildCrops())
                {
                    this.getCropChildren(crop).stream().forEach(cropChild->pestCrops.put(cropChild.getOrganismId(), cropChild));
                }
            });
        }
        
        return new ArrayList<>(pestCrops.values());
    }
    
    /**
     * 
     * @param cropOrganismId
     * @return List of Pests associated with this crop
     */
    public List<Organism> getCropPests(Integer cropOrganismId)
    {
    	return em.createNativeQuery("SELECT DISTINCT * FROM public.organism WHERE organism_id = ANY ((SELECT pest_organism_ids FROM public.crop_pest where crop_organism_id = :cropOrganismId)\\:\\:int[])", Organism.class)
                .setParameter("cropOrganismId", cropOrganismId)
                .getResultList();
    }
    
    /**
     * Searching recursively upwards in organism tree to find a cropPest.
     * @param cropOrganismId id of the crop
     * @param isPrimaryCrop false if this is the original crop, true if search upwards
     * @return 
     */
    public CropPest getCropPestRecursive(Integer cropOrganismId, boolean isPrimaryCrop)
    {
        // Try to find a cropPest for this crop
        CropPest cropPest = this.getCropPest(cropOrganismId);
        // If found AND either it is the primary crop OR it's a parent set to include all child crops
        // we return it
        if(cropPest != null && (isPrimaryCrop || cropPest.getIncludeAllChildCrops()))
        {
            return cropPest;
        }
        // If not, check if parent crop exists and keep on searching
        // (If no parent crop, we're done and return empty handed)
        else
        {
            Organism o = em.find(Organism.class, cropOrganismId);
            if(o.getParentOrganismId() != null)
            {
                return this.getCropPestRecursive(o.getParentOrganismId(), false);
            }
            else
            {
                return null;
            }
        }
    }
    
    public CropPest storeCropPest(CropPest cropPest)
    {
    	this.notifyUpdateOfOrganisms();
        return em.merge(cropPest);
    }

    public List<Integer> getCropCategoryOrganismIds(List<Integer> cropCategoryIds) {
        List<CropCategory> cropCategories = em.createNamedQuery("CropCategory.findByCropCategoryIds")
                .setParameter("cropCategoryIds", cropCategoryIds)
                .getResultList();
        List<Integer> retVal = new ArrayList<>();
        cropCategories.stream()
                .filter((cc) -> (cc.getCropOrganismIds() != null))
                .forEachOrdered((cc) -> {
                    retVal.addAll(Arrays.asList(cc.getCropOrganismIds()));
                });
        
        return retVal;
    }
    
    public List<CropCategory> getCropCategories(Integer organizationId)
    {
        try
        {
            return em.createNamedQuery("CropCategory.findByOrganizationId")
                    .setParameter("organizationId", organizationId)
                    .getResultList();
        }
        catch(NoResultException ex)
        {
            return new ArrayList<>();
        }
    }

    public Organism getOrganismByLatinName(String latinName) {
        Organism organism = em.createNamedQuery("Organism.findByLatinName", Organism.class)
                .setParameter("latinName", latinName)
                .getSingleResult();
        return organism;
    }
    
    public List<Organism> sortOrganismsByLocalName(List<Organism> organisms, final String locale)
    {
        Collections.sort(organisms, new Comparator<Organism>() {
                @Override
                public int compare(Organism org1, Organism org2)
                {
                    return org1.getLocalName(locale).compareTo(org2.getLocalName(locale));
                }
            }
        );
        return organisms;
    }
    
    public List<CropCategory> sortCropCategoryByLocalName(List<CropCategory> cropCategories, final String language)
    {
        Collections.sort(cropCategories, new Comparator<CropCategory>() {
                @Override
                public int compare(CropCategory cc1, CropCategory cc2)
                {
                    return cc1.getLocalName(language).compareTo(cc2.getLocalName(language));
                }
            }
        );
        return cropCategories;
    }

    public void storeOrganismCropCategories(Integer organizationId, Organism organism, Set<Integer> cropCategoryIds) {
        List<CropCategory> cropCategories = this.getCropCategories(organizationId);
        if(cropCategoryIds == null)
        {
            cropCategoryIds = new HashSet<>();
        }
        for(CropCategory cropCategory : cropCategories)
        {
            if(cropCategoryIds.contains(cropCategory.getCropCategoryId()))
            {
                cropCategory.addCropOrganismId(organism.getOrganismId());
            }
            else
            {
                cropCategory.removeCropOrganismId(organism.getOrganismId());
            }
        }
        
        this.notifyUpdateOfOrganisms();
    }

    public CropCategory getCropCategory(Integer cropCategoryId) {
        return em.find(CropCategory.class, cropCategoryId);
    }

    public void storeCropCategory(CropCategory cropCategory) {
        em.merge(cropCategory);
        this.notifyUpdateOfOrganisms();
    }

    public List<Organism> findOrganismsByLatinNames(List<String> latinNames) {
        return em.createNamedQuery("Organism.findByLatinNames").setParameter("latinNames", latinNames).getResultList();
    }

    public List<Organism> findOrganismsByLocalNames(List<String> localNames, String locale) {
        return em.createNamedQuery("Organism.findByLocalNames")
                .setParameter("localNames", localNames)
                .setParameter("locale", locale)
                .getResultList();
    }
    
    /**
     * The purpose of this was originally to give information to e.g. the VIPS Observation App
     * that a synchronization is required
     */
    public void notifyUpdateOfOrganisms() {
    	DbUpdate cropCategoriesAndOrganisms = em.find(DbUpdate.class, DbUpdate.CROP_CATEGORIES_AND_ORGANISMS);
    	cropCategoriesAndOrganisms.setLastUpdate(new Date());
    }
    
    /**
     * The purpose of this was originally to give information to e.g. the VIPS Observation App
     * that a synchronization is required
     */
    public Instant getLatestUpdateOfOrganisms() {
    	DbUpdate cropCategoriesAndOrganisms = em.find(DbUpdate.class, DbUpdate.CROP_CATEGORIES_AND_ORGANISMS);
    	return cropCategoriesAndOrganisms.getLastUpdate() != null ? cropCategoriesAndOrganisms.getLastUpdate().toInstant() : null;
    }
}
