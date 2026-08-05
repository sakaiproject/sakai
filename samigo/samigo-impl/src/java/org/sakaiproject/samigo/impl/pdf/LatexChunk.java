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

import lombok.Getter;

/**
 * A segment of text produced by {@link AssessmentPdfLatexParser}.
 */
@Getter
public final class LatexChunk {

    public enum Type {
        TEXT,
        LATEX
    }

    private final Type type;
    private final String content;

    public LatexChunk(Type type, String content) {
        this.type = type;
        this.content = content;
    }

    public static LatexChunk text(String content) {
        return new LatexChunk(Type.TEXT, content);
    }

    public static LatexChunk latex(String content) {
        return new LatexChunk(Type.LATEX, content);
    }
}
