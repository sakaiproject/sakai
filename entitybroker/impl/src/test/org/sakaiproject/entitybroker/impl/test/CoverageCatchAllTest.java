/**
 * CoverageCatchAllTest.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.impl.test;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.impl.test.mocks.FakeEvent;
import org.sakaiproject.entitybroker.impl.test.mocks.FakeServerConfigurationService;

/**
 * this test class is simply here to make it easier to tell what REAL methods we have missed in the
 * test coverage reports by running all the getters and setters on classes so that the test coverage
 * systems will not report these getters and setters as untested and therefore throw off the tested
 * code percentage
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CoverageCatchAllTest extends TestCase {

   public void testFakeServerConfigurationService() {
      // this really sucks -AZ
      FakeServerConfigurationService fscs = new FakeServerConfigurationService();
      fscs.getAccessUrl();
      fscs.getAccessPath();
      fscs.getBoolean(null, false);
      fscs.getDefaultTools(null);
      fscs.getGatewaySiteId();
      fscs.getHelpUrl(null);
      fscs.getInt(null, 0);
      fscs.getLoggedOutUrl();
      fscs.getPortalUrl();
      fscs.getSakaiHomePath();
      fscs.getServerId();
      fscs.getServerIdInstance();
      fscs.getServerInstance();
      fscs.getServerName();
      fscs.getServerUrl();
      fscs.getString(null);
      fscs.getString(null, null);
      fscs.getStrings(null);
      fscs.getToolCategories(null);
      fscs.getToolCategoriesAsMap(null);
      fscs.getToolToCategoryMap(null);
      fscs.getToolOrder(null);
      fscs.getToolsRequired(null);
      fscs.getToolUrl();
      fscs.getUserHomeUrl();
   }

   public void testFakeEvent() {
      // this really sucks -AZ
      FakeEvent event1 = new FakeEvent(null, false, 0, null); // test constructor
      assertNotNull(event1);
      FakeEvent event = new FakeEvent();
      event.getEvent();
      event.getModify();
      event.getPriority();
      event.getResource();
      event.getSessionId();
      event.getUserId();
   }

}
