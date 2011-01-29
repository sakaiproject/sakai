
    create table SimplePageTool (
        id numeric(19,0) identity not null,
        pageId varchar(250) null,
        sequence int null,
        sakaiId varchar(250) null,
        primary key (id)
    );
