
    create table SimplePageTool (
        id number(19,0) not null,
        pageId varchar2(250),
        sequence number(10,0),
        sakaiId varchar2(250),
        primary key (id)
    );

    create sequence hibernate_sequence;
