alter table lesson_builder_groups add siteId varchar(250);
alter table lesson_builder_items add showPeerEval bit;
alter table lesson_builder_items add groupOwned bit;
alter table lesson_builder_items add ownerGroups longtext;
alter table lesson_builder_items add attributeString longtext;
    create table lesson_builder_p_eval_results (
        PEER_EVAL_RESULT_ID bigint not null auto_increment,
        PAGE_ID bigint not null,
        TIME_POSTED datetime,
        GRADER varchar(255) not null,
        GRADEE varchar(255) not null,
        ROW_TEXT varchar(255) not null,
        COLUMN_VALUE integer not null,
        SELECTED bit,
        primary key (PEER_EVAL_RESULT_ID)
    );

alter table lesson_builder_pages add groupid varchar(36);
    create table lesson_builder_properties (
        id bigint not null auto_increment,
        attribute varchar(255) not null unique,
        value longtext,
        primary key (id)
    );

    create table lesson_builder_q_responses (
        id bigint not null auto_increment,
        timeAnswered datetime not null,
        questionId bigint not null,
        userId varchar(255) not null,
        correct bit not null,
        shortanswer longtext,
        multipleChoiceId bigint,
        originalText longtext,
        overridden bit not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_qr_totals (
        id bigint not null auto_increment,
        questionId bigint,
        responseId bigint,
        respcount bigint,
        primary key (id)
    );

alter table lesson_builder_student_pages add groupid varchar(36);
