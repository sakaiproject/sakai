CREATE TABLE SOCIAL_ALERTS
(
    ID bigint NOT NULL AUTO_INCREMENT,
    FROM_USER varchar(99) NOT NULL,
    TO_USER varchar(99) NOT NULL,
    EVENT varchar(32) NOT NULL,
    REF varchar(255) NOT NULL,
    TITLE varchar(255),
    SITE_ID varchar(99),
    URL TEXT NOT NULL,
    EVENT_DATE datetime NOT NULL,
    PRIMARY KEY(ID)
);

CREATE TABLE ACADEMIC_ALERTS
(
    ID bigint NOT NULL AUTO_INCREMENT,
    FROM_USER varchar(99) NOT NULL,
    TO_USER varchar(99) NOT NULL,
    EVENT varchar(32) NOT NULL,
    REF varchar(255) NOT NULL,
    TITLE varchar(255) NOT NULL,
    SITE_ID varchar(99),
    URL TEXT NOT NULL,
    EVENT_DATE datetime NOT NULL,
    PRIMARY KEY(ID)
);
