/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.assessment.facade.AssessmentTemplateFacade;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.ui.bean.author.IndexBean;
import org.sakaiproject.tool.assessment.ui.bean.author.TemplateBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.BeanSort;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * <p>Description: Listener for the Template(Assessment Type) page</p>
 */
@Slf4j
public class TemplateListener extends TemplateBaseListener
    implements ActionListener
{
  private static BeanSort bs;

  public TemplateListener()
  {
  }

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {

    // get service and managed bean
    AssessmentService assessmentService = new AssessmentService();
    IndexBean templateIndex = (IndexBean) ContextUtil.lookupBean(
                       "templateIndex");

    // look for some sort information passed as parameters
    processSortInfo(templateIndex);

    boolean autoSubmitEnabled = ServerConfigurationService.getBoolean("samigo.autoSubmit.enabled", false);
    if (!autoSubmitEnabled) {
    	templateIndex.setAutomaticSubmissionEnabled(false);
    }
    else {
    	templateIndex.setAutomaticSubmissionEnabled(true);
    }
    
    List<TemplateBean> templates = new ArrayList();
    try
    {
	FacesContext.getCurrentInstance().
	getExternalContext().getSessionMap().put("template", new TemplateBean());
	List<AssessmentTemplateFacade> list = assessmentService.getBasicInfoOfAllActiveAssessmentTemplates("title");
        Iterator iter = list.iterator();
        while (iter.hasNext())
        {
	 AssessmentTemplateFacade facade = (AssessmentTemplateFacade) iter.next();
         TemplateBean bean = new TemplateBean();
         bean.setTemplateName(facade.getTitle());
         bean.setIdString(facade.getAssessmentBaseId().toString());
         bean.setLastModified(facade.getLastModifiedDate().toString());
         bean.setTypeId(facade.getTypeId().toString());
         templates.add(bean);
        }
      } catch (Exception e) {
          log.error(e.getMessage(), e);
      }

     String sortProperty = templateIndex.getTemplateOrderBy();
     boolean sortAscending = templateIndex.isTemplateAscending();

     bs = new BeanSort(templates, sortProperty);
     if (templates != null)
     if ("lastModified".equals(sortProperty))
     {
       bs.toDateSort();
     }
     else
     {
       bs.toStringSort();
     }
     templates = (ArrayList)bs.sort();

     if (sortAscending==false)
     {
	Collections.reverse(templates);
     }

     // get the managed bean, author and set the list
     templateIndex.setSortTemplateList(templates);
  }


/**
   * look at sort info from post and set bean accordingly
   * @param bean the select index managed bean
   */
  private void processSortInfo(IndexBean bean) {
    bean.setTemplateOrderBy("templateName");
    bean.setTemplateAscending(true);
    String templateOrder = ContextUtil.lookupParam("templateSortType");
    String tempAscending = ContextUtil.lookupParam("templateAscending");

    if (templateOrder != null && !templateOrder.trim().equals("")) {
      bean.setTemplateOrderBy(templateOrder);
    }

    if (tempAscending != null && !tempAscending.trim().equals("")) {
      try {
        bean.setTemplateAscending((Boolean.valueOf(tempAscending)).booleanValue());
      }
      catch (Exception ex) {
        log.warn("tempAscending is not a boolena value:"+ex.getMessage());
      }
    }
  }

}
