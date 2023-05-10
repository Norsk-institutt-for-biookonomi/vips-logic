/* 
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
 * Created: May 10th, 2023
 */

-- Create POI type trap as entity
CREATE TABLE public.point_of_interest_trap (
    point_of_interest_id INTEGER REFERENCES public.point_of_interest(point_of_interest_id) PRIMARY KEY REFERENCES public.point_of_interest(point_of_interest_id)
);

INSERT INTO public.point_of_interest_type (point_of_interest_type_id, default_name)
VALUES (5, 'Trap');
