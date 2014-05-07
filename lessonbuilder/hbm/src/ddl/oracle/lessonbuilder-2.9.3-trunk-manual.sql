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

create index lb_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lb_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lb_qr_questionId on lesson_builder_q_responses(questionId);
