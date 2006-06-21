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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.sakaiproject.api.app.podcasts.PodcastService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;


public class podHomeBean {
	
	public class DecoratedPodcastBean {
		private String filename;
		private Date displayDate;
		private String title;
		private String decsription;
		private int size;
		private String type;
		public String getDecsription() {
			return decsription;
		}
		public void setDecsription(String decsription) {
			this.decsription = decsription;
		}
		public Date getDisplayDate() {
			return displayDate;
		}
		public void setDisplayDate(Date displayDate) {
			this.displayDate = displayDate;
		}
		public String getFilename() {
			return filename;
		}
		public void setFilename(String filename) {
			this.filename = filename;
		}
		public int getSize() {
			return size;
		}
		public void setSize(int size) {
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
		
		
	}
	private boolean resourceToolExists;
	private boolean podcastFolderExists;
	private boolean podcastResourceCheckFirstTry;
	private PodcastService podcastService;
	private List contents;
	

	private String URL;
	
	public podHomeBean() {
		resourceToolExists=false;
		podcastFolderExists = true;
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
			  // TODO: check to Resource tool to see if Podcasts folder exists
			  //       if it does, check if any podcasts exist.
			  //                       if they do, construct the list, sort by date,
			  //                       and return them 
			  //       else return true
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

	public ArrayList getContents() {
		List tempPodcasts = podcastService.getPodcasts();

		// create local List of DecoratedBeans
		ArrayList decoratedPodcasts = new ArrayList();

		Iterator podcastIter = tempPodcasts.iterator();
		
		// for each bean
		while (podcastIter.hasNext() ) {
			
		}
		//   ContentHostingService.getReqProps(name?)
		//   create new DecoratedBean
		//   fill up
		//   add to List
		// when done:
		return decoratedPodcasts; //new decorated list 
	}

	public void setContents(List contents) {
		this.contents = contents;
	}
}
