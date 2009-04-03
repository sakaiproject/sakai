/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.component.app.scheduler;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.utils.ConnectionProvider;
import org.sakaiproject.component.cover.ComponentManager;

public class ConnectionProviderDelegate implements ConnectionProvider
{

  private static final Log LOG = LogFactory.getLog(ConnectionProviderDelegate.class);
  private static DataSource ds;


  /**
   * @see org.quartz.utils.ConnectionProvider#getConnection()
   */
  public Connection getConnection() throws SQLException {

     if (LOG.isDebugEnabled()){
       LOG.debug("quartz getConnection()");
     }

     if (ds == null){
       ds = (DataSource) ComponentManager.get("javax.sql.DataSource");
     }
     return ds.getConnection();
  }

  /**
   * @see org.quartz.utils.ConnectionProvider#shutdown()
   */
  public void shutdown() throws SQLException {
  }

}