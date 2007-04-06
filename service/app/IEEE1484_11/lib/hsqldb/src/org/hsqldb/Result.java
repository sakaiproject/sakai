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

import java.io.DataInput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import org.hsqldb.lib.Iterator;
import org.hsqldb.rowio.RowInputBinary;
import org.hsqldb.rowio.RowOutputBinary;

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all operations
// methods for set operations moved here from Select.java
// tony_lai@users 20020820 - patch 595073 - duplicated exception msg
// fredt@users 20030801 - patch 1.7.2 - separate metadata and polymophic serialisation
// boucherb@users 200307/8 - various, in support of fred's work over the same time period

/**
 *  The primary unit of comunication between Connection, Server and Session
 *  objects.
 *
 *  An HSQLDB Result object encapsulates all requests (such as to alter or
 *  query session settings, to allocate and execute statements, etc.) and all
 *  responses (such as exception indications, update counts, result sets and
 *  result set metadata). It also implements the HSQL wire protocol for
 *  comunicating all such requests and responses across the network.
 *
 * Extensively rewritten and extended in successive versions of HSQLDB.
 *
 * @author Thomas Mueller (Hypersonic SQL Group)
 * @version 1.8.0
 * @since Hypersonic SQL
 */
public class Result {

    // record list
    public Record  rRoot;
    private Record rTail;
    private int    size;

    // transient - number of significant columns
    private int significantColumns;

    // type of result
    public int mode;

//    boolean isMulti;
    // database ID
    int databaseID;

    // session ID
    int sessionID;

    // user / password or error strings
    String mainString;
    String subString;

    // database name
    String subSubString;

    // the exception if this is an error
    private Throwable exception;

    // prepared statement id / error vendor code
    int statementID;

    // max rows (out) or update count (in)
    int                   updateCount;
    public ResultMetaData metaData;

    /** A Result object's metadata */
    public static class ResultMetaData {

        // always resolved
        public String[]  colLabels;
        public String[]  tableNames;
        public String[]  colNames;
        public boolean[] isLabelQuoted;
        public int[]     colTypes;
        public int[]     colSizes;
        public int[]     colScales;

        // extra attrs, sometimes resolved
        public String[]  catalogNames;
        public String[]  schemaNames;
        public int[]     colNullable;
        public boolean[] isIdentity;
        public boolean[] isWritable;
        public int[]     paramMode;

        // It's possible to do better than java.lang.Object
        // for type OTHER if the expression generating the value
        // is of type FUNCTION.  This applies to result set columns
        // whose value is the result of a SQL function call and
        // especially to the arguments and return value of a CALL
        public String[] classNames;
        boolean         isParameterDescription;

        ResultMetaData() {}

        ResultMetaData(int n) {
            prepareData(n);
        }

        /**
         *  Method declaration
         *
         * @param  columns
         */
        private void prepareData(int columns) {

            colLabels     = new String[columns];
            tableNames    = new String[columns];
            colNames      = new String[columns];
            isLabelQuoted = new boolean[columns];
            colTypes      = new int[columns];
            colSizes      = new int[columns];
            colScales     = new int[columns];
            catalogNames  = new String[columns];
            schemaNames   = new String[columns];
            colNullable   = new int[columns];
            isIdentity    = new boolean[columns];
            isWritable    = new boolean[columns];
            classNames    = new String[columns];
        }

        public int[] getParameterTypes() {
            return colTypes;
        }

        boolean isTableColumn(int i) {
            return tableNames[i] != null && tableNames[i].length() > 0
                   && colNames[i] != null && colNames[i].length() > 0;
        }

        private void decodeTableColumnAttrs(int in, int i) {

            colNullable[i] = in & 0x0000000f;
            isIdentity[i]  = (in & 0x00000010) != 0;
            isWritable[i]  = (in & 0x00000020) != 0;
        }

        private void writeTableColumnAttrs(RowOutputBinary out,
                                           int i)
                                           throws IOException, HsqlException {

            // HSQLDB also ignores precision and scale for all types except
            // XXXCHAR, for which it may (or may not) perform some trimming/padding.
            // All in all, it's currently meaningless (indeed misleading) to
            // transmit and report the values, as the data typically will
            // not be constrained accordingly.
//        switch(colType[i]) {
//            // As early as SQL 92, these are allowed to have a scale.
//            // However, DatabaseCommandInterpreter.processCreateColumn
//            // does not currently handle this correctly and will assign
//            // a precision instead of a scale if TIME(s) or TIMESTAMP(s)
//            // is specified
//            case Types.TIME :
//            case Types.TIMESTAMP :
//                  out.writeIntData(colScale[i]);
//                  break;
//            case Types.DECIMAL :
//            case Types.NUMERIC : {
//                out.writeIntData(colScale[i]);
//            } // fall through
//            // Apparently, SQL 92 specifies that FLOAT can have
//            // a declared precision, which is typically the number of
//            // bits (not binary digits).  In any case, this is somewhat
//            // meaningless under HSQLDB/Java, in that we use java.lang.Double
//            // to represent SQL FLOAT
//            case Types.FLOAT :
//            // It's legal to declare precision for these, although HSQLDB
//            // currently does not use it to constrain values
//            case Types.BINARY :
//            case Types.VARBINARY :
//            case Types.LONGVARBINARY :
//            // possibly, but not universally acted upon (trimmming/padding)
//            case Types.CHAR  :
//            case Types.VARCHAR :
//            case Types.LONGVARCHAR : {
//                out.writeIntData(colSize[i]);
//            }
//        }
            out.writeIntData(encodeTableColumnAttrs(i));
            out.writeString(catalogNames[i] == null ? ""
                                                    : catalogNames[i]);
            out.writeString(schemaNames[i] == null ? ""
                                                   : schemaNames[i]);
        }

        private int encodeTableColumnAttrs(int i) {

            int out = colNullable[i];    // always between 0x00 and 0x02

            if (isIdentity[i]) {
                out |= 0x00000010;
            }

            if (isWritable[i]) {
                out |= 0x00000020;
            }

            return out;
        }

        private void readTableColumnAttrs(RowInputBinary in,
                                          int i)
                                          throws IOException, HsqlException {

            decodeTableColumnAttrs(in.readIntData(), i);

            catalogNames[i] = in.readString();
            schemaNames[i]  = in.readString();
        }

        ResultMetaData(RowInputBinary in,
                       int mode) throws HsqlException, IOException {

            int l = in.readIntData();

            prepareData(l);

            if (mode == ResultConstants.PARAM_META_DATA) {
                isParameterDescription = true;
                paramMode              = new int[l];
            }

            for (int i = 0; i < l; i++) {
                colTypes[i] = in.readType();

                // fredt - 1.8.0 added
                colSizes[i]   = in.readIntData();
                colScales[i]  = in.readIntData();
                colLabels[i]  = in.readString();
                tableNames[i] = in.readString();
                colNames[i]   = in.readString();
                classNames[i] = in.readString();

                if (isTableColumn(i)) {
                    readTableColumnAttrs(in, i);
                }

                if (mode == ResultConstants.PARAM_META_DATA) {
                    paramMode[i] = in.readIntData();
                }
            }
        }

        void write(RowOutputBinary out,
                   int colCount) throws HsqlException, IOException {

            out.writeIntData(colCount);

            for (int i = 0; i < colCount; i++) {
                out.writeType(colTypes[i]);

                // fredt - 1.8.0 added
                out.writeIntData(colSizes[i]);
                out.writeIntData(colScales[i]);
                out.writeString(colLabels[i] == null ? ""
                                                     : colLabels[i]);
                out.writeString(tableNames[i] == null ? ""
                                                      : tableNames[i]);
                out.writeString(colNames[i] == null ? ""
                                                    : colNames[i]);
                out.writeString(classNames[i] == null ? ""
                                                      : classNames[i]);

                if (isTableColumn(i)) {
                    writeTableColumnAttrs(out, i);
                }

                if (isParameterDescription) {
                    out.writeIntData(paramMode[i]);
                }
            }
        }
    }

    /**
     *  General constructor
     */
    public Result(int type) {

        mode = type;

/*
        if (type == ResultConstants.MULTI) {
            isMulti = true;
        }
*/
        if (type == ResultConstants.DATA
                || type == ResultConstants.PARAM_META_DATA
                || type == ResultConstants.SQLEXECUTE
                || type == ResultConstants.SETSESSIONATTR) {
            metaData = new ResultMetaData();
        }
    }

    Result(ResultMetaData md) {

        mode               = ResultConstants.DATA;
        significantColumns = md.colTypes.length;
        metaData           = md;
    }

// fredt@users 20020221 - patch 513005 by sqlbob@users (RMP)

    /**
     *  Constructor for errors
     *
     * @param  error error message
     * @param  state   sql state
     * @param  code   vendor code
     */
    Result(String error, String state, int code) {

        mode         = ResultConstants.ERROR;
        mainString   = error;
        subString    = state;
        statementID  = code;
        subSubString = "";
    }

    /**
     *  Only used with DATA and PARAM_META_DATA results
     *
     * @param  columns
     */
    Result(int type, int columns) {

        metaData = new ResultMetaData();

        metaData.prepareData(columns);

        if (type == ResultConstants.PARAM_META_DATA) {
            metaData.isParameterDescription = true;
            metaData.paramMode              = new int[columns];
        }

        mode               = type;
        significantColumns = columns;
    }

    /**
     * For BATCHEXECUTE and BATCHEXECDIRECT
     */
    public Result(int type, int[] types, int id) {

        mode               = type;
        metaData           = new ResultMetaData();
        metaData.colTypes  = types;
        significantColumns = types.length;
        statementID        = id;
    }

    /**
     *  Constructor declaration
     *
     * @param  in
     * @exception  HsqlException  Description of the Exception
     */
    Result(RowInputBinary in) throws HsqlException {

        try {
            mode = in.readIntData();

            if (mode == ResultConstants.MULTI) {
                readMultiResult(in);

                return;
            }

            databaseID = in.readIntData();
            sessionID  = in.readIntData();

            switch (mode) {

                case ResultConstants.GETSESSIONATTR :
                case ResultConstants.SQLDISCONNECT :
                case ResultConstants.SQLSTARTTRAN :
                case ResultConstants.HSQLRESETSESSION :
                    break;

                case ResultConstants.SQLPREPARE :
                    setStatementType(in.readIntData());

                    mainString = in.readString();
                    break;

                case ResultConstants.PREPARE_ACK :
                case ResultConstants.SQLFREESTMT :
                    statementID = in.readIntData();
                    break;

                case ResultConstants.SQLEXECDIRECT :
                    updateCount = in.readIntData();
                    statementID = in.readIntData();
                    mainString  = in.readString();
                    break;

                case ResultConstants.ERROR :
                case ResultConstants.SQLCONNECT :
                    mainString   = in.readString();
                    subString    = in.readString();
                    subSubString = in.readString();
                    statementID  = in.readIntData();

//                    throw Trace.getError(string, code);
                    break;

                case ResultConstants.UPDATECOUNT :
                    updateCount = in.readIntData();
                    break;

                case ResultConstants.SQLENDTRAN : {
                    int type = in.readIntData();

                    setEndTranType(type);                    // endtran type

                    switch (type) {

                        case ResultConstants.SAVEPOINT_NAME_RELEASE :
                        case ResultConstants.SAVEPOINT_NAME_ROLLBACK :
                            mainString = in.readString();    // savepoint name
                    }

                    break;
                }
                case ResultConstants.BATCHEXECUTE :
                case ResultConstants.BATCHEXECDIRECT :
                case ResultConstants.SQLEXECUTE :
                case ResultConstants.SETSESSIONATTR : {
                    updateCount = in.readIntData();
                    statementID = in.readIntData();

                    int l = in.readIntData();

                    metaData           = new ResultMetaData(l);
                    significantColumns = l;

                    for (int i = 0; i < l; i++) {
                        metaData.colTypes[i] = in.readType();
                    }

                    int count = in.readIntData();

                    while (count-- > 0) {
                        add(in.readData(metaData.colTypes));
                    }

                    break;
                }
                case ResultConstants.DATA :
                case ResultConstants.PARAM_META_DATA : {
                    metaData           = new ResultMetaData(in, mode);
                    significantColumns = metaData.colLabels.length;

                    int count = in.readIntData();

                    while (count-- > 0) {
                        add(in.readData(metaData.colTypes));
                    }

                    break;
                }
                case ResultConstants.SQLSETCONNECTATTR : {
                    int type = in.readIntData();             // attr type

                    setConnectionAttrType(type);

                    switch (type) {

                        case ResultConstants.SQL_ATTR_SAVEPOINT_NAME :
                            mainString = in.readString();    // savepoint name

                        //  case ResultConstants.SQL_ATTR_AUTO_IPD :
                        //      - always true
                        //  default: throw - case never happens
                    }

                    break;
                }
                default :
                    throw new HsqlException(
                        Trace.getMessage(
                            Trace.Result_Result, true, new Object[]{
                                new Integer(mode) }), null, 0);
            }
        } catch (IOException e) {
            throw Trace.error(Trace.TRANSFER_CORRUPTED);
        }
    }

    static Result newSingleColumnResult(String colName, int colType) {

        Result result = new Result(ResultConstants.DATA, 1);

        result.metaData.colNames[0]   = colName;
        result.metaData.colLabels[0]  = colName;
        result.metaData.tableNames[0] = "";
        result.metaData.colTypes[0]   = colType;

        return result;
    }

    static Result newPrepareResponse(int csid, Result rsmd, Result pmd) {

        Result out;
        Result pack;

        out = new Result(ResultConstants.MULTI);

//        out.isMulti      = true;
        pack             = new Result(ResultConstants.PREPARE_ACK);
        pack.statementID = csid;

        out.add(new Object[]{ pack });
        out.add(new Object[]{ rsmd });
        out.add(new Object[]{ pmd });

        return out;
    }

    static Result newParameterDescriptionResult(int len) {

        Result r = new Result(ResultConstants.PARAM_META_DATA, len);

        r.metaData.isParameterDescription = true;
        r.metaData.paramMode              = new int[len];

        return r;
    }

    public static Result newFreeStmtRequest(int statementID) {

        Result r = new Result(ResultConstants.SQLFREESTMT);

        r.statementID = statementID;

        return r;
    }

    static Result newExecuteDirectRequest(String sql) {

        Result out;

        out = new Result(ResultConstants.SQLEXECDIRECT);

        out.setMainString(sql);

        return out;
    }

    public static Result newReleaseSavepointRequest(String name) {

        Result out;

        out = new Result(ResultConstants.SQLENDTRAN);

        out.setMainString(name);
        out.setEndTranType(ResultConstants.SAVEPOINT_NAME_RELEASE);

        return out;
    }

    public static Result newRollbackToSavepointRequest(String name) {

        Result out;

        out = new Result(ResultConstants.SQLENDTRAN);

        out.setMainString(name);
        out.setEndTranType(ResultConstants.SAVEPOINT_NAME_ROLLBACK);

        return out;
    }

    public static Result newSetSavepointRequest(String name) {

        Result out;

        out = new Result(ResultConstants.SQLSETCONNECTATTR);

        out.setConnectionAttrType(ResultConstants.SQL_ATTR_SAVEPOINT_NAME);
        out.setMainString(name);

        return out;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public int getSize() {
        return size;
    }

    /**
     *  Method declaration
     *
     * @param  columns
     */
    void setColumnCount(int columns) {
        significantColumns = columns;
    }

    /**
     *  Method declaration
     *
     * @return
     */
    public int getColumnCount() {
        return significantColumns;
    }

    /**
     *  Append Result argument to this.
     *
     * @param  a
     */
    void append(Result a) {

        if (a.rRoot == null) {
            return;
        }

        if (rRoot == null) {
            rRoot = a.rRoot;
        } else {
            rTail.next = a.rRoot;
        }

        rTail = a.rTail;
        size  += a.size;
    }

    void addAll(Result r) {

        if (r == null) {
            return;
        }

        Record from = r.rRoot;

        while (from != null) {
            add(from.data);

            from = from.next;
        }
    }

    public void clear() {

        rRoot = null;
        rTail = null;
        size  = 0;
    }

    public boolean isEmpty() {
        return rRoot == null;
    }

    /**
     *  Method declaration
     *
     * @param  a
     */
    void setRows(Result a) {

        if (a == null) {
            rRoot = null;
            rTail = null;
            size  = 0;
        } else {
            rRoot = a.rRoot;
            rTail = a.rTail;
            size  = a.size;
        }
    }

    /**
     *  Method declaration
     *
     * @param  d
     */
    public void add(Object[] d) {

        Record r = new Record();

        r.data = d;

        if (rRoot == null) {
            rRoot = r;
        } else {
            rTail.next = r;
        }

        rTail = r;

        size++;
    }

    /**
     *  Method declaration
     *
     * @param  limitstart  number of records to discard at the head
     * @param  limitcount  number of records to keep, all the rest if 0
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// rewritten and moved from Select.java
    void trimResult(int limitstart, int limitcount) {

        Record n = rRoot;

        if (n == null) {
            return;
        }

        if (limitstart >= size) {
            size  = 0;
            rRoot = rTail = null;

            return;
        }

        size -= limitstart;

        for (int i = 0; i < limitstart; i++) {
            n = n.next;

            if (n == null) {

                // if iSize is consistent this block will never be reached
                size  = 0;
                rRoot = rTail = n;

                return;
            }
        }

        rRoot = n;

        if (limitcount == 0 || limitcount >= size) {
            return;
        }

        for (int i = 1; i < limitcount; i++) {
            n = n.next;

            if (n == null) {

                // if iSize is consistent this block will never be reached
                return;
            }
        }

        size   = limitcount;
        n.next = null;
        rTail  = n;
    }

    /**
     * Removes duplicate rows on the basis of comparing the singificant
     * columns of the rows in the result.
     *
     * @throws  HsqlException
     */
    void removeDuplicates(Session session) throws HsqlException {
        removeDuplicates(session, significantColumns);
    }

    /**
     * Removes duplicate rows on the basis of comparing the first columnCount
     * columns of rows in the result.
     *
     * @throws  HsqlException
     */

// fredt@users 20020130 - patch 1.7.0 by fredt
// to ensure consistency of r.rTail r.iSize in all set operations
    void removeDuplicates(Session session,
                          int columnCount) throws HsqlException {

        if (rRoot == null) {
            return;
        }

        int[] order = new int[columnCount];
        int[] way   = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            order[i] = i;
            way[i]   = 1;
        }

        sortResult(session, order, way);

        Record n = rRoot;

        for (;;) {
            Record next = n.next;

            if (next == null) {
                break;
            }

            if (compareRecord(session, n.data, next.data, columnCount) == 0) {
                n.next = next.next;

                size--;
            } else {
                n = next;
            }
        }

        rTail = n;
    }

    /**
     *  Removes duplicates then removes the contents of the second result
     *  from this one base on first columnCount of the rows in each result.
     *
     * @param  minus
     * @throws  HsqlException
     */
    void removeSecond(Session session, Result minus,
                      int columnCount) throws HsqlException {

        removeDuplicates(session, columnCount);
        minus.removeDuplicates(session, columnCount);

        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = minus.rRoot;
        int     i     = 0;

        while (n != null && n2 != null) {
            i = compareRecord(session, n.data, n2.data, columnCount);

            if (i == 0) {
                if (rootr) {
                    rRoot = last = n.next;
                } else {
                    last.next = n.next;
                }

                n = n.next;

                size--;
            } else if (i > 0) {    // r > minus
                n2 = n2.next;
            } else {               // r < minus
                last  = n;
                rootr = false;
                n     = n.next;
            }
        }

        for (; n != null; ) {
            last = n;
            n    = n.next;
        }

        rTail = last;
    }

    /**
     * Removes all duplicate rows then removes all rows that are not shared
     * between this and the other result, based on comparing the first
     * columnCount columns of each result.
     *
     * @param  r2
     * @throws  HsqlException
     */
    void removeDifferent(Session session, Result r2,
                         int columnCount) throws HsqlException {

        removeDuplicates(session, columnCount);
        r2.removeDuplicates(session, columnCount);

        Record  n     = rRoot;
        Record  last  = rRoot;
        boolean rootr = true;    // checking rootrecord
        Record  n2    = r2.rRoot;
        int     i     = 0;

        size = 0;

        while (n != null && n2 != null) {
            i = compareRecord(session, n.data, n2.data, columnCount);

            if (i == 0) {             // same rows
                if (rootr) {
                    rRoot = n;        // make this the first record
                } else {
                    last.next = n;    // this is next record in resultset
                }

                rootr = false;
                last  = n;            // this is last record in resultset
                n     = n.next;
                n2    = n2.next;

                size++;
            } else if (i > 0) {       // r > r2
                n2 = n2.next;
            } else {                  // r < r2
                n = n.next;
            }
        }

        if (rootr) {             // if no lines in resultset
            rRoot = null;        // then return null
            last  = null;
        } else {
            last.next = null;    // else end resultset
        }

        rTail = last;
    }

    /**
     *  Method declaration
     *
     * @param  order
     * @param  way
     * @throws  HsqlException
     */
    void sortResult(Session session, final int[] order,
                    final int[] way) throws HsqlException {

        if (rRoot == null || rRoot.next == null) {
            return;
        }

        Record   source0, source1;
        Record[] target     = new Record[2];
        Record[] targetlast = new Record[2];
        int      dest       = 0;
        Record   n          = rRoot;

        while (n != null) {
            Record next = n.next;

            n.next       = target[dest];
            target[dest] = n;
            n            = next;
            dest         ^= 1;
        }

        for (int blocksize = 1; target[1] != null; blocksize <<= 1) {
            source0   = target[0];
            source1   = target[1];
            target[0] = target[1] = targetlast[0] = targetlast[1] = null;

            for (dest = 0; source0 != null; dest ^= 1) {
                int n0 = blocksize,
                    n1 = blocksize;

                while (true) {
                    if (n0 == 0 || source0 == null) {
                        if (n1 == 0 || source1 == null) {
                            break;
                        }

                        n       = source1;
                        source1 = source1.next;

                        n1--;
                    } else if (n1 == 0 || source1 == null) {
                        n       = source0;
                        source0 = source0.next;

                        n0--;
                    } else if (compareRecord(session, source0.data, source1
                            .data, order, way) > 0) {
                        n       = source1;
                        source1 = source1.next;

                        n1--;
                    } else {
                        n       = source0;
                        source0 = source0.next;

                        n0--;
                    }

                    if (target[dest] == null) {
                        target[dest] = n;
                    } else {
                        targetlast[dest].next = n;
                    }

                    targetlast[dest] = n;
                    n.next           = null;
                }
            }
        }

        rRoot = target[0];
        rTail = targetlast[0];
    }

    /**
     *  Method declaration
     *
     * @param  a
     * @param  b
     * @param  order
     * @param  way
     * @return -1, 0, +1
     * @throws  HsqlException
     */
    private int compareRecord(Session session, Object[] a, final Object[] b,
                              final int[] order,
                              int[] way) throws HsqlException {

        int i = Column.compare(session.database.collation, a[order[0]],
                               b[order[0]], metaData.colTypes[order[0]]);

        if (i == 0) {
            for (int j = 1; j < order.length; j++) {
                i = Column.compare(session.database.collation, a[order[j]],
                                   b[order[j]], metaData.colTypes[order[j]]);

                if (i != 0) {
                    return i * way[j];
                }
            }
        }

        return i * way[0];
    }

    /**
     *  Method declaration
     *
     * @param  a
     * @param  b
     * @param  len
     * @return -1, 0, +1
     * @throws  HsqlException
     */
    private int compareRecord(Session session, Object[] a, Object[] b,
                              int len) throws HsqlException {

        for (int j = 0; j < len; j++) {
            int i = Column.compare(session.database.collation, a[j], b[j],
                                   metaData.colTypes[j]);

            if (i != 0) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Result structure used for set/get session attributes
     */
    static Result newSessionAttributesResult() {

        Result r = new Result(ResultConstants.DATA, 7);

        r.metaData.colNames = r.metaData.colLabels = r.metaData.tableNames =
            new String[] {
            "", "", "", "", "", "", ""
        };
        r.metaData.colTypes = new int[] {
            Types.VARCHAR, Types.VARCHAR, Types.INTEGER, Types.INTEGER,
            Types.BOOLEAN, Types.BOOLEAN, Types.BOOLEAN
        };

        return r;
    }

    void write(RowOutputBinary out) throws IOException, HsqlException {

        if (mode == ResultConstants.MULTI) {
            writeMulti(out);

            return;
        }

        int startPos = out.size();

        out.writeSize(0);
        out.writeIntData(mode);
        out.writeIntData(databaseID);
        out.writeIntData(sessionID);

        switch (mode) {

            case ResultConstants.GETSESSIONATTR :
            case ResultConstants.SQLDISCONNECT :
            case ResultConstants.SQLSTARTTRAN :
            case ResultConstants.HSQLRESETSESSION :
                break;

            case ResultConstants.SQLPREPARE :

                // Allows the engine side to fast-fail prepare of non-CALL
                // statement against a CallableStatement object and CALL
                // statement against PreparedStatement.
                //
                // May be useful in the future for other things
                out.writeIntData(getStatementType());
                out.writeString(mainString);
                break;

            case ResultConstants.PREPARE_ACK :
            case ResultConstants.SQLFREESTMT :
                out.writeIntData(statementID);
                break;

            case ResultConstants.SQLEXECDIRECT :
                out.writeIntData(updateCount);
                out.writeIntData(statementID);          // currently unused
                out.writeString(mainString);
                break;

            case ResultConstants.ERROR :
            case ResultConstants.SQLCONNECT :
                out.writeString(mainString);
                out.writeString(subString);
                out.writeString(subSubString);
                out.writeIntData(statementID);
                break;

            case ResultConstants.UPDATECOUNT :
                out.writeIntData(updateCount);
                break;

            case ResultConstants.SQLENDTRAN : {
                int type = getEndTranType();

                out.writeIntData(type);                 // endtran type

                switch (type) {

                    case ResultConstants.SAVEPOINT_NAME_RELEASE :
                    case ResultConstants.SAVEPOINT_NAME_ROLLBACK :
                        out.writeString(mainString);    // savepoint name

                    // default; // do nothing
                }

                break;
            }
            case ResultConstants.BATCHEXECUTE :
            case ResultConstants.BATCHEXECDIRECT :
            case ResultConstants.SQLEXECUTE :
            case ResultConstants.SETSESSIONATTR : {
                out.writeIntData(updateCount);
                out.writeIntData(statementID);

                int l = significantColumns;

                out.writeIntData(l);

                for (int i = 0; i < l; i++) {
                    out.writeType(metaData.colTypes[i]);
                }

                out.writeIntData(size);

                Record n = rRoot;

                while (n != null) {
                    out.writeData(l, metaData.colTypes, n.data, null, null);

                    n = n.next;
                }

                break;
            }
            case ResultConstants.DATA :
            case ResultConstants.PARAM_META_DATA : {
                metaData.write(out, significantColumns);
                out.writeIntData(size);

                Record n = rRoot;

                while (n != null) {
                    out.writeData(significantColumns, metaData.colTypes,
                                  n.data, null, null);

                    n = n.next;
                }

                break;
            }
            case ResultConstants.SQLSETCONNECTATTR : {
                int type = getConnectionAttrType();

                out.writeIntData(type);                 // attr type

                switch (type) {

                    case ResultConstants.SQL_ATTR_SAVEPOINT_NAME :
                        out.writeString(mainString);    // savepoint name

                    // case ResultConstants.SQL_ATTR_AUTO_IPD // always true
                    // default: // throw, but case never happens
                }

                break;
            }
            default :
                throw new HsqlException(
                    Trace.getMessage(
                        Trace.Result_Result, true, new Object[]{
                            new Integer(mode) }), null, 0);
        }

        out.writeIntData(out.size(), startPos);
    }

    void readMultiResult(RowInputBinary in)
    throws HsqlException, IOException {

        mode       = ResultConstants.MULTI;
        databaseID = in.readIntData();
        sessionID  = in.readIntData();

        int count = in.readIntData();

        for (int i = 0; i < count; i++) {

            // Currently required for the outer result, but can simply
            // be ignored for sub-results
            in.readIntData();
            add(new Object[]{ new Result(in) });
        }
    }

    private void writeMulti(RowOutputBinary out)
    throws IOException, HsqlException {

        int startPos = out.size();

        out.writeSize(0);
        out.writeIntData(mode);
        out.writeIntData(databaseID);
        out.writeIntData(sessionID);
        out.writeIntData(size);

        Record n = rRoot;

        while (n != null) {
            ((Result) n.data[0]).write(out);

            n = n.next;
        }

        out.writeIntData(out.size(), startPos);
    }

    /**
     * Convenience method for writing, shared by Server side.
     */
    public static void write(Result r, RowOutputBinary rowout,
                             OutputStream dataout)
                             throws IOException, HsqlException {

        rowout.reset();
        r.write(rowout);
        dataout.write(rowout.getOutputStream().getBuffer(), 0,
                      rowout.getOutputStream().size());
        dataout.flush();
    }

    /**
     * Convenience method for reading, shared by Server side.
     */
    public static Result read(RowInputBinary rowin,
                              DataInput datain)
                              throws IOException, HsqlException {

        int length = datain.readInt();

        rowin.resetRow(0, length);

        byte[] byteArray = rowin.getBuffer();
        int    offset    = 4;

        datain.readFully(byteArray, offset, length - offset);

        return new Result(rowin);
    }

/** @todo fredt - move the messages to Trace.java */
    public Result(Throwable t, String statement) {

        mode      = ResultConstants.ERROR;
        exception = t;

        if (t instanceof HsqlException) {
            HsqlException he = (HsqlException) t;

            subString  = he.getSQLState();
            mainString = he.getMessage();

            if (statement != null) {
                mainString += " in statement [" + statement + "]";
            }

            statementID = he.getErrorCode();
        } else if (t instanceof OutOfMemoryError) {

            // At this point, we've nothing to lose by doing this
            System.gc();

            subString   = "S1000";
            mainString  = "out of memory";
            statementID = Trace.OUT_OF_MEMORY;
        } else {
            subString  = "S1000";
            mainString = Trace.getMessage(Trace.GENERAL_ERROR) + " " + t;

            if (statement != null) {
                mainString += " in statement [" + statement + "]";
            }

            statementID = Trace.GENERAL_ERROR;
        }

        subSubString = "";
    }

    public Throwable getException() {
        return exception;
    }

    public int getStatementID() {
        return statementID;
    }

    void setStatementID(int id) {
        statementID = id;
    }

    public String getMainString() {
        return mainString;
    }

    public void setMainString(String sql) {
        mainString = sql;
    }

    public String getSubString() {
        return subString;
    }

    public void setMaxRows(int count) {
        updateCount = count;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    int getConnectionAttrType() {
        return updateCount;
    }

    void setConnectionAttrType(int type) {
        updateCount = type;
    }

    int getEndTranType() {
        return updateCount;
    }

    void setEndTranType(int type) {
        updateCount = type;
    }

    /** @todo fred - check this repurposing */
    public int[] getUpdateCounts() {
        return metaData.colTypes;
    }

    Object[] getParameterData() {
        return (rRoot == null) ? null
                               : rRoot.data;
    }

    public void setParameterData(Object[] data) {

        if (rRoot == null) {
            rRoot = new Record();
        }

        rRoot.data = data;
        rRoot.next = null;
        rTail      = rRoot;
        size       = 1;
    }

    public void setResultType(int type) {
        mode = type;
    }

    public void setStatementType(int type) {
        updateCount = type;
    }

    public int getStatementType() {
        return updateCount;
    }

    public int getType() {
        return mode;
    }

    public boolean isData() {
        return mode == ResultConstants.DATA;
    }

    public boolean isError() {
        return mode == ResultConstants.ERROR;
    }

    public boolean isUpdateCount() {
        return mode == ResultConstants.UPDATECOUNT;
    }

    public Iterator iterator() {
        return new ResultIterator();
    }

    private class ResultIterator implements Iterator {

        boolean removed;
        int     counter;
        Record  current = rRoot;
        Record  last;

        public boolean hasNext() {
            return counter < size;
        }

        public Object next() {

            if (hasNext()) {
                removed = false;

                if (counter != 0) {
                    last    = current;
                    current = current.next;
                }

                counter++;

                return current.data;
            }

            throw new NoSuchElementException();
        }

        public int nextInt() {
            throw new NoSuchElementException();
        }

        public long nextLong() {
            throw new NoSuchElementException();
        }

        public void remove() {

            if (counter <= size && counter != 0 &&!removed) {
                removed = true;

                if (current == rTail) {
                    rTail = last;
                }

                if (current == rRoot) {
                    current = rRoot = rRoot.next;
                } else {
                    current      = last;
                    last         = null;
                    current.next = current.next.next;
                }

                size--;
                counter--;

                return;
            }

            throw new NoSuchElementException();
        }
    }
}
