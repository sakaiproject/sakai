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
