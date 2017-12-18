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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.questionpool;

import java.util.List;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.tool.assessment.services.QuestionPoolService;
import org.sakaiproject.tool.assessment.ui.bean.questionpool.QuestionPoolBean;
import org.sakaiproject.tool.assessment.ui.bean.shared.PersonBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * <p>Title: Samigo</p>2
 * <p>Description: Sakai Assessment Manager</p>
 * @version $Id$
 */
@Slf4j
public class SortQuestionListListener
    implements ActionListener
{

  public void processAction(ActionEvent ae) throws AbortProcessingException
  {
    // get service and managed bean
    QuestionPoolBean questionpoolbean = (QuestionPoolBean) ContextUtil.lookupBean("questionpool");
    
    String getItems =ContextUtil.lookupParam("getItems");
    String qpid=ContextUtil.lookupParam("qpid");

    QuestionPoolService delegate = new QuestionPoolService();

    // Check permission to this pool
    PersonBean person = (PersonBean) ContextUtil.lookupBean("person");
    String userId = person.getId();

    List<Long> poolsWithAccess = delegate.getPoolIdsByAgent(userId);
    if (StringUtils.isNotBlank(qpid) && !poolsWithAccess.contains(Long.valueOf(qpid))) {
        throw new IllegalArgumentException("userId " + userId + " does not have access to question pool id " + qpid);
    }

    List list;
    if (StringUtils.isNotBlank(getItems) && getItems.trim().equals("false")) {
        log.debug("Do not getItems: getItems = " + getItems);
    }
    else {
        list = delegate.getAllItems(StringUtils.isBlank(qpid) ? questionpoolbean.getCurrentPool().getId() : Long.valueOf(qpid));
        log.debug("AFTER CALLING DELEGATE");
        questionpoolbean.setAllItems(list);
    }
  }
}
