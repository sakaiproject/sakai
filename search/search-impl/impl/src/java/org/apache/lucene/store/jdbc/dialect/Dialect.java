/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.lucene.store.jdbc.dialect;

/**
 * A database specific abstraction. All other dialects must extend this Dialect.
 *
 * @author kimchy
 */
public abstract class Dialect {

    /**
     * Does the database (or the jdbc driver) supports transactional blob. Is so,
     * can be used with the {@link org.apache.lucene.store.jdbc.index.FetchPerTransactionJdbcIndexInput}.
     * Defaults to <code>false</code>.
     */
    public boolean supportTransactionalScopedBlobs() {
        return false;
    }

    /**
     * Does the database support "if exists" before the table name when constructing
     * a sql drop for the table. Defaults to <code>false<code>.
     */
    public boolean supportsIfExistsBeforeTableName() {
        return false;
    }

    /**
     * Does the database support "if exists" after the table name when constructing
     * a sql drop for the table. Defaults to <code>false</code>.
     */
    public boolean supportsIfExistsAfterTableName() {
        return false;
    }

    /**
     * Does the dialect support a special query to check if a table exists.
     * Defaults to <code>false</code>.
     */
    public boolean supportsTableExists() {
        return false;
    }

    /**
     * If the dialect support a special query to check if a table exists, the actual
     * sql that is used to perform it. Defaults to throw an Unsupported excetion (see
     * {@link #supportsTableExists()}.
     */
    public String sqlTableExists(String catalog, String schemaName) {
        throw new UnsupportedOperationException("Not sql provided to define if a table exists");
    }

    /**
     * Does the database require using an <code>InputStream</code> to insert a blob,
     * or the <code>setBlob</code> method. Defaults to <code>true</code>.
     */
    public boolean useInputStreamToInsertBlob() {
        return true;
    }

    /**
     * Do we need to perform a special check to see if the lock already exists in the database, or should
     * we try and insert it without checking. Defaults to <code>true</code>.
     */
    public boolean useExistsBeforeInsertLock() {
        return true;
    }

    /**
     * The opening quote for a quoted identifier. Defaults to ".
     */
    public char openQuote() {
        return '"';
    }

    /**
     * The closing quote for a quoted identifier . Defaults to ".
     */
    public char closeQuote() {
        return '"';
    }

    /**
     * Some database require quoting the blob in selects
     */
    public String openBlobSelectQuote() {
        return "";
    }

    /**
     * Some database require quoting the blob in selects
     */
    public String closeBlobSelectQuote() {
        return "";
    }

    /**
     * Completely optional cascading drop clause
     */
    public String getCascadeConstraintsString() {
        return "";
    }

    /**
     * Does the database supports select ... for update sql clause?
     */
    public abstract boolean supportsForUpdate();

    /**
     * Does this dialect support the <code>FOR UPDATE</code> syntax? Defaults to <code> for update</code>.
     */
    public String getForUpdateString() {
        return " for update";
    }

    /**
     * Does this dialect support the Oracle-style <code>FOR UPDATE NOWAIT</code> syntax?
     * Defaults to {@link #getForUpdateString()}.
     */
    public String getForUpdateNowaitString() {
        return getForUpdateString();
    }

    /**
     * The type of the table that is created. Defaults to an empty string.
     */
    public String getTableTypeString() {
        return "";
    }

    /**
     * Does the database supports a query for the current timestamp. Defaults to <code>false</code>.
     */
    public boolean supportsCurrentTimestampSelection() {
        return false;
    }

    /**
     * The database current time stamp select query.
     */
    public String getCurrentTimestampSelectString() {
        throw new UnsupportedOperationException("Database not known to define a current timestamp function");
    }

    /**
     * If the current timestamp select queyr is a callable query or not.
     */
    public boolean isCurrentTimestampSelectStringCallable() {
        throw new UnsupportedOperationException("Database not known to define a current timestamp function");
    }

    /**
     * The database current timestamp function that is used with several sql updates.
     */
    public abstract String getCurrentTimestampFunction();
    
    /**
     * The database varchar type for the given length. The length is in chars.
     */
    public abstract String getVarcharType(int length);

    /**
     * The database blob type for the given length. The length is in KB.
     */
    public abstract String getBlobType(long length);

    /**
     * The database number type.
     */
    public abstract String getNumberType();

    /**
     * The database TIMESTAMP type.
     */
    public abstract String getTimestampType();

    /**
     * The database BIT type.
     */
    public abstract String getBitType();

}
