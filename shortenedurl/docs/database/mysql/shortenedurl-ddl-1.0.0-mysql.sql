
    create table URL_RANDOMISED_MAPPINGS_T (
        ID bigint not null auto_increment,
        TINY varchar(255) not null,
        URL text not null,
        primary key (ID)
    );

    create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL(200));

    create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);
