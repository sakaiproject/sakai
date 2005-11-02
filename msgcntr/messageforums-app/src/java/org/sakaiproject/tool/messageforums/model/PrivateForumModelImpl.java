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

package org.sakaiproject.tool.messageforums.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.model.PrivateForumModel;

public class PrivateForumModelImpl extends BaseForumModelImpl implements PrivateForumModel {

    private static final Log LOG = LogFactory.getLog(PrivateForumModelImpl.class);
    
    private Boolean autoForward;
    private String autoForwardEmail;
    private Boolean previewPaneEnabled;
    
    // package level constructor only used for Testing
    PrivateForumModelImpl() {}
    
    public PrivateForumModelImpl(PrivateForum privateForum) {
        // TODO: set up this model based on hibernate object passes
        
    }
    
    public Boolean getAutoForward() {
        return autoForward;
    }

    public void setAutoForward(Boolean autoForward) {
        this.autoForward = autoForward;
    }

    public String getAutoForwardEmail() {
        return autoForwardEmail;
    }

    public void setAutoForwardEmail(String autoForwardEmail) {
        this.autoForwardEmail = autoForwardEmail;
    }

    public Boolean getPreviewPaneEnabled() {
        return previewPaneEnabled;
    }

    public void setPreviewPaneEnabled(Boolean previewPaneEnabled) {
        this.previewPaneEnabled = previewPaneEnabled;
    }
   
}
