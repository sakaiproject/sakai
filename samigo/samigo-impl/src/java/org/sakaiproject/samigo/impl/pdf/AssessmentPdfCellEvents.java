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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * PdfPCellEvent implementations for Samigo assessment PDF rendering.
 */
@Slf4j
public final class AssessmentPdfCellEvents {

    private AssessmentPdfCellEvents() {
    }

    /**
     * Renders a radio-button style circle in a PDF cell.
     */
    public static class CircleCellEvent implements PdfPCellEvent {
        private final boolean checked;
        private final boolean centered;

        public CircleCellEvent(boolean checked) {
            this(checked, false);
        }

        public CircleCellEvent(boolean checked, boolean centered) {
            this.checked = checked;
            this.centered = centered;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            float xAux = centered ? (position.getRight() - position.getLeft()) / 2 + position.getLeft() : position.getLeft() + 10;
            float yAux = centered ? (position.getTop() + position.getBottom()) / 2 : position.getTop() - 6f;
            float radius = 5f;

            canvas.circle(xAux, yAux, radius);
            canvas.setColorFill(Color.BLACK);
            canvas.fill();
            canvas.circle(xAux, yAux, radius * 0.95f);
            canvas.setColorFill(Color.WHITE);
            canvas.fill();
            if (checked) {
                canvas.circle(xAux, yAux, radius * 0.6f);
                canvas.setColorFill(Color.BLACK);
                canvas.fill();
            }

            canvas.setColorFill(Color.BLACK);
            canvas.setColorStroke(Color.BLACK);
        }
    }

    /**
     * Renders a checkbox style control in a PDF cell.
     */
    public static class CheckboxCellEvent implements PdfPCellEvent {
        private final boolean checked;

        public CheckboxCellEvent(boolean checked) {
            this.checked = checked;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            float xAux = position.getLeft() + 6;
            float yAux = position.getTop() - 10;

            canvas.rectangle(xAux, yAux, 8, 8);
            canvas.stroke();

            if (checked) {
                canvas.moveTo(xAux + 1.5f, yAux + 1.5f);
                canvas.lineTo(xAux + 6.5f, yAux + 6.5f);
                canvas.moveTo(xAux + 1.5f, yAux + 6.5f);
                canvas.lineTo(xAux + 6.5f, yAux + 1.5f);
                canvas.stroke();
            }
        }
    }

    /**
     * Overlays image-map answer regions and markers on a PDF cell.
     */
    public static class ImageMapQuestionCellEvent implements PdfPCellEvent {
        private static final float AREA_FILL_OPACITY = 0.18f;
        private static final float AREA_HALO_WIDTH = 3.5f;
        private static final float AREA_LINE_WIDTH = 1.2f;
        private static final float MARKER_RADIUS = 4.2f;
        private static final float MARKER_DOT_RADIUS = 1.4f;
        private static final float MARKER_LINE_WIDTH = 1.2f;
        private static final float LABEL_FONT_SIZE = 8f;
        private static final float LABEL_PADDING_X = 2.5f;
        private static final float LABEL_PADDING_Y = 1.8f;
        private static final float LABEL_ICON_SIZE = 4f;
        private static final float LABEL_ICON_GAP = 2f;
        private static final float LABEL_BORDER_WIDTH = 0.8f;
        private static final float LEADER_HALO_WIDTH = 3f;
        private static final float LEADER_LINE_WIDTH = 0.8f;

        private static final Color AREA_COLOR = AssessmentPdfStyle.TEXT_LINK;
        private static final Color MARKER_COLOR = AssessmentPdfStyle.TEXT_PRIMARY;
        private static final Color LEADER_COLOR = AssessmentPdfStyle.TEXT_SECONDARY;

        private final List<Rectangle> answerRectangles;
        private final List<Integer> answerRectangleIndexes;
        private final List<ImageMapCircle> answerCircles;
        private final float originalWidth;
        private final float originalHeight;
        private final float drawnWidth;
        private final float drawnHeight;

        /**
         * @param originalWidth  intrinsic image width, the coordinate space the stored answer
         *                       regions and responses are expressed in
         * @param originalHeight intrinsic image height, as above
         * @param drawnWidth     width the image actually occupies on the page after scaling
         * @param drawnHeight    height the image actually occupies on the page after scaling
         */
        public ImageMapQuestionCellEvent(List<ImageMapCircle> answerCircles, List<Rectangle> answerRectangles,
                List<Integer> answerRectangleIndexes, float originalWidth, float originalHeight, float drawnWidth,
                float drawnHeight) {
            this.answerCircles = answerCircles;
            this.answerRectangles = answerRectangles;
            this.answerRectangleIndexes = answerRectangleIndexes;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            this.drawnWidth = drawnWidth;
            this.drawnHeight = drawnHeight;
        }

        public List<Rectangle> getAnswerRectangles() {
            return answerRectangles;
        }

        public List<Integer> getAnswerRectangleIndexes() {
            return answerRectangleIndexes;
        }

        public List<ImageMapCircle> getAnswerCircles() {
            return answerCircles;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            if (originalWidth <= 0f || originalHeight <= 0f) {
                return;
            }
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            // Anchor on the image, not the cell: the image is added with no padding so it sits at
            // the cell's top left, but the cell is full width while the image is scaled down to
            // fit the page. Scaling by the cell would stretch every overlay past the image.
            float x = position.getLeft();
            float y = position.getTop() - drawnHeight;

            float scaleX = drawnWidth / originalWidth;
            float scaleY = drawnHeight / originalHeight;
            Rectangle imageBounds = new Rectangle(x, y, x + drawnWidth, y + drawnHeight);

            List<RegionBox> regionBoxes = transformedRegions(x, y, scaleX, scaleY);
            List<Marker> markers = transformedMarkers(x, y, scaleX, scaleY);

            drawRegions(canvas, regionBoxes);
            drawMarkers(canvas, markers);
            drawLabels(canvas, regionBoxes, markers, imageBounds);
            resetGraphicsState(canvas);
        }

        private List<RegionBox> transformedRegions(float x, float y, float scaleX, float scaleY) {
            List<RegionBox> boxes = new ArrayList<>();
            for (int i = 0; i < answerRectangles.size(); i++) {
                Rectangle region = answerRectangles.get(i);
                float left = x + region.getLeft() * scaleX;
                float right = x + region.getRight() * scaleX;
                float bottom = y + drawnHeight - region.getTop() * scaleY;
                float top = y + drawnHeight - region.getBottom() * scaleY;
                if (right > left && top > bottom) {
                    int areaIndex = answerRectangleIndexes != null && i < answerRectangleIndexes.size()
                            ? answerRectangleIndexes.get(i)
                            : i + 1;
                    boxes.add(new RegionBox(new Rectangle(left, bottom, right, top), areaIndex));
                }
            }
            return boxes;
        }

        private List<Marker> transformedMarkers(float x, float y, float scaleX, float scaleY) {
            List<Marker> markers = new ArrayList<>();
            for (ImageMapCircle circle : answerCircles) {
                if (circle.getX() == 0f || circle.getY() == 0f) {
                    continue;
                }
                float markerX = x + circle.getX() * scaleX;
                float markerY = y + drawnHeight - circle.getY() * scaleY;
                markers.add(new Marker(markerX, markerY, circle.getSequence()));
            }
            return markers;
        }

        private static void drawRegions(PdfContentByte canvas, List<RegionBox> regionBoxes) {
            for (RegionBox regionBox : regionBoxes) {
                Rectangle region = regionBox.getBox();
                canvas.saveState();
                PdfGState fillState = new PdfGState();
                fillState.setFillOpacity(AREA_FILL_OPACITY);
                canvas.setGState(fillState);
                canvas.setColorFill(AREA_COLOR);
                addRectangle(canvas, region);
                canvas.fill();
                canvas.restoreState();

                // White halo first so the outline stays visible on dark or busy images, then a
                // thinner coloured line on top of it.
                canvas.setLineWidth(AREA_HALO_WIDTH);
                canvas.setColorStroke(Color.WHITE);
                addRectangle(canvas, region);
                canvas.stroke();
                canvas.setLineWidth(AREA_LINE_WIDTH);
                canvas.setColorStroke(AREA_COLOR);
                addRectangle(canvas, region);
                canvas.stroke();
            }
        }

        private static void drawMarkers(PdfContentByte canvas, List<Marker> markers) {
            for (Marker marker : markers) {
                // White disc with dark border and centre dot stays readable on any image colour.
                canvas.setColorFill(Color.WHITE);
                canvas.circle(marker.getX(), marker.getY(), MARKER_RADIUS);
                canvas.fill();
                canvas.setLineWidth(MARKER_LINE_WIDTH);
                canvas.setColorStroke(MARKER_COLOR);
                canvas.circle(marker.getX(), marker.getY(), MARKER_RADIUS);
                canvas.stroke();
                canvas.setColorFill(MARKER_COLOR);
                canvas.circle(marker.getX(), marker.getY(), MARKER_DOT_RADIUS);
                canvas.fill();
            }
        }

        private void drawLabels(PdfContentByte canvas, List<RegionBox> regionBoxes, List<Marker> markers,
                Rectangle imageBounds) {
            try {
                BaseFont font = BaseFont.createFont();
                List<ImageMapLabelLayout.Label> labels = new ArrayList<>();
                List<Rectangle> obstacles = new ArrayList<>();

                for (RegionBox regionBox : regionBoxes) {
                    Rectangle region = regionBox.getBox();
                    float centerX = (region.getLeft() + region.getRight()) / 2f;
                    float centerY = (region.getBottom() + region.getTop()) / 2f;
                    obstacles.add(anchorBox(centerX, centerY, 2.5f));
                    labels.add(newLabel(String.valueOf(regionBox.getAreaIndex()), centerX, centerY, font, true));
                }
                for (Marker marker : markers) {
                    obstacles.add(anchorBox(marker.getX(), marker.getY(), MARKER_RADIUS + 1f));
                    labels.add(newLabel(String.valueOf(marker.getSequence()), marker.getX(), marker.getY(), font, false));
                }

                List<ImageMapLabelLayout.Placement> placements =
                        ImageMapLabelLayout.place(labels, obstacles, imageBounds);
                // Leader lines first so no line is ever drawn over a previously painted badge.
                for (ImageMapLabelLayout.Placement placement : placements) {
                    if (placement.isLeaderLineVisible()) {
                        drawLeaderLine(canvas, placement);
                    }
                }
                for (ImageMapLabelLayout.Placement placement : placements) {
                    drawBadge(canvas, font, placement);
                }
            } catch (Exception ex) {
                log.error("Cannot write the number of the ImageMap.", ex);
            }
        }

        private static ImageMapLabelLayout.Label newLabel(String text, float anchorX, float anchorY, BaseFont font,
                boolean areaLabel) {
            float textWidth = font.getWidthPoint(text, LABEL_FONT_SIZE);
            float width = LABEL_PADDING_X * 2f + LABEL_ICON_SIZE + LABEL_ICON_GAP + textWidth;
            float height = Math.max(LABEL_FONT_SIZE, LABEL_ICON_SIZE) + LABEL_PADDING_Y * 2f;
            float clearance = areaLabel ? 1f : MARKER_RADIUS + 0.8f;
            return new ImageMapLabelLayout.Label(text, anchorX, anchorY, width, height, areaLabel, clearance);
        }

        private static void drawLeaderLine(PdfContentByte canvas, ImageMapLabelLayout.Placement placement) {
            ImageMapLabelLayout.Label label = placement.getLabel();
            Rectangle box = placement.getBox();
            float endX = Math.min(Math.max(label.getAnchorX(), box.getLeft()), box.getRight());
            float endY = Math.min(Math.max(label.getAnchorY(), box.getBottom()), box.getTop());
            float dx = endX - label.getAnchorX();
            float dy = endY - label.getAnchorY();
            if (dx * dx + dy * dy < 1f) {
                return;
            }
            canvas.setLineWidth(LEADER_HALO_WIDTH);
            canvas.setColorStroke(Color.WHITE);
            canvas.moveTo(label.getAnchorX(), label.getAnchorY());
            canvas.lineTo(endX, endY);
            canvas.stroke();
            canvas.setLineWidth(LEADER_LINE_WIDTH);
            canvas.setColorStroke(LEADER_COLOR);
            canvas.moveTo(label.getAnchorX(), label.getAnchorY());
            canvas.lineTo(endX, endY);
            canvas.stroke();
        }

        private static void drawBadge(PdfContentByte canvas, BaseFont font,
                ImageMapLabelLayout.Placement placement) {
            ImageMapLabelLayout.Label label = placement.getLabel();
            Rectangle box = placement.getBox();

            canvas.setColorFill(Color.WHITE);
            addRectangle(canvas, box);
            canvas.fill();
            canvas.setLineWidth(LABEL_BORDER_WIDTH);
            canvas.setColorStroke(MARKER_COLOR);
            if (!label.isAreaLabel()) {
                // Dashed border for student markers, solid for correct areas, so both stay
                // distinguishable without relying on colour.
                canvas.setLineDash(2f, 1.2f, 0f);
            }
            addRectangle(canvas, box);
            canvas.stroke();
            canvas.setLineDash(0f);

            float centerY = (box.getBottom() + box.getTop()) / 2f;
            float iconLeft = box.getLeft() + LABEL_PADDING_X;
            if (label.isAreaLabel()) {
                canvas.setColorFill(AREA_COLOR);
                canvas.rectangle(iconLeft, centerY - LABEL_ICON_SIZE / 2f, LABEL_ICON_SIZE, LABEL_ICON_SIZE);
                canvas.fill();
            } else {
                canvas.setColorFill(MARKER_COLOR);
                canvas.circle(iconLeft + LABEL_ICON_SIZE / 2f, centerY, LABEL_ICON_SIZE / 2f);
                canvas.fill();
            }

            float textStart = iconLeft + LABEL_ICON_SIZE + LABEL_ICON_GAP;
            float textEnd = box.getRight() - LABEL_PADDING_X;
            float baseline = centerY
                    - (font.getAscentPoint(label.getText(), LABEL_FONT_SIZE)
                            + font.getDescentPoint(label.getText(), LABEL_FONT_SIZE)) / 2f;
            canvas.beginText();
            canvas.setFontAndSize(font, LABEL_FONT_SIZE);
            canvas.setColorFill(MARKER_COLOR);
            canvas.showTextAligned(Element.ALIGN_CENTER, label.getText(), (textStart + textEnd) / 2f, baseline, 0);
            canvas.endText();
        }

        private static Rectangle anchorBox(float centerX, float centerY, float halfSize) {
            return new Rectangle(centerX - halfSize, centerY - halfSize, centerX + halfSize, centerY + halfSize);
        }

        private static void addRectangle(PdfContentByte canvas, Rectangle box) {
            canvas.rectangle(box.getLeft(), box.getBottom(), box.getWidth(), box.getHeight());
        }

        private static void resetGraphicsState(PdfContentByte canvas) {
            PdfGState opaqueState = new PdfGState();
            opaqueState.setFillOpacity(1f);
            opaqueState.setStrokeOpacity(1f);
            canvas.setGState(opaqueState);
            canvas.setLineDash(0f);
            canvas.setLineWidth(1f);
            canvas.setColorFill(Color.BLACK);
            canvas.setColorStroke(Color.BLACK);
        }

        private static final class RegionBox {
            private final Rectangle box;
            private final int areaIndex;

            private RegionBox(Rectangle box, int areaIndex) {
                this.box = box;
                this.areaIndex = areaIndex;
            }

            private Rectangle getBox() {
                return box;
            }

            private int getAreaIndex() {
                return areaIndex;
            }
        }

        private static final class Marker {
            private final float x;
            private final float y;
            private final int sequence;

            private Marker(float x, float y, int sequence) {
                this.x = x;
                this.y = y;
                this.sequence = sequence;
            }

            private float getX() {
                return x;
            }

            private float getY() {
                return y;
            }

            private int getSequence() {
                return sequence;
            }
        }
    }

    /**
     * Renders a check or cross icon in a PDF cell.
     */
    public static class CheckOrCrossCellEvent implements PdfPCellEvent {
        private final boolean isCheckIcon;
        private final boolean centered;

        public CheckOrCrossCellEvent(boolean isCheckIcon) {
            this(isCheckIcon, false);
        }

        public CheckOrCrossCellEvent(boolean isCheckIcon, boolean centered) {
            this.isCheckIcon = isCheckIcon;
            this.centered = centered;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            float xAux;
            float yAux;
            if (centered) {
                xAux = (position.getLeft() + position.getRight()) / 2f - 1.5f;
                yAux = (position.getTop() + position.getBottom()) / 2f - (isCheckIcon ? 2f : 0f);
            } else {
                xAux = position.getLeft() + 2;
                yAux = position.getTop() - 10;
            }

            if (isCheckIcon) {
                canvas.setLineWidth(2.5f);
                canvas.setColorStroke(AssessmentPdfStyle.SUCCESS_COLOR);
                canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
                canvas.moveTo(xAux - 3, yAux + 1);
                canvas.lineTo(xAux, yAux - 2);
                canvas.lineTo(xAux + 6, yAux + 6);
                canvas.stroke();
            } else {
                canvas.setLineWidth(2.5f);
                canvas.setColorStroke(AssessmentPdfStyle.ERROR_COLOR);
                canvas.setLineCap(PdfContentByte.LINE_CAP_ROUND);
                canvas.moveTo(xAux - 3, yAux - 3);
                canvas.lineTo(xAux + 3, yAux + 3);
                canvas.stroke();
                canvas.moveTo(xAux - 3, yAux + 3);
                canvas.lineTo(xAux + 3, yAux - 3);
                canvas.stroke();
            }

            canvas.setLineWidth(1f);
            canvas.setColorFill(Color.BLACK);
            canvas.setColorStroke(Color.BLACK);
            canvas.setLineCap(PdfContentByte.LINE_CAP_BUTT);
        }
    }

    /**
     * Circle marker for image-map question overlays.
     */
    @Getter
    @Setter
    public static class ImageMapCircle {
        private float x;
        private float y;
        private int sequence;

        public ImageMapCircle(float x, float y, int sequence) {
            this.x = x;
            this.y = y;
            this.sequence = sequence;
        }
    }
}
