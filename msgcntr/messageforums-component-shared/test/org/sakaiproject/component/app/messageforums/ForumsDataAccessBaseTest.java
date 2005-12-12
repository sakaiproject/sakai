/**********************************************************************************
 * $URL$
 * $Id$
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.messageforums;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessageForumsUserManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.kernel.component.ComponentManager;
import org.sakaiproject.component.kernel.component.SpringCompMgr;

/**
 * Base unit test for data access components.
 * Register components from deployed tomcat components.  
 * Note: use /usr/local/sakai for sakai.properteis
 * 
 * @version $Id$
 */
public class ForumsDataAccessBaseTest extends TestCase {
        
  private static final Log LOG = LogFactory.getLog(ForumsDataAccessBaseTest.class);  
             
  private static String SAKAI_HOME = System.getProperty("sakaiproject.basedir");
  private static String TOMCAT_HOME = System.getProperty("TOMCAT_HOME");
  private static ComponentManager componentManager;
    
  protected static MessageForumsForumManager forumManager;
  protected static MessageForumsMessageManager messageManager;
  protected static AreaManager areaManager;    
  protected static MessageForumsTypeManager typeManager;
  protected static PrivateMessageManager privateMessageManager;
  protected static MessageForumsUserManager userManager;
  
  static{
    
    BasicConfigurator.configure();
    
    /** Set sakai.components.root property .. used by SpringCompMrg */
    System.setProperty("sakai.components.root", TOMCAT_HOME + "/components");
          
    componentManager = new SpringCompMgr(null);
    ((SpringCompMgr) componentManager).init();
    
    forumManager = (MessageForumsForumManager) componentManager.get(MessageForumsForumManager.class.getName());
    messageManager = (MessageForumsMessageManager) componentManager.get(MessageForumsMessageManager.class.getName());
    areaManager = (AreaManager) componentManager.get(AreaManager.class.getName());
    typeManager = (MessageForumsTypeManager) componentManager.get(MessageForumsTypeManager.class.getName());
    privateMessageManager = (PrivateMessageManager) componentManager.get(PrivateMessageManager.class.getName());
    userManager = (MessageForumsUserManager) componentManager.get(MessageForumsUserManager.class.getName());
    
  }
      
    public ForumsDataAccessBaseTest() {
        super();
        init();
    }

    public ForumsDataAccessBaseTest(String name) {
        super(name);
        init();
    }

    private void init() {                                          
      
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
                        
        super.setUp();
        //SessionFactory sessionFactory = (SessionFactory) getApplicationContext().getBean("org.springframework.orm.hibernate.SessionFactory");
        //Session s = sessionFactory.openSession();
        //TransactionSynchronizationManager.bindResource(sessionFactory, new SessionHolder(s));
        
        
        
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
//        SessionFactory sessionFactory = (SessionFactory) getApplicationContext().getBean("org.springframework.orm.hibernate.SessionFactory");
//        SessionHolder holder = (SessionHolder) TransactionSynchronizationManager.getResource(sessionFactory);
//        Session s = holder.getSession();
//        //s.flush();
//        TransactionSynchronizationManager.unbindResource(sessionFactory);
//        SessionFactoryUtils.closeSessionIfNecessary(s, sessionFactory);
    }
}