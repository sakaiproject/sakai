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
package org.sakaiproject.scorm.ui.player.pages;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.adl.sequencer.SeqNavRequests;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebResource;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.IRequestCodingStrategy;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.ContentPackageResource;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.ResourceNavigator;
import org.sakaiproject.scorm.ui.console.pages.PackageListPage;
import org.sakaiproject.scorm.ui.player.behaviors.ActivityAjaxEventBehavior;
import org.sakaiproject.scorm.ui.player.behaviors.CloseWindowBehavior;
import org.sakaiproject.scorm.ui.player.components.ButtonForm;
import org.sakaiproject.scorm.ui.player.components.ChoicePanel;
import org.sakaiproject.scorm.ui.player.components.DeniedPanel;
import org.sakaiproject.scorm.ui.player.components.LaunchPanel;
import org.sakaiproject.scorm.ui.player.components.LazyLoadPanel;
import org.sakaiproject.scorm.ui.player.util.ContentPackageWebResource;


public class PlayerPage extends BaseToolPage {
	
	//protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
	//protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( window.name )";
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(PlayerPage.class);
	
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormApplicationService api;
	@SpringBean
	transient ScormContentService contentService;
	@SpringBean
	transient ScormResourceService resourceService;
	@SpringBean
	transient ScormResultService resultService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	
	private SessionBean sessionBean;

	// Components
	private LaunchPanel launchPanel;
	private ActivityAjaxEventBehavior closeWindowBehavior;
	private ButtonForm buttonForm;
	
	public PlayerPage() {
		this(new PageParameters());
	}
	
	
	public PlayerPage(final PageParameters pageParams) {
		super();
		
		long contentPackageId = pageParams.getLong("id");
		String courseId = pageParams.getString("resourceId");
		
		ContentPackage contentPackage = contentService.getContentPackage(contentPackageId);
		
		sessionBean = sequencingService.newSessionBean(contentPackage);
		sessionBean.setCompletionUrl(getCompletionUrl());
		
		buttonForm = new ButtonForm("buttonForm", sessionBean, this);
		add(buttonForm);
				
		add(new LazyLoadPanel("actionPanel") {
			private static final long serialVersionUID = 1L;

			@Override
		    public Component getLazyLoadComponent(String lazyId, AjaxRequestTarget target) {
				return launch(sessionBean, lazyId, pageParams, target);
			}
		});
		
		//add(new AdminPanel("adminPanel", contentPackageId, runState));
		
		closeWindowBehavior = new CloseWindowBehavior(sessionBean);
		add(closeWindowBehavior);
		
	}
	
	
	private String getCompletionUrl() {
		RequestCycle cycle = getRequestCycle();
		IRequestCodingStrategy encoder = cycle.getProcessor().getRequestCodingStrategy();
		WebRequest webRequest = (WebRequest)getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();
		String toolUrl = servletRequest.getContextPath();
		
		Class<?> pageClass = PackageListPage.class;
		
		if (lms.canLaunchNewWindow())
			pageClass = CompletionPage.class;
			
		CharSequence completionUrl = encoder.encode(cycle, new BookmarkablePageRequestTarget(pageClass, new PageParameters()));
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/").append(completionUrl);
		
		return url.toString();
	}
		
	
	private int chooseStartOrResume(SessionBean sessionBean, INavigable navigator, AjaxRequestTarget target) {
		int navRequest = SeqNavRequests.NAV_START;
		
		List<Attempt> attempts = resultService.getAttempts(sessionBean.getContentPackage().getId(), sessionBean.getLearnerId());
		
		long attemptNumber = 1;
		
		if (attempts != null && attempts.size() > 0) {
			// Since attempts are order by attempt number, descending, then the first one is the max
			Attempt attempt = attempts.get(0);
			
			if (attempt.isSuspended()) {
				// If the user suspended the last attempt, let them return to it.
				attemptNumber = attempt.getAttemptNumber();
				navRequest = SeqNavRequests.NAV_RESUMEALL;
			} else if (attempt.isNotExited()) {
				// Or if the server crashed mid-session or something, then abandon the old one and start over
				attemptNumber = attempt.getAttemptNumber();
				log.warn("Abandoning old attempt and re-starting . . . ");
				sessionBean.setRestart(true);
				String result = sequencingService.navigate(SeqNavRequests.NAV_ABANDONALL, sessionBean, navigator, target);
			
				if (result != null && result.equals("_ENDSESSION_")) {
					attempt.setNotExited(false);
					resultService.saveAttempt(attempt);
				}
			} else {
				// Otherwise, we can start a new one
				attemptNumber = attempt.getAttemptNumber() + 1;
			}
		}
		
		sessionBean.setAttemptNumber(attemptNumber);
		
		return navRequest;
	}
	
	private boolean canLaunch(SessionBean sessionBean) {
		// Verify that the user is allowed to start a new attempt
		ContentPackage contentPackage = sessionBean.getContentPackage();
		
		// If the numberOfTries is not Unlimited then verify that we haven't hit the max
		if (contentPackage != null && contentPackage.getNumberOfTries() != -1) 	
			return (sessionBean.getAttemptNumber() <= contentPackage.getNumberOfTries());
		
		return true;
	}
	
	
	private String tryLaunch(SessionBean sessionBean, int navRequest, AjaxRequestTarget target) {		
		String result = sequencingService.navigate(navRequest, sessionBean, null, target);
		
		// Success is null.
		if (result == null || result.contains("_TOC_"))
			return null;
		
		// If we get an invalid nav request, chances are that we need to abandon and start again
		if (result.equals("_INVALIDNAVREQ_")) {
			sessionBean.setRestart(true);
			result = sequencingService.navigate(SeqNavRequests.NAV_ABANDONALL, sessionBean, null, target);

			// If it worked, start again
			if (result.equals("_ENDSESSION_")) {
				sessionBean.setRestart(true);
				result = sequencingService.navigate(SeqNavRequests.NAV_START, sessionBean, null, target);
			}
		// Otherwise, we may need to issue a 'None' 
		} else if (result.equals("_SEQBLOCKED_"))
			result = sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, target);
		
		
		return result;
	}
	
	
	private Component launch(SessionBean sessionBean, String lazyId, PageParameters pageParams, AjaxRequestTarget target) {
		
		String result = null;
		
		LocalResourceNavigator navigator = new LocalResourceNavigator();
		
		try {
			
			// If a content package has been suspended, we want to resume, otherwise start
			int navRequest = chooseStartOrResume(sessionBean, navigator, target);
			
			// Sometimes the user may want to override this
			if (pageParams.containsKey("navRequest"))
				navRequest = pageParams.getInt("navRequest");
			
			// Make sure the user's allowed to launch
			if (!canLaunch(sessionBean))
				return new DeniedPanel(lazyId, pageParams);
			
			result = tryLaunch(sessionBean, navRequest, target);
						
			if (result == null || result.contains("_TOC_")) {
				launchPanel = new LaunchPanel(lazyId, sessionBean, PlayerPage.this);
				
				loadSharedResources(sessionBean.getContentPackage().getResourceId());
				
				log.info("PlayerPage sco is " + sessionBean.getScoId());
				ScoBean scoBean = api.produceScoBean(sessionBean.getScoId(), sessionBean);
				scoBean.clearState();
				PlayerPage.this.synchronizeState(sessionBean, target);
				
				if (launchPanel.getTree().isEmpty()) {
					launchPanel.getTree().setVisible(false);
				}
				
				navigator.displayResource(sessionBean, target);
				
				return launchPanel;
			} 
			
			log.info("Result is " + result);
			
		} catch (Exception e) {
			result = e.getMessage();
			e.printStackTrace();
			
			log.error("Caught an exception: " , e);
		} 
		
		return new ChoicePanel(lazyId, pageParams, result);
	}
	
	public void synchronizeState(SessionBean sessionBean, AjaxRequestTarget target) {
		buttonForm.synchronizeState(sessionBean, target);
	}
	
	
	public boolean isVersioned()
	{
		return true;
	}
	
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.renderOnEventJavacript("window", "beforeunload", closeWindowBehavior.getCall());
	}
	
	
	private void loadSharedResources(String resourceId) {
		List<ContentPackageResource> resources = resourceService.getResources(resourceId);
		
		for (ContentPackageResource cpResource : resources) {
			String resourceName = cpResource.getPath();

			if (getApplication().getSharedResources().get(resourceName) == null) {
				
				WebResource webResource = new ContentPackageWebResource(cpResource);
				
				if (log.isDebugEnabled()) 
					log.debug("Adding a shared resource as " + resourceName);
				
				getApplication().getSharedResources().add(PlayerPage.class, resourceName, null, null, webResource);
				
			}
		}
		
		
		// Failing all else, we can grab the archive and unpack it
		//InputStream archiveStream = resourceService.getArchiveStream(resourceId);
		//unzip(new ZipInputStream(archiveStream), resourceId);
	}
	
	/*private void unzip(ZipInputStream zipStream, String resourceId) {
		ZipEntry entry;
		byte[] buffer = new byte[1024];
		int length;
		try {
			entry = (ZipEntry) zipStream.getNextEntry();
			while (entry != null) {
				String entryName = entry.getName();
								
				if (!entry.isDirectory()) {
					String resourceName = new StringBuilder(resourceId).append("/").append(entryName).toString();
					resourceName = resourceName.replace(" ", "%20");
					
					if (getApplication().getSharedResources().get(resourceName) == null) {
					
						final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						
						while ((length = zipStream.read(buffer)) > 0) {  
							outStream.write(buffer, 0, length);
			            }
			    		
						if (null != outStream)
							outStream.close();
						
						byte[] bytes = outStream.toByteArray();
			
						String mimeType = determineMimeType(entryName);
						
						ByteArrayResource resource = null;
						
						if (mimeType.equals("text/html"))
							resource = new CompressedByteArrayResource(mimeType, bytes);
						else 
							resource = new ByteArrayResource(mimeType, bytes);
						
						//LocalResource resource = new LocalResource(bytes, contentType);
	
						if (log.isDebugEnabled()) 
							log.debug("Adding a shared resource as " + resourceName);
						
						getApplication().getSharedResources().add(resourceName, resource);
						
					} else 
						log.debug("Not adding a shared resource for " + resourceName + " because it already exists");
				}
		    	entry = (ZipEntry) zipStream.getNextEntry();
		    }
		} catch (IOException ioe) {
			log.error("Caught an io exception reading from zip stream", ioe);
		} finally {
			try {
				if (null != zipStream)
					zipStream.close();
			} catch (IOException noie) {
				log.info("Caught an io exception closing streams!");
			}
		}
	}
	
	
	private String determineMimeType(String fileName) {
		String mimeType = new MimetypesFileTypeMap().getContentType(fileName);
		
		if (fileName.endsWith(".css"))
			mimeType = "text/css";
		else if (fileName.endsWith(".swf"))
			mimeType = "application/x-Shockwave-Flash";
		
		return mimeType;
	}
		
	
	public class CompressedByteArrayResource extends ByteArrayResource {
		
		private static final long serialVersionUID = 1L;
		
		public CompressedByteArrayResource(String contentType, byte[] array) {
			super(contentType, array);
		}
		
		@Override
		public IResourceStream getResourceStream() {
			
			final IResourceStream resourceStream = super.getResourceStream();
			
			return new CompressingResourceStream()	{
				private static final long serialVersionUID = 1L;
	
				protected IResourceStream getOriginalResourceStream() {
					return resourceStream;
				}
			};
		}
		
		protected void setHeaders(WebResponse response)
		{
			super.setHeaders(response);
			if (supportsCompression())
			{
				response.setHeader("Content-Encoding", "gzip");
			}
		}
	
		private boolean supportsCompression()
		{
			if (Application.get().getResourceSettings().getDisableGZipCompression())
			{
				return false;
			}
			if (RequestCycle.get() == null)
				return false;
			
			WebRequest request = (WebRequest)RequestCycle.get().getRequest();
			String s = request.getHttpServletRequest().getHeader("Accept-Encoding");
			if (s == null)
			{
				return false;
			}
			else
			{
				return s.indexOf("gzip") >= 0;
			}
		}
	}*/

	
	/*protected abstract class CompressingResourceStream implements IResourceStream
	{
		private static final long serialVersionUID = 1L;

		private SoftReference cache = new SoftReference(null);

		private Time timeStamp = null;


		public void close() throws IOException
		{
		}


		public String getContentType()
		{
			return getOriginalResourceStream().getContentType();
		}

		public InputStream getInputStream() throws ResourceStreamNotFoundException
		{
			return new ByteArrayInputStream(getCompressedContent());
		}

		public Locale getLocale()
		{
			return getOriginalResourceStream().getLocale();
		}

		public Time lastModifiedTime()
		{
			return getOriginalResourceStream().lastModifiedTime();
		}


		public long length()
		{
			return getCompressedContent().length;
		}


		public void setLocale(Locale locale)
		{
			getOriginalResourceStream().setLocale(locale);
		}


		private byte[] getCompressedContent()
		{
			IResourceStream stream = getOriginalResourceStream();
			try
			{
				byte ret[] = (byte[])cache.get();
				if (ret != null && timeStamp != null)
				{
					if (timeStamp.equals(stream.lastModifiedTime()))
					{
						return ret;
					}
				}

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				GZIPOutputStream zout = new GZIPOutputStream(out);
				Streams.copy(stream.getInputStream(), zout);
				zout.close();
				stream.close();
				ret = out.toByteArray();
				timeStamp = stream.lastModifiedTime();
				cache = new SoftReference(ret);
				return ret;
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			catch (ResourceStreamNotFoundException e)
			{
				throw new RuntimeException(e);
			}
		}

		protected abstract IResourceStream getOriginalResourceStream();
	}*/
	
	public class LocalResourceNavigator extends ResourceNavigator {

		private static final long serialVersionUID = 1L;

		@Override
		public Object getApplication() {
			return this.getApplication();
		}
		
		@Override
		protected ScormResourceService resourceService() {
			return PlayerPage.this.resourceService;
		}
		
	}

	public ButtonForm getButtonForm() {
		return buttonForm;
	}
	
	public LaunchPanel getLaunchPanel() {
		return launchPanel;
	}

	
}
