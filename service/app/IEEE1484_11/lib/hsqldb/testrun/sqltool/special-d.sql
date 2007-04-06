/*
    $Id: special-d.sql,v 1.2 2004/07/06 14:52:47 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special commands \dX.
    Right now it only tests \d*, \dt, \dv, and even these only partially.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    rejectStdoutRegex   (?s)\bNEW_\b.*\bMARK A\b
    rejectStdoutRegex   (?s)\bMARK B.*\bNEW_VIEW.*\bMARK C\b
    requireStdoutRegex  (?s)\bMARK B\b.*\bNEW_TBL\b.*\bMARK C
    requireStdoutRegex  (?s)\bMARK C\b.*\bNEW_VW\b.*\bMARK D
    rejectStdoutRegex   (?s)\bMARK C\b.*\bNEW_TBL\b.*\bMARK D
    rejectStdoutRegex   (?s)\bMARK D\b.*\bNEW_.*\bMARK E
    requireStdoutRegex  (?s)\bMARK E\b.*\bNEW_TBL\b
    arg                 mem 
    HARNESS_METADATA        END       
*/

\d*
\dt
\dv
\p MARK A

CREATE TABLE NEW_TBL (vc VARCHAR);
CREATE VIEW NEW_VW AS SELECT vc FROM NEW_TBL;

\p MARK B
\dt
\p MARK C
\dv

/* Test substring filter */
\p MARK D
\dt other
\p MARK E
\dt new
