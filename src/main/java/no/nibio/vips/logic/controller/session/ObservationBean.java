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

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Stateless
public class ObservationBean {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    public List<Observation> getObservations(Integer organizationId)
    {
        Organization organization= em.find(Organization.class, organizationId);
        List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                        .setParameter("organizationId", organization)
                                        .getResultList();
        
        return em.createNamedQuery("Observation.findAll").getResultList();
    }

    public Observation storeObservation(Observation observation) {
        return em.merge(observation);
    }

    public void deleteObservation(Integer observationId) {
        Observation observation = em.find(Observation.class, observationId);
        em.remove(observation);
    }
}
