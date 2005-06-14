/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

package org.sakaiproject.tool.assessment.settings;

import java.util.Properties;

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
 * @version $Id: OjbBootStrap.java,v 1.1 2005/04/12 15:37:02 daisyf.stanford.edu Exp $
 */
public class OjbBootStrap
{
  private static final org.apache.log4j.Logger LOG =
    org.apache.log4j.Logger.getLogger(OjbBootStrap.class);

  /**
   * Creates a new OjbBootStrap object.
   */
  public OjbBootStrap()
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("new OjbBootStrap()");
    }
  }

  /**
   * DOCUMENTATION PENDING
   *
   * @throws IllegalStateException DOCUMENTATION PENDING
   */
  public void bootstrap()
  {
    LOG.debug("bootstrap()");
    try
    {
      PathInfo path = PathInfo.getInstance();
      if(
        (path.getBasePathToSecurity() == null) ||
          (path.getBasePathToSettings() == null) ||
          (path.getPathToSecurity() == null) ||
          (path.getPathToSettings() == null))
      {
        LOG.fatal("PathInfo not initialized properly");
        throw new IllegalStateException("PathInfo not initialized properly");
      }

      Properties securityProperties =
        path.getSecurityProperties("SAM.properties");
      Properties settingsProperties =
        path.getSettingsProperties("SAM.properties");
      if((securityProperties == null) || (settingsProperties == null))
      {
        LOG.fatal("Could not open Properties");
        throw new IllegalStateException("Could not open Properties");
      }

      if(
        ! securityProperties.containsKey("username") ||
          ! securityProperties.containsKey("password"))
      {
        LOG.fatal("Properties do not contain username or password");
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
        LOG.fatal("Missing one or more required keys");
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
					LOG.debug("connection repository object value: " + connectionRepository);
					connectionRepository.addDescriptor(jdbcConnectionDescriptor);				
			}
			else{
				throw new Error("Error is MetadataManager is null");
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
      LOG.info("started: " + settingsProperties.getProperty("jcd-alias"));
    }
    catch(Exception e)
    {
      LOG.error(e.getMessage(), e);
      throw new IllegalStateException(e.getMessage());
    }
  }
}
