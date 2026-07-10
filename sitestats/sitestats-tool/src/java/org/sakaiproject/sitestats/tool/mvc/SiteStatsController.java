package org.sakaiproject.sitestats.tool.mvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.report.Report;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsServerWideReportIds;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.PreferencesForm;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.ReportForm;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.UserActivityForm;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequiredArgsConstructor
public class SiteStatsController {

    private final SiteStatsToolService toolService;
    private final SakaiFacade facade;
    private final MessageSource messageSource;

    @GetMapping({"/", "/index.html"})
    public String index() {
        return toolService.isAdminTool() ? "redirect:/admin" : "redirect:/home";
    }

    @GetMapping("/home")
    public String overview(@RequestParam(required = false) String siteId, Model model) {
        SiteStatsOverview overview = toolService.overview(siteId);
        Map<String, String> endpoints = new LinkedHashMap<String, String>();
        for (SiteStatsWidget widget : overview.getWidgets()) {
            if (widget.isVisible()) {
                for (SiteStatsWidgetTab tab : widget.getTabs()) {
                    endpoints.put(widget.getId() + ":" + tab.getId(), SiteStatsApiUrls.widgetReport(
                            overview.getSiteId(), widget.getId(), tab.getId(), reportRequest()));
                }
            }
        }
        commonModel(model, overview.getSiteId(), "overview");
        model.addAttribute("overview", overview);
        model.addAttribute("widgetEndpoints", endpoints);
        return "overview";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        commonModel(model, authorizedSiteId, "reports");
        model.addAttribute("reports", toolService.reports(authorizedSiteId));
        return "reports/list";
    }

    @GetMapping("/reports/new")
    public String newReport(@RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        ReportForm form = new ReportForm();
        commonReportForm(model, authorizedSiteId, form);
        return "reports/edit";
    }

    @GetMapping("/reports/{reportId}/edit")
    public String editReport(@PathVariable long reportId, @RequestParam(required = false) String siteId, Model model) {
        ReportDef report = toolService.reportDefinition(siteId, reportId);
        ReportForm form = ReportForm.from(report, facade.getUserTimeService().getLocalTimeZone().toZoneId());
        commonReportForm(model, report.getSiteId(), form);
        return "reports/edit";
    }

    @PostMapping("/reports/save")
    public String saveReport(@RequestParam(required = false) String siteId, @ModelAttribute ReportForm reportForm,
            @RequestParam String action, Model model, RedirectAttributes redirectAttributes) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        String validationCode = toolService.validateReport(reportForm);
        if (validationCode != null) {
            commonReportForm(model, authorizedSiteId, reportForm);
            model.addAttribute("error", message(validationCode));
            return "reports/edit";
        }
        if ("preview".equals(action)) {
            String previewId = toolService.previewReport(authorizedSiteId, reportForm);
            return "redirect:/reports/preview/" + previewId + "?siteId=" + authorizedSiteId;
        }
        long reportId = toolService.saveReport(authorizedSiteId, reportForm);
        redirectAttributes.addFlashAttribute("success", message("sitestats_report_saved"));
        return "redirect:/reports/" + reportId + "?siteId=" + authorizedSiteId;
    }

    @PostMapping("/reports/{reportId}/copy")
    public String copyReport(@PathVariable long reportId, @RequestParam(required = false) String siteId,
            RedirectAttributes redirectAttributes) {
        ReportDef report = toolService.reportDefinition(siteId, reportId);
        ReportForm form = ReportForm.from(report, facade.getUserTimeService().getLocalTimeZone().toZoneId());
        form.setId(0);
        form.setTemplateId(report.getId());
        long copyId = toolService.saveReport(report.getSiteId(), form);
        redirectAttributes.addFlashAttribute("success", message("sitestats_report_copied"));
        return "redirect:/reports/" + copyId + "?siteId=" + report.getSiteId();
    }

    @PostMapping("/reports/{reportId}/delete")
    public String deleteReport(@PathVariable long reportId, @RequestParam(required = false) String siteId,
            RedirectAttributes redirectAttributes) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        toolService.deleteReport(authorizedSiteId, reportId);
        redirectAttributes.addFlashAttribute("success", message("sitestats_report_deleted"));
        return "redirect:/reports?siteId=" + authorizedSiteId;
    }

    @GetMapping("/reports/{reportId}")
    public String report(@PathVariable long reportId, @RequestParam(required = false) String siteId, Model model) {
        ReportDef report = toolService.reportDefinition(siteId, reportId);
        commonModel(model, report.getSiteId(), "reports");
        model.addAttribute("report", report);
        model.addAttribute("reportEndpoint", SiteStatsApiUrls.persistedReport(report.getSiteId(), reportId, reportRequest()));
        model.addAttribute("preview", false);
        return "reports/view";
    }

    @GetMapping("/reports/preview/{previewId}")
    public String preview(@PathVariable String previewId, @RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        if (!facade.getSiteStatsReportExportService().canExportPreviewReport(authorizedSiteId, previewId)) {
            throw new IllegalArgumentException("The report preview expired or is unavailable");
        }
        commonModel(model, authorizedSiteId, "reports");
        model.addAttribute("reportEndpoint", SiteStatsApiUrls.previewReport(authorizedSiteId, previewId, reportRequest()));
        model.addAttribute("previewId", previewId);
        model.addAttribute("preview", true);
        return "reports/view";
    }

    @GetMapping("/reports/{reportId}/export/{format}")
    public ResponseEntity<byte[]> exportReport(@PathVariable long reportId, @PathVariable String format,
            @RequestParam(required = false) String siteId) {
        ReportDef reportDef = toolService.reportDefinition(siteId, reportId);
        Report report = facade.getSiteStatsReportExportService().getPersistedReport(reportDef.getSiteId(), reportId);
        return export(report, reportDef.getTitle(), format);
    }

    @GetMapping("/reports/preview/{previewId}/export/{format}")
    public ResponseEntity<byte[]> exportPreview(@PathVariable String previewId, @PathVariable String format,
            @RequestParam(required = false) String siteId) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        Report report = facade.getSiteStatsReportExportService().getPreviewReport(authorizedSiteId, previewId);
        return export(report, "sitestats-report", format);
    }

    @GetMapping("/preferences")
    public String preferences(@RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        PrefsData preferences = toolService.preferences(authorizedSiteId);
        PreferencesForm form = new PreferencesForm();
        form.setListToolEventsOnlyAvailableInSite(preferences.isListToolEventsOnlyAvailableInSite());
        form.setShowOwnStatisticsToStudents(preferences.isShowOwnStatisticsToStudents());
        form.setUseAllTools(preferences.isUseAllTools());
        form.setItemLabelsVisible(preferences.isItemLabelsVisible());
        form.setChartTransparency(preferences.getChartTransparency());
        form.setSelectedEventIds(new ArrayList<String>(preferences.getToolEventsStringList()));
        commonModel(model, authorizedSiteId, "preferences");
        model.addAttribute("preferencesForm", form);
        model.addAttribute("tools", preferences.getToolEventsDef());
        model.addAttribute("chartTransparencyChoices", Arrays.asList(
                1.0f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f));
        return "preferences";
    }

    @PostMapping("/preferences")
    public String savePreferences(@RequestParam(required = false) String siteId,
            @ModelAttribute PreferencesForm preferencesForm, RedirectAttributes redirectAttributes) {
        String authorizedSiteId = toolService.authorizedSite(siteId, false);
        toolService.savePreferences(authorizedSiteId, preferencesForm);
        redirectAttributes.addFlashAttribute("success", message("sitestats_preferences_saved"));
        return "redirect:/preferences?siteId=" + authorizedSiteId;
    }

    @GetMapping("/useractivity")
    public String userActivity(@RequestParam(required = false) String siteId,
            @ModelAttribute UserActivityForm userActivityForm, Model model) {
        SiteStatsToolService.UserActivityResult result = toolService.userActivity(siteId, userActivityForm);
        commonModel(model, result.getSiteId(), "useractivity");
        model.addAttribute("activity", result);
        model.addAttribute("userActivityForm", userActivityForm);
        return "user-activity";
    }

    @GetMapping("/useractivity/events/{eventId}")
    public String userActivityDetails(@PathVariable long eventId,
            @RequestParam(required = false) String siteId, Model model) {
        SiteStatsToolService.EventDetailsResult result = toolService.eventDetails(siteId, eventId);
        commonModel(model, result.getSiteId(), "useractivity");
        model.addAttribute("eventDetails", result);
        return "user-activity-details";
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) String search, @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") int page, Model model) {
        List<Site> sites = toolService.adminSites(search, type, page);
        commonModel(model, toolService.currentSiteId(), "admin");
        model.addAttribute("sites", sites);
        model.addAttribute("siteTypes", toolService.siteTypes());
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("page", page);
        model.addAttribute("lastJobRun", toolService.latestJobRun());
        return "admin/sites";
    }

    @GetMapping("/serverwide")
    public String serverWide(@RequestParam String siteId,
            @RequestParam(defaultValue = SiteStatsServerWideReportIds.MONTHLY_LOGIN) String reportType, Model model) {
        if (!SiteStatsServerWideReportIds.isSupported(reportType)) {
            throw new IllegalArgumentException("Unknown server-wide report");
        }
        String authorizedSiteId = toolService.authorizedSite(siteId, true);
        commonModel(model, authorizedSiteId, "serverwide");
        model.addAttribute("reportType", reportType);
        model.addAttribute("reportEndpoint", SiteStatsApiUrls.serverWideReport(authorizedSiteId, reportType));
        return "admin/server-wide";
    }

    @ExceptionHandler(SecurityException.class)
    public String forbidden(SecurityException exception, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        model.addAttribute("message", exception.getMessage());
        return "error/403";
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String notFound(RuntimeException exception, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        model.addAttribute("message", exception.getMessage());
        return "error/404";
    }

    private void commonReportForm(Model model, String siteId, ReportForm form) {
        commonModel(model, siteId, "reports");
        model.addAttribute("reportForm", form);
        model.addAttribute("templates", toolService.reportTemplates(siteId));
        model.addAttribute("editorOptions", toolService.reportEditorOptions(siteId));
    }

    private void commonModel(Model model, String siteId, String activeMenu) {
        model.addAttribute("siteId", siteId);
        model.addAttribute("activeMenu", activeMenu);
        model.addAttribute("adminTool", toolService.isAdminTool());
    }

    private SiteStatsReportRequest reportRequest() {
        SiteStatsReportRequest request = new SiteStatsReportRequest();
        request.setIncludeTable(true);
        request.setIncludeChart(true);
        return request;
    }

    private ResponseEntity<byte[]> export(Report report, String title, String format) {
        byte[] body;
        MediaType mediaType;
        String extension;
        if ("csv".equals(format)) {
            body = facade.getReportManager().getReportAsCsv(report).getBytes(StandardCharsets.UTF_8);
            mediaType = MediaType.parseMediaType("text/csv");
            extension = "csv";
        } else if ("pdf".equals(format)) {
            body = facade.getReportManager().getReportAsPDF(report);
            mediaType = MediaType.APPLICATION_PDF;
            extension = "pdf";
        } else {
            body = facade.getReportManager().getReportAsExcel(report, title);
            mediaType = MediaType.parseMediaType("application/vnd.ms-excel");
            extension = "xls";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(StringUtils.defaultIfBlank(title, "sitestats-report") + "." + extension, StandardCharsets.UTF_8)
                .build());
        return new ResponseEntity<byte[]>(body, headers, HttpStatus.OK);
    }

    private String message(String code) {
        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }
}
