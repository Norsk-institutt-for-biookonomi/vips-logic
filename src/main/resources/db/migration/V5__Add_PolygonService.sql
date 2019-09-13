/* 
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
 * Created: Aug 22, 2019
 */

-- ALTER TABLE public.observation
--    DROP COLUMN polygon_service_id;

-- DROP TABLE public.organization_polygon_service;

-- DROP TABLE public.polygon_service;

CREATE TABLE public.polygon_service (
    polygon_service_id SERIAL PRIMARY KEY,
    polygon_service_name VARCHAR(255),
    description TEXT,
    gis_search_url_template VARCHAR(1024),
    EPSG INTEGER
);

CREATE TABLE public.organization_polygon_service (
    organization_id INTEGER REFERENCES public.organization(organization_id),
    polygon_service_id INTEGER REFERENCES public.polygon_service(polygon_service_id),
    CONSTRAINT organization_polygon_service_pkey PRIMARY KEY (organization_id, polygon_service_id)
);

ALTER TABLE public.observation 
    ADD COLUMN polygon_service_id INTEGER REFERENCES public.polygon_service(polygon_service_id);
