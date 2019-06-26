FROM tomcat:9.0.20-jre8

ARG release=19.1

COPY CONFIG/tomcat/server.xml /usr/local/tomcat/conf/server.xml
COPY CONFIG/tomcat/context.xml /usr/local/tomcat/conf/context.xml
COPY lib/build_image_entry.sh /entrypoint.sh

COPY sakai/deploy/components /usr/local/tomcat/components/
COPY sakai/deploy/lib /usr/local/tomcat/sakai-lib/
COPY sakai/deploy/webapps /usr/local/tomcat/webapps/

RUN mkdir -p /usr/local/sakai/properties && sed -i '/^common.loader\=/ s/$/,"\$\{catalina.base\}\/sakai-lib\/*.jar"/' /usr/local/tomcat/conf/catalina.properties && curl -L -o /usr/local/tomcat/lib/mysql-connector-java-5.1.47.jar https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar && mkdir -p /usr/local/tomcat/sakai && chmod +x /entrypoint.sh

ENV CATALINA_OPTS_MEMORY -Xms2000m -Xmx2000m
ENV CATALINA_OPTS \
-server \
-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseConcMarkSweepGC -XX:+UseParNewGC \
-XX:+CMSParallelRemarkEnabled -XX:+UseCompressedOops -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=80 -XX:TargetSurvivorRatio=90 \
-Djava.awt.headless=true \
-Dsun.net.inetaddr.ttl=0 \
-Dsakai.component.shutdownonerror=true \
-Duser.language=en -Duser.country=US \
-Dsakai.home=/usr/local/sakai/properties -Dsakai.security=/usr/local/tomcat/sakai \
-Duser.timezone=US/Eastern \
-Dsun.net.client.defaultConnectTimeout=300000 \
-Dsun.net.client.defaultReadTimeout=1800000 \
-Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false \
-Dsun.lang.ClassLoader.allowArraySyntax=true \
-Dhttp.agent=Sakai \
-Djava.util.Arrays.useLegacyMergeSort=true 

ENTRYPOINT ["/entrypoint.sh"]
