package org.sakaiproject.entity.api;

import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface DOMEntitySerializer {
	/**
	 * Serialize the entity into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "entity" element.
	 * @return The newly added element.
	 */
	Element toXml(Entity entity, Document doc, Stack stack);
	
	/**
	 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
	 * 
	 * @param doc
	 *        The DOM doc to contain the XML (or null for a string return).
	 * @param stack
	 *        The DOM elements, the top of which is the containing element of the new "resource" element.
	 * @return The newly added element.
	 */
	Element toXml(ResourceProperties properties, Document doc, Stack stack);


}
