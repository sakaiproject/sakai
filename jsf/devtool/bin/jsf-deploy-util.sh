# $Id: jsf-deploy-util.sh,v 1.3 2005/05/03 21:18:33 janderse.umich.edu Exp $
# quickie script for deploying just the JSF, comment out what you don't want


# edit the following to your settings
MAVEN=~/maven-1.0.2/bin/maven
TOMCAT=~/tomcat
JSF=~/sakai2/jsf
MYTEMP=/tmp/my_deploy_tmp

pushd $JSF

# all Sakai 1 commented out
#cd widgets-1
#$MAVEN
#cd ..
#cd resource-1
#$MAVEN
#cd ..
# we also comment out standalone tools and utilities
#cd devtool
#$MAVEN
#cd ..
#
#cd tool
#$MAVEN
#cd ..
#cd app
#$MAVEN
#cd ..
cd widgets-myfaces
$MAVEN
cd ..
cd widgets-sun
$MAVEN
cd ..
cd widgets
$MAVEN
cd ..
cd resource
$MAVEN
cd ..
cd example
$MAVEN
cd ..

mkdir -p $MYTEMP
echo temp: $MYTEMP

cp */target/*.war $MYTEMP
cp */target/*.jar $MYTEMP
# we're going to remove the v 1 libs and dev tools
# poor man's exclude :)
rm $MYTEMP/*-1*.*ar
rm $MYTEMP/*dev*.*ar


cp $MYTEMP/*.*ar $TOMCAT/webapps
rm $MYTEMP/*

popd
