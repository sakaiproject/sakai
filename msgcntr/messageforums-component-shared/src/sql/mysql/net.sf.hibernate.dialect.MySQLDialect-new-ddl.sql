alter table MFR_LABEL_T drop foreign key FKC6611543EA902104;
alter table MFR_LABEL_T drop foreign key FKC661154344B127B6;
alter table MFR_AP_ACCESSORS_T drop foreign key FKC8532ED796792399;
alter table MFR_AP_ACCESSORS_T drop foreign key FKC8532ED721BCC7D2;
alter table MFR_AP_MODERATORS_T drop foreign key FK75B43C0D21BCC7D2;
alter table MFR_AP_MODERATORS_T drop foreign key FK75B43C0DC49D71A5;
alter table MFR_TOPIC_T drop foreign key FK863DC0BEC6FDB1CF;
alter table MFR_TOPIC_T drop foreign key FK863DC0BE7AFA22C2;
alter table MFR_TOPIC_T drop foreign key FK863DC0BE20D91C10;
alter table MFR_MESSAGE_PERMISSIONS_T drop foreign key FK750F9AFB17721828;
alter table MFR_MESSAGE_PERMISSIONS_T drop foreign key FK750F9AFB51C89994;
alter table MFR_MESSAGE_PERMISSIONS_T drop foreign key FK750F9AFBE581B336;
alter table MFR_AP_CONTRIBUTORS_T drop foreign key FKA221A1F7737F309B;
alter table MFR_AP_CONTRIBUTORS_T drop foreign key FKA221A1F721BCC7D2;
alter table MFR_MESSAGE_T drop foreign key FK80C1A316FE0789EA;
alter table MFR_MESSAGE_T drop foreign key FK80C1A3164FDCE067;
alter table MFR_PVT_MSG_USR_T drop foreign key FKC4DE0E14FA8620E;
alter table MFR_ATTACHMENT_T drop foreign key FK7B2D5CDE2AFBA652;
alter table MFR_ATTACHMENT_T drop foreign key FK7B2D5CDEC6FDB1CF;
alter table MFR_ATTACHMENT_T drop foreign key FK7B2D5CDE20D91C10;
alter table MFR_ATTACHMENT_T drop foreign key FK7B2D5CDEFDEB22F9;
alter table MFR_ATTACHMENT_T drop foreign key FK7B2D5CDEAD5AF852;
alter table MFR_MEMBERSHIP_ITEM_T drop foreign key FKE03761CB6785AF85;
alter table MFR_MEMBERSHIP_ITEM_T drop foreign key FKE03761CBC6FDB1CF;
alter table MFR_MEMBERSHIP_ITEM_T drop foreign key FKE03761CB2AFBA652;
alter table MFR_MEMBERSHIP_ITEM_T drop foreign key FKE03761CB925CE0F4;
alter table MFR_CONTROL_PERMISSIONS_T drop foreign key FKA07CF1D1E581B336;
alter table MFR_CONTROL_PERMISSIONS_T drop foreign key FKA07CF1D151C89994;
alter table MFR_CONTROL_PERMISSIONS_T drop foreign key FKA07CF1D117721828;
alter table MFR_PRIVATE_FORUM_T drop foreign key FKA9EE57544FDCE067;
alter table MFR_OPEN_FORUM_T drop foreign key FKC17608474FDCE067;
drop table if exists MFR_LABEL_T;
drop table if exists MFR_UNREAD_STATUS_T;
drop table if exists MFR_AP_ACCESSORS_T;
drop table if exists MFR_AP_MODERATORS_T;
drop table if exists MFR_TOPIC_T;
drop table if exists MFR_MESSAGE_PERMISSIONS_T;
drop table if exists MFR_PERMISSION_LEVEL_T;
drop table if exists MFR_AP_CONTRIBUTORS_T;
drop table if exists MFR_MESSAGE_T;
drop table if exists MFR_AREA_T;
drop table if exists MFR_PVT_MSG_USR_T;
drop table if exists MFR_ATTACHMENT_T;
drop table if exists MFR_ACTOR_PERMISSIONS_T;
drop table if exists MFR_MEMBERSHIP_ITEM_T;
drop table if exists MFR_CONTROL_PERMISSIONS_T;
drop table if exists MFR_PRIVATE_FORUM_T;
drop table if exists MFR_OPEN_FORUM_T;
drop table if exists MFR_MESSAGE_FORUMS_USER_T;
drop table if exists MFR_DATE_RESTRICTIONS_T;
create table MFR_LABEL_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   KEY_C varchar(255) not null,
   VALUE_C varchar(255) not null,
   df_surrogateKey bigint,
   df_index_col integer,
   dt_surrogateKey bigint,
   dt_index_col integer,
   primary key (ID)
);
create table MFR_UNREAD_STATUS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   TOPIC_C bigint not null,
   MESSAGE_C bigint not null,
   USER_C varchar(255) not null,
   READ_C bit not null,
   primary key (ID)
);
create table MFR_AP_ACCESSORS_T (
   apaSurrogateKey bigint not null,
   userSurrogateKey bigint not null,
   accessors_index_col integer not null,
   primary key (apaSurrogateKey, accessors_index_col)
);
create table MFR_AP_MODERATORS_T (
   apmSurrogateKey bigint not null,
   userSurrogateKey bigint not null,
   moderators_index_col integer not null,
   primary key (apmSurrogateKey, moderators_index_col)
);
create table MFR_TOPIC_T (
   ID bigint not null auto_increment,
   TOPIC_DTYPE varchar(2) not null,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   DEFAULTASSIGNNAME varchar(255),
   TITLE varchar(255) not null,
   SHORT_DESCRIPTION varchar(255),
   EXTENDED_DESCRIPTION text,
   MUTABLE bit not null,
   SORT_INDEX integer not null,
   TYPE_UUID varchar(36) not null,
   of_surrogateKey bigint,
   pf_surrogateKey bigint,
   USER_ID varchar(255),
   CONTEXT_ID varchar(36),
   pt_surrogateKey bigint,
   LOCKED bit,
   DRAFT bit,
   CONFIDENTIAL_RESPONSES bit,
   MUST_RESPOND_BEFORE_READING bit,
   HOUR_BEFORE_RESPONSES_VISIBLE integer,
   MODERATED bit,
   GRADEBOOK varchar(255),
   GRADEBOOK_ASSIGNMENT varchar(255),
   primary key (ID)
);
create table MFR_MESSAGE_PERMISSIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   ROLE_C varchar(255) not null,
   READ_C bit not null,
   REVISE_ANY bit not null,
   REVISE_OWN bit not null,
   DELETE_ANY bit not null,
   DELETE_OWN bit not null,
   READ_DRAFTS bit not null,
   MARK_AS_READ bit not null,
   DEFAULT_VALUE bit not null,
   areaSurrogateKey bigint,
   forumSurrogateKey bigint,
   topicSurrogateKey bigint,
   primary key (ID)
);
create table MFR_PERMISSION_LEVEL_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(255) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(255) not null,
   NAME varchar(50) not null,
   TYPE_UUID varchar(36) not null,
   CHANGE_SETTINGS bit not null,
   DELETE_ANY bit not null,
   DELETE_OWN bit not null,
   MARK_AS_READ bit not null,
   MOVE_POSTING bit not null,
   NEW_FORUM bit not null,
   NEW_RESPONSE bit not null,
   NEW_RESPONSE_TO_RESPONSE bit not null,
   NEW_TOPIC bit not null,
   POST_TO_GRADEBOOK bit not null,
   X_READ bit not null,
   REVISE_ANY bit not null,
   REVISE_OWN bit not null,
   MODERATE_POSTINGS bit not null,
   primary key (ID)
);
create table MFR_AP_CONTRIBUTORS_T (
   apcSurrogateKey bigint not null,
   userSurrogateKey bigint not null,
   contributors_index_col integer not null,
   primary key (apcSurrogateKey, contributors_index_col)
);
create table MFR_MESSAGE_T (
   ID bigint not null auto_increment,
   MESSAGE_DTYPE varchar(2) not null,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   TITLE varchar(255) not null,
   BODY text,
   AUTHOR varchar(255) not null,
   HAS_ATTACHMENTS bit not null,
   GRADECOMMENT text,
   GRADEASSIGNMENTNAME varchar(255),
   LABEL varchar(255),
   IN_REPLY_TO bigint,
   GRADEBOOK varchar(255),
   GRADEBOOK_ASSIGNMENT varchar(255),
   TYPE_UUID varchar(36) not null,
   APPROVED bit not null,
   DRAFT bit not null,
   surrogateKey bigint,
   EXTERNAL_EMAIL bit,
   EXTERNAL_EMAIL_ADDRESS varchar(255),
   RECIPIENTS_AS_TEXT text,
   primary key (ID)
);
create table MFR_AREA_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   CONTEXT_ID varchar(255) not null,
   NAME varchar(255) not null,
   HIDDEN bit not null,
   TYPE_UUID varchar(36) not null,
   ENABLED bit not null,
   LOCKED bit not null,
   primary key (ID)
);
create table MFR_PVT_MSG_USR_T (
   messageSurrogateKey bigint not null,
   USER_ID varchar(255) not null,
   TYPE_UUID varchar(255) not null,
   CONTEXT_ID varchar(255) not null,
   READ_STATUS bit not null,
   user_index_col integer not null,
   primary key (messageSurrogateKey, user_index_col)
);
create table MFR_ATTACHMENT_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(255) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(255) not null,
   ATTACHMENT_ID varchar(255) not null,
   ATTACHMENT_URL varchar(255) not null,
   ATTACHMENT_NAME varchar(255) not null,
   ATTACHMENT_SIZE varchar(255) not null,
   ATTACHMENT_TYPE varchar(255) not null,
   m_surrogateKey bigint,
   of_surrogateKey bigint,
   pf_surrogateKey bigint,
   t_surrogateKey bigint,
   of_urrogateKey bigint,
   primary key (ID)
);
create table MFR_ACTOR_PERMISSIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   primary key (ID)
);
create table MFR_MEMBERSHIP_ITEM_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(255) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(255) not null,
   NAME varchar(255) not null,
   TYPE integer not null,
   PERMISSION_LEVEL_NAME varchar(255) not null,
   PERMISSION_LEVEL bigint unique,
   a_surrogateKey bigint,
   of_surrogateKey bigint,
   t_surrogateKey bigint,
   primary key (ID)
);
create table MFR_CONTROL_PERMISSIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   ROLE varchar(255) not null,
   NEW_FORUM bit not null,
   POST_TO_GRADEBOOK bit not null,
   NEW_TOPIC bit not null,
   NEW_RESPONSE bit not null,
   RESPONSE_TO_RESPONSE bit not null,
   MOVE_POSTINGS bit not null,
   CHANGE_SETTINGS bit not null,
   DEFAULT_VALUE bit not null,
   areaSurrogateKey bigint,
   forumSurrogateKey bigint,
   topicSurrogateKey bigint,
   primary key (ID)
);
create table MFR_PRIVATE_FORUM_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   TITLE varchar(255) not null,
   SHORT_DESCRIPTION varchar(255),
   EXTENDED_DESCRIPTION text,
   TYPE_UUID varchar(36) not null,
   SORT_INDEX integer not null,
   OWNER varchar(255) not null,
   AUTO_FORWARD bit,
   AUTO_FORWARD_EMAIL varchar(255),
   PREVIEW_PANE_ENABLED bit,
   surrogateKey bigint,
   primary key (ID)
);
create table MFR_OPEN_FORUM_T (
   ID bigint not null auto_increment,
   FORUM_DTYPE varchar(2) not null,
   VERSION integer not null,
   UUID varchar(36) not null,
   CREATED datetime not null,
   CREATED_BY varchar(36) not null,
   MODIFIED datetime not null,
   MODIFIED_BY varchar(36) not null,
   DEFAULTASSIGNNAME varchar(255),
   TITLE varchar(255) not null,
   SHORT_DESCRIPTION varchar(255),
   EXTENDED_DESCRIPTION text,
   TYPE_UUID varchar(36) not null,
   SORT_INDEX integer not null,
   LOCKED bit not null,
   DRAFT bit,
   surrogateKey bigint,
   MODERATED bit,
   primary key (ID)
);
create table MFR_MESSAGE_FORUMS_USER_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   UUID varchar(36) not null,
   USER_ID varchar(255) not null,
   TYPE_UUID varchar(36) not null,
   primary key (ID)
);
create table MFR_DATE_RESTRICTIONS_T (
   ID bigint not null auto_increment,
   VERSION integer not null,
   VISIBLE datetime not null,
   VISIBLE_POST_ON_SCHEDULE bit not null,
   POSTING_ALLOWED datetime not null,
   PSTNG_ALLWD_PST_ON_SCHD bit not null,
   READ_ONLY datetime not null,
   READ_ONLY_POST_ON_SCHEDULE bit not null,
   HIDDEN datetime not null,
   HIDDEN_POST_ON_SCHEDULE bit not null,
   primary key (ID)
);
create index MFR_LABEL_PARENT_I2 on MFR_LABEL_T (dt_surrogateKey);
create index MFR_LABEL_PARENT_I1 on MFR_LABEL_T (df_surrogateKey);
alter table MFR_LABEL_T add index FKC6611543EA902104 (df_surrogateKey), add constraint FKC6611543EA902104 foreign key (df_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_LABEL_T add index FKC661154344B127B6 (dt_surrogateKey), add constraint FKC661154344B127B6 foreign key (dt_surrogateKey) references MFR_TOPIC_T (ID);
create index MFR_UNREAD_STATUS_I1 on MFR_UNREAD_STATUS_T (TOPIC_C, MESSAGE_C, USER_C, READ_C);
alter table MFR_AP_ACCESSORS_T add index FKC8532ED796792399 (apaSurrogateKey), add constraint FKC8532ED796792399 foreign key (apaSurrogateKey) references MFR_ACTOR_PERMISSIONS_T (ID);
alter table MFR_AP_ACCESSORS_T add index FKC8532ED721BCC7D2 (userSurrogateKey), add constraint FKC8532ED721BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T (ID);
alter table MFR_AP_MODERATORS_T add index FK75B43C0D21BCC7D2 (userSurrogateKey), add constraint FK75B43C0D21BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T (ID);
alter table MFR_AP_MODERATORS_T add index FK75B43C0DC49D71A5 (apmSurrogateKey), add constraint FK75B43C0DC49D71A5 foreign key (apmSurrogateKey) references MFR_ACTOR_PERMISSIONS_T (ID);
create index MFR_TOPIC_PARENT_I1 on MFR_TOPIC_T (of_surrogateKey);
create index MRF_TOPIC_DTYPE_I on MFR_TOPIC_T (TOPIC_DTYPE);
create index MFR_TOPIC_PRI_PARENT_I on MFR_TOPIC_T (pt_surrogateKey);
create index MFR_PT_CONTEXT_I on MFR_TOPIC_T (CONTEXT_ID);
create index MFR_TOPIC_PARENT_I2 on MFR_TOPIC_T (pf_surrogateKey);
create index MFR_TOPIC_CREATED_I on MFR_TOPIC_T (CREATED);
alter table MFR_TOPIC_T add index FK863DC0BEC6FDB1CF (of_surrogateKey), add constraint FK863DC0BEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_TOPIC_T add index FK863DC0BE7AFA22C2 (pt_surrogateKey), add constraint FK863DC0BE7AFA22C2 foreign key (pt_surrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_TOPIC_T add index FK863DC0BE20D91C10 (pf_surrogateKey), add constraint FK863DC0BE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T (ID);
create index MFR_MP_PARENT_FORUM_I on MFR_MESSAGE_PERMISSIONS_T (forumSurrogateKey);
create index MFR_MP_PARENT_AREA_I on MFR_MESSAGE_PERMISSIONS_T (areaSurrogateKey);
create index MFR_MP_PARENT_TOPIC_I on MFR_MESSAGE_PERMISSIONS_T (topicSurrogateKey);
alter table MFR_MESSAGE_PERMISSIONS_T add index FK750F9AFB17721828 (forumSurrogateKey), add constraint FK750F9AFB17721828 foreign key (forumSurrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_MESSAGE_PERMISSIONS_T add index FK750F9AFB51C89994 (areaSurrogateKey), add constraint FK750F9AFB51C89994 foreign key (areaSurrogateKey) references MFR_AREA_T (ID);
alter table MFR_MESSAGE_PERMISSIONS_T add index FK750F9AFBE581B336 (topicSurrogateKey), add constraint FK750F9AFBE581B336 foreign key (topicSurrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_AP_CONTRIBUTORS_T add index FKA221A1F7737F309B (apcSurrogateKey), add constraint FKA221A1F7737F309B foreign key (apcSurrogateKey) references MFR_ACTOR_PERMISSIONS_T (ID);
alter table MFR_AP_CONTRIBUTORS_T add index FKA221A1F721BCC7D2 (userSurrogateKey), add constraint FKA221A1F721BCC7D2 foreign key (userSurrogateKey) references MFR_MESSAGE_FORUMS_USER_T (ID);
create index MFR_MESSAGE_LABEL_I on MFR_MESSAGE_T (LABEL);
create index MFR_MESSAGE_HAS_ATTACHMENTS_I on MFR_MESSAGE_T (HAS_ATTACHMENTS);
create index MFR_MESSAGE_CREATED_I on MFR_MESSAGE_T (CREATED);
create index MFR_MESSAGE_AUTHOR_I on MFR_MESSAGE_T (AUTHOR);
create index MFR_MESSAGE_DTYPE_I on MFR_MESSAGE_T (MESSAGE_DTYPE);
create index MFR_MESSAGE_TITLE_I on MFR_MESSAGE_T (TITLE);
create index MFR_MESSAGE_PARENT_TOPIC_I on MFR_MESSAGE_T (surrogateKey);
alter table MFR_MESSAGE_T add index FK80C1A316FE0789EA (IN_REPLY_TO), add constraint FK80C1A316FE0789EA foreign key (IN_REPLY_TO) references MFR_MESSAGE_T (ID);
alter table MFR_MESSAGE_T add index FK80C1A3164FDCE067 (surrogateKey), add constraint FK80C1A3164FDCE067 foreign key (surrogateKey) references MFR_TOPIC_T (ID);
create index MFR_AREA_CONTEXT_I on MFR_AREA_T (CONTEXT_ID);
create index MFR_AREA_TYPE_I on MFR_AREA_T (TYPE_UUID);
create index MFR_PVT_MSG_USR_I1 on MFR_PVT_MSG_USR_T (USER_ID, TYPE_UUID, CONTEXT_ID, READ_STATUS);
alter table MFR_PVT_MSG_USR_T add index FKC4DE0E14FA8620E (messageSurrogateKey), add constraint FKC4DE0E14FA8620E foreign key (messageSurrogateKey) references MFR_MESSAGE_T (ID);
create index MFR_ATTACHMENT_PARENT_I4 on MFR_ATTACHMENT_T (t_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I on MFR_ATTACHMENT_T (m_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I3 on MFR_ATTACHMENT_T (pf_surrogateKey);
create index MFR_ATTACHMENT_PARENT_I2 on MFR_ATTACHMENT_T (of_surrogateKey);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDE2AFBA652 (t_surrogateKey), add constraint FK7B2D5CDE2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDEC6FDB1CF (of_surrogateKey), add constraint FK7B2D5CDEC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDE20D91C10 (pf_surrogateKey), add constraint FK7B2D5CDE20D91C10 foreign key (pf_surrogateKey) references MFR_PRIVATE_FORUM_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDEFDEB22F9 (m_surrogateKey), add constraint FK7B2D5CDEFDEB22F9 foreign key (m_surrogateKey) references MFR_MESSAGE_T (ID);
alter table MFR_ATTACHMENT_T add index FK7B2D5CDEAD5AF852 (of_urrogateKey), add constraint FK7B2D5CDEAD5AF852 foreign key (of_urrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CB6785AF85 (a_surrogateKey), add constraint FKE03761CB6785AF85 foreign key (a_surrogateKey) references MFR_AREA_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CBC6FDB1CF (of_surrogateKey), add constraint FKE03761CBC6FDB1CF foreign key (of_surrogateKey) references MFR_OPEN_FORUM_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CB2AFBA652 (t_surrogateKey), add constraint FKE03761CB2AFBA652 foreign key (t_surrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_MEMBERSHIP_ITEM_T add index FKE03761CB925CE0F4 (PERMISSION_LEVEL), add constraint FKE03761CB925CE0F4 foreign key (PERMISSION_LEVEL) references MFR_PERMISSION_LEVEL_T (ID);
create index MFR_CP_PARENT_FORUM_I on MFR_CONTROL_PERMISSIONS_T (forumSurrogateKey);
create index MFR_CP_PARENT_TOPIC_I on MFR_CONTROL_PERMISSIONS_T (topicSurrogateKey);
create index MFR_CP_PARENT_AREA_I on MFR_CONTROL_PERMISSIONS_T (areaSurrogateKey);
alter table MFR_CONTROL_PERMISSIONS_T add index FKA07CF1D1E581B336 (topicSurrogateKey), add constraint FKA07CF1D1E581B336 foreign key (topicSurrogateKey) references MFR_TOPIC_T (ID);
alter table MFR_CONTROL_PERMISSIONS_T add index FKA07CF1D151C89994 (areaSurrogateKey), add constraint FKA07CF1D151C89994 foreign key (areaSurrogateKey) references MFR_AREA_T (ID);
alter table MFR_CONTROL_PERMISSIONS_T add index FKA07CF1D117721828 (forumSurrogateKey), add constraint FKA07CF1D117721828 foreign key (forumSurrogateKey) references MFR_OPEN_FORUM_T (ID);
create index MFR_PRIVATE_FORUM_CREATED_I on MFR_PRIVATE_FORUM_T (CREATED);
create index MFR_PRIVATE_FORUM_OWNER_I on MFR_PRIVATE_FORUM_T (OWNER);
create index MFR_PF_PARENT_BASEFORUM_I on MFR_PRIVATE_FORUM_T (surrogateKey);
alter table MFR_PRIVATE_FORUM_T add index FKA9EE57544FDCE067 (surrogateKey), add constraint FKA9EE57544FDCE067 foreign key (surrogateKey) references MFR_AREA_T (ID);
create index MFR_OPEN_FORUM_DTYPE_I on MFR_OPEN_FORUM_T (FORUM_DTYPE);
create index MFR_OPEN_FORUM_TYPE_I on MFR_OPEN_FORUM_T (TYPE_UUID);
create index MFR_OF_PARENT_BASEFORUM_I on MFR_OPEN_FORUM_T (surrogateKey);
alter table MFR_OPEN_FORUM_T add index FKC17608474FDCE067 (surrogateKey), add constraint FKC17608474FDCE067 foreign key (surrogateKey) references MFR_AREA_T (ID);
