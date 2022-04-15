/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.grading.api;

import java.math.MathContext;
import java.math.RoundingMode;

public interface GradingConstants {

    // These have been deprecated in favour of the {@link GradingType} enum
    @Deprecated
    public static final int GRADE_TYPE_POINTS = 1;
    @Deprecated
    public static final int GRADE_TYPE_PERCENTAGE = 2;
    @Deprecated
    public static final int GRADE_TYPE_LETTER = 3;

    public static final int CATEGORY_TYPE_NO_CATEGORY = 1;
    public static final int CATEGORY_TYPE_ONLY_CATEGORY = 2;
    public static final int CATEGORY_TYPE_WEIGHTED_CATEGORY = 3;

    public static final String REFERENCE_ROOT = "/gbassignment";

    public static final String[] validLetterGrade = { "a+", "a", "a-", "b+", "b", "b-",
            "c+", "c", "c-", "d+", "d", "d-", "f" };

    // These Strings have been kept for backwards compatibility as they are used everywhere,
    // however the {@link GraderPermission} enum should be used going forward.
    @Deprecated
    public static final String gradePermission = GradingPermission.GRADE.toString();
    @Deprecated
    public static final String viewPermission = GradingPermission.VIEW.toString();
    @Deprecated
    public static final String noPermission = GradingPermission.NONE.toString();

    public static final String enableLetterGradeString = "gradebook_enable_letter_grade";

    public static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_DOWN);

    /**
     * Array of chars that are not allowed at the beginning of a gb item title
     */
    public static final String[] INVALID_CHARS_AT_START_OF_GB_ITEM_NAME = { "#", "*", "[" };
}
