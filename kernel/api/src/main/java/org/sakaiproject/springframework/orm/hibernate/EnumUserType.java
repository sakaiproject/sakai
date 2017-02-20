/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.springframework.orm.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
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
 * User: Duffy Gillman <duffy@rsmart.com>
 * Date: Aug 26, 2010
 * Time: 4:48:50 PM
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

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] strings, SessionImplementor sessionImplementor, Object o) throws HibernateException, SQLException {
        String name = resultSet.getString(strings[0]);
        E result = null;

        if (!resultSet.wasNull())
        {
            result = Enum.valueOf(myClass, name);
        }

        return result;
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i, SessionImplementor sessionImplementor) throws HibernateException, SQLException {
        if (null == o) {
            preparedStatement.setNull(i, Types.VARCHAR);
        } else {
            preparedStatement.setString(i, ((Enum)o).name());
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
