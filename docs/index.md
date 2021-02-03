# Building VIPSLogic
## But first: A deployment note
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

## Docker
### Building
You need the
Make sure you're located in the parent folder of the VIPSLogic project. You need these resource files/folders in your current folder:
* standalone.xml (see below)
* VIPSCommon/ (can be cloned [from here](https://gitlab.nibio.no/VIPS/VIPSCommon)) - built with `mvn install`

Run

`sudo docker build --tag vips/logic:TEST01 -f VIPSLogic/Dockerfile .`

#### Contents of standalone.xml
VIPSLogic is using system properties for instance specific settings. In Wildfly (JBoss), these can be provided
in the standalone.xml file. Examples for this is available in the `wildfly_config_examples/` folder. 

### Running

`sudo docker run --publish 18080:8080 --add-host=vipslogicdb:[YOUR_HOSTS_IP_ADDRESS] --detach --name vipslogic vips/logic:TEST01`

### Troubleshooting by logging in
`sudo docker exec -it <containername> bash`

