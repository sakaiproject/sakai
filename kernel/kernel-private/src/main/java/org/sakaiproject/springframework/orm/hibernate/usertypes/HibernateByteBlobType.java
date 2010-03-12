package org.sakaiproject.springframework.orm.hibernate.usertypes;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.Blob;

import org.hibernate.usertype.UserType;
import org.hibernate.HibernateException;

/**
 * <p/>
 * A hibernate user type which converts a Blob into a byte[] and back again.
 * </p>
 */
public class HibernateByteBlobType
        implements UserType {
    public int[] sqlTypes() {
        return new int[]
                {
                        Types.BLOB
                };
    }

    public Class returnedClass() {
        return byte[].class;
    }

    public boolean equals(Object x, Object y) {
        return (x == y)
                || (x != null && y != null && java.util.Arrays.equals(
                (byte[]) x,
                (byte[]) y));
    }

    public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
        final Object object;

        final InputStream inputStream = resultSet.getBinaryStream(names[0]);
        if (inputStream == null) {
            object = null;
        } else {
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            try {
                final byte[] buffer = new byte[65536];
                int read = -1;

                while ((read = inputStream.read(buffer)) > -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.close();
            }
            catch (IOException exception) {
                throw new HibernateException("Unable to read blob " + names[0], exception);
            }
            object = outputStream.toByteArray();
        }

        return object;
    }

    public void nullSafeSet(PreparedStatement statement, Object value, int index) throws SQLException {
        final byte[] bytes = (byte[]) value;
        if (bytes == null) {
            try {
                statement.setBinaryStream(index, null, 0);
            }
            catch (SQLException exception) {
            	//FB doesn't like this but is needed to avoid method ambiguity in java 1.6
            	Blob nullBlob = null;
                statement.setBlob(index, nullBlob);
            }
        } else {
            statement.setBinaryStream(index, new ByteArrayInputStream(bytes), bytes.length);
        }
    }

    public Object deepCopy(Object value) {
        if (value == null)
            return null;

        byte[] bytes = (byte[]) value;
        byte[] result = new byte[bytes.length];
        System.arraycopy(bytes, 0, result, 0, bytes.length);

        return result;
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
