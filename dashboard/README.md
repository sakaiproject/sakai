# Dashboard 

The Sakai Dashboard tool will display current and upcoming activity from the Announcements, Assignments, Calendar and Resources tools. Additional tools may be added later.

Additional information on changing the Dashboard default configuration is available at <https://confluence.sakaiproject.org/display/DASH/Configuring+dashboard>.

## Quartz Job Configuration

There are several Dashboard Quartz Scheduler jobs that can be configured as follows:

   * Select the "Job Scheduler" Admin tool
   * Select Jobs -> New Job, select the desired job and enter a name
   * Select Triggers -> New Trigger and enter a name and a cron expression, for example the following will run every 30 minutes:
   
       0 0/30 * * * ?


### Dashboard Aggregate Job

This scheduled quartz job is *optional*.

The Dashboard can be configured to allow event processing to be scheduled, instead of in real time, for performance concerns (DASH-256). Regular dashboard event processing should be disabled before scheduling this quartz job:

   disable.dashboard.eventprocessing=true 

Instead of running dashboard event processing thread on each server, this quartz job can be optionally enabled on several or one specific server, run at a specified time and process certain amount of events since the last time it was run. It behaves like SiteStats jobs as far as the event aggregation goes.
   
### Dashboard Check Admin Configuration Changes Job

This scheduled quartz job is *optional*.

This quartz job checks the setting of “PROP_LOOP_TIMER_ENABLED” in the DASH_CONFIG database table. If the setting is enabled, and this quartz job is scheduled or executed, diagnostic logging will be generated.

### Dashboard Check Availability Job

This scheduled quartz job is __recommended__.

Many Sakai items (resources, assignment, announcement, etc) have release/availability date settings, and are not shown to users in Dashboard tool before the release dates. 

This quartz job periodically compares the current system date with the dashboard item release dates, and creates dashboard item links for those items if the release date is reached. 

### Dashboard Expire Purge Job

This scheduled quartz job is __recommended__.

This quartz job will periodically cleans the DASH_NEWS_LINK and DASH_CALENDAR_LINK tables depending on 

1) the following config variable in DASH_CONFIG table

PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS	
PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS	
PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS	
PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS	
PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS	
PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS	
PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS	
PROP_REMOVE_CALENDAR_ITEMS_WITH_NO_LINKS	

and 

2) the values of NEWS_TIME in DASH_NEWS_ITEM or CALENDAR_TIME in DASH_CALENDAR_ITEM table.

For example, say the value of PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS is set to 4. For an given dashboard news item, if it is not starred or not hidden, and the current system date is beyond 4 weeks since the NEWS_TIME date, this dashboard item links will be removed from database.

Notice the starred or hidden dashboard items have different removal date settings, which overrides the general removal policy described above.

### Dashboard Repeat Event Job

This scheduled quartz job is __recommended__.

This job deals with a special type of calendar item, a.k.a. the repeating Sakai schedule event, with frequency setting for more than once. It periodically checks the frequency settings, creating new instances into the future date (based on PROP_WEEKS_TO_HORIZON setting).

### Dashboard Synchronize Dashboard Link Users with Site Users Job

This scheduled quartz job is __recommended__.

DASH-324 The quartz job constructs a hashmap (key=sakai site id, and value=site membership set) from dashboard link tables. It then consults the AuthzGroupService for the current Sakai site membership set. Comparing the two membership sets, this quartz job drops dashboard item link(s) if the user is no longer in site, or add dashboard item link(s) if the user is present.

## Update Guide

The Sakai 11 update script will add the Dashboard tool as the first tool in the !user MyWorkspace site template. This will apply to all new users.

If you would like existing users to also have the Dashboard tool as the first tool in every MyWorkspace site, then the `updateMyWorkspaceSites.pl` script should be modified for your local Sakai instance and executed.
 

## Acknowledgements

People have been asking for the dashboard tool for a long time.  It was finally implemented thanks to the efforts of participants in a graduate course in the School of Information at The University of Michigan in Winter term 2011. 

The course, "Open Source Software Development", was organized by Chuck Severance and included contributions from people at various institutions that use Sakai, especially Charles Hedrick of Rutgers.  

The students primarily responsible for defining requirements and developing the initial design for the dashboard were Caitlin Holman and Nikola Collins.

Another group of students from the School of Information at The University of Michigan in Winter term 2011 evaluated the usability of the sakai portal and suggested improvements, including recommendations related to the dashboard proposal.  Those students were Sayan Bhattacharyya, Elliott Andrew Manzon, Rachael Shaney and Amelia Mowry.

Tiffany Chow and Shwetangi Savant assisted with usability and quality assurance testing during development of the dashboard project while working as interns for CTools during the Fall term of 2012.




