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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.sakaiproject.service.framework.log.Logger;

import uk.ac.cam.caret.sakai.rwiki.tool.bean.RecentlyVisitedBean;

/**
 * Helper bean to get a RecentlyVistedBean from a request.
 * 
 * @author andrew
 */
//FIXME: Tool

public class RecentlyVisitedHelperBean {

    /**
     * Session attribute to save the recentlyVisitedBean
     */
    public static final String RECENT_VISIT_ATTR = "recentlyVisitedBean";

    private ServletRequest request;

    private RecentlyVisitedBean recentBean;

    private Logger log;

    private String defaultSpace;

    /**
     * Sets the recently visited bean using the set request and logger and
     * default space.
     */
    public void init() {
        recentBean = RecentlyVisitedHelperBean.getRecentlyVisitedBean(
                (HttpServletRequest) request, log, defaultSpace);
    }

    /**
     * Set the current request
     * 
     * @param servletRequest
     */
    public void setServletRequest(ServletRequest servletRequest) {
        this.request = servletRequest;
    }

    /**
     * Set the default space
     * 
     * @param defaultSpace
     */
    public void setDefaultSpace(String defaultSpace) {
        this.defaultSpace = defaultSpace;
    }

    /**
     * Retrieve the current <code>RecentlyVisitedBean</code> from the passed
     * in <code>ServletRequest</code> or create one in the default space.
     * 
     * @param request current servlet request
     * @param log current logger
     * @param defaultSpace defaultSpace to for the RecentlyVisitedBean
     * @return RecentlyVisitedBean
     */
    public static RecentlyVisitedBean getRecentlyVisitedBean(
            HttpServletRequest request, Logger log, String defaultSpace) {
        HttpSession session = request.getSession();
        RecentlyVisitedBean bean = null;
        try {
            bean = (RecentlyVisitedBean) session
                    .getAttribute(RECENT_VISIT_ATTR);
        } catch (ClassCastException e) {
            log.warn("Session contains object at " + RECENT_VISIT_ATTR
                    + " which is not a valid breadcrumb bean\n" + "Object is: "
                    + session.getAttribute(RECENT_VISIT_ATTR).toString());
        }

        if (bean == null) {
            bean = new RecentlyVisitedBean(defaultSpace);
            session.setAttribute(RECENT_VISIT_ATTR, bean);
        }

        return bean;
    }

    /**
     * Get current logger
     * @return logger
     */
    public Logger getLog() {
        return log;
    }

    /**
     * Set the current logger
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * Get the retrieved recently visited bean
     * @return recentlyVisitedBean
     */ 
    public RecentlyVisitedBean getRecentlyVisitedBean() {
        return recentBean;
    }

}
