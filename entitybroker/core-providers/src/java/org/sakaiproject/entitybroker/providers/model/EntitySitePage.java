/**
 * $Id: EntityGroup.java 105077 2012-02-24 22:54:29Z ottenhoff@longsight.com $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/core-providers/src/java/org/sakaiproject/entitybroker/providers/model/EntityGroup.java $
 * EntitySite.java - entity-broker - Jun 29, 2008 9:31:10 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
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

package org.sakaiproject.entitybroker.providers.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.azeckoski.reflectutils.annotations.ReflectIgnoreClassFields;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityFieldRequired;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is needed to allow RESTful access to the site page data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@ReflectIgnoreClassFields({"createdBy","modifiedBy","containingSite", "properties", "propertiesEdit","tools"})
public class EntitySitePage implements SitePage {

    private static final long serialVersionUID = 7526472295622776147L;

    @EntityId
    private String id;
    @EntityFieldRequired
    private String siteId;
    @EntityFieldRequired
    private String title;
    private int position;
    private boolean titleCustom;

    public Map<String, String> props;

    public Map<String, String> getProps() {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public void setProperty(String key, String value) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put(key, value);
    }

    public String getProperty(String key) {
        if (props == null) {
            return null;
        }
        return props.get(key);
    }

    private transient SitePage sitePage;

    public EntitySitePage(SitePage sitePage) {
        this.sitePage = sitePage;
        this.id = this.sitePage.getId();
        this.title = this.sitePage.getTitle();
        this.siteId = this.sitePage.getSiteId();
        this.position = this.sitePage.getPosition();
        this.titleCustom = this.sitePage.getTitleCustom();
        // properties
        ResourceProperties rp = sitePage.getProperties();
        for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
            String name = iterator.next();
            String value = rp.getProperty(name);
            this.setProperty(name, value);
        }
    }

    @Override
    public String getId() {
        return this.id;
    }
    @Override
    public String getTitle() {
        return this.title;
    }
    @Override
    public String getSiteId() {
        return this.siteId;
    }
    @Override
    public void setPosition(int pos) {
        if (sitePage != null) {
            sitePage.setPosition(pos);
        }
        this.position = pos;
    }
    @Override
    public int getPosition() {
        if (sitePage != null) {
            return sitePage.getPosition();
        }
        return this.position;
    }
    @Override
    public void setTitle(String title) {
        if (sitePage != null) {
            sitePage.setTitle(title);
        }
        this.setTitle(title);
    }
    @Override
    public void setTitleCustom(boolean custom) {
        if (sitePage != null) {
            sitePage.setTitleCustom(custom);
        }
        this.setTitleCustom(custom);
    }
    @Override
    public boolean getTitleCustom() {
        return this.titleCustom;
    }
    
    @Override
    public boolean isHomePage()
    {
        if (sitePage != null) {
            return sitePage.isHomePage();
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setHomeToolsTitleCustom(String toolId)
    {
        if (sitePage != null) {
            sitePage.setHomeToolsTitleCustom(toolId);
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean getHomeToolsTitleCustom(String toolId)
    {
        if (sitePage != null) {
            return sitePage.getHomeToolsTitleCustom(toolId);
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getReference() {
        if (sitePage != null) {
            return sitePage.getReference();
        }
        return "/site/"+this.siteId+"/page/"+this.id;
    }


    @Override
    public boolean isActiveEdit() {
        if (sitePage != null) {
            return sitePage.isActiveEdit();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public ResourcePropertiesEdit getPropertiesEdit() {
        if (sitePage != null) {
            return sitePage.getPropertiesEdit();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public String getUrl() {
        if (sitePage != null) {
            return sitePage.getUrl();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public String getUrl(String rootProperty) {
        if (sitePage != null) {
            return sitePage.getUrl(rootProperty);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public String getReference(String rootProperty) {
        if (sitePage != null) {
            return sitePage.getReference(rootProperty);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public ResourceProperties getProperties() {
        if (sitePage != null) {
            return sitePage.getProperties();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public Element toXml(Document doc, Stack<Element> stack) {
        if (sitePage != null) {
            return sitePage.toXml(doc, stack);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public int getLayout() {
        if (sitePage != null) {
            return sitePage.getLayout();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public String getLayoutTitle() {
        if (sitePage != null) {
            return sitePage.getLayoutTitle();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ToolConfiguration> getTools() {
        if (sitePage != null) {
            return sitePage.getTools();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public List<ToolConfiguration> getTools(int col) {
        if (sitePage != null) {
            return sitePage.getTools(col);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public Collection<ToolConfiguration> getTools(String[] toolIds) {
        if (sitePage != null) {
            return sitePage.getTools(toolIds);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public String getSkin() {
        if (sitePage != null) {
            return sitePage.getSkin();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isPopUp() {
        if (sitePage != null) {
            return sitePage.isPopUp();
        }
        return false;
    }
    @Override
    public ToolConfiguration getTool(String id) {
        if (sitePage != null) {
            return sitePage.getTool(id);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public Site getContainingSite() {
        if (sitePage != null) {
            return sitePage.getContainingSite();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void localizePage() {
        if (sitePage != null) {
            sitePage.localizePage();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void setLayout(int layout) {
        if (sitePage != null) {
            sitePage.setLayout(layout);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void setPopup(boolean popup) {
        if (sitePage != null) {
            sitePage.setPopup(popup);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public ToolConfiguration addTool() {
        if (sitePage != null) {
            return sitePage.addTool();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public ToolConfiguration addTool(Tool reg) {
        if (sitePage != null) {
            return sitePage.addTool();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public ToolConfiguration addTool(String toolId) {
        if (sitePage != null) {
            return sitePage.addTool();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void removeTool(ToolConfiguration tool) {
        if (sitePage != null) {
            sitePage.removeTool(tool);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void moveUp() {
        if (sitePage != null) {
            sitePage.moveUp();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void moveDown() {
        if (sitePage != null) {
            sitePage.moveDown();
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public void setupPageCategory(String toolId) {
        if (sitePage != null) {
            sitePage.setupPageCategory(toolId);
        }
        throw new UnsupportedOperationException();
    }
    @Override
    public boolean isTitleToolException(String toolId) {
        if (sitePage != null) {
            return sitePage.isTitleToolException(toolId);
        }
        throw new UnsupportedOperationException();
    }

}
