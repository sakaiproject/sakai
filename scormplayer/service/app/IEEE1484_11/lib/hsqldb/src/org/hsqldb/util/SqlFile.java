/* Copyright (c) 2001-2005, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


package org.hsqldb.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/* $Id: SqlFile.java,v 1.135 2006/07/27 20:04:31 fredt Exp $ */

/**
 * Encapsulation of a sql text file like 'myscript.sql'.
 * The ultimate goal is to run the execute() method to feed the SQL
 * commands within the file to a jdbc connection.
 *
 * Some implementation comments and variable names use keywords based
 * on the following definitions.  <UL>
 * <LI> COMMAND = Statement || SpecialCommand || BufferCommand
 * Statement = SQL statement like "SQL Statement;"
 * SpecialCommand =  Special Command like "\x arg..."
 * BufferCommand =  Editing/buffer command like ":s/this/that/"
 *
 * When entering SQL statements, you are always "appending" to the
 * "current" command (not the "buffer", which is a different thing).
 * All you can do to the current command is append new lines to it,
 * execute it, or save it to buffer.
 *
 * In general, the special commands mirror those of Postgresql's psql,
 * but SqlFile handles command editing much different from Postgresql
 * because of Java's lack of support for raw tty I/O.
 * The \p special command, in particular, is very different from psql's.
 * Also, to keep the code simpler, we're sticking to only single-char
 * special commands until we really need more.
 *
 * Buffer commands are unique to SQLFile.  The ":" commands allow
 * you to edit the buffer and to execute the buffer.
 *
 * The command history consists only of SQL Statements (i.e., special
 * commands and editing commands are not stored for later viewing or
 * editing).
 *
 * Most of the Special Commands and Editing Commands are for
 * interactive use only.
 *
 * \d commands are very poorly supported for Mysql because
 * (a) Mysql lacks most of the most basic JDBC support elements, and
 * the most basic role and schema features, and
 * (b) to access the Mysql data dictionay, one must change the database
 * instance (to do that would require work to restore the original state
 * and could have disastrous effects upon transactions).
 *
 * To make changes to this class less destructive to external callers,
 * the input parameters should be moved to setters (probably JavaBean
 * setters would be best) instead of constructor args and System
 * Properties.
 *
 * @version $Revision: 1.135 $
 * @author Blaine Simpson unsaved@users
 */
public class SqlFile {

    private static final int DEFAULT_HISTORY_SIZE = 20;
    private File             file;
    private boolean          interactive;
    private String           primaryPrompt    = "sql> ";
    private String           chunkPrompt      = "raw> ";
    private String           contPrompt       = "  +> ";
    private Connection       curConn          = null;
    private boolean          htmlMode         = false;
    private HashMap          userVars         = null;
    private String[]         statementHistory = null;
    private boolean          chunking         = false;
    private String           csvNullRep       = null;

    /**
     * Private class to "share" a variable among a family of SqlFile
     * instances.
     */
    private static class BooleanBucket {

        private boolean bPriv = false;

        public void set(boolean bIn) {
            bPriv = bIn;
        }

        public boolean get() {
            return bPriv;
        }
    }

    // This is an imperfect solution since when user runs SQL they could
    // be running DDL or a commit or rollback statement.  All we know is,
    // they MAY run some DML that needs to be committed.
    BooleanBucket possiblyUncommitteds = new BooleanBucket();

    // Ascii field separator blanks
    private static final int SEP_LEN = 2;
    private static final String DIVIDER =
        "-----------------------------------------------------------------"
        + "-----------------------------------------------------------------";
    private static final String SPACES =
        "                                                                 "
        + "                                                                 ";
    private static String revnum = null;

    static {
        revnum = "$Revision: 1.135 $".substring("$Revision: ".length(),
                "$Revision: 1.135 $".length() - 2);
    }

    private static String BANNER =
        "(SqlFile processor v. " + revnum + ")\n"
        + "Distribution is permitted under the terms of the HSQLDB license.\n"
        + "(c) 2004-2005 Blaine Simpson and the HSQLDB Development Group.\n\n"
        + "    \\q    to Quit.\n" + "    \\?    lists Special Commands.\n"
        + "    :?    lists Buffer/Editing commands.\n"
        + "    *?    lists PL commands (including alias commands).\n\n"
        + "SPECIAL Commands begin with '\\' and execute when you hit ENTER.\n"
        + "BUFFER Commands begin with ':' and execute when you hit ENTER.\n"
        + "COMMENTS begin with '/*' and end with the very next '*/'.\n"
        + "PROCEDURAL LANGUAGE commands begin with '*' and end when you hit ENTER.\n"
        + "All other lines comprise SQL Statements.\n"
        + "  SQL Statements are terminated by either a blank line (which moves the\n"
        + "  statement into the buffer without executing) or a line ending with ';'\n"
        + "  (which executes the statement).\n"
        + "  SQL Statements may begin with '/PLVARNAME' and/or contain *{PLVARNAME}s.\n";
    private static final String BUFFER_HELP_TEXT =
        "BUFFER Commands (only \":;\" is available for non-interactive use).\n"
        + "    :?                Help\n"
        + "    :;                Execute current buffer as an SQL Statement\n"
        + "    :a[text]          Enter append mode with a copy of the buffer\n"
        + "    :l                List current contents of buffer\n"
        + "    :s/from/to        Substitute \"to\" for first occurrence of \"from\"\n"
        + "    :s/from/to/[i;g2] Substitute \"to\" for occurrence(s) of \"from\"\n"
        + "                from:  '$'s represent line breaks\n"
        + "                to:    If empty, from's will be deleted (e.g. \":s/x//\").\n"
        + "                       '$'s represent line breaks\n"
        + "                       You can't use ';' in order to execute the SQL (use\n"
        + "                       the ';' switch for this purpose, as explained below).\n"
        + "                /:     Can actually be any character which occurs in\n"
        + "                       neither \"to\" string nor \"from\" string.\n"
        + "                SUBSTITUTION MODE SWITCHES:\n"
        + "                       i:  case Insensitive\n"
        + "                       ;:  execute immediately after substitution\n"
        + "                       g:  Global (substitute ALL occurrences of \"from\" string)\n"
        + "                       2:  Narrows substitution to specified buffer line number\n"
        + "                           (Use any line number in place of '2').\n"
    ;
    private static final String HELP_TEXT = "SPECIAL Commands.\n"
        + "* commands only available for interactive use.\n"
        + "In place of \"3\" below, you can use nothing for the previous command, or\n"
        + "an integer \"X\" to indicate the Xth previous command.\n"
        + "Filter substrings are cases-sensitive!  Use \"SCHEMANAME.\" to narrow schema.\n"
        + "    \\?                   Help\n"
        + "    \\p [line to print]   Print string to stdout\n"
        + "    \\w file/path.sql     Append current buffer to file\n"
        + "    \\i file/path.sql     Include/execute commands from external file\n"
        + "    \\d{tvsiSanur*} [substr]  List objects of specified type:\n"
        + "  (Tbls/Views/Seqs/Indexes/SysTbls/Aliases/schemaNames/Users/Roles/table-like)\n"
        + "    \\d OBJECTNAME [subs] Describe table or view columns\n"
        + "    \\o [file/path.html]  Tee (or stop teeing) query output to specified file\n"
        + "    \\H                   Toggle HTML output mode\n"
        + "    \\! COMMAND ARGS      Execute external program (no support for stdin)\n"
        + "    \\c [true|false]      Continue upon errors (a.o.t. abort upon error)\n"
        + "    \\a [true|false]      Auto-commit JDBC DML commands\n"
        + "    \\b                   save next result to Binary buffer (no display)\n"
        + "    \\bd file/path.bin    Dump Binary buffer to file\n"
        + "    \\bl file/path.bin    Load file into Binary buffer\n"
        + "    \\bp                  Use ? in next SQL statement to upload Bin. buffer\n"
        + "    \\.                   Enter raw SQL.  End with line containing only \".\"\n"
        + "    \\s                   * Show previous commands (i.e. SQL command history)\n"
        + "    \\-[3][;]             * reload a command to buffer (opt. exec. w/ \":;\"))\n"
        + "    \\x {TABLE|SELECT...} eXport table or query to CSV text file\n"
        + "    \\m file/path.csv     iMport CSV text file records into a table\n"
        + "    \\q [abort message]   Quit (or end input like Ctrl-Z or Ctrl-D)\n"
    ;
    private static final String PL_HELP_TEXT = "PROCEDURAL LANGUAGE Commands.\n"
        + "    *?                            Help\n"
        + "    *                             Expand PL variables from now on.\n"
        + "                                  (this is also implied by all the following).\n"
        + "    * VARNAME = Variable value    Set variable value\n"
        + "    * VARNAME =                   Unset variable\n"
        + "    * VARNAME ~                   Set variable value to the value of the very\n"
        + "                                  next SQL statement executed (see details\n"
        + "                                  at the bottom of this listing).\n"
        + "    * VARNAME _                   Same as * VARNAME _, except the query is\n"
        + "                                  done silently (i.e, no rows to screen)\n"
        + "    * list[value] [VARNAME1...]   List variable(s) (defaults to all)\n"
        + "    * load VARNAME path.txt       Load variable value from text file\n"
        + "    * dump VARNAME path.txt       Dump variable value to text file\n"
        + "    * prepare VARNAME             Use ? in next SQL statement to upload val.\n"
        + "    * foreach VARNAME ([val1...]) Repeat the following PL block with the\n"
        + "                                  variable set to each value in turn.\n"
        + "    * if (logical expr)           Execute following PL block only if expr true\n"
        + "    * while (logical expr)        Repeat following PL block while expr true\n"
        + "    * end foreach|if|while        Ends a PL block\n"
        + "    * break [foreach|if|while|file] Exits a PL block or file early\n"
        + "    * continue [foreach|while]    Exits a PL block iteration early\n\n"
        + "Use PL variables (which you have set) like: *{VARNAME}.\n"
        + "You may use /VARNAME instead iff /VARNAME is the first word of a SQL command.\n"
        + "Use PL variables in logical expressions like: *VARNAME.\n\n"
        + "'* VARNAME ~' or '* VARNAME _' sets the variable value according to the very\n"
        + "next SQL statement (~ will echo the value, _ will do it silently):\n"
        + "    Query:  The value of the first field of the first row returned.\n"
        + "    other:  Return status of the command (for updates this will be\n"
        + "            the number of rows updated).\n"
    ;

    /**
     * Interpret lines of input file as SQL Statements, Comments,
     * Special Commands, and Buffer Commands.
     * Most Special Commands and many Buffer commands are only for
     * interactive use.
     *
     * @param inFile  inFile of null means to read stdin.
     * @param inInteractive  If true, prompts are printed, the interactive
     *                       Special commands are enabled, and
     *                       continueOnError defaults to true.
     */
    public SqlFile(File inFile, boolean inInteractive,
                   HashMap inVars) throws IOException {

        file        = inFile;
        interactive = inInteractive;
        userVars    = inVars;

        try {
            statementHistory =
                new String[interactive ? Integer.parseInt(System.getProperty("sqltool.historyLength"))
                                       : 1];
        } catch (Throwable t) {
            statementHistory = null;
        }

        if (statementHistory == null) {
            statementHistory = new String[DEFAULT_HISTORY_SIZE];
        }

        if (file != null &&!file.canRead()) {
            throw new IOException("Can't read SQL file '" + file + "'");
        }
    }

    /**
     * Constructor for reading stdin instead of a file for commands.
     *
     * @see #SqlFile(File,boolean)
     */
    public SqlFile(boolean inInteractive, HashMap inVars) throws IOException {
        this(null, inInteractive, inVars);
    }

    /**
     * Process all the commands on stdin.
     *
     * @param conn The JDBC connection to use for SQL Commands.
     * @see #execute(Connection,PrintStream,PrintStream,boolean)
     */
    public void execute(Connection conn,
                        Boolean coeOverride)
                        throws IOException, SqlToolError, SQLException {
        execute(conn, System.out, System.err, coeOverride);
    }

    /**
     * Process all the commands on stdin.
     *
     * @param conn The JDBC connection to use for SQL Commands.
     * @see #execute(Connection,PrintStream,PrintStream,boolean)
     */
    public void execute(Connection conn,
                        boolean coeOverride)
                        throws IOException, SqlToolError, SQLException {
        execute(conn, System.out, System.err, new Boolean(coeOverride));
    }

    // So we can tell how to handle quit and break commands.
    public boolean      recursed     = false;
    private String      curCommand   = null;
    private int         curLinenum   = -1;
    private int         curHist      = -1;
    private PrintStream psStd        = null;
    private PrintStream psErr        = null;
    private PrintWriter pwQuery      = null;
    private PrintWriter pwCsv        = null;
    StringBuffer        stringBuffer = new StringBuffer();
    /*
     * This is reset upon each execute() invocation (to true if interactive,
     * false otherwise).
     */
    private boolean             continueOnError = false;
    private static final String DEFAULT_CHARSET = "US-ASCII";
    private BufferedReader      br              = null;
    private String              charset         = null;

    /**
     * Process all the commands in the file (or stdin) associated with
     * "this" object.
     * Run SQL in the file through the given database connection.
     *
     * This is synchronized so that I can use object variables to keep
     * track of current line number, command, connection, i/o streams, etc.
     *
     * Sets encoding character set to that specified with System Property
     * 'sqlfile.charset'.  Defaults to "US-ASCII".
     *
     * @param conn The JDBC connection to use for SQL Commands.
     */
    public synchronized void execute(Connection conn, PrintStream stdIn,
                                     PrintStream errIn,
                                     Boolean coeOverride)
                                     throws IOException, SqlToolError,
                                         SQLException {

        psStd      = stdIn;
        psErr      = errIn;
        curConn    = conn;
        curLinenum = -1;

        String  inputLine;
        String  trimmedCommand;
        String  trimmedInput;
        String  deTerminated;
        boolean inComment = false;    // Globbling up a comment
        int     postCommentIndex;
        boolean gracefulExit = false;

        continueOnError = (coeOverride == null) ? interactive
                                                : coeOverride.booleanValue();

        if (userVars != null && userVars.size() > 0) {
            plMode = true;
        }

        String specifiedCharSet = System.getProperty("sqlfile.charset");

        charset = ((specifiedCharSet == null) ? DEFAULT_CHARSET
                                              : specifiedCharSet);

        try {
            br = new BufferedReader(new InputStreamReader((file == null)
                    ? System.in
                    : new FileInputStream(file), charset));
            curLinenum = 0;

            if (interactive) {
                stdprintln(BANNER);
            }

            while (true) {
                if (interactive) {
                    psStd.print((stringBuffer.length() == 0)
                                ? (chunking ? chunkPrompt
                                            : primaryPrompt)
                                : contPrompt);
                }

                inputLine = br.readLine();

                if (inputLine == null) {
                    /*
                     * This is because interactive EOD on some OSes doesn't
                     * send a line-break, resulting in no linebreak at all
                     * after the SqlFile prompt or whatever happens to be
                     * on their screen.
                     */
                    if (interactive) {
                        psStd.println();
                    }

                    break;
                }

                curLinenum++;

                if (chunking) {
                    if (inputLine.equals(".")) {
                        chunking = false;

                        setBuf(stringBuffer.toString());
                        stringBuffer.setLength(0);

                        if (interactive) {
                            stdprintln("Raw SQL chunk moved into buffer.  "
                                       + "Run \":;\" to execute the chunk.");
                        }
                    } else {
                        if (stringBuffer.length() > 0) {
                            stringBuffer.append('\n');
                        }

                        stringBuffer.append(inputLine);
                    }

                    continue;
                }

                if (inComment) {
                    postCommentIndex = inputLine.indexOf("*/") + 2;

                    if (postCommentIndex > 1) {

                        // I see no reason to leave comments in history.
                        inputLine = inputLine.substring(postCommentIndex);

                        // Empty the buffer.  The non-comment remainder of
                        // this line is either the beginning of a new SQL
                        // or Special command, or an empty line.
                        stringBuffer.setLength(0);

                        inComment = false;
                    } else {

                        // Just completely ignore the input line.
                        continue;
                    }
                }

                trimmedInput = inputLine.trim();

                try {

                    // This is the try for SQLException.  SQLExceptions are
                    // normally thrown below in Statement processing, but
                    // could be called up above if a Special processing
                    // executes a SQL command from history.
                    if (stringBuffer.length() == 0) {
                        if (trimmedInput.startsWith("/*")) {
                            postCommentIndex = trimmedInput.indexOf("*/", 2)
                                               + 2;

                            if (postCommentIndex > 1) {

                                // I see no reason to leave comments in
                                // history.
                                inputLine = inputLine.substring(
                                    postCommentIndex + inputLine.length()
                                    - trimmedInput.length());
                                trimmedInput = inputLine.trim();
                            } else {

                                // Just so we get continuation lines:
                                stringBuffer.append("COMMENT");

                                inComment = true;

                                continue;
                            }
                        }

                        // This is just to filter out useless newlines at
                        // beginning of commands.
                        if (trimmedInput.length() == 0) {
                            continue;
                        }

                        if (trimmedInput.charAt(0) == '*'
                                && (trimmedInput.length() < 2
                                    || trimmedInput.charAt(1) != '{')) {
                            try {
                                processPL((trimmedInput.length() == 1) ? ""
                                                                       : trimmedInput
                                                                       .substring(1)
                                                                       .trim());
                            } catch (BadSpecial bs) {
                                errprintln("Error at '"
                                           + ((file == null) ? "stdin"
                                                             : file.toString()) + "' line "
                                                             + curLinenum
                                                             + ":\n\""
                                                             + inputLine
                                                             + "\"\n"
                                                             + bs.getMessage());

                                if (!continueOnError) {
                                    throw new SqlToolError(bs);
                                }
                            }

                            continue;
                        }

                        if (trimmedInput.charAt(0) == '\\') {
                            try {
                                processSpecial(trimmedInput.substring(1));
                            } catch (BadSpecial bs) {
                                errprintln("Error at '"
                                           + ((file == null) ? "stdin"
                                                             : file.toString()) + "' line "
                                                             + curLinenum
                                                             + ":\n\""
                                                             + inputLine
                                                             + "\"\n"
                                                             + bs.getMessage());

                                if (!continueOnError) {
                                    throw new SqlToolError(bs);
                                }
                            }

                            continue;
                        }

                        if (trimmedInput.charAt(0) == ':'
                                && (interactive
                                    || (trimmedInput.charAt(1) == ';'))) {
                            try {
                                processBuffer(trimmedInput.substring(1));
                            } catch (BadSpecial bs) {
                                errprintln("Error at '"
                                           + ((file == null) ? "stdin"
                                                             : file.toString()) + "' line "
                                                             + curLinenum
                                                             + ":\n\""
                                                             + inputLine
                                                             + "\"\n"
                                                             + bs.getMessage());

                                if (!continueOnError) {
                                    throw new SqlToolError(bs);
                                }
                            }

                            continue;
                        }

                        String ucased = trimmedInput.toUpperCase();

                        if (ucased.startsWith("DECLARE")
                                || ucased.startsWith("BEGIN")) {
                            chunking = true;

                            stringBuffer.append(inputLine);

                            if (interactive) {
                                stdprintln(
                                    "Enter RAW SQL.  No \\, :, * commands.  "
                                    + "End with a line containing only \".\":");
                            }

                            continue;
                        }
                    }

                    if (trimmedInput.length() == 0) {

                        // Blank lines delimit commands ONLY IN INTERACTIVE
                        // MODE!
                        if (interactive &&!inComment) {
                            setBuf(stringBuffer.toString());
                            stringBuffer.setLength(0);
                            stdprintln("Current input moved into buffer.");
                        }

                        continue;
                    }

                    deTerminated = deTerminated(inputLine);

                    // A null terminal line (i.e., /\s*;\s*$/) is never useful.
                    if (!trimmedInput.equals(";")) {
                        if (stringBuffer.length() > 0) {
                            stringBuffer.append('\n');
                        }

                        stringBuffer.append((deTerminated == null) ? inputLine
                                                                   : deTerminated);
                    }

                    if (deTerminated == null) {
                        continue;
                    }

                    // If we reach here, then stringBuffer contains a complete
                    // SQL command.
                    curCommand     = stringBuffer.toString();
                    trimmedCommand = curCommand.trim();

                    if (trimmedCommand.length() == 0) {
                        throw new SQLException("Empty SQL Statement");
                    }

                    setBuf(curCommand);
                    processSQL();
                } catch (SQLException se) {
                    errprintln("SQL Error at '" + ((file == null) ? "stdin"
                                                                  : file.toString()) + "' line "
                                                                  + curLinenum
                                                                      + ":\n\""
                                                                          + curCommand
                                                                              + "\"\n"
                                                                                  + se
                                                                                  .getMessage());

                    if (!continueOnError) {
                        throw se;
                    }
                } catch (BreakException be) {
                    String msg = be.getMessage();

                    if ((!recursed) && (msg != null &&!msg.equals("file"))) {
                        errprintln("Unsatisfied break statement"
                                   + ((msg == null) ? ""
                                                    : (" (type " + msg
                                                       + ')')) + '.');
                    } else {
                        gracefulExit = true;
                    }

                    if (recursed ||!continueOnError) {
                        throw be;
                    }
                } catch (ContinueException ce) {
                    String msg = ce.getMessage();

                    if (!recursed) {
                        errprintln("Unsatisfied continue statement"
                                   + ((msg == null) ? ""
                                                    : (" (type " + msg
                                                       + ')')) + '.');
                    } else {
                        gracefulExit = true;
                    }

                    if (recursed ||!continueOnError) {
                        throw ce;
                    }
                } catch (QuitNow qn) {
                    throw qn;
                } catch (SqlToolError ste) {
                    if (!continueOnError) {
                        throw ste;
                    }
                }

                stringBuffer.setLength(0);
            }

            if (inComment || stringBuffer.length() != 0) {
                errprintln("Unterminated input:  [" + stringBuffer + ']');

                throw new SqlToolError("Unterminated input:  ["
                                       + stringBuffer + ']');
            }

            gracefulExit = true;
        } catch (QuitNow qn) {
            gracefulExit = qn.getMessage() == null;

            if ((!recursed) &&!gracefulExit) {
                errprintln("Aborting: " + qn.getMessage());
            }

            if (recursed ||!gracefulExit) {
                throw qn;
            }

            return;
        } finally {
            closeQueryOutputStream();

            if (fetchingVar != null) {
                errprintln("PL variable setting incomplete:  " + fetchingVar);

                gracefulExit = false;
            }

            if (br != null) {
                br.close();
            }

            if ((!gracefulExit) && possiblyUncommitteds.get()) {
                errprintln("Rolling back SQL transaction.");
                curConn.rollback();
                possiblyUncommitteds.set(false);
            }
        }
    }

    /**
     * Returns a copy of given string without a terminating semicolon.
     * If there is no terminating semicolon, null is returned.
     *
     * @param inString Base String, which will not be modified (because
     *                 a "copy" will be returned).
     */
    private static String deTerminated(String inString) {

        int index = inString.lastIndexOf(';');

        if (index < 0) {
            return null;
        }

        for (int i = index + 1; i < inString.length(); i++) {
            if (!Character.isWhitespace(inString.charAt(i))) {
                return null;
            }
        }

        return inString.substring(0, index);
    }

    /**
     * Utility nested Exception class for internal use.
     */
    private class BadSpecial extends Exception {

        // Special-purpose constructor
        private BadSpecial() {}

        // Normal use constructor
        private BadSpecial(String s) {
            super(s);
        }
    }

    /**
     * Utility nested Exception class for internal use.
     * This must extend SqlToolError because it has to percolate up from
     * recursions of SqlTool.execute(), yet SqlTool.execute() is public
     * and external users should not declare (or expect!) QuitNows to be
     * thrown.
     * SqlTool.execute() on throws a QuitNow if it is in a recursive call.
     */
    private class QuitNow extends SqlToolError {

        public QuitNow(String s) {
            super(s);
        }

        public QuitNow() {
            super();
        }
    }

    /**
     * Utility nested Exception class for internal use.
     * Very similar to QuitNow.
     */
    private class BreakException extends SqlToolError {

        public BreakException() {
            super();
        }

        public BreakException(String s) {
            super(s);
        }
    }

    /**
     * Utility nested Exception class for internal use.
     * Very similar to QuitNow.
     */
    private class ContinueException extends SqlToolError {

        public ContinueException() {
            super();
        }

        public ContinueException(String s) {
            super(s);
        }
    }

    /**
     * Utility nested Exception class for internal use.
     */
    private class BadSwitch extends Exception {

        private BadSwitch(int i) {
            super(Integer.toString(i));
        }
    }

    /**
     * Process a Buffer/Edit Command.
     *
     * Due to the nature of the goal here, we don't trim() "other" like
     * we do for other kinds of commands.
     *
     * @param inString Complete command, less the leading ':' character.
     * @throws SQLException Passed through from processSQL()
     * @throws BadSpecial Runtime error()
     */
    private void processBuffer(String inString)
    throws BadSpecial, SQLException {

        int    index = 0;
        int    special;
        char   commandChar = 'i';
        String other       = null;

        if (inString.length() > 0) {
            commandChar = inString.charAt(0);
            other       = inString.substring(1);

            if (other.trim().length() == 0) {
                other = null;
            }
        }

        switch (commandChar) {

            case ';' :
                curCommand = commandFromHistory(0);

                stdprintln("Executing command from buffer:\n" + curCommand
                           + '\n');
                processSQL();

                return;

            case 'a' :
            case 'A' :
                stringBuffer.append(commandFromHistory(0));

                if (other != null) {
                    String deTerminated = deTerminated(other);

                    if (!other.equals(";")) {
                        stringBuffer.append(((deTerminated == null) ? other
                                                                    : deTerminated));
                    }

                    if (deTerminated != null) {

                        // If we reach here, then stringBuffer contains a
                        // complete SQL command.
                        curCommand = stringBuffer.toString();

                        setBuf(curCommand);
                        stdprintln("Executing:\n" + curCommand + '\n');
                        processSQL();
                        stringBuffer.setLength(0);

                        return;
                    }
                }

                stdprintln("Appending to:\n" + stringBuffer);

                return;

            case 'l' :
            case 'L' :
                stdprintln("Current Buffer:\n" + commandFromHistory(0));

                return;

            case 's' :
            case 'S' :

                // Sat Apr 23 14:14:57 EDT 2005.  Changing history behavior.
                // It's very inconvenient to lose all modified SQL
                // commands from history just because _some_ may be modified
                // because they are bad or obsolete.
                boolean modeIC      = false;
                boolean modeGlobal  = false;
                boolean modeExecute = false;
                int     modeLine    = 0;

                try {
                    String       fromHist = commandFromHistory(0);
                    StringBuffer sb       = new StringBuffer(fromHist);

                    if (other == null) {
                        throw new BadSwitch(0);
                    }

                    String delim = other.substring(0, 1);
                    StringTokenizer toker = new StringTokenizer(other, delim,
                        true);

                    if (toker.countTokens() < 4
                            ||!toker.nextToken().equals(delim)) {
                        throw new BadSwitch(1);
                    }

                    String from = toker.nextToken().replace('$', '\n');

                    if (!toker.nextToken().equals(delim)) {
                        throw new BadSwitch(2);
                    }

                    String to = toker.nextToken().replace('$', '\n');

                    if (to.equals(delim)) {
                        to = "";
                    } else {
                        if (toker.countTokens() > 0
                                &&!toker.nextToken().equals(delim)) {
                            throw new BadSwitch(3);
                        }
                    }

                    if (toker.countTokens() > 0) {
                        String opts = toker.nextToken("");

                        for (int j = 0; j < opts.length(); j++) {
                            switch (opts.charAt(j)) {

                                case 'i' :
                                    modeIC = true;
                                    break;

                                case ';' :
                                    modeExecute = true;
                                    break;

                                case 'g' :
                                    modeGlobal = true;
                                    break;

                                case '1' :
                                case '2' :
                                case '3' :
                                case '4' :
                                case '5' :
                                case '6' :
                                case '7' :
                                case '8' :
                                case '9' :
                                    modeLine = Character.digit(opts.charAt(j),
                                                               10);
                                    break;

                                default :
                                    throw new BadSpecial(
                                        "Unknown Substitution option: "
                                        + opts.charAt(j));
                            }
                        }
                    }

                    if (modeIC) {
                        fromHist = fromHist.toUpperCase();
                        from     = from.toUpperCase();
                    }

                    // lineStart will be either 0 or char FOLLOWING a \n.
                    int lineStart = 0;

                    // lineStop is the \n AFTER what we consider.
                    int lineStop = -1;

                    if (modeLine > 0) {
                        for (int j = 1; j < modeLine; j++) {
                            lineStart = fromHist.indexOf('\n', lineStart) + 1;

                            if (lineStart < 1) {
                                throw new BadSpecial(
                                    "There are not " + modeLine
                                    + " lines in the buffer.");
                            }
                        }

                        lineStop = fromHist.indexOf('\n', lineStart);
                    }

                    if (lineStop < 0) {
                        lineStop = fromHist.length();
                    }

                    // System.err.println("["
                    // + fromHist.substring(lineStart, lineStop) + ']');
                    int i;

                    if (modeGlobal) {
                        i = lineStop;

                        while ((i = fromHist.lastIndexOf(from, i - 1))
                                >= lineStart) {
                            sb.replace(i, i + from.length(), to);
                        }
                    } else if ((i = fromHist.indexOf(from, lineStart)) > -1
                               && i < lineStop) {
                        sb.replace(i, i + from.length(), to);
                    }

                    //statementHistory[curHist] = sb.toString();
                    curCommand = sb.toString();

                    setBuf(curCommand);
                    stdprintln((modeExecute ? "Executing"
                                            : "Current Buffer") + ":\n"
                                            + curCommand);

                    if (modeExecute) {
                        stdprintln();
                    }
                } catch (BadSwitch badswitch) {
                    throw new BadSpecial(
                        "Substitution syntax:  \":s/from this/to that/i;g2\".  "
                        + "Use '$' for line separations.  ["
                        + badswitch.getMessage() + ']');
                }

                if (modeExecute) {
                    processSQL();
                    stringBuffer.setLength(0);
                }

                return;

            case '?' :
                stdprintln(BUFFER_HELP_TEXT);

                return;
        }

        throw new BadSpecial("Unknown Buffer Command");
    }

    private boolean doPrepare   = false;
    private String  prepareVar  = null;
    private String  csvColDelim = null;
    private String  csvRowDelim = null;
    private static final String CSV_SYNTAX_MSG =
        "Export syntax:  x table_or_view_anme "
        + "[column_delimiter [record_delimiter]]";

    /**
     * Process a Special Command.
     *
     * @param inString Complete command, less the leading '\' character.
     * @throws SQLException Passed through from processSQL()
     * @throws BadSpecial Runtime error()
     * @throws QuitNot Command execution (but not the JVM!) should stop
     */
    private void processSpecial(String inString)
    throws BadSpecial, QuitNow, SQLException, SqlToolError {

        int    index = 0;
        int    special;
        String arg1,
               other = null;

        if (inString.length() < 1) {
            throw new BadSpecial("Null special command");
        }

        if (plMode) {
            inString = dereference(inString, false);
        }

        StringTokenizer toker = new StringTokenizer(inString);

        arg1 = toker.nextToken();

        if (toker.hasMoreTokens()) {
            other = toker.nextToken("").trim();
        }

        switch (arg1.charAt(0)) {

            case 'q' :
                if (other != null) {
                    throw new QuitNow(other);
                }

                throw new QuitNow();
            case 'H' :
                htmlMode = !htmlMode;

                stdprintln("HTML Mode is now set to: " + htmlMode);

                return;

            case 'm' :
                if (arg1.length() != 1 || other == null) {
                    throw new BadSpecial();
                }

                csvColDelim =
                    convertEscapes((String) userVars.get("*CSV_COL_DELIM"));
                csvRowDelim =
                    convertEscapes((String) userVars.get("*CSV_ROW_DELIM"));
                csvNullRep = (String) userVars.get("*CSV_NULL_REP");

                if (csvColDelim == null) {
                    csvColDelim = DEFAULT_COL_DELIM;
                }

                if (csvRowDelim == null) {
                    csvRowDelim = DEFAULT_ROW_DELIM;
                }

                if (csvNullRep == null) {
                    csvNullRep = DEFAULT_NULL_REP;
                }

                try {
                    importCsv(other);
                } catch (IOException ioe) {
                    System.err.println("Failed to read in CSV file:  " + ioe);
                }

                return;

            case 'x' :
                try {
                    if (arg1.length() != 1 || other == null) {
                        throw new BadSpecial();
                    }

                    String tableName = ((other.indexOf(' ') > 0) ? null
                                                                 : other);

                    csvColDelim = convertEscapes(
                        (String) userVars.get("*CSV_COL_DELIM"));
                    csvRowDelim = convertEscapes(
                        (String) userVars.get("*CSV_ROW_DELIM"));
                    csvNullRep = (String) userVars.get("*CSV_NULL_REP");

                    String csvFilepath =
                        (String) userVars.get("*CSV_FILEPATH");

                    if (csvFilepath == null && tableName == null) {
                        throw new BadSpecial(
                            "You must set PL variable '*CSV_FILEPATH' in "
                            + "order to use the query variant of \\x");
                    }

                    File csvFile = new File((csvFilepath == null)
                                            ? (tableName + ".csv")
                                            : csvFilepath);

                    if (csvColDelim == null) {
                        csvColDelim = DEFAULT_COL_DELIM;
                    }

                    if (csvRowDelim == null) {
                        csvRowDelim = DEFAULT_ROW_DELIM;
                    }

                    if (csvNullRep == null) {
                        csvNullRep = DEFAULT_NULL_REP;
                    }

                    pwCsv = new PrintWriter(
                        new OutputStreamWriter(
                            new FileOutputStream(csvFile), charset));

                    displayResultSet(
                        null,
                        curConn.createStatement().executeQuery(
                            (tableName == null) ? other
                                                : ("SELECT * FROM "
                                                   + tableName)), null, null);
                    pwCsv.flush();
                    stdprintln("Wrote " + csvFile.length()
                               + " characters to file '" + csvFile + "'");
                } catch (Exception e) {
                    if (e instanceof BadSpecial) {

                        // Not sure this test is right.  Maybe .length() == 0?
                        if (e.getMessage() == null) {
                            throw new BadSpecial(CSV_SYNTAX_MSG);
                        } else {
                            throw (BadSpecial) e;
                        }
                    }

                    throw new BadSpecial("Failed to write to file '" + other
                                         + "':  " + e);
                } finally {

                    // Reset all state changes
                    if (pwCsv != null) {
                        pwCsv.close();
                    }

                    pwCsv       = null;
                    csvColDelim = null;
                    csvRowDelim = null;
                }

                return;

            case 'd' :
                if (arg1.length() == 2) {
                    listTables(arg1.charAt(1), other);

                    return;
                }

                if (arg1.length() == 1 && other != null) {
                    int space = other.indexOf(' ');

                    if (space < 0) {
                        describe(other, null);
                    } else {
                        describe(other.substring(0, space),
                                 other.substring(space + 1).trim());
                    }

                    return;
                }

                throw new BadSpecial("Describe commands must be like "
                                     + "'\\dX' or like '\\d OBJECTNAME'.");
            case 'o' :
                if (other == null) {
                    if (pwQuery == null) {
                        throw new BadSpecial(
                            "There is no query output file to close");
                    }

                    closeQueryOutputStream();

                    return;
                }

                if (pwQuery != null) {
                    stdprintln(
                        "Closing current query output file and opening "
                        + "new one");
                    closeQueryOutputStream();
                }

                try {
                    pwQuery = new PrintWriter(
                        new OutputStreamWriter(
                            new FileOutputStream(other, true), charset));

                    /* Opening in append mode, so it's possible that we will
                     * be adding superfluous <HTML> and <BODY> tages.
                     * I think that browsers can handle that */
                    pwQuery.println((htmlMode ? "<HTML>\n<!--"
                                              : "#") + " "
                                                     + (new java.util.Date())
                                                     + ".  Query output from "
                                                     + getClass().getName()
                                                     + (htmlMode
                                                        ? ". -->\n\n<BODY>"
                                                        : ".\n"));
                    pwQuery.flush();
                } catch (Exception e) {
                    throw new BadSpecial("Failed to write to file '" + other
                                         + "':  " + e);
                }

                return;

            case 'w' :
                if (other == null) {
                    throw new BadSpecial(
                        "You must supply a destination file name");
                }

                if (commandFromHistory(0).length() == 0) {
                    throw new BadSpecial("Empty command in buffer");
                }

                try {
                    PrintWriter pw = new PrintWriter(
                        new OutputStreamWriter(
                            new FileOutputStream(other, true), charset));

                    pw.println(commandFromHistory(0) + ';');
                    pw.flush();
                    pw.close();
                } catch (Exception e) {
                    throw new BadSpecial("Failed to append to file '" + other
                                         + "':  " + e);
                }

                return;

            case 'i' :
                if (other == null) {
                    throw new BadSpecial("You must supply an SQL file name");
                }

                try {
                    SqlFile sf = new SqlFile(new File(other), false,
                                             userVars);

                    sf.recursed = true;

                    // Share the possiblyUncommitted state
                    sf.possiblyUncommitteds = possiblyUncommitteds;
                    sf.plMode               = plMode;

                    sf.execute(curConn, continueOnError);
                } catch (ContinueException ce) {
                    throw ce;
                } catch (BreakException be) {
                    String beMessage = be.getMessage();

                    if (beMessage != null &&!beMessage.equals("file")) {
                        throw be;
                    }
                } catch (QuitNow qe) {
                    throw qe;
                } catch (Exception e) {
                    throw new BadSpecial("Failed to execute SQL from file '"
                                         + other + "':  " + e.getMessage());
                }

                return;

            case 'p' :
                if (other == null) {
                    stdprintln(true);
                } else {
                    stdprintln(other, true);
                }

                return;

            case 'a' :
                if (other != null) {
                    curConn.setAutoCommit(
                        Boolean.valueOf(other).booleanValue());
                }

                stdprintln("Auto-commit is set to: "
                           + curConn.getAutoCommit());

                return;

            case 'b' :
                if (arg1.length() == 1) {
                    fetchBinary = true;

                    return;
                }

                if (arg1.charAt(1) == 'p') {
                    doPrepare = true;

                    return;
                }

                if ((arg1.charAt(1) != 'd' && arg1.charAt(1) != 'l')
                        || other == null) {
                    throw new BadSpecial("Malformatted binary command");
                }

                File file = new File(other);

                try {
                    if (arg1.charAt(1) == 'd') {
                        dump(file);
                    } else {
                        load(file);
                    }
                } catch (Exception e) {
                    throw new BadSpecial(
                        "Failed to load/dump binary  data to file '" + other
                        + "'");
                }

                return;

            case '*' :
            case 'c' :
                if (other != null) {

                    // But remember that we have to abort on some I/O errors.
                    continueOnError = Boolean.valueOf(other).booleanValue();
                }

                stdprintln("Continue-on-error is set to: " + continueOnError);

                return;

            case 's' :
                showHistory();

                return;

            case '-' :
                int     commandsAgo = 0;
                String  numStr;
                boolean executeMode = arg1.charAt(arg1.length() - 1) == ';';

                if (executeMode) {

                    // Trim off terminating ';'
                    arg1 = arg1.substring(0, arg1.length() - 1);
                }

                numStr = (arg1.length() == 1) ? null
                                              : arg1.substring(1,
                                              arg1.length());

                if (numStr == null) {
                    commandsAgo = 0;
                } else {
                    try {
                        commandsAgo = Integer.parseInt(numStr);
                    } catch (NumberFormatException nfe) {
                        throw new BadSpecial("Malformatted command number");
                    }
                }

                setBuf(commandFromHistory(commandsAgo));

                if (executeMode) {
                    processBuffer(";");
                } else {
                    stdprintln(
                        "RESTORED following command to buffer.  Enter \":?\" "
                        + "to see buffer commands:\n"
                        + commandFromHistory(0));
                }

                return;

            case '?' :
                stdprintln(HELP_TEXT);

                return;

            case '!' :
                InputStream stream;
                byte[]      ba         = new byte[1024];
                String      extCommand = ((arg1.length() == 1) ? ""
                                                               : arg1.substring(1)) + ((arg1.length() > 1 && other != null)
                                                                   ? " "
                                                                   : "") + ((other == null)
                                                                       ? ""
                                                                       : other);

                try {
                    Process proc = Runtime.getRuntime().exec(extCommand);

                    proc.getOutputStream().close();

                    int i;

                    stream = proc.getInputStream();

                    while ((i = stream.read(ba)) > 0) {
                        stdprint(new String(ba, 0, i));
                    }

                    stream.close();

                    stream = proc.getErrorStream();

                    while ((i = stream.read(ba)) > 0) {
                        errprint(new String(ba, 0, i));
                    }

                    stream.close();

                    if (proc.waitFor() != 0) {
                        throw new BadSpecial("External command failed: '"
                                             + extCommand + "'");
                    }
                } catch (Exception e) {
                    throw new BadSpecial("Failed to execute command '"
                                         + extCommand + "':  " + e);
                }

                return;

            case '.' :
                chunking = true;

                if (interactive) {
                    stdprintln("Enter RAW SQL.  No \\, :, * commands.  "
                               + "End with a line containing only \".\":");
                }

                return;
        }

        throw new BadSpecial("Unknown Special Command");
    }

    private static final char[] nonVarChars = {
        ' ', '\t', '=', '}', '\n', '\r'
    };

    /**
     * Returns index specifying 1 past end of a variable name.
     *
     * @param inString String containing a variable name
     * @param startIndex Index within inString where the variable name begins
     * @returns Index within inString, 1 past end of the variable name
     */
    static int pastName(String inString, int startIndex) {

        String workString = inString.substring(startIndex);
        int    e          = inString.length();    // Index 1 past end of var name.
        int    nonVarIndex;

        for (int i = 0; i < nonVarChars.length; i++) {
            nonVarIndex = workString.indexOf(nonVarChars[i]);

            if (nonVarIndex > -1 && nonVarIndex < e) {
                e = nonVarIndex;
            }
        }

        return startIndex + e;
    }

    /**
     * Deference PL variables.
     *
     * @throws SQLException  This is really an inappropriate exception
     * type.  Only using it because I don't have time to do things properly.
     */
    private String dereference(String inString,
                               boolean permitAlias) throws SQLException {

        String       varName, varValue;
        StringBuffer expandBuffer = new StringBuffer(inString);
        int          b, e;    // begin and end of name.  end really 1 PAST name
        int          nonVarIndex;

        if (permitAlias && inString.trim().charAt(0) == '/') {
            int slashIndex = inString.indexOf('/');

            e = pastName(inString.substring(slashIndex + 1), 0);

            // In this case, e is the exact length of the var name.
            if (e < 1) {
                throw new SQLException("Malformed PL alias use");
            }

            varName  = inString.substring(slashIndex + 1, slashIndex + 1 + e);
            varValue = (String) userVars.get(varName);

            if (varValue == null) {
                throw new SQLException("Undefined PL variable:  " + varName);
            }

            expandBuffer.replace(slashIndex, slashIndex + 1 + e,
                                 (String) userVars.get(varName));
        }

        String s;

        while (true) {
            s = expandBuffer.toString();
            b = s.indexOf("*{");

            if (b < 0) {

                // No more unexpanded variable uses
                break;
            }

            e = s.indexOf('}', b + 2);

            if (e == b + 2) {
                throw new SQLException("Empty PL variable name");
            }

            if (e < 0) {
                throw new SQLException("Unterminated PL variable name");
            }

            varName = s.substring(b + 2, e);

            if (!userVars.containsKey(varName)) {
                throw new SQLException("Use of undefined PL variable: "
                                       + varName);
            }

            expandBuffer.replace(b, e + 1, (String) userVars.get(varName));
        }

        return expandBuffer.toString();
    }

    public boolean plMode = false;

    //  PL variable name currently awaiting query output.
    private String  fetchingVar = null;
    private boolean silentFetch = false;
    private boolean fetchBinary = false;

    /**
     * Process a Process Language Command.
     * Nesting not supported yet.
     *
     * @param inString Complete command, less the leading '\' character.
     * @throws BadSpecial Runtime error()
     */
    private void processPL(String inString)
    throws BadSpecial, SqlToolError, SQLException {

        if (inString.length() < 1) {
            plMode = true;

            stdprintln("PL variable expansion mode is now on");

            return;
        }

        if (inString.charAt(0) == '?') {
            stdprintln(PL_HELP_TEXT);

            return;
        }

        if (plMode) {
            inString = dereference(inString, false);
        }

        StringTokenizer toker      = new StringTokenizer(inString);
        String          arg1       = toker.nextToken();
        String[]        tokenArray = null;

        // If user runs any PL command, we turn PL mode on.
        plMode = true;

        if (userVars == null) {
            userVars = new HashMap();
        }

        if (arg1.equals("end")) {
            throw new BadSpecial("PL end statements may only occur inside of "
                                 + "a PL block");
        }

        if (arg1.equals("continue")) {
            if (toker.hasMoreTokens()) {
                String s = toker.nextToken("").trim();

                if (s.equals("foreach") || s.equals("while")) {
                    throw new ContinueException(s);
                } else {
                    throw new BadSpecial(
                        "Bad continue statement."
                        + "You may use no argument or one of 'foreach', "
                        + "'while'");
                }
            }

            throw new ContinueException();
        }

        if (arg1.equals("break")) {
            if (toker.hasMoreTokens()) {
                String s = toker.nextToken("").trim();

                if (s.equals("foreach") || s.equals("if")
                        || s.equals("while") || s.equals("file")) {
                    throw new BreakException(s);
                } else {
                    throw new BadSpecial(
                        "Bad break statement."
                        + "You may use no argument or one of 'foreach', "
                        + "'if', 'while', 'file'");
                }
            }

            throw new BreakException();
        }

        if (arg1.equals("list") || arg1.equals("listvalue")) {
            String  s;
            boolean doValues = (arg1.equals("listvalue"));

            if (toker.countTokens() == 0) {
                stdprint(formatNicely(userVars, doValues));
            } else {
                tokenArray = getTokenArray(toker.nextToken(""));

                if (doValues) {
                    stdprintln("The outermost parentheses are not part of "
                               + "the values.");
                } else {
                    stdprintln("Showing variable names and length of values "
                               + "(use 'listvalue' to see values).");
                }

                for (int i = 0; i < tokenArray.length; i++) {
                    s = (String) userVars.get(tokenArray[i]);

                    stdprintln("    " + tokenArray[i] + ": "
                               + (doValues ? ("(" + s + ')')
                                           : Integer.toString(s.length())));
                }
            }

            return;
        }

        if (arg1.equals("dump") || arg1.equals("load")) {
            if (toker.countTokens() != 2) {
                throw new BadSpecial("Malformatted PL dump/load command");
            }

            String varName = toker.nextToken();
            File   file    = new File(toker.nextToken());

            try {
                if (arg1.equals("dump")) {
                    dump(varName, file);
                } else {
                    load(varName, file);
                }
            } catch (Exception e) {
                throw new BadSpecial("Failed to dump/load variable '"
                                     + varName + "' to file '" + file + "'");
            }

            return;
        }

        if (arg1.equals("prepare")) {
            if (toker.countTokens() != 1) {
                throw new BadSpecial("Malformatted prepare command");
            }

            String s = toker.nextToken();

            if (userVars.get(s) == null) {
                throw new SQLException("Use of unset PL variable: " + s);
            }

            prepareVar = s;
            doPrepare  = true;

            return;
        }

        if (arg1.equals("foreach")) {
            if (toker.countTokens() < 2) {
                throw new BadSpecial("Malformatted PL foreach command (1)");
            }

            String varName   = toker.nextToken();
            String parenExpr = toker.nextToken("").trim();

            if (parenExpr.length() < 2 || parenExpr.charAt(0) != '('
                    || parenExpr.charAt(parenExpr.length() - 1) != ')') {
                throw new BadSpecial("Malformatted PL foreach command (2)");
            }

            String[] values = getTokenArray(parenExpr.substring(1,
                parenExpr.length() - 1));
            File   tmpFile = null;
            String varVal;

            try {
                tmpFile = plBlockFile("foreach");
            } catch (IOException ioe) {
                throw new BadSpecial(
                    "Failed to write given PL block temp file: " + ioe);
            }

            String origval = (String) userVars.get(varName);

            try {
                SqlFile sf;

                for (int i = 0; i < values.length; i++) {
                    try {
                        varVal = values[i];

                        userVars.put(varName, varVal);

                        sf          = new SqlFile(tmpFile, false, userVars);
                        sf.plMode   = true;
                        sf.recursed = true;

                        // Share the possiblyUncommitted state
                        sf.possiblyUncommitteds = possiblyUncommitteds;

                        sf.execute(curConn, continueOnError);
                    } catch (ContinueException ce) {
                        String ceMessage = ce.getMessage();

                        if (ceMessage != null
                                &&!ceMessage.equals("foreach")) {
                            throw ce;
                        }
                    }
                }
            } catch (BreakException be) {
                String beMessage = be.getMessage();

                if (beMessage != null &&!beMessage.equals("foreach")) {
                    throw be;
                }
            } catch (QuitNow qe) {
                throw qe;
            } catch (Exception e) {
                throw new BadSpecial("Failed to execute SQL from PL block.  "
                                     + e.getMessage());
            }

            if (origval == null) {
                userVars.remove(varName);
            } else {
                userVars.put(varName, origval);
            }

            if (tmpFile != null &&!tmpFile.delete()) {
                throw new BadSpecial(
                    "Error occurred while trying to remove temp file '"
                    + tmpFile + "'");
            }

            return;
        }

        if (arg1.equals("if")) {
            if (toker.countTokens() < 1) {
                throw new BadSpecial("Malformatted PL if command (1)");
            }

            String parenExpr = toker.nextToken("").trim();

            if (parenExpr.length() < 2 || parenExpr.charAt(0) != '('
                    || parenExpr.charAt(parenExpr.length() - 1) != ')') {
                throw new BadSpecial("Malformatted PL if command (2)");
            }

            String[] values = getTokenArray(parenExpr.substring(1,
                parenExpr.length() - 1));
            File tmpFile = null;

            try {
                tmpFile = plBlockFile("if");
            } catch (IOException ioe) {
                throw new BadSpecial(
                    "Failed to write given PL block temp file: " + ioe);
            }

            try {
                if (eval(values)) {
                    SqlFile sf = new SqlFile(tmpFile, false, userVars);

                    sf.plMode   = true;
                    sf.recursed = true;

                    // Share the possiblyUncommitted state
                    sf.possiblyUncommitteds = possiblyUncommitteds;

                    sf.execute(curConn, continueOnError);
                }
            } catch (BreakException be) {
                String beMessage = be.getMessage();

                if (beMessage == null ||!beMessage.equals("if")) {
                    throw be;
                }
            } catch (ContinueException ce) {
                throw ce;
            } catch (QuitNow qe) {
                throw qe;
            } catch (BadSpecial bs) {
                throw new BadSpecial("Malformatted PL if command (3): " + bs);
            } catch (Exception e) {
                throw new BadSpecial("Failed to execute SQL from PL block.  "
                                     + e.getMessage());
            }

            if (tmpFile != null &&!tmpFile.delete()) {
                throw new BadSpecial(
                    "Error occurred while trying to remove temp file '"
                    + tmpFile + "'");
            }

            return;
        }

        if (arg1.equals("while")) {
            if (toker.countTokens() < 1) {
                throw new BadSpecial("Malformatted PL while command (1)");
            }

            String parenExpr = toker.nextToken("").trim();

            if (parenExpr.length() < 2 || parenExpr.charAt(0) != '('
                    || parenExpr.charAt(parenExpr.length() - 1) != ')') {
                throw new BadSpecial("Malformatted PL while command (2)");
            }

            String[] values = getTokenArray(parenExpr.substring(1,
                parenExpr.length() - 1));
            File tmpFile = null;

            try {
                tmpFile = plBlockFile("while");
            } catch (IOException ioe) {
                throw new BadSpecial(
                    "Failed to write given PL block temp file: " + ioe);
            }

            try {
                SqlFile sf;

                while (eval(values)) {
                    try {
                        sf          = new SqlFile(tmpFile, false, userVars);
                        sf.recursed = true;

                        // Share the possiblyUncommitted state
                        sf.possiblyUncommitteds = possiblyUncommitteds;
                        sf.plMode               = true;

                        sf.execute(curConn, continueOnError);
                    } catch (ContinueException ce) {
                        String ceMessage = ce.getMessage();

                        if (ceMessage != null &&!ceMessage.equals("while")) {
                            throw ce;
                        }
                    }
                }
            } catch (BreakException be) {
                String beMessage = be.getMessage();

                if (beMessage != null &&!beMessage.equals("while")) {
                    throw be;
                }
            } catch (QuitNow qe) {
                throw qe;
            } catch (BadSpecial bs) {
                throw new BadSpecial("Malformatted PL while command (3): "
                                     + bs);
            } catch (Exception e) {
                throw new BadSpecial("Failed to execute SQL from PL block.  "
                                     + e.getMessage());
            }

            if (tmpFile != null &&!tmpFile.delete()) {
                throw new BadSpecial(
                    "Error occurred while trying to remove temp file '"
                    + tmpFile + "'");
            }

            return;
        }

        /* Since we don't want to permit both "* VARNAME = X" and
         * "* VARNAME=X" (i.e., whitespace is OPTIONAL in both positions),
         * we can't use the Tokenzier.  Therefore, start over again with
         * the inString. */
        toker = null;

        int    index    = pastName(inString, 0);
        int    inLength = inString.length();
        String varName  = inString.substring(0, index);

        while (index + 1 < inLength
                && (inString.charAt(index) == ' '
                    || inString.charAt(index) == '\t')) {
            index++;
        }

        // index now set to the next non-whitespace AFTER the var name.
        if (index + 1 > inLength) {
            throw new BadSpecial("Unterminated PL variable definition");
        }

        char   operator  = inString.charAt(index);
        String remainder = inString.substring(index + 1);

        switch (inString.charAt(index)) {

            case '_' :
                silentFetch = true;
            case '~' :
                if (remainder.length() > 0) {
                    throw new BadSpecial(
                        "PL ~/_ set commands take no other args");
                }

                userVars.remove(varName);

                fetchingVar = varName;

                return;

            case '=' :
                if (fetchingVar != null && fetchingVar.equals(varName)) {
                    fetchingVar = null;
                }

                if (remainder.length() > 0) {
                    userVars.put(varName,
                                 inString.substring(index + 1).trim());
                } else {
                    userVars.remove(varName);
                }

                return;
        }

        throw new BadSpecial("Unknown PL command (3)");
    }

    /*
     * Read a PL block into a new temp file.
     *
     * WARNING!!! foreach blocks are not yet smart about comments
     * and strings.  We just look for a line beginning with a PL "end"
     * command without worrying about comments or quotes (for now).
     *
     * WARNING!!! This is very rudimentary.
     * Users give up all editing and feedback capabilities while
     * in the foreach loop.
     * A better solution would be to pass current input stream to a
     * new SqlFile.execute() with a mode whereby commands are written
     * to a separate history but not executed.
     */
    private File plBlockFile(String type) throws IOException, SqlToolError {

        String          s;
        StringTokenizer toker;

        // Have already read the if/while/foreach statement, so we are already
        // at nest level 1.  When we reach nestlevel 1 (read 1 net "end"
        // statement), we're at level 0 and return.
        int    nestlevel = 1;
        String curPlCommand;

        if (type == null
                || ((!type.equals("foreach")) && (!type.equals("if"))
                    && (!type.equals("while")))) {
            throw new RuntimeException(
                "Assertion failed.  Unsupported PL block type:  " + type);
        }

        File tmpFile = File.createTempFile("sqltool-", ".sql");
        PrintWriter pw = new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(tmpFile), charset));

        pw.println("/* " + (new java.util.Date()) + ". "
                   + getClass().getName() + " PL block. */\n");

        while (true) {
            s = br.readLine();

            if (s == null) {
                errprintln("Unterminated '" + type + "' PL block");

                throw new SqlToolError("Unterminated '" + type
                                       + "' PL block");
            }

            curLinenum++;

            if (s.trim().length() > 1 && s.trim().charAt(0) == '*') {
                toker        = new StringTokenizer(s.trim().substring(1));
                curPlCommand = toker.nextToken();

                // PL COMMAND of some sort.
                if (curPlCommand.equals(type)) {
                    nestlevel++;
                } else if (curPlCommand.equals("end")) {
                    if (toker.countTokens() < 1) {
                        errprintln("PL end statement requires arg of "
                                   + "'foreach' or 'if' or 'while' (1)");

                        throw new SqlToolError(
                            "PL end statement requires arg "
                            + " of 'foreach' or 'if' or 'while' (1)");
                    }

                    String inType = toker.nextToken();

                    if (inType.equals(type)) {
                        nestlevel--;

                        if (nestlevel < 1) {
                            break;
                        }
                    }

                    if ((!inType.equals("foreach")) && (!inType.equals("if"))
                            && (!inType.equals("while"))) {
                        errprintln("PL end statement requires arg of "
                                   + "'foreach' or 'if' or 'while' (2)");

                        throw new SqlToolError(
                            "PL end statement requires arg of "
                            + "'foreach' or 'if' or 'while' (2)");
                    }
                }
            }

            pw.println(s);
        }

        pw.flush();
        pw.close();

        return tmpFile;
    }

    /**
     * Wrapper methods so don't need to call x(..., false) in most cases.
     */
    private void stdprintln() {
        stdprintln(false);
    }

    private void stdprint(String s) {
        stdprint(s, false);
    }

    private void stdprintln(String s) {
        stdprintln(s, false);
    }

    /**
     * Encapsulates normal output.
     *
     * Conditionally HTML-ifies output.
     */
    private void stdprintln(boolean queryOutput) {

        if (htmlMode) {
            psStd.println("<BR>");
        } else {
            psStd.println();
        }

        if (queryOutput && pwQuery != null) {
            if (htmlMode) {
                pwQuery.println("<BR>");
            } else {
                pwQuery.println();
            }

            pwQuery.flush();
        }
    }

    /**
     * Encapsulates error output.
     *
     * Conditionally HTML-ifies error output.
     */
    private void errprint(String s) {

        psErr.print(htmlMode
                    ? ("<DIV style='color:white; background: red; "
                       + "font-weight: bold'>" + s + "</DIV>")
                    : s);
    }

    /**
     * Encapsulates error output.
     *
     * Conditionally HTML-ifies error output.
     */
    private void errprintln(String s) {

        psErr.println(htmlMode
                      ? ("<DIV style='color:white; background: red; "
                         + "font-weight: bold'>" + s + "</DIV>")
                      : s);
    }

    /**
     * Encapsulates normal output.
     *
     * Conditionally HTML-ifies output.
     */
    private void stdprint(String s, boolean queryOutput) {

        psStd.print(htmlMode ? ("<P>" + s + "</P>")
                             : s);

        if (queryOutput && pwQuery != null) {
            pwQuery.print(htmlMode ? ("<P>" + s + "</P>")
                                   : s);
            pwQuery.flush();
        }
    }

    /**
     * Encapsulates normal output.
     *
     * Conditionally HTML-ifies output.
     */
    private void stdprintln(String s, boolean queryOutput) {

        psStd.println(htmlMode ? ("<P>" + s + "</P>")
                               : s);

        if (queryOutput && pwQuery != null) {
            pwQuery.println(htmlMode ? ("<P>" + s + "</P>")
                                     : s);
            pwQuery.flush();
        }
    }

    // Just because users may be used to seeing "[null]" in normal
    // SqlFile output, we use the same default value for null in CSV
    // files, but this CSV null representation can be changed to anything.
    private static final String DEFAULT_NULL_REP = "[null]";
    private static final String DEFAULT_ROW_DELIM =
        System.getProperty("line.separator");
    private static final String DEFAULT_COL_DELIM = "|";
    private static final int    DEFAULT_ELEMENT   = 0,
                                HSQLDB_ELEMENT    = 1,
                                ORACLE_ELEMENT    = 2
    ;

    // These do not specify order listed, just inclusion.
    private static final int[] listMDSchemaCols = { 1 };
    private static final int[] listMDIndexCols  = {
        2, 6, 3, 9, 4, 10, 11
    };

    /** Column numbering starting at 1. */
    private static final int[][] listMDTableCols = {
        {
            2, 3
        },    // Default
        {
            2, 3
        },    // HSQLDB
        {
            2, 3
        },    // Oracle
    };

    /**
     * SYS and SYSTEM are the only base system accounts in Oracle, however,
     * from an empirical perspective, all of these other accounts are
     * system accounts because <UL>
     * <LI> they are hidden from the casual user
     * <LI> they are created by the installer at installation-time
     * <LI> they are used automatically by the Oracle engine when the
     *      specific Oracle sub-product is used
     * <LI> the accounts should not be <I>messed with</I> by database users
     * <LI> the accounts should certainly not be used if the specific
     *      Oracle sub-product is going to be used.
     * </UL>
     *
     * General advice:  If you aren't going to use an Oracle sub-product,
     * then <B>don't install it!</B>
     * Don't blindly accept default when running OUI.
     *
     * If users also see accounts that they didn't create with names like
     * SCOTT, ADAMS, JONES, CLARK, BLAKE, OE, PM, SH, QS, QS_*, these
     * contain sample data and the schemas can safely be removed.
     */
    private static final String[] oracleSysSchemas = {
        "SYS", "SYSTEM", "OUTLN", "DBSNMP", "OUTLN", "MDSYS", "ORDSYS",
        "ORDPLUGINS", "CTXSYS", "DSSYS", "PERFSTAT", "WKPROXY", "WKSYS",
        "WMSYS", "XDB", "ANONYMOUS", "ODM", "ODM_MTR", "OLAPSYS", "TRACESVR",
        "REPADMIN"
    };

    /**
     * Lists available database tables.
     *
     * When a filter is given, we assume that there are no lower-case
     * characters in the object names (which would require "quotes" when
     * creating them).
     *
     * @throws BadSpecial
     */
    private void listTables(char c, String inFilter) throws BadSpecial {

        String   schema  = null;
        int[]    listSet = null;
        String[] types   = null;

        /** For workaround for \T for Oracle */
        String[] additionalSchemas = null;

        /** This is for specific non-getTable() queries */
        Statement statement = null;
        ResultSet rs        = null;
        String    narrower  = "";
        /*
         * Doing case-sensitive filters now, for greater portability.
        String                    filter = ((inFilter == null)
                                          ? null : inFilter.toUpperCase());
         */
        String filter = inFilter;

        try {
            DatabaseMetaData md            = curConn.getMetaData();
            String           dbProductName = md.getDatabaseProductName();

            //System.err.println("DB NAME = (" + dbProductName + ')');
            // Database-specific table filtering.
            String excludePrefix = null;

            /* 3 Types of actions:
             *    1) Special handling.  Return from the "case" block directly.
             *    2) Execute a specific query.  Set statement in the "case".
             *    3) Otherwise, set filter info for dbmd.getTable() in the
             *       "case".
             */
            types = new String[1];

            switch (c) {

                case '*' :
                    types = null;
                    break;

                case 'S' :
                    if (dbProductName.indexOf("Oracle") > -1) {
                        System.err.println(
                            "*** WARNING:\n*** Listing tables in "
                            + "system-supplied schemas since\n*** Oracle"
                            + "(TM) doesn't return a JDBC system table list.");

                        types[0]          = "TABLE";
                        schema            = "SYS";
                        additionalSchemas = oracleSysSchemas;
                    } else {
                        types[0] = "SYSTEM TABLE";
                    }
                    break;

                case 's' :
                    if (dbProductName.indexOf("HSQL") > -1) {

                        //  HSQLDB does not consider Sequences as "tables",
                        //  hence we do not list them in
                        //  DatabaseMetaData.getTables().
                        if (filter != null
                                && filter.charAt(filter.length() - 1)
                                   == '.') {
                            narrower =
                                "\nWHERE sequence_schema = '"
                                + filter.substring(0, filter.length() - 1)
                                + "'";
                            filter = null;
                        }

                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT sequence_schema, sequence_name FROM "
                            + "information_schema.system_sequences"
                            + narrower);
                    } else {
                        types[0] = "SEQUENCE";
                    }
                    break;

                case 'r' :
                    if (dbProductName.indexOf("HSQL") > -1) {
                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT authorization_name FROM "
                            + "information_schema.system_authorizations\n"
                            + "WHERE authorization_type = 'ROLE'\n"
                            + "ORDER BY authorization_name");
                    } else if (dbProductName.indexOf(
                            "Adaptive Server Enterprise") > -1) {

                        // This is the basic Sybase server.  Sybase also has
                        // their "Anywhere", ASA (for embedded), and replication
                        // databases, but I don't know the Metadata strings for
                        // those.
                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT name FROM syssrvroles ORDER BY name");
                    } else {
                        throw new BadSpecial(
                            "SqlFile does not yet support "
                            + "\\dr for your database vendor");
                    }
                    break;

                case 'u' :
                    if (dbProductName.indexOf("HSQL") > -1) {
                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT user, admin FROM "
                            + "information_schema.system_users\n"
                            + "ORDER BY user");
                    } else if (dbProductName.indexOf("Oracle") > -1) {
                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT username, created FROM all_users "
                            + "ORDER BY username");
                    } else if (dbProductName.indexOf("PostgreSQL") > -1) {
                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT usename, usesuper FROM pg_catalog.pg_user "
                            + "ORDER BY usename");
                    } else if (dbProductName.indexOf(
                            "Adaptive Server Enterprise") > -1) {

                        // This is the basic Sybase server.  Sybase also has
                        // their "Anywhere", ASA (for embedded), and replication
                        // databases, but I don't know the Metadata strings for
                        // those.
                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT name, accdate, fullname FROM syslogins "
                            + "ORDER BY name");
                    } else {
                        throw new BadSpecial(
                            "SqlFile does not yet support "
                            + "\\du for your database vendor");
                    }
                    break;

                case 'a' :
                    if (dbProductName.indexOf("HSQL") > -1) {

                        //  HSQLDB Aliases are not the same things as the
                        //  aliases listed in DatabaseMetaData.getTables().
                        if (filter != null
                                && filter.charAt(filter.length() - 1)
                                   == '.') {
                            narrower =
                                "\nWHERE alias_schem = '"
                                + filter.substring(0, filter.length() - 1)
                                + "'";
                            filter = null;
                        }

                        statement = curConn.createStatement();

                        statement.execute(
                            "SELECT alias_schem, alias FROM "
                            + "information_schema.system_aliases" + narrower);
                    } else {
                        types[0] = "ALIAS";
                    }
                    break;

                case 't' :
                    excludeSysSchemas = (dbProductName.indexOf("Oracle")
                                         > -1);
                    types[0] = "TABLE";
                    break;

                case 'v' :
                    types[0] = "VIEW";
                    break;

                case 'n' :
                    rs = md.getSchemas();

                    if (rs == null) {
                        throw new BadSpecial(
                            "Failed to get metadata from database");
                    }

                    displayResultSet(null, rs, listMDSchemaCols, filter);

                    return;

                case 'i' :

                    // Some databases require to specify table, some don't.
                    /*
                    if (filter == null) {
                        throw new BadSpecial("You must specify the index's "
                                + "table as argument to \\di");
                    }
                     */
                    schema = null;

                    String table = null;

                    if (filter != null) {
                        int dotat = filter.indexOf('.');

                        schema = ((dotat > 0) ? filter.substring(0, dotat)
                                              : null);

                        if (dotat < filter.length() - 1) {

                            // Not a schema-only specifier
                            table = ((dotat > 0) ? filter.substring(dotat + 1)
                                                 : filter);
                        }

                        filter = null;
                    }

                    // N.b. Oracle incorrectly reports the INDEX SCHEMA as
                    // the TABLE SCHEMA.  The Metadata structure seems to
                    // be designed with the assumption that the INDEX schema
                    // will be the same as the TABLE schema.
                    rs = md.getIndexInfo(null, schema, table, false, true);

                    if (rs == null) {
                        throw new BadSpecial(
                            "Failed to get metadata from database");
                    }

                    displayResultSet(null, rs, listMDIndexCols, null);

                    return;

                default :
                    throw new BadSpecial("Unknown describe option: '" + c
                                         + "'");
            }

            if (statement == null) {
                if (dbProductName.indexOf("HSQL") > -1) {
                    listSet = listMDTableCols[HSQLDB_ELEMENT];
                } else if (dbProductName.indexOf("Oracle") > -1) {
                    listSet = listMDTableCols[ORACLE_ELEMENT];
                } else {
                    listSet = listMDTableCols[DEFAULT_ELEMENT];
                }

                if (schema == null && filter != null
                        && filter.charAt(filter.length() - 1) == '.') {
                    schema = filter.substring(0, filter.length() - 1);
                    filter = null;
                }
            }

            rs = ((statement == null)
                  ? md.getTables(null, schema, null, types)
                  : statement.getResultSet());

            if (rs == null) {
                throw new BadSpecial("Failed to get metadata from database");
            }

            displayResultSet(null, rs, listSet, filter);

            if (additionalSchemas != null) {
                for (int i = 1; i < additionalSchemas.length; i++) {
                    /*
                     * Inefficient, but we have to do each successful query
                     * twice in order to prevent calling displayResultSet
                     * for empty/non-existent schemas
                     */
                    rs = md.getTables(null, additionalSchemas[i], null,
                                      types);

                    if (rs == null) {
                        throw new BadSpecial(
                            "Failed to get metadata from database for '"
                            + additionalSchemas[i] + "'");
                    }

                    if (!rs.next()) {
                        continue;
                    }

                    displayResultSet(
                        null,
                        md.getTables(
                            null, additionalSchemas[i], null, types), listSet, filter);
                }
            }
        } catch (SQLException se) {
            throw new BadSpecial("Failure getting MetaData: " + se);
        } catch (NullPointerException npe) {
            throw new BadSpecial("Failure getting MetaData (NPE)");
        } finally {
            excludeSysSchemas = false;

            if (rs != null) {
                rs = null;
            }

            if (statement != null) {
                try {
                    statement.close();
                } catch (Exception e) {}

                statement = null;
            }
        }
    }

    private boolean excludeSysSchemas = false;

    /**
     * Process the current command as an SQL Statement
     */
    private void processSQL() throws SQLException {

        // Really don't know whether to take the network latency hit here
        // in order to check autoCommit in order to set
        // possiblyUncommitteds more accurately.
        // I'm going with "NO" for now, since autoCommit will usually be off.
        // If we do ever check autocommit, we have to keep track of the
        // autocommit state when every SQL statement is run, since I may
        // be able to have uncommitted DML, turn autocommit on, then run
        // other DDL with autocommit on.  As a result, I could be running
        // SQL commands with autotommit on but still have uncommitted mods.
        String    sql       = (plMode ? dereference(curCommand, true)
                                      : curCommand);
        Statement statement = null;

        if (doPrepare) {
            if (sql.indexOf('?') < 1) {
                throw new SQLException(
                    "Prepared statements must contain one '?'");
            }

            doPrepare = false;

            PreparedStatement ps = curConn.prepareStatement(sql);

            if (prepareVar == null) {
                if (binBuffer == null) {
                    throw new SQLException("Binary SqlFile buffer is empty");
                }

                ps.setBytes(1, binBuffer);
            } else {
                String val = (String) userVars.get(prepareVar);

                if (val == null) {
                    throw new SQLException("PL Variable '" + prepareVar
                                           + "' is empty");
                }

                prepareVar = null;

                ps.setString(1, val);
            }

            ps.executeUpdate();

            statement = ps;
        } else {
            statement = curConn.createStatement();

            statement.execute(sql);
        }

        possiblyUncommitteds.set(true);

        try {
            displayResultSet(statement, statement.getResultSet(), null, null);
        } finally {
            try {
                statement.close();
            } catch (Exception e) {}
        }
    }

    /**
     * Display the given result set for user.
     * The last 3 params are to narrow down records and columns where
     * that can not be done with a where clause (like in metadata queries).
     *
     * @param statement The SQL Statement that the result set is for.
     *                  (This is so we can get the statement's update count.
     *                  Can be null for non-update queries.)
     * @param r         The ResultSet to display.
     * @param incCols   Optional list of which columns to include (i.e., if
     *                  given, then other columns will be skipped).
     * @param incFilter Optional case-insensitive substring.
     *                  Rows are skipped which to not contain this substring.
     */
    private void displayResultSet(Statement statement, ResultSet r,
                                  int[] incCols,
                                  String filter) throws SQLException {

        java.sql.Timestamp ts;
        int                updateCount = (statement == null) ? -1
                                                             : statement
                                                                 .getUpdateCount();
        boolean            silent      = silentFetch;
        boolean            binary      = fetchBinary;

        silentFetch = false;
        fetchBinary = false;

        if (excludeSysSchemas) {
            stdprintln(
                "*** WARNING:\n*** Omitting tables from system-supplied "
                + "schemas\n*** (because Oracle(TM) "
                + "doesn't differentiate them to JDBC).");
        }

        switch (updateCount) {

            case -1 :
                if (r == null) {
                    stdprintln("No result", true);

                    break;
                }

                ResultSetMetaData m        = r.getMetaData();
                int               cols     = m.getColumnCount();
                int               incCount = (incCols == null) ? cols
                                                               : incCols
                                                                   .length;
                String            val;
                ArrayList         rows        = new ArrayList();
                String[]          headerArray = null;
                String[]          fieldArray;
                int[]             maxWidth = new int[incCount];
                int               insi;
                boolean           skip;
                boolean           ok;

                // STEP 1: GATHER DATA
                if (!htmlMode) {
                    for (int i = 0; i < maxWidth.length; i++) {
                        maxWidth[i] = 0;
                    }
                }

                boolean[] rightJust = new boolean[incCount];
                int[]     dataType  = new int[incCount];
                boolean[] autonulls = new boolean[incCount];

                insi        = -1;
                headerArray = new String[incCount];

                for (int i = 1; i <= cols; i++) {
                    if (incCols != null) {
                        skip = true;

                        for (int j = 0; j < incCols.length; j++) {
                            if (i == incCols[j]) {
                                skip = false;
                            }
                        }

                        if (skip) {
                            continue;
                        }
                    }

                    headerArray[++insi] = m.getColumnLabel(i);
                    dataType[insi]      = m.getColumnType(i);
                    rightJust[insi]     = false;
                    autonulls[insi]     = true;

                    switch (dataType[insi]) {

                        case java.sql.Types.BIGINT :
                        case java.sql.Types.BIT :
                        case java.sql.Types.DECIMAL :
                        case java.sql.Types.DOUBLE :
                        case java.sql.Types.FLOAT :
                        case java.sql.Types.INTEGER :
                        case java.sql.Types.NUMERIC :
                        case java.sql.Types.REAL :
                        case java.sql.Types.SMALLINT :
                        case java.sql.Types.TINYINT :
                            rightJust[insi] = true;
                            break;

                        case java.sql.Types.VARBINARY :
                        case java.sql.Types.VARCHAR :
                            autonulls[insi] = false;
                            break;
                    }

                    if (htmlMode) {
                        continue;
                    }

                    if (headerArray[insi].length() > maxWidth[insi]) {
                        maxWidth[insi] = headerArray[insi].length();
                    }
                }

                boolean filteredOut;

                EACH_ROW:
                while (r.next()) {
                    fieldArray  = new String[incCount];
                    insi        = -1;
                    filteredOut = filter != null;

                    for (int i = 1; i <= cols; i++) {

                        // This is the only case where we can save a data
                        // read by recognizing we don't need this datum early.
                        if (incCols != null) {
                            skip = true;

                            for (int j = 0; j < incCols.length; j++) {
                                if (i == incCols[j]) {
                                    skip = false;
                                }
                            }

                            if (skip) {
                                continue;
                            }
                        }

                        // This row may still be ditched, but it is now
                        // certain that we need to increment the fieldArray
                        // index.
                        ++insi;

                        if (!canDisplayType(dataType[insi])) {
                            binary = true;
                        }

                        val = null;

                        if (!binary) {

                            // The special formatting for Timestamps is
                            // because the most popular current databases
                            // are VERY inconsistent about the format
                            // returned by getString() for a Timestamp field.
                            // In many cases, the output is very user-
                            // unfriendly.  However, getTimestamp().toString()
                            // is consistent and convenient.
                            if (dataType[insi] == java.sql.Types.TIMESTAMP) {
                                ts  = r.getTimestamp(i);
                                val = ((ts == null) ? null
                                                    : ts.toString());
                            } else {
                                val = r.getString(i);

                                // If we tried to get a String but it failed,
                                // try getting it with a String Stream
                                if (val == null) {
                                    try {
                                        val = streamToString(
                                            r.getAsciiStream(i));
                                    } catch (Exception e) {}
                                }
                            }
                        }

                        if (binary || (val == null &&!r.wasNull())) {
                            if (pwCsv != null) {

                                // TODO:  Should throw something other than
                                // a SQLException
                                throw new SQLException(
                                    "Table has a binary column.  CSV files "
                                    + "are text, not binary, files");
                            }

                            // DB has a value but we either explicitly want
                            // it as binary, or we failed to get it as String.
                            try {
                                binBuffer =
                                    streamToBytes(r.getBinaryStream(i));
                            } catch (IOException ioe) {
                                throw new SQLException(
                                    "Failed to read value using stream");
                            }

                            stdprintln("Read " + binBuffer.length
                                       + " bytes from field '"
                                       + headerArray[insi] + "' (type "
                                       + sqlTypeToString(dataType[insi])
                                       + ") into binary buffer");

                            return;
                        }

                        if (excludeSysSchemas && i == 2) {
                            for (int z = 0; z < oracleSysSchemas.length;
                                    z++) {
                                if (val.equals(oracleSysSchemas[z])) {
                                    filteredOut = true;

                                    break;
                                }
                            }
                        }

                        if (fetchingVar != null) {
                            userVars.put(fetchingVar, val);

                            fetchingVar = null;
                        }

                        if (silent) {
                            return;
                        }

                        // We do not omit rows here.  We collect information
                        // so we can make the decision after all rows are
                        // read in.
                        if (filter != null
                                && (val == null
                                    || val.indexOf(filter) > -1)) {
                            filteredOut = false;
                        }

                        ///////////////////////////////
                        // A little tricky here.  fieldArray[] MUST get set.
                        if (val == null && pwCsv == null) {
                            if (dataType[insi] == java.sql.Types.VARCHAR) {
                                fieldArray[insi] = (htmlMode ? "<I>null</I>"
                                                             : "[null]");
                            } else {
                                fieldArray[insi] = "";
                            }
                        } else {
                            fieldArray[insi] = val;
                        }

                        ///////////////////////////////
                        if (htmlMode || pwCsv != null) {
                            continue;
                        }

                        if (fieldArray[insi].length() > maxWidth[insi]) {
                            maxWidth[insi] = fieldArray[insi].length();
                        }
                    }

                    if (!filteredOut) {
                        rows.add(fieldArray);
                    }
                }

                // STEP 2: DISPLAY DATA  (= 2a OR 2b)
                // STEP 2a (Non-CSV)
                if (pwCsv == null) {
                    condlPrintln("<TABLE border='1'>", true);

                    if (incCount > 1) {
                        condlPrint(htmlRow(COL_HEAD) + '\n' + PRE_TD, true);

                        for (int i = 0; i < headerArray.length; i++) {
                            condlPrint("<TD>" + headerArray[i] + "</TD>",
                                       true);
                            condlPrint(((i > 0) ? spaces(2)
                                                : "") + pad(
                                                    headerArray[i],
                                                    maxWidth[i],
                                                    rightJust[i],
                                                    (i < headerArray.length
                                                     - 1 || rightJust[i])), false);
                        }

                        condlPrintln("\n" + PRE_TR + "</TR>", true);
                        condlPrintln("", false);

                        if (!htmlMode) {
                            for (int i = 0; i < headerArray.length; i++) {
                                condlPrint(((i > 0) ? spaces(2)
                                                    : "") + divider(
                                                        maxWidth[i]), false);
                            }

                            condlPrintln("", false);
                        }
                    }

                    for (int i = 0; i < rows.size(); i++) {
                        condlPrint(htmlRow(((i % 2) == 0) ? COL_EVEN
                                                          : COL_ODD) + '\n'
                                                          + PRE_TD, true);

                        fieldArray = (String[]) rows.get(i);

                        for (int j = 0; j < fieldArray.length; j++) {
                            condlPrint("<TD>" + fieldArray[j] + "</TD>",
                                       true);
                            condlPrint(((j > 0) ? spaces(2)
                                                : "") + pad(
                                                    fieldArray[j],
                                                    maxWidth[j],
                                                    rightJust[j],
                                                    (j < fieldArray.length
                                                     - 1 || rightJust[j])), false);
                        }

                        condlPrintln("\n" + PRE_TR + "</TR>", true);
                        condlPrintln("", false);
                    }

                    condlPrintln("</TABLE>", true);

                    if (rows.size() != 1) {
                        stdprintln("\n" + rows.size() + " rows", true);
                    }

                    condlPrintln("<HR>", true);

                    break;
                }

                // STEP 2b (CSV)
                if (incCount > 0) {
                    for (int i = 0; i < headerArray.length; i++) {
                        csvSafe(headerArray[i]);
                        pwCsv.print(headerArray[i]);

                        if (i < headerArray.length - 1) {
                            pwCsv.print(csvColDelim);
                        }
                    }

                    pwCsv.print(csvRowDelim);
                }

                for (int i = 0; i < rows.size(); i++) {
                    fieldArray = (String[]) rows.get(i);

                    for (int j = 0; j < fieldArray.length; j++) {
                        csvSafe(fieldArray[j]);
                        pwCsv.print((fieldArray[j] == null)
                                    ? (autonulls[j] ? ""
                                                    : csvNullRep)
                                    : fieldArray[j]);

                        if (j < fieldArray.length - 1) {
                            pwCsv.print(csvColDelim);
                        }
                    }

                    pwCsv.print(csvRowDelim);
                }

                stdprintln(Integer.toString(rows.size())
                           + " rows read from DB");
                break;

            default :
                if (fetchingVar != null) {
                    userVars.put(fetchingVar, Integer.toString(updateCount));

                    fetchingVar = null;
                }

                if (updateCount != 0) {
                    stdprintln(Integer.toString(updateCount) + " row"
                               + ((updateCount == 1) ? ""
                                                     : "s") + " updated");
                }
                break;
        }
    }

    private static final int    COL_HEAD = 0,
                                COL_ODD  = 1,
                                COL_EVEN = 2
    ;
    private static final String PRE_TR   = spaces(4);
    private static final String PRE_TD   = spaces(8);

    /**
     * Print a properly formatted HTML &lt;TR&gt; command for the given
     * situation.
     *
     * @param colType Column type:  COL_HEAD, COL_ODD or COL_EVEN.
     */
    private static String htmlRow(int colType) {

        switch (colType) {

            case COL_HEAD :
                return PRE_TR + "<TR style='font-weight: bold;'>";

            case COL_ODD :
                return PRE_TR
                       + "<TR style='background: #94d6ef; font: normal "
                       + "normal 10px/10px Arial, Helvitica, sans-serif;'>";

            case COL_EVEN :
                return PRE_TR
                       + "<TR style='background: silver; font: normal "
                       + "normal 10px/10px Arial, Helvitica, sans-serif;'>";
        }

        return null;
    }

    /**
     * Returns a divider of hypens of requested length.
     *
     * @param len Length of output String.
     */
    private static String divider(int len) {
        return (len > DIVIDER.length()) ? DIVIDER
                                        : DIVIDER.substring(0, len);
    }

    /**
     * Returns a String of spaces of requested length.
     *
     * @param len Length of output String.
     */
    private static String spaces(int len) {
        return (len > SPACES.length()) ? SPACES
                                       : SPACES.substring(0, len);
    }

    /**
     * Pads given input string out to requested length with space
     * characters.
     *
     * @param inString Base string.
     * @param fulllen  Output String length.
     * @param rightJustify  True to right justify, false to left justify.
     */
    private static String pad(String inString, int fulllen,
                              boolean rightJustify, boolean doPad) {

        if (!doPad) {
            return inString;
        }

        int len = fulllen - inString.length();

        if (len < 1) {
            return inString;
        }

        String pad = spaces(len);

        return ((rightJustify ? pad
                              : "") + inString + (rightJustify ? ""
                                                               : pad));
    }

    /**
     * Display command history, which consists of complete or incomplete SQL
     * commands.
     */
    private void showHistory() {

        int      ctr = -1;
        String   s;
        String[] reversedList = new String[statementHistory.length];

        try {
            for (int i = curHist; i >= 0; i--) {
                s = statementHistory[i];

                if (s == null) {
                    return;
                }

                reversedList[++ctr] = s;
            }

            for (int i = statementHistory.length - 1; i > curHist; i--) {
                s = statementHistory[i];

                if (s == null) {
                    return;
                }

                reversedList[++ctr] = s;
            }
        } finally {
            if (ctr < 0) {
                stdprintln("<<<    No history yet    >>>");

                return;
            }

            for (int i = ctr; i >= 0; i--) {
                psStd.println(((i == 0) ? "BUFR"
                                        : ("-" + i + "  ")) + " **********************************************\n"
                                        + reversedList[i]);
            }

            psStd.println(
                "\n<<<  Copy a command to buffer like \"\\-3\"       "
                + "Re-execute buffer like \":;\"  >>>");
        }
    }

    /**
     * Return a SQL Command from command history.
     */
    private String commandFromHistory(int commandsAgo) throws BadSpecial {

        if (commandsAgo >= statementHistory.length) {
            throw new BadSpecial("History can only hold up to "
                                 + statementHistory.length + " commands");
        }

        String s =
            statementHistory[(statementHistory.length + curHist - commandsAgo) % statementHistory.length];

        if (s == null) {
            throw new BadSpecial("History doesn't go back that far");
        }

        return s;
    }

    /**
     * Push a command onto the history array (the first element of which
     * is the "Buffer").
     */
    private void setBuf(String inString) {

        curHist++;

        if (curHist == statementHistory.length) {
            curHist = 0;
        }

        statementHistory[curHist] = inString;
    }

    /**
     * Describe the columns of specified table.
     *
     * @param tableName  Table that will be described.
     * @param filter  Substring to filter by
     */
    private void describe(String tableName,
                          String inFilter) throws SQLException {

        /*
         * Doing case-sensitive filters now, for greater portability.
        String filter = ((inFilter == null) ? null : inFilter.toUpperCase());
         */
        String    filter = inFilter;
        String    val;
        ArrayList rows        = new ArrayList();
        String[]  headerArray = {
            "name", "datatype", "width", "no-nulls"
        };
        String[]  fieldArray;
        int[]     maxWidth  = {
            0, 0, 0, 0
        };
        boolean[] rightJust = {
            false, false, true, false
        };

        // STEP 1: GATHER DATA
        for (int i = 0; i < headerArray.length; i++) {
            if (htmlMode) {
                continue;
            }

            if (headerArray[i].length() > maxWidth[i]) {
                maxWidth[i] = headerArray[i].length();
            }
        }

        Statement statement = curConn.createStatement();
        ResultSet r         = null;

        try {
            statement.execute("SELECT * FROM " + tableName + " WHERE 1 = 2");

            r = statement.getResultSet();

            ResultSetMetaData m    = r.getMetaData();
            int               cols = m.getColumnCount();

            for (int i = 0; i < cols; i++) {
                fieldArray    = new String[4];
                fieldArray[0] = m.getColumnName(i + 1);

                if (filter != null && fieldArray[0].indexOf(filter) < 0) {
                    continue;
                }

                fieldArray[1] = m.getColumnTypeName(i + 1);
                fieldArray[2] = Integer.toString(m.getColumnDisplaySize(i
                        + 1));
                fieldArray[3] =
                    ((m.isNullable(i + 1) == java.sql.ResultSetMetaData.columnNullable)
                     ? (htmlMode ? "&nbsp;"
                                 : "")
                     : "*");

                rows.add(fieldArray);

                for (int j = 0; j < fieldArray.length; j++) {
                    if (fieldArray[j].length() > maxWidth[j]) {
                        maxWidth[j] = fieldArray[j].length();
                    }
                }
            }

            // STEP 2: DISPLAY DATA
            condlPrint("<TABLE border='1'>\n" + htmlRow(COL_HEAD) + '\n'
                       + PRE_TD, true);

            for (int i = 0; i < headerArray.length; i++) {
                condlPrint("<TD>" + headerArray[i] + "</TD>", true);
                condlPrint(((i > 0) ? spaces(2)
                                    : "") + pad(headerArray[i], maxWidth[i],
                                                rightJust[i],
                                                (i < headerArray.length - 1
                                                 || rightJust[i])), false);
            }

            condlPrintln("\n" + PRE_TR + "</TR>", true);
            condlPrintln("", false);

            if (!htmlMode) {
                for (int i = 0; i < headerArray.length; i++) {
                    condlPrint(((i > 0) ? spaces(2)
                                        : "") + divider(maxWidth[i]), false);
                }

                condlPrintln("", false);
            }

            for (int i = 0; i < rows.size(); i++) {
                condlPrint(htmlRow(((i % 2) == 0) ? COL_EVEN
                                                  : COL_ODD) + '\n'
                                                  + PRE_TD, true);

                fieldArray = (String[]) rows.get(i);

                for (int j = 0; j < fieldArray.length; j++) {
                    condlPrint("<TD>" + fieldArray[j] + "</TD>", true);
                    condlPrint(((j > 0) ? spaces(2)
                                        : "") + pad(
                                            fieldArray[j], maxWidth[j],
                                            rightJust[j],
                                            (j < fieldArray.length - 1
                                             || rightJust[j])), false);
                }

                condlPrintln("\n" + PRE_TR + "</TR>", true);
                condlPrintln("", false);
            }

            condlPrintln("\n</TABLE>\n<HR>", true);
        } finally {
            try {
                if (r != null) {
                    r.close();

                    r = null;
                }

                statement.close();
            } catch (Exception e) {}
        }
    }

    public static String[] getTokenArray(String inString) {

        // I forget how to code a String array literal outside of a
        // definition.
        String[] mtString = {};

        if (inString == null) {
            return mtString;
        }

        StringTokenizer toker = new StringTokenizer(inString);
        String[]        sa    = new String[toker.countTokens()];

        for (int i = 0; i < sa.length; i++) {
            sa[i] = toker.nextToken();
        }

        return sa;
    }

    private boolean eval(String[] inTokens) throws BadSpecial {

        // dereference *VARNAME variables.
        // N.b. we work with a "copy" of the tokens.
        boolean  negate = inTokens.length > 0 && inTokens[0].equals("!");
        String[] tokens = new String[negate ? (inTokens.length - 1)
                                            : inTokens.length];

        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = (inTokens[i + (negate ? 1
                                              : 0)].length() > 1 && inTokens[i + (negate ? 1
                                                                                         : 0)].charAt(
                                                                                         0) == '*') ? ((String) userVars.get(
                                                                                             inTokens[i + (negate ? 1
                                                                                                                  : 0)]
                                                                                                                  .substring(
                                                                                                                      1)))
                                                                                                    : inTokens[i + (negate ? 1
                                                                                                                           : 0)];

            if (tokens[i] == null) {
                tokens[i] = "";
            }
        }

        if (tokens.length == 1) {
            return (tokens[0].length() > 0 &&!tokens[0].equals("0")) ^ negate;
        }

        if (tokens.length == 3) {
            if (tokens[1].equals("==")) {
                return tokens[0].equals(tokens[2]) ^ negate;
            }

            if (tokens[1].equals("!=") || tokens[1].equals("<>")
                    || tokens[1].equals("><")) {
                return (!tokens[0].equals(tokens[2])) ^ negate;
            }

            if (tokens[1].equals(">")) {
                return (tokens[0].length() > tokens[2].length() || ((tokens[0].length() == tokens[2].length()) && tokens[0].compareTo(tokens[2]) > 0))
                       ^ negate;
            }

            if (tokens[1].equals("<")) {
                return (tokens[2].length() > tokens[0].length() || ((tokens[2].length() == tokens[0].length()) && tokens[2].compareTo(tokens[0]) > 0))
                       ^ negate;
            }
        }

        throw new BadSpecial("Unrecognized logical operation");
    }

    private void closeQueryOutputStream() {

        if (pwQuery == null) {
            return;
        }

        if (htmlMode) {
            pwQuery.println("</BODY></HTML>");
            pwQuery.flush();
        }

        pwQuery.close();

        pwQuery = null;
    }

    /**
     * Print to psStd and possibly pwQuery iff current HTML mode matches
     * supplied printHtml.
     */
    private void condlPrintln(String s, boolean printHtml) {

        if ((printHtml &&!htmlMode) || (htmlMode &&!printHtml)) {
            return;
        }

        psStd.println(s);

        if (pwQuery != null) {
            pwQuery.println(s);
            pwQuery.flush();
        }
    }

    /**
     * Print to psStd and possibly pwQuery iff current HTML mode matches
     * supplied printHtml.
     */
    private void condlPrint(String s, boolean printHtml) {

        if ((printHtml &&!htmlMode) || (htmlMode &&!printHtml)) {
            return;
        }

        psStd.print(s);

        if (pwQuery != null) {
            pwQuery.print(s);
            pwQuery.flush();
        }
    }

    private static String formatNicely(Map map, boolean withValues) {

        String       key;
        StringBuffer sb = new StringBuffer();
        Iterator     it = (new TreeMap(map)).keySet().iterator();

        if (withValues) {
            sb.append("The outermost parentheses are not part of "
                      + "the values.\n");
        } else {
            sb.append("Showing variable names and length of values "
                      + "(use 'listvalue' to see values).\n");
        }

        while (it.hasNext()) {
            key = (String) it.next();

            String s = (String) map.get(key);

            sb.append("    " + key + ": " + (withValues ? ("(" + s + ')')
                                                        : Integer.toString(
                                                        s.length())) + '\n');
        }

        return sb.toString();
    }

    /**
     * Ascii file dump.
     */
    private void dump(String varName,
                      File dumpFile) throws IOException, BadSpecial {

        String val = (String) userVars.get(varName);

        if (val == null) {
            throw new BadSpecial("Variable '" + varName
                                 + "' has no value set");
        }

        OutputStreamWriter osw =
            new OutputStreamWriter(new FileOutputStream(dumpFile), charset);

        osw.write(val);

        boolean terminated = false;

        if (val.length() > 0) {
            char lastChar = val.charAt(val.length() - 1);

            if (lastChar != '\n' && lastChar != '\r') {
                terminated = true;

                osw.write('\n');    // I hope this really writes \r\n for DOS
            }
        }

        osw.flush();
        osw.close();

        // Since opened in overwrite mode, since we didn't exception out,
        // we can be confident that we wrote all the bytest in the file.
        stdprintln("Saved " + dumpFile.length() + " characters to '"
                   + dumpFile + "'");
    }

    byte[] binBuffer = null;

    /**
     * Binary file dump
     */
    private void dump(File dumpFile) throws IOException, BadSpecial {

        if (binBuffer == null) {
            throw new BadSpecial("Binary SqlFile buffer is currently empty");
        }

        FileOutputStream fos = new FileOutputStream(dumpFile);

        fos.write(binBuffer);

        int len = binBuffer.length;

        binBuffer = null;

        fos.flush();
        fos.close();
        stdprintln("Saved " + len + " bytes to '" + dumpFile + "'");
    }

    private String streamToString(InputStream is) throws IOException {

        char[]            xferBuffer   = new char[10240];
        StringWriter      stringWriter = new StringWriter();
        InputStreamReader isr          = new InputStreamReader(is, charset);
        int               i;

        while ((i = isr.read(xferBuffer)) > 0) {
            stringWriter.write(xferBuffer, 0, i);
        }

        return stringWriter.toString();
    }

    private byte[] streamToBytes(InputStream is) throws IOException {

        byte[]                xferBuffer = new byte[10240];
        ByteArrayOutputStream baos       = new ByteArrayOutputStream();
        int                   i;

        while ((i = is.read(xferBuffer)) > 0) {
            baos.write(xferBuffer, 0, i);
        }

        return baos.toByteArray();
    }

    /**
     * Ascii file load.
     */
    private void load(String varName, File asciiFile) throws IOException {

        char[]       xferBuffer   = new char[10240];
        StringWriter stringWriter = new StringWriter();
        InputStreamReader isr =
            new InputStreamReader(new FileInputStream(asciiFile), charset);
        int i;

        while ((i = isr.read(xferBuffer)) > 0) {
            stringWriter.write(xferBuffer, 0, i);
        }

        isr.close();
        userVars.put(varName, stringWriter.toString());
    }

    /**
     * Binary file load
     */
    private void load(File binFile) throws IOException {

        byte[]                xferBuffer = new byte[10240];
        ByteArrayOutputStream baos       = new ByteArrayOutputStream();
        FileInputStream       fis        = new FileInputStream(binFile);
        int                   i;

        while ((i = fis.read(xferBuffer)) > 0) {
            baos.write(xferBuffer, 0, i);
        }

        fis.close();

        binBuffer = baos.toByteArray();

        stdprintln("Loaded " + binBuffer.length
                   + " bytes into Binary buffer");
    }

    /**
     * This method is used to tell SqlFile whether this Sql Type must
     * ALWAYS be loaded to the binary buffer without displaying.
     *
     * N.b.:  If this returns "true" for a type, then the user can never
     * "see" values for these columns.
     * Therefore, if a type may-or-may-not-be displayable, better to return
     * false here and let the user choose.
     * In general, if there is a toString() operator for this Sql Type
     * then return false, since the JDBC driver should know how to make the
     * value displayable.
     *
     * The table on this page lists the most common SqlTypes, all of which
     * must implement toString():
     *     http://java.sun.com/docs/books/tutorial/jdbc/basics/retrieving.html
     *
     * @see java.sql.Types
     */
    public static boolean canDisplayType(int i) {

        /* I don't now about some of the more obscure types, like REF and
         * DATALINK */
        switch (i) {

            //case java.sql.Types.BINARY :
            case java.sql.Types.BLOB :
            case java.sql.Types.JAVA_OBJECT :

            //case java.sql.Types.LONGVARBINARY :
            //case java.sql.Types.LONGVARCHAR :
            case java.sql.Types.OTHER :
            case java.sql.Types.STRUCT :

                //case java.sql.Types.VARBINARY :
                return false;
        }

        return true;
    }

    // won't compile with JDK 1.3 without these
    private static final int JDBC3_BOOLEAN  = 16;
    private static final int JDBC3_DATALINK = 70;

    public static String sqlTypeToString(int i) {

        switch (i) {

            case java.sql.Types.ARRAY :
                return "ARRAY";

            case java.sql.Types.BIGINT :
                return "BIGINT";

            case java.sql.Types.BINARY :
                return "BINARY";

            case java.sql.Types.BIT :
                return "BIT";

            case java.sql.Types.BLOB :
                return "BLOB";

            case JDBC3_BOOLEAN :
                return "BOOLEAN";

            case java.sql.Types.CHAR :
                return "CHAR";

            case java.sql.Types.CLOB :
                return "CLOB";

            case JDBC3_DATALINK :
                return "DATALINK";

            case java.sql.Types.DATE :
                return "DATE";

            case java.sql.Types.DECIMAL :
                return "DECIMAL";

            case java.sql.Types.DISTINCT :
                return "DISTINCT";

            case java.sql.Types.DOUBLE :
                return "DOUBLE";

            case java.sql.Types.FLOAT :
                return "FLOAT";

            case java.sql.Types.INTEGER :
                return "INTEGER";

            case java.sql.Types.JAVA_OBJECT :
                return "JAVA_OBJECT";

            case java.sql.Types.LONGVARBINARY :
                return "LONGVARBINARY";

            case java.sql.Types.LONGVARCHAR :
                return "LONGVARCHAR";

            case java.sql.Types.NULL :
                return "NULL";

            case java.sql.Types.NUMERIC :
                return "NUMERIC";

            case java.sql.Types.OTHER :
                return "OTHER";

            case java.sql.Types.REAL :
                return "REAL";

            case java.sql.Types.REF :
                return "REF";

            case java.sql.Types.SMALLINT :
                return "SMALLINT";

            case java.sql.Types.STRUCT :
                return "STRUCT";

            case java.sql.Types.TIME :
                return "TIME";

            case java.sql.Types.TIMESTAMP :
                return "TIMESTAMP";

            case java.sql.Types.TINYINT :
                return "TINYINT";

            case java.sql.Types.VARBINARY :
                return "VARBINARY";

            case java.sql.Types.VARCHAR :
                return "VARCHAR";
        }

        return "Unknown type " + i;
    }

    /**
     * Validate that String is safe to display in a CSV file.
     *
     * @throws SQLException (should throw something else, since this is
     * not an SQL problem.  Fix the caller!)
     */
    public void csvSafe(String s) throws SQLException {

        if (pwCsv == null || csvColDelim == null || csvRowDelim == null
                || csvNullRep == null) {
            throw new RuntimeException(
                "Assertion failed.  \n"
                + "csvSafe called when CSV settings are incomplete");
        }

        if (s == null) {
            return;
        }

        if (s.indexOf(csvColDelim) > 0) {
            throw new SQLException(
                "Table data contains our column delimiter '" + csvColDelim
                + "'");
        }

        if (s.indexOf(csvRowDelim) > 0) {
            throw new SQLException("Table data contains our row delimiter '"
                                   + csvRowDelim + "'");
        }

        if (s.indexOf(csvNullRep) > 0) {
            throw new SQLException(
                "Table data contains our null representation '" + csvNullRep
                + "'");
        }
    }

    public static String convertEscapes(String inString) {

        if (inString == null) {
            return null;
        }

        String workString = new String(inString);
        int    i;

        i = 0;

        while ((i = workString.indexOf("\\n", i)) > -1
                && i < workString.length() - 1) {
            workString = workString.substring(0, i) + '\n'
                         + workString.substring(i + 2);
        }

        i = 0;

        while ((i = workString.indexOf("\\r", i)) > -1
                && i < workString.length() - 1) {
            workString = workString.substring(0, i) + '\r'
                         + workString.substring(i + 2);
        }

        i = 0;

        while ((i = workString.indexOf("\\t", i)) > -1
                && i < workString.length() - 1) {
            workString = workString.substring(0, i) + '\t'
                         + workString.substring(i + 2);
        }

        return workString;
    }

    /**
     * Name is self-explanatory.
     *
     * If there is user demand, open file in random access mode so don't
     * need to load 2 copies of the entire file into memory.
     * This will be difficult because can't use standard Java language
     * features to search through a character array for multi-character
     * substrings.
     */
    public void importCsv(String filePath) throws IOException, BadSpecial {

        char[] bfr  = null;
        File   file = new File(filePath);

        if (!file.canRead()) {
            throw new IOException("Can't read file '" + file + "'");
        }

        int fileLength = (int) (file.length());

        try {
            bfr = new char[fileLength];
        } catch (RuntimeException re) {
            throw new IOException(
                "SqlFile can only read in your CSV file in one chunk at this time.\n"
                + "Please run the program with more RAM (try Java -Xm* switches).");
        }

        InputStreamReader isr =
            new InputStreamReader(new FileInputStream(file), charset);
        int retval = isr.read(bfr, 0, bfr.length);

        isr.close();

        if (retval != bfr.length) {
            throw new IOException("Didn't read all characters.  Read in "
                                  + retval + " characters");
        }

        String string = null;

        try {
            string = new String(bfr);
        } catch (RuntimeException re) {
            throw new IOException(
                "SqlFile converts your entire CSV file to a String at this time.\n"
                + "Please run the program with more RAM (try Java -Xm* switches).");
        }

        ArrayList headerList = new ArrayList();
        String    recordString;

        // N.b.  ENDs are the index of 1 PAST the current item
        int recEnd;
        int colStart;
        int colEnd;

        // First read header line
        int recStart = 0;

        recEnd = string.indexOf(csvRowDelim, recStart);

        if (recEnd < 0) {

            // File consists of only a header line
            recEnd = string.length();
        }

        colStart = recStart;
        colEnd   = -1;

        while (true) {
            if (colEnd == recEnd) {

                // We processed final column last time through loop
                break;
            }

            colEnd = string.indexOf(csvColDelim, colStart);

            if (colEnd < 0 || colEnd > recEnd) {
                colEnd = recEnd;
            }

            if (colEnd - colStart < 1) {
                throw new IOException("No column header for column "
                                      + (headerList.size() + 1));
            }

            headerList.add(string.substring(colStart, colEnd));

            colStart = colEnd + csvColDelim.length();
        }

        String[]  headers   = (String[]) headerList.toArray(new String[0]);
        boolean[] autonulls = new boolean[headers.length];
        String    tableName = (String) userVars.get("*CSV_TABLENAME");

        if (tableName == null) {
            tableName = file.getName();

            int i = tableName.lastIndexOf('.');

            if (i > 0) {
                tableName = tableName.substring(0, i);
            }
        }

        StringBuffer tmpSb = new StringBuffer();

        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                tmpSb.append(", ");
            }

            tmpSb.append(headers[i]);
        }

        StringBuffer sb = new StringBuffer("INSERT INTO " + tableName + " ("
                                           + tmpSb + ") VALUES (");
        StringBuffer typeQuerySb = new StringBuffer("SELECT " + tmpSb
            + " FROM " + tableName + " WHERE 1 = 2");

        try {
            int ctype;
            ResultSetMetaData rsmd = curConn.createStatement().executeQuery(
                typeQuerySb.toString()).getMetaData();

            if (rsmd.getColumnCount() != autonulls.length) {
                throw new BadSpecial("Metadata mismatch for columns");
            }

            for (int i = 0; i < autonulls.length; i++) {
                ctype = rsmd.getColumnType(i + 1);

                // I.e., for VAR* column types, "" in CSV file means
                // to insert "".  Otherwise, we'll insert null for "".
                autonulls[i] = (ctype != java.sql.Types.VARBINARY
                                && ctype != java.sql.Types.VARCHAR);
            }
        } catch (SQLException se) {
            throw new BadSpecial("Failed to get metadata for query: "
                                 + se.getMessage());
        }

        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append('?');
        }

        //System.out.println("INSERTION: (" + sb + ')');
        try {
            PreparedStatement ps = curConn.prepareStatement(sb.toString()
                + ')');
            String[] dataVals = new String[headers.length];
            int      recCount = 0;
            int      colCount;

            // Insert data rows 1-row-at-a-time
            while (true) {
                recStart = recEnd + csvRowDelim.length();

                if (recStart >= string.length()) {
                    break;
                }

                recEnd = string.indexOf(csvRowDelim, recStart);

                if (recEnd < 0) {

                    // Last record
                    recEnd = string.length();
                }

                colStart = recStart;
                colEnd   = -1;
                colCount = 0;

                recCount++;

                while (true) {
                    if (colEnd == recEnd) {

                        // We processed final column last time through loop
                        break;
                    }

                    colEnd = string.indexOf(csvColDelim, colStart);

                    if (colEnd < 0 || colEnd > recEnd) {
                        colEnd = recEnd;
                    }

                    if (colCount == dataVals.length) {
                        throw new IOException(
                            "Header has " + headers.length
                            + " columns.  CSV record " + recCount
                            + " has too many column values.");
                    }

                    dataVals[colCount++] = string.substring(colStart, colEnd);
                    colStart             = colEnd + csvColDelim.length();
                }

                if (colCount != dataVals.length) {
                    throw new IOException("Header has " + headers.length
                                          + " columns.  CSV record "
                                          + recCount + " has " + colCount
                                          + " column values.");
                }

                for (int i = 0; i < dataVals.length; i++) {

                    //System.err.println("ps.setString(" + i + ", "
                    //      + dataVals[i] + ')');
                    ps.setString(
                        i + 1,
                        (((dataVals[i].length() < 1 && autonulls[i]) || dataVals[i].equals(csvNullRep))
                         ? null
                         : dataVals[i]));
                }

                retval = ps.executeUpdate();

                if (retval != 1) {
                    curConn.rollback();

                    throw new BadSpecial("Insert of row " + recCount
                                         + " failed.  " + retval
                                         + " rows modified");
                }

                possiblyUncommitteds.set(true);
            }

            stdprintln("Successfully inserted " + recCount
                       + " rows into table '" + tableName + "'");
        } catch (SQLException se) {
            try {
                curConn.rollback();
            } catch (SQLException se2) {}

            throw new BadSpecial(
                "SQL error encountered when inserting CSV data: " + se);
        }
    }
}
