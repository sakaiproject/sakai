/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.poll.tool.service.PollPermissionsService;
import org.sakaiproject.poll.tool.service.PollResultsService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PollExportController {

    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    private final PollsService pollsService;
    private final MessageSource messageSource;
    private final ToolManager toolManager;
    private final UserTimeService userTimeService;
    private final PollResultsService pollResultsService;
    private final PollPermissionsService pollPermissionsService;

    public PollExportController(PollsService pollsService,
                                MessageSource messageSource,
                                ToolManager toolManager,
                                @Qualifier("org.sakaiproject.time.api.UserTimeService") UserTimeService userTimeService,
                                PollResultsService pollResultsService,
                                PollPermissionsService pollPermissionsService) {
        this.pollsService = pollsService;
        this.messageSource = messageSource;
        this.toolManager = toolManager;
        this.userTimeService = userTimeService;
        this.pollResultsService = pollResultsService;
        this.pollPermissionsService = pollPermissionsService;
    }

    @GetMapping("/polls/export/xlsx/{pollId}")
    public Object exportXlsx(@PathVariable("pollId") String pollId, Locale locale, RedirectAttributes redirectAttributes) {
        return exportPoll(pollId, ExportFormat.XLSX, locale, redirectAttributes);
    }

    @GetMapping("/polls/export/csv/{pollId}")
    public Object exportCsv(@PathVariable("pollId") String pollId, Locale locale, RedirectAttributes redirectAttributes) {
        return exportPoll(pollId, ExportFormat.CSV, locale, redirectAttributes);
    }

    private Object exportPoll(String pollId, ExportFormat format, Locale locale, RedirectAttributes redirectAttributes) {
        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        Optional<Poll> pollOpt = pollsService.getPollById(pollId);

        if (pollOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }

        Poll poll = pollOpt.get();

        if (!poll.getSiteId().equals(currentSiteId) || !pollPermissionsService.canEditPoll(poll)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }

        PollResultsService.PollResults results = pollResultsService.buildResults(poll, currentSiteId, locale);
        ZonedDateTime now = nowInSakaiZone();

        try {
            byte[] fileBytes = switch (format) {
                case XLSX -> buildXlsx(poll, results, now, locale);
                case CSV -> buildCsv(poll, results, now, locale);
            };
            String filename = buildExportFilename(poll.getText(), format.extension, now);
            return buildFileResponse(fileBytes, filename, format.mediaType);
        } catch (IOException | RuntimeException e) {
            log.error("Error generating {} for poll {}", format.extension.toUpperCase(Locale.ROOT), pollId, e);
            redirectAttributes.addFlashAttribute("alert",
                    messageSource.getMessage("poll_export_failed", new Object[] { pollId }, locale));
            return "redirect:/votePolls";
        }
    }

    private byte[] buildXlsx(Poll poll, PollResultsService.PollResults results, ZonedDateTime now, Locale locale) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            String sheetName = messageSource.getMessage("poll_export_sheet_name", null, locale);
            Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(sheetName));

            Font boldFont = wb.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 11);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setBorderBottom(BorderStyle.MEDIUM);

            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFont(boldFont);

            CellStyle percentStyle = wb.createCellStyle();
            DataFormat df = wb.createDataFormat();
            percentStyle.setDataFormat(df.getFormat("0.00%"));

            Row title1 = sheet.createRow(0);
            title1.createCell(0).setCellValue(messageSource.getMessage("poll_export_title", null, locale));
            title1.getCell(0).setCellStyle(titleStyle);

            Row title2 = sheet.createRow(2);
            title2.createCell(0).setCellValue(
                    messageSource.getMessage("poll_export_poll_label", null, locale) + " " + poll.getText()
            );
            title2.getCell(0).setCellStyle(titleStyle);

            String formattedDate = now.format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale)
            );

            Row title3 = sheet.createRow(3);
            title3.createCell(0).setCellValue(
                    messageSource.getMessage("poll_export_download_date", null, locale) + " " + formattedDate
            );
            title3.getCell(0).setCellStyle(titleStyle);

            Row header = sheet.createRow(5);
            String[] headers = {
                messageSource.getMessage("poll_export_header_option", null, locale),
                messageSource.getMessage("poll_export_header_votes", null, locale),
                messageSource.getMessage("poll_export_header_percentage", null, locale)
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 6;
            for (PollResultsService.ResultRow resultRow : results.getRows()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(resultRow.getChartLabel());

                Cell voteCell = row.createCell(1);
                voteCell.setCellValue(resultRow.getVotes());

                Cell percentCell = row.createCell(2);
                percentCell.setCellValue(resultRow.getPercentageValue());
                percentCell.setCellStyle(percentStyle);
            }

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private byte[] buildCsv(Poll poll, PollResultsService.PollResults results, ZonedDateTime now, Locale locale) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
            CSVWriter csvWriter = new CSVWriter(writer,
                    CSVWriter.DEFAULT_SEPARATOR,
                    CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.RFC4180_LINE_END)) {
            String formattedDate = now.format(
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale)
            );

            // Write a UTF-8 BOM so spreadsheet applications detect non-ASCII characters correctly.
            bos.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });

            csvWriter.writeNext(new String[] {
                sanitizeCsvCell(messageSource.getMessage("poll_export_title", null, locale))
            }, false);
            csvWriter.writeNext(new String[] {
                sanitizeCsvCell(messageSource.getMessage("poll_export_poll_label", null, locale)),
                sanitizeCsvCell(poll.getText())
            }, false);
            csvWriter.writeNext(new String[] {
                sanitizeCsvCell(messageSource.getMessage("poll_export_download_date", null, locale)),
                sanitizeCsvCell(formattedDate)
            }, false);
            csvWriter.writeNext(new String[0], false);

            csvWriter.writeNext(new String[] {
                sanitizeCsvCell(messageSource.getMessage("poll_export_header_option", null, locale)),
                sanitizeCsvCell(messageSource.getMessage("poll_export_header_votes", null, locale)),
                sanitizeCsvCell(messageSource.getMessage("poll_export_header_percentage", null, locale))
            }, false);

            for (PollResultsService.ResultRow resultRow : results.getRows()) {
                csvWriter.writeNext(new String[] {
                    sanitizeCsvCell(resultRow.getChartLabel()),
                    String.valueOf(resultRow.getVotes()),
                    sanitizeCsvCell(resultRow.getPercentageLabel())
                }, false);
            }

            csvWriter.flush();
            return bos.toByteArray();
        }
    }

    private String buildExportFilename(String pollText, String extension, ZonedDateTime now) {
        String safePollText = StringUtils.defaultString(pollText, "poll").replaceAll("[^a-zA-Z0-9_-]", "_");
        if (safePollText.length() > 30) {
            safePollText = safePollText.substring(0, 30);
        }

        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        return "Poll_" + safePollText + "_" + timestamp + "." + extension;
    }

    private ZonedDateTime nowInSakaiZone() {
        return ZonedDateTime.now(userTimeService.getLocalTimeZone().toZoneId());
    }

    private String sanitizeCsvCell(String value) {
        String text = StringUtils.defaultString(value);
        String trimmed = StringUtils.stripStart(text, null);
        if (StringUtils.startsWithAny(trimmed, "=", "+", "-", "@")) {
            text = "'" + text;
        }
        return text;
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] fileBytes, String filename, String mediaType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileBytes.length))
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(X_CONTENT_TYPE_OPTIONS, "nosniff")
                .contentType(MediaType.parseMediaType(mediaType))
                .body(fileBytes);
    }

    private enum ExportFormat {
        XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        CSV("csv", "text/csv;charset=UTF-8");

        private final String extension;
        private final String mediaType;

        ExportFormat(String extension, String mediaType) {
            this.extension = extension;
            this.mediaType = mediaType;
        }
    }
}
