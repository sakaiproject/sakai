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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class ToolOrderController {

    private final SitePageEditHandler pageEditHandler;
    private final MessageSource messageSource;

    @Autowired
    public ToolOrderController(SitePageEditHandler pageEditHandler, MessageSource messageSource) {
        this.pageEditHandler = pageEditHandler;
        this.messageSource = messageSource;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model, Locale locale) {
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
    public ResponseEntity<Map<String, Object>> reorder(@RequestBody ReorderRequest request, Locale locale) {
        try {
            pageEditHandler.reorderPages(request.getPageIds());
            return ok(message("success_order_saved", locale), "pages", pageEditHandler.getPages());
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/title")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> rename(@PathVariable String pageId, @RequestBody RenameRequest request,
            Locale locale) {
        try {
            ToolOrderPage row = pageEditHandler.renamePage(pageId, request.getTitle(), request.getIframeSource());
            return ok(message("success_title_saved", locale), "row", row);
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/visibility")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> visibility(@PathVariable String pageId, @RequestBody VisibilityRequest request,
            Locale locale) {
        try {
            ToolOrderPage row = pageEditHandler.setPageVisible(pageId, request.isVisible());
            String message = request.isVisible() ? message("success_visible", locale, row.getTitle())
                    : message("success_hidden", locale, row.getTitle());
            return ok(message, "row", row);
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/pages/{pageId}/access")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> access(@PathVariable String pageId, @RequestBody AccessRequest request,
            Locale locale) {
        try {
            ToolOrderPage row = pageEditHandler.setPageEnabled(pageId, request.isEnabled());
            String message = request.isEnabled() ? message("success_enabled", locale, row.getTitle())
                    : message("success_disabled", locale, row.getTitle());
            return ok(message, "row", row);
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @DeleteMapping("/api/pages/{pageId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String pageId, Locale locale) {
        try {
            ToolOrderPage row = pageEditHandler.deletePage(pageId);
            return ok(message("success_removed", locale, row.getTitle()), "pageId", row.getId());
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/api/reset-order")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resetOrder(Locale locale) {
        try {
            pageEditHandler.resetOrder();
            return ok(message("success_order_reset", locale), "pages", pageEditHandler.getPages());
        } catch (RuntimeException e) {
            return error(e, locale);
        }
    }

    @PostMapping("/done")
    public String done() {
        pageEditHandler.prepareDone();
        return "redirect:" + pageEditHandler.getDoneUrl();
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

    private String message(String code, Locale locale, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    public static class ReorderRequest {
        private List<String> pageIds;

        public List<String> getPageIds() {
            return pageIds;
        }

        public void setPageIds(List<String> pageIds) {
            this.pageIds = pageIds;
        }
    }

    public static class RenameRequest {
        private String title;
        private String iframeSource;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getIframeSource() {
            return iframeSource;
        }

        public void setIframeSource(String iframeSource) {
            this.iframeSource = iframeSource;
        }
    }

    public static class VisibilityRequest {
        private boolean visible;

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }

    public static class AccessRequest {
        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
