drop table SAM_ANSWERFEEDBACK_T cascade constraints;
drop table SAM_ANSWER_T cascade constraints;
drop table SAM_ASSESSACCESSCONTROL_T cascade constraints;
drop table SAM_ASSESSEVALUATION_T cascade constraints;
drop table SAM_ASSESSFEEDBACK_T cascade constraints;
drop table SAM_ASSESSMENTBASE_T cascade constraints;
drop table SAM_ASSESSMENTGRADING_T cascade constraints;
drop table SAM_ASSESSMETADATA_T cascade constraints;
drop table SAM_AUTHZDATA_T cascade constraints;
drop table SAM_FUNCTIONDATA_T cascade constraints;
drop table SAM_GRADINGSUMMARY_T cascade constraints;
drop table SAM_ITEMFEEDBACK_T cascade constraints;
drop table SAM_ITEMGRADING_T cascade constraints;
drop table SAM_ITEMMETADATA_T cascade constraints;
drop table SAM_ITEMTEXT_T cascade constraints;
drop table SAM_ITEM_T cascade constraints;
drop table SAM_MEDIA_T cascade constraints;
drop table SAM_PUBLISHEDACCESSCONTROL_T cascade constraints;
drop table SAM_PUBLISHEDANSWERFEEDBACK_T cascade constraints;
drop table SAM_PUBLISHEDANSWER_T cascade constraints;
drop table SAM_PUBLISHEDASSESSMENT_T cascade constraints;
drop table SAM_PUBLISHEDEVALUATION_T cascade constraints;
drop table SAM_PUBLISHEDFEEDBACK_T cascade constraints;
drop table SAM_PUBLISHEDITEMFEEDBACK_T cascade constraints;
drop table SAM_PUBLISHEDITEMMETADATA_T cascade constraints;
drop table SAM_PUBLISHEDITEMTEXT_T cascade constraints;
drop table SAM_PUBLISHEDITEM_T cascade constraints;
drop table SAM_PUBLISHEDMETADATA_T cascade constraints;
drop table SAM_PUBLISHEDSECTIONMETADATA_T cascade constraints;
drop table SAM_PUBLISHEDSECTION_T cascade constraints;
drop table SAM_PUBLISHEDSECUREDIP_T cascade constraints;
drop table SAM_QUALIFIERDATA_T cascade constraints;
drop table SAM_QUESTIONPOOLACCESS_T cascade constraints;
drop table SAM_QUESTIONPOOLITEM_T cascade constraints;
drop table SAM_QUESTIONPOOL_T cascade constraints;
drop table SAM_SECTIONMETADATA_T cascade constraints;
drop table SAM_SECTION_T cascade constraints;
drop table SAM_SECUREDIP_T cascade constraints;
drop table SAM_TYPE_T cascade constraints;
drop sequence SAM_ANSWERFEEDBACK_ID_S;
drop sequence SAM_ANSWER_ID_S;
drop sequence SAM_ASSESSMENTBASE_ID_S;
drop sequence SAM_ASSESSMENTGRADING_ID_S;
drop sequence SAM_ASSESSMETADATA_ID_S;
drop sequence SAM_AUTHZDATA_S;
drop sequence SAM_FUNCTIONID_S;
drop sequence SAM_GRADINGSUMMARY_ID_S;
drop sequence SAM_ITEMFEEDBACK_ID_S;
drop sequence SAM_ITEMGRADING_ID_S;
drop sequence SAM_ITEMMETADATA_ID_S;
drop sequence SAM_ITEMTEXT_ID_S;
drop sequence SAM_ITEM_ID_S;
drop sequence SAM_MEDIA_ID_S;
drop sequence SAM_PUBANSWERFEEDBACK_ID_S;
drop sequence SAM_PUBANSWER_ID_S;
drop sequence SAM_PUBITEMFEEDBACK_ID_S;
drop sequence SAM_PUBITEMMETADATA_ID_S;
drop sequence SAM_PUBITEMTEXT_ID_S;
drop sequence SAM_PUBITEM_ID_S;
drop sequence SAM_PUBLISHEDASSESSMENT_ID_S;
drop sequence SAM_PUBLISHEDMETADATA_ID_S;
drop sequence SAM_PUBLISHEDSECTION_ID_S;
drop sequence SAM_PUBLISHEDSECUREDIP_ID_S;
drop sequence SAM_QUESTIONPOOL_ID_S;
drop sequence SAM_SECTIONMETADATA_ID_S;
drop sequence SAM_SECTION_ID_S;
drop sequence SAM_SECUREDIP_ID_S;
drop sequence SAM_TYPE_ID_S;
create table SAM_ANSWERFEEDBACK_T (ANSWERFEEDBACKID number(19,0) not null, ANSWERID number(19,0) not null, TYPEID varchar(36), TEXT varchar(4000), primary key (ANSWERFEEDBACKID));
create table SAM_ANSWER_T (ANSWERID number(19,0) not null, ITEMTEXTID number(19,0) not null, ITEMID number(19,0) not null, TEXT varchar(4000), SEQUENCE integer not null, LABEL varchar(20), ISCORRECT varchar(1), GRADE varchar(80), SCORE float, primary key (ANSWERID));
create table SAM_ASSESSACCESSCONTROL_T (ASSESSMENTID number(19,0) not null, SUBMISSIONSALLOWED integer, UNLIMITEDSUBMISSIONS integer, SUBMISSIONSSAVED integer, ASSESSMENTFORMAT integer, BOOKMARKINGITEM integer, TIMELIMIT integer, TIMEDASSESSMENT integer, RETRYALLOWED integer, LATEHANDLING integer, STARTDATE timestamp, DUEDATE timestamp, SCOREDATE timestamp, FEEDBACKDATE timestamp, RETRACTDATE timestamp, AUTOSUBMIT integer, ITEMNAVIGATION integer, ITEMNUMBERING integer, DISPLAYSCORE integer, SUBMISSIONMESSAGE varchar(4000), RELEASETO varchar(255), USERNAME varchar(255), PASSWORD varchar(255), FINALPAGEURL varchar(1023), primary key (ASSESSMENTID));
create table SAM_ASSESSEVALUATION_T (ASSESSMENTID number(19,0) not null, EVALUATIONCOMPONENTS varchar(255), SCORINGTYPE integer, NUMERICMODELID varchar(255), FIXEDTOTALSCORE integer, GRADEAVAILABLE integer, ISSTUDENTIDPUBLIC integer, ANONYMOUSGRADING integer, AUTOSCORING integer, TOGRADEBOOK varchar(255), primary key (ASSESSMENTID));
create table SAM_ASSESSFEEDBACK_T (ASSESSMENTID number(19,0) not null, FEEDBACKDELIVERY integer, FEEDBACKAUTHORING integer, EDITCOMPONENTS integer, SHOWQUESTIONTEXT integer, SHOWSTUDENTRESPONSE integer, SHOWCORRECTRESPONSE integer, SHOWSTUDENTSCORE integer, SHOWSTUDENTQUESTIONSCORE integer, SHOWQUESTIONLEVELFEEDBACK integer, SHOWSELECTIONLEVELFEEDBACK integer, SHOWGRADERCOMMENTS integer, SHOWSTATISTICS integer, primary key (ASSESSMENTID));
create table SAM_ASSESSMENTBASE_T (ID number(19,0) not null, isTemplate varchar2(255 char) not null, PARENTID integer, TITLE varchar(255), DESCRIPTION varchar(4000), COMMENTS varchar(4000), TYPEID varchar(36), INSTRUCTORNOTIFICATION integer, TESTEENOTIFICATION integer, MULTIPARTALLOWED integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, ASSESSMENTTEMPLATEID number(19,0), primary key (ID));
create table SAM_ASSESSMENTGRADING_T (ASSESSMENTGRADINGID number(19,0) not null, PUBLISHEDASSESSMENTID integer not null, AGENTID varchar(36) not null, SUBMITTEDDATE timestamp, ISLATE varchar(1) not null, FORGRADE integer not null, TOTALAUTOSCORE float, TOTALOVERRIDESCORE float, FINALSCORE float, COMMENTS varchar(4000), GRADEDBY varchar(36), GRADEDDATE timestamp, STATUS integer not null, ATTEMPTDATE timestamp, TIMEELAPSED integer, primary key (ASSESSMENTGRADINGID));
create table SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID number(19,0) not null, ASSESSMENTID number(19,0) not null, LABEL varchar(255) not null, ENTRY varchar(255), primary key (ASSESSMENTMETADATAID));
create table SAM_AUTHZDATA_T (ID number(19,0) not null, lockId number(10,0) not null, AGENTID varchar2(36 char) not null, FUNCTIONID varchar2(36 char) not null, QUALIFIERID varchar2(36 char) not null, EFFECTIVEDATE date, EXPIRATIONDATE date, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE date not null, ISEXPLICIT integer, primary key (ID), unique (AGENTID, FUNCTIONID, QUALIFIERID));
create table SAM_FUNCTIONDATA_T (FUNCTIONID number(19,0) not null, REFERENCENAME varchar(255) not null, DISPLAYNAME varchar(255), DESCRIPTION varchar(4000), FUNCTIONTYPEID varchar(4000), primary key (FUNCTIONID));
create table SAM_GRADINGSUMMARY_T (ASSESSMENTGRADINGSUMMARYID number(19,0) not null, PUBLISHEDASSESSMENTID number(19,0) not null, AGENTID varchar(36) not null, TOTALSUBMITTED integer, TOTALSUBMITTEDFORGRADE integer, LASTSUBMITTEDDATE timestamp, LASTSUBMITTEDASSESSMENTISLATE integer not null, SUMOF_AUTOSCOREFORGRADE float, AVERAGE_AUTOSCOREFORGRADE float, HIGHEST_AUTOSCOREFORGRADE float, LOWEST_AUTOSCOREFORGRADE float, LAST_AUTOSCOREFORGRADE float, SUMOF_OVERRIDESCOREFORGRADE float, AVERAGE_OVERRIDESCOREFORGRADE float, HIGHEST_OVERRIDESCOREFORGRADE float, LOWEST_OVERRIDESCOREFORGRADE float, LAST_OVERRIDESCOREFORGRADE float, SCORINGTYPE integer, ACCEPTEDASSESSMENTISLATE integer, FINALASSESSMENTSCORE float, FEEDTOGRADEBOOK integer, primary key (ASSESSMENTGRADINGSUMMARYID));
create table SAM_ITEMFEEDBACK_T (ITEMFEEDBACKID number(19,0) not null, ITEMID number(19,0) not null, TYPEID varchar(36) not null, TEXT varchar(4000), primary key (ITEMFEEDBACKID));
create table SAM_ITEMGRADING_T (ITEMGRADINGID number(19,0) not null, ASSESSMENTGRADINGID number(19,0) not null, PUBLISHEDITEMID integer not null, PUBLISHEDITEMTEXTID integer not null, AGENTID varchar(36) not null, SUBMITTEDDATE timestamp not null, PUBLISHEDANSWERID integer, RATIONALE varchar(4000), ANSWERTEXT varchar(4000), AUTOSCORE float, OVERRIDESCORE float, COMMENTS varchar(4000), GRADEDBY varchar(36), GRADEDDATE timestamp, REVIEW integer, ATTEMPTSREMAINING integer, LASTDURATION varchar(36), primary key (ITEMGRADINGID));
create table SAM_ITEMMETADATA_T (ITEMMETADATAID number(19,0) not null, ITEMID number(19,0) not null, LABEL varchar(255) not null, ENTRY varchar(255), primary key (ITEMMETADATAID));
create table SAM_ITEMTEXT_T (ITEMTEXTID number(19,0) not null, ITEMID number(19,0) not null, SEQUENCE integer not null, TEXT varchar(4000), primary key (ITEMTEXTID));
create table SAM_ITEM_T (ITEMID number(19,0) not null, SECTIONID number(19,0), ITEMIDSTRING varchar(36), SEQUENCE integer, DURATION integer, TRIESALLOWED integer, INSTRUCTION varchar(4000), DESCRIPTION varchar(4000), TYPEID varchar(36) not null, GRADE varchar(80), SCORE float, HINT varchar(4000), HASRATIONALE varchar(1), STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, primary key (ITEMID));
create table SAM_MEDIA_T (MEDIAID number(19,0) not null, ITEMGRADINGID number(19,0), MEDIA long raw, FILESIZE integer, MIMETYPE varchar(80), DESCRIPTION varchar(4000), LOCATION varchar(255), FILENAME varchar(255), ISLINK integer, ISHTMLINLINE integer, STATUS integer, CREATEDBY varchar(36), CREATEDDATE timestamp, LASTMODIFIEDBY varchar(36), LASTMODIFIEDDATE timestamp, DURATION varchar(36), primary key (MEDIAID));
create table SAM_PUBLISHEDACCESSCONTROL_T (ASSESSMENTID number(19,0) not null, UNLIMITEDSUBMISSIONS integer, SUBMISSIONSALLOWED integer, SUBMISSIONSSAVED integer, ASSESSMENTFORMAT integer, BOOKMARKINGITEM integer, TIMELIMIT integer, TIMEDASSESSMENT integer, RETRYALLOWED integer, LATEHANDLING integer, STARTDATE timestamp, DUEDATE timestamp, SCOREDATE timestamp, FEEDBACKDATE timestamp, RETRACTDATE timestamp, AUTOSUBMIT integer, ITEMNAVIGATION integer, ITEMNUMBERING integer, SUBMISSIONMESSAGE varchar(4000), RELEASETO varchar(255), USERNAME varchar(255), PASSWORD varchar(255), FINALPAGEURL varchar(1023), primary key (ASSESSMENTID));
create table SAM_PUBLISHEDANSWERFEEDBACK_T (ANSWERFEEDBACKID number(19,0) not null, ANSWERID number(19,0) not null, TYPEID varchar(36), TEXT varchar(4000), primary key (ANSWERFEEDBACKID));
create table SAM_PUBLISHEDANSWER_T (ANSWERID number(19,0) not null, ITEMTEXTID number(19,0) not null, ITEMID number(19,0) not null, TEXT varchar(4000), SEQUENCE integer not null, LABEL varchar(20), ISCORRECT varchar(1), GRADE varchar(80), SCORE float, primary key (ANSWERID));
create table SAM_PUBLISHEDASSESSMENT_T (ID number(19,0) not null, TITLE varchar(255), ASSESSMENTID integer, DESCRIPTION varchar(4000), COMMENTS varchar(255), TYPEID varchar(36), INSTRUCTORNOTIFICATION integer, TESTEENOTIFICATION integer, MULTIPARTALLOWED integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, primary key (ID));
create table SAM_PUBLISHEDEVALUATION_T (ASSESSMENTID number(19,0) not null, EVALUATIONCOMPONENTS varchar(255), SCORINGTYPE integer, NUMERICMODELID varchar(255), FIXEDTOTALSCORE integer, GRADEAVAILABLE integer, ISSTUDENTIDPUBLIC integer, ANONYMOUSGRADING integer, AUTOSCORING integer, TOGRADEBOOK integer, primary key (ASSESSMENTID));
create table SAM_PUBLISHEDFEEDBACK_T (ASSESSMENTID number(19,0) not null, FEEDBACKDELIVERY integer, FEEDBACKAUTHORING integer, EDITCOMPONENTS integer, SHOWQUESTIONTEXT integer, SHOWSTUDENTRESPONSE integer, SHOWCORRECTRESPONSE integer, SHOWSTUDENTSCORE integer, SHOWSTUDENTQUESTIONSCORE integer, SHOWQUESTIONLEVELFEEDBACK integer, SHOWSELECTIONLEVELFEEDBACK integer, SHOWGRADERCOMMENTS integer, SHOWSTATISTICS integer, primary key (ASSESSMENTID));
create table SAM_PUBLISHEDITEMFEEDBACK_T (ITEMFEEDBACKID number(19,0) not null, ITEMID number(19,0) not null, TYPEID varchar(36) not null, TEXT varchar(4000), primary key (ITEMFEEDBACKID));
create table SAM_PUBLISHEDITEMMETADATA_T (ITEMMETADATAID number(19,0) not null, ITEMID number(19,0) not null, LABEL varchar(255) not null, ENTRY varchar(255), primary key (ITEMMETADATAID));
create table SAM_PUBLISHEDITEMTEXT_T (ITEMTEXTID number(19,0) not null, ITEMID number(19,0) not null, SEQUENCE integer not null, TEXT varchar(4000), primary key (ITEMTEXTID));
create table SAM_PUBLISHEDITEM_T (ITEMID number(19,0) not null, SECTIONID number(19,0) not null, ITEMIDSTRING varchar(36), SEQUENCE integer, DURATION integer, TRIESALLOWED integer, INSTRUCTION varchar(4000), DESCRIPTION varchar(4000), TYPEID varchar(36) not null, GRADE varchar(80), SCORE float, HINT varchar(4000), HASRATIONALE varchar(1), STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, primary key (ITEMID));
create table SAM_PUBLISHEDMETADATA_T (ASSESSMENTMETADATAID number(19,0) not null, ASSESSMENTID number(19,0) not null, LABEL varchar(255) not null, ENTRY varchar(255), primary key (ASSESSMENTMETADATAID));
create table SAM_PUBLISHEDSECTIONMETADATA_T (PUBLISHEDSECTIONMETADATAID number(19,0) not null, SECTIONID number(19,0) not null, LABEL varchar(255) not null, ENTRY varchar(255), primary key (PUBLISHEDSECTIONMETADATAID));
create table SAM_PUBLISHEDSECTION_T (SECTIONID number(19,0) not null, ASSESSMENTID number(19,0) not null, DURATION integer, SEQUENCE integer, TITLE varchar(255), DESCRIPTION varchar(4000), TYPEID integer not null, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, primary key (SECTIONID));
create table SAM_PUBLISHEDSECUREDIP_T (IPADDRESSID number(19,0) not null, ASSESSMENTID number(19,0) not null, HOSTNAME varchar(255), IPADDRESS varchar(255), primary key (IPADDRESSID));
create table SAM_QUALIFIERDATA_T (QUALIFIERID number(19,0) not null, REFERENCENAME varchar(255) not null, DISPLAYNAME varchar(255), DESCRIPTION varchar(4000), QUALIFIERTYPEID varchar(4000), primary key (QUALIFIERID));
create table SAM_QUESTIONPOOLACCESS_T (QUESTIONPOOLID number(19,0) not null, AGENTID varchar2(255 char) not null, ACCESSTYPEID number(19,0) not null, primary key (QUESTIONPOOLID, AGENTID, ACCESSTYPEID));
create table SAM_QUESTIONPOOLITEM_T (QUESTIONPOOLID number(19,0) not null, ITEMID varchar2(255 char) not null, primary key (QUESTIONPOOLID, ITEMID));
create table SAM_QUESTIONPOOL_T (QUESTIONPOOLID number(19,0) not null, TITLE varchar(255), DESCRIPTION varchar(255), PARENTPOOLID integer, OWNERID varchar(255), ORGANIZATIONNAME varchar(255), DATECREATED timestamp, LASTMODIFIEDDATE timestamp, LASTMODIFIEDBY varchar(255), DEFAULTACCESSTYPEID integer, OBJECTIVE varchar(255), KEYWORDS varchar(255), RUBRIC varchar(4000), TYPEID integer, INTELLECTUALPROPERTYID integer, primary key (QUESTIONPOOLID));
create table SAM_SECTIONMETADATA_T (SECTIONMETADATAID number(19,0) not null, SECTIONID number(19,0) not null, LABEL varchar(255) not null, ENTRY varchar(255), primary key (SECTIONMETADATAID));
create table SAM_SECTION_T (SECTIONID number(19,0) not null, ASSESSMENTID number(19,0) not null, DURATION integer, SEQUENCE integer, TITLE varchar(255), DESCRIPTION varchar(4000), TYPEID integer, STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, primary key (SECTIONID));
create table SAM_SECUREDIP_T (IPADDRESSID number(19,0) not null, ASSESSMENTID number(19,0) not null, HOSTNAME varchar(255), IPADDRESS varchar(255), primary key (IPADDRESSID));
create table SAM_TYPE_T (TYPEID number(19,0) not null, AUTHORITY varchar(255), DOMAIN varchar(255), KEYWORD varchar(255), DESCRIPTION varchar(4000), STATUS integer not null, CREATEDBY varchar(36) not null, CREATEDDATE timestamp not null, LASTMODIFIEDBY varchar(36) not null, LASTMODIFIEDDATE timestamp not null, primary key (TYPEID));
alter table SAM_ANSWERFEEDBACK_T add constraint FK58CEF0D8DEC85889 foreign key (ANSWERID) references SAM_ANSWER_T;
alter table SAM_ANSWER_T add constraint FKDD0580933288DBBD foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_ANSWER_T add constraint FKDD058093278A7DAD foreign key (ITEMTEXTID) references SAM_ITEMTEXT_T;
alter table SAM_ASSESSACCESSCONTROL_T add constraint FKC945448A694216CC foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_ASSESSEVALUATION_T add constraint FK6A6F29F5694216CC foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
alter table SAM_ASSESSFEEDBACK_T add constraint FK557D4CFE694216CC foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
create index SAM_PUBLISHEDASSESSMENT_I on SAM_ASSESSMENTGRADING_T (PUBLISHEDASSESSMENTID);
alter table SAM_ASSESSMETADATA_T add constraint FK7E6F9A28694216CC foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
create index sam_authz_functionId_idx on SAM_AUTHZDATA_T (FUNCTIONID);
create index sam_authz_qualifierId_idx on SAM_AUTHZDATA_T (QUALIFIERID);
alter table SAM_GRADINGSUMMARY_T add constraint FKBC88AA27D02EF633 foreign key (PUBLISHEDASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_ITEMFEEDBACK_T add constraint FK3254E9ED3288DBBD foreign key (ITEMID) references SAM_ITEM_T;
create index SAM_ITEMGRADING_PUBANS_I on SAM_ITEMGRADING_T (PUBLISHEDANSWERID);
create index SAM_ITEMGRADING_ITEM_I on SAM_ITEMGRADING_T (PUBLISHEDITEMID);
create index SAM_ITEMGRADING_ITEMTEXT_I on SAM_ITEMGRADING_T (PUBLISHEDITEMTEXTID);
create index SAM_ASSESSMENTGRADING_I on SAM_ITEMGRADING_T (ASSESSMENTGRADINGID);
alter table SAM_ITEMGRADING_T add constraint FKB68E675667B430D5 foreign key (ASSESSMENTGRADINGID) references SAM_ASSESSMENTGRADING_T;
alter table SAM_ITEMMETADATA_T add constraint FK5B4737173288DBBD foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_ITEMTEXT_T add constraint FK271D63153288DBBD foreign key (ITEMID) references SAM_ITEM_T;
alter table SAM_ITEM_T add constraint FK3AAC5EA870CE2BD foreign key (SECTIONID) references SAM_SECTION_T;
create index SAM_MEDIA_ITEMGRADING_I on SAM_MEDIA_T (ITEMGRADINGID);
alter table SAM_MEDIA_T add constraint FKD4CF5A194D7EA7B3 foreign key (ITEMGRADINGID) references SAM_ITEMGRADING_T;
alter table SAM_PUBLISHEDACCESSCONTROL_T add constraint FK2EDF39E09482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
create index SAM_PUBANSWERFB_ANSWER_I on SAM_PUBLISHEDANSWERFEEDBACK_T (ANSWERID);
alter table SAM_PUBLISHEDANSWERFEEDBACK_T add constraint FK6CB765A624D77573 foreign key (ANSWERID) references SAM_PUBLISHEDANSWER_T;
create index SAM_PUBANSWER_ITEM_I on SAM_PUBLISHEDANSWER_T (ITEMID);
create index SAM_PUBANSWER_ITEMTEXT_I on SAM_PUBLISHEDANSWER_T (ITEMTEXTID);
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA36131446627 foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_PUBLISHEDANSWER_T add constraint FKB41EA36126460817 foreign key (ITEMTEXTID) references SAM_PUBLISHEDITEMTEXT_T;
create index SAM_PUBA_ASSESSMENT_I on SAM_PUBLISHEDASSESSMENT_T (ASSESSMENTID);
alter table SAM_PUBLISHEDEVALUATION_T add constraint FK94CB245F9482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDFEEDBACK_T add constraint FK1488D9E89482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
create index SAM_PUBITEMFB_ITEM_I on SAM_PUBLISHEDITEMFEEDBACK_T (ITEMID);
alter table SAM_PUBLISHEDITEMFEEDBACK_T add constraint FKB7D03A3B31446627 foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_PUBLISHEDITEMMETADATA_T add constraint FKE0C2876531446627 foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
alter table SAM_PUBLISHEDITEMTEXT_T add constraint FK9C790A6331446627 foreign key (ITEMID) references SAM_PUBLISHEDITEM_T;
create index SAM_PUBITEM_SECTION_I on SAM_PUBLISHEDITEM_T (SECTIONID);
alter table SAM_PUBLISHEDITEM_T add constraint FK53ABDCF6895D4813 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T;
alter table SAM_PUBLISHEDMETADATA_T add constraint FK3D7B27129482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDSECTIONMETADATA_T add constraint FKDF50FC3B895D4813 foreign key (SECTIONID) references SAM_PUBLISHEDSECTION_T;
alter table SAM_PUBLISHEDSECTION_T add constraint FK424F87CC9482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_PUBLISHEDSECUREDIP_T add constraint FK1EDEA25B9482C945 foreign key (ASSESSMENTID) references SAM_PUBLISHEDASSESSMENT_T;
alter table SAM_QUESTIONPOOLITEM_T add constraint FKF0FAAE2A39ED26BB foreign key (QUESTIONPOOLID) references SAM_QUESTIONPOOL_T;
alter table SAM_SECTIONMETADATA_T add constraint FK762AD74970CE2BD foreign key (SECTIONID) references SAM_SECTION_T;

alter table SAM_SECTION_T add constraint FK364450DACAC2365B foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;

alter table SAM_SECUREDIP_T add constraint FKE8C55FE9694216CC foreign key (ASSESSMENTID) references SAM_ASSESSMENTBASE_T;
create sequence SAM_ANSWERFEEDBACK_ID_S;
create sequence SAM_ANSWER_ID_S;
create sequence SAM_ASSESSMENTBASE_ID_S;
create sequence SAM_ASSESSMENTGRADING_ID_S;
create sequence SAM_ASSESSMETADATA_ID_S;
create sequence SAM_AUTHZDATA_S;
create sequence SAM_FUNCTIONID_S;
create sequence SAM_GRADINGSUMMARY_ID_S;
create sequence SAM_ITEMFEEDBACK_ID_S;
create sequence SAM_ITEMGRADING_ID_S;
create sequence SAM_ITEMMETADATA_ID_S;
create sequence SAM_ITEMTEXT_ID_S;
create sequence SAM_ITEM_ID_S;
create sequence SAM_MEDIA_ID_S;
create sequence SAM_PUBANSWERFEEDBACK_ID_S;
create sequence SAM_PUBANSWER_ID_S;
create sequence SAM_PUBITEMFEEDBACK_ID_S;
create sequence SAM_PUBITEMMETADATA_ID_S;
create sequence SAM_PUBITEMTEXT_ID_S;
create sequence SAM_PUBITEM_ID_S;
create sequence SAM_PUBLISHEDASSESSMENT_ID_S;
create sequence SAM_PUBLISHEDMETADATA_ID_S;
create sequence SAM_PUBLISHEDSECTION_ID_S;
create sequence SAM_PUBLISHEDSECUREDIP_ID_S;
create sequence SAM_QUESTIONPOOL_ID_S;
create sequence SAM_SECTIONMETADATA_ID_S;
create sequence SAM_SECTION_ID_S;
create sequence SAM_SECUREDIP_ID_S;
create sequence SAM_TYPE_ID_S;
