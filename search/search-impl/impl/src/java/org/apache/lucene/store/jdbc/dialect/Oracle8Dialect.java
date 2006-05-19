package org.apache.lucene.store.jdbc.dialect;

/**
 * An Oracle diaclet. Works with Oracle version 8.
 *
 * @author kimchy
 * @author jbloggs
 */
public class Oracle8Dialect extends OracleDialect {

    public String getCurrentTimestampSelectString() {
        return "select sysdate from dual";
    }
    
    public String getCurrentTimestampFunction() {
        return "sysdate";
    }

    public String getVarcharType(int length) {
        return "varchar2(" + length + ")";
    }

    public String getTimestampType() {
        return "date";
    }

}
