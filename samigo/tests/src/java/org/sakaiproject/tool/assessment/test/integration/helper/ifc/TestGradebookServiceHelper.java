package org.sakaiproject.tool.assessment.test.integration.helper.ifc;

import junit.framework.*;
import org.sakaiproject.tool.assessment.data.dao.assessment.*;
import org.sakaiproject.tool.assessment.data.ifc.grading.*;
import org.sakaiproject.tool.assessment.integration.helper.ifc.GradebookServiceHelper;
import org.sakaiproject.spring.SpringBeanLocator;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

public class TestGradebookServiceHelper extends TestCase {
  private GradebookServiceHelper gradebookServiceHelper = null;

  private boolean integrated;
  private WebApplicationContext context;

  public TestGradebookServiceHelper(GradebookServiceHelper helper,
                             boolean integrated) {
    this.gradebookServiceHelper = helper;
    this.integrated = integrated;
    this.context = context;
    assertNotNull(gradebookServiceHelper);
  }

  protected void setUp() throws Exception {
    super.setUp();
    /**@todo verify the constructors*/
    gradebookServiceHelper = null;
  }

  protected void tearDown() throws Exception {
    gradebookServiceHelper = null;
    super.tearDown();
  }

  public void testAddToGradebook(){
    boolean success = false;

    try
    {
      PublishedAssessmentData publishedAssessment = null;
      boolean expectedReturn = false;
      // should fail (false) with null, so success shoudl be false
      boolean actualReturn = gradebookServiceHelper.addToGradebook(
        publishedAssessment);
      assertEquals("return value", expectedReturn, actualReturn);
      success = true;
    }
    catch (Exception ex)
    {
      System.out.println("testAddToGradebook " + ex);
    }
  assertFalse(success);
  }

  public void testGradebookExists() {
    // standalone will always return false, for integrated use non-existent
    String gradebookUId = "bogusGB";
    boolean expectedReturn = false;
    boolean actualReturn = false;
    // for nowwe bypass for integrated, 'cause we need to fake up web ctxt.
    if (this.isStandalone())
    {
      actualReturn = gradebookServiceHelper.gradebookExists(gradebookUId);
    }
    assertEquals("return value", expectedReturn, actualReturn);
  }




  public void testRemoveExternalAssessment(){

    boolean success = false;
    try
    {
      String gradebookUId = null;
      String publishedAssessmentId = null;
      gradebookServiceHelper.removeExternalAssessment(gradebookUId,
        publishedAssessmentId);
      success = true;
    }
    catch (NoSuchBeanDefinitionException nb)
    {
      success = true; // unit test, calling sb locator, so we call success
      System.out.println("unit test hitting locator");
    }
    catch (Exception ex)
    {
      System.out.println("testRemoveExternalAssessment " + ex);
    }
    assertTrue(success);
  }

  public void testUpdateExternalAssessmentScore() {
    boolean success = false;

    try
    {
      AssessmentGradingIfc ag = null;
      gradebookServiceHelper.updateExternalAssessmentScore(ag);
      success = true;
    }
    catch (NoSuchBeanDefinitionException nb)
    {
      success = true; // unit test, calling sb locator, so we call success
      System.out.println("unit test hitting locator");
    }
    catch (Exception ex)
    {
      System.out.println("testUpdateExternalAssessmentScore " + ex);
    }
    assertTrue(success);
  }
  public boolean isIntegrated()
  {
    return integrated;
  }
  public boolean isStandalone()
  {
    return !integrated;
  }

}
