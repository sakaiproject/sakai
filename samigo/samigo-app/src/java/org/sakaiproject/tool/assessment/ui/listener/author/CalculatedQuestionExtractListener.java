/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/samigo-app/src/java/org/sakaiproject/tool/assessment/ui/bean/author/MatchItemBean.java $
 * $Id: MatchItemBean.java 59684 2009-04-03 23:33:27Z arwhyte@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionFormulaBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoExpressionError;
import org.sakaiproject.tool.assessment.util.SamigoExpressionParser;

public class CalculatedQuestionExtractListener implements ActionListener{

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
        FacesContext context=FacesContext.getCurrentInstance();
                
        List<String> errors = this.validate(item);        
        
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
     * <p>Errors include <ul><li>no variables or formulas named in the instructions</li>
     * <li>variables and formulas sharing a name</li>
     * <li>variables with invalid ranges of values</li>
     * <li>formulas that are syntactically wrong</li></ul>
     * Any errors are written to the context messager
     * <p>The validate formula is also called directly from the ItemAddListner, before
     * saving a calculated question, to ensure any last minute changes are caught.
     * @param item - an ItemBean, which contains all of the needed information 
     * about the CalculatedQuestion
     * @returns a List<String> of error messages to be displayed in the context messager.
     */
    public List<String> validate(ItemBean item) {
        this.extractFromInstructions(item);
        
        List<String> errors = new ArrayList<String>();
        CalculatedQuestionBean question = item.getCalculatedQuestion();
        
        // question must have at least on variable and one formula
        if (question.getActiveVariables().size() == 0) {
            String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","no_variables");
            errors.add(err);
        }
        if (question.getActiveFormulas().size() == 0) {
            String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","no_formulas");
            errors.add(err);
        }
        
        // variables max must be greater than min
        for (CalculatedQuestionVariableBean variable : question.getActiveVariables().values()) {
            if (variable.getMax() < variable.getMin()) {
                String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","max_less_than_min");
                errors.add(err);
            }
        }
        
        // formula tolerances must be numbers or percentages
        for (CalculatedQuestionFormulaBean formula : question.getActiveFormulas().values()) {
            String tolerance = formula.getTolerance();
            if (!tolerance.matches("^\\s*[0-9]+\\.?[0-9]*\\%\\s*$")) {
                try {
                    Double.parseDouble(tolerance);
                } catch (NumberFormatException n) {
                    String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "invalid_tolerance");
                    errors.add(err);
                }
            }
        }
        
        // throw an error if variables and formulas share any names
        if (!Collections.disjoint(question.getActiveFormulas().keySet(), question.getActiveVariables().keySet())) {
            String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","unique_names");
            errors.add(err);
        }
        
        // capture formula errors
        // TODO - there is more information available in the SamigoExpressionError 
        // object that we are not using, may want to return the Error object, instead
        // of the string here.
        Map<Integer, String> formulaErrors = this.validateFormulas(item);
        if (formulaErrors.size() > 0) {
            for (Map.Entry<Integer, String> error : formulaErrors.entrySet()) {
                Integer key = error.getKey();
                String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", 
                        "samigo_formula_error_" + key.toString());
                errors.add(err);
            };
        }
        
        return errors;
    }
    
    /**
     * extractFromInstructions examines the instructions for this calculated
     * question and creates any new variables or formulas that are found.
     * Variables are identified by single curly-braces (i.e. {a}).  Formula
     * are identified by double curly-braces (i.e. {{a}}).  Variables and
     * formulas that are found are created if missing.  Any variable or
     * formula found is also made active, allowing the UI to distinguish
     * between variables that are defined but not used in the instructions.
     * Unused variables and formulas are deleted when the question is saved.
     * @param instructions
     */
    public void extractFromInstructions(ItemBean item) {
        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
        // set beans and variables inactive.
        // once the new ones are read in, they will be reactivated.
        // if a variable is not found on a new extract, it will be left inactive
        for (CalculatedQuestionFormulaBean bean : formulas.values()) {
            bean.setActive(false);
        }
        for (CalculatedQuestionVariableBean bean : variables.values()) {
            bean.setActive(false);
        }
        
        // create or activate formulas and variables
        extractFormulasFromInstructions(item);
        extractVariablesFromInstructions(item);       
        
    }
    
    private void extractFormulasFromInstructions(ItemBean item) {
        String instructions = item.getInstruction();
        GradingService gs = new GradingService();
        List<String> formulaNames = gs.extractFormulas(instructions);
        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
          
        // add any missing formulas
        for (String formulaName : formulaNames) {
            if (!formulas.containsKey(formulaName)) {
                CalculatedQuestionFormulaBean bean = new CalculatedQuestionFormulaBean();
                bean.setName(formulaName);
                bean.setSequence(new Long(variables.size() + formulas.size() + 1));
                item.getCalculatedQuestion().addFormula(bean);
            } else {
                CalculatedQuestionFormulaBean bean = formulas.get(formulaName);
                bean.setActive(true);
            }
        }                 
    }
    
    /**
     * extractVariablesFromInstructions examines the question instructions, pulls 
     * any variables that are not already defined as MatchItemBeans and adds them
     * to the list.
     */
    private void extractVariablesFromInstructions(ItemBean item) {
        String instructions = item.getInstruction();
        GradingService gs = new GradingService();
        List<String> variableNames = gs.extractVariables(instructions);
        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
        
        // add any missing variables
        for (String variableName : variableNames) {
            if (!variables.containsKey(variableName)) {
                CalculatedQuestionVariableBean bean = new CalculatedQuestionVariableBean();
                bean.setName(variableName);
                bean.setSequence(new Long(variables.size() + formulas.size() + 1));
                item.getCalculatedQuestion().addVariable(bean);
            } else {
                CalculatedQuestionVariableBean bean = variables.get(variableName);
                bean.setActive(true);
            }
        }         
    }

    /**
     * validateFormulas() iterates through all of the formula definitions.  It 
     * creates valid dummy values for all of the defined variables, substitutes those
     * variables into the formula, then executes the formula to determine if a value
     * is returned.  This is a syntax checker; a syntactically valid formula can
     * definitely return the wrong value if the author enters a wrong formula.
     * @param item
     * @return a map of errors.  The Key is an integer value, set by the SamigoExpressionParser, the
     * value is the string result of that error message.
     */
    private Map<Integer, String> validateFormulas(ItemBean item) {
        Map<Integer, String> errors = new HashMap<Integer, String>();
        GradingService service = new GradingService();
        SamigoExpressionParser parser = new SamigoExpressionParser();
        
        // list of variables to substitute
        Map<String, String> variableRangeMap = new HashMap<String, String>();
        for (CalculatedQuestionVariableBean variable : item.getCalculatedQuestion().getActiveVariables().values()) {
            String match = variable.getMin() + "|" + variable.getMax() + "," + variable.getDecimalPlaces();
            variableRangeMap.put(variable.getName(), match);
        }
        
        // dummy variables needed to generate random values within ranges for the variables
        long dummyItemId = 1;
        long dummyGradingId = 1;
        String dummyAgentId = "dummy";
        
        for (int attemptCnt = 0; attemptCnt < 100; attemptCnt++) {
        
            // create random values for the variables to substitute into the formulas
            Map<String, String> answersMap = service.determineRandomValuesForRanges(variableRangeMap, dummyItemId, 
                    dummyGradingId, dummyAgentId, attemptCnt);
            
            // evaluate each formula
            for (CalculatedQuestionFormulaBean formulaBean : item.getCalculatedQuestion().getActiveFormulas().values()) {
                String formulaStr = formulaBean.getText();
                formulaBean.setValidated(true);
                String substitutedFormulaStr = service.replaceMappedVariablesWithNumbers(formulaStr, answersMap);
                try {
                    if (isNegativeSqrt(substitutedFormulaStr)) {
                        formulaBean.setValidated(false);
                        errors.put(8, "Negative Squrare Root");
                    } else {
                        String numericAnswerString = parser.parse(substitutedFormulaStr);
                        if (!service.isAnswerValid(numericAnswerString)) {                                
                            throw new Exception("invalid answer, try again");
                        }
                    }
                } catch (SamigoExpressionError e) {
                    formulaBean.setValidated(false);
                    errors.put(Integer.valueOf(e.get_id()), e.get());
                } catch (Exception e) {
                    formulaBean.setValidated(false);
                    errors.put(500, e.getMessage());
                }
            }
            if (errors.size() > 0) {
                break;
            }
        }
        return errors;
    }
    
    /**
     * isNegativeSqrt() looks at the incoming expression and looks specifically
     * to see if it executes the SQRT function.  If it does, it evaluates it.  If
     * it has an error, it assumes that the SQRT function tried to evaluate a 
     * negative number and evaluated to NaN.
     * @param expression a mathematical formula, with all variables replaced by
     * real values, to be evaluated
     * @return true if the function uses the SQRT function, and the SQRT function
     * evaluates as an error; else false
     * @throws SamigoExpressionError if the evaluation of the SQRT function throws
     * som other parse error
     */
    private boolean isNegativeSqrt(String expression) throws SamigoExpressionError {
        final String SQRT = "sqrt(";
        boolean isNegative = false;
        GradingService service = new GradingService();
        
        expression = expression.toLowerCase();
        int startIndex = expression.indexOf(SQRT);
        if (startIndex > -1) {
            int endIndex = expression.indexOf(')', startIndex);
            String sqrtExpression = expression.substring(startIndex, endIndex + 1);
            SamigoExpressionParser parser = new SamigoExpressionParser();
            String numericAnswerString = parser.parse(sqrtExpression);
            if (!service.isAnswerValid(numericAnswerString)) {
                isNegative = true;
            }            
        }
        return isNegative;
    }
}
