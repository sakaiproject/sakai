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
package org.sakaiproject.samigo.impl.pdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Cursor-based LaTeX delimiter parser for PDF text rendering.
 */
public final class AssessmentPdfLatexParser {

    private static final String DOLLAR = "$$";
    private static final String PAREN_OPEN = "\\(";
    private static final String PAREN_CLOSE = "\\)";
    private static final String BRACKET_OPEN = "\\[";
    private static final String BRACKET_CLOSE = "\\]";

    private AssessmentPdfLatexParser() {
    }

    public static List<LatexChunk> parseLatexChunks(String text, boolean mathJaxEnabled) {
        List<LatexChunk> chunks = new ArrayList<>();
        if (StringUtils.isBlank(text) || !mathJaxEnabled) {
            chunks.add(LatexChunk.text(text == null ? "" : text));
            return chunks;
        }

        int cursor = 0;
        while (cursor < text.length()) {
            DelimiterMatch match = findEarliestDelimiter(text, cursor);
            if (match == null) {
                appendText(chunks, text.substring(cursor));
                break;
            }
            if (match.openIndex > cursor) {
                appendText(chunks, text.substring(cursor, match.openIndex));
            }
            int contentStart = match.openIndex + match.openToken.length();
            int closeIndex = text.indexOf(match.closeToken, contentStart);
            if (closeIndex < 0) {
                appendText(chunks, text.substring(match.openIndex));
                break;
            }
            String latex = text.substring(contentStart, closeIndex);
            chunks.add(LatexChunk.latex(latex));
            cursor = closeIndex + match.closeToken.length();
        }
        return chunks;
    }

    private static void appendText(List<LatexChunk> chunks, String text) {
        if (!text.isEmpty()) {
            chunks.add(LatexChunk.text(text));
        }
    }

    private static DelimiterMatch findEarliestDelimiter(String text, int fromIndex) {
        DelimiterMatch best = null;
        best = consider(text, fromIndex, DOLLAR, DOLLAR, best);
        best = consider(text, fromIndex, PAREN_OPEN, PAREN_CLOSE, best);
        best = consider(text, fromIndex, BRACKET_OPEN, BRACKET_CLOSE, best);
        return best;
    }

    private static DelimiterMatch consider(String text, int fromIndex, String open, String close, DelimiterMatch current) {
        int openIndex = text.indexOf(open, fromIndex);
        if (openIndex < 0) {
            return current;
        }
        if (current == null || openIndex < current.openIndex) {
            return new DelimiterMatch(openIndex, open, close);
        }
        return current;
    }

    private static final class DelimiterMatch {
        private final int openIndex;
        private final String openToken;
        private final String closeToken;

        private DelimiterMatch(int openIndex, String openToken, String closeToken) {
            this.openIndex = openIndex;
            this.openToken = openToken;
            this.closeToken = closeToken;
        }
    }
}
