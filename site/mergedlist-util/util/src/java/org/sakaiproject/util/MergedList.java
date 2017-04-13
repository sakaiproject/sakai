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

package org.sakaiproject.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

/**
 * Contains the list of merged/non-merged channels
 */
public class MergedList extends ArrayList
{
	 /**
	  * Used to create a reference.	This is unique to each caller, so we
	  * need an interface.
	  */
	 public interface ChannelReferenceMaker
	 {
		  
		  String makeReference(String siteId);
	 }
	 
	/**
	 * channel entry used to communicate with the Velocity templates when dealing with merged channels.
	 */
	public interface MergedEntry extends Comparable
	{
		/**
		 * Returns the display string for the channel.
		 */
		public String getDisplayName();

		/**
		 * Returns the ID of the group.	(The ID is used as a key.)
		 */
		public String getReference();

		/**
		 * Returns true if this channel is currently being merged.
		 */
		public boolean isMerged();

		/**
		 * Marks this channel as being merged or not.
		 */
		public void setMerged(boolean b);

		/**
		 * This returns true if this list item should be visible to the user.
		 */
		public boolean isVisible();

		/**
		 * Implemented so that we can order by the group full name.
		 */
		public int compareTo(Object arg0);
	}

	/**
	 * This interface is used to describe a generic list entry provider so that
	 * a variety of list entries can be used.	 This currently serves merged sites
	 * for the schedule and merged channels for announcements.
	 */
	public interface EntryProvider
	{
		/**
		 * Gets an iterator for the channels, calendars, etc.
		 */
		public Iterator getIterator();
		
		/**
		 * See if we can do a "get" on the calendar, channel, etc.
		 */
		public boolean allowGet(String ref);
		
		/**
		 * Generically access the context of the resource provided
		 * by the getIterator() call.
		 * @return The context.
		 */
		public String getContext(Object obj);
		
		/**
		 * Generically access the reference of the resource provided
		 * by the getIterator() call.
		 */
		public String getReference(Object obj);
		
		/**
		 * Generically access the resource's properties.
		 * @return The resource's properties.
		 */
		public ResourceProperties getProperties(Object obj);

		  
		  public boolean isUserChannel(Object channel);

		  
		  public boolean isSpecialSite(Object channel);

		  
		  public String getSiteUserId(Object channel);

		  
		  public Site getSite(Object channel);

	}

	/** This is used to separate group names in the config parameter. */
	static private final String ID_DELIMITER = "_,_";

	/**
	 * Implementation of channel entry used for rendering the list of merged channels
	 */
	private class MergedChannelEntryImpl implements MergedEntry
	{
		final private String channelReference;
		final private String channelFullName;
		private boolean merged;
		private boolean visible;

		
		public MergedChannelEntryImpl(
			String channelReference,
			String channelFullName,
			boolean merged,
			boolean visible)
		{
			this.channelReference = channelReference;
			this.channelFullName = channelFullName;
			this.merged = merged;
			this.visible = visible;
		}

		/* (non-Javadoc)
		 * @see org.chefproject.actions.channelAction.MergedCalenderEntry#getchannelDisplayName()
		 */
		public String getDisplayName()
		{
			return channelFullName;
		}

		/* (non-Javadoc)
		 * @see org.chefproject.actions.channelAction.MergedCalenderEntry#getchannelReference()
		 */
		public String getReference()
		{
			return channelReference;
		}

		/* (non-Javadoc)
		 * @see org.chefproject.actions.channelAction.MergedCalenderEntry#isMerged()
		 */
		public boolean isMerged()
		{
			return merged;
		}

		/* (non-Javadoc)
		 * @see org.chefproject.actions.channelAction.MergedCalenderEntry#setMerged(boolean)
		 */
		public void setMerged(boolean b)
		{
			merged = b;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object arg0)
		{
			MergedChannelEntryImpl compObj = (MergedChannelEntryImpl) arg0;

			return this.getDisplayName().compareTo(compObj.getDisplayName());
		}

		/* (non-Javadoc)
		 * @see org.chefproject.actions.channelAction.MergedCalenderEntry#isVisible()
		 */
		public boolean isVisible()
		{
			return visible;
		}
	}
	
	/**
	 * loadChannelsFromDelimitedString
	 *
	 * Selects and loads channels from a list provided by the entryProvider
	 * parameter.	The algorithm for loading channels is a bit complex, and
	 * depends on whether or not the user is currently in their "My Workspace", etc.
	 * 
	 * This function formerly filtered through a list of all sites.  It still
	 * goes through the motions of filtering, and deciding how to flag the channels
	 * as to whether or not they are merged, hidden, etc.	 However, it has been
	 * modified to take all of its information from an EntryProvider parameter,
	 * This list is now customized and is no longer "all sites in existence". 
	 * When sites are being selected for merging, this list can be quite long.	 
	 * This function is more often called just to display merged events, so 
	 * passing a more restricted list makes for better performance.
	 * 
	 * At some point we could condense redundant logic, but this modification
	 * was performed under the time constraints of a release.  So, an effort was
	 * made not to tinker with the logic, so much as to reduce the set of data
	 * that the function had to process.
	 * 
	 * @param isOnWorkspaceTab - true if this is the user's my workspace 
	 * @param entryProvider - provides available channels for load/merge
	 * @param userId - current userId
	 * @param channelArray - array of selected channels for load/merge
	 * @param isSuperUser - if true, then don't merge all available channels
	 * @param currentSiteId - current worksite
	 */
	public void loadChannelsFromDelimitedString(
			boolean isOnWorkspaceTab,
			EntryProvider entryProvider, String userId, String[] channelArray,
			boolean isSuperUser, String currentSiteId)
				
	{
		loadChannelsFromDelimitedString( isOnWorkspaceTab, true, entryProvider,
													userId, channelArray, isSuperUser, currentSiteId );
	}
	
	/**
	 * loadChannelsFromDelimitedString
	 *
	 * (see description on above method)
	 *
	 * @param isOnWorkspaceTab - true if this is the user's my workspace 
	 * @param mergeAllOnWorkspaceTab - if true, merge all channels in channelArray
	 * @param entryProvider - provides available channels for load/merge
	 * @param userId - current userId
	 * @param channelArray - array of selected channels for load/merge
	 * @param isSuperUser - if true, then don't merge all available channels
	 * @param currentSiteId - current worksite
	 */
	public void loadChannelsFromDelimitedString(
			boolean isOnWorkspaceTab,
			boolean mergeAllOnWorkspaceTab,
			EntryProvider entryProvider, String userId, String[] channelArray,
			boolean isSuperUser, String currentSiteId)
	{
		// Remove any initial list contents.
		this.clear();

		// We'll need a map since we want to test for the
		// presence of channels without searching through a list.
		Map currentlyMergedchannels = makeChannelMap(channelArray);

		// Loop through the channels that the EntryProvider gives us.
		Iterator it = entryProvider.getIterator();

		while (it.hasNext())
		{
			Object channel = it.next();
			
			// Watch out for null channels.	Ignore them if they are there.
			if ( channel == null )
			{
				 continue;
			}

			// If true, this channel will be added to the list of
			// channels that may be merged.
			boolean addThisChannel = false;

			// If true, this channel will be marked as "merged".
			boolean merged = false;

			// If true, then this channel will be in the list, but will not
			// be shown to the user.
			boolean hidden = false;

			// If true, this is a user channel.
			boolean thisIsUserChannel = entryProvider.isUserChannel(channel);

			// If true, this is a "special" site.
			boolean isSpecialSite = entryProvider.isSpecialSite(channel);

			// If true, this is the channel associated with the current
			// user.
			boolean thisIsTheUsersMyWorkspaceChannel = false;
			if ( thisIsUserChannel
						  && userId.equals(
									 entryProvider.getSiteUserId(channel)) )
			{
				 thisIsTheUsersMyWorkspaceChannel = true;
			}

			//
			// Don't put the channels of other users in the merge list.
			// Go to the next item in the loop.
			//
			if (thisIsUserChannel && !thisIsTheUsersMyWorkspaceChannel)
			{
				continue;
			}

			// Only add to the list if the user can access this channel.
			if (entryProvider.allowGet(entryProvider.getReference(channel)))
			{
				// Merge *almost* everything the user can access.
				if (thisIsTheUsersMyWorkspaceChannel)
				{
					// Don't merge the user's channel in with a
					// group channel.	 If we're on the "My Workspace"
					// tab, then it's okay to merge.
					if (isOnWorkspaceTab)
					{
						merged = true;
					}
					else
					{
						merged = false;
					}
				}
				else
				{
					//
					// If we're the admin, and we're on our "My Workspace" tab, then only
					// use our channel (handled above).	 We'd be overloaded if we could
					// see everyone's events.
					//
					if (isSuperUser && isOnWorkspaceTab)
					{
						merged = false;
					}
					else
					{
						// merge all sites if onWorkspaceTab and mergeAll is enabled
						if (isOnWorkspaceTab && mergeAllOnWorkspaceTab)
						{
							merged = true;
						}
						// Set it to merged if the channel was specified in the merged
						// channel list that we got from the portlet configuration.
						else
						{
							merged =
								currentlyMergedchannels.containsKey(
									entryProvider.getReference(channel));
						}
					}
				}

				addThisChannel = true;

				// Hide user or "special" sites from the user interface merge list.
				if (thisIsUserChannel || isSpecialSite)
				{
					// Hide the user's own channel from them.
					hidden = true;
				}
			}

			if (addThisChannel)
			{
				String siteDisplayName = "";
				
				// There is no point in getting the display name for hidden items
				if (!hidden)
				{
					 String displayNameProperty = entryProvider.getProperties(
								channel).getProperty(
								entryProvider.getProperties(channel)
										  .getNamePropDisplayName());

					 // If the channel has a displayName property and use that
					 // instead.
					 if (displayNameProperty != null
								&& displayNameProperty.length() != 0)
					 {
						  siteDisplayName = displayNameProperty;
					 } 
					 else
					 {
						  String channelName = "";

						  Site site = entryProvider.getSite(channel);

						  if (site != null)
						  {
								boolean isCurrentSite = currentSiteId.equals(site.getId());

								//
								// Hide and force the current site to be merged.
								//
								if (isCurrentSite)
								{
									 hidden = true;
									 merged = true;
								} 
								else
								{
									 // Else just get the name.
									 channelName = site.getTitle();
									 siteDisplayName = channelName + " ("
												+ site.getId() + ") ";
								}
						  }
					 }
				}

				this.add(
					new MergedChannelEntryImpl(
						entryProvider.getReference(channel),
						siteDisplayName,
						merged,
						!hidden));
			}
		}

		// MergedchannelEntry implements Comparable, so the sort will work correctly.
		Collections.sort(this);
	} // loadFromPortletConfig
	
	/**
	 * Forms an array of all channel references to which the user has read access.
	 */
	public String[] getAllPermittedChannels(ChannelReferenceMaker refMaker)
	{
		 List finalList = new ArrayList();
		 String [] returnArray = null;

		 // Get all accessible sites for the current user, not requiring descriptions
		 List siteList = SiteService.getUserSites(false);
		 
		 Iterator it = siteList.iterator();
		 
		 // Add all the references to the list.
		 while ( it.hasNext() )
		 {
			Site site = (Site) it.next();
			finalList.add(refMaker.makeReference(site.getId()));
		 }
		 
		 // Make the array that we'll return
		 returnArray = new String[finalList.size()];
		 
		 for ( int i=0; i < finalList.size(); i++ )
		 {
			  returnArray[i] = (String) finalList.get(i);
		 }
		 
		 return returnArray;		 
	}
	
	/**
	 * This gets a list of channels from the portlet configuration information.
	 * Channels here can really be a channel or a schedule from a site.
	 */
	public String[] getChannelReferenceArrayFromDelimitedString(
		String primarychannelReference,
		String mergedInitParameterValue)
	{
		String mergedChannels = null;

		// Get a list of the currently merged channels.	 This is a delimited list.
		mergedChannels =
			StringUtils.trimToNull(
				mergedInitParameterValue);

		String[] mergedChannelArray = null;

		// Split the configuration string into an array of channel references.
		if (mergedChannels != null)
		{
			mergedChannelArray = mergedChannels.split(ID_DELIMITER);
		}
		else
		{
			// If there are no merged channels, default to the primary channel.
			mergedChannelArray = new String[1];
			mergedChannelArray[0] = primarychannelReference;
		}

		return mergedChannelArray;
	} // getChannelReferenceArrayFromDelimitedString
	
	/**
	  * Create a channel reference map from an array of channel references.
	  */
	 private Map makeChannelMap(String[] mergedChannelArray)
	 {
		  // Make a map of those channels that are currently merged.
		Map currentlyMergedchannels = new HashMap();

		if (mergedChannelArray != null)
		{
			for (int i = 0; i < mergedChannelArray.length; i++)
			{
				currentlyMergedchannels.put(
					mergedChannelArray[i],
					Boolean.valueOf(true));
			}
		}
		  return currentlyMergedchannels;
	 }

	 /**
	 * Loads data input by the user into this list and then saves the list to
	 * the portlet config information.	The initContextForMergeOptions() function
	 * must have previously been called.
	 */
	public void loadFromRunData(ParameterParser params)
	{
		Iterator it = this.iterator();

		while (it.hasNext())
		{
			MergedEntry entry = (MergedEntry) it.next();

			// If the group is even mentioned in the parameters, then
			// it means that the checkbox was selected.	Deselected checkboxes
			// will not be present in the parameter list.
			if (params.getString(entry.getReference())
				!= null)
			{
				entry.setMerged(true);
			}
			else
			{
				//
				// If the entry isn't visible, then we can't "unmerge" it due to
				// the lack of a checkbox in the user interface.
				//
				if (entry.isVisible())
				{
					entry.setMerged(false);
				}
			}
		}
	}

	/**
	 * Loads data input by the user into this list and then saves the list to
	 * the portlet config information.	The initContextForMergeOptions() function
	 * must have previously been called.
	 */
	public String getDelimitedChannelReferenceString()
	{
		StringBuilder mergedReferences = new StringBuilder("");
			
		Iterator it = this.iterator();
		boolean firstEntry = true;
			
		while (it.hasNext())
		{
			MergedEntry entry = (MergedEntry) it.next();
				
			if (entry.isMerged())
			{
				// Add a delimiter, if appropriate.
				if ( !firstEntry )
				{
					mergedReferences.append(ID_DELIMITER);
				}
				else
				{
					firstEntry = false;
				}
					
				// Add to our list
				mergedReferences.append(entry.getReference());
			}
		}
			
		// Return the delimited list of merged references
		return mergedReferences.toString();
	}
		
	/**
	 * Returns an array of merged references.
	 */
	public List getReferenceList()
	{
		List references = new ArrayList();
			
		Iterator it = this.iterator();
			
		while (it.hasNext())
		{
			MergedEntry mergedEntry = (MergedEntry) it.next();
				
			// Only add it to the list if it has been merged.
			if (mergedEntry.isMerged())
			{
				references.add(mergedEntry.getReference());
			}
		}
			
		return references;
	}
}
