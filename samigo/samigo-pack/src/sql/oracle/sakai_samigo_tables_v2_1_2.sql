alter table SAM_ANSWER_T drop constraint FKDD0580938152036E;
alter table SAM_ANSWER_T drop constraint FKDD058093CBA347DB;
alter table SAM_PUBLISHEDASSESSMENT_T drop constraint FKB2E48A65C07F835D;
alter table SAM_ASSESSMENTGRADING_T drop constraint FKDAED4C879E4AF02B;
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756C42AA2BC;
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756E5D3D24D;
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756A75F9029;
alter table SAM_ITEMGRADING_T drop constraint FKB68E6756D4927;
alter table SAM_PUBLISHEDMETADATA_T drop constraint FK3D7B2712C07F835D;
alter table SAM_PUBLISHEDSECTIONMETADATA_T drop constraint FKDF50FC3B7DA376A0;
alter table SAM_SECTION_T drop constraint FK364450DAC07F835D;
alter table SAM_PUBLISHEDITEMFEEDBACK_T drop constraint FKB7D03A3B8152036E;
alter table SAM_ITEMFEEDBACK_T drop constraint FK3254E9ED8152036E;
alter table SAM_ITEMMETADATA_T drop constraint FK5B4737178152036E;
alter table SAM_PUBLISHEDFEEDBACK_T drop constraint FK1488D9E8C07F835D;
alter table SAM_GRADINGSUMMARY_T drop constraint FKBC88AA279E4AF02B;
alter table SAM_PUBLISHEDEVALUATION_T drop constraint FK94CB245FC07F835D;
alter table SAM_PUBLISHEDACCESSCONTROL_T drop constraint FK2EDF39E0C07F835D;
alter table SAM_ASSESSEVALUATION_T drop constraint FK6A6F29F5C07F835D;
alter table SAM_ANSWERFEEDBACK_T drop constraint FK58CEF0D810DF4559;
alter table SAM_PUBLISHEDANSWER_T drop constraint FKB41EA361B9BF0B8E;
alter table SAM_PUBLISHEDANSWER_T drop constraint FKB41EA361CBA347DB;
alter table SAM_PUBLISHEDANSWERFEEDBACK_T drop constraint FK6CB765A610DF4559;
alter table SAM_PUBLISHEDITEM_T drop constraint FK53ABDCF67DA376A0;
alter table SAM_ASSESSACCESSCONTROL_T drop constraint FKC945448AC07F835D;
alter table SAM_PUBLISHEDSECTION_T drop constraint FK424F87CCC07F835D;
alter table SAM_PUBLISHEDITEMTEXT_T drop constraint FK9C790A638152036E;
alter table SAM_PUBLISHEDSECUREDIP_T drop constraint FK1EDEA25BC07F835D;
alter table SAM_SECTIONMETADATA_T drop constraint FK762AD7497DA376A0;
alter table SAM_ITEM_T drop constraint FK3AAC5EA87DA376A0;
alter table SAM_ASSESSFEEDBACK_T drop constraint FK557D4CFEC07F835D;
alter table SAM_SECUREDIP_T drop constraint FKE8C55FE9C07F835D;
alter table SAM_ITEMTEXT_T drop constraint FK271D63158152036E;
alter table SAM_PUBLISHEDITEMMETADATA_T drop constraint FKE0C287658152036E;
alter table SAM_MEDIA_T drop constraint FKD4CF5A1971254D1C;
alter table SAM_ASSESSMETADATA_T drop constraint FK7E6F9A28C07F835D;
drop table SAM_ANSWER_T cascade constraints;
drop table SAM_PUBLISHEDASSESSMENT_T cascade constraints;
drop table SAM_ASSESSMENTGRADING_T cascade constraints;
drop table SAM_FUNCTIONDATA_T cascade constraints;
drop table SAM_ITEMGRADING_T cascade constraints;
drop table SAM_PUBLISHEDMETADATA_T cascade constraints;
drop table SAM_PUBLISHEDSECTIONMETADATA_T cascade constraints;
drop table SAM_SECTION_T cascade constraints;
drop table SAM_QUESTIONPOOLITEM_T cascade constraints;
drop table SAM_PUBLISHEDITEMFEEDBACK_T cascade constraints;
drop table SAM_ITEMFEEDBACK_T cascade constraints;
drop table SAM_ITEMMETADATA_T cascade constraints;
drop table SAM_PUBLISHEDFEEDBACK_T cascade constraints;
drop table SAM_GRADINGSUMMARY_T cascade constraints;
drop table SAM_PUBLISHEDEVALUATION_T cascade constraints;
drop table SAM_PUBLISHEDACCESSCONTROL_T cascade constraints;
drop table SAM_QUALIFIERDATA_T cascade constraints;
drop table SAM_QUESTIONPOOLACCESS_T cascade constraints;
drop table SAM_AUTHZDATA_T cascade constraints;
drop table SAM_ASSESSEVALUATION_T cascade constraints;
drop table SAM_ANSWERFEEDBACK_T cascade constraints;
drop table SAM_PUBLISHEDANSWER_T cascade constraints;
drop table SAM_PUBLISHEDANSWERFEEDBACK_T cascade constraints;
drop table SAM_PUBLISHEDITEM_T cascade constraints;
drop table SAM_ASSESSACCESSCONTROL_T cascade constraints;
drop table SAM_PUBLISHEDSECTION_T cascade constraints;
drop table SAM_PUBLISHEDITEMTEXT_T cascade constraints;
drop table SAM_PUBLISHEDSECUREDIP_T cascade constraints;
drop table SAM_QUESTIONPOOL_T cascade constraints;
drop table SAM_SECTIONMETADATA_T cascade constraints;
drop table SAM_ITEM_T cascade constraints;
drop table SAM_ASSESSFEEDBACK_T cascade constraints;
drop table SAM_SECUREDIP_T cascade constraints;
drop table SAM_ITEMTEXT_T cascade constraints;
drop table SAM_PUBLISHEDITEMMETADATA_T cascade constraints;
drop table SAM_MEDIA_T cascade constraints;
drop table SAM_ASSESSMENTBASE_T cascade constraints;
drop table SAM_TYPE_T cascade constraints;
drop table SAM_ASSESSMETADATA_T cascade constraints;
drop sequence SAM_PUBANSWER_ID_S;
drop sequence SAM_PUBANSWERFEEDBACK_ID_S;
drop sequence SAM_PUBITEMMETADATA_ID_S;
drop sequence SAM_QUESTIONPOOL_ID_S;
drop sequence SAM_FUNCTIONID_S;
drop sequence SAM_PUBLISHEDMETADATA_ID_S;
drop sequence SAM_TYPE_ID_S;
drop sequence SAM_GRADINGSUMMARY_ID_S;
drop sequence SAM_PUBLISHEDASSESSMENT_ID_S;
drop sequence SAM_ASSESSMENTBASE_ID_S;
drop sequence SAM_ASSESSMENTGRADING_ID_S;
drop sequence SAM_ITEMMETADATA_ID_S;
drop sequence SAM_PUBITEM_ID_S;
drop sequence SAM_SECTION_ID_S;
drop sequence SAM_ASSESSMETADATA_ID_S;
drop sequence SAM_PUBLISHEDSECUREDIP_ID_S;
drop sequence SAM_MEDIA_ID_S;
drop sequence SAM_PUBLISHEDSECTION_ID_S;
drop sequence SAM_SECTIONMETADATA_ID_S;
drop sequence SAM_SECUREDIP_ID_S;
drop sequence SAM_ANSWER_ID_S;
drop sequence SAM_ITEMTEXT_ID_S;
drop sequence SAM_ITEM_ID_S;
drop sequence SAM_AUTHZDATA_S;
drop sequence SAM_ITEMGRADING_ID_S;
drop sequence SAM_ITEMFEEDBACK_ID_S;
drop sequence SAM_ANSWERFEEDBACK_ID_S;
drop sequence SAM_PUBITEMTEXT_ID_S;
drop sequence SAM_PUBITEMFEEDBACK_ID_S;
create table SAM_ANSWER_T (
   ANSWERID number(19,0) not null,
   ITEMTEXTID number(19,0) not null,
   ITEMID number(19,0) not null,
   TEXT varchar(4000),
   SEQUENCE integer not null,
   LABEL varchar(20),
   ISCORRECT varchar(1),
   GRADE varchar(80),
   SCORE float,
   primary key (ANSWERID)
);
create table SAM_PUBLISHEDASSESSMENT_T (
   ID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   COMMENTS varchar(255),
   TYPEID varchar(36),
   INSTRUCTORNOTIFICATION integer,
   TESTEENOTIFICATION integer,
   MULTIPARTALLOWED integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   primary key (ID)
);
create table SAM_ASSESSMENTGRADING_T (
   ASSESSMENTGRADINGID number(19,0) not null,
   PUBLISHEDASSESSMENTID number(19,0) not null,
   AGENTID varchar(36) not null,
   SUBMITTEDDATE date,
   ISLATE varchar(1) not null,
   FORGRADE integer not null,
   TOTALAUTOSCORE float,
   TOTALOVERRIDESCORE float,
   FINALSCORE float,
   COMMENTS varchar(4000),
   GRADEDBY varchar(36),
   GRADEDDATE date,
   STATUS integer not null,
   ATTEMPTDATE date,
   TIMEELAPSED integer,
   primary key (ASSESSMENTGRADINGID)
);
create table SAM_FUNCTIONDATA_T (
   FUNCTIONID number(19,0) not null,
   REFERENCENAME varchar(255) not null,
   DISPLAYNAME varchar(255),
   DESCRIPTION varchar(4000),
   FUNCTIONTYPEID varchar(4000),
   primary key (FUNCTIONID)
);
create table SAM_ITEMGRADING_T (
   ITEMGRADINGID number(19,0) not null,
   ASSESSMENTGRADINGID number(19,0) not null,
   PUBLISHEDITEMID number(19,0) not null,
   PUBLISHEDITEMTEXTID number(19,0) not null,
   AGENTID varchar(36) not null,
   SUBMITTEDDATE date not null,
   PUBLISHEDANSWERID number(19,0),
   RATIONALE varchar(4000),
   ANSWERTEXT varchar(4000),
   AUTOSCORE float,
   OVERRIDESCORE float,
   COMMENTS varchar(4000),
   GRADEDBY varchar(36),
   GRADEDDATE date,
   REVIEW integer,
   primary key (ITEMGRADINGID)
);
create table SAM_PUBLISHEDMETADATA_T (
   ASSESSMENTMETADATAID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ASSESSMENTMETADATAID)
);
create table SAM_PUBLISHEDSECTIONMETADATA_T (
   PUBLISHEDSECTIONMETADATAID number(19,0) not null,
   SECTIONID number(19,0) not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (PUBLISHEDSECTIONMETADATAID)
);
create table SAM_SECTION_T (
   SECTIONID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   DURATION integer,
   SEQUENCE integer,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   TYPEID integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   primary key (SECTIONID)
);
create table SAM_QUESTIONPOOLITEM_T (
   QUESTIONPOOLID number(19,0) not null,
   ITEMID varchar2(255) not null,
   primary key (QUESTIONPOOLID, ITEMID)
);
create table SAM_PUBLISHEDITEMFEEDBACK_T (
   ITEMFEEDBACKID number(19,0) not null,
   ITEMID number(19,0) not null,
   TYPEID varchar(36) not null,
   TEXT varchar(4000),
   primary key (ITEMFEEDBACKID)
);
create table SAM_ITEMFEEDBACK_T (
   ITEMFEEDBACKID number(19,0) not null,
   ITEMID number(19,0) not null,
   TYPEID varchar(36) not null,
   TEXT varchar(4000),
   primary key (ITEMFEEDBACKID)
);
create table SAM_ITEMMETADATA_T (
   ITEMMETADATAID number(19,0) not null,
   ITEMID number(19,0) not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ITEMMETADATAID)
);
create table SAM_PUBLISHEDFEEDBACK_T (
   ASSESSMENTID number(19,0) not null,
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
);
create table SAM_GRADINGSUMMARY_T (
   ASSESSMENTGRADINGSUMMARYID number(19,0) not null,
   PUBLISHEDASSESSMENTID number(19,0) not null,
   AGENTID varchar(36) not null,
   TOTALSUBMITTED integer,
   TOTALSUBMITTEDFORGRADE integer,
   LASTSUBMITTEDDATE date,
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
   FEEDTOGRADEBOOK integer,
   primary key (ASSESSMENTGRADINGSUMMARYID)
);
create table SAM_PUBLISHEDEVALUATION_T (
   ASSESSMENTID number(19,0) not null,
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
);
create table SAM_PUBLISHEDACCESSCONTROL_T (
   ASSESSMENTID number(19,0) not null,
   UNLIMITEDSUBMISSIONS integer,
   SUBMISSIONSALLOWED integer,
   SUBMISSIONSSAVED integer,
   ASSESSMENTFORMAT integer,
   BOOKMARKINGITEM integer,
   TIMELIMIT integer,
   TIMEDASSESSMENT integer,
   RETRYALLOWED integer,
   LATEHANDLING integer,
   STARTDATE date,
   DUEDATE date,
   SCOREDATE date,
   FEEDBACKDATE date,
   RETRACTDATE date,
   AUTOSUBMIT integer,
   ITEMNAVIGATION integer,
   ITEMNUMBERING integer,
   SUBMISSIONMESSAGE varchar(4000),
   RELEASETO varchar(255),
   USERNAME varchar(255),
   PASSWORD varchar(255),
   FINALPAGEURL varchar(1023),
   primary key (ASSESSMENTID)
);
create table SAM_QUALIFIERDATA_T (
   QUALIFIERID number(19,0) not null,
   REFERENCENAME varchar(255) not null,
   DISPLAYNAME varchar(255),
   DESCRIPTION varchar(4000),
   QUALIFIERTYPEID varchar(4000),
   primary key (QUALIFIERID)
);
create table SAM_QUESTIONPOOLACCESS_T (
   QUESTIONPOOLID number(19,0) not null,
   AGENTID varchar2(255) not null,
   ACCESSTYPEID number(19,0) not null,
   primary key (QUESTIONPOOLID, AGENTID, ACCESSTYPEID)
);
create table SAM_AUTHZDATA_T (
   ID number(19,0) not null,
   lockId number(10,0) not null,
   AGENTID varchar2(36) not null,
   FUNCTIONID varchar2(36) not null,
   QUALIFIERID varchar2(36) not null,
   EFFECTIVEDATE date,
   EXPIRATIONDATE date,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   ISEXPLICIT integer,
   primary key (ID),
   unique (AGENTID, FUNCTIONID, QUALIFIERID)
);
create table SAM_ASSESSEVALUATION_T (
   ASSESSMENTID number(19,0) not null,
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
);
create table SAM_ANSWERFEEDBACK_T (
   ANSWERFEEDBACKID number(19,0) not null,
   ANSWERID number(19,0) not null,
   TYPEID varchar(36),
   TEXT varchar(4000),
   primary key (ANSWERFEEDBACKID)
);
create table SAM_PUBLISHEDANSWER_T (
   ANSWERID number(19,0) not null,
   ITEMTEXTID number(19,0) not null,
   itemId number(19,0) not null,
   TEXT varchar(4000),
   SEQUENCE integer not null,
   LABEL varchar(20),
   ISCORRECT varchar(1),
   GRADE varchar(80),
   SCORE float,
   primary key (ANSWERID)
);
create table SAM_PUBLISHEDANSWERFEEDBACK_T (
   ANSWERFEEDBACKID number(19,0) not null,
   ANSWERID number(19,0) not null,
   TYPEID varchar(36),
   TEXT varchar(4000),
   primary key (ANSWERFEEDBACKID)
);
create table SAM_PUBLISHEDITEM_T (
   ITEMID number(19,0) not null,
   SECTIONID number(19,0) not null,
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
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   primary key (ITEMID)
);
create table SAM_ASSESSACCESSCONTROL_T (
   ASSESSMENTID number(19,0) not null,
   SUBMISSIONSALLOWED integer,
   UNLIMITEDSUBMISSIONS integer,
   SUBMISSIONSSAVED integer,
   ASSESSMENTFORMAT integer,
   BOOKMARKINGITEM integer,
   TIMELIMIT integer,
   TIMEDASSESSMENT integer,
   RETRYALLOWED integer,
   LATEHANDLING integer,
   STARTDATE date,
   DUEDATE date,
   SCOREDATE date,
   FEEDBACKDATE date,
   RETRACTDATE date,
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
);
create table SAM_PUBLISHEDSECTION_T (
   SECTIONID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   DURATION integer,
   SEQUENCE integer,
   TITLE varchar(255),
   DESCRIPTION varchar(4000),
   TYPEID integer not null,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   primary key (SECTIONID)
);
create table SAM_PUBLISHEDITEMTEXT_T (
   ITEMTEXTID number(19,0) not null,
   ITEMID number(19,0) not null,
   SEQUENCE integer not null,
   TEXT varchar(4000),
   primary key (ITEMTEXTID)
);
create table SAM_PUBLISHEDSECUREDIP_T (
   IPADDRESSID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   HOSTNAME varchar(255),
   IPADDRESS varchar(255),
   primary key (IPADDRESSID)
);
create table SAM_QUESTIONPOOL_T (
   QUESTIONPOOLID number(19,0) not null,
   TITLE varchar(255),
   DESCRIPTION varchar(255),
   PARENTPOOLID integer,
   OWNERID varchar(255),
   ORGANIZATIONNAME varchar(255),
   DATECREATED date,
   LASTMODIFIEDDATE date,
   LASTMODIFIEDBY varchar(255),
   DEFAULTACCESSTYPEID integer,
   OBJECTIVE varchar(255),
   KEYWORDS varchar(255),
   RUBRIC varchar(4000),
   TYPEID integer,
   INTELLECTUALPROPERTYID integer,
   primary key (QUESTIONPOOLID)
);
create table SAM_SECTIONMETADATA_T (
   SECTIONMETADATAID number(19,0) not null,
   SECTIONID number(19,0) not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (SECTIONMETADATAID)
);
create table SAM_ITEM_T (
   ITEMID number(19,0) not null,
   SECTIONID number(19,0),
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
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   primary key (ITEMID)
);
create table SAM_ASSESSFEEDBACK_T (
   ASSESSMENTID number(19,0) not null,
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
);
create table SAM_SECUREDIP_T (
   IPADDRESSID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   HOSTNAME varchar(255),
   IPADDRESS varchar(255),
   primary key (IPADDRESSID)
);
create table SAM_ITEMTEXT_T (
   ITEMTEXTID number(19,0) not null,
   ITEMID number(19,0) not null,
   SEQUENCE integer not null,
   TEXT varchar(4000),
   primary key (ITEMTEXTID)
);
create table SAM_PUBLISHEDITEMMETADATA_T (
   ITEMMETADATAID number(19,0) not null,
   ITEMID number(19,0) not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ITEMMETADATAID)
);
create table SAM_MEDIA_T (
   MEDIAID number(19,0) not null,
   ITEMGRADINGID number(19,0),
   MEDIA blob,
   FILESIZE integer,
   MIMETYPE varchar(80),
   DESCRIPTION varchar(4000),
   LOCATION varchar(255),
   FILENAME varchar(255),
   ISLINK integer,
   ISHTMLINLINE integer,
   STATUS integer,
   CREATEDBY varchar(36),
   CREATEDDATE date,
   LASTMODIFIEDBY varchar(36),
   LASTMODIFIEDDATE date,
   primary key (MEDIAID)
);
create table SAM_ASSESSMENTBASE_T (
   ID number(19,0) not null,
   isTemplate varchar2(255) not null,
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
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   ASSESSMENTTEMPLATEID number(19,0),
   primary key (ID)
);
create table SAM_TYPE_T (
   TYPEID number(19,0) not null,
   AUTHORITY varchar(255),
   DOMAIN varchar(255),
   KEYWORD varchar(255),
   DESCRIPTION varchar(4000),
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE date not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE date not null,
   primary key (TYPEID)
);
create table SAM_ASSESSMETADATA_T (
   ASSESSMENTMETADATAID number(19,0) not null,
   ASSESSMENTID number(19,0) not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ASSESSMENTMETADATAID)
);
alter table SAM_ANSWER_T add constraint FKDD0580938152036E foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_ANSWER_T add constraint FKDD058093CBA347DB foreign key (ITEMTEXTID) references SAM_ITEMTEXT_T;
alter table SAM_PUBLISHEDASSESSMENT_T add constraint FKB2E48A65C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_ASSESSMENTGRADING_T add constraint FKDAED4C879E4AF02B foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_ITEMGRADING_T add constraint FKB68E6756C42AA2BC foreign key (PUBLISHEDITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_ITEMGRADING_T add constraint FKB68E6756E5D3D24D foreign key (ASSESSMENTGRADINGID) references SAM_ASSESSMENTGRADING_T;
alter table SAM_ITEMGRADING_T add constraint FKB68E6756A75F9029 foreign key (PUBLISHEDITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T;
alter table SAM_ITEMGRADING_T add constraint FKB68E6756D4927 foreign key (PUBLISHEDANSWERID) references SAM_PUBLISHEDANSWER_T;
alter table SAM_PUBLISHEDMETADATA_T add constraint FK3D7B2712C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDSECTIONMETADATA_T add constraint FKDF50FC3B7DA376A0 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T;
alter table SAM_SECTION_T add constraint FK364450DAC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_PUBLISHEDITEMFEEDBACK_T add constraint FKB7D03A3B8152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_ITEMFEEDBACK_T add constraint FK3254E9ED8152036E foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_ITEMMETADATA_T add constraint FK5B4737178152036E foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_PUBLISHEDFEEDBACK_T add constraint FK1488D9E8C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_GRADINGSUMMARY_T add constraint FKBC88AA279E4AF02B foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDEVALUATION_T add constraint FK94CB245FC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDACCESSCONTROL_T add constraint FK2EDF39E0C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
create index sam_authz_functionId_idx on SAM_AUTHZDATA_T (FUNCTIONID);
create index sam_authz_qualifierId_idx on SAM_AUTHZDATA_T (QUALIFIERID);
create index sam_authz_agentId_idx on SAM_AUTHZDATA_T (AGENTID);
alter table SAM_ASSESSEVALUATION_T add constraint FK6A6F29F5C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_ANSWERFEEDBACK_T add constraint FK58CEF0D810DF4559 foreign key (ANSWERID) references SAM_ANSWER_T;
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA361B9BF0B8E foreign key (itemId) references SAM_PUBLISHEDITEM_T;
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA361CBA347DB foreign key (ITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T;
alter table SAM_PUBLISHEDANSWERFEEDBACK_T add constraint FK6CB765A610DF4559 foreign key (ANSWERID) references SAM_PUBLISHEDANSWER_T;
alter table SAM_PUBLISHEDITEM_T add constraint FK53ABDCF67DA376A0 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T;
alter table SAM_ASSESSACCESSCONTROL_T add constraint FKC945448AC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_PUBLISHEDSECTION_T add constraint FK424F87CCC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDITEMTEXT_T add constraint FK9C790A638152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_PUBLISHEDSECUREDIP_T add constraint FK1EDEA25BC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_SECTIONMETADATA_T add constraint FK762AD7497DA376A0 foreign key (SECTIONID) references SAM_SECTION_T;
alter table SAM_ITEM_T add constraint FK3AAC5EA87DA376A0 foreign key (SECTIONID) references SAM_SECTION_T;
alter table SAM_ASSESSFEEDBACK_T add constraint FK557D4CFEC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_SECUREDIP_T add constraint FKE8C55FE9C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_ITEMTEXT_T add constraint FK271D63158152036E foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_PUBLISHEDITEMMETADATA_T add constraint FKE0C287658152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_MEDIA_T add constraint FKD4CF5A1971254D1C foreign key (ITEMGRADINGID) references SAM_ITEMGRADING_T;
alter table SAM_ASSESSMETADATA_T add constraint FK7E6F9A28C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
create sequence SAM_PUBANSWER_ID_S;
create sequence SAM_PUBANSWERFEEDBACK_ID_S;
create sequence SAM_PUBITEMMETADATA_ID_S;
create sequence SAM_QUESTIONPOOL_ID_S;
create sequence SAM_FUNCTIONID_S;
create sequence SAM_PUBLISHEDMETADATA_ID_S;
create sequence SAM_TYPE_ID_S;
create sequence SAM_GRADINGSUMMARY_ID_S;
create sequence SAM_PUBLISHEDASSESSMENT_ID_S;
create sequence SAM_ASSESSMENTBASE_ID_S;
create sequence SAM_ASSESSMENTGRADING_ID_S;
create sequence SAM_ITEMMETADATA_ID_S;
create sequence SAM_PUBITEM_ID_S;
create sequence SAM_SECTION_ID_S;
create sequence SAM_ASSESSMETADATA_ID_S;
create sequence SAM_PUBLISHEDSECUREDIP_ID_S;
create sequence SAM_MEDIA_ID_S;
create sequence SAM_PUBLISHEDSECTION_ID_S;
create sequence SAM_SECTIONMETADATA_ID_S;
create sequence SAM_SECUREDIP_ID_S;
create sequence SAM_ANSWER_ID_S;
create sequence SAM_ITEMTEXT_ID_S;
create sequence SAM_ITEM_ID_S;
create sequence SAM_AUTHZDATA_S;
create sequence SAM_ITEMGRADING_ID_S;
create sequence SAM_ITEMFEEDBACK_ID_S;
create sequence SAM_ANSWERFEEDBACK_ID_S;
create sequence SAM_PUBITEMTEXT_ID_S;
create sequence SAM_PUBITEMFEEDBACK_ID_S;
commit;

-- SAK-2461: improve performance (these are replaced by the followings in 2.1.2)
-- create index SAM_PUBLISHEDITEMTEXT_I01 ON SAM_PUBLISHEDITEMTEXT_T (ITEMID); 
-- create index SAM_PUBLISHEDANSWERFEEDBCK_I01 ON SAM_PUBLISHEDANSWERFEEDBACK_T (ANSWERID); 

-- Grading.hbm.xml
CREATE INDEX SAM_ASSESSMENTGRADING_I ON SAM_ITEMGRADING_T (ASSESSMENTGRADINGID);
CREATE INDEX SAM_ITEMGRADING_PUBANS_I ON SAM_ITEMGRADING_T (PUBLISHEDANSWERID);
CREATE INDEX SAM_ITEMGRADING_ITEM_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMID);
CREATE INDEX SAM_ITEMGRADING_ITEMTEXT_I ON SAM_ITEMGRADING_T (PUBLISHEDITEMTEXTID);

CREATE INDEX SAM_PUBLISHEDASSESSMENT_I ON SAM_ASSESSMENTGRADING_T (PUBLISHEDASSESSMENTID);

-- PublishedAssessment.hbm.xml
CREATE INDEX SAM_PUBA_ASSESSMENT_I ON SAM_PUBLISHEDASSESSMENT_T (ASSESSMENTID);
CREATE INDEX SAM_PUBSECTION_ASSESSMENT_I ON SAM_PUBLISHEDSECTION_T (ASSESSMENTID);
CREATE INDEX SAM_PUBSECTIONMETA_SECTION_I ON SAM_PUBLISHEDSECTIONMETADATA_T (SECTIONID);
CREATE INDEX SAM_PUBIP_ASSESSMENT_I ON SAM_PUBLISHEDSECUREDIP_T (ASSESSMENTID);

-- PublishedItemData.hbm.xml
CREATE INDEX SAM_PUBITEM_SECTION_I ON SAM_PUBLISHEDITEM_T (SECTIONID);
CREATE INDEX SAM_PUBITEMTEXT_ITEM_I ON SAM_PUBLISHEDITEMTEXT_T (ITEMID);
CREATE INDEX SAM_PUBITEMMETA_ITEM_I ON SAM_PUBLISHEDITEMMETADATA_T (ITEMID);
CREATE INDEX SAM_PUBITEMFB_ITEM_I ON SAM_PUBLISHEDITEMFEEDBACK_T (ITEMID);
CREATE INDEX SAM_PUBANSWER_ITEMTEXT_I ON SAM_PUBLISHEDANSWER_T (ITEMTEXTID);
CREATE INDEX SAM_PUBANSWER_ITEM_I ON SAM_PUBLISHEDANSWER_T (ITEMID);
CREATE INDEX SAM_PUBANSWERFB_ANSWER_I ON SAM_PUBLISHEDANSWERFEEDBACK_T (ANSWERID);

-- MediaData.hbm.xml
CREATE INDEX SAM_MEDIA_ITEMGRADING_I ON SAM_MEDIA_T (ITEMGRADINGID);

commit;