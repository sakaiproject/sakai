/**
 * $Id$
 * $URL$
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityTool;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;

/**
 * ToolEntityProvider
 *
 * @author Earle Nietzel
 * Created on Sep 4, 2013
 * 
 */
public class ToolEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, 
        Outputable, Resolvable, Describeable, ActionsExecutable, CollectionResolvable {

    public final static String ENTITY_PREFIX = "tool";

    private ToolManager toolManager;

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
     */
    @Override
    public String getEntityPrefix() {
        return ENTITY_PREFIX;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
     */
    @Override
    public Object getEntity(EntityReference ref) {
        if (ref == null || ref.getId() == null || getTool(ref.getId()) == null) {
            return new EntityTool();
        }

        return getToolEntity(getTool(ref.getId()));
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#getHandledOutputFormats()
     */
    @Override
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.HTML, Formats.JSON };
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider#entityExists(java.lang.String)
     */
    @Override
    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }

        Tool tool = getTool(id);

        if (tool != null) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable#getEntities(org.sakaiproject.entitybroker.EntityReference, org.sakaiproject.entitybroker.entityprovider.search.Search)
     */
    @Override
    public List<?> getEntities(EntityReference ref, Search search) {
        List<EntityTool> tools = new ArrayList<EntityTool>();
        if (search.getRestrictionByProperty("id") != null) {
            String id = search.getRestrictionByProperty("id").getStringValue();

            EntityTool entityTool = getToolEntity(getTool(id));
            if (entityTool != null) {
                tools.add(entityTool);
            }
        } else if (search.getRestrictionByProperty("keywords") != null) {
            String[] keywords = (String[]) search.getRestrictionByProperty("keywords").getArrayValue();
            Set<Tool> found = toolManager.findTools(null, new HashSet<String>(Arrays.asList(keywords)));

            for (Tool tool : found) {
                EntityTool entityTool = getToolEntity(tool);
                if (entityTool != null) {
                    tools.add(entityTool);
                }
            }
        } else if (search.getRestrictionByProperty("categories") != null) {
            String[] categories = (String[]) search.getRestrictionByProperty("categories").getArrayValue();
            Set<Tool> found = toolManager.findTools(new HashSet<String>(Arrays.asList(categories)), null);

            for (Tool tool : found) {
                EntityTool entityTool = getToolEntity(tool);
                if (entityTool != null) {
                    tools.add(entityTool);
                }
            }
        } else {
            Set<Tool> found = toolManager.findTools(null, null);

            for (Tool tool : found) {
                EntityTool entityTool = getToolEntity(tool);
                if (entityTool != null) {
                    tools.add(entityTool);
                }
            }
        }
        Collections.sort(tools);
        return tools;
    }

    @EntityCustomAction(action="allToolIds",viewKey=EntityView.VIEW_LIST)
    public Object getAllToolIds(EntityReference ref) {
        Set<Tool> tools = toolManager.findTools(null, null);
        List<String> toolIds = new ArrayList<String>(tools.size());

        for (Tool tool : tools) {
            toolIds.add(tool.getId());
        }
        Collections.sort(toolIds);
        return new ActionReturn(toolIds);
    }

    @EntityCustomAction(action="hiddenToolIds",viewKey=EntityView.VIEW_LIST)
    public Object getHiddenToolIds(EntityReference ref) {
        Set<Tool> allTools = toolManager.findTools(null, null);
        Set<Tool> publicTools = toolManager.findTools(Collections.<String> emptySet(), null);

        List<String> allToolIds = new ArrayList<String>(allTools.size());
        List<String> publicToolIds = new ArrayList<String>(publicTools.size());
        List<String> hiddenToolIds = new ArrayList<String>(allTools.size() - publicToolIds.size());

        for (Tool tool : allTools) {
            allToolIds.add(tool.getId());
        }

        for (Tool tool : publicTools) {
            publicToolIds.add(tool.getId());
        }

        hiddenToolIds.addAll(allToolIds);
        hiddenToolIds.removeAll(publicToolIds);

        Collections.sort(hiddenToolIds);
        return new ActionReturn(hiddenToolIds);
    }

    @EntityCustomAction(action="publicToolIds",viewKey=EntityView.VIEW_LIST)
    public Object getPublicToolIds(EntityReference ref) {
        Set<Tool> publicTools = toolManager.findTools(Collections.<String> emptySet(), null);

        List<String> publicToolIds = new ArrayList<String>(publicTools.size());

        for (Tool tool : publicTools) {
            publicToolIds.add(tool.getId());
        }

        Collections.sort(publicToolIds);
        return new ActionReturn(publicToolIds);
    }

    public void setToolManager(ToolManager toolManager) {
        this.toolManager = toolManager;
    }

    private Tool getTool(String id) {
        if (! "".equals(id)) {
            return toolManager.getTool(id);
        }
        return null;
    }

    private EntityTool getToolEntity(Tool tool) {
        if (tool != null) {
            return new EntityTool(tool);
        }
        return null;
    }

}
