#!/bin/bash
rm -f editor.jar
rm org/dojo/moxie/*.class
javac -cp .:lib/derby.jar:lib/servlet-api-2.5-6.1.3.jar:lib/jetty-6.1.3.jar:lib/jetty-util-6.1.3.jar:lib/json-lib-1.0b2-jdk13.jar:lib/commons-beanutils.jar:lib/ezmorph-1.0.jar:lib/commons-lang-2.2.jar org/dojo/moxie/*.java 
jar -cmvf manifest editor.jar org/dojo/moxie/*.class lib/*.jar 1>/dev/null
#cp -r ../server ../../../../release/dojo/demos/storage

