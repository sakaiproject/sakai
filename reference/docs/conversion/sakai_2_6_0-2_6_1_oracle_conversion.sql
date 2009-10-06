--SAK-16548 - Incorrect internationalization showing the grade NO GRADE

-- NOTE: It is possible that your xml column in assignment_submission may be a long type. You need to convert it to a clob if this is the case.
--       The following SQL will fail to run if the column is a long.

--       This seems to be randomly clob or long

--alter table assignment_submission modify xml clob;

-- Note after performing a conversion to clob your indexes may be in an invalid/unusable state. 
-- You will need to run ths following statement, and manually execute the generated 'alter indexes' and re-gather statistics on this table.
-- There are randomly named indexes so it can not be automated.

-- select 'alter index '||index_name||' rebuild online;' from user_indexes where status = 'INVALID' or status = 'UNUSABLE'; 

-- After the field is a clob continue with the updates
-- Values pulled from gen.nograd in ./assignment-bundles/assignment_*.properties
--assignment_zh_CN.properties
update assignment_submission set xml = replace(xml,unistr('"\65E0\8BC4\5206"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ar.properties
update assignment_submission set xml = replace(xml,unistr('"\0644\0627 \062A\0648\062C\062F \0623\064A \062F\0631\062C\0629."'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_pt_BR.properties
update assignment_submission set xml = replace(xml,unistr('"Nenhuma Avalia\00e7\00e3o"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_es.properties 
update assignment_submission set xml = replace(xml,unistr('"No hay calificaci\00F3n"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ko.properties
update assignment_submission set xml = replace(xml,unistr('"\d559\c810 \c5c6\c74c"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_eu.propertie
update assignment_submission set xml = replace(xml,unistr('"Kalifikatu gabe"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_nl.properties
update assignment_submission set xml = replace(xml,unistr('"Zonder beoordeling"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_fr_CA.properties
update assignment_submission set xml = replace(xml,unistr('"Aucune note"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_en_GB.properties
update assignment_submission set xml = replace(xml,unistr('"No Mark"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment.properties
update assignment_submission set xml = replace(xml,unistr('"No Grade"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ca.properties
update assignment_submission set xml = replace(xml,unistr('"No hi ha qualificació"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_pt_PT.properties
update assignment_submission set xml = replace(xml,unistr('"Sem avalia\00E7\00E3o"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ru.properties
update assignment_submission set xml = replace(xml,unistr('"\0411\0435\0437 \043e\0446\0435\043d\043a\0438"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_sv.properties
update assignment_submission set xml = replace(xml,unistr('"Betygs\00E4tts ej"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_ja.properties
update assignment_submission set xml = replace(xml,unistr('"\63a1\70b9\3057\306a\3044"'),'"gen.nograd"') where xml like '%graded="true"%';
--assignment_zh_TW.properties
update assignment_submission set xml = replace(xml,unistr('"\6c92\6709\8a55\5206"'),'"gen.nograd"') where xml like '%graded="true"%';

-- SAK-16847  asn.share.drafts permission should be added into 2.6.1 conversion script
-- This might have been added with the 2.6.0 conversion but was added after release

MERGE INTO SAKAI_REALM_FUNCTION a USING (
     SELECT 'asn.share.drafts' as FUNCTION_NAME from dual) b
 ON (a.FUNCTION_NAME = b.FUNCTION_NAME)
 WHEN NOT MATCHED THEN INSERT (FUNCTION_KEY,FUNCTION_NAME) VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'asn.share.drafts');

-- Two recent Jira's (http://jira.sakaiproject.org/browse/SAK-17061) and (http://jira.sakaiproject.org/browse/PRFL-97) 
-- have uncovered that when the field 'locked' was added to the SAKAI_PERSON_T a while ago 
-- (before 2.5.0 was cut), there was no DB upgrade script added to upgrade existing entries. 

-- Here is the Jira that added the locked field: http://jira.sakaiproject.org/browse SAK-10512

--As such, this field is null for old profiles. Its set correctly for any new profiles but all old entries need to be converted.

update SAKAI_PERSON_T set locked=0 where locked=null; 
