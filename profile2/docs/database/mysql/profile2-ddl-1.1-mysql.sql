
    create table PROFILE_FRIENDS_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(36) not null,
        FRIEND_UUID varchar(36) not null,
        RELATIONSHIP integer not null,
        REQUESTED_DATE datetime not null,
        CONFIRMED bit not null,
        CONFIRMED_DATE datetime,
        primary key (ID)
    );

    create table PROFILE_IMAGES_EXTERNAL_T (
        USER_UUID varchar(36) not null,
        URL_MAIN text not null,
        URL_THUMB text,
        primary key (USER_UUID)
    );

    create table PROFILE_IMAGES_T (
        ID bigint not null auto_increment,
        USER_UUID varchar(36) not null,
        RESOURCE_MAIN varchar(255) not null,
        RESOURCE_THUMB varchar(255) not null,
        IS_CURRENT bit not null,
        primary key (ID)
    );

    create table PROFILE_PREFERENCES_T (
        USER_UUID varchar(36) not null,
        EMAIL_REQUEST bit not null,
        EMAIL_CONFIRM bit not null,
        TWITTER_ENABLED bit not null,
        TWITTER_USERNAME varchar(255),
        TWITTER_PASSWORD varchar(255),
        primary key (USER_UUID)
    );

    create table PROFILE_PRIVACY_T (
        USER_UUID varchar(36) not null,
        PROFILE_IMAGE integer not null,
        BASIC_INFO integer not null,
        CONTACT_INFO integer not null,
        PERSONAL_INFO integer not null,
        BIRTH_YEAR bit not null,
        SEARCH integer not null,
        MY_FRIENDS integer not null,
        MY_STATUS integer not null,
        primary key (USER_UUID)
    );

    create table PROFILE_STATUS_T (
        USER_UUID varchar(36) not null,
        MESSAGE varchar(255) not null,
        DATE_ADDED datetime not null,
        primary key (USER_UUID)
    );

    create table SAKAI_PERSON_META_T (
        ID bigint not null auto_increment,
        USER_UUID bigint not null,
        PROPERTY varchar(255) not null,
        VALUE varchar(255) not null,
        primary key (ID)
    );
