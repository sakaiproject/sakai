$Id: readme.txt,v 1.13 2004/07/18 04:18:14 unsaved Exp $

SqlTool UNIT TESTING


To do anything at all with SqlTool unit testing, you need

    The HSQLDB test classes, built with Java 1.4.

        This is most commonly accomplished by running "build jartest" with
        a Java 1.4 SDK (as documented in the Build chapter of the HSQLB
        User Guide).  (If you are actively developing, you could alternatively
        run "build test" and work with the /classes branch instead of a jar).

    Your search path set so that a Java 1.4 "java" executable gets run.

        The tests will not work if you give a path to "java" to run
        the test programs, because the test program itself invokes java
        using just "java".  This won't work:

            /usr/java/j2sdk1.4.2_02/bin/java org.hsqldb.test.TestSqlTool...

    Shell environmental variable "CLASSPATH" set (and export if your shell 
    supports that) to include hsqldbtest.jar (or the HSQLDB "classes" 
    subdirectory).  The tests will not work if you supply a
    java classpath switch, for the exact same reason described for the
    previous item.
    (When time permits, the test harness should set the new classpath 
    according to the system property java.class.path.  This will get
    rid of the OS-specific requirement).

    IF you are running JUnit tests (ie., running a JUnit test manually
    or invoking TestSqlTool), then you also need to put the junit.jar 
    file into your classpath.

    Set up a urlid named "mem" in your sqltool.rc file, as documented in
    the SqlTool chapter of the HSQLDB User Guide.  If you started with
    the sample sqltool.rc file then you are all set (because that defines
    a "mem" urlid).

    I expect at some point I or somebody else will make unit tests which
    really need meaty data in the database.  In that case, it may make
    sense to set up another urlid and supply a (non-test) SQL file in
    this directory which will populate that database.  I don't have time
    right now to figure out how to document these dependencies, so just
    set up the "mem" urlid for now.

    Run the tests from this directory.  This is only because it has
    been more convenient to use relative filepaths in various places.
    org.hsqldb.tst.TestSqlTool uses relative filepaths to find and load
    the *.list files, then the *.list files in this directory use 
    relative paths for the SQL test files.


If you're a Java coder, and you don't have a good understanding of JUnit,
I recommend my JUnit HOWTO, available at
    http://admc.com/blaine/howtos/junit/index.html.

To run the JUnit test suite for SqlTool.

    Graphical.

        java org.hsqldb.test.TestSqlTool --gui

    Non-graphical

        java org.hsqldb.test.TestSqlTool

    If a test fails and you want to know exactly which test failed,
    then run SqlToolHarness with -v, as explained next.  (One
    JUnit test method may have dozens of specific tests).


To run tests of specific SQL files against SqlTool without JUnit.

        java [-v] org.hsqldb.test.SqlToolHarness file1.sql [file2.sql...]

    The -v switch will tell you exactly which test failed, and gives 
    information to help debug your test file itself (e.g. it echos all of
    the harness metadata values and stdout and stderr in their entirety).


To make a new SQL test file.

    Look at the appropriate annotated example file, annotated-*.sql.
    This explains how to code the metadata to describe exactly how 
    to run SqlTool for the test.  *Do not use the annotated examples
    as a template for your own SQL test files!* (see next item about
    that).

    Find a *.sql file in this directory closest to what you intend
    to do.  Use that file as a template by copying it to your new
    file name and editing it.  We don't use the annotated examples
    as templates because the annotations therein are purposefully
    verbose.  We don't want the same information duplicated in a 
    zillion files (what if we need to update an explanation!), plus,
    I like the real test files to be nice and concise.

    In general, try to use a Memory-only urlid named "mem".  This is
    just simpler because the url is easy to set up, you don't have to
    worry about the state of the database when you connect, and you
    don't need to worry about cleanup.


Regexes

    You can use regular expression values as documented in the 
    JDK 1.4 API Spec for java.util.regex.Pattern.

    This is a very powerful regular expression language very close
    to Perl's.  There are a few limitations (such as look-ahead and
    look-behind strings must be of fixed size), but, like I said,
    they are still extremely powerful.

    Example:
    
        requireStdoutRegex  (?im)\w+\s+something bad\s*$

    This would match lines anywhere in stdout of the SqlTool run
    which end with a word + whitespace + "something bad" (case
    insensitive) + optional whitespace.


JUnit integration

    The class org.hsqldb.tst.TestSqlTool is a JUnit test suite.
    It invokes one test method corresponding to each *.list file
    in this directory.  The file testSQL.list, for example, lists 
    the SQL test files that are executed and tested for the JUnit 
    test method "testSQL".  If, for any reason, you want to run
    a different set of SQL files for the "testSQL" JUnit test
    (e.g., you add a test or remove a test), then just edit the
    file testSQL.list accordingly.
