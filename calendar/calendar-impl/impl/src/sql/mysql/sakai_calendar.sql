-- ---------------------------------------------------------------------------
-- CALENDAR_CALENDAR
-- ---------------------------------------------------------------------------

CREATE TABLE CALENDAR_CALENDAR
(
    CALENDAR_ID VARCHAR (99) NOT NULL,
	NEXT_ID INT,
    XML LONGTEXT
);

CREATE UNIQUE INDEX CALENDAR_CALENDAR_INDEX ON CALENDAR_CALENDAR
(
	CALENDAR_ID
);

-- ---------------------------------------------------------------------------
-- CALENDAR_EVENT
-- ---------------------------------------------------------------------------

CREATE TABLE CALENDAR_EVENT
(
    CALENDAR_ID VARCHAR (99) NOT NULL,
	EVENT_ID VARCHAR (36) NOT NULL,
	EVENT_START DATETIME NOT NULL,
	EVENT_END DATETIME NOT NULL,
	RANGE_START INTEGER NOT NULL,
	RANGE_END INTEGER NOT NULL,
    XML LONGTEXT
);

CREATE UNIQUE INDEX EVENT_INDEX ON CALENDAR_EVENT
(
	EVENT_ID
);

CREATE INDEX CALENDAR_EVENT_INDEX ON CALENDAR_EVENT
(
	CALENDAR_ID
);

CREATE INDEX CALENDAR_EVENT_RSTART ON CALENDAR_EVENT
(
	RANGE_START
);

CREATE INDEX CALENDAR_EVENT_REND ON CALENDAR_EVENT
(
	RANGE_END
);

-- Composite index to support overlap filtering within a calendar and
-- improve ordering by EVENT_START for the filtered subset.
CREATE INDEX CALENDAR_EVENT_CID_RSTART_REND_ESTART ON CALENDAR_EVENT
(
	CALENDAR_ID,
	RANGE_START,
	RANGE_END,
	EVENT_START
);
