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

package org.sakaiproject.db.impl;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;

/**
 * methods for accessing sql service methods in a db2 database.
 */
public class BasicSqlServiceSqlDb2 extends BasicSqlServiceSqlDefault
{
   public boolean getRecordAlreadyExists(SQLException ex)
   {
      return ex.getErrorCode() == -803;
   }

   public PreparedStatement setNull(PreparedStatement pstmt, int pos) throws SQLException
   {
      // sometimes the type is not detectable, fall back on VARCHAR
      // see http://jira.springframework.org/browse/SPR-4465
      // db2 will accept type of VARCHAR in most cases
      try {
         ParameterMetaData pmd = pstmt.getParameterMetaData() ;
         pstmt.setNull(pos,  pmd.getParameterType(pos), null);
      } catch (SQLException e) {
         pstmt.setNull(pos,  Types.VARCHAR, null);
      }
      return pstmt;
   }


   public PreparedStatement setBytes(PreparedStatement pstmt, byte[] bytes, int pos) throws SQLException {
      Blob blob = new SerialBlob(bytes);
      pstmt.setBinaryStream(pos, blob.getBinaryStream(), (int)blob.length());
      return pstmt;
   }

}
