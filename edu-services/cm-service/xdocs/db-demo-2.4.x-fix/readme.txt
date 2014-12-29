In Sakai 2.4.x, the demo data loader creates sample Course Management data
for the year 2007 and the default Course Management implementation decides
what academic session is current based on the current system time. The
combination of these two factors meant that as of January 1 2008, no
course sites could be created on a Sakai demo system.

The SQL scripts included in this directory re-enable course site creation
on 2.4.x demo systems by simply changing existing Course Management sample
data to use 2008 dates.
