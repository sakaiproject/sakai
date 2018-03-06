/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
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
 */
package org.sakaiproject.content.types;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ResourceTypeRegistry;

@Slf4j
public class ContentTypeRegistryBean {
	
	private boolean useContentTypeRegistry;
	private ResourceTypeRegistry resourceTypeRegistry;
	
	
	public void setUseContentTypeRegistry(boolean useContentTypeRegistry) {
		this.useContentTypeRegistry = useContentTypeRegistry;
	}

	public void setResourceTypeRegistry(ResourceTypeRegistry resourceTypeRegistry) {
		this.resourceTypeRegistry = resourceTypeRegistry;
	}

	public void init() {
		log.info("init()");
		if(usingResourceTypeRegistry())
		{
			resourceTypeRegistry.register(new FileUploadType());
			resourceTypeRegistry.register(new FolderType());
			resourceTypeRegistry.register(new TextDocumentType());
			resourceTypeRegistry.register(new HtmlDocumentType());
			resourceTypeRegistry.register(new UrlResourceType());
		}
	}
	
	private boolean usingResourceTypeRegistry() {
		return useContentTypeRegistry;
	}
}
