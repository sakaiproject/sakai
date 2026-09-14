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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AssessmentPdfLatexParserTest {

    @Test
    public void parseLatexChunksReturnsPlainTextWhenMathDisabled() {
        assertEquals(1, AssessmentPdfLatexParser.parseLatexChunks("plain", false).size());
    }

    @Test
    public void parseLatexChunksSplitsDollarDelimitedExpression() {
        java.util.List<LatexChunk> chunks = AssessmentPdfLatexParser.parseLatexChunks("before $$x+1$$ after", true);
        assertEquals(3, chunks.size());
        assertEquals(LatexChunk.Type.TEXT, chunks.get(0).getType());
        assertEquals(LatexChunk.Type.LATEX, chunks.get(1).getType());
        assertEquals("x+1", chunks.get(1).getContent());
    }

    @Test
    public void parseLatexChunksHandlesUnclosedDelimiterAsText() {
        java.util.List<LatexChunk> chunks = AssessmentPdfLatexParser.parseLatexChunks("$$unclosed", true);
        assertEquals(1, chunks.size());
        assertEquals("$$unclosed", chunks.get(0).getContent());
    }

    @Test
    public void parseLatexChunksReturnsPlainTextWhenMathEnabledWithoutDelimiters() {
        java.util.List<LatexChunk> chunks = AssessmentPdfLatexParser.parseLatexChunks("plain text only", true);
        assertEquals(1, chunks.size());
        assertEquals(LatexChunk.Type.TEXT, chunks.get(0).getType());
        assertEquals("plain text only", chunks.get(0).getContent());
    }

    @Test
    public void parseLatexChunksSupportsParenAndBracketDelimiters() {
        java.util.List<LatexChunk> chunks = AssessmentPdfLatexParser.parseLatexChunks(
                "a \\(\\alpha\\) b \\[\\beta\\]", true);
        assertTrue(chunks.size() >= 3);
        boolean hasLatex = false;
        for (LatexChunk chunk : chunks) {
            if (chunk.getType() == LatexChunk.Type.LATEX) {
                hasLatex = true;
            }
        }
        assertTrue(hasLatex);
    }
}
