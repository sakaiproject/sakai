/*
    $Id: hist-recall-runquery.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Recall a SQL query from the SQL buffer and execute it.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?mis)recalling and executing now:.*select \* from t\b.*^31$
    arg                 mem 
    HARNESS_METADATA        END       
*/

/* The blank line after a command moves the command to history without
   executing it. */
select * from t

create table t (i int);
insert into t values(31);


\p Recalling and executing now:
\-2;
