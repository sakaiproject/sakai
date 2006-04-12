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

import org.sakaiproject.service.framework.log.Logger;

import uk.ac.cam.caret.sakai.rwiki.tool.api.PopulateService;

/**
 * PrePopulateBean is a helper bean which given the currentPageRealm, the
 * currentUser, and a populateService will populate the current page realm.
 * 
 * @author andrew
 */
//FIXME: Tool

public class PrePopulateBean {

    private PopulateService populateService;

    private String currentPageRealm;
    
    private String currentGroup;

    private String woksiteOwner;

    private Logger log;

    /**
     * Populates the current realm, relying on the service for caching etc.
     */
    public void doPrepopulate() {
        log.debug(this.getClass().getName() + " current-user: " + woksiteOwner
                + " pre-populating realm " + currentPageRealm);

        // Populate the realm...
        
        populateService.populateRealm(woksiteOwner, currentPageRealm, currentGroup);
    }

    /**
     * The current page realm.
     * @return currentPageRealm 
     */
    public String getCurrentPageRealm() {
        return currentPageRealm;
    }

    /**
     * Set the current page realm.
     * @param currentPageRealm
     */
    public void setCurrentPageRealm(String currentPageRealm) {
        this.currentPageRealm = currentPageRealm;
    }

    /**
     * The current user.
     * @return current user
     */
    public String getWoksiteOwner() {
        return woksiteOwner;
    }

    /**
     * Set the current user.
     * @param currentUser
     */
    public void setWoksiteOwner(String currentUser) {
        this.woksiteOwner = currentUser;
    }

    /**
     * The logger with which to register log events.
     * @return log
     */
    public Logger getLog() {
        return log;
    }

    /**
     * Set the logger with which to register log events.
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * The current populateService
     * @return populateService
     */
    public PopulateService getPopulateService() {
        return populateService;
    }

    /**
     * Set the current populateService.
     * @param populateService
     */
    public void setPopulateService(PopulateService populateService) {
        this.populateService = populateService;
    }

	/**
	 * @return Returns the currentGroup.
	 */
	public String getCurrentGroup() {
		return currentGroup;
	}

	/**
	 * @param currentGroup The currentGroup to set.
	 */
	public void setCurrentGroup(String currentGroup) {
		this.currentGroup = currentGroup;
	}

}
