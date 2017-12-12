/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.model.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Stack;

import lombok.extern.slf4j.Slf4j;
import org.apache.xerces.impl.dv.util.Base64;
import org.w3c.dom.CDATASection;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.util.BaseResourceProperties;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiEntity;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;
import uk.ac.cam.caret.sakai.rwiki.utils.SchemaNames;

@Slf4j
public class RWikiEntityImpl implements RWikiEntity
{

	public static final String RW_ID = "id";

	// I dont think that content is a propertyrp.addProperty("content",
	// this.getContent());

	private RWikiObject rwo = null;

	private Reference reference = null;

	public RWikiEntityImpl(RWikiObject rwo)
	{
		this.rwo = rwo;
	}

	public RWikiEntityImpl(Reference ref)
	{
		this.reference = ref;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getProperties()
	{
		if (rwo == null)
		{
			ResourceProperties rp = new BaseResourceProperties();
			rp.addProperty(RWikiEntity.RP_REALM, reference.getContext()
					+ reference.getContainer());
			rp.addProperty(RWikiEntity.RP_REALM, reference.getId());
			rp.addProperty(RWikiEntity.RP_CONTAINER, "true");
			return rp;
		}
		else
		{
			ResourceProperties rp = new BaseResourceProperties();
			rp.addProperty("id", this.getId());
			// I dont think that content is a propertyrp.addProperty("content",
			// this.getContent());
			rp.addProperty(RWikiEntity.RP_NAME, rwo.getName());
			rp.addProperty(RWikiEntity.RP_OWNER, rwo.getOwner());
			rp.addProperty(RWikiEntity.RP_REALM, rwo.getRealm());
			rp.addProperty(RWikiEntity.RP_REFERENCED, rwo.getReferenced());
			rp.addProperty(RWikiEntity.RP_RWID, rwo.getRwikiobjectid());
			rp.addProperty(RWikiEntity.RP_SHA1, rwo.getSha1());
			rp.addProperty(RWikiEntity.RP_USER, rwo.getUser());
			rp.addProperty(RWikiEntity.RP_GROUP_ADMIN, String.valueOf(rwo
					.getGroupAdmin()));
			rp.addProperty(RWikiEntity.RP_GROUP_READ, String.valueOf(rwo
					.getGroupRead()));
			rp.addProperty(RWikiEntity.RP_GROUP_WRITE, String.valueOf(rwo
					.getGroupWrite()));
			rp.addProperty(RWikiEntity.RP_OWNER_ADMIN, String.valueOf(rwo
					.getOwnerAdmin()));
			rp.addProperty(RWikiEntity.RP_OWNER_READ, String.valueOf(rwo
					.getOwnerRead()));
			rp.addProperty(RWikiEntity.RP_OWNER_WRITE, String.valueOf(rwo
					.getOwnerWrite()));
			rp.addProperty(RWikiEntity.RP_PUBLIC_READ, String.valueOf(rwo
					.getPublicRead()));
			rp.addProperty(RWikiEntity.RP_PUBLIC_WRITE, String.valueOf(rwo
					.getPublicWrite()));
			rp.addProperty(RWikiEntity.RP_REVISION, String.valueOf(rwo
					.getRevision()));
			rp.addProperty(RWikiEntity.RP_VERSION, String.valueOf(rwo
					.getVersion().getTime()));
			rp.addProperty(RWikiEntity.RP_CONTAINER, "false");

			return rp;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String getReference()
	{
		if (rwo == null)
			return RWikiObjectService.REFERENCE_ROOT + reference.getId() + ".";
		return RWikiObjectService.REFERENCE_ROOT + rwo.getName() + ".";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUrl()
	{
		if (rwo == null)
		{
			return RWikiObjectService.REFERENCE_ROOT + encode(reference.getId())
					+ ".";
		} 
		else {
			// /wiki access url 
			return RWikiObjectService.REFERENCE_ROOT + encode(rwo.getName()) + ".";
		}
	}

	private String encode(String toEncode) {
		try
		{		
			String encoded = URLEncoder.encode(toEncode, "UTF-8");
			encoded = encoded.replaceAll("\\+", "%20").replaceAll("%2F", "/");
			
			return encoded; 

		}
		catch (UnsupportedEncodingException e)
		{
			throw new IllegalStateException("UTF-8 Encoding is not supported when encoding: "  + toEncode + ": " + e.getMessage());
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Element toXml(Document doc, Stack stack)
	{
		if (rwo == null)
			throw new RuntimeException(
					" Cant serialise containers at the moment ");
		Element wikipage = doc.createElement(SchemaNames.EL_WIKIPAGE);

		if (stack.isEmpty())
		{
			doc.appendChild(wikipage);
		}
		else
		{
			((Element) stack.peek()).appendChild(wikipage);
		}

		stack.push(wikipage);

		wikipage.setAttribute(SchemaNames.ATTR_ID, rwo.getId());
		wikipage.setAttribute(SchemaNames.ATTR_PAGE_NAME, rwo.getName());
		wikipage.setAttribute(SchemaNames.ATTR_REVISION, String.valueOf(rwo
				.getRevision()));
		wikipage.setAttribute(SchemaNames.ATTR_USER, rwo.getUser());
		wikipage.setAttribute(SchemaNames.ATTR_OWNER, rwo.getOwner());

		// I would like to be able to render this, but we cant... because its a
		// pojo !
		getProperties().toXml(doc, stack);
		Element content = doc.createElement(SchemaNames.EL_WIKICONTENT);
		stack.push(content);
		wikipage.appendChild(content);
		content.setAttribute("enc", "BASE64");
		try
		{
			String b64Content = Base64.encode(rwo.getContent()
					.getBytes("UTF-8"));
			CDATASection t = doc.createCDATASection(b64Content);
			stack.push(t);
			content.appendChild(t);
			stack.pop();
		}
		catch (UnsupportedEncodingException usex)
		{
			// if UTF-8 isnt available, we are in big trouble !
			throw new IllegalStateException("Cannot find Encoding UTF-8");
		}
		stack.pop();

		stack.pop();

		return wikipage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void fromXml(Element el, String defaultRealm) throws Exception
	{
		if (rwo == null)
			throw new RuntimeException(
					" Cant deserialise containers at the moment ");

		NodeList nl = el.getElementsByTagName("properties");
		// this is hard coded in BaseResourceProperties the element name
		// properties
		if (nl == null || nl.getLength() != 1)
			throw new Exception("Cant find a properties element in "
					+ el.getNodeName() + " id: "
					+ el.getAttribute(SchemaNames.ATTR_ID) + " pagename: "
					+ el.getAttribute(SchemaNames.ATTR_PAGE_NAME));
		// only take the first properties
		Element properties = (Element) nl.item(0);

		ResourceProperties rp = new BaseResourceProperties(properties);

		nl = el.getElementsByTagName(SchemaNames.EL_WIKICONTENT);
		if (nl == null || nl.getLength() != 1)
			throw new Exception("Cant find a  wikiproperties element in "
					+ el.getNodeName() + " id: "
					+ el.getAttribute(SchemaNames.ATTR_ID) + " pagename: "
					+ el.getAttribute(SchemaNames.ATTR_PAGE_NAME));
		// only accpet the first
		Element wikiContents = (Element) nl.item(0);

		nl = wikiContents.getChildNodes();
		StringBuffer content = new StringBuffer();
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node n = nl.item(i);
			if (n instanceof CharacterData)
			{
				CharacterData cdnode = (CharacterData) n;
				try
				{
					content.append(new String(Base64.decode(cdnode.getData()),
							"UTF-8"));
				}
				catch (Throwable t)
				{
					log.warn("Cant decode node content for " + cdnode);
				}
			}
		}

		String realm = rp.getProperty(RWikiEntity.RP_REALM);
		rwo.setId(rp.getProperty(RWikiEntity.RP_ID));

		rwo.setName(NameHelper.globaliseName(NameHelper.localizeName(rp
				.getProperty(RWikiEntity.RP_NAME), realm), defaultRealm));
		rwo.setOwner(rp.getProperty(RWikiEntity.RP_OWNER));
		rwo.setRealm(defaultRealm);
		rwo.setReferenced(rp.getProperty(RWikiEntity.RP_REFERENCED));
		// rwo.setRwikiobjectid(rp.getProperty("rwid"));
		rwo.setContent(content.toString());

		if (!rwo.getSha1().equals(rp.getProperty(RWikiEntity.RP_SHA1)))
			throw new Exception("Sha Checksum Missmatch on content "
					+ rp.getProperty(RWikiEntity.RP_SHA1) + " != "
					+ rwo.getSha1());
		rwo.setUser(rp.getProperty(RWikiEntity.RP_USER));
		rwo.setGroupAdmin(rp.getBooleanProperty(RWikiEntity.RP_GROUP_ADMIN));
		rwo.setGroupRead(rp.getBooleanProperty(RWikiEntity.RP_GROUP_READ));
		rwo.setGroupWrite(rp.getBooleanProperty(RWikiEntity.RP_GROUP_WRITE));
		rwo.setOwnerAdmin(rp.getBooleanProperty(RWikiEntity.RP_OWNER_ADMIN));
		rwo.setOwnerRead(rp.getBooleanProperty(RWikiEntity.RP_OWNER_READ));
		rwo.setOwnerWrite(rp.getBooleanProperty(RWikiEntity.RP_OWNER_WRITE));
		rwo.setPublicRead(rp.getBooleanProperty(RWikiEntity.RP_PUBLIC_READ));
		rwo.setPublicWrite(rp.getBooleanProperty(RWikiEntity.RP_PUBLIC_WRITE));
		rwo.setRevision(Integer.valueOf(rp.getProperty(RWikiEntity.RP_REVISION)));
		rwo.setVersion(new Date(rp.getLongProperty(RWikiEntity.RP_VERSION)));

	}

	/**
	 * @inheritDoc
	 */
	public String getReference(String rootProperty)
	{
		return getReference();
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl(String rootProperty)
	{
		return getUrl();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		if (rwo == null) return reference.getId();
		return rwo.getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public RWikiObject getRWikiObject()
	{
		if (rwo == null)
			throw new RuntimeException(
					"RWiki Containers dont have objects attached ");
		return rwo;
	}

	public boolean isContainer()
	{
		return (rwo == null);
	}

}
