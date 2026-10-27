/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.scorm.content.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import org.sakaiproject.content.api.ContentEntity;
import static org.junit.Assert.*;

public class ZipReaderTest {
    @Test
    public void bothReaderModesNormalizePathsBeforeCallbacks() throws Exception {
        for (boolean first : new boolean[] {false, true}) {
            List<String> seen = new ArrayList<>();
            ZipReader reader = reader("./folder\\file.txt", seen);
            if (first) reader.readFirst(); else reader.read();
            assertEquals(List.of("folder/file.txt"), seen);
        }
    }

    @Test
    public void bothReaderModesRejectInvalidPathsBeforeCallbacks() throws Exception {
        for (String name : new String[] {"../file.txt", "..\\file.txt", "/file.txt", "C:/file.txt", "file:stream"}) {
            for (boolean first : new boolean[] {false, true}) {
                List<String> seen = new ArrayList<>();
                ZipReader reader = reader(name, seen);
                if (first) reader.readFirst(); else reader.read();
                assertTrue(seen.isEmpty());
            }
        }
    }

    private ZipReader reader(String name, List<String> seen) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry(name));
            zip.write(new byte[] {1});
            zip.closeEntry();
        }
        return new ZipReader(new ByteArrayInputStream(bytes.toByteArray())) {
            @Override protected boolean includeContent(boolean directory) { return true; }
            @Override protected boolean isValid(String path) { return true; }
            @Override protected ContentEntity processEntry(String path, ByteArrayOutputStream content, boolean directory) {
                seen.add(path);
                assertArrayEquals(new byte[] {1}, content.toByteArray());
                return null;
            }
        };
    }
}
