/*
    $Id: edit-s-switches.sql,v 1.2 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Command-line editing with switch command.  With subst. switches.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?m)\sMARK A\n.*Current Buffer:\nalpha beta gamma delta$
    requireStdoutRegex  (?m)\sMARK B\n.*Current Buffer:\nalphALTbeta gamma delta$
    requireStdoutRegex  (?m)\sMARK C\n.*Current Buffer:\nGLOBlphALTbetGLOB gGLOBmmGLOB deltGLOB$
    arg                 mem 
    HARNESS_METADATA        END       
*/

/* The blank line after each command moves the command to history without
   executing it. */

alpha beta gamma delta

/* case-sensitive has nothing to do here */
\p MARK A
:s/A /REPL/

/* case-insensitive does work here */
\p MARK B
:s/A /ALT/i

/* global */
\p MARK C
:s/a/GLOB/g
