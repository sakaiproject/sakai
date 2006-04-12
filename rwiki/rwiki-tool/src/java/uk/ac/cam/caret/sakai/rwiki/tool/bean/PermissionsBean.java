/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
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
package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;

/**
 * A Bean that has various helper methods to check whether certain things can be
 * done to a RWikiObject
 * 
 * @author andrew
 */

//FIXME: Tool

public class PermissionsBean {

    /**
     * currently set rwikiObject
     */
    private RWikiObject rwikiObject;

    /**
     * ObjectService to perform permissions checks with
     * 
     * @see RWikiObjectService
     */
    private RWikiObjectService objectService;


    public PermissionsBean() {

    }

    /**
     * Creates a fully set-up PermissionsBean
     * 
     * @param rwikiObject
     * @param currentUser
     * @param objectService
     */
    public PermissionsBean(RWikiObject rwikiObject, 
            RWikiObjectService objectService) {
        this.rwikiObject = rwikiObject;
        this.objectService = objectService;
    }

    /**
     * The currently set RWikiObjectService
     * 
     * @return objectService
     */
    public RWikiObjectService getObjectService() {
        return objectService;
    }

    /**
     * Sets the current RWikiObjectService
     * 
     * @param objectService
     */
    public void setObjectService(RWikiObjectService objectService) {
        this.objectService = objectService;
    }

    /**
     * The current RWikiObject.
     * 
     * @return rwikiObject.
     */
    public RWikiObject getRwikiObject() {
        return rwikiObject;
    }

    /**
     * Sets the current RWikiObject.
     * 
     * @param rwikiObject.
     */
    public void setRwikiObject(RWikiObject rwikiObject) {
        this.rwikiObject = rwikiObject;
    }

    /**
     * Using the current objectService checks whether admin functions can be
     * performed on the current RWikiObject by the currentUser.
     * 
     * @return true if the currentUser has admin rights on this rwikiObject.
     */
    public boolean isAdminAllowed() {
        return objectService.checkAdmin(rwikiObject);
    }

    /**
     * Using the current objectService checks whether update can be performed on
     * the current RWikiObject by the currentUser.
     * 
     * @return true if the currentUser has update rights on this rwikiObject
     */
    public boolean isUpdateAllowed() {
        return objectService.checkUpdate(rwikiObject);
    }

    /**
     * Using the current objectService checks whether the current RWikiObject
     * can be read by the currentUser.
     * 
     * @return true if the currentUser has read rights on this rwikiObject.
     */
    public boolean isReadAllowed() {
        return objectService.checkRead(rwikiObject);
    }
}
