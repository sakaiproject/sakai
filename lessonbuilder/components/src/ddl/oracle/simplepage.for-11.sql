alter table lesson_builder_p_eval_results modify (gradee null);                                                      
alter table lesson_builder_p_eval_results modify (row_text null);                                                    
alter table lesson_builder_p_eval_results add gradee_group varchar2(99) null;
alter table lesson_builder_p_eval_results add row_id number(20,0) default 0;
      
