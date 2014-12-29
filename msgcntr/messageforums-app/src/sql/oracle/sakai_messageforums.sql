alter table MFR_LABEL_T drop constraint FKC6611543EA902104
alter table MFR_LABEL_T drop constraint FKC661154344B127B6
alter table MFR_TOPIC_T drop constraint FK863DC0BEB88980FA
alter table MFR_TOPIC_T drop constraint FK863DC0BEFDACE462
alter table MFR_TOPIC_T drop constraint FK863DC0BEC6FDB1CF
alter table MFR_TOPIC_T drop constraint FK863DC0BE7E4BD00C
alter table MFR_TOPIC_T drop constraint FK863DC0BE7AFA22C2
alter table MFR_TOPIC_T drop constraint FK863DC0BE20D91C10
alter table MFR_TOPIC_T drop constraint FK863DC0BED83B3B18
alter table MFR_MESSAGE_T drop constraint FK80C1A316FE0789EA
alter table MFR_MESSAGE_T drop constraint FK80C1A3164FDCE067
alter table MFR_ATTACHMENT_T drop constraint FK7B2D5CDE2AFBA652
alter table MFR_ATTACHMENT_T drop constraint FK7B2D5CDEC6FDB1CF
alter table MFR_ATTACHMENT_T drop constraint FK7B2D5CDE20D91C10
alter table MFR_ATTACHMENT_T drop constraint FK7B2D5CDEFDEB22F9
alter table MFR_ATTACHMENT_T drop constraint FK7B2D5CDEAD5AF852
alter table MFR_OPEN_FORUM_T drop constraint FKC1760847FDACE462
alter table MFR_OPEN_FORUM_T drop constraint FKC17608474FDCE067
alter table MFR_OPEN_FORUM_T drop constraint FKC17608477E4BD00C
alter table MFR_OPEN_FORUM_T drop constraint FKC1760847D83B3B18
alter table MFR_OPEN_FORUM_T drop constraint FKC1760847B88980FA
alter table MFR_PRIVATE_FORUM_T drop constraint FKA9EE57544FDCE067
alter table MFR_MESSAGE_FORUMS_USER_T drop constraint FKF3B8460F737F309B
alter table MFR_MESSAGE_FORUMS_USER_T drop constraint FKF3B8460FC49D71A5
alter table MFR_MESSAGE_FORUMS_USER_T drop constraint FKF3B8460F96792399
alter table MFR_MESSAGE_FORUMS_USER_T drop constraint FKF3B8460FDD70E9E2
drop table MFR_LABEL_T cascade constraints
drop table MFR_UNREAD_STATUS_T cascade constraints
drop table MFR_TOPIC_T cascade constraints
drop table MFR_MESSAGE_T cascade constraints
drop table MFR_AREA_T cascade constraints
drop table MFR_ATTACHMENT_T cascade constraints
drop table MFR_OPEN_FORUM_T cascade constraints
drop table MFR_PRIVATE_FORUM_T cascade constraints
drop table MFR_CONTROL_PERSMISSIONS_T cascade constraints
drop table MFR_MESSAGE_PERSMISSIONS_T cascade constraints
drop table MFR_ACTOR_PERSMISSIONS_T cascade constraints
drop table MFR_MESSAGE_FORUMS_USER_T cascade constraints
drop table MFR_DATE_RESTRICTIONS_T cascade constraints
drop sequence MFR_MESSAGE_S
drop sequence MFR_ACTOR_PERSMISSIONS_S
drop sequence MFR_AREA_S
drop sequence MFR_PRIVATE_FORUM_S
drop sequence MFR_CONTROL_PERSMISSIONS_S
drop sequence MFR_TOPIC_S
drop sequence MFR_LABEL_S
drop sequence MFR_DATE_RESTRICTIONS_S
drop sequence MFR_UNREAD_STATUS_S
drop sequence MFR_MESSAGE_FORUMS_USER_S
drop sequence MFR_ATTACHMENT_S
drop sequence MFR_MESSAGE_PERSMISSIONS_S
drop sequence MFR_OPEN_FORUM_S
create table MFR_LABEL_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   KEY_C varchar2(255) not null,
   VALUE_C varchar2(255) not null,
   df_surrogateKey number(19,0),
   df_index_col number(10,0),
   dt_surrogateKey number(19,0),
   dt_index_col number(10,0),
   primary key (ID)
)
create table MFR_UNREAD_STATUS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   TOPIC_C varchar2(255) not null,
   MESSAGE_C varchar2(255) not null,
   USER_C varchar2(255) not null,
   READ_C number(1,0) not null,
   primary key (ID)
)
create table MFR_TOPIC_T (
   ID number(19,0) not null,
   TOPIC_DTYPE varchar2(2) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   TITLE varchar2(255) not null,
   SHORT_DESCRIPTION varchar2(255) not null,
   EXTENDED_DESCRIPTION clob not null,
   MUTABLE number(1,0) not null,
   SORT_INDEX number(10,0) not null,
   TYPE_UUID varchar2(36) not null,
   of_surrogateKey number(19,0),
   of_index_col number(10,0),
   pf_surrogateKey number(19,0),
   pf_index_col number(10,0),
   USER_ID varchar2(255),
   pt_surrogateKey number(19,0),
   pt_index_col number(10,0),
   CONTROL_PERMISSIONS number(19,0),
   MESSAGE_PERMISSIONS number(19,0),
   LOCKED number(1,0),
   CONFIDENTIAL_RESPONSES number(1,0),
   MUST_RESPOND_BEFORE_READING number(1,0),
   HOUR_BEFORE_RESPONSES_VISIBLE number(10,0),
   DATE_RESTRICTIONS number(19,0),
   ACTOR_PERMISSIONS number(19,0),
   MODERATED number(1,0),
   GRADEBOOK varchar2(255),
   GRADEBOOK_ASSIGNMENT varchar2(255),
   bf_index_col number(10,0),
   primary key (ID)
)
create table MFR_MESSAGE_T (
   ID number(19,0) not null,
   MESSAGE_DTYPE varchar2(2) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   TITLE varchar2(255) not null,
   BODY clob not null,
   AUTHOR varchar2(255) not null,
   LABEL varchar2(255),
   IN_REPLY_TO number(19,0),
   GRADEBOOK varchar2(255),
   GRADEBOOK_ASSIGNMENT varchar2(255),
   TYPE_UUID varchar2(36) not null,
   APPROVED number(1,0) not null,
   DRAFT number(1,0) not null,
   surrogateKey number(19,0),
   t_index_col number(10,0),
   EXTERNAL_EMAIL number(1,0),
   EXTERNAL_EMAIL_ADDRESS varchar2(255),
   primary key (ID)
)
create table MFR_AREA_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   CONTEXT_ID varchar2(255) not null,
   NAME varchar2(255) not null,
   HIDDEN number(1,0) not null,
   TYPE_UUID varchar2(36) not null,
   ENABLED number(1,0) not null,
   primary key (ID)
)
create table MFR_ATTACHMENT_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(255) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(255) not null,
   ATTACHMENT_ID varchar2(255) not null,
   ATTACHMENT_URL varchar2(255) not null,
   ATTACHMENT_NAME varchar2(255) not null,
   ATTACHMENT_SIZE varchar2(255) not null,
   ATTACHMENT_TYPE varchar2(255) not null,
   m_surrogateKey number(19,0),
   mes_index_col number(10,0),
   of_surrogateKey number(19,0),
   of_index_col number(10,0),
   pf_surrogateKey number(19,0),
   pf_index_col number(10,0),
   t_surrogateKey number(19,0),
   t_index_col number(10,0),
   of_urrogateKey number(19,0),
   f_index_col number(10,0),
   primary key (ID)
)
create table MFR_OPEN_FORUM_T (
   ID number(19,0) not null,
   FORUM_DTYPE varchar2(2) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   TITLE varchar2(255) not null,
   SHORT_DESCRIPTION varchar2(255) not null,
   EXTENDED_DESCRIPTION clob not null,
   TYPE_UUID varchar2(36) not null,
   SORT_INDEX number(10,0) not null,
   CONTROL_PERMISSIONS number(19,0),
   MESSAGE_PERMISSIONS number(19,0),
   LOCKED number(1,0) not null,
   surrogateKey number(19,0),
   area_index_col number(10,0),
   DATE_RESTRICTIONS number(19,0),
   ACTOR_PERMISSIONS number(19,0),
   MODERATED number(1,0),
   primary key (ID)
)
create table MFR_PRIVATE_FORUM_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   CREATED date not null,
   CREATED_BY varchar2(36) not null,
   MODIFIED date not null,
   MODIFIED_BY varchar2(36) not null,
   TITLE varchar2(255) not null,
   SHORT_DESCRIPTION varchar2(255) not null,
   EXTENDED_DESCRIPTION clob not null,
   TYPE_UUID varchar2(36) not null,
   SORT_INDEX number(10,0) not null,
   AUTO_FORWARD number(1,0) not null,
   AUTO_FORWARD_EMAIL varchar2(255) not null,
   PREVIEW_PANE_ENABLED number(1,0) not null,
   surrogateKey number(19,0),
   area_index_col number(10,0),
   primary key (ID)
)
create table MFR_CONTROL_PERSMISSIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   ROLE varchar2(255) not null,
   NEW_FORUM number(1,0) not null,
   NEW_TOPIC number(1,0) not null,
   NEW_RESPONSE number(1,0) not null,
   RESPONSE_TO_RESPONSE number(1,0) not null,
   MOVE_POSTINGS number(1,0) not null,
   CHANGE_SETTINGS number(1,0) not null,
   primary key (ID)
)
create table MFR_MESSAGE_PERSMISSIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   ROLE_C varchar2(255) not null,
   READ_C number(1,0) not null,
   REVISE_ANY number(1,0) not null,
   REVISE_OWN number(1,0) not null,
   DELETE_ANY number(1,0) not null,
   DELETE_OWN number(1,0) not null,
   READ_DRAFTS number(1,0) not null,
   primary key (ID)
)
create table MFR_ACTOR_PERSMISSIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   primary key (ID)
)
create table MFR_MESSAGE_FORUMS_USER_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   UUID varchar2(36) not null,
   apaSurrogateKey number(19,0),
   ap1_index_col number(10,0),
   apmSurrogateKey number(19,0),
   ap2_index_col number(10,0),
   apcSurrogateKey number(19,0),
   ap3_index_col number(10,0),
   mesSurrogateKey number(19,0),
   mes_index_col number(10,0),
   primary key (ID)
)
create table MFR_DATE_RESTRICTIONS_T (
   ID number(19,0) not null,
   VERSION number(10,0) not null,
   VISIBLE date not null,
   VISIBLE_POST_ON_SCHEDULE number(1,0) not null,
   POSTING_ALLOWED date not null,
   POSTING_ALLOWED_POST_ON_SCHEDULE number(1,0) not null,
   READ_ONLY date not null,
   READ_ONLY_POST_ON_SCHEDULE number(1,0) not null,
   HIDDEN date not null,
   HIDDEN_POST_ON_SCHEDULE number(1,0) not null,
   primary key (ID)
)
create index MFR_PARENT_I1 on MFR_LABEL_T (df_surrogateKey)
create index MFR_PARENT_I2 on MFR_LABEL_T (dt_surrogateKey)
alter table MFR_LABEL_T add constraint FKC6611543EA902104 foreign key (df_surrogateKey) references MFR_OPEN_FORUM_T
alter table MFR_LABEL_T add constraint FKC661154344B127B6 foreign key (dt_surrogateKey) references MFR_TOPIC_T
create index MFR_PARENT_I1 on MFR_TOPIC_T (of_surrogateKey)
create index MFR_PRI_PARENT_I on MFR_TOPIC_T (pt_surrogateKey)
create index MFR_PARENT_I2 on MFR_TOPIC_T (pf_surrogateKey)
alter table MFR_TOPIC_T add constraint FK863DC0BEB88980FA foreign key (ACTOR_PERMISSIONS) references MFR_ACTOR_PERSMISSIONS_T
alter table MFR_TOPIC_T add constraint FK863DC0BEFDACE462 foreign key (CONTROL_PERMISSIONS) references MFR_CONTROL_PERSMISSIONS_T
alter table MFR_TOPIC_T add constraint FK863DC0BEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T
alter table MFR_TOPIC_T add constraint FK863DC0BE7E4BD00C foreign key (MESSAGE_PERMISSIONS) references MFR_MESSAGE_PERSMISSIONS_T
alter table MFR_TOPIC_T add constraint FK863DC0BE7AFA22C2 foreign key (pt_surrogateKey) references MFR_TOPIC_T
alter table MFR_TOPIC_T add constraint FK863DC0BE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T
alter table MFR_TOPIC_T add constraint FK863DC0BED83B3B18 foreign key (DATE_RESTRICTIONS) references MFR_DATE_RESTRICTIONS_T
create index MFR_PARENT_TOPIC_I on MFR_MESSAGE_T (surrogateKey)
alter table MFR_MESSAGE_T add constraint FK80C1A316FE0789EA foreign key (IN_REPLY_TO) references MFR_MESSAGE_T
alter table MFR_MESSAGE_T add constraint FK80C1A3164FDCE067 foreign key (surrogateKey) references MFR_TOPIC_T
create index MFR_PARENT_I3 on MFR_ATTACHMENT_T (pf_surrogateKey)
create index MFR_PARENT_I on MFR_ATTACHMENT_T (m_surrogateKey)
create index MFR_PARENT_I2 on MFR_ATTACHMENT_T (of_surrogateKey)
create index MFR_PARENT_I4 on MFR_ATTACHMENT_T (t_surrogateKey)
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDE2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDEFDEB22F9 foreign key (m_surrogateKey) references MFR_MESSAGE_T
alter table MFR_ATTACHMENT_T add constraint FK7B2D5CDEAD5AF852 foreign key (of_urrogateKey) references MFR_OPEN_FORUM_T
create index MFR_PARENT_BASEFORUM_I on MFR_OPEN_FORUM_T (surrogateKey)
alter table MFR_OPEN_FORUM_T add constraint FKC1760847FDACE462 foreign key (CONTROL_PERMISSIONS) references MFR_CONTROL_PERSMISSIONS_T
alter table MFR_OPEN_FORUM_T add constraint FKC17608474FDCE067 foreign key (surrogateKey) references MFR_AREA_T
alter table MFR_OPEN_FORUM_T add constraint FKC17608477E4BD00C foreign key (MESSAGE_PERMISSIONS) references MFR_MESSAGE_PERSMISSIONS_T
alter table MFR_OPEN_FORUM_T add constraint FKC1760847D83B3B18 foreign key (DATE_RESTRICTIONS) references MFR_DATE_RESTRICTIONS_T
alter table MFR_OPEN_FORUM_T add constraint FKC1760847B88980FA foreign key (ACTOR_PERMISSIONS) references MFR_ACTOR_PERSMISSIONS_T
create index MFR_PARENT_BASEFORUM_I on MFR_PRIVATE_FORUM_T (surrogateKey)
alter table MFR_PRIVATE_FORUM_T add constraint FKA9EE57544FDCE067 foreign key (surrogateKey) references MFR_AREA_T
create index MFR_PARENT_I1 on MFR_MESSAGE_FORUMS_USER_T (apaSurrogateKey)
create index MFR_PARENT_I3 on MFR_MESSAGE_FORUMS_USER_T (apcSurrogateKey)
create index MFR_PARENT_I2 on MFR_MESSAGE_FORUMS_USER_T (apmSurrogateKey)
create index MFR_PARENT_I4 on MFR_MESSAGE_FORUMS_USER_T (mesSurrogateKey)
alter table MFR_MESSAGE_FORUMS_USER_T add constraint FKF3B8460F737F309B foreign key (apcSurrogateKey) references MFR_ACTOR_PERSMISSIONS_T
alter table MFR_MESSAGE_FORUMS_USER_T add constraint FKF3B8460FC49D71A5 foreign key (apmSurrogateKey) references MFR_ACTOR_PERSMISSIONS_T
alter table MFR_MESSAGE_FORUMS_USER_T add constraint FKF3B8460F96792399 foreign key (apaSurrogateKey) references MFR_ACTOR_PERSMISSIONS_T
alter table MFR_MESSAGE_FORUMS_USER_T add constraint FKF3B8460FDD70E9E2 foreign key (mesSurrogateKey) references MFR_MESSAGE_T
create sequence MFR_MESSAGE_S
create sequence MFR_ACTOR_PERSMISSIONS_S
create sequence MFR_AREA_S
create sequence MFR_PRIVATE_FORUM_S
create sequence MFR_CONTROL_PERSMISSIONS_S
create sequence MFR_TOPIC_S
create sequence MFR_LABEL_S
create sequence MFR_DATE_RESTRICTIONS_S
create sequence MFR_UNREAD_STATUS_S
create sequence MFR_MESSAGE_FORUMS_USER_S
create sequence MFR_ATTACHMENT_S
create sequence MFR_MESSAGE_PERSMISSIONS_S
create sequence MFR_OPEN_FORUM_S
