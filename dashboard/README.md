# Sakai Dashboard Tool

## Dashboards Overview

Dashboards come in two flavours – Home and Course. To add on to your course, you just pick the
Dashboard tool from the tool list in Site Info as you would with any other tool. To add the
Dashboard to you home site, you need use the Sites tool, or use a Sakai instance with the tool added to the !user home site template (in that case when you first login, your home site will be generated with the Dashboard tool already included).

## Home Dashboard

The home dashboard is designed to replace the functionality of the Overview page in your home Sakai
site. There is a message of the day and a set of widgets which work in pretty much the same way as
the synoptic tools in the Overview. So, those widgets will show data from across all of your Sakai
course/project sites, in aggregated form.

### Home Dashboard Features

- Message of the Day (MOTD) area. This shows the latest announcement posted by a Sakai administrator
in the Admin Workspace announcements tool.
- Widget panel. A panel, customisable through adding, removing or laying out widgets.

## Course/Project Dashboard

The course/project site dashboard has a different layout to the home dashboard. Only instructors, 
users in a site.upd role, can update the course dashboard using the features outlined below. All
roles can see the dashboard, though.

### Course/Project Dashboard Features

- Course overview and short description editing areas where you can directly edit the descriptive
elements of your course/project site. The end result is the same as if you'd used the current site
info tool to modify those descriptions.
- Layout templates. You can select from 3 (currently) different layouts for your course/project
site's dashboard.
- Course image editing. You can directly add or modify the image for your course/project site. This
uses the existing functionality for storing course images, and results in the image appearing above
the tool menu, too.
- Widget panel. A panel, customisable through adding, removing or laying out widgets.

### Widgets

- Announcements
- Calendar
- Forums
- Grades
- Tasks

#### Announcements

This works much in the same way as the Announcements widget from the overview tool. In "Home mode"
it pulls announcements from all of your course or project sites and displays them in a list format,
with links directly to the announcement in the site. When actually in a course or project site the
widget only shows the annoucnments for that particular site and you can still click a link to take
you directly into the announcement tool, to the announcement.

#### Calendar

In "Home mode" this widget aggregates all the calendar events from your course/project sites. On a
course or project site, the widget only shows the events for that particular site. When you click on
an event, the event details display underneath the calendar.

#### Forums

In "Home mode" this widget aggregates all the forums counts your course/project sites. The idea of
this widget is to give you some idea as to how "hot" a particular topic in a site might be and help
you decide whether it's worth heading on in there.On a course or project site, the widget only shows
the counts for topics in that site. When you click on a topic count you are taken into the forums
tool for that site.

#### Grades

The Grades widget calculates course average scores for gradebook assignments in your course sites.
All of your course sites, where you are an instructor or TA, are displayed in the Grades widget when
in your home site. When in a course site, only the gradebook assignments for that site are
displayed. When you click on the "next" link to a course average calculation, you are taken directly
into the gradebook for that course.

#### Tasks

This widget surfaces a new piece of functionality, the Tasks Service. Tasks are thing you need to
take action on, and are displayed in your Home dashboard, not on course or project site dashboard.
Currently, the only tool which creates tasks is the assignments tool. When you create an assignment
all the people who can either edit the assignment, or take it, will get a task assigned to them, and
that task will show up in the Tasks widget with the amount of time left until the deadline.

Tasks can be created by the user, and we call those tasks "User" tasks, as opposed to "System"
tasks, the assignment previously described being one example of "System" task. System tasks can be
edited by the user - they can have their priority, task text, or completed status set.

Tasks is not available in the course/project site dashboard.
