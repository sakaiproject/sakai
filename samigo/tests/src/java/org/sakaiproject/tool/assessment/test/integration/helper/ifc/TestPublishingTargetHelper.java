/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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
