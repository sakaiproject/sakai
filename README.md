# Sakai CLE

This is the source code for the Sakai CLE.

## Building

To build Sakai you need Java 1.7 and Maven 3.2. Once you have clone a copy of this repository you can
build it by running:
```
mvn install
```

## Running

Sakai runs on Apache Tomcat 7. Download the latest version from http://tomcat.apache.org and extract the archive.
*Note: Sakai does not work with Tomcat installed via a package from apt-get, yum or other package managers.*

Deploy Sakai to Maven:
```
mvn clean install sakai:deploy -Dmaven.tomcat.home=/path/to/your/tomcat
```

Start Tomcat:
```
cd /path/to/your/tomcat/bin
./startup.sh && tail -f ../logs.catalina.out
```

Once Sakai has started up (it usually takes around 30 seconds), open your browser and navigate to http://localhost:8080/portal

## Contributing

To contribute to the Sakai project please follow the workflow on:
https://confluence.sakaiproject.org/display/SAKDEV/Git+Setup

## Bugs

For filing bugs against Sakai please use our Jira instance: https://jira.sakaiproject.org/

# Get in touch

If you have any questions, please join the Sakai developer mailing list: http://collab.sakaiproject.org/mailman/listinfo/sakai-dev
