/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 */
package org.sakaiproject.grading.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for the persistent GradingScale
 */
@Getter @Setter
public class GradingScaleDefinition implements Serializable {

    private static final long serialVersionUID = 1L;

    private String uid;
    private String name;
    private List<String> grades;

    /**
     * This was added specifically to support CXF and is a different pattern to the original
     */
    private List<Double> defaultBottomPercentsAsList;

    /**
     * The original map
     */
    private Map<String, Double> defaultBottomPercents;

    public void setDefaultBottomPercentsAsList(List<Object> defaultBottomPercentsList) {

        // Depending on how this was called, the list may
        // be of Double, String, emtpy String, or null objects. Convert the strings.
        List<Double> doubleScores = new ArrayList<Double>();
        for (Object obj : defaultBottomPercentsList) {
            if (obj instanceof String) {
                String str = (String)obj;
                if (str.trim().length() == 0) {
                    obj = null;
                } else {
                    obj = Double.valueOf((String)obj);
                }
            }
            doubleScores.add((Double)obj);
        }
        this.defaultBottomPercentsAsList = doubleScores;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
