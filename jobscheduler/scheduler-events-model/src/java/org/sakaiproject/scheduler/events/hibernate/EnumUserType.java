package org.sakaiproject.scheduler.events.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/**
 * This custom user type template was created from instructions found on the JBoss site at:
 *      http://community.jboss.org/wiki/UserTypeforpersistinganEnumwithaVARCHARcolumn
 * 
 * User: duffy
 * Date: Aug 26, 2010
 * Time: 4:48:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class EnumUserType<E extends Enum<E>> implements UserType
{
    private Class<E>
        myClass = null;

    private static final int[]
        SQL_TYPES = {Types.VARCHAR};

    protected EnumUserType (Class<E> c)
    {
        myClass = c;
    }

    public int[] sqlTypes()
    {
        return SQL_TYPES;
    }

    public Class returnedClass()
    {
        return myClass;
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner)
        throws HibernateException, SQLException
    {
        String
            name = resultSet.getString(names[0]);
        E
            result = null;

        if (!resultSet.wasNull())
        {
            result = Enum.valueOf(myClass, name);
        }

        return result;
    }

    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index)
        throws HibernateException, SQLException
    {
        if (null == value)
        {
            preparedStatement.setNull(index, Types.VARCHAR);
        }
        else
        {
            preparedStatement.setString(index, ((Enum)value).name());
        }
    }

    public Object deepCopy(Object value)
        throws HibernateException
    {
        return value;
    }

    public boolean isMutable()
    {
        return false;
    }

    public Object assemble(Serializable cached, Object owner)
        throws HibernateException
    {
         return cached;
    }
 
    public Serializable disassemble(Object value)
        throws HibernateException
    {
        return (Serializable)value;
    }

    public Object replace(Object original, Object target, Object owner)
        throws HibernateException
    {
        return original;
    }

    public int hashCode(Object x)
        throws HibernateException
    {
        return x.hashCode();
    }

    public boolean equals(Object x, Object y)
        throws HibernateException
    {
        if (x == y)
            return true;
        if (null == x || null == y)
            return false;

        return x.equals(y);
    }
}