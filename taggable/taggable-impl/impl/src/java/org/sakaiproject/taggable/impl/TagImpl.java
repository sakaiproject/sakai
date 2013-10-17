/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-impl/impl/src/java/org/sakaiproject/taggable/impl/TagImpl.java $
 * $Id: TagImpl.java 45892 2008-02-22 19:54:48Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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
package org.sakaiproject.taggable.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.taggable.api.Link;
import org.sakaiproject.taggable.api.Tag;
import org.sakaiproject.taggable.api.TagList;
import org.sakaiproject.taggable.api.TaggingManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.taggable.api.TagColumn;
import org.sakaiproject.util.Validator;

public class TagImpl implements Tag
{
	private static final SiteService siteService;
	private static final TaggingManager taggingManager;

	static {
		siteService = (SiteService) ComponentManager
		.get("org.sakaiproject.site.api.SiteService");
		
		taggingManager = (TaggingManager) ComponentManager
		.get("org.sakaiproject.taggable.api.TaggingManager");
	}

	protected String tagCriteriaRef;
	private String activityRef;
	private String rubric;
	private String rationalle;
	private boolean exportable;
	private boolean visible;
	private Map<String, String> fieldMap = new HashMap<String, String>();

	public TagImpl() {
		;
	}

	public TagImpl(Link link) {
		this(link, taggingManager.createTagList());
	}
	
	public TagImpl(Link link, TagList subTags) {
		this.tagCriteriaRef = link.getTagCriteriaRef();
		this.activityRef = link.getActivityRef();
		this.rubric = link.getRubric();
		this.rationalle = link.getRationale();
		this.exportable = link.isExportable();
		this.visible = link.isVisible();
		
		Reference ref = EntityManager.newReference(tagCriteriaRef);
		Entity entity = ref.getEntity();
		
		initFieldMap(entity, ref.getContext());
		
	}

	public String getActivityRef() {
		return activityRef;
	}

	public Object getObject() {
		return tagCriteriaRef;
	}

	public String getField(TagColumn column) {
		return getField(column.getName());
	}

	protected String getField(String column) {
		String field = fieldMap.get(column);
		
		if (field == null)
			field = Validator.escapeHtml(TagListImpl.NA);
		
		return field;
	}

	public List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add(getField(TagList.CRITERIA));
		fields.add(getField(TagList.PARENT));
		fields.add(getField(TagList.WORKSITE));
		/*
		fields.add(getField(TagList.RUBRIC));
		fields.add(getField(TagList.RATIONALE));
		fields.add(getField(TagList.VISIBLE));
		fields.add(getField(TagList.EXPORTABLE));
		 */
		return fields;
	}

	private void initFieldMap(Entity entity, String refContext) {
		if (entity != null) {
			try
			{
				Site site = siteService.getSite(refContext);
				fieldMap.put(TagList.WORKSITE, Validator.escapeHtml(site.getTitle()));
			}
			catch (IdUnusedException e)
			{
				//couldn't find the site, so just leave the title blank
				fieldMap.put(TagList.WORKSITE, "");
			}

			fieldMap.put(TagList.PARENT, Validator.escapeHtml((String)entity.getProperties().get(TagList.PARENT)));

			String field;
			String url = entity.getUrl();
			if (url != null) {
// ONC-3722 - the below line injects an earlier version of jQuery (1.2.1) that interferes with A2's tooltip.
//			  commenting out.
//				field = (String)entity.getProperties().get(TagList.THICKBOX_INCLUDE);
				field ="<a href=\"" + url + "&TB_iframe=true&height=500&width=700\" class=\"thickbox\">";
				field += Validator.escapeHtml((String)entity.getProperties().get(TagList.CRITERIA));
				field += "</a>";
			}
			else
				field = Validator.escapeHtml((String)entity.getProperties().get(TagList.CRITERIA));
			
			fieldMap.put(TagList.CRITERIA, field);
			
			
			fieldMap.put(TagList.RUBRIC, Validator.escapeHtml(rubric));
			fieldMap.put(TagList.RATIONALE, Validator.escapeHtml(rationalle));
			fieldMap.put(TagList.VISIBLE, Validator.escapeHtml(String.valueOf(visible)));
			fieldMap.put(TagList.EXPORTABLE, Validator.escapeHtml(String.valueOf(exportable)));
		}
	}

}
