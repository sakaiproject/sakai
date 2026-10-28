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

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/** Calculates a published maximum using supplied question hashes, without performing I/O. */
public final class PublishedAssessmentScore {
    private PublishedAssessmentScore() {
    }

    /** Returns null when the configured draw or the required content hashes are unavailable. */
    public static Double calculate(PublishedAssessmentIfc assessment, Function<ItemDataIfc, String> hashResolver) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object sectionObject : assessment.getSectionSet()) {
            SectionDataIfc section = (SectionDataIfc) sectionObject;
            List<ItemDataIfc> items = section.getItemArray();
            String authorType = section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
            if (SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.toString().equals(authorType)
                    || SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOLS.toString().equals(authorType)
                    || SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString().equals(authorType)) {
                QuestionPoolDraw draw = QuestionPoolDraw.fromSection(section, hashResolver);
                int requested;
                try {
                    requested = Integer.parseInt(section.getSectionMetaDataByLabel(SectionDataIfc.NUM_QUESTIONS_DRAWN));
                } catch (NumberFormatException e) {
                    return null;
                }
                if (!draw.isValid(requested)) {
                    return null;
                }
                items = draw.select(requested);
            }
            for (ItemDataIfc item : items) {
                if (!item.getIsExtraCredit()) {
                    total = total.add(BigDecimal.valueOf(item.getScore()));
                }
            }
        }
        return total.doubleValue();
    }
}
