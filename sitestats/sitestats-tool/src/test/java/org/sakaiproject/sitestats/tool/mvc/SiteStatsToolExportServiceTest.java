package org.sakaiproject.sitestats.tool.mvc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.sitestats.api.view.SiteStatsReportExportService;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolExportService.ExportResult;
import org.springframework.http.MediaType;

public class SiteStatsToolExportServiceTest {

    private static final String SITE_ID = "site-1";
    private static final long REPORT_ID = 42L;

    private SiteStatsReportAccessService reportAccessService;
    private SiteStatsReportExportService reportExportService;
    private ReportManager reportManager;
    private Report report;
    private SiteStatsToolExportService service;

    @Before
    public void setup() {
        reportAccessService = mock(SiteStatsReportAccessService.class);
        reportExportService = mock(SiteStatsReportExportService.class);
        reportManager = mock(ReportManager.class);
        report = mock(Report.class);
        service = new SiteStatsToolExportService(reportAccessService, reportExportService, reportManager);

        ReportDef reportDef = mock(ReportDef.class);
        when(reportDef.getTitle()).thenReturn("Activity report");
        when(reportAccessService.persistedReportDefinition(SITE_ID, REPORT_ID)).thenReturn(reportDef);
        when(reportExportService.getPersistedReport(SITE_ID, REPORT_ID)).thenReturn(report);
    }

    @Test
    public void persistedCsvIncludesUtf8BodyAndFilename() {
        when(reportManager.getReportAsCsv(report)).thenReturn("name,François");

        ExportResult result = service.persistedReport(SITE_ID, REPORT_ID, "csv");

        assertArrayEquals("name,François".getBytes(StandardCharsets.UTF_8), result.getBody());
        assertEquals(MediaType.parseMediaType("text/csv"), result.getMediaType());
        assertEquals("Activity report.csv", result.getFilename());
    }

    @Test
    public void persistedPdfIncludesPdfMetadata() {
        byte[] pdf = new byte[] {1, 2, 3};
        when(reportManager.getReportAsPDF(report)).thenReturn(pdf);

        ExportResult result = service.persistedReport(SITE_ID, REPORT_ID, "pdf");

        assertArrayEquals(pdf, result.getBody());
        assertEquals(MediaType.APPLICATION_PDF, result.getMediaType());
        assertEquals("Activity report.pdf", result.getFilename());
    }

    @Test
    public void persistedExcelPreservesDefaultFormatBehavior() {
        byte[] excel = new byte[] {4, 5, 6};
        when(reportManager.getReportAsExcel(report, "Activity report")).thenReturn(excel);

        ExportResult result = service.persistedReport(SITE_ID, REPORT_ID, "xls");

        assertArrayEquals(excel, result.getBody());
        assertEquals(MediaType.parseMediaType("application/vnd.ms-excel"), result.getMediaType());
        assertEquals("Activity report.xls", result.getFilename());
    }
}
