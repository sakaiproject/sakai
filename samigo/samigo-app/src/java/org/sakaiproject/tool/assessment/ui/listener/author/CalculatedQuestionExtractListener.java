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
        List<String> errors = new ArrayList<String>();
        
        // prepare any already existing variables and formula for new extracts
        this.initializeVariables(item);
        this.initializeFormulas(item);
        
        // extract variable and formula names defined in the instructions
        String instructions = item.getInstruction();
        GradingService gs = new GradingService();
        List<String> formulaNames = gs.extractFormulas(instructions);
        List<String> variableNames = gs.extractVariables(instructions);

        // throw an error if variables and formulas share any names
        // don't continue processing if there are problems with the extract
        if (!Collections.disjoint(formulaNames, variableNames)) {
            String err=ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages","unique_names");
            errors.add(err);
        } else {
        
            
            // add any new variables/formulas
            createFormulasFromInstructions(item, formulaNames);
            createVariablesFromInstructions(item, variableNames);       
            
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
                        
            // variable mins and maxes must be numbers, and max must be greater than min
            for (CalculatedQuestionVariableBean variable : question.getActiveVariables().values()) {
                double min = 0d;
                try {
                    min = Double.parseDouble(variable.getMin());
                } catch (NumberFormatException n) {
                    String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "invalid_min");
                    errors.add(err);                    
                }
                double max = 0d;
                try {
                    max = Double.parseDouble(variable.getMax());
                } catch (NumberFormatException n) {
                    String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", "invalid_max");
                    errors.add(err);                    
                }
                if (max < min) {
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
            
            // don't bother looking at formulas if any data validations have failed.
            if (errors.size() == 0) {
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
            } else {
                String err = ContextUtil.getLocalizedString("org.sakaiproject.tool.assessment.bundle.AuthorMessages", 
                        "formulas_not_validated");
                errors.add(err);
            }
        }
        return errors;
    }
    
    /**
     * initializeVariables() prepares any previously defined variables for updates
     * that occur when extracting new variables from instructions
     * @param item
     */
    private void initializeVariables(ItemBean item) {
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
        for (CalculatedQuestionVariableBean bean : variables.values()) {
            bean.setActive(false);
        }        
    }
    
    /**
     * initializeFormulas() prepares any previously defined formulas for updates
     * that occur when extracting new formulas from instructions 
     * @param item
     */
    private void initializeFormulas(ItemBean item) {
        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        for (CalculatedQuestionFormulaBean bean : formulas.values()) {
            bean.setActive(false);
            bean.setValidated(true);
        }        
    }
    
    /**
     * createFormulasFromInstructions adds any formulas that exist in the list of formulaNames
     * but do not already exist in the question
     * @param item
     * @param formulaNames
     */
    private void createFormulasFromInstructions(ItemBean item, List<String> formulaNames) {
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
     * createVariablesFromInstructions adds any variables that exist in the list of variableNames
     * but do not already exist in the question
     * @param item
     * @param variableNames
     */
    private void createVariablesFromInstructions(ItemBean item, List<String> variableNames) {
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
                
                // look for wrapped variables that haven't been replaced (undefined variable)
                List<String> unwrappedVariables = service.extractVariables(substitutedFormulaStr);
                if (unwrappedVariables.size() > 0) {
                    formulaBean.setValidated(false);
                    errors.put(9, "Wrapped variable not found");
                } else {
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
