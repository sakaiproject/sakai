# Build Sakai from the repository root context, then run on Tomcat 10.1 / JDK 21.
# Prefer (from repo root): podman compose up --build
#   build.context = .  /  dockerfile = docker/Dockerfile

FROM maven:3.9-eclipse-temurin-21 AS build

# Single source of truth for build JVM/Maven memory (override with --build-arg).
ARG MAVEN_OPTS="-XX:InitialRAMPercentage=25.0 -XX:MaxRAMPercentage=75.0 -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=512m"
ENV MAVEN_OPTS=${MAVEN_OPTS}

COPY docker/maven/settings.xml /usr/share/maven/conf/settings.xml

RUN mkdir -p /deploy
WORKDIR /deploy/sakai

# Copy the full reactor; .dockerignore keeps this manageable.
COPY . .

# Cap parallel modules to limit peak heap during WAR/component assembly.
ARG MAVEN_THREADS=2
RUN mvn clean install --no-transfer-progress -T ${MAVEN_THREADS} sakai:deploy-exploded \
      -Dmaven.test.skip=true \
      -Dsakai.skip.webcomponents.tests=true \
      -Dmaven.tomcat.home=/deploy \
      -Dsakai.appserver.home=/deploy \
      -Dappserver.home=/deploy \
    && mvn clean \
    && rm -rf /root/.m2/repository

FROM tomcat:10.1-jdk21-temurin

COPY docker/tomcat/conf/ /usr/local/tomcat/conf/

RUN mkdir -p /usr/local/tomcat/sakai /usr/local/tomcat/bin

COPY docker/tomcat/sakai/ /usr/local/tomcat/sakai/
COPY docker/tomcat/bin/ /usr/local/tomcat/bin/

COPY --from=build /deploy/components /usr/local/tomcat/components/
COPY --from=build /deploy/lib /usr/local/tomcat/lib/
COPY --from=build /deploy/webapps /usr/local/tomcat/webapps/

COPY docker/tomcat/bin/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh /usr/local/tomcat/bin/entrypoint.sh /usr/local/tomcat/bin/setenv.sh

ENTRYPOINT ["/entrypoint.sh"]
