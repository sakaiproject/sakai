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

## Community supported versions
[Sakai 10](https://confluence.sakaiproject.org/display/DOC/Sakai+10+Release+Notes) is the recommended version for production use.

[Sakai 2.9](https://confluence.sakaiproject.org/display/DOC/Sakai+2.9+release+notes) is still supported, almost solely for security patches at this point in time.

## Under Development
[Sakai 11] (https://docs.google.com/presentation/d/1OaF04Kli51nYtsLw_0plmU829NarYUDtQkBnFJtB250/edit#slide=id.p4) is under development. Expected release is first half of 2016. Some of the key features include Responsive Design (code name Morpheus); spreadsheet entry Gradebook; improved Assessment (test and quizzes) delivery options to accommodate special needs; PA system for managing system-wide announcements; a Dashboard summary of activities and assignments for more efficient student access to content; user interface enhancements to Lessons, a community favorite tool that allows instructors to organize their course activities the way they would like, by leveraging other Sakai tools and adding workflow and adaptive release options.

## Community (contrib) tools
A number of institutions have written additional tools for Sakai that they use in their local installations, but are not yet in an official release of Sakai. These are being collected at https://github.com/sakaicontrib where you will find information about each one. You might find just the thing you are after!


