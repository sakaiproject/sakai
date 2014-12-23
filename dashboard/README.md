# Dashboard 

The Sakai Dashboard tool will display current and upcoming activity from the Announcements, Assignments, Calendar and Resources tools. Additional tools may be added later.

Additional information on changing the Dashboard default configuration is available at <https://confluence.sakaiproject.org/display/DASH/Configuring+dashboard>.

## Quartz Job Configuration

The Dashboard can optionally be configured to allow event processing to be scheduled, instead of in real time, for performance concerns (DASH-256). Here are the steps to config and start that quartz job:

1. Disable default dashboard processing
   disable.dashboard.eventprocessing=true 

2. Configure one or more of the following Quartz Scheduler jobs:
   * Select the "Job Scheduler" Admin tool
   * Select Jobs -> New Job, select the desired job and enter a name
   * Select Triggers -> New Trigger and enter a name and a cron expression, for example the following will run every 15 minutes:
   
       0 0/15 * * * ?


### Dashboard Aggregate Job

Description TBD
   
### Dashboard Check Admin Configuration Changes Job

Description TBD

### Dashboard Check Availability Job

Description TBD

### Dashboard Expire Purge Job

Description TBD

### Dashboard Repeat Event Job

Description TBD

### Dashboard Synchronize Dashboard Link Users with Site Users Job

Description TBD

## Update Guide

The Sakai 11 update script will add the Dashboard tool as the first tool in the !user MyWorkspace site template. This will apply to all new users.

If you would like existing users to also have the Dashboard tool as the first ool in every MyWorkspace site, then the `updateMyWorkspaceSites.pl` script should be modified for your local Sakai instance and executed.
 

## Acknowledgements

People have been asking for the dashboard tool for a long time.  It was finally implemented thanks to the efforts of participants in a graduate course in the School of Information at The University of Michigan in Winter term 2011. 

The course, "Open Source Software Development", was organized by Chuck Severance and included contributions from people at various institutions that use Sakai, especially Charles Hedrick of Rutgers.  

The students primarily responsible for defining requirements and developing the initial design for the dashboard were Caitlin Holman and Nikola Collins.

Another group of students from the School of Information at The University of Michigan in Winter term 2011 evaluated the usability of the sakai portal and suggested improvements, including recommendations related to the dashboard proposal.  Those students were Sayan Bhattacharyya, Elliott Andrew Manzon, Rachael Shaney and Amelia Mowry.

Tiffany Chow and Shwetangi Savant assisted with usability and quality assurance testing during development of the dashboard project while working as interns for CTools during the Fall term of 2012.




