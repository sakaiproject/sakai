/**
 * $Id$
 * $URL$
 * EntitySite.java - entity-broker - Jun 29, 2008 9:31:10 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


/**
 * This is needed to allow RESTful access to the site data
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
@SuppressWarnings("unchecked")
public class EntitySite implements Site {

   @EntityId
   private String id;
   private String title;
   private String shortDescription;
   private String description;
   private String iconUrl;
   private String iconFullUrl;
   private String infoUrl;
   private String infoUrlFull;
   private boolean joinable;
   private String joinerRole;
   private String maintainRole;
   private String skin;
   private boolean published;
   private boolean pubView;
   private String type;
   private String providerGroupId;
   private boolean customPageOrdered;

   public Map<String, String> props;
   public Map<String, String> getProps() {
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

   private transient Site site;

   public EntitySite() {}

   public EntitySite(String title, String shortDescription, String description, String iconUrl,
         String iconFullUrl, String infoUrl, String infoUrlFull, boolean joinable,
         String joinerRole, String maintainRole, String skin, boolean published, boolean pubView,
         String type, String providerGroupId, boolean customPageOrdered) {
      this.title = title;
      this.shortDescription = shortDescription;
      this.description = description;
      this.iconUrl = iconUrl;
      this.iconFullUrl = iconFullUrl;
      this.infoUrl = infoUrl;
      this.infoUrlFull = infoUrlFull;
      this.joinable = joinable;
      this.joinerRole = joinerRole;
      this.maintainRole = maintainRole;
      this.skin = skin;
      this.published = published;
      this.pubView = pubView;
      this.type = type;
      this.providerGroupId = providerGroupId;
      this.customPageOrdered = customPageOrdered;
   }

   @SuppressWarnings("unchecked")
   public EntitySite(Site site) {
      this.site = site;
      this.id = site.getId();
      this.title = site.getTitle();
      this.shortDescription = site.getShortDescription();
      this.description = site.getDescription();
      this.iconUrl = site.getIconUrl();
      this.infoUrl = site.getInfoUrl();
      this.joinable = site.isJoinable();
      this.joinerRole = site.getJoinerRole();
      this.skin = site.getSkin();
      this.published = site.isPublished();
      this.type = site.getType();
      this.customPageOrdered = site.isCustomPageOrdered();
      this.maintainRole = site.getMaintainRole();
      this.providerGroupId = site.getProviderGroupId();
      // properties
      ResourceProperties rp = site.getProperties();
      for (Iterator<String> iterator = rp.getPropertyNames(); iterator.hasNext();) {
         String name = iterator.next();
         String value = rp.getProperty(name);
         this.setProperty(name, value);
      }
   }

   public String getTitle() {
      return title;
   }

   public void setTitle(String title) {
      this.title = title;
   }

   public String getShortDescription() {
      return shortDescription;
   }

   public void setShortDescription(String shortDescription) {
      this.shortDescription = shortDescription;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getIconUrl() {
      return iconUrl;
   }

   public void setIconUrl(String iconUrl) {
      this.iconUrl = iconUrl;
   }

   public String getInfoUrl() {
      return infoUrl;
   }

   public void setInfoUrl(String infoUrl) {
      this.infoUrl = infoUrl;
   }

   public String getInfoUrlFull() {
      if (site != null) {
         return site.getInfoUrlFull();
      }
      return infoUrlFull;
   }

   public void setInfoUrlFull(String infoUrlFull) {
      this.infoUrlFull = infoUrlFull;
   }

   public boolean isJoinable() {
      return joinable;
   }

   public void setJoinable(boolean joinable) {
      this.joinable = joinable;
   }

   public String getJoinerRole() {
      return joinerRole;
   }

   public void setJoinerRole(String joinerRole) {
      this.joinerRole = joinerRole;
   }

   public String getSkin() {
      return skin;
   }

   public void setSkin(String skin) {
      this.skin = skin;
   }

   public boolean isPublished() {
      return published;
   }

   public void setPublished(boolean published) {
      this.published = published;
   }

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getIconFullUrl() {
      if (site != null) {
         return site.getIconUrlFull();
      }
      return iconFullUrl;
   }

   public void setIconFullUrl(String iconFullUrl) {
      this.iconFullUrl = iconFullUrl;
   }

   
   public String getMaintainRole() {
      return maintainRole;
   }

   
   public void setMaintainRole(String maintainRole) {
      this.maintainRole = maintainRole;
   }

   
   public String getProviderGroupId() {
      return providerGroupId;
   }

   
   public void setProviderGroupId(String providerGroupId) {
      this.providerGroupId = providerGroupId;
   }

   
   public boolean isCustomPageOrdered() {
      return customPageOrdered;
   }

   
   public void setCustomPageOrdered(boolean customPageOrdered) {
      this.customPageOrdered = customPageOrdered;
   }

   
   public String getId() {
      return id;
   }

   
   public void setId(String id) {
      this.id = id;
   }

   
   public boolean isPubView() {
      return pubView;
   }

   
   public void setPubView(boolean pubView) {
      this.pubView = pubView;
   }


   // Site operations

   public Group addGroup() {
      if (site != null) {
         return site.addGroup();
      }
      throw new NotImplementedException();
   }

   public SitePage addPage() {
      if (site != null) {
         return site.addPage();
      }
      throw new NotImplementedException();
   }

   public User getCreatedBy() {
      if (site != null) {
         return site.getCreatedBy();
      }
      throw new NotImplementedException();
   }

   public Time getCreatedTime() {
      if (site != null) {
         return site.getCreatedTime();
      }
      throw new NotImplementedException();
   }

   public Group getGroup(String arg0) {
      if (site != null) {
         return site.getGroup(arg0);
      }
      throw new NotImplementedException();
   }

   public Collection getGroups() {
      if (site != null) {
         return site.getGroups();
      }
      throw new NotImplementedException();
   }

   public Collection getGroupsWithMember(String arg0) {
      if (site != null) {
         return site.getGroupsWithMember(arg0);
      }
      throw new NotImplementedException();
   }

   public Collection getGroupsWithMemberHasRole(String arg0, String arg1) {
      if (site != null) {
         return site.getGroupsWithMemberHasRole(arg0, arg1);
      }
      throw new NotImplementedException();
   }

   public String getIconUrlFull() {
      if (site != null) {
         return site.getIconUrlFull();
      }
      throw new NotImplementedException();
   }

   public User getModifiedBy() {
      if (site != null) {
         return site.getModifiedBy();
      }
      throw new NotImplementedException();
   }

   public Time getModifiedTime() {
      if (site != null) {
         return site.getModifiedTime();
      }
      throw new NotImplementedException();
   }

   public List getOrderedPages() {
      if (site != null) {
         return site.getOrderedPages();
      }
      throw new NotImplementedException();
   }

   public SitePage getPage(String arg0) {
      if (site != null) {
         return site.getPage(arg0);
      }
      throw new NotImplementedException();
   }

   public List getPages() {
      if (site != null) {
         return site.getPages();
      }
      throw new NotImplementedException();
   }

   public ToolConfiguration getTool(String arg0) {
      if (site != null) {
         return site.getTool(arg0);
      }
      throw new NotImplementedException();
   }

   public ToolConfiguration getToolForCommonId(String arg0) {
      if (site != null) {
         return site.getToolForCommonId(arg0);
      }
      throw new NotImplementedException();
   }

   public Collection getTools(String[] arg0) {
      if (site != null) {
         return site.getTools(arg0);
      }
      throw new NotImplementedException();
   }

   public Collection getTools(String arg0) {
      if (site != null) {
         return site.getTools(arg0);
      }
      throw new NotImplementedException();
   }

   public boolean hasGroups() {
      if (site != null) {
         return site.hasGroups();
      }
      throw new NotImplementedException();
   }

   public boolean isType(Object arg0) {
      if (site != null) {
         return site.isType(arg0);
      }
      throw new NotImplementedException();
   }

   public void loadAll() {
      if (site != null) {
         site.loadAll();
      }
      throw new NotImplementedException();
   }

   public void regenerateIds() {
      if (site != null) {
         site.regenerateIds();
      }
      throw new NotImplementedException();
   }

   public void removeGroup(Group arg0) {
      if (site != null) {
         site.removeGroup(arg0);
      }
      throw new NotImplementedException();
   }

   public void removePage(SitePage arg0) {
      if (site != null) {
         site.removePage(arg0);
      }
      throw new NotImplementedException();
   }

   public ResourcePropertiesEdit getPropertiesEdit() {
      if (site != null) {
         return site.getPropertiesEdit();
      }
      throw new NotImplementedException();
   }

   public boolean isActiveEdit() {
      if (site != null) {
         return site.isActiveEdit();
      }
      throw new NotImplementedException();
   }

   public ResourceProperties getProperties() {
      if (site != null) {
         return site.getProperties();
      }
      throw new NotImplementedException();
   }

   public String getReference() {
      return "/site/" + id;
   }

   public String getReference(String arg0) {
      return this.getReference();
   }

   public String getUrl() {
      if (site != null) {
         return site.getUrl();
      }
      throw new NotImplementedException();
   }

   public String getUrl(String arg0) {
      if (site != null) {
         return site.getUrl(arg0);
      }
      throw new NotImplementedException();
   }

   public Element toXml(Document arg0, Stack arg1) {
      if (site != null) {
         return site.toXml(arg0, arg1);
      }
      throw new NotImplementedException();
   }

   public int compareTo(Object o) {
      if (site != null) {
         return site.compareTo(o);
      }
      throw new NotImplementedException();
   }

   public void addMember(String arg0, String arg1, boolean arg2, boolean arg3) {
      if (site != null) {
         addMember(arg0, arg1, arg2, arg3);
      }
      throw new NotImplementedException();
   }

   public Role addRole(String arg0) throws RoleAlreadyDefinedException {
      // TODO Auto-generated method stub
      return null;
   }

   public Role addRole(String arg0, Role arg1) throws RoleAlreadyDefinedException {
      // TODO Auto-generated method stub
      return null;
   }

   public Member getMember(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getMembers() {
      // TODO Auto-generated method stub
      return null;
   }

   public Role getRole(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getRoles() {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getRolesIsAllowed(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public Role getUserRole(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getUsers() {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getUsersHasRole(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public Set getUsersIsAllowed(String arg0) {
      // TODO Auto-generated method stub
      return null;
   }

   public boolean hasRole(String arg0, String arg1) {
      // TODO Auto-generated method stub
      return false;
   }

   public boolean isAllowed(String arg0, String arg1) {
      if (site != null) {
         return site.isAllowed(arg0, arg1);
      }
      return false;
   }

   public boolean isEmpty() {
      if (site != null) {
         return site.isEmpty();
      }
      return false;
   }

   public boolean keepIntersection(AuthzGroup arg0) {
      // TODO Auto-generated method stub
      return false;
   }

   public void removeMember(String arg0) {
      // TODO Auto-generated method stub
      
   }

   public void removeMembers() {
      // TODO Auto-generated method stub
      
   }

   public void removeRole(String arg0) {
      // TODO Auto-generated method stub
      
   }

   public void removeRoles() {
      // TODO Auto-generated method stub
      
   }

}
