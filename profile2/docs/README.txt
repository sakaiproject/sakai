
WHAT IS IN HERE:
--------------

database/		- contains scripts for creating the database tables if you do not/cannot have auto.ddl turned on
				- also contains conversion scripts for performing conversion between releases.

Creation scripts exist for Profile2 version 1.1 for all supported databases and conversion for Oracle and MySQL.
For releases beyond 1.1 I will endeavour to create upgrade scripts for the other databases, although I cannot vouch 
for their accuracy as they will be diffs against the Hibernate mappings, and I have no way of testing them, sorry.

test-plans/		- test scripts

NEW INSTALLS:
------------

Please set auto.ddl=true in sakai.properties, this is the best way to get the tables setup automatically. 
If you cannot, read on:

For a new install of Profile2, use profile2-ddl-VERSION-VENDOR.sql to setup the database,
	where: 	VERSION is the version of profile2 you are installing
			VENDOR is your database vendor, ie mysql, oracle, postgres...

The scripts are located in the directory for your particular vendor.

Bear in mind that Profile2 requires the 'Common' project, which has its own tables.


UPGRADES:
---------

For upgrades, apply the appropriate conversion scripts for your vendor and version, in succession:
ie to upgrade from Profile2 1.1 to 1.2 on MySQL use profile2-conversion-1.1-1.2-mysql.sql

You must also apply any conversion scripts in the 'common' folder that are relevant to your upgrade path and vendor.

Note: I have only tested the MySQL and Oracle conversion scripts. I do not have access to the other databases so
have not been able to test the conversion scripts. They are provided here in good faith only (or might be missing entirely).
Please check them before you run them, preferably with a DBA for your particular database vendor.

Note also: There is NO upgrade required for moving from 1.2 to 1.3 as no database tables were modified. Hence no SQL scripts for this upgrade.

INDEXES:
--------
Database table indexes are provided in a separate script. For new installs these should be run manually as Hibernate is not adding them.
This is a known issue. For upgrades, the upgrade script will add the indexes for you.


ERRORS/OMISSIONS:
-----------------
For any errors or omissions, please open a Jira ticket at http://jira.sakaiproject.org/jira/browse/PRFL

---
Steve Swinsburg
March 2009