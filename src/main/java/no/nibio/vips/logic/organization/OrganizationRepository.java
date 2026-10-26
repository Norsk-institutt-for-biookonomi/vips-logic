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

import java.util.List;
import no.nibio.vips.logic.entity.Organization;

/**
 * Persistence port for organizations.
 */
public interface OrganizationRepository {

    List<Organization> findAll();

    Organization findById(Integer organizationId);
}
