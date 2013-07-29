DASH-256 allows the dashboard event processing to take place via a scheduled quartz job. Here are the steps to config and start that quartz job:

1. Make sure to use the property to disable the thread collector or it will process duplicates.

disable.dashboard.eventprocessing=true 

2. Go to Job Scheduler->Jobs->New Job Select the Dashboard Event Aggregator, give it a name.

3. Click Triggers and Run Job Now. You can enter a trigger to make it run every X minutes like for every 15 minutes, for example:

0 0/15 * * * ?
