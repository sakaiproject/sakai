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
package org.sakaiproject.tool.summarycalendar.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.summarycalendar.jsf.InitializableBean;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;


public class PrefsBean extends InitializableBean implements Serializable {
	private static final long					serialVersionUID			= -6671159843904531584L;

	/** Preferences properties */
	public static String						PREFS_KEY					= "sakai:calendar:calendar-summary";
	public static String						PREFS_SET					= "set";
	public static String						PREFS_VIEW_MODE				= "viewMode";
	public static String						PREFS_HIGHPRIORITY_COLOR	= "highPriorityColor";
	public static String						PREFS_MEDIUMPRIORITY_COLOR	= "mediumPriorityColor";
	public static String						PREFS_LOWPRIORITY_COLOR		= "lowPriorityColor";
	public static String						PREFS_HIGHPRIORITY_EVENTS	= "highPriorityEvents";
	public static String						PREFS_MEDIUMPRIORITY_EVENTS	= "mediumPriorityEvents";
	public static String						PREFS_LOWPRIORITY_EVENTS	= "lowPriorityEvents";
	
	/** sakai.properties default values */
	public static String						SAKPROP_BASE				= "calendarSummary.";

	/** Our log (commons). */
	private static Log							LOG							= LogFactory.getLog(PrefsBean.class);

	/** Resource bundle */
	private transient ResourceLoader			msgs						= new ResourceLoader("org.sakaiproject.tool.summarycalendar.bundle.Messages");
	
	/** Bean members */
	private List								viewModes					= null;
	private String								selectedViewMode			= null;
	private String								selectedHighPrColor			= null;
	private String								selectedMediumPrColor		= null;
	private String								selectedLowPrColor			= null;
	private static List							eventTypes					= null;
	private Collection							highPriorityEvents			= null;
	private Collection							mediumPriorityEvents		= null;
	private Collection							lowPriorityEvents			= null;

	/** Private members */
	private String								message						= null;
	private Severity							messageSeverity				= null;	

	/** Sakai Services */
	private static transient PreferencesService	M_ps						= (PreferencesService) ComponentManager.get(PreferencesService.class.getName());
	private static transient SessionManager		M_sm						= (SessionManager) ComponentManager.get(SessionManager.class.getName());
	private static transient ServerConfigurationService M_cfg				= (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class.getName());

	static {
		eventTypes = new ArrayList();
		eventTypes.add("Academic Calendar");
		eventTypes.add("Activity");
		eventTypes.add("Cancellation");
		eventTypes.add("Class section - Discussion");
		eventTypes.add("Class section - Lab");
		eventTypes.add("Class section - Lecture");
		eventTypes.add("Class section - Small Group");
		eventTypes.add("Class session");
		eventTypes.add("Computer Session");
		eventTypes.add("Deadline");
		eventTypes.add("Exam");
		eventTypes.add("Meeting");
		eventTypes.add("Multidisciplinary Conference");
		eventTypes.add("Quiz");
		eventTypes.add("Special event");
		eventTypes.add("Web Assignment");
	}
	
	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("PrefsBean.init()");
		
		// Faces message, if any 
		if(message != null){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("msg", new FacesMessage(messageSeverity, message, null));
			message = null;
		}
		
		// Read preferences/default values
		// calendar view mode
		selectedViewMode = getPreferenceViewMode();
		
		// priority colors (CSS properties)
		Map priorityColorsMap = getPreferencePriorityColors();
		selectedHighPrColor = (String) priorityColorsMap.get(PREFS_HIGHPRIORITY_COLOR);
		selectedMediumPrColor = (String) priorityColorsMap.get(PREFS_MEDIUMPRIORITY_COLOR);
		selectedLowPrColor = (String) priorityColorsMap.get(PREFS_LOWPRIORITY_COLOR);
		
		// priority events
		Map priorityEventsMap = getPreferencePriorityEvents();
		highPriorityEvents = (List) priorityEventsMap.get(PREFS_HIGHPRIORITY_EVENTS);
		mediumPriorityEvents = (List) priorityEventsMap.get(PREFS_MEDIUMPRIORITY_EVENTS);
		lowPriorityEvents = (List) priorityEventsMap.get(PREFS_LOWPRIORITY_EVENTS);
		
		
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public String update() {
		try{
			// read from FacesContext
			setSelectedViewMode(getValueFromFacesContext("prefsForm:selectViewMode"));
			setSelectedHighPriorityColor(getValueFromFacesContext("prefsForm:highPriorityColor"));
			setSelectedMediumPriorityColor(getValueFromFacesContext("prefsForm:mediumPriorityColor"));
			setSelectedLowPriorityColor(getValueFromFacesContext("prefsForm:lowPriorityColor"));
			setSelectedHighPriorityEvents(getValuesFromFacesContext("prefsForm:highPriorityEvents"));
			setSelectedMediumPriorityEvents(getValuesFromFacesContext("prefsForm:mediumPriorityEvents"));
			setSelectedLowPriorityEvents(getValuesFromFacesContext("prefsForm:lowPriorityEvents"));
			
			// update User Preferences
			setPreferenceString(PREFS_VIEW_MODE, selectedViewMode);
			setPreferenceString(PREFS_HIGHPRIORITY_COLOR, selectedHighPrColor);
			setPreferenceString(PREFS_MEDIUMPRIORITY_COLOR, selectedMediumPrColor);
			setPreferenceString(PREFS_LOWPRIORITY_COLOR, selectedLowPrColor);			
			clearPreferenceList(PREFS_HIGHPRIORITY_EVENTS);
			clearPreferenceList(PREFS_MEDIUMPRIORITY_EVENTS);
			clearPreferenceList(PREFS_LOWPRIORITY_EVENTS);	
			setPreferenceList(PREFS_HIGHPRIORITY_EVENTS, highPriorityEvents);
			setPreferenceList(PREFS_MEDIUMPRIORITY_EVENTS, mediumPriorityEvents);
			setPreferenceList(PREFS_LOWPRIORITY_EVENTS, lowPriorityEvents);
		}catch(Exception e){
			// error occurred
			message = msgs.getString("prefs_not_updated");
			messageSeverity = FacesMessage.SEVERITY_FATAL;
			LOG.error("Calendar Summary: "+message, e);
			return "prefs";
		}

		// all ok
		FacesContext context = FacesContext.getCurrentInstance();
	    ValueBinding vb = context.getApplication().createValueBinding("#{CalBean.readPrefs}");
	    vb.setValue(context, "true");
		return "calendar";
	}

	public String cancel() {
		message = null;
		return "calendar";
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public String getSelectedViewMode() {
		return selectedViewMode;
	}
	public void setSelectedViewMode(String selectedViewMode) {
		this.selectedViewMode = selectedViewMode;
	}
	public List getViewModes() {
		viewModes = new ArrayList();
		viewModes.add(new SelectItem(CalendarBean.MODE_MONTHVIEW, msgs.getString("month_view")));
		viewModes.add(new SelectItem(CalendarBean.MODE_WEEKVIEW, msgs.getString("week_view")));
		return viewModes;
	}

	
	public String getSelectedHighPriorityColor() {
		return selectedHighPrColor;
	}
	public void setSelectedHighPriorityColor(String color) {
		this.selectedHighPrColor = color;
	}
	

	public String getSelectedMediumPriorityColor() {
		return selectedMediumPrColor;
	}
	public void setSelectedMediumPriorityColor(String color) {
		this.selectedMediumPrColor = color;
	}
	

	public String getSelectedLowPriorityColor() {
		return selectedLowPrColor;
	}
	public void setSelectedLowPriorityColor(String color) {
		this.selectedLowPrColor = color;
	}
	
	
	public List getHighPriorityEvents(){
		return listOfStringsToListOfSelectItem(highPriorityEvents);
	}	
	public void setHighPriorityEvents(List events){
		this.highPriorityEvents = events;
	}
	public List getSelectedHighPriorityEvents(){
		return new ArrayList();
	}	
	public void setSelectedHighPriorityEvents(Collection events){
		this.highPriorityEvents = events;
	}
	public void setSelectedHighPriorityEvents(List events){
		this.highPriorityEvents = events;
	}
	
	
	public List getMediumPriorityEvents(){
		return listOfStringsToListOfSelectItem(mediumPriorityEvents);
	}	
	public void setMediumPriorityEvents(List events){
		this.mediumPriorityEvents = events;
	}
	public List getSelectedMediumPriorityEvents(){
		return new ArrayList();
	}	
	public void setSelectedMediumPriorityEvents(Collection events){
		this.mediumPriorityEvents = events;
	}
	public void setSelectedMediumPriorityEvents(List events){
		this.mediumPriorityEvents = events;
	}
	
	
	public List getLowPriorityEvents(){
		return listOfStringsToListOfSelectItem(lowPriorityEvents);
	}	
	public void setLowPriorityEvents(List events){
		this.lowPriorityEvents = events;
	}
	public List getSelectedLowPriorityEvents(){
		return new ArrayList();
	}	
	public void setSelectedLowPriorityEvents(Collection events){
		this.lowPriorityEvents = events;
	}
	public void setSelectedLowPriorityEvents(List events){
		this.lowPriorityEvents = events;
	}
	
	
	
	public String getMoveUpStr() {
		return msgs.getString("prefs_move_up");
	}	
	public void setMoveUpStr(String str){		
	}	
	public String getMoveDownStr() {
		return msgs.getString("prefs_move_down");
	}	
	public void setMoveDownStr(String str){		
	}
	

	// ######################################################################################
	// Preferences methods
	// ######################################################################################
	public static String getPreferenceViewMode() {
		String value = getPreferenceString(PREFS_VIEW_MODE);
		
		// preferences not set, read from sakai.properties
		if(value == null){
			value = getDefaultStringFromSakaiProperties(PREFS_VIEW_MODE);
		}
		
		// sakai.properties default not set, using 'month'
		if(value == null){			
			return CalendarBean.MODE_MONTHVIEW;
		}else
			return value;
	}
	
	public static Map getPreferencePriorityColors() {
		HashMap map = new HashMap();		
		String h = getPreferenceString(PREFS_HIGHPRIORITY_COLOR);
		String m = getPreferenceString(PREFS_MEDIUMPRIORITY_COLOR);
		String l = getPreferenceString(PREFS_LOWPRIORITY_COLOR);
		
		// preferences not set, read from sakai.properties		
		if(h == null && m == null && l == null){
			h = getDefaultStringFromSakaiProperties(PREFS_HIGHPRIORITY_COLOR);
			m = getDefaultStringFromSakaiProperties(PREFS_MEDIUMPRIORITY_COLOR);
			l = getDefaultStringFromSakaiProperties(PREFS_LOWPRIORITY_COLOR);
		}
		
		map.put(PREFS_HIGHPRIORITY_COLOR, h);		
		map.put(PREFS_MEDIUMPRIORITY_COLOR, m);		
		map.put(PREFS_LOWPRIORITY_COLOR, l);		
		return map;
	}
	
	public static Map getPreferencePriorityEvents() {
		HashMap map = new HashMap();		
		List h = getPreferenceList(PREFS_HIGHPRIORITY_EVENTS);
		List m = getPreferenceList(PREFS_MEDIUMPRIORITY_EVENTS);
		List l = getPreferenceList(PREFS_LOWPRIORITY_EVENTS);

		// preferences not set, read from sakai.properties		
		if(h == null && m == null && l == null){
			h = getDefaultListFromSakaiProperties(PREFS_HIGHPRIORITY_EVENTS);
			m = getDefaultListFromSakaiProperties(PREFS_MEDIUMPRIORITY_EVENTS);
			l = getDefaultListFromSakaiProperties(PREFS_LOWPRIORITY_EVENTS);
		}
		
		if(h == null) h = new ArrayList();
		if(m == null) m = new ArrayList();
		if(l == null) l = new ArrayList();

		// make sure all available events are listed
		// no pass-by-reference in java, must use a work-around...
		List temp = new ArrayList();
		temp.addAll(eventTypes);		
		PairList lists = new PairList(h, temp);
		lists = validateEventList(lists);
		h = lists.dataList;
		
		lists.dataList = m;
		lists = validateEventList(lists);
		m = lists.dataList;
		
		lists.dataList = l;
		lists = validateEventList(lists);
		l = lists.dataList;
		
		// add all non-specified events to low priority list
		l.addAll(lists.tempList);		

		// sort lists
		Collections.sort(h);
		Collections.sort(m);
		Collections.sort(l);
		
		map.put(PREFS_HIGHPRIORITY_EVENTS, h);		
		map.put(PREFS_MEDIUMPRIORITY_EVENTS, m);		
		map.put(PREFS_LOWPRIORITY_EVENTS, l);		
		return map;
	}
	

	/**
	 * Get the current user preference value. First attempt Preferences, then defaults from sakai.properties.
	 * @param name The property name.
	 * @return The preference value or null if not set.
	 */
	private static String getPreferenceString(String name) {
		Preferences prefs = M_ps.getPreferences(M_sm.getCurrentSessionUserId());
		ResourceProperties rp = prefs.getProperties(PREFS_KEY);
		String value = rp.getProperty(name);
		return value;
	}

	/**
	 * Get the current user preference list value. First attempt Preferences, then defaults from sakai.properties.
	 * @param name The property name.
	 * @return The preference list value or null if not set.
	 */
	private static List getPreferenceList(String name) {
		Preferences prefs = M_ps.getPreferences(M_sm.getCurrentSessionUserId());
		ResourceProperties rp = prefs.getProperties(PREFS_KEY);
		List l = rp.getPropertyList(name);
		return l;
	}

	private static void setPreferenceString(String name, String value) throws Exception {
		PreferencesEdit prefsEdit = null;
		String userId = M_sm.getCurrentSessionUserId();
		try{
			prefsEdit = M_ps.edit(userId);
		}catch(IdUnusedException e){
			prefsEdit = M_ps.add(userId);
		}
		try{
			ResourcePropertiesEdit props = prefsEdit.getPropertiesEdit(PREFS_KEY);

			if(value == null){
				props.removeProperty(name);
			}else{
				props.addProperty(name, value.toString());
			}
		}catch(Exception e){	
			if(prefsEdit != null)
				M_ps.cancel(prefsEdit);
			throw e;
		}
		M_ps.commit(prefsEdit);
	}

	private static void setPreferenceList(String name, Collection values) throws Exception {
		PreferencesEdit prefsEdit = null;
		String userId = M_sm.getCurrentSessionUserId();
		try{
			prefsEdit = M_ps.edit(userId);
		}catch(IdUnusedException e){
			prefsEdit = M_ps.add(userId);
		}
		try{
			ResourcePropertiesEdit props = prefsEdit.getPropertiesEdit(PREFS_KEY);

			if(values == null){
				props.removeProperty(name);
			}else{
				List existing = props.getPropertyList(name);
				Iterator it = values.iterator();
				while(it.hasNext()){
					String value = (String) it.next();
					if(existing == null || !existing.contains(value))
						props.addPropertyToList(name, value.toString());
				}
			}
		}catch(Exception e){
			if(prefsEdit != null)
				M_ps.cancel(prefsEdit);
			M_ps.cancel(prefsEdit);
			throw e;
		}
		M_ps.commit(prefsEdit);
	}

	private static void clearPreferenceList(String name) throws Exception {
		PreferencesEdit prefsEdit = null;
		try{
			prefsEdit = M_ps.edit(M_sm.getCurrentSessionUserId());
			ResourcePropertiesEdit props = prefsEdit.getPropertiesEdit(PREFS_KEY);

			props.removeProperty(name);
		}catch(Exception e){
			M_ps.cancel(prefsEdit);
			throw e;
		}
		M_ps.commit(prefsEdit);
	}
	
	private static String getDefaultStringFromSakaiProperties(String name) {
		String value = M_cfg.getString(SAKPROP_BASE + name);
		return value;
	}
	
	private static List getDefaultListFromSakaiProperties(String name) {
		List l = new ArrayList();
		String[] valuesStr = M_cfg.getStrings(SAKPROP_BASE + name);
		if(valuesStr == null)
			return l;
		else{
			for(int i=0; i<valuesStr.length; i++){
				l.add(valuesStr[i]);
			}
		}
		return l;
	}

	// ######################################################################################
	// Util methods
	// ######################################################################################
	protected Set getValuesFromFacesContext(String componentId) {
		Set values = new HashSet();
		String[] str = (String[]) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get(componentId);
		if(str != null){
			for(int i = 0; i < str.length; i++){
				values.add(str[i]);
			}
		}
		return values;
	}
	
	protected String getValueFromFacesContext(String componentId) {
		String[] str = (String[]) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterValuesMap().get(componentId);
		return str[0];
	}
	
	private List listOfStringsToListOfSelectItem(Collection l) {
		List losi = new ArrayList();
		if(l == null)
			return losi;
		Iterator los = l.iterator();
		while(los.hasNext()){
			String s = (String) los.next();
			losi.add(new SelectItem(s));
		}
		return losi;
	}

	/**
	 * Foreach 'list' entry A, remove it from 'temp'. If A doesn't exist in 'temp', remove it from 'list'. 
	 * @param list A event priority List.
	 * @param temp A List with all events.
	 */
	private static PairList validateEventList(PairList lists) {
		List temp = lists.tempList;
		List list = lists.dataList;
		if(list == null){
			return lists;
		}
		List toRemoveFromList = new ArrayList();
		Iterator iL = list.iterator();
		while(iL.hasNext()){
			Object e = iL.next();
			if(temp.contains(e))
				temp.remove(e);
			else
				toRemoveFromList.add(e);
		}
		
		Iterator iR = toRemoveFromList.iterator();
		while(iR.hasNext()){
			Object e = iR.next();
			list.remove(e);
		}
		lists.dataList = list;
		lists.tempList = temp;
		return lists;
	}
}

/** Pair of lists */
class PairList {
	public List dataList;
	public List tempList;
	
	public PairList(List dataList, List tempList) {
		this.dataList = dataList;
		this.tempList = tempList;
	}		
}
