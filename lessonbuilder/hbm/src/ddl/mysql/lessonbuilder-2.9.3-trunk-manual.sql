alter table lesson_builder_items modify html mediumtext; 
alter table lesson_builder_items modify ownerGroups text;
alter table lesson_builder_items modify gradebookId varchar(100);
alter table lesson_builder_items modify altGradebook varchar(100);

create index lesson_builder_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lesson_builder_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lesson_builder_qr_questionId on lesson_builder_q_responses(questionId);
