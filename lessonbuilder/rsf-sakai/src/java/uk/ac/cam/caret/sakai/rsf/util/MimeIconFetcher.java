package uk.ac.cam.caret.sakai.rsf.util;

import org.sakaiproject.content.api.ContentTypeImageService;

/* This small utility class is responsible fetching URLs
 * for a MIME type and returning them as a String.
 * 
 * This should eventually be included/fixed somewhere in Sakai, but
 * this is being created for now to avoid duplication among 
 * multiple RSF Applications.
 */
public class MimeIconFetcher {
  private ContentTypeImageService contentTypeImageService;
  public void setContentTypeImageService(ContentTypeImageService service) {
    contentTypeImageService = service;
  }
  
  public String getIconURL(String mimetype) {
    return "/library/image/" + contentTypeImageService.getContentTypeImage(mimetype);
  }
}
