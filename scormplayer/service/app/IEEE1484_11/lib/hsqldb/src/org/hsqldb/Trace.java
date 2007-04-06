/* Copyright (c) 1995-2000, The Hypersonic SQL Group.
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
 * Neither the name of the Hypersonic SQL Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HYPERSONIC SQL GROUP,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This software consists of voluntary contributions made by many individuals 
 * on behalf of the Hypersonic SQL Group.
 *
 *
 * For work added by the HSQL Development Group:
 *
 * Copyright (c) 2001-2005, The HSQL Development Group
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


package org.hsqldb;

import java.io.PrintWriter;

import org.hsqldb.lib.HsqlByteArrayOutputStream;
import org.hsqldb.resources.BundleHandler;

/**
 * handles creation and reporting of error messages and throwing HsqlException
 *
 * Rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - error reporting
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - setting trace
// the system property hsqldb.tracesystemout == true is now used for printing
// trace message to System.out
// fredt@users 20020305 - patch 1.7.0 - various new messages added
// tony_lai@users 20020820 - patch 595073 - Duplicated exception msg
// fredt@users 20021230 - patch 488118 by xclay@users - allow multithreading
// wsonic@users 20031005 - moved string literal messages to Trace with new methods
// nitin chauhan 20031005 - moved concatenated string in asserts and checks to Trace with new methods
// fredt@users 20040322 - removed unused code - class is a collection of static methods now
// fredt@users 20050524 - use resource bundle for messages

/** @todo  fredt - 20021022 management of nested throws inside the program in
 * such a way that it is possible to return exactly the text of the error
 *  thrown at a given level without higher level messages being added and to
 * preserve the original error code
 */
public class Trace {

    public static boolean       TRACE          = false;
    public static boolean       TRACESYSTEMOUT = false;
    public static final boolean STOP           = false;
    public static final boolean DOASSERT       = false;

    //
    static String errPropsName = "sql-error-messages";
    static int bundleHandle = BundleHandler.getBundleHandle(errPropsName,
        null);

    //
    public static final int

    //
    DATABASE_ALREADY_IN_USE                 = 1,
    CONNECTION_IS_CLOSED                    = 2,
    CONNECTION_IS_BROKEN                    = 3,
    DATABASE_IS_SHUTDOWN                    = 4,
    COLUMN_COUNT_DOES_NOT_MATCH             = 5,
    DIVISION_BY_ZERO                        = 6,
    INVALID_ESCAPE                          = 7,
    INTEGRITY_CONSTRAINT_VIOLATION          = 8,
    VIOLATION_OF_UNIQUE_INDEX               = 9,
    TRY_TO_INSERT_NULL                      = 10,
    UNEXPECTED_TOKEN                        = 11,
    UNEXPECTED_END_OF_COMMAND               = 12,
    UNKNOWN_FUNCTION                        = 13,
    NEED_AGGREGATE                          = 14,
    SUM_OF_NON_NUMERIC                      = 15,
    WRONG_DATA_TYPE                         = 16,
    CARDINALITY_VIOLATION_NO_SUBCLASS       = 17,
    SERIALIZATION_FAILURE                   = 18,
    TRANSFER_CORRUPTED                      = 19,
    FUNCTION_NOT_SUPPORTED                  = 20,
    TABLE_ALREADY_EXISTS                    = 21,
    TABLE_NOT_FOUND                         = 22,
    INDEX_ALREADY_EXISTS                    = 23,
    SECOND_PRIMARY_KEY                      = 24,
    DROP_PRIMARY_KEY                        = 25,
    INDEX_NOT_FOUND                         = 26,
    COLUMN_ALREADY_EXISTS                   = 27,
    COLUMN_NOT_FOUND                        = 28,
    FILE_IO_ERROR                           = 29,
    WRONG_DATABASE_FILE_VERSION             = 30,
    DATABASE_IS_READONLY                    = 31,
    DATA_IS_READONLY                        = 32,
    ACCESS_IS_DENIED                        = 33,
    INPUTSTREAM_ERROR                       = 34,
    NO_DATA_IS_AVAILABLE                    = 35,
    USER_ALREADY_EXISTS                     = 36,
    USER_NOT_FOUND                          = 37,
    ASSERT_FAILED                           = 38,
    EXTERNAL_STOP                           = 39,
    GENERAL_ERROR                           = 40,
    WRONG_OUT_PARAMETER                     = 41,
    FUNCTION_NOT_FOUND                      = 42,
    TRIGGER_NOT_FOUND                       = 43,
    SAVEPOINT_NOT_FOUND                     = 44,
    LABEL_REQUIRED                          = 45,
    WRONG_DEFAULT_CLAUSE                    = 46,
    FOREIGN_KEY_NOT_ALLOWED                 = 47,
    UNKNOWN_DATA_SOURCE                     = 48,
    BAD_INDEX_CONSTRAINT_NAME               = 49,
    DROP_FK_INDEX                           = 50,
    RESULTSET_FORWARD_ONLY                  = 51,
    VIEW_ALREADY_EXISTS                     = 52,
    VIEW_NOT_FOUND                          = 53,
    NOT_USED_54                             = 54,
    NOT_A_TABLE                             = 55,
    SYSTEM_INDEX                            = 56,
    COLUMN_TYPE_MISMATCH                    = 57,
    BAD_ADD_COLUMN_DEFINITION               = 58,
    DROP_SYSTEM_CONSTRAINT                  = 59,
    CONSTRAINT_ALREADY_EXISTS               = 60,
    CONSTRAINT_NOT_FOUND                    = 61,
    INVALID_JDBC_ARGUMENT                   = 62,
    DATABASE_IS_MEMORY_ONLY                 = 63,
    OUTER_JOIN_CONDITION                    = 64,
    NUMERIC_VALUE_OUT_OF_RANGE              = 65,
    MISSING_SOFTWARE_MODULE                 = 66,
    NOT_IN_AGGREGATE_OR_GROUP_BY            = 67,
    INVALID_GROUP_BY                        = 68,
    INVALID_HAVING                          = 69,
    INVALID_ORDER_BY                        = 70,
    INVALID_ORDER_BY_IN_DISTINCT_SELECT     = 71,
    OUT_OF_MEMORY                           = 72,
    OPERATION_NOT_SUPPORTED                 = 73,
    INVALID_IDENTIFIER                      = 74,
    TEXT_TABLE_SOURCE                       = 75,
    TEXT_FILE                               = 76,
    NOT_USED_77                             = 77,
    ERROR_IN_SCRIPT_FILE                    = 78,
    NULL_IN_VALUE_LIST                      = 79,
    SOCKET_ERROR                            = 80,
    INVALID_CHARACTER_ENCODING              = 81,
    NOT_USED_82                             = 82,
    NOT_USED_83                             = 83,
    NOT_USED_84                             = 84,
    UNEXPECTED_EXCEPTION                    = 85,
    NOT_USED_86                             = 86,
    NOT_USED_87                             = 87,
    NOT_USED_88                             = 88,
    NOT_USED_89                             = 89,
    NOT_USED_90                             = 90,
    NOT_USED_91                             = 91,
    NOT_USED_92                             = 92,
    NOT_USED_93                             = 93,
    DATABASE_NOT_EXISTS                     = 94,
    INVALID_CONVERSION                      = 95,
    ERROR_IN_BINARY_SCRIPT_1                = 96,
    ERROR_IN_BINARY_SCRIPT_2                = 97,
    GENERAL_IO_ERROR                        = 98,
    EXPRESSION_NOT_SUPPORTED                = 99,
    Constraint_violation                    = 100,
    Database_dropTable                      = 101,
    ERROR_IN_CONSTRAINT_COLUMN_LIST         = 102,
    TABLE_HAS_NO_PRIMARY_KEY                = 103,
    VIOLATION_OF_UNIQUE_CONSTRAINT          = 104,
    NO_DEFAULT_VALUE_FOR_COLUMN             = 105,
    NOT_A_CONDITION                         = 106,
    DatabaseManager_getDatabase             = 107,
    NOT_USED_108                            = 108,
    NOT_USED_109                            = 109,
    NOT_USED_110                            = 110,
    NOT_USED_111                            = 111,
    NOT_USED_112                            = 112,
    DatabaseScriptReader_readDDL            = 113,
    DatabaseScriptReader_readExistingData   = 114,
    Message_Pair                            = 115,
    LOAD_SAVE_PROPERTIES                    = 116,
    INVALID_TRANSACTION_STATE_NO_SUBCLASS   = 117,
    JDBC_INVALID_BRI_SCOPE                  = 118,
    JDBC_NO_RESULT_SET_METADATA             = 119,
    JDBC_NO_RESULT_SET                      = 120,
    MISSING_CLOSEBRACKET                    = 121,
    ITSNS_OVERWRITE                         = 122,
    COLUMN_IS_IN_INDEX                      = 123,
    STRING_DATA_TRUNCATION                  = 124,
    QUOTED_IDENTIFIER_REQUIRED              = 125,
    STATEMENT_IS_CLOSED                     = 126,
    NOT_USED_127                            = 127,
    NOT_USED_128                            = 128,
    DATA_FILE_ERROR                         = 129,
    NOT_USED_130                            = 130,
    HsqlDateTime_null_string                = 131,
    NOT_USED_132                            = 132,
    HsqlDateTime_null_date                  = 133,
    NOT_USED_134                            = 134,
    HsqlProperties_load                     = 135,
    HsqlSocketFactorySecure_verify          = 136,
    HsqlSocketFactorySecure_verify2         = 137,
    jdbcConnection_nativeSQL                = 138,
    HsqlSocketFactorySecure_verify3         = 139,
    NOT_USED_140                            = 140,
    NOT_USED_141                            = 141,
    jdbcStatement_executeUpdate             = 142,
    LockFile_checkHeartbeat                 = 143,
    LockFile_checkHeartbeat2                = 144,
    TEXT_STRING_HAS_NEWLINE                 = 145,
    Result_Result                           = 146,
    SERVER_NO_DATABASE                      = 147,
    Server_openServerSocket                 = 148,
    Server_openServerSocket2                = 149,
    TEXT_TABLE_HEADER                       = 150,
    NOT_USED_151                            = 151,
    JDBC_PARAMETER_NOT_SET                  = 152,
    INVALID_LIMIT                           = 153,
    JDBC_STATEMENT_NOT_ROW_COUNT            = 154,
    JDBC_STATEMENT_NOT_RESULTSET            = 155,
    AMBIGUOUS_COLUMN_REFERENCE              = 156,
    CHECK_CONSTRAINT_VIOLATION              = 157,
    JDBC_RESULTSET_IS_CLOSED                = 158,
    SINGLE_COLUMN_EXPECTED                  = 159,
    TOKEN_REQUIRED                          = 160,
    NOT_USED_161                            = 161,
    NOT_USED_162                            = 162,
    ORDER_LIMIT_REQUIRED                    = 163,
    TRIGGER_ALREADY_EXISTS                  = 164,
    ASSERT_DIRECT_EXEC_WITH_PARAM           = 165,
    NOT_USED_166                            = 166,
    Expression_compareValues                = 167,
    INVALID_LIMIT_EXPRESSION                = 168,
    INVALID_TOP_EXPRESSION                  = 169,
    SQL_CONSTRAINT_REQUIRED                 = 170,
    TableWorks_dropConstraint               = 171,
    TEXT_TABLE_SOURCE_FILENAME              = 172,
    TEXT_TABLE_SOURCE_VALUE_MISSING         = 173,
    TEXT_TABLE_SOURCE_SEPARATOR             = 174,
    UNSUPPORTED_PARAM_CLASS                 = 175,
    JDBC_NULL_STREAM                        = 176,
    INTEGRITY_CONSTRAINT_VIOLATION_NOPARENT = 177,
    NOT_USED_178                            = 178,
    NOT_USED_179                            = 179,
    QuotedTextDatabaseRowInput_getField     = 180,
    QuotedTextDatabaseRowInput_getField2    = 181,
    TextDatabaseRowInput_getField           = 182,
    TextDatabaseRowInput_getField2          = 183,
    TextDatabaseRowInput_getField3          = 184,
    Parser_ambiguous_between1               = 185,
    SEQUENCE_REFERENCED_BY_VIEW             = 186,
    NOT_USED_187                            = 187,
    TextCache_openning_file_error           = 188,
    TextCache_closing_file_error            = 189,
    TextCache_purging_file_error            = 190,
    SEQUENCE_NOT_FOUND                      = 191,
    SEQUENCE_ALREADY_EXISTS                 = 192,
    TABLE_REFERENCED_CONSTRAINT             = 193,
    TABLE_REFERENCED_VIEW                   = 194,
    NOT_USED_195                            = 195,
    TEXT_SOURCE_EXISTS                      = 196,
    COLUMN_IS_REFERENCED                    = 197,
    FUNCTION_CALL_ERROR                     = 198,
    TRIGGERED_DATA_CHANGE                   = 199,
    INVALID_FUNCTION_ARGUMENT               = 200,
    UNSUPPORTED_INTERNAL_OPERATION          = 201,
    NOT_USED_202                            = 202,
    INVALID_PREPARED_STATEMENT              = 203,
    CREATE_TRIGGER_COMMAND_1                = 204,
    TRIGGER_FUNCTION_CLASS_NOT_FOUND        = 205,
    NOT_USED_206                            = 206,
    NOT_USED_207                            = 207,
    INVALID_COLLATION_NAME_NO_SUBCLASS      = 208,
    DataFileCache_makeRow                   = 209,
    DataFileCache_open                      = 210,
    DataFileCache_close                     = 211,
    Expression_resolveTypes1                = 212,
    Expression_resolveTypes2                = 213,
    Expression_resolveTypes3                = 214,
    Expression_resolveTypes4                = 215,
    UNRESOLVED_PARAMETER_TYPE               = 216,
    Expression_resolveTypes6                = 217,
    Expression_resolveTypes7                = 218,
    Expression_resolveTypeForLike           = 219,
    NOT_USED_220                            = 220,
    Expression_resolveTypeForIn2            = 221,
    Session_execute                         = 222,
    NOT_USED_223                            = 223,
    NOT_USED_224                            = 224,
    DATA_FILE_IS_FULL                       = 225,
    THREE_PART_IDENTIFIER                   = 226,
    INVALID_SCHEMA_NAME_NO_SUBCLASS         = 227,
    DEPENDENT_DATABASE_OBJECT_EXISTS        = 228,
    NO_SUCH_ROLE_GRANT                      = 229,
    NO_SUCH_ROLE_REVOKE                     = 230,
    NONMOD_ACCOUNT                          = 231,
    NO_SUCH_GRANTEE                         = 232,
    MISSING_SYSAUTH                         = 233,
    MISSING_GRANTEE                         = 234,
    CHANGE_GRANTEE                          = 235,
    NULL_NAME                               = 236,
    ILLEGAL_ROLE_NAME                       = 237,
    ROLE_ALREADY_EXISTS                     = 238,
    NO_SUCH_ROLE                            = 239,
    MISSING_ROLEMANAGER                     = 240,
    GRANTEE_ALREADY_EXISTS                  = 241,
    MISSING_PUBLIC_GRANTEE                  = 242,
    NONMOD_GRANTEE                          = 243,
    CIRCULAR_GRANT                          = 244,
    ALREADY_HAVE_ROLE                       = 245,
    DONT_HAVE_ROLE                          = 246,
    NOT_USED_247                            = 247,
    RETRIEVE_NEST_ROLE_FAIL                 = 248,
    NO_SUCH_RIGHT                           = 249,
    IN_SCHEMA_DEFINITION                    = 250,
    PRIMARY_KEY_NOT_ALLOWED                 = 251,
    COLUMN_IS_IN_CONSTRAINT                 = 252,
    COLUMN_SIZE_REQUIRED                    = 253,
    INVALID_SIZE_PRECISION                  = 254,
    LAST_ERROR_HANDLE                       = 255;

    //
    static String MESSAGE_TAG = "$$";

    //

    /** Used during tests. */
    static {
        try {
            TRACE = TRACE || Boolean.getBoolean("hsqldb.trace");
            TRACESYSTEMOUT = TRACESYSTEMOUT
                             || Boolean.getBoolean("hsqldb.tracesystemout");
        } catch (Exception e) {}

        if (!"LAST".equals(BundleHandler.getString(bundleHandle,
                String.valueOf(LAST_ERROR_HANDLE)))) {
            throw new RuntimeException();
        }
    }

    /**
     * Compose error message by inserting the strings in the add parameters
     * in placeholders within the error message. The message string contains
     * $$ markers for each context variable. Context variables are supplied in
     * the add parameters.
     *
     * @param code      main error code
     * @param subCode   sub error code (if 0 => no subMessage!)
     * @param   add     optional parameters
     *
     * @return an <code>HsqlException</code>
     */
    public static HsqlException error(int code, int subCode,
                                      final Object[] add) {

        // in case of negative code
        code = Math.abs(code);

        String mainErrorMessage = getMessage(code);
        String state            = "S1000";

        if (mainErrorMessage.length() >= 5) {
            state            = mainErrorMessage.substring(0, 5);
            mainErrorMessage = mainErrorMessage.substring(6);
        }

        if (subCode != 0) {
            mainErrorMessage += getMessage(Math.abs(subCode));
        }

        StringBuffer sb = new StringBuffer(mainErrorMessage.length() + 32);
        int          lastIndex = 0;
        int          escIndex  = mainErrorMessage.length();

        if (add != null) {

            // removed test: i < add.length
            // because if mainErrorMessage is equal to "blabla $$"
            // then the statement escIndex = mainErrorMessage.length();
            // is never reached!  ???
            for (int i = 0; i < add.length; i++) {
                escIndex = mainErrorMessage.indexOf(MESSAGE_TAG, lastIndex);

                if (escIndex == -1) {
                    break;
                }

                sb.append(mainErrorMessage.substring(lastIndex, escIndex));
                sb.append(add[i] == null ? "null exception message"
                                         : add[i].toString());

                lastIndex = escIndex + MESSAGE_TAG.length();
            }
        }

        escIndex = mainErrorMessage.length();

        sb.append(mainErrorMessage.substring(lastIndex, escIndex));

        return new HsqlException(sb.toString(), state, -code);
    }

    /**
     * Compose error message by inserting the strings in the add parameters
     * in placeholders within the error message. The message string contains
     * $$ markers for each context variable. Context variables are supplied in
     * the add parameters.
     *
     * @param code      main error code
     * @param   add     optional parameters
     *
     * @return an <code>HsqlException</code>
     */
    public static HsqlException error(int code, final Object[] add) {
        return error(code, 0, add);
    }

    public static HsqlException error(int code, int code2, String add) {
        return error(code, getMessage(code2) + ' ' + add);
    }

    public static HsqlException error(int code, int code2) {
        return error(code, getMessage(code2));
    }

    /**
     * Method declaration
     *
     *
     * @param code
     * @param add
     *
     * @return
     */
    public static HsqlException error(int code, Object add) {

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        code = Math.abs(code);

        String s = getMessage(code);

        if (add != null) {
            s += ": " + add.toString();
        }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        return new HsqlException(s.substring(6), s.substring(0, 5), -code);

        //return getError(s);
    }

    /**
     *     Return a new <code>HsqlException</code> according to the result parameter.
     *
     * @param result    the <code>Result</code> associated with the exception
     *     @return a new <code>HsqlException</code> according to the result parameter
     */
    public static HsqlException error(final Result result) {
        return new HsqlException(result);
    }

    /**
     * Return a new <code>Result</code> of type error.
     *
     * @param result    the <code>Result</code> associated with the exception
     *     @return a new <code>HsqlException</code> according to the result parameter
     */

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Constructor for errors
     *
     * @param  e exception
     */
    static Result toResult(HsqlException e) {
        return new Result(e.getMessage(), e.getSQLState(), e.getErrorCode());
    }

    public static RuntimeException runtimeError(int code, Object add) {

        HsqlException e = error(code, add);

        return new RuntimeException(e.getMessage());
    }

    /**
     * Returns the error message given the error code.<br/>
     * Note: this method must be used when throwing exception other
     * than <code>HsqlException</code>.
     *
     * @param errorCode    the error code associated to the error message
     * @return  the error message associated with the error code
     * @see #sDescription
     */
    public static String getMessage(final int errorCode) {
        return getMessage(errorCode, false, null);
    }

    /**
     * Returns the error message given the error code.<br/>
     * Note: this method must be used when throwing exception other
     * than <code>HsqlException</code>.
     *
     * @param errorCode    the error code associated to the error message
     * @param substitute    substitute the $$ tokens using data in the values
     * @param values       value(s) to use to replace the token(s)
     * @return the error message associated with the error code
     * @see #sDescription
     */
    public static String getMessage(final int errorCode,
                                    final boolean substitute,
                                    final Object[] values) {

        if (errorCode < 0) {
            return "";
        } else {
            String key = String.valueOf(errorCode);

            if (errorCode < 10) {
                key = "00" + key;
            } else if (errorCode < 100) {
                key = "0" + key;
            }

            String mainErrorMessage = BundleHandler.getString(bundleHandle,
                key);

            if (!substitute) {

//                return sDescription[errorCode];
                return mainErrorMessage;
            } else {

//                final String mainErrorMessage = sDescription[errorCode];
                final StringBuffer sb =
                    new StringBuffer(mainErrorMessage.length() + 32);
                int lastIndex = 0;
                int escIndex  = mainErrorMessage.length();

                if (values != null) {

                    // removed test: i < add.length
                    // because if mainErrorMessage is equal to "blabla $$"
                    // then the statement escIndex = mainErrorMessage.length();
                    // is never reached!  ???
                    for (int i = 0; i < values.length; i++) {
                        escIndex = mainErrorMessage.indexOf(MESSAGE_TAG,
                                                            lastIndex);

                        if (escIndex == -1) {
                            break;
                        }

                        sb.append(mainErrorMessage.substring(lastIndex,
                                                             escIndex));
                        sb.append(values[i].toString());

                        lastIndex = escIndex + MESSAGE_TAG.length();
                    }
                }

                escIndex = mainErrorMessage.length();

                sb.append(mainErrorMessage.substring(lastIndex, escIndex));

                return sb.toString();
            }
        }
    }

    /**
     * Method declaration
     *
     *
     * @param code
     *
     * @return
     */
    public static HsqlException error(int code) {
        return error(code, null);
    }

    /**
     *     Throws exception if condition is false
     *
     *     @param condition
     *     @param code
     *
     * @throws HsqlException
     */
    public static void check(boolean condition,
                             int code) throws HsqlException {
        check(condition, code, null, null, null, null);
    }

    /**
     *     Throws exception if condition is false
     *
     *     @param condition
     *     @param code
     *     @param add
     *
     * @throws HsqlException
     */
    public static void check(boolean condition, int code,
                             Object add) throws HsqlException {

        if (!condition) {
            throw error(code, add);
        }
    }

    /**
     * Method declaration
     *
     *
     * @param code
     * @param add
     *
     * @throws HsqlException
     */
    static void throwerror(int code, Object add) throws HsqlException {
        throw error(code, add);
    }

    /**
     * Used to print messages to System.out
     *
     *
     * @param message message to print
     */
    public static void printSystemOut(String message) {

        if (TRACESYSTEMOUT) {
            System.out.println(message);
        }
    }

    /**
     * Used to print messages to System.out
     *
     *
     * @param message1 message to print
     * @param message2 message to print
     */
    public static void printSystemOut(String message1, long message2) {

        if (TRACESYSTEMOUT) {
            System.out.print(message1);
            System.out.println(message2);
        }
    }

    /**
     * Returns the stack trace for doAssert()
     */
    private static String getStackTrace() {

        try {
            Exception e = new Exception();

            throw e;
        } catch (Exception e) {
            HsqlByteArrayOutputStream os = new HsqlByteArrayOutputStream();
            PrintWriter               pw = new PrintWriter(os, true);

            e.printStackTrace(pw);

            return os.toString();
        }
    }

    /**
     * Throws exception if condition is false
     *
     * @param condition
     * @param code
     * @param add1
     * @param add2
     *
     * @throws HsqlException
     */
    static void check(boolean condition, int code, String add1,
                      String add2) throws HsqlException {
        check(condition, code, add1, add2, null, null);
    }

    /**
     * Throws exception if condition is false
     *
     * @param condition
     * @param code
     * @param add1
     * @param add2
     * @param add3
     *
     * @throws HsqlException
     */
    static void check(boolean condition, int code, String add1, String add2,
                      String add3) throws HsqlException {
        check(condition, code, add1, add2, add3, null);
    }

    /**
     * Throws exception if condition is false
     *
     * @param condition
     * @param code
     * @param add1
     * @param add2
     * @param add3
     * @param add4
     *
     * @throws HsqlException
     */
    static void check(boolean condition, int code, String add1, String add2,
                      String add3, String add4) throws HsqlException {

        if (!condition) {
            String add = "";

            if (add1 != null) {
                add += add1;
            }

            if (add2 != null) {
                add += add2;
            }

            if (add3 != null) {
                add += add3;
            }

            if (add4 != null) {
                add += add4;
            }

            throw error(code, add.length() > 0 ? add
                                               : null);
        }
    }

    /**
     * Throws exception if assertion fails
     *
     * @param condition
     * @throws HsqlException
     */
    static void doAssert(boolean condition) throws HsqlException {
        doAssert(condition, null);
    }

    /**
     * Throws exception if assertion fails
     *
     * @param condition
     * @param error
     * @throws HsqlException
     */
    static void doAssert(boolean condition,
                         String error) throws HsqlException {

        if (!condition) {
            if (error == null) {
                error = "";
            }

            error += getStackTrace();

            throw error(ASSERT_FAILED, error);
        }
    }
}
