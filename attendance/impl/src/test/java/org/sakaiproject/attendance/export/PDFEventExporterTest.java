/*
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
package org.sakaiproject.attendance.export;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.test.AttendanceTestConfiguration;
import org.sakaiproject.user.api.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = AttendanceTestConfiguration.class)
public class PDFEventExporterTest {
    @Autowired private PDFEventExporter exporter;

    @Test
    public void concurrentExportsKeepEachEventsRosterAndTitleSeparate() throws Exception {
        CyclicBarrier bothExportsStarted = new CyclicBarrier(2);
        CompletableFuture<String> first = CompletableFuture.supplyAsync(() -> export("First", bothExportsStarted));
        CompletableFuture<String> second = CompletableFuture.supplyAsync(() -> export("Second", bothExportsStarted));
        String firstText = first.get(15, TimeUnit.SECONDS);
        String secondText = second.get(15, TimeUnit.SECONDS);
        assertTrue(firstText.contains("First meeting"));
        assertTrue(firstText.contains("First student"));
        assertFalse(firstText.contains("Second"));
        assertTrue(secondText.contains("Second meeting"));
        assertTrue(secondText.contains("Second student"));
        assertFalse(secondText.contains("First"));
    }

    private String export(String name, CyclicBarrier bothExportsStarted) {
        AttendanceEvent event = new AttendanceEvent();
        // Rendezvous inside the exporter after it receives each request's event.
        User student = mock(User.class);
        when(student.getSortName()).thenAnswer(invocation -> {
            bothExportsStarted.await(5, TimeUnit.SECONDS);
            return name + " student";
        });
        event.setName(name + " meeting");
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // An immutable roster also verifies that exporting does not sort the caller's list in place.
            exporter.createSignInPdf(event, output, List.of(student), name + " site");
            try (PdfReader reader = new PdfReader(output.toByteArray())) {
                return new PdfTextExtractor(reader).getTextFromPage(1);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
