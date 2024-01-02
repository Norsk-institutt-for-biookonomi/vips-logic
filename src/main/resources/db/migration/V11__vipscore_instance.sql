-- We are refactoring VIPS to support multiple instances of VIPSCore,
-- allowing for e.g. implementations in different programming languages,
-- such as VIPSCore-Python (https://gitlab.nibio.no/VIPS/VIPSCore-Python)
-- The entity VipsCoreInstance has been added
-- After this migration, remember to add at least one VIPSCoreInstance in the db
-- And you can safely delete these system-properties from your config:
-- * no.nibio.vips.coremanager.VIPSCORE_URL
-- * no.nibio.vips.logic.CORE_BATCH_USERNAME
-- * no.nibio.vips.logic.CORE_BATCH_PASSWORD

--ALTER TABLE public.model_information
--DROP COLUMN vipscore_instance_id;

--DROP TABLE public.vipscore_instance;

CREATE TABLE public.vipscore_instance
(
    vipscore_instance_id SERIAL PRIMARY KEY,
    uri VARCHAR(255),
    username VARCHAR(255),
    password VARCHAR(255)
);

ALTER TABLE public.model_information
    ADD COLUMN vipscore_instance_id INTEGER REFERENCES public.vipscore_instance(vipscore_instance_id) DEFAULT NULL;

ALTER TABLE if exists public.vipscore_instance
    OWNER TO vipslogic;


