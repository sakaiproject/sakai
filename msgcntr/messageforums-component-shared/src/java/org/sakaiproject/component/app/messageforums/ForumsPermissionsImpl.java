/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.messageforums;

import org.sakaiproject.api.app.messageforums.ForumsPermissions;
import org.sakaiproject.api.kernel.function.cover.FunctionManager;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.site.cover.SiteService;

public class ForumsPermissionsImpl implements ForumsPermissions {
    
    // Control Permissions
    public static final String PERMISSION_CP_NEW_FORUM = "messageforums.cp.newForum";
    public static final String PERMISSION_CP_NEW_TOPIC = "messageforums.cp.newTopic";
    public static final String PERMISSION_CP_NEW_RESPONSE = "messageforums.cp.newResponse";
    public static final String PERMISSION_CP_RESPONSE_TO_RESPONSE = "messageforums.cp.responseToResponse";
    public static final String PERMISSION_CP_MOVE_POSTINGS = "messageforums.cp.movePostings";
    public static final String PERMISSION_CP_CHANGE_SETTINGS = "messageforums.cp.changeSettings";

    // Message Permissions
    public static final String PERMISSION_MP_READ = "messageforums.mp.read";
    public static final String PERMISSION_MP_REVISE_ANY = "messageforums.mp.reviseAny";
    public static final String PERMISSION_MP_REVISE_OWN = "messageforums.mp.reviseOwn";
    public static final String PERMISSION_MP_DELETE_ANY = "messageforums.mp.deleteAny";
    public static final String PERMISSION_MP_DELETE_OWN = "messageforums.mp.deleteOwn";
    public static final String PERMISSION_MP_READ_DRAFTS = "messageforums.mp.readDrafts";
   
    /**
     * Init the app specific functions for the message forums tool.  This
     * applies mainly ControlPermissions, MessagePermissions, and ActorPermissions.
     */
    public void init() {
        FunctionManager.registerFunction(PERMISSION_CP_NEW_FORUM);
        FunctionManager.registerFunction(PERMISSION_CP_NEW_TOPIC);
        FunctionManager.registerFunction(PERMISSION_CP_NEW_RESPONSE);
        FunctionManager.registerFunction(PERMISSION_CP_RESPONSE_TO_RESPONSE);
        FunctionManager.registerFunction(PERMISSION_CP_MOVE_POSTINGS);
        FunctionManager.registerFunction(PERMISSION_CP_CHANGE_SETTINGS);
        FunctionManager.registerFunction(PERMISSION_MP_READ);
        FunctionManager.registerFunction(PERMISSION_MP_REVISE_ANY);
        FunctionManager.registerFunction(PERMISSION_MP_REVISE_OWN);
        FunctionManager.registerFunction(PERMISSION_MP_DELETE_ANY);
        FunctionManager.registerFunction(PERMISSION_MP_DELETE_OWN);
        FunctionManager.registerFunction(PERMISSION_MP_READ_DRAFTS);
    }

    // Actor Permissions  
    // TODO: Not sure where this data comes from
    public boolean isUserAccessor() {
        return false;
    }
    
    public boolean isUserContributor() {
        return false;
    }

    public boolean isUserModerator() {
        return false;
    }
    
    // Control Permissions
    public boolean isUserAbleToPostNewTopic(String uuid) {
        return (hasPermission(uuid, PERMISSION_CP_NEW_TOPIC));
    }

    public boolean isUserAbleToPostNewResponse(String uuid) {
        return (hasPermission(uuid, PERMISSION_CP_NEW_RESPONSE));
    }

    public boolean isUserAbleToPostResponseToResponse(String uuid) {
        return (hasPermission(uuid, PERMISSION_CP_RESPONSE_TO_RESPONSE));
    }

    public boolean isUserAbleToMovePostings(String uuid) {
        return (hasPermission(uuid, PERMISSION_CP_MOVE_POSTINGS));
    }

    public boolean isUserAbleToChangeControlSettings(String uuid) {
        return (hasPermission(uuid, PERMISSION_CP_CHANGE_SETTINGS));
    }
   
    // Message Permissions
    public boolean isUserAbleToRead(String uuid) {
        return (hasPermission(uuid, PERMISSION_MP_READ));
    }
    
    public boolean isUserAbleToReviseAny(String uuid) {
        return (hasPermission(uuid, PERMISSION_MP_REVISE_ANY));
    }
    
    public boolean isUserAbleToReviseOwn(String uuid) {
        return (hasPermission(uuid, PERMISSION_MP_REVISE_OWN));
    }
    
    public boolean isUserAbleToDeleteAny(String uuid) {
        return (hasPermission(uuid, PERMISSION_MP_DELETE_ANY));
    }
    
    public boolean isUserAbleToDeleteOwn(String uuid) {
        return (hasPermission(uuid, PERMISSION_MP_DELETE_OWN));
    }
    
    public boolean isUserAbleToReadDrafts(String uuid) {
        return (hasPermission(uuid, PERMISSION_MP_READ_DRAFTS));
    }
    
    private boolean hasPermission(String uuid, String permission) {
        return SecurityService.unlock(permission, SiteService.siteReference(uuid));
    }

}
