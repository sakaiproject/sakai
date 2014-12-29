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

/**
 * CalculatedQuestionFormulaBean contains the formula for the answer for a
 * Calculated question.  The formula contains references to CalculatedQuestionVariableBeans,
 * which allows a test to have different variables and answers each time that it is taken.
 * @author mgillian
 */
public class CalculatedQuestionFormulaBean implements Serializable, CalculatedQuestionAnswerIfc {

    private static final long serialVersionUID = 1747088544228808857L;
    private Long sequence;
    private String name;
    private String formula;
    private String tolerance;
    private String decimalPlaces;
    private boolean active;
    private transient boolean validatedFormula;
    private transient boolean validatedTolerance;
    
    private static final String DEFAULT_FORMULA = "0";
    private static final String DEFAULT_DECIMAL_PLACES = "3";
    private static final String DEFAULT_TOLERANCE = "0.01";
    
    public CalculatedQuestionFormulaBean() {
        this.formula = DEFAULT_FORMULA;
        this.decimalPlaces = DEFAULT_DECIMAL_PLACES;
        this.tolerance = DEFAULT_TOLERANCE;
        this.active = true;
        this.validatedFormula = true;
        this.validatedTolerance = true;
    }

    public void setSequence(Long sequence) {
        this.sequence = sequence;
    }
    
    public Long getSequence() {
        return this.sequence;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
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

    public void setTolerance(String tolerance) {
        this.tolerance = tolerance;
    }

    public String getTolerance() {
        return this.tolerance;
    }

    public void setDecimalPlaces(String decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public String getDecimalPlaces() {
        if (this.decimalPlaces == null) {
            return DEFAULT_DECIMAL_PLACES;
        }
        return this.decimalPlaces;
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
        String match = this.getText() + "|" + 
                this.getTolerance() + "," + 
                this.getDecimalPlaces();
        return match;
    }
}
