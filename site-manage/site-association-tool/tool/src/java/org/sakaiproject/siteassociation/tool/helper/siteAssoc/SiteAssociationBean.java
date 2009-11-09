/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.siteassociation.tool.helper.siteAssoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.siteassociation.tool.common.BaseBean;
import org.sakaiproject.siteassociation.tool.util.Pager;
import org.sakaiproject.siteassociation.tool.util.Sort;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;

public class SiteAssociationBean extends BaseBean {

	// Sort strings
	static final String ID = "id", TITLE = "title";

	private static final Log logger = LogFactory
			.getLog(SiteAssociationBean.class);

	// Pager sitesPager; See GM-47

	private List<Site> searchSites = new ArrayList<Site>();
	private List<Site> assocSites;
	private List<Site> searchSitesCompleteCopy = new ArrayList<Site>();

	Sort sitesSort, confirmSort, assocSitesSort, searchSitesSort;

	List<DecoratedSite> confirmSites;
	
	private String searchSiteParam = "", prevSearchParam ="";
	
	private Pager searchSitesPager, assocSitesPager;

	private int searchSiteListSize = -1;
	private int assocSiteListSize = -1;
	
	private ToolConfiguration tool;

	public SiteAssociationBean() {
		super();
	}

	public String cancelChanges() {
		cleanup();
		return CANCEL;
	}


	/*
	 * public Pager getSitesPager() { if (sitesPager == null) sitesPager = new
	 * Pager(Integer.valueOf(getSites().size()), Integer.valueOf(0),
	 * Integer.valueOf(20)); return sitesPager; }
	 */

	protected void cleanup() {
		searchSites = null;
		assocSites = null;
		searchSitesPager = null;
		searchSitesCompleteCopy = null;
		assocSitesPager = null;
		searchSiteParam = prevSearchParam = "";
		
		// sitesPager = null;
		sitesSort = confirmSort = assocSitesSort = searchSitesSort = null;
	}

	public List<DecoratedSite> getConfirmSites() {
		// Refresh this every time since we don't know what
		// has changed since a confirm cancel
		confirmSites = new ArrayList<DecoratedSite>();
		List<Site> current = getSavedAssocSites();
		
		for(Site s : assocSites){
			DecoratedSite ds = new DecoratedSite(s, true);
			ds.setModified(true);
			confirmSites.add(ds);			
		}
				
		for (Site s : current) {
			boolean exists = false;
			for(int i = confirmSites.size() - 1; i >= 0; i--){
				DecoratedSite dsite = confirmSites.get(i);
				if(dsite.getSite().getId().compareTo(s.getId()) == 0){
					exists = true;
					confirmSites.remove(i);
				}
			}				
			if(!exists){
				DecoratedSite ds = new DecoratedSite(s, false);
				ds.setModified(true);
				confirmSites.add(ds);
			}
		}

		sortDecoratedSites(confirmSites, getConfirmSort());
		return confirmSites;
	}

	public Sort getConfirmSort() {
		if (confirmSort == null) {
			confirmSort = new Sort("", true);
		}
		return confirmSort;
	}

	public boolean getShowUnassociateWarning() {
		for (DecoratedSite dSite : getConfirmSites()) {
			if (dSite.isModified() && !dSite.isAssociated()) {
				return true;
			}
		}
		return false;
	}

	public Site getSite() {
		Site site = null;
		try {
			site = getSiteService().getSite(getContext());
		} catch (IdUnusedException iue) {
			logger.error(iue.getMessage(), iue);
		}
		return site;
	}
	
	
	public List<Site> getSavedAssocSites(){
		ArrayList<Site> l = new ArrayList<Site>();
		for (String fromContext : getSiteAssocManager().getAssociatedTo(
				getContext())) {
			try {
				Site site = getSiteService().getSite(fromContext);
				l.add(site);
			} catch (IdUnusedException iue) {
				logger.error(iue.getMessage(), iue);
			}
		}
		return l;
	}

	public List<Site> getAssocSites(){
		if(assocSites == null){

			searchSites = getSearchResults(SiteService.SelectionType.ACCESS, searchSiteParam);
				
			assocSites = getSavedAssocSites();
			//save a copy of the complete search list for when a user removes an already
			//associated list, that you will be able to decide if that removed site should
			//pop back into the searchList
			searchSitesCompleteCopy = new ArrayList<Site>();
			for(int i = 0; i < searchSites.size(); i++){
				searchSitesCompleteCopy.add(searchSites.get(i));
			}
			
			//remove any sites that are already associated:
			for(int i = 0; i < assocSites.size(); i++){
				searchSites.remove(assocSites.get(i));
			}
			resetSearchSitesPager();
		}
		sortSites(assocSites, getAssocSitesSort());
		return assocSites;
	}
	
	public List<Site> getSearchSites(){
		sortSites(searchSites, getSearchSitesSort());
		return searchSites;
	}
	
	public String getSearchSiteParam() {
		return searchSiteParam;
	}
	
	public void setSearchSiteParam(String searchSiteParam) {
		this.searchSiteParam = searchSiteParam;
	}
	
	

	public Sort getSitesSort() {
		if (sitesSort == null) {
			sitesSort = new Sort("", true);
		}
		return sitesSort;
	}
	
	public Sort getAssocSitesSort() {
		if (assocSitesSort == null) {
			assocSitesSort = new Sort("", true);
		}
		return assocSitesSort;
	}
	
	public Sort getSearchSitesSort() {
		if (searchSitesSort == null) {
			searchSitesSort = new Sort("", true);
		}
		return searchSitesSort;
	}

	public String saveChanges() {	
		List<Site> current = getSavedAssocSites();
		
		
		if(current.size() != assocSites.size()){
			return SAVE;
		}else{
		
			for (Site s : current) {
				boolean exists = false;
				for(Site assoc : assocSites){
					if(assoc.getId().compareTo(s.getId()) == 0){
						exists = true;
					}
				}				
				if(!exists){
					return SAVE;
				}
			}
		}
		return cancelChanges();
	}

	public String resetChanges() {
		cleanup();
		
		return null;
	}
	

	public void sortSites(List<Site> list, final Sort sort) {
		Collections.sort(list, new Comparator<Site>() {
			public int compare(Site o1, Site o2) {
				Site s1 = null;
				Site s2 = null;
				if (sort.isAscending()) {
					s1 = o1;
					s2 = o2;
				} else {
					s2 = o1;
					s1 = o2;
				}
				if (sort.getSort().equalsIgnoreCase(TITLE)) {
					return s1.getTitle().compareToIgnoreCase(
							s2.getTitle());
				} else if (sort.getSort().equalsIgnoreCase(ID)) {
					return s1.getId().compareToIgnoreCase(
							s2.getId());
				} else {
					return s1.getTitle().compareToIgnoreCase(
							s2.getTitle());
				}
			}
		});
	}
	
	public void sortDecoratedSites(List<DecoratedSite> list, final Sort sort) {
		Collections.sort(list, new Comparator<DecoratedSite>() {
			public int compare(DecoratedSite o1, DecoratedSite o2) {
				DecoratedSite s1 = null;
				DecoratedSite s2 = null;
				if (sort.isAscending()) {
					s1 = o1;
					s2 = o2;
				} else {
					s2 = o1;
					s1 = o2;
				}
				if (sort.getSort().equalsIgnoreCase(TITLE)) {
					return s1.getSite().getTitle().compareToIgnoreCase(
							s2.getSite().getTitle());
				} else if (sort.getSort().equalsIgnoreCase(ID)) {
					return s1.getSite().getId().compareToIgnoreCase(
							s2.getSite().getId());
				} else {
					return s1.getSite().getTitle().compareToIgnoreCase(
							s2.getSite().getTitle());
				}
			}

		});
	}

	public String updateSites() {
		for (DecoratedSite dSite : confirmSites) {
			if (dSite.isAssociated()) {
				getSiteAssocManager().addAssociation(dSite.getSite().getId(),
						getContext());
			} else {
				getSiteAssocManager().removeAssociation(dSite.getSite().getId(),
						getContext());
			}
		}
		cleanup();
		return SAVE;
	}

	public class DecoratedSite {
		Site site;

		boolean original;

		boolean associated;
		
		boolean modified;

		public DecoratedSite(Site site, boolean associated) {
			this.site = site;
			original = this.associated = associated;
		}

		public Site getSite() {
			return site;
		}

		public boolean isAssociated() {
			return associated;
		}

		public boolean isModified() {
			return modified;
		}

		public void setModified(boolean modified){
			this.modified = modified;
		}
		
		public void setAssociated(boolean associated) {
			this.associated = associated;
		}
	}

	public String getPrevSearchParam() {
		return prevSearchParam;
	}

	public void setPrevSearchParam(String prevSearchParam) {
		this.prevSearchParam = prevSearchParam;
	}
	
	public List<Site> getSearchResults(SelectionType type, String criteria){
		List<Site> searchResults = getSiteService().getSites(type, null, criteria, 
				null, SiteService.SortType.TITLE_ASC, null);
		
		boolean found = false;
		for (Site site : searchResults) {
			if(site.getId().compareTo(criteria) == 0){
				found = true;
				break;
			}
		}
		
		if(!found){
			//look up site by ID
			try {
				Site s = getSiteService().getSite(criteria);	
				if(SiteService.SelectionType.ACCESS.equals(type) && s.isAllowed(UserDirectoryService.getCurrentUser().getId(), "site.visit")
						|| SiteService.SelectionType.ANY.equals(type)){
					searchResults.add(s);
				}
			} catch (IdUnusedException e) {
				//site doesn't exists, so don't worry
			}			
		}
		return 	searchResults;
	}	
	
	public void searchForSites(){
		

		if(isInstitutionSite()){
			searchSites = getSearchResults(SiteService.SelectionType.ANY, searchSiteParam);
		}else{
			searchSites = getSearchResults(SiteService.SelectionType.ACCESS, searchSiteParam);
		}
		
		//save a copy of the complete search list for when a user removes an already
		//associated list, that you will be able to decide if that removed site should
		//pop back into the searchList
		searchSitesCompleteCopy = new ArrayList<Site>();
		for(int i = 0; i < searchSites.size(); i++){
			searchSitesCompleteCopy.add(searchSites.get(i));
		}
		
		//remove any sites that are already associated:
		for(int i = 0; i < assocSites.size(); i++){
			searchSites.remove(assocSites.get(i));
		}
		resetSearchSitesPager();
		
		searchSitesPager = null;
		prevSearchParam = searchSiteParam;
		searchSiteParam = "";
	}
	
	public Pager getSearchSitesPager() {
		if (searchSitesPager == null) {
			searchSitesPager = new Pager(Integer.valueOf(searchSites == null? 0 : searchSites.size()), Integer
					.valueOf(0), Integer.valueOf(5));
		}
		return searchSitesPager;
	}
	
	public Pager getAssocSitesPager() {
		if (assocSitesPager == null) {
			assocSitesPager = new Pager(Integer.valueOf(assocSites == null? 0 : assocSites.size()), Integer
					.valueOf(0), Integer.valueOf(5));
		}
		return assocSitesPager;
	}
	
	public int getSearchSiteListSize(){
		return searchSiteListSize = (searchSites == null) ? -1 : searchSites.size();
	}
	
	public int getAssocSiteListSize(){
		if(assocSites == null){
			getAssocSites();
		}
		return assocSiteListSize = (assocSites == null) ? -1 : assocSites.size();
	}
	
	public String addSiteToAssocList(){
		String siteId = (String) getSessionManager().getCurrentToolSession()
		.getAttribute("addSiteId");

		boolean exists = false;
		for(Site s : assocSites){
			if(s.getId().compareTo(siteId) == 0 ){
				exists = true;
			}
		}
		
		if(!exists){
			Site s;
			try {
				s = getSiteService().getSite(siteId);	
				if(assocSites == null){
					assocSites = new ArrayList<Site>();
					assocSites.add(s);
				}else{
					assocSites.add(s);
					//set the assoc site pager to the way the user had it
					resetAssocSitesPager();
				}
				
				//remove this from the results list
				searchSites.remove(s);
				resetSearchSitesPager();

			} catch (IdUnusedException e) {
			}
			
		}
		
		sortSites(assocSites, getAssocSitesSort());
		
		return null;		
	}
	
	public String removeSiteFromAssocList(){
		String siteId = (String) getSessionManager().getCurrentToolSession()
		.getAttribute("removeSiteId");

		int removeIndex = -1;
		for(int i = 0; i < assocSites.size(); i++){	
			if(assocSites.get(i).getId().compareTo(siteId) == 0 ){
				removeIndex = i;
			}			
		}

		if(removeIndex != -1){
			Site removedS = assocSites.get(removeIndex);
			assocSites.remove(removeIndex);
			//add the removed assoc site to the search site if it is 
			//in the searchSitesCompleteCopy list
			if(searchSitesCompleteCopy.contains(removedS)){
				searchSites.add(removedS);
				resetSearchSitesPager();
			}
			//set the assoc site pager to the way the user had it
			resetAssocSitesPager();		
		}
			
		return null;		
	}
	
	public void addAllSites(){
		int first, results;
		
		if(searchSitesPager != null){
			first = searchSitesPager.getFirstItem();
			results = searchSitesPager.getPageSize();
			//show all case
			if(results == 0){
				results = searchSites.size();
			}
		}else{
			first = 0;
			results = searchSites.size();
		}
		
	//	for (Iterator iterator = searchSites.iterator(); iterator.hasNext();) {
			
			
		//}
		int startI = (first + results > searchSites.size()) ? searchSites.size() - 1 : first + results - 1;
		for(int i = startI; i >= first; i--){
			Site site = searchSites.get(i);			

			//assoc list:
			int exists = -1;
			for(int j = 0; j < assocSites.size(); j++){
				Site s = assocSites.get(j);			
				if(s.getId().compareTo(site.getId()) == 0){
					exists = j;
				}
			}

			if(exists == -1){
				assocSites.add(site);
				//remove this from the results list
				searchSites.remove(site);
				resetSearchSitesPager();
			}
		}
		
		resetAssocSitesPager();
	}
	
	public void removeAllSites(){

		int first, results;
		
		if(assocSitesPager != null){
			first = assocSitesPager.getFirstItem();
			results = assocSitesPager.getPageSize();
			//show all case
			if(results == 0){
				results = assocSites.size();
			}
		}else{
			first = 0;
			results = assocSites.size();
		}
		
		
		int i = (first + results > assocSites.size()) ? assocSites.size() - 1 : first + results - 1;
		
		for(i = i; i >= first; i--){
			Site removedS = assocSites.get(i);
			assocSites.remove(i);
			if(searchSitesCompleteCopy.contains(removedS)){
				searchSites.add(removedS);
			}
		}
		resetSearchSitesPager();
		//reset assoc pager:
		resetAssocSitesPager();
	}
	
	public void resetAssocSitesPager(){
		if(assocSitesPager != null){
			if(assocSites.size() > 5){
				int firstItem = assocSitesPager.getFirstItem();
				int pageSize = assocSitesPager.getPageSize();
				if(firstItem >= assocSites.size()){
					firstItem--;
				}

				assocSitesPager = null;

			
				getAssocSitesPager().setFirstItem(firstItem);
				getAssocSitesPager().setPageSize(pageSize);
			}else{
				assocSitesPager = null;
			}
		}
	}
	
	public void resetSearchSitesPager(){
		if(searchSitesPager != null){
			if(searchSites.size() > 5){
				int firstItem = searchSitesPager.getFirstItem();
				int pageSize = searchSitesPager.getPageSize();
				if(firstItem >= searchSites.size()){
					firstItem--;
				}

				searchSitesPager = null;

			
				getSearchSitesPager().setFirstItem(firstItem);
				getSearchSitesPager().setPageSize(pageSize);
			}else{
				searchSitesPager = null;
			}
		}
	}
	
	public boolean isInstitutionSite(){
		if(tool == null){
			tool = getSiteAssocManager().getSite(getContext()).getTool(ToolManager.getCurrentPlacement().getId());
		}
		return Boolean.parseBoolean((String)tool.getConfig().getProperty("institutionSite"));	
	}
}
