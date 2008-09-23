#!/bin/sh
#
# you could use the runconversion in db/db-util/conversion like this
# runconversion.sh -j $HOME/.m2/repository/org/sakaiproject/sakai-assignment-api/SNAPSHOT/sakai-assignment-api-M2.jar \
#        -j $HOME/.m2/repository/org/sakaiproject/sakai-assignment-api/SNAPSHOT/sakai-assignment-api-M2.jar  \
#          $@
#
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-collections/commons-collections/3.2/commons-collections-3.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-dbcp/commons-dbcp/1.2.2/commons-dbcp-1.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-pool/commons-pool/1.3/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/kernel/sakai-kernel-api/1.0RC2-SNAPSHOT/sakai-kernel-api-1.0RC2-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/kernel/sakai-kernel-impl/1.0RC2-SNAPSHOT/sakai-kernel-impl-1.0RC2-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/kernel/sakai-kernel-util/1.0RC2-SNAPSHOT/sakai-kernel-util-1.0RC2-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/kernel/sakai-kernel-common/1.0RC2-SNAPSHOT/sakai-kernel-common-1.0RC2-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/kernel/sakai-kernel-component/1.0RC2-SNAPSHOT/sakai-kernel-component-1.0RC2-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/sakaiproject/kernel/sakai-kernel-private/1.0RC2-SNAPSHOT/sakai-kernel-private-1.0RC2-SNAPSHOT.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/log4j/log4j/1.2.9/log4j-1.2.9.jar"

##### JDBC DRIVER #####
##### SUPPLY PATH TO YOUR JDBC DRIVER #####
## MYSQL ##
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/mysql/mysql-connector-java/3.1.14/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"
CLASSPATH="$CLASSPATH:$CATALINA_HOME/shared/lib/mysql-connector-java-5.0.5-bin.jar"
## ORACLE ##
CLASSPATH="$CLASSPATH:$CATALINA_HOME/common/lib/ojdbc-14.jar"

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
	org.sakaiproject.util.conversion.UpgradeSchema "$@" 
