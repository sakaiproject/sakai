/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/DoubleStorageSqlHSql.java $
 * $Id: DoubleStorageSqlHSql.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
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
 * methods for accessing double storage data in a hypersonic sql database.
 */
public class DoubleStorageSqlHSql extends DoubleStorageSqlDefault
{
	@Override
	public String addLimitToQuery(String sqlIn, int startRec, int endRec)
	{
		if ( startRec > endRec ) return null;
		int position = sqlIn.toLowerCase().indexOf("select ");
		if ( position < 0 ) return null;
		int recordCount = (endRec-startRec)+1;
		String retval = "select limit "+startRec+" "+recordCount+" "+sqlIn.substring(position+7);
		return retval;
	}

	@Override
	public String addTopToQuery(String sqlIn, int endRec)
	{
		int position = sqlIn.toLowerCase().indexOf("select ");
		if ( position < 0 ) return null;
		String retval = "select top "+endRec+" " + sqlIn.substring(position+7);
		return retval;
	}
}
