-- SQL for delayed invokation.
-- This has been moved out of the main quartz file so it's easier to upgrade the quartz file and
-- see what has changed between releases.

CREATE TABLE IF NOT EXISTS SCHEDULER_DELAYED_INVOCATION (
INVOCATION_ID VARCHAR(36) NOT NULL,
INVOCATION_TIME DATETIME NOT NULL,
COMPONENT VARCHAR(2000) NOT NULL,
CONTEXT VARCHAR(2000) NULL,
PRIMARY KEY (INVOCATION_ID),
KEY `SCHEDULER_DI_TIME_INDEX` (`INVOCATION_TIME`)
);
