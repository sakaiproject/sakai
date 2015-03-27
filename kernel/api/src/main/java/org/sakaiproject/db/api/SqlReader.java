/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.db.api;

import java.sql.ResultSet;

/**
 * <p>
 * SqlReader is the Interface for objects that can read stuff from a SQL request ResultSet.
 * @param <T> The type of objects returned from the reader.
 * </p>
 */
public interface SqlReader<T>
{
	/**
	 * Read fields from this result set, creating one object which is returned.
	 * 
	 * @param result
	 *        The SQL ResultSet, set to the proper record.
	 * @return The object read of type T.
	 */
	T readSqlResultRecord(ResultSet result) throws SqlReaderFinishedException;
}
