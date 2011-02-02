
WHAT IS IN HERE:
--------------

database/		- contains scripts for creating the database tables if you do not/cannot have auto.ddl turned on
				- also contains conversion scripts for performing conversion between releases.

NEW INSTALLS:
------------

Please set auto.ddl=true in sakai.properties, this is the best way to get the tables setup automatically. 
If you cannot, read on:

For a new install, use shortenedurl-ddl-VERSION-VENDOR.sql to setup the database,
	where: 	VERSION is the version of this application you are installing
			VENDOR is your database vendor, ie mysql, oracle, postgres...

The scripts are located in the directory for your particular vendor.

UPGRADES:
---------

For upgrades, apply the appropriate conversion scripts for your vendor and version, in succession:
ie to upgrade from ShortenedUrlService 1.0 to 1.1 on MySQL use shortenedurl-conversion-1.1-1.2-mysql.sql

INDEXES:
--------
Database table indexes are provided in a separate script. For new installs these should be run manually as Hibernate is not adding them.
This is a known issue. For upgrades, the upgrade script will add the indexes for you.


ERRORS/OMISSIONS:
-----------------
For any errors or omissions, please open a Jira ticket at http://jira.sakaiproject.org/jira/browse/SHORTURL

---
Steve Swinsburg
February 2011