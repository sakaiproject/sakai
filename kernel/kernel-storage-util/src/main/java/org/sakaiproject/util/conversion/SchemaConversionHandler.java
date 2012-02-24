/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/kernel-util/src/main/java/org/sakaiproject/util/conversion/SchemaConversionHandler.java $
 * $Id: SchemaConversionHandler.java 101634 2011-12-12 16:44:33Z aaronz@vt.edu $
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

package org.sakaiproject.util.conversion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ieb
 */
public interface SchemaConversionHandler {

	/**
	 * Retrievs a single source object form a ResultSet, returns null if the
	 * source record does not require conversion the result set was produiced
	 * from selectRecord SQL
	 * 
	 * @param id
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	Object getSource(String id, ResultSet rs) throws SQLException;

	/**
	 * Converts the source object and populates the preapred statement, the
	 * prepared statement is from getUpdateRecord.
	 * 
	 * @param id
	 * @param source
	 * @param updateRecord
	 * @throws SQLException
	 */
	boolean convertSource(String id, Object source, PreparedStatement updateRecord)
			throws SQLException;

	/**
	 * Validate that the source object before conversion is the same as the
	 * result object, after conversion, re-read from the database
	 * 
	 * @param id
	 *        the ID of the record
	 * @param source
	 *        the source object created by the same implementation
	 * @param result
	 *        the result object created by the same implementation
	 * @throws Exception
	 *         if the result is not equivalent to the source, at which point the
	 *         whole transaction will be rolled back
	 */
	void validate(String id, Object source, Object result) throws Exception;

	/**
	 * Get the source object for the validation record
	 * 
	 * @param id
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	Object getValidateSource(String id, ResultSet rs) throws SQLException;

}
