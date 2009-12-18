/* 
 * Add additional fields to SakaiPerson 
 * Use this for a fresh install of Profile2 1.2 only on Sakai versions < 2.7
 * 
 */
alter table SAKAI_PERSON_T add FAVOURITE_BOOKS text;
alter table SAKAI_PERSON_T add FAVOURITE_TV_SHOWS text;
alter table SAKAI_PERSON_T add FAVOURITE_MOVIES text;
alter table SAKAI_PERSON_T add FAVOURITE_QUOTES text;
alter table SAKAI_PERSON_T add EDUCATION_COURSE text;
alter table SAKAI_PERSON_T add EDUCATION_SUBJECTS text;

/* PRFL-97 */
update SAKAI_PERSON_T set locked=false where locked=null;
