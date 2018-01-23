/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business.importExport;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.Getter;

/**
 * Contains the data relevant to grade validation: invalid number and letter grades.
 * TODO: implement letter grade validation once letter grades are supported in GBNG.
 *
 * @author plukasew, bjones86
 */
public class GradeValidationReport
{
    //@Getter
    //private final SortedMap<String, SortedMap<String, String>> invalidLetterGrades;

    /**
     * maps columnTitle -> (userEid -> grade)
     */
    @Getter
    private final SortedMap<String, SortedMap<String, String>> invalidNumericGrades;

    public GradeValidationReport()
    {
        //invalidLetterGrades = new ConcurrentSkipListMap<>();
        invalidNumericGrades = new ConcurrentSkipListMap<>();
    }

    //public void addInvalidLetterGrade(String columnTitle, String userEID, String grade)
    //{
    //}

    public void addInvalidNumericGrade(String columnTitle, String studentIdentifier, String grade)
    {
        SortedMap<String, String> columnInvalidGradesMap = invalidNumericGrades.get(columnTitle);
        if (columnInvalidGradesMap == null)
        {
            columnInvalidGradesMap = new ConcurrentSkipListMap<>();
            columnInvalidGradesMap.put(studentIdentifier, grade);
            invalidNumericGrades.put(columnTitle, columnInvalidGradesMap);
        }
        else
        {
            columnInvalidGradesMap.put(studentIdentifier, grade);
        }
    }
}
