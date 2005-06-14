/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/sakai-samigo/src/org/sakaiproject/tool/assessment/facade/GradebookFacade.java,v 1.4 2005/06/03 20:32:22 janderse.umich.edu Exp $
*
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.facade;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.kernel.tool.Placement;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.service.framework.portal.cover.PortalService;


public class GradebookFacade implements Serializable {

  private static Log log = LogFactory.getLog(GradebookFacade.class);
	
  public static String getGradebookUId(){
    String context;
    
    Placement placement = org.sakaiproject.api.kernel.tool.cover.ToolManager.getInstance().getCurrentPlacement();
    if (placement == null)
    {
    	log.warn("getGradebookUId() - no tool placement found, probably taking an assessment via URL.  Gradebook not updated.");
        return null;
    }
    context = placement.getContext();
    
    //context = org.sakaiproject.service.framework.portal.cover.PortalService.getCurrentSiteId();
    return context;
  }

  // return Gradebook #1
  public static String getDefaultGradebookUId(){
    return "Test Gradebook #1";
  }

 }

/**********************************************************************************
*
* $Header: /cvs/sakai2/sam/sakai-samigo/src/org/sakaiproject/tool/assessment/facade/GradebookFacade.java,v 1.4 2005/06/03 20:32:22 janderse.umich.edu Exp $
*
**********************************************************************************/
