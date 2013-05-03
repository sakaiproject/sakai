alter table lesson_builder_items add showPeerEval tinyint null;
alter table lesson_builder_items add groupOwned tinyint null;
alter table lesson_builder_items add ownerGroups varchar(4000) null;
alter table lesson_builder_items add attributeString text null;
alter table lesson_builder_pages add groupid varchar(36) null;
    create table lesson_builder_peer_eval_results (
        PEER_EVAL_RESULT_ID numeric(19,0) identity not null,
        PAGE_ID numeric(19,0) not null,
        TIME_POSTED datetime null,
        GRADER varchar(255) not null,
        GRADEE varchar(255) not null,
        ROW_TEXT varchar(255) not null,
        COLUMN_VALUE int not null,
        SELECTED tinyint null,
        primary key (PEER_EVAL_RESULT_ID)
    );

    create table lesson_builder_q_responses (
        id numeric(19,0) identity not null,
        timeAnswered datetime not null,
        questionId numeric(19,0) not null,
        userId varchar(255) not null,
        correct tinyint not null,
        shortanswer text null,
        multipleChoiceId numeric(19,0) null,
        originalText text null,
        overridden tinyint not null,
        points double precision null,
        primary key (id)
    );

    create table lesson_builder_qr_totals (
        id numeric(19,0) identity not null,
        questionId numeric(19,0) null,
        responseId numeric(19,0) null,
        respcount numeric(19,0) null,
        primary key (id)
    );

alter table lesson_builder_student_pages add groupid varchar(36) null;
