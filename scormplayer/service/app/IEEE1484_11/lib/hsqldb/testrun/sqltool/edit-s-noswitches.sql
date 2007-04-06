/*
    $Id: edit-s-noswitches.sql,v 1.3 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Command-line editing with switch command.  No subst. switches.

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?m)\sMARK A\n.*Current Buffer:\nalpha beta gamma delta$
    requireStdoutRegex  (?m)\sMARK B\n.*Current Buffer:\nalphREPLbeta gamma delta$
    requireStdoutRegex  (?m)\sMARK C\n.*Current Buffer:\nalphREPLbeta g delta$
    arg                 mem 
    HARNESS_METADATA        END       
*/

/* The blank line after each command moves the command to history without
   executing it. */

alpha beta gamma delta

/* Should change nothing because case doesn't match */
\p MARK A
:s/A /REPL/

/* Should work */
\p MARK B
:s/a /REPL/

\p MARK C
:s/amma//
