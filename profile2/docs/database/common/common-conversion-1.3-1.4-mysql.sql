/* 
 * Add additional fields to SakaiPerson 
 * Use this when upgrading from Profile2 1.3 to Profile2 1.4 only on Sakai versions < 2.8
 * 
 */
alter table SAKAI_PERSON_T add STAFF_PROFILE text;
alter table SAKAI_PERSON_T add UNIVERSITY_PROFILE_URL text;
alter table SAKAI_PERSON_T add ACADEMIC_PROFILE_URL text;
alter table SAKAI_PERSON_T add PUBLICATIONS text;
alter table SAKAI_PERSON_T add BUSINESS_BIOGRAPHY text;
