/*
 * #%L
 * SCORM Tool
 * %%
 * Copyright (C) 2007 - 2016 Sakai Project
 * %%
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *             http://opensource.org/licenses/ecl2
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.sakaiproject.scorm.ui.player;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.protocol.http.SecondLevelCacheSessionStore;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.pagestore.DiskPageStore;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.util.file.Folder;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.ui.ContentPackageResourceMountStrategy;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.wicket.protocol.http.SakaiWebApplication;

public class ScormTool extends SakaiWebApplication {

	private static final Log LOG = LogFactory.getLog(ScormTool.class);

	private ScormResourceService resourceService;

	@Override
	public void init() {
		super.init();
		getExceptionSettings().setUnexpectedExceptionDisplay( IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE );
		mount(new ContentPackageResourceMountStrategy("contentpackages"));
	}

	@Override
	public RequestCycle newRequestCycle( Request request, Response response )
	{
		return new WebRequestCycle( this, (WebRequest) request, (WebResponse) response )
		{
			@Override
			public Page onRuntimeException( Page page, RuntimeException e )
			{
				// Let Sakai ErrorReportHandler (BugReport) handle errors
				throw e;
			}
		};
	}

	@Override
	public Class getHomePage() {
		return PackageListPage.class;
	}

	public Folder getUploadFolder() {
		Folder folder = new Folder(System.getProperty("java.io.tmpdir"), "scorm-uploads");

		// Make sure that this directory exists.
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				LOG.error("Cannot create temp dir: " + folder);
			}
		}

		return folder;
	}

	@Override
	protected ISessionStore newSessionStore() {
		return new SecondLevelCacheSessionStore(this, new DiskPageStore() {

			@Override
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
								LOG.error("Exception deserializing page ", e);

								//page = new PlayerPage();
							}
							
							return page;
						}
					}

					return null;
				}

			@Override
			public void storePage(String sessionId, Page page)
			{
				super.storePage(sessionId, page);
			}

			@Override
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
