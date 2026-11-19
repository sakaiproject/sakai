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

import com.lowagie.text.Rectangle;

/**
 * Places numbered labels around image-map anchors without overlapping each other or leaving the
 * image. Labels are tried at fixed offsets around their anchor and moved outwards until a free
 * spot is found, so small or nearby Hot Spot areas keep every number readable.
 */
final class ImageMapLabelLayout {

    static final float GAP = 1.5f;

    private static final int MAX_RINGS = 8;

    // Above, right, below, left, then the diagonals. Every direction is retried one ring further
    // out when the previous candidates are occupied.
    private static final float[][] DIRECTIONS = {
            {0f, 1f}, {1f, 0f}, {0f, -1f}, {-1f, 0f},
            {1f, 1f}, {-1f, 1f}, {1f, -1f}, {-1f, -1f}
    };

    private ImageMapLabelLayout() {
    }

    /**
     * A label to place, measured in PDF points.
     */
    static final class Label {
        private final String text;
        private final float anchorX;
        private final float anchorY;
        private final float width;
        private final float height;
        private final boolean areaLabel;
        private final float clearance;

        Label(String text, float anchorX, float anchorY, float width, float height, boolean areaLabel, float clearance) {
            this.text = text;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.width = width;
            this.height = height;
            this.areaLabel = areaLabel;
            this.clearance = clearance;
        }

        String getText() {
            return text;
        }

        float getAnchorX() {
            return anchorX;
        }

        float getAnchorY() {
            return anchorY;
        }

        float getWidth() {
            return width;
        }

        float getHeight() {
            return height;
        }

        boolean isAreaLabel() {
            return areaLabel;
        }

        float getClearance() {
            return clearance;
        }
    }

    /**
     * A placed label box. {@link #isLeaderLineVisible()} tells whether the box had to move far
     * enough from its anchor that a leader line is needed to keep the association clear.
     */
    static final class Placement {
        private final Label label;
        private final Rectangle box;

        Placement(Label label, Rectangle box) {
            this.label = label;
            this.box = box;
        }

        Label getLabel() {
            return label;
        }

        Rectangle getBox() {
            return box;
        }

        boolean isLeaderLineVisible() {
            float centerX = (box.getLeft() + box.getRight()) / 2f;
            float centerY = (box.getBottom() + box.getTop()) / 2f;
            float dx = centerX - label.getAnchorX();
            float dy = centerY - label.getAnchorY();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            float threshold = Math.max(label.getWidth(), label.getHeight()) / 2f + GAP + 2f;
            return distance > threshold;
        }
    }

    /**
     * Places every label in order. Previously placed labels and the supplied obstacles are treated
     * as occupied space, so labels never cover each other or an existing overlay marker.
     */
    static List<Placement> place(List<Label> labels, List<Rectangle> obstacles, Rectangle bounds) {
        List<Placement> placements = new ArrayList<>();
        List<Rectangle> occupied = new ArrayList<>();
        if (obstacles != null) {
            occupied.addAll(obstacles);
        }
        if (labels == null) {
            return placements;
        }
        for (Label label : labels) {
            Rectangle box = findBox(label, occupied, bounds);
            placements.add(new Placement(label, box));
            occupied.add(box);
        }
        return placements;
    }

    private static Rectangle findBox(Label label, List<Rectangle> occupied, Rectangle bounds) {
        float halfWidth = label.getWidth() / 2f;
        float halfHeight = label.getHeight() / 2f;
        float stepX = label.getWidth() + GAP;
        float stepY = label.getHeight() + GAP;
        float startX = halfWidth + GAP + label.getClearance();
        float startY = halfHeight + GAP + label.getClearance();
        Rectangle leastOverlapping = null;
        float leastOverlap = Float.MAX_VALUE;

        for (int ring = 0; ring < MAX_RINGS; ring++) {
            for (float[] direction : DIRECTIONS) {
                float centerX = label.getAnchorX() + direction[0] * (startX + ring * stepX);
                float centerY = label.getAnchorY() + direction[1] * (startY + ring * stepY);
                Rectangle candidate = clampToBounds(centerX, centerY, halfWidth, halfHeight, bounds);
                float overlap = totalOverlap(candidate, occupied);
                if (overlap <= 0f) {
                    return candidate;
                }
                if (overlap < leastOverlap) {
                    leastOverlap = overlap;
                    leastOverlapping = candidate;
                }
            }
        }

        // Pathological case: the image is too small or too crowded for every label. Keep the label
        // inside the image and let the opaque badge stay readable even if it overlaps another one.
        if (leastOverlapping != null) {
            return leastOverlapping;
        }
        return clampToBounds(label.getAnchorX(), label.getAnchorY(), halfWidth, halfHeight, bounds);
    }

    private static Rectangle clampToBounds(float centerX, float centerY, float halfWidth, float halfHeight, Rectangle bounds) {
        float width = Math.min(halfWidth * 2f, bounds.getWidth());
        float height = Math.min(halfHeight * 2f, bounds.getHeight());
        float left = Math.min(Math.max(centerX - width / 2f, bounds.getLeft()), bounds.getRight() - width);
        float bottom = Math.min(Math.max(centerY - height / 2f, bounds.getBottom()), bounds.getTop() - height);
        return new Rectangle(left, bottom, left + width, bottom + height);
    }

    private static float totalOverlap(Rectangle box, List<Rectangle> occupied) {
        float total = 0f;
        for (Rectangle other : occupied) {
            float width = Math.min(box.getRight(), other.getRight()) - Math.max(box.getLeft(), other.getLeft());
            float height = Math.min(box.getTop(), other.getTop()) - Math.max(box.getBottom(), other.getBottom());
            if (width > 0f && height > 0f) {
                total += width * height;
            }
        }
        return total;
    }
}
