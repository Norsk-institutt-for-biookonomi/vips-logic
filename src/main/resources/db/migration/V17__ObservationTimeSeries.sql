CREATE TABLE IF NOT EXISTS public.observation_time_series
(
    observation_time_series_id SERIAL PRIMARY KEY,
    crop_organism_id integer NOT NULL,
    organism_id integer NOT NULL,
    year integer NOT NULL,
    name character varying(1023) NOT NULL,
    description text,
    source character varying(6) NOT NULL DEFAULT 'WEB',
    user_id integer NOT NULL,
    created timestamp with time zone NOT NULL,
    last_modified_by integer,
    last_modified timestamp with time zone,
    location_point_of_interest_id integer NOT NULL,
    location_is_private boolean DEFAULT false,
    polygon_service_id integer,
    CONSTRAINT observation_time_series_crop_organism_id_fkey FOREIGN KEY (crop_organism_id) REFERENCES public.organism (organism_id),
    CONSTRAINT observation_time_series_organism_id_fkey FOREIGN KEY (organism_id) REFERENCES public.organism (organism_id),
    CONSTRAINT observation_time_series_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.vips_logic_user (user_id),
    CONSTRAINT observation_time_series_last_modified_by_fkey FOREIGN KEY (last_modified_by) REFERENCES public.vips_logic_user (user_id),
    CONSTRAINT observation_time_series_location_point_of_interest_id_fkey FOREIGN KEY (location_point_of_interest_id) REFERENCES public.point_of_interest (point_of_interest_id),
    CONSTRAINT observation_time_series_polygon_service_id_fkey FOREIGN KEY (polygon_service_id) REFERENCES public.polygon_service (polygon_service_id)
);

ALTER TABLE observation ADD COLUMN source character varying(6) NOT NULL DEFAULT 'WEB';
ALTER TABLE observation ADD COLUMN observation_time_series_id integer REFERENCES public.observation_time_series(observation_time_series_id);