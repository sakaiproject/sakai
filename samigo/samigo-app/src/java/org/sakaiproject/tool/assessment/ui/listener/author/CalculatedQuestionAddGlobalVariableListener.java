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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionFormulaBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionGlobalVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

public class CalculatedQuestionAddGlobalVariableListener implements ActionListener{
    private static final String ERROR_MESSAGE_BUNDLE = "org.sakaiproject.tool.assessment.bundle.AuthorMessages";
    private static final Pattern CALCQ_ALLOWING_VARIABLES_FORMULAS_NAMES = Pattern.compile("[a-zA-ZÀ-ÿ\\u00f1\\u00d1]\\w*", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * This listener will read in the instructions, parse any variables and 
     * formula names it finds, and then check to see if there are any errors 
     * in the configuration for the question.  
     * 
     * <p>Errors include <ul><li>no variables or formulas named in the instructions</li>
     * <li>variables and formulas sharing a name</li>
     * <li>variables with invalid ranges of values</li>
     * <li>formulas that are syntactically wrong</li></ul>
     * Any errors are written to the context messager
     * <p>The validate formula is also called directly from the ItemAddListner, before
     * saving a calculated question, to ensure any last minute changes are caught.
     */
    public void processAction(ActionEvent arg0) throws AbortProcessingException {
        ItemAuthorBean itemauthorbean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
        ItemBean item = itemauthorbean.getCurrentItem();

        FacesContext context = FacesContext.getCurrentInstance();
        String globalVariableName = ContextUtil.lookupParam("globalvariablename");
        List<String> errors = this.validate(item, globalVariableName, true);

        if (errors.size() > 0) {
            item.setOutcome("calculatedQuestion");
            item.setPoolOutcome("calculatedQuestion");
            for (String error : errors) {
                context.addMessage(null, new FacesMessage(error));
            }
            context.renderResponse();
        }
    }

    /**
     * validate() returns a list of error strings to display to the context.
     * <p>Errors include <ul><li>global variables is already included</li>
     * @param item - an ItemBean, which contains all of the needed information 
     * about the CalculatedQuestion
     * @param globalVariableName
     * @param addedButNotExtracted - boolean. Indicate if global variable has been added but not extracted on UI.
     * @returns a List<String> of error messages to be displayed in the context messager.
     */
    public List<String> validate(ItemBean item, String globalVariableName, boolean addedButNotExtracted) {
        List<String> errors = new ArrayList<String>();

        // validating globalVariableName
        if (globalVariableName == null || globalVariableName.length() == 0) {
            errors.add(getErrorMessage("global_variable_name_empty"));
        } else {
            if (!CALCQ_ALLOWING_VARIABLES_FORMULAS_NAMES.matcher(globalVariableName).matches()) {
                errors.add(getErrorMessage("global_variable_name_invalid"));
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
        Map<String, CalculatedQuestionGlobalVariableBean> globalVariables = item.getCalculatedQuestion().getGlobalvariables();
        Long maxSequenceValue = getMaxSequenceValue(variables, formulas, globalVariables);

        if (!globalVariables.containsKey(globalVariableName)) {
            CalculatedQuestionGlobalVariableBean bean = new CalculatedQuestionGlobalVariableBean(globalVariableName);
            bean.setName(globalVariableName);
            bean.setAddedButNotExtracted(addedButNotExtracted);
            bean.setActive(true);
            bean.setSequence(++maxSequenceValue);
            item.getCalculatedQuestion().addGlobalVariable(bean);
        } else {
            errors.add(getErrorMessage("unique_names"));
        }
        return errors;
    }

    private Long getMaxSequenceValue(Map<String, CalculatedQuestionVariableBean> variables,
    	Map<String, CalculatedQuestionFormulaBean> formulas,
    	Map<String, CalculatedQuestionGlobalVariableBean> globalVariables) {

    	Long maxValue = 0L;

    	for (CalculatedQuestionVariableBean variable:variables.values()) {
    		Long currentSequence = variable.getSequence(); 
    		if (currentSequence != null && currentSequence.compareTo(maxValue)>0)
    			maxValue = currentSequence;
    	}
    	for (CalculatedQuestionFormulaBean formula:formulas.values()) {
    		Long currentSequence = formula.getSequence(); 
    		if (currentSequence != null && currentSequence.compareTo(maxValue)>0)
    			maxValue = currentSequence;
    	}
    	for (CalculatedQuestionGlobalVariableBean globalVariable:globalVariables.values()) {
    		Long currentSequence = globalVariable.getSequence();
    		if (currentSequence != null && currentSequence.compareTo(maxValue)>0)
    			maxValue = currentSequence;
    	}
    	return maxValue;
    }

    /**
     * getErrorMessage() retrieves the localized error message associated with
     * the errorCode
     * @param errorCode
     * @return
     */
    private static String getErrorMessage(String errorCode) {
        String err = ContextUtil.getLocalizedString(ERROR_MESSAGE_BUNDLE, errorCode);
        return err;
    }

}
