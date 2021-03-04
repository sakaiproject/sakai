# Sakai Meetings Tool

This tool enables users in a Sakai site to conference using  the BigBlueButton open source web
conferencing system that enables universities and colleges to deliver a high-quality learning
experience to remote students.

#### *With this plugin you can*
- Control meetings - create/edit/update/delete BBB meetings from Sakai
- Meeting access - define meeting access by selective all users, groups, roles or individual users
in site
- Tool access - define who can do what on the Sakai tool
- Scheduling - optionally, define meeting start dates and/or end dates and add it to site Calender
- Notification - optionally, send an email notification to meeting participants
- Simplicity - the user interface is designed to be simple
- Fast - the Ajax driven interface (Javascript + JSON + Trimpath templates) provides good end-user
experience and low server load
- RESTful - full RESTful support via EntityBroker
- Statistics - the tool logs information automatically processed by the Site Stats tool

## Prerequisites

Blindside Networks provides you a test BigBlueButton server for testing this plugin.  To use this
test server, just accept the default settings when configuring the activity module.  The default
settings are
```
url: http://test-install.blindsidenetworks.com/bigbluebutton/
salt: 8cd8ef52e8e101574e400365b55e11a6
```
For information on how to setup your own BigBlueButton server see

http://bigbluebutton.org/
   
```
bbb.url=http://<server>/bigbluebutton
bbb.salt=<salt>
```   
To determine these values for your BigBlueButton server, enter the command
```
bbb-conf --salt
```
If you want to use the public test server for BigBlueButton, use the following settings 
```
bbb.url=http://test-install.blindsidenetworks.com/bigbluebutton   
bbb.salt=8cd8ef52e8e101574e400365b55e11a6
```
