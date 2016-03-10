#!/bin/bash
#
# Usage:
#   ./runconversion.sh -j JDBC_DRIVER_JAR -k KERNEL_VERSION -s SAKAI_VERSION -m /path/to/m2/repository -p SAKAI_PROPERTIES_FILE UPGRADESCHEMA_CONFIG
#
# Example:
#	./runconversion.sh -j "$CATALINA_HOME/common/lib/mysql-connector-java-3.1.14-bin.jar"  \
#		-p "$CATALINA_HOME/sakai/sakai.properties" \
#		-k 1.0.3   \
#		-s 2.6.0 \
#		-m ~/.m2/repository upgradeschema_mysql.config	

# The "runconversion.sh" script does not have to be run
# in place. It could be copied to the working directory.

# For Cygwin, ensure paths are in the proper format.
cygwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
esac
if $cygwin; then
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
  m2repository=`cygpath --path --unix "$HOMEDRIVE""$HOMEPATH"`/.m2/repository
else
  m2repository="$HOME"/.m2/repository
fi

usage()
{
      cat<<USAGE;
usage:
      -h this message
      -j extrajarfile
             should include your JDBC JAR; may be specified multiple times (e.g., for MySQL)
      -k kernelversion
             version of kernel being used
      -s sakaiversion
             version of sakai being used (from master/pom.xml)
      -m maven2repository
             directory for maven2 repoository (DEFAULT: ~/.m2/repository)
      -p sakaipropertiesfile
             may be used to set up database connections
      properties
             the configuration file
eg
    `basename $0` -j $CATALINA_HOME/shared/lib/ojdbc14.jar -k 1.0.3 -s 2.6.0 -m /tmp/m2/repository convertcontent.config
USAGE
}

while getopts 'hj:p:m:k:s:' OPTION
do
  case $OPTION in
  j) CLASSPATH="$CLASSPATH":"$OPTARG"
	;;
  p) JAVA_OPTS="$JAVA_OPTS -Dsakai.properties=$OPTARG"
	;;
  m) m2repository="$OPTARG"
	;;
  k) KERNELVERSION="$OPTARG"
	;;
  s) SAKAIVERSION="$OPTARG"
	;;
  h) usage
     exit 0
	;;
  \?) usage
      exit 2
	;;
  esac
done
shift $(($OPTIND - 1))

##### ASSIGNMENT SPECIFIC STUFF #####
#CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/sakai-mailarchive-impl/$SAKAIVERSION/sakai-mailarchive-impl-$SAKAIVERSION.jar"

CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/sakai-assignment-api/$SAKAIVERSION/sakai-assignment-api-$SAKAIVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/sakai-assignment-impl/$SAKAIVERSION/sakai-assignment-impl-$SAKAIVERSION.jar"

##### COMMON KERNEL STUFF #####
CLASSPATH="$CLASSPATH:"$m2repository"/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/commons-codec/commons-codec/1.3/commons-codec-1.3.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/commons-dbcp/commons-dbcp/1.2.2/commons-dbcp-1.2.2.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/commons-pool/commons-pool/1.3/commons-pool-1.3.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/kernel/sakai-kernel-api/$KERNELVERSION/sakai-kernel-api-$KERNELVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/kernel/sakai-kernel-impl/$KERNELVERSION/sakai-kernel-impl-$KERNELVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/kernel/sakai-kernel-util/$KERNELVERSION/sakai-kernel-util-$KERNELVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/kernel/sakai-kernel-common/$KERNELVERSION/sakai-kernel-common-$KERNELVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/kernel/sakai-kernel-component/$KERNELVERSION/sakai-kernel-component-$KERNELVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/org/sakaiproject/kernel/sakai-kernel-private/$KERNELVERSION/sakai-kernel-private-$KERNELVERSION.jar"
CLASSPATH="$CLASSPATH:"$m2repository"/log4j/log4j/1.2.9/log4j-1.2.9.jar"

# For Cygwin, ensure paths are in the proper format.
if $cygwin; then
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
fi

java $JAVA_OPTS  \
      -classpath "$CLASSPATH" \
      org.sakaiproject.util.conversion.UpgradeSchema "$@"

exit 0
