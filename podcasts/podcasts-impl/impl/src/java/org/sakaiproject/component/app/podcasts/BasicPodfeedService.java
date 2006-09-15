/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/syllabus/tags/sakai_2-2-001/syllabus-api/src/java/org/sakaiproject/api/app/syllabus/SyllabusService.java $
 * $Id: BasicPodfeedService.java 8802 2006-05-03 15:06:26Z josrodri@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.app.podcasts;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.cover.SiteService;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.itunes.EntryInformation;
import com.sun.syndication.feed.module.itunes.EntryInformationImpl;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Enclosure;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

public class BasicPodfeedService implements PodfeedService {

	/** MIME type for the global description of the feed **/
	private static final String DESCRIPTION_CONTENT_TYPE = "text/plain";

	/** The default feed type. Currently rss_2.0 **/
	private static final String defaultFeedType = "rss_2.0";

	/** The default language type. Currently 'en-us' **/
	private static final String LANGUAGE = "en-us";

	/** Used to get the global feed title which is a property of Podcasts folder **/
	private final String PODFEED_TITLE = "podfeedTitle";

	/** Used to get the global feed description which is a property of Podcasts folder **/
	private final String PODFEED_DESCRIPTION = "podfeedDescription";

	/** FUTURE: Use to pull copyright statement from message bundle **/
	private final String COPYRIGHT_STATEMENT = "feed_copyright";

	private static final Log LOG = LogFactory.getLog(PodcastServiceImpl.class);

	private PodcastService podcastService;

	private SecurityService securityService;

	/**
	 * @param securityService
	 *            The securityService to set.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * @param podcastService
	 *            The podcastService to set.
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	/**
	 * Gets the podcast folder collection ResourceProperties
	 * 
	 * @param siteId
	 * 			The site id whose podcast folder ResourceProperties wanted
	 * 
	 * @return ResourceProperties  
	 * 			The ResourceProperties collection for the podcasts folder
	 */
	private ResourceProperties getPodcastCollectionProperties(String siteId) {
		ContentCollection contentCollection = null;
		ResourceProperties rp = null;

		try {	
			enablePodfeedSecurityAdvisor();
			
			contentCollection = podcastService.getContentCollection(siteId);
			rp = contentCollection.getProperties();

		} 
		catch (Exception e) {
			// catches IdUnusedException, PermissionException
			LOG.error(e.getMessage() + " attempting to get feed title (getting podcast folder) "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);
			
		}
		finally {
			securityService.clearAdvisors();
		}

		return rp;
	}

	/**
	 * Returns the podfeed global title from content hosting via the podcastService
	 * 
	 * @return String 
	 * 			The global podfeed title
	 */
	public String getPodfeedTitle() {
		return getPodfeedTitle(podcastService.getSiteId());
	}

	/**
	 * Gets the title for the feed from podcast folder's properties.
	 * 
	 * @param siteId
	 * 			The site id
	 * 
	 * @return String 
	 * 			The global podfeed title 
	 */
	public String getPodfeedTitle(String siteId) {
		String feedTitle = null;

		try {
			ResourceProperties rp = getPodcastCollectionProperties(siteId);

			feedTitle = rp.getProperty(PODFEED_TITLE);

			/* For site where not added to folder upon creation
			 * and has not been revised/updated */
			if (feedTitle == null) {
				feedTitle = "Podcasts for " + SiteService.getSite(siteId).getTitle();
				LOG.info("No saved feed title found for site: " + siteId + ". Using " + feedTitle);

			}

		}
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException attempting to get feed title (getting podcast folder) "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);

		}

		return feedTitle;
	}

	/**
	 * Stores the title for the feed in the podcast folder's resources
	 * 
	 * @param String
	 *            The title for the feed
	 */
	public void setPodfeedTitle(String feedTitle) {
		setPodfeedTitle(feedTitle, podcastService.getSiteId());
	}

	/**
	 * Stores the title for the feed in the podcast folder's resources. Used by
	 * the actual feed so need to pass in the siteId also.
	 * 
	 * @param feedTitle
	 *            The title for the feed
	 * @param siteId
	 *            The siteId whose feed is being titled
	 */
	public void setPodfeedTitle(String feedTitle, String siteId) {
		ContentCollectionEdit contentCollection = null;
		
		try {
			contentCollection = podcastService.getContentCollectionEditable(siteId);
			ResourcePropertiesEdit rp = contentCollection.getPropertiesEdit();

			if (rp.getProperty(PODFEED_TITLE) != null) {
				rp.removeProperty(PODFEED_TITLE);
			}

			rp.addProperty(PODFEED_TITLE, feedTitle);

			podcastService.commitContentCollection(contentCollection);

		}
		catch (Exception e) {
			// catches IdUnusedException, PermissionException
			LOG.error(e.getMessage() + " attempting to add feed title property (getting podcast folder) "
						+ "for site: " + siteId + ". " + e.getMessage(), e);
			podcastService.cancelContentCollection(contentCollection);
			
			throw new Error(e);
		}
	}

	public String getPodfeedDescription() {
		return getPodfeedDescription(podcastService.getSiteId());
	}

	/**
	 * Returns the global feed description.
	 * 
	 * @param siteId 
	 * 			The site id to get the feed description from
	 * 
	 * @return String 
	 * 			The global feed description
	 */
	public String getPodfeedDescription(String siteId) {
		String feedDescription = null;

		try {
			ResourceProperties rp = getPodcastCollectionProperties(siteId);

			feedDescription = rp.getProperty(PODFEED_DESCRIPTION);

			/* For site where not added to folder upon creation
			 * and has not been revised/updated */
			if (feedDescription == null) {
				feedDescription = "This is the official podcast for course "
						+ SiteService.getSite(siteId).getTitle();
				feedDescription += ". Please check back throughout the semester for updates.";
				LOG.info("No feed description found for site: " + siteId + ". Using " + feedDescription);

			}

		}
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException attempting to get feed description (getting podcast folder) "
						+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);

		}

		return feedDescription;

	}

	public void setPodfeedDescription(String feedDescription) {
		setPodfeedDescription(feedDescription, podcastService.getSiteId());
	}

	/**
	 * Sets the description for the feed.
	 * 
	 * @param feedDescription
	 *            The String containing the feed description.
	 * @param siteId
	 *            The siteId where to store the description
	 */
	public void setPodfeedDescription(String feedDescription, String siteId) {
		ContentCollectionEdit contentCollection = null;
		
		try {
			contentCollection = podcastService.getContentCollectionEditable(siteId);
			ResourcePropertiesEdit rp = contentCollection.getPropertiesEdit();
			
			if (rp.getProperty(PODFEED_DESCRIPTION) != null) {
				rp.removeProperty(PODFEED_DESCRIPTION);
			}

			rp.addProperty(PODFEED_DESCRIPTION, feedDescription);

			podcastService.commitContentCollection(contentCollection);

		}
		catch (Exception e) {
			// catches IdUnusedException, PermissionException
			LOG.error(e.getMessage() + " attempting to add feed title property (while getting podcast folder) "
						+ "for site: " + siteId + ". " + e.getMessage(), e);
			podcastService.cancelContentCollection(contentCollection);
			
			throw new Error(e);
		}
	}

	/**
	 * This method generates the RSS feed
	 * 
	 * @return String 
	 * 			The feed XML file as a string
	 */
	public String generatePodcastRSS() {
		return generatePodcastRSS(podcastService.getSiteId(), null);

	}

	/**
	 * This method generates the RSS feed
	 * 
	 * @param siteID
	 *            The site id whose feed needs to be generated
	 * @param ftyle
	 * 			 The feed type (for potential future development) - currently rss 2.0
	 * 
	 * @return String 
	 * 			 The feed document as a String
	 */
	public String generatePodcastRSS(String siteId, String ftype) {
		final String feedType = (ftype != null) ? ftype : defaultFeedType;
		Date pubDate = null;
		Date lastBuildDate = null;

		// put each podcast entry/episode into a list
		List entries = populatePodcastArray(siteId);

		// Pull first entry if not null in order to establish publish date
		// for entire feed. Pull the second to get lastBuildDate
		if (entries != null) {
			Iterator iter = entries.iterator();

			if (iter.hasNext()) {
				Item firstPodcast = (Item) iter.next();

				pubDate = firstPodcast.getPubDate();

				if (iter.hasNext()) {
					Item nextPodcast = (Item) iter.next();

					lastBuildDate = nextPodcast.getPubDate();

				}
				else {
					// only one, so use the first podcast date
					lastBuildDate = pubDate;
				}

			} 
			else {
				// There are no podcasts to present, so use today
				pubDate = new Date();
				lastBuildDate = pubDate;
			}
		}

		// pull global information for the feed
		final String podfeedTitle = getPodfeedTitle(siteId);
		final String description = getPodfeedDescription(siteId);

		// This is the URL for the actual feed.
		final String URL = ServerConfigurationService.getServerUrl()
				+ Entity.SEPARATOR + "podcasts/site/" + siteId;

		// TODO: pull from message bundle
		final String copyright = "Copyright 2006 The Trustees of Indiana University. All rights reserved.";

		final WireFeed podcastFeed = doSyndication(podfeedTitle, URL,
				description, copyright, entries, feedType, pubDate,
				lastBuildDate);

		final WireFeedOutput wireWriter = new WireFeedOutput();

		try {
			return wireWriter.outputString(podcastFeed);

		} catch (FeedException e) {
			LOG.error(
					"Feed exception while attempting to write out the final xml file. "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new Error(e);

		}
	}

	/**
	 * This pulls the podcasts from Resourses and stuffs it in a list to be
	 * added to the feed
	 * 
	 * @param siteId
	 *            The site to pull the individual podcasts from
	 * 
	 * @return List The list of podcast entries from ContentHosting
	 */
	private List populatePodcastArray(String siteId) {
		List podEntries = null;
		List entries = new ArrayList();

		try {
			enablePodfeedSecurityAdvisor();
			
			// get the individual podcasts
			podEntries = podcastService.getPodcasts(siteId);
			
			// remove any that are in the future
			podEntries = podcastService.filterPodcasts(podEntries);

		} 
		catch (PermissionException e) {
			LOG.error("PermissionException getting podcasts in order to generate podfeed for site: "
						+ siteId + ". " + e.getMessage(), e);
			throw new Error(e);
			
		} 
		catch (Exception e) {
			LOG.info(e.getMessage() + "for site: " + siteId, e);
			throw new Error(e);
		} 
		finally {
			securityService.clearAdvisors();
		}

		if (podEntries != null) {
			// get the iterator
			Iterator podcastIter = podEntries.iterator();

			while (podcastIter.hasNext()) {

				// get its properties from ContentHosting
				ContentResource podcastResource = (ContentResource) podcastIter.next();
				ResourceProperties podcastProperties = podcastResource.getProperties();

				// publish date for this particular podcast
				Date publishDate = null;

				try {
					// need to put in GMT for the feed
					publishDate = podcastService.getGMTdate(podcastProperties.getTimeProperty(PodcastService.DISPLAY_DATE).getTime());

				} 
				catch (Exception e) {
					// catches EntityPropertyNotDefinedException, EntityPropertyTypeException
					LOG.warn(e.getMessage() + " generating podfeed getting DISPLAY_DATE for entry for site: "
									+ siteId + "while building feed. SKIPPING... " + e.getMessage());

				}
				
				// if getting the date generates an error, skip this podcast.
				if (publishDate != null) {
					try {
						final String title = podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);

						enablePodfeedSecurityAdvisor();
						String fileUrl = podcastService.getPodcastFileURL(podcastResource.getId());
						final String podcastFolderId = podcastService.retrievePodcastFolderId(siteId);
						securityService.clearAdvisors();
						
						// if site Display to Site, need to access actual podcasts thru Dav servlet
						// so change item URLs to do so
						if (!podcastService.isPublic(podcastFolderId)) {
							fileUrl = convertToDavUrl(fileUrl);
						
						}
					
						final String description = podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION);
						final String author = podcastProperties.getPropertyFormatted(ResourceProperties.PROP_CREATOR);
						final long contentLength = Long.parseLong(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH));
						final String contentType = podcastProperties	.getProperty(ResourceProperties.PROP_CONTENT_TYPE);

						entries.add(addPodcast(title, fileUrl, publishDate, description, author,
								contentLength, contentType));

					}
					catch (PermissionException e) {
						// Problem with this podcast file - LOG and skip
						LOG.error("PermissionException generating podfeed while adding entry for site: "
							+ siteId + ". SKIPPING... " + e.getMessage());

					} 
					catch (IdUnusedException e) {
						// Problem with this podcast file - LOG and skip
						LOG.warn("IdUnusedException generating podfeed while adding entry for site: "
							+ siteId + ".  SKIPPING... " + e.getMessage());

					}
				}
				
			}

		}

		securityService.clearAdvisors();

		return entries;

	}

	/**
	 * This add a particular podcast to the feed.
	 * 
	 * @param title
	 *            The title for this podcast
	 * @param mp3link
	 *            The URL where the podcast is stored
	 * @param date
	 *            The publish date for this podcast
	 * @param blogContent
	 *            The description of this podcast
	 * @param cat
	 *            The category of entry this is (Podcast)
	 * @param author
	 *            The author of this podcast
	 * 
	 * @return 
	 * 			 A SyndEntryImpl for this podcast
	 */
	private Item addPodcast(String title, String mp3link, Date date,
			String blogContent, String author, long length, String mimeType) 
	{
		final Item item = new Item();
		
		item.setAuthor(author);
		item.setTitle(title);

		// Need to replace all spaces with their HEX equiv
		// so podcatchers (iTunes) recognize it
		mp3link = mp3link.replaceAll(" ", "%20");

		date = new Date(date.toGMTString());
		item.setPubDate(date);
		item.setLink(mp3link);

		final Description itemDescription = new Description();
		itemDescription.setType(DESCRIPTION_CONTENT_TYPE);
		itemDescription.setValue(blogContent);
		item.setDescription(itemDescription);

		item.setGuid(new Guid());
		item.getGuid().setValue(mp3link);
		item.getGuid().setPermaLink(false);

		// This creates the enclosure so podcatchers (iTunes) can
		// find the podcasts		
		List enclosures = new ArrayList();

		final Enclosure enc = new Enclosure();
		enc.setUrl(mp3link);
		enc.setType(mimeType);
		enc.setLength(length);

		enclosures.add(enc);

		item.setEnclosures(enclosures);

		// Generate the iTunes tags
		List modules = new ArrayList();
		
		final EntryInformation iTunesModule = new EntryInformationImpl();

		iTunesModule.setAuthor("Created from ContentHosting");
		iTunesModule.setSummary(blogContent);

		modules.add(iTunesModule);

		item.setModules(modules);
		
		return item;
	}

	/**
	 * This puts the pieces together to generate the actual feed.
	 * 
	 * @param title
	 *            The global title for the podcast
	 * @param link
	 *            The URL for the feed
	 * @param description_loc
	 *            Global description of the feed
	 * @param copyright
	 *            Copyright information
	 * @param entries
	 *            The list of individual podcasts
	 * @param feedTyle
	 * 			 The output feed type (for potential future development). Set to rss_2.0
	 * @param pubDate
	 * 			 The date to set the publish date for this feed
	 * 
	 * @eturn SyndFeed
	 * 			The entire podcast stuffed into a SyndFeed object
	 */
	private Channel doSyndication(String title, String link,
			String description_loc, String copyright, List entries,
			String feedType, Date pubDate, Date lastBuildDate) {

		final Channel channel = new Channel();

		// FUTURE: How to determine what podcatcher supports and feed that to them
		channel.setFeedType(feedType);
		channel.setTitle(title);
		channel.setLanguage(LANGUAGE);

		channel.setPubDate(pubDate);
		channel.setLastBuildDate(lastBuildDate);

		channel.setLink(link);
		channel.setDescription(description_loc);		
		channel.setCopyright(copyright);
		channel.setGenerator("Oncourse 2.2.1 https://oncourse.iu.edu");

		channel.setItems(entries);

		// Used to remove the dc: tags from the channel level info
		List modules = new ArrayList();
		
		channel.setModules(modules);
	
		return channel;
	}

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 */
	protected void enablePodfeedSecurityAdvisor() {
		// put in a security advisor so we can do our podcast work without need
		// of further permissions
		securityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Returns podcast folder id using either 'podcasts' or 'Podcasts'. If it
	 * cannot find or is denied access, it will return null.
	 * 
	 * @param siteId
	 *            The site to search
	 *            
	 * @return String 
	 * 			 Contains the complete id for the podcast folder
	 */
	public String retrievePodcastFolderId(String siteId) {

		String podcastFolderId = null;

		try {
			enablePodfeedSecurityAdvisor();
			podcastFolderId = podcastService.retrievePodcastFolderId(siteId);

		} 
		catch (PermissionException e) {
			// log and return null to indicate there was a problem generating
			LOG.error("PermissionException while trying to retrieve Podcast folder Id string "
							+ "for site " + siteId + e.getMessage());
			
		}
		finally {
			securityService.clearAdvisors();
									
		}
		
		return podcastFolderId;

	}

	/**
	 * If site is Display to Site, need to retrieve files thru dav servlet.
	 * This converts a podcast URL to accomplish this.
	 * 
	 * @param fileUrl
	 * 			The current file URL. Access is through content.
	 * 
	 * @return String
	 * 			The changed URL that points to the dav servlet.
	 */
	private String convertToDavUrl(String fileUrl) {
		return fileUrl.replace("access/content/group", "dav");

	}

	/**
	 * Determines if authenticated user has 'read' access to podcast collection folder
	 * 
	 * @param id
	 * 			The id for the podcast collection folder
	 * 
	 * @return
	 * 		TRUE - has read access, FALSE - does not
	 */
	public boolean allowAccess(String id) {
		return podcastService.allowAccess(id);
	}
}