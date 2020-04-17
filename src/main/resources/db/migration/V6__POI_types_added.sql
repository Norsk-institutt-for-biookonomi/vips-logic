/* 
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
/**
 * Author:  Tor-Einar Skog <tor-einar.skog@nibio.no>
 * Created: Feb 28, 2020
 */

-- Create POI type farm as entity
CREATE TABLE public.point_of_interest_farm (
    point_of_interest_id INTEGER REFERENCES public.point_of_interest(point_of_interest_id) PRIMARY KEY REFERENCES public.point_of_interest(point_of_interest_id)
);

-- Create POI type field as entity
CREATE TABLE public.point_of_interest_field (
    point_of_interest_id INTEGER REFERENCES public.point_of_interest(point_of_interest_id) PRIMARY KEY REFERENCES public.point_of_interest(point_of_interest_id)
);
