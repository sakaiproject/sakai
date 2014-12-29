Site Stats tool
============================================
Always refer to http://bugs.sakaiproject.org/confluence/display/STAT
for detailed and most updated information.
============================================

1. About
2. Updating from previous releases
3. Installation instructions
4. Tool security (permissions)
5. Using the tool
6. Known bugs/issues
7. Contact



1. About
============================================
Site Stats is a tool for Sakai for showing site usage statistics.

Tool information is organized in 2 screens:
	- Overview: contains summary information about site visits and activity;
	- Reports:  generated exportable reports about site visits, tool and resource activity.
		
Tool support can be added/removed sakai-wide (by using configuration files) or on site basis (by
using the tool Preferences screen). See SiteStats Confluence page for configuration instructions.

Feel free to send suggestions to ufpuv-suporte@ufp.pt or by using the SiteStats JIRA section.


2. Updating from previous releases
============================================
  1. Undeploy previous SiteStats deployments (folder structure and jar/war filenames were changed)
  2. Please run the appropriate database conversion script located in the 'updating/' folder.
  3. Follow next step (Installation)


3. Installation instructions
============================================
3.1 Requirements
	* JDK 1.5+ installed
	* For MAVEN 1 deployments, the following must be manually added to local maven repository:
		* Ajax4JSF 1.1.1:
			- copy from {sitestats_src}/lib/ajax4jsf-1.1.1.jar to ~/.maven/repository/org.ajax4jsf/jars/
		* xmlgraphics-commons 1.1
			- copy from {sitestats_src}/lib/xmlgraphics-commons-1.1.jar to ~/.maven/repository/org.apache.xmlgraphics/jars/
		
3.2 Build/deploy
	* Copy the sitestats source folder into the Sakai source tree
	* Be sure to shutdown tomcat before proceeding
	* Be sure to read the SiteStats confluence page (link at top) for configuration instructions
	* Enter sitestats source folder, compile and deploy it:
		for Maven 1 deployments:
			maven -Dmaven.tomcat.home=$CATALINA_HOME sakai (for Maven 1 deployments)
		for Maven 2 deployments:
			mvn -Dmaven.tomcat.home=CATALINA_HOME clean install sakai:deploy
		(replace $CATALINA_HOME by your tomcat path)
		
3.3 Adding the tool
	* Startup tomcat if not started yet
	* Use the 'Worksite setup' or the 'Site Info' tool to add SiteStats to a sakai site:
		- click 'Add tools' link
		- select SiteStats tool
		- click 'Continue' to save changes


4. Tool security (permissions)
============================================
For SiteStats 0.5.5 or higher, the 'sitestats.view' permission IS REQUIRED in order to use the tool.
Please either add the permission to the '!site.helper' realm or use a script to update existing realms.
Access to the sitestats administrator view can also be granted to non-administrator users by adding the
permission 'sitestats.view.admin'.
The tool won't be listed on left menu if the user doesn't have the required permission.


5. Using the Tool
============================================
Please see http://bugs.sakaiproject.org/confluence/display/STAT for detailed information.
 

6. Known bugs/issues in SiteStats 1.0.0
============================================
For up-to-date information, please see http://bugs.sakaiproject.org/confluence/display/STAT/Features.


7. Contact
============================================
SiteStats was origionaly written by Nuno Fernandes at Universidade Fernando Pessoa.
If you wish, feel free to submit patches or any other contributions.
To learn more about how to contribute to Sakai please see: http://sakaiproject.org/technical-contributors
