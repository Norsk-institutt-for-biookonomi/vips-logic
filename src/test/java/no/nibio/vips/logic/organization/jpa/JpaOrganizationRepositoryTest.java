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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class JpaOrganizationRepositoryTest {

    @Mock
    private EntityManager em;

    @Mock
    private TypedQuery<Organization> query;

    private JpaOrganizationRepository repository;

    @BeforeEach
    public void setUp() {
        repository = new JpaOrganizationRepository(em);
    }

    @Test
    public void findAllUsesNamedQuery() {
        List<Organization> organizations = List.of(new Organization());
        when(em.createNamedQuery("Organization.findAll", Organization.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(organizations);

        assertSame(organizations, repository.findAll());
        verify(em).createNamedQuery("Organization.findAll", Organization.class);
    }

    @Test
    public void findByIdUsesEntityManagerFind() {
        Organization organization = new Organization();
        when(em.find(Organization.class, 1)).thenReturn(organization);

        assertSame(organization, repository.findById(1));
        verify(em).find(Organization.class, 1);
    }
}
