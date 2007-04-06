/*
    $Id: special-q.sql,v 1.3 2004/07/05 00:49:23 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \q with no arg.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  PRE-QUIT
    rejectStdoutRegex   POST-QUIT
    arg                 mem 
    HARNESS_METADATA        END       
*/

\p PRE-QUIT

\q

\p POST-QUIT
