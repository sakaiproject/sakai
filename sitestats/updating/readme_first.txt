Site Stats conversion scripts
-----------------------------

Change 'mysql' to 'oracle' if appropriate:

1.x         ==> 2.0:        SiteStats_1-x_2-0_mysql_conversion.sql
pre-1-0     ==> 1.0:        SiteStats_pre-1-0_1-0_mysql_conversion.sql
0.5.x (x>1) ==> pre-1-0:    SiteStats_0-5-x_pre-1-0_mysql_conversion.sql
0.5/0.5.1   ==> 0.5.2:      SiteStats_0-5-x_0-5-2_mysql_conversion.sql
0.5/0.5.1   ==> 0.5.2:      SiteStats_0-5-x_0-5-2_mysql_conversion.sql
0.4         ==> 0.5:        SiteStats_0-4_0-5_mysql_conversion.sql
0.1/2/3     ==> 0.5:        SiteStats_0-x_0-5_mysql_conversion.sql


Conversion from old preferences table in SiteStats 0.x:
-------------------------------------------------------
If upgrading from SiteStats 0.x to SiteStats >= pre-1.0, you can convert the
old SST_PREFS table to the new SST_PREFERENCES using the provided quartz job:
- login to Sakai as admin;
- select the 'Job Scheduler' tool;
- on the tool, click 'Jobs';
- click 'New Job', give it a name, select 'SiteStats old SST_PREFS table conversion'
  from the list and click 'Post';
- select the created job clicking on 'Triggers(0)';
- either click 'Run Job Now' or add a trigger to schedule the job for later.
