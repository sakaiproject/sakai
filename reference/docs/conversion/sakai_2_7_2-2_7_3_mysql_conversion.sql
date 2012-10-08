-- This is the MYSQL Sakai 2.7.2 -> 2.7.3 conversion script
-- --------------------------------------------------------------------------------------------------------------------------------------
-- 
-- use this to convert a Sakai database from 2.7.2 to 2.7.3.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
-- Script insertion format
-- -- [TICKET] [short comment]
-- -- [comment continued] (repeat as necessary)
-- SQL statement
-- --------------------------------------------------------------------------------------------------------------------------------------

-- This file contains SQL to fix SAK-22700 ("The citations importer in Resources doesn't always correctly import RIS- formatted files")
update citation_schema_field set PROPERTY_VALUE='A1,AU' where FIELD_ID='creator' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='BT,T1,TI' where SCHEMA_ID='book' and FIELD_ID='title' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='CT,T1,TI' where SCHEMA_ID='chapter' and FIELD_ID='title' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='ED,A2,A3' where FIELD_ID='editor' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='JF,JO,JA,J1,J2,BT' where SCHEMA_ID='article' and FIELD_ID='sourceTitle' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='N1,AB' where FIELD_ID='note' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='N2,AB' where FIELD_ID='abstract' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='SP' where FIELD_ID='pages' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='T1,TI,CT,BT' where SCHEMA_ID='unknown' and FIELD_ID='title' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='T1,TI,CT' where SCHEMA_ID='article' and FIELD_ID='title' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='T1,TI' where SCHEMA_ID='report' and FIELD_ID='title' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='Y1,PY' where FIELD_ID='year' and PROPERTY_NAME='sakai:ris_identifier';
update citation_schema_field set PROPERTY_VALUE='Y1,PY' where FIELD_ID='date' and PROPERTY_NAME='sakai:ris_identifier';
