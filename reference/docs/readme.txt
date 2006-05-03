INTRODUCTION

Welcome to the Sakai Release for version 2.1.0! The new functionality of the 2.1.0 release is marked most prominently by the inclusion of course sections and making several of the bundle tools "section-aware," but there are other new tools and many improvements to the code. See the documentation identified below for fuller details.


TABLE OF CONTENTS

1. Installation Overview
1.1 Choose an Install Type
1.2 Demo Installation
1.3 Binary Installation
1.4 Source Installation
2. Sakai Documentation
3. Provisional Tools


1. INSTALLATION OVERVIEW

1.1 Choose an Install Type:

Sakai is available for three different installation methods.

    * Demo: this archive provides the quickest and easiest way to try out Sakai. It includes the Tomcat container and pre-built webapps, so that the only prerequisite for getting started is Java (version 1.4.2).
    * Binary: this archive is a kind of Tomcat overlay. If you have a working Tomcat appropriately configured, the binary installation provides a quick way to drop the Sakai application bundle into place without modification. This may be a good choice if you're already comfortable with Sakai installation and configuration, but it is not recommended for a first look at the software.
    * Source: this archive includes only the source code to build Sakai, and therefore calls for separate, preparatory installations of Tomcat and the Maven build tool, along with (most likely) a preferred database.


1.2 Demo Installation:

A demo installatation is fairly brief and straightforward. The steps are as follows:

   1. Verify Java Installation and Environment Variables (see install/readme_demo.txt, or the "Set up Build Environment" section of the online install guide).
   2. Download and unpack the Demo Archive (see http://cvs.sakaiproject.org/release/2.1.0 for a link to the download)
   3. Start Tomcat with the following commands from the root Tomcat directory:
   
      Windows: start-sakai.bat
      Mac/*nix: start-sakai.sh
      
      Once Tomcat is started up, you can point your browser to

      http://localhost:8080/portal

      This will bring you to the Sakai gateway site where you can log in using the default administrator's account (username="admin" and password="admin"). New user accounts can be created from the admin "User" tool, or using the "New Account" link on the Sakai gateway site. New sites can be created using the admin "Worksite Setup" tool.

      To stop the demo:

      Windows: stop-sakai.bat
      Mac/*nix: stop-sakai.sh

**Demo Data Storage: The data for a Demo installation is stored in a HypersonicSql database (HSQLDB) which is stored in memory and then written out to files when Tomcat is shut down. These files are located at:

sakai-demo/sakai/db/sakai.db.*

Objects created and modified during runs of the Sakai demo will persist through server restarts. [Warning: HSQLDB is not appropriate for running Sakai in any sort of production environment. For such cases we recommend MySql or Oracle (see Database Configuration)]

**Email not configured in the Demo:

    * email detection in Sakai (i.e. mail sent to the Sakai server) is not enabled.
    * email from Sakai (i.e. mail sent from Sakai) is not configured. This would require providing Sakai with an SMTP server to use. See Post-Installation Configuration for more details.


1.3 Binary Installation:

The Binary Installation of Sakai provides a shortcut for those that already have Tomcat in place and configured as needed (see install/readme_bin.txt, or the "Set up Build Environment" section of the online install guide), and it does so by providing a pre-built Sakai that can simply be dropped into Tomcat as an overlay. All you need to do is unpack the Binary archive from within the root Tomcat directory (e.g. $CATALINA_HOME). The appropriate wars, jars, etc., will be deposited in the correct locations.

If you wish to configure a binary installation you'll need to manually create a 'sakai' directory in $CATALINA_HOME to hold your *.properties files, as described in Post-Installation Configuration.


1.4 Source Installation

The Demo and Binary installs described above offer much-abbreviated processes, but they assume that no special configuration is called for. The installation guide available here in the docs folder and on the release page is mainly devoted to the more rigorous and configurable Source installation, the steps of which may be organized into the following phases:

   1. Set up the build environment, including Java, Tomcat, and Maven.
   2. Build and deploy Sakai by running Maven on the source.
   3. Perform appropriate Post-Installation Configuration of the application.
   4. Carry out the steps for Database Configuration.

The Demo and Binary Installations can freely skip most of these phases, with exceptions noted above. To view detailed breakdowns of these phases please refer to docs/install/readme_source.txt (or the Installation Guide online at http://cvs.sakaiproject.org/release/2.1.0/InstallGuide.html).


2. SAKAI DOCUMENTATION

Sakai documentation comes in many forms.  The first and primary set of documentation is what's found here in the docs folder (the demo and binary archives may not include all of these, but you can find them in subversion at https://source.sakaiproject.org/svn/trunk/sakai/docs/):

* architecture - this holds much of the deeper, and more exhaustive documentation of Sakai's internals and configurations.  Turn to these docs when you have a working Sakai installation and want to learn more.
* install - speaks for itself.
* upgrading - notes on upgrade procedures and sql scripts will be stored here when a new release warrants it.
* provisional - introductory notes explaining how to enable provisional tools, and what their basic functions are (see the provisional tool section below).

Sakai documentation is also maintained in a more informal way on Confluence, the wiki software that the Sakai project currently uses for the sharing and collaborative forging of documents.  You can find it at http://bugs.sakaiproject.org/confluence (you are encouraged to create an account and participate).  The Sakaipedia is a good source of community information on specific topics, and the "Documentation" space is an area where official documentation is commented on and revised in an ongoing way.

The Sakai javadocs and tag libraries are also available online:

http://cvs.sakaiproject.org/release/2.1.0/javadoc/
http://cvs.sakaiproject.org/release/2.1.0/taglibdoc/

Or as a zipped download:

http://cvs.sakaiproject.org/release/2.1.0/javadoc/sakai-javadoc.zip
http://cvs.sakaiproject.org/release/2.1.0/taglibdoc/sakai-taglibdoc.zip


3. PROVISIONAL TOOLS

A provisional tool is one which is included in the release distribution, but is not considered to be an official part of the enterprise bundle, and such tools do not come enabled by default. Additional manual steps are required to enable these additional tools, per the discretion of the deploying institutions or individuals.

The basic idea of the provisional release is to allow sites to test these new capabilities for a release before promoting them to be part of the official release. Your feedback on the suitability of these tools for inclusion in an upcoming Sakai release is an important part of this process.

You are encouraged to use these tools taking careful care to test them in your environment and with your users. Some of the tools can be partially enabled to give you a way to roll out the tools to a subset of the users on your system.

The provisional tools available in 2.1.0 are:

    * Rwiki - a wiki tool
    * SU - an administrative tool for taking on the identity of another system user
    * Sakaiscript - a set of webservice endpoints for manipulating Sakai data
    * Roster - a tool for displaying the class roster, and potentially photos with the proper provisioning
    * TwinPeaks - a tool for searching repositories 
    
See the readmes in docs/provisional for details.
