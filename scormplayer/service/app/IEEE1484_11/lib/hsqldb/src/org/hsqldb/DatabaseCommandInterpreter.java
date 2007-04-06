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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Locale;

import org.hsqldb.HsqlNameManager.HsqlName;
import org.hsqldb.lib.ArrayUtil;
import org.hsqldb.lib.HashMappedList;
import org.hsqldb.lib.HsqlArrayList;
import org.hsqldb.lib.StringUtil;
import org.hsqldb.lib.java.JavaSystem;
import org.hsqldb.persist.HsqlDatabaseProperties;
import org.hsqldb.scriptio.ScriptWriterBase;
import org.hsqldb.scriptio.ScriptWriterText;

/**
 * Provides SQL Interpreter services relative to a Session and
 * its Database.
 *
 * The core functionality of this class was inherited from Hypersonic and
 * extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since 1.7.2
 */

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - various corrections
// fredt@users 20020430 - patch 549741 by velichko - ALTER TABLE RENAME
// fredt@users 20020405 - patch 1.7.0 - other ALTER TABLE statements
// tony_lai@users 20020820 - patch 595099 - use user-defined PK name
// tony_lai@users 20020820 - patch 595156 - violation of constraint name
// fredt@users 20020912 - patch 1.7.1 by fredt - log alter statements
// kloska@users 20021030 - patch 1.7.2 - ON UPDATE CASCADE | SET NULL | SET DEFAULT
// kloska@users 20021112 - patch 1.7.2 - ON DELETE SET NULL | SET DEFAULT
// boucherb@users 20020310 - disable ALTER TABLE DDL on VIEWs (avoid NPE)
// fredt@users 20030314 - patch 1.7.2 by gilead@users - drop table if exists syntax
// boucherb@users 20030425 - DDL methods are moved to DatabaseCommandInterpreter.java
// boucherb@users 20030425 - refactoring DDL methods into smaller units
// fredt@users 20030609 - support for ALTER COLUMN SET/DROP DEFAULT / RENAME TO
// wondersonic@users 20031205 - IF EXISTS support for DROP INDEX
// fredt@users 20031224 - support for CREATE SEQUENCE ...
// fredt@users 20041209 - patch by tytar@users to set default table type
class DatabaseCommandInterpreter {

    private Tokenizer tokenizer = new Tokenizer();
    private Database  database;
    private Session   session;

    /**
     * Constructs a new DatabaseCommandInterpreter for the given Session
     *
     * @param s session
     */
    DatabaseCommandInterpreter(Session s) {
        session  = s;
        database = s.getDatabase();
    }

    /**
     * Executes the SQL String. This method is always called from a block
     * synchronized on the database object.
     *
     * @param sql query
     * @return the result of executing the given SQL String
     */
    Result execute(String sql) {

        Result result;
        String token;
        int    cmd;

        JavaSystem.gc();

        result = null;
        cmd    = Token.UNKNOWNTOKEN;

        try {
            tokenizer.reset(sql);

            while (true) {
                tokenizer.setPartMarker();
                session.setScripting(false);

                token = tokenizer.getSimpleToken();

                if (token.length() == 0) {
                    session.endSchemaDefinition();

                    break;
                }

                cmd = Token.get(token);

                if (cmd == Token.SEMICOLON) {
                    session.endSchemaDefinition();

                    continue;
                }

                result = executePart(cmd, token);

                if (result.isError()) {
                    session.endSchemaDefinition();

                    break;
                }

                if (session.getScripting()) {
                    database.logger.writeToLog(session,
                                               tokenizer.getLastPart());
                }
            }
        } catch (Throwable t) {
            try {
                if (session.isSchemaDefintion()) {
                    HsqlName schemaName = session.getSchemaHsqlName(null);

                    database.schemaManager.dropSchema(schemaName.name, true);
                    database.logger.writeToLog(session,
                                               Token.T_DROP + ' '
                                               + Token.T_SCHEMA + ' '
                                               + schemaName.statementName
                                               + ' ' + Token.T_CASCADE);
                    session.endSchemaDefinition();
                }
            } catch (HsqlException e) {}

            result = new Result(t, tokenizer.getLastPart());
        }

        return result == null ? Session.emptyUpdateCount
                              : result;
    }

    private Result executePart(int cmd, String token) throws Throwable {

        Result result   = Session.emptyUpdateCount;
        int    brackets = 0;

        if (session.isSchemaDefintion()) {
            switch (cmd) {

                case Token.CREATE :
                case Token.GRANT :
                    break;

                default :
                    throw Trace.error(Trace.INVALID_IDENTIFIER,
                                      Trace.IN_SCHEMA_DEFINITION,
                                      new Object[]{ token });
            }
        }

        switch (cmd) {

            case Token.OPENBRACKET : {
                Parser parser = new Parser(session, database, tokenizer);

                brackets = parser.parseOpenBracketsSelect() + 1;
            }
            case Token.SELECT : {
                Parser parser = new Parser(session, database, tokenizer);
                CompiledStatement cStatement =
                    parser.compileSelectStatement(brackets);

                if (cStatement.parameters.length != 0) {
                    Trace.doAssert(
                        false,
                        Trace.getMessage(
                            Trace.ASSERT_DIRECT_EXEC_WITH_PARAM));
                }

                result = session.sqlExecuteCompiledNoPreChecks(cStatement,
                        null);

                break;
            }
            case Token.INSERT : {
                Parser parser = new Parser(session, database, tokenizer);
                CompiledStatement cStatement =
                    parser.compileInsertStatement();

                if (cStatement.parameters.length != 0) {
                    Trace.doAssert(
                        false,
                        Trace.getMessage(
                            Trace.ASSERT_DIRECT_EXEC_WITH_PARAM));
                }

                result = session.sqlExecuteCompiledNoPreChecks(cStatement,
                        null);

                break;
            }
            case Token.UPDATE : {
                Parser parser = new Parser(session, database, tokenizer);
                CompiledStatement cStatement =
                    parser.compileUpdateStatement();

                if (cStatement.parameters.length != 0) {
                    Trace.doAssert(
                        false,
                        Trace.getMessage(
                            Trace.ASSERT_DIRECT_EXEC_WITH_PARAM));
                }

                result = session.sqlExecuteCompiledNoPreChecks(cStatement,
                        null);

                break;
            }
            case Token.DELETE : {
                Parser parser = new Parser(session, database, tokenizer);
                CompiledStatement cStatement =
                    parser.compileDeleteStatement();

                if (cStatement.parameters.length != 0) {
                    Trace.doAssert(
                        false,
                        Trace.getMessage(
                            Trace.ASSERT_DIRECT_EXEC_WITH_PARAM));
                }

                result = session.sqlExecuteCompiledNoPreChecks(cStatement,
                        null);

                break;
            }
            case Token.CALL : {
                Parser parser = new Parser(session, database, tokenizer);
                CompiledStatement cStatement = parser.compileCallStatement();

                if (cStatement.parameters.length != 0) {
                    Trace.doAssert(
                        false,
                        Trace.getMessage(
                            Trace.ASSERT_DIRECT_EXEC_WITH_PARAM));
                }

                result = session.sqlExecuteCompiledNoPreChecks(cStatement,
                        null);

                break;
            }
            case Token.SET :
                processSet();
                break;

            case Token.COMMIT :
                processCommit();
                break;

            case Token.ROLLBACK :
                processRollback();
                break;

            case Token.SAVEPOINT :
                processSavepoint();
                break;

            case Token.RELEASE :
                processReleaseSavepoint();
                break;

            case Token.CREATE :
                processCreate();
                database.setMetaDirty(false);
                break;

            case Token.ALTER :
                processAlter();
                database.setMetaDirty(true);
                break;

            case Token.DROP :
                processDrop();
                database.setMetaDirty(true);
                break;

            case Token.GRANT :
                processGrantOrRevoke(true);
                database.setMetaDirty(false);
                break;

            case Token.REVOKE :
                processGrantOrRevoke(false);
                database.setMetaDirty(true);
                break;

            case Token.CONNECT :
                processConnect();
                database.setMetaDirty(false);
                session.setScripting(false);
                break;

            case Token.DISCONNECT :
                processDisconnect();
                session.setScripting(true);
                break;

            case Token.SCRIPT :
                result = processScript();
                break;

            case Token.SHUTDOWN :
                processShutdown();
                break;

            case Token.CHECKPOINT :
                processCheckpoint();
                break;

            case Token.EXPLAIN :
                result = processExplainPlan();
                break;

            default :
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        return result;
    }

    /**
     * Responsible for parsing and executing the SCRIPT SQL statement
     *
     * @return either an empty result or one in which each row is a DDL or DML
     * @throws IOException
     * @throws HsqlException
     */
    private Result processScript() throws IOException, HsqlException {

        String           token = tokenizer.getString();
        ScriptWriterText dsw   = null;

        session.checkAdmin();

        try {
            if (tokenizer.wasValue()) {
                if (tokenizer.getType() != Types.VARCHAR) {
                    throw Trace.error(Trace.INVALID_IDENTIFIER);
                }

                dsw = new ScriptWriterText(database, token, true, true, true);

                dsw.writeAll();

                return new Result(ResultConstants.UPDATECOUNT);
            } else {
                tokenizer.back();

                return DatabaseScript.getScript(database, false);
            }
        } finally {
            if (dsw != null) {
                dsw.close();
            }
        }
    }

    /**
     *  Responsible for handling CREATE ...
     *
     *  All CREATE command require an ADMIN user except: <p>
     *
     * <pre>
     * CREATE TEMP [MEMORY] TABLE
     * </pre>
     *
     * @throws  HsqlException
     */
    private void processCreate() throws HsqlException {

        boolean unique = false;
        int     tableType;
        boolean isTempTable = false;
        String  token;

        session.checkAdmin();
        session.checkDDLWrite();
        session.setScripting(true);

        if (tokenizer.isGetThis(Token.T_GLOBAL)) {
            tokenizer.getThis(Token.T_TEMPORARY);

            isTempTable = true;
        } else if (tokenizer.isGetThis(Token.T_TEMP)) {
            isTempTable = true;
        } else if (tokenizer.isGetThis(Token.T_TEMPORARY)) {
            isTempTable = true;
        }

        token = tokenizer.getSimpleToken();

        switch (Token.get(token)) {

            // table
            case Token.MEMORY :
                tokenizer.getThis(Token.T_TABLE);
            case Token.TABLE :
                tableType = isTempTable ? Table.TEMP_TABLE
                                        : database.getDefaultTableType();

                processCreateTable(tableType);

                return;

            case Token.CACHED :
                if (isTempTable) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }

                tokenizer.getThis(Token.T_TABLE);
                processCreateTable(Table.CACHED_TABLE);

                return;

            case Token.TEXT :
                if (isTempTable) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }

                tokenizer.getThis(Token.T_TABLE);
                processCreateTable(Table.TEXT_TABLE);

                return;

            default :
                if (isTempTable) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }
        }

        switch (Token.get(token)) {

            // other objects
            case Token.ALIAS :
                processCreateAlias();
                break;

            case Token.SEQUENCE :
                processCreateSequence();
                break;

            case Token.SCHEMA :
                session.setScripting(false);
                processCreateSchema();
                break;

            case Token.TRIGGER :
                processCreateTrigger();
                break;

            case Token.USER :
                processCreateUser();
                break;

            case Token.ROLE :
                database.getGranteeManager().addRole(getUserIdentifier());
                break;

            case Token.VIEW :
                processCreateView();
                break;

            // index
            case Token.UNIQUE :
                unique = true;

                tokenizer.getThis(Token.T_INDEX);

            //fall thru
            case Token.INDEX :
                processCreateIndex(unique);
                break;

            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }
        }
    }

    /**
     *  Process a bracketed column list as used in the declaration of SQL
     *  CONSTRAINTS and return an array containing the indexes of the columns
     *  within the table.
     *
     * @param  t table that contains the columns
     * @return  column index map
     * @throws  HsqlException if a column is not found or is duplicate
     */
    private int[] processColumnList(Table t,
                                    boolean acceptAscDesc)
                                    throws HsqlException {

        HashMappedList list = Parser.processColumnList(tokenizer,
            acceptAscDesc);
        int   size = list.size();
        int[] col  = new int[size];

        for (int i = 0; i < size; i++) {
            col[i] = t.getColumnNr((String) list.getKey(i));
        }

        return col;
    }

    /**
     *  Responsible for handling the execution of CREATE TRIGGER SQL
     *  statements. <p>
     *
     *  typical sql is: CREATE TRIGGER tr1 AFTER INSERT ON tab1 CALL "pkg.cls"
     *
     * @throws HsqlException
     */
    private void processCreateTrigger() throws HsqlException {

        Table      t;
        boolean    isForEach;
        boolean    isNowait;
        int        queueSize;
        String     triggerName;
        boolean    isQuoted;
        String     sWhen;
        String     sOper;
        String     tableName;
        String     token;
        String     className;
        TriggerDef td;
        Trigger    o;

        triggerName = tokenizer.getName();

        String schemaname = tokenizer.getLongNameFirst();

        database.schemaManager.checkTriggerExists(triggerName,
                session.getSchemaNameForWrite(schemaname), false);

        isQuoted  = tokenizer.wasQuotedIdentifier();
        isForEach = false;
        isNowait  = false;
        queueSize = TriggerDef.getDefaultQueueSize();
        sWhen     = tokenizer.getSimpleToken();
        sOper     = tokenizer.getSimpleToken();

        tokenizer.getThis(Token.T_ON);

        tableName = tokenizer.getName();

        if (schemaname == null) {
            schemaname =
                session.getSchemaNameForWrite(tokenizer.getLongNameFirst());
        } else if (!schemaname.equals(
                session.getSchemaNameForWrite(
                    tokenizer.getLongNameFirst()))) {
            throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
        }

        t = database.schemaManager.getUserTable(session, tableName,
                schemaname);

        if (t.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE);
        }

        session.setScripting(true);

        // "FOR EACH ROW" or "CALL"
        token = tokenizer.getSimpleToken();

        if (token.equals(Token.T_FOR)) {
            token = tokenizer.getSimpleToken();

            if (token.equals(Token.T_EACH)) {
                token = tokenizer.getSimpleToken();

                if (token.equals(Token.T_ROW)) {
                    isForEach = true;

                    // should be 'NOWAIT' or 'QUEUE' or 'CALL'
                    token = tokenizer.getSimpleToken();
                } else {
                    throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, token);
                }
            } else {
                throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, token);
            }
        }

        if (token.equals(Token.T_NOWAIT)) {
            isNowait = true;

            // should be 'CALL' or 'QUEUE'
            token = tokenizer.getSimpleToken();
        }

        if (token.equals(Token.T_QUEUE)) {
            queueSize = tokenizer.getInt();

            // should be 'CALL'
            token = tokenizer.getSimpleToken();
        }

        if (!token.equals(Token.T_CALL)) {
            throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, token);
        }

        className = tokenizer.getSimpleName();

        if (!tokenizer.wasQuotedIdentifier()) {
            throw Trace.error(Trace.UNEXPECTED_END_OF_COMMAND, className);
        }

        HsqlName name = database.nameManager.newHsqlName(triggerName,
            isQuoted);

        td = new TriggerDef(name, sWhen, sOper, isForEach, t, className,
                            isNowait, queueSize, database.classLoader);

        t.addTrigger(td);

        if (td.isValid()) {
            try {

                // start the trigger thread
                td.start();
            } catch (Exception e) {
                throw Trace.error(Trace.UNKNOWN_FUNCTION, e.toString());
            }
        }

        database.schemaManager.registerTriggerName(triggerName, t.getName());

// --
    }

    private Column processCreateColumn() throws HsqlException {

        String   token    = tokenizer.getSimpleName();
        boolean  isQuoted = tokenizer.wasQuotedIdentifier();
        HsqlName hsqlName = database.nameManager.newHsqlName(token, isQuoted);

        return processCreateColumn(hsqlName);
    }

    /**
     *  Responsible for handling the creation of table columns during the
     *  process of executing CREATE TABLE DDL statements.
     *
     *  @param  hsqlName name of the column
     *  @return a Column object with indicated attributes
     *  @throws  HsqlException
     */
    private Column processCreateColumn(HsqlName hsqlName)
    throws HsqlException {

        boolean    isIdentity        = false;
        long       identityStart     = database.firstIdentity;
        long       identityIncrement = 1;
        boolean    isPrimaryKey      = false;
        String     typeName;
        int        type;
        int        length      = 0;
        int        scale       = 0;
        boolean    hasLength   = false;
        boolean    isNullable  = true;
        Expression defaultExpr = null;
        String     token;

        typeName = tokenizer.getSimpleToken();
        type     = Types.getTypeNr(typeName);

        if (type == Types.CHAR) {
            if (tokenizer.isGetThis(Token.T_VARYING)) {
                type = Types.VARCHAR;
            }
        }

        if (typeName.equals(Token.T_IDENTITY)) {
            isIdentity   = true;
            isPrimaryKey = true;
        }

        // fredt - when SET IGNORECASE is in effect, all new VARCHAR columns are defined as VARCHAR_IGNORECASE
        if (type == Types.DOUBLE) {
            tokenizer.isGetThis(Token.T_PRECISION);
        }

        if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
            hasLength = true;
            length    = tokenizer.getInt();

            Trace.check(Types.acceptsPrecisionCreateParam(type),
                        Trace.UNEXPECTED_TOKEN);

            if (type != Types.TIMESTAMP && type != Types.TIME
                    && length == 0) {
                throw Trace.error(Trace.INVALID_SIZE_PRECISION);
            }

            if (tokenizer.isGetThis(Token.T_COMMA)) {
                Trace.check(Types.acceptsScaleCreateParam(type),
                            Trace.UNEXPECTED_TOKEN);

                scale = tokenizer.getInt();
            }

            tokenizer.getThis(Token.T_CLOSEBRACKET);
        } else if (type == Types.CHAR && database.sqlEnforceStrictSize) {
            length = 1;
        } else if (type == Types.VARCHAR && database.sqlEnforceStrictSize) {
            throw Trace.error(Trace.COLUMN_SIZE_REQUIRED);
        }

        /**
         * @todo fredt - drop support for SET IGNORECASE and replace the
         * type name with a qualifier specifying the case sensitivity of VARCHAR
         */
        if (type == Types.VARCHAR && database.isIgnoreCase()) {
            type = Types.VARCHAR_IGNORECASE;
        }

        if (type == Types.FLOAT && length > 53) {
            throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
        }

        if (type == Types.TIMESTAMP) {
            if (!hasLength) {
                length = 6;
            } else if (length != 0 && length != 6) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }

        if (type == Types.TIME) {
            if (length != 0) {
                throw Trace.error(Trace.NUMERIC_VALUE_OUT_OF_RANGE);
            }
        }

        token = tokenizer.getSimpleToken();

        if (token.equals(Token.T_DEFAULT)) {
            defaultExpr = processCreateDefaultExpression(type, length, scale);
            token       = tokenizer.getSimpleToken();
        } else if (token.equals(Token.T_GENERATED)) {
            tokenizer.getThis(Token.T_BY);
            tokenizer.getThis(Token.T_DEFAULT);
            tokenizer.getThis(Token.T_AS);
            tokenizer.getThis(Token.T_IDENTITY);

            if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
                tokenizer.getThis(Token.T_START);
                tokenizer.getThis(Token.T_WITH);

                identityStart = tokenizer.getBigint();

                if (tokenizer.isGetThis(Token.T_COMMA)) {
                    tokenizer.getThis(Token.T_INCREMENT);
                    tokenizer.getThis(Token.T_BY);

                    identityIncrement = tokenizer.getBigint();
                }

                tokenizer.getThis(Token.T_CLOSEBRACKET);
            }

            isIdentity   = true;
            isPrimaryKey = true;
            token        = tokenizer.getSimpleToken();
        }

        // fredt@users - accept IDENTITY before or after NOT NULL
        if (token.equals(Token.T_IDENTITY)) {
            isIdentity   = true;
            isPrimaryKey = true;
            token        = tokenizer.getSimpleToken();
        }

        if (token.equals(Token.T_NULL)) {
            token = tokenizer.getSimpleToken();
        } else if (token.equals(Token.T_NOT)) {
            tokenizer.getThis(Token.T_NULL);

            isNullable = false;
            token      = tokenizer.getSimpleToken();
        }

        if (token.equals(Token.T_IDENTITY)) {
            if (isIdentity) {
                throw Trace.error(Trace.SECOND_PRIMARY_KEY, Token.T_IDENTITY);
            }

            isIdentity   = true;
            isPrimaryKey = true;
            token        = tokenizer.getSimpleToken();
        }

        if (token.equals(Token.T_PRIMARY)) {
            tokenizer.getThis(Token.T_KEY);

            isPrimaryKey = true;
        } else {
            tokenizer.back();
        }

        // make sure IDENTITY and DEFAULT are not used together
        if (isIdentity && defaultExpr != null) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, Token.T_DEFAULT);
        }

        Column column = new Column(hsqlName, isNullable, type, length, scale,
                                   isPrimaryKey, defaultExpr);

        column.setIdentity(isIdentity, identityStart, identityIncrement);

        return column;
    }

    /**
     * @param type data type of column
     * @param length maximum length of column
     * @throws HsqlException
     * @return new Expression
     */
    private Expression processCreateDefaultExpression(int type, int length,
            int scale) throws HsqlException {

        if (type == Types.OTHER) {
            throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE);
        }

        Parser     parser = new Parser(session, database, tokenizer);
        Expression expr   = parser.readDefaultClause(type);

        expr.resolveTypes(session);

        int newType = expr.getType();

        if (newType == Expression.VALUE || newType == Expression.TRUE
                || newType == Expression.FALSE
                || (newType == Expression.FUNCTION
                    && expr.function.isSimple)) {
            Object defValTemp;

            try {
                defValTemp = expr.getValue(session, type);
            } catch (HsqlException e) {
                throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE);
            }

            if (defValTemp != null && database.sqlEnforceStrictSize) {
                try {
                    Column.enforceSize(defValTemp, type, length, scale, true);
                } catch (HsqlException e) {

                    // default value is too long for fixed size column
                    throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE);
                }
            }

            return expr;
        }

        throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE);
    }

    public static void checkBooleanDefault(String s,
                                           int type) throws HsqlException {

        if (type != Types.BOOLEAN || s == null) {
            return;
        }

        s = s.toUpperCase();

        if (s.equals(Token.T_TRUE) || s.equals(Token.T_FALSE)) {
            return;
        }

        if (s.equals("0") || s.equals("1")) {
            return;
        }

        throw Trace.error(Trace.WRONG_DEFAULT_CLAUSE, s);
    }

    /**
     * Responsible for handling constraints section of CREATE TABLE ...
     *
     * @param t table
     * @param constraint CONSTRAINT keyword used
     * @param primarykeycolumn primary columns
     * @throws HsqlException
     * @return list of constraints
     */
    private HsqlArrayList processCreateConstraints(Table t,
            boolean constraint, int[] primarykeycolumn) throws HsqlException {

        String        token;
        HsqlArrayList tcList;
        Constraint    tempConst;
        HsqlName      pkHsqlName;

// fredt@users 20020225 - comment
// HSQLDB relies on primary index to be the first one defined
// and needs original or system added primary key before any
// non-unique index is created
        tcList = new HsqlArrayList();
        tempConst = new Constraint(null, primarykeycolumn, null, null,
                                   Constraint.MAIN, Constraint.NO_ACTION,
                                   Constraint.NO_ACTION);

// tony_lai@users 20020820 - patch 595099
        pkHsqlName = null;

        tcList.add(tempConst);

        if (!constraint) {
            return tcList;
        }

        while (true) {
            HsqlName cname = null;

            if (tokenizer.isGetThis(Token.T_CONSTRAINT)) {
                token = tokenizer.getName();

                String constraintSchema = tokenizer.getLongNameFirst();

                if (constraintSchema != null) {
                    constraintSchema = session.getSchemaNameForWrite(
                        tokenizer.getLongNameFirst());

                    if (!t.getSchemaName().equals(constraintSchema)) {
                        throw Trace.error(
                            Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS,
                            constraintSchema);
                    }
                }

                cname = database.nameManager.newHsqlName(token,
                        tokenizer.wasQuotedIdentifier());
            }

            token = tokenizer.getSimpleToken();

            switch (Token.get(token)) {

                case Token.PRIMARY : {
                    tokenizer.getThis(Token.T_KEY);

                    // tony_lai@users 20020820 - patch 595099
                    pkHsqlName = cname;

                    int[]      cols = processColumnList(t, false);
                    Constraint mainConst;

                    mainConst = (Constraint) tcList.get(0);

                    if (mainConst.core.mainColArray != null) {
                        if (!ArrayUtil.areEqual(mainConst.core.mainColArray,
                                                cols, cols.length, true)) {
                            throw Trace.error(Trace.SECOND_PRIMARY_KEY);
                        }
                    }

                    mainConst.core.mainColArray = cols;
                    mainConst.constName         = pkHsqlName;

                    break;
                }
                case Token.UNIQUE : {
                    int[] col = processColumnList(t, false);

                    if (cname == null) {
                        cname = database.nameManager.newAutoName("CT");
                    }

                    tempConst = new Constraint(cname, col, null, null,
                                               Constraint.UNIQUE,
                                               Constraint.NO_ACTION,
                                               Constraint.NO_ACTION);

                    tcList.add(tempConst);

                    break;
                }
                case Token.FOREIGN : {
                    tokenizer.getThis(Token.T_KEY);

                    tempConst = processCreateFK(t, cname);

                    if (tempConst.core.refColArray == null) {
                        Constraint mainConst = (Constraint) tcList.get(0);

                        tempConst.core.refColArray =
                            mainConst.core.mainColArray;

                        if (tempConst.core.refColArray == null) {
                            throw Trace.error(Trace.CONSTRAINT_NOT_FOUND,
                                              Trace.TABLE_HAS_NO_PRIMARY_KEY);
                        }
                    }

                    checkFKColumnDefaults(t, tempConst);
                    t.checkColumnsMatch(tempConst.core.mainColArray,
                                        tempConst.core.refTable,
                                        tempConst.core.refColArray);
                    tcList.add(tempConst);

                    break;
                }
                case Token.CHECK : {
                    if (cname == null) {
                        cname = database.nameManager.newAutoName("CT");
                    }

                    tempConst = new Constraint(cname, null, null, null,
                                               Constraint.CHECK,
                                               Constraint.NO_ACTION,
                                               Constraint.NO_ACTION);

                    processCreateCheckConstraintCondition(tempConst);
                    tcList.add(tempConst);

                    break;
                }
            }

            token = tokenizer.getSimpleToken();

            if (token.equals(Token.T_COMMA)) {
                continue;
            }

            if (token.equals(Token.T_CLOSEBRACKET)) {
                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        return tcList;
    }

    /**
     * Responsible for handling check constraints section of CREATE TABLE ...
     *
     * @param c check constraint
     * @throws HsqlException
     */
    private void processCreateCheckConstraintCondition(Constraint c)
    throws HsqlException {

        tokenizer.getThis(Token.T_OPENBRACKET);

        Parser     parser    = new Parser(session, database, tokenizer);
        Expression condition = parser.parseExpression();

        tokenizer.getThis(Token.T_CLOSEBRACKET);

        c.core.check = condition;
    }

    /**
     * Responsible for handling the execution CREATE TABLE SQL statements.
     *
     * @param type Description of the Parameter
     * @throws HsqlException
     */
    private void processCreateTable(int type) throws HsqlException {

        String token = tokenizer.getName();
        HsqlName schemaname =
            session.getSchemaHsqlNameForWrite(tokenizer.getLongNameFirst());

        database.schemaManager.checkUserTableNotExists(session, token,
                schemaname.name);

        boolean isnamequoted = tokenizer.wasQuotedIdentifier();
        int[]   pkCols       = null;
        int     colIndex     = 0;
        boolean constraint   = false;
        Table   t = newTable(type, token, isnamequoted, schemaname);

        tokenizer.getThis(Token.T_OPENBRACKET);

        while (true) {
            token = tokenizer.getString();

            switch (Token.get(token)) {

                case Token.CONSTRAINT :
                case Token.PRIMARY :
                case Token.FOREIGN :
                case Token.UNIQUE :
                case Token.CHECK :

                    // fredt@users : check for quoted reserved words used as column names
                    constraint = !tokenizer.wasQuotedIdentifier()
                                 &&!tokenizer.wasLongName();
            }

            tokenizer.back();

            if (constraint) {
                break;
            }

            Column newcolumn = processCreateColumn();

            t.addColumn(newcolumn);

            if (newcolumn.isPrimaryKey()) {
                Trace.check(pkCols == null, Trace.SECOND_PRIMARY_KEY,
                            newcolumn.columnName.name);

                pkCols = new int[]{ colIndex };
            }

            token = tokenizer.getSimpleToken();

            if (token.equals(Token.T_COMMA)) {
                colIndex++;

                continue;
            }

            if (token.equals(Token.T_CLOSEBRACKET)) {
                break;
            }

            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        HsqlArrayList tempConstraints = processCreateConstraints(t,
            constraint, pkCols);

        if (tokenizer.isGetThis(Token.T_ON)) {
            if (!t.isTemp) {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, Token.T_ON);
            }

            tokenizer.getThis(Token.T_COMMIT);

            token = tokenizer.getSimpleToken();

            if (token.equals(Token.T_DELETE)) {}
            else if (token.equals(Token.T_PRESERVE)) {
                t.onCommitPreserve = true;
            } else {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }

            tokenizer.getThis(Token.T_ROWS);
        }

        try {
            session.commit();

            Constraint primaryConst = (Constraint) tempConstraints.get(0);

            t.createPrimaryKey(null, primaryConst.core.mainColArray, true);

            if (primaryConst.core.mainColArray != null) {
                if (primaryConst.constName == null) {
                    primaryConst.constName = t.makeSysPKName();
                }

                Constraint newconstraint =
                    new Constraint(primaryConst.constName, t, t.getPrimaryIndex(),
                                   Constraint.PRIMARY_KEY);

                t.addConstraint(newconstraint);
                database.schemaManager.registerConstraintName(
                    primaryConst.constName.name, t.getName());
            }

            for (int i = 1; i < tempConstraints.size(); i++) {
                Constraint tempConst = (Constraint) tempConstraints.get(i);

                if (tempConst.constType == Constraint.UNIQUE) {
                    TableWorks tableWorks = new TableWorks(session, t);

                    tableWorks.createUniqueConstraint(
                        tempConst.core.mainColArray, tempConst.constName);

                    t = tableWorks.getTable();
                }

                if (tempConst.constType == Constraint.FOREIGN_KEY) {
                    TableWorks tableWorks = new TableWorks(session, t);

                    tableWorks.createForeignKey(tempConst.core.mainColArray,
                                                tempConst.core.refColArray,
                                                tempConst.constName,
                                                tempConst.core.refTable,
                                                tempConst.core.deleteAction,
                                                tempConst.core.updateAction);

                    t = tableWorks.getTable();
                }

                if (tempConst.constType == Constraint.CHECK) {
                    TableWorks tableWorks = new TableWorks(session, t);

                    tableWorks.createCheckConstraint(tempConst,
                                                     tempConst.constName);

                    t = tableWorks.getTable();
                }
            }

            database.schemaManager.linkTable(t);
        } catch (HsqlException e) {

// fredt@users 20020225 - comment
// if a HsqlException is thrown while creating table, any foreign key that has
// been created leaves it modification to the expTable in place
// need to undo those modifications. This should not happen in practice.
            database.schemaManager.removeExportedKeys(t);
            database.schemaManager.removeIndexNames(t.tableName);
            database.schemaManager.removeConstraintNames(t.tableName);

            throw e;
        }
    }

// fredt@users 20020221 - patch 520213 by boucherb@users - self reference FK
// allows foreign keys that reference a column in the same table

    /**
     * @param t table
     * @param cname foreign key name
     * @throws HsqlException
     * @return constraint
     */
    private Constraint processCreateFK(Table t,
                                       HsqlName cname) throws HsqlException {

        int[]  localcol;
        int[]  expcol;
        String expTableName;
        Table  expTable;
        String token;

        localcol = processColumnList(t, false);

        tokenizer.getThis(Token.T_REFERENCES);

        expTableName = tokenizer.getName();

        String constraintSchema = tokenizer.getLongNameFirst();

        if (constraintSchema != null) {
            constraintSchema =
                session.getSchemaNameForWrite(tokenizer.getLongNameFirst());

            if (!t.getSchemaName().equals(constraintSchema)) {
                throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS,
                                  constraintSchema);
            }
        }

        if (t.getName().name.equals(expTableName)) {
            expTable = t;
        } else {
            expTable = database.schemaManager.getTable(session, expTableName,
                    t.getSchemaName());
        }

        expcol = null;
        token  = tokenizer.getSimpleToken();

        tokenizer.back();

        if (token.equals(Token.T_OPENBRACKET)) {
            expcol = processColumnList(expTable, false);
        } else {
            if (expTable.getPrimaryKey() == null) {

                // getPrimaryKey() == null is true while creating the table
                // fredt - FK statement is part of CREATE TABLE and is self-referencing
                // reference must be to same table being created
                // it is resolved in the calling method
                Trace.check(t == expTable, Trace.TABLE_HAS_NO_PRIMARY_KEY);
            } else {
                if (expTable.hasPrimaryKey()) {
                    expcol = expTable.getPrimaryKey();
                } else {
                    throw Trace.error(Trace.CONSTRAINT_NOT_FOUND,
                                      Trace.TABLE_HAS_NO_PRIMARY_KEY);
                }
            }
        }

        token = tokenizer.getSimpleToken();

        // -- In a while loop we parse a maximium of two
        // -- "ON" statements following the foreign key
        // -- definition this can be
        // -- ON [UPDATE|DELETE] [NO ACTION|RESTRICT|CASCADE|SET [NULL|DEFAULT]]
        int deleteAction = Constraint.NO_ACTION;
        int updateAction = Constraint.NO_ACTION;

        while (token.equals(Token.T_ON)) {
            token = tokenizer.getSimpleToken();

            if (deleteAction == Constraint.NO_ACTION
                    && token.equals(Token.T_DELETE)) {
                token = tokenizer.getSimpleToken();

                if (token.equals(Token.T_SET)) {
                    token = tokenizer.getSimpleToken();

                    if (token.equals(Token.T_DEFAULT)) {
                        deleteAction = Constraint.SET_DEFAULT;
                    } else if (token.equals(Token.T_NULL)) {
                        deleteAction = Constraint.SET_NULL;
                    } else {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                    }
                } else if (token.equals(Token.T_CASCADE)) {
                    deleteAction = Constraint.CASCADE;
                } else if (token.equals(Token.T_RESTRICT)) {

                    // LEGACY compatibility/usability
                    // - same as NO ACTION or nothing at all
                } else {
                    tokenizer.matchThis(Token.T_NO);
                    tokenizer.getThis(Token.T_ACTION);
                }
            } else if (updateAction == Constraint.NO_ACTION
                       && token.equals(Token.T_UPDATE)) {
                token = tokenizer.getSimpleToken();

                if (token.equals(Token.T_SET)) {
                    token = tokenizer.getSimpleToken();

                    if (token.equals(Token.T_DEFAULT)) {
                        updateAction = Constraint.SET_DEFAULT;
                    } else if (token.equals(Token.T_NULL)) {
                        updateAction = Constraint.SET_NULL;
                    } else {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                    }
                } else if (token.equals(Token.T_CASCADE)) {
                    updateAction = Constraint.CASCADE;
                } else if (token.equals(Token.T_RESTRICT)) {

                    // LEGACY compatibility/usability
                    // - same as NO ACTION or nothing at all
                } else {
                    tokenizer.matchThis(Token.T_NO);
                    tokenizer.getThis(Token.T_ACTION);
                }
            } else {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }

            token = tokenizer.getSimpleToken();
        }

        tokenizer.back();

        if (cname == null) {
            cname = database.nameManager.newAutoName("FK");
        }

        return new Constraint(cname, localcol, expTable, expcol,
                              Constraint.FOREIGN_KEY, deleteAction,
                              updateAction);
    }

    /**
     * Responsible for handling the execution CREATE VIEW SQL statements.
     *
     * @throws HsqlException
     */
    private void processCreateView() throws HsqlException {

        String name = tokenizer.getName();
        HsqlName schemaname =
            session.getSchemaHsqlNameForWrite(tokenizer.getLongNameFirst());
        int logposition = tokenizer.getPartMarker();

        database.schemaManager.checkUserViewNotExists(session, name,
                schemaname.name);

        HsqlName viewHsqlName = database.nameManager.newHsqlName(name,
            tokenizer.wasQuotedIdentifier());

        viewHsqlName.schema = schemaname;

        HsqlName[] colList = null;

        // fredt - a bug in 1.8.0.0 and previous versions causes view
        // definitions to script without double quotes around column names
        // in certain cases; the workaround here discards such scripted column
        // lists when used in OOo
        if (tokenizer.isGetThis(Token.T_OPENBRACKET)) {
            try {
                HsqlArrayList list = Parser.getColumnNames(database, null,
                    tokenizer, true);

                colList = new HsqlName[list.size()];
                colList = (HsqlName[]) list.toArray(colList);

                //added lines to make sure all valid columns are quoted
                if (database.isStoredFileAccess()) {
                    for (int i = 0; i < colList.length; i++) {
                        if (!colList[i].isNameQuoted) {
                            colList = null;

                            break;
                        }
                    }
                }
            } catch (HsqlException e) {

                //added lines to catch unquoted names with spaces
                if (database.isStoredFileAccess()) {
                    while (!tokenizer.getString().equals(
                            Token.T_CLOSEBRACKET)) {}
                } else {
                    throw e;
                }
            }
        }

        tokenizer.getThis(Token.T_AS);
        tokenizer.setPartMarker();

        Parser parser   = new Parser(session, database, tokenizer);
        int    brackets = parser.parseOpenBracketsSelect();
        Select select;

        // accept ORDER BY or ORDRY BY with LIMIT - accept unions
        select = parser.parseSelect(brackets, true, false, true, true);

        if (select.sIntoTable != null) {
            throw (Trace.error(Trace.INVALID_IDENTIFIER, Token.INTO));
        }

        select.prepareResult(session);

        View view = new View(session, database, viewHsqlName,
                             tokenizer.getLastPart(), colList);

        session.commit();
        database.schemaManager.linkTable(view);
        tokenizer.setPartMarker(logposition);
    }

    /**
     * Responsible for handling tail of ALTER TABLE ... RENAME ...
     * @param t table
     * @throws HsqlException
     */
    private void processAlterTableRename(Table t) throws HsqlException {

        String  schema = t.getSchemaName();
        String  newName;
        boolean isquoted;

        // ensures that if temp table, it also belongs to this session
/*
        if (!t.equals(session, name)) {
            throw Trace.error(Trace.TABLE_NOT_FOUND);
        }
*/
        tokenizer.getThis(Token.T_TO);

        newName = tokenizer.getName();

        String newSchema = tokenizer.getLongNameFirst();

        isquoted  = tokenizer.wasQuotedIdentifier();
        newSchema = newSchema == null ? schema
                                      : session.getSchemaNameForWrite(
                                          newSchema);

        if (!schema.equals(newSchema)) {
            throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
        }

        database.schemaManager.checkUserTableNotExists(session, newName,
                schema);
        session.commit();
        session.setScripting(true);
        database.schemaManager.renameTable(session, t, newName, isquoted);
    }

    /**
     * Handles ALTER TABLE statements. <p>
     *
     * ALTER TABLE <name> RENAME TO <newname>
     * ALTER INDEX <name> RENAME TO <newname>
     *
     * ALTER TABLE <name> ADD CONSTRAINT <constname> FOREIGN KEY (<col>, ...)
     * REFERENCE <other table> (<col>, ...) [ON DELETE CASCADE]
     *
     * ALTER TABLE <name> ADD CONSTRAINT <constname> UNIQUE (<col>, ...)
     *
     * @throws HsqlException
     */
    private void processAlter() throws HsqlException {

        String token;

        session.checkAdmin();
        session.checkDDLWrite();
        session.setScripting(true);

        token = tokenizer.getSimpleToken();

        switch (Token.get(token)) {

            case Token.INDEX : {
                processAlterIndex();

                break;
            }
            case Token.SCHEMA : {
                processAlterSchema();

                break;
            }
            case Token.SEQUENCE : {
                processAlterSequence();

                break;
            }
            case Token.TABLE : {
                processAlterTable();

                break;
            }
            case Token.USER : {
                processAlterUser();

                break;
            }
            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }
        }
    }

    /**
     * Handles ALTER TABLE DDL.
     *
     * @throws HsqlException
     */
    private void processAlterTable() throws HsqlException {

        String tableName = tokenizer.getName();
        String schema =
            session.getSchemaNameForWrite(tokenizer.getLongNameFirst());
        Table t = database.schemaManager.getUserTable(session, tableName,
            schema);
        String token;

        if (t.isView()) {
            throw Trace.error(Trace.NOT_A_TABLE);
        }

        session.setScripting(true);

        token = tokenizer.getSimpleToken();

        switch (Token.get(token)) {

            case Token.RENAME : {
                processAlterTableRename(t);

                return;
            }
            case Token.ADD : {
                HsqlName cname = null;

                if (tokenizer.isGetThis(Token.T_CONSTRAINT)) {
                    token = tokenizer.getName();

                    String constraintSchema = tokenizer.getLongNameFirst();

                    if (constraintSchema != null) {
                        constraintSchema = session.getSchemaNameForWrite(
                            tokenizer.getLongNameFirst());

                        if (!t.getSchemaName().equals(constraintSchema)) {
                            throw Trace.error(
                                Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS,
                                constraintSchema);
                        }
                    }

                    cname = database.nameManager.newHsqlName(token,
                            tokenizer.wasQuotedIdentifier());
                }

                token = tokenizer.getString();

                if (tokenizer.wasQuotedIdentifier()
                        && tokenizer.wasSimpleName()) {
                    tokenizer.back();
                    processAlterTableAddColumn(t);

                    return;
                }

                if (!tokenizer.wasSimpleToken()) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }

                switch (Token.get(token)) {

                    case Token.FOREIGN :
                        tokenizer.getThis(Token.T_KEY);
                        processAlterTableAddForeignKeyConstraint(t, cname);

                        return;

                    case Token.UNIQUE :
                        processAlterTableAddUniqueConstraint(t, cname);

                        return;

                    case Token.CHECK :
                        processAlterTableAddCheckConstraint(t, cname);

                        return;

                    case Token.PRIMARY :
                        tokenizer.getThis(Token.T_KEY);
                        processAlterTableAddPrimaryKey(t, cname);

                        return;

                    default :
                        if (cname != null) {
                            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                        }

                        tokenizer.back();
                    case Token.COLUMN :
                        processAlterTableAddColumn(t);

                        return;
                }
            }
            case Token.DROP : {
                token = tokenizer.getString();

                if (tokenizer.wasQuotedIdentifier()
                        && tokenizer.wasSimpleName()) {
                    tokenizer.back();
                    processAlterTableDropColumn(t);

                    return;
                }

                if (!tokenizer.wasSimpleToken()) {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }

                switch (Token.get(token)) {

                    case Token.PRIMARY :
                        tokenizer.getThis(Token.T_KEY);

                        if (t.hasPrimaryKey()) {
                            processAlterTableDropConstraint(
                                t, t.getPrimaryConstraint().getName().name);
                        } else {
                            throw Trace.error(Trace.CONSTRAINT_NOT_FOUND,
                                              Trace.TABLE_HAS_NO_PRIMARY_KEY,
                                              new Object[] {
                                "PRIMARY KEY", t.getName().name
                            });
                        }

                        return;

                    case Token.CONSTRAINT :
                        processAlterTableDropConstraint(t);

                        return;

                    default :
                        tokenizer.back();
                    case Token.COLUMN :
                        processAlterTableDropColumn(t);

                        return;
                }
            }
            case Token.ALTER : {
                tokenizer.isGetThis(Token.T_COLUMN);
                processAlterColumn(t);

                return;
            }
            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }
        }
    }

    /**
     * Handles ALTER COLUMN
     *
     * @param t table
     * @throws HsqlException
     */
    private void processAlterColumn(Table t) throws HsqlException {

        String columnName  = tokenizer.getSimpleName();
        int    columnIndex = t.getColumnNr(columnName);
        Column column      = t.getColumn(columnIndex);
        String token       = tokenizer.getSimpleToken();

        switch (Token.get(token)) {

            case Token.RENAME : {
                tokenizer.getThis(Token.T_TO);
                processAlterColumnRename(t, column);

                return;
            }
            case Token.DROP : {
                tokenizer.getThis(Token.T_DEFAULT);

                TableWorks tw = new TableWorks(session, t);

                tw.setColDefaultExpression(columnIndex, null);

                return;
            }
            case Token.SET : {

//4-8-2005 MarcH and HuugO ALTER TABLE <tablename> ALTER COLUMN <column name> SET [NOT] NULL support added
                token = tokenizer.getSimpleToken();

                if (token.equals(Token.T_NOT)) {
                    tokenizer.getThis(Token.T_NULL);

                    TableWorks tw = new TableWorks(session, t);

                    tw.setColNullability(column, false);
                } else if (token.equals(Token.T_NULL)) {
                    TableWorks tw = new TableWorks(session, t);

                    tw.setColNullability(column, true);
                } else if (token.equals(Token.T_DEFAULT)) {

                    //alter table alter column set default
                    TableWorks tw     = new TableWorks(session, t);
                    int        type   = column.getType();
                    int        length = column.getSize();
                    int        scale  = column.getScale();
                    Expression expr = processCreateDefaultExpression(type,
                        length, scale);

                    tw.setColDefaultExpression(columnIndex, expr);
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }

                return;
            }
            case Token.RESTART : {
                tokenizer.getThis(Token.T_WITH);

                long identityStart = tokenizer.getBigint();
                int  id            = t.getIdentityColumn();

                if (id == -1) {
                    throw Trace.error(Trace.OPERATION_NOT_SUPPORTED);
                }

                t.identitySequence.reset(identityStart);

                return;
            }
            default : {
                tokenizer.back();
                processAlterColumnType(t, column);
            }
        }
    }

    private void processAlterColumnType(Table table,
                                        Column oldCol) throws HsqlException {

        Column     newCol = processCreateColumn(oldCol.columnName);
        TableWorks tw     = new TableWorks(session, table);

        tw.reTypeColumn(oldCol, newCol);
    }

    /**
     * Responsible for handling tail of ALTER COLUMN ... RENAME ...
     * @param t table
     * @param column column
     * @throws HsqlException
     */
    private void processAlterColumnRename(Table t,
                                          Column column)
                                          throws HsqlException {

        String  newName  = tokenizer.getSimpleName();
        boolean isquoted = tokenizer.wasQuotedIdentifier();

        if (t.findColumn(newName) > -1) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS, newName);
        }

        t.database.schemaManager.checkColumnIsInView(t,
                column.columnName.name);
        session.commit();
        session.setScripting(true);
        t.renameColumn(column, newName, isquoted);
    }

    /**
     * Handles ALTER INDEX.
     *
     * @throws HsqlException
     */
    private void processAlterIndex() throws HsqlException {

        // only the one supported operation, so far
        processAlterIndexRename();
    }

    private void processAlterSchema() throws HsqlException {

        // only the one supported operation, so far
        processAlterSchemaRename();
    }

    /**
     * Responsible for handling parse and execute of SQL DROP DDL
     *
     * @throws  HsqlException
     */
    private void processDrop() throws HsqlException {

        String  token;
        boolean isview;

        session.checkReadWrite();
        session.checkAdmin();
        session.setScripting(true);

        token  = tokenizer.getSimpleToken();
        isview = false;

        switch (Token.get(token)) {

            case Token.INDEX : {
                processDropIndex();

                break;
            }
            case Token.SCHEMA : {
                processDropSchema();

                break;
            }
            case Token.SEQUENCE : {
                processDropSequence();

                break;
            }
            case Token.TRIGGER : {
                processDropTrigger();

                break;
            }
            case Token.USER : {
                processDropUser();

                break;
            }
            case Token.ROLE : {
                database.getGranteeManager().dropRole(
                    tokenizer.getSimpleName());

                break;
            }
            case Token.VIEW : {
                isview = true;
            }    //fall thru
            case Token.TABLE : {
                processDropTable(isview);

                break;
            }
            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }
        }
    }

    /**
     *  Responsible for handling the execution of GRANT and REVOKE SQL
     *  statements.
     *
     * @param grant true if grant, false if revoke
     * @throws HsqlException
     */
    private void processGrantOrRevoke(boolean grant) throws HsqlException {

        int    right;
        Object accessKey;
        String token;

        session.checkAdmin();
        session.checkDDLWrite();
        session.setScripting(true);

        right = 0;
        token = tokenizer.getSimpleToken();

        tokenizer.back();

        if (!GranteeManager.validRightString(token)) {
            processRoleGrantOrRevoke(grant);

            return;
        }

        do {
            token = tokenizer.getSimpleToken();
            right |= GranteeManager.getCheckRight(token);
        } while (tokenizer.isGetThis(Token.T_COMMA));

        tokenizer.getThis(Token.T_ON);

        accessKey = null;

        if (tokenizer.isGetThis(Token.T_CLASS)) {
            accessKey = tokenizer.getSimpleName();

            if (!tokenizer.wasQuotedIdentifier()) {
                throw Trace.error(Trace.QUOTED_IDENTIFIER_REQUIRED);
            }
        } else {
            token = tokenizer.getName();

            String schema =
                session.getSchemaName(tokenizer.getLongNameFirst());
            Table t = database.schemaManager.getTable(session, token, schema);

            accessKey = t.getName();

            session.setScripting(true);
        }

        tokenizer.getThis(grant ? Token.T_TO
                                : Token.T_FROM);

        token = getUserIdentifier();

        GranteeManager gm = database.getGranteeManager();

        if (grant) {
            gm.grant(token, accessKey, right);
        } else {
            gm.revoke(token, accessKey, right);
        }
    }

    /**
     * Responsible for handling CONNECT
     *
     * @throws HsqlException
     */
    private void processConnect() throws HsqlException {

        String userName;
        String password;
        User   user;

        tokenizer.getThis(Token.T_USER);

        userName = getUserIdentifier();

        if (tokenizer.isGetThis(Token.T_PASSWORD)) {

            // legacy log statement or connect statement issued by user
            password = getPassword();
            user     = database.getUserManager().getUser(userName, password);

            session.commit();
            session.setUser(user);
            database.logger.logConnectUser(session);
        } else if (session.isProcessingLog) {

            // processing log statement
            // do not change the user, as isSys() must remain true when processing log
            session.commit();
        } else {

            // force throw if not log statement
            tokenizer.getThis(Token.T_PASSWORD);
        }
    }

    /**
     * Responsible for handling the execution of SET SQL statements
     *
     * @throws  HsqlException
     */
    private void processSet() throws HsqlException {

        String token;

        session.setScripting(true);

        token = tokenizer.getSimpleToken();

        switch (Token.get(token)) {

            case Token.PROPERTY : {
                HsqlDatabaseProperties p;

                session.checkAdmin();

                token = tokenizer.getSimpleName();

                if (!tokenizer.wasQuotedIdentifier()) {
                    throw Trace.error(Trace.QUOTED_IDENTIFIER_REQUIRED);
                }

                p = database.getProperties();

                boolean isboolean  = p.isBoolean(token);
                boolean isintegral = p.isIntegral(token);
                boolean isstring   = p.isString(token);

                Trace.check(isboolean || isintegral || isstring,
                            Trace.ACCESS_IS_DENIED, token);

                int    type  = isboolean ? Types.BOOLEAN
                                         : isintegral ? Types.INTEGER
                                                      : Types.VARCHAR;
                Object value = tokenizer.getInType(type);

                if (HsqlDatabaseProperties.hsqldb_cache_file_scale.equals(
                        token)) {
                    if (database.logger.hasCache()
                            || ((Integer) value).intValue() != 8) {
                        Trace.throwerror(Trace.ACCESS_IS_DENIED, token);
                    }
                }

                p.setDatabaseProperty(token, value.toString().toLowerCase());
                p.setDatabaseVariables();

                break;
            }
            case Token.SCHEMA : {
                session.setScripting(false);
                session.setSchema(tokenizer.getSimpleName());

                break;
            }
            case Token.PASSWORD : {
                session.checkDDLWrite();
                session.getUser().setPassword(getPassword());

                break;
            }
            case Token.READONLY : {
                session.commit();
                session.setReadOnly(processTrueOrFalse());

                break;
            }
            case Token.LOGSIZE : {
                session.checkAdmin();
                session.checkDDLWrite();

                int i = tokenizer.getInt();

                database.logger.setLogSize(i);

                break;
            }
            case Token.SCRIPTFORMAT : {
                session.checkAdmin();
                session.checkDDLWrite();
                session.setScripting(false);

                token = tokenizer.getSimpleToken();

                int i = ArrayUtil.find(ScriptWriterBase.LIST_SCRIPT_FORMATS,
                                       token);

                if (i == 0 || i == 1 || i == 3) {
                    database.logger.setScriptType(i);
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                }

                break;
            }
            case Token.IGNORECASE : {
                session.checkAdmin();
                session.checkDDLWrite();
                database.setIgnoreCase(processTrueOrFalse());

                break;
            }
            case Token.MAXROWS : {
                session.setScripting(false);

                int i = tokenizer.getInt();

                session.setSQLMaxRows(i);

                break;
            }
            case Token.AUTOCOMMIT : {
                session.setAutoCommit(processTrueOrFalse());

                break;
            }
            case Token.TABLE : {
                session.checkAdmin();
                session.checkDDLWrite();

                token = tokenizer.getName();

                String schema = session.getSchemaNameForWrite(
                    tokenizer.getLongNameFirst());
                Table t = database.schemaManager.getTable(session, token,
                    schema);

                token = tokenizer.getSimpleToken();

                session.setScripting(true);

                switch (Token.get(token)) {

                    default : {
                        throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
                    }
                    case Token.SOURCE : {
                        session.checkAdmin();

                        if (tokenizer.isGetThis(Token.T_HEADER)) {
                            token = tokenizer.getString();

                            if (!tokenizer.wasQuotedIdentifier()) {
                                throw Trace.error(Trace.TEXT_TABLE_SOURCE);
                            }

                            t.setHeader(token);

                            break;
                        }

                        token = tokenizer.getString();

                        if (!tokenizer.wasQuotedIdentifier()) {
                            throw Trace.error(Trace.TEXT_TABLE_SOURCE);
                        }

                        boolean isDesc = false;

                        isDesc = tokenizer.isGetThis(Token.T_DESC);

                        t.setDataSource(session, token, isDesc, false);

                        break;
                    }
                    case Token.READONLY : {
                        session.checkAdmin();
                        t.setDataReadOnly(processTrueOrFalse());

                        break;
                    }
                    case Token.INDEX : {
                        session.checkAdmin();

                        String roots =
                            (String) tokenizer.getInType(Types.VARCHAR);

                        t.setIndexRoots(roots);

                        break;
                    }
                }

                break;
            }
            case Token.REFERENTIAL_INTEGRITY : {
                session.checkAdmin();
                session.checkDDLWrite();
                session.setScripting(false);
                database.setReferentialIntegrity(processTrueOrFalse());

                break;
            }
            case Token.CHECKPOINT : {
                session.checkAdmin();
                session.checkDDLWrite();
                tokenizer.getThis(Token.T_DEFRAG);

                int size = tokenizer.getInt();

                database.getProperties().setProperty(
                    HsqlDatabaseProperties.hsqldb_defrag_limit, size);

                break;
            }
            case Token.WRITE_DELAY : {
                session.checkAdmin();
                session.checkDDLWrite();

                int delay = 0;

                tokenizer.getString();

                Object value = tokenizer.getAsValue();

                if (tokenizer.getType() == Types.INTEGER) {
                    delay = ((Integer) value).intValue();
                } else if (Boolean.TRUE.equals(value)) {
                    delay = database.getProperties().getDefaultWriteDelay();
                } else if (Boolean.FALSE.equals(value)) {
                    delay = 0;
                } else {
                    throw Trace.error(Trace.UNEXPECTED_TOKEN);
                }

                if (!tokenizer.isGetThis("MILLIS")) {
                    delay *= 1000;
                }

                database.logger.setWriteDelay(delay);

                break;
            }
            case Token.DATABASE : {
                session.checkAdmin();
                session.checkDDLWrite();
                tokenizer.getThis(Token.T_COLLATION);

                String cname = tokenizer.getSimpleName();

                if (!tokenizer.wasQuotedIdentifier()) {
                    throw Trace.error(Trace.INVALID_IDENTIFIER);
                }

                database.collation.setCollation(cname);

                break;
            }
            default : {
                throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
            }
        }
    }

    /**
     * Retrieves boolean value corresponding to the next token.
     *
     * @return   true if next token is "TRUE"; false if next token is "FALSE"
     * @throws  HsqlException if the next token is neither "TRUE" or "FALSE"
     */
    private boolean processTrueOrFalse() throws HsqlException {

        String sToken = tokenizer.getSimpleToken();

        if (sToken.equals(Token.T_TRUE)) {
            return true;
        } else if (sToken.equals(Token.T_FALSE)) {
            return false;
        } else {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, sToken);
        }
    }

    /**
     * Responsible for  handling the execution of COMMIT [WORK]
     *
     * @throws  HsqlException
     */
    private void processCommit() throws HsqlException {
        tokenizer.isGetThis(Token.T_WORK);
        session.commit();
    }

    /**
     * Responsible for handling the execution of ROLLBACK SQL statements.
     *
     * @throws  HsqlException
     */
    private void processRollback() throws HsqlException {

        String  token;
        boolean toSavepoint;

        token       = tokenizer.getSimpleToken();
        toSavepoint = false;

        if (token.equals(Token.T_WORK)) {

            // do nothing
        } else if (token.equals(Token.T_TO)) {
            tokenizer.getThis(Token.T_SAVEPOINT);

            token       = tokenizer.getSimpleName();
            toSavepoint = true;
        } else {
            tokenizer.back();
        }

        if (toSavepoint) {
            session.rollbackToSavepoint(token);
        } else {
            session.rollback();
        }
    }

    /**
     * Responsible for handling the execution of SAVEPOINT SQL statements.
     *
     * @throws  HsqlException
     */
    private void processSavepoint() throws HsqlException {

        String token;

        token = tokenizer.getSimpleName();

        session.savepoint(token);
    }

    /**
     * Responsible for handling the execution of SHUTDOWN SQL statements
     *
     * @throws  HsqlException
     */
    private void processShutdown() throws HsqlException {

        int    closemode;
        String token;

        // HUH?  We should *NEVER* be able to get here if session is closed
        if (!session.isClosed()) {
            session.checkAdmin();
        }

        closemode = Database.CLOSEMODE_NORMAL;
        token     = tokenizer.getSimpleToken();

        // fredt - todo - catch misspelt qualifiers here and elsewhere
        if (token.equals(Token.T_IMMEDIATELY)) {
            closemode = Database.CLOSEMODE_IMMEDIATELY;
        } else if (token.equals(Token.T_COMPACT)) {
            closemode = Database.CLOSEMODE_COMPACT;
        } else if (token.equals(Token.T_SCRIPT)) {
            closemode = Database.CLOSEMODE_SCRIPT;
        } else if (token.equals(Token.T_SEMICOLON)) {

            // only semicolon is accepted here
        } else if (token.length() != 0) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        database.close(closemode);
    }

    /**
     * Responsible for handling CHECKPOINT [DEFRAG].
     *
     * @throws  HsqlException
     */
    private void processCheckpoint() throws HsqlException {

        boolean defrag;
        String  token;

        session.checkAdmin();
        session.checkDDLWrite();

        defrag = false;
        token  = tokenizer.getSimpleToken();

        if (token.equals(Token.T_DEFRAG)) {
            defrag = true;
        } else if (token.equals(Token.T_SEMICOLON)) {

            // only semicolon is accepted here
        } else if (token.length() != 0) {
            throw Trace.error(Trace.UNEXPECTED_TOKEN, token);
        }

        database.logger.checkpoint(defrag);
    }

// --------------------- new methods / simplifications ------------------------
    private HsqlName newIndexHsqlName(String name,
                                      boolean isQuoted) throws HsqlException {

        return HsqlName.isReservedIndexName(name)
               ? database.nameManager.newAutoName("USER", name)
               : database.nameManager.newHsqlName(name, isQuoted);
    }

    private Table newTable(int type, String name, boolean quoted,
                           HsqlName schema) throws HsqlException {

        HsqlName tableHsqlName = database.nameManager.newHsqlName(name,
            quoted);

        tableHsqlName.schema = schema;

        switch (type) {

            case Table.TEMP_TEXT_TABLE :
            case Table.TEXT_TABLE : {
                return new TextTable(database, tableHsqlName, type);
            }
            default : {
                return new Table(database, tableHsqlName, type);
            }
        }
    }

    /**
     * Checks if the attributes of the Column argument, c, are compatible with
     * the operation of adding such a Column to the Table argument, t.
     *
     * @param t to which to add the Column, c
     * @param c the Column to add to the Table, t
     * @throws HsqlException if the operation of adding the Column, c, to
     *      the table t is not valid
     */
    private void checkAddColumn(Table t, Column c) throws HsqlException {

        boolean canAdd = true;

        if (t.findColumn(c.columnName.name) != -1) {
            throw Trace.error(Trace.COLUMN_ALREADY_EXISTS);
        }

        if (c.isPrimaryKey() && t.hasPrimaryKey()) {
            canAdd = false;
        }

        if (canAdd &&!t.isEmpty(session)) {
            canAdd = c.isNullable() || c.getDefaultExpression() != null;
        }

        if (!canAdd) {
            throw Trace.error(Trace.BAD_ADD_COLUMN_DEFINITION);
        }
    }

    private void checkFKColumnDefaults(Table t,
                                       Constraint tc) throws HsqlException {

        boolean check = tc.core.updateAction == Constraint.SET_DEFAULT;

        check = check || tc.core.deleteAction == Constraint.SET_DEFAULT;

        if (check) {
            int[] localCol = tc.core.mainColArray;

            for (int j = 0; j < localCol.length; j++) {
                Column     column  = t.getColumn(localCol[j]);
                Expression defExpr = column.getDefaultExpression();

                if (defExpr == null) {
                    String columnName = column.columnName.name;

                    throw Trace.error(Trace.NO_DEFAULT_VALUE_FOR_COLUMN,
                                      new Object[]{ columnName });
                }
            }
        }
    }

    private void processAlterSequence() throws HsqlException {

        long   start;
        String name       = tokenizer.getName();
        String schemaname = tokenizer.getLongNameFirst();

        schemaname = session.getSchemaNameForWrite(schemaname);

        tokenizer.getThis(Token.T_RESTART);
        tokenizer.getThis(Token.T_WITH);

        start = tokenizer.getBigint();

        NumberSequence seq = database.schemaManager.getSequence(name,
            schemaname);

        seq.reset(start);
    }

    /**
     * Handles ALTER INDEX &lt;index-name&gt; RENAME.
     *
     * @throws HsqlException
     */
    private void processAlterIndexRename() throws HsqlException {

        String name = tokenizer.getName();
        String schema =
            session.getSchemaNameForWrite(tokenizer.getLongNameFirst());

        tokenizer.getThis(Token.T_RENAME);
        tokenizer.getThis(Token.T_TO);

        String newName   = tokenizer.getName();
        String newSchema = tokenizer.getLongNameFirst();

        newSchema = newSchema == null ? schema
                                      : session.getSchemaNameForWrite(
                                          newSchema);

        boolean isQuoted = tokenizer.wasQuotedIdentifier();

        if (!schema.equals(newSchema)) {
            throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
        }

        Table t = database.schemaManager.findUserTableForIndex(session, name,
            schema);

        if (t == null) {
            throw Trace.error(Trace.INDEX_NOT_FOUND, name);
        }

        database.schemaManager.checkIndexExists(name, t.getSchemaName(),
                true);

        if (HsqlName.isReservedIndexName(name)) {
            throw Trace.error(Trace.SYSTEM_INDEX, name);
        }

        if (HsqlName.isReservedIndexName(newName)) {
            throw Trace.error(Trace.BAD_INDEX_CONSTRAINT_NAME, newName);
        }

        session.setScripting(true);
        session.commit();
        t.getIndex(name).setName(newName, isQuoted);
        database.schemaManager.renameIndex(name, newName, t.getName());
    }

    /**
     * Handles ALTER SCHEMA ... RENAME TO .
     *
     * @throws HsqlException
     */
    private void processAlterSchemaRename() throws HsqlException {

        String name = tokenizer.getSimpleName();

        tokenizer.getThis(Token.T_RENAME);
        tokenizer.getThis(Token.T_TO);

        String  newName  = tokenizer.getSimpleName();
        boolean isQuoted = tokenizer.wasQuotedIdentifier();

        database.schemaManager.renameSchema(name, newName, isQuoted);
    }

    /**
     *
     * @param t table
     * @throws HsqlException
     */
    private void processAlterTableAddColumn(Table t) throws HsqlException {

        String token;
        int    colindex = t.getColumnCount();
        Column column   = processCreateColumn();

        checkAddColumn(t, column);

        if (tokenizer.isGetThis(Token.T_BEFORE)) {
            token    = tokenizer.getSimpleName();
            colindex = t.getColumnNr(token);
        }

        session.commit();

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.addColumn(column, colindex);

        return;
    }

    /**
     * Responsible for handling tail of ALTER TABLE ... DROP COLUMN ...
     *
     * @param t table
     * @throws HsqlException
     */
    private void processAlterTableDropColumn(Table t) throws HsqlException {

        String token;
        int    colindex;

        token    = tokenizer.getName();
        colindex = t.getColumnNr(token);

        session.commit();

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.dropColumn(colindex);
    }

    /**
     * Responsible for handling tail of ALTER TABLE ... DROP CONSTRAINT ...
     *
     * @param t table
     * @throws HsqlException
     */
    private void processAlterTableDropConstraint(Table t)
    throws HsqlException {
        processAlterTableDropConstraint(t, tokenizer.getName());
    }

    /**
     * Responsible for handling tail of ALTER TABLE ... DROP CONSTRAINT ...
     *
     * @param t table
     * @param name
     * @throws HsqlException
     */
    private void processAlterTableDropConstraint(Table t,
            String cname) throws HsqlException {

        session.commit();

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.dropConstraint(cname);

        return;
    }

    /**
     * If an invalid alias is encountered while processing an old script,
     * simply discard it.
     */
    private void processCreateAlias() throws HsqlException {

        String alias;
        String methodFQN;

        try {
            alias = tokenizer.getSimpleName();
        } catch (HsqlException e) {
            if (session.isProcessingScript()) {
                alias = null;
            } else {
                throw e;
            }
        }

        tokenizer.getThis(Token.T_FOR);

        methodFQN = upgradeMethodFQN(tokenizer.getSimpleName());

        if (alias != null) {
            database.getAliasMap().put(alias, methodFQN);
        }
    }

    private void processCreateIndex(boolean unique) throws HsqlException {

        Table   t;
        String  indexName       = tokenizer.getName();
        String  schema          = tokenizer.getLongNameFirst();
        boolean indexNameQuoted = tokenizer.wasQuotedIdentifier();

        tokenizer.getThis(Token.T_ON);

        String tablename = tokenizer.getName();
        String tableschema =
            session.getSchemaNameForWrite(tokenizer.getLongNameFirst());

        if (schema != null &&!schema.equals(tableschema)) {
            throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
        }

        t = database.schemaManager.getTable(session, tablename, tableschema);

        database.schemaManager.checkIndexExists(indexName, t.getSchemaName(),
                false);

        HsqlName indexHsqlName = newIndexHsqlName(indexName, indexNameQuoted);
        int[]    indexColumns  = processColumnList(t, true);
        String   extra         = tokenizer.getSimpleToken();

        if (!Token.T_DESC.equals(extra) &&!Token.T_ASC.equals(extra)) {
            tokenizer.back();
        }

        session.commit();
        session.setScripting(true);

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.createIndex(indexColumns, indexHsqlName, unique, false,
                               false);
    }

    /**
     * limitations in Tokenizer dictate that initial value or increment must
     * be positive
     * @throws HsqlException
     */
    private void processCreateSequence() throws HsqlException {

/*
        CREATE SEQUENCE <name>
        [AS {INTEGER | BIGINT}]
        [START WITH <value>]
        [INCREMENT BY <value>]
*/
        int     type      = Types.INTEGER;
        long    increment = 1;
        long    start     = 0;
        String  name      = tokenizer.getName();
        boolean isquoted  = tokenizer.wasQuotedIdentifier();
        HsqlName schemaname =
            session.getSchemaHsqlNameForWrite(tokenizer.getLongNameFirst());

        if (tokenizer.isGetThis(Token.T_AS)) {
            String typestring = tokenizer.getSimpleToken();

            type = Types.getTypeNr(typestring);

            Trace.check(type == Types.INTEGER || type == Types.BIGINT,
                        Trace.WRONG_DATA_TYPE);
        }

        if (tokenizer.isGetThis(Token.T_START)) {
            tokenizer.getThis(Token.T_WITH);

            start = tokenizer.getBigint();
        }

        if (tokenizer.isGetThis(Token.T_INCREMENT)) {
            tokenizer.getThis(Token.T_BY);

            increment = tokenizer.getBigint();
        }

        HsqlName hsqlname = database.nameManager.newHsqlName(name, isquoted);

        hsqlname.schema = schemaname;

        database.schemaManager.createSequence(hsqlname, start, increment,
                                              type);
    }

    /**
     * CREATE SCHEMA PUBLIC in scripts should pass this, so we do not throw
     * if this schema is created a second time
     */
    private void processCreateSchema() throws HsqlException {

        String  name     = tokenizer.getSimpleName();
        boolean isquoted = tokenizer.wasQuotedIdentifier();

        if (session.isSchemaDefintion()) {
            throw Trace.error(Trace.INVALID_IDENTIFIER);
        }

        tokenizer.getThis(Token.T_AUTHORIZATION);
        tokenizer.getThis(GranteeManager.DBA_ADMIN_ROLE_NAME);

        if (database.schemaManager.schemaExists(name)) {
            if (!session.isProcessingScript) {
                throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
            }
        } else {
            database.schemaManager.createSchema(name, isquoted);
        }

        HsqlName schemaName = database.schemaManager.getSchemaHsqlName(name);

        database.logger.writeToLog(session,
                                   DatabaseScript.getSchemaCreateDDL(database,
                                       schemaName));
        database.logger.writeToLog(session,
                                   "SET SCHEMA " + schemaName.statementName);
        session.startSchemaDefinition(name);

        session.loggedSchema = session.currentSchema;
    }

    private void processCreateUser() throws HsqlException {

        String  name;
        String  password;
        boolean admin;

        name = getUserIdentifier();

        tokenizer.getThis(Token.T_PASSWORD);

        password = getPassword();
        admin    = tokenizer.isGetThis(Token.T_ADMIN);

        database.getUserManager().createUser(name, password);

        if (admin) {
            database.getGranteeManager().grant(
                name, GranteeManager.DBA_ADMIN_ROLE_NAME);
        }
    }

    private void processDisconnect() throws HsqlException {
        session.close();
    }

    private void processDropTable(boolean isView) throws HsqlException {

        boolean ifexists = false;
        boolean cascade  = false;

        if (tokenizer.isGetThis(Token.T_IF)) {
            tokenizer.getThis(Token.T_EXISTS);

            ifexists = true;
        }

        String name   = tokenizer.getName();
        String schema = tokenizer.getLongNameFirst();

        if (tokenizer.isGetThis(Token.T_IF)) {
            tokenizer.getThis(Token.T_EXISTS);

            ifexists = true;
        }

        cascade = tokenizer.isGetThis(Token.T_CASCADE);

        if (!cascade) {
            tokenizer.isGetThis(Token.T_RESTRICT);
        }

        if (ifexists && schema != null
                &&!database.schemaManager.schemaExists(schema)) {
            return;
        }

        schema = session.getSchemaNameForWrite(schema);

        database.schemaManager.dropTable(session, name, schema, ifexists,
                                         isView, cascade);
    }

    private void processDropUser() throws HsqlException {

        session.checkAdmin();
        session.checkDDLWrite();

        String userName = getPassword();

        if (database.getSessionManager().isUserActive(userName)) {

            // todo - new error message "cannot drop a user that is currently connected."    // NOI18N
            throw Trace.error(Trace.ACCESS_IS_DENIED);
        }

        database.getUserManager().dropUser(userName);
    }

    private void processDropSequence() throws HsqlException {

        boolean ifexists = false;

        session.checkAdmin();
        session.checkDDLWrite();

        String name = tokenizer.getName();
        String schemaname =
            session.getSchemaNameForWrite(tokenizer.getLongNameFirst());

        if (tokenizer.isGetThis(Token.T_IF)) {
            tokenizer.getThis(Token.T_EXISTS);

            ifexists = true;
        }

        boolean cascade = tokenizer.isGetThis(Token.T_CASCADE);

        if (!cascade) {
            tokenizer.isGetThis(Token.T_RESTRICT);
        }

        NumberSequence sequence = database.schemaManager.findSequence(name,
            schemaname);

        if (sequence == null) {
            if (ifexists) {
                return;
            } else {
                throw Trace.error(Trace.SEQUENCE_NOT_FOUND);
            }
        }

        database.schemaManager.checkCascadeDropViews(sequence, cascade);
        database.schemaManager.dropSequence(sequence);
    }

    private void processDropTrigger() throws HsqlException {

        session.checkAdmin();
        session.checkDDLWrite();

        String triggername = tokenizer.getName();
        String schemaname =
            session.getSchemaNameForWrite(tokenizer.getLongNameFirst());

        database.schemaManager.dropTrigger(session, triggername, schemaname);
    }

    private void processDropIndex() throws HsqlException {

        String name = tokenizer.getName();
        String schema =
            session.getSchemaNameForWrite(tokenizer.getLongNameFirst());
        boolean ifexists = false;

        // accept a table name - no check performed if it is the right table
        if (tokenizer.isGetThis(Token.T_ON)) {
            tokenizer.getName();
        }

        if (tokenizer.isGetThis(Token.T_IF)) {
            tokenizer.getThis(Token.T_EXISTS);

            ifexists = true;
        }

        session.checkAdmin();
        session.checkDDLWrite();
        database.schemaManager.dropIndex(session, name, schema, ifexists);
    }

    private void processDropSchema() throws HsqlException {

        String  name    = tokenizer.getSimpleName();
        boolean cascade = tokenizer.isGetThis(Token.T_CASCADE);

        if (!cascade) {
            tokenizer.isGetThis(Token.T_RESTRICT);
        }

        processDropSchema(name, cascade);
    }

    private void processDropSchema(String name,
                                   boolean cascade) throws HsqlException {

        if (!database.schemaManager.schemaExists(name)) {
            throw Trace.error(Trace.INVALID_SCHEMA_NAME_NO_SUBCLASS);
        }

        database.schemaManager.dropSchema(name, cascade);

        if (name.equals(session.getSchemaName(null))) {
            session.setSchema(database.schemaManager.getDefaultSchemaName());
        }
    }

    private Result processExplainPlan() throws IOException, HsqlException {

        // PRE:  we assume only one DML or DQL has been submitted
        //       and simply ignore anything following the first
        //       sucessfully compliled statement
        String            token;
        Parser            parser;
        int               cmd;
        CompiledStatement cs;
        Result            result;
        String            line;
        LineNumberReader  lnr;

        tokenizer.getThis(Token.T_PLAN);
        tokenizer.getThis(Token.T_FOR);

        parser = new Parser(session, database, tokenizer);
        token  = tokenizer.getSimpleToken();
        cmd    = Token.get(token);
        result = Result.newSingleColumnResult("OPERATION", Types.VARCHAR);

        int brackets = 0;

        switch (cmd) {

            case Token.OPENBRACKET :
                brackets = parser.parseOpenBracketsSelect() + 1;
            case Token.SELECT :
                cs = parser.compileSelectStatement(brackets);
                break;

            case Token.INSERT :
                cs = parser.compileInsertStatement();
                break;

            case Token.UPDATE :
                cs = parser.compileUpdateStatement();
                break;

            case Token.DELETE :
                cs = parser.compileDeleteStatement();
                break;

            case Token.CALL :
                cs = parser.compileCallStatement();
                break;

            default :

                // - No real need to throw, so why bother?
                // - Just return result with no rows for now
                // - Later, maybe there will be plan desciptions
                //   for other operations
                return result;
        }

        lnr = new LineNumberReader(new StringReader(cs.describe(session)));

        while (null != (line = lnr.readLine())) {
            result.add(new Object[]{ line });
        }

        return result;
    }

// fredt@users 20010701 - patch 1.6.1 by fredt - open <1.60 db files
// convert org.hsql.Library aliases from versions < 1.60 to org.hsqldb
// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP) - ABS function
    static final String oldLib    = "org.hsql.Library.";
    static final int    oldLibLen = oldLib.length();
    static final String newLib    = "org.hsqldb.Library.";

    private static String upgradeMethodFQN(String fqn) {

        if (fqn.startsWith(oldLib)) {
            fqn = newLib + fqn.substring(oldLibLen);
        } else if (fqn.equals("java.lang.Math.abs")) {
            fqn = "org.hsqldb.Library.abs";
        }

        return fqn;
    }

    /**
     * Processes a SELECT INTO for a new table.
     */
    Result processSelectInto(Result result, HsqlName intoHsqlName,
                             int intoType) throws HsqlException {

        // fredt@users 20020215 - patch 497872 by Nitin Chauhan
        // to require column labels in SELECT INTO TABLE
        int colCount = result.getColumnCount();

        for (int i = 0; i < colCount; i++) {
            if (result.metaData.colLabels[i].length() == 0) {
                throw Trace.error(Trace.LABEL_REQUIRED);
            }
        }

        // fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        Table t = (intoType == Table.TEXT_TABLE)
                  ? new TextTable(database, intoHsqlName, intoType)
                  : new Table(database, intoHsqlName, intoType);

        t.addColumns(result.metaData, result.getColumnCount());
        t.createPrimaryKey();
        database.schemaManager.linkTable(t);

        // fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)
        if (intoType == Table.TEXT_TABLE) {
            try {

                // Use default lowercase name "<table>.csv" (with invalid
                // char's converted to underscores):
                String txtSrc =
                    StringUtil.toLowerSubset(intoHsqlName.name, '_') + ".csv";

                t.setDataSource(session, txtSrc, false, true);
                logTableDDL(t);
                t.insertIntoTable(session, result);
            } catch (HsqlException e) {
                database.schemaManager.dropTable(session, intoHsqlName.name,
                                                 null, false, false, false);

                throw (e);
            }
        } else {
            logTableDDL(t);

            // SELECT .. INTO can't fail because of constraint violation
            t.insertIntoTable(session, result);
        }

        Result uc = new Result(ResultConstants.UPDATECOUNT);

        uc.updateCount = result.getSize();

        return uc;
    }

    /**
     *  Logs the DDL for a table created with INTO.
     *  Uses two dummy arguments for getTableDDL() as the new table has no
     *  FK constraints.
     *
     *
     * @param t table
     * @throws  HsqlException
     */
    private void logTableDDL(Table t) throws HsqlException {

        StringBuffer tableDDL;
        String       sourceDDL;

        tableDDL = new StringBuffer();

        DatabaseScript.getTableDDL(database, t, 0, null, true, tableDDL);

        sourceDDL = DatabaseScript.getDataSource(t);

        database.logger.writeToLog(session, tableDDL.toString());

        if (sourceDDL != null) {
            database.logger.writeToLog(session, sourceDDL);
        }
    }

    private void processAlterTableAddUniqueConstraint(Table t,
            HsqlName n) throws HsqlException {

        int[] col;

        col = processColumnList(t, false);

        if (n == null) {
            n = database.nameManager.newAutoName("CT");
        }

        session.commit();

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.createUniqueConstraint(col, n);
    }

    private void processAlterTableAddForeignKeyConstraint(Table t,
            HsqlName n) throws HsqlException {

        Constraint tc;

        if (n == null) {
            n = database.nameManager.newAutoName("FK");
        }

        tc = processCreateFK(t, n);

        checkFKColumnDefaults(t, tc);
        t.checkColumnsMatch(tc.core.mainColArray, tc.core.refTable,
                            tc.core.refColArray);
        session.commit();

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.createForeignKey(tc.core.mainColArray,
                                    tc.core.refColArray, tc.constName,
                                    tc.core.refTable, tc.core.deleteAction,
                                    tc.core.updateAction);
    }

    private void processAlterTableAddCheckConstraint(Table table,
            HsqlName name) throws HsqlException {

        Constraint check;

        if (name == null) {
            name = database.nameManager.newAutoName("CT");
        }

        check = new Constraint(name, null, null, null, Constraint.CHECK,
                               Constraint.NO_ACTION, Constraint.NO_ACTION);

        processCreateCheckConstraintCondition(check);
        session.commit();

        TableWorks tableWorks = new TableWorks(session, table);

        tableWorks.createCheckConstraint(check, name);
    }

    private void processAlterTableAddPrimaryKey(Table t,
            HsqlName n) throws HsqlException {

        int[] col;

        col = processColumnList(t, false);

        session.commit();

        TableWorks tableWorks = new TableWorks(session, t);

        tableWorks.addPrimaryKey(col, n);
    }

    private void processReleaseSavepoint() throws HsqlException {

        String token;

        tokenizer.getThis(Token.T_SAVEPOINT);

        token = tokenizer.getSimpleName();

        session.releaseSavepoint(token);
    }

    private void processAlterUser() throws HsqlException {

        String userName;
        String password;
        User   userObject;

        userName = getUserIdentifier();
        userObject =
            (User) database.getUserManager().getUsers().get(userName);

        Trace.check(userObject != null, Trace.USER_NOT_FOUND, userName);
        tokenizer.getThis(Token.T_SET);
        tokenizer.getThis(Token.T_PASSWORD);

        password = getPassword();

        userObject.setPassword(password);
        database.logger.writeToLog(session, userObject.getAlterUserDDL());
        session.setScripting(false);
    }

    private String getUserIdentifier() throws HsqlException {

        String    token = tokenizer.getString();
        Tokenizer t     = new Tokenizer(token);

        return t.getSimpleName();
    }

    private String getPassword() throws HsqlException {

        String token = tokenizer.getString();

        return token.toUpperCase(Locale.ENGLISH);
    }

    /**
     *  Responsible for handling the execution of GRANT/REVOKE role...
     *  statements.
     *
     * @throws HsqlException
     */
    private void processRoleGrantOrRevoke(boolean grant)
    throws HsqlException {

        String         token;
        HsqlArrayList  list = new HsqlArrayList();
        String         role;
        GranteeManager granteeManager = database.getGranteeManager();

        do {
            role = tokenizer.getSimpleToken();

            Trace.check(granteeManager.isRole(role),
                        (grant ? Trace.NO_SUCH_ROLE_GRANT
                               : Trace.NO_SUCH_ROLE_REVOKE));
            list.add(role);
        } while (tokenizer.isGetThis(Token.T_COMMA));

        tokenizer.getThis(grant ? Token.T_TO
                                : Token.T_FROM);

        token = getUserIdentifier();

        GranteeManager gm = database.getGranteeManager();

        for (int i = 0; i < list.size(); i++) {
            if (grant) {
                gm.grant(token, (String) list.get(i));
            } else {
                gm.revoke(token, (String) list.get(i));
            }
        }
    }
}
