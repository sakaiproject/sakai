# Site Stats

- About
- Tool security (permissions)
- External DB feature
- History

## About
Site Stats is a tool for Sakai for showing site usage statistics.

Tool information is organized in 2 screens:
- Overview: contains summary information about site visits and activity;
- Reports:  generated exportable reports about site visits, tool and resource activity.
		
Tool support can be added/removed sakai-wide (by using configuration files) or on site basis (by
using the tool Preferences screen). See SiteStats Confluence page for configuration instructions.

## Tool security (permissions)
For SiteStats 0.5.5 or higher, the 'sitestats.view' permission IS REQUIRED in order to use the tool.
Please use the permissions admin tool to back fill permissions existing realms or add the permission to the '!site.helper' realm.
Access to the sitestats administrator view can be granted to non-administrator users by adding the permission 'sitestats.admin.view'.
The tool won't be listed on left menu if the user doesn't have the required permission.

### Student / access unlock (Sakai 27+)
Default site templates grant `sitestats.view` (in addition to `sitestats.own`) to **Student** and **access**.
After upgrading, run the Sakai 27 conversion scripts in sakai-reference so existing sites receive the same grant.
With `view` + `own`, learners can open SiteStats and see the overview **own** audience widgets.
The site preference `showOwnStatisticsToStudents` still applies: when it is false, own statistics remain hidden even if the user has `sitestats.own`.

### Dashboard widgets
Course dashboards can offer compact SiteStats overview widgets (`sitestats-*`):
audience **all** to users with `sitestats.all`, or audience **own** (`sitestats-student-*`) to users with `sitestats.own` (and without all).
Home dashboards can offer institutional/admin widgets (audience **admin**) to users with `sitestats.admin.view`.
Toggle with `dashboard.sitestats.enabled` (default true).
Default layout order is configured per audience via `dashboard.course.widget.layout.student` /
`dashboard.course.widget.layout.instructor` and `dashboard.home.widget.layout` /
`dashboard.home.widget.layout.admin`; tiles still require the matching permission to appear.

## External Database feature
In order to configure sitestats to use a different database for its tables set the following sakai property:
```
sitestats.db=internal <--- default, don't use an external db
sitestats.db=external
```
Next you will need to configure the following properties for indicating the external database (the defaults are listed):
```
sitestats.externalDb.jdbcUrl=jdbc:hsqldb:mem:sitestats_db
sitestats.externalDb.driverClassName=org.hsqldb.jdbcDriver
sitestats.externalDb.username=sa
sitestats.externalDb.password=secret
sitestats.externalDb.connectionTestQuery=SELECT 1
sitestats.externalDb.poolName=externalDBCP
sitestats.externalDb.hibernate.dialect=org.hibernate.dialect.HSQLDialect
sitestats.externalDb.auto.ddl=update
sitestats.externalDb.hibernate.show_sql=false
```
Caching for the external db is disabled by default
Enabling this will require configuring a region in Ignite's configuration
```
sitestats.externalDb.hibernate.cache.region.factory_class=
sitestats.externalDb.hibernate.cache.use_query_cache=false
sitestats.externalDb.hibernate.cache.use_second_level_cache=false
```

## Server Wide Stats
See the following [doc](server_wide_stats.md)

## History
SiteStats was origionaly written by Nuno Fernandes at Universidade Fernando Pessoa.
