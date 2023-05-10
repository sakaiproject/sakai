/***********************************************************************************
* Copyright (c) ${license.git.copyrightYears} ${holder}
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*             http://opensource.org/licenses/ecl2
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
************************************************************************************/

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionGlobalVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class CalculatedQuestionDeleteGlobalVariableListener implements ActionListener{

    /**
     * This listener will delete a global variable
     */
    public void processAction(ActionEvent arg0) throws AbortProcessingException {
        ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
        ItemBean item = itemauthorbean.getCurrentItem();

        FacesContext context = FacesContext.getCurrentInstance();
        String globalVariableName = ContextUtil.lookupParam("globalvariabledelete");

        Map<String, CalculatedQuestionGlobalVariableBean> globalVariables = item.getCalculatedQuestion().getGlobalvariables();
        if (globalVariables.containsKey(globalVariableName)) {
            item.getCalculatedQuestion().removeGlobalVariable(globalVariableName);
        }
        item.setOutcome("calculatedQuestion");
        item.setPoolOutcome("calculatedQuestion");
        context.renderResponse();
    }

}
