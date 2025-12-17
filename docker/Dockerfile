FROM maven:3.9.11-eclipse-temurin-17-alpine as build

WORKDIR /usr/src/mymaven

ARG repo=https://github.com/sakaiproject/sakai.git
ARG branch=master

RUN apk add git && git clone -b ${branch} --depth=1 ${repo} . && git checkout ${branch}

ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

RUN mkdir /deploy && mvn clean install --no-transfer-progress -T 1C sakai:deploy-exploded -Dmaven.tomcat.home=/deploy -Dmaven.test.skip=true

FROM tomcat:9-jdk17-temurin

COPY tomcat/conf/* /usr/local/tomcat/conf/

RUN mkdir -p /usr/local/tomcat/sakai /usr/local/tomcat/bin

COPY tomcat/sakai/* /usr/local/tomcat/sakai/
COPY tomcat/bin/* /usr/local/tomcat/bin/

COPY --from=build /deploy/components /usr/local/tomcat/components/

# This might be able to deploy to a different lib directory
COPY --from=build /deploy/lib /usr/local/tomcat/lib/
COPY --from=build /deploy/webapps /usr/local/tomcat/webapps/

COPY tomcat/bin/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
