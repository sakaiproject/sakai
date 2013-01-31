alter table lesson_builder_items add groupOwned bit;
alter table lesson_builder_items add ownerGroups text;
alter table lesson_builder_items add attributeString text;
alter table lesson_builder_pages add groupid varchar(36);
    create table lesson_builder_qr_totals (
        id bigint not null auto_increment,
        questionId bigint,
        responseId bigint,
        count bigint,
        primary key (id)
    );

    create table lesson_builder_question_responses (
        id bigint not null auto_increment,
        timeAnswered datetime not null,
        questionId bigint not null,
        userId varchar(255) not null,
        correct bit not null,
        shortanswer text,
        multipleChoiceId bigint,
        originalText text,
        overridden bit not null,
        points double precision,
        primary key (id)
    );

alter table lesson_builder_student_pages add groupid varchar(36);
