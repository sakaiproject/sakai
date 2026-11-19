# Attendance
A simple [Sakai](https://github.com/sakaiproject/sakai) tool for tracking attendance that integrates with the Gradebook.

## Sakai master integration

Attendance is part of the Sakai reactor build. It targets Java 21, Jakarta Servlet 6
(Tomcat 10.1), Sakai's Spring 6 / Hibernate 6.6 JPA infrastructure, and Wicket 10.
Its Wicket version follows the version used by Gradebook.

Build and run the model, persistence, service, PDF export, and job lifecycle tests:

```sh
mvn -f attendance/pom.xml clean install
```

Deploy to an existing Sakai installation:

```sh
mvn -f attendance/pom.xml sakai:deploy -Dmaven.tomcat.home=/path/to/tomcat
```

Start or restart Tomcat manually after deployment. The tool is available to course
and project sites through **Site Info > Manage Tools**.

Run the Playwright smoke tests against a running demo installation with the standard
`instructor1` and `student0011` accounts and SakaiScript fixture support:

```sh
PLAYWRIGHT_BASE_URL=https://your-sakai-host mvn -f e2e-tests/pom.xml test -Dtest=AttendanceTest
```

The tests cover event creation, editing in the Wicket dialog, deletion, saving
attendance, persistence after reload, and the student's read-only record view.

## Upstream provenance

The initial import is an unmodified snapshot of
[sakaicontrib/attendance master](https://github.com/sakaicontrib/attendance/tree/2a230316cb859a3627c5b0d43960fe4f1d786f03)
at `2a230316cb859a3627c5b0d43960fe4f1d786f03` (September 1, 2026).
The import commit credits every author identity in the upstream master history and
retains upstream license, copyright notices, and Maven developer metadata.
Individual upstream commits remain in the
[original repository history](https://github.com/sakaicontrib/attendance/commits/2a230316cb859a3627c5b0d43960fe4f1d786f03/).
The second commit integrates and modernizes the snapshot. Sakai's Playwright suite
replaces the standalone Cypress setup.

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
Database conversions are maintained in
[sakai-reference](https://github.com/sakaiproject/sakai-reference/tree/master/docs/conversion).
The Sakai 27 Attendance conversions are in
[sakai-reference PR #304](https://github.com/sakaiproject/sakai-reference/pull/304).
For installations upgrading older contrib releases, the prerequisite 1.0 and
20170215 schema scripts remain in the
[original contrib history](https://github.com/sakaicontrib/attendance/tree/2a230316cb859a3627c5b0d43960fe4f1d786f03/docs/sql).

## Developers
Many thanks to the LMS devs at the University of Dayton for creating this tool!

## License 
Educational Community License 2.0 (ECL-2.0)
