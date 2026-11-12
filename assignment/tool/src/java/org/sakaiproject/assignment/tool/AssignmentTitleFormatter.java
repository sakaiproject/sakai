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
package org.sakaiproject.assignment.tool;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

/** Formats titles stored as sanitized HTML for plain-text displays and edit forms. */
public class AssignmentTitleFormatter {

    /** Decode only the storage encoding; callers must escape this value for its output context. */
    public String toPlainText(String storedTitle) {
        return StringEscapeUtils.unescapeHtml4(StringUtils.defaultString(storedTitle));
    }

    /** Escape the decoded title for HTML text or a double-quoted HTML attribute. */
    public String escapeHtml(String storedTitle) {
        return StringEscapeUtils.escapeHtml4(toPlainText(storedTitle));
    }
}
