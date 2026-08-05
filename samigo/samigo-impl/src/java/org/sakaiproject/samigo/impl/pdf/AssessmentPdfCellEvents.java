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
        private final ArrayList<Rectangle> answerRectangles;
        private final ArrayList<ImageMapCircle> answerCircles;
        private final float originalWidth;
        private final float originalHeight;

        public ImageMapQuestionCellEvent(ArrayList<ImageMapCircle> answerCircles, ArrayList<Rectangle> answerRectangles, float originalWidth, float originalHeight) {
            this.answerCircles = answerCircles;
            this.answerRectangles = answerRectangles;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            if (originalWidth <= 0f || originalHeight <= 0f) {
                return;
            }
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            float x = position.getLeft();
            float y = position.getBottom();

            float scaleX = position.getWidth() / originalWidth;
            float scaleY = position.getHeight() / originalHeight;
            for (Rectangle answerRectangle : answerRectangles) {
                PdfGState transparentState = new PdfGState();
                transparentState.setFillOpacity(0.65f);
                transparentState.setStrokeOpacity(1f);
                float transformedX = x + answerRectangle.getLeft() * scaleX;
                float transformedY = y + position.getHeight() - answerRectangle.getTop() * scaleY;
                float transformedW = answerRectangle.getWidth() * scaleX;
                float transformedH = answerRectangle.getHeight() * scaleY;
                canvas.rectangle(transformedX, transformedY, transformedW, transformedH);
                canvas.setColorFill(Color.BLUE);
                canvas.setColorStroke(Color.BLACK);
                canvas.setGState(transparentState);
            }
            canvas.fillStroke();
            canvas.fill();

            float radius = 3f;
            int smallestValue = answerCircles.size() > 0 ? answerCircles.get(0).getPublishedItemId() + 1 : 0;
            for (ImageMapCircle answerCircle : answerCircles) {
                PdfGState transparentState = new PdfGState();
                transparentState.setFillOpacity(0.3f);
                transparentState.setStrokeOpacity(0.8f);
                float transformedX = x + answerCircle.getX() * scaleX;
                float transformedY = y + position.getHeight() - answerCircle.getY() * scaleY;
                if (answerCircle.getX() != 0 && answerCircle.getY() != 0) {
                    canvas.circle(transformedX, transformedY, radius);
                    canvas.setGState(transparentState);
                    canvas.circle(transformedX, transformedY, 0.3f);
                    canvas.setColorFill(Color.YELLOW);
                    canvas.setColorStroke(Color.YELLOW);
                }
                if (answerCircle.getPublishedItemId() < smallestValue) {
                    smallestValue = answerCircle.getPublishedItemId();
                }
            }
            canvas.fillStroke();
            canvas.fill();

            int questionIndex = 1;
            for (Rectangle answerRectangle : answerRectangles) {
                PdfGState transparentState = new PdfGState();
                transparentState.setFillOpacity(1f);
                transparentState.setStrokeOpacity(1f);
                float transformedX = x + answerRectangle.getLeft() * scaleX;
                float transformedY = y + position.getHeight() - answerRectangle.getTop() * scaleY;
                float transformedW = answerRectangle.getWidth() * scaleX;
                canvas.setGState(transparentState);
                try {
                    canvas.beginText();
                    canvas.setColorFill(Color.BLUE);
                    canvas.setFontAndSize(BaseFont.createFont(), 9);
                    canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(questionIndex), transformedX + transformedW + 1, transformedY, 0);
                    canvas.endText();
                    questionIndex++;
                } catch (Exception ex) {
                    log.error("Cannot write the number of the ImageMap.", ex);
                }
            }

            int toReduce = smallestValue;
            for (ImageMapCircle answerCircle : answerCircles) {
                float transformedX = x + answerCircle.getX() * scaleX;
                float transformedY = y + position.getHeight() - answerCircle.getY() * scaleY;
                if (answerCircle.getX() != 0 && answerCircle.getY() != 0) {
                    try {
                        int answerIndex = answerCircles.size() - (answerCircle.getPublishedItemId() - toReduce);
                        canvas.beginText();
                        canvas.setColorFill(Color.YELLOW);
                        canvas.setFontAndSize(BaseFont.createFont(), 9);
                        canvas.showTextAligned(Element.ALIGN_LEFT, String.valueOf(answerIndex), transformedX + 4, transformedY - 3, 0);
                        canvas.endText();
                        canvas.fill();
                    } catch (Exception ex) {
                        log.error("Cannot write the number of the ImageMap.", ex);
                    }
                }
            }

            PdfGState transparentState = new PdfGState();
            transparentState.setFillOpacity(1f);
            transparentState.setStrokeOpacity(1f);
            canvas.setGState(transparentState);
            canvas.setColorFill(Color.BLACK);
            canvas.setColorStroke(Color.BLACK);
        }
    }

    /**
     * Renders a check or cross icon in a PDF cell.
     */
    public static class CheckOrCrossCellEvent implements PdfPCellEvent {
        private final boolean isCheckIcon;

        public CheckOrCrossCellEvent(boolean isCheckIcon) {
            this.isCheckIcon = isCheckIcon;
        }

        @Override
        public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            float xAux = position.getLeft() + 2;
            float yAux = position.getTop() - 10;

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
        private int publishedItemId;

        public ImageMapCircle(float x, float y, int publishedItemId) {
            this.x = x;
            this.y = y;
            this.publishedItemId = publishedItemId;
        }
    }
}
