-- SAM-1008:  oracle only
alter table SAM_ANSWER_T add (TEMP_CLOB_TEXT clob);
update SAM_ANSWER_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_ANSWER_T drop column TEXT;
alter table SAM_ANSWER_T rename column TEMP_CLOB_TEXT to TEXT;

alter table SAM_PUBLISHEDANSWER_T add (TEMP_CLOB_TEXT clob);
update SAM_PUBLISHEDANSWER_T SET TEMP_CLOB_TEXT = TEXT;
alter table SAM_PUBLISHEDANSWER_T drop column TEXT;
alter table SAM_PUBLISHEDANSWER_T rename column TEMP_CLOB_TEXT to TEXT;
