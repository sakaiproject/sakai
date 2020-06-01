Sakai GoogleDrive Integration
==========================

Overview
--------
This integration is part of the cloud storage options provided by Sakai.


User Guide
----------
Please read the README file on parent folder.


APP Registration
----------------
In order to make this integration work, you'll have to register an APP via Google's developers portal:

	1. Access https://console.developers.google.com/apis/dashboard using your Google account login.
		1b. Optionally you can create a new project or use a existing one.
	2. On the APIs and Services menu, click on "ENABLE APIS AND SERVICES".
	3. Select Google Drive API, enable.
	4. Access Credentials / Create Credentials / Id Oauth client.
	5. Click on Configure authentication screen, if you haven't done it before.
	6. Give the APP a name and Save.
	7. Select Web, give the Client a name.
	8. The redirect uri (Web) must match the one you add on your sakai.properties files, by default it is "https://YOUR-SERVER-URL/sakai-googledrive-tool".
	9. Click on "Save". Get the Oauth client ID and secret, as you'll have to set them on your sakai.properties (googledrive.client_id and googledrive.client_secret).
	
	
Sakai Configuration
-------------------
Once you have your App successfully registered, you can make use of the related properties. The one you'll need for enabling this functionality on sakai are these:

	googledrive.enabled=true
	googledrive.client_id=--CLIENT_ID--
	googledrive.client_secret=--SECRET--
	googledrive.redirect_uri=${serverUrl}/sakai-googledrive-tool

	
Special features
----------------

Attach: 		Files created using Google Drive can't be downloaded (only exported), for this reason these files won't show the Attach option on the filepicker.

Permissions: 	Google doesn't provide a public url for the files of a Drive account.
				For this reason, after linking a doc using the filepicker (and depending on the permissions of the document), users accessing it might be redirected to a Google "Request access" screen.