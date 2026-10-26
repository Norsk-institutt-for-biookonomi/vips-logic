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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.util.List;
import no.nibio.vips.logic.entity.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class OrganizationResourceTest {

    @Mock
    private OrganizationService organizationService;

    private OrganizationResource organizationResource;

    @BeforeEach
    public void setUp() {
        organizationResource = new OrganizationResource(organizationService);
    }

    @Test
    public void getOrganizationsReturnsOkWithList() {
        List<Organization> organizations = List.of(organization(1));
        when(organizationService.findAll()).thenReturn(organizations);

        Response response = organizationResource.getOrganizations();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(organizations, response.getEntity());
    }

    @Test
    public void getOrganizationReturnsOkWithEntity() {
        Organization organization = organization(1);
        when(organizationService.findById(1)).thenReturn(organization);

        Response response = organizationResource.getOrganization(1);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertSame(organization, response.getEntity());
    }

    @Test
    public void getOrganizationReturnsOkWhenMissingToPreserveLegacyContract() {
        when(organizationService.findById(99)).thenReturn(null);

        Response response = organizationResource.getOrganization(99);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }

    private Organization organization(Integer id) {
        Organization organization = new Organization();
        organization.setOrganizationId(id);
        return organization;
    }
}
