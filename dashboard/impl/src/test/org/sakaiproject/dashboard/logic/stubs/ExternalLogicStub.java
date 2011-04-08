/******************************************************************************
 * ExternalLogicStub.java - created by Sakai App Builder -AZ
 * 
 * Copyright (c) 2006 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.dashboard.logic.stubs;

import org.sakaiproject.dashboard.logic.ExternalLogic;
import org.sakaiproject.dashboard.logic.FakeDataPreload;

/**
 * Stub class for the external logic impl (for testing)
 * @author Sakai App Builder -AZ
 */
public class ExternalLogicStub implements ExternalLogic {

    /**
     * represents the current user userId, can be changed to simulate multiple users 
     */
    public String currentUserId;
    /**
     * represents the current location, can be changed to simulate multiple locations 
     */
    public String currentLocationId;

    /**
     * Reset the current user and location to defaults
     */
    public void setDefaults() {
        currentUserId = FakeDataPreload.USER_ID;
        currentLocationId = FakeDataPreload.LOCATION1_ID;
    }

    public ExternalLogicStub() {
        setDefaults();
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.dashboard.logic.ExternalLogic#getCurrentLocationId()
     */
    public String getCurrentLocationId() {
        return currentLocationId;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.dashboard.logic.ExternalLogic#getLocationTitle(java.lang.String)
     */
    public String getLocationTitle(String locationId) {
        if (locationId.equals(FakeDataPreload.LOCATION1_ID)) {
            return FakeDataPreload.LOCATION1_TITLE;
        } else if (locationId.equals(FakeDataPreload.LOCATION2_ID)) {
            return FakeDataPreload.LOCATION2_TITLE;
        }
        return "--------";
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.dashboard.logic.ExternalLogic#getCurrentUserId()
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.dashboard.logic.ExternalLogic#getUserDisplayName(java.lang.String)
     */
    public String getUserDisplayName(String userId) {
        if (userId.equals(FakeDataPreload.USER_ID)) {
            return FakeDataPreload.USER_DISPLAY;
        } else if (userId.equals(FakeDataPreload.ACCESS_USER_ID)) {
            return FakeDataPreload.ACCESS_USER_DISPLAY;
        } else if (userId.equals(FakeDataPreload.MAINT_USER_ID)) {
            return FakeDataPreload.MAINT_USER_DISPLAY;
        } else if (userId.equals(FakeDataPreload.ADMIN_USER_ID)) {
            return FakeDataPreload.ADMIN_USER_DISPLAY;
        }
        return "----------";
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.dashboard.logic.ExternalLogic#isUserAdmin(java.lang.String)
     */
    public boolean isUserAdmin(String userId) {
        if (userId.equals(FakeDataPreload.ADMIN_USER_ID)) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.dashboard.logic.ExternalLogic#isUserAllowedInLocation(java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean isUserAllowedInLocation(String userId, String permission, String locationId) {
        if (userId.equals(FakeDataPreload.USER_ID)) {
            if (locationId.equals(FakeDataPreload.LOCATION1_ID)) {
                return false;
            }
        } else if (userId.equals(FakeDataPreload.ACCESS_USER_ID)) {
            if (locationId.equals(FakeDataPreload.LOCATION1_ID)) {
                return false;
            }
        } else if (userId.equals(FakeDataPreload.MAINT_USER_ID)) {
            if (locationId.equals(FakeDataPreload.LOCATION1_ID)) {
                if (permission.equals(ITEM_WRITE_ANY) ||
                        permission.equals(ITEM_READ_HIDDEN) ) {
                    return true;
                }
            }
        } else if (userId.equals(FakeDataPreload.ADMIN_USER_ID)) {
            // admin can do anything in any context
            return true;
        }
        return false;
    }

}
