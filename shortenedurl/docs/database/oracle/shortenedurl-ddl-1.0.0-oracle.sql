
    create table URL_RANDOMISED_MAPPINGS_T (
        ID number(19,0) not null,
        TINY varchar2(255) not null,
        URL varchar2(4000) not null,
        primary key (ID)
    );

    create index URL_INDEX on URL_RANDOMISED_MAPPINGS_T (URL);

    create index KEY_INDEX on URL_RANDOMISED_MAPPINGS_T (TINY);

    create sequence URL_RANDOMISED_MAPPINGS_S;
