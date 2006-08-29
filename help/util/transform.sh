#! /bin/sh

## These JARS should be on the CLASSPATH: xalan-2.6.0.jar, xerces-2.4.0.jar, xercesImpl-2.6.2.jar, xmlParserAPIs-2.6.2.jar

export JAVA_HOME=/usr/lib/jvm/java
export JRE_HOME=/usr/lib/jvm/java/jre

CLASSPATH=/usr/local/src/helpxml/jars
export CLASSPATH

echo Transforming $1

java org.apache.xalan.xslt.Process -in docs/$1.xml -xsl kb-to-help.xsl -out docs/$1.html

