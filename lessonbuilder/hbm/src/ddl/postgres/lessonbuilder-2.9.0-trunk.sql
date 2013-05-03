alter table lesson_builder_items add showPeerEval bool;
alter table lesson_builder_items add groupOwned bool;
alter table lesson_builder_items add ownerGroups varchar(4000);
alter table lesson_builder_items add attributeString text;
alter table lesson_builder_pages add groupid varchar(36);
    create table lesson_builder_peer_eval_results (
        PEER_EVAL_RESULT_ID int8 not null,
        PAGE_ID int8 not null,
        TIME_POSTED timestamp,
        GRADER varchar(255) not null,
        GRADEE varchar(255) not null,
        ROW_TEXT varchar(255) not null,
        COLUMN_VALUE int4 not null,
        SELECTED bool,
        primary key (PEER_EVAL_RESULT_ID)
    );

    create table lesson_builder_q_responses (
        id int8 not null,
        timeAnswered timestamp not null,
        questionId int8 not null,
        userId varchar(255) not null,
        correct bool not null,
        shortanswer text,
        multipleChoiceId int8,
        originalText text,
        overridden bool not null,
        points float8,
        primary key (id)
    );

    create table lesson_builder_qr_totals (
        id int8 not null,
        questionId int8,
        responseId int8,
        respcount int8,
        primary key (id)
    );

alter table lesson_builder_student_pages add groupid varchar(36);
alter table lesson_builder_student_pages add create sequence hibernate_sequence;;
