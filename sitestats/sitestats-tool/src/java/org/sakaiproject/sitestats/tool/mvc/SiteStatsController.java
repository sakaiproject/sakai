/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.mvc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.view.SiteStatsApiUrls;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsServerWideReportIds;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolExportService.ExportResult;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.CopiedReport;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.OverviewResult;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.PreferencesForm;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.PreferencesResult;
import org.sakaiproject.sitestats.tool.mvc.SiteStatsToolService.UserActivityForm;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SiteStatsController {

    private final SiteStatsToolService toolService;
    private final SiteStatsToolExportService exportService;
    private final MessageSource messageSource;

    @GetMapping({"/", "/index.html"})
    public String index(RedirectAttributes redirectAttributes) {
        return toolService.isAdminTool() ? "redirect:/admin" : "redirect:/home";
    }

    @GetMapping("/home")
    public String overview(@RequestParam(required = false) String siteId, Model model) {
        OverviewResult result = toolService.overviewWithEndpoints(siteId);
        commonModel(model, result.getOverview().getSiteId(), "overview");
        model.addAttribute("overview", result.getOverview());
        model.addAttribute("widgetEndpoints", result.getWidgetEndpoints());
        model.addAttribute("widgetHighlightsJson", result.getWidgetHighlightsJson());
        return "overview";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.reportSite(siteId);
        commonModel(model, authorizedSiteId, "reports");
        model.addAttribute("reports", toolService.reports(authorizedSiteId));
        return "reports/list";
    }

    @GetMapping("/reports/new")
    public String newReport(@RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.reportSite(siteId);
        SiteStatsReportForm form = toolService.newReportForm();
        commonReportForm(model, authorizedSiteId, form);
        return "reports/edit";
    }

    @GetMapping("/reports/users")
    @ResponseBody
    public List<SiteStatsToolService.NamedOption> reportUsers(@RequestParam(required = false) String siteId,
            @RequestParam String q) {
        return toolService.searchReportUsers(siteId, q);
    }

    @GetMapping("/reports/{reportId}/edit")
    public String editReport(@PathVariable long reportId, @RequestParam(required = false) String siteId, Model model) {
        String authorizedSiteId = toolService.reportSite(siteId);
        SiteStatsReportForm form = toolService.editReportForm(authorizedSiteId, reportId);
        commonReportForm(model, authorizedSiteId, form);
        return "reports/edit";
    }

    @PostMapping("/reports/save")
    public String saveReport(@RequestParam(required = false) String siteId, @ModelAttribute SiteStatsReportForm reportForm,
            @RequestParam String action, Model model, RedirectAttributes redirectAttributes) {
        String authorizedSiteId = toolService.reportSite(siteId);
        String validationCode = toolService.validateReport(authorizedSiteId, reportForm);
        if (validationCode != null) {
            commonReportForm(model, authorizedSiteId, reportForm);
            model.addAttribute("error", message(validationCode));
            model.addAttribute("errorCode", validationCode);
            return "reports/edit";
        }
        if ("preview".equals(action)) {
            String previewId = toolService.previewReport(authorizedSiteId, reportForm);
            return "redirect:/reports/preview/" + previewId + "?siteId=" + authorizedSiteId;
        }
        long reportId = toolService.saveReport(authorizedSiteId, reportForm);
        redirectAttributes.addFlashAttribute("success", message("report_save_success", reportForm.getTitle()));
        return "redirect:/reports/" + reportId + "?siteId=" + authorizedSiteId;
    }

    @PostMapping("/reports/{reportId}/copy")
    public String copyReport(@PathVariable long reportId, @RequestParam(required = false) String siteId,
            RedirectAttributes redirectAttributes) {
        CopiedReport copiedReport = toolService.copyReport(siteId, reportId);
        redirectAttributes.addFlashAttribute("success", message("report_copy_success"));
        return "redirect:/reports/" + copiedReport.getReportId() + "?siteId=" + copiedReport.getSiteId();
    }

    @PostMapping("/reports/{reportId}/delete")
    public String deleteReport(@PathVariable long reportId, @RequestParam(required = false) String siteId,
            RedirectAttributes redirectAttributes) {
        String authorizedSiteId = toolService.reportSite(siteId);
        toolService.deleteReport(authorizedSiteId, reportId);
        redirectAttributes.addFlashAttribute("success", message("report_delete_success"));
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
        String authorizedSiteId = toolService.reportSite(siteId);
        if (!toolService.canViewPreview(authorizedSiteId, previewId)) {
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
        String authorizedSiteId = toolService.reportSite(siteId);
        return export(exportService.persistedReport(authorizedSiteId, reportId, format));
    }

    @GetMapping("/reports/preview/{previewId}/export/{format}")
    public ResponseEntity<byte[]> exportPreview(@PathVariable String previewId, @PathVariable String format,
            @RequestParam(required = false) String siteId) {
        String authorizedSiteId = toolService.reportSite(siteId);
        return export(exportService.previewReport(authorizedSiteId, previewId, format));
    }

    @GetMapping("/preferences")
    public String preferences(@RequestParam(required = false) String siteId, Model model) {
        PreferencesResult result = toolService.preferences(siteId);
        commonModel(model, result.getSiteId(), "preferences");
        model.addAttribute("preferencesForm", result.getForm());
        model.addAttribute("tools", result.getTools());
        model.addAttribute("chartTransparencyChoices", Arrays.asList(
                1.0f, 0.9f, 0.8f, 0.7f, 0.6f, 0.5f, 0.4f, 0.3f, 0.2f, 0.1f));
        return "preferences";
    }

    @PostMapping("/preferences")
    public String savePreferences(@RequestParam(required = false) String siteId,
            @ModelAttribute PreferencesForm preferencesForm, RedirectAttributes redirectAttributes) {
        String authorizedSiteId = toolService.reportSite(siteId);
        toolService.savePreferences(authorizedSiteId, preferencesForm);
        redirectAttributes.addFlashAttribute("success", message("prefs_updated"));
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
            @RequestParam(required = false) String siteId,
            @ModelAttribute UserActivityForm userActivityForm, Model model) {
        SiteStatsToolService.EventDetailsResult result = toolService.eventDetails(siteId, eventId);
        commonModel(model, result.getSiteId(), "useractivity");
        model.addAttribute("eventDetails", result);
        model.addAttribute("userActivityForm", userActivityForm);
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
        String authorizedSiteId = toolService.adminSite(siteId);
        commonModel(model, authorizedSiteId, "serverwide");
        model.addAttribute("reportType", reportType);
        model.addAttribute("reportEndpoint", SiteStatsApiUrls.serverWideReport(authorizedSiteId, reportType));
        return "admin/server-wide";
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Void> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> notFound() {
        return ResponseEntity.notFound().build();
    }

    private void commonReportForm(Model model, String siteId, SiteStatsReportForm form) {
        commonModel(model, siteId, "reports");
        SiteStatsToolService.ReportEditorOptions editorOptions = toolService.reportEditorOptions(
                siteId, form.getWhoUserIds());
        toolService.prepareReportForm(form, editorOptions);
        model.addAttribute("reportForm", form);
        model.addAttribute("editorOptions", editorOptions);
    }

    private void commonModel(Model model, String siteId, String activeMenu) {
        model.addAttribute("siteId", siteId);
        model.addAttribute("activeMenu", activeMenu);
        boolean adminTool = toolService.isAdminTool();
        model.addAttribute("adminTool", adminTool);
        model.addAttribute("toolMenuAvailable", adminTool || toolService.canViewAllSiteStats(siteId));
        model.addAttribute("userActivityAvailable", !adminTool && toolService.canViewUserActivity(siteId));
    }

    private SiteStatsReportRequest reportRequest() {
        SiteStatsReportRequest request = new SiteStatsReportRequest();
        request.setIncludeTable(true);
        request.setIncludeChart(true);
        return request;
    }

    private ResponseEntity<byte[]> export(ExportResult exportResult) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(exportResult.getMediaType());
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(exportResult.getFilename(), StandardCharsets.UTF_8)
                .build());
        return new ResponseEntity<byte[]>(exportResult.getBody(), headers, HttpStatus.OK);
    }

    private String message(String code, Object... arguments) {
        return messageSource.getMessage(code, arguments, code, LocaleContextHolder.getLocale());
    }
}
