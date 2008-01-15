#!/bin/sh
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/sakai-util-log-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/log4j-1.2.8.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-util-pack/WEB-INF/lib/sakai-util-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-web-pack/WEB-INF/lib/sakai-entity-util-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-assignment-pack/WEB-INF/lib/sakai-assignment-impl-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-assignment-pack/WEB-INF/lib/commons-codec-1.3.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-logging-1.0.4.jar"
#CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-dbcp-1.2.1.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/sakai-util-api-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/sakai-entity-api-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/sakai-assignment-api-2-4-x.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-dbcp-1.2.1.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-collections-3.1.jar"

##### JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/ojdbc14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.assignment.impl.conversion.impl.UpgradeSchema "$@" 
