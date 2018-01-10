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
 *       http://www.opensource.org/licenses/ECL-2.0
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

import lombok.extern.slf4j.Slf4j;

import org.quartz.utils.ConnectionProvider;
import org.sakaiproject.component.cover.ComponentManager;

/**
 * This looks up the DataSource from Spring so that it will use the main Sakai connection.
 */
@Slf4j
public class ConnectionProviderDelegate implements ConnectionProvider
{
  private DataSource ds;

  @Override
  public Connection getConnection() throws SQLException {
     if (log.isDebugEnabled()){
       log.debug("quartz getConnection()");
     }
     return ds.getConnection();
  }

  @Override
  public void shutdown() throws SQLException {
  }

  @Override
  public void initialize() throws SQLException {
      ds = ComponentManager.get(javax.sql.DataSource.class);
  }

}
