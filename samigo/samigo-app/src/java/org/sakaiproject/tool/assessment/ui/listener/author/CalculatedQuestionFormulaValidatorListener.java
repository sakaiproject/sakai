package org.sakaiproject.tool.assessment.ui.listener.author;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;

import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionFormulaBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionGlobalVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.CalculatedQuestionVariableBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemAuthorBean;
import org.sakaiproject.tool.assessment.ui.bean.author.ItemBean;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;
import org.sakaiproject.tool.assessment.util.SamigoExpressionError;

public class CalculatedQuestionFormulaValidatorListener implements ActionListener {
    private static final String ERROR_MESSAGE_BUNDLE = "org.sakaiproject.tool.assessment.bundle.AuthorMessages";

    @Override
    public void processAction(ActionEvent event) {
        ItemAuthorBean itemAuthorBean = (ItemAuthorBean) ContextUtil.lookupBean("itemauthor");
        ItemBean item = itemAuthorBean.getCurrentItem();

        // make sure any last minute updates to instructions are handles
        // this also does the standard validations
        CalculatedQuestionExtractListener extractListener = new CalculatedQuestionExtractListener();
        List<String> errors = extractListener.validate(item, false);

        if (!errors.isEmpty()) {
            item.setOutcome("calculatedQuestion");
            for (String error : errors) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, error, null));
            }
        }

        CalculatedQuestionBean cqb = item.getCalculatedQuestion();
        GradingService service = new GradingService();
        calculateFormulasAndCalculations(cqb, service);
    }

    private void calculateFormulasAndCalculations(CalculatedQuestionBean cqb, GradingService service) {
        // list of variables to substitute
        Map<String, String> variableRangeMap = new HashMap<>();
        for (CalculatedQuestionVariableBean variable : cqb.getActiveVariables().values()) {
            String match = variable.getMin() + "|" + variable.getMax() + "," + variable.getDecimalPlaces();
            variableRangeMap.put(variable.getName(), match);
        }

        // mapping global variables with the formula
        Map<String, String> globalAnswersMap = new HashMap<>();
        for (CalculatedQuestionGlobalVariableBean globalVariable : cqb.getGlobalActiveVariables().values()) {
            globalAnswersMap.put(globalVariable.getName(), globalVariable.getText());
        }

        // create random values for the variables to substitute into the formulas (using dummy values) (random value)
        Map<String, String> answersMap = service.determineRandomValuesForRanges(variableRangeMap, 1, 1, "dummy", (int) (Math.random() * 100));

        // evaluate each formula
        for (CalculatedQuestionFormulaBean formulaBean : cqb.getActiveFormulas().values()) {
            String formulaStr = formulaBean.getText();

            try {
                String substitutedFormula = service.replaceMappedVariablesWithNumbers(formulaStr, answersMap);
                substitutedFormula = service.checkingEmptyGlobalVariables(substitutedFormula, answersMap, globalAnswersMap);
                String formulaValue = service.processFormulaIntoValue(substitutedFormula, 5);
                formulaValue = substitutedFormula + " = " + formulaValue;

                formulaBean.setValue(formulaValue);
                formulaBean.setStatus("OK");
                formulaBean.setValidFormula(true);
            } catch (SamigoExpressionError e) {
                formulaBean.setValue("?");
                String msg = getErrorMessage("samigo_formula_error_" + e.get_id());
                formulaBean.setStatus(msg);
            } catch (Exception e) {
                formulaBean.setValue("?");
                String msg = getErrorMessage("samigo_formula_error_500");
                formulaBean.setStatus(msg);
            }
        }

        // evaluate each calculation
        CalculatedQuestionExtractListener.evaluateCalculations(cqb, service, answersMap, globalAnswersMap, new ArrayList<>());
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
