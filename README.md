# Sakai CLE

This is the source code for the Sakai CLE.

## Building

[![Build Status](https://travis-ci.org/sakaiproject/sakai.svg?branch=master)](https://travis-ci.org/sakaiproject/sakai)


To build Sakai you need Java 1.8 and Maven 3.2. Once you have clone a copy of this repository you can
build it by running:
```
mvn install
```

## Running

Sakai runs on Apache Tomcat 8. Download the latest version from http://tomcat.apache.org and extract the archive.
*Note: Sakai does not work with Tomcat installed via a package from apt-get, yum or other package managers.*

You **must** configure Tomcat according to the instructions on this page:
https://confluence.sakaiproject.org/pages/viewpage.action?pageId=75667828

When you are done, deploy Sakai to Tomcat:
```
mvn clean install sakai:deploy -Dmaven.tomcat.home=/path/to/your/tomcat
```

Now start Tomcat:
```
cd /path/to/your/tomcat/bin
./startup.sh && tail -f ../logs.catalina.out
```

Once Sakai has started up (it usually takes around 30 seconds), open your browser and navigate to http://localhost:8080/portal

## Licensing

Sakai is licensed under the [Educational Community License version 2.0](http://opensource.org/licenses/ECL-2.0) 

Sakai is an [Apereo Foundation](http://www.apereo.org) project and follows the Foundation's guidelines and requirements for [Contributor License Agreements](https://www.apereo.org/licensing).

## Contributing

See [our dedicated page](CONTRIBUTING.md) for more information on contributing to Sakai.

## Bugs

For filing bugs against Sakai please use our Jira instance: https://jira.sakaiproject.org/

## Nightly servers 
For testing out the latest builds go to the [nightly server page](http://nightly2.sakaiproject.org)

## Get in touch
If you have any questions, please join the Sakai developer mailing list: To subscribe send an email to sakai-dev+subscribe@apereo.org

To see a full list of Sakai email lists and the status of the migration to Apereo Google Groups check out this Sakai wiki page:
https://confluence.sakaiproject.org/display/PMC/Sakai+email+lists
