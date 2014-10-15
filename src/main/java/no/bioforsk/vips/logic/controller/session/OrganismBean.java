/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.session;

import com.ibm.icu.util.ULocale;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import no.bioforsk.vips.logic.entity.ExternalResource;
import no.bioforsk.vips.logic.entity.ExternalResourceType;
import no.bioforsk.vips.logic.entity.HierarchyCategory;
import no.bioforsk.vips.logic.entity.Organism;
import no.bioforsk.vips.logic.entity.OrganismExternalResource;
import no.bioforsk.vips.logic.entity.OrganismLocale;
import no.bioforsk.vips.logic.entity.OrganismLocalePK;
import no.bioforsk.vips.logic.util.HierarchyCategoryLocaleNames;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        List<Organism> retVal = em.createNativeQuery("SELECT * FROM Organism o WHERE o.parent_organism_id IS NULL", Organism.class).getResultList();
        for(Organism organism: retVal)
        {
            List<Organism> children = em.createNamedQuery("Organism.findByParentOrganismId").setParameter("parentOrganismId", organism.getOrganismId()).getResultList();
            organism.setChildOrganisms(children);
        }
        return retVal;
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

    public Organism storeOrganism(Organism organism) {
        // Ensure that parentOrganismId = -1 => null
        if(organism.getParentOrganismId().equals(-1))
        {
            organism.setParentOrganismId(null);
        }
        return em.merge(organism);
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
    public void storeOrganismLocalName(Organism organism, String localName, ULocale currentLocale) {
        
        if(organism.getOrganismLocale(currentLocale.getLanguage()) == null)
        {
            
            OrganismLocalePK pk = new OrganismLocalePK(organism.getOrganismId(), currentLocale.getLanguage());
            OrganismLocale oLocale = new OrganismLocale(pk);
            oLocale.setLocalName(localName);
            em.persist(oLocale);
        }
        else
        {
            OrganismLocale oLocale = organism.getOrganismLocale(currentLocale.getLanguage());
            oLocale.setLocalName(localName);
        }
    }
    
}
