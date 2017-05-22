-- LSNBLDR-500
alter table lesson_builder_pages add folder varchar2(250);
create index lb_page_folder on lesson_builder_pages(siteId, folder);
