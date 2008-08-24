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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.jcr.api.JCRConstants;
import org.w3c.dom.Element;

/**
 * @author ieb
 */
public class BaseJCRResourceEdit extends BaseResourceEdit
{


	private static final Log log = LogFactory.getLog(BaseJCRResourceEdit.class);

	private Node node;

	/**
	 * @param baseContentService
	 * @param id
	 */
	public BaseJCRResourceEdit(BaseContentService baseContentService, String id)
	{
		baseContentService.super(id);
		 m_active = true;

	}

	/**
	 * @param baseContentService
	 * @param el
	 */

	public BaseJCRResourceEdit(BaseContentService baseContentService, Element el)
	{
		baseContentService.super(el);
		 m_active = true;
	}

	/**
	 * @param baseContentService
	 * @param other
	 */
	public BaseJCRResourceEdit(BaseContentService baseContentService,
			ContentResource other)
	{
		baseContentService.super(other);
		 m_active = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit#getContent()
	 */
	@Override
	public byte[] getContent() throws ServerOverloadException
	{
		try
		{
			Node c = node.getNode(JCRConstants.JCR_CONTENT);
			Property p = c.getProperty(JCRConstants.JCR_DATA);
			long length = p.getLength();
			if (length > 4096)
			{
				log
						.error(
								"Full content is being requested into memory this is bad! ... but we will do it anyway, memory used =  "
										+ length, new Exception("Traceback"));
			}
			byte[] buffer = new byte[(int) length];
			InputStream in = p.getStream();
			in.read(buffer, 0, (int) length);
			in.close();
			return buffer;
		}
		catch (RepositoryException e)
		{
			log.error("Failed to get content stream ", e);
			throw new ServerOverloadException("Failed to get content Stream from JCR: "
					+ e.getMessage());
		}
		catch (IOException e)
		{
			log.error("Failed to get content stream ", e);
			throw new ServerOverloadException("Failed to get content Stream from JCR "
					+ e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit#set(org.sakaiproject.content.api.ContentResource)
	 */
	@Override
	protected void set(ContentResource other)
	{
		super.set(other);
		if ( other instanceof BaseJCRResourceEdit ) {
			BaseJCRResourceEdit bother = (BaseJCRResourceEdit) other;
			node = bother.getNode();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit#setContent(byte[])
	 */
	@Override
	public void setContent(byte[] content)
	{
		try
		{
			if (content.length > 4096)
			{
				log
						.error(
								"Content is being stored in memory, this is bad! ... but we will do it anyway, memory used =  "
										+ content.length);
				log.debug("Traceback ", new Exception("Traceback"));
			}
			Node c = node.getNode(JCRConstants.JCR_CONTENT);
			Property p = c.getProperty(JCRConstants.JCR_DATA);
			p.setValue(new ByteArrayInputStream(content));
		}
		catch (RepositoryException e)
		{
			log.error("Failed to get content stream ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit#setContent(java.io.InputStream)
	 */
	@Override
	public void setContent(InputStream stream)
	{
		try
		{
			Node content = node.getNode(JCRConstants.JCR_CONTENT);
			Property p = content.getProperty(JCRConstants.JCR_DATA);
			p.setValue(stream);
		}
		catch (RepositoryException e)
		{
			log.error("Failed to get content stream ", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.content.impl.BaseContentService.BaseResourceEdit#streamContent()
	 */
	@Override
	public InputStream streamContent() throws ServerOverloadException
	{
		try
		{
			Node content = node.getNode(JCRConstants.JCR_CONTENT);
			Property p = content.getProperty(JCRConstants.JCR_DATA);
			return p.getStream();
		}
		catch (RepositoryException e)
		{
			log.error("Failed to get content stream ", e);
		}
		return null;
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
		this.node = node;
	}

}
