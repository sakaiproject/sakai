-- ---------------------------------------------------------------------------
-- tables are created by hibernate; so we only need indices here. While in development
-- put newer statements first, since reading the file will stop if the first
-- statement fails. If you need to force it to be read, try
-- update SAKAI_CLUSTER set SERVER_ID='1' where SERVER_ID='1';
-- ---------------------------------------------------------------------------

create index lesson_builder_log_index3 on lesson_builder_log(itemId);
create index lesson_builder_log_index2 on lesson_builder_log(userId,toolId);
create index lesson_builder_groups_itemid on lesson_builder_groups(itemId);
create index lesson_builder_pages_pageid on lesson_builder_pages(pageId);
create index lesson_builder_pages_toolid on lesson_builder_pages(toolId, parent);
create index lesson_builder_items_pageid on lesson_builder_items(pageId);
create index lesson_builder_items_sakaiid on lesson_builder_items(sakaiId);
create index lesson_builder_log_index on lesson_builder_log(userId,itemId);