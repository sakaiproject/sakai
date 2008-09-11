/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.user.tool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.app.syllabus.SyllabusService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.mailarchive.api.MailArchiveService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;

/**
 * UserPrefsTool is the Sakai end-user tool to view and edit one's preferences.
 */
public class UserPrefsTool
{
	/** Our log (commons). */
	private static final Log LOG = LogFactory.getLog(UserPrefsTool.class);

	/** * Resource bundle messages */
	ResourceLoader msgs = new ResourceLoader("user-tool-prefs");

	/** The string that Charon uses for preferences. */
	private static final String CHARON_PREFS = "sakai:portal:sitenav";

	/** The string to get whether privacy status should be visible */
	private static final String ENABLE_PRIVACY_STATUS = "enable.privacy.status";

	/** Should research/collab specific preferences (no syllabus) be displayed */
	private static final String PREFS_RESEARCH = "prefs.research.collab";

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

	/** Session manager (injected dependency) */
	protected SessionManager m_sessionManager = null;

	/** The PreferencesEdit in KeyNameValue collection form. */
	protected Collection m_stuff = null;

	// /** The user id (from the end user) to edit. */
	// protected String m_userId = null;
	/** For display and selection of Items in JSF-- edit.jsp */
	private List prefExcludeItems = new ArrayList();

	private List prefOrderItems = new ArrayList();

	private List prefTimeZones = new ArrayList();

	private List prefLocales = new ArrayList();

	private String DEFAULT_TAB_COUNT = "4";
	private String prefTabCount = null;

	private String[] selectedExcludeItems;

	private String[] selectedOrderItems;

	private String[] tablist;

	private int noti_selection, tab_selection, timezone_selection, language_selection, privacy_selection,j;

	//The preference list names
	private String Notification="prefs_noti_title";
	private String CustomTab="prefs_tab_title";
	private String Timezone="prefs_timezone_title";
	private String Language="prefs_lang_title";
	private String Privacy="prefs_privacy_title";
	
	private boolean refreshMode=false;


	protected final static String EXCLUDE_SITE_LISTS = "exclude";

	protected final static String ORDER_SITE_LISTS = "order";

	protected boolean isNewUser = false;

	protected boolean tabUpdated = false;

	// user's currently selected time zone
	private TimeZone m_timeZone = null;

	// user's currently selected regional language locale
	private Locale m_locale = null;

	private LocaleComparator localeComparator = new LocaleComparator();

	/** The user id retrieved from UsageSessionService */
	private String userId = "";

	private String SAKAI_LOCALES = "locales";

	/**
	 * SAK-11460:  With DTHML More Sites, there are potentially two
	 * "Customize Tab" pages, namely "tab.jsp" (for the pre-DHTML more
	 * sites), and "tab-dhtml-moresites.jsp".  Which one is used depends on the
	 * sakai.properties "portal.use.dhtml.more".  Default is to use
	 * the pre-DTHML page.
	 */
	private String m_TabOutcome = "tab";

	// //////////////////////////////// PROPERTY GETTER AND SETTER ////////////////////////////////////////////
	/**
	 * @return Returns the ResourceLoader value. Note: workaround for <f:selectItem> element, which doesn't like using the <f:loadBundle> map variable
	 */
	public String getMsgNotiAnn1()
	{
		return msgs.getString("noti_ann_1");
	}

	public String getMsgNotiAnn2()
	{
		return msgs.getString("noti_ann_2");
	}

	public String getMsgNotiAnn3()
	{
		return msgs.getString("noti_ann_3");
	}

	public String getMsgNotiMail1()
	{
		return msgs.getString("noti_mail_1");
	}

	public String getMsgNotiMail2()
	{
		return msgs.getString("noti_mail_2");
	}

	public String getMsgNotiMail3()
	{
		return msgs.getString("noti_mail_3");
	}

	public String getMsgNotiRsrc1()
	{
		return msgs.getString("noti_rsrc_1");
	}

	public String getMsgNotiRsrc2()
	{
		return msgs.getString("noti_rsrc_2");
	}

	public String getMsgNotiRsrc3()
	{
		return msgs.getString("noti_rsrc_3");
	}

	public String getMsgNotiSyll1()
	{
		return msgs.getString("noti_syll_1");
	}

	public String getMsgNotiSyll2()
	{
		return msgs.getString("noti_syll_2");
	}

	public String getMsgNotiSyll3()
	{
		return msgs.getString("noti_syll_3");
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setPrefExcludeItems(List " + prefExcludeItems + ")");
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setPrefOrderItems(List " + prefOrderItems + ")");
		}

		this.prefOrderItems = prefOrderItems;
	}

	/**
	 ** @return number of worksite tabs to display in standard site navigation bar
	 **/
	public String getTabCount()
	{
		if ( prefTabCount != null )
			return prefTabCount;

		Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
		ResourceProperties props = prefs.getProperties(CHARON_PREFS);
		prefTabCount = props.getProperty("tabs");

		if ( prefTabCount == null )
			prefTabCount = DEFAULT_TAB_COUNT; 

		return prefTabCount;
	}

	/**
	 ** @param count 
	 **			number of worksite tabs to display in standard site navigation bar
	 **/
	public void setTabCount( String count )
	{
		if ( count == null || count.trim().equals("") )
			prefTabCount = DEFAULT_TAB_COUNT; 
		else
			prefTabCount = count.trim();

		if ( Integer.parseInt(prefTabCount) < Integer.parseInt(DEFAULT_TAB_COUNT) )
			prefTabCount = count;
	}

	/**
	 * @return Returns the prefTimeZones.
	 */
	public List getPrefTimeZones()
	{
		if (prefTimeZones.size() == 0)
		{
			String[] timeZoneArray = TimeZone.getAvailableIDs();
			Arrays.sort(timeZoneArray);
			for (int i = 0; i < timeZoneArray.length; i++)
				prefTimeZones.add(new SelectItem(timeZoneArray[i], timeZoneArray[i]));
		}

		return prefTimeZones;
	}

	/**
	 * @param prefTimeZones
	 *        The prefTimeZones to set.
	 */
	public void setPrefTimeZones(List prefTimeZones)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setPrefTimeZones(List " + prefTimeZones + ")");
		}

		this.prefTimeZones = prefTimeZones;
	}

	/**
	 * *
	 * 
	 * @return Locale based on its string representation (language_region)
	 */
	private Locale getLocaleFromString(String localeString)
	{
		String[] locValues = localeString.trim().split("_");
		if (locValues.length > 1)
			return new Locale(locValues[0], locValues[1]); // language, country
		else if (locValues.length == 1)
			return new Locale(locValues[0]); // just language
		else
			return Locale.getDefault();
	}

	/**
	 * @return Returns the prefLocales
	 */
	public List getPrefLocales()
	{
		// Initialize list of supported locales, if necessary
		if (prefLocales.size() == 0)
		{
			Locale[] localeArray = null;
			String localeString = ServerConfigurationService.getString(SAKAI_LOCALES);

			if (localeString != null && !localeString.equals(""))
			{
				String[] sakai_locales = localeString.split(",");
				localeArray = new Locale[sakai_locales.length + 1];
				for (int i = 0; i < sakai_locales.length; i++)
					localeArray[i] = getLocaleFromString(sakai_locales[i]);
				localeArray[localeArray.length - 1] = Locale.getDefault();
			}
			else
				// if no locales specified, get default list
			{
				localeArray = new Locale[] { Locale.getDefault() };
			}

			// Sort locales and add to prefLocales (removing duplicates)
			Arrays.sort(localeArray, localeComparator);
			for (int i = 0; i < localeArray.length; i++)
			{
				if (i == 0 || !localeArray[i].equals(localeArray[i - 1]))
					prefLocales.add(new SelectItem(localeArray[i].toString(), localeArray[i].getDisplayName()));
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setPrefLocales(List " + prefLocales + ")");
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedExcludeItems(String[] " + Arrays.toString(selectedExcludeItems) + ")");
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedOrderItems(String[] " + Arrays.toString(selectedOrderItems) + ")");
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
	 * @param selectedTimeZone
	 *        The selectedTimeZone to set.
	 */
	public void setSelectedTimeZone(String selectedTimeZone)
	{
		if (selectedTimeZone != null)
			m_timeZone = TimeZone.getTimeZone(selectedTimeZone);
		else
			LOG.warn(this + "setSelctedTimeZone() has null TimeZone");
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

		if (hasValue(prefLocale))
			m_locale = getLocaleFromString(prefLocale);
		else
			m_locale = Locale.getDefault();

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
		if (selectedLocale != null) m_locale = getLocaleFromString(selectedLocale);
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setUserId(String " + userId + ")");
		}
		this.userId = userId;
	}

	/**
	 * @param mgr
	 *        The preferences service.
	 */
	public void setPreferencesService(PreferencesService mgr)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setPreferencesService(PreferencesService " + mgr + ")");
		}

		m_preferencesService = mgr;
	}

	/**
	 * @param mgr
	 *        The session manager.
	 */
	public void setSessionManager(SessionManager mgr)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSessionManager(SessionManager " + mgr + ")");
		}

		m_sessionManager = mgr;
	}

	/**
	 * @return Returns the tabUpdated.
	 */
	public boolean isTabUpdated()
	{
		return tabUpdated;
	}

	/**
	 * @param tabUpdated
	 *        The tabUpdated to set.
	 */
	public void setTabUpdated(boolean tabUpdated)
	{
		this.tabUpdated = tabUpdated;
	}

	// /////////////////////////////////////// CONSTRUCTOR ////////////////////////////////////////////
	/**
	 * no-arg constructor.
	 */
	public UserPrefsTool()
	{
		// Is DTHML more site enabled?
		if (ServerConfigurationService.getBoolean ("portal.use.dhtml.more", false))
			m_TabOutcome = "tabDHTMLMoreSites";
		else
			m_TabOutcome = "tab";

		//Tab order configuration
		String defaultPreference="prefs_tab_title, prefs_noti_title, prefs_timezone_title, prefs_lang_title";

		if (ServerConfigurationService.getString("preference.pages")==null)
		{
			LOG.warn("The preference.pages is not specified in sakai.properties, hence the default option of 'prefs_tab_title, prefs_noti_title, prefs_timezone_title, prefs_lang_title' is considered");
		}
		else
		{
			LOG.info("Setting preference.pages as "+ ServerConfigurationService.getString("preference.pages"));
		}
		
		//To indicate that it is in the refresh mode
		refreshMode=true;
		String tabOrder=ServerConfigurationService.getString("preference.pages",defaultPreference);

		tablist=tabOrder.split(",");

		for(int i=0; i<tablist.length; i++)
		{
			tablist[i]=tablist[i].trim();			
			if(tablist[i].equals(Notification)) noti_selection=i+1;
			else if(tablist[i].equals(CustomTab)) tab_selection=i+1;
			else if(tablist[i].equals(Timezone)) timezone_selection=i+1;
			else if (tablist[i].equals(Language)) language_selection=i+1;
			else if (tablist[i].equals(Privacy)) privacy_selection=i+1;
			else LOG.warn(tablist[i] + " is not valid!!! Re-configure preference.pages at sakai.properties");
		}

		//defaultPage=tablist[0];

		// Set the default tab count to the system property, initially.
		DEFAULT_TAB_COUNT = ServerConfigurationService.getString ("portal.default.tabs", DEFAULT_TAB_COUNT);

		LOG.debug("new UserPrefsTool()");
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

	public int getTab_selection()
	{
		//Loading the data for customize tab in the refresh mode
		if (tab_selection==1 && refreshMode==true)
		{
			processActionEdit();
		}
		return tab_selection;
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



	public String getTabTitle()
	{
		return "tabtitle";
	}

	// Naming in faces-config.xml Refresh jsp- "refresh" , Notification jsp- "noti" , tab cutomization jsp- "tab"
	// ///////////////////////////////////// PROCESS ACTION ///////////////////////////////////////////
	/**
	 * Process the add command from the edit view.
	 * 
	 * @return navigation outcome to tab customization page (edit)
	 */
	public String processActionAdd()
	{
		LOG.debug("processActionAdd()");
		tabUpdated = false; // reset successful text message if existing with same jsp
		String[] values = getSelectedExcludeItems();
		if (values.length < 1)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(msgs.getString("add_to_sites_msg")));
			return m_TabOutcome;
		}

		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];
			getPrefOrderItems().add(removeItems(value, getPrefExcludeItems(), ORDER_SITE_LISTS, EXCLUDE_SITE_LISTS));
		}
		return m_TabOutcome;
	}

	/**
	 * Process remove from order list command
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionRemove()
	{
		LOG.debug("processActionRemove()");
		tabUpdated = false; // reset successful text message if existing in jsp
		String[] values = getSelectedOrderItems();
		if (values.length < 1)
		{
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(msgs.getString("remove_from_sites_msg")));
			return m_TabOutcome;
		}

		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];
			getPrefExcludeItems().add(removeItems(value, getPrefOrderItems(), EXCLUDE_SITE_LISTS, ORDER_SITE_LISTS));
		}
		return m_TabOutcome;
	}

	/**
	 * Process Add All action
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionAddAll()
	{
		LOG.debug("processActionAddAll()");

		getPrefOrderItems().addAll(getPrefExcludeItems());
		getPrefExcludeItems().clear();
		tabUpdated = false; // reset successful text message if existing in jsp
		return m_TabOutcome;
	}

	/**
	 * Process Remove All command
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionRemoveAll()
	{
		LOG.debug("processActionRemoveAll()");

		getPrefExcludeItems().addAll(getPrefOrderItems());
		getPrefOrderItems().clear();
		tabUpdated = false; // reset successful text message if existing in jsp
		return m_TabOutcome;
	}

	/**
	 * Move Up the selected item in Ordered List
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionMoveUp()
	{
		LOG.debug("processActionMoveUp()");
		return doSiteMove(true, false); //moveUp = true, absolute = false
	}

	/**
	 * Move down the selected item in Ordered List
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionMoveDown()
	{
		LOG.debug("processActionMoveDown()");
		return doSiteMove(false, false); //moveUp = false, absolute = false
	}

	public String processActionMoveTop()
	{
		LOG.debug("processActionMoveTop()");
		return doSiteMove(true, true); //moveUp = true, absolute = true
	}

	public String processActionMoveBottom()
	{
		LOG.debug("processActionMoveBottom()");
		return doSiteMove(false, true); //moveUp = false, absolute = true
	}

	private String doSiteMove(boolean moveUp, boolean absolute) {
		tabUpdated = false;
		Set<String> selected   = new HashSet(Arrays.asList(getSelectedOrderItems()));
		List<SelectItem> toMove = new ArrayList<SelectItem>();

		//Prune bad selections and split lists if moving absolutely
		for (Iterator i = prefOrderItems.iterator(); i.hasNext(); ) {
			SelectItem item = (SelectItem) i.next();
			if (selected.contains(item.getValue())) {
				toMove.add(item);
				if (absolute)
					i.remove();
			}
		}

		if (toMove.size() == 0) {
			String message = msgs.getString(moveUp ? "move_up_msg" : "move_down_msg");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(message));
		}
		else {
			if (absolute) {
				//Move the selected list, in order to the right spot.
				if (moveUp)
					prefOrderItems.addAll(0, toMove);
				else
					prefOrderItems.addAll(toMove);
			}
			else {
				//Iterate in the right direction
				int start = 0;
				int interval = 1;
				int end = prefOrderItems.size() - 1;

				if (!moveUp) {
					start = prefOrderItems.size() - 1;
					interval = -1;
					end = 0;
				}

				for (int i = start; i != end; i += interval) {
					SelectItem cur  = (SelectItem) prefOrderItems.get(i);
					SelectItem next = (SelectItem) prefOrderItems.get(i + interval);
					if (toMove.contains(next) && !toMove.contains(cur)) {
						prefOrderItems.set(i, next);
						prefOrderItems.set(i + interval, cur);
						toMove.remove(next);
					}
				}
			}
		}
		return m_TabOutcome;

	}

	/**
	 * Process the edit command.
	 * 
	 * @return navigation outcome to tab customization page (edit)
	 */
	public String processActionEdit()
	{
		LOG.debug("processActionEdit()");

		if (!hasValue(getUserId()))
		{
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(msgs.getString("user_missing")));
			return null;
		}
		tabUpdated = false; // Reset display of text message on JSP
		refreshMode=false;
		prefExcludeItems = new ArrayList();
		prefOrderItems = new ArrayList();
		setUserEditingOn();
		List prefExclude = new Vector();
		List prefOrder = new Vector();

		Preferences prefs = m_preferencesService.getPreferences(getUserId());
		ResourceProperties props = prefs.getProperties(CHARON_PREFS);
		List l = props.getPropertyList("exclude");
		if (l != null)
		{
			prefExclude = l;
		}

		l = props.getPropertyList("order");
		if (l != null)
		{
			prefOrder = l;
		}

		List mySites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null, null,
				org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
		// create excluded and order list of Sites and add balance mySites to excluded Site list for display in Form
		List ordered = new Vector();
		List excluded = new Vector();
		for (Iterator i = prefOrder.iterator(); i.hasNext();)
		{
			String id = (String) i.next();
			// find this site in the mySites list
			int pos = indexOf(id, mySites);
			if (pos != -1)
			{
				// move it from mySites to order
				Site s = (Site) mySites.get(pos);
				ordered.add(s);
				mySites.remove(pos);
			}
		}
		for (Iterator iter = prefExclude.iterator(); iter.hasNext();)
		{
			String element = (String) iter.next();
			int pos = indexOf(element, mySites);
			if (pos != -1)
			{
				Site s = (Site) mySites.get(pos);
				excluded.add(s);
				mySites.remove(pos);
			}
		}
		// pick up the rest of the sites if not available with exclude and order list
		// and add to the ordered list as when a new site is created it is displayed in header
		ordered.addAll(mySites);

		// Now convert to SelectItem for display in JSF
		for (Iterator iter = excluded.iterator(); iter.hasNext();)
		{
			Site element = (Site) iter.next();
			SelectItem excludeItem = new SelectItem(element.getId(), element.getTitle());
			prefExcludeItems.add(excludeItem);
		}

		for (Iterator iter = ordered.iterator(); iter.hasNext();)
		{
			Site element = (Site) iter.next();
			SelectItem orderItem = new SelectItem(element.getId(), element.getTitle());
			prefOrderItems.add(orderItem);
		}
		// release lock
		m_preferencesService.cancel(m_edit);
		return m_TabOutcome;
	}

	/**
	 * Process the save command from the edit view.
	 * 
	 * @return navigation outcome to tab customization page (edit)
	 */
	public String processActionSave()
	{
		LOG.debug("processActionSave()");

		setUserEditingOn();
		// Remove existing property
		ResourcePropertiesEdit props = m_edit.getPropertiesEdit(CHARON_PREFS);
		props.removeProperty("exclude");
		props.removeProperty("order");
		// Commit to remove from database, for next set of value storing
		m_preferencesService.commit(m_edit);

		m_stuff = new Vector();
		String oparts = "";
		String eparts = "";
		for (int i = 0; i < prefExcludeItems.size(); i++)
		{
			SelectItem item = (SelectItem) prefExcludeItems.get(i);
			String evalue = (String) item.getValue();
			eparts += evalue + ", ";
		}
		for (int i = 0; i < prefOrderItems.size(); i++)
		{
			SelectItem item = (SelectItem) prefOrderItems.get(i);
			String value = (String) item.getValue();
			oparts += value + ", ";
		}
		// add property name and value for saving
		m_stuff.add(new KeyNameValue(CHARON_PREFS, "exclude", eparts, true));
		m_stuff.add(new KeyNameValue(CHARON_PREFS, "order", oparts, true));
		m_stuff.add(new KeyNameValue(CHARON_PREFS, "tabs", prefTabCount, false));

		// save
		saveEdit();
		// release lock and clear session variables
		cancelEdit();
		// To stay on the same page - load the page data
		processActionEdit();
		tabUpdated = true; // set for display of text message on JSP

		// schedule a "peer" html element refresh, to update the site nav tabs
		// TODO: hard coding this frame id is fragile, portal dependent, and needs to be fixed -ggolden
		setRefreshElement("sitenav");

		return m_TabOutcome;
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to tab customization page (edit)
	 */
	public String processActionCancel()
	{
		LOG.debug("processActionCancel()");

		prefTabCount = null; // reset to retrieve original prefs

		// remove session variables
		cancelEdit();
		// To stay on the same page - load the page data
		processActionEdit();
		return m_TabOutcome;
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to Notification page (list)
	 */
	public String processActionNotiFrmEdit()
	{
		LOG.debug("processActionNotiFrmEdit()");
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
		LOG.debug("processActionTZFrmEdit()");

		refreshMode=false;
		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "timezone";
	}

	/**
	 * Process the cancel command from the edit view.
	 * 
	 * @return navigation outcome to locale page (list)
	 */
	public String processActionLocFrmEdit()
	{
		LOG.debug("processActionLocFrmEdit()");

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
		LOG.debug("processActionPrivFrmEdit()");

		cancelEdit();
		// navigation page data are loaded through getter method as navigation is the default page for 'sakai.preferences' tool.
		return "privacy";
	}

	/**
	 * This is called from edit page for navigation to refresh page
	 * 
	 * @return navigation outcome to refresh page (refresh)
	 */
	public String processActionRefreshFrmEdit()
	{
		LOG.debug("processActionRefreshFrmEdit()");

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
		LOG.debug("cancelEdit()");

		// cleanup
		m_stuff = null;
		m_edit = null;
		prefExcludeItems = new ArrayList();
		prefOrderItems = new ArrayList();
		isNewUser = false;

		tabUpdated = false;
		notiUpdated = false;
		tzUpdated = false;
		locUpdated = false;
		refreshUpdated = false;
	}

	/**
	 * used with processActionAdd() and processActionRemove()
	 * 
	 * @return SelectItem
	 */
	private SelectItem removeItems(String value, List items, String addtype, String removetype)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeItems(String " + value + ", List " + items + ", String " + addtype + ", String " + removetype + ")");
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
		LOG.debug("setUserEditingOn()");

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
		LOG.debug("saveEdit()");

		// user editing is required as commit() disable isActive() flag
		setUserEditingOn();
		// move the stuff from m_stuff into the edit
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("hasValue(String " + eval + ")");
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("indexOf(String " + value + ", List " + siteList + ")");
		}

		for (int i = 0; i < siteList.size(); i++)
		{
			Site site = (Site) siteList.get(i);
			if (site.equals(value))
			{
				return i;
			}
		}
		return -1;
	}

	// ////////////////////////////////// NOTIFICATION ACTIONS ////////////////////////////////
	private String selectedAnnItem = "";

	private String selectedMailItem = "";

	private String selectedRsrcItem = "";

	private String selectedSyllItem = "";

	protected boolean notiUpdated = false;

	protected boolean tzUpdated = false;

	protected boolean locUpdated = false;

	// ///////////////////////////////// GETTER AND SETTER ///////////////////////////////////
	// TODO chec for any preprocessor for handling request for first time. This can simplify getter() methods as below
	/**
	 * @return Returns the selectedAnnItem.
	 */
	public String getSelectedAnnItem()
	{
		LOG.debug("getSelectedAnnItem()");

		if (!hasValue(selectedAnnItem))
		{
			Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
			String a = buildTypePrefsContext(AnnouncementService.APPLICATION_ID, "annc", selectedAnnItem, prefs);
			if (hasValue(a))
			{
				selectedAnnItem = a; // load from saved data
			}
			else
			{
				selectedAnnItem = "3"; // default setting
			}
		}
		return selectedAnnItem;
	}

	/**
	 * @param selectedAnnItem
	 *        The selectedAnnItem to set.
	 */
	public void setSelectedAnnItem(String selectedAnnItem)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedAnnItem(String " + selectedAnnItem + ")");
		}

		this.selectedAnnItem = selectedAnnItem;
	}

	/**
	 * @return Returns the selectedMailItem.
	 */
	public String getSelectedMailItem()
	{
		LOG.debug("getSelectedMailItem()");

		if (!hasValue(this.selectedMailItem))
		{
			Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
			String a = buildTypePrefsContext(MailArchiveService.APPLICATION_ID, "mail", selectedMailItem, prefs);
			if (hasValue(a))
			{
				selectedMailItem = a; // load from saved data
			}
			else
			{
				selectedMailItem = "3"; // default setting
			}
		}
		return selectedMailItem;
	}

	/**
	 * @param selectedMailItem
	 *        The selectedMailItem to set.
	 */
	public void setSelectedMailItem(String selectedMailItem)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedMailItem(String " + selectedMailItem + ")");
		}

		this.selectedMailItem = selectedMailItem;
	}

	/**
	 * @return Returns the selectedRsrcItem.
	 */
	public String getSelectedRsrcItem()
	{
		LOG.debug("getSelectedRsrcItem()");

		if (!hasValue(this.selectedRsrcItem))
		{
			Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
			String a = buildTypePrefsContext(ContentHostingService.APPLICATION_ID, "rsrc", selectedRsrcItem, prefs);
			if (hasValue(a))
			{
				selectedRsrcItem = a; // load from saved data
			}
			else
			{
				selectedRsrcItem = "3"; // default setting
			}
		}
		return selectedRsrcItem;
	}

	/**
	 * @param selectedRsrcItem
	 *        The selectedRsrcItem to set.
	 */
	public void setSelectedRsrcItem(String selectedRsrcItem)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedRsrcItem(String " + selectedRsrcItem + ")");
		}

		this.selectedRsrcItem = selectedRsrcItem;
	}

	// syllabus
	public String getSelectedSyllItem()
	{
		LOG.debug("getSelectedSyllItem()");

		if (!hasValue(this.selectedSyllItem))
		{
			Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
			String a = buildTypePrefsContext(SyllabusService.APPLICATION_ID, "syll", selectedSyllItem, prefs);
			if (hasValue(a))
			{
				selectedSyllItem = a; // load from saved data
			}
			else
			{
				selectedSyllItem = "3"; // default setting
			}
		}
		return selectedSyllItem;
	}

	public void setSelectedSyllItem(String selectedSyllItem)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedRsrcItem(String " + selectedRsrcItem + ")");
		}

		this.selectedSyllItem = selectedSyllItem;
	}

	/**
	 * @return Returns the notiUpdated.
	 */
	public boolean getNotiUpdated()
	{
		return notiUpdated;
	}

	/**
	 * @param notiUpdated
	 *        The notiUpdated to set.
	 */
	public void setNotiUpdated(boolean notiUpdated)
	{
		this.notiUpdated = notiUpdated;
	}

	/**
	 * @return Returns the tzUpdated.
	 */
	public boolean getTzUpdated()
	{
		return tzUpdated;
	}

	/**
	 * @param notiUpdated
	 *        The tzUpdated to set.
	 */
	public void setTzUpdated(boolean tzUpdated)
	{
		this.tzUpdated = tzUpdated;
	}

	/**
	 * @return Returns the tzUpdated.
	 */
	public boolean getLocUpdated()
	{
		return locUpdated;
	}

	/**
	 * @param notiUpdated
	 *        The locUpdated to set.
	 */
	public void setLocUpdated(boolean locUpdated)
	{
		this.locUpdated = locUpdated;
	}

	// ///////////////////////////////////////NOTIFICATION ACTION - copied from NotificationprefsAction.java////////
	// TODO - clean up method call. These are basically copied from legacy legacy implementations.
	/**
	 * Process the save command from the edit view.
	 * 
	 * @return navigation outcome to notification page
	 */
	public String processActionNotiSave()
	{
		LOG.debug("processActionNotiSave()");

		// get an edit
		setUserEditingOn();
		if (m_edit != null)
		{
			readTypePrefs(AnnouncementService.APPLICATION_ID, "annc", m_edit, getSelectedAnnItem());
			readTypePrefs(MailArchiveService.APPLICATION_ID, "mail", m_edit, getSelectedMailItem());
			readTypePrefs(ContentHostingService.APPLICATION_ID, "rsrc", m_edit, getSelectedRsrcItem());
			readTypePrefs(SyllabusService.APPLICATION_ID, "syll", m_edit, getSelectedSyllItem());

			// update the edit and release it
			m_preferencesService.commit(m_edit);
		}
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
		LOG.debug("processActionNotiCancel()");

		loadNotiData();
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

		tzUpdated = true; // set for display of text massage
		return "timezone";
	}

	/**
	 * process timezone cancel
	 * 
	 * @return navigation outcome to timezone page
	 */
	public String processActionTzCancel()
	{
		LOG.debug("processActionTzCancel()");

		// restore original time zone
		m_timeZone = null;
		getSelectedTimeZone();

		return "timezone";
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

		locUpdated = true; // set for display of text massage
		return "locale";
	}

	/**
	 * process locale cancel
	 * 
	 * @return navigation outcome to locale page
	 */
	public String processActionLocCancel()
	{
		LOG.debug("processActionLocCancel()");

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
		LOG.debug("processActionRefreshFrmNoti()");

		loadRefreshData();
		return "refresh";
	}

	// ////////////////////////////////////// HELPER METHODS FOR NOTIFICATIONS /////////////////////////////////////
	/**
	 * Load saved notification data - this is called from cancel button of notification page as navigation stays in the page
	 */
	protected void loadNotiData()
	{
		LOG.debug("loadNotiData()");

		selectedAnnItem = "";
		selectedMailItem = "";
		selectedRsrcItem = "";
		selectedSyllItem = "";
		notiUpdated = false;
		Preferences prefs = (PreferencesEdit) m_preferencesService.getPreferences(getUserId());
		String a = buildTypePrefsContext(AnnouncementService.APPLICATION_ID, "annc", selectedAnnItem, prefs);
		if (hasValue(a))
		{
			selectedAnnItem = a; // load from saved data
		}
		else
		{
			selectedAnnItem = "2"; // default setting
		}
		String m = buildTypePrefsContext(MailArchiveService.APPLICATION_ID, "mail", selectedMailItem, prefs);
		if (hasValue(m))
		{
			selectedMailItem = m; // load from saved data
		}
		else
		{
			selectedMailItem = "3"; // default setting
		}
		String r = buildTypePrefsContext(ContentHostingService.APPLICATION_ID, "rsrc", selectedRsrcItem, prefs);
		if (hasValue(r))
		{
			selectedRsrcItem = r; // load from saved data
		}
		else
		{
			selectedRsrcItem = "3"; // default setting
		}
		// syllabus
		String s = buildTypePrefsContext(SyllabusService.APPLICATION_ID, "syll", selectedSyllItem, prefs);
		if (hasValue(s))
		{
			selectedSyllItem = s; // load from saved data
		}
		else
		{
			selectedSyllItem = "3"; // default setting
		}
	}

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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("readTypePrefs(String " + type + ", String " + prefix + ", PreferencesEdit " + edit + ", String " + data
					+ ")");
		}

		// update the default settings from the form
		ResourcePropertiesEdit props = edit.getPropertiesEdit(NotificationService.PREFS_TYPE + type);

		// read the defaults
		props.addProperty(Integer.toString(NotificationService.NOTI_OPTIONAL), data);

	} // readTypePrefs

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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("buildTypePrefsContext(String " + type + ", String " + prefix + ", String " + context + ", Preferences "
					+ prefs + ")");
		}

		ResourceProperties props = prefs.getProperties(NotificationService.PREFS_TYPE + type);
		String value = props.getProperty(new Integer(NotificationService.NOTI_OPTIONAL).toString());

		return value;
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setSelectedRefreshItem(String " + selectedRefreshItem + ")");
		}

		this.selectedRefreshItem = selectedRefreshItem;
	}

	// /**
	// * process saving of refresh
	// *
	// * @return navigation outcome to refresh page
	// */
	// public String processActionRefreshSave()
	// {
	// LOG.debug("processActionRefreshSave()");
	//
	// // get an edit
	// setUserEditingOn();
	// if (m_edit != null)
	// {
	// setStringPref(PortalService.SERVICE_NAME, "refresh", m_edit, getSelectedRefreshItem());
	// // update the edit and release it
	// m_preferencesService.commit(m_edit);
	// }
	// refreshUpdated = true;
	// return "refresh";
	// }

	/**
	 * Process cancel and navigate to list page.
	 * 
	 * @return navigation outcome to refresh page
	 */
	public String processActionRefreshCancel()
	{
		LOG.debug("processActionRefreshCancel()");

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
		LOG.debug("processActionNotiFrmRefresh()");

		loadNotiData();
		return "noti";
		//return "tab";
	}

	// ///////////////////////////////////// HELPER METHODS FOR REFRESH /////////////////////////
	/**
	 * Load refresh data from stored information. This is called when navigated into this page for first time.
	 */
	protected void loadRefreshData()
	{
		LOG.debug("loadRefreshData()");

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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("setStringPref(String " + pref_base + ", String " + type + ", PreferencesEdit " + edit + ", String " + newval
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
		if (LOG.isDebugEnabled())
		{
			LOG.debug("getStringPref(String " + pref_base + ", String " + type + ", PreferencesEdit " + prefs + ")");
		}

		ResourceProperties props = prefs.getProperties(pref_base);
		String a = props.getProperty(type);
		return a;
	} // getIntegerPref

	/** The html "peer" element to refresh on the next rendering. */
	protected String m_refreshElement = null;

	/**
	 * Get, and clear, the refresh element
	 * 
	 * @return The html "peer" element to refresh on the next rendering, or null if none defined.
	 */
	public String getRefreshElement()
	{
		String rv = m_refreshElement;
		m_refreshElement = null;
		return rv;
	}

	/**
	 * Set the "peer" html element to refresh on the next rendering.
	 * 
	 * @param element
	 */
	public void setRefreshElement(String element)
	{
		m_refreshElement = element;
	}

	/**
	 * Pull whether privacy status should be enabled from sakai.properties
	 * 
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
}
