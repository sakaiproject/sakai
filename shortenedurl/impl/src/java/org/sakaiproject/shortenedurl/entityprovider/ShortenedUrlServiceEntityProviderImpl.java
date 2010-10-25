package org.sakaiproject.shortenedurl.entityprovider;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.shortenedurl.api.ShortenedUrlService;

/**
 * Implementation of the EntityProvider for the ShortenedUrlService to allow URL shortening via GET requests.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ShortenedUrlServiceEntityProviderImpl implements ShortenedUrlServiceEntityProvider, CoreEntityProvider, AutoRegisterEntityProvider, RESTful  {

	
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}

	@EntityCustomAction(action="shorten",viewKey=EntityView.VIEW_LIST)
	public Object shorten(OutputStream out, EntityView view, Map<String, Object> params) {
		
		String path = (String)params.get("path");
		if(StringUtils.isBlank(path)){
			throw new EntityException("Invalid path.", path);
		}
		
		boolean secure = Boolean.parseBoolean((String)params.get("secure"));
		
		try {
			String shortenedUrl = shortenedUrlService.shorten(URLDecoder.decode(path, "UTF-8"), secure);
			if(StringUtils.isBlank(shortenedUrl)){
				throw new EntityException("Couldn't shorten URL.", path);
			}
			return shortenedUrl;
		} catch (UnsupportedEncodingException e) {
			throw new EntityException("Unable to decode path.", path);
		}
	
	}
	
	
	private ShortenedUrlService shortenedUrlService;
	public void setShortenedUrlService(ShortenedUrlService shortenedUrlService) {
		this.shortenedUrlService = shortenedUrlService;
	}
	
	
	public boolean entityExists(String eid) {
		return true;
	}

	public Object getSampleEntity() {
		return null;
	}
	
	public Object getEntity(EntityReference ref) {
		return null;
	}
	
	public String[] getHandledOutputFormats() {
		return new String[] {};
	}

	public String[] getHandledInputFormats() {
		return new String[] {};
	}
	
	public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
		return null;
	}

	public void updateEntity(EntityReference ref, Object entity,Map<String, Object> params) {
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	}

	public List<?> getEntities(EntityReference ref, Search search) {
		return null;
	}
}
