
-- LSNBLDR-227
alter table lesson_builder_groups add siteId varchar2(250 char);

-- alter table lesson_builder_items modify description clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=description;
alter table lesson_builder_items drop column description;
alter table lesson_builder_items rename column temp to description;

-- alter table lesson_builder_items modify groups clob;
alter table lesson_builder_items add temp clob;
update lesson_builder_items set temp=groups;
alter table lesson_builder_items drop column groups;
alter table lesson_builder_items rename column temp to groups;

create table lesson_builder_properties (
      id number(19,0) not null,
      attribute varchar2(255 char) not null unique,
      value clob,
      primary key (id)
);

create index lb_item_gb on lesson_builder_items(gradebookid);
create index lb_item_altgb on lesson_builder_items(altGradebook);
create index lb_prop_idx on lesson_builder_properties(attribute);
create index lb_group_site on lesson_builder_groups(siteId);
-- end LSNBLDR-227

-- BLTI-238
ALTER TABLE lti_mapping MODIFY (     matchpattern VARCHAR2(255) );
ALTER TABLE lti_mapping MODIFY (     launch VARCHAR2(255) );
ALTER TABLE lti_content MODIFY (     title VARCHAR2(255) );
ALTER TABLE lti_tools MODIFY   (     title VARCHAR2(255) );
ALTER TABLE lti_tools MODIFY   (     launch VARCHAR2(1024) );
ALTER TABLE lti_tools MODIFY   (     consumerkey VARCHAR2(255) );
ALTER TABLE lti_tools MODIFY   (     secret VARCHAR2(255) );
-- end BLTI-238

-- SAM-973
alter table SAM_ITEMGRADING_t add ISCORRECT number(1,0);
-- end SAM-973
