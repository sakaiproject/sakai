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

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfPartModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfQuestionModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfImageMapRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfItemGradingModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfPrintSettingsModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPrintPdfModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentStudentReportPdfModel;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.ImageMapCircle;
import org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.ImageMapQuestionCellEvent;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.util.ImageMapCoordinates;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPRow;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class AssessmentPdfQuestionRendererTest {

    private static final byte[] TEST_PNG = createPng();
    private static final String MAP_RESOURCE = "/group/site/map.png";
    private static final String STUDENT_RESOURCE = "/group/site/student.png";
    private static final String VALID_REGION = "{\"x1\":17,\"y1\":19,\"x2\":181,\"y2\":283}";
    private static final String STUDENT_CLICK = "{\"click\":true,\"x\":555,\"y\":666}";
    private static final String LEGACY_CLICK = "{\"x\":555,\"y\":666}";

    @Test
    public void extractJsonObjectStripsPrefixAndUndefined() {
        assertEquals("{\"x1\":1,\"y1\":2,\"x2\":3,\"y2\":4}",
                AssessmentPdfImageMapCoords.extractJsonObject("prefix {\"x1\":1,\"y1\":2,\"x2\":3,\"y2\":4}"));
        assertNull(AssessmentPdfImageMapCoords.extractJsonObject("undefined"));
        assertNull(AssessmentPdfImageMapCoords.extractJsonObject("no-json"));
        assertNull(AssessmentPdfImageMapCoords.extractJsonObject(null));
    }

    @Test
    public void finiteNumberRejectsNullTextAndNonFiniteCoordinates() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue(AssessmentPdfImageMapCoords.finiteNumber(mapper.readTree("{\"x1\":17}"), "x1").isPresent());
        assertTrue(AssessmentPdfImageMapCoords.finiteNumber(mapper.readTree("{\"x1\":null}"), "x1").isEmpty());
        assertTrue(AssessmentPdfImageMapCoords.finiteNumber(mapper.readTree("{\"x1\":\"17\"}"), "x1").isEmpty());
        assertTrue(AssessmentPdfImageMapCoords.finiteNumber(mapper.readTree("{}"), "x1").isEmpty());
        JsonNode overflow = mapper.readTree("{\"x1\":1e309}");
        assertTrue(AssessmentPdfImageMapCoords.finiteNumber(overflow, "x1").isEmpty());
    }

    @Test
    public void parseRectanglesAndCirclesSkipInvalidCoordinates() throws Exception {
        AssessmentPdfImageMapCoords coords = new AssessmentPdfImageMapCoords(new ObjectMapper());
        List<Rectangle> rectangles = coords.parseRectangles(List.of(
                VALID_REGION,
                "{\"x1\":null,\"y1\":19,\"x2\":181,\"y2\":283}",
                "{\"x1\":\"17\",\"y1\":19,\"x2\":181,\"y2\":283}",
                "{\"x1\":1e309,\"y1\":19,\"x2\":181,\"y2\":283}",
                "{\"y1\":19,\"x2\":181,\"y2\":283}"));
        assertEquals(1, rectangles.size());
        assertEquals(17f, rectangles.get(0).getLeft(), 0.01f);
        assertEquals(283f, rectangles.get(0).getTop(), 0.01f);

        List<ImageMapCircle> circles = coords.parseCircles(List.of(
                new AssessmentPdfItemGradingModel(STUDENT_CLICK, 1L),
                new AssessmentPdfItemGradingModel("{\"x\":null,\"y\":666}", 2L),
                new AssessmentPdfItemGradingModel("{\"x\":\"555\",\"y\":666}", 3L),
                new AssessmentPdfItemGradingModel("{\"x\":1e309,\"y\":666}", 4L)));
        assertEquals(1, circles.size());
        assertEquals(555f, circles.get(0).getX(), 0.01f);
        assertEquals(666f, circles.get(0).getY(), 0.01f);
        assertEquals(1, circles.get(0).getSequence());
    }

    @Test
    public void parseCirclesUsesStoredClickWhenFlagPresentAndOffsetsLegacyTopLeft() {
        AssessmentPdfImageMapCoords coords = new AssessmentPdfImageMapCoords(new ObjectMapper());
        List<ImageMapCircle> clickCircles = coords.parseCircles(List.of(
                new AssessmentPdfItemGradingModel(STUDENT_CLICK, 1L)));
        assertEquals(1, clickCircles.size());
        assertEquals(555f, clickCircles.get(0).getX(), 0.01f);
        assertEquals(666f, clickCircles.get(0).getY(), 0.01f);

        List<ImageMapCircle> legacyCircles = coords.parseCircles(List.of(
                new AssessmentPdfItemGradingModel(LEGACY_CLICK, 1L)));
        assertEquals(1, legacyCircles.size());
        assertEquals(555f + (float) ImageMapCoordinates.LEGACY_OFFSET_X, legacyCircles.get(0).getX(), 0.01f);
        assertEquals(666f + (float) ImageMapCoordinates.LEGACY_OFFSET_Y, legacyCircles.get(0).getY(), 0.01f);
    }

    @Test
    public void parseCirclesUsesItemTextSequenceNotPublishedId() {
        AssessmentPdfImageMapCoords coords = new AssessmentPdfImageMapCoords(new ObjectMapper());
        List<ImageMapCircle> circles = coords.parseCircles(List.of(
                new AssessmentPdfItemGradingModel("{\"x\":30,\"y\":40}", 9002L, 2),
                new AssessmentPdfItemGradingModel("{\"x\":10,\"y\":20}", 9001L, 1)));
        assertEquals(2, circles.size());
        assertEquals(2, circles.get(0).getSequence());
        assertEquals(1, circles.get(1).getSequence());
    }

    @Test
    public void printableRenderUsesImageMapSrcAndPaintsRegionsWhenKeysAreShown() throws Exception {
        ContentHostingService contentHostingService = contentHosting(MAP_RESOURCE);
        AssessmentPdfQuestionModel question = imageMapQuestion(null, MAP_RESOURCE, List.of(VALID_REGION),
                Collections.emptyList());

        OverlayCapturingDocument withKeys = renderPrint(question, printSettings(true), contentHostingService);
        assertEquals(1, withKeys.overlays.size());
        assertEquals(1, withKeys.overlays.get(0).getAnswerRectangles().size());
        assertEquals(17f, withKeys.overlays.get(0).getAnswerRectangles().get(0).getLeft(), 0.01f);
        assertTrue(withKeys.overlays.get(0).getAnswerCircles().isEmpty());
        verify(contentHostingService).getResource(MAP_RESOURCE);

        OverlayCapturingDocument withoutKeys = renderPrint(question, printSettings(false), contentHosting(MAP_RESOURCE));
        assertTrue(withoutKeys.overlays.isEmpty());
    }

    @Test
    public void printableRenderSkipsOverlayForNullTextAndNonFiniteRegions() throws Exception {
        ContentHostingService contentHostingService = contentHosting(MAP_RESOURCE);
        AssessmentPdfQuestionModel question = imageMapQuestion(null, MAP_RESOURCE, List.of(
                "{\"x1\":null,\"y1\":19,\"x2\":181,\"y2\":283}",
                "{\"x1\":\"17\",\"y1\":19,\"x2\":181,\"y2\":283}",
                "{\"x1\":1e309,\"y1\":19,\"x2\":181,\"y2\":283}"), Collections.emptyList());

        OverlayCapturingDocument document = renderPrint(question, printSettings(true), contentHostingService);
        assertTrue(document.overlays.isEmpty());
        verify(contentHostingService).getResource(MAP_RESOURCE);
    }

    @Test
    public void studentReportRenderPrefersImageSrcAndOverlaysStudentClick() throws Exception {
        ContentHostingService contentHostingService = contentHosting(STUDENT_RESOURCE);
        AssessmentPdfQuestionModel question = imageMapQuestion(STUDENT_RESOURCE, MAP_RESOURCE, List.of(VALID_REGION),
                List.of(new AssessmentPdfItemGradingModel(STUDENT_CLICK, 9001L, 1)));

        OverlayCapturingDocument document = renderReport(question, contentHostingService);
        assertEquals(1, document.overlays.size());
        ImageMapQuestionCellEvent overlay = document.overlays.get(0);
        assertEquals(1, overlay.getAnswerRectangles().size());
        assertEquals(1, overlay.getAnswerCircles().size());
        ImageMapCircle click = overlay.getAnswerCircles().get(0);
        assertEquals(555f, click.getX(), 0.01f);
        assertEquals(666f, click.getY(), 0.01f);
        assertEquals(1, click.getSequence());
        verify(contentHostingService).getResource(STUDENT_RESOURCE);
        verify(contentHostingService, never()).getResource(MAP_RESOURCE);
    }

    private static AssessmentPdfQuestionModel imageMapQuestion(String imageSrc, String imageMapSrc, List<String> regions,
            List<AssessmentPdfItemGradingModel> clicks) {
        return AssessmentPdfQuestionModel.builder()
                .typeId(TypeIfc.IMAGEMAP_QUESTION)
                .itemHtmlText("Identify the organs")
                .itemScore(Double.valueOf(1))
                .sequence("1")
                .text("Identify the organs")
                .imageSrc(imageSrc)
                .imageMapSrc(imageMapSrc)
                .imageMapItemTexts(List.of("Heart"))
                .imageMapRegionJsons(regions)
                .imageMapRows(List.of(new AssessmentPdfImageMapRowModel("1. Heart", Boolean.TRUE)))
                .itemGradingData(clicks)
                .build();
    }

    private static AssessmentPdfPrintSettingsModel printSettings(boolean showKeys) {
        return new AssessmentPdfPrintSettingsModel(showKeys, "3", Boolean.FALSE, Boolean.FALSE, Boolean.TRUE);
    }

    private static ContentHostingService contentHosting(String resourceId) throws Exception {
        ContentHostingService contentHostingService = mock(ContentHostingService.class);
        ContentResource contentResource = mock(ContentResource.class);
        when(contentHostingService.getResource(resourceId)).thenReturn(contentResource);
        when(contentResource.getContent()).thenReturn(TEST_PNG);
        return contentHostingService;
    }

    private static OverlayCapturingDocument renderPrint(AssessmentPdfQuestionModel question,
            AssessmentPdfPrintSettingsModel settings, ContentHostingService contentHostingService) throws Exception {
        AssessmentPdfContentHelper helper = new AssessmentPdfContentHelper(contentHostingService);
        AssessmentPrintPdfModel printModel = new AssessmentPrintPdfModel("Quiz", "", false, settings,
                List.of(new AssessmentPdfPartModel("Part 1", "", Collections.emptyList(), List.of(question))));
        return render(QuestionRenderContext.forPrint(question, 1, 1, printModel, helper), helper);
    }

    private static OverlayCapturingDocument renderReport(AssessmentPdfQuestionModel question,
            ContentHostingService contentHostingService) throws Exception {
        AssessmentPdfContentHelper helper = new AssessmentPdfContentHelper(contentHostingService);
        AssessmentStudentReportPdfModel reportModel = new AssessmentStudentReportPdfModel(
                "Student One", "Student", "student@example.com", null, "Quiz", "Site A", 1.0, 1.0, false,
                List.of(new AssessmentPdfPartModel("Part 1", "", Collections.emptyList(), List.of(question), "1", 1, 0, 1.0, 1.0)));
        return render(QuestionRenderContext.forReport(question, 1, 1, reportModel, helper), helper);
    }

    private static OverlayCapturingDocument render(QuestionRenderContext context, AssessmentPdfContentHelper helper)
            throws Exception {
        OverlayCapturingDocument document = new OverlayCapturingDocument();
        PdfWriter.getInstance(document, new ByteArrayOutputStream());
        document.open();
        new AssessmentPdfQuestionRenderer(helper).render(document, context);
        document.close();
        return document;
    }

    private static class OverlayCapturingDocument extends Document {
        private final List<ImageMapQuestionCellEvent> overlays = new ArrayList<>();

        @Override
        public boolean add(Element element) throws DocumentException {
            collectOverlays(element);
            return super.add(element);
        }

        private void collectOverlays(Element element) {
            if (!(element instanceof PdfPTable)) {
                return;
            }
            PdfPTable table = (PdfPTable) element;
            for (int i = 0; i < table.size(); i++) {
                PdfPRow row = table.getRow(i);
                if (row == null || row.getCells() == null) {
                    continue;
                }
                for (PdfPCell cell : row.getCells()) {
                    if (cell != null && cell.getCellEvent() instanceof ImageMapQuestionCellEvent) {
                        overlays.add((ImageMapQuestionCellEvent) cell.getCellEvent());
                    }
                }
            }
        }
    }

    private static byte[] createPng() {
        try {
            BufferedImage image = new BufferedImage(200, 150, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", output)) {
                throw new IllegalStateException("No PNG ImageIO writer is available");
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
