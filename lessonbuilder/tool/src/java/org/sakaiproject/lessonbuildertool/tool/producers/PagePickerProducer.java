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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.service.PageIndexService;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageIndex;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageNode;
import org.sakaiproject.lessonbuildertool.service.PlacementPageService;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.helpers.PageVisibilityHelper;
import org.sakaiproject.lessonbuildertool.tool.beans.helpers.PageVisibilityHelper.VisibilityResult;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
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
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/** Creates the page chooser used when attaching an existing Lessons page. */
@Slf4j
@Setter
public class PagePickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    public static final String VIEW_ID = "PagePicker";

    private SimplePageBean simplePageBean;
    private SimplePageToolDao simplePageToolDao;
    private PageIndexService pageIndexService;
    private PlacementPageService placementPageService;
    private LessonsAccess lessonsAccess;
    private ToolManager toolManager;
    private MessageLocator messageLocator;
    private LocaleGetter localeGetter;
    private SiteService siteService;
    private SecurityService securityService;

    @Override
    public String getViewID() {
        return VIEW_ID;
    }

    private List<Site> findSitesWithLessons() {
        List<Site> sites = siteService.getSites(SiteService.SelectionType.UPDATE, null, null, null,
                        SiteService.SortType.TITLE_ASC, null).stream()
                .filter(site -> !site.getTools(LessonBuilderConstants.TOOL_ID).isEmpty())
                .filter(site -> canUpdateLessons(site.getId()))
                .collect(Collectors.toList());
        Collections.reverse(sites);
        return sites;
    }

    @Override
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
        GeneralViewParameters params = (GeneralViewParameters) viewparams;
        if (params.getSendingPage() != -1) {
            try {
                simplePageBean.updatePageObject(params.getSendingPage());
            } catch (Exception e) {
                log.warn("Unable to open Lessons page picker for page {}", params.getSendingPage(), e);
                return;
            }
        }

        UIOutput.make(tofill, "html")
                .decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
                .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

        String source = params.getSource();
        String returnView = params.getReturnView();
        String siteId = toolManager.getCurrentPlacement().getContext();
        ToolSession toolSession = SessionManager.getCurrentToolSession();
        if (toolSession.getAttribute("lessonbuilder.selectedsite") != null) {
            siteId = (String) toolSession.getAttribute("lessonbuilder.selectedsite");
            toolSession.removeAttribute("lessonbuilder.selectedsite");
        }

        if ("anotherPage".equals(source) || toolSession.getAttribute("lessonbuilder.loadFromSite") != null) {
            toolSession.removeAttribute("lessonbuilder.loadFromSite");
            if (StringUtils.isBlank(returnView)) {
                returnView = "reorder";
            }
            renderSitePicker(tofill, siteId);
        }

        UIOutput.make(tofill, "title", StringUtils.equals(returnView, "reorder")
                ? messageLocator.getMessage("simplepage.page.add.from.other")
                : messageLocator.getMessage("simplepage.page.chooser"));

        Long itemId = params.getItemId();
        simplePageBean.setItemId(itemId);
        SimplePage currentPage = simplePageBean.getCurrentPage();
        if (itemId != null && itemId != -1) {
            SimplePageItem currentItem = simplePageToolDao.findItem(itemId);
            if (currentItem == null || currentItem.getPageId() != currentPage.getPageId()) {
                return;
            }
        }

        boolean canEditPage = canUpdateLessons(siteId);
        if (!canEditPage) {
            log.warn("User cannot update Lessons in selected site {}", siteId);
            return;
        }
        placementPageService.ensurePlacementPagesExist(siteId);

        PageIndex pageIndex = pageIndexService.getPageIndex(siteId);
        VisibilityResult visibility = PageVisibilityHelper.getVisiblePages(
                pageIndex.activePages(), canEditPage, simplePageBean);
        List<PageNode> activePages = visibility.pages();
        List<SimplePage> removedPages = canEditPage ? pageIndex.removedPages() : Collections.emptyList();

        if (!canEditPage && visibility.hasPrerequisites()) {
            UIOutput.make(tofill, "onlyseen");
        }
        if (canEditPage && !pageIndex.sharedPageIds().isEmpty()) {
            UIOutput.make(tofill, "sharedpageexplanation");
        }

        UIForm form = UIForm.make(tofill, "page-picker");
        Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
        if (sessionToken != null) {
            UIInput.make(form, "csrf", "#{simplePageBean.csrfToken}", sessionToken.toString());
        }

        List<String> values = activePages.stream()
                .map(node -> Long.toString(node.page().getPageId()))
                .collect(Collectors.toCollection(ArrayList::new));
        values.addAll(removedPages.stream()
                .map(page -> Long.toString(page.getPageId()))
                .collect(Collectors.toList()));
        UISelect select = UISelect.make(form, "page-span", values.toArray(new String[0]),
                "#{simplePageBean.selectedEntity}", null);

        // In newTopLevel mode, disable top-level pages (including when they also appear as shared subpages).
        Set<Long> topLevelPageIds = activePages.stream()
                .filter(PageNode::topLevel)
                .map(node -> node.page().getPageId())
                .collect(Collectors.toUnmodifiableSet());

        int index = 0;
        for (PageNode node : activePages) {
            renderPageChoice(form, "active-page:", select, index++, node.page(), node.pageItem().getName(),
                    node.level(), node.topLevel(), node.pageItem().getId(), pageIndex.sharedPageIds(), siteId,
                    params.newTopLevel && topLevelPageIds.contains(node.page().getPageId()));
        }

        if (!removedPages.isEmpty()) {
            UIBranchContainer section = UIBranchContainer.make(form, "removed-section:");
            UIOutput.make(section, "heading", messageLocator.getMessage("simplepage.chooser.unused"));
            for (SimplePage page : removedPages) {
                renderPageChoice(section, "removed-page:", select, index++, page, page.getTitle(), 0, false, null,
                        pageIndex.sharedPageIds(), siteId,
                        params.newTopLevel && topLevelPageIds.contains(page.getPageId()));
            }
        }

        renderActions(form, params, itemId, returnView);
    }

    private void renderSitePicker(UIContainer tofill, String siteId) {
        UIOutput.make(tofill, "site-dropdown-title", messageLocator.getMessage("simplepage.page.add.choose.site"));
        UIForm siteForm = UIForm.make(tofill, "site-picker");
        Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
        if (sessionToken != null) {
            UIInput.make(siteForm, "csrf2", "simplePageBean.csrfToken", sessionToken.toString());
        }
        UICommand.make(siteForm, "submitSite", messageLocator.getMessage("simplepage.chooser.select.site"),
                "#{simplePageBean.selectSite}");
        List<Site> sites = findSitesWithLessons();
        List<String> siteIds = sites.stream().map(Site::getId).collect(Collectors.toList());
        List<String> siteNames = sites.stream().map(Site::getTitle).collect(Collectors.toList());
        UISelect.make(siteForm, "pick-site", siteIds.toArray(new String[0]), siteNames.toArray(new String[0]),
                "#{simplePageBean.selectedSite}", siteId);
    }

    private boolean canUpdateLessons(String siteId) {
        return securityService.unlock(
                SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteService.siteReference(siteId));
    }

    private void renderPageChoice(UIContainer rowContainer, String branchId, UISelect select, int index,
            SimplePage page, String title,
            int level, boolean topLevel, Long itemId, Set<Long> sharedPageIds, String siteId,
            boolean disableSelection) {
        UIBranchContainer row = UIBranchContainer.make(rowContainer, branchId);
        if (topLevel) {
            row.decorate(new UIFreeAttributeDecorator("class", "top-level-page"));
        }
        UISelectChoice radio = UISelectChoice.make(row, "select", select.getFullID(), index);
        radio.decorate(new UIFreeAttributeDecorator("title",
                title + " " + messageLocator.getMessage("simplepage.select")));
        if (disableSelection) {
            radio.decorate(new UIFreeAttributeDecorator("disabled", "disabled"));
        }

        GeneralViewParameters preview = new GeneralViewParameters(PreviewProducer.VIEW_ID);
        preview.setSendingPage(page.getPageId());
        preview.setSiteId(siteId);
        UIInternalLink.make(row, "link", preview)
                .decorate(new UIFreeAttributeDecorator("style", "padding-left: " + Math.min(level, 5) + "em"))
                .decorate(new UIFreeAttributeDecorator("target", "_blank"))
                .decorate(new UIFreeAttributeDecorator("rel", "noopener"));
        String linkNote = messageLocator.getMessage("simplepage.opens-in-new") + ". ";
        if (level > 0) {
            linkNote = messageLocator.getMessage("simplepage.status.level")
                    .replace("{}", Integer.toString(Math.min(level, 5))) + " " + linkNote;
        }
        UIOutput.make(row, "link-note", linkNote);
        UIOutput.make(row, "link-text", title);

        if (itemId == null) {
            return;
        }

        String note = sharedPageIds.contains(page.getPageId())
                ? messageLocator.getMessage("simplepage.sharedpage") : null;
        String release = simplePageBean.getReleaseString(simplePageBean.findItem(itemId), localeGetter.get());
        if (release != null) {
            note = note == null ? release : note + release;
        }
        if (note != null) {
            UIOutput.make(row, "shared", note);
        }

        if (ServerConfigurationService.getBoolean("lessonbuilder.accessibilitydebug", false)) {
            if (lessonsAccess.isPageAccessible(page.getPageId(), siteId,
                    "c08d3ac9-c717-472a-ad91-7ce0b434f42f", null)) {
                UIOutput.make(row, "page1");
            }
            if (lessonsAccess.isPageAccessible(page.getPageId(), siteId,
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

    private void renderActions(UIForm form, GeneralViewParameters params, Long itemId, String returnView) {
        UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
        if (Long.valueOf(-1).equals(itemId) && !params.newTopLevel) {
            UIOutput.make(form, "hr");
            if (!StringUtils.equals(returnView, "reorder")) {
                UIOutput.make(form, "options");
            }
            UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
            UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);
        }

        if (StringUtils.equals(returnView, "reorder")) {
            UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"),
                    "#{simplePageBean.selectPage}");
        } else if (params.newTopLevel) {
            UIInput.make(form, "addBefore", "#{simplePageBean.addBefore}", params.getAddBefore());
            UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"),
                    "#{simplePageBean.addOldPage}");
        } else {
            UIInput.make(form, "addBefore", "#{simplePageBean.addBefore}", params.getAddBefore());
            UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"),
                    "#{simplePageBean.createSubpage}");
        }
        UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
    }

    @Override
    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    @Override
    public List reportNavigationCases() {
        List<NavigationCase> cases = new ArrayList<>();
        cases.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        cases.add(new NavigationCase("failure", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        cases.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        cases.add(new NavigationCase("selectpage", new GeneralViewParameters(ReorderProducer.VIEW_ID)));
        GeneralViewParameters selectSite = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
        selectSite.setSource("anotherPage");
        cases.add(new NavigationCase("selectsite", selectSite));
        return cases;
    }
}
