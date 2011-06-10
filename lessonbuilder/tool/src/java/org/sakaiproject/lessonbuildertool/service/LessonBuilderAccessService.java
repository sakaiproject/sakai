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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

import java.net.URI;

import org.apache.commons.logging.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;

import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.SessionManager;

import org.sakaiproject.util.Validator;
import org.sakaiproject.util.Web;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.LessonBuilderAccessAPI;

import uk.org.ponder.messageutil.MessageLocator;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.lessonbuildertool.service.ForumEntity;
import org.sakaiproject.lessonbuildertool.service.SamigoEntity;
import org.sakaiproject.lessonbuildertool.service.AssignmentEntity;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.tool.api.ToolManager;

/**
 * <p>
 * LessonBuilderAccessService implements /access/lessonbuilder
 * </p>
 */
public class LessonBuilderAccessService
{
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
	forumEntity = (LessonEntity)e;
    }

    LessonEntity quizEntity = null;
    public void setQuizEntity(Object e) {
	quizEntity = (LessonEntity)e;
    }

    LessonEntity assignmentEntity = null;
    public void setAssignmentEntity(Object e) {
	assignmentEntity = (LessonEntity)e;
    }

    private GradebookIfc gradebookIfc = null;

    public void setGradebookIfc(GradebookIfc g) {
	gradebookIfc = g;
    }

    protected static final long MAX_URL_LENGTH = 8192;
    protected static final int STREAM_BUFFER_SIZE = 102400;


    public void init() {
	lessonBuilderAccessAPI.setHttpAccess(getHttpAccess());
    }

    // references are currently of the form /access/lessonbuilder/item/NNN
    // however the Reference has an id of /item/NNN

    /**
     * {@inheritDoc}
     */
    public HttpAccess getHttpAccess() {
	return new HttpAccess() {

	    public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
				     Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException,
									      EntityAccessOverloadException, EntityCopyrightException {

		// if the id is null, the request was for just ".../content"
		String refId = ref.getId();
		if (refId == null) refId = "";
		
		if (!refId.startsWith("/item"))
		    throw new EntityNotDefinedException(ref.getReference());
		
		String itemString = refId.substring("/item/".length());
		int i = itemString.lastIndexOf(".");
		if (i >= 0)
		    itemString = itemString.substring(0, i);
		Long itemId = 0L;
		try {
		    itemId = (Long)Long.parseLong(itemString);
		} catch (Exception e) {
		    throw new EntityNotDefinedException(ref.getReference());
		}

		SimplePageItem item = simplePageToolDao.findItem(itemId.longValue());
		if (item == null || (item.getType() != SimplePageItem.RESOURCE && item.getType() != SimplePageItem.MULTIMEDIA))
		    throw new EntityNotDefinedException(ref.getReference());

		String id = item.getSakaiId();

		if (!allowGetResource(id))
		    throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), ContentHostingService.AUTH_RESOURCE_READ, ref.getReference());
		
		if (item.isPrerequisite()) {
		    // computing requirements is so messy that it's worth instantiating
		    // a SimplePageBean to do it. Otherwise we have to duplicate lots of
		    // code that changes. And we want it to be a transient bean becase there are
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
		    simplePageBean.setGradebookIfc(gradebookIfc);

		    if (!simplePageBean.isItemAvailable(item, item.getPageId())) {
			throw new EntityPermissionException(null, null, null);
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

		try
		{
		    long len = resource.getContentLength();
		    String contentType = resource.getContentType();

		    // for url resource type, encode a redirect to the body URL
		    if (contentType.equalsIgnoreCase(ResourceProperties.TYPE_URL)) {
			if (len < MAX_URL_LENGTH) {
			    
			    byte[] content = resource.getContent();
			    if ((content == null) || (content.length == 0)) {
				throw new IdUnusedException(ref.getReference());
			    }
	
			    // An invalid URI format will get caught by the outermost catch block 
			    URI uri = new URI(new String(content, "UTF-8"));
			    eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ, resource.getReference(null), false));
			    res.sendRedirect(uri.toASCIIString());
			} else {					
			    // we have a text/url mime type, but the body is too long to issue as a redirect
			    throw new EntityNotDefinedException(ref.getReference());
			}

		    } else {
			// use the last part, the file name part of the id, for the download file name
			String fileName = Web.encodeFileName( req, Validator.getFileName(ref.getId()) );
			
			String disposition = null;

			// checks whether type can reasonably be done inline. but we bypass
			// the usual check whether HTML is allowed to be done inline
			if (Validator.letBrowserInline(contentType)) {
			    disposition = "inline; filename=\"" + fileName + "\"";
			} else {
			    disposition = "attachment; filename=\"" + fileName + "\"";
			}
				
			// NOTE: Only set the encoding on the content we have to.
			// Files uploaded by the user may have been created with different encodings, such as ISO-8859-1;
			// rather than (sometimes wrongly) saying its UTF-8, let the browser auto-detect the encoding.
			// If the content was created through the WYSIWYG editor, the encoding does need to be set (UTF-8).
			String encoding = resource.getProperties().getProperty(ResourceProperties.PROP_CONTENT_ENCODING);
			if (encoding != null && encoding.length() > 0) {
			    contentType = contentType + "; charset=" + encoding;
			}

			// not yet
		        // ArrayList<Range> ranges = parseRange(req, res, len);

			//  if (req.getHeader("Range") == null || (ranges == null) || (ranges.isEmpty())) {
			if (true) {
			    // stream the content using a small buffer to keep memory managed
			    InputStream content = null;
			    OutputStream out = null;
	
			    try {
				content = resource.streamContent();
				if (content == null) {
				    throw new IdUnusedException(ref.getReference());
				}
	
				res.setContentType(contentType);
				res.addHeader("Content-Disposition", disposition);
				// not now		res.addHeader("Accept-Ranges", "bytes");
				// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4187336
				if (len <= Integer.MAX_VALUE){
				    res.setContentLength((int)len);
				} else {
				    res.addHeader("Content-Length", Long.toString(len));
				}

				// set the buffer of the response to match what we are reading from the request
				if (len < STREAM_BUFFER_SIZE) {
				    res.setBufferSize((int)len);
				} else {
				    res.setBufferSize(STREAM_BUFFER_SIZE);
				}
	
				out = res.getOutputStream();
	
				copyRange(content, out, 0, len-1);
			    } catch (ServerOverloadException e) {
				throw e;
			    }catch (Exception ignore) {
			    } finally {
				// be a good little program and close the stream - freeing up valuable system resources
				if (content != null)
				    content.close();
	
				if (out != null) {
				    try {
					out.close();
				    } catch (Exception ignore) {}
				}
			    }
					
			    // Track event - only for full reads
			    eventTrackingService.post(eventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ, resource.getReference(null), false));

			}

		    }
		} catch (Exception t) {
		    throw new EntityNotDefinedException(ref.getReference(), t);
		}
	    }
	};
    }

    // simplified versions of stuff from BaseContentService

    public boolean allowGetResource(String id) {
	return unlockCheck(ContentHostingService.AUTH_RESOURCE_READ, id);
    }

    public String getReference(String id) {
	return "/content" + id;  // apparently
    }

    protected boolean unlockCheck(String lock, String id)  {
	boolean isAllowed = securityService.isSuperUser();
	if(! isAllowed) {
	    // make a reference from the resource id, if specified
	    String ref = null;
	    if (id != null)
		ref = getReference(id);

	    isAllowed = ref != null && securityService.unlock(lock, ref);
	    // no checks of hidden or availabitility, since Lesson Builder does
	    // its own checks, and faculty may want to hide the area from normal
	    // access
	}
	
	return isAllowed;
    }

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

}
