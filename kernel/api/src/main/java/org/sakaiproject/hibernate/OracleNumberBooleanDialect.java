package org.sakaiproject.hibernate;

import java.sql.Types;

import org.hibernate.dialect.OracleDialect;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;

public class OracleNumberBooleanDialect extends OracleDialect {

    @Override
    public JdbcType resolveSqlTypeDescriptor(
            String columnTypeName,
            int jdbcTypeCode,
            int precision,
            int scale,
            JdbcTypeRegistry jdbcTypeRegistry) {

        if (jdbcTypeCode == Types.BOOLEAN) {
            return jdbcTypeRegistry.getDescriptor(Types.INTEGER);
        }

        return super.resolveSqlTypeDescriptor(
                columnTypeName,
                jdbcTypeCode,
                precision,
                scale,
                jdbcTypeRegistry
        );
    }
}