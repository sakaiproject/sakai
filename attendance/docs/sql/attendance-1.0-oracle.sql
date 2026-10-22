--------------------------------------------------------
--  File created - Friday-February-05-2016   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for Table ATTENDANCE_EVENT_T
--------------------------------------------------------

  CREATE TABLE "ATTENDANCE_EVENT_T" 
   (	"A_EVENT_ID" NUMBER(19,0), 
	"NAME" VARCHAR2(255 CHAR), 
	"START_DATE_TIME" TIMESTAMP (6), 
	"END_DATE_TIME" TIMESTAMP (6), 
	"IS_REOCCURRING" NUMBER(1,0), 
	"REOCCURRING_ID" NUMBER(19,0), 
	"IS_REQUIRED" NUMBER(1,0), 
	"RELEASED_TO" VARCHAR2(255 CHAR), 
	"LOCATION" VARCHAR2(255 CHAR), 
	"A_SITE_ID" NUMBER(19,0)
   );
--------------------------------------------------------
--  DDL for Table ATTENDANCE_GRADE_T
--------------------------------------------------------

  CREATE TABLE "ATTENDANCE_GRADE_T" 
   (	"A_GRADE_ID" NUMBER(19,0), 
	"GRADE" FLOAT(126), 
	"USER_ID" VARCHAR2(255 CHAR), 
	"A_SITE_ID" NUMBER(19,0)
   );
--------------------------------------------------------
--  DDL for Table ATTENDANCE_RECORD_T
--------------------------------------------------------

  CREATE TABLE "ATTENDANCE_RECORD_T" 
   (	"A_RECORD_ID" NUMBER(19,0), 
	"USER_ID" VARCHAR2(255 CHAR), 
	"STATUS" VARCHAR2(255 CHAR), 
	"A_EVENT_ID" NUMBER(19,0), 
	"RECORD_COMMENT" CLOB
   ); 
--------------------------------------------------------
--  DDL for Table ATTENDANCE_SITE_T
--------------------------------------------------------

  CREATE TABLE "ATTENDANCE_SITE_T" 
   (	"A_SITE_ID" NUMBER(19,0), 
	"SITE_ID" VARCHAR2(255 CHAR), 
	"DEFAULT_STATUS" VARCHAR2(255 CHAR), 
	"MAXIMUM_GRADE" FLOAT(126), 
	"IS_GRADE_SHOWN" NUMBER(1,0), 
	"SEND_TO_GRADEBOOK" NUMBER(1,0), 
	"GRADEBOOK_ITEM_NAME" VARCHAR2(255 CHAR), 
	"SHOW_COMMENTS" NUMBER(1,0)
   ); 
--------------------------------------------------------
--  DDL for Table ATTENDANCE_STATUS_T
--------------------------------------------------------

  CREATE TABLE "ATTENDANCE_STATUS_T" 
   (	"A_STATUS_ID" NUMBER(19,0), 
	"IS_ACTIVE" NUMBER(1,0), 
	"STATUS" VARCHAR2(255 CHAR), 
	"SORT_ORDER" NUMBER(10,0), 
	"A_SITE_ID" NUMBER(19,0)
   ); 
--------------------------------------------------------
--  DDL for Index UNQ_ATTD_EVENT 
--------------------------------------------------------

  CREATE UNIQUE INDEX "UNQ_ATTD_EVENT" ON "ATTENDANCE_EVENT_T" ("A_EVENT_ID") 
  ;
--------------------------------------------------------
--  DDL for Index UNQ_ATTD_GRADE 
--------------------------------------------------------

  CREATE UNIQUE INDEX "UNQ_ATTD_GRADE" ON "ATTENDANCE_GRADE_T" ("A_GRADE_ID") 
  ;
--------------------------------------------------------
--  DDL for Index UNQ_ATTD_RECORD 
--------------------------------------------------------

  CREATE UNIQUE INDEX "UNQ_ATTD_RECORD" ON "ATTENDANCE_RECORD_T" ("A_RECORD_ID") 
  ;
--------------------------------------------------------
--  DDL for Index UNQ_ATTD_SITE 
--------------------------------------------------------

  CREATE UNIQUE INDEX "UNQ_ATTD_SITE" ON "ATTENDANCE_SITE_T" ("A_SITE_ID") 
  ;
--------------------------------------------------------
--  DDL for Index UNQ_ATTD_STATUS 
--------------------------------------------------------

  CREATE UNIQUE INDEX "UNQ_ATTD_STATUS" ON "ATTENDANCE_STATUS_T" ("A_STATUS_ID") 
  ;
--------------------------------------------------------
--  Constraints for Table ATTENDANCE_EVENT_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_EVENT_T" ADD PRIMARY KEY ("A_EVENT_ID")
  USING INDEX ENABLE; 
  ALTER TABLE "ATTENDANCE_EVENT_T" MODIFY ("A_SITE_ID" NOT NULL ENABLE);
  ALTER TABLE "ATTENDANCE_EVENT_T" MODIFY ("A_EVENT_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table ATTENDANCE_GRADE_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_GRADE_T" ADD PRIMARY KEY ("A_GRADE_ID")
  USING INDEX ENABLE; 
  ALTER TABLE "ATTENDANCE_GRADE_T" MODIFY ("A_SITE_ID" NOT NULL ENABLE);
  ALTER TABLE "ATTENDANCE_GRADE_T" MODIFY ("A_GRADE_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table ATTENDANCE_RECORD_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_RECORD_T" ADD PRIMARY KEY ("A_RECORD_ID")
  USING INDEX ENABLE; 
  ALTER TABLE "ATTENDANCE_RECORD_T" MODIFY ("A_EVENT_ID" NOT NULL ENABLE);
  ALTER TABLE "ATTENDANCE_RECORD_T" MODIFY ("A_RECORD_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table ATTENDANCE_SITE_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_SITE_T" ADD PRIMARY KEY ("A_SITE_ID")
  USING INDEX ENABLE;
  ALTER TABLE "ATTENDANCE_SITE_T" MODIFY ("A_SITE_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Constraints for Table ATTENDANCE_STATUS_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_STATUS_T" ADD PRIMARY KEY ("A_STATUS_ID")
  USING INDEX ENABLE; 
  ALTER TABLE "ATTENDANCE_STATUS_T" MODIFY ("A_SITE_ID" NOT NULL ENABLE);
  ALTER TABLE "ATTENDANCE_STATUS_T" MODIFY ("A_STATUS_ID" NOT NULL ENABLE);
--------------------------------------------------------
--  Ref Constraints for Table ATTENDANCE_EVENT_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_EVENT_T" ADD CONSTRAINT "FK1_ATTD_EVENT" FOREIGN KEY ("A_SITE_ID")
	  REFERENCES "ATTENDANCE_SITE_T" ("A_SITE_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table ATTENDANCE_GRADE_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_GRADE_T" ADD CONSTRAINT "FK1_ATTD_GRADE" FOREIGN KEY ("A_SITE_ID")
	  REFERENCES "ATTENDANCE_SITE_T" ("A_SITE_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table ATTENDANCE_RECORD_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_RECORD_T" ADD CONSTRAINT "FK1_ATTD_RECORD" FOREIGN KEY ("A_EVENT_ID")
	  REFERENCES "ATTENDANCE_EVENT_T" ("A_EVENT_ID") ENABLE;
--------------------------------------------------------
--  Ref Constraints for Table ATTENDANCE_STATUS_T
--------------------------------------------------------

  ALTER TABLE "ATTENDANCE_STATUS_T" ADD CONSTRAINT "FK1_ATTD_STATUS" FOREIGN KEY ("A_SITE_ID")
	  REFERENCES "ATTENDANCE_SITE_T" ("A_SITE_ID") ENABLE;

