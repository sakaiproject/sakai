/**
 * $Id: AppNameEntityProvider.java 61603 2009-07-03 14:18:25Z aaronz@vt.edu DashboardEntityProvider.java 48619 2008-05-03 18:59:16Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/contrib/programmerscafe/appbuilder/trunk/templates/crud/entitybroker/AppNameEntityProvider.java $
 * DashboardEntityProvider.java - Dashboard - Apr 20, 2008 5:13:25 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 */

package org.sakaiproject.dashboard.logic.entity;

import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;

import org.sakaiproject.dashboard.logic.DashboardLogic;
import org.sakaiproject.dashboard.model.DashboardItem;

/**
 * Sample Dashboard provider
 * @author Sakai App Builder -AZ
 */
public class DashboardEntityProvider implements RESTful, AutoRegisterEntityProvider {

   private DashboardLogic logic;
   public void setLogic(DashboardLogic logic) {
      this.logic = logic;
   }

   private DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
      this.developerHelperService = developerHelperService;
   }

   public static String PREFIX = "dashboard";
   public String getEntityPrefix() {
      return PREFIX;
   }

   public String[] getHandledOutputFormats() {
      return new String[] {Formats.XML, Formats.JSON};
   }

   public String[] getHandledInputFormats() {
      return new String[] {Formats.HTML, Formats.XML, Formats.JSON};
   }

   public Object getEntity(EntityReference ref) {
      if (ref.getId() == null) {
         return new DashboardItem();
      }
      DashboardItem entity = logic.getItemById( new Long(ref.getId()) );
      if (entity != null) {
         return entity;
      }
      throw new IllegalArgumentException("Invalid id:" + ref.getId());
   }

   public List<?> getEntities(EntityReference ref, Search search) {
      String locationId = null;
      if (search != null) {
         Restriction restriction = search.getRestrictionByProperty("locationId");
         if (restriction != null) {
            locationId = restriction.property;
         }
      }
      if (locationId == null) {
         String locRef = developerHelperService.getCurrentLocationReference();
         if (locRef != null) {
            locationId = developerHelperService.getLocationIdFromRef(locRef);
         }
      }
      return logic.getAllVisibleItems(locationId, getCurrentUser());
   }

   public String createEntity(EntityReference ref, Object entity) {
      return createEntity(ref, entity, null);
   }
   
   public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
	   DashboardItem item = (DashboardItem) entity;
	   item.setCreatorId(getCurrentUser());
	   logic.saveItem(item);
	   return item.getId().toString();
   }


   public Object getSampleEntity() {
      return new DashboardItem();
   }

   public void updateEntity(EntityReference ref, Object entity) {
      updateEntity(ref, entity, null);
   }
   
   public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
	  DashboardItem item = (DashboardItem) entity;
	  DashboardItem current = logic.getItemById( new Long(ref.getId()) );
	  if (current == null) {
		  throw new IllegalArgumentException("Could not locate entity to update");
	  }
	  checkAllowed(current);
	  developerHelperService.copyBean(item, current, 0, new String[] {"id", "type", "creatorName", "createdTime", "creatorId"}, true);
	  logic.saveItem(current);
   }

   public void deleteEntity(EntityReference ref) {
	   deleteEntity(ref, null);
   }
   
   public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	   DashboardItem current = logic.getItemById( new Long(ref.getId()) );
	   if (current == null) {
		   throw new IllegalArgumentException("Could not locate entity to remove");
	   }
	   checkAllowed(current);
	   logic.removeItem(current);
   }

   private void checkAllowed(DashboardItem current) {
      if (! logic.canWriteItem(current, current.getLocationId(), getCurrentUser())) {
         throw new SecurityException("Only the owner can remove this entity: " + current.getCreatorId());
      }
   }

   private String getCurrentUser() {
      String userRef = developerHelperService.getCurrentUserReference();
      if (userRef == null) {
         throw new SecurityException("Must be logged in to create/update/delete entities");
      }
      return developerHelperService.getUserIdFromRef(userRef);
   }
   

}
