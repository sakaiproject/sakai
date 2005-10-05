package org.sakaiproject.tool.assessment.test.integration.context;

import org.springframework.test.*;

import org.sakaiproject.tool.assessment.integration.context.*;
//import org.sakaiproject.tool.assessment.integration.helper.ifc.*;
import org.sakaiproject.tool.assessment.test.integration.helper.ifc.
  TestAgentHelper;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.sakaiproject.spring.SpringBeanLocator;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TestIntegrationContextFactory extends
  AbstractTransactionalSpringContextTests
{
  private IntegrationContextFactory integrationContextFactory;


  public TestIntegrationContextFactory(String name)
  {
  }

  protected void onSetUpInTransaction() throws Exception
  {
  }

  protected String[] getConfigLocations()
  {

    String[] configLocations =
      {
      "org/sakaiproject/spring/integrationContext.xml",
//      "org/sakaiproject/spring/applicationContext.xml",
      "org/sakaiproject/spring/BeanDefinitions.xml",
//      "org/sakaiproject/config/shared_components.xml",
      //gb
//      "org/sakaiproject/spring/spring-beans-test.xml",
//      "org/sakaiproject/spring/spring-hib-test.xml",
    };

    return configLocations;
  }

  public void testGetInstance()
  {
    IntegrationContextFactory actualReturn = integrationContextFactory.
      getInstance();
    integrationContextFactory = actualReturn;
    assertNotNull(actualReturn);
    System.out.println("testGetInstance=" + actualReturn);
  }

  public void testMethods()
  {
    System.out.println("testMethods");
    // first, we make sure that SpringBeanLocator can handle non-web context
    int configLength = getConfigLocations().length;
    String[] fileConfigs = new String[configLength];
    for (int i = 0; i < configLength; i++)
    {
      fileConfigs[i] = "**/" + getConfigLocations()[i];
    }
    FileSystemXmlApplicationContext context =
      new FileSystemXmlApplicationContext(fileConfigs);
//      this.getContext(integrationContextFactory);
    SpringBeanLocator.getInstance().setConfigurableApplicationContext(context);
    TestIntCtxtFactoryMethods testMethods = new TestIntCtxtFactoryMethods(
      integrationContextFactory);
    System.out.println("testIsIntegrated");
    testMethods.testIsIntegrated();
    System.out.println("testGetAgentHelper");
    testMethods.testGetAgentHelper();
//    System.out.println("testGetAuthzHelper");
//    testMethods.testGetAuthzHelper();
    System.out.println("testGetGradebookHelper");
    testMethods.testGetGradebookHelper();
    System.out.println("testGetGradeBookServiceHelper");
    testMethods.testGetGradeBookServiceHelper();
    System.out.println("testGetPublishingTargetHelper");
    testMethods.testGetPublishingTargetHelper();
  }

  public static void main(String[] args)
  {
    System.out.println("starting.");
    TestIntegrationContextFactory test = new TestIntegrationContextFactory(
      "Test Integration Context Factory");
    try
    {
      test.setUp(); // we test instance next, so we don't do it in setUp()
      test.testGetInstance(); //must be run first
      test.testMethods();
      test.tearDown();
    }
    catch (Exception ex)
    {
      System.out.println("ex=" + ex);
      ex.printStackTrace();
    }
    System.out.println("done.");
  }

}
