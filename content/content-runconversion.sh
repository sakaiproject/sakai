#!/bin/sh
#
# Usage:
#   content-runconversion.sh -j JDBC_DRIVER_JAR -p SAKAI_PROPERTIES_FILE UPGRADESCHEMA_CONFIG
#
# Example:
#   content-runconversion.sh -j "$CATALINA_HOME/shared/lib/ojdbc14.jar" \
#      -p "$CATALINA_HOME/sakai/sakai.properties" \
#      upgradeschema-oracle.config

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

bash ../db/db-util/conversion/runconversion.sh \
	-j "$m2repository"/commons-collections/commons-collections/3.2/commons-collections-3.2.jar \
	-j "$m2repository"/commons-pool/commons-pool/1.3/commons-pool-1.3.jar \
	-j "$m2repository"/org/sakaiproject/sakai-db-api/SNAPSHOT/sakai-db-api-SNAPSHOT.jar \
	-j "$m2repository"/org/sakaiproject/sakai-component-api/SNAPSHOT/sakai-component-api-SNAPSHOT.jar \
	$@
