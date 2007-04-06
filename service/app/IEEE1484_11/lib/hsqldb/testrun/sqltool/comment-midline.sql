/*
    $Id: comment-midline.sql,v 1.1 2004/06/17 03:03:30 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Special command \s with no arg.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?m)\sPRE-COMMENT-A\n.*\sPOST-COMMENT-A\n.*\sPRE-COMMENT-B\n.*\sPOST-COMMENT-B\n.*\sPRE-COMMENT-C\n.*\sPOST-COMMENT-C$
    rejectStdoutRegex   Writing some
    rejectStdoutRegex   crap
    rejectStdoutRegex   right here
    arg                 mem 
    HARNESS_METADATA        END       
*/

\p PRE-COMMENT-A
        /* Writing some
    crap
right here */\p POST-COMMENT-A


\p PRE-COMMENT-B
/* Writing some
    crap
        right here */  \p POST-COMMENT-B

\p PRE-COMMENT-C
     /* Writing some right here */  \p POST-COMMENT-C
