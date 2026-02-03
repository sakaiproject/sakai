/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.announcement.tool;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * <p>
 * AnnouncementActionState is the state object for the AnnouncementAction tool. This object listens for changes on the announcement, and requests a UI delivery when changes occur.
 * </p>
 */
public class AnnouncementActionState extends ControllerState implements SessionBindingListener
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("announcement");
	
	public static int DEFAULT_DISPLAY_NUMBER_OPTION = 100;
    public static int DEFAULT_DAYS_IN_PAST_OPTION = 30;

	/**
	 * Holds the display options for the Announcements tool
	 */
	static public class DisplayOptions
	{
		private static final String varNameEnforceNumberOfCharsPerAnnouncementLimit = "limitNumberOfCharsPerAnnouncement";

		private static final String varNameEnforceNumberOfAnnouncementsLimit = "limitNumberOfAnnouncements";

		private static final String varNameEnforceNumberOfDaysInPastLimit = "limitNumberOfDaysInPast";

		private static final String varNameShowAnnouncementBody = "showAnnouncementBody";

		private static final String varNameShowAllColumns = "showAllColumns";

		private static final String varNameNumberOfDaysInPast = "days";

		private static final String varNameNumberOfAnnouncements = "items";

		private static final String varNameNumberCharsPerAnnouncement = "length";

		private static final String varNameShowOnlyOptionsButton = "showOnlyOptionsButton";

		private static final String VarNameDisplaySelection = "displaySelection";
		
		private static final String ANNOUNCEMENT_TOOL_ID = "sakai.announcements";

		boolean showAllColumns = true;

		boolean showAnnouncementBody = false;

		int numberOfDaysInThePast = 365;

		boolean enforceNumberOfDaysInThePastLimit;

		int numberOfAnnouncements = DEFAULT_DISPLAY_NUMBER_OPTION;

		boolean enforceNumberOfAnnouncementsLimit;

		int numberOfCharsPerAnnouncement = Integer.MAX_VALUE;

		boolean enforceNumberOfCharsPerAnnouncement;

		boolean showOnlyOptionsButton = false;
      
		/**
		 * Default constructor
		 */
		public DisplayOptions()
		{
		}

		/**
		 * Gets the number of announcements that we will show (if the limit is enabled).
		 */
		public int getNumberOfAnnouncements()
		{
			return numberOfAnnouncements;
		}

		/**
		 * Gets the number of characters that we will show in an announcement (if the limit is enabled).
		 */
		public int getNumberOfCharsPerAnnouncement()
		{
			return numberOfCharsPerAnnouncement;
		}

		/**
		 * Gets the number of days in the past for which we will show announcments (if the limit is enabled).
		 */
		public int getNumberOfDaysInThePast()
		{
			return numberOfDaysInThePast;
		}

		/**
		 * Gets whether or not we should show the announcement body.
		 */
		public boolean isShowAnnouncementBody()
		{
			return showAnnouncementBody;
		}

		/**
		 * Gets whether or not we should show all the columns associated with the announcement.
		 */
		public boolean isShowAllColumns()
		{
			return showAllColumns;
		}

		/**
		 * Sets the limit on the number of announcements to show (if the limit is enabled).
		 */
		public void setNumberOfAnnouncements(int i)
		{
			// Force the setting to be greater than or equal to zero since
			// a negative value doesn't make sense.
			numberOfAnnouncements = Math.max(i, 0);
		}

		/**
		 * Sets the number of characters to show per announcement (if the limit is enabled).
		 */
		public void setNumberOfCharsPerAnnouncement(int i)
		{
			// Force the setting to be greater than or equal to zero since
			// a negative value doesn't make sense.
			numberOfCharsPerAnnouncement = Math.max(i, 0);
		}

		/**
		 * Sets the number of days in the past for which we will show announcments (if the limit is enabled).
		 */
		public void setNumberOfDaysInThePast(int i)
		{
			// Force the setting to be greater than or equal to zero since
			// a negative value doesn't make sense.
			numberOfDaysInThePast = Math.max(i, 0);
		}

		/**
		 * Sets whether or not we should show the announcement body.
		 */
		public void setShowAnnouncementBody(boolean b)
		{
			showAnnouncementBody = b;
		}

		/**
		 * Sets whether or not we should show all the columns associated with the announcement.
		 */
		public void setShowAllColumns(boolean b)
		{
			showAllColumns = b;
		}

		/**
		 * Returns true if we should limit the number of announcments shown.
		 */
		public boolean isEnforceNumberOfAnnouncementsLimit()
		{
			return enforceNumberOfAnnouncementsLimit;
		}

		/**
		 * Returns true if we should limit the number of characters per announcement.
		 */
		public boolean isEnforceNumberOfCharsPerAnnouncement()
		{
			return enforceNumberOfCharsPerAnnouncement;
		}

		/**
		 * Returns true if we should limit the announcements displayed based on the number of days in the past on which the occurred.
		 */
		public boolean isEnforceNumberOfDaysInThePastLimit()
		{
			return enforceNumberOfDaysInThePastLimit;
		}

		/**
		 * Sets whether or not we should limit the number of announcements displayed.
		 */
		public void setEnforceNumberOfAnnouncementsLimit(boolean b)
		{
			enforceNumberOfAnnouncementsLimit = b;
		}

		/**
		 * Sets whether or not we should limit the number of chars per announcement.
		 */
		public void setEnforceNumberOfCharsPerAnnouncement(boolean b)
		{
			enforceNumberOfCharsPerAnnouncement = b;
		}

		/**
		 * Sets whether or not we shoud limit the announcements displayed based on the number of days in the past on which they occurred.
		 */
		public void setEnforceNumberOfDaysInThePastLimit(boolean b)
		{
			enforceNumberOfDaysInThePastLimit = b;
		}

		/**
		 * Utility routine used to get an integer named value from a map or supply a default value if none is found.
		 */
		private int getIntegerParameter(Map<?, ?> params, String paramName, int defaultValue)
		{
            int value = defaultValue;
            String intValString = (String) params.get(paramName);
            if (StringUtils.trimToNull(intValString) != null) {
                try {
                    value = Integer.parseInt(intValString);
                } catch (NumberFormatException e) {
                    value = defaultValue;
                }
            } else {
                value = defaultValue;
            }
            return value;
		}

		/**
		 * Utility routine used to get an boolean named value from a map or supply a default value if none is found.
		 */
		boolean getBooleanParameter(Map<?, ?> params, String paramName, boolean defaultValue)
		{
			String booleanValString = (String) params.get(paramName);

			if (StringUtils.trimToNull(booleanValString) != null)
			{
				return Boolean.valueOf(booleanValString).booleanValue();
			}
			else
			{
				return defaultValue;
			}
		}

		/**
		 * Loads properties from a map into our object.
		 */
		public void loadProperties(Map<?, ?> params)
		{
			setShowAllColumns(getBooleanParameter(params, varNameShowAllColumns, showAllColumns));
			setShowAnnouncementBody(getBooleanParameter(params, varNameShowAnnouncementBody, showAnnouncementBody));
			setShowOnlyOptionsButton(getBooleanParameter(params, varNameShowOnlyOptionsButton, showOnlyOptionsButton));

			if (params.get(varNameNumberOfDaysInPast) != null)
			{
				setNumberOfDaysInThePast(getIntegerParameter(params, varNameNumberOfDaysInPast, numberOfDaysInThePast));
				setEnforceNumberOfDaysInThePastLimit(true);
			}
			else
			{
				setEnforceNumberOfDaysInThePastLimit(false);
			}

			if (params.get(varNameNumberOfAnnouncements) != null)
			{
				setNumberOfAnnouncements(getIntegerParameter(params, varNameNumberOfAnnouncements, numberOfAnnouncements));
				setEnforceNumberOfAnnouncementsLimit(true);
			}
			else
			{
				setEnforceNumberOfAnnouncementsLimit(false);
			}

			if (params.get(varNameNumberCharsPerAnnouncement) != null)
			{
				setNumberOfCharsPerAnnouncement(getIntegerParameter(params, varNameNumberCharsPerAnnouncement,
						numberOfCharsPerAnnouncement));
				setEnforceNumberOfCharsPerAnnouncement(true);
			}
			else
			{
				setEnforceNumberOfCharsPerAnnouncement(false);
			}
		}

		/**
		 * Loads properties from a ParameterParser object (usually gotten from a user's page submission).
		 */
		public void loadProperties(ParameterParser parameters)
		{
			Tool tool = ToolManager.getCurrentTool();
			if (tool.getId().equals(ANNOUNCEMENT_TOOL_ID))
			{
				String VarNameDisplaySelection = parameters.getString("VarNameDisplaySelection");
				if (VarNameDisplaySelection.equals("sortable"))
				{
					setShowAllColumns(true);
					setShowAnnouncementBody(false);

					setNumberOfCharsPerAnnouncement(numberOfCharsPerAnnouncement);
					setEnforceNumberOfCharsPerAnnouncement(false);
				}
				else if (VarNameDisplaySelection.equals("sortableWithBody"))
				{
					setShowAllColumns(true);
					setShowAnnouncementBody(true);

					String varNameNumberChars = parameters.getString("changeChars");
					if (varNameNumberChars.equals(rb.getString("custom.shofir")))
					{
						setNumberOfCharsPerAnnouncement(50);
						setEnforceNumberOfCharsPerAnnouncement(true);
					}
					else if (varNameNumberChars.equals(rb.getString("custom.shofirtwo")))
					{
						setNumberOfCharsPerAnnouncement(100);
						setEnforceNumberOfCharsPerAnnouncement(true);
					}
					else if (varNameNumberChars.equals(rb.getString("custom.shoall")))
					{
						setNumberOfCharsPerAnnouncement(numberOfCharsPerAnnouncement);
						setEnforceNumberOfCharsPerAnnouncement(false);
					}
				}
				else if (VarNameDisplaySelection.equals("list"))
				{
					setShowAllColumns(false);
					setShowAnnouncementBody(true);

					String varNameNumberChars = parameters.getString("changeChars");
					if (varNameNumberChars.equals(rb.getString("custom.shofir")))
					{
						setNumberOfCharsPerAnnouncement(50);
						setEnforceNumberOfCharsPerAnnouncement(true);
					}
					else if (varNameNumberChars.equals(rb.getString("custom.shofirtwo")))
					{
						setNumberOfCharsPerAnnouncement(100);
						setEnforceNumberOfCharsPerAnnouncement(true);
					}
					else if (varNameNumberChars.equals(rb.getString("custom.shoall")))
					{
						setNumberOfCharsPerAnnouncement(numberOfCharsPerAnnouncement);
						setEnforceNumberOfCharsPerAnnouncement(false);
					}
				}
			}
			else
			{
				setShowAllColumns(parameters.getBoolean(varNameShowAllColumns));
				setShowAnnouncementBody(parameters.getBoolean(varNameShowAnnouncementBody));

				String varNameNumberChars = parameters.getString("changeChars");
				if (varNameNumberChars.equals(rb.getString("custom.shofir")))
				{
					setNumberOfCharsPerAnnouncement(50);
					setEnforceNumberOfCharsPerAnnouncement(true);
				}
				else if (varNameNumberChars.equals(rb.getString("custom.shofirtwo")))
				{
					setNumberOfCharsPerAnnouncement(100);
					setEnforceNumberOfCharsPerAnnouncement(true);
				}
				else if (varNameNumberChars.equals(rb.getString("custom.shoall")))
				{
					setNumberOfCharsPerAnnouncement(numberOfCharsPerAnnouncement);
					setEnforceNumberOfCharsPerAnnouncement(false);
				}
			}
			// if this parameter has been defined, use its value to replace the current setting
			// otherwise, leave alone the current setting
			if (parameters.get(varNameShowOnlyOptionsButton) != null)
			{
				setShowOnlyOptionsButton(parameters.getBoolean(varNameShowOnlyOptionsButton));
			}

			setNumberOfDaysInThePast(parameters.getInt(varNameNumberOfDaysInPast, DEFAULT_DAYS_IN_PAST_OPTION));
			setEnforceNumberOfDaysInThePastLimit(StringUtils.trimToNull(parameters.get(varNameNumberOfDaysInPast)) != null);

			setNumberOfAnnouncements(parameters.getInt(varNameNumberOfAnnouncements, DEFAULT_DISPLAY_NUMBER_OPTION));
			setEnforceNumberOfAnnouncementsLimit(StringUtils.trimToNull(parameters.get(varNameNumberOfAnnouncements)) != null);

		}

		/**
		 * Saves the properties in this object to a ResourcePropertiesEdit object.
		 */
		public void saveProperties(Properties resEdit)
		{
			resEdit.setProperty(varNameShowAllColumns, Boolean.toString(showAllColumns));
			resEdit.setProperty(varNameShowAnnouncementBody, Boolean.toString(showAnnouncementBody));
			resEdit.setProperty(varNameShowOnlyOptionsButton, Boolean.toString(showOnlyOptionsButton));
			if (isEnforceNumberOfDaysInThePastLimit())
			{
				resEdit.setProperty(varNameNumberOfDaysInPast, Integer.toString(numberOfDaysInThePast));
			}
			else
			{
				// Since the absence of a property means that there are no limits, remove the
				// property from the resEdit object, in case it is already present.
				resEdit.remove(varNameNumberOfDaysInPast);
			}

			if (this.isEnforceNumberOfAnnouncementsLimit())
			{
				resEdit.setProperty(varNameNumberOfAnnouncements, Integer.toString(numberOfAnnouncements));
			}
			else
			{
				// Since the absence of a property means that there are no limits, remove the
				// property from the resEdit object, in case it is already present.
				resEdit.remove(varNameNumberOfAnnouncements);
			}

			if (this.isEnforceNumberOfCharsPerAnnouncement())
			{
				resEdit.setProperty(varNameNumberCharsPerAnnouncement, Integer.toString(numberOfCharsPerAnnouncement));
			}
			else
			{
				// Since the absence of a property means that there are no limits, remove the
				// property from the resEdit object, in case it is already present.
				resEdit.remove(varNameNumberCharsPerAnnouncement);
			}
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameEnforceNumberOfAnnouncementsLimit()
		{
			return varNameEnforceNumberOfAnnouncementsLimit;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameEnforceNumberOfDaysInPastLimit()
		{
			return varNameEnforceNumberOfDaysInPastLimit;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameNumberCharsPerAnnouncement()
		{
			return varNameNumberCharsPerAnnouncement;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameNumberOfAnnouncements()
		{
			return varNameNumberOfAnnouncements;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameEnforceNumberOfCharsPerAnnouncementLimit()
		{
			return varNameEnforceNumberOfCharsPerAnnouncementLimit;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameNumberOfDaysInPast()
		{
			return varNameNumberOfDaysInPast;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameShowAllColumns()
		{
			return varNameShowAllColumns;
		}

		/**
		 * Gets a variable name for use in Velocity scripts to name input fields, etc.
		 */
		public static String getVarNameShowAnnouncementBody()
		{
			return varNameShowAnnouncementBody;
		}
		
		public boolean isShowOnlyOptionsButton()
		{
			return showOnlyOptionsButton;
		}

		public void setShowOnlyOptionsButton(boolean b)
		{
			showOnlyOptionsButton = b;
		}

	}

	/** Creates new AnnouncementActionState */
	public AnnouncementActionState()
	{
		init();
	} // constructor

	/**
	 * Release any resources and restore the object to initial conditions to be reused.
	 */
	public void recycle()
	{
		super.recycle();

	} // recycle

	/**
	 * Init to startup values
	 */
	protected void init()
	{
		super.init();

	} // init

	private Site m_editSite;

	// the announcement channel id
	private String m_channelId = null;

	// the announecement message id
	// private String m_messageId = null;
	// the announecement message reference
	private String m_messageReference = null;

	// parameter for announcement status: true - new created; false - already existing
	private boolean m_isNewAnn = true;

	// the template index: the main list screen - true; other screen - false
	private boolean m_isListVM = true;

	// the list of messages to be deleted
	private Vector m_delete_messages = new Vector();

	// collection ID
	// private String m_collectionId = null;
	// vm status
	private String m_status = null;

	// temporary attachment list - never set to null!
	private List<Reference> m_attachments = EntityManager.newReferenceList();

	// temporary selected attachment list
	private Vector m_selectedAttachments = new Vector();

	// temporary added attachments
	private Vector m_moreAttachments = new Vector();

	// temporary attachments only for attachment editing
	private Vector m_tempAttachments = new Vector();

	// temporary moreAttachments only for attachment editing
	private Vector m_tempMoreAttachments = new Vector();

	// temporary storage for new announcement subject
	private String m_tempSubject;

	// temporary storage for new announcement body
	private String m_tempBody;

	// temporary storage for announcement highlight
	private boolean m_tempHighlight = false;

	// temporary storage for new announcement release date
	private Time m_releaseDate = null;
	
	// temporary storage for new announcement retract date
	private Time m_retractDate = null;
	
	// temporary storage for announcement hidden
	private Boolean m_hidden = null;
	
	// temporary storage for announce to selection
	private String m_tempAnnounceTo;

	// temporary storage for announce to groups selection
	private Collection m_tempAnnounceToGroups;
	
	// temporary storage for announce to roles selection
	private String[] m_tempAnnounceToRoles;

	// temporary storage for local file inputStream, contentType and display name
	private HashMap m_fileProperties = new HashMap();

	// temporary storage for attachment properties: title, description, and copyright
	private HashMap m_attachProperties = new HashMap();

	// storage for home collection Id
	private String m_homeCollectionId;

	// storage for home Collection Display ame
	private String m_homeCollectionDisplayName;

	// ********* for sorting *********
	// the current sorted by property name
	private String m_currentSortedBy = "message_order";

	// the current sort sequence: ture - acscending/false - descending
	private boolean m_currentSortAsc = false;

	// ********* for sorting *********

	private DisplayOptions m_displayOptions;

	/**
	 * Get
	 */
	public String getTempSubject()
	{
		return m_tempSubject;

	} // getTempSubject()

	/**
	 * Get
	 */
	public String getTempBody()
	{
		return m_tempBody;

	} // getTempBody()

	/**
	 * Get
	 */
	public boolean getTempHighlight()
	{
		return m_tempHighlight;

	} // getTempHighlight()

	/**
	 * Get
	 */
	public String getTempAnnounceTo()
	{
		return m_tempAnnounceTo;

	} // getTempAnnounceTo()

	/**
	 * set
	 */
	public void setTempAnnounceTo(String tempAnnounceTo)
	{
		// if there's a change
		if (tempAnnounceTo == null || !tempAnnounceTo.equals(m_tempAnnounceTo))
		{
			// remember the new
			m_tempAnnounceTo = tempAnnounceTo;
		}

	} // setTempAnnounceTo()

	/**
	 * Get
	 */
	public Collection getTempAnnounceToGroups()
	{
		return m_tempAnnounceToGroups;

	} // getTempAnnounceToGroups()

	/**
	 * set
	 */
	public void setTempAnnounceToGroups(Collection tempAnnounceToGroups)
	{
		m_tempAnnounceToGroups = tempAnnounceToGroups;

	} // setTempAnnounceTo()
	
	/**
	 * Get
	 */
	public String[] getTempAnnounceToRoles()
	{
		return m_tempAnnounceToRoles;
	} // getTempAnnounceToRoles()

	/**
	 * set
	 */
	public void setTempAnnounceToRoles(String[] tempAnnounceToRoles)
	{
		m_tempAnnounceToRoles = tempAnnounceToRoles;
	} // setTempAnnounceToRoles()

	/**
	 * Get
	 */
	public void setTempSubject(String tempSubject)
	{
		// if there's a change
		if (!tempSubject.equals(m_tempSubject))
		{
			// remember the new
			m_tempSubject = tempSubject;
		}

	} // setTempSubject()

	/**
	 * Get
	 */
	public void setTempBody(String tempBody)
	{
		if (!tempBody.equals(m_tempBody))
		{
			// remember the new
			m_tempBody = tempBody;
		}

	} // setTempBody()

	/**
	 * Set
	 */
	public void setTempHighlight(boolean tempHighlight)
	{
		if (tempHighlight != m_tempHighlight)
		{
			m_tempHighlight = tempHighlight;
		}
	} // setTempHighlight()

	public void setTempReleaseDate(Time tempDate) 
	{
		if (tempDate != m_releaseDate)
		{
			//remember the new
			m_releaseDate = tempDate;
		}
	} // setTempReleaseDate()

	public Time getTempReleaseDate() 
	{
		return m_releaseDate;
	} // getTempReleaseDate()

	public void setTempRetractDate(Time tempDate) 
	{
		if (tempDate != m_retractDate)
		{
			// remember the new
			m_retractDate = tempDate;
		}
	} // setTempRetractDate()

	public Time getTempRetractDate() 
	{
		return m_retractDate;
	} // getTempRetractDate()

	public Boolean getTempHidden() 
	{
		return m_hidden;
	}

	public void setTempHidden(Boolean hidden) 
	{
		if (hidden != m_hidden)
		{
			m_hidden = hidden;
		}
	}

	/**
	 * Get the channel id of current state
	 * 
	 * @return The current channel id
	 */
	public String getChannelId()
	{
		return m_channelId;

	} // getChannelId

	/**
	 * Set the chat channel id to listen to.
	 * 
	 * @param channel
	 *        The chat channel id to listen to.
	 */
	public void setChannelId(String channelId)
	{
		// if there's a change
		if (!channelId.equals(m_channelId))
		{
			// remember the new
			m_channelId = channelId;
		}

	} // setChannelId

	// /**
	// * Get the collectionId of current state
	// * @return The current collectionId
	// */
	// public String getCollectionId()
	// {
	// return m_collectionId;
	//
	// } // getCollectionId
	//	
	//
	// /**
	// * Set the chat collectionId to listen to.
	// * @param collectionId The collectionId.
	// */
	// public void setCollectionId(String collectionId)
	// {
	// // if there's a change
	// if (collectionId != m_collectionId)
	// {
	// // remember the new
	// m_collectionId = collectionId;
	// }
	//
	// } // setCollectionId

	/**
	 * Get the the file properties for uploading
	 * 
	 * @return The current collectionId
	 */
	public Vector getFileProperties(String key)
	{

		Set m_filePropertiesSet = m_fileProperties.entrySet();
		Iterator i = m_filePropertiesSet.iterator();

		while (i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			if ((me.getKey()).equals(key)) return (Vector) me.getValue();
		}
		return null;

	} // getFileProperties

	/**
	 * Set the fileProperties
	 * 
	 * @param key
	 *        The key for map class, which is the absolute local path of file
	 * @param properties
	 *        The Vector which stores the inputStream, contentType, fileName of the file in order
	 */

	public void setFileProperties(String key, Vector properties)
	{
		m_fileProperties.put(key, properties);

	} // setFileProperties

	/**
	 * Get the attachment properties
	 * 
	 * @return The property based on the given key
	 */
	public Vector getAttachProperties(String key)
	{

		Set m_attachPropertiesSet = m_attachProperties.entrySet();
		Iterator i = m_attachPropertiesSet.iterator();

		while (i.hasNext())
		{
			Map.Entry me = (Map.Entry) i.next();
			if ((me.getKey()).equals(key)) return (Vector) me.getValue();
		}
		return null;

	} // getAttachProperties

	/**
	 * Set the attachProperties
	 * 
	 * @param key
	 *        The key for map class, which is the absolute local path of file
	 * @param properties
	 *        The Vector which stores the attachment properties: title, description, and copyright in order
	 */
	public void setAttachProperties(String key, Vector properties)
	{
		m_attachProperties.put(key, properties);

	} // setAttachProperties

	/**
	 * Get the status of preview: true - view new created; false - view revised existed
	 * 
	 * @return The current status
	 */
	public boolean getIsNewAnnouncement()
	{
		return m_isNewAnn;

	} // gsetIsNewAnnouncement

	/**
	 * Set the status of preview: true - view new created; false - view revised existed
	 * 
	 * @param preview_status
	 *        The status of preview: true - view new created; false - view revised existed
	 */
	public void setIsNewAnnouncement(boolean isNewAnn)
	{
		// if there's a change
		if (isNewAnn != m_isNewAnn)
		{
			// remember the new
			m_isNewAnn = isNewAnn;
		}

	} // setIsNewAnnouncement

	/**
	 * Get the current vm: true - in main list view; false - in other view
	 * 
	 * @return The boolean to show whether in main list view
	 */
	public boolean getIsListVM()
	{
		return m_isListVM;

	} // getIsListVM

	/**
	 * Set the current vm: true - in main list view; false - in other view
	 * 
	 * @param m_isListVM:
	 *        true - in main list view; false - in other view
	 */
	public void setIsListVM(boolean isListVM)
	{
		m_isListVM = isListVM;
	} // setIsListVM

	/**
	 * Get
	 */
	public Vector getDelete_messages()
	{
		return m_delete_messages;

	} // getDelete_messages

	/**
	 * Set
	 */
	public void setDeleteMessages(Vector delete_messages)
	{
		// if there's a change
		if (delete_messages != null)
		{
			m_delete_messages = (Vector) delete_messages.clone();
		}
		else
		{
			m_delete_messages = null;
		}
		// remember the new

	} // setDelete_messages

	private AnnouncementMessageEdit m_edit;

	/**
	 * Get edit The edit object
	 */
	public void setEdit(AnnouncementMessageEdit edit)
	{
		m_edit = edit;
	}

	/**
	 * Get edit object
	 * 
	 * @return m_edit The edit object
	 */
	public AnnouncementMessageEdit getEdit()
	{
		return m_edit;
	}

	/**
	 * Get
	 */
	public List<Reference> getAttachments()
	{
		return m_attachments;

	} // getAttachment

	/**
	 * Set
	 */
	public void setAttachments(List<Reference> attachments)
	{
		if (attachments != null)
		{
			m_attachments = EntityManager.newReferenceList(attachments);
		}
		else
		{
			m_attachments.clear();
		}
		// remember the new

	} // setAttachments

	/**
	 * Get
	 */
	public Vector getSelectedAttachments()
	{
		return m_selectedAttachments;

	} // getSelectedAttachment

	/**
	 * Set
	 */
	public void setSelectedAttachments(Vector selectedAttachments)
	{
		// if there's a change
		if (selectedAttachments != null)
		{
			m_selectedAttachments = (Vector) selectedAttachments.clone();
		}
		else
		{
			m_selectedAttachments = null;
		}

		// remember the new
	} // setSelectedAttachments

	/**
	 * Get
	 */
	public Vector getMoreAttachments()
	{
		return m_moreAttachments;

	} // getMoreAttachment

	/**
	 * Set
	 */
	public void setMoreAttachments(Vector moreAttachments)
	{
		// if there's a change
		if (moreAttachments != null)
		{
			m_moreAttachments = (Vector) moreAttachments.clone();
		}
		else
		{
			m_moreAttachments = null;
		}

		// remember the new

	} // setMoreAttachments

	/**
	 * Get
	 */
	public Vector getTempAttachments()
	{
		return m_tempAttachments;

	} // getTempAttachments

	/**
	 * Set
	 */
	public void setTempAttachments(Vector tempAttachments)
	{
		// if there's a change
		if (tempAttachments != null)
		{
			m_tempAttachments = (Vector) tempAttachments.clone();
		}
		else
		{
			m_tempAttachments = null;
		}

		// remember the new

	} // setTempAttachments

	/**
	 * Get
	 */
	public Vector getTempMoreAttachments()
	{
		return m_tempMoreAttachments;

	} // getTempMoreAttachments()

	/**
	 * Set
	 */
	public void setTempMoreAttachments(Vector tempMoreAttachments)
	{
		// if there's a change
		if (tempMoreAttachments != null)
		{
			m_tempMoreAttachments = (Vector) tempMoreAttachments.clone();
		}
		else
		{
			m_tempMoreAttachments = null;
		}

		// remember the new

	} // setTempMoreAttachments()

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Observer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * This method is called whenever the observed object is changed. An application calls an <tt>Observable</tt> object's <code>notifyObservers</code> method to have all the object's observers notified of the change.
	 * 
	 * @param o
	 *        the observable object.
	 * @param arg
	 *        an argument passed to the <code>notifyObservers</code> method.
	 */
	public void update(Observable o, Object arg)
	{
	}

	/**
	 * Get the status to be
	 * 
	 * @return The status to be
	 */
	public String getStatus()
	{
		return m_status;

	}

	/**
	 * Set the status to be
	 * 
	 * @param status
	 *        The status to be
	 */
	public void setStatus(String status)
	{
		if (status!=null)
		{
			// if there's a change
			if (!status.equals(m_status))
			{
				// remember the new
				m_status = status;
			}
		}

	}

	// ********* for sorting *********
	/**
	 * set the current sorted by property name
	 * 
	 * @param name
	 *        The sorted by property name
	 */
	protected void setCurrentSortedBy(String name)
	{
		m_currentSortedBy = name;

	} // setCurrentSortedBy

	/**
	 * get the current sorted by property name
	 * 
	 * @return "true" if the property is sorted ascendingly; "false" if the property is sorted descendingly
	 */
	protected String getCurrentSortedBy()
	{
		return m_currentSortedBy;

	} // getSortCurrentSortedBy

	/**
	 * set the current sort property
	 * 
	 * @param asc
	 *        "true" if the property is sorted ascendingly; "false" if the property is sorted descendingly
	 */
	protected void setCurrentSortAsc(boolean asc)
	{
		m_currentSortAsc = asc;

	} // setCurrentSortAsc

	/**
	 * get the current sort property
	 * 
	 * @return "true" if the property is sorted ascendingly; "false" if the property is sorted descendingly
	 */
	protected boolean getCurrentSortAsc()
	{
		return m_currentSortAsc;

	} // getCurrentSortAsc

	// ********* for sorting *********

	/**
	 * Returns the currently selected message reference.
	 */
	public String getMessageReference()
	{
		return m_messageReference;
	}

	/**
	 * Sets the currently selected message reference.
	 */
	public void setMessageReference(String string)
	{
		m_messageReference = string;
	}

	public Site getEditSite()
	{
		return m_editSite;
	}

	public void setEditSite(Site site)
	{
		m_editSite = site;

	}

	public DisplayOptions getDisplayOptions()
	{
		return m_displayOptions;
	}

	public void setDisplayOptions(DisplayOptions options)
	{
		m_displayOptions = options;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * SessionBindingListener implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public void valueBound(SessionBindingEvent event)
	{
	}

	public void valueUnbound(SessionBindingEvent event)
	{
		// pass it on to my edits
		if ((m_editSite != null) && (m_editSite instanceof SessionBindingListener))
		{
			((SessionBindingListener) m_editSite).valueUnbound(event);
		}

		if ((m_edit != null) && (m_edit instanceof SessionBindingListener))
		{
			((SessionBindingListener) m_edit).valueUnbound(event);
		}
	}
}
