/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Sakai Foundation
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
package org.sakaiproject.content.impl;

import java.text.MessageFormat;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentFilter;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.util.Validator;

/**
 * Simple filter that adds header and footer fragments to HTML pages, it can detect
 * to add HTML or be forced to/not.
 * 
 * @author buckett
 *
 */
public class HtmlPageFilter implements ContentFilter {

	private EntityManager entityManager;
	
	private ServerConfigurationService serverConfigurationService;
	
	/** If <code>false</false> then this filter is disabled. */
	private boolean enabled = true;
	
	private String headerTemplate = 
"<html>\n" +
"  <head>\n" +
"    <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" /> \n" +
"    <title>{2}</title>\n" +
"    <link href=\"{0}/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
"    <link href=\"{0}/{1}/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
"    <script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/js/headscripts.js\"></script>\n" +
"    <style>body '{ padding: 5px !important; }'</style>\n" +
"  </head>\n" +
"  <body>\n";
	
	private String footerTemplate = "\n" +
"  </body>\n" +
"</html>\n";

	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}
	
	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public void setHeaderTemplate(String headerTemplate) {
		this.headerTemplate = headerTemplate;
	}

	public void setFooterTemplate(String footerTemplate) {
		this.footerTemplate = footerTemplate;
	}

	public boolean isFiltered(ContentResource resource) {
		String addHtml = resource.getProperties().getProperty(ResourceProperties.PROP_ADD_HTML);
		return enabled && ("text/html".equals(resource.getContentType())) && ((addHtml == null) || (!addHtml.equals("no") || addHtml.equals("yes")));
	}

	public ContentResource wrap(final ContentResource content) {
		if (!isFiltered(content)) {
			return content;
		}
		Reference contentRef = entityManager.newReference(content.getReference());
		Reference siteRef = entityManager.newReference(contentRef.getContext());
		Entity entity = siteRef.getEntity();
		
		String addHtml = content.getProperties().getProperty(ResourceProperties.PROP_ADD_HTML);
		
		String skinRepo = getSkinRepo();
		String siteSkin = getSiteSkin(entity);
		
		final boolean detectHtml = addHtml == null || addHtml.equals("auto");
		String title = getTitle(content);
		final String header = MessageFormat.format(headerTemplate, skinRepo, siteSkin, title);
		final String footer = footerTemplate;
		
		return new WrappedContentResource(content, header, footer, detectHtml);
	}

	private String getTitle(final ContentResource content) {
		String title = content.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if (title == null) {
			title = content.getId();
		}
		return Validator.escapeHtml(title);
	}

	private String getSkinRepo() {
		final String skinRepo = serverConfigurationService.getString("skin.repo", "/library/skins");
		return skinRepo;
	}

	private String getSiteSkin(Entity entity) {
		String siteSkin = serverConfigurationService.getString("skin.default"); 
		if (entity instanceof Site) {
			Site site =(Site)entity;
			if (site.getSkin() != null && site.getSkin().length() > 0) {
				siteSkin = site.getSkin();
			}
		}

		return siteSkin;
	}

}
