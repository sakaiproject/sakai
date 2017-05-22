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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionCalculationBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionFormulaBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoExpressionError;

public class CalculatedQuestionExtractListener implements ActionListener{
    private static final String ERROR_MESSAGE_BUNDLE = "org.sakaiproject.tool.assessment.bundle.AuthorMessages";

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
                
        List<String> errors = this.validate(item,true);        
        
        if (errors.size() > 0) {
            item.setOutcome("calculatedQuestion");
            item.setPoolOutcome("calculatedQuestion");
            FacesContext context=FacesContext.getCurrentInstance();
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
     * <li>formulas that are syntactically wrong</li>
     * <li>formulas with no variables inside (only when saving a question)</li></ul>
     * Any errors are written to the context messager
     * <p>The validate formula is also called directly from the ItemAddListner, before
     * saving a calculated question, to ensure any last minute changes are caught.
     * @param item - an ItemBean, which contains all of the needed information 
     * about the CalculatedQuestion
     * @param extracting - boolean. True when extracting Variables a formulas, false when saving
     * a question.
     * @returns a List<String> of error messages to be displayed in the context messager.
     */
    public List<String> validate(ItemBean item,boolean extracting) {
        List<String> errors = new ArrayList<String>();

        // prepare any already existing variables and formula for new extracts
        this.initializeVariables(item);
        this.initializeFormulas(item);

        GradingService service = new GradingService();
        String instructions = item.getInstruction();
        List<String> formulaNames = service.extractFormulas(instructions);
        List<String> variableNames = service.extractVariables(instructions);

        errors.addAll(validateExtractedNames(variableNames, formulaNames));

        // add new variables and formulas
        // verify that at least one variable and formula are defined
        if (errors.size() == 0) {
            errors.addAll(createFormulasFromInstructions(item, formulaNames));
            errors.addAll(createVariablesFromInstructions(item, variableNames));
            errors.addAll(createCalculationsFromInstructions(item.getCalculatedQuestion(), instructions, service));
        }

        // validate variable min and max and formula tolerance
        if (errors.size() == 0) {
            errors.addAll(validateMinMax(item));
            errors.addAll(validateTolerance(item));
        }

        // don't bother looking at formulas if any data validations have failed
        if (errors.size() == 0) {
            errors.addAll(validateFormulas(item, service,extracting));
            errors.addAll(validateCalculations(item.getCalculatedQuestion(), service));
        } else {
            errors.add(getErrorMessage("formulas_not_validated"));
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
            bean.setValidMax(true);
            bean.setValidMin(true);
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
            bean.setValidFormula(true);
            bean.setValidTolerance(true);
        }
    }

    private Long getMaxSequenceValue(Map<String, CalculatedQuestionVariableBean> variables, Map<String, CalculatedQuestionFormulaBean> formulas){
    	Long maxValue = 0L;

    	for(CalculatedQuestionVariableBean variable:variables.values()){
    		Long currentSequence = variable.getSequence(); 
    		if(currentSequence != null && currentSequence.compareTo(maxValue)>0)
    			maxValue = currentSequence;
    	}
    	for(CalculatedQuestionFormulaBean formula:formulas.values()){
    		Long currentSequence = formula.getSequence(); 
    		if(currentSequence != null && currentSequence.compareTo(maxValue)>0)
    			maxValue = currentSequence;
    	}
    	return maxValue;
    }

    /**
     * createFormulasFromInstructions adds any formulas that exist in the list of formulaNames
     * but do not already exist in the question
     * @param item
     * @param formulaNames
     */
    private List<String> createFormulasFromInstructions(ItemBean item, List<String> formulaNames) {
        List<String> errors = new ArrayList<String>();
        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
        Long maxSequenceValue = getMaxSequenceValue(variables, formulas);
        
        // add any missing formulas
        for (String formulaName : formulaNames) {
            if (!formulas.containsKey(formulaName)) {
                CalculatedQuestionFormulaBean bean = new CalculatedQuestionFormulaBean();
                bean.setName(formulaName);
                bean.setSequence(++maxSequenceValue);
                item.getCalculatedQuestion().addFormula(bean);
            } else {
                CalculatedQuestionFormulaBean bean = formulas.get(formulaName);
                bean.setActive(true);
            }
        }
        if (item.getCalculatedQuestion().getActiveFormulas().size() == 0) {
            errors.add(getErrorMessage("no_formulas_defined"));
        }
        return errors;
    }

    /**
     * createVariablesFromInstructions adds any variables that exist in the list
     * of variableNames but do not already exist in the question
     * @param item
     * @param variableNames
     */
    private List<String> createVariablesFromInstructions(ItemBean item, List<String> variableNames) {
        List<String> errors = new ArrayList<String>();
        Map<String, CalculatedQuestionFormulaBean> formulas = item.getCalculatedQuestion().getFormulas();
        Map<String, CalculatedQuestionVariableBean> variables = item.getCalculatedQuestion().getVariables();
        Long maxSequenceValue = getMaxSequenceValue(variables, formulas);
        
        // add any missing variables
        for (String variableName : variableNames) {
            if (!variables.containsKey(variableName)) {
                CalculatedQuestionVariableBean bean = new CalculatedQuestionVariableBean();
                bean.setName(variableName);
                bean.setSequence(++maxSequenceValue);
                item.getCalculatedQuestion().addVariable(bean);
            } else {
                CalculatedQuestionVariableBean bean = variables.get(variableName);
                bean.setActive(true);
            }
        }
        if (item.getCalculatedQuestion().getActiveVariables().size() == 0) {
            errors.add(getErrorMessage("no_variables_defined"));
        }
        return errors;
    }

    /**
     * Finds the calculations in the instructions and places them in the CalculatedQuestionBean
     * (destroys anything that was already there)
     * 
     * @param calculatedQuestionBean
     * @param instructions
     * @param service
     * @return list of error messages (empty if there are none)
     */
    static List<String> createCalculationsFromInstructions(CalculatedQuestionBean calculatedQuestionBean, String instructions, GradingService service) {
        List<String> errors = new ArrayList<String>();
        calculatedQuestionBean.clearCalculations(); // reset the current set and extract a new one
        List<String> calculations = service.extractCalculations(instructions);
        if (!calculations.isEmpty()) {
            for (String calculation : calculations) {
                CalculatedQuestionCalculationBean calc = new CalculatedQuestionCalculationBean(calculation);
                calculatedQuestionBean.addCalculation(calc);
            }
        }
        return errors;
    }

    /**
     * validateExtractedNames looks through all of the variable and formula names defined
     * in the instructions and determines if the names are valid, and if the formula and
     * variable names overlap.
     * @param item
     * @return a list of validation errors to display
     */
    private List<String> validateExtractedNames(List<String> variableNames, List<String> formulaNames) {
        List<String> errors = new ArrayList<String>();

        // formula validations
        for (String formula : formulaNames) {
            if (formula == null || formula.length() == 0) {
                errors.add(getErrorMessage("formula_name_empty"));
            } else {
                if (!formula.matches("[a-zA-Z]\\w*")) {
                    errors.add(getErrorMessage("formula_name_invalid"));
                }
            }
        }

        // variable validations
        for (String variable : variableNames) {
            if (variable == null || variable.length() == 0) {
                errors.add(getErrorMessage("variable_name_empty"));
            } else {
                if (!variable.matches("[a-zA-Z]\\w*")) {
                    errors.add(getErrorMessage("variable_name_invalid"));
                }
            }
        }

        // throw an error if variables and formulas share any names
        // don't continue processing if there are problems with the extract
        if (!Collections.disjoint(formulaNames, variableNames)) {
            errors.add(getErrorMessage("unique_names"));
        }
        return errors;
    }

    /**
     * validateTolerance() verifies that formula tolerances are positive numbers
     * @param item
     * @return a list of validation errors to display
     */
    private List<String> validateTolerance(ItemBean item) {
        List<String> errors = new ArrayList<String>();
        CalculatedQuestionBean question = item.getCalculatedQuestion();

        // formula tolerances must be numbers or percentages
        for (CalculatedQuestionFormulaBean formula : question.getActiveFormulas().values()) {
            String toleranceStr = formula.getTolerance().trim();

            // cannot be blank
            if (toleranceStr == null || toleranceStr.length() == 0) {
                errors.add(getErrorMessage("empty_field"));
                formula.setValidTolerance(false);
            }

            // no non-number characters (although percentage is allowed
            // allow a negative here, we'll catch negative tolerances in another place
            // we add scientific notation numbers            
            if (formula.getValidTolerance()) {
                if ( (!toleranceStr.matches("[0-9\\.\\-\\%]+") ) &&
                   	 (!toleranceStr.matches("\\-?[0-9]+\\.?[0-9]*([eE][\\-]?[0-9]+)?") ) ) {
                    errors.add(getErrorMessage("invalid_tolerance"));
                    formula.setValidTolerance(false);
                }
            }

            // if not a percentage, try to convert to a double to validate
            // format
            if (formula.getValidTolerance()) {
                if (!toleranceStr.matches("[0-9]+\\.?[0-9]*\\%")) {
                    try {
                        double tolerance = Double.parseDouble(toleranceStr);

                        // this strips out any leading spaces or zeroes
                        formula.setTolerance(Double.toString(tolerance));

                        if (tolerance < 0) {
                            errors.add(getErrorMessage("tolerance_negative"));
                            formula.setValidTolerance(false);
                        }
                    } catch (NumberFormatException n) {
                        errors.add(getErrorMessage("invalid_tolerance"));
                        formula.setValidTolerance(false);
                    }
                }
            }
        }
        return errors;
    }

    /**
     * validateMinMax() looks at each variable and ensures that the 
     * min and max are valid numbers.  It also verifies that the min is less 
     * than the max.
     * @param item
     * @return a list of validation errors to display
     */
    private List<String> validateMinMax(ItemBean item) {
        List<String> errors = new ArrayList<String>();
        CalculatedQuestionBean question = item.getCalculatedQuestion();
        GradingService gradingService = new GradingService();

        for (CalculatedQuestionVariableBean variable : question.getActiveVariables().values()) {

            // decimal
            String decimalStr = variable.getDecimalPlaces().trim();
            int decimals;
            try {
                decimals = Integer.parseInt(decimalStr);
            } catch (NumberFormatException e) {
                decimals = 2; // default when decimals is not known
            }

            // min
            String minStr = variable.getMin().trim();
            double min = 0d;
            if (minStr == null || minStr.length() == 0) {
                errors.add(getErrorMessage("empty_field"));
                variable.setValidMin(false);
            }
            if (variable.getValidMin()) {
                try {
                    BigDecimal bd = new BigDecimal(minStr);                    
                    bd.setScale(decimals,RoundingMode.HALF_UP);
                    min = bd.doubleValue();                                 
                    String value = gradingService.toScientificNotation(bd.toPlainString(),decimals);
                    variable.setMin(value);
                } catch (NumberFormatException n) {
                    errors.add(getErrorMessage("invalid_min"));
                    variable.setValidMin(false);
                } catch (ArithmeticException n) {
                    errors.add(getErrorMessage("invalid_min_scale"));
                    variable.setValidMin(false);
                }
            }

            // max
            String maxStr = variable.getMax().trim();
            double max = 0d;
            if (maxStr == null || maxStr.length() == 0) {
                errors.add(getErrorMessage("empty_field"));
                variable.setValidMax(false);
            }
            if (variable.getValidMax()) {
                try {
                    BigDecimal bd = new BigDecimal(maxStr);                   
                    bd.setScale(decimals,RoundingMode.HALF_UP);
                    max = bd.doubleValue();
                    String value = gradingService.toScientificNotation(bd.toPlainString(),decimals);
                    variable.setMax(value);
                } catch (NumberFormatException n) {
                    errors.add(getErrorMessage("invalid_max"));
                    variable.setValidMax(false);
                } catch (ArithmeticException n) {
                    errors.add(getErrorMessage("invalid_max_scale"));
                    variable.setValidMax(false);
                }
            }

            // max < min
            if (variable.getValidMax() && variable.getValidMin()) {
                if (max < min) {
                    errors.add(getErrorMessage("max_less_than_min"));
                    variable.setValidMin(false);
                    variable.setValidMax(false);
                }
            }
        }
        return errors;
    }

    /**
     * Takes a populated calculatedQuestionBean and validates the calculations and populates the
     * calculation values and sample data (and status)
     * 
     * @param calculatedQuestionBean
     * @param service
     * @return a list of validation errors to display
     */
    static List<String> validateCalculations(CalculatedQuestionBean calculatedQuestionBean, GradingService service) {
        List<String> errors = new ArrayList<String>();
        if (service == null) {
            service = new GradingService();
        }

        // list of variables to substitute
        Map<String, String> variableRangeMap = new HashMap<String, String>();
        for (CalculatedQuestionVariableBean variable : calculatedQuestionBean.getActiveVariables().values()) {
            String match = variable.getMin() + "|" + variable.getMax() + "," + variable.getDecimalPlaces();
            variableRangeMap.put(variable.getName(), match);
        }

        int attemptCnt = 0;
        while (attemptCnt < 100 && errors.size() == 0) {
            // create random values for the variables to substitute into the formulas (using dummy values)
            Map<String, String> answersMap = service.determineRandomValuesForRanges(variableRangeMap, 1, 1, "dummy", attemptCnt);

            // evaluate each calculation
            for (CalculatedQuestionCalculationBean cqcb : calculatedQuestionBean.getCalculationsList()) {
                String formulaStr = GradingService.cleanFormula(cqcb.getText());
                if (formulaStr == null || formulaStr.length() == 0) {
                    String msg = getErrorMessage("empty_field");
                    cqcb.setStatus(msg);
                    errors.add(msg);
                } else {
                    String substitutedFormulaStr = service.replaceMappedVariablesWithNumbers(formulaStr, answersMap);
                    cqcb.setFormula(substitutedFormulaStr);
                    // look for wrapped variables that haven't been replaced (undefined variable)
                    List<String> unwrappedVariables = service.extractVariables(substitutedFormulaStr);
                    if (unwrappedVariables.size() > 0) {
                        String msg = getErrorMessage("samigo_formula_error_9");
                        cqcb.setStatus(msg);
                        errors.add(msg + " :"+substitutedFormulaStr);
                    } else {
                        try {
                            if (service.isNegativeSqrt(substitutedFormulaStr)) {
                                String msg = getErrorMessage("samigo_formula_error_8");
                                cqcb.setStatus(msg);
                                errors.add(msg + " :"+substitutedFormulaStr);
                            } else {
                                String formulaValue = service.processFormulaIntoValue(substitutedFormulaStr, 5); // throws exceptions if formula is invalid
                                cqcb.setValue(formulaValue);
                                cqcb.setText(formulaStr);
                            }
                        } catch (SamigoExpressionError e) {
                            String msg = getErrorMessage("samigo_formula_error_" + Integer.valueOf(e.get_id()));
                            cqcb.setStatus(msg);
                            errors.add(msg + " :"+substitutedFormulaStr);
                        } catch (Exception e) {
                            String msg = getErrorMessage("samigo_formula_error_500");
                            cqcb.setStatus(msg);
                            errors.add(msg + " :"+substitutedFormulaStr);
                        }
                    }
                }
            }
            attemptCnt++;
        }
        return errors;
    }

    /**
     * validateFormulas() iterates through all of the formula definitions.  It 
     * creates valid dummy values for all of the defined variables, substitutes those
     * variables into the formula, then executes the formula to determine if a value
     * is returned.  This is a syntax checker; a syntactically valid formula can
     * definitely return the wrong value if the author enters a wrong formula.
     * @param item
     * @param extracting - boolean. True when extracting Variables a formulas, false when saving
     * a question
     * @return a map of errors.  The Key is an integer value, set by the SamigoExpressionParser, the
     * value is the string result of that error message.
     */
    private List<String> validateFormulas(ItemBean item, GradingService service,boolean extracting) {
        List<String> errors = new ArrayList<String>();
        if (service == null) {
            service = new GradingService();
        }
        
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
        
        int attemptCnt = 0;
        while (attemptCnt < 100 && errors.size() == 0) {
        
            // create random values for the variables to substitute into the formulas
            Map<String, String> answersMap = service.determineRandomValuesForRanges(variableRangeMap, dummyItemId, 
                    dummyGradingId, dummyAgentId, attemptCnt);
            
            // evaluate each formula
            for (CalculatedQuestionFormulaBean formulaBean : item.getCalculatedQuestion().getActiveFormulas().values()) {
                String formulaStr = formulaBean.getText();
                
                if (formulaStr == null || formulaStr.length() == 0) {
                    formulaBean.setValidFormula(false);
                    errors.add(getErrorMessage("empty_field"));
                }else if ( !extracting && !formulaStr.contains("{")){
                    formulaBean.setValidFormula(false);
                    errors.add(getErrorMessage("no_variables_formula") + " : " + formulaBean.getName() + " = "+ formulaStr);                    
                } else {
                    String substitutedFormulaStr = service.replaceMappedVariablesWithNumbers(formulaStr, answersMap);
                    
                    // look for wrapped variables that haven't been replaced (undefined variable)
                    List<String> unwrappedVariables = service.extractVariables(substitutedFormulaStr);
                    if (unwrappedVariables.size() > 0) {
                        formulaBean.setValidFormula(false);
                        errors.add(getErrorMessage("samigo_formula_error_9") + " :"+substitutedFormulaStr);
                    } else {
                        try {
                            if (service.isNegativeSqrt(substitutedFormulaStr)) {
                                formulaBean.setValidFormula(false);
                                errors.add(getErrorMessage("samigo_formula_error_8") + " :"+substitutedFormulaStr);
                            } else {
                                service.processFormulaIntoValue(substitutedFormulaStr, 5); // throws exceptions on failure
                            }
                        } catch (SamigoExpressionError e) {
                            formulaBean.setValidFormula(false);
                            String msg = getErrorMessage("samigo_formula_error_" + Integer.valueOf(e.get_id()));
                            errors.add(msg + " :"+substitutedFormulaStr);
                        } catch (Exception e) {
                            formulaBean.setValidFormula(false);
                            errors.add(getErrorMessage("samigo_formula_error_500") + " :"+substitutedFormulaStr);
                        }
                    }
                }
            }
            attemptCnt++;
        }
        return errors;
    }
    
    /**
     * getErrorMessage() retrieves the localized error message associated with
     * the errorCode
     * @param errorCode
     * @return
     */
    private static String getErrorMessage(String errorCode) {
        String err = ContextUtil.getLocalizedString(ERROR_MESSAGE_BUNDLE, 
                errorCode);
        return err;
    }

}
