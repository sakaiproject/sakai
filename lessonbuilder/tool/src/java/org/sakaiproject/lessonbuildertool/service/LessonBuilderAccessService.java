/**********************************************************************************
 * $URL: 
 * $Id: 
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2011 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentFilterService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimplePageProperty;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.memory.api.SimpleConfiguration;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

/**
 * <p>
 * LessonBuilderAccessService implements /access/lessonbuilder
 * </p>
 */
@Slf4j
public class LessonBuilderAccessService {

	public static final int CACHE_MAX_ENTRIES = 5000;
	public static final int CACHE_TIME_TO_LIVE_SECONDS = 600;
	public static final int CACHE_TIME_TO_IDLE_SECONDS = 360;

	public static final String ATTR_SESSION = "sakai.session";
   
	public static final String COPYRIGHT_ACCEPTED_REFS_ATTR = "Access.Copyright.Accepted";

	// This is the date format for Last-Modified header
	public static final String RFC1123_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final Locale LOCALE_US = Locale.US;

	LessonBuilderAccessAPI lessonBuilderAccessAPI = null;

	public void setLessonBuilderAccessAPI(LessonBuilderAccessAPI s) {
		lessonBuilderAccessAPI = s;
	}

	SimplePageToolDao simplePageToolDao = null;

	public void setSimplePageToolDao(SimplePageToolDao d) {
		simplePageToolDao = d;
	}

	SecurityService securityService = null;

	public void setSecurityService(SecurityService s) {
		securityService = s;
	}

	ContentHostingService contentHostingService = null;

	public void setContentHostingService(ContentHostingService s) {
		contentHostingService = s;
	}

	EventTrackingService eventTrackingService = null;

	public void setEventTrackingService(EventTrackingService s) {
		eventTrackingService = s;
	}

	SessionManager sessionManager = null;

	public void setSessionManager(SessionManager s) {
		sessionManager = s;
	}

	public MessageLocator messageLocator;

	public void setMessageLocator(MessageLocator s) {
		messageLocator = s;
	}

	private ToolManager toolManager;

	public void setToolManager(ToolManager s) {
		toolManager = s;
	}

	private SiteService siteService;

	public void setSiteService(SiteService s) {
		siteService = s;
	}

	LessonEntity forumEntity = null;

	public void setForumEntity(Object e) {
		forumEntity = (LessonEntity) e;
	}

	LessonEntity quizEntity = null;

	public void setQuizEntity(Object e) {
		quizEntity = (LessonEntity) e;
	}

	LessonEntity assignmentEntity = null;

	ContentFilterService contentFilterService;

	public void setContentFilterService(ContentFilterService s) {
		contentFilterService = s;
	}

	public void setAssignmentEntity(Object e) {
		assignmentEntity = (LessonEntity) e;
	}

        LessonEntity bltiEntity = null;
        public void setBltiEntity(Object e) {
	    bltiEntity = (LessonEntity)e;
        }

	static MemoryService memoryService = null;

	public void setMemoryService(MemoryService m) {
		memoryService = m;
	}

	private GradebookIfc gradebookIfc = null;

	public void setGradebookIfc(GradebookIfc g) {
		gradebookIfc = g;
	}

	private AuthzGroupService authzGroupService;

	public void setAuthzGroupService(AuthzGroupService a) {
		authzGroupService = a;
	}

	protected static final long MAX_URL_LENGTH = 8192;
	protected static final int STREAM_BUFFER_SIZE = 102400;
	public static final String INLINEHTML = "lessonbuilder.inlinehtml";
	private boolean inlineHtml = ServerConfigurationService.getBoolean(INLINEHTML, true);
	public static final String USECSP = "lessonbuilder.use-csp-headers";
	public static final String USECSPSTUDENT = "lessonbuilder.use-csp-headers-for-student-content";

	public static final String CSPHeadersDefault = "sandbox allow-forms allow-scripts allow-top-navigation allow-popups allow-pointer-lock allow-popups-to-escape-sandbox";
	
	//This will either be true/false or a list of headers
	private String CSPHeaders = ServerConfigurationService.getString(USECSP, CSPHeadersDefault);
	//Use the regular property as the default
	private String CSPHeadersStudent = ServerConfigurationService.getString(USECSPSTUDENT, CSPHeaders);
	
	private boolean useCsp = true;
	private boolean useCspStudent = true;
        
        protected static final String MIME_SEPARATOR = "SAKAI_MIME_BOUNDARY";

        // uuid is the private key for sharing the session id
        private SecretKey sessionKey = null;
    
	public SecretKey getSessionKey() {
	    return sessionKey;
	}
	    
	// cache for availability check. Is the item available?
	// we cache only positive answers, because they can easily change
	// from no to yes in less than 10 min. going back is very unusual
	// item : userid => string true
	private static Cache accessCache = null;

        SecurityAdvisor allowReadAdvisor = new SecurityAdvisor() {
		public SecurityAdvice isAllowed(String userId, String function, String reference) {
		    if("content.read".equals(function) || "content.hidden".equals(function)) {
			return SecurityAdvice.ALLOWED;
		    }else {
			return SecurityAdvice.PASS;
		    }
		}
	    };

	public void init() {
		lessonBuilderAccessAPI.setHttpAccess(getHttpAccess());
		accessCache = memoryService.createCache(
				"org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService.cache",
				new SimpleConfiguration<Object, Object>(CACHE_MAX_ENTRIES, CACHE_TIME_TO_LIVE_SECONDS, CACHE_TIME_TO_IDLE_SECONDS)
		);

		SimplePageItem metaItem = null;
		// Get crypto session key from metadata item
		//   we have to keep it in the database so it's the same on all servers
		// There's no entirely sound way to create the item if it doesn't exist
		//   other than by getting a table lock. Fortunately this will only be
		//   needed when the entry is created.

		SimplePageProperty prop = simplePageToolDao.findProperty("accessCryptoKey");
		if (prop == null) {
		    try {
			sessionKey = KeyGenerator.getInstance("Blowfish").generateKey();
			// need string version to save in item
			byte[] keyBytes = ((SecretKeySpec)sessionKey).getEncoded();
			// set attribute to hex version of key
			prop = simplePageToolDao.makeProperty("accessCryptoKey", DatatypeConverter.printHexBinary(keyBytes));
			simplePageToolDao.quickSaveItem(prop);
		    } catch (Exception e) {
			log.info("unable to init cipher for session " + e);
			// in case of race condition, our save will fail, but we'll be able to get a value
			// saved by someone else
			simplePageToolDao.flush();
			prop = simplePageToolDao.findProperty("accessCryptoKey");
		    }
		}

		if (prop != null) {
		    String keyString = prop.getValue();
		    byte[] keyBytes = DatatypeConverter.parseHexBinary(keyString);
		    sessionKey = new SecretKeySpec(keyBytes, "Blowfish");
		}
		
		//Explicit true/false
		if ("true".equals(CSPHeaders)) {
			useCsp = true;
			CSPHeaders = CSPHeadersDefault;
		}
		else if ("false".equals(CSPHeaders)) {
			useCsp = false;
			//No headers needed
			CSPHeaders = "";
		}
		else {
			//Custom headers but if using separate content domain we don't need this protection
			useCsp = !ServerConfigurationService.getBoolean("content.separateDomains", false);
		}

		//For student content pages
		//Explicit true/false
		if ("true".equals(CSPHeadersStudent)) {
			useCspStudent = true;
			CSPHeadersStudent = CSPHeadersDefault;
		}
		else if ("false".equals(CSPHeadersStudent)) {
			useCspStudent = false;
			//No headers needed
			CSPHeadersStudent = "";
		}
		else {
			//Custom headers but if using separate content domain we don't need this protection
			useCspStudent = useCsp;
		}


	}

	public void destroy() {
	    //		accessCache.destroy();
	    //		accessCache = null;
	}


	// references are currently of the form /access/lessonbuilder/item/NNN/group/MMM/...
        // the purpose of using /access/lessonbuilder rather than /access/content is
        // that we can do access control by Lesson Builder's rules. However users can
        // still go to the item directly unless you hide the content in resources.
        // we can't obscure the URLs, or references in HTML won't work.

        // access checks:
        //  target URL must be in the same site as the item. /access/content will
        //     be used to access material in other sites. That's to prevent people 
        //     from using an item in a site they control to access other user's data
        //  if the target URL is the actual item referred to in /item/NNN, just do
        //     normal Lesson Builder checks
        //  otherwise it should be a file referred to, e.g. images referred to in
        //     an HTML file. We have no good way to check whether that's OK. So we
        //     just check the item in /item/NNN. But in addition, we see whether there's
        //     a Lesson Builder item in the same site that has prerequisites. If so, we
        //     refuse the access. That lets you control something like an answer sheet and
        //     make sure a user can't get to it by crafting a URL based on a item that 
        //     they have acccess to. But there's no obvious way to know whether an
        //     arbitrary file in resources is OK to show or not. 


	public HttpAccess getHttpAccess() {
		return new HttpAccess() {

			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
					EntityAccessOverloadException, EntityCopyrightException {
				
			    // preauthorized by encrypted key
				boolean isAuth = false;

				// if the id is null, the request was for just ".../content"
				String refId = ref.getId();
				if (refId == null) {
					refId = "";
				}

				if (!refId.startsWith("/item")) {
					throw new EntityNotDefinedException(ref.getReference());
				}

				String itemString = refId.substring("/item/".length());
				// string is of form /item/NNN/url. get the number
				int i = itemString.indexOf("/");
				if (i < 0) {
					throw new EntityNotDefinedException(ref.getReference());
				}

				// get session. The problem here is that some multimedia tools don't reliably
				// pass JSESSIONID

				String sessionParam = req.getParameter("lb.session");

				if (sessionParam != null) {
				    try {
					Cipher sessionCipher = Cipher.getInstance("Blowfish");
					sessionCipher.init(Cipher.DECRYPT_MODE, sessionKey);
					byte[] sessionBytes = DatatypeConverter.parseHexBinary(sessionParam);
					sessionBytes = sessionCipher.doFinal(sessionBytes);
					String sessionString = new String(sessionBytes);
					int j = sessionString.indexOf(":");
					String sessionId = sessionString.substring(0, j);
					String url = sessionString.substring(j+1);

					UsageSession s = UsageSessionService.getSession(sessionId);
					if (s == null || s.isClosed() || url == null || ! url.equals(refId)) {
					    throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), 
					       ContentHostingService.AUTH_RESOURCE_READ, ref.getReference());
					} else {
					    isAuth = true;
					}

				    } catch (Exception e) {
					log.info("unable to decode lb.session " + e);
				    }
				}

				// basically there are two checks to be done: is the item accessible in Lessons,
				// and is the underlying resource accessible in Sakai.
				// This code really does check both. Sort of. 
				// 1) it checks accessibility to the containing page by seeing if it has been visited.
				//  This is stricter than necessary, but there's no obvious reason to let people use this
				//  who aren't following an actual URL we gave them.
				// 2) it checks group access as part of the normal resource permission check. Sakai
				//  should sync the two. We actually don't check it for items in student home directories,
				//  as far as I can tell
				// 3) it checks availability (prerequisites) by calling the code from SimplePageBean
				// We could rewrite this with the new LessonsAccess methods, but since we have to do
				// resource permission checking also, and there's some duplication, it doesn't seem worth
				// rewriting this code. What I've done is review it to make sure it does the same thing.

				String id = itemString.substring(i);
				itemString = itemString.substring(0, i);

				boolean pushedAdvisor = false;
				
				try {
					securityService.pushAdvisor(allowReadAdvisor);
					
					pushedAdvisor = true;
				
					Long itemId = 0L;
					try {
						itemId = (Long) Long.parseLong(itemString);
					} catch (Exception e) {
						throw new EntityNotDefinedException(ref.getReference());
					}
					
					// say we've read this
					if (itemId != 0L)
					    track(itemId.longValue(), sessionManager.getCurrentSessionUserId());

	// code here is also in simplePageBean.isItemVisible. change it there
	// too if you change this logic

					SimplePageItem item = simplePageToolDao.findItem(itemId.longValue());
					SimplePage currentPage = simplePageToolDao.getPage(item.getPageId());
					String owner = currentPage.getOwner();  // if student content
					String group = currentPage.getGroup();  // if student content
					
					//If owner != null or group != null it's a student page, if both null/either null it isn't
					boolean studentcontent = (owner != null || group != null);

					if (group != null)
					    group = "/site/" + currentPage.getSiteId() + "/group/" + group;
					String currentSiteId = currentPage.getSiteId();
					
					// first let's make sure the user is allowed to access
					// the containing page

					if (!isAuth && !canReadPage(currentSiteId)) {
					    throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), 
					       ContentHostingService.AUTH_RESOURCE_READ, ref.getReference());
					}
					    
					// If the resource is the actual one in the item, or
					// it is in the containing folder, then do lesson builder checking.
					// otherwise do normal resource checking

					if (!isAuth) {

					boolean useLb = false;

					// I've seen sakai id's with //, not sure why. they work but will mess up the comparison
					String itemResource = item.getSakaiId().replace("//","/");

					// only use lb security if the user has visited the page
					// this handles the various page release issues, although
					// it's not quite as flexible as the real code. But I don't
					// see any reason to help the user follow URLs that they can't
					// legitimately have seen.

					if (simplePageToolDao.isPageVisited(item.getPageId(), sessionManager.getCurrentSessionUserId(), owner)) {
					    if (id.equals(itemResource) || item.getAttribute("multimediaUrl") != null) {
						useLb = true;
					    } else {
						// not exact, but see if it's in the containing folder
						int endFolder = itemResource.lastIndexOf("/");
						if (endFolder > 0) {
						    String folder = itemResource.substring(0, endFolder+1);
						    if (id.startsWith(folder))
							useLb = true;
						}
					    }
					}

					if (useLb) {
					    // key into access cache
					    String accessKey = itemString + ":" + sessionManager.getCurrentSessionUserId();
					    // special access if we have a student site and item is in worksite of one of the students
					    // Normally we require that the person doing the access be able to see the file, but in
					    // that specific case we allow the access. Note that in order to get a sakaiid pointing
					    // into the user's space, the person editing the page must have been able to read the file.
					    // this allows a user in your group to share any of your resources that he can see.
					    String usersite = null;
					    if (owner != null && group != null && id.startsWith("/user/")) {
						String username = id.substring(6);
						int slash = username.indexOf("/");
						if (slash > 0)
						    usersite = username.substring(0,slash);
						// normally it is /user/EID, so convert to userid
						try {
						    usersite = UserDirectoryService.getUserId(usersite);
						} catch (Exception e) {};
						String itemcreator = item.getAttribute("addedby");
						// suppose a member of the group adds a resource from another member of
						// the group. (This will only work if they have read access to it.)
						// We don't want to gimick access in that case. I think if you
						// add your own item, you've given consent. But not if someone else does.
						// itemcreator == null is for items added before this patch. I'm going to
						// continue to allow access for them, to avoid breaking existing content.
						if (usersite != null && itemcreator != null && !usersite.equals(itemcreator))
						    usersite = null;
					    }							

					// code here is also in simplePageBean.isItemVisible. change it there
					// too if you change this logic

					    // for a student page, if it's in one of the groups' worksites, allow it
					    // The assumption is that only one of those people can put content in the
					    // page, and then only if the can see it.

					    if (owner != null && usersite != null && authzGroupService.getUserRole(usersite, group) != null) {
						// OK
					    } else if (owner != null && group == null && id.startsWith("/user/" + owner)) {
						// OK
					    } else if (item.getAttribute("multimediaUrl") != null) {
						// OK
					    } else {

						// do normal checking for other content
						if (pushedAdvisor) {
						    securityService.popAdvisor();
						    pushedAdvisor = false;
						}
						// our version of allowget does not check hidden but does everything else
						// if it's a student page, however use the normal check so students can't
						// use this to bypass release control
						if (owner == null && !allowGetResource(id, currentSiteId) ||
						    owner != null && !contentHostingService.allowGetResource(id)) {
						    throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), 
							      ContentHostingService.AUTH_RESOURCE_READ, ref.getReference());
						}

						securityService.pushAdvisor(allowReadAdvisor);
						pushedAdvisor = true;

					    }

					    // now enforce LB access restrictions if any
					    if (item != null && item.isPrerequisite() && !"true".equals((String) accessCache.get(accessKey))) {
						// computing requirements is so messy that it's worth
						// instantiating
						// a SimplePageBean to do it. Otherwise we have to duplicate
						// lots of
						// code that changes. And we want it to be a transient bean
						// because there are
						// caches that we aren't trying to manage in the long term
						// but don't do this unless the item needs checking

						if (!canSeeAll(currentPage.getSiteId())) {
						SimplePageBean simplePageBean = new SimplePageBean();
						simplePageBean.setMessageLocator(messageLocator);
						simplePageBean.setToolManager(toolManager);
						simplePageBean.setSecurityService(securityService);
						simplePageBean.setSessionManager(sessionManager);
						simplePageBean.setSiteService(siteService);
						simplePageBean.setContentHostingService(contentHostingService);
						simplePageBean.setSimplePageToolDao(simplePageToolDao);
						simplePageBean.setForumEntity(forumEntity);
						simplePageBean.setQuizEntity(quizEntity);
						simplePageBean.setAssignmentEntity(assignmentEntity);
						simplePageBean.setBltiEntity(bltiEntity);
						simplePageBean.setGradebookIfc(gradebookIfc);
						simplePageBean.setCurrentSiteId(currentPage.getSiteId());
						simplePageBean.setCurrentPage(currentPage);
						simplePageBean.setCurrentPageId(currentPage.getPageId());
						simplePageBean.init();

						if (!simplePageBean.isItemAvailable(item, item.getPageId())) {
							throw new EntityPermissionException(null, null, null);
						}
						}
						accessCache.put(accessKey, "true");
						
					    }
					} else {
					    // normal security. no reason to use advisor
					    if(pushedAdvisor) securityService.popAdvisor();
					    pushedAdvisor = false;

					    // not uselb -- their allowget, not ours. theirs checks hidden
					    if (!contentHostingService.allowGetResource(id)) {
						throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(),  
								ContentHostingService.AUTH_RESOURCE_READ, ref.getReference());
					    }
					}
					
					}

					// access checks are OK, get the thing

					// first see if it's not in resources, i.e.
					// if it doesn't start with /access/content it's something odd. redirect to it.
					// probably resources access control won't apply to it
					String url = contentHostingService.getUrl(id);					
					// https://heidelberg.rutgers.edu/access/citation/content/group/24da8519-08c2-4c8c-baeb-8abdfd6c69d7/New%20Citation%20List

					if (item.getAttribute("multimediaUrl") != null) {
					    eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_READ, "/lessonbuilder/item/" + item.getId(), false));
					    res.sendRedirect(item.getAttribute("multimediaUrl"));
					    return;
					}

					int n = url.indexOf("//");
					if (n > 0) {
					    n = url.indexOf("/", n+2);
					    if (n > 0) {
						String path = url.substring(n);
						if (!path.startsWith("/access/content")) {
						    res.sendRedirect(url);
						    return;
						}
					    }
					}

					ContentResource resource = null;
					try {
						resource = contentHostingService.getResource(id);
					} catch (IdUnusedException e) {
						throw new EntityNotDefinedException(e.getId());
					} catch (PermissionException e) {
						throw new EntityPermissionException(e.getUser(), e.getLock(), e.getResource());
					} catch (TypeException e) {
						throw new EntityNotDefinedException(id);
					}

					// we only do copyright on resources. I.e. not on inline things,which are MULTIMEDIA
					if (item.getType() == SimplePageItem.RESOURCE && 
					    needsCopyright(resource)) {
					    throw new EntityCopyrightException(resource.getReference());
					}  
					try {
						// Wrap it in any filtering needed.
						resource = contentFilterService.wrap(resource);

						// from contenthosting
						res.addHeader("Cache-Control", "must-revalidate, private");
						res.addHeader("Expires", "-1");

					    // following cast is redundant is current kernels, but is needed for Sakai 2.6.1
						long len = (long)resource.getContentLength();
						String contentType = resource.getContentType();
						ResourceProperties rp = resource.getProperties();
						long lastModTime = 0;

						try {
							Instant modTime = rp.getInstantProperty(ResourceProperties.PROP_MODIFIED_DATE);
							lastModTime = modTime.getEpochSecond();
						} catch (Exception e1) {
							log.info("Could not retrieve modified time for: " + resource.getId());
						}

						// KNL-1316 tell the browser when our file was last modified for caching reasons
						if (lastModTime > 0) {
							SimpleDateFormat rfc1123Date = new SimpleDateFormat(RFC1123_DATE, LOCALE_US);
							rfc1123Date.setTimeZone(TimeZone.getTimeZone("GMT"));
							res.addHeader("Last-Modified", rfc1123Date.format(lastModTime));
						}
						
						// 	for url resource type, encode a redirect to the body URL
						// in 2.10 have to check resourcetype, but in previous releasese
						// it doesn't get copied in site copy, so check content type. 10 doesn't set the contenttype to url
						// so we have to check both to work in all versions
						if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL) ||
						    "org.sakaiproject.content.types.urlResource".equalsIgnoreCase(resource.getResourceType())) 
						{
							if (len < MAX_URL_LENGTH) {

								byte[] content = resource.getContent();
								if ((content == null) || (content.length == 0)) {
									throw new IdUnusedException(ref.getReference());
								}
								
								// 	An invalid URI format will get caught by the outermost catch block
								URI uri = new URI(new String(content, "UTF-8"));
								eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ, resource.getReference(null), false));

								String decodedUrl = null;
								if (id.endsWith(".URL")) {
								    // created by resources tool. Use new processing for it.
									decodedUrl = URLDecoder.decode(uri.toString(), "UTF-8");
									decodedUrl = contentHostingService.expandMacros(decodedUrl);
								} else {
								    // created by Lessons. Use it as is.
								    decodedUrl = uri.toString();
								}

								res.sendRedirect(decodedUrl);
							} else {
								// 	we have a text/url mime type, but the body is too
								// 	long to issue as a redirect
								throw new EntityNotDefinedException(ref.getReference());
							}
						}

						else
						{
							// use the last part, the file name part of the id, for the download file name
							String fileName = Web.encodeFileName(req, Validator.getFileName(ref.getId()));
							String disposition = null;
							
							boolean inline = false;
							boolean dangerous = false;

							if (Validator.letBrowserInline(contentType)) {
							    // type can be inline, but if HTML we have more checks to do
							    // because browsers upgrade some types to HTML the check isn't just for HTML

							    String lcct = contentType.toLowerCase();
							    if ( lcct.startsWith("text/") || lcct.startsWith("image/") 
								 || lcct.contains("html") || lcct.contains("script"))
								dangerous = true;

							    // inlineHtml is true by default. This logic is for sites
							    // that want to be super-careful
							    if (inlineHtml || !dangerous)
								// easy cases: not HTML or HTML always OK
								inline = true;
							    else {
								// HTML and html is not allowed globally. code copied from BaseContentServices
								boolean fileInline = false;
								boolean folderInline = false;

								try {
								    fileInline = rp.getBooleanProperty(ResourceProperties.PROP_ALLOW_INLINE);
								}
								catch (EntityPropertyNotDefinedException e) {
								    // we expect this so nothing to do!
								}

								if (!fileInline) 
								    try
									{
									    folderInline = resource.getContainingCollection().getProperties().getBooleanProperty(ResourceProperties.PROP_ALLOW_INLINE);
									}
								    catch (EntityPropertyNotDefinedException e) {
									// we expect this so nothing to do!
								    }		
						
								if (fileInline || folderInline) {
								    inline = true;
								}
							    }
							}								

							if (inline) {
								disposition = "inline; filename=\"" + fileName + "\"";
							} else {
								disposition = "attachment; filename=\"" + fileName + "\"";
							}
							
							// note that by default inline is always set. If we have inline and dangerous (i.e. HTML or
							// potentially HTML, we set security headers to provide some protection.

							//This differens if it's a student page or non-student page
							if (studentcontent && inline && dangerous && useCspStudent) {
							    res.addHeader("Content-Security-Policy", CSPHeadersStudent);
							}
							else if (!studentcontent && inline && dangerous && useCsp) {
							    res.addHeader("Content-Security-Policy", CSPHeaders);
							}

							// NOTE: Only set the encoding on the content we have to.
							// Files uploaded by the user may have been created with different encodings, such as ISO-8859-1;
							// rather than (sometimes wrongly) saying its UTF-8, let the browser auto-detect the encoding.
							// If the content was created through the WYSIWYG editor, the encoding does need to be set (UTF-8).
							String encoding = resource.getProperties().getProperty(ResourceProperties.PROP_CONTENT_ENCODING);
							if (encoding != null && encoding.length() > 0) {
								contentType = contentType + "; charset=" + encoding;
							}

				// KNL-1316 let's see if the user already has a cached copy. Code copied and modified from Tomcat DefaultServlet.java
				long headerValue = req.getDateHeader("If-Modified-Since");
				if (headerValue != -1 && (lastModTime < headerValue + 1000)) {
				    // The entity has not been modified since the date specified by the client. This is not an error case.
				    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				    return; 
				}

				// If there is a direct link to the asset, no sense streaming it.
				// Send the asset directly to the load-balancer or to the client
				URI directLinkUri = contentHostingService.getDirectLinkToAsset(resource);

		        ArrayList<Range> ranges = parseRange(req, res, len);

				if (directLinkUri != null || req.getHeader("Range") == null || (ranges == null) || (ranges.isEmpty())) {
					res.addHeader("Accept-Ranges", "none");
					res.setContentType(contentType);
					res.addHeader("Content-Disposition", disposition);
					// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4187336
					if (len <= Integer.MAX_VALUE) {
						res.setContentLength((int)len);
					} else {
						res.addHeader("Content-Length", Long.toString(len));
					}

					// SAK-30455: Track event now so the direct link still records a content.read
					eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ, resource.getReference(null), false));

					// Bypass loading the asset and just send the user a link to it.
					if (directLinkUri != null) {
						if (ServerConfigurationService.getBoolean("cloud.content.sendfile", false)) {
							int hostLength = new String(directLinkUri.getScheme() + "://" + directLinkUri.getHost()).length();
							String linkPath = "/sendfile" + directLinkUri.toString().substring(hostLength);
							if (log.isDebugEnabled()) {
								log.debug("X-Sendfile: " + linkPath);
							}

							// Nginx uses X-Accel-Redirect and Apache and others use X-Sendfile
							res.addHeader("X-Accel-Redirect", linkPath);
							res.addHeader("X-Sendfile", linkPath);
							return;
						}
						else if (ServerConfigurationService.getBoolean("cloud.content.directurl", true)) {
							res.sendRedirect(directLinkUri.toString());
							return;
						}
					}
		        	
					// stream the content using a small buffer to keep memory managed
					InputStream content = null;
					OutputStream out = null;
	
					try
					{
						content = resource.streamContent();
						if (content == null)
						{
							throw new IdUnusedException(ref.getReference());
						}
	

						// set the buffer of the response to match what we are reading from the request
						if (len < STREAM_BUFFER_SIZE)
						{
							res.setBufferSize((int)len);
						}
						else
						{
							res.setBufferSize(STREAM_BUFFER_SIZE);
						}
	
						out = res.getOutputStream();
	
						copyRange(content, out, 0, len-1);
					}
					catch (ServerOverloadException e)
					{
						throw e;
					}
					catch (Exception ignore)
					{
					}
					finally
					{
						// be a good little program and close the stream - freeing up valuable system resources
						if (content != null)
						{
							content.close();
						}
	
						if (out != null)
						{
							try
							{
								out.close();
							}
							catch (Exception ignore)
							{
							}
						}
					}

		        }
		        else 
		        {
		        	// Output partial content. Adapted from Apache Tomcat 5.5.27 DefaultServlet.java
		            res.addHeader("Accept-Ranges", "bytes");
		        	res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

		            if (ranges.size() == 1) {

		            	// Single response
		            	
		                Range range = (Range) ranges.get(0);
		                res.addHeader("Content-Range", "bytes "
		                                   + range.start
		                                   + "-" + range.end + "/"
		                                   + range.length);
		                long length = range.end - range.start + 1;
		                if (length < Integer.MAX_VALUE) {
		                    res.setContentLength((int) length);
		                } else {
		                    // Set the content-length as String to be able to use a long
		                    res.setHeader("content-length", "" + length);
		                }

						res.addHeader("Content-Disposition", disposition);

		                if (contentType != null) {
		                    res.setContentType(contentType);
		                }

						// stream the content using a small buffer to keep memory managed
						InputStream content = null;
						OutputStream out = null;
		
						try
						{
							content = resource.streamContent();
							if (content == null)
							{
								throw new IdUnusedException(ref.getReference());
							}
				
							// set the buffer of the response to match what we are reading from the request
							if (len < STREAM_BUFFER_SIZE)
							{
								res.setBufferSize((int)len);
							}
							else
							{
								res.setBufferSize(STREAM_BUFFER_SIZE);
							}
		
							out = res.getOutputStream();

							copyRange(content, out, range.start, range.end);

						}
						catch (ServerOverloadException e)
						{
							throw e;
						}
						catch (SocketException e)
						{
							//a socket exception usualy means the client aborted the connection or similar
							if (log.isDebugEnabled())
							{
								log.debug("SocketExcetion", e);
							}
						}
						catch (Exception ignore)
						{
						}
						finally
						{
							// be a good little program and close the stream - freeing up valuable system resources
							IOUtils.closeQuietly(content);
							IOUtils.closeQuietly(out);
						}
		              
		            } else {

		            	// Multipart response

		            	res.setContentType("multipart/byteranges; boundary=" + MIME_SEPARATOR);

						// stream the content using a small buffer to keep memory managed
						OutputStream out = null;
		
						try
						{
							// set the buffer of the response to match what we are reading from the request
							if (len < STREAM_BUFFER_SIZE)
							{
								res.setBufferSize((int)len);
							}
							else
							{
								res.setBufferSize(STREAM_BUFFER_SIZE);
							}
		
							out = res.getOutputStream();

			            	copyRanges(resource, out, ranges.iterator(), contentType);

						}
						catch (SocketException e)
						{
							//a socket exception usualy means the client aborted the connection or similar
							if (log.isDebugEnabled())
							{
								log.debug("SocketExcetion", e);
							}
						}
						catch (Exception ignore)
						{
							log.error("Swallowing exception", ignore);
						}
						finally
						{
							// be a good little program and close the stream - freeing up valuable system resources
							IOUtils.closeQuietly(out);
						}
		              
		            } // output multiple ranges

		        } // output partial content 

						}

					} catch (Exception t) {
					    throw new EntityNotDefinedException(ref.getReference());
					    // following won't work in 2.7.1
					    // throw new EntityNotDefinedException(ref.getReference(), t);
					}
					
			// not sure why we're trapping exceptions and calling them not defined, but
			// a few types are needed by the caller
				} catch(EntityCopyrightException ce) {
				    // copyright exception needs to go as is, to give copyright alert
				    throw ce;
				} catch(EntityPermissionException pe) {
				    // also want permission exceptions; it will generate a login page
				    throw pe;
				} catch(Exception ex) {
					throw new EntityNotDefinedException(ref.getReference());
				}finally {
				    if(pushedAdvisor) securityService.popAdvisor();
				}
			}
		};
	}

	/**
	 * Range inner class. From Apache Tomcat DefaultServlet.java 
	 *
	 */
    protected class Range {

        public long start;
        public long end;
        public long length;

        /**
         * Validate range.
         */
        public boolean validate() {
            if (end >= length)
                end = length - 1;
            return ( (start >= 0) && (end >= 0) && (start <= end)
                     && (length > 0) );
        }

        public void recycle() {
            start = 0;
            end = 0;
            length = 0;
        }

    }

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    protected ArrayList<Range> parseRange(HttpServletRequest request,
                                HttpServletResponse response,
                                long fileLength)
        throws IOException {

    	/* Commented out pending implementation of last-modified / if-modified.
    	 * See http://jira.sakaiproject.org/jira/browse/SAK-3916
    	
        // Checking If-Range

    	String headerValue = request.getHeader("If-Range");

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (Exception e) {
                ;
            }

            String eTag = getETag(resourceAttributes);
            long lastModified = resourceAttributes.getLastModified();

            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim()))
                    return FULL;

            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000))
                    return FULL;

            }

        }
        
    	*/
    	
        if (fileLength == 0)
            return null;

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");

        if (rangeHeader == null)
            return null;
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError
                (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully
        // parsed.
        ArrayList result = new ArrayList();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.start = Long.parseLong
                        (rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1)
                        currentRange.end = Long.parseLong
                            (rangeDefinition.substring
                             (dashPos + 1, rangeDefinition.length()));
                    else
                        currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }

    /**
     * Copy the partial contents of the specified input stream to the specified
     * output stream.
     * 
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    protected IOException copyRange(InputStream istream,
                                  OutputStream ostream,
                                  long start, long end) {

    	try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        byte buffer[] = new byte[STREAM_BUFFER_SIZE];
        int len = buffer.length;
        while ( (bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length)
                break;
        }

        return exception;
    }

  
    /**
     * Copy the contents of the specified input stream to the specified
     * output stream in a set of chunks as per the specified ranges.
     *
     * @param InputStream The input stream to read from
     * @param out The output stream to write to
     * @param ranges Enumeration of the ranges the client wanted to retrieve
     * @param contentType Content type of the resource
     * @exception IOException if an input/output error occurs
     */
    protected void copyRanges(ContentResource content, OutputStream out,
                      Iterator ranges, String contentType)
        throws IOException {

        IOException exception = null;
                        
        while ( (exception == null) && (ranges.hasNext()) ) {

            Range currentRange = (Range) ranges.next();
                  
            // Writing MIME header.
            IOUtils.write("\r\n--" + MIME_SEPARATOR + "\r\n", out);
            if (contentType != null)
                IOUtils.write("Content-Type: " + contentType + "\r\n", out);
            IOUtils.write("Content-Range: bytes " + currentRange.start
                           + "-" + currentRange.end + "/"
                           + currentRange.length + "\r\n", out);
            IOUtils.write("\r\n", out);

            // Printing content
			InputStream in = null;
			try {
				in = content.streamContent();
			} catch (ServerOverloadException se) {
				exception = new IOException("ServerOverloadException reported getting inputstream");
			}
			
            InputStream istream =
                new BufferedInputStream(in, STREAM_BUFFER_SIZE);
          
            exception = copyRange(istream, out, currentRange.start, currentRange.end);

            IOUtils.closeQuietly(istream);
        }

        IOUtils.write("\r\n--" + MIME_SEPARATOR + "--\r\n", out);
        
        // Rethrow any exception that has occurred
        if (exception != null) {
            throw exception;
        }
    }


    // similar to SimplePageBean.track, but doesn't need bean;
    public void track(long itemId, String userId) {
	if (userId == null)
	    userId = ".anon";
	SimplePageLogEntry entry = simplePageToolDao.getLogEntry(userId, itemId, -1L);
	// don't need a toolid for this entry. it's only used for pages
	if (entry == null) {
	    entry = simplePageToolDao.makeLogEntry(userId, itemId, null);
	    simplePageToolDao.quickSaveItem(entry);
	} else {
	    // with path == null it doesn't seem like this would actually do anything
	    //	    simplePageToolDao.quickUpdate(entry);
	}
    }    

	// simplified versions of stuff from BaseContentService

        public boolean allowGetResource(String id, String siteId) {
	        return unlockCheck(ContentHostingService.AUTH_RESOURCE_READ, id, siteId);
	}

	public String getReference(String id) {
		return "/content" + id; // apparently
	}

        // specialized version for resources only. assumes it is called with advisor in place
        protected boolean unlockCheck(String lock, String id, String siteId) {
		boolean isAllowed = securityService.isSuperUser();
		if (!isAllowed) {
			// make a reference from the resource id, if specified
			String ref = null;
			if (id != null) {
				ref = getReference(id);
			}

			// if site maintainer or see all, allow any access.
			// used to check this after the unlock below, but a user with see all
			// may be prevented by group access from seeing the resource, but
			// we still want them to see it.
			if (canSeeAll(siteId) && id.startsWith("/group/" + siteId))
			    return true;

			// this will check basic access and group access. FOr that normal Sakai
			// checking is fine.
			isAllowed = ref != null && securityService.unlock(lock, ref);

			// availability is for hidden and release date. Do our own, because
			// we implement release date but ignore hidden. That lets faculty hide
			// resources from normal view but still see them through Lessons

			if (isAllowed) {

			    boolean pushedAdvisor = false;
			    ContentResource resource = null;
			    // we're used with allow all advisor in effect, so this is OK
			    try {
				// need advisor so we can look at resource to get its properties
				securityService.pushAdvisor(allowReadAdvisor);
				pushedAdvisor = true;

				resource = contentHostingService.getResource(id);
				isAllowed = isAvailable(resource);

				securityService.popAdvisor();
				pushedAdvisor = false;
			    } catch (Exception e) {
				isAllowed = false;
			    } finally {
				if (pushedAdvisor)
				    securityService.popAdvisor();
			    }
			}

		}
		
		return isAllowed;
	}

        // check release dates, both resource and collection. assumes it is called with advisor in place
        // NOTE: does not enforce hidden
        protected boolean isAvailable(ContentEntity entity) {
        Instant now = Instant.now();
        Instant releaseDate = entity.getReleaseInstant();
	    if (releaseDate != null && ! releaseDate.isBefore(now))
		return false;
	    Instant retractDate = entity.getRetractInstant();
	    if (retractDate != null && ! retractDate.isAfter(now))
		return false;
	    ContentEntity parent = (ContentEntity)entity.getContainingCollection();
	    if (parent != null)
		return isAvailable(parent);
	    else
		return true;
	}

	public boolean canReadPage(String siteId) {
		String ref = "/site/" + siteId;
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	}

	public boolean canSeeAll(String siteId) {
		String ref = "/site/" + siteId;
		if (securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref))
		    return true;
		if (securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL, ref))
		    return true;
		return false;
	}

    /* this is a public service, used by other modules */
	public boolean needsCopyright (String sakaiId) {
	    try {
		ContentResource resource = contentHostingService.getResource(sakaiId);
		return needsCopyright(resource);
	    } catch (Exception e) {
		return false;
	    }
	}

	public boolean needsCopyright (ContentResource resource) {

	    try {
		ResourceProperties props = resource.getProperties();
		boolean requiresCopyrightAgreement = 
		    props.getProperty(ResourceProperties.PROP_COPYRIGHT_ALERT) != null;

		if (!requiresCopyrightAgreement)
		    return false;
		
		// requires copyright agreement. See if user has agreed

		Collection accepted = (Collection) sessionManager.getCurrentSession().getAttribute(COPYRIGHT_ACCEPTED_REFS_ATTR);
		// if no collection, initialize it
		if (accepted == null) {
		    accepted = new Vector();
		    sessionManager.getCurrentSession().setAttribute(COPYRIGHT_ACCEPTED_REFS_ATTR, accepted);
		}
		
		// now see if user has accepted copyright
                if (!accepted.contains(resource.getReference()))
		    return true;

	    } catch (Exception e) {
		// if we can't get the resource, attempt to enforce copyright
		// will almost certainly fail, so fall through to false
	    }

	    return false;
	}

	public void acceptCopyright (String sakaiId) {
	    try {
		Collection accepted = (Collection) sessionManager.getCurrentSession().getAttribute(COPYRIGHT_ACCEPTED_REFS_ATTR);
		// if no collection, initialize it
		if (accepted == null) {
		    accepted = new Vector();
		    sessionManager.getCurrentSession().setAttribute(COPYRIGHT_ACCEPTED_REFS_ATTR, accepted);
		}
		
		accepted.add(contentHostingService.getReference(sakaiId));

	    } catch (Exception e) {
		// if can't find session or reference, not much we can do
	    }
	}

}
