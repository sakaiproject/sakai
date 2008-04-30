package org.sakaiproject.entity.api;

import org.xml.sax.ContentHandler;

public interface SAXEntitySerializer {

	void toXml(Entity entity, ContentHandler handler);

	void toXml(ResourceProperties entity, ContentHandler handler);

	/**
	 * Get a ContentHandler to handle SAX parsing of properties
	 * @return
	 */
	ContentHandler getContentHander(Entity entity);
	/**
	 * Get a ContentHandler to handle SAX parsing of properties
	 * @return
	 */
	ContentHandler getContentHander(ResourceProperties properties);

}
