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

package org.sakaiproject.tool.assessment.ui.bean.author;

import java.io.Serializable;

/**
 * CalculatedQuestionVariableBean stores the definition of a variable for
 * a calculated question.  In Calculated questions, variables can have
 * a range of values, which ensures that a test will be different each
 * time that it is taken. 
 * @author mgillian
 */
public class CalculatedQuestionVariableBean implements Serializable {

    private static final long serialVersionUID = -7835973840484043575L;
    private Long sequence;
    private String name;
    private double min;
    private double max;
    private String decimalPlaces;
    private boolean active;
    public static final String DEFAULT_DECIMAL_PLACES = "0";

    public CalculatedQuestionVariableBean() {
        this.decimalPlaces = DEFAULT_DECIMAL_PLACES;
        this.active = true;
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
    
    public void setMin(double min) {
        this.min = min;
    }
    
    public double getMin() {
        return this.min;
    }
    
    public void setMax(double max) {
        this.max = max;
    }
    
    public double getMax() {
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
}
