/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

/**
 * Shared naming rules for files Samigo offers for download.
 * <p>
 * Assessment titles and user names are free text, so every export builds its filename from
 * something an author typed. Centralising the rules keeps a spreadsheet export, a printable
 * assessment and a student report from each inventing their own convention.
 * </p>
 */
public final class FilenameUtil {

    /**
     * Characters removed from generated filenames: reserved on Windows, awkward in a shell, or
     * liable to be re-encoded somewhere between the header and the file system.
     */
    private static final char[] ILLEGAL_FILENAME_CHARS = new char[] {
            '#', '%', '&', '{', '}', '\\', '<', '>', '*', '?', '/', '$', '!', '\"', '\'', ':', '@', '+', '`', '|', '=' };

    /**
     * ISO 8601 basic form, e.g. {@code 20260806T010138}. The extended form's colons are reserved
     * on Windows and would be stripped by {@link #cleanFilename(String)}, so the basic form is the
     * standard representation that survives a file system intact.
     */
    private static final DateTimeFormatter FILENAME_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private FilenameUtil() {
    }

    /**
     * Current local time as an ISO 8601 basic timestamp suitable for a filename.
     */
    public static String timestamp() {
        return LocalDateTime.now().format(FILENAME_TIMESTAMP);
    }

    /**
     * Builds a normalised filename with a timestamp appended, e.g.
     * {@code Report_Ada-Lovelace_Unit-3_20260806T010138.pdf}. The timestamp is separated by an
     * underscore so it stays legible against the hyphenated name.
     *
     * @param base      descriptive part of the name, free text; normalised by {@link #cleanFilename}
     * @param extension file extension including the leading dot, e.g. {@code .pdf}
     */
    public static String timestampedFilename(String base, String extension) {
        return cleanFilename(base) + "_" + timestamp() + StringUtils.defaultString(extension);
    }

    /**
     * Normalises a filename: spaces become hyphens and reserved characters are dropped.
     * <p>
     * This is not an encoding step. Callers still hand the result to
     * {@code ContentDisposition.attachment().filename(name, UTF_8)}, which applies the RFC 5987
     * encoding; escaping here as well would be encoded a second time.
     * </p>
     *
     * @param dirtyFilename raw filename, may be null
     * @return the normalised filename, or an empty string when given null
     */
    public static String cleanFilename(String dirtyFilename) {
        String fileName = StringUtils.defaultString(dirtyFilename);
        fileName = StringUtils.replace(fileName, " ", "-");

        for (char illegalChar : ILLEGAL_FILENAME_CHARS) {
            fileName = StringUtils.remove(fileName, illegalChar);
        }

        return fileName;
    }
}
