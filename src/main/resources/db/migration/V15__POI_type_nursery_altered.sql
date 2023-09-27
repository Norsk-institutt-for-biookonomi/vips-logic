/* 
 * Copyright (c) 2023 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
/**
 * Author:  Tor-Einar Skog <tor-einar.skog@nibio.no>
 * Created: September 27th, 2023
 */



-- Add property impact_radius to POI type nursery
ALTER TABLE public.point_of_interest_nursery
    ADD COLUMN impact_radius DOUBLE PRECISION DEFAULT 0.0;