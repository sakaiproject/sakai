-- SAM-2408 Restoring sequence if missing 
declare 
init_value number; 
bad_object number; 
begin 
    select object_type into bad_object from user_objects where object_name = 'SAM_PUBATTACHMENT_ID_S'; 
    if bad_object = 'SEQUENCE' then 
        select SAM_PUBATTACHMENT_ID_S.nextval into init_value from dual; 
        execute immediate 'create sequence SAM_PUBLISHEDATTACHMENT_ID_S start with ' || init_value; 
    end if; 
    execute immediate 'drop ' || bad_object || ' SAM_PUBATTACHMENT_ID_S'; 
exception when no_data_found then 
    null; -- Nothing to do 
end; 
-- end SAM-2408 
