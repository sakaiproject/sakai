/*
    $Id: special-q-arg.sql,v 1.4 2004/07/05 00:49:23 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \q with arg.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  PRE-QUIT
    requireErroutRegex  Abort message here
    rejectStdoutRegex   POST-QUIT
    arg                 mem 
    exitValue           2
    HARNESS_METADATA        END       
*/

\p PRE-QUIT

\q Abort message here

\p POST-QUIT
