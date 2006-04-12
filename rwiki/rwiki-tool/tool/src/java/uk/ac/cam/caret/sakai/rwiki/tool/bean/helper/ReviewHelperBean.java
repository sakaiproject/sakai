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
package uk.ac.cam.caret.sakai.rwiki.tool.bean.helper;

import javax.servlet.ServletRequest;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HistoryBean;

/**
 * Bean which will create a mock RWikiObject with the content set to the content
 * of a requested revision
 * 
 * @author andrew
 */
//FIXME: Tool

public class ReviewHelperBean {

    /**
     * SerlvetRequest to get revision details from
     */
    private ServletRequest request;

    /**
     * the real rwikiObject
     */
    private RWikiObject rwikiObject;
    
    private RWikiObjectService rwikiObjectService;

    /**
     * the mock rwikiObject
     */
    private RWikiObject mock;

    /**
     * the revision number of the mock
     */
    private int interestedRevision;

    /**
     * Initialize the bean by setting the interested revision from the request
     * and creating the mock.
     */
    public void init() {
        if (request != null) {
            interestedRevision = Integer.parseInt(request
                    .getParameter(HistoryBean.REVISION_PARAM));
        }

        if (interestedRevision == rwikiObject.getRevision().intValue()) {
            mock = rwikiObject;
        } else {
            mock =  rwikiObjectService.getRWikiHistoryObject(rwikiObject,interestedRevision);
        }
    }

    /**
     * Set the current request
     * @param servletRequest
     */
    public void setServletRequest(ServletRequest servletRequest) {
        this.request = servletRequest;
    }

    /**
     * Get the associated real rwikiObject
     * @return the associated real rwikiObject
     */
    public RWikiObject getRwikiObject() {
        return rwikiObject;
    }

    /**
     * Set the real rwikiObject
     * @param rwikiObject the real object
     */
    public void setRwikiObject(RWikiObject rwikiObject) {
        this.rwikiObject = rwikiObject;
    }

    /**
     * Get the requested interestedRevision
     * @return revision number of the mock
     */
    public int getInterestedRevision() {
        return interestedRevision;
    }

    /**
     * Set the interestedRevision of the mock
     * @param interestedRevision the interestedRevision to set
     */
    public void setInterestedRevision(int interestedRevision) {
        this.interestedRevision = interestedRevision;
    }

    /**
     * Get the mock rwikiObject
     * @return the mock rwikiObject
     */
    public RWikiObject getMock() {
        return mock;
    }

    /**
     * Set the mock rwikiObject
     * @param mock object to set
     */
    public void setMock(RWikiObject mock) {
        this.mock = mock;
    }

	public RWikiObjectService getRwikiObjectService() {
		return rwikiObjectService;
	}

	public void setRwikiObjectService(RWikiObjectService rwikiObjectService) {
		this.rwikiObjectService = rwikiObjectService;
	}
}
