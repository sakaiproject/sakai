/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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



package org.sakaiproject.tool.assessment.settings;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ojb.broker.PBKey;
import org.apache.ojb.broker.PersistenceBrokerFactory;
import org.apache.ojb.broker.metadata.ConnectionPoolDescriptor;
import org.apache.ojb.broker.metadata.ConnectionRepository;
import org.apache.ojb.broker.metadata.DescriptorRepository;
import org.apache.ojb.broker.metadata.JdbcConnectionDescriptor;
import org.apache.ojb.broker.metadata.MetadataManager;
import org.apache.ojb.broker.metadata.SequenceDescriptor;

/**
 * Bootstaps the OJB configuration.
 *
 * @author <a href="mailto:bmcgough@indiana.edu">Brian McGough</a>
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @version $Id$
 */
public class OjbBootStrap
{
  private static Log log = LogFactory.getLog(OjbBootStrap.class);

  /**
   * Creates a new OjbBootStrap object.
   */
  public OjbBootStrap()
  {
    if(log.isDebugEnabled())
    {
      log.debug("new OjbBootStrap()");
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @throws IllegalStateException DOCUMENTATION PENDING
   */
  public void bootstrap()
  {
    log.debug("bootstrap()");
    try
    {
      PathInfo path = PathInfo.getInstance();
      if(
        (path.getBasePathToSecurity() == null) ||
          (path.getBasePathToSettings() == null) ||
          (path.getPathToSecurity() == null) ||
          (path.getPathToSettings() == null))
      {
        log.fatal("PathInfo not initialized properly");
        throw new IllegalStateException("PathInfo not initialized properly");
      }

      Properties securityProperties =
        path.getSecurityProperties("SAM.properties");
      Properties settingsProperties =
        path.getSettingsProperties("SAM.properties");
      if((securityProperties == null) || (settingsProperties == null))
      {
        log.fatal("Could not open Properties");
        throw new IllegalStateException("Could not open Properties");
      }

      if(
        ! securityProperties.containsKey("username") ||
          ! securityProperties.containsKey("password"))
      {
        log.fatal("Properties do not contain username or password");
        throw new IllegalStateException(
          "Properties do not contain username or password");
      }

      if(
        ! settingsProperties.containsKey("jcd-alias") ||
          ! settingsProperties.containsKey("platform") ||
          ! settingsProperties.containsKey("jdbc-level") ||
          ! settingsProperties.containsKey("driver") ||
          ! settingsProperties.containsKey("protocol") ||
          ! settingsProperties.containsKey("subprotocol") ||
          ! settingsProperties.containsKey("dbalias") ||
          ! settingsProperties.containsKey("repositoryXmlFilePath") ||
          ! settingsProperties.containsKey("maxActivePersistenceBrokers") ||
          ! settingsProperties.containsKey("sequenceManagerClass") ||
          ! settingsProperties.containsKey("validationQuery") ||
          ! settingsProperties.containsKey("testOnBorrow") ||
          ! settingsProperties.containsKey("testWhileIdle"))
      {
        log.fatal("Missing one or more required keys");
        throw new IllegalStateException("Missing one or more required keys");
      }


      JdbcConnectionDescriptor jdbcConnectionDescriptor =
        new JdbcConnectionDescriptor();
      jdbcConnectionDescriptor.setJcdAlias(
        settingsProperties.getProperty("jcd-alias"));
      jdbcConnectionDescriptor.setUserName(
        securityProperties.getProperty("username"));
      jdbcConnectionDescriptor.setPassWord(
        securityProperties.getProperty("password"));
      jdbcConnectionDescriptor.setDbAlias(
        settingsProperties.getProperty("dbalias"));
      jdbcConnectionDescriptor.setDbms(
        settingsProperties.getProperty("platform"));
      jdbcConnectionDescriptor.setDefaultConnection(true);
      jdbcConnectionDescriptor.setJdbcLevel(
        settingsProperties.getProperty("jdbc-level"));
      jdbcConnectionDescriptor.setDriver(
        settingsProperties.getProperty("driver"));
      jdbcConnectionDescriptor.setProtocol(
        settingsProperties.getProperty("protocol"));
      jdbcConnectionDescriptor.setSubProtocol(
        settingsProperties.getProperty("subprotocol"));



      SequenceDescriptor sequenceDescriptor =
        new SequenceDescriptor(jdbcConnectionDescriptor);
      sequenceDescriptor.setSequenceManagerClass(
        Class.forName(settingsProperties.getProperty("sequenceManagerClass")));
      sequenceDescriptor.setConfigurationProperties(new Properties());
      jdbcConnectionDescriptor.setSequenceDescriptor(sequenceDescriptor);


      ConnectionPoolDescriptor connectionPoolDescriptor =
        new ConnectionPoolDescriptor();
      connectionPoolDescriptor.setMaxActive(
        Integer.parseInt(
          settingsProperties.getProperty("maxActivePersistenceBrokers")));
      connectionPoolDescriptor.setValidationQuery(
        settingsProperties.getProperty("validationQuery"));
      connectionPoolDescriptor.setTestOnBorrow(
        "true".equalsIgnoreCase(settingsProperties.getProperty("testOnBorrow")));
      connectionPoolDescriptor.setTestWhileIdle(
        "true".equalsIgnoreCase(
          settingsProperties.getProperty("testWhileIdle")));
      jdbcConnectionDescriptor.setConnectionPoolDescriptor(
        connectionPoolDescriptor);


			MetadataManager mm = MetadataManager.getInstance();
			if (mm != null){
			  ConnectionRepository connectionRepository =
					mm.connectionRepository();
					log.debug("connection repository object value: " + connectionRepository);
					connectionRepository.addDescriptor(jdbcConnectionDescriptor);
			}
			else{
				throw new RuntimeException("Error is MetadataManager is null");
			}


//      MetdataManager m = MetadataManager.getInstance();
//
//      if (m != null){
//        ConnectionRepository connectionRepository =
//        m.connectionRepository();
//      }
//
//      connectionRepository.addDescriptor(jdbcConnectionDescriptor);

      DescriptorRepository descriptorRepostitory =
        MetadataManager.getInstance().readDescriptorRepository(
          settingsProperties.getProperty("repositoryXmlFilePath"));
      MetadataManager.getInstance().mergeDescriptorRepository(
        descriptorRepostitory);


      //MetadataManager.getInstance().setDescriptor(descriptorRepostitory, true);
      PersistenceBrokerFactory.setDefaultKey(
        new PBKey(
          settingsProperties.getProperty("jcd-alias"),
          securityProperties.getProperty("username"),
          securityProperties.getProperty("password")));
      log.info("started: " + settingsProperties.getProperty("jcd-alias"));
    }
    catch(Exception e)
    {
      log.error(e.getMessage(), e);
      throw new IllegalStateException(e.getMessage());
    }
  }
}
