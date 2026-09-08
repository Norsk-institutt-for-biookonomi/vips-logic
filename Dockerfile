# Quarkus JVM image for VIPSLogic (staging/local Docker).
# CI: ci_settings.xml is copied from vips-common-config in before_script before docker build.
#
# Build:
#   mvn package -DskipTests
#   docker build -t vipslogic:local .
#
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
COPY ci_settings.xml* ./

RUN if [ -f ci_settings.xml ]; then \
      mvn -B -ntp -s ci_settings.xml dependency:go-offline -DskipTests; \
    else \
      mvn -B -ntp dependency:go-offline -DskipTests; \
    fi

COPY src ./src

RUN if [ -f ci_settings.xml ]; then \
      mvn -B -ntp -s ci_settings.xml package -DskipTests; \
    else \
      mvn -B -ntp package -DskipTests; \
    fi

FROM registry.access.redhat.com/ubi8/openjdk-17:1.21

ENV LANGUAGE='en_US:en'

USER root
RUN microdnf install -y curl && microdnf clean all

COPY --from=builder --chown=185 /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=185 /build/target/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=185 /build/target/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=185 /build/target/quarkus-app/quarkus/ /deployments/quarkus/

COPY docker/entrypoint.sh /opt/jboss/container/java/run/entrypoint.sh
RUN chmod +x /opt/jboss/container/java/run/entrypoint.sh

EXPOSE 8080
USER 185

ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT ["/opt/jboss/container/java/run/entrypoint.sh"]
