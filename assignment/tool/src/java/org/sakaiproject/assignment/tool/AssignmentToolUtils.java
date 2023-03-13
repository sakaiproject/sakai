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
package org.sakaiproject.assignment.tool;

import org.apache.commons.lang3.StringUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class AssignmentToolUtils {

    private static FormattedText formattedText;

    static {
        formattedText = ComponentManager.get(FormattedText.class);
    }

    private static ResourceLoader rb = new ResourceLoader("assignment");

    /**
     * Tests the format of the supplied grade and sets alert messages in the
     * state as required.
     */
    public static List<String> validPointGrade(final String grade, int factor) {

        List<String> alerts = new ArrayList<String>();

        if (grade != null && !"".equals(grade)) {
            if (grade.startsWith("-")) {
                // check for negative sign
                alerts.add(rb.getString("plesuse3"));
            } else {
                int dec = (int) Math.log10(factor);
                NumberFormat nbFormat = formattedText.getNumberFormat();
                String decSeparator = formattedText.getDecimalSeparator();

                // only the right decimal separator is allowed and no other grouping separator
                if ((",".equals(decSeparator) && grade.contains("."))
                        || (".".equals(decSeparator) && grade.contains(","))
                        || grade.contains(" ")) {
                    alerts.add(rb.getString("plesuse1"));
                    return alerts;
                }

                // parse grade from localized number format
                int index = grade.indexOf(decSeparator);
                if (index != -1) {
                    // when there is decimal points inside the grade, scale the number by "factor"
                    // but only one decimal place is supported
                    // for example, change 100.0 to 1000
                    if (!decSeparator.equals(grade)) {
                        if (grade.length() > index + dec + 1) {
                            // if there are more than "factor" decimal points
                            alerts.add(rb.getFormattedMessage("plesuse2", String.valueOf(dec)));
                        } else {
                            // decimal points is the only allowed character inside grade
                            // replace it with '1', and try to parse the new String into int
                            String zeros = "";
                            for (int i = 0; i < dec; i++) {
                                zeros = zeros.concat("0");
                            }
                            String gradeString = grade.endsWith(decSeparator) ? grade.substring(0, index).concat(zeros) :
                                    grade.substring(0, index).concat(grade.substring(index + 1));
                            try {
                                nbFormat.parse(gradeString);
                                try {
                                    Integer.parseInt(gradeString);
                                } catch (NumberFormatException e) {
                                    //log.warn(this + ":validPointGrade " + e.getMessage());
                                    alerts.addAll(alertInvalidPoint(gradeString, factor));
                                }
                            } catch (ParseException e) {
                                //log.warn(this + ":validPointGrade " + e.getMessage());
                                alerts.add(rb.getString("plesuse1"));
                            }
                        }
                    } else {
                        // grade is decSeparator
                        alerts.add(rb.getString("plesuse1"));
                    }
                } else {
                    // There is no decimal point; should be int number
                    String gradeString = grade;
                    for (int i = 0; i < dec; i++) {
                        gradeString = gradeString.concat("0");
                    }
                    try {
                        nbFormat.parse(gradeString);
                        try {
                            Integer.parseInt(gradeString);
                        } catch (NumberFormatException e) {
                            //log.warn(this + ":validPointGrade " + e.getMessage());
                            alerts.addAll(alertInvalidPoint(gradeString, factor));
                        }
                    } catch (ParseException e) {
                        //log.warn(this + ":validPointGrade " + e.getMessage());
                        alerts.add(rb.getString("plesuse1"));
                    }
                }
            }
        }

        return alerts;
    }

    public static List<String> alertInvalidPoint(String grade, int factor) {

        List<String> alerts = new ArrayList<>();

        String decSeparator = formattedText.getDecimalSeparator();

        String VALID_CHARS_FOR_INT = "-01234567890";

        boolean invalid = false;
        // case 1: contains invalid char for int
        for (int i = 0; i < grade.length() && !invalid; i++) {
            char c = grade.charAt(i);
            if (VALID_CHARS_FOR_INT.indexOf(c) == -1) {
                invalid = true;
            }
        }
        if (invalid) {
            alerts.add(rb.getString("plesuse1"));
        } else {
            int dec = (int) Math.log10(factor);
            int maxInt = Integer.MAX_VALUE / factor;
            int maxDec = Integer.MAX_VALUE - maxInt * factor;
            // case 2: Due to our internal scaling, input String is larger than Integer.MAX_VALUE/10
            alerts.add(rb.getFormattedMessage("plesuse4", grade.substring(0, grade.length() - dec)
                    + decSeparator + grade.substring(grade.length() - dec), maxInt + decSeparator + maxDec));
        }

        return alerts;
    }

    /**
     * scale the point value by "factor" if there is a valid point grade
     */
    public static String scalePointGrade(String point, int factor, List<String> alerts) {

        String decSeparator = formattedText.getDecimalSeparator();
        int dec = (int) Math.log10(factor);

        alerts.addAll(validPointGrade(point, factor));

        if (point != null && (point.length() >= 1)) {
            // when there is decimal points inside the grade, scale the number by "factor"
            // but only one decimal place is supported
            // for example, change 100.0 to 1000
            int index = point.indexOf(decSeparator);
            if (index != -1) {
                if (index == 0) {
                    int trailingData = point.substring(1).length();
                    // if the point is the first char, add a 0 for the integer part
                    point = "0".concat(point.substring(1));
                    // ensure that the point value has the correct # of decimals
                    // by padding with zeros
                    if (trailingData < dec) {
                        for (int i = trailingData; i < dec; i++) {
                            point = point + "0";
                        }
                    }
                } else if (index < point.length() - 1) {
                    // adjust the number of decimals, adding 0's to the end
                    int length = point.length() - index - 1;
                    for (int i = length; i < dec; i++) {
                        point = point + "0";
                    }

                    // use scale integer for gradePoint
                    point = point.substring(0, index) + point.substring(index + 1);
                } else {
                    // decimal point is the last char
                    point = point.substring(0, index);
                    for (int i = 0; i < dec; i++) {
                        point = point + "0";
                    }
                }
            } else {
                // if there is no decimal place, scale up the integer by "factor"
                for (int i = 0; i < dec; i++) {
                    point = point + "0";
                }
            }

            // filter out the "zero grade"
            if ("00".equals(point)) {
                point = "0";
            }
        }

        if (StringUtils.trimToNull(point) != null) {
            try {
                point = Integer.valueOf(point).toString();
            } catch (Exception e) {
                //log.warn(this + " scalePointGrade: cannot parse " + point + " into integer. " + e.getMessage());
            }
        }
        return point;

    } // scalePointGrade
}
