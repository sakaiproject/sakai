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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.sql.DataSource;

import org.apache.lucene.store.jdbc.JdbcStoreException;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;

/**
 * @author kimchy
 */
public class DialectResolver {

    public static interface DatabaseMetaDataToDialectMapper {

        Class getDialect(DatabaseMetaData metaData) throws SQLException;
    }

    public static class DatabaseNameToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private String databaseName;

        private Class dialect;

        public DatabaseNameToDialectMapper(String databaseName, Class dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
        }

        public Class getDialect(DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().equals(databaseName)) {
                return dialect;
            }
            return null;
        }
    }

    public static class DatabaseNameStartsWithToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private String databaseName;

        private Class dialect;

        public DatabaseNameStartsWithToDialectMapper(String databaseName, Class dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
        }

        public Class getDialect(DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().startsWith(databaseName)) {
                return dialect;
            }
            return null;
        }
    }

    public static class DatabaseNameAndVersionToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private String databaseName;

        private Class dialect;

        private int version;

        public DatabaseNameAndVersionToDialectMapper(String databaseName, int version, Class dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
            this.version = version;
        }

        public Class getDialect(DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().equals(databaseName) && metaData.getDatabaseMajorVersion() == version) {
                return dialect;
            }
            return null;
        }
    }

    private LinkedList mappers = new LinkedList();

    public DialectResolver() {
        this(true);
    }

    public DialectResolver(boolean useDefaultMappers) {
        if (!useDefaultMappers) {
            return;
        }
        mappers.add(new DatabaseNameToDialectMapper("HSQL Database Engine", HSQLDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("DB2/NT", DB2Dialect.class));
        mappers.add(new DatabaseNameToDialectMapper("MySQL", MySQLDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("PostgreSQL", PostgreSQLDialect.class));
        mappers.add(new DatabaseNameStartsWithToDialectMapper("Microsoft SQL Server", SQLServerDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("Sybase SQL Server", SybaseDialect.class));
        mappers.add(new DatabaseNameAndVersionToDialectMapper("Oracle", 8, Oracle8Dialect.class));
        mappers.add(new DatabaseNameToDialectMapper("Oracle", OracleDialect.class));
    }

    public void addFirstMapper(DatabaseMetaDataToDialectMapper mapper) {
        mappers.addFirst(mapper);
    }

    public void addLastMapper(DatabaseMetaDataToDialectMapper mapper) {
        mappers.addLast(mapper);
    }

    public Dialect getDialect(DataSource dataSource) throws JdbcStoreException {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        String databaseName;
        int databaseMajorVersion;
        int databaseMinorVersion;
        String driverName;
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            databaseName = metaData.getDatabaseProductName();
            databaseMajorVersion = metaData.getDatabaseMajorVersion();
            databaseMinorVersion = metaData.getDatabaseMinorVersion();
            driverName = metaData.getDriverName();
            for (int i = 0; i < mappers.size(); i++) {
                DatabaseMetaDataToDialectMapper mapper = (DatabaseMetaDataToDialectMapper) mappers.get(i);
                Class dialectClass = mapper.getDialect(metaData);
                if (dialectClass == null) {
                    continue;
                }
                return (Dialect) dialectClass.newInstance();
            }
        } catch (Exception e) {
            throw new JdbcStoreException("Failed to auto detect dialect", e);
        } finally {
            DataSourceUtils.releaseConnection(conn);
        }
        throw new JdbcStoreException("Failed to auto detect dialect, no match found for database [" + databaseName +
                "] version [" + databaseMajorVersion + "/" + databaseMinorVersion + "] driver [" + driverName + "]");
    }
}
