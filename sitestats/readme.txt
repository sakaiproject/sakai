Site Stats Tool
--------------------------
1. About
2. Building/deploying
3. Updating from previous releases
4. Permission-sensitive pages/tools
5. Using the tool
6. Known bugs/issues
7. Contact



1. About
-----------------------
Site Stats is a tool for Sakai for showing site statistics by user, event, or resource.
The tool’s main page shows a weekly/monthly/yearly snapshot of activity in the site 
and simple statistics (totals and averages).

Currently, it has an overview page with summary information about site visits, unique site
visits and activity, an events page listing site events per user, a resources page listing
site resources accessed by user, and a preferences page.
There is also an administrator version of the tool, with a site list view.

This tool is still in development. Somewhere in the future, an implementation will be added
to gather data from the new (future) Sakai Warehouse.
Feel free to send suggestions to ufpuv-suporte@ufp.pt or by using the SiteStats JIRA section.


2. Building / Deploying
-----------------------
The standard way in Sakai 2.x is to copy the source folder into the Sakai
source tree and run maven on it ('maven sakai').
Alternatively, you can place this tool source folder in other folder as long
as you link '../master' to the 'master' folder of the Sakai source tree (Sakai
uses a master project descriptor file at '../master/project.xml').


3. Updating from previous releases
-----------------------
  1. Undeploy previous SiteStats deployments (folder strucutre and jar/war filenames were changed)
  2. Please run the appropriate database conversion script located in the 'updating/' folder.
  3. Build/deploy with maven


4. Permission-sensitive pages/tools
-----------------------
Sakai currently does not support permission-sensitive pages although there is already a feature request 
for this (SAK-4120). Meanwhile, and while there is no native support for this, you can use the custom 
solution explained in http://bugs.sakaiproject.org/confluence/display/STAT


5. Using the Tool
-----------------------
Use the 'Site Info' tool to add SiteStats to a site.
In the events and resources page, events can searched by user ID or name, filtered by groups,
or by time period.
The preferences page allows a user to select events to be displayed in Events page and to count
as Activity in the Overview page.
 

6. Known bugs/issues/limitations
-----------------------
- Assignments tool: SAK-4315: When grading in Assignments, 'asn.submit.submission' events are logged to SAKAI_EVENT
instead of 'asn.grade.submission' (FIXED in r11229);
- Email archive: unable to process email attachments (no reference to site id);
- Discussion tool: SAK-5340: Category related events are always logged as 'disc.null' (FIXED in r11194);
- Message Center: SAK-5341: Events logged should have unique ID and normalized reference;
- SiteStats is not cluster-aware (yet);


7. Contact
-----------------------
SiteStats is written by Nuno Fernandes at Universidade Fernando Pessoa.
If you wish, feel free to submit patches or any other contributions.
You may contact us at ufpuv-suporte@ufp.pt and nuno@ufp.pt.
