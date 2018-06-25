/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.user.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistration;
import org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Web;

/**
 * UserPrefsTool is the Sakai end-user tool to view and edit one's preferences.
 */
@Slf4j
public class UserPrefsTool
{

	/** * Resource bundle messages */
	ResourceLoader msgs = new ResourceLoader("user-tool-prefs");

	/** The string to get whether privacy status should be visible */
	private static final String ENABLE_PRIVACY_STATUS = "enable.privacy.status";

	/** Should research/collab specific preferences (no syllabus) be displayed */
	private static final String PREFS_RESEARCH = "prefs.research.collab";
	
	public static final String PREFS_EXPAND = "prefs.expand";
	private static final String PREFS_EXPAND_TRUE = "1";
	private static final String PREFS_EXPAND_FALSE = "0";

	/**
	 * Represents a name value pair in a keyed preferences set.
	 */
	public class KeyNameValue
	{
		/** Is this value a list?. */
		protected boolean m_isList = false;

		/** The key. */
		protected String m_key = null;

		/** The name. */
		protected String m_name = null;

		/** The original is this value a list?. */
		protected boolean m_origIsList = false;

		/** The original key. */
		protected String m_origKey = null;

		/** The original name. */
		protected String m_origName = null;

		/** The original value. */
		protected String m_origValue = null;

		/** The value. */
		protected String m_value = null;

		public KeyNameValue(String key, String name, String value, boolean isList)
		{
			m_key = key;
			m_origKey = key;
			m_name = name;
			m_origName = name;
			m_value = value;
			m_origValue = value;
			m_isList = isList;
			m_origIsList = isList;
		}

		public String getKey()
		{
			return m_key;
		}

		public String getName()
		{
			return m_name;
		}

		public String getOrigKey()
		{
			return m_origKey;
		}

		public String getOrigName()
		{
			return m_origName;
		}

		public String getOrigValue()
		{
			return m_origValue;
		}

		public String getValue()
		{
			return m_value;
		}

		public boolean isChanged()
		{
			return ((!m_name.equals(m_origName)) || (!m_value.equals(m_origValue)) || (!m_key.equals(m_origKey)) || (m_isList != m_origIsList));
		}

		public boolean isList()
		{
			return m_isList;
		}

		public boolean origIsList()
		{
			return m_origIsList;
		}

		public void setKey(String value)
		{
			if (!m_key.equals(value))
			{
				m_key = value;
			}
		}

		public void setList(boolean b)
		{
			m_isList = b;
		}

		public void setName(String value)
		{
			if (!m_name.equals(value))
			{
				m_name = value;
			}
		}

		public void setValue(String value)
		{
			if (!m_value.equals(value))
			{
				m_value = value;
			}
		}
	}

	/** The PreferencesEdit being worked on. */
	protected PreferencesEdit m_edit = null;

	/** Preferences service (injected dependency) */
	protected PreferencesService m_preferencesService = null;
	
	protected UserNotificationPreferencesRegistrationService m_userNotificationPreferencesRegistrationService = null;

	/** Session manager (injected dependency) */
	protected SessionManager m_sessionManager = null;

	/** The PreferencesEdit in KeyNameValue collection form. */
	protected Collection m_stuff = null;

	// /** The user id (from the end user) to edit. */
	// protected String m_userId = null;
	/** For display and selection of Items in JSF-- edit.jsp */
	private List prefExcludeItems = new ArrayList();

	private List prefOrderItems = new ArrayList();

	private List<SelectItem> prefTimeZones = new ArrayList<>();

	private List<SelectItem> prefLocales = new ArrayList<SelectItem>();

	// SAK-23895
       	private String prefTabLabel = null;
       	private int DEFAULT_TAB_LABEL = 1;

	private String[] selectedExcludeItems;

	private String[] selectedOrderItems;

	private String prefTabString = null;
	private String prefHiddenString = null;

	private String[] tablist;

	private int noti_selection, tab_selection, timezone_selection, language_selection, privacy_selection, hidden_selection, editor_selection, j;

	private String hiddenSitesInput = null;

	//The preference list names
	private String Notification="prefs_noti_title";
	private String Timezone="prefs_timezone_title";
	private String Language="prefs_lang_title";
	private String Privacy="prefs_privacy_title";
	private String Hidden="prefs_hidden_title";
	private String Editor="prefs_editor_title";
	
	private boolean refreshMode=false;

	protected final static String EXCLUDE_SITE_LISTS = "exclude";

	protected final static String ORDER_SITE_LISTS = "order";

	protected final static String TAB_LABEL_PREF = "tab:label";

	protected final static String EDITOR_TYPE = "editor:type";

	protected boolean isNewUser = false;

	// user's currently selected time zone
	private TimeZone m_timeZone = null;

	// user's currently selected editor type 
	private String m_editorType = null;

	// user's currently selected regional language locale
	private Locale m_locale = null;

	/** The user id retrieved from UsageSessionService */
	private String userId = "";

	private String m_TabOutcome = "tab";
	
	private Map<String, Integer> m_sortedTypes = new HashMap<String, Integer>();
	private List<DecoratedNotificationPreference> m_registereddNotificationItems = new ArrayList<DecoratedNotificationPreference>();	
	private List<Site> m_sites = new ArrayList<Site>();

	// SAK-23895
	private boolean prefShowTabLabelOption = true;
	
	// //////////////////////////////// PROPERTY GETTER AND SETTER ////////////////////////////////////////////

	public boolean isPrefShowTabLabelOption() {
	    return prefShowTabLabelOption;
	}

	public void setPrefShowTabLabelOption(boolean prefShowTabLabelOption) {
	    this.prefShowTabLabelOption = prefShowTabLabelOption;
	}

	/**
	 * @return Returns the prefExcludeItems.
	 */
	public List getPrefExcludeItems()
	{	
		return prefExcludeItems;
	}

	/**
	 * @param prefExcludeItems
	 *        The prefExcludeItems to set.
	 */
	public void setPrefExcludeItems(List prefExcludeItems)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setPrefExcludeItems(List " + prefExcludeItems + ")");
		}

		this.prefExcludeItems = prefExcludeItems;
	}

	/**
	 * @return Returns the prefOrderItems.
	 */
	public List getPrefOrderItems()
	{
		return prefOrderItems;
	}

	/**
	 * @param prefOrderItems
	 *        The prefOrderItems to set.
	 */
	public void setPrefOrderItems(List prefOrderItems)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setPrefOrderItems(List " + prefOrderItems + ")");
		}

		this.prefOrderItems = prefOrderItems;
	}

	/**
	 * @return Returns the prefHiddenItems.
	 */
	public List getPrefHiddenItems()
	{	
		return prefExcludeItems;
	}

	public String getPrefTabString()
	{
		return "";
	}

	public void setPrefTabString(String inp)
	{
		inp = inp.trim();
		prefTabString = inp;
		if ( inp.length() < 1 ) prefTabString = null;
		return;
	}

	public String getPrefHiddenString()
	{
		return "";
	}

	public void setPrefHiddenString(String inp)
	{
		inp = inp.trim();
		prefHiddenString = inp;
		if ( inp.length() < 1 ) prefHiddenString = null;
		return;
	}

	/**
	 * @return Returns the prefTimeZones.
	 */
	public List<SelectItem> getPrefTimeZones()
	{
		if (prefTimeZones.size() == 0)
		{
			String[] timeZoneArray = TimeZone.getAvailableIDs();
			Arrays.sort(timeZoneArray);
			for (int i = 0; i < timeZoneArray.length; i++) {
				String tzt = timeZoneArray[i];
				if (StringUtils.contains(tzt, '/') && StringUtils.indexOf(tzt, "SystemV/") != 0) {
					String id = tzt;
					String name = tzt;
					if (StringUtils.contains(tzt, '_')) {
						name = StringUtils.replace(tzt, "_", " ");
					}
					prefTimeZones.add(new SelectItem(id, name));
				}
			}
		}

		return prefTimeZones;
	}

	/**
	 * @param prefTimeZones
	 *        The prefTimeZones to set.
	 */
	public void setPrefTimeZones(List<SelectItem> prefTimeZones)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setPrefTimeZones(List " + prefTimeZones + ")");
		}

		this.prefTimeZones = prefTimeZones;
	}

	/**
	 * @return Returns the prefLocales
	 */
	public List<SelectItem> getPrefLocales()
	{
		// Initialize list of supported locales, if necessary
		if (prefLocales.isEmpty())
		{
		    org.sakaiproject.component.api.ServerConfigurationService scs = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get(org.sakaiproject.component.api.ServerConfigurationService.class);
		    Locale[] localeArray = scs.getSakaiLocales();
			for (int i = 0; i < localeArray.length; i++)
			{
				if (i == 0 || !localeArray[i].equals(localeArray[i - 1])) {
					prefLocales.add(new SelectItem(localeArray[i].toString(), msgs.getLocaleDisplayName(localeArray[i])));
				}
			}
		}

		return prefLocales;
	}

	/**
	 * @param prefLocales
	 *        The prefLocales to set.
	 */
	public void setPrefLocales(List prefLocales)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setPrefLocales(List " + prefLocales + ")");
		}

		this.prefLocales = prefLocales;
	}

	/**
	 * @return Returns the selectedExcludeItems.
	 */
	public String[] getSelectedExcludeItems()
	{
		return selectedExcludeItems;
	}

	/**
	 * @param selectedExcludeItems
	 *        The selectedExcludeItems to set.
	 */
	public void setSelectedExcludeItems(String[] selectedExcludeItems)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setSelectedExcludeItems(String[] " + Arrays.toString(selectedExcludeItems) + ")");
		}

		this.selectedExcludeItems = selectedExcludeItems;
	}

	/**
	 * @return Returns the selectedOrderItems.
	 */
	public String[] getSelectedOrderItems()
	{
		return selectedOrderItems;
	}

	/**
	 * @param selectedOrderItems
	 *        The selectedOrderItems to set.
	 */
	public void setSelectedOrderItems(String[] selectedOrderItems)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setSelectedOrderItems(String[] " + Arrays.toString(selectedOrderItems) + ")");
		}

		this.selectedOrderItems = selectedOrderItems;
	}

	/**
	 * @return Returns the user's selected TimeZone ID
	 */
	public String getSelectedTimeZone()
	{
		if (m_timeZone != null) return m_timeZone.getID();

		Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
		ResourceProperties props = prefs.getProperties(TimeService.APPLICATION_ID);
		String timeZone = props.getProperty(TimeService.TIMEZONE_KEY);

		if (hasValue(timeZone))
			m_timeZone = TimeZone.getTimeZone(timeZone);
		else
			m_timeZone = TimeZone.getDefault();

		return m_timeZone.getID();
	}

	/**
	 * @return Returns the user's selected Editor Type
	 */
	public String getSelectedEditorType()
	{
		if (m_editorType != null) return m_editorType;

		Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
		ResourceProperties props = prefs.getProperties(PreferencesService.EDITOR_PREFS_KEY);
		String editorType = props.getProperty(PreferencesService.EDITOR_PREFS_TYPE);

		if (hasValue(editorType))
			m_editorType = editorType;
		else
			m_editorType = "auto";

		return m_editorType;
	}


	/**
	 * @param selectedTimeZone
	 *        The selectedTimeZone to set.
	 */
	public void setSelectedTimeZone(String selectedTimeZone)
	{
		if (selectedTimeZone != null)
			m_timeZone = TimeZone.getTimeZone(selectedTimeZone);
		else
			log.warn(this + "setSelctedTimeZone() has null TimeZone");
	}

	/**
	 * @param selectedTimeZone
	 *        The selectedTimeZone to set.
	 */
	public void setSelectedEditorType(String selectedEditorType)
	{
		if (selectedEditorType != null)
			m_editorType = selectedEditorType;
		else
			log.warn(this + "setSelectedEditorType() has null Editor");
	}


	/**
	 * @return Returns the user's selected Locale ID
	 */
	private Locale getSelectedLocale()
	{
		if (m_locale != null) return m_locale;

		Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
		ResourceProperties props = prefs.getProperties(ResourceLoader.APPLICATION_ID);
		String prefLocale = props.getProperty(ResourceLoader.LOCALE_KEY);

		if (hasValue(prefLocale)) {
		    org.sakaiproject.component.api.ServerConfigurationService scs = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get(org.sakaiproject.component.api.ServerConfigurationService.class);
			m_locale = scs.getLocaleFromString(prefLocale);
		} else {
			m_locale = Locale.getDefault();
		}

		return m_locale;
	}

	/**
	 * @return Returns the user's selected Locale ID
	 */
	public String getSelectedLocaleName()
	{
		return getSelectedLocale().getDisplayName();
	}

	/**
	 * @return Returns the user's selected Locale ID
	 */
	public String getSelectedLocaleString()
	{
		return getSelectedLocale().toString();
	}

	/**
	 * @param selectedLocale
	 *        The selectedLocale to set.
	 */
	public void setSelectedLocaleString(String selectedLocale)
	{
		if (selectedLocale != null) {
		    org.sakaiproject.component.api.ServerConfigurationService scs = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager.get(org.sakaiproject.component.api.ServerConfigurationService.class);
		    m_locale = scs.getLocaleFromString(selectedLocale);
		}
	}

	/**
	 * @return Returns the userId.
	 */
	public String getUserId()
	{
		return m_sessionManager.getCurrentSessionUserId();
	}

	/**
	 * @param userId
	 *        The userId to set.
	 */
	public void setUserId(String userId)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setUserId(String " + userId + ")");
		}
		this.userId = userId;
	}

	/**
	 * @param mgr
	 *        The preferences service.
	 */
	public void setPreferencesService(PreferencesService mgr)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setPreferencesService(PreferencesService " + mgr + ")");
		}

		m_preferencesService = mgr;
	}

	public void setUserNotificationPreferencesRegistrationService(
			UserNotificationPreferencesRegistrationService userNotificationPreferencesRegistrationService) {
		if (log.isDebugEnabled())
		{
			log.debug("setUserNotificationPreferencesRegistrationService(UserNotificationPreferencesRegistrationService " + userNotificationPreferencesRegistrationService + ")");
		}
		m_userNotificationPreferencesRegistrationService = userNotificationPreferencesRegistrationService;
	}

	/**
	 * @param mgr
	 *        The session manager.
	 */
	public void setSessionManager(SessionManager mgr)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setSessionManager(SessionManager " + mgr + ")");
		}

		m_sessionManager = mgr;
	}


	/**
	 * Init some services that are needed.  
	 * Unfortunately they were needed when the constructor was called so 
	 * injecting wasn't soon enough.
	 */
	private void initServices() {
		if (m_userNotificationPreferencesRegistrationService == null) {
			m_userNotificationPreferencesRegistrationService = (UserNotificationPreferencesRegistrationService)ComponentManager.get("org.sakaiproject.user.api.UserNotificationPreferencesRegistrationService");
		}
		
		if (m_preferencesService == null) {
			m_preferencesService = (PreferencesService)ComponentManager.get("org.sakaiproject.user.api.PreferencesService");
		}
		
		if (m_sessionManager == null) {
			m_sessionManager = (SessionManager)ComponentManager.get("org.sakaiproject.tool.api.SessionManager");
		}
	}

	// /////////////////////////////////////// CONSTRUCTOR ////////////////////////////////////////////
	/**
	 * no-arg constructor.
	 */
	public UserPrefsTool()
	{
		// do we show the option to display by site title or short description?
		boolean show_tab_label_option = ServerConfigurationService.getBoolean("preference.show.tab.label.option", true);
		setPrefShowTabLabelOption(show_tab_label_option);

		//To indicate that it is in the refresh mode
		refreshMode=true;
		String tabOrder = ServerConfigurationService.getString("preference.pages", "prefs_noti_title, prefs_timezone_title, prefs_lang_title, prefs_hidden_title, prefs_hidden_title, prefs_editor_title");
		log.debug("Setting preference.pages as " + tabOrder);

		tablist=tabOrder.split(",");

		for(int i=0; i<tablist.length; i++)
		{
			tablist[i]=tablist[i].trim();			
			if(tablist[i].equals(Notification)) noti_selection=i+1;
			else if(tablist[i].equals(Timezone)) timezone_selection=i+1;
			else if (tablist[i].equals(Language)) language_selection=i+1;
			else if (tablist[i].equals(Privacy)) privacy_selection=i+1;
			else if (tablist[i].equals(Hidden)) hidden_selection=i+1;
			else if (tablist[i].equals(Editor)) editor_selection=i+1;
			else log.warn(tablist[i] + " is not valid!!! Please fix preference.pages property in sakai.properties");
		}

		initNotificationStructures();
		log.debug("new UserPrefsTool()");
	}
	
	/**
	 * Init a bunch of stuff that we'll need for the notification preferences
	 */
	private void initNotificationStructures() {
		
		initServices();
		
		//Get my sites
		m_sites = SiteService.getSites(SelectionType.ACCESS, null, null, null,
				SortType.TITLE_ASC, null);
		
		initRegisteredNotificationItems();
		
		
	}

	public int getNoti_selection()
	{
		//Loading the data for notification in the refresh mode
		if (noti_selection==1 && refreshMode==true)
		{
			processActionNotiFrmEdit();
		}
		return noti_selection;
	}

	public int getTimezone_selection()
	{
		//Loading the data for timezone in the refresh mode
		if (timezone_selection==1 && refreshMode==true)
		{
			processActionTZFrmEdit();
		}
		
		return timezone_selection;
	}

	public int getLanguage_selection()
	{
		//Loading the data for language in the refresh mode
		if (language_selection==1 && refreshMode==true)
		{
			processActionLocFrmEdit();
		}
		return language_selection;
	}
	
	public int getPrivacy_selection()
	{
		//Loading the data for notification in the refresh mode
		if (privacy_selection==1 && refreshMode==true)
		{
			processActionPrivFrmEdit();
		}
		return privacy_selection;
	}

	public int getHidden_selection()
	{
		//Loading the data for notification in the refresh mode
		if (hidden_selection==1 && refreshMode==true)
		{
			processActionHiddenFrmEdit();
		}
		return hidden_selection;
	}
	

	public int getEditor_selection()
	{
		//Loading the data for notification in the refresh mode
		if (editor_selection==1 && refreshMode==true)
		{
			processActionHiddenFrmEdit();
		}
		return editor_selection;
	}

	public String getTabTitle()
	{
		return "tabtitle";
	}

	/**
	 * SAK-29138 - Get the site or section title for the current user for the current site.
	 * Takes into account 'portal.use.sectionTitle' sakai.property; if set to true,
	 * this method will return the title of the section the current user is enrolled
	 * in for the site (if it can be found). Otherwise, it will return the site
	 * title (default behaviour)
	 * 
	 * @param site the site in question
	 * @param truncate whether or not to truncate the site title for display purposes
	 * @return the site or section title
	 */
	public static String getUserSpecificSiteTitle( Site site, boolean truncate )
	{
		String retVal = SiteService.getUserSpecificSiteTitle( site, UserDirectoryService.getCurrentUser().getId() );
		if (truncate)
		{
			return Web.escapeHtml( FormattedText.makeShortenedText( retVal, null, null, null ) );
		}
		else
		{
			return Web.escapeHtml( retVal );
		}
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to tab customization page (edit)
	 */
	public String processActionCancel()
	{
		log.debug("processActionCancel()");

		prefTabLabel = null; // reset to retrieve original prefs

		// remove session variables
		cancelEdit();
		// To stay on the same page - load the page data

		return m_TabOutcome;
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to Notification page (list)
	 */
	public String processActionNotiFrmEdit()
	{
		log.debug("processActionNotiFrmEdit()");
		refreshMode=false;
		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "noti";
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to timezone page (list)
	 */
	public String processActionTZFrmEdit()
	{
		log.debug("processActionTZFrmEdit()");

		refreshMode=false;
		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "timezone";
	}
	
	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to editor page (list)
	 */
	public String processActionEditorFrmEdit()
	{
		log.debug("processActionEditorFrmEdit()");

		refreshMode=false;
		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "editor";
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to locale page (list)
	 */
	public String processActionLocFrmEdit()
	{
		log.debug("processActionLocFrmEdit()");

		refreshMode=false;
		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "locale";
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to locale page (list)
	 */
	public String processActionPrivFrmEdit()
	{
		log.debug("processActionPrivFrmEdit()");

		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "privacy";
	}

	public String processActionHiddenFrmEdit()
	{
		log.debug("processActionHiddenFrmEdit()");

		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "hidden";
	}

	/**
	 * This is called from edit page for navigation to refresh page
	 * 
	 * @return navigation outcome to refresh page (refresh)
	 */
	public String processActionRefreshFrmEdit()
	{
		log.debug("processActionRefreshFrmEdit()");

		// is required as user editing is set on while entering to tab customization page
		cancelEdit();
		loadRefreshData();
		return "refresh";
	}

	// //////////////////////////////////// HELPER METHODS TO ACTIONS ////////////////////////////////////////////
	/**
	 * Cancel the edit and cleanup.
	 */
	protected void cancelEdit()
	{
		log.debug("cancelEdit()");

		// cleanup
		m_stuff = null;
		m_edit = null;
		prefExcludeItems = new ArrayList();
		prefOrderItems = new ArrayList();
		isNewUser = false;

		notiUpdated = false;
		tzUpdated = false;
		locUpdated = false;
		refreshUpdated = false;
		hiddenUpdated = false;
		editorUpdated = false;
	}

	/**
	 * used with processActionAdd() and processActionRemove()
	 * 
	 * @return SelectItem
	 */
	private SelectItem removeItems(String value, List items, String addtype, String removetype)
	{
		if (log.isDebugEnabled())
		{
			log.debug("removeItems(String " + value + ", List " + items + ", String " + addtype + ", String " + removetype + ")");
		}

		SelectItem result = null;
		for (int i = 0; i < items.size(); i++)
		{
			SelectItem item = (SelectItem) items.get(i);
			if (value.equals(item.getValue()))
			{
				result = (SelectItem) items.remove(i);
				break;
			}
		}
		return result;
	}

	/**
	 * Set editing mode on for user and add user if not existing
	 */
	protected void setUserEditingOn()
	{
		log.debug("setUserEditingOn()");

		try
		{
			m_edit = m_preferencesService.edit(getUserId());
		}
		catch (IdUnusedException e)
		{
			try
			{
				m_edit = m_preferencesService.add(getUserId());
				isNewUser = true;
			}
			catch (Exception ee)
			{
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(ee.toString()));
			}
		}
		catch (Exception e)
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(e.toString()));
		}
	}

	/**
	 * Save any changed values from the edit and cleanup.
	 */
	protected void saveEdit()
	{
		log.debug("saveEdit()");

		// user editing is required as commit() disable isActive() flag
		setUserEditingOn();
		// move the stuff from m_stuff into the edit
		if (m_stuff != null)
		{
			for (Iterator i = m_stuff.iterator(); i.hasNext();)
			{
				KeyNameValue knv = (KeyNameValue) i.next();
				// find the original to remove (unless this one was new)
				if (!knv.getOrigKey().equals(""))
				{
					ResourcePropertiesEdit props = m_edit.getPropertiesEdit(knv.getOrigKey());
					props.removeProperty(knv.getOrigName());
				}
				// add the new if we have a key and name and value
				if ((!knv.getKey().equals("")) && (!knv.getName().equals("")) && (!knv.getValue().equals("")))
				{
					ResourcePropertiesEdit props = m_edit.getPropertiesEdit(knv.getKey());
					if (knv.isList())
					{
						// split by ", "
						String[] parts = knv.getValue().split(", ");
						for (int p = 0; p < parts.length; p++)
						{
							props.addPropertyToList(knv.getName(), parts[p]);
						}
					}
					else
					{
						props.addProperty(knv.getName(), knv.getValue());
					}
				}
			}
		}
		// save the preferences, release the edit
		m_preferencesService.commit(m_edit);
	}

	/**
	 * Check String has value, not null
	 * 
	 * @return boolean
	 */
	protected boolean hasValue(String eval)
	{
		if (log.isDebugEnabled())
		{
			log.debug("hasValue(String " + eval + ")");
		}

		if (eval != null && !eval.trim().equals(""))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	// Copied from CheronPortal.java
	/**
	 * Find the site in the list that has this id - return the position. *
	 * 
	 * @param value
	 *        The site id to find.
	 * @param siteList
	 *        The list of Site objects.
	 * @return The index position in siteList of the site with site id = value, or -1 if not found.
	 */
	protected int indexOf(String value, List siteList)
	{
		if (log.isDebugEnabled())
		{
			log.debug("indexOf(String " + value + ", List " + siteList + ")");
		}

		for (int i = 0; i < siteList.size(); i++)
		{
			Site site = (Site) siteList.get(i);
			if (site.getId().equals(value))
			{
				return i;
			}
		}
		return -1;
	}

	// ////////////////////////////////// NOTIFICATION ACTIONS ////////////////////////////////
	
	private DecoratedNotificationPreference currentDecoratedNotificationPreference = null;
	
	@Getter @Setter
	protected boolean notiUpdated = false;

	@Getter @Setter
	protected boolean tzUpdated = false;

	@Getter @Setter
	protected boolean locUpdated = false;

	@Getter @Setter
	protected boolean hiddenUpdated = false;
	
	@Getter @Setter
	protected boolean editorUpdated = false;

	// ///////////////////////////////////////NOTIFICATION ACTION - copied from NotificationprefsAction.java////////
	// TODO - clean up method call. These are basically copied from legacy legacy implementations.
	/**
	 * Process the save command from the edit view.
	 * 
	 * @return navigation outcome to notification page
	 */
	public String processActionNotiSave()
	{
		log.debug("processActionNotiSave()");

		// get an edit
		setUserEditingOn();
		if (m_edit != null)
		{
			
			List<DecoratedNotificationPreference> items = getRegisteredNotificationItems();
			for(UserNotificationPreferencesRegistration upr : m_userNotificationPreferencesRegistrationService.getRegisteredItems()) {
				readTypePrefs(upr.getType(), upr.getPrefix(), m_edit, getSelectedNotificationItemByKey(upr.getType(), items));
				
				DecoratedNotificationPreference dnp = getDecoItemByKey(upr.getType(), items);
				if (dnp != null) {
					readOverrideTypePrefs(upr.getType() + NotificationService.NOTI_OVERRIDE_EXTENSION, upr.getPrefix(), m_edit, dnp.getSiteOverrides());
				}
			}

			// update the edit and release it
			m_preferencesService.commit(m_edit);
		}
		processRegisteredNotificationItems();
		notiUpdated = true;
		return "noti";
	}

	/**
	 * process notification cancel
	 * 
	 * @return navigation outcome to notification page
	 */
	public String processActionNotiCancel()
	{
		log.debug("processActionNotiCancel()");
		initRegisteredNotificationItems();
		return "noti";
	}

	/**
	 * Process the save command from the edit view.
	 * 
	 * @return navigation outcome to timezone page
	 */
	public String processActionTzSave()
	{
		setUserEditingOn();
		ResourcePropertiesEdit props = m_edit.getPropertiesEdit(TimeService.APPLICATION_ID);
		props.addProperty(TimeService.TIMEZONE_KEY, m_timeZone.getID());
		m_preferencesService.commit(m_edit);

		TimeService.clearLocalTimeZone(getUserId()); // clear user's cached timezone

		tzUpdated = true; // set for display of text message
		return "timezone";
	}
	/**
	 * 
	 * Processes the save command from the edit vieprefShowEditorLabelOptionw
	 * @return navigation outcome to editor page
	 */
	
	public String processActionEditorSave() 
	{
		setUserEditingOn();
		ResourcePropertiesEdit props = m_edit.getPropertiesEdit(PreferencesService.EDITOR_PREFS_KEY);
		props.addProperty(PreferencesService.EDITOR_PREFS_TYPE, m_editorType);
		m_preferencesService.commit(m_edit);


		
		editorUpdated = true; // set for display of text message
		return "editor";
	}
	

	/**
	 * process timezone cancel
	 * 
	 * @return navigation outcome to timezone page
	 */
	public String processActionTzCancel()
	{
		log.debug("processActionTzCancel()");

		// restore original time zone
		m_timeZone = null;
		getSelectedTimeZone();

		return "timezone";
	}
	
	/**
	 * process editor cancel
	 * 
	 * @return navigation outcome to editor page
	 */
	public String processActionEditorCancel()
	{
		log.debug("processActionEditorCancel()");
		
		// restore original editor
		m_editorType = null;
		getSelectedEditorType();

		return "editor";
	}

	/**
	 * Process the save command from the edit view.
	 * 
	 * @return navigation outcome to locale page
	 */
	public String processActionLocSave()
	{
		setUserEditingOn();
		ResourcePropertiesEdit props = m_edit.getPropertiesEdit(ResourceLoader.APPLICATION_ID);
		props.addProperty(ResourceLoader.LOCALE_KEY, m_locale.toString());
		m_preferencesService.commit(m_edit);

		TimeService.clearLocalTimeZone(getUserId()); // clear user's cached timezone

		//Save the preference in the session also      
		ResourceLoader rl = new ResourceLoader();
		Locale loc = rl.setContextLocale(null);		

		// reset notification items with the locale
		initRegisteredNotificationItems();

		locUpdated = true; // set for display of text message
		return "locale";
	}

	/**
	 * process locale cancel
	 * 
	 * @return navigation outcome to locale page
	 */
	public String processActionLocCancel()
	{
		log.debug("processActionLocCancel()");

		// restore original locale
		m_locale = null;
		getSelectedLocale();

		return "locale";
	}

	/**
	 * This is called from notification page for navigation to Refresh page
	 * 
	 * @return navigation outcome to refresh page
	 */
	public String processActionRefreshFrmNoti()
	{
		log.debug("processActionRefreshFrmNoti()");

		loadRefreshData();
		return "refresh";
	}

	// ////////////////////////////////////// HELPER METHODS FOR NOTIFICATIONS /////////////////////////////////////

	/**
	 * Read the two context references for defaults for this type from the form.
	 * 
	 * @param type
	 *        The resource type (i.e. a service name).
	 * @param prefix
	 *        The prefix for context references.
	 * @param edit
	 *        The preferences being edited.
	 * @param data
	 *        The rundata with the form fields.
	 */
	protected void readTypePrefs(String type, String prefix, PreferencesEdit edit, String data)
	{
		if (log.isDebugEnabled())
		{
			log.debug("readTypePrefs(String " + type + ", String " + prefix + ", PreferencesEdit " + edit + ", String " + data
					+ ")");
		}

		// update the default settings from the form
		ResourcePropertiesEdit props = edit.getPropertiesEdit(NotificationService.PREFS_TYPE + type);

		// read the defaults
		props.addProperty(Integer.toString(NotificationService.NOTI_OPTIONAL), data);

	} // readTypePrefs
	
	/**
	 * Read the two context references for defaults for this type from the form.
	 * 
	 * @param type
	 *        The resource type (i.e. a service name).
	 * @param prefix
	 *        The prefix for context references.
	 * @param edit
	 *        The preferences being edited.
	 * @param data
	 *        The rundata with the form fields.
	 */
	protected void readOverrideTypePrefs(String type, String prefix, PreferencesEdit edit, List<SiteOverrideBean> data)
	{
		if (log.isDebugEnabled())
		{
			log.debug("readOverrideTypePrefs(String " + type + ", String " + prefix + ", PreferencesEdit " + edit + ", String " + data
					+ ")");
		}
		
		List<SiteOverrideBean> toDel = new ArrayList<SiteOverrideBean>();

		// update the default settings from the form
		ResourcePropertiesEdit props = edit.getPropertiesEdit(NotificationService.PREFS_TYPE + type);

		// read the defaults
		for (SiteOverrideBean sob : data) {
			if (!sob.remove) {
				props.addProperty(sob.getSiteId(), sob.getOption());
			}
			else {
				props.removeProperty(sob.getSiteId());
				toDel.add(sob);
			}
		}
		data.removeAll(toDel);

	} // readOverrideTypePrefs
	
	/**
	 * Read the two context references for defaults for this type from the form.
	 * 
	 * @param type
	 *        The resource type (i.e. a service name).
	 * @param edit
	 *        The preferences being edited.
	 * @param data
	 *        The rundata with the form fields.
	 */
	protected void readOverrideTypePrefs(String type, PreferencesEdit edit, List<SiteOverrideBean> data)
	{
		if (log.isDebugEnabled())
		{
			log.debug("readOverrideTypePrefs(String " + type + ", PreferencesEdit " + edit + ", String " + data
					+ ")");
		}

		// update the default settings from the form
		ResourcePropertiesEdit props = edit.getPropertiesEdit(NotificationService.PREFS_TYPE + type);

		// read the defaults
		for (SiteOverrideBean sob : data) {
			props.addProperty(sob.getSiteId(), sob.getOption());
		}

	} // readOverrideTypePrefs
	
	/**
	 * delete the preferences for this type.
	 * 
	 * @param type
	 *        The resource type (i.e. a service name).
	 * @param prefix
	 *        The prefix for context references.
	 * @param edit
	 *        The preferences being edited.
	 * @param data
	 *        The rundata with the form fields.
	 */
	protected void deleteOverrideTypePrefs(String type, String prefix, PreferencesEdit edit, List<String> data)
	{
		if (log.isDebugEnabled())
		{
			log.debug("deleteOverrideTypePrefs(String " + type + ", String " + prefix + ", PreferencesEdit " + edit + ", String " + data
					+ ")");
		}

		ResourcePropertiesEdit props = edit.getPropertiesEdit(NotificationService.PREFS_TYPE + type);

		// delete
		for (String siteId : data) {
			props.removeProperty(siteId);
		}

	} // deleteOverrideTypePrefs

	/**
	 * Add the two context references for defaults for this type.
	 * 
	 * @param type
	 *        The resource type (i.e. a service name).
	 * @param prefix
	 *        The prefix for context references.
	 * @param context
	 *        The context.
	 * @param prefs
	 *        The full set of preferences.
	 */
	protected String buildTypePrefsContext(String type, String prefix, String context, Preferences prefs)
	{
		if (log.isDebugEnabled())
		{
			log.debug("buildTypePrefsContext(String " + type + ", String " + prefix + ", String " + context + ", Preferences "
					+ prefs + ")");
		}

		ResourceProperties props = prefs.getProperties(NotificationService.PREFS_TYPE + type);
		String value = props.getProperty(new Integer(NotificationService.NOTI_OPTIONAL).toString());

		return value;
	}
	
	/**
	 * Add the two context references for defaults for this type.
	 * 
	 * @param type
	 *        The resource type (i.e. a service name).
	 * @param prefix
	 *        The prefix for context references.
	 * @param context
	 *        The context.
	 * @param prefs
	 *        The full set of preferences.
	 */
	protected List<SiteOverrideBean> buildOverrideTypePrefsContext(String type, String prefix, String context, Preferences prefs)
	{
		if (log.isDebugEnabled())
		{
			log.debug("buildOverrideTypePrefsContext(String " + type + ", String " + prefix + ", String " + context + ", Preferences "
					+ prefs + ")");
		}

		ResourceProperties props = prefs.getProperties(NotificationService.PREFS_TYPE + type);
		
		List<SiteOverrideBean> result = new ArrayList<SiteOverrideBean>();
		
		for (Iterator<String> i = props.getPropertyNames(); i.hasNext();) {
			String propName = i.next();
			SiteOverrideBean sob = new SiteOverrideBean(propName, props.getProperty(propName));
			result.add(sob);
		}
		
		Collections.sort(result, new SiteOverrideBeanSorter());
		
		return result;
	}

	// SAK-23895
	private String selectedTabLabel = "";


	private String getPrefTabLabel(){
	    if ( prefTabLabel != null )
	        return prefTabLabel;

	    Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
	    ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
	    prefTabLabel = props.getProperty(TAB_LABEL_PREF);

	    if ( prefTabLabel == null )
	        prefTabLabel = String.valueOf(DEFAULT_TAB_LABEL);

	    return prefTabLabel;
	}
	/**
	 * @return Returns the getSelectedTabLabel.
	 */
	public String getSelectedTabLabel()
	{
	    this.selectedTabLabel= getPrefTabLabel();
	    return this.selectedTabLabel;

	}

	/**
	 * @param label
	 *        The tab label to set.
	 */
	public void setSelectedTabLabel(String label)
	{
	    this.prefTabLabel = label;
	    this.selectedTabLabel = label;
	}


	// ////////////////////////////////////// REFRESH //////////////////////////////////////////
	private String selectedRefreshItem = "";

	protected boolean refreshUpdated = false;

	/**
	 * @return Returns the selectedRefreshItem.
	 */
	public String getSelectedRefreshItem()
	{
		return selectedRefreshItem;
	}

	/**
	 * @param selectedRefreshItem
	 *        The selectedRefreshItem to set.
	 */
	public void setSelectedRefreshItem(String selectedRefreshItem)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setSelectedRefreshItem(String " + selectedRefreshItem + ")");
		}

		this.selectedRefreshItem = selectedRefreshItem;
	}

	/**
	 * Process cancel and navigate to list page.
	 * 
	 * @return navigation outcome to refresh page
	 */
	public String processActionRefreshCancel()
	{
		log.debug("processActionRefreshCancel()");

		loadRefreshData();
		return "refresh";
	}

	/**
	 * Process cancel and navigate to list page.
	 * 
	 * @return navigation outcome to notification page
	 */
	public String processActionNotiFrmRefresh()
	{
		log.debug("processActionNotiFrmRefresh()");

		return "noti";
		//return "tab";
	}

	// ///////////////////////////////////// HELPER METHODS FOR REFRESH /////////////////////////
	/**
	 * Load refresh data from stored information. This is called when navigated into this page for first time.
	 */
	protected void loadRefreshData()
	{
		log.debug("loadRefreshData()");

		selectedRefreshItem = "";
		refreshUpdated = false;
		if (!hasValue(selectedRefreshItem))
		{
			Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
			// String a = getStringPref(PortalService.SERVICE_NAME, "refresh", prefs);
			// if (hasValue(a))
			// {
			// setSelectedRefreshItem(a); // load from saved data
			// }
			// else
			// {
			// setSelectedRefreshItem("2"); // default setting
			// }
		}
	}

	/**
	 * Set an integer preference.
	 * 
	 * @param pres_base
	 *        The name of the group of properties (i.e. a service name)
	 * @param type
	 *        The particular property
	 * @param edit
	 *        An edit version of the full set of preferences for the current logged in user.
	 * @param newval
	 *        The string to be the new preference.
	 */
	protected void setStringPref(String pref_base, String type, PreferencesEdit edit, String newval)
	{
		if (log.isDebugEnabled())
		{
			log.debug("setStringPref(String " + pref_base + ", String " + type + ", PreferencesEdit " + edit + ", String " + newval
					+ ")");
		}

		ResourcePropertiesEdit props = edit.getPropertiesEdit(pref_base);

		props.addProperty(type, newval);
	} // setStringPref

	/**
	 * Retrieve a preference
	 * 
	 * @param pres_base
	 *        The name of the group of properties (i.e. a service name)
	 * @param type
	 *        The particular property
	 * @param prefs
	 *        The full set of preferences for the current logged in user.
	 */
	protected String getStringPref(String pref_base, String type, Preferences prefs)
	{
		if (log.isDebugEnabled())
		{
			log.debug("getStringPref(String " + pref_base + ", String " + type + ", PreferencesEdit " + prefs + ")");
		}

		ResourceProperties props = prefs.getProperties(pref_base);
		String a = props.getProperty(type);
		return a;
	} // getIntegerPref

	/** Should we reload the top window? */
	protected Boolean m_reloadTop = Boolean.FALSE;

	/**
	 * Get, and clear, the reload element
	 */
	public Boolean getReloadTop()
	{
		Boolean rv = m_reloadTop;
		m_reloadTop = Boolean.FALSE;
		return rv;
	}

	public void setReloadTop(Boolean val) { }

	/**
	 * Pull whether privacy status should be enabled from sakai.properties
	 */
	public boolean isPrivacyEnabled()
	{
		//return ServerConfigurationService.getBoolean(ENABLE_PRIVACY_STATUS, false);
		if (getPrivacy_selection()==0){
			return false;
		}
		else {
			return true;
		}
		
	}

	/**
	 * Should research/collab specific preferences (no syllabus) be displayed?
	 * 
	 */
	public boolean isResearchCollab()
	{
		return ServerConfigurationService.getBoolean(PREFS_RESEARCH, false);
	}
	
	public List<DecoratedNotificationPreference> getRegisteredNotificationItems() {
		log.debug("getRegisteredNotificationItems()");
		return m_registereddNotificationItems;
	}
	
	public void initRegisteredNotificationItems() {
		log.debug("initRegisteredNotificationItems()");
		m_registereddNotificationItems.clear();
		for (UserNotificationPreferencesRegistration upr : m_userNotificationPreferencesRegistrationService.getRegisteredItems()) {

			m_registereddNotificationItems.addAll(getRegisteredNotificationItems(upr));
		}
		
		processRegisteredNotificationItems();
	}
	
	/**
	 * Convenience method to take a string array and put it in a map
	 * The key is the value in the array and the data in the map is the position in the array
	 * @param array
	 * @return
	 */
	private Map<String, Integer> stringArrayToMap(String[] array) {
		Map<String, Integer> retMap = new HashMap<String, Integer>();
		Integer index = 0;
		if (array != null) {
		    for (String key : array) {
			retMap.put(key, index);
			index++;
		    }
		}
		return retMap;
	}
	
	/**
	 * Convenience method to take a string (comma delimited list of values) and turn it in to a list.
	 * @param toolIds
	 * @return
	 */
	private List<String> stringToList(String toolIds)
	{
		if ((toolIds != null) && (toolIds.length() > 0))
		{
			String[] items = toolIds.split(",");
			return Arrays.asList(items);
		}
		return new ArrayList<String>();
	}
	
	/**
	 * Looks at the various hide and stealth properties to determine which tools should be displayed.
	 * This method mostly taken from org.sakaiproject.tool.impl.ToolComponent.init() and modified a bit.
	 * @return
	 */
	private String[] getHiddenTools() {
		// compute the tools to hide: these are the stealth tools plus the hidden tools, minus the visible ones
		Set<String> toHide = new HashSet();
		
		String hiddenToolsPrefs = ServerConfigurationService.getString("prefs.tool.hidden");
		String stealthTools = ServerConfigurationService.getString("stealthTools@org.sakaiproject.tool.api.ActiveToolManager");
		String hiddenTools = ServerConfigurationService.getString("hiddenTools@org.sakaiproject.tool.api.ActiveToolManager");
		String visibleTools = ServerConfigurationService.getString("visibleTools@org.sakaiproject.tool.api.ActiveToolManager");
		

		if (stealthTools != null)
		{
			toHide.addAll(stringToList(stealthTools));
		}

		if (hiddenTools!= null)
		{
			toHide.addAll(stringToList(hiddenTools));
		}

		if (visibleTools != null)
		{
			toHide.removeAll(stringToList(visibleTools));
		}
		
		if (hiddenToolsPrefs != null)
		{
			toHide.addAll(stringToList(hiddenToolsPrefs));
		}
		
		return toHide.toArray(new String[]{});

	}
	
	/**
	 * Determine the sorting and if any should be hidden from view
	 * @param decoItems
	 */
	private void processRegisteredNotificationItems() {
		
		Map<String, Integer> toolOrderMap = new HashMap<String, Integer>();
		String[] toolOrder = ServerConfigurationService.getStrings("prefs.tool.order");
		//String hiddenTools = ServerConfigurationService.getString("prefs.tool.hidden");
		
		String[] parsedHidden = getHiddenTools();
		Map<String, Integer> hiddenToolMap = new HashMap<String, Integer>();
		
		toolOrderMap = stringArrayToMap(toolOrder);
		hiddenToolMap = stringArrayToMap(parsedHidden);
		
		Preferences prefs = m_preferencesService.getPreferences(getUserId());
		
		for (DecoratedNotificationPreference dnp : m_registereddNotificationItems) {
			String toolId = dnp.getUserNotificationPreferencesRegistration().getToolId();
			Integer sort = toolOrderMap.get(toolId);
			if (sort != null)
				dnp.setSortOrder(sort);
			if (hiddenToolMap.get(toolId) != null) {
				dnp.setHidden(true);
			}
			
			ResourceProperties expandProps = prefs.getProperties(PREFS_EXPAND);
			if (expandProps != null) {
				String expandProp = expandProps.getProperty(dnp.key);
				if (expandProp != null) {
					boolean overrideExpand = expandProp.equalsIgnoreCase(PREFS_EXPAND_TRUE) ? true : false;
					dnp.setExpandOverride(overrideExpand);
				}
			}
		}
		Collections.sort(m_registereddNotificationItems, new DecoratedNotificationPreferenceSorter());
	}
	
	/**
	 * Get the current preference settings for this registration item
	 * @param upr
	 * @return
	 */
	public List<DecoratedNotificationPreference> getRegisteredNotificationItems(UserNotificationPreferencesRegistration upr) {
		log.debug("getRegisteredNotificationItems(UserNotificationPreferencesRegistration)");
		List<DecoratedNotificationPreference> selNotiItems = new ArrayList<DecoratedNotificationPreference>();
		Preferences prefs = m_preferencesService.getPreferences(getUserId());
		List<SiteOverrideBean> siteOverrides = new ArrayList<SiteOverrideBean>();
		if (upr.isOverrideBySite()) {
			siteOverrides = 
				buildOverrideTypePrefsContext(upr.getType() + NotificationService.NOTI_OVERRIDE_EXTENSION, 
						upr.getPrefix(), null, prefs);
		}
		DecoratedNotificationPreference dnp = new DecoratedNotificationPreference(upr, siteOverrides);
			String regItem = buildTypePrefsContext(upr.getType(), upr.getPrefix(), dnp.getSelectedOption(), prefs);
			if (hasValue(regItem))
			{
				dnp.setSelectedOption(regItem); // load from saved data
			}
			else
			{
				dnp.setSelectedOption(upr.getDefaultValue()); // default setting
			}
			
			selNotiItems.add(dnp);
		return selNotiItems;
	}
		
	public List<String> getSelectedNotificationItemIds(DecoratedNotificationPreference dnp) {
		log.debug("getSelectedNotificationItemIds(DecoratedNotificationPreference)");
		List<String> result = new ArrayList<String>();
		for (SiteOverrideBean sob : dnp.getSiteOverrides()) {
				result.add(sob.siteId);
		}
		return result;
	}

	public void setCurrentDecoratedNotificationPreference(
			DecoratedNotificationPreference currentDecoratedNotificationPreference) {
		this.currentDecoratedNotificationPreference = currentDecoratedNotificationPreference;
	}

	public DecoratedNotificationPreference getCurrentDecoratedNotificationPreference() {
		return currentDecoratedNotificationPreference;
	}

	private DecoratedNotificationPreference getDecoItemByKey(String key, List<DecoratedNotificationPreference> decoPreferences) {
		log.debug("getDecoItemByKey(" + key + ")");
		for (DecoratedNotificationPreference dnp : decoPreferences) {
			if (dnp.getKey().equalsIgnoreCase(key)) {
				return dnp;
			}
		}
		return null;
	}
	private String getSelectedNotificationItemByKey(String key, List<DecoratedNotificationPreference> decoPreferences) {
		log.debug("getSelectedNotificationItemByKey(" + key + ")");
		DecoratedNotificationPreference dnp = getDecoItemByKey(key, decoPreferences);
		if (dnp != null) {
			return dnp.getSelectedOption();
		}
		return null;
	}
	
	/**
	 * Get the display name for the site type in question.  
	 * If a course site, it will pull the info from the "term" site property
	 * @param site
	 * @return
	 */
	private String getSiteTypeDisplay(Site site) {
		ResourceProperties siteProperties = site.getProperties();

		String type = site.getType();
		String term = null;

		if ("course".equals(type))
		{
			term = siteProperties.getProperty("term");
			if (term==null) term = msgs.getString("moresite_no_term","");
		}
		else if ("project".equals(type))
		{
			term = msgs.getString("moresite_projects");
		}
		else if ("portfolio".equals(type))
		{
			term = msgs.getString("moresite_portfolios");
		}
		else if ("scs".equals(type))
		{
			term = msgs.getString("moresite_scs");
		}
		else if ("admin".equals(type))
		{
			term = msgs.getString("moresite_administration");
		}
		else
		{
			term = msgs.getString("moresite_other");
		}
		return term;
	}
	
	public class DecoratedNotificationPreference {
		
		private String key = "";
		private UserNotificationPreferencesRegistration userNotificationPreferencesRegistration = null;
		private String selectedOption = "";
		private List<SelectItem> optionSelectItems = new ArrayList<SelectItem>();
		private List<SiteOverrideBean> siteOverrides = new ArrayList<SiteOverrideBean>();
		private List<DecoratedSiteTypeBean> siteList = new ArrayList<DecoratedSiteTypeBean>();
		private Integer sortOrder = Integer.MAX_VALUE;
		private boolean hidden = false;
		private Boolean expandOverride = null;
		
		public DecoratedNotificationPreference() { 
			log.debug("DecoratedNotificationPreference()");
		}
		
		public DecoratedNotificationPreference(UserNotificationPreferencesRegistration userNotificationPreferencesRegistration, List<SiteOverrideBean> siteOverrides) {
			log.debug("DecoratedNotificationPreference(...)");
			this.userNotificationPreferencesRegistration = userNotificationPreferencesRegistration;
			this.key = userNotificationPreferencesRegistration.getType();
			this.siteOverrides = siteOverrides;
			
			for (String optionKey : userNotificationPreferencesRegistration.getOptions().keySet()) {
				SelectItem si = new SelectItem(optionKey, userNotificationPreferencesRegistration.getOptions().get(optionKey));
				optionSelectItems.add(si);
			}
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}

		public void setUserNotificationPreferencesRegistration(UserNotificationPreferencesRegistration userNotificationPreferencesRegistration) {
			this.userNotificationPreferencesRegistration = userNotificationPreferencesRegistration;
		}

		public UserNotificationPreferencesRegistration getUserNotificationPreferencesRegistration() {
			return userNotificationPreferencesRegistration;
		}

		public void setSelectedOption(String selectedOption) {
			this.selectedOption = selectedOption;
		}

		public String getSelectedOption() {
			return selectedOption;
		}

		public void setOptionSelectItems(List<SelectItem> optionSelectItems) {
			this.optionSelectItems = optionSelectItems;
		}

		public List<SelectItem> getOptionSelectItems() {
			return optionSelectItems;
		}

		public void setSiteOverrides(List<SiteOverrideBean> siteOverrides) {
			this.siteOverrides = siteOverrides;
		}

		public List<SiteOverrideBean> getSiteOverrides() {
			return siteOverrides;
		}
		
		public List<DecoratedSiteTypeBean> getSiteList() {
			return siteList;
		}

		public void setSiteList(List<DecoratedSiteTypeBean> siteList) {
			this.siteList = siteList;
		}

		public Integer getSortOrder() {
			return sortOrder;
		}

		public void setSortOrder(Integer sortOrder) {
			this.sortOrder = sortOrder;
		}

		public boolean isHidden() {
			return hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}
		
		public Boolean getExpandOverride() {
			return expandOverride;
		}

		public void setExpandOverride(Boolean expandOverride) {
			this.expandOverride = expandOverride;
		}
		
		public boolean getExpand() {
			Boolean override = getExpandOverride();
			if (override != null)
				return override;
			else
				return this.getUserNotificationPreferencesRegistration().isExpandByDefault();
		}

		public String processActionAddOverrides() {
			setCurrentDecoratedNotificationPreference(this);
			initSiteList();
			return "noti_addSiteOverride";
		}
		
		/**
		 * Initializing the site structures
		 */
		private void initSiteList() {
			Map<String, List<DecoratedSiteBean>> siteTypeMap = new HashMap<String, List<DecoratedSiteBean>>();
			Map<String, String> siteTypeKeyMap = new HashMap<String, String>();
			
			List<String> selectedSites = getSelectedNotificationItemIds(this);
			for (Site site : m_sites) {
				if (site.getToolForCommonId(userNotificationPreferencesRegistration.getToolId()) != null) {
					String typeDisplay = getSiteTypeDisplay(site);
					List<DecoratedSiteBean> sitesList = siteTypeMap.get(typeDisplay);
					if (sitesList == null) {
						sitesList = new ArrayList<DecoratedSiteBean>();
					}
					boolean selected = selectedSites.contains(site.getId());
					sitesList.add(new DecoratedSiteBean(site, selected));
					siteTypeMap.put(typeDisplay, sitesList);
					siteTypeKeyMap.put(typeDisplay, site.getType());
				}
			}
			
			String expandTypeString = ServerConfigurationService.getString("prefs.type.autoExpanded", "portfolio");
			
			String[] sortedTypeList = ServerConfigurationService.getStrings("prefs.type.order");
            if(sortedTypeList == null) {
                sortedTypeList = new String[] {"portfolio","course","project"};
            }
			String[] termOrder = ServerConfigurationService.getStrings("portal.term.order");
			List<String> myTermOrder = new ArrayList<String>();
					
			
			if (termOrder != null)
			{
				for (int i = 0; i < termOrder.length; i++)
				{

					if (siteTypeMap.containsKey(termOrder[i]))
					{
						myTermOrder.add(termOrder[i]);
					}
				}
			}

			int count = 0;
			for (int i = 0; i < sortedTypeList.length; i++) {
				
				if ("course".equalsIgnoreCase(sortedTypeList[i])) {
					boolean firstCourse = true;
					for (String value : myTermOrder) {
						if (value != null && !value.equals("")) {
							m_sortedTypes.put(value, count);
							count++;
							if (firstCourse) {
								expandTypeString = expandTypeString.concat("," + value);
								firstCourse=false;
							}
						}
					}
				}
				else {
					String value = sortedTypeList[i];
					if (value != null && !value.equals("")) {
						m_sortedTypes.put(value, count);
						count++;
					}
				}
			}
			//List<DecoratedSiteTypeBean> siteOverrideList = new ArrayList<DecoratedSiteTypeBean>();
			List<String> expandedTypes = Arrays.asList(expandTypeString.split(","));
			siteList = getFullSiteOverrideList(siteTypeMap, siteTypeKeyMap, expandedTypes);
		}
		
		/**
		 * Get all the sites by type and determine if the div should be expanded.
		 * @return
		 */
		private List<DecoratedSiteTypeBean> getFullSiteOverrideList(Map<String, List<DecoratedSiteBean>> siteTypeMap, 
				Map<String, String> siteTypeKeyMap, List<String> expandedTypes) {
			log.debug("getFullSiteOverrideList()");
			List<DecoratedSiteTypeBean> list = new ArrayList<DecoratedSiteTypeBean>();
			
			for (String keyText : siteTypeMap.keySet()) {
				boolean expand = false;
				String typeKey = siteTypeKeyMap.get(keyText);
				if (expandedTypes.contains(typeKey) || expandedTypes.contains(keyText.toUpperCase()))
					expand = true;
				
				list.add(new DecoratedSiteTypeBean(typeKey, keyText, siteTypeMap.get(keyText), expand));
			}
			
			Collections.sort(list, new SiteTypeSorter());
			
			return list;
		}
		
		/**
		 * Do the save action
		 * @return
		 */
		public String processActionSiteOverrideSave() {
			log.debug("processActionSiteOverrideSave()");
			
			// get an edit
			setUserEditingOn();
			if (m_edit != null)
			{
				List<SiteOverrideBean> toAdd = new ArrayList<SiteOverrideBean>();
				List<String> toDel = new ArrayList<String>();
				
				/** previously saved choices */
				List<String> existingList = convertToStringList(getSiteOverrides());
				
				log.debug("processActionSiteOverrideSave().existingList: " + convertListToString(existingList));
				
				for (DecoratedSiteTypeBean dstb : siteList) {
					for (DecoratedSiteBean dsb : dstb.getSites()) {
						String siteId = dsb.getSite().getId();
						log.debug("processActionSiteOverrideSave().selected?: " +siteId + ": " + dsb.selected);
						SiteOverrideBean sob = new SiteOverrideBean(siteId, Integer.toString(NotificationService.PREF_NONE));
						if (dsb.selected && !existingList.contains(siteId)) {
							toAdd.add(sob);
							//siteOverrides.add(sob);
						}
						else if (!dsb.selected && existingList.contains(siteId)) {
							toDel.add(siteId);
							//siteOverrides.remove(sob);
						}
					}
				}

				log.debug("processActionSiteOverrideSave().toAdd: " + convertListToString(toAdd));
				log.debug("processActionSiteOverrideSave().toDel: " + convertListToString(toDel));
				//adds
				readOverrideTypePrefs(userNotificationPreferencesRegistration.getType() + NotificationService.NOTI_OVERRIDE_EXTENSION, 
						m_edit, toAdd);

				//deletes
				deleteOverrideTypePrefs(userNotificationPreferencesRegistration.getType() + NotificationService.NOTI_OVERRIDE_EXTENSION, 
						userNotificationPreferencesRegistration.getPrefix(), m_edit, toDel);

				// update the edit and release it
				m_preferencesService.commit(m_edit);
				
				//make sure the list gets updated
				initRegisteredNotificationItems();
			}
			
			notiUpdated = true;
			return "noti";
		}
		
		/**
		 * For display/debug purposes.  Turns a List into a readable string
		 * @param list
		 * @return
		 */
		private String convertListToString(List list) {
			String retVal = "[";
			int counter = 0;
			for (Iterator i = list.iterator(); i.hasNext();) {
				
				Object rawValue = i.next();
				String val = "";
				if (rawValue instanceof SiteOverrideBean) {
					val = ((SiteOverrideBean)rawValue).getSiteId();
				}
				else {
					val = (String) rawValue;
				}
				
				if (counter > 0) 
					retVal = retVal.concat(",");
				retVal = retVal.concat(val);
				counter++;
			}
			return retVal.concat("]");
		}
		
		/**
		 * For display/debug purposes.  Turns a List of SiteOverrideBeans into a readable string
		 * @param list
		 * @return
		 */
		private List<String> convertToStringList(List<SiteOverrideBean> list) {
			List<String> retList = new ArrayList<String>(list.size());
			for (SiteOverrideBean sob : list) {
				retList.add(sob.getSiteId());
			}
			return retList;
		}
		
		/**
		 * Do the cancel action
		 * @return
		 */
		public String processActionSiteOverrideCancel() {
			log.debug("processActionSiteOverrideCancel()");
			processRegisteredNotificationItems();
			return "noti";
		}
	}
		

	public String processHiddenSites()
	{
		setUserEditingOn();
		if (m_edit != null) {
			// Remove existing property
			ResourcePropertiesEdit props = m_edit.getPropertiesEdit(PreferencesService.SITENAV_PREFS_KEY);

			List currentFavoriteSites = props.getPropertyList(ORDER_SITE_LISTS);

			if (currentFavoriteSites == null) {
				currentFavoriteSites = Collections.<String>emptyList();
			}

			props.removeProperty(TAB_LABEL_PREF);
			props.removeProperty(ORDER_SITE_LISTS);
			props.removeProperty(EXCLUDE_SITE_LISTS);

			m_preferencesService.commit(m_edit);
			cancelEdit();

			// Set favorites and hidden sites
			setUserEditingOn();
			props = m_edit.getPropertiesEdit(PreferencesService.SITENAV_PREFS_KEY);

			for (Object siteId : currentFavoriteSites) {
				props.addPropertyToList(ORDER_SITE_LISTS, (String)siteId);
			}

			if (hiddenSitesInput != null && !hiddenSitesInput.isEmpty()) {
				for (String siteId : hiddenSitesInput.split(",")) {
					props.addPropertyToList(EXCLUDE_SITE_LISTS, siteId);
				}
			}

			props.addProperty(TAB_LABEL_PREF, prefTabLabel);

			m_preferencesService.commit(m_edit);
			cancelEdit();

			hiddenUpdated = true;

			m_reloadTop = Boolean.TRUE;
		}

		return "hidden";
	}


	public class SiteOverrideBean {
		
		private String siteId = "";
		private String siteTitle = "";
		private String option = "";
		private boolean remove = false;
		
		public SiteOverrideBean() {
			log.debug("SiteOverrideBean()");
		}
		
		public SiteOverrideBean(String siteId, String option) {
			log.debug("SiteOverrideBean(String, String)");
			this.siteId = siteId;
			this.option = option;
			
			try {
				Site site = SiteService.getSite(siteId);
				this.siteTitle = getUserSpecificSiteTitle(site, true);
			} catch (IdUnusedException e) {
				log.warn("Unable to get Site object for id: " + siteId, e);
			}
		}
		
		public String getSiteId() {
			return siteId;
		}
		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}
		public String getSiteTitle() {
			return siteTitle;
		}
		public void setSiteTitle(String siteTitle) {
			this.siteTitle = siteTitle;
		}
		public String getOption() {
			return option;
		}
		public void setOption(String option) {
			this.option = option;
		}

		public void setRemove(boolean remove) {
			this.remove = remove;
		}

		public boolean isRemove() {
			return remove;
		}
	}
	
	public class DecoratedSiteTypeBean {
		private String typeKey = "";
		private String typeText = "";
		private String condensedTypeText = "";
		private List<DecoratedSiteBean> sites = new ArrayList<DecoratedSiteBean>();
		private List<SelectItem> sitesAsSelects = new ArrayList<SelectItem>();
		private boolean defaultOpen = false;
		
		public DecoratedSiteTypeBean() {
			log.debug("DecoratedSiteTypeBean()");
		}
		
		public DecoratedSiteTypeBean(String typeKey, String typeText, List<DecoratedSiteBean> sites, boolean defaultOpen) {
			log.debug("DecoratedSiteTypeBean(...)");
			this.setTypeKey(typeKey);
			this.typeText = typeText;
			this.sites = sites;
			this.condensedTypeText = typeText.replace(" ", "");
			this.defaultOpen = defaultOpen;
			
			for (DecoratedSiteBean dsb : sites) {
				sitesAsSelects.add(new SelectItem(dsb.getSite().getId(), getUserSpecificSiteTitle(dsb.getSite(), true)));
			}			
		}
		
		public String getStyle() {
			String style = "display: none;";
			if (defaultOpen)
				style = "display: block;";
			return style;
		}
		
		public String getIconUrl() {
			String url = "/library/image/sakai/expand.gif";
			if (defaultOpen)
				url = "/library/image/sakai/collapse.gif";
			return url;
		}
		
		public String getShowHideText() {
			String key = "hideshowdesc_toggle_show";
			if (defaultOpen)
				key = "hideshowdesc_toggle_hide";
			return msgs.getString(key);
		}
		
		public Integer getSortOrder() {
			Integer sort = null;
			if (typeKey.equals("course")) {
				sort = m_sortedTypes.get(typeText.toUpperCase());
			}
			else {
				sort = m_sortedTypes.get(typeKey);
			}
			
			if (sort == null)
				sort = Integer.MAX_VALUE;
			
			return sort;
		}

		public void setTypeKey(String typeKey) {
			this.typeKey = typeKey;
		}

		public String getTypeKey() {
			return typeKey;
		}

		public String getTypeText() {
			return typeText;
		}

		public void setTypeText(String typeText) {
			this.typeText = typeText;
		}

		public String getCondensedTypeText() {
			return condensedTypeText;
		}

		public void setCondensedTypeText(String condensedTypeText) {
			this.condensedTypeText = condensedTypeText;
		}

		public List<DecoratedSiteBean> getSites() {
			return sites;
		}

		public void setSites(List<DecoratedSiteBean> sites) {
			this.sites = sites;
		}

		public List<SelectItem> getSitesAsSelects() {
			return sitesAsSelects;
		}

		public void setSitesAsSelects(List<SelectItem> sitesAsSelects) {
			this.sitesAsSelects = sitesAsSelects;
		}

		public void setDefaultOpen(boolean defaultOpen) {
			this.defaultOpen = defaultOpen;
		}

		public boolean isDefaultOpen() {
			return defaultOpen;
		}
	}
	
	public class DecoratedSiteBean {
		private Site site = null;
		private boolean selected = false;
		
		public DecoratedSiteBean() {
			log.debug("DecoratedSiteBean()");
		}
		
		public DecoratedSiteBean(Site site) {
			log.debug("DecoratedSiteBean(Site)");
			this.site = site;
		}
		
		public DecoratedSiteBean(Site site, boolean selected) {
			log.debug("DecoratedSiteBean(Site, boolean)");
			this.site = site;
			this.selected = selected;
		}

		public Site getSite() {
			return site;
		}

		public void setSite(Site site) {
			this.site = site;
		}

		public boolean isSelected() {
			return selected;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}
		
		public void checkBoxChanged(ValueChangeEvent vce) {
			log.debug("checkBoxChanged()" + site.getId() + ": " + vce.getOldValue().toString() + "-->" + vce.getNewValue().toString());
			Boolean tmpSelected = (Boolean)vce.getNewValue();
			if (tmpSelected != null) {
				selected = tmpSelected;
			}
		}
	}
	
	/**
	 * Class that figures out the order of the site types
	 * @author chrismaurer
	 *
	 */
	class SiteTypeSorter implements Comparator<DecoratedSiteTypeBean>
	{
		public int compare(DecoratedSiteTypeBean first, DecoratedSiteTypeBean second)
		{
			if (first == null || second == null) return 0;

			Integer firstSort = first.getSortOrder();
			Integer secondSort = second.getSortOrder();

			if (firstSort != null)
				return firstSort.compareTo(secondSort);

			return 0;
		}

	}
	
	/**
	 * Class that figures out the order of the site types
	 * @author chrismaurer
	 *
	 */
	class SiteOverrideBeanSorter implements Comparator<SiteOverrideBean>
	{
		public int compare(SiteOverrideBean first, SiteOverrideBean second)
		{
			if (first == null || second == null) return 0;

			String firstSort = first.getSiteTitle();
			String secondSort = second.getSiteTitle();

			if (firstSort != null)
				return firstSort.toLowerCase().compareTo(secondSort.toLowerCase());

			return 0;
		}

	}
	
	/**
	 * Class that figures out the order of the site types
	 * @author chrismaurer
	 *
	 */
	class DecoratedNotificationPreferenceSorter implements Comparator<DecoratedNotificationPreference>
	{
		public int compare(DecoratedNotificationPreference first, DecoratedNotificationPreference second)
		{
			if (first == null || second == null) return 0;

			Integer firstSort = first.getSortOrder();
			Integer secondSort = second.getSortOrder();

			if (firstSort != null)
				return firstSort.compareTo(secondSort);

			return 0;
		}

	}
	
	public class TermSites
	{
		private List<Term> terms;
		private List <String> termOrder;

		public class Term implements Comparable<Term> {
			private String label;
			private List<Site> sites;
			
			public Term(String label, List<Site> sites) {
				if (sites.isEmpty()) {
					throw new RuntimeException("List of sites can't be empty");
				}

				this.label = label;
				this.sites = sites;
			}

			public String getLabel() {
				return label;
			}

			public List<Site> getSites() {
				return sites;
			}

			public String getType() {
				return this.getSites().get(0).getType();
			}

			public int compareTo(Term other) {
				if (termOrder != null && (termOrder.contains(this.label) || termOrder.contains(other.label))) {
					return(NumberUtils.compare(termOrder.indexOf(this.label), termOrder.indexOf(other.label)));
				}
				
				String myType = this.getType();
				String theirType = other.getType();

				// Otherwise if not found in a term course sites win out over non-course-sites
				if (myType == null) {
					return 1;
				} else if (theirType == null) {
					return -1;
				} else if (myType.equals(theirType)) {
					return 0;
				} else if ("course".equals(myType)) {
					return -1;
				} else {
					return 1;
				}
			}
		}

		public TermSites(List<Site> sites) {
			List<String> termNames = new ArrayList<String>();
			Map<String, List<Site>> termsToSites = new HashMap<String, List<Site>>();

			for (Site site : sites) {
				String term = determineTerm(site);

				if (!termNames.contains(term)) {
					termNames.add(term);
					termsToSites.put(term, new ArrayList<Site>(1));
				}

				// This is being used to display the full site title in the tool tip.
				// It's necessary to pack this into some random/unused field of the Site object
				// because the tool is using an old JSF version, which does not support calling
				// methods with parameters in a JSP file to a backing bean. So, as a hack, we stuff
				// the untruncated site title into the 'infoUrl' String to accomplish this.
				// The site object is never saved, so we don't have to worry about overwriting the 'infoUrl' value
				site.setInfoUrl(getUserSpecificSiteTitle(site, false));

				site.setTitle(getUserSpecificSiteTitle(site, true));
				termsToSites.get(term).add(site);
			}


			terms = new ArrayList<Term>();

			for (String name : termNames) {
				terms.add(new Term(name, termsToSites.get(name)));
			}

			termOrder = PortalUtils.getPortalTermOrder(null);

			Collections.sort(terms);
		}


		public List<Term> getTerms() {
			return terms;
		}

		private String determineTerm(Site site) {
			ResourceProperties siteProperties = site.getProperties();

			String type = site.getType();

			if (isCourseType(type)) {
				String term = siteProperties.getProperty(Site.PROP_SITE_TERM);
				if (term == null) {
					term = msgs.getString("moresite_no_term");
				}

				return term;
			} else if (isProjectType(type)) {
				return msgs.getString("moresite_projects");
			} else if ("portfolio".equals(type)) {
				return msgs.getString("moresite_portfolios");
			} else if ("admin".equals(type)) {
				return msgs.getString("moresite_administration");
			} else {
				return msgs.getString("moresite_other");
			}
		}

		public List<String> getSiteTypeStrings(String type)
		{
			String[] siteTypes = ServerConfigurationService.getStrings(type + "SiteType");
			if (siteTypes == null || siteTypes.length == 0)
			{
				siteTypes = new String[] {type};
			}
			return Arrays.asList(siteTypes);
		}

		private boolean isCourseType(String type)
		{
			return getSiteTypeStrings("course").contains(type);
		}

		private boolean isProjectType(String type)
		{
			return getSiteTypeStrings("project").contains(type);
		}
	}

	public TermSites getTermSites() {
		List<Site> mySites = (List<Site>)SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
				null, null, null,
				org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC,
				null);

		return new TermSites(mySites);
	}

	public String getHiddenSites() {
		Preferences prefs = m_preferencesService.getPreferences(getUserId());
		ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
		List currentHiddenSites = props.getPropertyList(EXCLUDE_SITE_LISTS);

		StringBuilder result = new StringBuilder();
		if (currentHiddenSites != null) {
			for (Object siteId : currentHiddenSites) {
				if (result.length() > 0) {
					result.append(",");
				}
				result.append(siteId);
			}
		}

		return result.toString();
	}

	public void setHiddenSites(String hiddenSiteCSV) {
		this.hiddenSitesInput = hiddenSiteCSV;
	}


	/**
	 * Gets the name of the service.
	 * @return The name of the service that should be shown to users.
	 */
	public String getServiceName() {
		return ServerConfigurationService.getString("ui.name", "Sakai");
	}
	
}
