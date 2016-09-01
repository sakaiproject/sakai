package org.sakaiproject.lessonbuildertool.tool.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.producers.PagePickerProducer;
import org.sakaiproject.lessonbuildertool.tool.producers.PagePickerProducer.PageEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Code more or less extracted from SimplePageBean
public class OrphanPageFinder {

	private String siteId;
	private Set<Long> orphanedPages;
	private SimplePageToolDao simplePageToolDao;
	private PagePickerProducer pagePickerProducer;
	private static Logger log = LoggerFactory.getLogger(SimplePageBean.class);

	public OrphanPageFinder(String siteId, SimplePageToolDao simplePageToolDao, PagePickerProducer pagePickerProducer) {
		this.siteId = siteId;
		this.orphanedPages = new HashSet<Long>();
		this.simplePageToolDao = simplePageToolDao;
		this.pagePickerProducer = pagePickerProducer;

		loadOrphanedPages();
	}

	public boolean isOrphan(long itemId) {
		return orphanedPages.contains(itemId);
	}

	public Collection<Long> getOrphanPageIds() {
		return orphanedPages;
	}

	//Simple Page bean expects an array of string instead of a Collection of Long so need to do the conversion
	public String [] getOrphanStringsIds() {
		List<String> orphans = new ArrayList<String>();
		for (Long pageId : orphanedPages) {
			orphans.add(Long.toString(pageId));
		}
		return orphans.toArray( new String[orphans.size()]);
	}


	private void loadOrphanedPages() {
		log.debug("loadOrphanedPages for site:" +siteId);
		Map<Long,SimplePage> allPagesInSite = new HashMap<Long,SimplePage>();

		// Load our map of all pages
		//
		// This seems a bit wasteful: pagePickerProducer doesn't
		// really need the pages at all (it just uses the keys
		// of this map as a set).  Could probably refactor this
		// not to pull all the pages into memory if needed.
		for (SimplePage p: simplePageToolDao.getSitePages(siteId)) {
			allPagesInSite.put(p.getPageId(), p);
		}

		// Get a list of the top-level lessons pages
		List<SimplePageItem> topLevelPages =  simplePageToolDao.findItemsInSite(siteId);
		Set<Long> topLevelPageIds = new HashSet<Long>();
		for (SimplePageItem i : topLevelPages)
			topLevelPageIds.add(Long.valueOf(i.getSakaiId()));


		// Walk from our top-level pages to find all reachable pages
		List<PagePickerProducer.PageEntry> entries = new ArrayList<PagePickerProducer.PageEntry> ();
		Set<Long> sharedPages = new HashSet<Long>();
		for (SimplePageItem topLevelPage : topLevelPages) {
			pagePickerProducer.findAllPages(topLevelPage, entries, allPagesInSite, topLevelPageIds, sharedPages, 0, true, true);
		}

		// allPagesInSite has been mutated to remove anything
		// that was reachable.  The remaining pages might be
		// orphans.
		if (allPagesInSite.size() > 0) {
			for (SimplePage p: allPagesInSite.values()) {
				// non-null owner are student pages (not orphans)
				if(p.getOwner() == null) {
					orphanedPages.add(p.getPageId());
				}
			}
		}	    
		//If debugging is enabled write out all if the ids found
		if (log.isDebugEnabled()) {
			for (Long pageId : orphanedPages) {
				log.debug(String.valueOf(pageId));
			}
		}


	}
}
