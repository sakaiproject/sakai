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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.SecurityAdvisor.SecurityAdvice;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

import uk.org.ponder.messageutil.MessageLocator;

/**
 * <p>
 * LessonBuilderAccessService implements /access/lessonbuilder
 * </p>
 */
public class LessonBuilderAccessService {
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

	protected static final long MAX_URL_LENGTH = 8192;
	protected static final int STREAM_BUFFER_SIZE = 102400;

	// cache for availability check. Is the item available?
	// we cache only positive answers, because they can easily change
	// from no to yes in less than 10 min. going back is very unusual
	// item : userid => string true
	private static Cache accessCache = null;
	protected static final int DEFAULT_EXPIRATION = 10 * 60;

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

		accessCache = memoryService.newCache("org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService.cache");
	}

	public void destroy() {
		accessCache.destroy();
		accessCache = null;
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
				
				// if the id is null, the request was for just ".../content"
				String refId = ref.getId();
				if (refId == null) {
					refId = "";
				}

				if (!refId.startsWith("/item"))
					throw new EntityNotDefinedException(ref.getReference());

				String itemString = refId.substring("/item/".length());
				// string is of form /item/NNN/url. get the number
				int i = itemString.indexOf("/");
				if (i < 0)
					throw new EntityNotDefinedException(ref.getReference());

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
					
					SimplePageItem item = simplePageToolDao.findItem(itemId.longValue());
					SimplePage currentPage = simplePageToolDao.getPage(item.getPageId());
					String owner = currentPage.getOwner();  // if student content
					String currentSiteId = currentPage.getSiteId();
					

					// first let's make sure the user is allowed to access
					// the containing page

					if (!canReadPage(currentSiteId))
					    throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), 
					       ContentHostingService.AUTH_RESOURCE_READ, ref.getReference());
					    
					// If the resource is the actual one in the item, or
					// it is in the containing folder, then do lesson builder checking.
					// otherwise do normal resource checking

					boolean useLb = false;

					// I've seen sakai id's with //, not sure why. they work but will mess up the comparison
					String itemResource = item.getSakaiId().replace("//","/");

					// only use lb security if the user has visited the page
					// this handles the various page release issues, although
					// it's not quite as flexible as the real code. But I don't
					// see any reason to help the user follow URLs that they can't
					// legitimately have seen.

					if (simplePageToolDao.isPageVisited(item.getPageId(), sessionManager.getCurrentSessionUserId(), owner)) {
					    if (id.equals(itemResource))
						useLb = true;
					    else {
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

					    if (owner != null && id.startsWith("/user/" + owner)) {
						// for a student page, if it's in the student's worksite
						// allow it. The assumption is that only the page owner
						// can put content in the page, and he would only put
						// in his own content if he wants it to be visible
					    } else {
						// do normal checking for other content
						if (pushedAdvisor) {
						    securityService.popAdvisor();
						    pushedAdvisor = false;
						}
						// our version of allowget does not check hidden but does everything else
						if (!allowGetResource(id, currentSiteId)) {
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
						simplePageBean.setMemoryService(memoryService);
						simplePageBean.setCurrentSiteId(currentPage.getSiteId());
						simplePageBean.setCurrentPage(currentPage);
						simplePageBean.setCurrentPageId(currentPage.getPageId());

						if (!simplePageBean.isItemAvailable(item, item.getPageId())) {
							throw new EntityPermissionException(null, null, null);
						}
						accessCache.put(accessKey, "true", DEFAULT_EXPIRATION);
						
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
					
					// access checks are OK, get the thing

					// first see if it's not in resources, i.e.
					// if it doesn't start with /access/content it's something odd. redirect to it.
					// probably resources access control won't apply to it
					String url = contentHostingService.getUrl(id);					
					// https://heidelberg.rutgers.edu/access/citation/content/group/24da8519-08c2-4c8c-baeb-8abdfd6c69d7/New%20Citation%20List

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
					// no copyright enforcement, I don't think

					try {
					    // following cast is redundant is current kernels, but is needed for Sakai 2.6.1
						long len = (long)resource.getContentLength();
						String contentType = resource.getContentType();
						
						// 	for url resource type, encode a redirect to the body URL
						if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL)) {
							if (len < MAX_URL_LENGTH) {
								byte[] content = resource.getContent();
								if ((content == null) || (content.length == 0))
									throw new IdUnusedException(ref.getReference());
								
								// 	An invalid URI format will get caught by the
								// 	outermost catch block
								URI uri = new URI(new String(content, "UTF-8"));
								eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ,
										resource.getReference(null), false));
								res.sendRedirect(uri.toASCIIString());
							} else
								// 	we have a text/url mime type, but the body is too
								// 	long to issue as a redirect
								throw new EntityNotDefinedException(ref.getReference());

						} else {
							
							// 	use the last part, the file name part of the id, for
							// 	the download file name
							String fileName = Web.encodeFileName(req, Validator.getFileName(ref.getId()));
							
							String disposition = null;
							
							// 	checks whether type can reasonably be done inline.
							// 	but we bypass
							// 	the usual check whether HTML is allowed to be done
							// 	inline
							if (Validator.letBrowserInline(contentType)) {
								disposition = "inline; filename=\"" + fileName + "\"";
							} else {
								disposition = "attachment; filename=\"" + fileName + "\"";
							}
							
							// NOTE: Only set the encoding on the content we have
							// to.
							// Files uploaded by the user may have been created with
							// different encodings, such as ISO-8859-1;
							// rather than (sometimes wrongly) saying its UTF-8, let
							// the browser auto-detect the encoding.
							// If the content was created through the WYSIWYG
							// editor, the encoding does need to be set (UTF-8).
							String encoding = resource.getProperties().getProperty(ResourceProperties.PROP_CONTENT_ENCODING);
							if (encoding != null && encoding.length() > 0) {
								contentType = contentType + "; charset=" + encoding;
							}

							// not yet
							// ArrayList<Range> ranges = parseRange(req, res, len);

							// if (req.getHeader("Range") == null || (ranges ==
							// null) || (ranges.isEmpty())) {
							if (true) {
								// stream the content using a small buffer to keep
								// memory managed
								InputStream content = null;
								OutputStream out = null;

								try {
									content = resource.streamContent();
									if (content == null)
										throw new IdUnusedException(ref.getReference());

									res.setContentType(contentType);
									res.addHeader("Content-Disposition", disposition);
									// not now res.addHeader("Accept-Ranges",
									// "bytes");
									// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4187336
									if (len <= Integer.MAX_VALUE) {
										res.setContentLength((int) len);
									} else {
										res.addHeader("Content-Length", Long.toString(len));
									}

									// set the buffer of the response to match what
									// we are reading from the request
									if (len < STREAM_BUFFER_SIZE) {
										res.setBufferSize((int) len);
									} else {
										res.setBufferSize(STREAM_BUFFER_SIZE);
									}

									out = res.getOutputStream();
									
									copyRange(content, out, 0, len - 1);
								} catch (ServerOverloadException e) {
									throw e;
								} catch (Exception ignore) {} finally {
									// be a good little program and close the stream
									// - freeing up valuable system resources
									if (content != null) {
										content.close();
									}

									if (out != null) {
										try {
											out.close();
										} catch (Exception ignore) {}
									}
								}

								// Track event - only for full reads
								eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ,
										resource.getReference(null), false));

							}

						}
					} catch (Exception t) {
					    throw new EntityNotDefinedException(ref.getReference());
					    // following won't work in 2.7.1
					    // throw new EntityNotDefinedException(ref.getReference(), t);
					}
					
				}catch(Exception ex) {
					throw new EntityNotDefinedException(ref.getReference());
				}finally {
				    if(pushedAdvisor) securityService.popAdvisor();
				}
			}
		};
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

			// this will check basic access and group access. FOr that normal Sakai
			// checking is fine.
			isAllowed = ref != null && securityService.unlock(lock, ref);

			// availability is for hidden and release date. Do our own, because
			// we implement release date but ignore hidden. That lets faculty hide
			// resources from normal view but still see them through Lessons

			if (isAllowed) {
			    // if site maintainer, don't check release dates. The real check is complex, involving
			    // who owns the page. For our purposes if it's a resource in this site, allow a maintainer
			    // to access it.
			    if (canWritePage(siteId) && id.startsWith("/group/" + siteId))
				return true;

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
	    Time now = TimeService.newTime();
	    Time releaseDate = entity.getReleaseDate();
	    if (releaseDate != null && ! releaseDate.before(now))
		return false;
	    Time retractDate = entity.getRetractDate();
	    if (retractDate != null && ! retractDate.after(now))
		return false;
	    ContentEntity parent = (ContentEntity)entity.getContainingCollection();
	    if (parent != null)
		return isAvailable(parent);
	    else
		return true;
	}

	protected IOException copyRange(InputStream istream, OutputStream ostream, long start, long end) {

		try {
			istream.skip(start);
		} catch (IOException e) {
			return e;
		}

		IOException exception = null;
		long bytesToRead = end - start + 1;

		byte buffer[] = new byte[STREAM_BUFFER_SIZE];
		int len = buffer.length;
		while ((bytesToRead > 0) && (len >= buffer.length)) {
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
			if (len < buffer.length) {
				break;
			}
		}

		return exception;
	}

	public boolean canReadPage(String siteId) {
		String ref = "/site/" + siteId;
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	}

	public boolean canWritePage(String siteId) {
		String ref = "/site/" + siteId;
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
	}


}
