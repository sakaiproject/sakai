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
**********************************************************************************/

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * CalculatedQuestionGlobalVariableBean stores the definition of a global variable for
 * a calculated question
 * @author jesusmmp
 */
public class CalculatedQuestionGlobalVariableBean implements Serializable, CalculatedQuestionAnswerIfc {

    private static final long serialVersionUID = 1747088544228808857L;
    @Getter @Setter private Long sequence;
    @Getter @Setter private String name;
    @Getter @Setter private String formula;
    @Getter @Setter private boolean active;
    @Getter @Setter private boolean addedButNotExtracted;
    private transient boolean validatedFormula;

    private static final String DEFAULT_FORMULA = "0";

    public CalculatedQuestionGlobalVariableBean() {
        this.formula = DEFAULT_FORMULA;
        this.active = true;
    }

    public CalculatedQuestionGlobalVariableBean(String name) {
        this.name = name;
        this.formula = DEFAULT_FORMULA;
        this.active = true;
    }

    public void setText(String formula) {
        this.formula = formula;
    }

    public String getText() {
        if (this.formula == null) {
            return DEFAULT_FORMULA;
        }
        return this.formula;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
    * setValidFormula() controls whether the formula has been successfully
    * validated for syntax and returns a real answer
    * @param validated true if the formula passes all syntax checks and returns
    * a valid value, otherwise false
    */
    public void setValidFormula(boolean validatedFormula) {
        this.validatedFormula = validatedFormula;
    }

   /**
    * getValidFormula() returns whether the formula has been successfully
    * validated for syntax and returns a real answer
    * @return true if the formula passes all syntax checks and returns a valid
    * value, otherwise false
    */
    public boolean getValidFormula() {
        return this.validatedFormula;
    }

    @Override
    public String getMatch() {
        return this.getText();
    }

}