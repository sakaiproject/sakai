/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.impl.BaseContentService;
import org.sakaiproject.content.impl.BaseContentService.BaseCollectionEdit;
import org.w3c.dom.Element;

/**
 * @author ieb
 *
 */
public class BaseJCRCollectionEdit extends BaseCollectionEdit
{

	private Node node;
	private static final Log log = LogFactory.getLog(BaseJCRCollectionEdit.class);
	/**
	 * @param baseContentService
	 * @param id
	 */
	public BaseJCRCollectionEdit(BaseContentService baseContentService, String id)
	{
		baseContentService.super(id);
		 m_active = true;
	}

	
	/**
	 * @param baseContentService
	 * @param el
	 */
	public BaseJCRCollectionEdit(BaseContentService baseContentService, Element el)
	{
		baseContentService.super(el);
		 m_active = true;
	}
	/**
	 * @param baseContentService
	 * @param other
	 */
	public BaseJCRCollectionEdit(BaseContentService baseContentService, ContentCollection other)
	{
		baseContentService.super(other);
		 m_active = true;
	}


	/**
	 * @return the node
	 */
	public Node getNode()
	{
		return node;
	}


	/**
	 * @param node the node to set
	 */
	public void setNode(Node node)
	{
		if ( log.isDebugEnabled() )
			log.debug("Setting Node on "+this+" to "+node);
		this.node = node;
	}

}
