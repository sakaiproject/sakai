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
import java.text.Collator;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.StatsManager;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class ResourcesBean extends BaseFilteringBean implements Serializable {
	private static final long	serialVersionUID			= -1200104029531352936L;

	/** Our log (commons). */
	private static Log			LOG							= LogFactory.getLog(ResourcesBean.class);

	/** Statistics Manager object */
	private BaseBean			baseBean					= null;
	private StatsManager		sm							= getStatsManager();
	private Collator			collator					= Collator.getInstance();

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		super.init();
		LOG.debug("ResourcesBean.init()");
		
		initializeBaseBean();

		if(baseBean.isAllowed()){
			refreshAllData();
	
			List events = getEvents();
			// filter groups
			String grpId = getSelectedGroup();
			if(grpId != null && !grpId.equals(ALL)) events = filterGroup(events, grpId);
	
			// update pager
			int total = events.size();
			setTotalItems(total);
			if(total <= getPageSize()) setRenderPager(false);
			else setRenderPager(true);
	
			// sort lists
			List groups = getGroups();
			Collections.sort(events, getCommonStatGrpByDateComparator(getSortColumn(), isSortAscending(), collator));
			setEvents(events);
			setGroups(groups);
		}
	}
	
	private void initializeBaseBean(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		baseBean = (BaseBean) facesContext.getApplication()
			.createValueBinding("#{BaseBean}")
			.getValue(facesContext);
	}

	private void refreshAllData() {
		// initialize events
		List events = null;

		// filter dates?
		Date iDate = null;
		Date fDate = null;
		if(getSelectedDateType().equals("1")){
			iDate = convertToDate(getFromYear(), getFromMonth(), getFromDay());
			fDate = convertToDate(getToYear(), getToMonth(), getToDay());
		}else{
			iDate = sm.getInitialActivityDate(baseBean.getSiteId());
			fDate = Calendar.getInstance().getTime();
		}			
		
		// searching users?
		String key = getSearchKeyword();
		if(key == null || key.equals("") || key.equals(msgs.getString("search_int")))
			key = null;
		
		// get data
		events = sm.getResourceStatsGrpByDateAndAction(baseBean.getSiteId(), key, iDate, fDate);
		setEvents(events);
	}

	private StatsManager getStatsManager() {
		return (StatsManager) ComponentManager.get(StatsManager.class.getName());
	}

	// ######################################################################################
	// ActionListener methods
	// ######################################################################################
	public String processActionSearch() {
		return "resources";
	}

	public String processActionClearSearch() {
		setSearchKeyword(msgs.getString("search_int"));
		return "resources";
	}

	public void processActionSearchChangeListener(ValueChangeEvent event) {
		String newValue = (String) event.getNewValue();
		if(!getSearchKeyword().equals(newValue)) setFirstItem(0);
	}
	
}
