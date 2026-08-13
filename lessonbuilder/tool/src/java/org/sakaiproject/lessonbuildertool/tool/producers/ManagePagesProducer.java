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
package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.service.PageIndexService;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageIndex;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageNode;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.helpers.PageVisibilityHelper;
import org.sakaiproject.lessonbuildertool.tool.beans.helpers.PageVisibilityHelper.VisibilityResult;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/** Renders the read-only page index and instructor recovery controls. */
@Slf4j
@Setter
public class ManagePagesProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    public static final String VIEW_ID = "ManagePages";

    private SimplePageBean simplePageBean;
    private SimplePageToolDao simplePageToolDao;
    private PageIndexService pageIndexService;
    private LessonsAccess lessonsAccess;
    private ToolManager toolManager;
    private MessageLocator messageLocator;
    private LocaleGetter localeGetter;
    private Map<String, String> imageToMimeMap;

    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    @Override
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        UIOutput.make(tofill, "html")
                .decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        GeneralViewParameters returnView = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
        UIOutput.make(tofill, "return-div");
        UIInternalLink.make(tofill, "return",
                messageLocator.getMessage("simplepage.return").replace("{}", simplePageBean.getPageTitle()),
                returnView);
        UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.page.index"));
        renderFlashMessages(tofill);

        boolean canEditPage = simplePageBean.getEditPrivs() == 0;
        String siteId = toolManager.getCurrentPlacement().getContext();
        PageIndex pageIndex = pageIndexService.getPageIndex(siteId);
        VisibilityResult visibility = PageVisibilityHelper.getVisiblePages(
                pageIndex.activePages(), canEditPage, simplePageBean);
        List<PageNode> activePages = visibility.pages();

        if (!canEditPage && visibility.hasPrerequisites()) {
            UIOutput.make(tofill, "onlyseen");
        }
        if (canEditPage && !pageIndex.sharedPageIds().isEmpty()) {
            UIOutput.make(tofill, "sharedpageexplanation");
        }
        if (!activePages.isEmpty()) {
            UIOutput.make(tofill, "in-lessons-section");
            renderActivePages(tofill, activePages, pageIndex.sharedPageIds(),
                    canEditPage, visibility.hasPrerequisites());
        }
        if (canEditPage && !pageIndex.removedPages().isEmpty()) {
            renderRemovedPages(tofill, pageIndex.removedPages());
        }
    }

    private void renderFlashMessages(UIContainer tofill) {
        List<String> errors = simplePageBean.errMessages();
        if (errors != null) {
            UIOutput.make(tofill, "error-div");
            for (String message : errors) {
                UIBranchContainer error = UIBranchContainer.make(tofill, "errors:");
                UIOutput.make(error, "error-message", message);
            }
        }

        List<String> successes = simplePageBean.successMessages();
        if (successes != null) {
            UIOutput.make(tofill, "success-div");
            for (String message : successes) {
                UIBranchContainer success = UIBranchContainer.make(tofill, "successes:");
                UIOutput.make(success, "success-message", message);
            }
        }
    }

    private void renderActivePages(UIContainer tofill, List<PageNode> pages,
            Set<Long> sharedPageIds, boolean canEditPage, boolean hasPrerequisites) {
        for (PageNode node : pages) {
            SimplePageLogEntry logEntry = simplePageBean.getLogEntry(node.pageItem().getId());
            String status = null;
            String iconClass = "invisible";
            if (logEntry != null && logEntry.isComplete()) {
                iconClass = "bi-check2";
                status = messageLocator.getMessage("simplepage.status.completed");
            } else if (logEntry != null && !logEntry.getDummy()) {
                iconClass = "bi-hourglass-split";
                status = messageLocator.getMessage("simplepage.status.inprogress");
            } else if (!canEditPage && hasPrerequisites) {
                continue;
            }

            UIBranchContainer row = UIBranchContainer.make(tofill, "active-page:");
            UIOutput.make(row, "status-image").decorate(new UIStyleDecorator(iconClass));

            int level = Math.min(node.level(), 5);
            GeneralViewParameters pageView = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
            pageView.setSendingPage(node.page().getPageId());
            pageView.setItemId(node.pageItem().getId());
            pageView.setPath("log");
            UIInternalLink.make(row, "link", pageView)
                    .decorate(new UIFreeAttributeDecorator("style", "padding-left: " + level + "em"));

            if (level > 0) {
                String levelText = messageLocator.getMessage("simplepage.status.level")
                        .replace("{}", Integer.toString(level));
                status = status == null ? levelText : levelText + " " + status;
            }
            if (status != null) {
                UIOutput.make(row, "link-note", status + " ");
            }
            UIOutput.make(row, "link-text", node.pageItem().getName());

            renderPageNote(row, node, sharedPageIds, canEditPage);
            renderPageItems(row, node.page().getPageId());
        }
    }

    private void renderPageNote(UIBranchContainer row, PageNode node,
            Set<Long> sharedPageIds, boolean canEditPage) {
        if (!canEditPage) {
            return;
        }
        String note = sharedPageIds.contains(node.page().getPageId())
                ? messageLocator.getMessage("simplepage.sharedpage") : null;
        String release = simplePageBean.getReleaseString(node.pageItem(), localeGetter.get());
        if (release != null) {
            note = note == null ? release : note + release;
        }
        if (note != null) {
            UIOutput.make(row, "shared", note);
        }

        if (ServerConfigurationService.getBoolean("lessonbuilder.accessibilitydebug", false)) {
            String siteId = simplePageBean.getCurrentSiteId();
            Long pageId = node.page().getPageId();
            Long itemId = node.pageItem().getId();
            if (lessonsAccess.isPageAccessible(pageId, siteId,
                    "c08d3ac9-c717-472a-ad91-7ce0b434f42f", null)) {
                UIOutput.make(row, "page1");
            }
            if (lessonsAccess.isPageAccessible(pageId, siteId,
                    "c08d3ac9-c717-472a-ad91-7ce0b434f42f", simplePageBean)) {
                UIOutput.make(row, "page2");
            }
            if (lessonsAccess.isItemAccessible(itemId, siteId,
                    "c08d3ac9-c717-472a-ad91-7ce0b434f42f", null)) {
                UIOutput.make(row, "item1");
            }
            if (lessonsAccess.isItemAccessible(itemId, siteId,
                    "c08d3ac9-c717-472a-ad91-7ce0b434f42f", simplePageBean)) {
                UIOutput.make(row, "item2");
            }
        }
    }

    private void renderPageItems(UIBranchContainer row, Long pageId) {
        if (!ServerConfigurationService.getBoolean("lessonbuilder.enable-show-items", true)) {
            return;
        }

        List<SimplePageItem> pageItems = simplePageToolDao.findItemsOnPage(pageId);
        if (pageItems == null || pageItems.isEmpty()) {
            return;
        }
        Set<String> myGroups = simplePageBean.getMyGroups();
        List<SimplePageItem> visibleItems = pageItems.stream()
                .filter(item -> isItemVisible(item, myGroups))
                .toList();
        if (visibleItems.isEmpty()) {
            return;
        }

        UIOutput.make(row, "item-list-toggle");
        UIOutput.make(row, "item-list-container");
        UIOutput.make(row, "item-list");

        for (SimplePageItem item : visibleItems) {
            UIBranchContainer itemRow = UIBranchContainer.make(row, "item:");
            UIOutput.make(itemRow, item.isRequired() ? "required-image" : "not-required-image");
            UIOutput.make(itemRow, "item-icon").decorate(getImageSourceDecorator(item));
            UIOutput.make(itemRow, "name", getItemName(item));
        }
    }

    private boolean isItemVisible(SimplePageItem item, Set<String> myGroups) {
        Collection<String> itemGroups;
        try {
            itemGroups = simplePageBean.getItemGroups(item, null, false);
        } catch (IdUnusedException e) {
            return false;
        }
        if (itemGroups == null) {
            return true;
        }
        return itemGroups.stream().anyMatch(myGroups::contains);
    }

    private String getItemName(SimplePageItem item) {
        if (item.getType() == SimplePageItem.TEXT) {
            return messageLocator.getMessage("simplepage.chooser.textitemplaceholder");
        }
        if (item.getType() == SimplePageItem.BREAK) {
            return messageLocator.getMessage("section".equals(item.getFormat())
                    ? "simplepage.break-here" : "simplepage.break-column-here");
        }
        return item.getName();
    }

    private void renderRemovedPages(UIContainer tofill, List<SimplePage> removedPages) {
        UIOutput.make(tofill, "removed-pages-section");
        UIOutput.make(tofill, "removed-pages-description",
                messageLocator.getMessage("simplepage.chooser.unused.description"));

        UIForm form = UIForm.make(tofill, "removed-pages-form");
        UIOutput.make(form, "chooseall");
        Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
        if (sessionToken != null) {
            UIInput.make(form, "csrf", "#{simplePageBean.csrfToken}", sessionToken.toString());
        }
        UIInput.make(form, "restore-page-id", "#{simplePageBean.selectedEntity}");

        String[] values = removedPages.stream()
                .map(page -> Long.toString(page.getPageId()))
                .toArray(String[]::new);
        String[] initialValues = removedPages.stream().map(page -> "").toArray(String[]::new);
        UISelect selection = UISelect.makeMultiple(form, "page-selection", values,
                "#{simplePageBean.selectedEntities}", initialValues);

        for (int index = 0; index < removedPages.size(); index++) {
            SimplePage page = removedPages.get(index);
            UIBranchContainer row = UIBranchContainer.make(form, "removed-page:");
            UISelectChoice choice = UISelectChoice.make(row, "select-for-deletion", selection.getFullID(), index);
            choice.decorate(new UIFreeAttributeDecorator("id", choice.getFullID()));
            choice.decorate(new UIFreeAttributeDecorator("data-page-title", page.getTitle()));
            UIOutput.make(row, "select-for-deletion-label",
                    messageLocator.getMessage("simplepage.select-for-deletion-page").replace("{}", page.getTitle()))
                    .decorate(new UIFreeAttributeDecorator("for", choice.getFullID()));

            GeneralViewParameters preview = new GeneralViewParameters(PreviewProducer.VIEW_ID);
            preview.setSendingPage(page.getPageId());
            UIInternalLink.make(row, "link", preview)
                    .decorate(new UIFreeAttributeDecorator("target", "_blank"))
                    .decorate(new UIFreeAttributeDecorator("rel", "noopener"));
            UIOutput.make(row, "link-note", messageLocator.getMessage("simplepage.opens-in-new") + ". ");
            UIOutput.make(row, "link-text", page.getTitle());
            UICommand.make(row, "restore-page", messageLocator.getMessage("simplepage.restore-page"),
                    "#{simplePageBean.restorePage}")
                    .decorate(new UIFreeAttributeDecorator("data-page-id", Long.toString(page.getPageId())));
        }

        UIOutput.make(form, "delete-button", messageLocator.getMessage("simplepage.delete-selected"));
        UIOutput.make(form, "delete-dialog-close")
                .decorate(new UIFreeAttributeDecorator("aria-label", messageLocator.getMessage("simplepage.cancel")));
        UICommand.make(form, "delete-confirm-submit", messageLocator.getMessage("simplepage.delete-selected"),
                "#{simplePageBean.deletePages}");
    }

    private UIStyleDecorator getImageSourceDecorator(SimplePageItem item) {
        return switch (item.getType()) {
            case SimplePageItem.FORUM -> new UIStyleDecorator("si si-sakai-forums");
            case SimplePageItem.ASSIGNMENT -> new UIStyleDecorator("si si-sakai-assignment-grades");
            case SimplePageItem.ASSESSMENT -> new UIStyleDecorator("si si-sakai-samigo");
            case SimplePageItem.SCORM -> new UIStyleDecorator("si si-sakai-scorm-tool");
            case SimplePageItem.QUESTION -> new UIStyleDecorator("bi-question-circle");
            case SimplePageItem.COMMENTS -> new UIStyleDecorator("bi-chat-dots");
            case SimplePageItem.BLTI -> new UIStyleDecorator("bi-box-arrow-up-right");
            case SimplePageItem.PAGE -> new UIStyleDecorator("bi-folder");
            case SimplePageItem.BREAK -> new UIStyleDecorator("bi-textarea-t");
            case SimplePageItem.URL -> new UIStyleDecorator("bi-link-45deg");
            case SimplePageItem.STUDENT_CONTENT -> new UIStyleDecorator("bi-person-square");
            case SimplePageItem.PEEREVAL -> new UIStyleDecorator("bi-people");
            case SimplePageItem.CHECKLIST -> new UIStyleDecorator("bi-list-check");
            case SimplePageItem.FORUM_SUMMARY -> new UIStyleDecorator("si si-sakai-forums");
            case SimplePageItem.ANNOUNCEMENTS -> new UIStyleDecorator("si si-sakai-announcements");
            case SimplePageItem.TWITTER -> new UIStyleDecorator("bi-twitter");
            case SimplePageItem.CALENDAR -> new UIStyleDecorator("si si-sakai-schedule");
            case SimplePageItem.RESOURCE_FOLDER -> new UIStyleDecorator("bi-folder2-open");
            case SimplePageItem.RESOURCE, SimplePageItem.MULTIMEDIA -> getMimeTypeDecorator(item);
            case SimplePageItem.TEXT -> new UIStyleDecorator(imageToMimeMap.getOrDefault("text/html", "bi-file-text"));
            default -> new UIStyleDecorator("bi-type");
        };
    }

    private UIStyleDecorator getMimeTypeDecorator(SimplePageItem item) {
        return new UIStyleDecorator(imageToMimeMap.getOrDefault(simplePageBean.getContentType(item), "bi-file-text"));
    }

    @Override
    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    @Override
    public List reportNavigationCases() {
        List<NavigationCase> cases = new ArrayList<>();
        cases.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        cases.add(new NavigationCase("manage-pages", new SimpleViewParameters(ManagePagesProducer.VIEW_ID)));
        return cases;
    }
}
