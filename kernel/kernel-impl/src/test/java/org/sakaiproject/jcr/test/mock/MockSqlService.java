/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.jcr.test.mock;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.util.List;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.ServerOverloadException;

/**
 * @author ieb
 *
 */
public class MockSqlService implements SqlService
{

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#borrowConnection()
	 */
	public Connection borrowConnection() throws SQLException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbCancel(java.sql.Connection)
	 */
	public void dbCancel(Connection arg0)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbInsert(java.sql.Connection, java.lang.String, java.lang.Object[], java.lang.String)
	 */
	public Long dbInsert(Connection arg0, String arg1, Object[] arg2, String arg3)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbInsert(java.sql.Connection, java.lang.String, java.lang.Object[], java.lang.String, java.io.InputStream, int)
	 */
	public Long dbInsert(Connection arg0, String arg1, Object[] arg2, String arg3,
			InputStream arg4, int arg5)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbRead(java.lang.String)
	 */
	public List dbRead(String arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbRead(java.lang.String, java.lang.Object[], org.sakaiproject.db.api.SqlReader)
	 */
	public List dbRead(String arg0, Object[] arg1, SqlReader arg2)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbRead(java.sql.Connection, java.lang.String, java.lang.Object[], org.sakaiproject.db.api.SqlReader)
	 */
	public List dbRead(Connection arg0, String arg1, Object[] arg2, SqlReader arg3)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadBinary(java.lang.String, java.lang.Object[], byte[])
	 */
	public void dbReadBinary(String arg0, Object[] arg1, byte[] arg2)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadBinary(java.lang.String, java.lang.Object[], boolean)
	 */
	public InputStream dbReadBinary(String arg0, Object[] arg1, boolean arg2)
			throws ServerOverloadException
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadBinary(java.sql.Connection, java.lang.String, java.lang.Object[], byte[])
	 */
	public void dbReadBinary(Connection arg0, String arg1, Object[] arg2, byte[] arg3)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadBlobAndUpdate(java.lang.String, byte[])
	 */
	public void dbReadBlobAndUpdate(String arg0, byte[] arg1)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadLock(java.lang.String, java.lang.StringBuffer)
	 */
	public Connection dbReadLock(String arg0, StringBuffer arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbUpdateCommit(java.lang.String, java.lang.Object[], java.lang.String, java.sql.Connection)
	 */
	public void dbUpdateCommit(String arg0, Object[] arg1, String arg2, Connection arg3)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWrite(java.lang.String)
	 */
	public boolean dbWrite(String arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWrite(java.lang.String, java.lang.String)
	 */
	public boolean dbWrite(String arg0, String arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWrite(java.lang.String, java.lang.Object[])
	 */
	public boolean dbWrite(String arg0, Object[] arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWrite(java.sql.Connection, java.lang.String, java.lang.Object[])
	 */
	public boolean dbWrite(Connection arg0, String arg1, Object[] arg2)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWrite(java.lang.String, java.lang.Object[], java.lang.String)
	 */
	public boolean dbWrite(String arg0, Object[] arg1, String arg2)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWriteBinary(java.lang.String, java.lang.Object[], byte[], int, int)
	 */
	public boolean dbWriteBinary(String arg0, Object[] arg1, byte[] arg2, int arg3,
			int arg4)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbWriteFailQuiet(java.sql.Connection, java.lang.String, java.lang.Object[])
	 */
	public boolean dbWriteFailQuiet(Connection arg0, String arg1, Object[] arg2)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#ddl(java.lang.ClassLoader, java.lang.String)
	 */
	public void ddl(ClassLoader arg0, String arg1)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#getBooleanConstant(boolean)
	 */
	public String getBooleanConstant(boolean arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#getCal()
	 */
	public GregorianCalendar getCal()
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#getNextSequence(java.lang.String, java.sql.Connection)
	 */
	public Long getNextSequence(String arg0, Connection arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#getVendor()
	 */
	public String getVendor()
	{
		return "derby";
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#returnConnection(java.sql.Connection)
	 */
	public void returnConnection(Connection arg0)
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#transact(java.lang.Runnable, java.lang.String)
	 */
	public boolean transact(Runnable arg0, String arg1)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadLock(java.lang.String, java.lang.StringBuilder)
	 */
	public Connection dbReadLock(String arg0, StringBuilder arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.db.api.SqlService#dbReadLock(java.lang.String, org.sakaiproject.db.api.SqlReader)
	 */
	public Connection dbReadLock(String arg0, SqlReader arg1)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
