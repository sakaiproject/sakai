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

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;

import org.jsoup.nodes.Document;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentFilter;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.api.FormattedText;

/**
 * Simple filter that adds header and footer fragments to HTML pages, it can detect
 * to add HTML or be forced to/not.
 * 
 * @author buckett
 *
 */
@Slf4j
public class HtmlPageFilter implements ContentFilter {

	private static final String MATHJAX_SRC_PATH_SAKAI_PROP = "portal.mathjax.src.path";

	@Setter private EntityManager entityManager;
	@Setter private PreferencesService preferencesService;
	@Setter private ServerConfigurationService serverConfigurationService;
	@Setter private SessionManager sessionManager;
	@Setter private FormattedText formattedText;
	
	/** If <code>false</false> then this filter is disabled. */
	private boolean enabled = true;
	
	private String headerTemplate = 
"<html class=\"Mrphs-html {4}\">\n" +
"  <head>\n" +
"    <meta name=\"viewport\" content=\"width=device-width\">\n" +
"    <title>{2}</title>\n" +
"    <link href=\"{0}/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
"    <link href=\"{0}/{1}/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n" +
"    <script src=\"/library/js/headscripts.js\"></script>\n" +
"{3}"+
"    <style>body '{ padding: 5px !important; }'</style>\n" +
"  </head>\n" +
"  <body>\n";
	
	private String footerTemplate = "\n" +
"  </body>\n" +
"</html>\n";

	private JSONArray ext = new JSONArray();
	private	JSONArray jax = new JSONArray();
	private	Boolean defaultDelimiters = false;

	public String getMathJaxConfig() {
		// The HTML file generated from Resources can be used outside Sakai, so mathjax-config.js won't work.
		String [] mathJaxFormat = serverConfigurationService.getStrings("mathjax.config.format");
		JSONObject jsonMathjaxConfig = new JSONObject();

		if (mathJaxFormat == null) {
			log.error("No property for MathJax config was specified. Using LaTeX as default.");
			useDefaultFormat();
		} else {
			for (String format : mathJaxFormat) {

				switch (format) {
					case "LaTeX":
						useDefaultFormat();
						break;
					case "AsciiMath":
						ext.add("asciimath2jax.js");
						jax.add("input/AsciiMath");
						break;
					default:
						log.error(format + " is not a supported format." +
						" Check available options on Sakai default properties");
						break;
				}
			}
		}

		if (ext.isEmpty()) {
			log.error("None of the received formats match the supported ones. Using LaTeX as default.");
			useDefaultFormat();
		}

		jax.add("output/HTML-CSS");

		jsonMathjaxConfig.put("extensions", ext);
		jsonMathjaxConfig.put("jax", jax);
		jsonMathjaxConfig.put("messageStyle", "none");

		if (defaultDelimiters) {
			JSONArray delimiter1 = new JSONArray();
			delimiter1.add("$$");
			delimiter1.add("$$");
	
			JSONArray delimiter2 = new JSONArray();
			delimiter2.add("\\(");
			delimiter2.add("\\)");
	
			JSONArray delimiters = new JSONArray();
			delimiters.add(delimiter1);
			delimiters.add(delimiter2);
	
			JSONObject inlineMath = new JSONObject();
			inlineMath.put("inlineMath", delimiters);
			jsonMathjaxConfig.put("tex2jax", inlineMath);
		}

		String mathjaxConfig = jsonMathjaxConfig.toJSONString();
		mathjaxConfig = "MathJax.Hub.Config (" + mathjaxConfig + ");";

		return mathjaxConfig;
	}

	public void useDefaultFormat() {
		ext.add("tex2jax.js");
		jax.add("input/TeX");
		defaultDelimiters = true;
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
		Reference siteRef = entityManager.newReference(Entity.SEPARATOR+ SiteService.SITE_SUBTYPE+ Entity.SEPARATOR+ contentRef.getContext());
		Entity entity = siteRef.getEntity();
		
		String addHtml = content.getProperties().getProperty(ResourceProperties.PROP_ADD_HTML);
		
		String skinRepo = getSkinRepo();
		String siteSkin = getSiteSkin(entity);

		String thisUser = sessionManager.getCurrentSessionUserId();
		Preferences prefs = preferencesService.getPreferences(thisUser);
		String userTheme = StringUtils.defaultIfEmpty(prefs.getProperties(PreferencesService.USER_SELECTED_UI_THEME_PREFS).getProperty("theme"), "sakaiUserTheme-notSet");
		
		final boolean detectHtml = addHtml == null || addHtml.equals("auto");
		String title = getTitle(content);

		StringBuilder header = new StringBuilder();
		if (detectHtml) {
			String docType = serverConfigurationService.getString("content.html.doctype", "<!DOCTYPE html>");
			header.append(docType + "\n");
		}

		Document additionalScripts = new Document("");
		if (isMathJaxEnabled(entity)) {		
			additionalScripts.appendElement("script").attr("type", "text/x-mathjax-config").text(getMathJaxConfig());
			additionalScripts.appendElement("script").attr("type", "text/javascript").attr("src", serverConfigurationService.getString(MATHJAX_SRC_PATH_SAKAI_PROP));
		}
		additionalScripts.appendElement("script").attr("type", "text/javascript").attr("src", serverConfigurationService.getString("portal.include.extrahead", ""));
		header.append(MessageFormat.format(headerTemplate, skinRepo, siteSkin, title, additionalScripts.toString(), userTheme));

		return new WrappedContentResource(content, header.toString(), footerTemplate, detectHtml);
	}

	private String getTitle(final ContentResource content) {
		String title = content.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		if (title == null) {
			title = content.getId();
		}
		return formattedText.escapeHtml(title);
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

	/**
	 * Check if MathJax should be enabled for this site.
	 * @param entity The Site that the content is in.
	 * @return <code>true</code> if we should enabled MathJax
     */
	private boolean isMathJaxEnabled(Entity entity) {
		if (serverConfigurationService.getBoolean("portal.mathjax.enabled", true)) {
			if (entity instanceof Site) {
				Site site = (Site)entity;
				return Boolean.parseBoolean(site.getProperties().getProperty(Site.PROP_SITE_MATHJAX_ALLOWED));
			}
		}
		return false;
	}

}
