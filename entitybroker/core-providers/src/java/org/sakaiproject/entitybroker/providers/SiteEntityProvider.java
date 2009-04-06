/**
 * $Id: SiteEntityProvider.java 52667 2008-09-25 16:00:21Z aaronz@vt.edu $
 * $URL: https://source.sakaiproject.org/svn/entitybroker/trunk/impl/src/java/org/sakaiproject/entitybroker/providers/SiteEntityProvider.java $
 * SiteEntityProvider.java - entity-broker - Jun 29, 2008 8:35:55 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.azeckoski.reflectutils.ReflectUtils;
import org.sakaiproject.authz.api.RoleAlreadyDefinedException;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.EntityView.Method;
import org.sakaiproject.entitybroker.entityprovider.CoreEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityParameters;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityURLRedirect;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.providers.model.EntityGroup;
import org.sakaiproject.entitybroker.providers.model.EntitySite;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;

/**
 * Creates a provider for dealing with sites
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class SiteEntityProvider extends AbstractEntityProvider implements CoreEntityProvider, 
        RESTful, ActionsExecutable, Redirectable, RequestStorable {

    private SiteService siteService;
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    private UserEntityProvider userEntityProvider;
    public void setUserEntityProvider(UserEntityProvider userEntityProvider) {
        this.userEntityProvider = userEntityProvider;
    }

    public static String PREFIX = "site";
    public String getEntityPrefix() {
        return PREFIX;
    }

    @EntityURLRedirect("/{prefix}/{id}/memberships")
    public String redirectMemberships(Map<String,String> vars) {
        return MembershipEntityProvider.PREFIX + "/site/" + vars.get("id") + vars.get(TemplateParseUtil.DOT_EXTENSION);
    }

    @EntityCustomAction(action="exists", viewKey=EntityView.VIEW_SHOW)
    public boolean checkSiteExists(EntityView view) {
        String siteId = view.getEntityReference().getId();
        boolean exists = entityExists(siteId);
        return exists;
    }

    @EntityCustomAction(action="role", viewKey="")
    public void handleRoles (EntityView view){
    	String siteId = view.getEntityReference().getId();
    	String roleId = view.getPathSegment(3);
    	if (roleId == null){
    		throw new IllegalArgumentException("No role id specified");
    	}
    	Site site = getSiteById(siteId);
    	if (view.getMethod().equals(Method.POST.name())){
	    	try {
				site.addRole(roleId);
			} catch (RoleAlreadyDefinedException e) {
				// Ignore
			} 
    	} else if (view.getMethod().equals(Method.DELETE.name())) {
    		site.removeRole(roleId);
    	} else {
    		throw new IllegalArgumentException("Method " + view.getMethod() + " not supported");
    	}
    	try {
			siteService.save(site);
		} catch (IdUnusedException e) {
			// Ignore
		} catch (PermissionException e) {
			throw new SecurityException("User not allowed to update role " + roleId + " in site " + siteId);
		}
    }

    @EntityCustomAction(action="group", viewKey=EntityView.VIEW_SHOW)
    public EntityGroup handleGroup(EntityView view) {
        // expects site/siteId/group/groupId
        String groupId = view.getPathSegment(3);
        if (groupId == null) {
            throw new IllegalArgumentException("Invalid path provided: expect to receive the ");
        }
        String siteId = view.getEntityReference().getId();
        Site site = getSiteById(siteId);
        Group group = site.getGroup(groupId);
        EntityGroup eg = new EntityGroup(group);
        return eg;
    }


    /**
     * @param site the site to check perms in
     * @return true if the current user can view this site
     * @throws SecurityException if not allowed
     */
    protected boolean isAllowedAccessMembers(Site site) {
        // check if the current user can access this
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("Anonymous users may not view memberships in ("+site.getReference()+")");
        } else {
            if (! siteService.allowViewRoster(site.getId())) {
                throw new SecurityException("Memberships in this site ("+site.getReference()+") are not accessible for the current user: " + userReference);
            }
        }
        return true;
    }

    public boolean entityExists(String id) {
        if (id == null) {
            return false;
        }
        if ("".equals(id)) {
            return true;
        }
        boolean exists = siteService.siteExists(id);
        return exists;
    }

    public String createEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String siteId = null;
        if (ref.getId() != null && ref.getId().length() > 0) {
            siteId = ref.getId();
        }
        if (entity.getClass().isAssignableFrom(Site.class)) {
            // if someone passes in a Site
            Site site = (Site) entity;
            if (siteId == null && site.getId() != null) {
                siteId = site.getId();
            }
            Site s = null;
            try {
                s = siteService.addSite(siteId, site.getType());
                s.setCustomPageOrdered(site.isCustomPageOrdered());
                s.setDescription(site.getDescription());
                s.setIconUrl(site.getIconUrl());
                s.setInfoUrl(site.getInfoUrl());
                s.setJoinable(site.isJoinable());
                s.setJoinerRole(site.getJoinerRole());
                s.setMaintainRole(site.getMaintainRole());
                s.setProviderGroupId(site.getProviderGroupId());
                s.setPublished(site.isPublished());
                s.setPubView(site.isPubView());
                s.setShortDescription(site.getShortDescription());
                s.setSkin(site.getSkin());
                s.setTitle(site.getTitle());
                siteService.save(s);
                siteId = s.getId();
            } catch (IdInvalidException e) {
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId + ":" + e.getMessage(), e);
            } catch (IdUsedException e) {
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                throw new SecurityException("Current user does not have permissions to create site: " + ref + ":" + e.getMessage(), e);
            } catch (IdUnusedException e) {
                throw new IllegalArgumentException("Cannot save new site with given id: " + siteId + ":" + e.getMessage(), e);
            }
        } else if (entity.getClass().isAssignableFrom(EntitySite.class)) {
            // if they instead pass in the EntitySite object
            EntitySite site = (EntitySite) entity;
            if (siteId == null && site.getId() != null) {
                siteId = site.getId();
            }
            Site s = null;
            try {
                s = siteService.addSite(siteId, site.getType());
                s.setCustomPageOrdered(site.isCustomPageOrdered());
                s.setDescription(site.getDescription());
                s.setIconUrl(site.getIconUrl());
                s.setInfoUrl(site.getInfoUrl());
                s.setJoinable(site.isJoinable());
                s.setJoinerRole(site.getJoinerRole());
                s.setMaintainRole(site.getMaintainRole());
                s.setProviderGroupId(site.getProviderGroupId());
                s.setPublished(site.isPublished());
                s.setPubView(site.isPubView());
                s.setShortDescription(site.getShortDescription());
                s.setSkin(site.getSkin());
                s.setTitle(site.getTitle());
                // attempt to set the owner as requested
                String ownerUserId = site.getOwner();
                if (ownerUserId != null) {
                    ownerUserId = userEntityProvider.findAndCheckUserId(ownerUserId, null);
                    if (ownerUserId == null) {
                        throw new IllegalArgumentException("Invalid userId supplied for owner of site: " + site.getOwner());
                    }
                    ReflectUtils.getInstance().setFieldValue(s, "m_createdUserId", ownerUserId);
                }
                // save the site
                siteService.save(s);
                siteId = s.getId();
            } catch (IdInvalidException e) {
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId + ":" + e.getMessage(), e);
            } catch (IdUsedException e) {
                throw new IllegalArgumentException("Cannot create site with given id: " + siteId + ":" + e.getMessage(), e);
            } catch (PermissionException e) {
                throw new SecurityException("Current user does not have permissions to create site: " + ref + ":" + e.getMessage(), e);
            } catch (IdUnusedException e) {
                throw new IllegalArgumentException("Cannot save new site with given id: " + siteId + ":" + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("Invalid entity for creation, must be Site or EntitySite object");
        }
        return siteId;
    }

    public Object getSampleEntity() {
        return new EntitySite();
    }

    public void updateEntity(EntityReference ref, Object entity, Map<String, Object> params) {
        String siteId = ref.getId();
        if (siteId == null) {
            throw new IllegalArgumentException("Cannot update, No siteId in provided reference: " + ref);
        }
        Site s = getSiteById(siteId);
        if (s == null) {
            throw new IllegalArgumentException("Cannot find site to update with id: " + siteId);
        }

        if (entity.getClass().isAssignableFrom(Site.class)) {
            // if someone passes in a Site
            Site site = (Site) entity;
            s.setCustomPageOrdered(site.isCustomPageOrdered());
            s.setDescription(site.getDescription());
            s.setIconUrl(site.getIconUrl());
            s.setInfoUrl(site.getInfoUrl());
            s.setJoinable(site.isJoinable());
            s.setJoinerRole(site.getJoinerRole());
            s.setMaintainRole(site.getMaintainRole());
            s.setProviderGroupId(site.getProviderGroupId());
            s.setPublished(site.isPublished());
            s.setPubView(site.isPubView());
            s.setShortDescription(site.getShortDescription());
            s.setSkin(site.getSkin());
            s.setTitle(site.getTitle());
            // put in properties
            ResourcePropertiesEdit rpe = s.getPropertiesEdit();
            rpe.set(site.getProperties());
        } else if (entity.getClass().isAssignableFrom(EntitySite.class)) {
            // if they instead pass in the entitysite object
            EntitySite site = (EntitySite) entity;
            s.setCustomPageOrdered(site.isCustomPageOrdered());
            if (site.getDescription() != null)
                s.setDescription(site.getDescription());
            if (site.getIconUrl() != null)
                s.setIconUrl(site.getIconUrl());
            s.setJoinable(site.isJoinable());
            if (site.getJoinerRole() != null)
                s.setJoinerRole(site.getJoinerRole());
            if (site.getMaintainRole() != null)
                s.setMaintainRole(site.getMaintainRole());
            if (site.getProviderGroupId() != null)
                s.setProviderGroupId(site.getProviderGroupId());
            s.setPublished(site.isPublished());
            s.setPubView(site.isPubView());
            if (site.getShortDescription() != null)
                s.setShortDescription(site.getShortDescription());
            if (site.getSkin() != null)
                s.setSkin(site.getSkin());
            if (site.getTitle() != null)
                s.setTitle(site.getTitle());
            // put in properties
            ResourcePropertiesEdit rpe = s.getPropertiesEdit();
            for (String key : site.getProps().keySet()) {
                String value = site.getProps().get(key);
                rpe.addProperty(key, value);
            }
            // attempt to set the owner as requested
            String ownerUserId = site.getOwner();
            if (ownerUserId != null) {
                ownerUserId = userEntityProvider.findAndCheckUserId(ownerUserId, null);
                if (ownerUserId == null) {
                    throw new IllegalArgumentException("Invalid userId supplied for owner of site: " + site.getOwner());
                }
                ReflectUtils.getInstance().setFieldValue(s, "m_createdUserId", ownerUserId);
            }
        } else {
            throw new IllegalArgumentException("Invalid entity for update, must be Site or EntitySite object");
        }
        try {
            siteService.save(s);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Sakai was unable to save a site which it just fetched: " + ref, e);
        } catch (PermissionException e) {
            throw new SecurityException("Current user does not have permissions to update site: " + ref + ":" + e.getMessage(), e);
        }
    }

    @EntityParameters(accepted={"includeGroups"})
    public Object getEntity(EntityReference ref) {
        boolean includeGroups = false;
        if (requestStorage.getStoredValue("includeGroups") != null) {
            includeGroups = true;
        }
        if (ref.getId() == null) {
            return new EntitySite();
        }
        String siteId = ref.getId();
        Site site = getSiteById(siteId);
        // check if the user can access site
        isAllowedAccessSite(site);
        // convert
        EntitySite es = new EntitySite(site, includeGroups);
        return es;
    }

    /**
     * @param site the site to check perms in
     * @return true if the current user can view this site
     * @throws SecurityException if not allowed
     */
    protected boolean isAllowedAccessSite(Site site) {
        // check if the user can access this
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            if (! siteService.allowAccessSite(site.getId())) {
                throw new SecurityException("This site ("+site.getReference()+") is not accessible to anon and there is no current user so the site is inaccessible");
            }
        } else {
            if (! site.isPubView() 
                    && ! siteService.allowAccessSite(site.getId())) {
                throw new SecurityException("This site ("+site.getReference()+") is not public and is not accessible for the current user: " + userReference);
            }
        }
        return true;
    }

    public void deleteEntity(EntityReference ref, Map<String, Object> params) {
        String siteId = ref.getId();
        if (siteId == null || "".equals(siteId)) {
            throw new IllegalArgumentException("Cannot delete site, No siteId in provided reference: " + ref);
        }
        Site site = getSiteById(siteId);
        if (site != null) {
            try {
                siteService.removeSite(site);
            } catch (PermissionException e) {
                throw new SecurityException("Permission denied: Site cannot be removed: " + ref);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public List<?> getEntities(EntityReference ref, Search search) {
        String criteria = null;
        String selectType = "access";
        Restriction select = search.getRestrictionByProperty("select");
        if (select == null) {
            select = search.getRestrictionByProperty("selectionType");
        }
        if (select != null) {
            selectType = select.value + "";
        }
        SelectionType sType = SelectionType.ACCESS;
        if ("access".equals(selectType)) {
            sType = SelectionType.ACCESS;
        } else if ("update".equals(selectType)) {
            sType = SelectionType.UPDATE;
        } else if ("joinable".equals(selectType)) {
            sType = SelectionType.JOINABLE;
        } else if ("pubView".equals(selectType)) {
            sType = SelectionType.PUBVIEW;
        } else {
            // based on the current user
            String userReference = developerHelperService.getCurrentUserReference();
            if (userReference == null) {
                sType = SelectionType.PUBVIEW;
            } else {
                if (developerHelperService.isUserAdmin(userReference)) {
                    sType = SelectionType.ANY;
                }
            }
        }

        Restriction restrict = search.getRestrictionByProperty("search");
        if (restrict == null) {
            restrict = search.getRestrictionByProperty("criteria");
        }
        if (restrict != null) {
            criteria = restrict.value + "";
        }
        List<Site> sites = siteService.getSites(sType, null, criteria, null, 
                SortType.TITLE_ASC, new PagingPosition(1, 50));
        // convert these into EntityUser objects
        List<EntitySite> entitySites = new ArrayList<EntitySite>();
        for (Site site : sites) {
            EntitySite es = new EntitySite(site, false);
            entitySites.add( es );
        }
        return entitySites;
    }

    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON, Formats.FORM };
    }


    private Site getSiteById(String siteId) {
        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (IdUnusedException e) {
            throw new IllegalArgumentException("Cannot find site by siteId: " + siteId, e);
        }
        return site;
    }

    private RequestStorage requestStorage;
    public void setRequestStorage(RequestStorage requestStorage) {
        this.requestStorage = requestStorage;
    }

}
