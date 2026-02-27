/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;                                                                                    
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIInput;
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

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;

import static org.sakaiproject.site.api.SiteService.PROP_PARENT_ID;


/**
 * Creates a window for the user to choose which page to add
 * 
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
@Slf4j
@Setter
public class PagePickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

    public static final String VIEW_ID = "PagePicker";

    private SimplePageBean simplePageBean;
    private SimplePageToolDao simplePageToolDao;
    private LessonsAccess lessonsAccess;
    private ToolManager toolManager;
    private MessageLocator messageLocator;
    private LocaleGetter localeGetter;
    private Map<String,String> imageToMimeMap;
    private SiteService siteService;
    private SessionManager sessionManager;

    private boolean somePagesHavePrerequisites = false;
    private long currentPageId = -1;

    public class PageEntry {

        Long pageId;
        Long itemId;
        String title;
        int level;
        boolean toplevel;
        boolean hidden;
        Date releaseDate;
        String toolId;
    }

    public String getViewID() {
        return VIEW_ID;
    }

    // this is complicated. We're trying to produce a list of all pages in a reasonable order.
    // we start with a list of top level pages and traverse it recursively. But if there's a reference
    // Data:
    //   entries - the list we're building to display
    //   pageMap - a map that starts out having all pages in the site, we remove entries as we show them
    //      that lets us find at the end anything that hasn't been shown

    public void findAllPages(SimplePageItem pageItem, List<PageEntry>entries, Map<Long,SimplePage> pageMap, Set<Long>topLevelPages, Set<Long>sharedPages, int level, boolean toplevel, boolean canEditPage) {

        // log.info("in findallpages " + pageItem.getName() + " " + toplevel);
        Long pageId = Long.valueOf(pageItem.getSakaiId());

        if (pageId == 0L) {
            return;
        }

        try {
            if (pageItem.isPrerequisite() || simplePageBean.getItemGroups(pageItem, null, false) != null) {
                somePagesHavePrerequisites = true;
            }
        } catch (IdUnusedException exe) {
            // underlying item missing. should be impossible for a page
            return;
        }

        SimplePage page = simplePageToolDao.getPage(pageId);
        // implement hidden.
        if (!canEditPage) {
            if (page.isHidden()) {
                return;
            }
            if (page.getReleaseDate() != null && page.getReleaseDate().after(new Date())) {
                return;
            }
            if (toplevel) {
                if (page.getToolId() != null) {
                    // getCurrentSite is cached, so it's reasonable to get it at this level
                    Site site = simplePageBean.getCurrentSite();
                    SitePage sitePage = site.getPage(page.getToolId());
                    List<ToolConfiguration> tools = sitePage.getTools();
                    // If all the tools on a page require site.upd then only users with site.upd will see
                    // the page in the site nav of Charon... not sure about the other Sakai portals floating
                    // about
                    boolean visible = false;
                    for (ToolConfiguration placement: tools) {
                        Properties roleConfig = placement.getPlacementConfig();
                        String roleList = roleConfig.getProperty("functions.require");
                        String visibility = roleConfig.getProperty(ToolManager.PORTAL_VISIBLE);
                        // log.info("roles " + roleList + " visi " + visibility);
                        // doesn't require site update, so visible
                        if ((visibility == null || !visibility.equals("false")) &&
                                (roleList == null || roleList.indexOf(SiteService.SECURE_UPDATE_SITE) < 0)) {
                            // only need one tool on the page to be visible
                            visible = true;
                            break;
                        }
                    }

                    // not visible, ignore it
                    if (!visible) {
                        return;
                    }
                }
            }
        }

        PageEntry entry = new PageEntry();
        entry.pageId = pageId;
        entry.itemId = pageItem.getId();
        entry.title = pageItem.getName();
        entry.level = level;
        entry.toplevel = toplevel;
        entry.hidden = page.isHidden();
        entry.releaseDate = page.getReleaseDate();
        entry.toolId = page.getToolId();

        // add entry
        entries.add(entry);

        // if page has already been done, don't do the subpages. Otherwise we can
        // get into infinite loops

        // already done if removed from map.
        // however for top level pages, expand them for their primary entry,
        // i.e. when toplevel is set.
        if (pageMap.get(pageId) == null || (topLevelPages.contains(pageId) && !toplevel)) {
            sharedPages.add(pageId);
            return;
        }

        // say done
        pageMap.remove(pageId);

        // now recursively do subpages

        List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
        List<SimplePageItem> nexts = new ArrayList<SimplePageItem>();

        // subpages done in place
        for (SimplePageItem item: items) {
            if (item.getType() == SimplePageItem.PAGE) {
                Long pageNum = Long.valueOf(item.getSakaiId());

                // ignore top-level pages.

                // show next pages (including top level pages) after all the subpages
                // so stick it on the delayed display list.
                if (item.getNextPage()) {
                    nexts.add(item);
                } else  {
                    // log.info("call for subpage " + item.getName() + " " + false);
                    findAllPages(item, entries, pageMap, topLevelPages, sharedPages, level +1, false, canEditPage);
                }
            }
        }

        // nexts done afterwards
        for (SimplePageItem item: nexts) {
            if (item.getType() == SimplePageItem.PAGE) {
                findAllPages(item, entries, pageMap, topLevelPages, sharedPages, level, false, canEditPage);
            }
        }
    }

    public boolean hasSubPages(Long pageId) {

        // now recursively do subpages
        List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);

        // subpages done in place
        for (SimplePageItem item: items) {
            if (item.getType() == SimplePageItem.PAGE) {
                return true;
            }
        }

        return false;
    }

    public List findAllSites(int outputflag){   //get all sites that the current user can Update. Just names, just IDs, or whole sites.
        List<Site> sites = siteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.UPDATE, null, null, null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
        if (outputflag==1){ //return just site ID strings
            List<String> siteids = new ArrayList<String>();
            for (Site site: sites){
                if(site.getTools(LessonBuilderConstants.TOOL_ID).size() > 0){    //filter...we only want ones with a Lessons instance
                    siteids.add(site.getId());
                }
            }
            Collections.reverse(siteids);   //it naturally lists them oldest to newest, but we want newest first
            return siteids;
        } else if(outputflag==2){   //return site names only
            List<String> sitenames = new ArrayList<String>();
            for (Site site: sites){
                if(site.getTools(LessonBuilderConstants.TOOL_ID).size() > 0){    //filter...we only want ones with a Lessons instance
                    sitenames.add(site.getTitle());
                }
            }
            Collections.reverse(sitenames); //it naturally lists them oldest to newest, but we want newest first
            return sitenames;
        }
        return sites;   //return the entire site data; note that this one is not filtered or reversed, and it's List<Site> instead of List<String> like the others.
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

        if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
            // will fail if page not in this site
            // security then depends upon making sure that we only deal with this page
            try {
                simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
            } catch (Exception e) {
                log.info("PagePicker permission exception " + e);
                return;
            }
        }
        String source = ((GeneralViewParameters) viewparams).getSource();
        // summaryPage is the "index of pages". It has status icons and links, but isn't a chooser
        // otherwise we have the chooser page for the "add subpage" command
        boolean summaryPage = "summary".equals(source);
        String returnView = ((GeneralViewParameters) viewparams).getReturnView();

        UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
            .decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));
        String siteId = toolManager.getCurrentPlacement().getContext();
        ToolSession toolSession = sessionManager.getCurrentToolSession();
        if (toolSession.getAttribute("lessonbuilder.selectedsite") != null){    //if we selected another site earlier, use that ID
            siteId = (String) toolSession.getAttribute("lessonbuilder.selectedsite");
            toolSession.setAttribute("lessonbuilder.selectedsite", null);
        }
        Object sessionToken = sessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
        if ("anotherPage".equals(source) || toolSession.getAttribute("lessonbuilder.loadFromSite")!=null){   //this is all the stuff for picking a site to select a page from; we want it for Add Items From Another Page [anotherPage] only.
            toolSession.setAttribute("lessonbuilder.loadFromSite", null);   //this tag needs to be cleared after the use above so it's accurate
            if (StringUtils.isBlank(returnView)){   //as part of this case, the ReturnView was occasionally seen to be null, so we'll fill it here.
                returnView = "reorder";
            }
            UIOutput.make(tofill, "site-dropdown-title", messageLocator.getMessage("simplepage.page.add.choose.site")); //caption for the dropdown: "Select A Site"
            UIForm siteform = UIForm.make(tofill, "site-picker");   //form to receive input
            if (sessionToken != null) {
                UIInput.make(siteform, "csrf2", "simplePageBean.csrfToken", sessionToken.toString());
            }
            UICommand.make(siteform, "submitSite", messageLocator.getMessage("simplepage.chooser.select.site"), "#{simplePageBean.selectSite}");    //Submit button for the form
            List<String> siteids = findAllSites(1);
            List<String> sitenames = findAllSites(2);
            UISelect.make(siteform, "pick-site", siteids.toArray(new String[1]), sitenames.toArray(new String[1]), "#{simplePageBean.selectedSite}", siteId);   //create the dropdown of sites
        }
        boolean canEditPage = (simplePageBean.getEditPrivs() == 0);
        if (summaryPage) {
            GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
            // path defaults to null, which is next
            UIOutput.make(tofill, "return-div");

            String currentToolTitle = simplePageBean.getPageTitle();
            String returnText = messageLocator.getMessage("simplepage.return").replace("{}", currentToolTitle);
            UIInternalLink.make(tofill, "return", returnText, view);

            UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.page.index"));
        } else if (StringUtils.equals(returnView, "reorder")){
            UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.page.add.from.other"));
        } else {
            UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.page.chooser"));
        }

        if (!StringUtils.equals(returnView, "reorder")) {
            UIOutput.make(tofill, "hide-show-container");
        }

        // Explain which pages may be deleted
        if (summaryPage && canEditPage) {
            UIOutput.make(tofill, "deleteAlert");
        }

        // this looks at pages in the site, which should be safe
        // but need to make sure the item we update is legal

        Long itemId = ((GeneralViewParameters) viewparams).getItemId();

        simplePageBean.setItemId(itemId);

        SimplePage page = simplePageBean.getCurrentPage();
        currentPageId = page.getPageId();

        if (itemId != null && itemId != -1) {
            SimplePageItem currentItem = simplePageToolDao.findItem(itemId);
            if (currentItem == null) {
                return;
            }
            // trying to hack on item not on this page
            if (currentItem.getPageId() != page.getPageId()) {
                return;
            }
        }

        final String usableSiteId = siteId;  //"Final" version, for benefit of lambda expression later

        final List<ToolConfiguration> siteTools = simplePageToolDao.getSiteTools(siteId);

        final Set<String> existingSitePageToolIds
            = simplePageToolDao.getSitePages(siteId).stream().map(SimplePage::getToolId).collect(Collectors.toSet());

        if (siteTools.size() > existingSitePageToolIds.size()) {
            // Create a top level page for each tool that doesn't yet have one
            siteTools.forEach(tc -> {

                if (!existingSitePageToolIds.contains(tc.getPageId())) {
                    // No page has been created for this tool placement yet. Create one.
                    SimplePage sp = simplePageToolDao.makePage(tc.getPageId(), usableSiteId, tc.getTitle(), null, null);
                    if (simplePageToolDao.saveItem(sp, new ArrayList<String>(), "ignored", false)) {
                        SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(sp.getPageId()), sp.getTitle());
                        if (!simplePageToolDao.saveItem(item, new ArrayList<String>(), "ignored", false)) {
                            log.error("Failed to save simple page item for tool {}", tc.getPageId());
                        }
                    } else {
                        log.error("Failed to save simple page for tool {}", tc.getPageId());
                    }
                }
            });
        }

		final List<SimplePageItem> sitePages = simplePageToolDao.getOrderedTopLevelPageItems(siteId);

        final Set<Long> topLevelPages
            = sitePages.stream().map(sp -> Long.valueOf(sp.getSakaiId())).collect(Collectors.toSet());

        // build map of all pages, so we can see if any are left over
        final Map<Long, SimplePage> pageMap
            = simplePageToolDao.getSitePages(siteId)
                .stream().collect(Collectors.toMap(SimplePage::getPageId, Function.identity()));

        // list we're going to display
        List<PageEntry> entries = new ArrayList<>();
        Set<Long> sharedPages = new HashSet<>();

        // this adds everything you can find from top level pages to entries. But make sure user can see
        // the tool
        sitePages.forEach(spi -> findAllPages(spi, entries, pageMap, topLevelPages, sharedPages, 0, true, canEditPage));

        // warn students if we aren't showing all the pages
        if (!canEditPage && somePagesHavePrerequisites) {
            UIOutput.make(tofill, "onlyseen");
        }

        // now add everything we didn't find that way
        if (canEditPage && pageMap.size() > 0) {
            // marker
            PageEntry marker = new PageEntry();
            marker.level = -1;
            entries.add(marker);
            for (SimplePage p: pageMap.values()) {
                if (!simplePageBean.isStudentPage(p)) {
                    PageEntry entry = new PageEntry();
                    entry.pageId = p.getPageId();
                    entry.itemId = null;
                    entry.title = p.getTitle();
                    entry.level = 0;

                    // TopLevel determines if we can select the page.
                    // Since this means that the page is detached, it isn't
                    // a conflict to be able to select it.
                    entry.toplevel = false;

                    entries.add(entry);
                }
            }
        }

        if (canEditPage && sharedPages.size() > 0) {
            UIOutput.make(tofill, "sharedpageexplanation");
        }

        UIForm form = UIForm.make(tofill, "page-picker");
        if (sessionToken != null) {
            UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
        }

        List<String> values = new ArrayList<>();
        List<String> initValues = new ArrayList<>();
        for (PageEntry entry: entries) {
            if (entry.level >= 0) {
                values.add(entry.pageId.toString());
                initValues.add("");
            }
        }

        UISelect select = null;
        if (summaryPage) {
            select = UISelect.makeMultiple(form, "page-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntities}" , initValues.toArray(new String[1]));
        } else {
            select = UISelect.make(form, "page-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntity}", null);
        }

        int index = 0;
        boolean showDeleteButton = false;

        for (PageEntry entry: entries) {
            UIBranchContainer row = UIBranchContainer.make(form, "page:");
            if (entry.toplevel) {
                row.decorate(new UIFreeAttributeDecorator("style", "list-style-type:none; margin-top:0.5em"));
            }

            if (entry.level < 0) {
                UIOutput.make(row, "heading", messageLocator.getMessage("simplepage.chooser.unused"));
                if (summaryPage) {
                    UIOutput.make(row, "chooseall");
                }
            } else if (summaryPage && entry.itemId != null) {
                // if no itemid, it's unused. Only canedit people will see it
                int level = entry.level;
                if (level > 5) {
                    level = 5;
                }
                String imagePath = "/lessonbuilder-tool/images/";

                SimplePageLogEntry logEntry = simplePageBean.getLogEntry(entry.itemId);
                String note = null;
                if (logEntry != null && logEntry.isComplete()) {
                    imagePath += "checkmark.png";
                    note = messageLocator.getMessage("simplepage.status.completed");
                } else if (logEntry != null && !logEntry.getDummy()) {
                    imagePath += "hourglass.png";
                    note = messageLocator.getMessage("simplepage.status.inprogress");
                } else if (!canEditPage && somePagesHavePrerequisites) {
                    // it's too complex to compute prerequisites for all pages, and
                    // I'm concerned that faculty may not be careful in setting them
                    // for pages that would normally not be accessible. So if there are
                    // any prerequisites in the site, only show pages that are
                    // in progress or done.
                    continue;
                } else {
                    imagePath += "not-required.png";
                }
                UIOutput.make(row, "status-image").decorate(new UIFreeAttributeDecorator("src", imagePath));
                GeneralViewParameters p = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
                p.setSendingPage(entry.pageId);
                p.setItemId(entry.itemId);
                // reset the path to the saved one
                p.setPath("log");
                UIInternalLink.make(row, "link", p).
                        decorate(new UIFreeAttributeDecorator("style", "padding-left: " + (2*level) + "em"));
                String levelstr = null;
                if (level > 0) {
                    levelstr = messageLocator.getMessage("simplepage.status.level").replace("{}", Integer.toString(level));
                }
                if (levelstr != null) {
                    if (note != null) {
                        note = levelstr + " " + note;
                    } else {
                        note = levelstr;
                    }
                }
                if (note != null) {
                    UIOutput.make(row, "link-note", note + " ");
                }
                UIOutput.make(row, "link-text", entry.title);

                if (ServerConfigurationService.getBoolean("lessonbuilder.enable-show-items", true)) {
                    UIOutput.make(row, "item-list-toggle");
                    UIOutput.make(row, "itemListContainer").decorate(new UIFreeAttributeDecorator("style", "margin-left: " + (3*level) + "em"));
                    UIOutput.make(row, "itemList");

                    Set<String> myGroups = simplePageBean.getMyGroups();

                    for (SimplePageItem pageItem : simplePageToolDao.findItemsOnPage(entry.pageId)) {
                        // if item is group controlled, skip if user isn't in one of the groups
                        Collection<String>itemGroups = null;
                        try {
                            itemGroups = simplePageBean.getItemGroups(pageItem, null, false);
                        } catch (IdUnusedException e) {
                            // underlying assignment, etc, doesn't exist. skip the item
                            continue;
                        }
                        if (itemGroups != null) {
                            boolean groupsOk = false;
                            for (String group: itemGroups) {
                                if (myGroups.contains(group)) {
                                    groupsOk = true;
                                    break;
                                }
                            }
                            if (!groupsOk) {
                                continue;
                            }
                        }

                        UIBranchContainer itemListItem = UIBranchContainer.make(row, "item:");

                        if (pageItem.isRequired()) {
                            UIOutput.make(itemListItem, "required-image");
                        } else {
                            UIOutput.make(itemListItem, "not-required-image");
                        }

                        UIOutput.make(itemListItem,"item-icon").decorate(getImageSourceDecorator(pageItem));

                        if (SimplePageItem.TEXT == pageItem.getType()) {
                            UIOutput.make(itemListItem, "name",  messageLocator.getMessage("simplepage.chooser.textitemplaceholder"))
                                .decorate(new UIFreeAttributeDecorator("class", "text-item-placeholder"));
                        } else if (SimplePageItem.BREAK == pageItem.getType()) {
                            String text = null;
                            if ("section".equals(pageItem.getFormat())) {
                                text = messageLocator.getMessage("simplepage.break-here");
                            } else {
                                text = messageLocator.getMessage("simplepage.break-column-here");
                            }
                            UIOutput.make(itemListItem, "name", text)
                                .decorate(new UIFreeAttributeDecorator("class", "text-item-placeholder"));
                        } else {
                            UIOutput.make(itemListItem, "name", pageItem.getName());
                        }
                    }
                } // if (enableShowItems)
                index++;

                // for pagepicker or summary if canEdit and page doesn't have an item
            } else {
                int level = entry.level;
                if (level > 5) {
                    level = 5;
                }
                if (!summaryPage) {
                    // i.e. pagepicker; for the moment to edit something you need to attach it to something
                    UISelectChoice.make(row, "select", select.getFullID(), index).
                            decorate(new UIFreeAttributeDecorator("title", entry.title + " " + messageLocator.getMessage("simplepage.select")));
                } else if (summaryPage) {
                    // i.e. summary if canEdit and page doesn't have an item
                    UISelectChoice.make(row, "select-for-deletion", select.getFullID(), index).
                            decorate(new UIFreeAttributeDecorator("title", entry.title + " " + messageLocator.getMessage("simplepage.select-for-deletion")));
                    showDeleteButton = true; // at least one item to delete
                }

                GeneralViewParameters params = new GeneralViewParameters();
                params.viewID = PreviewProducer.VIEW_ID;
                params.setSendingPage(entry.pageId);

                UIInternalLink.make(row, "link", params).
                        decorate(new UIFreeAttributeDecorator("style", "padding-left: " + (2*level) + "em")).
                        decorate(new UIFreeAttributeDecorator("target", "_blank"));
                String levelstr = messageLocator.getMessage("simplepage.status.level").replace("{}", Integer.toString(level)) + " ";
                if (level > 0) {
                    UIOutput.make(row, "link-note", levelstr);
                }
                UIOutput.make(row, "link-text", entry.title);
                index++;
            }

            if (canEditPage && entry != null && entry.pageId != null && entry.itemId != null) {
                String text = null;
                if (sharedPages.contains(entry.pageId)) {
                    text = messageLocator.getMessage("simplepage.sharedpage");
                }
                SimplePageItem item = simplePageBean.findItem(entry.itemId);
                String released = simplePageBean.getReleaseString(item, localeGetter.get());
                if (released != null) {
                    if (text != null) {
                        text = text + released;
                    } else {
                        text = released;
                    }
                }
                if (text != null) {
                    UIOutput.make(row, "shared", text);
                }

                // debug code for development. this will be removed at some point
                //UIOutput.make(row, "page2", lessonsAccess.printPath(lessonsAccess.getPagePaths(entry.pageId)));
                //UIOutput.make(row, "page1", Boolean.toString(lessonsAccess.isPageAccessible(entry.pageId, simplePageBean.getCurrentSiteId(), "c08d3ac9-c717-472a-ad91-7ce0b434f42f", simplePageBean)));

                if (ServerConfigurationService.getBoolean("lessonbuilder.accessibilitydebug", false)) {
                    if (entry != null && entry.pageId != null && lessonsAccess.isPageAccessible(entry.pageId,simplePageBean.getCurrentSiteId(),"c08d3ac9-c717-472a-ad91-7ce0b434f42f", null)) {
                        UIOutput.make(row, "page1");
                    }
                    if (entry != null && entry.pageId != null && lessonsAccess.isPageAccessible(entry.pageId,simplePageBean.getCurrentSiteId(),"c08d3ac9-c717-472a-ad91-7ce0b434f42f", simplePageBean)) {
                        UIOutput.make(row, "page2");
                    }
                    if (entry != null && entry.pageId != null && lessonsAccess.isItemAccessible(entry.itemId,simplePageBean.getCurrentSiteId(),"c08d3ac9-c717-472a-ad91-7ce0b434f42f", null)) {
                        UIOutput.make(row, "item1");
                    }
                    if (entry != null && entry.pageId != null && lessonsAccess.isItemAccessible(entry.itemId,simplePageBean.getCurrentSiteId(),"c08d3ac9-c717-472a-ad91-7ce0b434f42f", simplePageBean)) {
                        UIOutput.make(row, "item2");
                    }
                }
            }
        } //for (PageEntry entry: entries)

        if (!summaryPage) {

            UIInput.make(form, "item-id", "#{simplePageBean.itemId}");

            if (itemId == -1 && !((GeneralViewParameters) viewparams).newTopLevel) {
                UIOutput.make(form, "hr");
                if (!StringUtils.equals(returnView, "reorder")) {
                    UIOutput.make(form, "options");
                }
                UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
                UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);
            }

            if (returnView != null && returnView.equals("reorder")) {
                // return to Reorder, to add items from this page
                UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.selectPage}");
            } else if (((GeneralViewParameters) viewparams).newTopLevel) {
                UIInput.make(form, "addBefore", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());
                UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addOldPage}");
            } else {
                UIInput.make(form, "addBefore", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());
                UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.createSubpage}");
            }
            UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
        } else if (showDeleteButton) {
            UICommand.make(form, "submit", messageLocator.getMessage("simplepage.delete-selected"), "#{simplePageBean.deletePages}");
        }
    }

    private UIStyleDecorator getImageSourceDecorator(SimplePageItem pageItem) {

        switch (pageItem.getType()) {
            case SimplePageItem.FORUM:
                return new UIStyleDecorator("icon-sakai--sakai-forums");
            case SimplePageItem.ASSIGNMENT:
                return new UIStyleDecorator("icon-sakai--sakai-assignment-grades");
            case SimplePageItem.ASSESSMENT:
                return new UIStyleDecorator("icon-sakai--sakai-samigo");
            case SimplePageItem.SCORM:
                return new UIStyleDecorator("icon-sakai--sakai-scorm-tool");
            case SimplePageItem.QUESTION:
                return new UIStyleDecorator("fa-question");
            case SimplePageItem.COMMENTS:
                return new UIStyleDecorator("fa-commenting");
            case SimplePageItem.BLTI:
                return new UIStyleDecorator("fa-globe");
            case SimplePageItem.PAGE:
                return new UIStyleDecorator("fa-folder-open-o");
            case SimplePageItem.RESOURCE:
                return getImageSourceDecoratorFromMimeType(pageItem);
            case SimplePageItem.MULTIMEDIA:
                return getImageSourceDecoratorFromMimeType(pageItem);
            case SimplePageItem.TEXT:
                return getImageSourceDecoratorFromMimeType(pageItem);
            default:
                return new UIStyleDecorator("");
        } 
    }

    private UIStyleDecorator getImageSourceDecoratorFromMimeType(SimplePageItem pageItem) {

        String mimeType;

        if (SimplePageItem.TEXT == pageItem.getType()) {
            mimeType = "text/html";
        } else {
            mimeType = simplePageBean.getContentType(pageItem);
        }

        String src = imageToMimeMap.get(mimeType);

        if (src == null) {
            src = "fa-file-o";
        }
        
        return new UIStyleDecorator(src);
    }

    public ViewParameters getViewParameters() {
        return new GeneralViewParameters();
    }

    public List reportNavigationCases() {

        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("failure", new SimpleViewParameters(ForumPickerProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
        togo.add(new NavigationCase("selectpage", new GeneralViewParameters(ReorderProducer.VIEW_ID)));
        GeneralViewParameters selectsiteParams = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
        selectsiteParams.setSource("anotherPage");
        togo.add(new NavigationCase("selectsite", selectsiteParams));
        return togo;
    }
}
