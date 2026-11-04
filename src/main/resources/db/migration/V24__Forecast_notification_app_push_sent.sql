ALTER TABLE messaging.forecast_notification_log
    ADD COLUMN app_push_sent boolean NOT NULL DEFAULT true;
