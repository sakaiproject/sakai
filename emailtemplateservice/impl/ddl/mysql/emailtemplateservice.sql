
    create table EMAIL_TEMPLATE_ITEM (
        ID bigint not null auto_increment,
        LAST_MODIFIED datetime not null,
        OWNER varchar(255) not null,
        SUBJECT text not null,
        MESSAGE text not null,
        TEMPLATE_KEY varchar(255) not null,
        TEMPLATE_LOCALE varchar(255),
        defaultType varchar(255),
        primary key (ID)
    );

    create index email_templ_owner on EMAIL_TEMPLATE_ITEM (OWNER);

    create index email_templ_key on EMAIL_TEMPLATE_ITEM (TEMPLATE_KEY);
