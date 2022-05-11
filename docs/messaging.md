# VIPSLogic messaging

## Configuration
These system properties must be set in order for the messaging system to function:

```xml
<property name="no.nibio.vips.logic.messaging.dist.SIMULATE_MAIL_SENDING" value="true"/>
<property name="no.nibio.vips.logic.messaging.dist.MAIL_SENDER_ADDRESS" value="noreply@foobar.com"/>
<property name="no.nibio.vips.logic.messaging.dist.SMS_SERVER" value="smsgateway.foobar.com"/>
<property name="no.nibio.vips.logic.messaging.dist.SMS_USERNAME" value="foobar"/>
<property name="no.nibio.vips.logic.messaging.dist.SMS_PASSWORD" value="foobar"/>
<property name="no.nibio.vips.logic.messaging.dist.SMS_SENDER_SRC" value="foobar"/>
<property name="no.nibio.vips.logic.messaging.dist.TRAFFIC_LOG_DIRECTORY" value="/var/log/trafficLog/"/>

```