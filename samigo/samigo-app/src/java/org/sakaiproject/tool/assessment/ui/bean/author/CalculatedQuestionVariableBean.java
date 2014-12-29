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
 * CalculatedQuestionVariableBean stores the definition of a variable for
 * a calculated question.  In Calculated questions, variables can have
 * a range of values, which ensures that a test will be different each
 * time that it is taken. 
 * @author mgillian
 */
public class CalculatedQuestionVariableBean implements Serializable, CalculatedQuestionAnswerIfc {

    private static final long serialVersionUID = -7835973840484043575L;
    private Long sequence;
    private String name;
    private String min;
    private String max;
    private String decimalPlaces;
    private boolean active;
    private transient boolean validMin;
    private transient boolean validMax;
    public static final String DEFAULT_DECIMAL_PLACES = "3";
    public static final String DEFAULT_MIN = "0";
    public static final String DEFAULT_MAX = "0";

    public CalculatedQuestionVariableBean() {
        this.decimalPlaces = DEFAULT_DECIMAL_PLACES;
        this.active = true;
        this.min = DEFAULT_MIN;
        this.max = DEFAULT_MAX;
        this.validMin = true;
        this.validMax = true;
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
    
    public void setMin(String min) {
        this.min = min;
    }
    
    public String getMin() {
        return this.min;
    }
    
    public void setMax(String max) {
        this.max = max;
    }
    
    public String getMax() {
        return this.max;
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
     * setValidMin() sets whether the min value has been calculated correctly
     * @param validMin
     */
    public void setValidMin(boolean validMin) {
        this.validMin = validMin;
    }
    
    /**
     * getValidMin() returns whether the min has been calculated correctly
     * @return
     */
    public boolean getValidMin() {
        return this.validMin;
    }
    
    /**
     * setValidMax() sets whether the max value has been calculated correctly;
     * @param validMax
     */
    public void setValidMax(boolean validMax) {
        this.validMax = validMax;
    }
    
    /**
     * getValidMax() returns whether the max value has been calculated correctly.
     * @return
     */
    public boolean getValidMax() {
        return this.validMax;
    }
    
    public String getMatch() {
        String match = this.getMin() + "|" + 
                this.getMax() + "," + 
                this.getDecimalPlaces();
        return match;
        
    }
}
