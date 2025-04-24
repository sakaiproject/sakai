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

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CalculatedQuestionFormulaBean contains the formula for the answer for a
 * Calculated question.  The formula contains references to CalculatedQuestionVariableBeans,
 * which allows a test to have different variables and answers each time that it is taken.
 * @author mgillian
 */
@Data
@NoArgsConstructor
public class CalculatedQuestionFormulaBean implements Serializable, CalculatedQuestionAnswerIfc {

    private static final long serialVersionUID = 1747088544228808857L;

    private Long sequence;
    private String name;
    private String formula = DEFAULT_FORMULA;
    private String tolerance = DEFAULT_TOLERANCE;
    private String decimalPlaces = DEFAULT_DECIMAL_PLACES;
    private String value = DEFAULT_VALUE;
    private String status = "OK";
    private boolean active = true;
    private boolean addedButNotExtracted;
    private transient boolean validatedFormula = true;
    private transient boolean validatedTolerance = true;

    private static final String DEFAULT_FORMULA = "0";
    private static final String DEFAULT_DECIMAL_PLACES = "3";
    private static final String DEFAULT_TOLERANCE = "0.01";
    private static final String DEFAULT_VALUE = "0";

    public void setText(String formula) {
        this.formula = formula;
    }

    public String getText() {
        return this.formula == null ? DEFAULT_FORMULA : this.formula;
    }

    public String getDecimalPlaces() {
        return this.decimalPlaces == null ? DEFAULT_DECIMAL_PLACES : this.decimalPlaces;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
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

    /**
     * setValidTolerance() controls whether the tolerance has been successfully
     * validated for syntax and returns a real answer
     * @param validatedTolerance
     */
    public void setValidTolerance(boolean validatedTolerance) {
        this.validatedTolerance = validatedTolerance;
    }

    /**
     * getValidTolerance() returns whether the tolerance has been successfully
     * validated for syntax a returns a real answer
     * @return true if tolerance passes all syntax checks and returns a valid
     * value, otherwise false
     */
    public boolean getValidTolerance() {
        return this.validatedTolerance;
    }

    public String getMatch() {
        return this.getText() + "|" + this.getTolerance() + "," + this.getDecimalPlaces();
    }
}
