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
package no.nibio.vips.logic.organization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    private OrganizationServiceImpl organizationService;

    @Before
    public void setUp() {
        organizationService = new OrganizationServiceImpl(organizationRepository);
    }

    @Test
    public void findAllDelegatesToRepository() {
        List<Organization> organizations = Arrays.asList(organization(1), organization(2));
        when(organizationRepository.findAll()).thenReturn(organizations);

        assertSame(organizations, organizationService.findAll());
        verify(organizationRepository).findAll();
    }

    @Test
    public void findByIdDelegatesToRepository() {
        Organization organization = organization(1);
        when(organizationRepository.findById(1)).thenReturn(organization);

        assertSame(organization, organizationService.findById(1));
        verify(organizationRepository).findById(1);
    }

    @Test
    public void findByIdReturnsNullWhenMissing() {
        when(organizationRepository.findById(99)).thenReturn(null);

        assertNull(organizationService.findById(99));
        verify(organizationRepository).findById(99);
    }

    @Test
    public void findAllReturnsEmptyListFromRepository() {
        when(organizationRepository.findAll()).thenReturn(List.of());

        assertEquals(0, organizationService.findAll().size());
    }

    private Organization organization(Integer id) {
        Organization organization = new Organization();
        organization.setOrganizationId(id);
        return organization;
    }
}
