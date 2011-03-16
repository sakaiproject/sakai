
    create table PROFILE_COMPANY_PROFILES_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(99) not null,
        COMPANY_NAME varchar(255),
        COMPANY_DESCRIPTION text,
        COMPANY_WEB_ADDRESS varchar(255),
        primary key (ID)
    );

    create table PROFILE_EXTERNAL_INTEGRATION_T (
        USER_UUID varchar(99) not null,
        TWITTER_TOKEN varchar(255),
        TWITTER_SECRET varchar(255),
        primary key (USER_UUID)
    );

    create table PROFILE_FRIENDS_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(99) not null,
        FRIEND_UUID varchar(99) not null,
        RELATIONSHIP integer not null,
        REQUESTED_DATE datetime not null,
        CONFIRMED bit not null,
        CONFIRMED_DATE datetime,
        primary key (ID)
    );

    create table PROFILE_GALLERY_IMAGES_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(99) not null,
        RESOURCE_MAIN text not null,
        RESOURCE_THUMB text not null,
        DISPLAY_NAME varchar(255) not null,
        primary key (ID)
    );

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar(99) not null,
        URL_MAIN text not null,
        URL_THUMB text,
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_OFFICIAL_T (
        USER_UUID varchar(99) not null,
        URL text not null,
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(99) not null,
        RESOURCE_MAIN text not null,
        RESOURCE_THUMB text not null,
        IS_CURRENT bit not null,
        primary key (ID)
    );

    create table PROFILE_KUDOS_T (
        USER_UUID varchar(99) not null,
        SCORE integer not null,
        PERCENTAGE numeric(19,2) not null,
        DATE_ADDED datetime not null,
        primary key (USER_UUID)
    );

    create table PROFILE_MESSAGES_T (
        ID varchar(36) not null,
        FROM_UUID varchar(99) not null,
        MESSAGE_BODY text not null,
        MESSAGE_THREAD varchar(36) not null,
        DATE_POSTED datetime not null,
        primary key (ID)
    );

    create table PROFILE_MESSAGE_PARTICIPANTS_T (
        ID bigint not null auto_increment,
        MESSAGE_ID varchar(36) not null,
        PARTICIPANT_UUID varchar(99) not null,
        MESSAGE_READ bit not null,
        MESSAGE_DELETED bit not null,
        primary key (ID)
    );

    create table PROFILE_MESSAGE_THREADS_T (
        ID varchar(36) not null,
        SUBJECT varchar(255) not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar(99) not null,
        EMAIL_REQUEST bit not null,
        EMAIL_CONFIRM bit not null,
        EMAIL_MESSAGE_NEW bit not null,
        EMAIL_MESSAGE_REPLY bit not null,
        USE_OFFICIAL_IMAGE bit not null,
        SHOW_KUDOS bit not null,
        SHOW_GALLERY_FEED bit not null,
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar(99) not null,
        PROFILE_IMAGE integer not null,
        BASIC_INFO integer not null,
        CONTACT_INFO integer not null,
        BUSINESS_INFO integer not null,
        PERSONAL_INFO integer not null,
        BIRTH_YEAR bit not null,
        MY_FRIENDS integer not null,
        MY_STATUS integer not null,
        MY_PICTURES integer not null,
        MESSAGES integer not null,
        STAFF_INFO integer not null,
        STUDENT_INFO integer not null,
        SOCIAL_NETWORKING_INFO integer not null,
        MY_KUDOS integer not null,
        primary key (USER_UUID)
    );

    create table PROFILE_SOCIAL_INFO_T (
        USER_UUID varchar(99) not null,
        FACEBOOK_URL varchar(255),
        LINKEDIN_URL varchar(255),
        MYSPACE_URL varchar(255),
        SKYPE_USERNAME varchar(255),
        TWITTER_URL varchar(255),
        primary key (USER_UUID)
    );

    create table PROFILE_STATUS_T (
        USER_UUID varchar(99) not null,
        MESSAGE varchar(255) not null,
        DATE_ADDED datetime not null,
        primary key (USER_UUID)
    );

    create table SAKAI_PERSON_META_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(99) not null,
        PROPERTY varchar(255) not null,
        VALUE varchar(255) not null,
        primary key (ID)
    );

	create index PROFILE_CP_USER_UUID_I on PROFILE_COMPANY_PROFILES_T (USER_UUID);

    create index PROFILE_FRIENDS_FRIEND_UUID_I on PROFILE_FRIENDS_T (FRIEND_UUID);

    create index PROFILE_FRIENDS_USER_UUID_I on PROFILE_FRIENDS_T (USER_UUID);

    create index PROFILE_GI_USER_UUID_I on PROFILE_GALLERY_IMAGES_T (USER_UUID);

    create index PROFILE_IMAGES_USER_UUID_I on PROFILE_IMAGES_T (USER_UUID);

    create index PROFILE_IMAGES_IS_CURRENT_I on PROFILE_IMAGES_T (IS_CURRENT);

    create index PROFILE_M_THREAD_I on PROFILE_MESSAGES_T (MESSAGE_THREAD);

    create index PROFILE_M_DATE_POSTED_I on PROFILE_MESSAGES_T (DATE_POSTED);

    create index PROFILE_M_FROM_UUID_I on PROFILE_MESSAGES_T (FROM_UUID);

    create index PROFILE_M_P_UUID_I on PROFILE_MESSAGE_PARTICIPANTS_T (PARTICIPANT_UUID);

    create index PROFILE_M_P_MESSAGE_ID_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_ID);

    create index PROFILE_M_P_DELETED_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_DELETED);

    create index PROFILE_M_P_READ_I on PROFILE_MESSAGE_PARTICIPANTS_T (MESSAGE_READ);

    create index SAKAI_PERSON_META_USER_UUID_I on SAKAI_PERSON_META_T (USER_UUID);

    create index SAKAI_PERSON_META_PROPERTY_I on SAKAI_PERSON_META_T (PROPERTY);
