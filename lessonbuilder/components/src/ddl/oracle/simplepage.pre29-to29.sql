-- in the future all index changes will be handled by simplepage.sql. However
-- some older changes do not include the drops. We're not quite sure we want to
-- insert these into simplepage.sql, because we're not sure of the state of
-- existing systems. So in upgrading to the 2.9 version (Lessons 1.4) from older versions, we
-- recommend looking at your system to see whether these are needed. It should be harmless
-- to execute these for all systems, but some drops may fail and the last 2 may unnecessarily
-- recreate the index. So rather than just doing these, you may prefer to check which of the
-- indices currently exist and only do the lines that are needed.

drop index lesson_builder_comments_itemid on lesson_builder_comments;
create index lesson_builder_comments_itemid_author on lesson_builder_comments(itemId, author);
drop index lesson_builder_log_index on lesson_builder_log;
create index lesson_builder_log_index on lesson_builder_log(userId,itemId, studentPageId);
