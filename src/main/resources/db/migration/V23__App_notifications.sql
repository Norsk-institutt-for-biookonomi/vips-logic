CREATE TABLE messaging.app_notification_preference (
    user_id integer NOT NULL,
    forecast_status_enabled boolean NOT NULL DEFAULT true,
    forecast_configuration_ids bigint[] NOT NULL DEFAULT '{}',
    message_enabled boolean NOT NULL DEFAULT false,
    message_crop_category_ids integer[] NOT NULL DEFAULT '{}',
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT app_notification_preference_pkey PRIMARY KEY (user_id),
    CONSTRAINT app_notification_preference_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES public.vips_logic_user (user_id) ON DELETE CASCADE
);

CREATE TABLE messaging.user_device (
    user_device_id serial PRIMARY KEY,
    user_id integer NOT NULL,
    fcm_token text NOT NULL,
    platform character varying(16) NOT NULL,
    updated_at timestamp with time zone NOT NULL DEFAULT now(),
    CONSTRAINT user_device_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES public.vips_logic_user (user_id) ON DELETE CASCADE,
    CONSTRAINT user_device_fcm_token_key UNIQUE (fcm_token),
    CONSTRAINT user_device_platform_check CHECK (platform IN ('ANDROID', 'IOS'))
);

CREATE INDEX user_device_user_id_idx ON messaging.user_device (user_id);
