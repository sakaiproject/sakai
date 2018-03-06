/**
 * Copyright (c) 2010-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.entityprovider;

import java.util.Map;
import java.util.List;

import lombok.Setter;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityParameters;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;

import org.sakaiproject.util.foorm.FoormUtil;

import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import org.sakaiproject.exception.IdUnusedException;


public class LTIEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Outputable, ActionsExecutable, Describeable
{

        public final static String PREFIX = "lti";
        public final static String TOOL_ID = "sakai.lti";

        private final static String ADMIN_SITE = "!admin";

	@Setter
        protected static LTIService ltiService = null;

	@Setter
        protected static SiteService siteService = null;

        public String getEntityPrefix() {
                return PREFIX;
        }

        public String getAssociatedToolId() {
                return TOOL_ID;
        }

        public String[] getHandledOutputFormats() {
            return new String[] { Formats.TXT ,Formats.JSON, Formats.HTML};
        }

        @EntityCustomAction(action = "deploys", viewKey = EntityView.VIEW_SHOW)
	@EntityParameters(accepted = { "order", "first", "last" })
        public LTIListEntity handleDeploysCollection(EntityView view, Map<String, Object> params) {
		String siteId = view.getEntityReference().getId();
		getSiteById(siteId);
		requireAdminUser(siteId);
		boolean inAdmin = inAdmin(siteId);
		int [] paging = parsePaging(params);

		// Search is not yet safely implemented
                List<Map<String,Object>> deploys = ltiService.getDeploysDao(null, (String)params.get("order"),
			paging[0], paging[1], siteId, inAdmin);
		adjustList(deploys, inAdmin, siteId, "deploy");
		LTIListEntity retval = new LTIListEntity (deploys);
                return retval;
        }

        @EntityCustomAction(action = "deploy", viewKey = "")
        public Map<String,Object> handleDeploy(EntityView view) {
                String siteId = view.getPathSegment(2);
                String deployId = view.getPathSegment(3);
		getSiteById(siteId);
		requireAdminUser(siteId);
		boolean inAdmin = inAdmin(siteId);
                Map<String,Object> deploy = ltiService.getDeployDao(new Long(deployId), siteId, inAdmin);
		adjustMap(deploy, inAdmin, siteId, "deploy");
                return deploy;
        }


        @EntityCustomAction(action = "tools", viewKey = EntityView.VIEW_SHOW)
	@EntityParameters(accepted = { "order", "first", "last" })
        public LTIListEntity handleToolsCollection(EntityView view, Map<String, Object> params) {
		String siteId = view.getEntityReference().getId();
		getSiteById(siteId);
		requireMemberUser(siteId);
		boolean inAdmin = inAdmin(siteId);
		int [] paging = parsePaging(params);

		// Search is not yet safely implemented
                List<Map<String,Object>> tools = ltiService.getToolsDao(null, (String)params.get("order"),
			paging[0], paging[1], siteId, inAdmin);
		adjustList(tools, inAdmin, siteId, "tool");
		LTIListEntity retval = new LTIListEntity (tools);
                return retval;
        }

        @EntityCustomAction(action = "tool", viewKey = "")
        public Map<String,Object> handleTool(EntityView view) {
                String siteId = view.getPathSegment(2);
                String toolId = view.getPathSegment(3);
		getSiteById(siteId);
		requireMemberUser(siteId);
		boolean inAdmin = inAdmin(siteId);
                Map<String,Object> tool = ltiService.getToolDao(new Long(toolId), siteId, inAdmin);
		adjustMap(tool, inAdmin, siteId, "tool");
                return tool;
        }

        @EntityCustomAction(action = "contents", viewKey = EntityView.VIEW_SHOW)
	@EntityParameters(accepted = { "order", "first", "last" })
        public LTIListEntity handleContentsCollection(EntityView view, Map<String, Object> params) {
		String siteId = view.getEntityReference().getId();
		getSiteById(siteId);
		requireMemberUser(siteId);
		boolean inAdmin = inAdmin(siteId);
		int [] paging = parsePaging(params);

		// Search is not yet safely implemented
                List<Map<String,Object>> contents = ltiService.getContentsDao(null, (String)params.get("order"),
			paging[0], paging[1], siteId, inAdmin);
		adjustList(contents, inAdmin, siteId, "content");
		LTIListEntity retval = new LTIListEntity (contents);
                return retval;
        }

        @EntityCustomAction(action = "content", viewKey = "")
        public Map<String,Object> handleContent(EntityView view) {
                String siteId = view.getPathSegment(2);
                String contentId = view.getPathSegment(3);
		getSiteById(siteId);
		requireMemberUser(siteId);
		boolean inAdmin = inAdmin(siteId);
                Map<String,Object> content = ltiService.getContentDao(new Long(contentId), siteId, inAdmin);
		adjustMap(content, inAdmin, siteId, "content");
                return content;
        }

	public int [] parsePaging(Map<String, Object> params) {
		String sfirst = (String)params.get("first");
		String slast = (String)params.get("last");

		int first = 0;
		int last = 0;
		if ( sfirst != null ) first = Integer.parseInt(sfirst);
		if ( slast != null ) last = Integer.parseInt(slast);
		if ( last < first ) {
			first = 0;
			last = 0;
		}
		int [] retval = {first, last};
		return retval;
	}

	// Filter fields not to be shown
	protected void adjustList(List<Map<String, Object>> things, boolean inAdmin, String siteId, String kind) {
		for (Map<String, Object> thing : things) {
			adjustMap(thing, inAdmin, siteId, kind);
		}
	}

	void adjustMap(Map<String, Object> thing, boolean inAdmin, String siteId, String kind) {
		Long id = FoormUtil.getLongNull(thing.get(LTIService.LTI_ID));
		if ( id != null && id >= 0 ) {
			thing.put("@id","/lti/"+kind+"/"+siteId+"/"+id+".json");
		}

		for (String key : thing.keySet()) {
			if ( key.startsWith("allow") ) continue;
			if (key.contains("secret")) {
				thing.put(key, LTIService.SECRET_HIDDEN);
			}
			if (key.contains("password")) {
				thing.put(key, LTIService.SECRET_HIDDEN);
			}
		}
	}

	protected String getLoggedInUserReference() {
		String userReference = developerHelperService.getCurrentUserReference();
		if (userReference == null) {
			throw new SecurityException(
				"This action is not accessible to anon and there is no current user.");
		}
		return userReference;
	}

	protected void requireMemberUser(String siteId) {
		if (!isMember(siteId)) {
			throw new SecurityException(
					"The requested site is not accessible to the current user.");
		}

	}

	protected void requireAdminUser(String siteId) {
                if (!inAdmin(siteId)) {
                        throw new SecurityException(
                                "The requested site is not accessible to the current user.");
                }

	}

	protected boolean inAdmin(String siteId) {
		if ( ! ADMIN_SITE.equals(siteId) ) return false;
		return isMaintain(siteId);
	}

	protected boolean isMaintain(String siteId) {
		return developerHelperService.isUserAllowedInEntityReference(getLoggedInUserReference(), "site.upd", "/site/"+siteId);
	}

	protected boolean isMember(String siteId) {
		return developerHelperService.isUserAllowedInEntityReference(getLoggedInUserReference(), "site.visit", "/site/"+siteId);
	}

	protected Site getSiteById(String siteId) {
		Site site;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("Cannot find site by siteId: " + siteId, e);
		}
		return site;
	}
}
