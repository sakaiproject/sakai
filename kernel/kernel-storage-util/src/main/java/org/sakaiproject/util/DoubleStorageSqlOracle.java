/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/DoubleStorageSqlOracle.java $
 * $Id: DoubleStorageSqlOracle.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 Sakai Foundation
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

package org.sakaiproject.util;

/**
 * methods for accessing double storage data in an oracle database.
 */
public class DoubleStorageSqlOracle extends DoubleStorageSqlDefault
{
	public String getSelectXml3Sql(String table, String idField, String ref)
	{
		return "select XML from " + table + " where ( " + idField + " = '" + ref + "' ) for update nowait";
	}

	/*
	 * (non-Javadoc)
	 * @see org.sakaiproject.mailarchive.impl.DoubleStorageSqlDefault#addLimitToQuery(java.lang.String, int, int)
	 * 
	 * Since the limit is zero-based we must add one to the values.
	 */
	@Override
	public String addLimitToQuery(String sqlIn, int startRec, int endRec)
	{
		if ( startRec > endRec ) return null;
		String retval = "select * from ( select a.*, rownum rnum from ( " +sqlIn + 
			" ) a where rownum <= "+(endRec+1)+" ) where rnum >= "+(startRec+1);
		return retval;
	}

}
