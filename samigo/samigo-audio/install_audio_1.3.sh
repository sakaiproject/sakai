#!/bin/csh
(cd ~/sakai/sam/samigo-audio/src/java/org/sakaiproject/tool/assessment/audio; /usr/pubsw/apps/jdk-1.3.1/bin/javac *.java)
(cd ~/sakai/sam/samigo-audio/; mkdir -p audio-1.3/org/sakaiproject/tool/assessment/audio/; mv src/java/org/sakaiproject/tool/assessment/audio/*.class audio-1.3/org/sakaiproject/tool/assessment/audio/; cp src/java/org/sakaiproject/tool/assessment/audio/*.properties audio-1.3/org/sakaiproject/tool/assessment/audio/;)
(cd ~/sakai/sam/samigo-audio/audio-1.3; fastjar cvf sakai-samigo-audio-1.3-dev.jar org)
(cd ~/sakai/sam/samigo-audio/audio-1.3; cp sakai-samigo-audio-1.3-dev.jar ~/tomcat/webapps/samigo/applets )
(cd ~/ ; ./signApplet.sh) 
