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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfAttachmentModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfFillInRowModel;
import org.sakaiproject.samigo.api.pdf.model.AssessmentPdfValueTypes.AssessmentPdfMediaModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;

import com.lowagie.text.Anchor;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import lombok.extern.slf4j.Slf4j;

import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfCellEvents.CheckOrCrossCellEvent;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BACKGROUND_GRAY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BODY_BOLD_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BODY_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.BORDER_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SECONDARY_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SMALL_BOLD_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.SMALL_FONT;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TEXT_LINK;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TEXT_PRIMARY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.TEXT_SECONDARY;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.WHITE_COLOR;
import static org.sakaiproject.samigo.impl.pdf.AssessmentPdfStyle.fontWithColor;

/**
 * Shared PDF content helpers extracted from assessment export rendering.
 */
@Slf4j
public class AssessmentPdfContentHelper {

    private final ContentHostingService contentHostingService;

    public AssessmentPdfContentHelper(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    /**
     * Strips HTML tags and removes embedded tables in a single pass.
     */
    public String cleanText(String text) {
        if (text == null) {
            return "";
        }
        org.jsoup.nodes.Document document = Jsoup.parse(text);
        document.select("table").remove();
        return document.text();
    }

    /**
     * Builds a question title table from HTML question text.
     */
    public PdfPTable getQuestionTitle(String questionText, boolean showAllInformation, boolean mathJaxEnabled) {
        return getQuestionTitle(questionText, showAllInformation, mathJaxEnabled, null);
    }

    /**
     * Builds a question title table from HTML question text with optional print font scaling.
     */
    public PdfPTable getQuestionTitle(String questionText, boolean showAllInformation, boolean mathJaxEnabled, String fontSizeSetting) {
        if (StringUtils.isBlank(questionText)) {
            return null;
        }

        PdfPTable auxTable = new PdfPTable(1);
        auxTable.setWidthPercentage(100f);
        configureSplittableTable(auxTable);

        String[] textSeparatedByLineBreak = questionText.split("<br />");
        StringBuilder finalTextBuilder = new StringBuilder();
        if (textSeparatedByLineBreak.length > 1) {
            for (String text : textSeparatedByLineBreak) {
                String cleanedText = cleanText(text);
                if (StringUtils.isNotEmpty(cleanedText)) {
                    finalTextBuilder.append(cleanedText).append('\n');
                }
            }
        } else {
            if (questionText.indexOf('\n') != -1) {
                textSeparatedByLineBreak = questionText.split("\n");
            }
            for (String text : textSeparatedByLineBreak) {
                String cleanedText = cleanText(text);
                if (StringUtils.isNotEmpty(cleanedText)) {
                    finalTextBuilder.append(cleanedText).append('\n');
                }
            }
        }
        String finalText = finalTextBuilder.toString().trim();
        boolean hasEmbeddedContent = showAllInformation && !Jsoup.parse(questionText).select("table, img").isEmpty();

        if (StringUtils.isEmpty(finalText) && !hasEmbeddedContent) {
            return null;
        }

        if (StringUtils.isNotEmpty(finalText)) {
            PdfPCell textCell = new PdfPCell();
            textCell.setBorder(Rectangle.NO_BORDER);
            textCell.setPadding(0f);
            configureSplittableCell(textCell);

            Font bodyBase = fontSizeSetting != null ? AssessmentPdfStyle.scaledFont(BODY_FONT, fontSizeSetting) : BODY_FONT;
            Font smallBase = fontSizeSetting != null ? AssessmentPdfStyle.scaledFont(SMALL_FONT, fontSizeSetting) : SMALL_FONT;
            Font textFont = showAllInformation ? fontWithColor(bodyBase, TEXT_PRIMARY) : fontWithColor(smallBase, TEXT_PRIMARY);
            Paragraph textParagraph = createLatexParagraph(finalText, textFont, mathJaxEnabled);
            textParagraph.setSpacingBefore(0f);
            textParagraph.setSpacingAfter(0f);
            textParagraph.setLeading(0f, 1.0f);
            textCell.addElement(textParagraph);
            auxTable.addCell(textCell);
        }

        if (showAllInformation) {
            addTableElementsToTable(questionText, auxTable);
            addImageElementsToTable(questionText, auxTable);
        }

        if (auxTable.getRows().isEmpty()) {
            return null;
        }

        return auxTable;
    }

    /**
     * Adds a question title table to the document when it has renderable content.
     */
    public void addQuestionTitleToDocument(Document document, String questionText, boolean showAllInformation, boolean mathJaxEnabled, String fontSizeSetting) throws Exception {
        PdfPTable titleTable = getQuestionTitle(questionText, showAllInformation, mathJaxEnabled, fontSizeSetting);
        if (titleTable != null) {
            titleTable.setSpacingAfter(AssessmentPdfStyle.ELEMENT_SPACING);
            document.add(titleTable);
        }
    }

    /**
     * Creates a paragraph with LaTeX segments rendered as images when MathJax is enabled.
     */
    public Paragraph createLatexParagraph(String text, Font font, boolean mathJaxEnabled) {
        Paragraph latexParagraph = new Paragraph();
        List<LatexChunk> chunks = AssessmentPdfLatexParser.parseLatexChunks(text, mathJaxEnabled);
        for (LatexChunk chunk : chunks) {
            if (chunk.getType() == LatexChunk.Type.TEXT) {
                latexParagraph.add(new Chunk(chunk.getContent(), font));
            } else {
                String latex = chunk.getContent().replace("@", "\\text{at}");
                try {
                    TeXFormula formula = new TeXFormula(latex);
                    Image pdfLatexImage = Image.getInstance(formula.createBufferedImage(TeXFormula.BOLD, 300, null, null), null);
                    float finalWidth = formula.createBufferedImage(TeXFormula.BOLD, 10, null, null).getWidth(null);
                    float finalHeight = formula.createBufferedImage(TeXFormula.BOLD, 10, null, null).getHeight(null);
                    pdfLatexImage.scaleAbsolute(finalWidth, finalHeight);
                    latexParagraph.add(new Chunk(pdfLatexImage, -1, -2, true));
                } catch (Exception ex) {
                    log.error(ex.getMessage(), ex);
                    latexParagraph.add(new Chunk(chunk.getContent(), font));
                }
            }
        }
        return latexParagraph;
    }

    /**
     * Adds HTML table elements from a string into a PDF table.
     */
    public void addTableElementsToTable(String text, PdfPTable table) {
        try {
            Elements tables = Jsoup.parse(text).select("table");
            for (org.jsoup.nodes.Element tableElement : tables) {
                org.jsoup.nodes.Element firstRow = tableElement.select("tr").first();
                if (firstRow == null) {
                    continue;
                }
                PdfPTable pdfTable = new PdfPTable(firstRow.children().size());
                for (org.jsoup.nodes.Element row : tableElement.select("tr")) {
                    for (org.jsoup.nodes.Element cell : row.children()) {
                        PdfPCell contentCell = new PdfPCell(new Paragraph(cell.text()));
                        contentCell.setBorderWidth(0);
                        contentCell.setBorderWidthBottom(1);
                        contentCell.setPadding(5f);
                        pdfTable.addCell(contentCell);
                    }
                }
                PdfPCell questionCell = new PdfPCell(pdfTable);
                questionCell.setBorderWidth(0);
                table.addCell(questionCell);
                PdfPCell blankLine = new PdfPCell(new Paragraph(Chunk.NEWLINE));
                blankLine.setBorderWidth(0);
                table.addCell(blankLine);
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * Builds HTML for a list of Samigo attachments (filename plus optional inline image).
     */
    public String buildAttachmentListHtml(List<?> attachmentList, String attachmentsLabel) {
        if (attachmentList == null || attachmentList.isEmpty()) {
            return "";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append("<br />");
        buffer.append(attachmentsLabel);
        for (Object attachmentObject : attachmentList) {
            String filename = null;
            String mimeType = null;
            String resourceId = null;
            if (attachmentObject instanceof AssessmentPdfAttachmentModel) {
                AssessmentPdfAttachmentModel attachment = (AssessmentPdfAttachmentModel) attachmentObject;
                filename = attachment.getFilename();
                mimeType = attachment.getMimeType();
                resourceId = attachment.getResourceId();
            } else if (attachmentObject instanceof AttachmentIfc) {
                AttachmentIfc attachment = (AttachmentIfc) attachmentObject;
                filename = attachment.getFilename();
                mimeType = attachment.getMimeType();
                resourceId = attachment.getResourceId();
            } else {
                continue;
            }
            buffer.append("<br />");
            appendAttachmentHtml(buffer, filename, mimeType, resourceId);
        }
        return buffer.toString();
    }

    /**
     * Renders Samigo attachment metadata and inline images into the PDF document.
     */
    public void addAttachmentListToDocument(Document document, List<?> attachmentList, String fontSizeSetting, boolean mathJaxEnabled) throws Exception {
        if (attachmentList == null || attachmentList.isEmpty()) {
            return;
        }

        Font smallFont = fontSizeSetting != null ? AssessmentPdfStyle.scaledFont(SMALL_FONT, fontSizeSetting) : SMALL_FONT;
        Font smallBoldFont = fontSizeSetting != null ? AssessmentPdfStyle.scaledFont(SMALL_BOLD_FONT, fontSizeSetting) : SMALL_BOLD_FONT;
        Font nameFont = fontWithColor(smallBoldFont, TEXT_LINK);
        Font urlFont = fontWithColor(smallFont, TEXT_SECONDARY);
        Font labelFont = fontWithColor(smallBoldFont, TEXT_SECONDARY);

        PdfPTable attachmentTable = new PdfPTable(1);
        attachmentTable.setWidthPercentage(100f);
        attachmentTable.setSpacingBefore(8f);
        configureSplittableTable(attachmentTable);

        PdfPCell labelCell = new PdfPCell(new Paragraph(AssessmentPdfBundle.getPrintString("attachments"), labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(0f);
        labelCell.setPaddingBottom(4f);
        configureSplittableCell(labelCell);
        attachmentTable.addCell(labelCell);

        boolean hasRenderableAttachment = false;
        for (Object attachmentObject : attachmentList) {
            String filename = null;
            String mimeType = null;
            String resourceId = null;
            if (attachmentObject instanceof AssessmentPdfAttachmentModel) {
                AssessmentPdfAttachmentModel attachment = (AssessmentPdfAttachmentModel) attachmentObject;
                filename = attachment.getFilename();
                mimeType = attachment.getMimeType();
                resourceId = attachment.getResourceId();
            } else if (attachmentObject instanceof AttachmentIfc) {
                AttachmentIfc attachment = (AttachmentIfc) attachmentObject;
                filename = attachment.getFilename();
                mimeType = attachment.getMimeType();
                resourceId = attachment.getResourceId();
            } else {
                continue;
            }
            if (StringUtils.isBlank(filename) && StringUtils.isBlank(resourceId)) {
                continue;
            }
            PdfPCell attachmentCell = createAttachmentCell(filename, mimeType, resourceId, nameFont, urlFont);
            configureSplittableCell(attachmentCell);
            attachmentTable.addCell(attachmentCell);
            hasRenderableAttachment = true;
        }

        if (hasRenderableAttachment) {
            document.add(attachmentTable);
        }
    }

    /**
     * Renders uploaded media files in student reports with clickable filenames.
     */
    public void addMediaFileListToDocument(Document document, List<AssessmentPdfMediaModel> mediaItems) throws Exception {
        if (mediaItems == null || mediaItems.isEmpty()) {
            return;
        }

        Font nameFont = fontWithColor(BODY_FONT, TEXT_LINK);
        Font urlFont = fontWithColor(SMALL_FONT, TEXT_SECONDARY);

        PdfPTable attachmentTable = new PdfPTable(1);
        attachmentTable.setWidthPercentage(100f);
        attachmentTable.setSpacingBefore(12f);
        configureSplittableTable(attachmentTable);

        for (AssessmentPdfMediaModel mediaData : mediaItems) {
            if (mediaData == null || StringUtils.isBlank(mediaData.getFilename())) {
                continue;
            }
            String accessUrl = buildShowMediaUrl(mediaData.getMediaId());
            PdfPCell fileCell = createLinkedFileCell(mediaData.getFilename(), accessUrl, nameFont, urlFont);
            configureSplittableCell(fileCell);
            attachmentTable.addCell(fileCell);
        }

        if (!attachmentTable.getRows().isEmpty()) {
            document.add(attachmentTable);
        }
    }

    /**
     * Builds the public Sakai content URL for a content-hosting resource id.
     */
    public static String buildContentAccessUrl(String resourceIdOrUrl) {
        if (StringUtils.isBlank(resourceIdOrUrl)) {
            return null;
        }
        String contentId = resolveContentResourceId(resourceIdOrUrl.trim());
        if (contentId == null) {
            return null;
        }
        try {
            String accessUrl = ServerConfigurationService.getAccessUrl();
            if (StringUtils.isNotBlank(accessUrl)) {
                String normalizedAccess = accessUrl.trim();
                if (normalizedAccess.startsWith("http://") || normalizedAccess.startsWith("https://")) {
                    return StringUtils.removeEnd(normalizedAccess, "/") + "/content" + contentId;
                }
            }
            String serverUrl = StringUtils.removeEnd(ServerConfigurationService.getServerUrl(), "/");
            if (StringUtils.isBlank(serverUrl) || "null".equalsIgnoreCase(serverUrl)) {
                return "/access/content" + contentId;
            }
            if (StringUtils.isBlank(accessUrl)) {
                accessUrl = "/access";
            } else if (!accessUrl.startsWith("/")) {
                accessUrl = "/" + accessUrl;
            }
            return serverUrl + accessUrl + "/content" + contentId;
        } catch (Exception e) {
            return "/access/content" + contentId;
        }
    }

    /**
     * Builds the ShowMedia servlet URL for a submitted assessment file.
     */
    public static String buildShowMediaUrl(Long mediaId) {
        if (mediaId == null) {
            return null;
        }
        try {
            String serverUrl = StringUtils.removeEnd(ServerConfigurationService.getServerUrl(), "/");
            return serverUrl + "/samigo-app/servlet/ShowMedia?mediaId=" + mediaId + "&setMimeType=false";
        } catch (Exception e) {
            return "/samigo-app/servlet/ShowMedia?mediaId=" + mediaId + "&setMimeType=false";
        }
    }

    private PdfPCell createAttachmentCell(String filename, String mimeType, String resourceId, Font nameFont, Font urlFont)
            throws Exception {
        String displayName = StringUtils.defaultIfBlank(filename, resourceId);
        String accessUrl = buildContentAccessUrl(resourceId);
        PdfPCell cell = createLinkedFileCell(displayName, accessUrl, nameFont, urlFont);

        String contentResourceId = resolveContentResourceId(StringUtils.defaultString(resourceId));
        if (isImageMimeType(mimeType) && StringUtils.isNotBlank(contentResourceId)) {
            Optional<Image> image = loadContentImage(contentResourceId);
            if (image.isPresent()) {
                Image loadedImage = image.get();
                scaleImageForPage(loadedImage);
                loadedImage.setSpacingBefore(6f);
                cell.addElement(loadedImage);
            }
        }
        return cell;
    }

    private PdfPCell createLinkedFileCell(String displayName, String accessUrl, Font nameFont, Font urlFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8f);
        cell.setBackgroundColor(BACKGROUND_GRAY);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(1f);
        cell.setBorderColorBottom(BORDER_COLOR);

        Paragraph fileParagraph = new Paragraph();
        fileParagraph.setLeading(0f, 1.3f);
        if (StringUtils.isNotBlank(accessUrl)) {
            Anchor fileLink = new Anchor(displayName, nameFont);
            fileLink.setReference(accessUrl);
            fileParagraph.add(fileLink);
            fileParagraph.add(Chunk.NEWLINE);
            Anchor urlLink = new Anchor(accessUrl, urlFont);
            urlLink.setReference(accessUrl);
            fileParagraph.add(urlLink);
        } else {
            fileParagraph.add(new Chunk(displayName, nameFont));
        }
        cell.addElement(fileParagraph);
        return cell;
    }

    /**
     * Configures a table to split across pages instead of leaving large blank areas.
     */
    public void configureSplittableTable(PdfPTable table) {
        table.setSplitLate(false);
        table.setSplitRows(true);
        table.setKeepTogether(false);
    }

    /**
     * Configures a cell so its content can break across pages when needed.
     */
    public void configureSplittableCell(PdfPCell cell) {
        cell.setNoWrap(false);
    }

    /**
     * Appends attachment HTML to a buffer. Image attachments include a {@code /samigo} image src
     * that {@link #addImageElementsToTable(String, PdfPTable)} resolves server-side.
     */
    public void appendAttachmentHtml(StringBuilder buffer, String filename, String mimeType, String resourceId) {
        buffer.append(StringEscapeUtils.escapeHtml4(StringUtils.defaultString(filename)));
        if (isImageMimeType(mimeType) && StringUtils.isNotBlank(resourceId)) {
            buffer.append("<br />  <img src=\"/samigo");
            buffer.append(resourceId);
            buffer.append("\" />");
        }
        buffer.append("<br />");
        buffer.append("<br />");
    }

    static boolean isImageMimeType(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        String mime = mimeType.toLowerCase();
        return mime.equals("image/jpeg") || mime.equals("image/pjpeg") || mime.equals("image/gif")
                || mime.equals("image/png");
    }

    /**
     * Adds HTML image elements from a string into a PDF table.
     */
    public void addImageElementsToTable(String text, PdfPTable table) {
        if (StringUtils.isBlank(text)) {
            return;
        }
        try {
            Elements imageElements = Jsoup.parse(text).select("img");
            for (org.jsoup.nodes.Element imageElement : imageElements) {
                String imageSrc = imageElement.attr("src");
                String resourceId = resolveContentResourceId(imageSrc);
                if (resourceId == null) {
                    log.warn("Could not resolve image src for PDF export: {}", imageSrc);
                    continue;
                }
                Optional<Image> image = loadContentImage(resourceId);
                if (image.isEmpty()) {
                    continue;
                }
                Image loadedImage = image.get();
                scaleImageForPage(loadedImage);
                PdfPCell imageCell = new PdfPCell(loadedImage);
                imageCell.setBorderWidth(0);
                imageCell.setPaddingTop(4f);
                configureSplittableCell(imageCell);
                table.addCell(imageCell);
            }
        } catch (Exception ex) {
            log.warn("Failed to add image elements to PDF table", ex);
        }
    }

    /**
     * Normalizes an HTML image src to a Sakai content hosting resource id.
     */
    static String resolveContentResourceId(String imageSrc) {
        if (StringUtils.isBlank(imageSrc)) {
            return null;
        }
        String src = imageSrc.trim();
        int accessContentMarker = src.indexOf("/access/content");
        if (accessContentMarker >= 0) {
            src = src.substring(accessContentMarker + "/access/content".length());
        } else {
            try {
                String serverUrl = StringUtils.removeEnd(ServerConfigurationService.getServerUrl(), "/");
                if (StringUtils.isNotBlank(serverUrl) && src.startsWith(serverUrl)) {
                    src = src.substring(serverUrl.length());
                }
            } catch (Exception e) {
                // continue with current src
            }
            if (src.startsWith("/samigo")) {
                src = src.substring("/samigo".length());
            }
            try {
                String accessUrl = ServerConfigurationService.getAccessUrl();
                if (StringUtils.isNotBlank(accessUrl)) {
                    String accessContentPrefix = accessUrl.endsWith("/")
                            ? accessUrl + "content"
                            : accessUrl + "/content";
                    if (src.startsWith(accessContentPrefix)) {
                        src = src.substring(accessContentPrefix.length());
                    }
                }
            } catch (Exception e) {
                // continue with current src
            }
        }
        try {
            src = URLDecoder.decode(src, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            // keep undecoded src
        }
        if (!src.startsWith("/")) {
            src = "/" + src;
        }
        return StringUtils.isBlank(src) ? null : src;
    }

    /**
     * Loads a content-hosting image for PDF export. {@link ContentHostingService#getResource(String)}
     * enforces {@code content.read} via {@code SecurityService}; denied access is logged and skipped.
     */
    Optional<Image> loadContentImage(String resourceId) {
        try {
            ContentResource resource = contentHostingService.getResource(resourceId);
            return Optional.of(Image.getInstance(resource.getContent()));
        } catch (PermissionException e) {
            log.warn("User lacks permission to read content resource for PDF export: {}", resourceId);
            return Optional.empty();
        } catch (IdUnusedException e) {
            log.warn("Content resource not found for PDF export: {}", resourceId);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Failed to load content resource for PDF export: {}", resourceId, e);
            return Optional.empty();
        }
    }

    /**
     * Scales an image to fit within the assessment PDF maximum dimensions.
     */
    public void scaleImageForPage(Image image) {
        float originalWidth = image.getWidth();
        float originalHeight = image.getHeight();
        if (originalWidth <= 0 || originalHeight <= 0) {
            return;
        }
        if (originalHeight > AssessmentPdfStyle.MAX_IMAGE_HEIGHT) {
            float scale = AssessmentPdfStyle.MAX_IMAGE_HEIGHT / originalHeight;
            image.scaleAbsolute(originalWidth * scale, originalHeight * scale);
        }
    }

    /**
     * Renders HTML (with images/LaTeX) or plain text into a PDF cell.
     */
    public void populateCellWithHtml(PdfPCell cell, String html, Font font, boolean mathJaxEnabled, String fontSizeSetting) throws Exception {
        if (StringUtils.isNotBlank(html) && html.contains("<")) {
            PdfPTable titleTable = getQuestionTitle(html, true, mathJaxEnabled, fontSizeSetting);
            if (titleTable != null) {
                cell.addElement(titleTable);
            }
        } else if (StringUtils.isNotBlank(html)) {
            cell.setPhrase(createLatexParagraph(html, font, mathJaxEnabled));
        }
    }

    public void styleStatsCell(PdfPCell cell, boolean rightAlign) {
        cell.setPaddingTop(8f);
        cell.setPaddingBottom(8f);
        cell.setPaddingLeft(0f);
        cell.setPaddingRight(0f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(1f);
        cell.setBorderColorBottom(BORDER_COLOR);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    public PdfPCell createSummaryHeaderCell(String text, boolean rightAlign) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, fontWithColor(BODY_BOLD_FONT, TEXT_PRIMARY)));
        cell.setPadding(8f);
        cell.setBorder(Rectangle.NO_BORDER);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        return cell;
    }

    public void styleSummaryBodyCell(PdfPCell cell, boolean rightAlign) {
        cell.setPadding(8f);
        cell.setBackgroundColor(BACKGROUND_GRAY);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthBottom(1.5f);
        cell.setBorderColorBottom(WHITE_COLOR);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    public void styleQuestionHeaderCell(PdfPCell cell, boolean rightAlign) {
        cell.setPaddingLeft(0f);
        cell.setPaddingRight(0f);
        cell.setPaddingTop(10f);
        cell.setPaddingBottom(10f);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthTop(1.5f);
        cell.setBorderColorTop(TEXT_PRIMARY);
        if (rightAlign) {
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
    }

    public void styleAnswerCell(PdfPCell cell, float paddingLeft) {
        cell.setBorderWidth(0);
        cell.setPaddingLeft(paddingLeft);
        cell.setPaddingTop(0f);
        cell.setPaddingBottom(2f);
    }

    public PdfPCell createInfoBoxCell(Color backgroundColor, Color leftBorderColor) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8f);
        cell.setBackgroundColor(backgroundColor);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBorderWidthLeft(2f);
        cell.setBorderColorLeft(leftBorderColor);
        return cell;
    }

    public void addInfoBox(Document document, Color backgroundColor, Color leftBorderColor, Paragraph content) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(12f);
        configureSplittableTable(table);
        PdfPCell cell = createInfoBoxCell(backgroundColor, leftBorderColor);
        configureSplittableCell(cell);
        cell.addElement(content);
        table.addCell(cell);
        document.add(table);
    }

    public void addCorrectResponseBox(Document document, String value) throws Exception {
        if (StringUtils.isBlank(value)) {
            return;
        }
        Paragraph paragraph = new Paragraph();
        paragraph.setLeading(0f, 1.2f);
        paragraph.add(new Chunk(AssessmentPdfBundle.getEvaluationString("correct_responses") + ": ", fontWithColor(SMALL_BOLD_FONT, SECONDARY_COLOR)));
        paragraph.add(new Chunk(value, fontWithColor(SMALL_FONT, TEXT_PRIMARY)));
        addInfoBox(document, BACKGROUND_GRAY, SECONDARY_COLOR, paragraph);
    }

    /**
     * Renders a fill-in-the-blank or numeric question and its response cells.
     */
    public void processFillInQuestion(Document document, List<AssessmentPdfFillInRowModel> fillInArray, boolean numeric, boolean mathJaxEnabled) throws Exception {
        PdfPTable fillInTable = new PdfPTable(1);
        fillInTable.setWidthPercentage(100f);
        fillInTable.setHorizontalAlignment(PdfPTable.ALIGN_LEFT);
        configureSplittableTable(fillInTable);
        int i = 0;
        StringBuilder questionText = new StringBuilder();
        for (AssessmentPdfFillInRowModel fillInRow : fillInArray) {
            if (i + 1 != fillInArray.size()) {
                questionText.append(fillInRow.getText()).append(" (").append(++i).append(") ");
                PdfPCell fillInCell = createFillInCell(i, fillInRow.getResponse(), fillInRow.getCorrect());
                fillInCell.setPaddingBottom(6f);
                fillInTable.addCell(fillInCell);
            } else {
                questionText.append(fillInRow.getText());
            }
        }
        addQuestionTitleToDocument(document, questionText.toString(), true, mathJaxEnabled, null);
        fillInTable.setSpacingBefore(AssessmentPdfStyle.ELEMENT_SPACING);
        fillInTable.setSpacingAfter(8f);
        document.add(fillInTable);
    }

    public PdfPCell createFillInCell(int position, String response, Boolean isCorrect) {
        String responseText = StringUtils.isEmpty(response) ? AssessmentPdfBundle.getAuthorString("no_answer.text") : response;
        PdfPCell fillInCell = new PdfPCell(new Phrase("(" + position + ") " + responseText, fontWithColor(BODY_FONT, TEXT_PRIMARY)));
        fillInCell.setPaddingBottom(2f);
        fillInCell.setPaddingLeft(2f);
        fillInCell.setBorder(Rectangle.NO_BORDER);
        fillInCell.setCellEvent(new CheckOrCrossCellEvent(isCorrect != null && isCorrect));
        return fillInCell;
    }
}
