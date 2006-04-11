package org.sakaiproject.tool.assessment.test.integration.context.spring;

import junit.framework.*;
import org.sakaiproject.tool.assessment.integration.context.spring.*;
import org.sakaiproject.tool.assessment.integration.context.*;
import org.springframework.test.AbstractTransactionalSpringContextTests;

public class TestFactoryUtil extends AbstractTransactionalSpringContextTests {
  private FactoryUtil factoryUtil = null;

  public TestFactoryUtil(String name) {
  }

  protected void onSetUpInTransaction() throws Exception {
  }

  protected String[] getConfigLocations() {

      String[] configLocations = {"org/sakaiproject/spring/applicationContext.xml"};

      return configLocations;
  }


  public void testLookup() throws Exception {
    IntegrationContextFactory actualReturn = factoryUtil.lookup();
    assertNotNull(actualReturn);
  }

  public static void main(String[] args)
  {
    System.out.println("starting.");
    TestFactoryUtil test = new TestFactoryUtil("test");
    try
    {
      test.setUp();
      test.testLookup();
      test.tearDown();
    }
    catch (Exception ex)
    {
      System.out.println("ex="+ex);
    }
    System.out.println("done.");
  }

}
