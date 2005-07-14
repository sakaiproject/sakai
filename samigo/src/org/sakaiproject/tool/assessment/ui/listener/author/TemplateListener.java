/**********************************************************************************
* $URL$
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.IndexBean;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id$
 */

public class TemplateListener extends TemplateBaseListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(TemplateListener.class);
  private static ContextUtil cu;
  private static BeanSort bs;

  public TemplateListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    FacesContext context = FacesContext.getCurrentInstance();
    Map reqMap = context.getExternalContext().getRequestMap();
    Map requestParams = context.getExternalContext().getRequestParameterMap();
    System.out.println("debugging ActionEvent: " + ae);
    System.out.println("debug requestParams: " + requestParams);
    System.out.println("debug reqMap: " + reqMap);

    // get service and managed bean
    AssessmentService assessmentService = new AssessmentService();
    IndexBean templateIndex = (IndexBean) cu.lookupBean(
                       "templateIndex");

    // look for some sort information passed as parameters
    processSortInfo(templateIndex);

    ArrayList templates = new ArrayList();
    try
    {
	FacesContext.getCurrentInstance().
	getExternalContext().getSessionMap().
	put("template", new TemplateBean());
	ArrayList list = assessmentService.getBasicInfoOfAllActiveAssessmentTemplates("title");
        System.out.println("Qingru Got " + list.size() + " templates in front end");
        Iterator iter = list.iterator();
        while (iter.hasNext())
        {
	 AssessmentTemplateFacade facade =
	 (AssessmentTemplateFacade) iter.next();
         TemplateBean bean = new TemplateBean();
	  bean.setTemplateName(facade.getTitle());
         bean.setIdString(facade.getAssessmentBaseId().toString());
          bean.setLastModified(facade.getLastModifiedDate().toString());
         templates.add(bean);
        }
      } catch (Exception e) {
	e.printStackTrace();
      }

      if (templates != null)
      System.out.println("Qingru's test 1  templates' size is  " + templates.size());

     String sortProperty = templateIndex.getTemplateOrderBy();
     boolean sortAscending = templateIndex.isTemplateAscending();

     bs = new BeanSort(templates, sortProperty);
     if (templates != null)
     System.out.println("Qingru's test 2 templates' size is  " + templates.size());

     System.out.println("Qingru's test sortProperty is  " + sortProperty);
     System.out.println("Qingru's test templateAscending is  " + templateIndex.isTemplateAscending());
     if (sortProperty.equals("lastModified"))
     {
       bs.toDateSort();
     }
     else
     {
       bs.toStringSort();
     }

     bs.sort();
     if (sortAscending==false)
     {
	Collections.reverse(templates);
     }

     // get the managed bean, author and set the list
     templateIndex.setSortTemplateList(templates);
     // System.out.println("Qingru's test: isTemplateAscending is  " + templateIndex.isTemplateAscending());
   System.out.println("Qingru's test: templateIndex.getTemplateList is  " + templateIndex.getSortTemplateList());
  }


/**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(IndexBean bean) {
    String templateOrder = cu.lookupParam("templateSortType");
    String tempAscending = cu.lookupParam("templateAscending");

    if (templateOrder != null && !templateOrder.trim().equals("")) {
      bean.setTemplateOrderBy(templateOrder);
 System.out.println("Qingru's 1 ");
    }

    if (tempAscending != null && !tempAscending.trim().equals("")) {
      try {
        bean.setTemplateAscending((Boolean.valueOf(tempAscending)).booleanValue());
 System.out.println("Qingru's 2 ");
      }
      catch (Exception ex) { //skip
      }
    }
    else
    {
 System.out.println("Qingru's 3  ");
	bean.setTemplateAscending(true);
    }

  }

}
