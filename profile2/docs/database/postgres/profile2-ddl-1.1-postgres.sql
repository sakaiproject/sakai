
    create table PROFILE_FRIENDS_T (
        ID int8 not null,
        USER_UUID varchar(36) not null,
        FRIEND_UUID varchar(36) not null,
        RELATIONSHIP int4 not null,
        REQUESTED_DATE timestamp not null,
        CONFIRMED bool not null,
        CONFIRMED_DATE timestamp,
        primary key (ID)
    );

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar(36) not null,
        URL_MAIN varchar(4000) not null,
        URL_THUMB varchar(4000),
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID int8 not null,
        USER_UUID varchar(36) not null,
        RESOURCE_MAIN varchar(255) not null,
        RESOURCE_THUMB varchar(255) not null,
        IS_CURRENT bool not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar(36) not null,
        EMAIL_REQUEST bool not null,
        EMAIL_CONFIRM bool not null,
        TWITTER_ENABLED bool not null,
        TWITTER_USERNAME varchar(255),
        TWITTER_PASSWORD varchar(255),
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar(36) not null,
        PROFILE_IMAGE int4 not null,
        BASIC_INFO int4 not null,
        CONTACT_INFO int4 not null,
        PERSONAL_INFO int4 not null,
        BIRTH_YEAR bool not null,
        SEARCH int4 not null,
        MY_FRIENDS int4 not null,
        MY_STATUS int4 not null,
        primary key (USER_UUID)
    );

    create table PROFILE_STATUS_T (
        USER_UUID varchar(36) not null,
        MESSAGE varchar(255) not null,
        DATE_ADDED timestamp not null,
        primary key (USER_UUID)
    );

    create table SAKAI_PERSON_META_T (
        ID int8 not null,
        USER_UUID int8 not null,
        PROPERTY varchar(255) not null,
        VALUE varchar(255) not null,
        primary key (ID)
    );

    create sequence PROFILE_FRIENDS_S;

    create sequence PROFILE_IMAGES_S;

    create sequence SAKAI_PERSON_META_S;
