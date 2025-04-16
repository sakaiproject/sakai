export UMASK='0022'

CATALINA_OPTS="-server \
               -Djava.awt.headless=true \
               -XX:+UseCompressedOops \
               -XX:+AlwaysPreTouch \
               -XX:+DisableExplicitGC \
               -Djava.net.preferIPv4Stack=true"

# Memory size
CATALINA_OPTS="$CATALINA_OPTS -Xms3g -Xmx4g"

# Generational New size
CATALINA_OPTS="$CATALINA_OPTS -XX:NewSize=500m -XX:MaxNewSize=500m"

# Garbage Collector
CATALINA_OPTS="$CATALINA_OPTS -XX:+UseG1GC"
#CATALINA_OPTS="$CATALINA_OPTS -XX:+UseZGC"
#CATALINA_OPTS="$CATALINA_OPTS -XX:+UseZGC -XX:+ZGenerational"
#CATALINA_OPTS="$CATALINA_OPTS -XX:+UseShenandoahGC"
#CATALINA_OPTS="$CATALINA_OPTS -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=75"

# GC Logging
#CATALINA_OPTS="$CATALINA_OPTS -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails"

# Java Modules
JAVA_OPTS="$JAVA_OPTS \
    --add-opens=java.base/jdk.internal.access=ALL-UNNAMED \
    --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED \
    --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
    --add-opens=java.base/sun.util.calendar=ALL-UNNAMED \
    --add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
    --add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
    --add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
    --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED \
    --add-opens=java.base/java.io=ALL-UNNAMED \
    --add-opens=java.base/java.nio=ALL-UNNAMED \
    --add-opens=java.base/java.net=ALL-UNNAMED \
    --add-opens=java.base/java.util=ALL-UNNAMED \
    --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
    --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED \
    --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
    --add-opens=java.base/java.lang=ALL-UNNAMED \
    --add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
    --add-opens=java.base/java.math=ALL-UNNAMED \
    --add-opens=java.sql/java.sql=ALL-UNNAMED \
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
    --add-opens=java.base/java.time=ALL-UNNAMED \
    --add-opens=java.base/java.text=ALL-UNNAMED \
    --add-opens=java.management/sun.management=ALL-UNNAMED \
    --add-opens=java.desktop/java.awt.font=ALL-UNNAMED \
    --add-opens=java.desktop/javax.swing.tree=ALL-UNNAMED"

# Jasper config
CATALINA_OPTS="$CATALINA_OPTS -Dorg.apache.jasper.compiler.Parser.STRICT_QUOTE_ESCAPING=false"

# Enable Sakai demo mode
CATALINA_OPTS="$CATALINA_OPTS -Dsakai.demo=true"

# Sets the http agent
CATALINA_OPTS="$CATALINA_OPTS -Dhttp.agent=Sakai"

# Timezone some examples for the U.S.:  US/Eastern, US/Central, US/Mountain, US/Pacific, US/Arizona
CATALINA_OPTS="$CATALINA_OPTS -Duser.timezone=US/Eastern"

# Sets the cookie name
CATALINA_OPTS="$CATALINA_OPTS -Dsakai.cookieName=SAKAIID"

# JMX config
#CATALINA_OPTS="$CATALINA_OPTS -Djava.rmi.server.hostname=172.31.6.159 \
#	                          -Dcom.sun.management.jmxremote.port=xxxxx \
#	                          -Dcom.sun.management.jmxremote.ssl=false \
#	                          -Dcom.sun.management.jmxremote.authenticate=false" 

# JDBC Driver
# if [[ $(pwd) =~ "mysql8" ]]; then
#     CLASSPATH="/var/jdbc-connectors/mysql-connector-j-8.0.31.jar"
# fi
