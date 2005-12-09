/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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
import junit.framework.TestResult;

import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.component.kernel.component.SpringCompMgr;
import org.sakaiproject.util.ComponentsLoader;
import org.sakaiproject.util.PropertyOverrideConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MessageForumsTests2 extends TestCase
{
  private static final String[] LOCAL_COMPONENTS_XML = {"components-test.xml"};

  private ApplicationContext context;
  private org.sakaiproject.api.kernel.component.ComponentManager componentManager;
  
  
  private MessageForumsForumManager forumManager;

  private MessageForumsMessageManager messageManager;

  private AreaManager areaManager;
  
  private MessageForumsTypeManager typeManager;

  private void init() {

  }

  public TestResult run()
  {
    // TODO Auto-generated method stub
    return super.run();
  }

  public void testThis(){
    assertTrue(true);
  }

  protected void setUp() throws Exception
  {
    // TODO Auto-generated method stub
    super.setUp();
    
    
    
    String baseDir = System.getProperty("basedir");
    
    System.setProperty("sakai.components.root", "c:\\tomcat5\\components");
                        
    componentManager = new SpringCompMgr(null);
    ((SpringCompMgr) componentManager).init();
    
    //ConfigurableApplicationContext ac = new ClassPathXmlApplicationContext();
    
        
    //ac.
    
    
    //ComponentsLoader loader = new ComponentsLoader();
    //loader.load(componentManager, baseDir + "/common/common-components/src/");
    //loader.load(componentManager, baseDir + "/kernel/db-components/src/");
    //loader.load(componentManager, baseDir + "/legacy/legacy-components/src/");
    //loader.load(componentManager, baseDir + "/messageforums/messageforums-component-shared/test/");
    
    //componentManager.set
    
                
    forumManager = (MessageForumsForumManager) componentManager.get(MessageForumsForumManager.class.getName());
    messageManager = (MessageForumsMessageManager) componentManager.get(MessageForumsMessageManager.class.getName());
    areaManager = (AreaManager) componentManager.get(AreaManager.class.getName());
    typeManager = new MessageForumsTypeManagerImpl();
    
    System.out.println("klj");
    
    
  }
  
}

/**********************************************************************************
*
* $Header$
*
**********************************************************************************/