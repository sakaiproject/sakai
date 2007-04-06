#!/bin/sh
# -----------------------------------------------------
# Run with -help for usage.
# If $JAVA_HOME is set, editing this script should not be required.
# Send any questions to fchoong@user.sourceforge.net
# -----------------------------------------------------

# the value set here will override the value passed by $JAVA_HOME or the -jdkhome switch
jdkhome=""
jargs=""
thread_flag=""

PRG=$0

#
# resolve symlinks
#

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname $PRG`/$link"
    fi
done

progdir=`dirname $PRG`
progname=`basename $0`

# ../.. will lead us to the home
dbhome="$progdir/../.."

# absolutize dbhome

dbhome=`cd ${dbhome}; pwd`

#
# bring in needed functions

. ${dbhome}/lib/functions

#--------------------------------------------------------------------------------------------------------------
pre_main

#
# let's go
# This presumes that hsqldbtest.jar was built in ${dbhome}/lib.
# If you haven't done this run "ant jartest" in the ${dbhome}/build directory.
#
    cp=$cp:${dbhome}/lib/hsqldbtest.jar
    exec "$jdkhome/bin/java" $thread_flag -classpath "$cp" $jargs "org.hsqldb.test.$@"
# and we exit.
