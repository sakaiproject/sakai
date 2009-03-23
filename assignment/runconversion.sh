#!/bin/sh
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/sakai-kernel-common-1.0.4.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/log4j-1.2.12.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-kernel-component/WEB-INF/lib/sakai-kernel-util-1.0.4.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-assignment-pack/WEB-INF/lib/sakai-assignment-impl-2.6.0.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/components/sakai-assignment-pack/WEB-INF/lib/commons-codec-1.3.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/sakai-kernel-api-1.0.4.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/sakai-assignment-api-2.6.0.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-dbcp-1.2.2.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/commons-collections-3.1.jar"

##### JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/ojdbc14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.assignment.impl.conversion.impl.UpgradeSchema "$@" 
