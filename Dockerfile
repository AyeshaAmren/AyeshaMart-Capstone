# ============================================================
# AyeshaMart - production container
# Stack: Tomcat 9.x (javax.*) + Temurin Java 17 + H2 + HikariCP
# Multi-stage: builds the WAR from source so it deploys from
# a git repo (no pre-built target/ needed on the build host).
# ============================================================

# ---- Stage 1: build the WAR ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependencies: copy pom.xml first, resolve, then compile.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B package -DskipTests

# ---- Stage 2: Tomcat runtime ----
FROM tomcat:9.0-jdk17-temurin

LABEL org.opencontainers.image.title="AyeshaMart" \
      org.opencontainers.image.description="Multi-seller e-commerce capstone (JSP/Servlet MVC, H2, HikariCP)" \
      org.opencontainers.image.source="https://github.com/AyeshaAmren/AyeshaMart-Capstone"

# Remove the default webapps we never use (leaner and fewer surprises)
RUN rm -rf /usr/local/tomcat/webapps/docs \
           /usr/local/tomcat/webapps/examples \
           /usr/local/tomcat/webapps/host-manager \
           /usr/local/tomcat/webapps/manager \
           /usr/local/tomcat/webapps/ROOT

# The application artefact built in stage 1
COPY --from=build /build/target/ayeshamart.war /usr/local/tomcat/webapps/ayeshamart.war

# Entrypoint: rebind Tomcat's HTTP connector to the platform's
# dynamic $PORT and start Tomcat in the foreground.
COPY docker/tomcat-entrypoint.sh /usr/local/tomcat/bin/tomcat-entrypoint.sh
RUN chmod +x /usr/local/tomcat/bin/tomcat-entrypoint.sh

EXPOSE 8080

ENV AYESHAMART_H2_CONSOLE="false"

CMD ["/usr/local/tomcat/bin/tomcat-entrypoint.sh"]