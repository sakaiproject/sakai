/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/DoubleStorageSqlMySql.java $
 * $Id: DoubleStorageSqlMySql.java 101656 2011-12-12 22:40:28Z aaronz@vt.edu $
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
 * methods for accessing double storage data in a mysql database.
 */
public class DoubleStorageSqlMySql extends DoubleStorageSqlDefault
{

	/**
	 * Return a record ID to use internally in the database. This is needed for databases (MySQL) that have limits on key lengths. The hash code
	 * ensures that the record ID will be unique, even if the DB only considers a prefix of a very long record ID.
	 * 
	 * @param recordId
	 * @return The record ID to use internally in the database
	 */
	public String getRecordId(String recordId)
	{
		if (recordId == null) recordId = "null";
		return recordId.hashCode() + " - " + recordId;
	}
    
	// MySql supports the limit operation at the end of the Query so this is pretty easy
	@Override
        public String addLimitToQuery(String sqlIn, int startRec, int endRec)
	{
		int recordCount = (endRec-startRec)+1;       
		return sqlIn + " limit "+startRec+","+recordCount;
	}

}
