
    create table EMAIL_TEMPLATE_ITEM (
        ID number(19,0) not null,
        LAST_MODIFIED date not null,
        OWNER varchar2(255) not null,
        SUBJECT clob not null,
        MESSAGE clob not null,
        TEMPLATE_KEY varchar2(255) not null,
        TEMPLATE_LOCALE varchar2(255),
        defaultType varchar2(255),
        primary key (ID)
    );

    create index email_templ_owner on EMAIL_TEMPLATE_ITEM (OWNER);

    create index email_templ_key on EMAIL_TEMPLATE_ITEM (TEMPLATE_KEY);

    create sequence hibernate_sequence;
