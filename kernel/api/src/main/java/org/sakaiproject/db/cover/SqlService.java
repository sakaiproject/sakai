/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.db.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * SqlService is a static Cover for the {@link org.sakaiproject.db.api.SqlService SqlService}; see that interface for usage details.
 * </p>
 * @deprecated Static covers should not be used in favour of injection or lookup
 * via the component manager. This cover will be removed in a later version of the Kernel
 */
public class SqlService
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.db.api.SqlService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.db.api.SqlService) ComponentManager.get(org.sakaiproject.db.api.SqlService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.db.api.SqlService) ComponentManager.get(org.sakaiproject.db.api.SqlService.class);
		}
	}

	private static org.sakaiproject.db.api.SqlService m_instance = null;

	public static java.sql.Connection borrowConnection() throws java.sql.SQLException
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.borrowConnection();
	}

	public static void returnConnection(java.sql.Connection param0)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return;

		service.returnConnection(param0);
	}

	public static boolean transact(Runnable callback, String tag)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.transact(callback, tag);
	}

	public static java.util.List dbRead(java.lang.String param0)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.dbRead(param0);
	}

	public static java.util.List dbRead(java.lang.String param0, java.lang.Object[] param1, org.sakaiproject.db.api.SqlReader param2)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.dbRead(param0, param1, param2);
	}

	public static java.util.List dbRead(java.sql.Connection param0, java.lang.String param1, java.lang.Object[] param2,
			org.sakaiproject.db.api.SqlReader param3)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.dbRead(param0, param1, param2, param3);
	}

	public static void dbReadBinary(java.lang.String param0, java.lang.Object[] param1, byte[] param2)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return;

		service.dbReadBinary(param0, param1, param2);
	}

	public static void dbReadBinary(java.sql.Connection param0, java.lang.String param1, java.lang.Object[] param2, byte[] param3)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return;

		service.dbReadBinary(param0, param1, param2, param3);
	}

	public static java.io.InputStream dbReadBinary(java.lang.String param0, java.lang.Object[] param1, boolean param2)
			throws org.sakaiproject.exception.ServerOverloadException
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.dbReadBinary(param0, param1, param2);
	}

	public static boolean dbWrite(java.lang.String param0)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWrite(param0);
	}

	public static boolean dbWrite(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWrite(param0, param1);
	}

	public static boolean dbWrite(java.lang.String param0, java.lang.Object[] param1)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWrite(param0, param1);
	}

	public static boolean dbWrite(java.sql.Connection param0, java.lang.String param1, java.lang.Object[] param2)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWrite(param0, param1, param2);
	}

	public static boolean dbWrite(java.lang.String param0, java.lang.Object[] param1, java.lang.String param2)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWrite(param0, param1, param2);
	}

	public static boolean dbWriteBinary(java.lang.String param0, java.lang.Object[] param1, byte[] param2, int param3, int param4)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWriteBinary(param0, param1, param2, param3, param4);
	}

	public static boolean dbWriteFailQuiet(java.sql.Connection param0, java.lang.String param1, java.lang.Object[] param2)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return false;

		return service.dbWriteFailQuiet(param0, param1, param2);
	}

	public static void dbReadBlobAndUpdate(java.lang.String param0, byte[] param1)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return;

		service.dbReadBlobAndUpdate(param0, param1);
	}

	public static java.sql.Connection dbReadLock(java.lang.String param0, java.lang.StringBuilder param1)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.dbReadLock(param0, param1);
	}

	public static void dbUpdateCommit(java.lang.String param0, java.lang.Object[] param1, java.lang.String param2,
			java.sql.Connection param3)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return;

		service.dbUpdateCommit(param0, param1, param2, param3);
	}

	public static void dbCancel(java.sql.Connection param0)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return;

		service.dbCancel(param0);
	}

	public static java.util.GregorianCalendar getCal()
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.getCal();
	}

	public static java.lang.String getVendor()
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.getVendor();
	}

	public static Long getNextSequence(String tableName, java.sql.Connection conn)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.getNextSequence(tableName, conn);
	}

	public static String getBooleanConstant(boolean value)
	{
		org.sakaiproject.db.api.SqlService service = getInstance();
		if (service == null) return null;

		return service.getBooleanConstant(value);
	}
}
