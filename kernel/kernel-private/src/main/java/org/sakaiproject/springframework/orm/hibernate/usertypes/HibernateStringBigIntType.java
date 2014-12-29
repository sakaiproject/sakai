package org.sakaiproject.springframework.orm.hibernate.usertypes;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 25, 2007
 * Time: 3:39:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class HibernateStringBigIntType implements UserType {

    public int[] sqlTypes() {
        return new int[]
                {
                        Types.BIGINT
                };
    }

    public Class returnedClass() {
        return String.class;
    }

    public boolean equals(Object x, Object y) {
        return x.equals(y);
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
        final Object object;

        final Integer bigInt = resultSet.getInt(names[0]);
        if (bigInt == null) {
            return null;
        }

        return bigInt.toString();
    }

    public void nullSafeSet(PreparedStatement statement, Object value, int index) throws SQLException {
        final String strValue = (String) value;
        if (strValue == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setInt(index, Integer.valueOf(strValue));
        }
    }

    public Object deepCopy(Object value) {
        if (value == null)
            return null;

        return String.valueOf((String) value);
    }

    public boolean isMutable() {
        return true;
    }

    public Object replace(Object original, Object target, Object owner) {
        return original;
    }

    public Object assemble(java.io.Serializable cached, Object owner) {
        return cached;
    }

    public java.io.Serializable disassemble(Object value) {
        return (java.io.Serializable) value;
    }

    public int hashCode(Object x) {
        return x.hashCode();
    }

}
