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
 * A MySQL dialect.
 *
 * @author kimchy
 */
public class MySQLDialect extends Dialect {

	
	private boolean seekable = false;
	
	public MySQLDialect(boolean seekable) {
		this.seekable = seekable;
	}
	
    /**
     * MySQL requires quoting the blob column with connector J 3.1 when using emulateLocators=true.
     * IEB, disabled since we cant get emulateLocators in Sakai
     */
    public String openBlobSelectQuote() {
        if ( seekable ) return "'";
        return "";
    }

    /**
     * MySQL requires quoting the blob column with connector J 3.1 when using emulateLocators=true.
     * 
     * IEB, disabled since we cant get emulateLocators in Sakai
     */
    public String closeBlobSelectQuote() {
        if (seekable )return "'";
        return "";
    }

    public char closeQuote() {
        return '`';
    }

    public char openQuote() {
        return '`';
    }

    /**
     * MySQL supports select ... for update.
     */
    public boolean supportsForUpdate() {
        return true;
    }

    /**
     * MySQL supports if exists before the table name.
     */
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    /**
     * MySQL supports current timestamp selection.
     */
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    public String getCurrentTimestampSelectString() {
        return "select now()";
    }

    public String getVarcharType(int length) {
        return "varchar(" + length + ")";
    }

    public String getBlobType(long length) {
        return "longblob";
    }

    public String getNumberType() {
        return "integer";
    }

    public String getTimestampType() {
        return "datetime";
    }

    public String getCurrentTimestampFunction() {
        return "current_timestamp";
    }

    public String getBitType() {
        return "bit";
    }
}
