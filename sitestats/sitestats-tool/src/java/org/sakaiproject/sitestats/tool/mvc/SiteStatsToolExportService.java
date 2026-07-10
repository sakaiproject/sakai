package org.sakaiproject.sitestats.tool.mvc;

import java.nio.charset.StandardCharsets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteStatsToolExportService {

    private static final String DEFAULT_FILENAME = "sitestats-report";
    private static final MediaType CSV_MEDIA_TYPE = MediaType.parseMediaType("text/csv");
    private static final MediaType EXCEL_MEDIA_TYPE = MediaType.parseMediaType("application/vnd.ms-excel");

    private final SiteStatsReportAccessService reportAccessService;
    private final SiteStatsReportExportService reportExportService;
    private final ReportManager reportManager;

    public ExportResult persistedReport(String siteId, long reportId, String format) {
        ReportDef reportDef = reportAccessService.persistedReportDefinition(siteId, reportId);
        Report report = reportExportService.getPersistedReport(siteId, reportId);
        return export(report, reportDef.getTitle(), format);
    }

    public ExportResult previewReport(String siteId, String previewId, String format) {
        Report report = reportExportService.getPreviewReport(siteId, previewId);
        return export(report, DEFAULT_FILENAME, format);
    }

    private ExportResult export(Report report, String title, String format) {
        String filename = StringUtils.defaultIfBlank(title, DEFAULT_FILENAME);
        if ("csv".equals(format)) {
            byte[] body = reportManager.getReportAsCsv(report).getBytes(StandardCharsets.UTF_8);
            return new ExportResult(body, CSV_MEDIA_TYPE, filename + ".csv");
        }
        if ("pdf".equals(format)) {
            return new ExportResult(reportManager.getReportAsPDF(report), MediaType.APPLICATION_PDF, filename + ".pdf");
        }
        return new ExportResult(reportManager.getReportAsExcel(report, title), EXCEL_MEDIA_TYPE, filename + ".xls");
    }

    @Getter
    @RequiredArgsConstructor
    public static class ExportResult {
        private final byte[] body;
        private final MediaType mediaType;
        private final String filename;
    }
}
