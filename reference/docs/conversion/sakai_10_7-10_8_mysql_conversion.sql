-- LSNBLDR-500
alter table lesson_builder_pages add folder varchar(250);
create index lesson_builder_page_folder on lesson_builder_pages(siteId, folder);
