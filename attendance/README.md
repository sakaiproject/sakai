# Attendance
A simple [Sakai](https://github.com/sakaiproject/sakai) tool for tracking attendance that integrates with the Gradebook.

## Performance Improvements - MUST READ
With the release of 20170215, statistics are now stored in a table. As such, a job was created to calculate these stats.
This job should be run once after the deployment of 20170215; afterwards it may be run as needed or at regular interval
as determined by the administrators. The job **MUST BE** run at least once to calculate & save the statistics to begin with, afterwards the job is only needed to be run to sync up the stats to the current roster (in
the case of users attending an item and then later leaving the site). This job is titled "Attendance Stat Calc - SEE DOCS".

## Resources
Pages: http://sakaicontrib.github.io/attendance/

Presentation:
http://prezi.com/m3dvmxokf8as/ - Delivered at Apereo 16.

## Compatibility
Version 1.0+ of Attendance is compatible with Sakai 11. Though, there are some UI changes which are still necessary. 

The Sakai property auto.ddl should be set to true when first starting this tool with Sakai.
If not, queries for MySQL and Oracle can be found in [docs/sql/](docs/sql/), though use at your own risk.

## Contact
If you have any questions please contact the LMS devs at the University of Dayton at lms-devs@udayton.edu.
