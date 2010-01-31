-- SAK-16835 columns for new quartz version
alter table QRTZ_TRIGGERS add PRIORITY number(2);
alter table QRTZ_FIRED_TRIGGERS add PRIORITY number(2); 

-- SAK-17821 Add additional fields to SakaiPerson
alter table SAKAI_PERSON_T add STAFF_PROFILE varchar2(4000);
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL varchar2(4000);
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL varchar2(4000);
alter table SAKAI_PERSON_T add PUBLICATIONS varchar2(4000);
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY varchar2(4000);


