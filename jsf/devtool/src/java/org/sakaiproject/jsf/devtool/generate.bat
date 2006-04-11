
SET DEPS=%MAVEN_REPO%\velocity\jars\velocity-1.3.1.jar

java -classpath "c:\w\jsf\bin;%DEPS%" org.sakaiproject.jsf.devtool.JSFGenerator %1

