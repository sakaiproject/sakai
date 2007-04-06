/*
    $Id: pl-variable.sql,v 1.1 2004/06/17 02:48:56 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  PL variable definition and use.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?m)\sMARK A\n.*\d\d?:\d\d?:\d\d$
    requireStdoutRegex  (?m)\sMARK B: \(CALL CURRENT_TIME\)$
    requireStdoutRegex  (?m)\sMARK C\n.* 4$
    requireStdoutRegex  (?m)\sMARK D\n.* 3$
    arg                 mem 
    HARNESS_METADATA        END       
*/

* ct = CALL CURRENT_TIME

\p MARK A
*ct;

\p MARK B: (*{ct})

CREATE TABLE t (i INT);
INSERT INTO t VALUES (10);
INSERT INTO t VALUES (20);
INSERT INTO t VALUES (30);
INSERT INTO t VALUES (40);

* mycount = SELECT COUNT(*) FROM t

\p MARK C
*mycount;

\p MARK D
*mycount WHERE i > 15;
