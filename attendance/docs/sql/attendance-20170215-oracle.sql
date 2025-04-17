--------------------------------------------------------
-- These are SQL statements to manually upgrade the DB
-- in instances where auto.ddl is not available.
-- A beset effort is made to create this, that is it is
-- UNTESTED.
--------------------------------------------------------

--------------------------------------------------------
-- Add new column to ATTENDANCE_GRADE_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_GRADE_T" ADD "OVERRIDE" NUMBER(1,0);

--------------------------------------------------------
--  DDL for Table ATTENDANCE_ITEM_STATS_T
--------------------------------------------------------
CREATE TABLE "ATTENDANCE_ITEM_STATS_T"
(	"A_ITEM_STATS_ID" NUMBER(19,0),
   "PRESENT" NUMBER(10,0),
   "UNEXCUSED" NUMBER(10,0),
   "EXCUSED" NUMBER(10,0),
   "LATE" NUMBER(10,0),
   "LEFT_EARLY" NUMBER(10,0)
);

--------------------------------------------------------
--  DDL for Table ATTENDANCE_RULE_T
--------------------------------------------------------
CREATE TABLE "ATTENDANCE_RULE_T"
(	"GRADING_RULE_ID" NUMBER(19,0),
   "A_SITE_ID" NUMBER(19,0),
   "STATUS" VARCHAR2(255 CHAR),
   "START_RANGE" NUMBER(10,0),
   "END_RANGE" NUMBER(10,0),
   "POINTS" FLOAT(126)
);

--------------------------------------------------------
--  Alter table for Table ATTENDANCE_SITE_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_SITE_T" ADD (
  "SYNC" NUMBER(1,0),
  "SYNC_TIME" TIMESTAMP (6),
  "AUTO_GRADING" NUMBER(1,0),
  "GRADE_BY_SUBTRACTION" NUMBER(1,0)
  );

--------------------------------------------------------
--  DDL for Table ATTENDANCE_USER_STATS_T
--------------------------------------------------------
CREATE TABLE "ATTENDANCE_USER_STATS_T"
(	"A_USER_STATS_ID" NUMBER(19,0),
   "USER_ID" VARCHAR2(255 CHAR),
   "A_SITE_ID" NUMBER(19,0),
   "PRESENT" NUMBER(10,0),
   "UNEXCUSED" NUMBER(10,0),
   "EXCUSED" NUMBER(10,0),
   "LATE" NUMBER(10,0),
   "LEFT_EARLY" NUMBER(10,0)
);

--------------------------------------------------------
--  DDL for Index IX_ATTD_I_STATS
--------------------------------------------------------
CREATE UNIQUE INDEX "IX_ATTD_I_STATS" ON "ATTENDANCE_ITEM_STATS_T" ("A_ITEM_STATS_ID");

--------------------------------------------------------
--  DDL for Index IX_ATTD_RULE
--------------------------------------------------------
CREATE UNIQUE INDEX "IX_ATTD_RULE" ON "ATTENDANCE_RULE_T" ("GRADING_RULE_ID");

--------------------------------------------------------
--  DDL for Index IX_ATTD_U_STATS
--------------------------------------------------------
CREATE UNIQUE INDEX "IX_ATTD_U_STATS" ON "ATTENDANCE_USER_STATS_T" ("A_USER_STATS_ID");

--------------------------------------------------------
--  Constraints for Table ATTENDANCE_ITEM_STATS_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_ITEM_STATS_T" ADD PRIMARY KEY ("A_ITEM_STATS_ID") ENABLE;
ALTER TABLE "ATTENDANCE_ITEM_STATS_T" MODIFY ("A_ITEM_STATS_ID" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table ATTENDANCE_RULE_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_RULE_T" ADD PRIMARY KEY ("GRADING_RULE_ID") ENABLE;
ALTER TABLE "ATTENDANCE_RULE_T" MODIFY ("POINTS" NOT NULL ENABLE);
ALTER TABLE "ATTENDANCE_RULE_T" MODIFY ("START_RANGE" NOT NULL ENABLE);
ALTER TABLE "ATTENDANCE_RULE_T" MODIFY ("STATUS" NOT NULL ENABLE);
ALTER TABLE "ATTENDANCE_RULE_T" MODIFY ("A_SITE_ID" NOT NULL ENABLE);
ALTER TABLE "ATTENDANCE_RULE_T" MODIFY ("GRADING_RULE_ID" NOT NULL ENABLE);

--------------------------------------------------------
--  Constraints for Table ATTENDANCE_USER_STATS_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_USER_STATS_T" ADD PRIMARY KEY ("A_USER_STATS_ID") ENABLE;
ALTER TABLE "ATTENDANCE_USER_STATS_T" MODIFY ("A_SITE_ID" NOT NULL ENABLE);
ALTER TABLE "ATTENDANCE_USER_STATS_T" MODIFY ("A_USER_STATS_ID" NOT NULL ENABLE);

--------------------------------------------------------
--  Ref Constraints for Table ATTENDANCE_RULE_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_RULE_T" ADD CONSTRAINT "FK1_ATTD_RULE" FOREIGN KEY ("A_SITE_ID")
REFERENCES "ATTENDANCE_SITE_T" ("A_SITE_ID") ENABLE;

--------------------------------------------------------
--  Ref Constraints for Table ATTENDANCE_USER_STATS_T
--------------------------------------------------------
ALTER TABLE "ATTENDANCE_USER_STATS_T" ADD CONSTRAINT "FK1_ATTD_U_STATS" FOREIGN KEY ("A_SITE_ID")
REFERENCES "ATTENDANCE_SITE_T" ("A_SITE_ID") ENABLE;

--------------------------------------------------------
-- Alter ATTENDANCE_GRADE_S sequence to be next value of
-- A_GRADE_ID (ATTENDANCE_GRADE_S sequence used to be nonexistent
-- and A_GRADE_ID was using the ATTENDANCE_SITE_S sequence.
--
-- ATTENDANCE_GRADE_S sequence is the name of the sequence made by AUTO.DDL. I see no sequence names
-- in the attendance-1.0-oracle.sql scripts so your sequence name may be different.
--------------------------------------------------------
DECLARE
  actual_sequence_number INTEGER;
  max_number_from_table INTEGER;
  difference INTEGER;
BEGIN
  SELECT ATTENDANCE_GRADE_S.CURRVAL INTO actual_sequence_number FROM DUAL;
  SELECT MAX(A_GRADE_ID) INTO max_number_from_table FROM "ATTENDANCE_GRADE_T";
  SELECT (max_number_from_table-actual_sequence_number)+1 INTO difference FROM DUAL;
  --DBMS_OUTPUT.put_line (actual_sequence_number);
  --DBMS_OUTPUT.put_line (CONCAT('alter sequence sq_cd_tp_taxa_serv increment by ', difference));
  EXECUTE IMMEDIATE CONCAT('alter sequence ATTENDANCE_GRADE_S increment by ', difference);
END;
