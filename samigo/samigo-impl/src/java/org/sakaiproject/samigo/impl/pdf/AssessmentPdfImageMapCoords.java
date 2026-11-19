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
import java.util.Optional;
import java.util.OptionalDouble;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfItemGradingModel;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.ImageMapCircle;
import org.sakaiproject.tool.assessment.util.ImageMapCoordinates;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Rectangle;

import lombok.extern.slf4j.Slf4j;

/**
 * Parses authored image-map regions and student click markers from stored JSON.
 */
@Slf4j
final class AssessmentPdfImageMapCoords {

    private final ObjectMapper jsonMapper;

    AssessmentPdfImageMapCoords(ObjectMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    List<Rectangle> parseRectangles(List<String> regionJsons) {
        List<Rectangle> answerRectangles = new ArrayList<>();
        if (regionJsons == null) {
            return answerRectangles;
        }
        for (String regionJson : regionJsons) {
            String json = extractJsonObject(regionJson);
            if (json == null) {
                continue;
            }
            try {
                JsonNode jsonNode = jsonMapper.readTree(json);
                OptionalDouble x1 = finiteNumber(jsonNode, "x1");
                OptionalDouble y1 = finiteNumber(jsonNode, "y1");
                OptionalDouble x2 = finiteNumber(jsonNode, "x2");
                OptionalDouble y2 = finiteNumber(jsonNode, "y2");
                if (x1.isEmpty() || y1.isEmpty() || x2.isEmpty() || y2.isEmpty()) {
                    continue;
                }
                answerRectangles.add(new Rectangle((float) x1.getAsDouble(), (float) y1.getAsDouble(), (float) x2.getAsDouble(), (float) y2.getAsDouble()));
            } catch (JsonProcessingException e) {
                log.warn("Skipping unparseable image map answer rectangle [{}], {}", regionJson, e.toString());
            }
        }
        return answerRectangles;
    }

    List<ImageMapCircle> parseCircles(List<AssessmentPdfItemGradingModel> itemsGrading) {
        List<ImageMapCircle> answerCircles = new ArrayList<>();
        if (itemsGrading == null) {
            return answerCircles;
        }
        int fallbackSequence = 1;
        for (AssessmentPdfItemGradingModel itemGrading : itemsGrading) {
            Long publishedItemTextId = itemGrading.getPublishedItemTextId();
            if (publishedItemTextId == null) {
                continue;
            }
            Optional<ImageMapCoordinates.Point> point = ImageMapCoordinates.parseStudentPoint(itemGrading.getAnswerText());
            if (point.isEmpty()) {
                continue;
            }
            Integer sequence = itemGrading.getSequence();
            int markerSequence = sequence != null ? sequence.intValue() : fallbackSequence;
            answerCircles.add(new ImageMapCircle((float) point.get().getClickX(), (float) point.get().getClickY(), markerSequence));
            fallbackSequence++;
        }
        return answerCircles;
    }

    static String extractJsonObject(String raw) {
        if (StringUtils.isBlank(raw) || Strings.CI.equals(raw, "undefined")) {
            return null;
        }
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start < 0 || end <= start) {
            return null;
        }
        return raw.substring(start, end + 1);
    }

    static OptionalDouble finiteNumber(JsonNode parent, String field) {
        if (parent == null || StringUtils.isBlank(field)) {
            return OptionalDouble.empty();
        }
        JsonNode node = parent.get(field);
        if (node == null || node.isMissingNode() || node.isNull() || !node.isNumber()) {
            return OptionalDouble.empty();
        }
        double value = node.asDouble();
        if (!Double.isFinite(value) || value > Float.MAX_VALUE || value < -Float.MAX_VALUE) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value);
    }
}
