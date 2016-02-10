insert into sam_questionPool_t ("QUESTIONPOOLID" ,
    "TITLE" ,"DESCRIPTION" ,"PARENTPOOLID" ,"ORGANIZATIONNAME" ,
    "DEFAULTACCESSTYPEID" ,"OBJECTIVE" ,"KEYWORDS" ,"RUBRIC" ,
    "TYPEID" ,"INTELLECTUALPROPERTYID" ,"OWNERID" ,
    "DATECREATED" ,"LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" ) 
select "QUESTIONPOOLID" ,
    "TITLE" ,"DESCRIPTION" ,"PARENTPOOLID" ,"ORGANIZATIONNAME" ,
    "DEFAULTACCESSTYPEID" ,"OBJECTIVE" ,"KEYWORDS" ,"RUBRIC" ,
    "TYPEID" ,"INTELLECTUALPROPERTYID" ,"OWNERID" ,
    "DATECREATED" ,"LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" from QUESTIONPOOLDATA;

insert into sam_questionPoolItem_t ("QUESTIONPOOLID" ,
    "ITEMID" ) 
select "QUESTIONPOOLID" ,
    "ITEMID" from QUESTIONPOOLITEMDATA ;

insert into sam_questionPoolAccess_t ("QUESTIONPOOLID" ,
    "AGENTID" ,"ACCESSTYPEID" ) 
select "QUESTIONPOOLID" ,
    "AGENTID" ,"ACCESSTYPEID"  from QUESTIONPOOLACCESSDATA;

insert into sam_assessmentBase_t ("ID" ,"ISTEMPLATE" ,
    "PARENTID" ,"TITLE" ,"DESCRIPTION" ,"COMMENTS" ,
    "ASSESSMENTTEMPLATEID" ,"TYPEID" ,"INSTRUCTORNOTIFICATION" ,
    "TESTEENOTIFICATION" ,"MULTIPARTALLOWED" ,"STATUS" ,
    "CREATEDBY" ,"CREATEDDATE" ,"LASTMODIFIEDBY" ,
    "LASTMODIFIEDDATE" )  
select "ID" ,"ISTEMPLATE" ,
    "PARENTID" ,"TITLE" ,"DESCRIPTION" ,"COMMENTS" ,
    "ASSESSMENTTEMPLATEID" ,"TYPEID" ,"INSTRUCTORNOTIFICATION" ,
    "TESTEENOTIFICATION" ,"MULTIPARTALLOWED" ,"STATUS" ,
    "CREATEDBY" ,"CREATEDDATE" ,"LASTMODIFIEDBY" ,
    "LASTMODIFIEDDATE" from ASSESSMENTBASE;

insert into sam_type_t ("TYPEID" ,"AUTHORITY" ,"DOMAIN" ,
    "KEYWORD" ,"DESCRIPTION" ,"STATUS" ,"CREATEDBY" ,
    "CREATEDDATE" ,"LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" ) 
select "TYPEID" ,"AUTHORITY" ,"DOMAIN" ,
    "KEYWORD" ,"DESCRIPTION" ,"STATUS" ,"CREATEDBY" ,
    "CREATEDDATE" ,"LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" from TYPE;

insert into sam_publishedAssessment_t  ("ID" ,"ASSESSMENTID"
    ,"TITLE" ,"DESCRIPTION" ,"COMMENTS" ,
    "TYPEID" ,"INSTRUCTORNOTIFICATION" ,"TESTEENOTIFICATION" ,
    "MULTIPARTALLOWED" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,
    "LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" ) 
select "ID" ,"ASSESSMENTID"
    ,"TITLE" ,"DESCRIPTION" ,"COMMENTS" ,
    "TYPEID" ,"INSTRUCTORNOTIFICATION" ,"TESTEENOTIFICATION" ,
    "MULTIPARTALLOWED" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,
    "LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" from PUBLISHEDASSESSMENT;

insert into sam_assessMetaData_t  ("ASSESSMENTMETADATAID" ,
"ASSESSMENTID" ,"LABEL" ,"ENTRY" ) 
select "ASSESSMENTMETADATAID" ,
"ASSESSMENTID" ,"LABEL" ,"ENTRY" from ASSESSMENTMETADATA;

insert into sam_assessAccessControl_t ("ASSESSMENTID" ,
    "UNLIMITEDSUBMISSIONS" ,"SUBMISSIONSALLOWED" ,
    "SUBMISSIONSSAVED" ,"ASSESSMENTFORMAT" ,"BOOKMARKINGITEM" ,
    "TIMELIMIT" ,"TIMEDASSESSMENT" ,"RETRYALLOWED" ,
    "LATEHANDLING" ,"STARTDATE" ,"DUEDATE" ,"SCOREDATE" ,
    "FEEDBACKDATE" ,"RETRACTDATE" ,"AUTOSUBMIT" ,"ITEMNAVIGATION"
    ,"ITEMNUMBERING" , "DISPLAYSCORE","SUBMISSIONMESSAGE" ,"RELEASETO" ,
    "USERNAME" ,"PASSWORD" ,"FINALPAGEURL" ) 
select "ASSESSMENTID" ,
    "UNLIMITEDSUBMISSIONS" ,"SUBMISSIONSALLOWED" ,
    "SUBMISSIONSSAVED" ,"ASSESSMENTFORMAT" ,"BOOKMARKINGITEM" ,
    "TIMELIMIT" ,"TIMEDASSESSMENT" ,"RETRYALLOWED" ,
    "LATEHANDLING" ,"STARTDATE" ,"DUEDATE" ,"SCOREDATE" ,
    "FEEDBACKDATE" ,"RETRACTDATE" ,"AUTOSUBMIT" ,"ITEMNAVIGATION"
    ,"ITEMNUMBERING" , "DISPLAYSCORE", "SUBMISSIONMESSAGE" ,"RELEASETO" ,
    "USERNAME" ,"PASSWORD" ,"FINALPAGEURL"  from ASSESSMENTACCESSCONTROL;

insert into sam_assessEvaluation_t ("ASSESSMENTID"
    ,"EVALUATIONCOMPONENTS" ,"SCORINGTYPE" ,"NUMERICMODELID" ,
    "FIXEDTOTALSCORE" ,"GRADEAVAILABLE" ,"ISSTUDENTIDPUBLIC" ,
    "ANONYMOUSGRADING" ,"AUTOSCORING" ,"TOGRADEBOOK" ) 
select "ASSESSMENTID"
    ,"EVALUATIONCOMPONENTS" ,"SCORINGTYPE" ,"NUMERICMODELID" ,
    "FIXEDTOTALSCORE" ,"GRADEAVAILABLE" ,"ISSTUDENTIDPUBLIC" ,
    "ANONYMOUSGRADING" ,"AUTOSCORING" ,"TOGRADEBOOK" from ASSESSMENTEVALUATIONMODEL;

insert into sam_assessFeedback_t ("ASSESSMENTID" ,
    "FEEDBACKDELIVERY" ,"EDITCOMPONENTS" ,"SHOWQUESTIONTEXT" ,
    "SHOWSTUDENTRESPONSE" ,"SHOWCORRECTRESPONSE" ,
    "SHOWSTUDENTSCORE" ,"SHOWQUESTIONLEVELFEEDBACK" ,
    "SHOWSELECTIONLEVELFEEDBACK" ,"SHOWGRADERCOMMENTS" ,
    "SHOWSTATISTICS" )  
select "ASSESSMENTID" ,
    "FEEDBACKDELIVERY" ,"EDITCOMPONENTS" ,"SHOWQUESTIONTEXT" ,
    "SHOWSTUDENTRESPONSE" ,"SHOWCORRECTRESPONSE" ,
    "SHOWSTUDENTSCORE" ,"SHOWQUESTIONLEVELFEEDBACK" ,
    "SHOWSELECTIONLEVELFEEDBACK" ,"SHOWGRADERCOMMENTS" ,
    "SHOWSTATISTICS" from ASSESSMENTFEEDBACK;

insert into sam_section_t ("SECTIONID" ,"ASSESSMENTID" ,
    "DURATION" ,"SEQUENCE" ,"TITLE" ,"DESCRIPTION" ,"TYPEID" ,
    "STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,"LASTMODIFIEDBY" ,
    "LASTMODIFIEDDATE" )  
select  "SECTIONID" ,"ASSESSMENTID" ,
    "DURATION" ,"SEQUENCE" ,"TITLE" ,"DESCRIPTION" ,"TYPEID" ,
    "STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,"LASTMODIFIEDBY" ,
    "LASTMODIFIEDDATE" from SECTION;

insert into sam_securedIP_t ("IPADDRESSID" ,
    "ASSESSMENTID" ,"HOSTNAME" ,"IPADDRESS" )  
select "IPADDRESSID" ,
    "ASSESSMENTID" ,"HOSTNAME" ,"IPADDRESS"  from SECUREDIPADDRESS;

insert into sam_gradingSummary_t  ("ASSESSMENTGRADINGSUMMARYID" ,"PUBLISHEDASSESSMENTID" ,
    "AGENTID" ,"TOTALSUBMITTED" ,"TOTALSUBMITTEDFORGRADE" ,
     "LASTSUBMITTEDDATE" ,
    "LASTSUBMITTEDASSESSMENTISLATE" ,"SUMOF_AUTOSCOREFORGRADE" ,
    "AVERAGE_AUTOSCOREFORGRADE" ,"HIGHEST_AUTOSCOREFORGRADE" ,
    "LOWEST_AUTOSCOREFORGRADE" ,"LAST_AUTOSCOREFORGRADE" ,
    "SUMOF_OVERRIDESCOREFORGRADE" ,
    "AVERAGE_OVERRIDESCOREFORGRADE" ,
    "HIGHEST_OVERRIDESCOREFORGRADE" ,
    "LOWEST_OVERRIDESCOREFORGRADE" ,"LAST_OVERRIDESCOREFORGRADE" ,
    "SCORINGTYPE" ,
    "ACCEPTEDASSESSMENTISLATE" ,"FINALASSESSMENTSCORE" ,
    "FEEDTOGRADEBOOK" )  
select "ASSESSMENTGRADINGSUMMARYID" ,"PUBLISHEDASSESSMENTID" ,
    "AGENTID" ,"TOTALSUBMITTED" ,"TOTALSUBMITTEDFORGRADE" ,
    "LASTSUBMITTEDDATE" ,
    "LASTSUBMITTEDASSESSMENTISLATE" ,"SUMOF_AUTOSCOREFORGRADE" ,
    "AVERAGE_AUTOSCOREFORGRADE" ,"HIGHEST_AUTOSCOREFORGRADE" ,
    "LOWEST_AUTOSCOREFORGRADE" ,"LAST_AUTOSCOREFORGRADE" ,
    "SUMOF_OVERRIDESCOREFORGRADE" ,
    "AVERAGE_OVERRIDESCOREFORGRADE" ,
    "HIGHEST_OVERRIDESCOREFORGRADE" ,
    "LOWEST_OVERRIDESCOREFORGRADE" ,"LAST_OVERRIDESCOREFORGRADE" ,
    "SCORINGTYPE" ,
    "ACCEPTEDASSESSMENTISLATE" ,"FINALASSESSMENTSCORE" ,
    "FEEDTOGRADEBOOK" from ASSESSMENTGRADINGSUMMARY;

insert into sam_publishedEvaluation_t ("ASSESSMENTID" ,
    "EVALUATIONCOMPONENTS" ,"SCORINGTYPE" ,"NUMERICMODELID" ,
    "FIXEDTOTALSCORE" ,"GRADEAVAILABLE" ,"ISSTUDENTIDPUBLIC" ,
    "ANONYMOUSGRADING" ,"AUTOSCORING" ,"TOGRADEBOOK" )   
select "ASSESSMENTID" ,
    "EVALUATIONCOMPONENTS" ,"SCORINGTYPE" ,"NUMERICMODELID" ,
    "FIXEDTOTALSCORE" ,"GRADEAVAILABLE" ,"ISSTUDENTIDPUBLIC" ,
    "ANONYMOUSGRADING" ,"AUTOSCORING" ,"TOGRADEBOOK" from PUBLISHEDEVALUATIONMODEL;

insert into sam_publishedFeedback_t ("ASSESSMENTID" ,
    "FEEDBACKDELIVERY" ,"EDITCOMPONENTS" ,"SHOWQUESTIONTEXT" ,
    "SHOWSTUDENTRESPONSE" ,"SHOWCORRECTRESPONSE" ,
    "SHOWSTUDENTSCORE" ,"SHOWQUESTIONLEVELFEEDBACK" ,
    "SHOWSELECTIONLEVELFEEDBACK" ,"SHOWGRADERCOMMENTS" ,
    "SHOWSTATISTICS" )  
select "ASSESSMENTID" ,
    "FEEDBACKDELIVERY" ,"EDITCOMPONENTS" ,"SHOWQUESTIONTEXT" ,
    "SHOWSTUDENTRESPONSE" ,"SHOWCORRECTRESPONSE" ,
    "SHOWSTUDENTSCORE" ,"SHOWQUESTIONLEVELFEEDBACK" ,
    "SHOWSELECTIONLEVELFEEDBACK" ,"SHOWGRADERCOMMENTS" ,
    "SHOWSTATISTICS" from PUBLISHEDFEEDBACK;
 
insert into sam_publishedSecuredIP_t ("IPADDRESSID" ,
    "ASSESSMENTID" ,"HOSTNAME" ,"IPADDRESS" )  
select "IPADDRESSID" ,
    "ASSESSMENTID" ,"HOSTNAME" ,"IPADDRESS" from PUBLISHEDSECUREDIPADDRESS;

insert into sam_publishedAccessControl_t ("ASSESSMENTID" ,
    "UNLIMITEDSUBMISSIONS" ,"SUBMISSIONSALLOWED" ,
    "SUBMISSIONSSAVED" ,"ASSESSMENTFORMAT" ,"BOOKMARKINGITEM" ,
    "TIMELIMIT" ,"TIMEDASSESSMENT" ,"RETRYALLOWED" ,
    "LATEHANDLING" ,"STARTDATE" ,"DUEDATE" ,"SCOREDATE" ,
    "FEEDBACKDATE" ,"RETRACTDATE" ,"AUTOSUBMIT" ,"ITEMNAVIGATION"
    ,"ITEMNUMBERING" ,"SUBMISSIONMESSAGE" ,"RELEASETO" ,
    "USERNAME" ,"PASSWORD" ,"FINALPAGEURL" )  
select "ASSESSMENTID" ,
    "UNLIMITEDSUBMISSIONS" ,"SUBMISSIONSALLOWED" ,
    "SUBMISSIONSSAVED" ,"ASSESSMENTFORMAT" ,"BOOKMARKINGITEM" ,
    "TIMELIMIT" ,"TIMEDASSESSMENT" ,"RETRYALLOWED" ,
    "LATEHANDLING" ,"STARTDATE" ,"DUEDATE" ,"SCOREDATE" ,
    "FEEDBACKDATE" ,"RETRACTDATE" ,"AUTOSUBMIT" ,"ITEMNAVIGATION"
    ,"ITEMNUMBERING" ,"SUBMISSIONMESSAGE" ,"RELEASETO" ,
    "USERNAME" ,"PASSWORD" ,"FINALPAGEURL" from PUBLISHEDACCESSCONTROL;

insert into sam_assessmentGrading_t ("ASSESSMENTGRADINGID" ,
    "PUBLISHEDASSESSMENTID" ,"AGENTID" ,
    "SUBMITTEDDATE" ,"ISLATE" ,"FORGRADE" ,"TOTALAUTOSCORE" ,
    "TOTALOVERRIDESCORE" ,"FINALSCORE" ,"COMMENTS" ,"GRADEDBY" ,
    "GRADEDDATE" ,"STATUS" ,"ATTEMPTDATE" ,"TIMEELAPSED" ) 
select "ASSESSMENTGRADINGID" ,
    "PUBLISHEDASSESSMENTID" ,"AGENTID" ,
    "SUBMITTEDDATE" ,"ISLATE" ,"FORGRADE" ,"TOTALAUTOSCORE" ,
    "TOTALOVERRIDESCORE" ,"FINALSCORE" ,"COMMENTS" ,"GRADEDBY" ,
    "GRADEDDATE" ,"STATUS" ,"ATTEMPTDATE" ,"TIMEELAPSED" from ASSESSMENTGRADINGDATA;

insert into sam_publishedSection_t ("SECTIONID" ,
    "ASSESSMENTID" ,"DURATION" ,"SEQUENCE" ,"TITLE" ,
    "DESCRIPTION" ,"TYPEID" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE"
    ,"LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" )  
select "SECTIONID" ,
    "ASSESSMENTID" ,"DURATION" ,"SEQUENCE" ,"TITLE" ,
    "DESCRIPTION" ,"TYPEID" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE"
    ,"LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" from PUBLISHEDSECTION;

insert into sam_publishedMetaData_t ("ASSESSMENTMETADATAID",
    "ASSESSMENTID" ,"LABEL" ,"ENTRY" )  
select "ASSESSMENTMETADATAID",
    "ASSESSMENTID" ,"LABEL" ,"ENTRY" from PUBLISHEDMETADATA;

insert into sam_publishedItem_t  ("ITEMID" ,"SECTIONID" ,
    "ITEMIDSTRING" ,"SEQUENCE" ,"DURATION" ,"TRIESALLOWED" ,
    "INSTRUCTION" ,"DESCRIPTION" ,"TYPEID" ,"GRADE" ,"SCORE" ,
    "HINT" ,"HASRATIONALE" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,
    "LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" )  
select "ITEMID" ,"SECTIONID" ,
    "ITEMIDSTRING" ,"SEQUENCE" ,"DURATION" ,"TRIESALLOWED" ,
    "INSTRUCTION" ,"DESCRIPTION" ,"TYPEID" ,"GRADE" ,"SCORE" ,
    "HINT" ,"HASRATIONALE" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,
    "LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" from PUBLISHEDITEM;

insert into sam_publishedItemText_t ("ITEMTEXTID" ,"ITEMID","SEQUENCE" ,"TEXT" )  
select "ITEMTEXTID" ,"ITEMID","SEQUENCE" ,"TEXT" from PUBLISHEDITEMTEXT;

insert into sam_publishedAnswer_t ("ANSWERID" ,"ITEMTEXTID" ,
    "ITEMID" ,"TEXT" ,"SEQUENCE" ,"LABEL" ,"ISCORRECT" ,"GRADE" ,
    "SCORE" )   
select "ANSWERID" ,"ITEMTEXTID" ,
    "ITEMID" ,"TEXT" ,"SEQUENCE" ,"LABEL" ,"ISCORRECT" ,"GRADE" ,
    "SCORE" from PUBLISHEDANSWER;

insert into sam_publishedAnswerFeedback_t ("ANSWERFEEDBACKID" ,"ANSWERID" ,"TYPEID" ,"TEXT" )  
select "ANSWERFEEDBACKID" ,"ANSWERID" ,"TYPEID" ,"TEXT" from PUBLISHEDANSWERFEEDBACK;

insert into sam_publishedItemFeedback_t ("ITEMFEEDBACKID" ,
    "ITEMID" ,"TYPEID" ,"TEXT" ) 
select  "ITEMFEEDBACKID" ,
    "ITEMID" ,"TYPEID" ,"TEXT" from PUBLISHEDITEMFEEDBACK;

insert into sam_publishedItemMetaData_t ("ITEMMETADATAID" ,
    "ITEMID" ,"LABEL" ,"ENTRY" ) 
select  "ITEMMETADATAID" ,
    "ITEMID" ,"LABEL" ,"ENTRY" from PUBLISHEDITEMMETADATA;

insert into sam_itemGrading_t  ("ITEMGRADINGID" ,
    "ASSESSMENTGRADINGID" ,"PUBLISHEDITEMID" ,
    "PUBLISHEDITEMTEXTID" ,"AGENTID" ,"SUBMITTEDDATE" ,
    "PUBLISHEDANSWERID" ,"RATIONALE" ,"ANSWERTEXT" ,"AUTOSCORE" ,
    "OVERRIDESCORE" ,"COMMENTS" ,"GRADEDBY" ,"GRADEDDATE" ,
    "REVIEW" )   
select "ITEMGRADINGID" ,
    "ASSESSMENTGRADINGID" ,"PUBLISHEDITEMID" ,
    "PUBLISHEDITEMTEXTID" ,"AGENTID" ,"SUBMITTEDDATE" ,
    "PUBLISHEDANSWERID" ,"RATIONALE" ,"ANSWERTEXT" ,"AUTOSCORE" ,
    "OVERRIDESCORE" ,"COMMENTS" ,"GRADEDBY" ,"GRADEDDATE" ,
    "REVIEW"  from ITEMGRADINGDATA;

insert into sam_item_t  ("ITEMID" ,"SECTIONID" ,
    "ITEMIDSTRING" ,"SEQUENCE" ,"DURATION" ,"TRIESALLOWED" ,
    "INSTRUCTION" ,"DESCRIPTION" ,"TYPEID" ,"GRADE" ,"SCORE" ,
    "HINT" ,"HASRATIONALE" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,
    "LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" )   
select  "ITEMID" ,"SECTIONID" ,
    "ITEMIDSTRING" ,"SEQUENCE" ,"DURATION" ,"TRIESALLOWED" ,
    "INSTRUCTION" ,"DESCRIPTION" ,"TYPEID" ,"GRADE" ,"SCORE" ,
    "HINT" ,"HASRATIONALE" ,"STATUS" ,"CREATEDBY" ,"CREATEDDATE" ,
    "LASTMODIFIEDBY" ,"LASTMODIFIEDDATE" from ITEM;

insert into sam_itemMetaData_t ("ITEMMETADATAID" ,"ITEMID" ,
    "LABEL" ,"ENTRY" )  
select  "ITEMMETADATAID" ,"ITEMID" ,
    "LABEL" ,"ENTRY" from ITEMMETADATA;

insert into sam_itemFeedback_t  ("ITEMFEEDBACKID" ,"ITEMID" ,
    "TYPEID" ,"TEXT" )  
select "ITEMFEEDBACKID" ,"ITEMID" ,
    "TYPEID" ,"TEXT" from ITEMFEEDBACK;

insert into sam_itemText_t  ("ITEMTEXTID" ,"ITEMID" ,
    "SEQUENCE" ,"TEXT" )   
select "ITEMTEXTID" ,"ITEMID" ,
    "SEQUENCE" ,"TEXT" from ITEMTEXT;

insert into sam_answer_t  ("ANSWERID" ,"ITEMTEXTID" ,
    "ITEMID" ,"TEXT" ,"SEQUENCE" ,"LABEL" ,"ISCORRECT" ,"GRADE" ,
    "SCORE" )   
select "ANSWERID" ,"ITEMTEXTID" ,
    "ITEMID" ,"TEXT" ,"SEQUENCE" ,"LABEL" ,"ISCORRECT" ,"GRADE" ,
    "SCORE" from ANSWER;

insert into sam_answerFeedback_t ("ANSWERFEEDBACKID" ,
    "ANSWERID" ,"TYPEID" ,"TEXT" )  
select  "ANSWERFEEDBACKID" ,
    "ANSWERID" ,"TYPEID" ,"TEXT" from ANSWERFEEDBACK;


col cSeqVal noprint new_value uSeqVal
col cMaxVal noprint new_value uMaxVal
col cIncVal noprint new_value uIncVal

select sam_questionPool_id_s.nextval cSeqVal from dual;
select MAX(questionPoolId) cMaxVal from sam_questionPool_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_questionPool_id_s increment by &uIncVal;
select sam_questionPool_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_questionPool_id_s increment by 1;

select sam_assessmentBase_id_s.nextval cSeqVal from dual;
select MAX(id) cMaxVal from sam_assessmentBase_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_assessmentBase_id_s increment by &uIncVal;
select sam_assessmentBase_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_assessmentBase_id_s increment by 1;

select sam_type_id_s.nextval cSeqVal from dual;
select MAX(typeId) cMaxVal from sam_type_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_type_id_s increment by &uIncVal;
select sam_type_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_type_id_s increment by 1;

select sam_publishedAssessment_id_s.nextval cSeqVal from dual;
select MAX(id) cMaxVal from sam_publishedAssessment_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_publishedAssessment_id_s increment by &uIncVal;
select sam_publishedAssessment_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_publishedAssessment_id_s increment by 1;

select sam_assessMetaData_id_s.nextval cSeqVal from dual;
select MAX(assessmentMetaDataId) cMaxVal from sam_assessMetaData_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_assessMetaData_id_s increment by &uIncVal;
select sam_assessMetaData_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_assessMetaData_id_s increment by 1;

select sam_section_id_s.nextval cSeqVal from dual;
select MAX(sectionId) cMaxVal from sam_section_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_section_id_s increment by &uIncVal;
select sam_section_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_section_id_s increment by 1;

select sam_securedIP_id_s.nextval cSeqVal from dual;
select MAX(ipAddressId) cMaxVal from sam_securedIP_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_securedIP_id_s increment by &uIncVal;
select sam_securedIP_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_securedIP_id_s increment by 1;

select sam_gradingSummary_id_s.nextval cSeqVal from dual;
select MAX(assessmentGradingSummaryId) cMaxVal from sam_gradingSummary_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_gradingSummary_id_s increment by &uIncVal;
select sam_gradingSummary_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_gradingSummary_id_s increment by 1;

select sam_publishedSecuredIP_id_s.nextval cSeqVal from dual;
select MAX(ipAddressId) cMaxVal from sam_publishedSecuredIP_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_publishedSecuredIP_id_s increment by &uIncVal;
select sam_publishedSecuredIP_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_publishedSecuredIP_id_s increment by 1;

select sam_assessmentGrading_id_s.nextval cSeqVal from dual;
select MAX(assessmentGradingId) cMaxVal from sam_assessmentGrading_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_assessmentGrading_id_s increment by &uIncVal;
select sam_assessmentGrading_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_assessmentGrading_id_s increment by 1;

select sam_publishedSection_id_s.nextval cSeqVal from dual;
select MAX(sectionId) cMaxVal from sam_publishedSection_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_publishedSection_id_s increment by &uIncVal;
select sam_publishedSection_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_publishedSection_id_s increment by 1

select sam_publishedMetaData_id_s.nextval cSeqVal from dual;
select MAX(assessmentMetaDataId) cMaxVal from sam_publishedMetaData_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_publishedMetaData_id_s increment by &uIncVal;
select sam_publishedMetaData_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_publishedMetaData_id_s increment by 1;

select sam_pubItem_id_s.nextval cSeqVal from dual;
select MAX(itemId) cMaxVal from sam_publishedItem_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_pubItem_id_s increment by &uIncVal;
select sam_pubItem_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_pubItem_id_s increment by 1;

select sam_pubItemText_id_s.nextval cSeqVal from dual;
select MAX(itemTextId) cMaxVal from sam_publishedItemText_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_pubItemText_id_s increment by &uIncVal;
select sam_pubItemText_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_pubItemText_id_s increment by 1;

select sam_pubanswer_id_s.nextval cSeqVal from dual;
select MAX(answerId) cMaxVal from sam_publishedAnswer_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_pubanswer_id_s increment by &uIncVal;
select sam_pubanswer_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_pubanswer_id_s increment by 1;

select sam_pubanswerfeedback_id_s.nextval cSeqVal from dual;
select MAX(answerFeedbackId) cMaxVal from sam_publishedanswerfeedback_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_pubanswerfeedback_id_s increment by &uIncVal;
select sam_pubanswerfeedback_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_pubanswerfeedback_id_s increment by 1;

select sam_pubItemFeedback_id_s.nextval cSeqVal from dual;
select MAX(itemFeedbackId) cMaxVal from sam_publishedItemFeedback_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_pubItemFeedback_id_s increment by &uIncVal;
select sam_pubItemFeedback_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_pubItemFeedback_id_s increment by 1;

select sam_pubItemMetaData_id_s.nextval cSeqVal from dual;
select MAX(itemMetaDataId) cMaxVal from sam_publishedItemMetaData_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_pubItemMetaData_id_s increment by &uIncVal;
select sam_pubItemMetaData_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_pubItemMetaData_id_s increment by 1;

select sam_itemGrading_id_s.nextval cSeqVal from dual;
select MAX(itemGradingId) cMaxVal from sam_itemGrading_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_itemGrading_id_s increment by &uIncVal;
select sam_itemGrading_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_itemGrading_id_s increment by 1;

select sam_item_id_s.nextval cSeqVal from dual;
select MAX(itemId) cMaxVal from sam_item_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_item_id_s increment by &uIncVal;
select sam_item_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_item_id_s increment by 1;

select sam_itemMetaData_id_s.nextval cSeqVal from dual;
select MAX(itemMetaDataId) cMaxVal from sam_itemMetaData_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_itemMetaData_id_s increment by &uIncVal;
select sam_itemMetaData_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_itemMetaData_id_s increment by 1;

select sam_itemFeedback_id_s.nextval cSeqVal from dual;
select MAX(itemFeedbackId) cMaxVal from sam_itemFeedback_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_itemFeedback_id_s increment by &uIncVal;
select sam_itemFeedback_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_itemFeedback_id_s increment by 1;

select sam_itemText_id_s.nextval cSeqVal from dual;
select MAX(itemTextId) cMaxVal from sam_itemText_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_itemText_id_s increment by &uIncVal;
select sam_itemText_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_itemText_id_s increment by 1;

select sam_answer_id_s.nextval cSeqVal from dual;
select MAX(answerId) cMaxVal from sam_answer_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_answer_id_s increment by &uIncVal;
select sam_answer_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_answer_id_s increment by 1;

select sam_answerFeedback_id_s.nextval cSeqVal from dual;
select MAX(answerFeedbackId) cMaxVal from sam_answerFeedback_t;
select &uMaxVal - &uSeqVal + 2 cIncVal from dual;
alter sequence sam_answerFeedback_id_s increment by &uIncVal;
select sam_answerFeedback_id_s.nextval "RESETTING SEQUENCE VALUE" from dual;
alter sequence sam_answerFeedback_id_s increment by 1;
 
commit; 

-- ------the following new tables are added recently, no data in the old tables to copy from ----------------- 
-- ------sam_authzData_t----------
-- ------sam_qualifierData_t--------
-- ------sam_functionData_t-------- 
-- ------sam_media_t----------- 




