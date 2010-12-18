
    create table SimplePageTool (
        id int8 not null,
        pageId varchar(250),
        sequence int4,
        sakaiId varchar(250),
        primary key (id)
    );

    create sequence hibernate_sequence;
