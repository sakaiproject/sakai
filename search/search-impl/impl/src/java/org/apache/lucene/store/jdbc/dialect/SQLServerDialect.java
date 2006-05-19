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
 * A SQLServer dialect.
 *
 * @author kimchy
 */
public class SQLServerDialect extends SybaseDialect {

    public char closeQuote() {
        return ']';
    }

    public char openQuote() {
        return '[';
    }
    
    /**
     * SQLServer supports if table exists queries.
     */
    public boolean supportsTableExists() {
        return true;
    }

    public String sqlTableExists(String catalog, String schemaName) {
        StringBuffer sb = new StringBuffer();
        sb.append("select table_name from INFORMATION_SCHEMA.Tables where lower(table_name) = ?");
        if (schemaName != null) {
            sb.append(" and lower(table_schema) = '").append(schemaName.toLowerCase()).append("'");
        }
        if (catalog != null) {
            sb.append(" and lower(table_catalog) = '").append(catalog.toLowerCase()).append("'");
        }
        return sb.toString();
    }
}
