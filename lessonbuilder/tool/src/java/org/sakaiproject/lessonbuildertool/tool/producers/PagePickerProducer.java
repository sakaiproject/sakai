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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import org.sakaiproject.lessonbuildertool.service.LessonEntity;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UIInput;
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

/**
 * Creates a window for the user to choose which page to add
 * 
 * @author Charles Hedrick <hedrick@rutgers.edu>
 * 
 */
public class PagePickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	public static final String VIEW_ID = "PagePicker";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private ToolManager toolManager;
	public MessageLocator messageLocator;

        public class PageEntry {
	    Long pageId;
	    String title;
	    int level;
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public void setToolManager(ToolManager service) {
		toolManager = service;
	}

	public String getViewID() {
		return VIEW_ID;
	}

    // this is complicated. We're trying to produce a list of all pages in a reasonable order.
    // we start with a list of top level pages and traverse it recursively. But if there's a reference
    // to a top level page on another one, we ignore it and show the page in its normal location.
    //   then at the end if there are any pages we didn't show, we show them as if they were top level pages
    // Data:
    //   entries - the list we're building to display
    //   pageMap - a map that starts out having all pages in the site, we remove entires as we show them
    //      that lets us find at the end anything that hasn't been shown
    //   topLevels - the list of all top level pages

	public void findAllPages(SimplePageItem pageItem, List<PageEntry>entries, Map<Long,SimplePage> pageMap, Set<Long> topLevels, int level) {
	    Long pageId = Long.valueOf(pageItem.getSakaiId());	    

	    // already done if page is null
	    SimplePage page = pageMap.get(pageId);

	    // we prefer to do top levels when they appear as level 0
	    if (level > 0 && topLevels.contains(pageId))
		return;

	    // already done, except that we always do level 0
	    if (level > 0 && page == null)
		return;

	    PageEntry entry = new PageEntry();
	    entry.pageId = pageId;
	    entry.title = pageItem.getName();
	    entry.level = level;

	    // add entry
	    entries.add(entry);
	    // say done
	    pageMap.remove(pageId);
	    
	    // now recursively do subpages
	    List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
	    for (SimplePageItem item: items) {
		if (item.getType() == SimplePageItem.PAGE)
		    findAllPages(item, entries, pageMap, topLevels, 
				 ("button".equals(item.getFormat()) ? level : level + 1));
	    }
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
		    // will fail if page not in this site
		    // security then depends upon making sure that we only deal with this page
		    try {
			simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
		    } catch (Exception e) {
			System.out.println("PagePicker permission exception " + e);
			return;
		    }
		}

		// this looks at pages in the site, which should be safe
		// but need to make sure the item we update is legal

		Long itemId = ((GeneralViewParameters) viewparams).getItemId();

		simplePageBean.setItemId(itemId);

		if (simplePageBean.canEditPage()) {
		        
			SimplePage page = simplePageBean.getCurrentPage();

			if (itemId != null && itemId != -1) {
			    SimplePageItem currentItem = simplePageToolDao.findItem(itemId);
			    if (currentItem == null)
				return;
			    // trying to hack on item not on this page
			    if (currentItem.getPageId() != page.getPageId())
				return;
			}

			// list we're going to display
			List<PageEntry> entries = new ArrayList<PageEntry> ();

			// build map of all pages, so we can see if any are left over
			Map<Long,SimplePage> pageMap = new HashMap<Long,SimplePage>();
			
			List<SimplePage> pages = simplePageToolDao.getSitePages(simplePageBean.getCurrentSiteId());
			for (SimplePage p: pages)
			    pageMap.put(p.getPageId(), p);

			// list of all top level pages
			List<SimplePageItem> sitePages =  simplePageToolDao.findItemsInSite(toolManager.getCurrentPlacement().getContext());
			// need it as a set also for quicker checking
			Set<Long>topLevelIds = new HashSet<Long>();
			for (SimplePageItem i: sitePages)
			    topLevelIds.add(Long.valueOf(i.getSakaiId()));

			// this adds everything you can find from top level pages to entires
			for (SimplePageItem sitePageItem : sitePages) {
			    findAllPages(sitePageItem, entries, pageMap, topLevelIds, 0);
			}
			// now add everything we didn't find that way
			if (pageMap.size() > 0) {
			    // marker
			    PageEntry marker = new PageEntry();
			    marker.level = -1;
			    entries.add(marker);
			    for (SimplePage p: pageMap.values()) {
				PageEntry entry = new PageEntry();
				entry.pageId = p.getPageId();
				entry.title = p.getTitle();
				entry.level = 0;
				entries.add(entry);
			    }
			}

			UIForm form = UIForm.make(tofill, "page-picker");

			ArrayList<String> values = new ArrayList<String>();
			for (PageEntry entry: entries)
			    if (entry.level >= 0)
				values.add(entry.pageId.toString());

			UISelect select = UISelect.make(form, "page-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntity}", null);
			int index = 0;
			for (PageEntry entry: entries) {

			    UIBranchContainer row = UIBranchContainer.make(form, "page:", Integer.toString(index));

			    if (entry.level < 0)
				UIOutput.make(row, "heading", messageLocator.getMessage("simplepage.chooser.unused"));
			    else {
				UISelectChoice.make(row, "select", select.getFullID(), index);
				int level = entry.level;
				if (level > 5)
				    level = 5;
				GeneralViewParameters params = new GeneralViewParameters();
				params.viewID = PreviewProducer.VIEW_ID;
				params.setSendingPage(entry.pageId);

				UIInternalLink.make(row, "link", entry.title, params).
				    decorate(new UIFreeAttributeDecorator("style", "padding-left: " + (2*level) + "em"));

				index++;
			    }

			}

			UIInput.make(form, "item-id", "#{simplePageBean.itemId}");

			if (itemId == -1) {
			    UIOutput.make(form, "hr");
			    UIOutput.make(form, "options");
			    UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
			    UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);
			}

			UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.createSubpage}");
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");

		}

	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(ForumPickerProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}
}
