alter table lesson_builder_p_eval_results modify (gradee null);                                                      
alter table lesson_builder_p_eval_results modify (row_text null);                                                    
alter table lesson_builder_p_eval_results add gradee_group varchar2(99) null;
alter table lesson_builder_p_eval_results add row_id number(20,0) default 0;
ALTER TABLE lesson_builder_groups ADD (tmpgroups CLOB);
UPDATE lesson_builder_groups SET tmpgroups=groups;
ALTER TABLE lesson_builder_groups DROP COLUMN groups;
ALTER TABLE lesson_builder_groups RENAME COLUMN tmpgroups TO groups;
      
