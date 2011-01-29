
    create table SimplePageTool (
        id bigint not null,
        pageId varchar(250),
        sequence integer,
        sakaiId varchar(250),
        primary key (id)
    );

    create table hibernate_unique_key (
         next_hi integer 
    );

    insert into hibernate_unique_key values ( 0 );
