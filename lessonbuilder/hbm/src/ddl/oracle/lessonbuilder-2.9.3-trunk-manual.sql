--- alter table lesson_builder_items modify ownerGroups clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=ownerGroups;
alter table lesson_builder_items drop column ownerGroups;
alter table lesson_builder_items rename column temp to ownerGroups;

alter table lesson_builder_items modify gradebookId varchar2(100 char);
alter table lesson_builder_items modify altGradebook altGradebook varchar2(100 char);

create index lb_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lb_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lb_qr_questionId on lesson_builder_q_responses(questionId);
