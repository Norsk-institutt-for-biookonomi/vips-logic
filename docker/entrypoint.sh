#!/bin/bash
set -euo pipefail

append_system_property() {
  local property_name="$1"
  local env_name="$2"
  if [ -n "${!env_name:-}" ]; then
    JAVA_OPTS_APPEND="${JAVA_OPTS_APPEND:-} -D${property_name}=${!env_name}"
  fi
}

JAVA_OPTS_APPEND="${JAVA_OPTS_APPEND:-} -Dquarkus.http.host=0.0.0.0"
JAVA_OPTS_APPEND="${JAVA_OPTS_APPEND} -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

append_system_property "no.nibio.vips.logic.VIPSLOGIC_PROTOCOL" "VIPSLOGIC_PROTOCOL"
append_system_property "no.nibio.vips.logic.SERVER_NAME" "VIPSLOGIC_SERVER_NAME"
append_system_property "no.nibio.vips.logic.MD5_SALT" "VIPSLOGIC_MD5_SALT"
append_system_property "no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_ID" "VIPSLOGIC_GOOGLE_OPENID_CLIENT_ID"
append_system_property "no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_SECRET" "VIPSLOGIC_GOOGLE_OPENID_CLIENT_SECRET"
append_system_property "no.nibio.vips.logic.START_SCHEDULING_ON_BOOT" "VIPSLOGIC_START_SCHEDULING_ON_BOOT"
append_system_property "no.nibio.vips.logic.DISABLE_MESSAGING_SYSTEM" "VIPSLOGIC_DISABLE_MESSAGING"
append_system_property "no.nibio.vips.logic.MESSAGE_ILLUSTRATION_PATH" "VIPSLOGIC_MESSAGE_ILLUSTRATION_PATH"
append_system_property "no.nibio.vips.logic.OBSERVATION_ILLUSTRATION_PATH" "VIPSLOGIC_OBSERVATION_ILLUSTRATION_PATH"
append_system_property "no.nibio.vips.logic.weather.VIPS_WEATHER_PROXY_BASE_URL" "VIPSLOGIC_WEATHER_PROXY_BASE_URL"
append_system_property "no.nibio.vips.logic.weather.FIELDCLIMATE_API_USERNAME" "VIPSLOGIC_FIELDCLIMATE_API_USERNAME"
append_system_property "no.nibio.vips.logic.weather.FIELDCLIMATE_API_PASSWORD" "VIPSLOGIC_FIELDCLIMATE_API_PASSWORD"
append_system_property "no.nibio.vips.logic.weather.FIELDCLIMATE_API_CLIENT_ID" "VIPSLOGIC_FIELDCLIMATE_API_CLIENT_ID"
append_system_property "no.nibio.vips.logic.weather.FIELDCLIMATE_API_CLIENT_SECRET" "VIPSLOGIC_FIELDCLIMATE_API_CLIENT_SECRET"
append_system_property "no.nibio.vips.logic.RIMPROXVEN_USERNAME" "VIPSLOGIC_RIMPROXVEN_USERNAME"
append_system_property "no.nibio.vips.logic.RIMPROXVEN_PASSWORD" "VIPSLOGIC_RIMPROXVEN_PASSWORD"
append_system_property "no.nibio.vips.logic.SYSTEM_TIME_OFFSET_MONTHS" "VIPSLOGIC_SYSTEM_TIME_OFFSET_MONTHS"
append_system_property "no.nibio.vips.logic.AVAILABLE_LANGUAGES" "VIPSLOGIC_AVAILABLE_LANGUAGES"
append_system_property "no.nibio.vips.logic.AVAILABLE_TIMEZONES" "VIPSLOGIC_AVAILABLE_TIMEZONES"
append_system_property "no.nibio.vips.logic.USER_COUNTRY_CODES" "VIPSLOGIC_USER_COUNTRY_CODES"
append_system_property "no.nibio.vips.logic.ALLOWED_X_DOMAINS" "VIPSLOGIC_ALLOWED_X_DOMAINS"
append_system_property "no.nibio.vips.logic.SMTP_SERVER" "VIPSLOGIC_SMTP_SERVER"
append_system_property "no.nibio.vips.logic.messaging.dist.SIMULATE_MAIL_SENDING" "VIPSLOGIC_SIMULATE_MAIL_SENDING"
append_system_property "no.nibio.vips.logic.messaging.dist.MAIL_SENDER_ADDRESS" "VIPSLOGIC_MAIL_SENDER_ADDRESS"
append_system_property "no.nibio.vips.logic.messaging.dist.SMS_SERVER" "VIPSLOGIC_SMS_SERVER"
append_system_property "no.nibio.vips.logic.messaging.dist.SMS_USERNAME" "VIPSLOGIC_SMS_USERNAME"
append_system_property "no.nibio.vips.logic.messaging.dist.SMS_PASSWORD" "VIPSLOGIC_SMS_PASSWORD"
append_system_property "no.nibio.vips.logic.messaging.dist.SMS_SENDER_SRC" "VIPSLOGIC_SMS_SENDER_SRC"
append_system_property "no.nibio.vips.logic.messaging.dist.TRAFFIC_LOG_DIRECTORY" "VIPSLOGIC_TRAFFIC_LOG_DIRECTORY"

export JAVA_OPTS_APPEND

exec /opt/jboss/container/java/run/run-java.sh
