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

import static org.sakaiproject.assignment.api.AssignmentConstants.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.SubmissionTransferBean;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.assignment.api.model.AssignmentSubmission;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.rubrics.api.RubricsConstants;
import org.sakaiproject.rubrics.api.RubricsService;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.time.api.TimeService;
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

    private AssignmentService assignmentService;
    private RubricsService rubricsService;
    private TimeService timeService;
    private LTIService ltiService;

    private static ResourceLoader rb = new ResourceLoader("assignment");

    /**
     * scale the point value by "factor" if there is a valid point grade
     */
    public String scalePointGrade(String point, int factor, List<String> alerts) {

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

    /**
     * Tests the format of the supplied grade and sets alert messages in the
     * state as required.
     */
    public List<String> validPointGrade(final String grade, int factor) {

        List<String> alerts = new ArrayList<>();

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

    public List<String> alertInvalidPoint(String grade, int factor) {

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
     * construct time object based on various state variables
     *
     * @param state
     * @param monthString
     * @param dayString
     * @param yearString
     * @param hourString
     * @param minString
     * @return
     */
    public Instant getTimeFromOptions(Map<String, Object> options, String monthString, String dayString, String yearString, String hourString, String minString) {

        if (options.get(monthString) != null ||
                options.get(dayString) != null ||
                options.get(yearString) != null ||
                options.get(hourString) != null ||
                options.get(minString) != null) {
            int month = (Integer) options.get(monthString);
            int day = (Integer) options.get(dayString);
            int year = (Integer) options.get(yearString);
            int hour = (Integer) options.get(hourString);
            int min = (Integer) options.get(minString);
            return LocalDateTime.of(year, month, day, hour, min, 0).atZone(timeService.getLocalTimeZone().toZoneId()).toInstant();
        } else {
            return null;
        }
    }

    /**
     * Contains logic to consistently output a String based version of a grade
     * Interprets the grade using the scale for display
     *
     * This should probably be moved to a static utility class - ern
     *
     * @param grade
     * @param typeOfGrade
     * @param scaleFactor
     * @return
     */
    public String getGradeDisplay(String grade, Assignment.GradeType typeOfGrade, Integer scaleFactor) {
        String returnGrade = StringUtils.trimToEmpty(grade);
        if (scaleFactor == null) scaleFactor = assignmentService.getScaleFactor();

        switch (typeOfGrade) {
            case SCORE_GRADE_TYPE:
                if (!returnGrade.isEmpty() && !"0".equals(returnGrade)) {
                    int dec = new Double(Math.log10(scaleFactor)).intValue();
                    String decSeparator = formattedText.getDecimalSeparator();
                    String decimalGradePoint = returnGrade;
                    try {
                        Integer.parseInt(returnGrade);
                        // if point grade, display the grade with factor decimal place
                        if (returnGrade.length() > dec) {
                            decimalGradePoint = returnGrade.substring(0, returnGrade.length() - dec) + decSeparator + returnGrade.substring(returnGrade.length() - dec);
                        } else {
                            String newGrade = "0".concat(decSeparator);
                            for (int i = returnGrade.length(); i < dec; i++) {
                                newGrade = newGrade.concat("0");
                            }
                            decimalGradePoint = newGrade.concat(returnGrade);
                        }
                    } catch (NumberFormatException nfe1) {
                        log.debug("Could not parse grade [{}] as an Integer trying as a Float, {}", returnGrade, nfe1.getMessage());
                        try {
                            Float.parseFloat(returnGrade);
                            decimalGradePoint = returnGrade;
                        } catch (NumberFormatException nfe2) {
                            log.debug("Could not parse grade [{}] as a Float, {}", returnGrade, nfe2.getMessage());
                        }
                    }
                    // get localized number format
                    NumberFormat nbFormat = formattedText.getNumberFormat(dec, dec, false);
                    DecimalFormat dcformat = (DecimalFormat) nbFormat;
                    // show grade in localized number format
                    try {
                        Double dblGrade = dcformat.parse(decimalGradePoint).doubleValue();
                        decimalGradePoint = nbFormat.format(dblGrade);
                        returnGrade = decimalGradePoint;
                    } catch (Exception e) {
                        log.warn("Could not parse grade [{}], {}", returnGrade, e.getMessage());
                    }
                }
                break;
            case UNGRADED_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("gen.nograd")) {
                    returnGrade = rb.getString("gen.nograd");
                }
                break;
            case PASS_FAIL_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("Pass")) {
                    returnGrade = rb.getString("pass");
                } else if (returnGrade.equalsIgnoreCase("Fail")) {
                    returnGrade = rb.getString("fail");
                } else {
                    returnGrade = rb.getString("ungra");
                }
                break;
            case CHECK_GRADE_TYPE:
                if (returnGrade.equalsIgnoreCase("Checked")) {
                    returnGrade = rb.getString("gen.checked");
                } else {
                    returnGrade = rb.getString("ungra");
                }
                break;
            default:
                if (returnGrade.isEmpty()) {
                    returnGrade = rb.getString("ungra");
                }
        }
        return returnGrade;
    }

    public boolean isDraftSubmission(SubmissionTransferBean s) {

        return (!s.getSubmitted()
            && ((s.getSubmittedText() != null && s.getSubmittedText().length() > 0)
            || (s.getAttachments() != null && s.getAttachments().size() > 0)));
    }

    private String displayGrade(String grade, Integer factor) {
        return assignmentService.getGradeDisplay(grade, Assignment.GradeType.SCORE_GRADE_TYPE, factor);
    }

    public boolean hasRubricSelfReview(String assignmentId) {
        try {
            Optional<ToolItemRubricAssociation> rubricAssociation = rubricsService.getRubricAssociation(RubricsConstants.RBCS_TOOL_ASSIGNMENT_GRADES, assignmentId);
            if (rubricAssociation.isPresent()) {
                return Integer.valueOf(1).equals(rubricAssociation.get().getParameters().get(RubricsConstants.RBCS_STUDENT_SELF_REPORT));
            }
        } catch (Exception e) {
            log.warn("Error trying to retrieve rubrics association for assignment : {}", e.getMessage());
        }
        return false;
    }

    public int getRubricSelfReviewMode(String assignmentId) {
        try {
            Optional<ToolItemRubricAssociation> rubricAssociation = rubricsService.getRubricAssociation(RubricsConstants.RBCS_TOOL_ASSIGNMENT_GRADES, assignmentId);
            if (rubricAssociation.isPresent()) {
                return rubricAssociation.get().getParameters().get(RubricsConstants.RBCS_STUDENT_SELF_REPORT_MODE);
            }
        } catch (Exception e) {
            log.warn("Error trying to retrieve rubrics association for assignment : {}", e.getMessage());
        }
        return -1;
    }

    public boolean hasRubricHiddenToStudent(String assignmentId) {
        try {
            Optional<ToolItemRubricAssociation> rubricAssociation = rubricsService.getRubricAssociation(RubricsConstants.RBCS_TOOL_ASSIGNMENT_GRADES, assignmentId);
            if (rubricAssociation.isPresent()) {
                return Integer.valueOf(1).equals(rubricAssociation.get().getParameters().get("hideStudentPreview"));
            }
        } catch (Exception e) {
            log.warn("Error trying to retrieve rubrics association for assignment : {}", e.getMessage());
        }
        return false;
    }

    public String displayGrade(SessionState state, String grade, Integer factor) {

        String currentStateMessage = (String) state.getAttribute(VelocityPortletPaneledAction.STATE_MESSAGE);
        if (currentStateMessage == null || currentStateMessage.startsWith(rb.getString("pleasee6"))) {
            if (StringUtils.isNotBlank(grade)) {
                grade = assignmentService.getGradeDisplay(grade, Assignment.GradeType.SCORE_GRADE_TYPE, factor);
            } else {
                grade = "";
            }
        }
        return grade;
    }
}
