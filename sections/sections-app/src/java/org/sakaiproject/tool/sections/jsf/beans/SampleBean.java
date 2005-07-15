/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.sections.jsf.beans;

import java.io.Serializable;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.sections.SampleManager;
import org.sakaiproject.api.sections.facades.Authn;

/**
 * A sample jsf backing bean.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class SampleBean extends InitializableBean implements Serializable {
    private static final long serialVersionUID = -1922222210846114276L;
    private static final Log log = LogFactory.getLog(SampleBean.class);
    
    // TODO Centralize the local services in a base backing bean
    private SampleManager sampleManager;
    private Authn authn;
    
    // Fields for the UI (initialize these in init())
    private List sections;
    private String userName;
    
    // Fields for UI Components
    private String title;


    /**
     * Makes any queries necessary to initialize the state of this backing bean.
     * 
     * @see org.sakaiproject.tool.sections.jsf.beans.InitializableBean#init()
     */
    protected void init() {
        log.info("SampleBean initializing...");
        sections = sampleManager.getSections();
        userName = authn.getUserUid();
    }
    
    // Manually initialize the bean
    // TODO Replace with flowState initialization
    public String getConfigureBean() {
        log.info("Manually configuring SampleBean");
        init();
        return "";
    }
    
    //// Action events
    public void processCreateSection(ActionEvent e) {
        log.info("Creating title = " + title);        
        sampleManager.createSection(title);
    }

    //// Bean getters / setters for UI
    public List getSections() {
        return sections;
    }
    public String getUserName() {
        return userName;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    
    //// Setters for dep. injection
    public void setSampleManager(SampleManager sampleManager) {
        this.sampleManager = sampleManager;
    }
    
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }
}


/**********************************************************************************
 * $Id$
 *********************************************************************************/
