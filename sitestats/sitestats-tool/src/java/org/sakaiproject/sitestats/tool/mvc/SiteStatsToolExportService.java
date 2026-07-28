package org.sakaiproject.sitestats.tool.mvc;

import java.nio.charset.StandardCharsets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteStatsToolExportService {

    private static final String DEFAULT_FILENAME = "sitestats-report";
    private static final MediaType CSV_MEDIA_TYPE = MediaType.parseMediaType("text/csv");
    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.ms-excel");

    private final SiteStatsReportExportService reportExportService;
    private final ReportManager reportManager;

    public ExportResult persistedReport(String siteId, long reportId, String format) {
        Report report = reportExportService.getPersistedReport(siteId, reportId);
        return export(report, report.getReportDefinition().getTitle(), format);
    }

    public ExportResult previewReport(String siteId, String previewId, String format) {
        Report report = reportExportService.getPreviewReport(siteId, previewId);
        return export(report, DEFAULT_FILENAME, format);
    }

    private ExportResult export(Report report, String title, String format) {
        String filename = StringUtils.defaultIfBlank(title, DEFAULT_FILENAME);
        switch (format) {
            case "csv":
                byte[] csvBody = reportManager.getReportAsCsv(report).getBytes(StandardCharsets.UTF_8);
                return new ExportResult(csvBody, CSV_MEDIA_TYPE, filename + ".csv");
            case "pdf":
                byte[] pdfBody = requireExportData(reportManager.getReportAsPDF(report));
                return new ExportResult(pdfBody, MediaType.APPLICATION_PDF, filename + ".pdf");
            case "xls":
                byte[] excelBody = requireExportData(reportManager.getReportAsExcel(report, title));
                return new ExportResult(excelBody, EXCEL_MEDIA_TYPE, filename + ".xls");
            default:
                throw new IllegalArgumentException("Unknown report export format: " + format);
        }
    }

    private byte[] requireExportData(byte[] body) {
        if (body == null || body.length == 0) {
            throw new SiteStatsOperationException(
                    "sitestats_error_report_export", "The report export could not be generated");
        }
        return body;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ExportResult {
        private final byte[] body;
        private final MediaType mediaType;
        private final String filename;
    }
}
