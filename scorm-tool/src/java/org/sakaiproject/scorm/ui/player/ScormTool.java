/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.scorm.ui.player;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.protocol.http.pagestore.DiskPageStore;
import org.apache.wicket.request.target.coding.BookmarkablePageRequestTargetUrlCodingStrategy;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.session.pagemap.IPageMapEntry;
import org.apache.wicket.util.file.Folder;
import org.apache.wicket.util.lang.Objects;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.ContentPackageResourceMountStrategy;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class ScormTool extends SakaiWebApplication {
	
	private static Log log = LogFactory.getLog(ScormTool.class);
	
	private ScormResourceService resourceService;
	
	@Override
	public void init() {
		super.init();

		Objects.setObjectStreamFactory(new org.apache.wicket.util.io.WicketObjectStreamFactory());
		
		/*mount(new BookmarkablePageRequestTargetUrlCodingStrategy("scormplayer", PlayerPage.class, null) {
			public boolean matches(String path)
			{
				String mountPath = getMountPath();
				if (path.startsWith(mountPath) && !path.contains("/contentPackages"))
				{
					String remainder = path.substring(mountPath.length());
					if (remainder.length() == 0 || remainder.startsWith("/"))
					{
						return true;
					}
				}
				return false;
			}
		});*/
		
		mount(new ContentPackageResourceMountStrategy("contentpackages"));
		
	}
	
	@Override
	public Class getHomePage() {
		return PackageListPage.class;
	}

	public Folder getUploadFolder() {
		Folder folder = new Folder(System.getProperty("java.io.tmpdir"), "scorm-uploads");
	
		// Make sure that this directory exists.
		folder.mkdirs();
		
		return folder;
	}
	
	protected ISessionStore newSessionStore() {
		return new SecondLevelCacheSessionStore(this, new DiskPageStore() {
			
			public Page getPage(String sessionId, String pagemap, int id, int versionNumber,
					int ajaxVersionNumber)
				{
					SessionEntry entry = getSessionEntry(sessionId, false);
					if (entry != null)
					{
						byte[] data;

						if (isSynchronous())
						{
							data = entry.loadPage(pagemap, id, versionNumber, ajaxVersionNumber);
						}
						else
						{
							// we need to make sure that the there are no pending pages to
							// be saved before loading a page
							List pages = getPagesToSaveList(sessionId);
							synchronized (pages)
							{
								flushPagesToSaveList(sessionId, pages);
								data = entry.loadPage(pagemap, id, versionNumber, ajaxVersionNumber);
							}
						}

						if (data != null)
						{
							Page page = null;
							try {
								 page = deserializePage(data, versionNumber);
							} catch (Exception e) {
								log.error("Exception deserializing page ", e);
								
								//page = new PlayerPage();
							}
							
							return page;
						}
					}

					return null;
				}
			
			public void storePage(String sessionId, Page page)
			{
				super.storePage(sessionId, page);
			}
			
			protected boolean isSynchronous()
			{
				return false;
			}
		});
	}
	
	public ScormResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ScormResourceService resourceService) {
		this.resourceService = resourceService;
	}

		
}
