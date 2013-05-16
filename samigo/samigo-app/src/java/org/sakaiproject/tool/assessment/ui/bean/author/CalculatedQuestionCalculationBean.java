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

import org.apache.commons.lang.StringUtils;

/**
 * Contains the fields related to a calculation (which is just a formula that is directly evaluated as part of the question)
 * 
 * @author Aaron Zeckoski (azeckoski @ vt.edu)
 */
public class CalculatedQuestionCalculationBean implements Serializable {

    private static final long serialVersionUID = 1747088544228808857L;

    private String text;
    private String formula;
    private String value;
    private String status = "OK";

    /**
     * Don't use this one
     */
    public CalculatedQuestionCalculationBean() {}

    /**
     * @param text (e.g. {A}+{B}+{C})
     */
    public CalculatedQuestionCalculationBean(String text) {
        if (text == null || "".equals(text)) {
            throw new IllegalArgumentException("calculation text must be set");
        }
        this.text = text;
        this.formula = "?";
        this.value = "?";
    }

    /**
     * @param text (e.g. {A}+{B}+{C})
     * @param formula after variable replacement (e.g. 1+2+3)
     * @param value value of the formula after evaluation (e.g. 6)
     * @param status status of the formula evaluation (from the parser), (e.g. OK)
     */
    public CalculatedQuestionCalculationBean(String text, String formula, String value, String status) {
        this(text);
        if (formula != null) this.formula = formula;
        if (value != null) this.value = value;
        if (status != null) this.status = status;
    }

    /**
     * @return the text (e.g. {A}+{B}+{C})
     */
    public String getText() {
        return text;
    }

    /**
     * @param text (e.g. {A}+{B}+{C})
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return the formula after variable replacement (e.g. 1+2+3)
     */
    public String getFormula() {
        return formula;
    }

    /**
     * @param formula the formula after variable replacement (e.g. 1+2+3)
     */
    public void setFormula(String formula) {
        this.formula = formula;
    }

    /**
     * @return the value of the formula after evaluation (e.g. 6)
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value of the formula after evaluation (e.g. 6)
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the status of the formula evaluation (from the parser)
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status of the formula evaluation (from the parser)
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
