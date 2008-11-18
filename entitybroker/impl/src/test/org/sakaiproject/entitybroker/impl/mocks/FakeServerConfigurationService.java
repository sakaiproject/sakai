/**
 * FakeServerConfigurationService.java - created by aaronz on Jul 25, 2007
 */

package org.sakaiproject.entitybroker.impl.mocks;

import java.util.List;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.mocks.data.TestData;

/**
 * Do NOT use this for anything other than internal testing of EB, Only getServerUrl does anything
 * and it only return a fake string
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@SuppressWarnings("unchecked")
public class FakeServerConfigurationService implements ServerConfigurationService {

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getAccessUrl()
    */
   public String getAccessUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getBoolean(java.lang.String,
    *      boolean)
    */
   public boolean getBoolean(String name, boolean dflt) {
      // TODO Auto-generated method stub
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getDefaultTools(java.lang.String)
    */
   public List getDefaultTools(String category) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getGatewaySiteId()
    */
   public String getGatewaySiteId() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getHelpUrl(java.lang.String)
    */
   public String getHelpUrl(String helpContext) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getInt(java.lang.String, int)
    */
   public int getInt(String name, int dflt) {
      // TODO Auto-generated method stub
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getLoggedOutUrl()
    */
   public String getLoggedOutUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getPortalUrl()
    */
   public String getPortalUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getSakaiHomePath()
    */
   public String getSakaiHomePath() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getServerId()
    */
   public String getServerId() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getServerIdInstance()
    */
   public String getServerIdInstance() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getServerInstance()
    */
   public String getServerInstance() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getServerName()
    */
   public String getServerName() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getServerUrl()
    */
   public String getServerUrl() {
      return TestData.SERVER_URL;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getRawProperty(java.lang.String)
    */
   public String getRawProperty(String name) {
      // TODO Auto-generated method stub
      return null;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String)
    */
   public String getString(String name) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getString(java.lang.String,
    *      java.lang.String)
    */
   public String getString(String name, String dflt) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getStrings(java.lang.String)
    */
   public String[] getStrings(String name) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getToolOrder(java.lang.String)
    */
   public List getToolOrder(String category) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getToolUrl()
    */
   public String getToolUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getToolsRequired(java.lang.String)
    */
   public List getToolsRequired(String category) {
      // TODO Auto-generated method stub
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.sakaiproject.component.api.ServerConfigurationService#getUserHomeUrl()
    */
   public String getUserHomeUrl() {
      // TODO Auto-generated method stub
      return null;
   }

   public String getAccessPath() {
      // TODO Auto-generated method stub
      return null;
   }

   public List<String> getToolCategories(String category) {
      // TODO Auto-generated method stub
      return null;
   }

   public Map<String, List<String>> getToolCategoriesAsMap(String category) {
      // TODO Auto-generated method stub
      return null;
   }

   public Map<String, String> getToolToCategoryMap(String category) {
      // TODO Auto-generated method stub
      return null;
   }

}
