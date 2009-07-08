/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/admin-tools/su/src/java/org/sakaiproject/tool/su/SuTool.java $
 * $Id: SuTool.java 5970 2006-02-15 03:07:19Z ggolden@umich.edu $
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

package org.sakaiproject.site.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseSitePage is an implementation of the Site API SitePage.
 * </p>
 */
public class BaseSitePage implements SitePage, Identifiable
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BaseSitePage.class);

	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The title. */
	protected String m_title = null;

	/** The layout. */
	protected int m_layout = LAYOUT_SINGLE_COL;

	/** The popup setting. */
	protected boolean m_popup = false;

	/** The site id. */
	protected String m_id = null;

	/** The properties. */
	protected ResourcePropertiesEdit m_properties = null;

	/** the list of tool configurations for this SitePage */
	protected ResourceVector m_tools = null;

	/** false while the page's tools have not yet been read in. */
	protected boolean m_toolsLazy = false;

	/** Active flag. */
	protected boolean m_active = false;

	/** The site I belong to. */
	protected Site m_site = null;

	/** The site id I belong to, in case I have no m_site. */
	protected String m_siteId = null;

	/** The site skin, in case I have no m_site. */
	protected String m_skin = null;
   
	private BaseSiteService siteService;

	/**
	 * Construct. Auto-generate the id.
	 * 
	 * @param site
	 *        The site in which this page lives.
	 */
	protected BaseSitePage(BaseSiteService siteService, Site site)
	{
		this.siteService = siteService;
		m_site = site;
		m_id = IdManager.createUuid();
		m_properties = new BaseResourcePropertiesEdit();
		m_tools = new ResourceVector();
	}

	/**
	 * ReConstruct
	 * 
	 * @param site
	 *        The site in which this page lives.
	 * @param id
	 *        The page id.
	 * @param title
	 *        The page title.
	 * @param layout
	 *        The layout as a string ("0" or not currently supported).
	 * @param popup
	 *        The page popup setting.
	 */
	protected BaseSitePage(BaseSiteService siteService, Site site, String id, String title, String layout,
			boolean popup)
	{
		this.siteService = siteService;
		m_site = site;
		m_id = id;

		m_properties = new BaseResourcePropertiesEdit();
		((BaseResourcePropertiesEdit) m_properties).setLazy(true);

		m_tools = new ResourceVector();
		m_toolsLazy = true;

		m_title = title;

		if (layout.equals(String.valueOf(LAYOUT_SINGLE_COL)))
		{
			m_layout = LAYOUT_SINGLE_COL;
		}
		else if (layout.equals(String.valueOf(LAYOUT_DOUBLE_COL)))
		{
			m_layout = LAYOUT_DOUBLE_COL;
		}

		m_popup = popup;
	}

	/**
	 * ReConstruct - if we don't have a site to follow up to get to certain site
	 * info.
	 * 
	 * @param pageId
	 *        The page id.
	 * @param title
	 *        The page title.
	 * @param layout
	 *        The layout as a string ("0" or not currently supported).
	 * @param popup
	 *        The page popup setting.
	 * @param siteId
	 *        The page's site's id.
	 * @param skin
	 *        The page's site's skin.
	 */
	protected BaseSitePage(BaseSiteService siteService, String pageId, String title, String layout, boolean popup,
			String siteId, String skin)
	{
		this.siteService = siteService;

		m_site = null;
		m_id = pageId;
		m_popup = popup;

		m_properties = new BaseResourcePropertiesEdit();
		((BaseResourcePropertiesEdit) m_properties).setLazy(true);

		m_tools = new ResourceVector();
		m_toolsLazy = true;

		m_title = title;

		if (layout.equals(String.valueOf(LAYOUT_SINGLE_COL)))
		{
			m_layout = LAYOUT_SINGLE_COL;
		}
		else if (layout.equals(String.valueOf(LAYOUT_DOUBLE_COL)))
		{
			m_layout = LAYOUT_DOUBLE_COL;
		}

		m_popup = popup;

		m_siteId = siteId;
		m_skin = skin;
	}

	/**
	 * Construct as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 * @param site
	 *        The site in which this page lives.
	 * @param exact
	 *        If true, we copy ids - else we generate new ones for page and
	 *        tools.
	 */
	protected BaseSitePage(BaseSiteService siteService, SitePage other, Site site, boolean exact)
	{
		this.siteService = siteService;

		BaseSitePage bOther = (BaseSitePage) other;

		m_site = site;

		if (exact)
		{
			m_id = bOther.m_id;
		}
		else
		{
			m_id = IdManager.createUuid();
		}
		m_title = bOther.m_title;
		m_layout = bOther.m_layout;
		m_popup = bOther.m_popup;

		m_properties = new BaseResourcePropertiesEdit();
		ResourceProperties pOther = other.getProperties();
		// exact copying of SitePage properties vs replacing occurence of site
		// id within, depending on "exact" setting --- zqian
		if (exact)
		{
			m_properties.addAll(pOther);
		}
		else
		{
			Iterator l = pOther.getPropertyNames();
			while (l.hasNext())
			{
				String pOtherName = (String) l.next();
				m_properties.addProperty(pOtherName, pOther.getProperty(pOtherName)
						.replaceAll(bOther.getSiteId(), getSiteId()));
			}
		}

		((BaseResourcePropertiesEdit) m_properties)
				.setLazy(((BaseResourceProperties) other.getProperties()).isLazy());

		// deep copy the tools
		m_tools = new ResourceVector();
		for (Iterator iTools = bOther.getTools().iterator(); iTools.hasNext();)
		{
			BaseToolConfiguration tool = (BaseToolConfiguration) iTools.next();
			m_tools.add(new BaseToolConfiguration(siteService,tool, this, exact));
		}
		m_toolsLazy = ((BaseSitePage) other).m_toolsLazy;

		m_siteId = bOther.m_siteId;
		m_skin = bOther.m_skin;
	}

	/**
	 * Construct from XML element.
	 * 
	 * @param el
	 *        The XML element.
	 * @param site
	 *        The site in which this page lives.
	 */
	protected BaseSitePage(BaseSiteService siteService, Element el, Site site)
	{
		this.siteService = siteService;

		m_site = site;

		// setup for properties
		m_properties = new BaseResourcePropertiesEdit();

		// setup for page list
		m_tools = new ResourceVector();

		m_id = el.getAttribute("id");
		m_title = StringUtil.trimToNull(el.getAttribute("title"));
		try
		{
			m_layout = Integer.parseInt(StringUtil.trimToNull(el.getAttribute("layout")));
		}
		catch (Exception e)
		{
		}

		try
		{
			m_popup = Boolean.valueOf(el.getAttribute("popup")).booleanValue();
		}
		catch (Exception e)
		{
		}

		// the children (properties and page list)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for properties
			if (element.getTagName().equals("properties"))
			{
				// re-create properties
				m_properties = new BaseResourcePropertiesEdit(element);
			}

			// look for the tool list
			else if (element.getTagName().equals("tools"))
			{
				NodeList toolsNodes = element.getChildNodes();
				for (int t = 0; t < toolsNodes.getLength(); t++)
				{
					Node toolNode = toolsNodes.item(t);
					if (toolNode.getNodeType() != Node.ELEMENT_NODE) continue;
					Element toolEl = (Element) toolNode;
					if (!toolEl.getTagName().equals("tool")) continue;

					BaseToolConfiguration tool = new BaseToolConfiguration(siteService, toolEl, this);
					m_tools.add(tool);
				}
			}
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public String getTitle()
	{
		// check for special home page tool id
		if (	getProperties().get(IS_HOME_PAGE) != null )
		{
			 String title = ActiveToolManager.getLocalizedToolProperty(HOME_TOOL_ID, "title");
			 if ( title != null )
				 return title;
			 else
				 return m_title;
		}
			
		// if	 more than one tool on this page, just return the default page title
		else if ( getTools().size() != 1 )
		{
			return m_title;
		}
			
		// Get the toolId of the first tool associated with this page
		String toolId = ((BaseToolConfiguration) (getTools().get(0))).getToolId();
		
		// IFrame and News toolIds are considered 'custom' (page title easily configurable)
		// tbd: find more generalizeable or configurable implementation
		if ( IFRAME_TOOL_ID.equals(toolId) || NEWS_TOOL_ID.equals(toolId) || LINK_TOOL_ID.equals(toolId) )
			return m_title;
			
		// otherwise, return localized title
		else
			return ActiveToolManager.getTool(toolId).getTitle();
	}

	/**
	 * @inheritDoc
	 */
	public int getLayout()
	{
		return m_layout;
	}

	/**
	 * @inheritDoc
	 */
	public String getSkin()
	{
		if (m_site != null)
		{
			return siteService.adjustSkin(m_site
					.getSkin(), m_site.isPublished());
		}

		return m_skin;
	}

	/**
	 * @inheritDoc
	 */
	public String getSiteId()
	{
		if (m_site != null)
		{
			return m_site.getId();
		}

		return m_siteId;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isPopUp()
	{
		return m_popup;
	}

	/**
	 * @inheritDoc
	 */
	public String getLayoutTitle()
	{
		return ((BaseSiteService) (SiteService.getInstance())).getLayoutNames()[m_layout];
	}

	/**
	 * @inheritDoc
	 */
	public List getTools()
	{
		if (m_toolsLazy)
		{
			((BaseSiteService) (SiteService.getInstance())).m_storage.readPageTools(this,
					m_tools);
			m_toolsLazy = false;
		}

		// TODO: need to sort by layout hint
		return m_tools;
	}

	/**
	 * @inheritDoc
	 */
	public Collection getTools(String[] toolIds)
	{
		List rv = new Vector();
		if ((toolIds == null) || (toolIds.length == 0)) return rv;

		for (Iterator iTools = getTools().iterator(); iTools.hasNext();)
		{
			ToolConfiguration tc = (ToolConfiguration) iTools.next();
			Tool tool = tc.getTool();
			if ((tool != null) && (tool.getId() != null))
			{
				for (int i = 0; i < toolIds.length; i++)
				{
					if (tool.getId().equals(toolIds[i]))
					{
						rv.add(tc);
					}
				}
			}
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public List getTools(int col)
	{
		// TODO: need to sort by layout hint
		List rv = new Vector();
		for (Iterator iTools = getTools().iterator(); iTools.hasNext();)
		{
			ToolConfiguration tc = (ToolConfiguration) iTools.next();
			// row, col
			int[] layout = tc.parseLayoutHints();
			if (layout != null)
			{
				if (layout[1] == col)
				{
					rv.add(tc);
				}
			}
			// else consider it part of the 0 column
			else if (col == 0)
			{
				rv.add(tc);
			}
		}
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public ToolConfiguration getTool(String id)
	{
		return (ToolConfiguration) ((ResourceVector) getTools()).getById(id);
	}

	/**
	 * @inheritDoc
	 */
	public void setTitle(String title)
	{
		m_title = StringUtil.trimToNull(title);
	}

	/**
	 * @inheritDoc
	 */
	public void setLayout(int layout)
	{
		if ((layout == LAYOUT_SINGLE_COL) || (layout == LAYOUT_DOUBLE_COL))
		{
			m_layout = layout;
		}
		else
			M_log.warn("setLayout(): set to invalid value: " + layout);
	}

	/**
	 * @inheritDoc
	 */
	public void setPopup(boolean popup)
	{
		m_popup = popup;
	}

	/**
	 * {@inheritDoc}
	 */
	public void localizePage()
	{
		String localizedTitle = null;
		
		// First localize tools
		for (Iterator iTools = getTools().iterator(); iTools.hasNext();)
		{
			BaseToolConfiguration tool = (BaseToolConfiguration)iTools.next();
			if ( tool.getTool() != null ) // this could happen in cafe build
				localizedTitle = tool.localizeTool();
		}
		
		// if one and only one tool title exists (and it's valid) replace page title with localized tool title
		if ( getTools().size() == 1 && localizedTitle != null && !localizedTitle.trim().equals("") )
			setTitle(localizedTitle);
	}
	
	/**
	 * @inheritDoc
	 */
	public ToolConfiguration addTool()
	{
		BaseToolConfiguration tool = new BaseToolConfiguration(siteService, this);
		((ResourceVector) getTools()).add(tool);

		return tool;
	}

	/**
	 * @inheritDoc
	 */
	public ToolConfiguration addTool(Tool reg)
	{
		BaseToolConfiguration tool = new BaseToolConfiguration(siteService,reg, this);
		((ResourceVector) getTools()).add(tool);

		return tool;
	}

	/**
	 * @inheritDoc
	 */
	public ToolConfiguration addTool(String toolId)
	{
		BaseToolConfiguration tool = new BaseToolConfiguration(siteService, toolId, this);
		((ResourceVector) getTools()).add(tool);

		return tool;
	}

	/**
	 * @inheritDoc
	 */
	public void removeTool(ToolConfiguration tool)
	{
		((ResourceVector) getTools()).remove(tool);
	}

	/**
	 * @inheritDoc
	 */
	public void moveUp()
	{
		if (m_site == null) return;
		((ResourceVector) m_site.getPages()).moveUp(this);
	}

	/**
	 * @inheritDoc
	 */
	public void setPosition(int pos)
	{
		if (m_site == null) return;
		((ResourceVector) m_site.getPages()).moveTo(this, pos);
	}

	/**
	 * @inheritDoc
	 */
	public int getPosition()
	{
		if (m_site == null) return -1;
		return ((ResourceVector) m_site.getPages()).indexOf(this);
	}

	public void setupPageCategory(String toolId)
	{
		String defaultCategory = null;
		Map<String, String> toolCategories = ServerConfigurationService
				.getToolToCategoryMap(m_site.getType());
		defaultCategory = toolCategories.get(toolId);
		if (getProperties().get(PAGE_CATEGORY_PROP) == null && defaultCategory != null)
		{
			getProperties().addProperty(PAGE_CATEGORY_PROP, defaultCategory);
		}
	}

	/**
	 * @inheritDoc
	 */
	public void moveDown()
	{
		if (m_site == null) return;
		((ResourceVector) m_site.getPages()).moveDown(this);
	}

	/**
	 * @inheritDoc
	 */
	public ResourcePropertiesEdit getPropertiesEdit()
	{
		if (((BaseResourceProperties) m_properties).isLazy())
		{
			((BaseSiteService) (SiteService.getInstance())).m_storage.readPageProperties(
					this, m_properties);
			((BaseResourcePropertiesEdit) m_properties).setLazy(false);
		}

		return m_properties;
	}

	/**
	 * Enable editing.
	 */
	protected void activate()
	{
		m_active = true;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isActiveEdit()
	{
		return m_active;
	}

	/**
	 * Close the edit object - it cannot be used after this.
	 */
	protected void closeEdit()
	{
		m_active = false;
	}

	/**
	 * @inheritDoc
	 */
	public String getUrl()
	{
		String rv = null;
		if (m_site == null)
		{
			rv = ((BaseSiteService) (SiteService.getInstance()))
					.serverConfigurationService().getPortalUrl()
					+ ((BaseSiteService) (SiteService.getInstance())).sitePageReference(
							m_siteId, m_id);
		} else {

			rv = ((BaseSiteService) (SiteService.getInstance())).serverConfigurationService()
				.getPortalUrl()
				+ ((BaseSiteService) (SiteService.getInstance())).sitePageReference(
						m_site.getId(), m_id);
		}
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getReference()
	{
		if (m_site == null)
		{
			return ((BaseSiteService) (SiteService.getInstance())).sitePageReference(
					m_siteId, m_id);
		}

		return ((BaseSiteService) (SiteService.getInstance())).sitePageReference(m_site
				.getId(), m_id);
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
	 * @inheritDoc
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * @inheritDoc
	 */
	public Site getContainingSite()
	{
		return m_site;
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getProperties()
	{
		if (((BaseResourceProperties) m_properties).isLazy())
		{
			((BaseSiteService) (SiteService.getInstance())).m_storage.readPageProperties(
					this, m_properties);
			((BaseResourcePropertiesEdit) m_properties).setLazy(false);
		}

		return m_properties;
	}

	/**
	 * @inheritDoc
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element page = doc.createElement("page");
		((Element) stack.peek()).appendChild(page);

		page.setAttribute("id", getId());
		if (m_title != null) page.setAttribute("title", m_title);
		page.setAttribute("layout", Integer.toString(m_layout));
		page.setAttribute("popup", Boolean.valueOf(m_popup).toString());

		// properties
		stack.push(page);
		getProperties().toXml(doc, stack);
		stack.pop();

		// tools
		Element list = doc.createElement("tools");
		page.appendChild(list);
		stack.push(list);
		for (Iterator iTools = getTools().iterator(); iTools.hasNext();)
		{
			BaseToolConfiguration tool = (BaseToolConfiguration) iTools.next();
			tool.toXml(doc, stack);
		}
		stack.pop();

		return page;
	}
}
