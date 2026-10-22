# Attendance
A simple [Sakai](https://github.com/sakaiproject/sakai) tool for tracking attendance that integrates with the Gradebook.

## Performance Improvements
With the release of 20170215, statistics are now stored in a table. As such, a job was created to calculate these stats.
This job should run at a regular interval (e.g., once per night or several times throughout the day) in the Job Scheduler. 
The job **MUST BE** run at least once to calculate & save the statistics to begin with, afterwards the job is only needed 
to sync up the stats to the current roster (in the case of users attending an item and then later leaving the site).
This job is titled "Attendance Stat Calc - SEE DOCS".

## Resources
Pages: https://sakaicontrib.github.io/attendance/

Presentation:
https://prezi.com/m3dvmxokf8as/ - Delivered at Apereo 16.

## Compatibility
The Sakai property auto.ddl should be set to true when first starting this tool with Sakai.
If not, queries for MySQL and Oracle can be found in [docs/sql/](docs/sql/), though use at your own risk.

## Developers
Many thanks to the LMS devs at the University of Dayton for creating this tool!

## License 
Educational Community License 2.0 (ECL-2.0)
