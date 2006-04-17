/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.test.integration.helper.ifc;

import junit.framework.*;
import org.sakaiproject.tool.assessment.integration.helper.ifc.*;
import org.sakaiproject.service.framework.log.*;
import org.sakaiproject.site.*;

import java.util.*;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class TestPublishingTargetHelper extends TestCase {
  private PublishingTargetHelper publishingTargetHelper = null;
  private boolean isIntegrated;

  public TestPublishingTargetHelper(PublishingTargetHelper helper,
                             boolean integrated) {
    this.publishingTargetHelper = helper;
    this.isIntegrated = integrated;
    assertNotNull(publishingTargetHelper);

  }

  protected void setUp() throws Exception {
    super.setUp();
    /**@todo verify the constructors*/
    publishingTargetHelper = null;
  }

  protected void tearDown() throws Exception {
    publishingTargetHelper = null;
    super.tearDown();
  }

  public void testGetLog() {
    Logger actualReturn = null;
    boolean unimplemented = false;
    try
    {
      actualReturn = publishingTargetHelper.getLog();
    }
    catch (NoSuchBeanDefinitionException nb)
    {
      unimplemented = false; // we hit service to lookup bean in unit test

    }
    catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertFalse(unimplemented);
    }
  }

  public void testGetSiteService() {
    SiteService actualReturn = null;
    boolean unimplemented = false;
    try
    {
      actualReturn = publishingTargetHelper.getSiteService();
    }
    catch (NoSuchBeanDefinitionException nb)
    {
      unimplemented = false; // we hit service to lookup bean in unit test

    }
    catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertFalse(unimplemented);
    }
  }

  public void testGetTargets() {
    HashMap actualReturn = publishingTargetHelper.getTargets();
    int actualSize = actualReturn.size();
    this.assertFalse(actualSize==0);
  }

  public void testSetLog() {
    Logger log = null;

    boolean unimplemented = false;
    boolean success = true;
    try
    {
      publishingTargetHelper.setLog(log);
    }
    catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }
    catch (Exception ex2)
    {
      success = false;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertTrue(success);
    }

  }

  public void testSetSiteService() {
    SiteService siteService = null;

    boolean unimplemented = false;
    boolean success = true;
    try
    {
      publishingTargetHelper.setSiteService(siteService);
    }
    catch (java.lang.UnsupportedOperationException ex)
    {
      unimplemented = true;
    }
    catch (Exception ex2)
    {
      success = false;
    }

    if (isStandalone())
    {
      assertTrue(unimplemented);
    }
    if (this.isIntegrated())
    {
      assertTrue(success);
    }



  }
  public boolean isIntegrated()
  {
    return isIntegrated;
  }
  public boolean isStandalone()
  {
    return !isIntegrated;
  }
}
