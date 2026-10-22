#CHANGELOG

## 20180412
* Hibernate4 upgrade for Sakai 12+

## 20170215
* Change in versioning to date based
* Performance improvements
  * Attendance Stats are now stored in the DB, in order to avoid having
    to consistently calculate the statistics.
    As such a job, __Attendance Stat Calc - SEE DOCS__, was created which will
    calculate the stats and store them in the DB. Afterwards statistics will be
    updated on the save of any record.
    This job __must manually__ be run once after applying this update to calculate
    statuses for older sites.
    Afterwards, the job should be set up to run periodically at a user defined interval.
    The job is necessary to run if you want stats to to be correct in case of users
    no longer being present in the site.
  * Records are now created only when needed (prior to saving a status)
* Attendance Items are now added / edited via a modal window
* Attendance Records now have improved styling and formating
  * Statuses are now color coded
  * Record input panel is now more responsive and will go vertical with labels
  when the screen width is <800px.
* Added new auto grading feature
  * Allows for the ability to award points based on statuses (or have students lose
  points)
  For example: Present: 1-5 times, 1 point; Present: 6-10 times, 1 point.
  * Based on user defined rules. NOTE: The rules are not validated (so they may overlap
  or count twice if ill-defined).
  * Have the ability to override an auto grade
  * Grading occurs on each status update, as well as whenever the grading settings are
  saved
* Styling improvements
* Bugfixes
  * Sequence for ATTENDANCE\_GRADE\_T set correctly.
  Oracle users should run the PSQL defined at the bottom of the 
  [attendance-20170215-oracle.sql](/docs/sql/attendance-20170215-oracle.sql) file.
  * Minor fixes

## 1.0
* Initial Release
