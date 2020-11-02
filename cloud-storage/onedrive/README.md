Sakai OneDrive Integration
==========================

Overview
--------
This integration is part of the cloud storage options provided by Sakai.


User Guide
----------
Please read the README file on parent folder.


APP Registration
----------------
In order to make this integration work, you'll have to register an APP via Microsoft's Azure portal:

	1. Access https://portal.azure.com/#blade/Microsoft_AAD_RegisteredApps/ApplicationsListBlade using your Microsoft account login.
	2. On the App registrations menu, click on "New registration".
	3. Introduce a unique name for your application.
	4. The redirect uri (Web) must match the one you add on your sakai.properties files, by default it is "https://YOUR-SERVER-URL/sakai-onedrive-tool".
	5. Click on "Register". Get the Application (client) ID, as you'll have to set it on your sakai.properties (onedrive.client_id).
	6. Under Call APIs, click on "View Api Permissions".
	7. Click on "Add a permission". You'll need to allow two Microsoft Graph delegated permissions: "User.Read" and "Files.Read.All".
	8. Access the "Certificates & secrets" section on the menu. Click on "New client secret" and choose your preferred expiration configuration.
		NOTE: You can only copy the secret's value just after creating it, and you can only have one valid secret at any moment.
	9. Click on "Add" and copy the Value for the generated client secret, you'll have to set it on your sakai.properties (onedrive.client_secret).


Sakai Configuration
-------------------
Once you have your App successfully registered, you can make use of the related properties. The one you'll need for enabling this functionality on sakai are these:

	onedrive.enabled=true
	onedrive.client_id=--CLIENT_ID--
	onedrive.client_secret=--SECRET--
	onedrive.redirect_uri=${serverUrl}/sakai-onedrive-tool
	onedrive.endpoint_uri=https://login.microsoftonline.com/{TenantID}/oauth2/v2.0/
