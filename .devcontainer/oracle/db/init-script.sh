#!/bin/bash 

SQLPLUS="/opt/oracle/product/26ai/dbhomeFree/bin/sqlplus"

$SQLPLUS sys/sakairoot@FREEPDB1 as sysdba <<EOF
begin
    EXECUTE IMMEDIATE 'create user sakai identified by "ironchef" default tablespace USERS quota unlimited on USERS';
    EXECUTE IMMEDIATE 'grant create SESSION to sakai';
    EXECUTE IMMEDIATE 'grant create TABLE to sakai';
    EXECUTE IMMEDIATE 'grant create SEQUENCE to sakai';
    EXECUTE IMMEDIATE 'grant create PROCEDURE to sakai';
    EXECUTE IMMEDIATE 'grant create VIEW to sakai';
end;
/
COMMIT;
EOF
