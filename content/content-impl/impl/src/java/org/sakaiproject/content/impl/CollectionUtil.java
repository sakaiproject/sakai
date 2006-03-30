/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.impl;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.webapp.cover.SessionManager;

public class CollectionUtil
{
	private static final Log LOG = LogFactory.getLog(CollectionUtil.class);

	private static ResourceBundle rb = ResourceBundle.getBundle("content");

	/**
	 * Using two arg version of concat to be compatible w/all db. MySql supports n arguments while Oracle, HSQLDB suport only 2 arg version
	 */
	private static final String sql = "select ss.site_id, ss.title, sstool.registration "
			+ "from SAKAI_SITE_TOOL sstool, SAKAI_SITE_USER ssuser, " + "SAKAI_SITE ss "
			+ "where (sstool.registration = 'sakai.resources' " + "or sstool.registration = 'sakai.dropbox') "
			+ "and sstool.site_id = ssuser.site_id " + "and sstool.site_id = ss.site_id " + "and ssuser.user_id = ? "
			+ "order by ss.title";

	private static final SqlReader sr = new SqlReader()
	{
		public Object readSqlResultRecord(ResultSet result)
		{
			try
			{
				List list = new ArrayList(2);

				String registration = result.getString("registration");
				String context = result.getString("site_id");

				if ("sakai.dropbox".equals(registration))
				{
					list.add(ContentHostingService.getDropboxCollection(context));
					list.add(result.getString("title") + " " + rb.getString("gen.drop"));
				}
				else
				{
					list.add(ContentHostingService.getSiteCollection(context));
					list.add(result.getString("title") + " " + rb.getString("gen.reso"));
				}
				return list;

			}
			catch (Throwable t)
			{
				LOG.warn("Sql.dbRead: sql: " + sql + t);
			}
			return null;
		}
	};

	static Map getCollectionMap()
	{
		// create SqlReader
		String userId = SessionManager.getCurrentSessionUserId().trim();
		Object[] fields = new Object[] { userId };
		List collectionList = SqlService.dbRead(sql, fields, sr);

		Map collectionMap = new LinkedHashMap(collectionList.size());
		for (Iterator i = collectionList.iterator(); i.hasNext();)
		{
			List arrayList = (List) i.next();
			collectionMap.put(arrayList.get(1), arrayList.get(0)); // title, collection_id
		}
		return collectionMap;
	}
}