Sakai 2.x readme.txt


Starting the demo
-----------------------------------------------------

1) Change directory to this sakai-demo folder.

2) Start up tomcat:

<windows>

      start-sakai.bat

<mac, linux>

      ./start-sakai.sh


Logs from the demo
-----------------------------------------------------

The logs are in the logs folder.  Tomcat's catalina.out has the most interesting entries.


Accessing Sakai
-----------------------------------------------------

Once your Sakai is started, open a browser and enter the following URL:

http://localhost:8080/portal

This will bring you to the Sakai gateway site.


Logging into to Sakai
-----------------------------------------------------

One user account is included in the demo, the administrator's account.  Login using user id "admin" and password "admin".

New user accounts can be created from the admin's User tool, or using the "new account" link on the Sakai gateway site.


Stopping the demo
-----------------------------------------------------

1) stop tomcat:  

<windows>

      stop-sakai.bat

<mac, linux>

      ./stop-sakai.sh


Data from the demo
-----------------------------------------------------

The data is stored in a HypersonicSql database located:

sakai-demo/sakai/db/sakai.db.*

Objects created and modified in runs of the Sakai demo will persist through server restarts.

HSQL is not appropriate for running Sakai in any sort of production or even medium to large scale demonstration and evaluation environments.  For these, we recommend MySql or Oracle.


Not in the demo
-----------------------------------------------------

1) email detection in Sakai (i.e. mail sent to the Sakai server) is not enabled.

2) email from Sakai (i.e. mail sent from Sakai) is not configured.

This requires providing Sakai with an SMTP server to use. If you want to do this, you can edit the sakai.properties found in sakai-demo/sakai and add:

	smtp@org.sakaiproject.email.api.EmailService=<SMTP>

where <SMTP> is replaced with the name or ip address of an SMPT server that will accept mail from your Sakai app server.


Problems getting started
-----------------------------------------------------

1) Sakai's Tomcat will run on port 8080, and also make use of ports 8005 and 8009.
   If another process is running on any of these ports, Sakai will not start up.

2) Sakai's Tomcat will find itself relative to the startup directory.  Always start it from the sakai-demo folder.
   If you have an environment variable CATALINA_HOME set to another Tomcat, this will interfere with Sakai's startup.
