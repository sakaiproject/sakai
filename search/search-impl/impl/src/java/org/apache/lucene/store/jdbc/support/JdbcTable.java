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

package org.apache.lucene.store.jdbc.support;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
import org.apache.lucene.store.jdbc.dialect.Dialect;

/**
 * An internal representation of a database table used to store the {@link org.apache.lucene.store.jdbc.JdbcDirectory}
 * settings.
 *
 * @author kimchy
 */
public class JdbcTable {

    private Dialect dialect;

    private JdbcDirectorySettings settings;

    private String name;

    private String schema;

    private String catalog;

    private boolean quoted;

    private boolean schemaQuoted;

    private String sqlCreate;
    private String sqlDrop;
    private String sqlSelectNames;
    private String sqlSelectNameExists;
    private String sqlSelecltLastModifiedByName;
    private String sqlUpdateLastModifiedByName;
    private String sqlDeleteByName;
    private String sqlMarkDeleteByName;
    private String sqlUpdateNameByName;
    private String sqlSelectSizeByName;
    private String sqlInsert;
    private String sqlUpdateSizeLastModifiedByName;
    private String sqlSelectSizeValueByName;
    private String sqlDeletaAll;
    private String sqlDeletaMarkDeleteByDelta;
    private String sqlSelectNameForUpdateNoWait;

    private JdbcColumn nameColumn;
    private JdbcColumn valueColumn;
    private JdbcColumn sizeColumn;
    private JdbcColumn lastModifiedColumn;
    private JdbcColumn deletedColumn;

    public JdbcTable(JdbcDirectorySettings settings, Dialect dialect, String name) {
        this(settings, dialect, name, null, null);
    }

    public JdbcTable(JdbcDirectorySettings settings, Dialect dialect, String name, String catalog, String schema) {
        this.dialect = dialect;
        this.settings = settings;
        setName(name);
        setSchema(schema);
        setCatalog(catalog);
        nameColumn = new JdbcColumn(dialect, settings.getNameColumnName(), 1, dialect.getVarcharType(settings.getNameColumnLength()));
        valueColumn = new JdbcColumn(dialect, settings.getValueColumnName(), 2, dialect.getBlobType(settings.getValueColumnLengthInK()));
        sizeColumn = new JdbcColumn(dialect, settings.getSizeColumnName(), 3, dialect.getNumberType());
        lastModifiedColumn = new JdbcColumn(dialect, settings.getLastModifiedColumnName(), 4, dialect.getTimestampType());
        deletedColumn = new JdbcColumn(dialect, settings.getDeletedColumnName(), 5, dialect.getBitType());

        StringBuffer sb = new StringBuffer();

        sqlCreate = sb.append("create table ").append(getQualifiedName()).append(" (")
                .append(nameColumn.getName()).append(' ').append(nameColumn.getType()).append(" , ")
                .append(valueColumn.getName()).append(' ').append(valueColumn.getType()).append(" , ")
                .append(sizeColumn.getName()).append(' ').append(sizeColumn.getType()).append(" , ")
                .append(lastModifiedColumn.getName()).append(' ').append(lastModifiedColumn.getType()).append(" , ")
                .append(deletedColumn.getName()).append(' ').append(deletedColumn.getType())
                .append(", " + "primary key (").append(nameColumn.getName()).append(") ) ")
                .append(dialect.getTableTypeString()).toString();

        sb.setLength(0);
        sb.append("drop table ");
        if (dialect.supportsIfExistsBeforeTableName()) sb.append("if exists ");
        sb.append(getQualifiedName()).append(dialect.getCascadeConstraintsString());
        if (dialect.supportsIfExistsAfterTableName()) sb.append(" if exists");
        sqlDrop = sb.toString();

        sb.setLength(0);
        sqlSelectNames = sb.append("select ").append(nameColumn.getQuotedName())
                .append(" from ").append(getQualifiedName())
                .append(" where ").append(deletedColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlSelectNameExists = sb.append("select ").append(deletedColumn.getQuotedName())
                .append(" from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlSelecltLastModifiedByName = sb.append("select ").append(lastModifiedColumn.getQuotedName())
                .append(" from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlUpdateLastModifiedByName = sb.append("update ").append(getQualifiedName())
                .append(" set ").append(lastModifiedColumn.getQuotedName()).append(" = ").append(dialect.getCurrentTimestampFunction())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlDeleteByName = sb.append("delete from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlDeletaMarkDeleteByDelta = sb.append("delete from ").append(getQualifiedName())
                .append(" where ").append(deletedColumn.getQuotedName()).append(" = ?")
                .append("and ").append(lastModifiedColumn.getQuotedName()).append(" < ?").toString();

        sb.setLength(0);
        sqlUpdateNameByName = sb.append("update ").append(getQualifiedName())
                .append(" set ").append(nameColumn.getQuotedName())
                .append(" = ?" + " where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlSelectNameForUpdateNoWait = sb.append("select ").append(nameColumn.getQuotedName())
                .append(" from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?")
                .append(dialect.getForUpdateNowaitString()).toString();


        sb.setLength(0);
        sqlSelectSizeByName = sb.append("select ").append(sizeColumn.getQuotedName())
                .append(" from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlInsert = sb.append("insert into ").append(getQualifiedName())
                .append(" (").append(nameColumn.getQuotedName()).append(", ")
                .append(valueColumn.getQuotedName()).append(", ")
                .append(sizeColumn.getQuotedName()).append(", ")
                .append(lastModifiedColumn.getQuotedName()).append(", ")
                .append(deletedColumn.getQuotedName())
                .append(") values ( ?, ?, ?, ").append(dialect.getCurrentTimestampFunction()).append(", ?").append(" )").toString();

        sb.setLength(0);
        sqlUpdateSizeLastModifiedByName = sb.append("update ").append(getQualifiedName())
                .append(" set ").append(sizeColumn.getQuotedName()).append(" = ? , ")
                .append(lastModifiedColumn.getQuotedName()).append(" = ").append(dialect.getCurrentTimestampFunction())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlMarkDeleteByName = sb.append("update ").append(getQualifiedName())
                .append(" set ").append(deletedColumn.getQuotedName()).append(" = ? , ")
                .append(lastModifiedColumn.getQuotedName()).append(" = ").append(dialect.getCurrentTimestampFunction())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlSelectSizeValueByName = sb.append("select ").append(nameColumn.getQuotedName()).append(", ")
                .append(dialect.openBlobSelectQuote()).append(valueColumn.getQuotedName()).append(dialect.closeBlobSelectQuote()).append(" as x")
                .append(", ").append(sizeColumn.getQuotedName())
                .append(" from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" = ?").toString();

        sb.setLength(0);
        sqlDeletaAll = sb.append("delete from ").append(getQualifiedName())
                .append(" where ").append(nameColumn.getQuotedName()).append(" <> '").append(IndexWriter.COMMIT_LOCK_NAME).append("'")
                .append(" and ").append(nameColumn.getQuotedName()).append(" <> '").append(IndexWriter.WRITE_LOCK_NAME).append("'").toString();
    }

    public void setName(String name) {
        if (name.charAt(0) == dialect.openQuote()) {
            quoted = true;
            this.name = name.substring(1, name.length() - 1).replace('-', '_');
        } else {
            this.name = name.replace('-', '_');
        }
    }

    public String getName() {
        return name;
    }

    public void setSchema(String schema) {
        if (schema != null && schema.charAt(0) == dialect.openQuote()) {
            schemaQuoted = true;
            this.schema = schema.substring(1, schema.length() - 1);
        } else {
            this.schema = schema;
        }
    }

    public String getSchema() {
        return schema;
    }


    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public JdbcColumn getNameColumn() {
        return this.nameColumn;
    }

    public JdbcColumn getSizeColumn() {
        return this.sizeColumn;
    }

    public JdbcColumn getValueColumn() {
        return this.valueColumn;
    }

    public JdbcColumn getLastModifiedColumn() {
        return this.lastModifiedColumn;
    }

    public JdbcColumn getDeletedColumn() {
        return this.deletedColumn;
    }

    public String sqlSelectNames() {
        return sqlSelectNames;
    }

    public String sqlSelectNameExists() {
        return sqlSelectNameExists;
    }

    public String sqlSelecltLastModifiedByName() {
        return sqlSelecltLastModifiedByName;
    }

    public String sqlUpdateLastModifiedByName() {
        return sqlUpdateLastModifiedByName;
    }

    public String sqlDeleteByName() {
        return sqlDeleteByName;
    }

    public String sqlUpdateNameByName() {
        return sqlUpdateNameByName;
    }

    public String sqlSelectSizeByName() {
        return sqlSelectSizeByName;
    }

    public String sqlDeletaMarkDeleteByDelta() {
        return sqlDeletaMarkDeleteByDelta;
    }

    public String sqlInsert() {
        return sqlInsert;
    }

    public String sqlUpdateSizeLastModifiedByName() {
        return sqlUpdateSizeLastModifiedByName;
    }

    public String sqlSelectSizeValueByName() {
        return sqlSelectSizeValueByName;
    }

    public String sqlSelectNameForUpdateNoWait() {
        return sqlSelectNameForUpdateNoWait;
    }

    public String sqlMarkDeleteByName() {
        return sqlMarkDeleteByName;
    }

    public String sqlDeletaAll() {
        return sqlDeletaAll;
    }

    public String sqlCreate() {
        return sqlCreate;
    }

    public String sqlDrop() {
        return sqlDrop;
    }

    public String getQualifiedName() {
        String quotedName = getQuotedName();
        return qualify(catalog, getQuotedSchema(), quotedName);
    }

    public String getQuotedName() {
        return quoted ?
                dialect.openQuote() + name + dialect.closeQuote() :
                name;
    }

    public String getQuotedSchema() {
        if (schema == null) {
            return null;
        }
        return schemaQuoted ?
                dialect.openQuote() + schema + dialect.closeQuote() :
                schema;
    }

    public static String qualify(String catalog, String schema, String table) {
        StringBuffer qualifiedName = new StringBuffer();
        if (catalog != null) {
            qualifiedName.append(catalog).append('.');
        }
        if (schema != null) {
            qualifiedName.append(schema).append('.');
        }
        return qualifiedName.append(table).toString();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (getCatalog() != null) buf.append(getCatalog()).append(".");
        if (getSchema() != null) buf.append(getSchema()).append(".");
        buf.append(getName());
        return buf.toString();
    }

    public JdbcDirectorySettings getSettings() {
        return settings;
    }

    public Dialect getDialect() {
        return this.dialect;
    }
}
