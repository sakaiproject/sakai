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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ImageMapCoordinatesTest {

    private static final List<String> REGIONS = List.of(
            "{\"x1\":10,\"y1\":10,\"x2\":20,\"y2\":20}",
            "{\"x1\":30,\"y1\":30,\"x2\":40,\"y2\":40}");

    @Test
    public void findRegionIndexReturnsTheAreaContainingTheClick() {
        assertEquals(Integer.valueOf(1), ImageMapCoordinates.findRegionIndex(
                "{\"click\":true,\"x\":15,\"y\":15}", REGIONS).orElse(null));
        assertEquals(Integer.valueOf(2), ImageMapCoordinates.findRegionIndex(
                "{\"click\":true,\"x\":35,\"y\":35}", REGIONS).orElse(null));
    }

    @Test
    public void findRegionIndexUsesLegacyClickCoordinates() {
        // Legacy responses stored the marker top-left; the click is offset by half a marker.
        assertEquals(Integer.valueOf(1), ImageMapCoordinates.findRegionIndex(
                "{\"x\":2,\"y\":2}", REGIONS).orElse(null));
    }

    @Test
    public void findRegionIndexReturnsEmptyWhenClickIsOutsideEveryArea() {
        assertTrue(ImageMapCoordinates.findRegionIndex(
                "{\"click\":true,\"x\":100,\"y\":100}", REGIONS).isEmpty());
        assertTrue(ImageMapCoordinates.findRegionIndex("undefined", REGIONS).isEmpty());
        assertTrue(ImageMapCoordinates.findRegionIndex(null, REGIONS).isEmpty());
    }
}
