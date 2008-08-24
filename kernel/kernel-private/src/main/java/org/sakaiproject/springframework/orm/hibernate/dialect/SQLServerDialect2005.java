/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.springframework.orm.hibernate.dialect;

import org.hibernate.dialect.SQLServerDialect;

import java.sql.Types;

/**
 * Set up nvarchar & other defaults for SQL Server 2005
 * <br>Creation Date: May 1, 2006
 *
 * @author Mike DeSimone, mike.[at].rsmart.com
 * @version $Revision$
 */
public class SQLServerDialect2005 extends SQLServerDialect {

   public SQLServerDialect2005() {
      super();
      registerColumnType( Types.CHAR, "nchar(1)" );

      // 8000 limit, but nvarchar takes 2 bytes per character
      registerColumnType( Types.VARCHAR, 4000, "nvarchar($l)" );
      registerColumnType( Types.VARCHAR, "nvarchar(max)" );

      registerColumnType( Types.VARBINARY, 8000, "varbinary($l)" );
      registerColumnType( Types.VARBINARY, "varbinary(max)" );

      registerColumnType( Types.BLOB, "varbinary(max)" );
      registerColumnType( Types.CLOB, "nvarchar(max)" );
   }
}
/**********************************************************************************
 *
 * $Header:  $
 *
 **********************************************************************************/