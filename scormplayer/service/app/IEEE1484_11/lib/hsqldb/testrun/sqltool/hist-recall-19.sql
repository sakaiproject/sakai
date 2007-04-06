/*
    $Id: hist-recall-19.sql,v 1.3 2004/06/17 02:50:25 unsaved Exp $

    See readme.txt in this directory for how to unit test SqlTool.

    Tests:  Recall a command from the SQL buffer 20 commands ago.
            (command -19, since the history begins at 0).

    HARNESS_METADATA        BEGIN         
    arg                 --noAutoFile
    requireStdoutRegex  (?mi)restored following command.*\n\Qcommand 06\E$
    arg                     mem 
    HARNESS_METADATA        END       
*/

/* The blank line after each command moves the command to history without
   executing it. */
command 01

command 02

command 03

command 04

command 05

command 06

command 07

command 08

command 09

command 10

command 11

command 12

command 13

command 14

command 15

command 16

command 17

command 18

command 19

command 20

command 21

command 22

command 23

command 24

command 25

\-19
