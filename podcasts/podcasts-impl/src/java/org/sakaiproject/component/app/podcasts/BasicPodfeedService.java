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

package org.sakaiproject.component.app.podcasts;

import static org.sakaiproject.component.app.podcasts.Utilities.checkSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.podcasts.PodcastPermissionsService;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.sakaiproject.api.app.podcasts.exception.PodcastException;
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
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.util.ResourceLoader;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.module.DCModuleImpl;
import com.sun.syndication.feed.module.itunes.EntryInformation;
import com.sun.syndication.feed.module.itunes.EntryInformationImpl;
import com.sun.syndication.feed.rss.Channel;
import com.sun.syndication.feed.rss.Description;
import com.sun.syndication.feed.rss.Enclosure;
import com.sun.syndication.feed.rss.Guid;
import com.sun.syndication.feed.rss.Item;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedOutput;

@Slf4j
public class BasicPodfeedService implements PodfeedService {

	/** MIME type for the global description of the feed **/
	private static final String DESCRIPTION_CONTENT_TYPE = "text/plain";

	/** The default feed type. Currently rss_2.0 **/
	private static final String defaultFeedType = "rss_2.0";

	/** The default language type. Currently 'en-us' **/
	private static final String LANGUAGE = "en-us";

	/** Used to get the global feed title which is a property of Podcasts folder **/
	private final String PODFEED_TITLE = "podfeedTitle";
	
	/** Used to grab the default feed title prefix */
	private final String FEED_TITLE_STRING = "feed_title";

	/** Used to get the global feed description which is a property of Podcasts folder **/
	private final String PODFEED_DESCRIPTION = "podfeedDescription";
	
	/** Used to pull copyright statement from sakai.properties file */
	private final String FEED_COPYRIGHT_STATEMENT = "podfeed_copyrighttext";
	
	/** Used to get the copyright statement if stored in Podcasts folder */
	private final String PODFEED_COPYRIGHT = "feed_copyright";
	
	/** Used to pull generator value from sakai.properties file */
	private final String FEED_GENERATOR_STRING = "podfeed_generator";
	
	/** Used to pull generator value from Podcasts folder */
	private final String PODFEED_GENERATOR = "feed_generator";

	/** Used to pull item author from sakai.properties file */
	private final String FEED_ITEM_AUTHOR_STRING = "podfeed_author";
		
	/** Used to get the default feed description pieces from the message bundle */
	private final String FEED_DESC1_STRING = "feed_desc1";
	private final String FEED_DESC2_STRING = "feed_desc2";
	
	/** Used to pull message bundle */
	private final String PODFEED_MESSAGE_BUNDLE = "org.sakaiproject.api.podcasts.bundle.Messages";
	private ResourceLoader resbud = new ResourceLoader(PODFEED_MESSAGE_BUNDLE);

	private PodcastService podcastService;
	private PodcastPermissionsService podcastPermissionsService;
	private SecurityService securityService;
	private SiteService siteService;

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
	 * @param siteService
	 *            The siteService to set.
	 */
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setPodcastPermissionsService(PodcastPermissionsService podcastPermissionsService) {
		this.podcastPermissionsService = podcastPermissionsService;
	}

	public void init() {
		checkSet(podcastService, "podcastService");
		checkSet(podcastPermissionsService, "podcastPermissionsService");
		checkSet(securityService, "securityService");
		checkSet(siteService, "siteService");
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
			log.error(e.getMessage() + " attempting to get feed title (getting podcast folder) "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new PodcastException(e);
			
		}
		finally {
			securityService.popAdvisor();
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
				feedTitle = siteService.getSite(siteId).getTitle() + getMessageBundleString(FEED_TITLE_STRING);
				log.info("No saved feed title found for site: " + siteId + ". Using " + feedTitle);

			}

		}
		catch (IdUnusedException e) {
			log.error("IdUnusedException attempting to get feed title (getting podcast folder) "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new PodcastException(e);

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
		storeProperty(PODFEED_TITLE, feedTitle, siteId);
	}

	/**
	 * Returns the String of the global feed description
	 */
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
				feedDescription =  siteService.getSite(siteId).getTitle()
									+ getMessageBundleString(FEED_DESC1_STRING)
									+ getMessageBundleString(FEED_DESC2_STRING);
				log.info("No feed description found for site: " + siteId + ". Using " + feedDescription);
			}
		}
		catch (IdUnusedException e) {
			log.error("IdUnusedException attempting to get feed title (getting podcast folder) "
					+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new PodcastException(e);
		}
		
		return feedDescription;
	}

	/**
	 * Returns the global feed generator String.
	 */
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
		storeProperty(PODFEED_DESCRIPTION, feedDescription, siteId);
	}

	public String getPodfeedGenerator() {
		return getPodfeedGenerator(podcastService.getSiteId());
	}

	/**
	 * Returns the global feed generator string.
	 * 
	 * @param siteId 
	 * 			The site id to get the feed description from
	 * 
	 * @return String 
	 * 			The global feed generator string.
	 */
	public String getPodfeedGenerator(String siteId) {		  
		// Generator consists of 3 parts, first 2 pulled from sakai.properties:
		//		ui.service - institution name
		//		version.service - version number for the instance
		//		last part is url of this instance
		final String localSakaiName = ServerConfigurationService.getString("ui.service","Sakai");		//localsakainame
		final String versionNumber = ServerConfigurationService.getString("version.service", "?");//dev
		final String portalUrl = ServerConfigurationService.getPortalUrl(); //last part exactly http://
		
		final String generatorString = localSakaiName + " " + versionNumber + " " + 
											portalUrl.substring(0, portalUrl.lastIndexOf("/")+1);	
		
		return generatorString;
	}

	/**
	 * 
	 */
	public void setPodfeedGenerator(String feedGenerator) {
		setPodfeedGenerator(feedGenerator, podcastService.getSiteId());
	}

	/**
	 * Sets the description for the feed.
	 * 
	 * @param feedDescription
	 *            The String containing the feed description.
	 * @param siteId
	 *            The siteId where to store the description
	 */
	public void setPodfeedGenerator(String feedGenerator, String siteId) {
		storeProperty(PODFEED_GENERATOR, feedGenerator, siteId);
	}

	public String getPodfeedCopyright() {
		return getPodfeedCopyright(podcastService.getSiteId());
	}

	/**
	 * Returns the global feed generator string.
	 * 
	 * @param siteId 
	 * 			The site id to get the feed description from
	 * 
	 * @return String 
	 * 			The global feed generator string.
	 */
	public String getPodfeedCopyright(String siteId) {	
		String currentCopyright=retrievePropValue(PODFEED_COPYRIGHT, siteId, FEED_COPYRIGHT_STATEMENT);
		Calendar rightNow = Calendar.getInstance();              
		int year = rightNow.get(Calendar.YEAR); 
		Object[] arguments = {
			     new Integer(year).toString()			    
			 };
		
		MessageFormat form = new MessageFormat(currentCopyright);
		String returnCopyright = form.format(arguments);
		
		return returnCopyright;
	}

	/**
	 * Sets feed copyright statement from within site.
	 */
	public void setPodfeedCopyright(String feedCopyright) {
		setPodfeedCopyright(feedCopyright, podcastService.getSiteId());
	}

	/**
	 * Sets the description for the feed.
	 * 
	 * @param feedDescription
	 *            The String containing the feed description.
	 * @param siteId
	 *            The siteId where to store the description
	 */
	public void setPodfeedCopyright(String feedCopyright, String siteId) {
		storeProperty(PODFEED_COPYRIGHT, feedCopyright, siteId);
	}

	/**
	 * Returns the property value for the property requested if stored within the
	 * Podcasts folder resource of the site id passed in. If not stored, retrieves
	 * the value from the Message bundle.
	 * 
	 * @param propName
	 * 				The name of the property wanted.
	 * 
	 * @param siteId
	 * 				The id of the site wanted.
	 * 
	 * @param bundleName
	 * 				The name within the Message bundle for the default string.
	 * 
	 * @return
	 * 				String containing either the stored property or the default Message bundle one.
	 */
	private String retrievePropValue(String propName, String siteId, String bundleName) {
		String propValue = null;

		ResourceProperties rp = getPodcastCollectionProperties(siteId);

		propValue = rp.getProperty(propName);

		/* For site where not added to folder upon creation
		 * and has not been revised/updated */
		if (propValue == null || "".equals(propValue)) {
			propValue = getMessageBundleString(bundleName);			
		}

		return propValue;
		
	}

	/**
	 * Stores the property propValue in the Podcasts folder resource under the name propName
	 * 
	 * @param propName
	 * 				The name within the resource to store the value
	 * 
	 * @param propValue
	 * 				The value to store
	 * 
	 * @param siteId
	 * 				Which site's Podcasts folder to store this property within.
	 */
	private void storeProperty(final String propName, String propValue, String siteId) {
		ContentCollectionEdit contentCollection = null;
		
		try {
			contentCollection = podcastService.getContentCollectionEditable(siteId);
			ResourcePropertiesEdit rp = contentCollection.getPropertiesEdit();
			
			if (rp.getProperty(propName) != null) {
				rp.removeProperty(propName);
			}

			rp.addProperty(propName, propValue);

			podcastService.commitContentCollection(contentCollection);
		}
		catch (Exception e) {
			// catches IdUnusedException, PermissionException
			log.error(e.getMessage() + " attempting to add property " + propName 
						+ " for site: " + siteId + ". " + e.getMessage(), e);
			podcastService.cancelContentCollection(contentCollection);
			
			throw new PodcastException(e);
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

		// pull global information for the feed into a Map so
		// can be passed all at once
		Map feedInfo = new HashMap();
		
		feedInfo.put("title", getPodfeedTitle(siteId));
		feedInfo.put("desc", getPodfeedDescription(siteId));
		feedInfo.put("gen", getPodfeedGenerator(siteId));
		
		// This is the URL for the actual feed.
		feedInfo.put("url", ServerConfigurationService.getServerUrl()
				+ Entity.SEPARATOR + "podcasts/site/" + siteId);
		
		feedInfo.put("copyright", getPodfeedCopyright(siteId));
		
		final WireFeed podcastFeed = doSyndication(feedInfo, entries, feedType, 
										pubDate, lastBuildDate);

		final WireFeedOutput wireWriter = new WireFeedOutput();

		try {
			return wireWriter.outputString(podcastFeed);

		} catch (FeedException e) {
			log.error(
					"Feed exception while attempting to write out the final xml file. "
							+ "for site: " + siteId + ". " + e.getMessage(), e);
			throw new PodcastException(e);

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
			
			if (podcastService.isPodcastFolderHidden(siteId)) {
				return entries;
			}
			else {
				// get the individual podcasts
				podEntries = podcastService.getPodcasts(siteId);
			
				// remove any that user cannot access
				// need to popAdvisor since now group aware need to
				// check if need to filter podcasts based on group membership
				securityService.popAdvisor();
				podEntries = podcastService.filterPodcasts(podEntries, siteId);
			}
		} 
		catch (PermissionException e) {
			log.error("PermissionException getting podcasts in order to generate podfeed for site: "
					+ siteId + ". " + e.getMessage(), e);
			throw new PodcastException(e);
		} 
		catch (Exception e) {
			log.info(e.getMessage() + "for site: " + siteId, e);
			throw new PodcastException(e);
		} 
		finally {
			securityService.popAdvisor();
		}

		if (podEntries != null) {
			// get the iterator
			Iterator podcastIter = podEntries.iterator();

			while (podcastIter.hasNext()) {

				// get its properties from ContentHosting
				ContentResource podcastResource = (ContentResource) podcastIter.next();
				ResourceProperties podcastProperties = podcastResource.getProperties();

				// publish date for this particular podcast
				// SAK-12052: need to compare for hidden using local time
				// then grab GMT time when storing for podcast feed
				Date publishDate = null;
				Date compareDate = null;
				try {
					if (podcastResource.getReleaseDate() != null) {
						compareDate = new Date(podcastResource.getReleaseDate().getTime());
						publishDate = podcastService.getGMTdate(podcastResource.getReleaseDate().getTime());
					}
					else {
						// need to put in GMT for the feed
						compareDate = new Date(podcastProperties.getTimeProperty(PodcastService.DISPLAY_DATE).getTime());
						publishDate = podcastService.getGMTdate(podcastProperties.getTimeProperty(PodcastService.DISPLAY_DATE).getTime());
					}
				} 
				catch (Exception e) {
					// catches EntityPropertyNotDefinedException, EntityPropertyTypeException
					log.warn(e.getMessage() + " generating podfeed getting DISPLAY_DATE for entry for site: "
									+ siteId + "while building feed. SKIPPING... " + e.getMessage(), e);
				}
				
				// if getting the date generates an error, skip this podcast.
				if (publishDate != null && ! hiddenInUI(podcastResource, compareDate)) {
					try {
						Map podcastMap = new HashMap();
						podcastMap.put("date", publishDate);
						podcastMap.put("title", podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
						
						enablePodfeedSecurityAdvisor();
						String fileUrl = podcastService.getPodcastFileURL(podcastResource.getId());
						podcastMap.put("guid", fileUrl);
						final String podcastFolderId = podcastService.retrievePodcastFolderId(siteId);
						securityService.popAdvisor();
						
						// if site Display to Site, need to access actual podcasts thru Dav servlet
						// so change item URLs to do so
						if (!podcastService.isPublic(podcastFolderId)) {
							fileUrl = convertToDavUrl(fileUrl);
						}
					
						podcastMap.put("url", fileUrl);
						podcastMap.put("description",podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION));
						podcastMap.put("author", podcastProperties.getPropertyFormatted(ResourceProperties.PROP_CREATOR));
						podcastMap.put("len", Long.parseLong(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)));
						podcastMap.put("type", podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
						
						entries.add(addPodcast(podcastMap));

					}
					catch (PermissionException e) {
						// Problem with this podcast file - LOG and skip
						log.error("PermissionException generating podfeed while adding entry for site: "
							+ siteId + ". SKIPPING... " + e.getMessage(), e);

					} 
					catch (IdUnusedException e) {
						// Problem with this podcast file - LOG and skip
						log.warn("IdUnusedException generating podfeed while adding entry for site: "
							+ siteId + ".  SKIPPING... " + e.getMessage(), e);

					}
				}
				
			}

		}

		securityService.popAdvisor();

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
	private Item addPodcast(Map values) 
	{
		final Item item = new Item();

		// set title for this podcast
		item.setTitle((String) values.get("title"));

        // Replace all occurrences of pattern (ie, spaces) in input
        // with hex equivalent (%20)
        Pattern pattern = Pattern.compile(" ");
        String url = (String) values.get("url");
        Matcher matcher = pattern.matcher(url);
        url = matcher.replaceAll("%20");
        item.setLink(url);

        // Set Publish date for this podcast/episode
        // NOTE: date has local time, but when feed rendered,
        // converts it to GMT
		item.setPubDate((Date) values.get("date"));

		// Set description for this podcast/episode
		final Description itemDescription = new Description();
		itemDescription.setType(DESCRIPTION_CONTENT_TYPE);
		itemDescription.setValue((String) values.get("description"));
		item.setDescription(itemDescription);

		// Set guid for this podcast/episode
		item.setGuid(new Guid());
		item.getGuid().setValue((String) values.get("guid"));
		item.getGuid().setPermaLink(false);

		// This creates the enclosure so podcatchers (iTunes) can
		// find the podcasts		
		List enclosures = new ArrayList();

		final Enclosure enc = new Enclosure();
		enc.setUrl(url);
		enc.setType((String) values.get("type"));
		enc.setLength((Long) values.get("len"));

		enclosures.add(enc);

		item.setEnclosures(enclosures);

		// Currently uses 2 modules:
		//  iTunes for podcasting
		//  DCmodule since validators want email with author tag, 
		//    so use dc:creator instead
		List modules = new ArrayList();
		
		// Generate the iTunes tags
		final EntryInformation iTunesModule = new EntryInformationImpl();

		iTunesModule.setAuthor(getMessageBundleString(FEED_ITEM_AUTHOR_STRING));
		iTunesModule.setSummary((String) values.get("description"));

		// Set dc:creator tag
		final DCModuleImpl dcModule = new DCModuleImpl();
		
		dcModule.setCreator((String) values.get("author"));
		
		modules.add(iTunesModule);
		modules.add(dcModule);
		
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
	private Channel doSyndication(Map feedInfo, List entries,
			String feedType, Date pubDate, Date lastBuildDate) {

		final Channel channel = new Channel();

		// FUTURE: How to determine what podcatcher supports and feed that to them
		channel.setFeedType(feedType);
		channel.setTitle((String) feedInfo.get("title"));
		channel.setLanguage(LANGUAGE);

		channel.setPubDate(pubDate);
		channel.setLastBuildDate(lastBuildDate);

		channel.setLink((String) feedInfo.get("url"));
		channel.setDescription((String) feedInfo.get("desc"));		
		channel.setCopyright((String) feedInfo.get("copyright"));
		channel.setGenerator((String) feedInfo.get("gen"));

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
			log.error("PermissionException while trying to retrieve Podcast folder Id string "
							+ "while generating feed for site " + siteId + e.getMessage(), e);
		}
		finally {
			securityService.popAdvisor();
									
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
        // Compile regular expression
        Pattern pattern = Pattern.compile("access/content/group");

        // Replace all occurrences of pattern in input
        Matcher matcher = pattern.matcher(fileUrl);
        fileUrl = matcher.replaceAll("dav");

		return fileUrl;

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
		return podcastPermissionsService.allowAccess(id);
	}
	
	/**
	 * Returns whether podcast should be 'hidden' in UI. Conditions:
	 * 	Hidden property set
	 *  Release date is in future
	 *  Retract date is in past
	 *  
	 * @param podcastResource
	 * 			The actual podcast to check
	 * @param tempDate
	 * 			Release date (if exists) or display date (older version)
	 */
	private boolean hiddenInUI(ContentResource podcastResource, Date tempDate) {
		return podcastPermissionsService.isResourceHidden(podcastResource, tempDate);
	}

	/**
	 * Sets the Faces error message by pulling the message from the
	 * MessageBundle using the name passed in
	 * 
	 * @param key
	 *           The name in the MessageBundle for the message wanted
	 *            
	 * @return String
	 * 			The string that is the value of the message
	 */
	private String getMessageBundleString(String key) {
		return resbud.getString(key);
	}
}
