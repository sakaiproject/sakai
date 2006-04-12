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
import uk.ac.cam.caret.sakai.rwiki.tool.bean.DiffBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HistoryBean;

/**
 * Relatively short lived bean to help create a DiffBean. This is currently used
 * by the <code>RequestScopeSuperBean</code>, and was used by the
 * RequestScopeApplicationContext before it became too slow.
 * 
 * @author andrew
 */
//FIXME: Tool

public class DiffHelperBean {

    /**
     * current servletRequest
     */
    private ServletRequest request;

    /**
     * current rwikiObject
     */
    private RWikiObject rwikiObject;

    private RWikiObjectService rwikiObjectService;
    /**
     * DiffBean that was created
     */
    private DiffBean diffBean;

    /**
     * initialise the DiffHelperBean, effectively creating a new diffBean for
     * the rwikiObject
     */
    public void init() {
        String left = request.getParameter(HistoryBean.LEFT_PARAM);
        String right = request.getParameter(HistoryBean.RIGHT_PARAM);
        diffBean = new DiffBean(rwikiObject, rwikiObjectService, left, right);
    }

    /**
     * Set the current servletRequest
     * 
     * @param servletRequest
     *            current request to respond to
     */
    public void setServletRequest(ServletRequest servletRequest) {
        this.request = servletRequest;
    }

    /**
     * Get the currently selected rwikiObject
     * 
     * @return rwikiObject
     */
    public RWikiObject getRwikiObject() {
        return rwikiObject;
    }

    /**
     * Set the RWikiObject for the diffBean
     * 
     * @param rwikiObject
     *            RWikiObject for the diffs
     */
    public void setRwikiObject(RWikiObject rwikiObject) {
        this.rwikiObject = rwikiObject;
    }

    /**
     * Get the DiffBean for the current RWikiObject and the request
     * 
     * @return
     */
    public DiffBean getDiffBean() {
        return diffBean;
    }

    /**
     * Set the diffBean for this DiffHelperBean
     * 
     * @param diffBean
     */
    public void setDiffBean(DiffBean diffBean) {
        this.diffBean = diffBean;
    }

	public RWikiObjectService getRwikiObjectService() {
		return rwikiObjectService;
	}

	public void setRwikiObjectService(RWikiObjectService rwikiObjectService) {
		this.rwikiObjectService = rwikiObjectService;
	}

}
