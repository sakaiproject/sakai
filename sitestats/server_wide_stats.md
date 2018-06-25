# Site Stats - Server Wide Stats

Up until v2.3 of SiteStats, the server wide stats feature was a direct query on the sakai_event and sakai_session tables.
This was problematic in two ways:
1. to retain any historical data, institutions needed to keep everything in both of those tables, which could grow
to be extremely large (tens of millions of rows).
1. the query turned out to be *very* time consuming to run, often timing out in the UI which caused problems of its own. 

As of SiteStats 2.3, this has been re-implemented to store this data in two separate tables:
SST_SERVERSTATS which stores daily records of server activities (site.add, site.del, user.add, user.del, user.login) across the entire system
SST_USERSTATS which stores daily unique logins (user.login).

Using both of these we can recreate the original intent of the server wide stats, but in an easier to manage structure. We also
don't have the issue with retaining historical data.

The data is still retrieved via the Sakai SqlService and for 2.3 is only implemented with MySQL however as of 2.4, Oracle is now supported.

## Enabling/Disabling

Server Wide stats are enabled by default. Disable them via:
```
serverWideStatsEnabled@org.sakaiproject.sitestats.api.StatsManager=false
```
in sakai.properties

## Historical data

There is no automatic conversion of the existing historical data to the new site stats database tables. 
This needs to be performed manually. Below are the original functions (MySQL) and a sample of data that they return.

At the end are the queries required to take the historical data and put it into the format required for insert into the new tables.

### Monthly Logins
```
SELECT 
    STR_TO_DATE(date_format(SESSION_START, '%Y-%m-01'),'%Y-%m-%d') as period,
    count(*) as user_logins,
    count(distinct SESSION_USER) as unique_users
    FROM SAKAI_SESSION
    GROUP BY 1;
```

period | user_logins | unique_logins
------ | ----------- | -------------
2011-04-01 | 12856 | 733
2011-05-01 | 14614 | 904
2011-06-01 | 9105 | 786

### Weekly Logins
```
SELECT
    STR_TO_DATE(concat(date_format(SESSION_START, '%x-%v'), ' Monday'),'%x-%v %W') as week_start,
    count(*) as user_logins,
    count(distinct SESSION_USER) as unique_users
    FROM SAKAI_SESSION
    GROUP BY 1;
```

week_start | user_logins | unique_users
---------- | ----------- | ------------	
2011-05-23 | 3433 | 420
2011-05-30 | 3223 | 403
2011-06-06 | 3129 | 386

### Daily Logins
```
SELECT date(SESSION_START) as session_date, count(*) as user_logins, count(distinct SESSION_USER) as unique_users
    FROM SAKAI_SESSION
    WHERE SESSION_START > DATE_SUB(CURDATE(), INTERVAL 5000 DAY)
    GROUP BY 1;
```

session_date | user_logins | unique_users
------------ | ----------- | ------------
2011-05-30 | 532 | 118
2011-05-31 | 527 | 114
2011-06-01 | 560 | 156


### Site Created/Deleted (Daily)
```
SELECT
    date(EVENT_DATE) as event_period,
    sum(if(event = 'site.add' && ref not regexp '/site/[~!]',1,0)) as site_created,
    sum(if(event = 'site.del' && ref not regexp '/site/[~!]',1,0)) as site_deleted
    FROM SAKAI_EVENT
    WHERE EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 5000 DAY)
    GROUP BY 1;
```

event_period | site_created | site_deleted
------------ | ------------ | ------------
2011-06-25 | 0 | 0
2011-06-26 | 2 | 0
2011-06-27 | 3 | 0


### New Users (Daily)
```
SELECT date(EVENT_DATE) as event_period, sum(if(event = 'site.add' && ref regexp '/site/[~!]',1,0)) as new_user
    FROM SAKAI_EVENT
    WHERE EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 5000 DAY)
    GROUP BY 1;
```
event_period | new_user
------------ | --------
2011-05-31 | 8
2011-06-01 | 9
2011-06-02 | 13


### Top 20 activities
```
SELECT 
    event,
    sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 7 DAY),1,0))/7 as last7,
    sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 30 DAY),1,0))/30 as last30,
    sum(if(event_date > DATE_SUB(CURDATE(), INTERVAL 365 DAY),1,0))/365 as last365
    FROM SAKAI_EVENT
    WHERE event not in ('content.read', 'user.login', 'user.logout', 'pres.end', 'realm.upd', 'realm.add', 'realm.del', 'realm.upd.own') and event_date > DATE_SUB(CURDATE(), INTERVAL 365 DAY)
    GROUP BY 1 ORDER BY 2 desc, 3 desc, 4 desc LIMIT 20;
```

event | last7 | last30 | last365
----- | ----- | ------ | -------
content.revise | 0.0000 | 0.7000 | 10.0877
content.new | 0.0000 | 0.3000 | 118.9425
content.delete | 0.0000 | 0.3000 | 68.0137


### Weekly regular users
```
select 
    s.week_start,
    sum(if(s.user_logins >= 5,1,0)) as five_plus,
    sum(if(s.user_logins = 4,1,0)) as four,
    sum(if(s.user_logins = 3,1,0)) as three,
    sum(if(s.user_logins = 2,1,0)) as twice,
    sum(if(s.user_logins = 1,1,0)) as once
    from (
        select STR_TO_DATE(concat(date_format(session_start, '%x-%v'), ' Monday'),'%x-%v %W') as week_start,
        session_user,
        count(*) as user_logins
            from SAKAI_SESSION group by 1, 2
    ) as s group by 1;
```

week_start | five_plus | four | three | twice | once
---------- | --------- | ---- | ----- | ----- | ----
2011-03-07 | 60 | 32 | 44 | 81 | 199
2011-03-14 | 71 | 24 | 36 | 68 | 193
2011-03-21 | 66 | 20 | 40 | 86 | 189


### Hourly usage pattern

```
select date(SESSION_START) as session_date, hour(session_start) as hour_start, count(distinct SESSION_USER) as unique_users
    from SAKAI_SESSION where SESSION_START > DATE_SUB(CURDATE(), INTERVAL 5000 DAY)
    group by 1, 2;
```
session_date | hour_start | unique_users
------------ | ---------- | ------------
2011-06-20 | 9 | 16
2011-06-20 | 10 | 34
2011-06-20 | 11 | 30


### Tool count

```
SELECT registration, count(*) as site_count
    FROM SAKAI_SITE_TOOL where site_id regexp '^[[:digit:]]'
    group by 1 order by 2 desc;
```

registration | site_count
------------ | ----------
sakai.forums | 130
blogger | 87
sakai.discussion | 79

## Historical data conversion (for MySQL)

You need to run the following queries to convert the historical data into the new tables. No data is lost in this process.
You should backup any data in the SSL_SERVERSTATS and SST_USERSTATS tables just in case you decide to start over.
Once verified, you can begin the archival/removal of data in the SAKAI_SESSION and SAKAI_EVENT tables, as necessary.


### Convert daily total user logins
```
insert into sst_serverstats (activity_date, event_id, activity_count)
    select date(SESSION_START), "user.login", count(*)
        from SAKAI_SESSION 
        where SESSION_START > DATE_SUB(CURDATE(), INTERVAL 5000 DAY) group by 1;
```

### Convert daily individual user logins
```
insert into sst_userstats (login_date, user_id, login_count) 
    select date(SESSION_START), SESSION_USER, count(*) from SAKAI_SESSION
    where SESSION_START > DATE_SUB(CURDATE(), INTERVAL 5000 DAY) group by 2,1;
```


### Convert the daily site.add server events per day
```
insert into sst_serverstats (activity_date, event_id, activity_count) 
select date(EVENT_DATE) as activity_date, event, count(*) as event_id
from SAKAI_EVENT
where EVENT ='site.add'
and REF not regexp '/site/[~!]'
and EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 5000 DAY) group by 1;
```

### Convert the daily site.del server events per day
```
insert into sst_serverstats (activity_date, event_id, activity_count)
select date(EVENT_DATE) as activity_date, event, count(*) as event_id
from SAKAI_EVENT
where EVENT ='site.del'
and REF not regexp '/site/[~!]'
and EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 5000 DAY) group by 1;
```

### Convert the daily user.add server events per day
```
insert into sst_serverstats (activity_date, event_id, activity_count)
select date(EVENT_DATE) as activity_date, event, count(*) as event_id
from SAKAI_EVENT
where EVENT ='user.add'
and EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 5000 DAY) group by 1;
```

### Convert the daily user.del server events per day
```
insert into sst_serverstats (activity_date, event_id, activity_count)
select date(EVENT_DATE) as activity_date, event, count(*) as event_id
from SAKAI_EVENT
where EVENT ='user.del'
and EVENT_DATE > DATE_SUB(CURDATE(), INTERVAL 5000 DAY) group by 1;
```

## Historical data conversion (for Oracle)

This section contains Oracle versions of the SQL statements from the previous section.

### Convert daily total user logins
```
insert into sst_serverstats (id, activity_date, event_id, activity_count) 
    select sst_serverstats_id.nextval, sub.*
        from (select trunc(SESSION_START, 'DDD') as session_date, 'user.login', count(*) from SAKAI_SESSION where SESSION_START > (SYSDATE - 5000) group by trunc(SESSION_START, 'DDD'))
        sub;
```

### Convert daily individual user logins
```
insert into sst_userstats (id, login_date, user_id, login_count) 
select sst_userstats_id.nextval, sub.*
from (select trunc(SESSION_START, 'DDD'), SESSION_USER, count(*)
      from SAKAI_SESSION
      where SESSION_START > (SYSDATE - 5000)
      group by SESSION_USER, trunc(SESSION_START, 'DDD')) sub;
```

### Convert the daily site.add server events per day
```
insert into sst_serverstats (id, activity_date, event_id, activity_count) 
select sst_serverstats_id.nextval, sub.*
from (select trunc(EVENT_DATE, 'DDD'), event, count(*) as event_id
      from SAKAI_EVENT
      where EVENT = 'site.add'
      and REF not like '/site/~%'
      and REF not like '/site/!%'
      and EVENT_DATE > (SYSDATE - 5000)
      group by trunc(EVENT_DATE, 'DDD'), event) sub;
```

### Convert the daily site.del server events per day
```
insert into sst_serverstats (id, activity_date, event_id, activity_count) 
select sst_serverstats_id.nextval, sub.*
from (select trunc(EVENT_DATE, 'DDD'), event, count(*) as event_id
      from SAKAI_EVENT
      where EVENT = 'site.del'
      and REF not like '/site/~%'
      and REF not like '/site/!%'
      and EVENT_DATE > (SYSDATE - 5000)
      group by trunc(EVENT_DATE, 'DDD'), event) sub;
```

### Convert the daily user.add server events per day
```
insert into sst_serverstats (id, activity_date, event_id, activity_count) 
select sst_serverstats_id.nextval, sub.*
from (select trunc(EVENT_DATE, 'DDD'), event, count(*) as event_id
      from SAKAI_EVENT
      where EVENT = 'user.add'
      and EVENT_DATE > (SYSDATE - 5000)
      group by trunc(EVENT_DATE, 'DDD'), event) sub;
```
### Convert the daily user.del server events per day
```
insert into sst_serverstats (id, activity_date, event_id, activity_count) 
select sst_serverstats_id.nextval, sub.*
from (select trunc(EVENT_DATE, 'DDD'), event, count(*) as event_id
      from SAKAI_EVENT
      where EVENT = 'user.del'
      and EVENT_DATE > (SYSDATE - 5000)
      group by trunc(EVENT_DATE, 'DDD'), event) sub;
```