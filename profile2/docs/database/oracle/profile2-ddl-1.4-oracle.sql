
    create table PROFILE_COMPANY_PROFILES_T (
        ID number(19,0) not null,
        USER_UUID varchar2(99) not null,
        COMPANY_NAME varchar2(255),
        COMPANY_DESCRIPTION varchar2(4000),
        COMPANY_WEB_ADDRESS varchar2(255),
        primary key (ID)
    );

    create table PROFILE_EXTERNAL_INTEGRATION_T (
        USER_UUID varchar2(99) not null,
        TWITTER_TOKEN varchar2(255),
        TWITTER_SECRET varchar2(255),
        primary key (USER_UUID)
    );

    create table PROFILE_FRIENDS_T (
        ID number(19,0) not null,
        USER_UUID varchar2(99) not null,
        FRIEND_UUID varchar2(99) not null,
        RELATIONSHIP number(10,0) not null,
        REQUESTED_DATE date not null,
        CONFIRMED number(1,0) not null,
        CONFIRMED_DATE date,
        primary key (ID)
    );

    create table PROFILE_GALLERY_IMAGES_T (
        ID number(19,0) not null,
        USER_UUID varchar2(99) not null,
        RESOURCE_MAIN varchar2(4000) not null,
        RESOURCE_THUMB varchar2(4000) not null,
        DISPLAY_NAME varchar2(255) not null,
        primary key (ID)
    );

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar2(99) not null,
        URL_MAIN varchar2(4000) not null,
        URL_THUMB varchar2(4000),
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_OFFICIAL_T (
        USER_UUID varchar2(99) not null,
        URL varchar2(4000) not null,
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID number(19,0) not null,
        USER_UUID varchar2(99) not null,
        RESOURCE_MAIN varchar2(4000) not null,
        RESOURCE_THUMB varchar2(4000) not null,
        IS_CURRENT number(1,0) not null,
        primary key (ID)
    );

    create table PROFILE_KUDOS_T (
        USER_UUID varchar2(99) not null,
        SCORE number(10,0) not null,
        PERCENTAGE number(19,2) not null,
        DATE_ADDED timestamp(6) not null,
        primary key (USER_UUID)
    );

    create table PROFILE_MESSAGES_T (
        ID varchar2(36) not null,
        FROM_UUID varchar2(99) not null,
        MESSAGE_BODY varchar2(4000) not null,
        MESSAGE_THREAD varchar2(36) not null,
        DATE_POSTED timestamp(6) not null,
        primary key (ID)
    );

    create table PROFILE_MESSAGE_PARTICIPANTS_T (
        ID number(19,0) not null,
        MESSAGE_ID varchar2(36) not null,
        PARTICIPANT_UUID varchar2(99) not null,
        MESSAGE_READ number(1,0) not null,
        MESSAGE_DELETED number(1,0) not null,
        primary key (ID)
    );

    create table PROFILE_MESSAGE_THREADS_T (
        ID varchar2(36) not null,
        SUBJECT varchar2(255) not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar2(99) not null,
        EMAIL_REQUEST number(1,0) not null,
        EMAIL_CONFIRM number(1,0) not null,
        EMAIL_MESSAGE_NEW number(1,0) not null,
        EMAIL_MESSAGE_REPLY number(1,0) not null,
        USE_OFFICIAL_IMAGE number(1,0) not null,
        SHOW_KUDOS number(1,0) not null,
        SHOW_GALLERY_FEED number(1,0) not null,
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar2(99) not null,
        PROFILE_IMAGE number(10,0) not null,
        BASIC_INFO number(10,0) not null,
        CONTACT_INFO number(10,0) not null,
        BUSINESS_INFO number(10,0) not null,
        PERSONAL_INFO number(10,0) not null,
        BIRTH_YEAR number(1,0) not null,
        MY_FRIENDS number(10,0) not null,
        MY_STATUS number(10,0) not null,
        MY_PICTURES number(10,0) not null,
        MESSAGES number(10,0) not null,
        STAFF_INFO number(10,0) not null,
        STUDENT_INFO number(10,0) not null,
        SOCIAL_NETWORKING_INFO number(10,0) not null,
        MY_KUDOS number(10,0) not null,
        primary key (USER_UUID)
    );

    create table PROFILE_SOCIAL_INFO_T (
        USER_UUID varchar2(99) not null,
        FACEBOOK_URL varchar2(255),
        LINKEDIN_URL varchar2(255),
        MYSPACE_URL varchar2(255),
        SKYPE_USERNAME varchar2(255),
        TWITTER_URL varchar2(255),
        primary key (USER_UUID)
    );

    create table PROFILE_STATUS_T (
        USER_UUID varchar2(99) not null,
        MESSAGE varchar2(255) not null,
        DATE_ADDED date not null,
        primary key (USER_UUID)
    );

    create table SAKAI_PERSON_META_T (
        ID number(19,0) not null,
        USER_UUID varchar2(99) not null,
        PROPERTY varchar2(255) not null,
        VALUE varchar2(255) not null,
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
