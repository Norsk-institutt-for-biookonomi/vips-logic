# Since we are including resources from the parent folder,
# this Dockerfile has to be executed from the project's parent folder, like this:
# sudo docker build --tag vips/logic:TEST01 -f VIPSLocic/Dockerfile .

# the first stage of our build will use a maven 3.6 parent image
FROM maven:3.6-openjdk-11 AS MAVEN_BUILD

# We need VIPSCommon too, so the source code must be available in the parent folder
COPY ./VIPSCommon ./VIPSCommon
RUN cd VIPSCommon && mvn clean install

# copy the pom and src code to the container
COPY ./VIPSLogic ./
 
# package our application code
RUN mvn clean install

# Used this as a template: https://github.com/jboss-dockerfiles/wildfly/blob/master/Dockerfile 
# Use latest jboss/base-jdk:11 image as the base
FROM jboss/base-jdk:11


# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 20.0.0.Final
ENV WILDFLY_SHA1 3cab3453c9270c662766417adf16c27806124361
ENV JBOSS_HOME /opt/jboss/wildfly

USER root

# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN cd $HOME \
    && curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && sha1sum wildfly-$WILDFLY_VERSION.tar.gz | grep $WILDFLY_SHA1 \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && mv $HOME/wildfly-$WILDFLY_VERSION $JBOSS_HOME \
    && rm wildfly-$WILDFLY_VERSION.tar.gz \
    && chown -R jboss:0 ${JBOSS_HOME} \
    && chmod -R g+rw ${JBOSS_HOME}

# copy only the artifacts we need from the first stage and discard the rest
COPY --from=MAVEN_BUILD /target/VIPSLogic-1.0-SNAPSHOT.war /VIPSLogic-1.0-SNAPSHOT.war
COPY --from=MAVEN_BUILD /root/.m2 /root/.m2
# Use the sample wildfly_config/standalone.xml, add the correct configs, and copy to the parent folder of the 
# VIPSLogic source folder
COPY  ./standalone.xml ${JBOSS_HOME}/standalone/configuration/standalone.xml
#TODO Download and install the required modules
# PostgreSQL and PostGIS jdbc drivers
RUN mkdir -p ${JBOSS_HOME}/modules/org/postgresql/main
RUN echo '<?xml version="1.0" encoding="UTF-8"?>\
<module xmlns="urn:jboss:module:1.0" name="org.postgresql">\
    <resources>\
        <resource-root path="postgresql-42.2.18.jar"/>\
        <resource-root path="postgis-jdbc-2.1.0.jar"/>\
    </resources>\
    <dependencies>\
        <module name="javax.api"/>\
        <module name="javax.transaction.api"/>\
    </dependencies>\
</module>' > ${JBOSS_HOME}/modules/org/postgresql/main/module.xml
RUN cp /root/.m2/repository/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar ${JBOSS_HOME}/modules/org/postgresql/main
RUN cp /root/.m2/repository/net/postgis/postgis-jdbc/2.2.2/postgis-jdbc-2.2.2.jar ${JBOSS_HOME}/modules/org/postgresql/main

RUN ln -s /VIPSLogic-1.0-SNAPSHOT.war ${JBOSS_HOME}/standalone/deployments/VIPSLogic-1.0-SNAPSHOT.war

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

USER jboss

# Expose the ports we're interested in
EXPOSE 8080

# Set the default command to run on boot
# This will boot WildFly in the standalone mode and bind to all interface
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]

