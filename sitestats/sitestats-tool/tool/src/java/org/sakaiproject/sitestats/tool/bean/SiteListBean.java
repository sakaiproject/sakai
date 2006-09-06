/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.tool.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.jsf.InitializableBean;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class SiteListBean extends InitializableBean implements Serializable {
	private static final long	serialVersionUID	= -8271768875730368317L;

	private static Log			LOG					= LogFactory.getLog(SiteListBean.class);

	protected ResourceBundle	msgs				= ResourceBundle.getBundle("org.sakaiproject.sitestats.tool.bundle.Messages");
	private String				SITE_TYPE_ALL		= msgs.getString("all");
	private final static String COL_TITLE			= "title";
	private final static String COL_TYPE			= "type";
	private final static String COL_STATUS			= "status";

	/** Private */
	//private boolean				allowed				= true;
	private Boolean				allowed				= Boolean.TRUE;
	private List				siteRows;
	private String				siteId				= null;
	private Site				site				= null;
	private String				userId				= SessionManager.getCurrentSessionUserId();
	private Tool				tool				= ToolManager.getCurrentTool();

	/** UI related */
	private List				siteTypes;
	private String				searchKeyword;
	private String				selectedSiteType;
	private int					totalItems			= 0;
	private int					firstItem			= 0;
	private int					pageSize			= 20;
	private SortType			sortType			= SortType.TITLE_ASC;
	private boolean				sortAscending		= true;
	private String				sortColumn			= COL_TITLE;

	/** Manager APIs */
	private SiteService			M_ss				= (SiteService) ComponentManager.get(SiteService.class.getName());
	private StatsManager		sm					= (StatsManager) ComponentManager.get(StatsManager.class.getName());

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("SiteListsBean.init()");

		if(isAllowed()){
			getSiteRows();
		}
	}

	/*public boolean isAllowed() {
		allowed = SecurityService.isSuperUser() && ToolManager.getCurrentTool().getId().endsWith("admin");
		if(!allowed){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, msgs.getString("unauthorized"), null));
		}
		return allowed;
	}*/
	public boolean isAllowed() {
		// get site
		if(site == null){
			try{
				site = M_ss.getSite(ToolManager.getCurrentPlacement().getContext());
			}catch(IdUnusedException e){
				LOG.error("Unable to get current site.", e);
				return false;
			}
		}

		if(allowed == null) allowed = new Boolean(sm.isUserAllowed(userId, site, tool));
		if(!allowed.booleanValue()){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("allowed", new FacesMessage(FacesMessage.SEVERITY_FATAL, msgs.getString("unauthorized"), null));
		}
		return allowed.booleanValue();
	}
	
	public String getSiteId() {
		if(siteId == null){
			Placement placement = ToolManager.getCurrentPlacement();
			siteId = placement.getContext();
		}
		return siteId;
	}

	public Site getSite() {
		try{
			site = M_ss.getSite(getSiteId());
		}catch(IdUnusedException e){
			LOG.warn("BaseBean: no site found with id: " + siteId);
		}
		return site;
	}

	// ######################################################################################
	// ActionListener methods
	// ######################################################################################
	public String processActionSearch() {
		firstItem = 0;
		totalItems = 0;
		return "sitelist";
	}

	public String processActionClearSearch() {
		searchKeyword = null;
		firstItem = 0;
		totalItems = 0;
		return "sitelist";
	}

	public void processActionSearchChangeListener(ValueChangeEvent event) {
		String newValue = (String) event.getNewValue();
		setSearchKeyword(newValue);
		firstItem = 0;
		totalItems = 0;
	}

	public void processActionSiteTypeChangeListener(ValueChangeEvent event) {
		String newValue = (String) event.getNewValue();
		setSelectedSiteType(newValue);
		firstItem = 0;
		totalItems = 0;
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public List getSiteRows() {
		// site type
		String sType = getSelectedSiteType();
		if(sType.equals(SITE_TYPE_ALL)) sType = null;

		// pager
		int start = firstItem + 1;
		int end = start + pageSize - 1;
		PagingPosition pp = new PagingPosition(start, end);

		siteRows = M_ss.getSites(SelectionType.NON_USER, sType, searchKeyword, null, sortType, pp);
		totalItems = M_ss.countSites(SelectionType.NON_USER, sType, searchKeyword, null);
		return siteRows;
	}

	public List getSiteTypes() {
		if(siteTypes == null){
			List types = siteTypes = M_ss.getSiteTypes();
			siteTypes = new ArrayList();
			siteTypes.add(new SelectItem(SITE_TYPE_ALL));
			Iterator i = types.iterator();
			while (i.hasNext()){
				siteTypes.add(new SelectItem((String) i.next()));
			}
		}
		return siteTypes;
	}

	public String getSelectedSiteType() {
		if(selectedSiteType != null) return selectedSiteType;
		else{
			selectedSiteType = ((SelectItem) getSiteTypes().get(0)).getLabel();
			return selectedSiteType;
		}
	}

	public void setSelectedSiteType(String selectedSiteType) {
		this.selectedSiteType = selectedSiteType;
	}

	public void setSearchKeyword(String searchKeyword) {
		if(searchKeyword != null && (searchKeyword.trim().length() == 0 || searchKeyword.equals(msgs.getString("search_int2")))) this.searchKeyword = null;
		else this.searchKeyword = searchKeyword;
	}

	public String getSearchKeyword() {
		if(searchKeyword == null) return msgs.getString("search_int2");
		return searchKeyword;
	}

	public boolean isEmptySiteList() {
		return siteRows == null || siteRows.size() == 0;
	}

	public int getFirstItem() {
		return firstItem;
	}

	public void setFirstItem(int firstItem) {
		this.firstItem = firstItem;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalItems() {
		return totalItems;
	}
	
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
		selectSortType(this.sortColumn, this.sortAscending);
	}

	public boolean getSortAscending() {
		return this.sortAscending;
	}

	public String getSortColumn() {
		return this.sortColumn;
	}

	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
		selectSortType(this.sortColumn, this.sortAscending);
	}
	
	private void selectSortType(String sortColumn, boolean sortAscending){
		if(sortColumn.equals(COL_TITLE)){
			if(sortAscending) sortType = SortType.TITLE_ASC;
			else sortType = SortType.TITLE_DESC;
		}else if(sortColumn.equals(COL_TYPE)){
			if(sortAscending) sortType = SortType.TYPE_ASC;
			else sortType = SortType.TYPE_DESC;
		}else if(sortColumn.equals(COL_STATUS)){
			if(sortAscending) sortType = SortType.PUBLISHED_ASC;
			else sortType = SortType.PUBLISHED_DESC;
		}
	}
}
