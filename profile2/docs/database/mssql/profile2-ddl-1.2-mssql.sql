
    create table PROFILE_FRIENDS_T (
        ID numeric(19,0) identity not null,
        USER_UUID varchar(99) not null,
        FRIEND_UUID varchar(99) not null,
        RELATIONSHIP int not null,
        REQUESTED_DATE datetime not null,
        CONFIRMED tinyint not null,
        CONFIRMED_DATE datetime null,
        primary key (ID)
    );

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar(99) not null,
        URL_MAIN varchar(4000) not null,
        URL_THUMB varchar(4000) null,
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID numeric(19,0) identity not null,
        USER_UUID varchar(99) not null,
        RESOURCE_MAIN varchar(255) not null,
        RESOURCE_THUMB varchar(255) not null,
        IS_CURRENT tinyint not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar(99) not null,
        EMAIL_REQUEST tinyint not null,
        EMAIL_CONFIRM tinyint not null,
        TWITTER_ENABLED tinyint not null,
        TWITTER_USERNAME varchar(255) null,
        TWITTER_PASSWORD varchar(255) null,
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar(99) not null,
        PROFILE_IMAGE int not null,
        BASIC_INFO int not null,
        CONTACT_INFO int not null,
        ACADEMIC_INFO int not null,
        PERSONAL_INFO int not null,
        BIRTH_YEAR tinyint not null,
        SEARCH int not null,
        MY_FRIENDS int not null,
        MY_STATUS int not null,
        primary key (USER_UUID)
    );

    create table PROFILE_STATUS_T (
        USER_UUID varchar(99) not null,
        MESSAGE varchar(255) not null,
        DATE_ADDED datetime not null,
        primary key (USER_UUID)
    );

    create table SAKAI_PERSON_META_T (
        ID numeric(19,0) identity not null,
        USER_UUID varchar(99) not null,
        PROPERTY varchar(255) not null,
        VALUE varchar(255) not null,
        primary key (ID)
    );

    create index PROFILE_FRIENDS_USER_UUID_I on PROFILE_FRIENDS_T (USER_UUID);

    create index PROFILE_FRIENDS_FRIEND_UUID_I on PROFILE_FRIENDS_T (FRIEND_UUID);

    create index PROFILE_IMAGES_USER_UUID_I on PROFILE_IMAGES_T (USER_UUID);

    create index PROFILE_IMAGES_IS_CURRENT_I on PROFILE_IMAGES_T (IS_CURRENT);

    create index SAKAI_PERSON_META_USER_UUID_I on SAKAI_PERSON_META_T (USER_UUID);

    create index SAKAI_PERSON_META_PROPERTY_I on SAKAI_PERSON_META_T (PROPERTY);
