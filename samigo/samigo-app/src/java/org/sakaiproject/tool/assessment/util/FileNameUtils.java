/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2023 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.util;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * Utility class for generating standardized file names for assessment exports.
 */
public class FileNameUtils {

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd'T'HHmmss";
    private static final int MAX_FILENAME_LENGTH = 100;

    /**
     * Sanitizes text for use in file names.
     *
     * @param text The text to sanitize
     * @return Sanitized filename-safe text
     */
    public static String sanitizeFilename(String text) {
        if (text == null) return "Unknown";

        String safeText = StringEscapeUtils.unescapeHtml(text);
        safeText = Normalizer.normalize(safeText, Normalizer.Form.NFD);
        safeText = safeText.replaceAll("\\p{M}", "");
        safeText = safeText.replaceAll("[^a-zA-Z0-9]+", "-");
        safeText = safeText.replaceAll("^-+|-+$", "");

        if (safeText.length() > MAX_FILENAME_LENGTH) {
            safeText = safeText.substring(0, MAX_FILENAME_LENGTH);
        }

        return safeText;
    }

    /**
     * Generates a timestamp string.
     *
     * @return Formatted timestamp
     */
    public static String generateTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return dateFormat.format(new Date());
    }

    /**
     * Generates ZIP filename for bulk scores export.
     *
     * @return ZIP filename
     */
    public static String generateScoresZipFilename() {
        return "assessments-scores-" + generateTimestamp() + ".zip";
    }

    /**
     * Generates XLSX filename for scores export.
     *
     * @param assessmentName Assessment name
     * @return XLSX filename
     */
    public static String generateScoresExcelFilename(String assessmentName) {
        String sanitizedName = sanitizeFilename(assessmentName);
        if (sanitizedName.isEmpty()) sanitizedName = "assessment";
        return sanitizedName + "-scores-" + generateTimestamp() + ".xlsx";
    }

    /**
     * Generates XLSX filename for scores export without timestamp.
     *
     * @param assessmentName Assessment name
     * @return XLSX filename
     */
    public static String generateScoresExcelFilenameForZip(String assessmentName) {
        String sanitizedName = sanitizeFilename(assessmentName);
        if (sanitizedName.isEmpty()) sanitizedName = "assessment";
        return sanitizedName + "-scores.xlsx";
    }
}