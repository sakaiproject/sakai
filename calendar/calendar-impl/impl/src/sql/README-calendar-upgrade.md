# Calendar Event Range Query Upgrade

This upgrade accompanies the change to the calendar events range filter in
`calendar/calendar-impl/impl/src/java/org/sakaiproject/calendar/impl/DbCalendarService.java`.

What changed
- Query now uses a sargable overlap predicate: `(RANGE_START < :end) AND (RANGE_END > :start)`.
- A composite index is introduced so the engine can efficiently filter within a calendar
  by range and then order by `EVENT_START`.

The index is added to new installs in the baseline DDL. Use the scripts below to upgrade existing databases.

Important
- Run these during a maintenance window if possible.
- Back up before changes.
- Keep the existing single-column indexes unless you’ve verified they’re no longer needed in your workload.

## MySQL (InnoDB)

Preferred (online when supported by your version):

```sql
ALTER TABLE CALENDAR_EVENT
  ADD INDEX CALENDAR_EVENT_CID_RSTART_REND_ESTART
    (CALENDAR_ID, RANGE_START, RANGE_END, EVENT_START)
  , ALGORITHM=INPLACE, LOCK=NONE;
```

Fallback (no algorithm hints):

```sql
CREATE INDEX CALENDAR_EVENT_CID_RSTART_REND_ESTART
  ON CALENDAR_EVENT (CALENDAR_ID, RANGE_START, RANGE_END, EVENT_START);
```

Update statistics:

```sql
ANALYZE TABLE CALENDAR_EVENT;
```

Verify:

```sql
SHOW INDEX FROM CALENDAR_EVENT WHERE Key_name = 'CALENDAR_EVENT_CID_RSTART_REND_ESTART';
```

## Oracle

Note: Oracle has a 30-character identifier limit; the baseline index name is shortened.

```sql
CREATE INDEX CAL_EVT_CID_RS_RE_ES
  ON CALENDAR_EVENT (CALENDAR_ID, RANGE_START, RANGE_END, EVENT_START);
```

Update statistics (replace `YOUR_SCHEMA` if needed):

```sql
BEGIN
  DBMS_STATS.GATHER_TABLE_STATS(
    ownname  => USER,            -- or 'YOUR_SCHEMA'
    tabname  => 'CALENDAR_EVENT',
    cascade  => TRUE
  );
END;
/
```

Verify:

```sql
SELECT index_name, column_name, column_position
  FROM user_ind_columns
 WHERE table_name = 'CALENDAR_EVENT'
   AND index_name = 'CAL_EVT_CID_RS_RE_ES'
 ORDER BY column_position;
```

## HSQLDB (dev/test)

```sql
CREATE INDEX CAL_EVT_CID_RS_RE_ES
  ON CALENDAR_EVENT (CALENDAR_ID, RANGE_START, RANGE_END, EVENT_START);
```

Verify:

```sql
SELECT * FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO
 WHERE TABLE_NAME = 'CALENDAR_EVENT';
```

## Optional clean-up (only after testing)

You may consider dropping one of the single-column range indexes if unused:

MySQL:

```sql
-- Example; drop only after verifying no regressions
-- DROP INDEX CALENDAR_EVENT_RSTART ON CALENDAR_EVENT;
-- DROP INDEX CALENDAR_EVENT_REND ON CALENDAR_EVENT;
```

Oracle/HSQLDB: adjust `DROP INDEX` syntax similarly.

## Explain plan check

Re-run an `EXPLAIN` (or Oracle plan) for a representative query, e.g.:

```sql
-- MySQL example
EXPLAIN SELECT XML
  FROM CALENDAR_EVENT
 WHERE CALENDAR_ID = '/calendar/calendar/<uuid>/main'
   AND (RANGE_START < :endHours AND RANGE_END > :startHours)
 ORDER BY EVENT_START ASC;
```

Expected: dramatically fewer rows examined using the new composite index. A filesort may still appear due to ordering on `EVENT_START`, but the filtered set should be small.

