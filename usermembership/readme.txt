User Membership Tool 
-----------------------
1. About
2. Building/deploying
3. Using the tool
4. Known bugs/issues
5. Contact



1. About
-----------------------
User Membership is a tool for Sakai for administrators to see which 
sites and groups an user belongs to.


2. Building / Deploying
-----------------------
The standard way in Sakai 2.x is to copy the source folder into the Sakai
source tree and run maven on it ('maven sakai').
Alternatively, you can place this tool source folder in other folder as long
as you link '../master' to the 'master' folder of the Sakai source tree (Sakai
uses a master project descriptor file at '../master/project.xml').
There is no need to stop Sakai to deploy User Membership tool.

For more information: https://elearning.ufp.pt/wiki/index.php/UFP_Sakai_tools


3. Using the Tool
-----------------------
Use the 'Sites' tool to add User Membership to the 'Administration Workspace'
site. Tool id is 'sakai.usermembership'.

You can search by user type, id, first name, last name or email. The result is a list
of sites and groups the user belongs to - clicking on a site link will open that site.

This tool verifies that the user has administrative privileges and logs an error
otherwise. For aditional security it can be added to the following property in
sakai.properties:
  stealthTools@org.sakaiproject.api.kernel.tool.ActiveToolManager=sakai.usermembership
 

4. Known bugs/issues
-----------------------
At the time of this writing there are no known bugs.


5. Contact
-----------------------
UserMembership is written by Nuno Fernandes at Universidade Fernando Pessoa.
If you wish, feel free to submit patches or any other contributions.
You may contact us at ufpuv-suporte@ufp.pt and nuno@ufp.pt.
