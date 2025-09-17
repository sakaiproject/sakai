/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;

/**
 * <p>Copyright: Copyright (c) 2003-5</p>
 * <p>Organization: Sakai Project</p>
 * @author jlannan
 * @author Ed Smiley esmiley@stanford.edu
 * @version $Id$
 */
@Slf4j
public class TextFormat {

    public static String convertPlaintextToFormattedTextNoHighUnicode(String value) {
        if (value == null) return "";
        // Escape HTML characters and replace newlines with <br />
        String escaped = StringEscapeUtils.escapeHtml4(value);
        return escaped.replace("\n", "<br />\n");
    }

    // Reverse method
    public static String convertFormattedTextToPlaintext(String value) {
        if (value == null) return "";
        // Step 1: Replace <br> variants with newlines
        String withNewlines = value
                .replaceAll("(?i)<br\\s*/?>", "\n")  // Handles <br>, <br />, <BR>, etc.
                .replaceAll("(?i)<br\\s+[^>]*>", "\n");  // Handles <br attributes>
        // Step 2: Unescape HTML entities
        return StringEscapeUtils.unescapeHtml4(withNewlines);
    }

}
