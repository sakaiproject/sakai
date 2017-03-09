alter table SAM_ANSWER_T drop constraint FKDD0580938152036E
alter table SAM_ANSWER_T drop constraint FKDD058093CBA347DB
alter table SAM_PUBLISHEDASSESSMENT_T drop constraint FKB2E48A65C07F835D
alter table SAM_ASSESSMENTGRADING_T drop constraint FKDAED4C879E4AF02B
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756C42AA2BC
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756E5D3D24D
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756A75F9029
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756D4927
alter table SAM_PUBLISHEDMETADATA_T drop constraint FK3D7B2712C07F835D
alter table SAM_PUBLISHEDSECTIONMETADATA_T drop constraint FKDF50FC3B7DA376A0
alter table SAM_SECTION_T drop constraint FK364450DAC07F835D
alter table SAM_PUBLISHEDITEMFEEDBACK_T drop constraint FKB7D03A3B8152036E
alter table SAM_ITEMFEEDBACK_T drop constraint FK3254E9ED8152036E
alter table SAM_ITEMMETADATA_T drop constraint FK5B4737178152036E
alter table SAM_PUBLISHEDFEEDBACK_T drop constraint FK1488D9E8C07F835D
alter table SAM_GRADINGSUMMARY_T drop constraint FKBC88AA279E4AF02B
alter table SAM_PUBLISHEDEVALUATION_T drop constraint FK94CB245FC07F835D
alter table SAM_PUBLISHEDACCESSCONTROL_T drop constraint FK2EDF39E0C07F835D
alter table SAM_ASSESSEVALUATION_T drop constraint FK6A6F29F5C07F835D
alter table SAM_ANSWERFEEDBACK_T drop constraint FK58CEF0D810DF4559
alter table SAM_PUBLISHEDANSWER_T drop constraint FKB41EA361B9BF0B8E
alter table SAM_PUBLISHEDANSWER_T drop constraint FKB41EA361CBA347DB
alter table SAM_PUBLISHEDANSWERFEEDBACK_T drop constraint FK6CB765A610DF4559
alter table SAM_PUBLISHEDITEM_T drop constraint FK53ABDCF67DA376A0
alter table SAM_ASSESSACCESSCONTROL_T drop constraint FKC945448AC07F835D
alter table SAM_PUBLISHEDSECTION_T drop constraint FK424F87CCC07F835D
alter table SAM_PUBLISHEDITEMTEXT_T drop constraint FK9C790A638152036E
alter table SAM_PUBLISHEDSECUREDIP_T drop constraint FK1EDEA25BC07F835D
alter table SAM_SECTIONMETADATA_T drop constraint FK762AD7497DA376A0
alter table SAM_ITEM_T drop constraint FK3AAC5EA87DA376A0
alter table SAM_ASSESSFEEDBACK_T drop constraint FK557D4CFEC07F835D
alter table SAM_SECUREDIP_T drop constraint FKE8C55FE9C07F835D
alter table SAM_ITEMTEXT_T drop constraint FK271D63158152036E
alter table SAM_PUBLISHEDITEMMETADATA_T drop constraint FKE0C287658152036E
alter table SAM_MEDIA_T drop constraint FKD4CF5A1971254D1C
alter table SAM_ASSESSMETADATA_T drop constraint FK7E6F9A28C07F835D
drop table SAM_ANSWER_T if exists
drop table SAM_PUBLISHEDASSESSMENT_T if exists
drop table SAM_ASSESSMENTGRADING_T if exists
drop table SAM_FUNCTIONDATA_T if exists
drop table SAM_ITEMGRADING_T if exists
drop table SAM_PUBLISHEDMETADATA_T if exists
drop table SAM_PUBLISHEDSECTIONMETADATA_T if exists
drop table SAM_SECTION_T if exists
drop table SAM_QUESTIONPOOLITEM_T if exists
drop table SAM_PUBLISHEDITEMFEEDBACK_T if exists
drop table SAM_ITEMFEEDBACK_T if exists
drop table SAM_ITEMMETADATA_T if exists
drop table SAM_PUBLISHEDFEEDBACK_T if exists
drop table SAM_GRADINGSUMMARY_T if exists
drop table SAM_PUBLISHEDEVALUATION_T if exists
drop table SAM_PUBLISHEDACCESSCONTROL_T if exists
drop table SAM_QUALIFIERDATA_T if exists
drop table SAM_QUESTIONPOOLACCESS_T if exists
drop table SAM_AUTHZDATA_T if exists
drop table SAM_ASSESSEVALUATION_T if exists
drop table SAM_ANSWERFEEDBACK_T if exists
drop table SAM_PUBLISHEDANSWER_T if exists
drop table SAM_PUBLISHEDANSWERFEEDBACK_T if exists
drop table SAM_PUBLISHEDITEM_T if exists
drop table SAM_ASSESSACCESSCONTROL_T if exists
drop table SAM_PUBLISHEDSECTION_T if exists
drop table SAM_PUBLISHEDITEMTEXT_T if exists
drop table SAM_PUBLISHEDSECUREDIP_T if exists
drop table SAM_QUESTIONPOOL_T if exists
drop table SAM_SECTIONMETADATA_T if exists
drop table SAM_ITEM_T if exists
drop table SAM_ASSESSFEEDBACK_T if exists
drop table SAM_SECUREDIP_T if exists
drop table SAM_ITEMTEXT_T if exists
drop table SAM_PUBLISHEDITEMMETADATA_T if exists
drop table SAM_MEDIA_T if exists
drop table SAM_ASSESSMENTBASE_T if exists
drop table SAM_TYPE_T if exists
drop table SAM_ASSESSMETADATA_T if exists
create table SAM_ANSWER_T (
   ANSWERID bigint generated by default as identity (start with 1),
   ITEMTEXTID bigint not null,
   ITEMID bigint not null,
   TEXT varchar(4000),
   SEQUENCE integer not null,
   LABEL varchar(20),
   ISCORRECT varchar(1),
   GRADE varchar(80),
   SCORE float
)
create table SAM_PUBLISHEDASSESSMENT_T (
   ID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   COMMENTS varchar(255),
   TYPEID varchar(36),
   INSTRUCTORNOTIFICATION integer,
   TESTEENOTIFICATION integer,
   MULTIPARTALLOWED integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null
)
create table SAM_ASSESSMENTGRADING_T (
   ASSESSMENTGRADINGID bigint generated by default as identity (start with 1),
   PUBLISHEDASSESSMENTID bigint not null,
   AGENTID varchar(36) not null,
   SUBMITTEDDATE datetime,
   ISLATE varchar(1) not null,
   FORGRADE integer not null,
   TOTALAUTOSCORE float,
   TOTALOVERRIDESCORE float,
   FINALSCORE float,
   COMMENTS varchar(4000),
   GRADEDBY varchar(36),
   GRADEDDATE datetime,
   STATUS integer not null,
   ATTEMPTDATE datetime,
   TIMEELAPSED integer
)
create table SAM_FUNCTIONDATA_T (
   FUNCTIONID bigint generated by default as identity (start with 1),
   REFERENCENAME varchar(255) not null,
   DISPLAYNAME varchar(255),
   DESCRIPTION varchar(4000),
   FUNCTIONTYPEID varchar(4000)
)
create table SAM_ITEMGRADING_T (
   ITEMGRADINGID bigint generated by default as identity (start with 1),
   ASSESSMENTGRADINGID bigint not null,
   PUBLISHEDITEMID bigint not null,
   PUBLISHEDITEMTEXTID bigint not null,
   AGENTID varchar(36) not null,
   SUBMITTEDDATE datetime not null,
   PUBLISHEDANSWERID bigint,
   RATIONALE varchar(4000),
   ANSWERTEXT varchar(4000),
   AUTOSCORE float,
   OVERRIDESCORE float,
   COMMENTS varchar(4000),
   GRADEDBY varchar(36),
   GRADEDDATE datetime,
   REVIEW integer
)
create table SAM_PUBLISHEDMETADATA_T (
   ASSESSMENTMETADATAID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255)
)
create table SAM_PUBLISHEDSECTIONMETADATA_T (
   PUBLISHEDSECTIONMETADATAID bigint generated by default as identity (start with 1),
   SECTIONID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255)
)
create table SAM_SECTION_T (
   SECTIONID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   DURATION integer,
   SEQUENCE integer,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   TYPEID integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null
)
create table SAM_QUESTIONPOOLITEM_T (
   QUESTIONPOOLID bigint not null,
   ITEMID varchar(255) not null,
   primary key (QUESTIONPOOLID, ITEMID)
)
create table SAM_PUBLISHEDITEMFEEDBACK_T (
   ITEMFEEDBACKID bigint generated by default as identity (start with 1),
   ITEMID bigint not null,
   TYPEID varchar(36) not null,
   TEXT varchar(4000)
)
create table SAM_ITEMFEEDBACK_T (
   ITEMFEEDBACKID bigint generated by default as identity (start with 1),
   ITEMID bigint not null,
   TYPEID varchar(36) not null,
   TEXT varchar(4000)
)
create table SAM_ITEMMETADATA_T (
   ITEMMETADATAID bigint generated by default as identity (start with 1),
   ITEMID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255)
)
create table SAM_PUBLISHEDFEEDBACK_T (
   ASSESSMENTID bigint not null,
   FEEDBACKDELIVERY integer,
   FEEDBACKAUTHORING integer,
   EDITCOMPONENTS integer,
   SHOWQUESTIONTEXT integer,
   SHOWSTUDENTRESPONSE integer,
   SHOWCORRECTRESPONSE integer,
   SHOWSTUDENTSCORE integer,
   SHOWSTUDENTQUESTIONSCORE integer,
   SHOWQUESTIONLEVELFEEDBACK integer,
   SHOWSELECTIONLEVELFEEDBACK integer,
   SHOWGRADERCOMMENTS integer,
   SHOWSTATISTICS integer,
   primary key (ASSESSMENTID)
)
create table SAM_GRADINGSUMMARY_T (
   ASSESSMENTGRADINGSUMMARYID bigint generated by default as identity (start with 1),
   PUBLISHEDASSESSMENTID bigint not null,
   AGENTID varchar(36) not null,
   TOTALSUBMITTED integer,
   TOTALSUBMITTEDFORGRADE integer,
   LASTSUBMITTEDDATE datetime,
   LASTSUBMITTEDASSESSMENTISLATE integer not null,
   SUMOF_AUTOSCOREFORGRADE float,
   AVERAGE_AUTOSCOREFORGRADE float,
   HIGHEST_AUTOSCOREFORGRADE float,
   LOWEST_AUTOSCOREFORGRADE float,
   LAST_AUTOSCOREFORGRADE float,
   SUMOF_OVERRIDESCOREFORGRADE float,
   AVERAGE_OVERRIDESCOREFORGRADE float,
   HIGHEST_OVERRIDESCOREFORGRADE float,
   LOWEST_OVERRIDESCOREFORGRADE float,
   LAST_OVERRIDESCOREFORGRADE float,
   SCORINGTYPE integer,
   ACCEPTEDASSESSMENTISLATE integer,
   FINALASSESSMENTSCORE float,
   FEEDTOGRADEBOOK integer
)
create table SAM_PUBLISHEDEVALUATION_T (
   ASSESSMENTID bigint not null,
   EVALUATIONCOMPONENTS varchar(255),
   SCORINGTYPE integer,
   NUMERICMODELID varchar(255),
   FIXEDTOTALSCORE integer,
   GRADEAVAILABLE integer,
   ISSTUDENTIDPUBLIC integer,
   ANONYMOUSGRADING integer,
   AUTOSCORING integer,
   TOGRADEBOOK integer,
   primary key (ASSESSMENTID)
)
create table SAM_PUBLISHEDACCESSCONTROL_T (
   ASSESSMENTID bigint not null,
   UNLIMITEDSUBMISSIONS integer,
   SUBMISSIONSALLOWED integer,
   SUBMISSIONSSAVED integer,
   ASSESSMENTFORMAT integer,
   BOOKMARKINGITEM integer,
   TIMELIMIT integer,
   TIMEDASSESSMENT integer,
   RETRYALLOWED integer,
   LATEHANDLING integer,
   STARTDATE datetime,
   DUEDATE datetime,
   SCOREDATE datetime,
   FEEDBACKDATE datetime,
   RETRACTDATE datetime,
   AUTOSUBMIT integer,
   ITEMNAVIGATION integer,
   ITEMNUMBERING integer,
   SUBMISSIONMESSAGE varchar(4000),
   RELEASETO varchar(255),
   USERNAME varchar(255),
   PASSWORD varchar(255),
   FINALPAGEURL varchar(1023),
   primary key (ASSESSMENTID)
)
create table SAM_QUALIFIERDATA_T (
   QUALIFIERID bigint not null,
   REFERENCENAME varchar(255) not null,
   DISPLAYNAME varchar(255),
   DESCRIPTION varchar(4000),
   QUALIFIERTYPEID varchar(4000),
   primary key (QUALIFIERID)
)
create table SAM_QUESTIONPOOLACCESS_T (
   QUESTIONPOOLID bigint not null,
   AGENTID varchar(255) not null,
   ACCESSTYPEID bigint not null,
   primary key (QUESTIONPOOLID, AGENTID, ACCESSTYPEID)
)
create table SAM_AUTHZDATA_T (
   ID bigint generated by default as identity (start with 1),
   lockId integer not null,
   AGENTID varchar(36) not null,
   FUNCTIONID varchar(36) not null,
   QUALIFIERID varchar(36) not null,
   EFFECTIVEDATE datetime,
   EXPIRATIONDATE datetime,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   ISEXPLICIT integer,
   unique (AGENTID, FUNCTIONID, QUALIFIERID)
)
create table SAM_ASSESSEVALUATION_T (
   ASSESSMENTID bigint not null,
   EVALUATIONCOMPONENTS varchar(255),
   SCORINGTYPE integer,
   NUMERICMODELID varchar(255),
   FIXEDTOTALSCORE integer,
   GRADEAVAILABLE integer,
   ISSTUDENTIDPUBLIC integer,
   ANONYMOUSGRADING integer,
   AUTOSCORING integer,
   TOGRADEBOOK varchar(255),
   primary key (ASSESSMENTID)
)
create table SAM_ANSWERFEEDBACK_T (
   ANSWERFEEDBACKID bigint generated by default as identity (start with 1),
   ANSWERID bigint not null,
   TYPEID varchar(36),
   TEXT varchar(4000)
)
create table SAM_PUBLISHEDANSWER_T (
   ANSWERID bigint generated by default as identity (start with 1),
   ITEMTEXTID bigint not null,
   itemId bigint not null,
   TEXT varchar(4000),
   SEQUENCE integer not null,
   LABEL varchar(20),
   ISCORRECT varchar(1),
   GRADE varchar(80),
   SCORE float
)
create table SAM_PUBLISHEDANSWERFEEDBACK_T (
   ANSWERFEEDBACKID bigint generated by default as identity (start with 1),
   ANSWERID bigint not null,
   TYPEID varchar(36),
   TEXT varchar(4000)
)
create table SAM_PUBLISHEDITEM_T (
   ITEMID bigint generated by default as identity (start with 1),
   SECTIONID bigint not null,
   ITEMIDSTRING varchar(36),
   SEQUENCE integer,
   DURATION integer,
   TRIESALLOWED integer,
   INSTRUCTION varchar(4000),
   DESCRIPTION varchar(4000),
   TYPEID varchar(36) not null,
   GRADE varchar(80),
   SCORE float,
   HINT varchar(4000),
   HASRATIONALE varchar(1),
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null
)
create table SAM_ASSESSACCESSCONTROL_T (
   ASSESSMENTID bigint not null,
   SUBMISSIONSALLOWED integer,
   UNLIMITEDSUBMISSIONS integer,
   SUBMISSIONSSAVED integer,
   ASSESSMENTFORMAT integer,
   BOOKMARKINGITEM integer,
   TIMELIMIT integer,
   TIMEDASSESSMENT integer,
   RETRYALLOWED integer,
   LATEHANDLING integer,
   STARTDATE datetime,
   DUEDATE datetime,
   SCOREDATE datetime,
   FEEDBACKDATE datetime,
   RETRACTDATE datetime,
   AUTOSUBMIT integer,
   ITEMNAVIGATION integer,
   ITEMNUMBERING integer,
   DISPLAYSCORE integer, 
   SUBMISSIONMESSAGE varchar(4000),
   RELEASETO varchar(255),
   USERNAME varchar(255),
   PASSWORD varchar(255),
   FINALPAGEURL varchar(1023),
   primary key (ASSESSMENTID)
)
create table SAM_PUBLISHEDSECTION_T (
   SECTIONID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   DURATION integer,
   SEQUENCE integer,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   TYPEID integer not null,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null
)
create table SAM_PUBLISHEDITEMTEXT_T (
   ITEMTEXTID bigint generated by default as identity (start with 1),
   ITEMID bigint not null,
   SEQUENCE integer not null,
   TEXT varchar(4000)
)
create table SAM_PUBLISHEDSECUREDIP_T (
   IPADDRESSID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   HOSTNAME varchar(255),
   IPADDRESS varchar(255)
)
create table SAM_QUESTIONPOOL_T (
   QUESTIONPOOLID bigint generated by default as identity (start with 1),
   TITLE varchar(255),
   DESCRIPTION varchar(255),
   PARENTPOOLID integer,
   OWNERID varchar(255),
   ORGANIZATIONNAME varchar(255),
   DATECREATED datetime,
   LASTMODIFIEDDATE datetime,
   LASTMODIFIEDBY varchar(255),
   DEFAULTACCESSTYPEID integer,
   OBJECTIVE varchar(255),
   KEYWORDS varchar(255),
   RUBRIC varchar(4000),
   TYPEID integer,
   INTELLECTUALPROPERTYID integer
)
create table SAM_SECTIONMETADATA_T (
   SECTIONMETADATAID bigint generated by default as identity (start with 1),
   SECTIONID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255)
)
create table SAM_ITEM_T (
   ITEMID bigint generated by default as identity (start with 1),
   SECTIONID bigint,
   ITEMIDSTRING varchar(36),
   SEQUENCE integer,
   DURATION integer,
   TRIESALLOWED integer,
   INSTRUCTION varchar(4000),
   DESCRIPTION varchar(4000),
   TYPEID varchar(36) not null,
   GRADE varchar(80),
   SCORE float,
   HINT varchar(4000),
   HASRATIONALE varchar(1),
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null
)
create table SAM_ASSESSFEEDBACK_T (
   ASSESSMENTID bigint not null,
   FEEDBACKDELIVERY integer,
   FEEDBACKAUTHORING integer,
   EDITCOMPONENTS integer,
   SHOWQUESTIONTEXT integer,
   SHOWSTUDENTRESPONSE integer,
   SHOWCORRECTRESPONSE integer,
   SHOWSTUDENTSCORE integer,
   SHOWSTUDENTQUESTIONSCORE integer,
   SHOWQUESTIONLEVELFEEDBACK integer,
   SHOWSELECTIONLEVELFEEDBACK integer,
   SHOWGRADERCOMMENTS integer,
   SHOWSTATISTICS integer,
   primary key (ASSESSMENTID)
)
create table SAM_SECUREDIP_T (
   IPADDRESSID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   HOSTNAME varchar(255),
   IPADDRESS varchar(255)
)
create table SAM_ITEMTEXT_T (
   ITEMTEXTID bigint generated by default as identity (start with 1),
   ITEMID bigint not null,
   SEQUENCE integer not null,
   TEXT varchar(4000)
)
create table SAM_PUBLISHEDITEMMETADATA_T (
   ITEMMETADATAID bigint generated by default as identity (start with 1),
   ITEMID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255)
)
create table SAM_MEDIA_T (
   MEDIAID bigint generated by default as identity (start with 1),
   ITEMGRADINGID bigint,
   MEDIA longvarbinary,
   FILESIZE integer,
   MIMETYPE varchar(80),
   DESCRIPTION varchar(4000),
   LOCATION varchar(255),
   FILENAME varchar(255),
   ISLINK integer,
   ISHTMLINLINE integer,
   STATUS integer,
   CREATEDBY varchar(36),
   CREATEDDATE datetime,
   LASTMODIFIEDBY varchar(36),
   LASTMODIFIEDDATE datetime
)
create table SAM_ASSESSMENTBASE_T (
   ID bigint generated by default as identity (start with 1),
   isTemplate varchar(255) not null,
   PARENTID integer,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   COMMENTS varchar(4000),
   TYPEID varchar(36),
   INSTRUCTORNOTIFICATION integer,
   TESTEENOTIFICATION integer,
   MULTIPARTALLOWED integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   ASSESSMENTTEMPLATEID bigint
)
create table SAM_TYPE_T (
   TYPEID bigint generated by default as identity (start with 1),
   AUTHORITY varchar(255),
   DOMAIN varchar(255),
   KEYWORD varchar(255),
   DESCRIPTION varchar(4000),
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null
)
create table SAM_ASSESSMETADATA_T (
   ASSESSMENTMETADATAID bigint generated by default as identity (start with 1),
   ASSESSMENTID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255)
)
alter table SAM_ANSWER_T add constraint FKDD0580938152036E foreign key (ITEMID) references SAM_ITEM_T
alter table SAM_ANSWER_T add constraint FKDD058093CBA347DB foreign key (ITEMTEXTID) references SAM_ITEMTEXT_T
alter table SAM_PUBLISHEDASSESSMENT_T add constraint FKB2E48A65C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T
alter table SAM_ASSESSMENTGRADING_T add constraint FKDAED4C879E4AF02B foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_ITEMGRADING_T add constraint FKB68E6756C42AA2BC foreign key (PUBLISHEDITEMID) references SAM_PUBLISHEDITEM_T
alter table SAM_ITEMGRADING_T add constraint FKB68E6756E5D3D24D foreign key (ASSESSMENTGRADINGID) references SAM_ASSESSMENTGRADING_T
alter table SAM_ITEMGRADING_T add constraint FKB68E6756A75F9029 foreign key (PUBLISHEDITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T
alter table SAM_ITEMGRADING_T add constraint FKB68E6756D4927 foreign key (PUBLISHEDANSWERID) references SAM_PUBLISHEDANSWER_T
alter table SAM_PUBLISHEDMETADATA_T add constraint FK3D7B2712C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_PUBLISHEDSECTIONMETADATA_T add constraint FKDF50FC3B7DA376A0 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T
alter table SAM_SECTION_T add constraint FK364450DAC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T
alter table SAM_PUBLISHEDITEMFEEDBACK_T add constraint FKB7D03A3B8152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T
alter table SAM_ITEMFEEDBACK_T add constraint FK3254E9ED8152036E foreign key (ITEMID) references SAM_ITEM_T
alter table SAM_ITEMMETADATA_T add constraint FK5B4737178152036E foreign key (ITEMID) references SAM_ITEM_T
alter table SAM_PUBLISHEDFEEDBACK_T add constraint FK1488D9E8C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_GRADINGSUMMARY_T add constraint FKBC88AA279E4AF02B foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_PUBLISHEDEVALUATION_T add constraint FK94CB245FC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_PUBLISHEDACCESSCONTROL_T add constraint FK2EDF39E0C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
create index sam_authz_functionId_idx on SAM_AUTHZDATA_T (FUNCTIONID)
create index sam_authz_qualifierId_idx on SAM_AUTHZDATA_T (QUALIFIERID)
alter table SAM_ASSESSEVALUATION_T add constraint FK6A6F29F5C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T
alter table SAM_ANSWERFEEDBACK_T add constraint FK58CEF0D810DF4559 foreign key (ANSWERID) references SAM_ANSWER_T
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA361B9BF0B8E foreign key (itemId) references SAM_PUBLISHEDITEM_T
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA361CBA347DB foreign key (ITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T
alter table SAM_PUBLISHEDANSWERFEEDBACK_T add constraint FK6CB765A610DF4559 foreign key (ANSWERID) references SAM_PUBLISHEDANSWER_T
alter table SAM_PUBLISHEDITEM_T add constraint FK53ABDCF67DA376A0 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T
alter table SAM_ASSESSACCESSCONTROL_T add constraint FKC945448AC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T
alter table SAM_PUBLISHEDSECTION_T add constraint FK424F87CCC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_PUBLISHEDITEMTEXT_T add constraint FK9C790A638152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T
alter table SAM_PUBLISHEDSECUREDIP_T add constraint FK1EDEA25BC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T
alter table SAM_SECTIONMETADATA_T add constraint FK762AD7497DA376A0 foreign key (SECTIONID) references SAM_SECTION_T
alter table SAM_ITEM_T add constraint FK3AAC5EA87DA376A0 foreign key (SECTIONID) references SAM_SECTION_T
alter table SAM_ASSESSFEEDBACK_T add constraint FK557D4CFEC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T
alter table SAM_SECUREDIP_T add constraint FKE8C55FE9C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T
alter table SAM_ITEMTEXT_T add constraint FK271D63158152036E foreign key (ITEMID) references SAM_ITEM_T
alter table SAM_PUBLISHEDITEMMETADATA_T add constraint FKE0C287658152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T
alter table SAM_MEDIA_T add constraint FKD4CF5A1971254D1C foreign key (ITEMGRADINGID) references SAM_ITEMGRADING_T
alter table SAM_ASSESSMETADATA_T add constraint FK7E6F9A28C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T

-- create index SAM_PUBLISHEDANSWERFEEDBCK_I01 ON SAM_PUBLISHEDANSWERFEEDBACK_T (ANSWERID);
-- create index SAM_PUBLISHEDITEMTEXT_I01 ON SAM_PUBLISHEDITEMTEXT_T (ITEMID);
-- Grading.hbm.xml
CREATE INDEX SAM_ITEMGRADING_PUBANS_I ON SAM_ITEMGRADING_T (PUBLISHEDANSWERID);
CREATE INDEX SAM_ITEMGRADING_ITEM_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMID);
CREATE INDEX SAM_ITEMGRADING_ITEMTEXT_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMTEXTID);

CREATE INDEX SAM_PUBLISHEDASSESSMENT_I ON SAM_ASSESSMENTGRADING_T (PUBLISHEDASSESSMENTID);

-- PublishedAssessment.hbm.xml
CREATE INDEX SAM_PUBA_ASSESSMENT_I ON SAM_PUBLISHEDASSESSMENT_T (ASSESSMENTID);

-- PublishedItemData.hbm.xml

-- MediaData.hbm.xml

