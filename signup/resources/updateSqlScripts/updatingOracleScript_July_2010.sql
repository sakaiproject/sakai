-- modify the column size
alter table signup_site_groups modify (calendar_event_id varchar2(2000));
alter table signup_sites modify (calendar_event_id varchar2(2000));