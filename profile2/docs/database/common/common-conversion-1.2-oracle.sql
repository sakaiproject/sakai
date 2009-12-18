/* 
 * Add additional fields to SakaiPerson 
 * Use this for a fresh install of Profile2 1.2 only on Sakai versions < 2.7
 * 
 */
alter table SAKAI_PERSON_T add FAVOURITE_BOOKS varchar2(255);
alter table SAKAI_PERSON_T add FAVOURITE_TV_SHOWS varchar2(255);
alter table SAKAI_PERSON_T add FAVOURITE_MOVIES varchar2(255);
alter table SAKAI_PERSON_T add FAVOURITE_QUOTES varchar2(255);
alter table SAKAI_PERSON_T add EDUCATION_COURSE varchar2(255);
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS varchar2(255);

/* PRFL-97 */
update SAKAI_PERSON_T set locked=false where locked=null;