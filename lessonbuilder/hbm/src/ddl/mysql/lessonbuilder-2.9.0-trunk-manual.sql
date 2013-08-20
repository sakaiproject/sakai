alter table lesson_builder_items modify html mediumtext; 
alter table lesson_builder_items modify description text;
alter table lesson_builder_items modify groups text;
alter table lesson_builder_items modify ownerGroups text;
alter table lesson_builder_items modify gradebookId varchar(100);
alter table lesson_builder_items modify altGradebook varchar(100);

create index lesson_builder_group_site on lesson_builder_groups(siteId);
create index lesson_builder_item_gb on lesson_builder_items(gradebookid);
create index lesson_builder_item_altgb on lesson_builder_items(altGradebook);
create index lesson_builder_prop_idx on lesson_builder_properties(attribute);
create index lesson_builder_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lesson_builder_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lesson_builder_qr_questionId on lesson_builder_q_responses(questionId);
