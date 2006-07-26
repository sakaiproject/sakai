package org.sakaiproject.component.app.podcasts;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.w3c.dom.Document;

import com.sun.syndication.feed.module.itunes.EntryInformationImpl;
import com.sun.syndication.feed.module.itunes.types.Duration;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEnclosureImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;



public class BasicPodfeedService implements PodfeedService {

	private static final String DESCRIPTION_CONTENT_TYPE = "text/plain";
	private static final String defaultFeedType = "rss_2.0";
	private static final String LANGUAGE = "en-us";
	
	private static String fileName = "podtest.xml";
	private static String feedType = null;
	private Date pubDate = null;
	
	private PodcastService podcastService;
	private Log LOG = LogFactory.getLog(PodcastServiceImpl.class);
	
	/**
	 * @return Returns the podcastService.
	 */
	public PodcastService getPodcastService() {
		return podcastService;
	}

	/**
	 * @param podcastService The podcastService to set.
	 */
	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	/**
	 * This method generates the RSS feeds for podcasting based on category (i.e. Podcast)
	 * 
	 * @param Category The category to run as containing podcast content.
	 * @param Name The filename to write out the format to. THIS FOR DEBUG PURPOSES ONLY.
	 */
      public String generatePodcastRSS(String Category, String Name) {
		return generatePodcastRSS(Category, Name, podcastService.getSiteId(), null);

	}

  	/**
  	 * This method generates the RSS feeds for podcasting based on category (i.e. Podcast)
  	 * 
  	 * @param Category The category to run as containing podcast content.
  	 * @param Name The filename to write out the format to. THIS FOR DEBUG PURPOSES ONLY.
  	 * @param siteID The site ID passed in by the podfeed servlet
  	 */
        public String generatePodcastRSS(String Category, String Name, String siteID, String ftype) {
     		fileName = Name;
     		 
     		feedType = (ftype!=null) ? ftype : defaultFeedType;
  		
     		List entries = populatePodcastArray(Category, siteID);

     		// TODO: need to pass in global information for podcast here
     		String courseName = null;
     		String podfeedTitle;
     		String description;
  		
     		String URL = ServerConfigurationService.getServerUrl() + Entity.SEPARATOR + "podcasts/site/" + siteID;
 /* 		Properties siteProps = null;
  		
  		try {
			Site site = SiteService.getSite(siteID);
			
			Iterator pages = site.getPages().iterator();
			
			while(pages.hasNext()) {
				
				SitePage page = (SitePage) pages.next();
				Iterator tools = page.getTools().iterator();
			
				while(tools.hasNext()) {
				
					Tool tool = (Tool) tools.next();
					
					if (tool.getMutableConfig().getProperty("registration") ==  "sakai.podcast") {
						siteProps = tool.getMutableConfig();
						
					}
				
				}
				
			}
  		}
		catch (IdUnusedException e) {
			LOG.warn("IdUnusedException while generating podcast feed when attempting to get site " + siteID + ". " + e.getMessage());
				
		}
			
  		if ((podfeedTitle = siteProps.getProperty("podfeedTitle")) == null) {
  			ResourceProperties siteProperties = null;
  	        
  			try{
  			  siteProperties = SiteService.getSite(siteID).getProperties();
  			}
  			catch(IdUnusedException e){
  				LOG.info("IdUnusedException for site: " + siteID + ". " + e.getMessage());
  				
  			}
  			
  			if (siteProperties != null){ 
  				// courseName = siteProperties.getProperty("site-oncourse-course-id");
  				//TODO: parse coursename to get Department and Course Number
  				courseName = "Podfeed101";
/*  			}
  			else {
  				  courseName = "PodfeedTesting";
  				
  			}
*/
  			  podfeedTitle = "Podcast for " + courseName;
/*  			  siteProps.setProperty("podfeedTitle", podfeedTitle);
  			
  		}

  		if ((description = siteProps.getProperty("podfeedDescription")) == null) {
*/  	  		description = "This is the official podcast for the course " + courseName + ". Please check back throughout the semester for updates.";
//  	  		siteProps.setProperty("podfeedDescription", description);

//  		}
  		
  		String copyright = "IUPUI 2006";
  		
  		SyndFeed podcastFeed = doSyndication(podfeedTitle, URL, description, copyright, entries, fileName);
  		
		final SyndFeedOutput feedWriter = new SyndFeedOutput();
		
		String xmlDoc = "";
		Document xmlJdomDoc = null;
		
		try {
			xmlDoc = feedWriter.outputString(podcastFeed);
//			String test = feedWriter.toString();
//			System.out.println(test);

//			xmlJdomDoc = feedWriter.outputW3CDom(podcastFeed);
			
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			LOG.info("FeedException for site: " + siteID + " while generating podcast feed. " + e.getMessage());
			
		}
		catch (ClassCastException e) {
			String test = feedWriter.toString();
			LOG.info(test + " " + e.getMessage());
		}

/*		if (xmlJdomDoc == null) {
			return "";
		} else {
			String returnValue = xmlJdomDoc.toString();
			return returnValue;
		} */
		
		return xmlDoc;
  	}

    /**
	 * This method generates the RSS feeds for podcasting based on category (i.e. Podcast)
	 * 
	 * @param Category The category to run as containing podcast content.
	 * @param Name The filename to write out the format to. THIS FOR DEBUG PURPOSES ONLY.
	 */
      public String generatePodcastRSS(String Category) {
		return generatePodcastRSS(Category, ""+ podcastService.getSiteId()+ "feedsave.xml", podcastService.getSiteId(), null);

      }

    /**
     * This pulls the podcasts from Resourses and stuffs it in an array
     * to be added to the feed
     * 
     * @param category_string For what Category of feed wanted, ie, blog, podcast, etc.
     */
    	private List populatePodcastArray(String category_string) {
    		return populatePodcastArray(category_string, podcastService.getSiteId());
    }

    /**
     * This pulls the podcasts from Resourses and stuffs it in an array
     * to be added to the feed
     * 
     * @param category_string For what Category of feed wanted, ie, blog, podcast, etc.
     */
     private List populatePodcastArray(String category_string, String siteID) {
        	 List podEntries = null;
        	 List entries = new ArrayList();
        	 pubDate = null;
        	    
     	 try {
     		 podEntries = podcastService.getPodcasts(siteID);
    		
    		}
    		catch (PermissionException pe) {
    			// TODO: Set error message to say you don't have permission
   			 LOG.info("PermissionException generating podfeed for site: " + siteID + ". " + pe.getMessage());

    		} catch (InUseException e) {
    			// TODO Or try again? Set Error Message?
    			LOG.info("InUseException generating podfeed for site: " + siteID + ". " + e.getMessage());
    			
    		} catch (IdInvalidException e) {
    			// TODO Set a LOG message before rethrowing?
    			LOG.info("IdInvalidException generating podfeed for site: " + siteID + ". " + e.getMessage());
    			
    		} catch (InconsistentException e) {
    			// TODO Auto-generated catch block
    			LOG.info("InconsistentException generating podfeed for site: " + siteID + ". " + e.getMessage());
    			
    		} catch (IdUsedException e) {
    			// TODO Auto-generated catch block
    			LOG.info("IdUnusedException generating podfeed for site: " + siteID + ". " + e.getMessage());
    			
    		}

    		// get the iterator
    		if (podEntries != null) {
    			Iterator podcastIter = podEntries.iterator();
        		
    			while (podcastIter.hasNext()) {
        		    
			// get its properties from ContentHosting
			ContentResource podcastResource = (ContentResource) podcastIter.next();
			ResourceProperties podcastProperties = podcastResource.getProperties();

			// Format date, change SimpleDateFormat to format from example
			Date publishDate = null;
			try {
				publishDate = new Date(podcastProperties.getTimeProperty(PodcastService.DISPLAY_DATE).getTime());
				if (pubDate == null) {
					pubDate = publishDate;

				}
				else if (publishDate.after(pubDate)) {
					pubDate = publishDate;
					
				}
			} catch (EntityPropertyNotDefinedException e) {
				// TODO If not date, set to today? skip? throw PodcastFormatException?
				publishDate = new Date();
				LOG.info("EntityPropertyNotDefinedException generating podfeed getting DISPLAY_DATE for entry for site: " + siteID + "using current date. " + e.getMessage());

			} catch (EntityPropertyTypeException e) {
				// TODO Same thing, set to today? skip? throw PodcastFormatException?
				publishDate = new Date();
				LOG.info("EntityPropertyTypeException generating podfeed getting DISPLAY_DATE for entry for site: " + siteID + "using current date. " + e.getMessage());

			}
    				
			try {
 		            entries.add(addPodcast(podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME),
							   podcastService.getPodcastFileURL(podcastResource.getId()).replace("localhost", "149.166.143.203"), 
							   publishDate, 
							   podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION),
							   category_string, 
							   podcastProperties.getPropertyFormatted(ResourceProperties.PROP_CREATOR),
							   Long.parseLong(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH)),
							   podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_TYPE)));
 		            
    					} catch (PermissionException e) {
    						// TODO LOG.error - Feeder should have permission
    						LOG.info("PermissionException generating podfeed while adding entry  for site: " + siteID + ". " + e.getMessage());

    					} catch (IdUnusedException e) {
    						// TODO Problem with this podcast file - LOG and skip?
    						LOG.info("IdUnusedException generating podfeed while adding entry for site: " + siteID + ". " + e.getMessage());
    						
    					}
        			}
        		
        		}
           		
    		return entries;
        		    		
	}

    	/**
    	 * This add a particular podcast to the feed
    	 * 
    	 * @param title The title for this podcast
    	 * @param mp3link The URL where the podcast is stored
    	 * @param date The publish date for this podcast
    	 * @param blogContent The description of this podcast
    	 * @param cat The category of entry this is (Podcast)
    	 * @param author The author of this podcast
    	 * 
    	 * @return A SyndEntryImpl for this podcast
    	 */
    	private SyndEntryImpl addPodcast(String title, String mp3link, Date date, String blogContent, String cat, String author,
    									long length, String mimeType) {

    	    SyndEntryImpl entry = new SyndEntryImpl();
   	    EntryInformationImpl e = new EntryInformationImpl();
   	    ArrayList modules = new ArrayList();
        
       // TODO: Determine duration of file
   	    e.setAuthor(author);
  
   	    e.setDuration( new Duration( 263000 ) );

        modules.add( e );
        entry.setModules( modules );

         entry.setAuthor(author);
         entry.setTitle(title);
         
         // Since we have the complete URL, we only need to replace spaces, not slashes
         mp3link = mp3link.replaceAll(" ", "%20");
         entry.setLink(mp3link);
         
         entry.setPublishedDate(date);
   
         SyndContentImpl description = new SyndContentImpl();
         description.setType(DESCRIPTION_CONTENT_TYPE);
         description.setValue(blogContent);
         entry.setDescription(description);
            
         SyndCategoryImpl category = new SyndCategoryImpl();
         category.setName(cat);
            
         List categories = new ArrayList();
            
         categories.add(category);
         entry.setCategories(categories);
    	
         List enclosures = new ArrayList();
         
         SyndEnclosureImpl enc = new SyndEnclosureImpl();
         enc.setUrl(mp3link);
       
         // TODO: check if valid MIME type that can be displayed
         enc.setType(mimeType);
         enc.setLength(length);
         
         enclosures.add(enc);

         entry.setEnclosures(enclosures);
         
         return entry;
    	}

    	/**
    	 * This constructs the actual feed. Currently writes it out to a file.
    	 * 
    	 * @param title The global title for the podcast
    	 * @param link The URL for the feed
    	 * @param description_loc Global description of the feed 
    	 * @param copyright Copyright information
    	 * @param xml Where to write the XML
    	 */
    	private SyndFeed doSyndication(String title, String link, String description_loc, String copyright, List entries, String xml) {

    		SyndFeed feed = null;
    		try {
             feed = new SyndFeedImpl();
             
//           TODO: How to determine what podcatcher supports and feed that to them
             feed.setFeedType(feedType);		
     
             // Set global values for feed
             feed.setTitle(title);
             
             // Set langauge for the feed
             feed.setLanguage(LANGUAGE);

             // Set the publish date
             feed.setPublishedDate(pubDate);
             
             // remove following line, put in so can test with iTunes
             feed.setLink(link.replace("localhost", "149.166.143.203"));
             
             feed.setDescription(description_loc);
             feed.setCopyright(copyright);
    			
             feed.setEntries(entries);
    			   
             final Writer writer = new FileWriter(xml);
             final SyndFeedOutput output = new SyndFeedOutput();
             
             // keep this for now for debugging purposes.
             output.output(feed,writer);
             writer.close();

   		}
   	    catch (FeedException ex) {
             LOG.warn("FeedException generating actual xml feed for podfeed: " + title + ". " + ex.getMessage());
             
         }
   	    catch (IOException ioe) {
   	    		LOG.warn("IOException generating actual xml feed for podfeed: " + title + ". " + ioe.getMessage());
   	    		
   	    }
            
         return feed;
    	}

}
