alter table lesson_builder_items add groupOwned bool;
alter table lesson_builder_items add ownerGroups varchar(4000);
alter table lesson_builder_items add attributeString text;
alter table lesson_builder_pages add groupid varchar(36);
    create table lesson_builder_qr_totals (
        id int8 not null,
        questionId int8,
        responseId int8,
        count int8,
        primary key (id)
    );

    create table lesson_builder_question_responses (
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

alter table lesson_builder_student_pages add groupid varchar(36);
