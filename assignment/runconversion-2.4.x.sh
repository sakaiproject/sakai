#!/bin/sh
MAVEN_TOMCAT_HOME=$HOME/dev/tomcat-gatech-sakai
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/common/lib/sakai-util-log-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/common/lib/log4j-1.2.8.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/components/sakai-util-pack/WEB-INF/lib/sakai-util-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/components/sakai-web-pack/WEB-INF/lib/sakai-entity-util-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/components/sakai-assignment-pack/WEB-INF/lib/sakai-assignment-impl-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/commons-logging-1.0.4.jar"
#CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/commons-dbcp-1.2.1.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/sakai-util-api-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/sakai-entity-api-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/sakai-assignment-api-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/sakai-db-storage-2-4-x.jar"
CLASSPATH="$CLASSPATH:$MAVEN_TOMCAT_HOME/shared/lib/sakai-db-conversion-2-4-x.jar"
# this jar isn't part of the 2-4-x build but is part of 2-5
CLASSPATH="$CLASSPATH:$HOME/dev/commons-dbcp-1.2.2.jar"

##### JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.11.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/ojdbc-14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.util.conversion.UpgradeSchema "$@" 
