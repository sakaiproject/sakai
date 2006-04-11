# OBSOLETE, UPDATED VERSION FOR sakai2/jsf IS jsf-deploy-util.sh, retained until verified no cron.
MAVEN=~/maven-1.0.2/bin/maven
TOMCAT=~/tomcat

pushd ../../widgets

$MAVEN
# Next line not necessary since webapps that use our JSF widgets 
# must include the widget jar file in their webapp/lib; it should
# not be in shared/lib.
# cp target/*.jar $TOMCAT/shared/lib

cd ../resource
$MAVEN
cp target/*.war $TOMCAT/webapps

cd ../example
$MAVEN
cp target/*.war $TOMCAT/webapps

popd
