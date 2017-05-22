For the most up-to-date conversion scripts please see the files in https://github.com/sakaiproject/sakai/tree/master/reference/docs/conversion

You should be able to check just this directory out with subversion using the command:

`svn co https://github.com/sakaiproject/sakai/trunk/reference/docs/conversion`

Or just clone it from Github.

You need to run all scripts in order to ensure a proper conversion. For instance if upgrading to 11 from 10.3, you need to see if scripts exist for 10.4, 10.5, 10.6, etc before running the 11.0 script. 

A database conversion is typically required in order to upgrade from one Sakai version to another. Database conversion scripts -- in distinct versions for MySQL and Oracle, respectively -- are found in the reference/docs/conversion folder in the master branch.

In the same directory you will also find conversion scripts for earlier Sakai releases. Migration from an earlier version will require the successive application of all intermediate scripts (see the following table). You cannot, for example, move from 2.6.1 to 2.9.0 by applying a single script. You will need to run 6 or 7 scripts all in a row.
(warning) Note for oracle, some of the scripts will leave your indexes in an invalid state because of LONG->CLOB conversion. You will need to run this script to find the invalid/unusable indexes, THEN run the result of this script to alter these indexes.
select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE'; 

-- Run the resulting SQL commands this script generates if any)
(warning) As a general rule, be sure to read through the conversion scripts before applying them. The conversion scripts are generic in the sense that they do not take into account any special customizations you may have made - such as new roles, or the deployment of additional tools or if you are migrating from 2.4.x - and they may complicate your migration with unintended consequences if you execute them blindly.
(minus) For conversions prior to 2.6 please see the 2.8 install guide. Conversions from much older are not very well supported or tested but should still work.

Upgrade Step| MySql | Oracle | Notes
-------------------------------------
2.6.0 |sakai_2_6_0_mysql_conversion.sql | sakai_2_6_0_oracle_conversion.sql | Use scripts updated in 2.6.x branch (r65964+). Include fixes for ,  and . If you are upgrading from 2.5 please review  for an important property setting issue (not a database conversion
issue).
