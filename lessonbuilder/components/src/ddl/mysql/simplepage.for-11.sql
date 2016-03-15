alter table lesson_builder_p_eval_results modify column gradee varchar(99) null;                                     
alter table lesson_builder_p_eval_results modify column row_text varchar(255) null;                                  
alter table lesson_builder_p_eval_results add column gradee_group varchar(99) null;
alter table lesson_builder_p_eval_results add column row_id  bigint(20) default 0;



