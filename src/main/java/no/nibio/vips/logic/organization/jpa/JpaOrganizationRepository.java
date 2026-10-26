/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package no.nibio.vips.logic.organization.jpa;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.organization.OrganizationRepository;

@ApplicationScoped
public class JpaOrganizationRepository implements OrganizationRepository {

    private final EntityManager em;

    @Inject
    public JpaOrganizationRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Organization> findAll() {
        return em.createNamedQuery("Organization.findAll", Organization.class).getResultList();
    }

    @Override
    public Organization findById(Integer organizationId) {
        return em.find(Organization.class, organizationId);
    }
}
