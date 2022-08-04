/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 Sakai Foundation
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

package org.sakaiproject.util;

import java.util.Properties;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

/**
 * <p>
 * Placement is a utility class that implements the Placement interface.
 * </p>
 */
public class Placement implements org.sakaiproject.tool.api.Placement
{

	/** Placement configuration Properties (mutable). */
	protected Properties m_config = new Properties();

	/** The placement context. */
	protected String m_context = null;

	/** The well known identifier string. */
	protected String m_id = null;

	/** The title string. */
	protected String m_title = null;

	/** Tool placed. */
	protected Tool m_tool = null;

	/** The placed tool's id - use if the tool is null. */
	protected String m_toolId = null;

    private static Object LOCK = new Object();

    private static ToolManager toolManager;
    protected static ToolManager getToolManager() {
        if (toolManager == null) {
            synchronized (LOCK) {
                ToolManager component = (ToolManager) ComponentManager.get(ToolManager.class);
                if (component == null) {
                    throw new IllegalStateException("Unable to find the ToolManager using the ComponentManager");
                } else {
                    toolManager = component;
                }
            }
        }
        return toolManager;
    }

	/**
	 * Construct
	 */
	public Placement()
	{
	}

	/**
	 * Construct.
	 *
	 * @param id
	 *        The placement id.
	 * @param toolId
	 *        The well-known tool id associated with this placement.
	 * @param tool
	 *        The tool to place.
	 * @param config
	 *        The particular placement config Properties to use.
	 * @param context
	 *        The particular placement context to use.
	 * @param title
	 *        The tool placement title.
	 */
	public Placement(String id, String toolId, Tool tool, Properties config, String context, String title)
	{
		m_id = id;
		m_toolId = toolId;
		m_tool = tool;
		if (config != null)
		{
			m_config = config;
		}
		m_context = context;
		m_title = title;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Placement))
		{
			return false;
		}

		return ((Placement) obj).getId().equals(getId());
	}

	/**
	 * {@inheritDoc}
	 */
	public Properties getConfig()
	{
		// the placement config overrides registered config
		Properties p = new Properties();

		// put the mutable registered ones in, and do it first so that the placement can override
		if (m_tool != null)
		{
			p.putAll(m_tool.getMutableConfig());
		}

		// put the placement properties in
		p.putAll(getPlacementConfig());

		// put the final registered ones in last so they cannot be overriden by the placement
		if (m_tool != null)
		{
			p.putAll(m_tool.getFinalConfig());
		}
		
		return p;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContext()
	{
		return m_context;
	}

	/**
	 * @inheritDoc
	 */
	public String getId()
	{
		return m_id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Properties getPlacementConfig()
	{
		return m_config;
	}

	/**
	 * @inheritDoc
	 */
	public String getTitle()
	{
		// shouldn't happen (see BaseToolConfiguration.getTitle()
		return "(title unknown)";
	}

	/**
	 * @inheritDoc
	 */
	public Tool getTool()
	{
		// we might have a case where a placement was made before a tool was registered, leaving us with a toolId but no tool.
		if ((m_tool == null) && (m_toolId != null))
		{
			// if so, we can try again
			m_tool = getToolManager().getTool(m_toolId);
		}

		return m_tool;
	}

	/**
	 * @inheritDoc
	 */
	public String getToolId()
	{
		// if we really have a tool, use this id
		if (m_tool != null) return m_tool.getId();

		// otherwise use the id we are holding
		return m_toolId;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		return getId().hashCode();
	}

	/**
	 * Set the context.
	 *
	 * @param context
	 *        The context to set.
	 */
	public void setContext(String context)
	{
		m_context = context;
	}

	/**
	 * Set the id.
	 *
	 * @param id
	 *        The id to set.
	 */
	public void setId(String id)
	{
		m_id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTitle(String title)
	{
		m_title = title;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setTool(String toolId, Tool tool)
	{
		m_toolId = toolId;
		m_tool = tool;
	}

	/**
	 * {@inheritDoc}
	 */
	public void save()
	{
	}
}
