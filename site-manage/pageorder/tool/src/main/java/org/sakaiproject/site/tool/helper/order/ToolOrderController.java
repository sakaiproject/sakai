/**
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
package org.sakaiproject.site.tool.helper.order;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;
import org.sakaiproject.site.tool.helper.order.model.ToolOrderPage;
import org.sakaiproject.util.api.LocaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ToolOrderController {

    private final SitePageEditHandler pageEditHandler;
    private final LocaleService localeService;
    private final MessageSource messageSource;

    @Autowired
    public ToolOrderController(SitePageEditHandler pageEditHandler, LocaleService localeService,
            MessageSource messageSource) {
        this.pageEditHandler = pageEditHandler;
        this.localeService = localeService;
        this.messageSource = messageSource;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        if (!pageEditHandler.canUpdateCurrentSite()) {
            model.addAttribute("message", message("access_error", locale));
            return "access";
        }

        Site site = pageEditHandler.requireCurrentSite();
        model.addAttribute("siteTitle", site.getTitle());
        model.addAttribute("pages", pageEditHandler.getPages());
        model.addAttribute("reorderAllowed", pageEditHandler.isReorderAllowed());
        model.addAttribute("siteOrdered", pageEditHandler.isSiteOrdered());
        return "index";
    }

    @PostMapping("/api/order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reorder(@RequestBody ReorderRequest request) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        try {
            if (request == null || request.getPageIds() == null) {
                return validationError(message("error_order_required", locale));
            }
            pageEditHandler.reorderPages(request.getPageIds());
            return ok(message("success_order_saved", locale), "pages", pageEditHandler.getPages());
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/details")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePageDetails(@PathVariable String pageId,
            @RequestBody PageDetailsRequest request) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        try {
            ToolOrderPage row = pageEditHandler.updatePageDetails(pageId, request.getTitle(),
                    request.getWebContentUrl());
            return ok(message("success_page_details_saved", locale), "row", row);
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/visibility")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> visibility(@PathVariable String pageId,
            @RequestBody VisibilityRequest request) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        try {
            if (request == null || request.getVisible() == null) {
                return validationError(message("error_visibility_required", locale));
            }
            boolean visible = request.getVisible();
            ToolOrderPage row = pageEditHandler.setPageVisible(pageId, visible);
            String message = visible ? message("success_visible", locale, row.getTitle())
                    : message("success_hidden", locale, row.getTitle());
            return ok(message, "row", row);
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/access")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> access(@PathVariable String pageId, @RequestBody AccessRequest request) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        try {
            if (request == null || request.getEnabled() == null) {
                return validationError(message("error_access_required", locale));
            }
            boolean enabled = request.getEnabled();
            ToolOrderPage row = pageEditHandler.setPageEnabled(pageId, enabled);
            String message = enabled ? message("success_enabled", locale, row.getTitle())
                    : message("success_disabled", locale, row.getTitle());
            return ok(message, "row", row);
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String pageId) {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        try {
            ToolOrderPage row = pageEditHandler.deletePage(pageId);
            return ok(message("success_removed", locale, row.getTitle()), "pageId", row.getId());
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/reset-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetOrder() {
        Locale locale = localeService.getLocaleForCurrentSiteAndUser();
        try {
            pageEditHandler.resetOrder();
            return ok(message("success_order_reset", locale), "pages", pageEditHandler.getPages());
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/done")
    public RedirectView done() {
        pageEditHandler.prepareDone();
        RedirectView redirectView = new RedirectView(pageEditHandler.getDoneUrl(), false);
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    private ResponseEntity<Map<String, Object>> ok(String message, String dataName, Object data) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.TRUE);
        body.put("message", message);
        body.put(dataName, data);
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<Map<String, Object>> error(RuntimeException e, Locale locale) {
        log.warn("Tool Order request failed", e);
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.FALSE);
        String message = e instanceof SecurityException ? message("access_error", locale) : message("status_error", locale);
        body.put("message", message);
        HttpStatus status = e instanceof SecurityException ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Map<String, Object>> validationError(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("success", Boolean.FALSE);
        body.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private String message(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    @Getter
    @Setter
    public static class ReorderRequest {
        private List<String> pageIds;
    }

    @Getter
    @Setter
    public static class PageDetailsRequest {
        private String title;
        private String webContentUrl;
    }

    @Getter
    @Setter
    public static class VisibilityRequest {
        private Boolean visible;
    }

    @Getter
    @Setter
    public static class AccessRequest {
        private Boolean enabled;
    }
}
