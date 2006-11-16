-- This is the MySQL Sakai 2.3.0 (or later) -> 2.4.0 conversion script
----------------------------------------------------------------------------------------------------------------------------------------
--
-- use this to convert a Sakai database from 2.3.0 to 2.4.0.  Run this before you run your first app server.
-- auto.ddl does not need to be enabled in your app server - this script takes care of all new TABLEs, changed TABLEs, and changed data.
--
----------------------------------------------------------------------------------------------------------------------------------------

----------------------------------------------------------------------------------------------------------------------------------------

-- OSP conversion
alter table osp_presentation_template add column propertyFormType varchar(36);
alter table osp_presentation add column property_form varchar(36);

-- SAMIGO SAK-6790 conversion
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
alter table SAM_PUBLISHEDATTACHMENT_T  MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_AUTHZDATA_T MODIFY AGENTID varchar(255) not null, MODIFY FUNCTIONID varchar(36) not null, MODIFY QUALIFIERID varchar(36) not null;
alter table SAM_AUTHZDATA_T MODIFY LASTMODIFIEDBY varchar(255) not null;
alter table SAM_ASSESSMENTGRADING_T MODIFY AGENTID varchar(255) not null, MODIFY GRADEDBY varchar(255);
alter table SAM_ITEMGRADING_T MODIFY AGENTID varchar(255) not null, MODIFY GRADEDBY varchar(255);
alter table SAM_GRADINGSUMMARY_T MODIFY AGENTID varchar(255) not null;
alter table SAM_MEDIA_T MODIFY CREATEDBY varchar(255), MODIFY LASTMODIFIEDBY varchar(255);
alter table SAM_TYPE_T MODIFY CREATEDBY varchar(255) not null, MODIFY LASTMODIFIEDBY varchar(255) not null;
