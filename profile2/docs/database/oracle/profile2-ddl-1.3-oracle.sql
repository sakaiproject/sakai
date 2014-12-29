
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

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar2(99) not null,
        URL_MAIN varchar2(4000) not null,
        URL_THUMB varchar2(4000),
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID number(19,0) not null,
        USER_UUID varchar2(99) not null,
        RESOURCE_MAIN varchar2(255) not null,
        RESOURCE_THUMB varchar2(255) not null,
        IS_CURRENT number(1,0) not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar2(99) not null,
        EMAIL_REQUEST number(1,0) not null,
        EMAIL_CONFIRM number(1,0) not null,
        TWITTER_ENABLED number(1,0) not null,
        TWITTER_USERNAME varchar2(255),
        TWITTER_PASSWORD varchar2(255),
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar2(99) not null,
        PROFILE_IMAGE number(10,0) not null,
        BASIC_INFO number(10,0) not null,
        CONTACT_INFO number(10,0) not null,
        ACADEMIC_INFO number(10,0) not null,
        PERSONAL_INFO number(10,0) not null,
        BIRTH_YEAR number(1,0) not null,
        SEARCH number(10,0) not null,
        MY_FRIENDS number(10,0) not null,
        MY_STATUS number(10,0) not null,
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

    create index PROFILE_FRIENDS_FRIEND_UUID_I on PROFILE_FRIENDS_T (FRIEND_UUID);

    create index PROFILE_FRIENDS_USER_UUID_I on PROFILE_FRIENDS_T (USER_UUID);

    create index PROFILE_IMAGES_USER_UUID_I on PROFILE_IMAGES_T (USER_UUID);

    create index PROFILE_IMAGES_IS_CURRENT_I on PROFILE_IMAGES_T (IS_CURRENT);

    create sequence PROFILE_FRIENDS_S;

    create sequence PROFILE_IMAGES_S;

    create sequence SAKAI_PERSON_META_S;

    create index SAKAI_PERSON_META_USER_UUID_I on SAKAI_PERSON_META_T (USER_UUID);

    create index SAKAI_PERSON_META_PROPERTY_I on SAKAI_PERSON_META_T (PROPERTY);
