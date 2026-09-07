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

import java.util.Optional;
import java.util.OptionalDouble;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * Parses authored Hot Spot regions and student markers from stored JSON.
 *
 * New student responses include {@code "click":true} and store the image pixel that was clicked.
 * Older responses omit that flag and store the top-left of the on-screen marker; those are shifted
 * to an approximate click before scoring or drawing.
 */
@Slf4j
public final class ImageMapCoordinates {

    /**
     * JSON field set by delivery when {@code x}/{@code y} are the click, not the marker corner.
     */
    public static final String CLICK_FIELD = "click";

    /**
     * Half of the 18×16 crosshair box in {@code imageQuestion.student.css}. Used only for
     * responses that stored the marker top-left. New responses do not use this.
     */
    public static final double LEGACY_OFFSET_X = 9d;

    public static final double LEGACY_OFFSET_Y = 8d;

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private ImageMapCoordinates() {
    }

    public static Optional<Point> parseStudentPoint(String answerText) {
        String json = extractJsonObject(answerText);
        if (json == null) {
            return Optional.empty();
        }
        try {
            JsonNode node = JSON_MAPPER.readTree(json);
            OptionalDouble x = finiteNumber(node, "x");
            OptionalDouble y = finiteNumber(node, "y");
            if (x.isEmpty() || y.isEmpty()) {
                return Optional.empty();
            }
            JsonNode clickNode = node.get(CLICK_FIELD);
            boolean clickStored = clickNode != null && clickNode.isBoolean() && clickNode.booleanValue();
            return Optional.of(new Point(x.getAsDouble(), y.getAsDouble(), clickStored));
        } catch (JsonProcessingException e) {
            log.warn("Skipping unparseable image map student point [{}], {}", answerText, e.toString());
            return Optional.empty();
        }
    }

    public static Optional<Region> parseRegion(String answerText) {
        String json = extractJsonObject(answerText);
        if (json == null) {
            return Optional.empty();
        }
        try {
            JsonNode node = JSON_MAPPER.readTree(json);
            OptionalDouble x1 = finiteNumber(node, "x1");
            OptionalDouble y1 = finiteNumber(node, "y1");
            OptionalDouble x2 = finiteNumber(node, "x2");
            OptionalDouble y2 = finiteNumber(node, "y2");
            if (x1.isEmpty() || y1.isEmpty() || x2.isEmpty() || y2.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new Region(x1.getAsDouble(), y1.getAsDouble(), x2.getAsDouble(), y2.getAsDouble()));
        } catch (JsonProcessingException e) {
            log.warn("Skipping unparseable image map region [{}], {}", answerText, e.toString());
            return Optional.empty();
        }
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
        if (!Double.isFinite(value)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(value);
    }

    /**
     * A student marker in image pixels. {@link #getClickX()} / {@link #getClickY()} are the
     * coordinates to score and draw.
     */
    public static final class Point {
        private final double storedX;
        private final double storedY;
        private final boolean clickStored;

        Point(double storedX, double storedY, boolean clickStored) {
            this.storedX = storedX;
            this.storedY = storedY;
            this.clickStored = clickStored;
        }

        public boolean isClickStored() {
            return clickStored;
        }

        public double getClickX() {
            return clickStored ? storedX : storedX + LEGACY_OFFSET_X;
        }

        public double getClickY() {
            return clickStored ? storedY : storedY + LEGACY_OFFSET_Y;
        }
    }

    /**
     * An authored rectangular Hot Spot region in image pixels.
     */
    public static final class Region {
        private final double x1;
        private final double y1;
        private final double x2;
        private final double y2;

        Region(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public boolean contains(double x, double y) {
            return x >= x1 && x <= x2 && y >= y1 && y <= y2;
        }
    }
}
