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
