/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/MultiSingleStorageSqlHSql.java $
 * $Id: MultiSingleStorageSqlHSql.java 51317 2008-08-24 04:38:02Z csev@umich.edu $
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
 * methods for accessing single storage data in a hypersonic sql database.
 */
public class MultiSingleStorageSqlHSql extends MultiSingleStorageSqlDefault
{

	/**
	 * @param storage_fields
	 */
	public MultiSingleStorageSqlHSql(String storage_fields)
	{
		super(storage_fields);
	}
}
