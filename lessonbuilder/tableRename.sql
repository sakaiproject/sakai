RENAME TABLE SimplePageTool TO lesson_builder_items, SimplePages TO lesson_builder_pages, SimplePageLog TO lesson_builder_log;
 UPDATE SAKAI_SITE_TOOL SET registration = 'sakai.lessonbuildertool' WHERE registration = 'sakai.simplepagetool';
 