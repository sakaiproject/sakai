Sakai Cloud Storage
===================

Overview
--------
This module provides an integration within Sakai for implementations of different Cloud storage solutions.

At the moment, the current supported cloud providers are:

	- Microsoft OneDrive.
	- Google Drive.


User Guide
----------
These modules are associated with Sakai's filepicker, so it can be used all across the platform. If you enable any of the cloud providers, you will notice a new tab on the filepicker screen.
The first time a user accesses that section, they'll have to click on the Configurate button in order to add their login data.
They'll be redirected to the provider's login screen and, once validated and accepted, they'll be redirected back to a Sakai confirmation screen.
From that point on, every time they enter that screen they will be able to browse, navigate, link or copy all the files from each provider.
If they click on Revoke, the account will be unlinked and they'll have to configure it again.
The file and folder listing is cached in order to save calls to the API, but they can get the current status at anytime by clicking on the Refresh button.

Sakai Configuration
-------------------
For each available implementation, you'll have to enable it by using the corresponding Sakai properties. For more information, please read their own README file.