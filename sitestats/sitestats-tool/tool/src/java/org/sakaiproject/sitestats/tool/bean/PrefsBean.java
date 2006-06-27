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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.jsf.InitializableBean;



/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class PrefsBean extends InitializableBean implements Serializable {
	private static final long	serialVersionUID			= -4463424105673377369L;
	protected ResourceBundle	msgs						= ResourceBundle.getBundle("org.sakaiproject.sitestats.tool.bundle.Messages");
	private static Log			LOG							= LogFactory.getLog(PrefsBean.class);

	/** Bean members */
	private Map					eventNames					= null;
	private List				availableEPEvents			= null;
	private List				availableOPEvents			= null;
	private String[]			configuredEPEvents			= null;
	private String[]			configuredOPEvents			= null;
	private String[]			tempConfiguredEPEvents		= null;
	private String[]			tempConfiguredOPEvents		= null;

	/** Statistics Manager object */
	private String				message;
	private boolean				updatedEvents				= false;
	private boolean				noEPEventsSelected			= false;
	private boolean				noOPEventsSelected			= false;
	private BaseBean			baseBean				= null;
	private StatsManager		sm							= getStatsManager();
	private Collator			collator					= Collator.getInstance();

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("PrefsBean.init()");
		
		initializeBaseBean();
		
		if(baseBean.isAllowed()){
			eventNames = getEventNames();
			availableEPEvents = getAvailableEPEvents();
			availableOPEvents = getAvailableOPEvents();
			configuredEPEvents = getConfiguredEPEvents();
			configuredOPEvents = getConfiguredOPEvents();
	
			// show message
			if(updatedEvents){
				FacesContext fc = FacesContext.getCurrentInstance();
				fc.addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
				updatedEvents = false;
			}else if(noEPEventsSelected || noOPEventsSelected){
				FacesContext fc = FacesContext.getCurrentInstance();
				fc.addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
				noEPEventsSelected = false;
				noOPEventsSelected = false;
			}
			
			Collections.sort(availableEPEvents, getComboItemsComparator(collator));
			Collections.sort(availableOPEvents, getComboItemsComparator(collator));
		}
	}
	
	private void initializeBaseBean(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		baseBean = (BaseBean) facesContext.getApplication()
			.createValueBinding("#{BaseBean}")
			.getValue(facesContext);
	}

	private StatsManager getStatsManager() {
		return (StatsManager) ComponentManager.get(StatsManager.class.getName());
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public String update() {
		boolean updatedEP = updateEP();
		boolean updatedOP = updateOP();
		if(updatedEP || updatedOP){
			updatedEvents = true;
			message = msgs.getString("prefs_updated");
		}
		return "prefs";
	}
	
	private boolean updateEP(){
		int size = tempConfiguredEPEvents.length;
		if(size == 0){
			noEPEventsSelected = true;
			message = msgs.getString("prefs_noeventsselected");
			tempConfiguredEPEvents = null;
			return false;
		}else{
			List newPrefs = new ArrayList();
			configuredEPEvents = new String[size];
			for(int i = 0; i < size; i++){
				configuredEPEvents[i] = tempConfiguredEPEvents[i];
				newPrefs.add(tempConfiguredEPEvents[i]);
			}
			sm.setSiteConfiguredEventIds(baseBean.getSiteId(), newPrefs, StatsManager.PREFS_EVENTS_PAGE);
			tempConfiguredEPEvents = null;
			return true;
		}
	}
	
	private boolean updateOP(){
		int size = tempConfiguredOPEvents.length;
		if(size == 0){
			noOPEventsSelected = true;
			message = msgs.getString("prefs_noeventsselected");
			tempConfiguredOPEvents = null;
			return false;
		}else{
			List newPrefs = new ArrayList();
			configuredOPEvents = new String[size];
			for(int i = 0; i < size; i++){
				configuredOPEvents[i] = tempConfiguredOPEvents[i];
				newPrefs.add(tempConfiguredOPEvents[i]);
			}
			sm.setSiteConfiguredEventIds(baseBean.getSiteId(), newPrefs, StatsManager.PREFS_OVERVIEW_PAGE);
			tempConfiguredOPEvents = null;
			return true;
		}
	}

	public String cancel() {
		tempConfiguredEPEvents = null;
		tempConfiguredOPEvents = null;
		return "prefs";
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public List getAvailableEPEvents() {
		if(availableEPEvents == null){
			availableEPEvents = new ArrayList();
			List l = sm.getRegisteredEventIds();
			Iterator i = l.iterator();
			while (i.hasNext()){
				String eId = (String) i.next();
				availableEPEvents.add(new SelectItem(eId, (String) eventNames.get(eId)));
			}
		}
		return availableEPEvents;
	}
	
	public List getAvailableOPEvents() {
		if(availableOPEvents == null){
			availableOPEvents = new ArrayList();
			List l = sm.getDefaultEventIdsForActivity();
			Iterator i = l.iterator();
			while (i.hasNext()){
				String eId = (String) i.next();
				availableOPEvents.add(new SelectItem(eId, (String) eventNames.get(eId)));
			}
		}
		return availableOPEvents;
	}

	public String[] getConfiguredEPEvents() {
		if(tempConfiguredEPEvents == null){
			List l = sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_EVENTS_PAGE);
			int size = l.size();
			if(size == 0){
				l = sm.getRegisteredEventIds();
				size = l.size();
			}
			configuredEPEvents = new String[size];
			tempConfiguredEPEvents = new String[size];
			Iterator i = l.iterator();
			int n = 0;
			while (i.hasNext()){
				configuredEPEvents[n] = (String) i.next();
				tempConfiguredEPEvents[n] = configuredEPEvents[n];
				n++;
			}
		}
		return tempConfiguredEPEvents;
	}

	public void setConfiguredEPEvents(String[] events) {
		this.tempConfiguredEPEvents = events;
	}

	public String[] getConfiguredOPEvents() {
		if(tempConfiguredOPEvents == null){
			List l = sm.getSiteConfiguredEventIds(baseBean.getSiteId(), StatsManager.PREFS_OVERVIEW_PAGE);
			int size = l.size();
			if(size == 0){
				l = sm.getDefaultEventIdsForActivity();
				size = l.size();
			}
			configuredOPEvents = new String[size];
			tempConfiguredOPEvents = new String[size];
			Iterator i = l.iterator();
			int n = 0;
			while (i.hasNext()){
				configuredOPEvents[n] = (String) i.next();
				tempConfiguredOPEvents[n] = configuredOPEvents[n];
				n++;
			}
		}
		return tempConfiguredOPEvents;
	}

	public void setConfiguredOPEvents(String[] events) {
		this.tempConfiguredOPEvents = events;
	}

	public Map getEventNames() {
		if(eventNames == null) eventNames = sm.getEventNameMap();
		return eventNames;
	}

	public static final Comparator getComboItemsComparator(final Collator collator) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof SelectItem && o2 instanceof SelectItem){
					SelectItem r1 = (SelectItem) o1;
					SelectItem r2 = (SelectItem) o2;
					return collator.compare(r1.getLabel().toLowerCase(), r2.getLabel().toLowerCase());
				}
				return 0;
			}
		};
	}
}
