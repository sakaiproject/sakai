/* 
 * Add additional fields to SakaiPerson 
 * Use this when upgrading from Profile2 1.1 to Profile2 1.2 only on Sakai versions < 2.7
 * 
 */
alter table SAKAI_PERSON_T add EDUCATION_COURSE varchar2(255);
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS varchar2(255);

/* PRFL-97 */
update SAKAI_PERSON_T set locked=false where locked=null;