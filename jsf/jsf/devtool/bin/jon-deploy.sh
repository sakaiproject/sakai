# $Id: jon-deploy.sh,v 1.1 2005/05/03 21:18:34 janderse.umich.edu Exp $
# simpler quickie script for deploying Sakai JSF.  Comment out what you don't want.

# edit the following to your settings
MAVEN=~/maven-1.0.2/bin/maven
TOMCAT=~/tomcat/
JSF=~/sakai2/jsf/
# shouldn't have to change this setting, ever.
MAVEN_REPO_REMOTE=http://www.ibiblio.org/maven/,http://cvs.sakaiproject.org/maven/
pushd $JSF

# Make sure the Sakai Maven plugin is downloaded and installed
# Note: This only needs to run ONCE, but we'll run it in this script 
# every time just to be sure.
$MAVEN plugin:download -DgroupId=sakaiproject -DartifactId=sakai -Dversion=sakai.2.0.0 -Dmaven.repo.remote=$MAVEN_REPO_REMOTE

# This cleans, compiles, and deploys the JSF projects.
$MAVEN sakai -Dmaven.repo.remote=$MAVEN_REPO_REMOTE -Dmaven.tomcat.home=$TOMCAT

popd
