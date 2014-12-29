package org.sakaiproject.content.types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ResourceTypeRegistry;

public class ContentTypeRegistryBean {

	private static Log log = LogFactory.getLog(ContentTypeRegistryBean.class);
	
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
