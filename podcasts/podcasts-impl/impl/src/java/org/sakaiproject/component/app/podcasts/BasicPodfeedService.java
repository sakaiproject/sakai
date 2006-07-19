package org.sakaiproject.component.app.podcasts;


import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.api.app.podcasts.PodfeedService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;

import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class BasicPodfeedService implements PodfeedService {

	private static final String DESCRIPTION_CONTENT_TYPE = "text/plain";
	
	private static String fileName = "podtest.xml";
	private static String feedType = null;
	
	private PodcastService podcastService;
	
	/**
	 * This method generates the RSS feeds for podcasting based on category (i.e. Podcast)
	 * @param Category The category to run as containing podcast content.
	 * @param Name The filename to write out the format to.
	 */
      public void generatePodcastRSS(String Category, String Name) {
		feedType = "rss_2.0";
		fileName = Name;
		
		populatePodcastArray(Category);
	}

    /**
     * This pulls the podcasts from Resourses and stuffs it in an array
     * to be added to the feed
     * 
     * @param category_string For what Category of feed wanted, ie, blog, podcast, etc.
     */
    	private void populatePodcastArray(String category_string) {
    	    List podEntries = null;
    	    List entries = new ArrayList(); 
    	    
 		try {
    			podEntries = podcastService.getPodcasts();
		
		}
		catch (PermissionException pe) {
			// TODO: Set error message to say you don't have permission
//			 setErrorMessage(PERMISSION_ALERT);
		} catch (InUseException e) {
			// TODO Or try again? Set Error Message?
//			setErrorMessage(INTERNAL_ERROR_ALERT);
		} catch (IdInvalidException e) {
			// TODO Set a LOG message before rethrowing?
//			setErrorMessage(ID_INVALID_ALERT);
		} catch (InconsistentException e) {
			// TODO Auto-generated catch block
//			return null;
		} catch (IdUsedException e) {
			// TODO Auto-generated catch block
//			setErrorMessage(ID_UNUSED_ALERT);
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
				} catch (EntityPropertyNotDefinedException e) {
					// TODO If not date, set to today? skip? throw PodcastFormatException?
					publishDate = new Date();

				} catch (EntityPropertyTypeException e) {
					// TODO Same thing, set to today? skip? throw PodcastFormatException?
					publishDate = new Date();

				}
				
    				try {
    		            entries.add(addPodCast(podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME),
									   podcastService.getPodcastFileURL(podcastResource.getId()), 
									   publishDate, 
									   podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION),
									   category_string, 
									   podcastProperties.getPropertyFormatted(ResourceProperties.PROP_CREATOR)));
					} catch (PermissionException e) {
						// TODO LOG.error - Feeder should have permission

					} catch (IdUnusedException e) {
						// TODO Problem with this podcast file - LOG and skip?
						
					}
    			}
    		
    		// need to pass in global information for podcast here
    		doSyndication("name", "url", "description", "copyright", entries, fileName);
    		System.out.println("Did Syndication");
    		
    		}
    		    		
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
    	private SyndEntryImpl addPodCast(String title, String mp3link, Date date, String blogContent, String cat, String author) {
    		
    		SyndEntryImpl entry = new SyndEntryImpl();
         entry.setAuthor(author);
         entry.setTitle(title);
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
    	private void doSyndication(String title, String link, String description_loc, String copyright, List entries, String xml) {

    		try {
             final SyndFeed feed = new SyndFeedImpl();
             
//           TODO: How to determine what podcatcher supports and feed that to them
             feed.setFeedType(feedType);		
     
             feed.setTitle(title);
             feed.setLink(link);
             feed.setDescription(description_loc);
             feed.setCopyright(copyright);
    				
             feed.setEntries(entries);
    			   
             final Writer writer = new FileWriter(xml);
             final SyndFeedOutput output = new SyndFeedOutput();
             output.output(feed,writer);
             System.out.println(feed.toString());
             writer.close();
     
             System.out.println("************* The feed has been written to the file ["+xml+"]");
     
         }
    	     catch (Exception ex) {
               ex.printStackTrace();
               System.out.println("ERROR: "+ex.getMessage());
         }
            
    	}

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
}
