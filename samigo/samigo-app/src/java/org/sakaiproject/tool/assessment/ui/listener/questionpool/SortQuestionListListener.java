/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.ArrayList;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */

public class SortQuestionListListener
    implements ActionListener
{
  private static Log log = LogFactory.getLog(SortQuestionListListener.class);

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    // get service and managed bean
    QuestionPoolBean questionpoolbean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
    
    String orderBy = ContextUtil.lookupParam("orderBy");
    String ascending =ContextUtil.lookupParam("ascending");
    if (orderBy != null &&!orderBy.trim().equals("")){
    	questionpoolbean.setSortQuestionProperty(orderBy);
    	log.debug("orderBy = " + ContextUtil.lookupParam("orderBy"));
    }
    
    if (ascending != null && ascending.trim().equals("")){
    	questionpoolbean.setSortAscending(Boolean.valueOf(ascending).booleanValue());
    	log.debug("ascending = " + ascending);
    }
    
    questionpoolbean.setSortQuestionAscending(Boolean.valueOf(ContextUtil.lookupParam("ascending")).booleanValue());
    
    String qpid=ContextUtil.lookupParam("qpid");
    QuestionPoolService delegate = new QuestionPoolService();
    ArrayList list= null;
    if (qpid==null ||("").equals(qpid)){
     list = delegate.getAllItemsSorted(questionpoolbean.getCurrentPool().getId(), orderBy, ascending);
    }
    else{
	list = delegate.getAllItemsSorted(Long.valueOf(qpid),orderBy, ascending);
    }
    
    log.debug("AFTER CALLING DELEGATE");
    questionpoolbean.setAllItems(list);

  }
}
