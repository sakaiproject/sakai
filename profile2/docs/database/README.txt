
What is in here:
--------------

database/		- contains scripts for creating the database tables if you do not have auto.ddl on
				- also contains conversion scripts for performing conversion between releases.

Creation scripts exist for version 1.1 and for the conversion from 1.0.1 to 1.1


Note that there are no scripts to create the databases for 1.0 not to convert to 1.0.1 as these versions
have been superseded by 1.1. The release of 1.0.1 was very soon after 1.0 and no one has requested any conversion documents. 
If you had auto.ddl=true then your databse would ahve automatically been upgraded as the only change was that 
new tables were added, no existing tables were altered.


---
Steve Swinsburg
March 2009