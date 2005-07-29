/**********************************************************************************
*
* $Id: LoginBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
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

package org.sakaiproject.tool.section.jsf.backingbean.standalone;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.sakaiproject.tool.section.facade.impl.standalone.AuthnStandaloneImpl;
import org.sakaiproject.tool.section.facade.impl.standalone.ContextStandaloneImpl;

/**
 * @author <a href="jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class LoginBean {
    private String userName;
    private String context;

    public String processSetUserNameAndContext() {
        // We store the username and context as a session attribute in standalone mode
        HttpSession session = (HttpSession)FacesContext.getCurrentInstance().getExternalContext().getSession(true);
        session.setAttribute(AuthnStandaloneImpl.USER_NAME, userName);
        session.setAttribute(ContextStandaloneImpl.CONTEXT, context);
        
        // Send the user to the sample page
        return "sample";
    }
    
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
}


/**************************************************************************************************************************************************************************************************************************************************************
 * $Id: LoginBean.java 637 2005-07-15 16:35:46Z jholtzman@berkeley.edu $
 *************************************************************************************************************************************************************************************************************************************************************/
