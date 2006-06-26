/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.tool.podcasts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;


public class podHomeBean {
	
	/**
	 * Stores the properties of a specific podcast to be displayed
	 * on the main page.
	 * 
	 * @author josephrodriguez
	 *
	 */
	public class DecoratedPodcastBean {
		private String filename;
		private String displayDate;
		private String title;
		private String description;
		private String size;
		private String type;
		private String postedTime;
		private String postedDate;
		private String author;
		
		public DecoratedPodcastBean() {
			
		}
		
		public DecoratedPodcastBean(String filename, String displayDate, String title, String description, String size, String type) {
			this.filename = filename;
			this.displayDate = displayDate;
			this.title = title;
			this.description = description;
			this.size = size;
			this.type = type;
		}
		
		public String getDescription() {
			return description;
		}
		public void setDescription(String decsription) {
			this.description = decsription;
		}
		public String getDisplayDate() {
			return displayDate;
		}

		public void setDisplayDate(String displayDate) {
			this.displayDate = displayDate;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
		public String getSize() {
			return size;
		}
		public void setSize(String size) {
			this.size = size;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}

		public String getPostedTime() {
			return postedTime;
		}

		public void setPostedTime(String postedTime) {
			this.postedTime = postedTime;
		}

		public String getPostedDate() {
			return postedDate;
		}

		public void setPostedDate(String postedDate) {
			this.postedDate = postedDate;
		}

		public String getAuthor() {
			return author;
		}

		public void setAuthor(String author) {
			this.author = author;
		}
		
		
	}
	
	private boolean resourceToolExists;
	private boolean podcastFolderExists;
	private boolean actPodcastsExist;
	private boolean podcastResourceCheckFirstTry;
	private PodcastService podcastService;
	private List contents;
	

	private String URL;
	
	public podHomeBean() {
		resourceToolExists=false;
		podcastFolderExists = false;
		actPodcastsExist = false;
		podcastResourceCheckFirstTry=true;
	}

	/**
     *   Determines if Resource tool part of the site. Needed to store podcasts.
     *   Since multiple ui items need to be removed, set boolean variable so 
     *   only need to check actual resource once 
     *  
     * @return true if Resource tool exists so entire page can display
     *         false if does not exist so just error message displays
     */
	public boolean getResourceToolExists() {
		if (podcastResourceCheckFirstTry) {
			podcastResourceCheckFirstTry=false;

			try
			{
				Site thisSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
				List pageList = thisSite.getPages();
				Iterator iterator = pageList.iterator();

				while(iterator.hasNext())
				{
					SitePage pgelement = (SitePage) iterator.next();

					if (pgelement.getTitle().equals("Resources"))
					{
						resourceToolExists = true;
						break;
					}
				}
			}
			catch(Exception e)
			{
				return resourceToolExists;
			}
	    
			if(!resourceToolExists)
			{
				setErrorMessage("To use the Podcasts tool, you must first add the Resources tool.");
			}
		}
		
	    return resourceToolExists;
	}

	public void setResourseToolExists(boolean resourceToolExists) {
		this.resourceToolExists = resourceToolExists;
	}
	
	  private void setErrorMessage(String errorMsg)
	  {
		  FacesContext.getCurrentInstance().addMessage(null,
		      new FacesMessage("Alert: " + errorMsg));
	  }
	  
	  public boolean getPodcastFolderExists() {
		  podcastFolderExists=false;
		  
		  if (resourceToolExists) {
			  // we know resources tool exists, but need to know if podcast folder does
			  podcastFolderExists = podcastService.checkPodcastFolder();
		  }
		  
		  return podcastFolderExists;
	  }
	  
	  public void setPodcastFolderExists(boolean podcastFolderExists) {
		  this.podcastFolderExists = podcastFolderExists;
	  }
	  
	  public String getURL() {
		  URL = ServerConfigurationService.getServerUrl() + Entity.SEPARATOR + "podcasts/site/" 
		         + podcastService.getSiteId();
		  return URL;
	  }
	  
	  public void setURL(String URL) {
		  this.URL = URL;
	  }

	public PodcastService getPodcastService() {
		return podcastService;
	}

	public void setPodcastService(PodcastService podcastService) {
		this.podcastService = podcastService;
	}

	public List getContents() {
		contents = podcastService.getPodcasts();

		// create local List of DecoratedBeans
		ArrayList decoratedPodcasts = new ArrayList();

		if (contents != null) {
			Iterator podcastIter = contents.iterator();
		
			// for each bean
			while (podcastIter.hasNext() ) {
				try {
					// get its properties from ContentHosting
					ResourceProperties podcastProperties = ((ContentResource) podcastIter.next()).getProperties();
			
					// Create a new decorated bean to store the info
					DecoratedPodcastBean podcastInfo = new DecoratedPodcastBean();

					// fill up the decorated bean
					podcastInfo.setTitle(podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME));
					podcastInfo.setDescription(podcastProperties.getPropertyFormatted(ResourceProperties.PROP_DESCRIPTION));

					// to format the date as: DAY_OF_WEEK  DAY MONTH_NAME YEAR
					SimpleDateFormat formatter = new SimpleDateFormat ("EEEEEE',' dd MMMMM yyyy" );
					Date tempDate = new Date(podcastProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE).getTime());
					podcastInfo.setDisplayDate(formatter.format(tempDate));
										
					podcastInfo.setFilename(podcastProperties.getProperty(ResourceProperties.PROP_ORIGINAL_FILENAME));
					podcastInfo.setSize(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_LENGTH));
					
					// TODO: figure out how to determine/store content type
					podcastInfo.setType(podcastProperties.getProperty(ResourceProperties.PROP_CONTENT_TYPE));

					// get and format last modified time
					formatter.applyPattern("hh:mm a z" );
					tempDate = new Date(podcastProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE).getTime());
					podcastInfo.setPostedTime(formatter.format(tempDate));

					// get and format last modified date
					formatter.applyPattern("MM/dd/yyyy" );
					tempDate = new Date(podcastProperties.getTimeProperty(ResourceProperties.PROP_MODIFIED_DATE).getTime());
					podcastInfo.setPostedDate(formatter.format(tempDate));

					// get author
					podcastInfo.setAuthor(podcastProperties.getPropertyFormatted(ResourceProperties.PROP_CREATOR));

					// add it to the ArrayList to send to the page
					decoratedPodcasts.add(podcastInfo);
			
					// get the next podcast if it exists
				}
				catch (EntityPropertyNotDefinedException ende) {
					
				}
				catch (EntityPropertyTypeException epte) {
					
				}
				catch (/*IdUnused*/Exception e) {
					//TODO: determine exact execptions to catch
					System.out.println("Wrong Id used to collect podcasts ");
					e.printStackTrace();
					return null;
				}
/*			catch (PermissionException pe) {
				System.out.println("PermissionException");
			}
*/
			}
		
		}

		// when done:
		// TODO: sort the list
		return decoratedPodcasts; //new decorated list 
	}

	public void setContents(List contents) {
		this.contents = contents;
	}

	/**
	 * Resources/podcasts exists, but are there any actual podcasts
	 * 
	 * @return true if there are podcasts, false otherwise
	 */
	public boolean getActPodcastsExist() {
		if (!getPodcastFolderExists()) {
			// if for some reason there is not a podcast folder
			// for example, was renamed in Resources
			actPodcastsExist = false;
		}
		else  {
			// ask the service if there is anything in the podcast folder
			actPodcastsExist = podcastService.checkForActualPodcasts();
		}

		return actPodcastsExist;
	}

	public void setActPodcastsExist(boolean actPodcastsExist) {
		this.actPodcastsExist = actPodcastsExist;
	}
}
