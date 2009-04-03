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

public class TestGradebookHelper extends TestCase {
  private GradebookHelper gradebookHelper = null;
  private boolean isIntegrated;

  public TestGradebookHelper(GradebookHelper gradebookHelper,
                             boolean integrated) {
    this.gradebookHelper = gradebookHelper;
    this.isIntegrated = integrated;
    assertNotNull(gradebookHelper);
  }

  protected void setUp() throws Exception {
    super.setUp();
  }

  protected void tearDown() throws Exception {
    gradebookHelper = null;
    super.tearDown();
  }

  public void testGetDefaultGradebookUId() {
    assertNotNull(gradebookHelper);
    String actualReturn = gradebookHelper.getDefaultGradebookUId();
    assertNotNull(actualReturn);
    String expectedReturn = null;
    if (isStandalone())
    {
      expectedReturn = "QA_8";
    }
    if (this.isIntegrated())
    {
      expectedReturn = "Test Gradebook #1";
    }
      assertEquals("return value", expectedReturn, actualReturn);
  }

  // in unti test, we can't find some components yet, just test we can hit this.
  public void testGetGradebookUId() {
    String actualReturn = gradebookHelper.getGradebookUId();
    String expectedReturn = null;
    assertEquals("return value", expectedReturn, actualReturn);
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
