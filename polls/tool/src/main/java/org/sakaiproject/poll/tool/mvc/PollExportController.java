package org.sakaiproject.poll.tool.mvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sakaiproject.authz.api.SecurityService;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_EDIT_ANY;
import static org.sakaiproject.poll.api.PollConstants.PERMISSION_EDIT_OWN;
import org.sakaiproject.poll.api.model.Option;
import org.sakaiproject.poll.api.model.Poll;
import org.sakaiproject.poll.api.model.Vote;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PollExportController {

    private static final String X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";

    private final PollsService pollsService;
    private final MessageSource messageSource;
    private final ToolManager toolManager;
    private final SecurityService securityService;
    private final SiteService siteService;
    private final TimeService timeService;
    private final SessionManager sessionManager;

    @GetMapping("/polls/export/xlsx/{pollId}")
    public Object exportXlsx(@PathVariable("pollId") String pollId, Locale locale, RedirectAttributes redirectAttributes) {

        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        Optional<Poll> pollOpt = pollsService.getPollById(pollId);

        if (pollOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }

        Poll poll = pollOpt.get();

        if (!poll.getSiteId().equals(currentSiteId) || !canEditPoll(poll)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }

        List<Option> options = poll.getOptions();
        List<Vote> allVotes = pollsService.getAllVotesForPoll(poll.getId());
        long totalVotes = allVotes.size();
        int distinctVoters = pollsService.getDistinctVotersForPoll(poll);
        long percentageDenominator = getPercentageDenominator(poll, totalVotes, distinctVoters);
        ZonedDateTime now = nowInSakaiZone();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Poll Results");

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
            for (Option opt : options) {
                long votes = allVotes.stream()
                        .filter(v -> v.getOption().getId().equals(opt.getId()))
                        .count();

                double percentValue = percentageDenominator == 0 ? 0.0 : ((double) votes / percentageDenominator);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(opt.getText());

                Cell voteCell = row.createCell(1);
                voteCell.setCellValue(votes);

                Cell percentCell = row.createCell(2);
                percentCell.setCellValue(percentValue);
                percentCell.setCellStyle(percentStyle);
            }

            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            byte[] fileBytes = bos.toByteArray();

            String filename = buildExportFilename(poll.getText(), "xlsx", now);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileBytes.length))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header(X_CONTENT_TYPE_OPTIONS, "nosniff")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(fileBytes);

        } catch (IOException | RuntimeException e) {
            log.error("Error generating XLSX for poll {}", pollId, e);
            return "redirect:/votePolls";
        }
    }

    @GetMapping("/polls/export/csv/{pollId}")
    public Object exportCsv(@PathVariable("pollId") String pollId, Locale locale, RedirectAttributes redirectAttributes) {

        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        Optional<Poll> pollOpt = pollsService.getPollById(pollId);

        if (pollOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("poll_missing", null, locale));
            return "redirect:/votePolls";
        }

        Poll poll = pollOpt.get();

        if (!poll.getSiteId().equals(currentSiteId) || !canEditPoll(poll)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }

        List<Option> options = poll.getOptions();
        List<Vote> allVotes = pollsService.getAllVotesForPoll(poll.getId());
        long totalVotes = allVotes.size();
        int distinctVoters = pollsService.getDistinctVotersForPoll(poll);
        long percentageDenominator = getPercentageDenominator(poll, totalVotes, distinctVoters);
        ZonedDateTime now = nowInSakaiZone();

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

            for (Option opt : options) {
                long votes = allVotes.stream()
                        .filter(v -> v.getOption().getId().equals(opt.getId()))
                        .count();

                double percentValue = percentageDenominator == 0 ? 0.0 : ((double) votes / percentageDenominator) * 100;
                String percentLabel = String.format(locale, "%.2f%%", percentValue);

                csvWriter.writeNext(new String[] {
                    sanitizeCsvCell(opt.getText()),
                    String.valueOf(votes),
                    sanitizeCsvCell(percentLabel)
                }, false);
            }

            csvWriter.flush();
            byte[] fileBytes = bos.toByteArray();
            String filename = buildExportFilename(poll.getText(), "csv", now);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileBytes.length))
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header(HttpHeaders.EXPIRES, "0")
                    .header(X_CONTENT_TYPE_OPTIONS, "nosniff")
                    .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                    .body(fileBytes);

        } catch (IOException | RuntimeException e) {
            log.error("Error generating CSV for poll {}", pollId, e);
            return "redirect:/votePolls";
        }
    }

    private String buildExportFilename(String pollText, String extension, ZonedDateTime now) {
        String safePollText = pollText.replaceAll("[^a-zA-Z0-9-_]", "_");
        if (safePollText.length() > 30) {
            safePollText = safePollText.substring(0, 30);
        }

        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        return "Poll_" + safePollText + "_" + timestamp + "." + extension;
    }

    private ZonedDateTime nowInSakaiZone() {
        ZoneId sakaiZone = timeService.getLocalTimeZone().toZoneId();
        return ZonedDateTime.now(sakaiZone);
    }

    private long getPercentageDenominator(Poll poll, long totalVotes, int distinctVoters) {
        if (poll.getMaxOptions() == 1) {
            return totalVotes;
        }
        return distinctVoters;
    }

    private String sanitizeCsvCell(String value) {
        String text = StringUtils.defaultString(value);
        String trimmed = StringUtils.stripStart(text, null);
        if (StringUtils.startsWithAny(trimmed, "=", "+", "-", "@")) {
            text = "'" + text;
        }
        return text;
    }

    private boolean canEditPoll(Poll poll) {
        if (securityService.isSuperUser()) {
            return true;
        }
        String siteRef = siteService.siteReference(toolManager.getCurrentPlacement().getContext());
        if (securityService.unlock(PERMISSION_EDIT_ANY, siteRef)) {
            return true;
        }
        return securityService.unlock(PERMISSION_EDIT_OWN, siteRef) && StringUtils.equals(poll.getOwner(), sessionManager.getCurrentSessionUserId());
    }
}