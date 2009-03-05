
    create table PROFILE_FRIENDS_T (
        ID numeric(19,0) identity not null,
        USER_UUID varchar(36) not null,
        FRIEND_UUID varchar(36) not null,
        RELATIONSHIP int not null,
        REQUESTED_DATE datetime not null,
        CONFIRMED tinyint not null,
        CONFIRMED_DATE datetime null,
        primary key (ID)
    );

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar(36) not null,
        RESOURCE_MAIN varchar(4000) not null,
        RESOURCE_THUMB varchar(4000) null,
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID numeric(19,0) identity not null,
        USER_UUID varchar(36) not null,
        RESOURCE_MAIN varchar(255) not null,
        RESOURCE_THUMB varchar(255) not null,
        IS_CURRENT tinyint not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar(36) not null,
        EMAIL_REQUEST tinyint not null,
        EMAIL_CONFIRM tinyint not null,
        TWITTER_ENABLED tinyint not null,
        TWITTER_USERNAME varchar(255) null,
        TWITTER_PASSWORD varchar(255) null,
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar(36) not null,
        PROFILE_IMAGE int not null,
        BASIC_INFO int not null,
        CONTACT_INFO int not null,
        PERSONAL_INFO int not null,
        BIRTH_YEAR tinyint not null,
        SEARCH int not null,
        MY_FRIENDS int not null,
        primary key (USER_UUID)
    );

    create table PROFILE_STATUS_T (
        USER_UUID varchar(36) not null,
        MESSAGE varchar(255) not null,
        DATE_ADDED datetime not null,
        primary key (USER_UUID)
    );

    create table SAKAI_PERSON_META_T (
        ID numeric(19,0) identity not null,
        USER_UUID numeric(19,0) not null,
        PROPERTY varchar(255) not null,
        VALUE varchar(255) not null,
        primary key (ID)
    );
