/**********************************************************************************
* $HeadURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueries;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.AuthorBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class SortInactivePublishedAssessmentListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SortInactivePublishedAssessmentListener.class);
  private static ContextUtil cu;

  public SortInactivePublishedAssessmentListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    //System.out.println("debugging ActionEvent: " + ae);
    //System.out.println("debug requestParams: " + requestParams);
    //System.out.println("debug reqMap: " + reqMap);

    // get service and managed bean
    PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
    AuthorBean author = (AuthorBean) cu.lookupBean(
                       "author");

   processSortInfo(author);

    ArrayList inactivePublishedList = new ArrayList();
    inactivePublishedList = publishedAssessmentService.
          getBasicInfoOfAllInActivePublishedAssessments(this.getInactivePublishedOrderBy(author),author.isInactivePublishedAscending());

   // get the managed bean, author and set the list
   author.setInactivePublishedAssessments(inactivePublishedList);

  }

/**
   * get orderby parameter for takable table
   * @param select the SelectAssessment bean
   * @return
   */
  private String getInactivePublishedOrderBy(AuthorBean author) {
    String sort = author.getInactivePublishedAssessmentOrderBy();
    String returnType =  PublishedAssessmentFacadeQueries.TITLE;
    if (sort != null && sort.equals("releaseTo"))
    {
	returnType = PublishedAssessmentFacadeQueries.PUB_RELEASETO;
    }
    else if (sort.equals("startDate"))
    {
    	returnType = PublishedAssessmentFacadeQueries.PUB_STARTDATE;
    }
    else if (sort.equals("dueDate"))
    {
    	returnType = PublishedAssessmentFacadeQueries.PUB_DUEDATE;
    }


    return returnType;
  }

/**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(AuthorBean bean) {
    String inactiveOrder = cu.lookupParam("inactiveSortType");
    String inactivePublishedAscending = cu.lookupParam("inactivePublishedAscending");

    if (inactiveOrder != null && !inactiveOrder.trim().equals("")) {
      bean.setInactivePublishedAssessmentOrderBy(inactiveOrder);
    }

    if (inactivePublishedAscending != null && !inactivePublishedAscending.trim().equals("")) {
      try {
        bean.setInactivePublishedAscending((Boolean.valueOf(inactivePublishedAscending)).booleanValue());
      }
      catch (Exception ex) { //skip
      }
    }
    else
    {
	bean.setInactivePublishedAscending(true);
    }

  }

}
