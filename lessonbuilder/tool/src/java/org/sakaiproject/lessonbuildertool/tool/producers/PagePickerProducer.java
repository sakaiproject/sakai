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
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
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
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
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
	    Long itemId;
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
    // Data:
    //   entries - the list we're building to display
    //   pageMap - a map that starts out having all pages in the site, we remove entires as we show them
    //      that lets us find at the end anything that hasn't been shown

    public void findAllPages(SimplePageItem pageItem, List<PageEntry>entries, Map<Long,SimplePage> pageMap, Set<Long>topLevelPages, int level) {
	    Long pageId = Long.valueOf(pageItem.getSakaiId());	    

	    // already done if page is null
	    if (pageMap.get(pageId) == null)
		return;

	    // say done
	    pageMap.remove(pageId);

	    PageEntry entry = new PageEntry();
	    entry.pageId = pageId;
	    entry.itemId = pageItem.getId();
	    entry.title = pageItem.getName();
	    entry.level = level;

	    // add entry
	    entries.add(entry);

	    // now recursively do subpages
	    List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
	    List<SimplePageItem> nexts = new ArrayList<SimplePageItem>();

	    // subpages done in place
	    for (SimplePageItem item: items) {
		if (item.getType() == SimplePageItem.PAGE) {
		    Long pageNum = Long.valueOf(item.getSakaiId());

		    // show all pages where they appear, but for nexts and top-level
		    // do the full expansion later if necessary
		    if (item.getNextPage() || topLevelPages.contains(pageNum)) {
			// if it has nexts show item here but treat it fully afterwards
			PageEntry stub = new PageEntry();
			stub.pageId = Long.valueOf(item.getSakaiId());
			stub.itemId = item.getId();
			stub.title = item.getName();
			stub.level = level + 1;
			entries.add(stub);
			// if not top (which will be done anyway) schedule it to show after
			// pageid = 0 is a top level page; it will be shown anyway
			// if no sub pages, no need to expand it later
			if (!topLevelPages.contains(pageNum)) {
			    if (hasSubPages(pageNum))
				nexts.add(item);
			    else
				// we're done with this page, dont show again
				pageMap.remove(pageNum);
			}
		    } else
			findAllPages(item, entries, pageMap, topLevelPages, level +1);
		}
	    }
	    // nexts done afterwards
	    for (SimplePageItem item: nexts) {
		if (item.getType() == SimplePageItem.PAGE) {
		    findAllPages(item, entries, pageMap, topLevelPages, level);
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

        	boolean canEditPage = simplePageBean.canEditPage();

		String source = ((GeneralViewParameters) viewparams).getSource();
		boolean summaryPage = "summary".equals(source);

		if (summaryPage) {
		    GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
		    // path defaults to null, which is next
		    UIOutput.make(tofill, "return-div");
		    UIInternalLink.make(tofill, "return", messageLocator.getMessage("simplepage.return"), view);
		    UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.page.index"));
		} else {
		    UIOutput.make(tofill, "title", messageLocator.getMessage("simplepage.page.chooser"));
		}

		// this looks at pages in the site, which should be safe
		// but need to make sure the item we update is legal

		Long itemId = ((GeneralViewParameters) viewparams).getItemId();

		simplePageBean.setItemId(itemId);

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
			
		// all pages
		List<SimplePage> pages = simplePageToolDao.getSitePages(simplePageBean.getCurrentSiteId());
		for (SimplePage p: pages)
		    pageMap.put(p.getPageId(), p);

		// set of all top level pages, actually the items pointing to them
		List<SimplePageItem> sitePages =  simplePageToolDao.findItemsInSite(toolManager.getCurrentPlacement().getContext());
		Set<Long> topLevelPages = new HashSet<Long>();
		for (SimplePageItem i : sitePages)
		    topLevelPages.add(Long.valueOf(i.getSakaiId()));

		// this adds everything you can find from top level pages to entires
		for (SimplePageItem sitePageItem : sitePages) {
		    findAllPages(sitePageItem, entries, pageMap, topLevelPages, 0);
		}
		// now add everything we didn't find that way
		if (canEditPage && pageMap.size() > 0) {
		    // marker
		    PageEntry marker = new PageEntry();
		    marker.level = -1;
		    entries.add(marker);
		    for (SimplePage p: pageMap.values()) {
			PageEntry entry = new PageEntry();
			entry.pageId = p.getPageId();
			entry.itemId = null;
			entry.title = p.getTitle();
			entry.level = 0;
			entries.add(entry);
		    }
		}

		UIForm form = UIForm.make(tofill, "page-picker");

		ArrayList<String> values = new ArrayList<String>();
		ArrayList<String> initValues = new ArrayList<String>();
		for (PageEntry entry: entries)
		    if (entry.level >= 0) {
			values.add(entry.pageId.toString());
			initValues.add("");
		    }

		UISelect select = null;
		if (summaryPage)
		    select = UISelect.makeMultiple(form, "page-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntities}" , initValues.toArray(new String[1]));
		else
		    select = UISelect.make(form, "page-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntity}", null);

		int index = 0;
		boolean showDeleteButton = false;

		for (PageEntry entry: entries) {
		    
		    UIBranchContainer row = UIBranchContainer.make(form, "page:");
		    
		    if (entry.level < 0)
			UIOutput.make(row, "heading", messageLocator.getMessage("simplepage.chooser.unused"));
		    // if no itemid, it's unused. Only canedit people will see it
		    else if (summaryPage && entry.itemId != null) {
			int level = entry.level;
			if (level > 5)
			    level = 5;
			String imagePath = "/sakai-lessonbuildertool-tool/images/";
			SimplePageItem item = simplePageBean.findItem(entry.itemId);
			SimplePageLogEntry logEntry = simplePageBean.getLogEntry(entry.itemId);
			String note = null;
			if (logEntry != null && logEntry.isComplete()) {
			    imagePath += "checkmark.png";
			    note = messageLocator.getMessage("simplepage.status.completed");
			} else if (logEntry != null && !logEntry.getDummy()) {
			    imagePath += "hourglass.png";
			    note = messageLocator.getMessage("simplepage.status.inprogress");
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
			if (level > 0)
			    levelstr = messageLocator.getMessage("simplepage.status.level").replace("{}", Integer.toString(level));
			if (levelstr != null) {
			    if (note != null)
				note = levelstr + " " + note;
			    else
				note = levelstr;
			}
			if (note != null)
			    UIOutput.make(row, "link-note", note + " ");
			UIOutput.make(row, "link-text", entry.title);

			index++;

	  	// for pagepicker or summary if canEdit and page doesn't have an item
		    } else {
			int level = entry.level;
			if (level > 5)
			    level = 5;
			if (!summaryPage)  // i.e. pagepicker; for the moment to edit something you need to attach it to something
			    UISelectChoice.make(row, "select", select.getFullID(), index).
				decorate(new UIFreeAttributeDecorator("title", entry.title + " " + messageLocator.getMessage("simplepage.select")));
			else { // i.e. summary if canEdit and page doesn't have an item
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
			if (level > 0)
			    UIOutput.make(row, "link-note", levelstr);
			UIOutput.make(row, "link-text", entry.title);

			index++;

		    }

		}

		if (!summaryPage) {

		    UIInput.make(form, "item-id", "#{simplePageBean.itemId}");

		    if (itemId == -1) {
			UIOutput.make(form, "hr");
			UIOutput.make(form, "options");
			UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
			UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);
		    }

		    UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.createSubpage}");
		    UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
		} else if (showDeleteButton) {
		    UICommand.make(form, "submit", messageLocator.getMessage("simplepage.delete-selected"), "#{simplePageBean.deletePages}");
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
