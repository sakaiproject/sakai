alter table SAM_ASSESSMENTBASE_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_SECTION_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;

alter table SAM_PUBLISHEDASSESSMENT_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_PUBLISHEDSECTION_T MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;

alter table SAM_ITEM_T MODIFY  ITEMIDSTRING varchar(255), MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_ITEMFEEDBACK_T MODIFY TYPEID varchar(255) not null;
alter table SAM_ANSWERFEEDBACK_T MODIFY TYPEID varchar(255);
alter table SAM_ATTACHMENT_T MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;

alter table SAM_PUBLISHEDITEM_T MODIFY  ITEMIDSTRING varchar(255), MODIFY  CREATEDBY varchar(255) not null,  MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_PUBLISHEDITEMFEEDBACK_T MODIFY TYPEID varchar(255) not null;
alter table SAM_PUBLISHEDANSWERFEEDBACK_T MODIFY TYPEID varchar(255);
alter table SAM_PUBLISHEDATTACHMENT_T MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;

alter table SAM_AUTHZDATA_T MODIFY AGENTID varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;

alter table SAM_ASSESSMENTGRADING_T MODIFY AGENTID varchar(255) not null, MODIFY GRADEDBY varchar(255);
alter table SAM_ITEMGRADING_T MODIFY AGENTID varchar(255) not null, MODIFY GRADEDBY varchar(255);
alter table SAM_GRADINGSUMMARY_T MODIFY AGENTID varchar(255) not null;

alter table SAM_MEDIA_T MODIFY CREATEDBY varchar(255), MODIFY LASTMODIFIEDBY varchar(255);

alter table SAM_TYPE_T MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;