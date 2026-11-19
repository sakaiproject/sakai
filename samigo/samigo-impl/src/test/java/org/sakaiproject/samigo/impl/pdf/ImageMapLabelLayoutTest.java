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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.lowagie.text.Rectangle;

public class ImageMapLabelLayoutTest {

    private static final Rectangle BOUNDS = new Rectangle(0f, 0f, 400f, 300f);

    @Test
    public void labelsWithTheSameAnchorDoNotOverlap() {
        List<ImageMapLabelLayout.Label> labels = List.of(
                new ImageMapLabelLayout.Label("1", 200f, 150f, 16f, 12f, true, 0f),
                new ImageMapLabelLayout.Label("2", 200f, 150f, 16f, 12f, false, 0f));

        List<ImageMapLabelLayout.Placement> placements = ImageMapLabelLayout.place(labels, List.of(), BOUNDS);

        assertEquals(2, placements.size());
        assertFalse(overlap(placements.get(0).getBox(), placements.get(1).getBox()));
        assertInsideBounds(placements);
    }

    @Test
    public void labelsStayInsideTheImageNearTheEdges() {
        List<ImageMapLabelLayout.Label> labels = List.of(
                new ImageMapLabelLayout.Label("1", 3f, 3f, 18f, 12f, true, 0f),
                new ImageMapLabelLayout.Label("2", 397f, 297f, 18f, 12f, false, 0f),
                new ImageMapLabelLayout.Label("3", 200f, 2f, 18f, 12f, true, 0f));

        List<ImageMapLabelLayout.Placement> placements = ImageMapLabelLayout.place(labels, List.of(), BOUNDS);

        assertEquals(3, placements.size());
        assertInsideBounds(placements);
    }

    @Test
    public void labelsAvoidMarkersAndPreviouslyPlacedLabels() {
        Rectangle marker = new Rectangle(196f, 146f, 204f, 154f);
        List<ImageMapLabelLayout.Label> labels = List.of(
                new ImageMapLabelLayout.Label("1", 200f, 150f, 20f, 12f, false, 0f),
                new ImageMapLabelLayout.Label("2", 200f, 150f, 20f, 12f, false, 0f));

        List<ImageMapLabelLayout.Placement> placements = ImageMapLabelLayout.place(labels, List.of(marker), BOUNDS);

        for (ImageMapLabelLayout.Placement placement : placements) {
            assertFalse(overlap(placement.getBox(), marker));
        }
        assertFalse(overlap(placements.get(0).getBox(), placements.get(1).getBox()));
    }

    @Test
    public void clearanceKeepsTheLabelAwayFromItsAnchor() {
        List<ImageMapLabelLayout.Label> labels = List.of(
                new ImageMapLabelLayout.Label("1", 200f, 150f, 16f, 12f, false, 5f));

        Rectangle box = ImageMapLabelLayout.place(labels, List.of(), BOUNDS).get(0).getBox();

        boolean containsAnchor = box.getLeft() <= 200f && 200f <= box.getRight()
                && box.getBottom() <= 150f && 150f <= box.getTop();
        assertFalse(containsAnchor);
        assertTrue(box.getBottom() >= 150f + 5f || box.getLeft() >= 200f + 5f
                || box.getRight() <= 200f - 5f || box.getTop() <= 150f - 5f);
    }

    @Test
    public void adjacentPlacementDoesNotNeedALeaderLine() {
        List<ImageMapLabelLayout.Label> labels = List.of(
                new ImageMapLabelLayout.Label("1", 200f, 150f, 16f, 12f, true, 0f));

        ImageMapLabelLayout.Placement placement = ImageMapLabelLayout.place(labels, List.of(), BOUNDS).get(0);

        assertFalse(placement.isLeaderLineVisible());
    }

    @Test
    public void crowdedImageStillReturnsEveryLabelInsideBounds() {
        Rectangle smallBounds = new Rectangle(0f, 0f, 40f, 30f);
        List<ImageMapLabelLayout.Label> labels = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            labels.add(new ImageMapLabelLayout.Label(String.valueOf(i), 20f, 15f, 16f, 12f, i % 2 == 0, 0f));
        }

        List<ImageMapLabelLayout.Placement> placements = ImageMapLabelLayout.place(labels, List.of(), smallBounds);

        assertEquals(6, placements.size());
        for (ImageMapLabelLayout.Placement placement : placements) {
            assertTrue(placement.getBox().getLeft() >= smallBounds.getLeft());
            assertTrue(placement.getBox().getRight() <= smallBounds.getRight());
            assertTrue(placement.getBox().getBottom() >= smallBounds.getBottom());
            assertTrue(placement.getBox().getTop() <= smallBounds.getTop());
        }
    }

    private static void assertInsideBounds(List<ImageMapLabelLayout.Placement> placements) {
        for (ImageMapLabelLayout.Placement placement : placements) {
            Rectangle box = placement.getBox();
            assertTrue(box.getLeft() >= BOUNDS.getLeft());
            assertTrue(box.getRight() <= BOUNDS.getRight());
            assertTrue(box.getBottom() >= BOUNDS.getBottom());
            assertTrue(box.getTop() <= BOUNDS.getTop());
        }
    }

    private static boolean overlap(Rectangle first, Rectangle second) {
        float width = Math.min(first.getRight(), second.getRight()) - Math.max(first.getLeft(), second.getLeft());
        float height = Math.min(first.getTop(), second.getTop()) - Math.max(first.getBottom(), second.getBottom());
        return width > 0f && height > 0f;
    }
}
