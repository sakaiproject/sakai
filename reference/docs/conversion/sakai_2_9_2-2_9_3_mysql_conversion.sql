
-- LSNBLDR-227
alter table lesson_builder_groups add siteId varchar(250);
alter table lesson_builder_items modify description text;
alter table lesson_builder_items modify groups text;

create table if not exists lesson_builder_properties (
     id bigint not null auto_increment,
     attribute varchar(255) not null unique,
     value longtext,
     primary key (id)
);

create index lesson_builder_group_site on lesson_builder_groups(siteId);
create index lesson_builder_item_gb on lesson_builder_items(gradebookid);
create index lesson_builder_item_altgb on lesson_builder_items(altGradebook);
create index lesson_builder_prop_idx on lesson_builder_properties(attribute);
-- end LSNBLDR-227
