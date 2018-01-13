/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2014 Sakai Foundation
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
package org.sakaiproject.content.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingHandler;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.time.api.Time;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Just wrap up an existing ContentResource, this allows subclasses to easily override methods
 * where needed.
 * @see org.sakaiproject.content.api.ContentFilter
 * @author Matthew Buckett
 *
 */
public class AbstractContentResource implements ContentResource{
	
	// Use package visibility to allow things in impl to get the raw resource
	ContentResource wrapped;
	
	public AbstractContentResource(ContentResource wrapped) {
		this.wrapped = wrapped;
	}

	public ContentCollection getContainingCollection() {
		return wrapped.getContainingCollection();
	}

	public boolean isResource() {
		return wrapped.isResource();
	}

	public boolean isCollection() {
		return wrapped.isCollection();
	}

	public String getResourceType() {
		return wrapped.getResourceType();
	}

	public ContentHostingHandler getContentHandler() {
		return wrapped.getContentHandler();
	}

	public void setContentHandler(ContentHostingHandler chh) {
		setContentHandler(chh);
	}

	public ContentEntity getVirtualContentEntity() {
		return wrapped.getVirtualContentEntity();
	}

	public void setVirtualContentEntity(ContentEntity ce) {
		setVirtualContentEntity(ce);
	}

	public ContentEntity getMember(String nextId) {
		return wrapped.getMember(nextId);
	}

	public String getUrl(boolean relative) {
		return wrapped.getUrl(relative);
	}

	public Collection getGroups() {
		return wrapped.getGroups(); 
	}

	public Collection getGroupObjects() {
		return wrapped.getGroupObjects();
	}

	public AccessMode getAccess() {
		return wrapped.getAccess();
	}

	public Collection getInheritedGroups() {
		return wrapped.getInheritedGroups();
	}

	public Set<String> getRoleAccessIds() {
		return wrapped.getRoleAccessIds();
	}

	public Set<String> getInheritedRoleAccessIds() {
		return wrapped.getInheritedRoleAccessIds();
	}

	public Collection getInheritedGroupObjects() {
		return wrapped.getInheritedGroupObjects();
	}

	public AccessMode getInheritedAccess() {
		return wrapped.getInheritedAccess();
	}

	public Time getReleaseDate() {
		return wrapped.getReleaseDate();
	}

	public Time getRetractDate() {
		return wrapped.getRetractDate();
	}

	public boolean isHidden() {
		return wrapped.isHidden();
	}

	public boolean isAvailable() {
		return wrapped.isAvailable();
	}

	public String getUrl() {
		return wrapped.getUrl();
	}

	public String getReference() {
		return wrapped.getReference();
	}

	public String getUrl(String rootProperty) {
		return wrapped.getUrl(rootProperty);
	}

	public String getReference(String rootProperty) {
		return wrapped.getReference(rootProperty);
	}

	public String getId() {
		return wrapped.getId();
	}

	public ResourceProperties getProperties() {
		return wrapped.getProperties();
	}

	public Element toXml(Document doc, Stack stack) {
		return wrapped.toXml(doc, stack);
	}

	public long getContentLength() {
		return wrapped.getContentLength();
	}

	public String getContentType() {
		return wrapped.getContentType();
	}

	public byte[] getContent() throws ServerOverloadException {
		return wrapped.getContent();
	}

	public InputStream streamContent() throws ServerOverloadException {
		return wrapped.streamContent();
	}

	@Override
	public Instant getReleaseInstant() {
		return wrapped.getReleaseInstant();
	}

	@Override
	public Instant getRetractInstant() {
		return wrapped.getRetractInstant();
	}

}
