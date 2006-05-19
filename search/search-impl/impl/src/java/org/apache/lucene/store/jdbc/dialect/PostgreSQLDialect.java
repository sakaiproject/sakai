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
 * A PostgreSQL dialect.
 *
 * @author kimchy
 */
public class PostgreSQLDialect extends Dialect {

    /**
     * PostreSQL supports select ... for update.
     */
    public boolean supportsForUpdate() {
        return true;
    }

    /**
     * PostgreSQL supports transactional scoped blobs.
     */
    public boolean supportTransactionalScopedBlobs() {
        return true;
    }

    /**
     * PostrgreSQL supports a table exists query.
     */
    public boolean supportsTableExists() {
        return true;
    }

    public String sqlTableExists(String catalog, String schemaName) {
        return "select tablename from pg_tables where schemaname = 'public' and lower(tablename) = ?";
    }

    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    public boolean useInputStreamToInsertBlob() {
        return false;
    }

    public String getCurrentTimestampSelectString() {
        return "select now()";
    }
    
    public String getVarcharType(int length) {
        return "varchar(" + length + ")";
    }

    public String getBlobType(long length) {
        return "oid";
    }

    public String getNumberType() {
        return "int4";
    }

    public String getTimestampType() {
        return "timestamp";
    }

    public String getCurrentTimestampFunction() {
        return "current_timestamp";
    }

    public String getBitType() {
        return "bool";
    }
}
