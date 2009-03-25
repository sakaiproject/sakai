drop table CMN_TYPE_T cascade constraints
drop table SAKAI_PERSON_T cascade constraints
drop sequence CMN_TYPE_S
create table CMN_TYPE_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null unique,
   LAST_MODIFIED_BY varchar2(36) not null,
   LAST_MODIFIED_DATE date not null,
   CREATED_BY varchar2(36) not null,
   CREATED_DATE date not null,
   AUTHORITY varchar2(100) not null,
   DOMAIN varchar2(100) not null,
   KEYWORD varchar2(100) not null,
   DISPLAY_NAME varchar2(255) not null,
   DESCRIPTION varchar2(255),
   primary key (ID),
   unique (AUTHORITY, DOMAIN, KEYWORD)
)
create table SAKAI_PERSON_T (
   ID number(19,0) not null,
   PERSON_TYPE varchar2(3) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null unique,
   LAST_MODIFIED_BY varchar2(36) not null,
   LAST_MODIFIED_DATE date not null,
   CREATED_BY varchar2(36) not null,
   CREATED_DATE date not null,
   AGENT_UUID varchar2(36) not null,
   TYPE_UUID varchar2(36) not null,
   COMMON_NAME varchar2(255),
   DESCRIPTION varchar2(255),
   SEE_ALSO varchar2(255),
   STREET varchar2(255),
   SURNAME varchar2(255),
   TELEPHONE_NUMBER varchar2(255),
   FAX_NUMBER varchar2(255),
   LOCALITY_NAME varchar2(255),
   OU varchar2(255),
   PHYSICAL_DELIVERY_OFFICE_NAME varchar2(255),
   POSTAL_ADDRESS varchar2(255),
   POSTAL_CODE varchar2(255),
   POST_OFFICE_BOX varchar2(255),
   STATE_PROVINCE_NAME varchar2(255),
   STREET_ADDRESS varchar2(255),
   TITLE varchar2(255),
   BUSINESS_CATEGORY varchar2(255),
   CAR_LICENSE varchar2(255),
   DEPARTMENT_NUMBER varchar2(255),
   DISPLAY_NAME varchar2(255),
   EMPLOYEE_NUMBER varchar2(255),
   EMPLOYEE_TYPE varchar2(255),
   GIVEN_NAME varchar2(255),
   HOME_PHONE varchar2(255),
   HOME_POSTAL_ADDRESS varchar2(255),
   INITIALS varchar2(255),
   JPEG_PHOTO blob,
   LABELED_URI varchar2(255),
   MAIL varchar2(255),
   MANAGER varchar2(255),
   MOBILE varchar2(255),
   ORGANIZATION varchar2(255),
   PAGER varchar2(255),
   PREFERRED_LANGUAGE varchar2(255),
   ROOM_NUMBER varchar2(255),
   SECRETARY varchar2(255),
   UID_C varchar2(255),
   USER_CERTIFICATE raw(255),
   USER_PKCS12 raw(255),
   USER_SMIME_CERTIFICATE raw(255),
   X500_UNIQUE_ID varchar2(255),
   AFFILIATION varchar2(255),
   ENTITLEMENT varchar2(255),
   NICKNAME varchar2(255),
   ORG_DN varchar2(255),
   ORG_UNIT_DN varchar2(255),
   PRIMARY_AFFILIATION varchar2(255),
   PRIMARY_ORG_UNIT_DN varchar2(255),
   PRINCIPAL_NAME varchar2(255),
   CAMPUS varchar2(255),
   HIDE_PRIVATE_INFO number(1,0),
   HIDE_PUBLIC_INFO number(1,0),
   NOTES varchar2(255),
   PICTURE_URL varchar2(255),
   SYSTEM_PICTURE_PREFERRED number(1,0),
   ferpaEnabled number(1,0),
   primary key (ID),
   unique (AGENT_UUID, TYPE_UUID)
)
create index CMN_TYPE_T_DISPLAY_NAME_I on CMN_TYPE_T (DISPLAY_NAME)
create index CMN_TYPE_T_KEYWORD_I on CMN_TYPE_T (KEYWORD)
create index CMN_TYPE_T_DOMAIN_I on CMN_TYPE_T (DOMAIN)
create index CMN_TYPE_T_AUTHORITY_I on CMN_TYPE_T (AUTHORITY)
create index SAKAI_PERSON_TYPE_UUID_I on SAKAI_PERSON_T (TYPE_UUID)
create index SAKAI_PERSON_SURNAME_I on SAKAI_PERSON_T (SURNAME)
create index SAKAI_PERSON_ferpaEnabled_I on SAKAI_PERSON_T (ferpaEnabled)
create index SAKAI_PERSON_AGENT_UUID_I on SAKAI_PERSON_T (AGENT_UUID)
create index SAKAI_PERSON_GIVEN_NAME_I on SAKAI_PERSON_T (GIVEN_NAME)
create index SAKAI_PERSON_UID_I on SAKAI_PERSON_T (UID_C)
create sequence CMN_TYPE_S
create sequence SAKAI_PERSON_S
