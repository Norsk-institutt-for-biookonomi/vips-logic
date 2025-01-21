# VIPSLogic development and deployment guide

[The data model](./data_model.md)

[Authentication](./authentication.md)

[Notification system](./messaging.md)

[Troubleshooting](./troubleshooting.md)

[Translation](./translation.md)

## Building VIPSLogic

### Recommended: Building with Docker
#### First: the database
The docker container needs a database. Important parts of the data model [are described in the Data Model chapter](./data_model.md).

So make sure you have PostgreSQL >= 10 and PostGIS >=2.4 installed. Then create a user called vipslogic. Make sure you enter the vipslogic username and password in the `standalone.xml` file of Wildfly (see chapter below).

 Next up is initalizing the database. Here's how to do it, as the PostgreSQL superuser (In Ubuntu, typically `postgres`)

``` sql
-- This script must be run as superuser (postgres)
-- Before you deploy VIPSLogic

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

CREATE USER vipslogic WITH PASSWORD :vipslogic_password;

CREATE DATABASE vipslogic
  WITH OWNER = vipslogic
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       CONNECTION LIMIT = -1;


-- WE NEED TO DO THIS IN ORDER TO MAKE flywaydb RUN THE INITIAL MIGRATION
-- MAKE SURE YOU REVOKE THIS PRIVILEGE AFTER FIRST DEPLOYMENT OF VIPSLOGIC
-- (Like this: ALTER ROLE vipslogic NOSUPERUSER;)

ALTER ROLE vipslogic SUPERUSER;

```



#### Building the VIPSLogic image
Make sure you're located in the parent folder of the VIPSLogic project. You need these resource files/folders in your current folder:
* standalone.xml (see below)
* VIPSCommon/ (can be cloned [from here](https://gitlab.nibio.no/VIPS/VIPSCommon)) - built with `mvn install`

Run

`sudo docker build --tag vips/logic:TEST01 -f VIPSLogic/Dockerfile .`

##### Contents of standalone.xml
VIPSLogic is using system properties for instance specific settings. In Wildfly (JBoss), these can be provided
in the standalone.xml file. Examples for this is available in the `wildfly_config_examples/` folder. 

Explanation of the properties:

```properties
# Which protocol is this instance of vipslogic using? http or https. It's used for URL building/redirects
no.nibio.vips.logic.VIPSLOGIC_PROTOCOL=http
# Used for testing purposes. Substract e.g. 12 months to see the state of the system one year ago
# This value is overriden if no.nibio.vips.logic.SYSTEM_TIME_EXACT_DATE is set
no.nibio.vips.logic.SYSTEM_TIME_OFFSET_MONTHS=0
# Used for testing purposes. Fixes the system time to one specific date
no.nibio.vips.logic.SYSTEM_TIME_EXACT_DATE=2021-08-01
# Set to false if you don't want the scheduling system (which runs models several times per day) to start
no.nibio.vips.logic.START_SCHEDULING_ON_BOOT=false
# MD5 hash salt for passwords encryption
no.nibio.vips.logic.MD5_SALT=XXXXXXXXXXXXXXXXXXXXXXXXXXXXX
# Mail server hostname for sending mails to users (e.g. when registering a new user)
no.nibio.vips.logic.SMTP_SERVER=mail4.foobar.com
# Path to news message illustrations (served statically by Apache/Nginx)
no.nibio.vips.logic.MESSAGE_ILLUSTRATION_PATH=/home/developer/vips/temp/static/images/messages
# Path to field observation illustrations (served statically by Apache/Nginx)
no.nibio.vips.logic.OBSERVATION_ILLUSTRATION_PATH=/home/developer/vips/temp/static/images/observations
# Credentials for Google OAuth logins
no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_ID=XXXXXXXXXXXXXXXXXXXXXX
no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_SECRET=XXXXXXXXXXXXXXXXXXXXXX
# Which domains are allowed to access cookies and localStorage (given that we're behind a reverse proxy)
no.nibio.vips.logic.ALLOWED_X_DOMAINS=vipsweb,localhost
# Languages available for the application's web pages (in select list at top of page)
no.nibio.vips.logic.AVAILABLE_LANGUAGES=en,nb,zh_CN
# Set to true if you want to disable notifications 
no.nibio.vips.logic.DISABLE_MESSAGING_SYSTEM=true
# URL to the VIPS weather proxy (Currently used by the FMI forecasting service)
no.nibio.vips.logic.weather.VIPS_WEATHER_PROXY_BASE_URL=http://localhost:8080/VIPSWeatherProxy
# Countries available in relevant select lists
no.nibio.vips.logic.USER_COUNTRY_CODES=NO,SE,BA,LV,US,FI,LT,CH
# Timezones available in relevant select lists
no.nibio.vips.logic.AVAILABLE_TIMEZONES=Europe/Oslo,Europe/Zurich,Europe/Stockholm,Europe/Vilnius,Europe/Helsinki
# The properties below are explained in messaging.md
no.nibio.vips.logic.messaging.dist.SIMULATE_MAIL_SENDING=true
no.nibio.vips.logic.messaging.dist.MAIL_SENDER_ADDRESS=noreply@foobar.com
no.nibio.vips.logic.messaging.dist.SMS_SERVER=smsgateway.foobar.com
no.nibio.vips.logic.messaging.dist.SMS_USERNAME=foobar
no.nibio.vips.logic.messaging.dist.SMS_PASSWORD=foobar
no.nibio.vips.logic.messaging.dist.SMS_SENDER_SRC=foobar"
no.nibio.vips.logic.messaging.dist.TRAFFIC_LOG_DIRECTORY=/var/log/trafficLog/
```

The `no.nibio.vips.logic.messaging.dist.X` properties are explained in the documentation of the [notification system](./messaging.md).

#### Running VIPSLogic

`sudo docker run --publish 18080:8080 --add-host=vipslogicdb:[YOUR_HOSTS_IP_ADDRESS] --detach --name vipslogic vips/logic:TEST01`

`172.17.0.1` is the default IP adress for the Docker host if you are running on Linux. Change accordingly. On Mac and Windows you should be able to swap this for `host.docker.internal`.

#### Troubleshooting by logging in
`sudo docker exec -it <containername> bash`


#### Post build database operations
You need to complete these steps in order to have an organization and a super user in the system. Replace `$variable_name` with your info.

``` sql
INSERT INTO public.organization(organization_name,address1,address2,postal_code,country_code,default_locale,default_map_center,default_map_zoom,default_time_zone,city)
VALUES('$organization_name','$address_1','$address_2','$postal_code','$country_code','$default_language',ST_GeomFromText('POINT($longitude $latitude)',4326),4,'$timezone','$city');

-- You need the auto generated organization_id from the first INSERT
INSERT INTO public.vips_logic_user(email,first_name,last_name,organization_id,user_status_id,preferred_locale) 
VALUES('$user_email','$first_name','$last_name',(SELECT organization_id FROM public.organization WHERE organization_name='$organization_name'),4,'$default_language');

-- You need the auto generated user_id from the previous INSERT
-- To create the $passwordhash, see the python script below
INSERT INTO public.user_authentication(user_id,user_authentication_type_id,username,password) VALUES($userid,1,'$username','$passwordhash');

INSERT INTO public.user_vips_logic_role(vips_logic_role_id,user_id) VALUES(1,$userid);
```
To create the $passwordhash for the INSERT above, you can use this python script

``` python

# Python 3 code to demonstrate the  
# working of MD5 (string - hexadecimal) 
  
import hashlib 
  
# initializing string 
# salt must match the no.nibio.vips.logic.MD5_SALT in 
# WildFly's standalone.xml file
salt = "FoobarBarfoLoremipsum123"
p =  "YourUserPassword"
str2hash = p + salt
  
# encoding GeeksforGeeks using encode() 
# then sending to md5() 
result = hashlib.md5(str2hash.encode()) 
  
# printing the equivalent hexadecimal value. 
print("The hexadecimal equivalent of hash is : ", end ="") 
print(result.hexdigest()) # This is the $passwordhash to be used in the SQL above

```

#### Deployment
Whether you deploy using Docker or Wildfly (or another JEE server), the context path of the app is always /VIPSLogic. However, the app is designed to be accessible at the root level. So you always need to proxy it behind e.g. Apache or Nginx. Here's an example vhost for Apache:

``` apache
<VirtualHost *:80>
        ServerName vipslogic-local.no

        AddDefaultCharset UTF-8

        ProxyPass               /       http://localhost:18080/VIPSLogic/
        ProxyPassReverse        /       http://localhost:18080/VIPSLogic/
        ProxyPassReverseCookiePath      /VIPSLogic /
</VirtualHost>

```

After successful deployment in Apache (or Nginx), you should see this screen: 

![VIPSLogic login screen](./illustrations/vipslogic_login_screen.png "VIPSLogic login screen")

Log in with your newly generated superuser credentials, and you get to this screen:
![VIPSLogic start page](./illustrations/vipslogic_start_page.png "VIPSLogic start page")




