/******************************************************************************
 * DashboardLogic.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.sakaiproject.dashboard.model.DashboardItem;

/**
 * This is the interface for the app Logic, 
 * @author Sakai App Builder -AZ
 */
public interface DashboardLogic {

   /**
    * This returns an item based on an id
    * @param id the id of the item to fetch
    * @return a DashboardItem or null if none found
    */
   public DashboardItem getItemById(Long id);

   /**
    * Check if a specified user can write this item in a specified site
    * @param item to be modified or removed
    * @param locationId a unique id which represents the current location of the user (entity reference)
    * @param userId the internal user id (not username)
    * @return true if item can be modified, false otherwise
    */
   public boolean canWriteItem(DashboardItem item, String locationId, String userId);

   /**
    * This returns a List of items for a specified site that are
    * visible to the specified user
    * @param locationId a unique id which represents the current location of the user (entity reference)
    * @param userId the internal user id (not username)
    * @return a List of DashboardItem objects
    */
   public List<DashboardItem> getAllVisibleItems(String locationId, String userId);

   /**
    * Save (Create or Update) an item (uses the current site)
    * @param item the DashboardItem to create or update
    */
   public void saveItem(DashboardItem item);

   /**
    * Remove an item
    * @param item the DashboardItem to remove
    */
   public void removeItem(DashboardItem item);

   public void postDashboardItems(String eventType, Date eventTime,
		String contextId, String entityRef);

}
