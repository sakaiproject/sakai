alter table lesson_builder_items add groupOwned smallint;
alter table lesson_builder_items add ownerGroups varchar(4000);
alter table lesson_builder_items add attributeString clob(255);
alter table lesson_builder_pages add groupid varchar(36);
    create table lesson_builder_qr_totals (
        id bigint not null,
        questionId bigint,
        responseId bigint,
        count bigint,
        primary key (id)
    );

    create table lesson_builder_question_responses (
        id bigint not null,
        timeAnswered timestamp not null,
        questionId bigint not null,
        userId varchar(255) not null,
        correct smallint not null,
        shortanswer clob(255),
        multipleChoiceId bigint,
        originalText clob(255),
        overridden smallint not null,
        points double,
        primary key (id)
    );

alter table lesson_builder_student_pages add groupid varchar(36);
