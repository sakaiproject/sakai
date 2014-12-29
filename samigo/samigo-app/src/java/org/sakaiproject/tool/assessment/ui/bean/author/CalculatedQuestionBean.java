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

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;
import org.apache.commons.collections.comparators.NullComparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class CalculatedQuestionBean implements Serializable {

    private static final long serialVersionUID = -5567978724454506570L;
    private Map<String, CalculatedQuestionFormulaBean> formulas;
    private Map<String, CalculatedQuestionVariableBean> variables;
    private List<CalculatedQuestionCalculationBean> calculations;

    public CalculatedQuestionBean() {
        formulas = new HashMap<String, CalculatedQuestionFormulaBean>();
        variables = new HashMap<String, CalculatedQuestionVariableBean>();
        calculations = new Vector<CalculatedQuestionCalculationBean>();
    }

    public Map<String, CalculatedQuestionFormulaBean> getFormulas() {
        return formulas;
    }

    /**
     * getActiveVariables() returns a map of all variables that return getActive() of true
     * @return a map of all variables that return getActive() of true.  If no variables are active,
     * the map will have zero elements.
     */
    public Map<String, CalculatedQuestionFormulaBean> getActiveFormulas() {
        Map<String, CalculatedQuestionFormulaBean> results = new HashMap<String, CalculatedQuestionFormulaBean>();
        for (CalculatedQuestionFormulaBean formula : formulas.values()) {
            if (formula.getActive()) {
                results.put(formula.getName(), formula);
            }
        }
        return results;
    }

    /**
     * getFormulasList returns a List of all formulas, sorted by formula name
     * @return
     */
    public List<CalculatedQuestionFormulaBean> getFormulasList() {
        List<CalculatedQuestionFormulaBean> beanList = new ArrayList<CalculatedQuestionFormulaBean>(formulas.values());
        Collections.sort(beanList, new Comparator<CalculatedQuestionFormulaBean>() {
            public int compare(CalculatedQuestionFormulaBean bean1, CalculatedQuestionFormulaBean bean2) {
                return new NullComparator().compare(bean1.getName(), bean2.getName());
            }
        });
        return beanList;
    }

    public void addFormula(CalculatedQuestionFormulaBean formula) {
        formulas.put(formula.getName(), formula);
    }

    public CalculatedQuestionFormulaBean getFormula(String name) {
        return formulas.get(name);
    }

    public void removeFormula(String name) {
        formulas.remove(name);
    }

    public Map<String, CalculatedQuestionVariableBean> getVariables() {
        return variables;
    }

    /**
     * getActiveVariables() returns a map of all variables that return getActive() of true
     * @return a map of all variables that return getActive() of true.  If no variables are active,
     * the map will have zero elements.
     */
    public Map<String, CalculatedQuestionVariableBean> getActiveVariables() {
        Map<String, CalculatedQuestionVariableBean> results = new HashMap<String, CalculatedQuestionVariableBean>();
        for (CalculatedQuestionVariableBean variable : variables.values()) {
            if (variable.getActive()) {
                results.put(variable.getName(), variable);
            }
        }
        return results;
    }

    /**
     * getVariablesList returns a List of all variables, sorted by variable name
     * @return
     */
    public List<CalculatedQuestionVariableBean> getVariablesList() {
        List<CalculatedQuestionVariableBean> beanList = new ArrayList<CalculatedQuestionVariableBean>(variables.values());
        Collections.sort(beanList, new Comparator<CalculatedQuestionVariableBean>() {
            public int compare(CalculatedQuestionVariableBean bean1, CalculatedQuestionVariableBean bean2) {
                return new NullComparator().compare(bean1.getName(), bean2.getName());
            }
        });
        return beanList;
    }

    public void addVariable(CalculatedQuestionVariableBean variable) {
        variables.put(variable.getName(), variable);
    }

    public CalculatedQuestionVariableBean getVariable(String name) {
        return variables.get(name);
    }

    public void removeVariable(String name) {
        variables.remove(name);
    }

    // CALCULATIONS

    public List<CalculatedQuestionCalculationBean> getCalculationsList() {
        return this.calculations;
    }

    public boolean isHasCalculations() {
        return !this.calculations.isEmpty();
    }

    public void addCalculation(CalculatedQuestionCalculationBean calc) {
        this.calculations.add(calc);
    }

    public void clearCalculations() {
        this.calculations.clear();
    }

}