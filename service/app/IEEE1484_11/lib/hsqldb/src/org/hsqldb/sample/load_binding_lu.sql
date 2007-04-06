/*
    $Id: load_binding_lu.sql,v 1.1 2004/06/09 13:50:26 unsaved Exp $
    Load BINDING Lookup table
*/

\p Creating table BINDING_TMPTXT
CREATE TEMP TEXT TABLE binding_tmptxt (
    id integer,
    name varchar(12)
);

\p Setting text file source
SET TABLE binding_tmptxt SOURCE "binding_lu.ttbl;ignore_first=true;fs=|";

\p rows in binding_tmptxt:
select count(*) from binding_tmptxt;
\p PRE rows in binding_lu:
select count(*) from binding_lu;

INSERT INTO binding_lu (
    id,
    name
) SELECT
    id,
    name
FROM BINDING_TMPTXT;

commit;

\p POST rows in binding_lu:
select count(*) from binding_lu;
