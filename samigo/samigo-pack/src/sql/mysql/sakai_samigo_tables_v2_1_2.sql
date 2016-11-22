alter table SAM_ANSWER_T drop foreign key FKDD0580938152036E;
alter table SAM_ANSWER_T drop foreign key FKDD058093CBA347DB;
alter table SAM_PUBLISHEDASSESSMENT_T drop foreign key FKB2E48A65C07F835D;
alter table SAM_ASSESSMENTGRADING_T drop foreign key FKDAED4C879E4AF02B;
alter table SAM_ITEMGRADING_T drop foreign key FKB68E6756C42AA2BC;
alter table SAM_ITEMGRADING_T drop foreign key FKB68E6756E5D3D24D;
alter table SAM_ITEMGRADING_T drop foreign key FKB68E6756A75F9029;
alter table SAM_ITEMGRADING_T drop foreign key FKB68E6756D4927;
alter table SAM_PUBLISHEDMETADATA_T drop foreign key FK3D7B2712C07F835D;
alter table SAM_PUBLISHEDSECTIONMETADATA_T drop foreign key FKDF50FC3B7DA376A0;
alter table SAM_SECTION_T drop foreign key FK364450DAC07F835D;
alter table SAM_PUBLISHEDITEMFEEDBACK_T drop foreign key FKB7D03A3B8152036E;
alter table SAM_ITEMFEEDBACK_T drop foreign key FK3254E9ED8152036E;
alter table SAM_ITEMMETADATA_T drop foreign key FK5B4737178152036E;
alter table SAM_PUBLISHEDFEEDBACK_T drop foreign key FK1488D9E8C07F835D;
alter table SAM_GRADINGSUMMARY_T drop foreign key FKBC88AA279E4AF02B;
alter table SAM_PUBLISHEDEVALUATION_T drop foreign key FK94CB245FC07F835D;
alter table SAM_PUBLISHEDACCESSCONTROL_T drop foreign key FK2EDF39E0C07F835D;
alter table SAM_ASSESSEVALUATION_T drop foreign key FK6A6F29F5C07F835D;
alter table SAM_ANSWERFEEDBACK_T drop foreign key FK58CEF0D810DF4559;
alter table SAM_PUBLISHEDANSWER_T drop foreign key FKB41EA361B9BF0B8E;
alter table SAM_PUBLISHEDANSWER_T drop foreign key FKB41EA361CBA347DB;
alter table SAM_PUBLISHEDANSWERFEEDBACK_T drop foreign key FK6CB765A610DF4559;
alter table SAM_PUBLISHEDITEM_T drop foreign key FK53ABDCF67DA376A0;
alter table SAM_ASSESSACCESSCONTROL_T drop foreign key FKC945448AC07F835D;
alter table SAM_PUBLISHEDSECTION_T drop foreign key FK424F87CCC07F835D;
alter table SAM_PUBLISHEDITEMTEXT_T drop foreign key FK9C790A638152036E;
alter table SAM_PUBLISHEDSECUREDIP_T drop foreign key FK1EDEA25BC07F835D;
alter table SAM_SECTIONMETADATA_T drop foreign key FK762AD7497DA376A0;
alter table SAM_ITEM_T drop foreign key FK3AAC5EA87DA376A0;
alter table SAM_ASSESSFEEDBACK_T drop foreign key FK557D4CFEC07F835D;
alter table SAM_SECUREDIP_T drop foreign key FKE8C55FE9C07F835D;
alter table SAM_ITEMTEXT_T drop foreign key FK271D63158152036E;
alter table SAM_PUBLISHEDITEMMETADATA_T drop foreign key FKE0C287658152036E;
alter table SAM_MEDIA_T drop foreign key FKD4CF5A1971254D1C;
alter table SAM_ASSESSMETADATA_T drop foreign key FK7E6F9A28C07F835D;
drop table if exists SAM_ANSWER_T;
drop table if exists SAM_PUBLISHEDASSESSMENT_T;
drop table if exists SAM_ASSESSMENTGRADING_T;
drop table if exists SAM_FUNCTIONDATA_T;
drop table if exists SAM_ITEMGRADING_T;
drop table if exists SAM_PUBLISHEDMETADATA_T;
drop table if exists SAM_PUBLISHEDSECTIONMETADATA_T;
drop table if exists SAM_SECTION_T;
drop table if exists SAM_QUESTIONPOOLITEM_T;
drop table if exists SAM_PUBLISHEDITEMFEEDBACK_T;
drop table if exists SAM_ITEMFEEDBACK_T;
drop table if exists SAM_ITEMMETADATA_T;
drop table if exists SAM_PUBLISHEDFEEDBACK_T;
drop table if exists SAM_GRADINGSUMMARY_T;
drop table if exists SAM_PUBLISHEDEVALUATION_T;
drop table if exists SAM_PUBLISHEDACCESSCONTROL_T;
drop table if exists SAM_QUALIFIERDATA_T;
drop table if exists SAM_QUESTIONPOOLACCESS_T;
drop table if exists SAM_AUTHZDATA_T;
drop table if exists SAM_ASSESSEVALUATION_T;
drop table if exists SAM_ANSWERFEEDBACK_T;
drop table if exists SAM_PUBLISHEDANSWER_T;
drop table if exists SAM_PUBLISHEDANSWERFEEDBACK_T;
drop table if exists SAM_PUBLISHEDITEM_T;
drop table if exists SAM_ASSESSACCESSCONTROL_T;
drop table if exists SAM_PUBLISHEDSECTION_T;
drop table if exists SAM_PUBLISHEDITEMTEXT_T;
drop table if exists SAM_PUBLISHEDSECUREDIP_T;
drop table if exists SAM_QUESTIONPOOL_T;
drop table if exists SAM_SECTIONMETADATA_T;
drop table if exists SAM_ITEM_T;
drop table if exists SAM_ASSESSFEEDBACK_T;
drop table if exists SAM_SECUREDIP_T;
drop table if exists SAM_ITEMTEXT_T;
drop table if exists SAM_PUBLISHEDITEMMETADATA_T;
drop table if exists SAM_MEDIA_T;
drop table if exists SAM_ASSESSMENTBASE_T;
drop table if exists SAM_TYPE_T;
drop table if exists SAM_ASSESSMETADATA_T;
create table SAM_ANSWER_T (
   ANSWERID bigint not null auto_increment,
   ITEMTEXTID bigint not null,
   ITEMID bigint not null,
   TEXT text,
   SEQUENCE integer not null,
   LABEL varchar(20),
   ISCORRECT varchar(1),
   GRADE varchar(80),
   SCORE float,
   primary key (ANSWERID)
);
create table SAM_PUBLISHEDASSESSMENT_T (
   ID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   TITLE varchar(255),
   DESCRIPTION text,
   COMMENTS varchar(255),
   TYPEID varchar(36),
   INSTRUCTORNOTIFICATION integer,
   TESTEENOTIFICATION integer,
   MULTIPARTALLOWED integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   primary key (ID)
);
create table SAM_ASSESSMENTGRADING_T (
   ASSESSMENTGRADINGID bigint not null auto_increment,
   PUBLISHEDASSESSMENTID bigint not null,
   AGENTID varchar(36) not null,
   SUBMITTEDDATE datetime,
   ISLATE varchar(1) not null,
   FORGRADE integer not null,
   TOTALAUTOSCORE float,
   TOTALOVERRIDESCORE float,
   FINALSCORE float,
   COMMENTS text,
   GRADEDBY varchar(36),
   GRADEDDATE datetime,
   STATUS integer not null,
   ATTEMPTDATE datetime,
   TIMEELAPSED integer,
   primary key (ASSESSMENTGRADINGID)
);
create table SAM_FUNCTIONDATA_T (
   FUNCTIONID bigint not null auto_increment,
   REFERENCENAME varchar(255) not null,
   DISPLAYNAME varchar(255),
   DESCRIPTION text,
   FUNCTIONTYPEID text,
   primary key (FUNCTIONID)
);
create table SAM_ITEMGRADING_T (
   ITEMGRADINGID bigint not null auto_increment,
   ASSESSMENTGRADINGID bigint not null,
   PUBLISHEDITEMID bigint not null,
   PUBLISHEDITEMTEXTID bigint not null,
   AGENTID varchar(36) not null,
   SUBMITTEDDATE datetime not null,
   PUBLISHEDANSWERID bigint,
   RATIONALE text,
   ANSWERTEXT text,
   AUTOSCORE float,
   OVERRIDESCORE float,
   COMMENTS text,
   GRADEDBY varchar(36),
   GRADEDDATE datetime,
   REVIEW integer,
   primary key (ITEMGRADINGID)
);
create table SAM_PUBLISHEDMETADATA_T (
   ASSESSMENTMETADATAID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ASSESSMENTMETADATAID)
);
create table SAM_PUBLISHEDSECTIONMETADATA_T (
   PUBLISHEDSECTIONMETADATAID bigint not null auto_increment,
   SECTIONID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (PUBLISHEDSECTIONMETADATAID)
);
create table SAM_SECTION_T (
   SECTIONID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   DURATION integer,
   SEQUENCE integer,
   TITLE varchar(255),
   DESCRIPTION text,
   TYPEID integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   primary key (SECTIONID)
);
create table SAM_QUESTIONPOOLITEM_T (
   QUESTIONPOOLID bigint not null,
   ITEMID varchar(255) not null,
   primary key (QUESTIONPOOLID, ITEMID)
);
create table SAM_PUBLISHEDITEMFEEDBACK_T (
   ITEMFEEDBACKID bigint not null auto_increment,
   ITEMID bigint not null,
   TYPEID varchar(36) not null,
   TEXT text,
   primary key (ITEMFEEDBACKID)
);
create table SAM_ITEMFEEDBACK_T (
   ITEMFEEDBACKID bigint not null auto_increment,
   ITEMID bigint not null,
   TYPEID varchar(36) not null,
   TEXT text,
   primary key (ITEMFEEDBACKID)
);
create table SAM_ITEMMETADATA_T (
   ITEMMETADATAID bigint not null auto_increment,
   ITEMID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ITEMMETADATAID)
);
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
);
create table SAM_GRADINGSUMMARY_T (
   ASSESSMENTGRADINGSUMMARYID bigint not null auto_increment,
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
   FEEDTOGRADEBOOK integer,
   primary key (ASSESSMENTGRADINGSUMMARYID)
);
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
);
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
   SUBMISSIONMESSAGE text,
   RELEASETO varchar(255),
   USERNAME varchar(255),
   PASSWORD varchar(255),
   FINALPAGEURL text,
   primary key (ASSESSMENTID)
);
create table SAM_QUALIFIERDATA_T (
   QUALIFIERID bigint not null,
   REFERENCENAME varchar(255) not null,
   DISPLAYNAME varchar(255),
   DESCRIPTION text,
   QUALIFIERTYPEID text,
   primary key (QUALIFIERID)
);
create table SAM_QUESTIONPOOLACCESS_T (
   QUESTIONPOOLID bigint not null,
   AGENTID varchar(255) not null,
   ACCESSTYPEID bigint not null,
   primary key (QUESTIONPOOLID, AGENTID, ACCESSTYPEID)
);
create table SAM_AUTHZDATA_T (
   ID bigint not null auto_increment,
   lockId integer not null,
   AGENTID varchar(36) not null,
   FUNCTIONID varchar(36) not null,
   QUALIFIERID varchar(36) not null,
   EFFECTIVEDATE datetime,
   EXPIRATIONDATE datetime,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   ISEXPLICIT integer,
   primary key (ID),
   unique (AGENTID, FUNCTIONID, QUALIFIERID)
);
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
);
create table SAM_ANSWERFEEDBACK_T (
   ANSWERFEEDBACKID bigint not null auto_increment,
   ANSWERID bigint not null,
   TYPEID varchar(36),
   TEXT text,
   primary key (ANSWERFEEDBACKID)
);
create table SAM_PUBLISHEDANSWER_T (
   ANSWERID bigint not null auto_increment,
   ITEMTEXTID bigint not null,
   itemId bigint not null,
   TEXT text,
   SEQUENCE integer not null,
   LABEL varchar(20),
   ISCORRECT varchar(1),
   GRADE varchar(80),
   SCORE float,
   primary key (ANSWERID)
);
create table SAM_PUBLISHEDANSWERFEEDBACK_T (
   ANSWERFEEDBACKID bigint not null auto_increment,
   ANSWERID bigint not null,
   TYPEID varchar(36),
   TEXT text,
   primary key (ANSWERFEEDBACKID)
);
create table SAM_PUBLISHEDITEM_T (
   ITEMID bigint not null auto_increment,
   SECTIONID bigint not null,
   ITEMIDSTRING varchar(36),
   SEQUENCE integer,
   DURATION integer,
   TRIESALLOWED integer,
   INSTRUCTION text,
   DESCRIPTION text,
   TYPEID varchar(36) not null,
   GRADE varchar(80),
   SCORE float,
   HINT text,
   HASRATIONALE varchar(1),
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   primary key (ITEMID)
);
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
   SUBMISSIONMESSAGE text,
   RELEASETO varchar(255),
   USERNAME varchar(255),
   PASSWORD varchar(255),
   FINALPAGEURL text,
   primary key (ASSESSMENTID)
);
create table SAM_PUBLISHEDSECTION_T (
   SECTIONID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   DURATION integer,
   SEQUENCE integer,
   TITLE varchar(255),
   DESCRIPTION text,
   TYPEID integer not null,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   primary key (SECTIONID)
);
create table SAM_PUBLISHEDITEMTEXT_T (
   ITEMTEXTID bigint not null auto_increment,
   ITEMID bigint not null,
   SEQUENCE integer not null,
   TEXT text,
   primary key (ITEMTEXTID)
);
create table SAM_PUBLISHEDSECUREDIP_T (
   IPADDRESSID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   HOSTNAME varchar(255),
   IPADDRESS varchar(255),
   primary key (IPADDRESSID)
);
create table SAM_QUESTIONPOOL_T (
   QUESTIONPOOLID bigint not null auto_increment,
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
   RUBRIC text,
   TYPEID integer,
   INTELLECTUALPROPERTYID integer,
   primary key (QUESTIONPOOLID)
);
create table SAM_SECTIONMETADATA_T (
   SECTIONMETADATAID bigint not null auto_increment,
   SECTIONID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (SECTIONMETADATAID)
);
create table SAM_ITEM_T (
   ITEMID bigint not null auto_increment,
   SECTIONID bigint,
   ITEMIDSTRING varchar(36),
   SEQUENCE integer,
   DURATION integer,
   TRIESALLOWED integer,
   INSTRUCTION text,
   DESCRIPTION text,
   TYPEID varchar(36) not null,
   GRADE varchar(80),
   SCORE float,
   HINT text,
   HASRATIONALE varchar(1),
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   primary key (ITEMID)
);
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
);
create table SAM_SECUREDIP_T (
   IPADDRESSID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   HOSTNAME varchar(255),
   IPADDRESS varchar(255),
   primary key (IPADDRESSID)
);
create table SAM_ITEMTEXT_T (
   ITEMTEXTID bigint not null auto_increment,
   ITEMID bigint not null,
   SEQUENCE integer not null,
   TEXT text,
   primary key (ITEMTEXTID)
);
create table SAM_PUBLISHEDITEMMETADATA_T (
   ITEMMETADATAID bigint not null auto_increment,
   ITEMID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ITEMMETADATAID)
);
create table SAM_MEDIA_T (
   MEDIAID bigint not null auto_increment,
   ITEMGRADINGID bigint,
   MEDIA longblob,
   FILESIZE integer,
   MIMETYPE varchar(80),
   DESCRIPTION text,
   LOCATION varchar(255),
   FILENAME varchar(255),
   ISLINK integer,
   ISHTMLINLINE integer,
   STATUS integer,
   CREATEDBY varchar(36),
   CREATEDDATE datetime,
   LASTMODIFIEDBY varchar(36),
   LASTMODIFIEDDATE datetime,
   primary key (MEDIAID)
);
create table SAM_ASSESSMENTBASE_T (
   ID bigint not null auto_increment,
   isTemplate varchar(255) not null,
   PARENTID integer,
   TITLE varchar(255),
   DESCRIPTION text,
   COMMENTS text,
   TYPEID varchar(36),
   INSTRUCTORNOTIFICATION integer,
   TESTEENOTIFICATION integer,
   MULTIPARTALLOWED integer,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   ASSESSMENTTEMPLATEID bigint,
   primary key (ID)
);
create table SAM_TYPE_T (
   TYPEID bigint not null auto_increment,
   AUTHORITY varchar(255),
   DOMAIN varchar(255),
   KEYWORD varchar(255),
   DESCRIPTION text,
   STATUS integer not null,
   CREATEDBY varchar(36) not null,
   CREATEDDATE datetime not null,
   LASTMODIFIEDBY varchar(36) not null,
   LASTMODIFIEDDATE datetime not null,
   primary key (TYPEID)
);
create table SAM_ASSESSMETADATA_T (
   ASSESSMENTMETADATAID bigint not null auto_increment,
   ASSESSMENTID bigint not null,
   LABEL varchar(255) not null,
   ENTRY varchar(255),
   primary key (ASSESSMENTMETADATAID)
);
alter table SAM_ANSWER_T add index FKDD0580938152036E (ITEMID), add constraint FKDD0580938152036E foreign key (ITEMID) references SAM_ITEM_T (ITEMID);
alter table SAM_ANSWER_T add index FKDD058093CBA347DB (ITEMTEXTID), add constraint FKDD058093CBA347DB foreign key (ITEMTEXTID) references SAM_ITEMTEXT_T (ITEMTEXTID);
alter table SAM_PUBLISHEDASSESSMENT_T add index FKB2E48A65C07F835D (ASSESSMENTID), add constraint FKB2E48A65C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_ASSESSMENTGRADING_T add index FKDAED4C879E4AF02B (PUBLISHEDASSESSMENTID), add constraint FKDAED4C879E4AF02B foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_ITEMGRADING_T add index FKB68E6756C42AA2BC (PUBLISHEDITEMID), add constraint FKB68E6756C42AA2BC foreign key (PUBLISHEDITEMID) references SAM_PUBLISHEDITEM_T (ITEMID);
alter table SAM_ITEMGRADING_T add index FKB68E6756E5D3D24D (ASSESSMENTGRADINGID), add constraint FKB68E6756E5D3D24D foreign key (ASSESSMENTGRADINGID) references SAM_ASSESSMENTGRADING_T (ASSESSMENTGRADINGID);
alter table SAM_ITEMGRADING_T add index FKB68E6756A75F9029 (PUBLISHEDITEMTEXTID), add constraint FKB68E6756A75F9029 foreign key (PUBLISHEDITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T (ITEMTEXTID);
alter table SAM_ITEMGRADING_T add index FKB68E6756D4927 (PUBLISHEDANSWERID), add constraint FKB68E6756D4927 foreign key (PUBLISHEDANSWERID) references SAM_PUBLISHEDANSWER_T (ANSWERID);
alter table SAM_PUBLISHEDMETADATA_T add index FK3D7B2712C07F835D (ASSESSMENTID), add constraint FK3D7B2712C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_PUBLISHEDSECTIONMETADATA_T add index FKDF50FC3B7DA376A0 (SECTIONID), add constraint FKDF50FC3B7DA376A0 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T (SECTIONID);
alter table SAM_SECTION_T add index FK364450DAC07F835D (ASSESSMENTID), add constraint FK364450DAC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_PUBLISHEDITEMFEEDBACK_T add index FKB7D03A3B8152036E (ITEMID), add constraint FKB7D03A3B8152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T (ITEMID);
alter table SAM_ITEMFEEDBACK_T add index FK3254E9ED8152036E (ITEMID), add constraint FK3254E9ED8152036E foreign key (ITEMID) references SAM_ITEM_T (ITEMID);
alter table SAM_ITEMMETADATA_T add index FK5B4737178152036E (ITEMID), add constraint FK5B4737178152036E foreign key (ITEMID) references SAM_ITEM_T (ITEMID);
alter table SAM_PUBLISHEDFEEDBACK_T add index FK1488D9E8C07F835D (ASSESSMENTID), add constraint FK1488D9E8C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_GRADINGSUMMARY_T add index FKBC88AA279E4AF02B (PUBLISHEDASSESSMENTID), add constraint FKBC88AA279E4AF02B foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_PUBLISHEDEVALUATION_T add index FK94CB245FC07F835D (ASSESSMENTID), add constraint FK94CB245FC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_PUBLISHEDACCESSCONTROL_T add index FK2EDF39E0C07F835D (ASSESSMENTID), add constraint FK2EDF39E0C07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
create index sam_authz_functionId_idx on SAM_AUTHZDATA_T (FUNCTIONID);
create index sam_authz_qualifierId_idx on SAM_AUTHZDATA_T (QUALIFIERID);
alter table SAM_ASSESSEVALUATION_T add index FK6A6F29F5C07F835D (ASSESSMENTID), add constraint FK6A6F29F5C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_ANSWERFEEDBACK_T add index FK58CEF0D810DF4559 (ANSWERID), add constraint FK58CEF0D810DF4559 foreign key (ANSWERID) references SAM_ANSWER_T (ANSWERID);
alter table SAM_PUBLISHEDANSWER_T add index FKB41EA361B9BF0B8E (itemId), add constraint FKB41EA361B9BF0B8E foreign key (itemId) references SAM_PUBLISHEDITEM_T (ITEMID);
alter table SAM_PUBLISHEDANSWER_T add index FKB41EA361CBA347DB (ITEMTEXTID), add constraint FKB41EA361CBA347DB foreign key (ITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T (ITEMTEXTID);
alter table SAM_PUBLISHEDANSWERFEEDBACK_T add index FK6CB765A610DF4559 (ANSWERID), add constraint FK6CB765A610DF4559 foreign key (ANSWERID) references SAM_PUBLISHEDANSWER_T (ANSWERID);
alter table SAM_PUBLISHEDITEM_T add index FK53ABDCF67DA376A0 (SECTIONID), add constraint FK53ABDCF67DA376A0 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T (SECTIONID);
alter table SAM_ASSESSACCESSCONTROL_T add index FKC945448AC07F835D (ASSESSMENTID), add constraint FKC945448AC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_PUBLISHEDSECTION_T add index FK424F87CCC07F835D (ASSESSMENTID), add constraint FK424F87CCC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_PUBLISHEDITEMTEXT_T add index FK9C790A638152036E (ITEMID), add constraint FK9C790A638152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T (ITEMID);
alter table SAM_PUBLISHEDSECUREDIP_T add index FK1EDEA25BC07F835D (ASSESSMENTID), add constraint FK1EDEA25BC07F835D foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T (ID);
alter table SAM_SECTIONMETADATA_T add index FK762AD7497DA376A0 (SECTIONID), add constraint FK762AD7497DA376A0 foreign key (SECTIONID) references SAM_SECTION_T (SECTIONID);
alter table SAM_ITEM_T add index FK3AAC5EA87DA376A0 (SECTIONID), add constraint FK3AAC5EA87DA376A0 foreign key (SECTIONID) references SAM_SECTION_T (SECTIONID);
alter table SAM_ASSESSFEEDBACK_T add index FK557D4CFEC07F835D (ASSESSMENTID), add constraint FK557D4CFEC07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_SECUREDIP_T add index FKE8C55FE9C07F835D (ASSESSMENTID), add constraint FKE8C55FE9C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);
alter table SAM_ITEMTEXT_T add index FK271D63158152036E (ITEMID), add constraint FK271D63158152036E foreign key (ITEMID) references SAM_ITEM_T (ITEMID);
alter table SAM_PUBLISHEDITEMMETADATA_T add index FKE0C287658152036E (ITEMID), add constraint FKE0C287658152036E foreign key (ITEMID) references SAM_PUBLISHEDITEM_T (ITEMID);
alter table SAM_MEDIA_T add index FKD4CF5A1971254D1C (ITEMGRADINGID), add constraint FKD4CF5A1971254D1C foreign key (ITEMGRADINGID) references SAM_ITEMGRADING_T (ITEMGRADINGID);
alter table SAM_ASSESSMETADATA_T add index FK7E6F9A28C07F835D (ASSESSMENTID), add constraint FK7E6F9A28C07F835D foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T (ID);

-- SAK-2461: improve performance
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

