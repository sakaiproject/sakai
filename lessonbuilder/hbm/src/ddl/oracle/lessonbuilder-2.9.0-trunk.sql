alter table lesson_builder_groups add siteId varchar2(99 char);
alter table lesson_builder_items add showPeerEval number(1,0);
alter table lesson_builder_items add groupOwned number(1,0);
alter table lesson_builder_items add ownerGroups clob;
alter table lesson_builder_items add attributeString clob;
    create table lesson_builder_p_eval_results (
        PEER_EVAL_RESULT_ID number(19,0) not null,
        PAGE_ID number(19,0) not null,
        TIME_POSTED timestamp,
        GRADER varchar2(99 char) not null,
        GRADEE varchar2(99 char) not null,
        ROW_TEXT varchar2(255 char) not null,
        COLUMN_VALUE number(10,0) not null,
        SELECTED number(1,0),
        primary key (PEER_EVAL_RESULT_ID)
    );

alter table lesson_builder_pages add groupid varchar2(99 char);
    create table lesson_builder_properties (
        id number(19,0) not null,
        attribute varchar2(255 char) not null unique,
        value clob,
        primary key (id)
    );

    create table lesson_builder_q_responses (
        id number(19,0) not null,
        timeAnswered timestamp not null,
        questionId number(19,0) not null,
        userId varchar2(99 char) not null,
        correct number(1,0) not null,
        shortanswer clob,
        multipleChoiceId number(19,0),
        originalText clob,
        overridden number(1,0) not null,
        points double precision,
        primary key (id)
    );

    create table lesson_builder_qr_totals (
        id number(19,0) not null,
        questionId number(19,0),
        responseId number(19,0),
        respcount number(19,0),
        primary key (id)
    );

alter table lesson_builder_student_pages add groupid varchar2(99 char);
--- alter table lesson_builder_items modify description clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=description;
alter table lesson_builder_items drop column description;
alter table lesson_builder_items rename column temp to description;

--- alter table lesson_builder_items modify groups clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=groups;
alter table lesson_builder_items drop column groups;
alter table lesson_builder_items rename column temp to groups;


--- alter table lesson_builder_items modify ownerGroups clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=ownerGroups;
alter table lesson_builder_items drop column ownerGroups;
alter table lesson_builder_items rename column temp to ownerGroups;

alter table lesson_builder_items modify gradebookId varchar2(100 char);
alter table lesson_builder_items modify altGradebook varchar2(100 char);

alter table lesson_builder_student_pages modify owner varchar2(99 char);
alter table lesson_builder_student_pages modify groupid varchar2(99 char);
alter table lesson_builder_groups modify groupId varchar2(99 char);
alter table lesson_builder_groups modify siteId varchar2(99 char);
alter table lesson_builder_pages modify toolId varchar2(99 char);
alter table lesson_builder_pages modify siteId varchar2(99 char);
alter table lesson_builder_pages modify owner varchar2(99 char);
alter table lesson_builder_pages modify groupid varchar2(99 char);
alter table lesson_builder_comments modify author varchar2(99 char);
alter table lesson_builder_log modify userId varchar2(99 char);
alter table lesson_builder_log modify toolId varchar2(99 char);


create index lb_group_site on lesson_builder_groups(siteId);
create index lb_item_gb on lesson_builder_items(gradebookid);
create index lb_item_altgb on lesson_builder_items(altGradebook);
create index lb_prop_idx on lesson_builder_properties(attribute);
create index lb_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lb_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lb_qr_questionId on lesson_builder_q_responses(questionId);
