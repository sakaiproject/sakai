-- ---------------------------------------------------------------------------
-- tables are created by hibernate; so we only need indices here. While in development
-- put newer statements first, since reading the file will stop if the first
-- statement fails. If you need to force it to be read, try
-- update SAKAI_CLUSTER set SERVER_ID='1' where SERVER_ID='1';
-- ---------------------------------------------------------------------------

create index lesson_builder_p_eval_res_row on lesson_builder_p_eval_results(page_id);
alter table lesson_builder_p_eval_results modify column gradee varchar(99) null;                                     
alter table lesson_builder_p_eval_results modify column row_text varchar(255) null;                                  
create index lesson_builder_page_folder on lesson_builder_pages(siteId, folder);
create index lesson_builder_group_site on lesson_builder_groups(siteId);
create index lesson_builder_item_gb on lesson_builder_items(gradebookid);
create index lesson_builder_item_altgb on lesson_builder_items(altGradebook);
create index lesson_builder_prop_idx on lesson_builder_properties(attribute);
create index lesson_builder_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lesson_builder_qr_total_qi on lesson_builder_qr_totals(questionId);
create index lesson_builder_comments_itemid_author on lesson_builder_comments(itemId, author);
create index lesson_builder_student_pages_pageId on lesson_builder_student_pages(pageId);
create index lesson_builder_student_pages_itemId on lesson_builder_student_pages(itemId);
create index lesson_builder_log_index on lesson_builder_log(userId,itemId, studentPageId);
create index lesson_builder_student_pages_index on lesson_builder_student_pages(itemId, owner, deleted);
create index lesson_builder_comments_uuid on lesson_builder_comments(UUID);
create index lesson_builder_comments_author on lesson_builder_comments(pageId, author);
create index lesson_builder_log_index3 on lesson_builder_log(itemId);
create index lesson_builder_log_index2 on lesson_builder_log(userId,toolId);
create index lesson_builder_groups_itemid on lesson_builder_groups(itemId);
create index lesson_builder_pages_pageid on lesson_builder_pages(pageId);
create index lesson_builder_pages_toolid on lesson_builder_pages(toolId, parent);
create index lesson_builder_items_pageid on lesson_builder_items(pageId);
create index lesson_builder_items_sakaiid on lesson_builder_items(sakaiId);
