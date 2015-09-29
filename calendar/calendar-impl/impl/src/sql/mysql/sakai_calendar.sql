-- ---------------------------------------------------------------------------
-- CALENDAR_CALENDAR
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS CALENDAR_CALENDAR
(
    CALENDAR_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
	XML LONGTEXT,
	UNIQUE KEY `CALENDAR_CALENDAR_INDEX` (`CALENDAR_ID`)
);

-- ---------------------------------------------------------------------------
-- CALENDAR_EVENT
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS CALENDAR_EVENT
(
    CALENDAR_ID VARCHAR (99) NOT NULL,
	EVENT_ID VARCHAR (36) NOT NULL,
	EVENT_START DATETIME NOT NULL,
	EVENT_END DATETIME NOT NULL,
	RANGE_START INTEGER NOT NULL,
	RANGE_END INTEGER NOT NULL,
	XML LONGTEXT,
	UNIQUE KEY `EVENT_INDEX` (`EVENT_ID`),
	KEY `CALENDAR_EVENT_INDEX` (`CALENDAR_ID`),
	KEY `CALENDAR_EVENT_RSTART` (`RANGE_START`),
	KEY `CALENDAR_EVENT_REND` (`RANGE_END`)
);
