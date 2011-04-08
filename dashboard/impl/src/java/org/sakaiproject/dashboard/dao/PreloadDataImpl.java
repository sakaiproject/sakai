/******************************************************************************
 * PreloadDataImpl.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.dao;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.dashboard.dao.DashboardDao;
import org.sakaiproject.dashboard.model.DashboardItem;

/**
 * This checks and preloads any data that is needed for this app
 * @author Sakai App Builder -AZ
 */
public class PreloadDataImpl {

	private static Log log = LogFactory.getLog(PreloadDataImpl.class);

	private DashboardDao dao;
	public void setDao(DashboardDao dao) {
		this.dao = dao;
	}

	public void init() {
		preloadItems();
	}

	/**
	 * Preload some items into the database
	 */
	public void preloadItems() {

		// check if there are any items present, load some if not
		if(dao.findAll(DashboardItem.class).isEmpty()){

			// use the dao to preload some data here
			dao.save( new DashboardItem("Preload Title", 0, "Preload Description", "preload-entityId",null,"preload-accessUrl","preload-locationId","preload-locationUrl",
					"preload-locationName", "preload-status",new Date(),  "preload-creatorId", "Preload Owner") );
			
			log.info("Preloaded " + dao.countAll(DashboardItem.class) + " items");
		}
	}
}
