/*
    $Id: pl.sql,v 1.4 2005/05/02 15:07:26 unsaved Exp $
    SQL File to illustrate the use of SqlTool PL features.
    Invoke like
        java -jar .../hsqldb.jar .../pl.sql mem
                                                         -- blaine
*/

* if (! *MYTABLE)
    \p MYTABLE variable not set!
    /* You could use \q to Quit SqlTool, but it's often better to just
       break out of the current SQL file.
       If people invoke your script from SqlTool interactively (with
       \i yourscriptname.sql) any \q will kill their SqlTool session. */
    \p Use arguments "--setvar MYTABLE=mytablename" for SqlTool
    * break
* end if

/* Turning on Continue-upon-errors so that we can check for errors ourselves.*/
\c true

\p
\p Loading up a table named '*{MYTABLE}'...

/* This sets the PL variable 'retval' to the return status of the following
   SQL command */
* retval ~
CREATE TABLE *{MYTABLE} (
    i int,
    s varchar
);
\p CREATE status is *{retval}
\p

/* Validate our return status.  In logical expressions, unset variables like
   *unsetvar are equivalent to empty string, which is not equal to 0
   (though both do evaluate to false on their own, i.e. (*retval) is false
   and (0) is false */
* if (*retval != 0)
    \p Our CREATE TABLE command failed.
    * break
* end if

/* Default Continue-on-error behavior is what you usually want */
\c false
\p

/* Insert data with a foreach loop.
   These values could be from a read of another table or from variables
   set on the command line like
*/
\p Inserting some data int our new table (you should see 3 row update messages)
* foreach VALUE (12 22 24 15)
    * if (*VALUE > 23)
        \p Skipping *{VALUE} because it is greater than 23
        * continue
        \p YOU WILL NEVER SEE THIS LINE, because we just 'continued'.
    * end if
    INSERT INTO *{MYTABLE} VALUES (*{VALUE}, 'String of *{VALUE}');
* end foreach
\p

* themax ~
/* Can put Special Commands and comments between "* VARNAME ~" and the target 
   SQL statement. */
\p We're saving the max value for later.  You'll still see query output here:
SELECT MAX(i) FROM *{MYTABLE};

/* This is usually unnecessary because if the SELECT failed, retval would
   be undefined and the following print statement would make SqlTool exit with
   a failure status */
* if (! *themax)
    \p Failed to get the max value.
    /* It's possible that the query succeeded but themax is "0".
       You can check for that if you need to. */
    * break
    \p YOU WILL NEVER SEE THIS LINE, because we just 'broke'.
* end if

\p
\p ##############################################################
\p The results of our work:
SELECT * FROM *{MYTABLE};
\p MAX value is *{themax}

\p
\p Everything worked.
