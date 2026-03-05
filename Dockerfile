# Stage 1: Build VIPSCommon (install) + VIPSLogic (package) with Maven (JDK 17)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy only POMs first to leverage dependency cache
COPY VIPSCommon/pom.xml VIPSCommon/pom.xml
COPY VIPSLogic/pom.xml VIPSLogic/pom.xml

# Warm up Maven caches for both modules
RUN mvn -B -ntp -DskipTests -f VIPSCommon/pom.xml dependency:go-offline
RUN mvn -B -ntp -DskipTests -f VIPSLogic/pom.xml dependency:go-offline

# Copy sources and build VIPSCommon into the local repo
COPY VIPSCommon/ VIPSCommon/
RUN mvn -B -ntp -DskipTests -f VIPSCommon/pom.xml install

# Copy sources and build VIPSLogic WAR
COPY VIPSLogic/ VIPSLogic/
RUN mvn -B -ntp -DskipTests -f VIPSLogic/pom.xml package


# Stage 2: WildFly 34 + JDK 17 runtime
FROM quay.io/wildfly/wildfly:34.0.1.Final-jdk17

ENV JBOSS_HOME=/opt/jboss/wildfly

# Copy the WAR from the builder stage
COPY --from=builder /build/VIPSLogic/target/*.war ${JBOSS_HOME}/standalone/deployments/VIPSLogic-2026.1.war

# Provide the PostgreSQL JDBC driver as a WildFly module
USER root
RUN mkdir -p ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main
COPY --from=builder /root/.m2/repository/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar \
  ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/postgresql-42.7.4.jar

# Minimal module.xml for the Postgres driver
ADD <<'EOF' ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/module.xml
<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.9" name="org.postgresql">
  <resources>
    <resource-root path="postgresql-42.7.4.jar"/>
  </resources>
  <dependencies>
    <module name="javax.api"/>
    <module name="jakarta.transaction.api"/>
  </dependencies>
</module>
EOF


# Copy the WildFly standalone configuration used for local dev
COPY VIPSLogic/standalone.xml ${JBOSS_HOME}/standalone/configuration/standalone.xml

# Ensure writable dirs for any file-based paths used at runtime
RUN mkdir -p /data/messages /data/observations && \
    chown -R jboss:jboss /data ${JBOSS_HOME}

USER jboss

# Dev-friendly overrides (adjust as needed)
ENV JAVA_OPTS_APPEND="\
 -Dno.nibio.vips.logic.MESSAGE_ILLUSTRATION_PATH=/data/messages \
 -Dno.nibio.vips.logic.OBSERVATION_ILLUSTRATION_PATH=/data/observations \
 -Dno.nibio.vips.logic.START_SCHEDULING_ON_BOOT=false \
 -Djboss.bind.address=0.0.0.0 \
 -Djboss.bind.address.management=0.0.0.0 \
"

# Set JAVA_OPTS to ensure debug options are used
ENV JAVA_OPTS="${JAVA_OPTS_APPEND}"

# Expose HTTP and management ports
EXPOSE 8080 9990

# Start WildFly with the provided configuration
ENTRYPOINT ["/opt/jboss/wildfly/bin/standalone.sh", "-c", "standalone.xml", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]