-------------------
Jmeter Loadtesting:
-------------------

In order to run the loadtest, Jmeter 2.3 or higher is required and before running the test, 
the following parameters need to be set:

	-Sakai Server URL Setting: 
		* server name
		* port
		* protocal

	-User Defined Variables: 
		* siteId and pageId for sign-up tool,which can be obtained via the browser's url
		* userId and password
		* meeting_title
		* rowNum_groupMeeting_count_start_from_0, which defines the specific meeting to use (see meetings main page).
		* numberOfThread_to_run, which defines how many threads to run in a synchronized way

	-User Parameters:
		* define the userIds for the multiple testers
 
