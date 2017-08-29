/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.summarycalendar.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.CalendarUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;


public class PrefsBean {
	
	/** Preferences properties */
	public static String						PREFS_KEY					= "sakai:calendar:calendar-summary";
	public static String						PREFS_LAST_MODIFIED			= "lastModified";
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
	private static Logger							LOG							= LoggerFactory.getLogger(PrefsBean.class);

	/** Resource bundle */
	private transient ResourceLoader			msgs						= new ResourceLoader("calendar");
	
	private CalendarUtil calendarUtil = new CalendarUtil();
	
	/** Bean members */
	private List								viewModes					= null;
	private String								selectedViewMode			= null;
	private String								selectedHighPrColor			= null;
	private String								selectedMediumPrColor		= null;
	private String								selectedLowPrColor			= null;
	//private static List							eventTypes					= null;
	private Collection							highPriorityEvents			= null;
	private Collection							mediumPriorityEvents		= null;
	private Collection							lowPriorityEvents			= null;

	/** Private members */
	private String								message						= null;
	private Severity							messageSeverity				= null;
	private Map 								priorityColorsMap			= null;
	private Map 								priorityEventsMap			= null;

	/** Sakai Services */
	private static transient PreferencesService	M_ps						= (PreferencesService) ComponentManager.get(PreferencesService.class.getName());
	private static transient SessionManager		M_sm						= (SessionManager) ComponentManager.get(SessionManager.class.getName());
	private static transient ServerConfigurationService M_cfg				= (ServerConfigurationService) ComponentManager.get(ServerConfigurationService.class.getName());

	private static final Pattern COLOR_HEX_PATTERN = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
	
	// ######################################################################################
	// Main methods
	// ######################################################################################
	public PrefsBean(){
	}
	
	public String getInitValues() {
		// reload localized event types
		return "";
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public boolean isMessageToBeDisplayed() {
		if(message != null){
			FacesContext fc = FacesContext.getCurrentInstance();
			fc.addMessage("msg", new FacesMessage(messageSeverity, message, null));
			message = null;
			return true;
		}		
		return false;
	}
	
	private String getValidatedColorValue(String componentId) throws Exception {
        
		String value = getValueFromFacesContext(componentId).trim();
		if ("".equals(value)) {
			return value;
		}
		if (!COLOR_HEX_PATTERN.matcher(value).matches()) {
			throw new Exception("Invalid hex color code.");
		}
		return value;
	}
	
	public String update() {
		try{
			// read from FacesContext
			setSelectedViewMode(getValueFromFacesContext("prefsForm:selectViewMode"));
			setSelectedHighPriorityColor(getValidatedColorValue("prefsForm:highPriorityColor"));
			setSelectedMediumPriorityColor(getValidatedColorValue("prefsForm:mediumPriorityColor"));
			setSelectedLowPriorityColor(getValidatedColorValue("prefsForm:lowPriorityColor"));
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
			setPreferenceString(PREFS_LAST_MODIFIED, Long.toString(System.currentTimeMillis()));
			
			priorityColorsMap = null;
			priorityEventsMap = null;
		}catch(Exception e){
			// error occurred
			message = msgs.getString("prefs_not_updated");
			messageSeverity = FacesMessage.SEVERITY_FATAL;
			LOG.error("Calendar Summary: "+message, e);
			return "prefs";
		}

		// all ok
		return "calendar";
	}

	public String cancel() {
		message = null;
		priorityColorsMap = null;
		priorityEventsMap = null;
		return "calendar";
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	private void readPriorityColorsMap() {
		// priority colors (CSS properties)
		priorityColorsMap = getPreferencePriorityColors();
		selectedHighPrColor = (String) priorityColorsMap.get(PREFS_HIGHPRIORITY_COLOR);
		selectedMediumPrColor = (String) priorityColorsMap.get(PREFS_MEDIUMPRIORITY_COLOR);
		selectedLowPrColor = (String) priorityColorsMap.get(PREFS_LOWPRIORITY_COLOR);
	}
	private void readPriorityEventsMap() {
		// priority events
		priorityEventsMap = getPreferencePriorityEvents();
		highPriorityEvents = (List) priorityEventsMap.get(PREFS_HIGHPRIORITY_EVENTS);
		mediumPriorityEvents = (List) priorityEventsMap.get(PREFS_MEDIUMPRIORITY_EVENTS);
		lowPriorityEvents = (List) priorityEventsMap.get(PREFS_LOWPRIORITY_EVENTS);
	}
	
	public String getSelectedViewMode() {
		selectedViewMode = getPreferenceViewMode();
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
		if(priorityColorsMap == null)
			readPriorityColorsMap();
		return selectedHighPrColor;
	}
	public void setSelectedHighPriorityColor(String color) {
		this.selectedHighPrColor = color;
	}
	

	public String getSelectedMediumPriorityColor() {
		if(priorityColorsMap == null)
			readPriorityColorsMap();
		return selectedMediumPrColor;
	}
	public void setSelectedMediumPriorityColor(String color) {
		this.selectedMediumPrColor = color;
	}
	

	public String getSelectedLowPriorityColor() {
		if(priorityColorsMap == null)
			readPriorityColorsMap();
		return selectedLowPrColor;
	}
	public void setSelectedLowPriorityColor(String color) {
		this.selectedLowPrColor = color;
	}
	
	
	public List<SelectItem> getHighPriorityEvents(){
		if(priorityEventsMap == null)
			readPriorityEventsMap();
		return listOfEventTypesToLocalizedList(highPriorityEvents);
	}
	public void setHighPriorityEvents(List<SelectItem> events){
		this.highPriorityEvents = new ArrayList<String>();
		Iterator<SelectItem> i = events.iterator();
		while(i.hasNext()){
			SelectItem e = i.next();
			highPriorityEvents.add(e.getValue());
		}
	}
	public List<String> getSelectedHighPriorityEvents(){
		return new ArrayList<String>();
	}	
	public void setSelectedHighPriorityEvents(List<String> events){
		this.highPriorityEvents = events;
	}
	public void setSelectedHighPriorityEvents(Collection<String> events){
		this.highPriorityEvents = events;
	}
	
	
	public List<SelectItem> getMediumPriorityEvents(){
		if(priorityEventsMap == null)
			readPriorityEventsMap();
		return listOfEventTypesToLocalizedList(mediumPriorityEvents);
	}	
	public void setMediumPriorityEvents(List<SelectItem> events){
		this.mediumPriorityEvents = new ArrayList<String>();
		Iterator<SelectItem> i = events.iterator();
		while(i.hasNext()){
			SelectItem e = i.next();
			mediumPriorityEvents.add(e.getValue());
		}
	}
	public List<String> getSelectedMediumPriorityEvents(){
		return new ArrayList<String>();
	}	
	public void setSelectedMediumPriorityEvents(List<String> events){
		this.mediumPriorityEvents = events;
	}
	public void setSelectedMediumPriorityEvents(Collection<String> events){
		this.mediumPriorityEvents = events;
	}
	
	
	public List<SelectItem> getLowPriorityEvents(){
		if(priorityEventsMap == null)
			readPriorityEventsMap();
		return listOfEventTypesToLocalizedList(lowPriorityEvents);
	}	
	public void setLowPriorityEvents(List<SelectItem> events){
		this.lowPriorityEvents = new ArrayList<String>();
		Iterator<SelectItem> i = events.iterator();
		while(i.hasNext()){
			SelectItem e = i.next();
			lowPriorityEvents.add(e.getValue());
		}
	}
	public List<String> getSelectedLowPriorityEvents(){
		return new ArrayList<String>();
	}	
	public void setSelectedLowPriorityEvents(List<String> events){
		this.lowPriorityEvents = events;
	}
	public void setSelectedLowPriorityEvents(Collection<String> events){
		this.lowPriorityEvents = events;
	}
	

	// ######################################################################################
	// Preferences methods
	// ######################################################################################
	public static long getPreferenceLastModified() {
		Long lastModified = 0l;
		String value = getPreferenceString(PREFS_LAST_MODIFIED);
		
		if(value != null){
			try{
				lastModified = Long.parseLong(value);
			}catch(NumberFormatException e){
				lastModified = 0l;
			}
		}
		
		return lastModified;
	}
	
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
		temp.addAll(CalendarEventType.getTypes());		
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
		//Collections.sort(h);
		//Collections.sort(m);
		//Collections.sort(l);
		
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
	
	private List<SelectItem> listOfEventTypesToLocalizedList(Collection<String> l) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		if(l == null)
			return list;
		Iterator<String> lI = l.iterator();
		while(lI.hasNext()){
			String eventType = lI.next();
			SelectItem item = new SelectItem(eventType, calendarUtil.getLocalizedEventType(eventType));
			list.add(item);
		}
		return list;
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
