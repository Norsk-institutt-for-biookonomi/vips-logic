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

import com.webcohesion.enunciate.metadata.rs.TypeHint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import no.nibio.vips.logic.entity.Organization;

/**
 * REST inbound adapter for organizations. Maps HTTP to {@link OrganizationService} only.
 */
@ApplicationScoped
@Path("organization")
public class OrganizationResource {

    private final OrganizationService organizationService;

    @Inject
    public OrganizationResource(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GET
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organization[].class)
    public Response getOrganizations() {
        return Response.ok().entity(organizationService.findAll()).build();
    }

    @GET
    @Path("{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organization.class)
    public Response getOrganization(@PathParam("organizationId") Integer organizationId) {
        return Response.ok().entity(organizationService.findById(organizationId)).build();
    }
}
