#!/bin/sh
#
# you could use the runconversion in db/db-util/conversion like this
# runconversion.sh -j $HOME/.m2/repository/org/sakaiproject/sakai-assignment-api/M2/sakai-assignment-api-M2.jar \
#        -j $HOME/.m2/repository/org/sakaiproject/sakai-assignment-api/M2/sakai-assignment-api-M2.jar  \
#          $@
#
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-dbcp/commons-dbcp/1.2.2/commons-dbcp-1.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-pool/commons-pool/1.3/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-content-api/M2/sakai-content-api-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-content-impl/M2/sakai-content-impl-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-entity-api/M2/sakai-entity-api-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-entity-util/M2/sakai-entity-util-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-db-api/M2/sakai-db-api-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-db-storage/M2/sakai-db-storage-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-db-conversion/M2/sakai-db-conversion-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-component-api/M2/sakai-component-api-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util-api/M2/sakai-util-api-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util-util/M2/sakai-util-util-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util/M2/sakai-util-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/sakai-util-log/M2/sakai-util-log-M2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/log4j/log4j/1.2.9/log4j-1.2.9.jar"

##### JDBC DRIVER #####
##### SUPPLY PATH TO YOUR JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/mysql/mysql-connector-java/3.1.14/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/ojdbc-14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.util.conversion.UpgradeSchema "$@" 
