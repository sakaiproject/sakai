/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-hbm/hbm/src/java/org/sakaiproject/taggable/impl/LinkImpl.java $
 * $Id: LinkImpl.java 45892 2008-02-22 19:54:48Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

import java.io.Serializable;
import java.util.Stack;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.taggable.api.Link;
import org.sakaiproject.taggable.api.TaggingManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LinkImpl implements Link, Serializable {

	protected static final long serialVersionUID = 5644487945580590177L;

	protected String id;

	protected Integer version;

	//protected TagCriteria tagCriteria;
	
	protected String tagCriteriaRef;

	protected String activityRef;

	protected String rationale;

	protected String rubric;

	protected int exportString;

	protected boolean visible;
	
	protected boolean locked;

	protected LinkImpl() {
	}

	public LinkImpl(String activityRef, String tagCriteriaRef, String rationale,
			String rubric, boolean visible, boolean locked) {
		this.tagCriteriaRef = tagCriteriaRef;
		this.activityRef = activityRef;
		this.rationale = rationale;
		this.rubric = rubric;
		this.visible = visible;
		this.locked = locked;
	}

	public String getId() {
		return id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	protected Integer getVersion() {
		return version;
	}

	protected void setVersion(Integer version) {
		this.version = version;
	}
/*
	public TagCriteria getTagCriteria() {
		if (tagCriteria == null && tagCriteriaRef != null) {
			Reference ref = EntityManager.newReference(tagCriteriaRef);
			TagCriteria newTC = new TagCriteria();
			
		}
		return tagCriteria;
	}

	public void setTagCriteria(TagCriteria tagCriteria) {
		this.tagCriteria = tagCriteria;
	}
	*/
	public String getTagCriteriaRef() {
		//if (tagCriteria != null && tagCriteriaRef == null)
		//	return tagCriteria.getReference();
		return tagCriteriaRef;
	}
	
	public void setTagCriteriaRef(String tagCriteriaRef) {
		this.tagCriteriaRef = tagCriteriaRef;
	}

	public String getActivityRef() {
		return activityRef;
	}

	protected void setActivityRef(String activityRef) {
		this.activityRef = activityRef;
	}

	public String getRationale() {
		return rationale;
	}

	public void setRationale(String rationale) {
		this.rationale = rationale;
	}

	public boolean isExportable() {
		return (exportString > 0);
	}

	public boolean isExportable(int reportMask) {
		return ((exportString & reportMask) == reportMask);
	}

	public int getExportString() {
		return exportString;
	}

	public void setExportFlag(int reportMask) {
		exportString ^= reportMask;
	}

	protected void setExportString(int exportString) {
		this.exportString = exportString;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getRubric() {
		return rubric;
	}

	public void setRubric(String rubric) {
		this.rubric = rubric;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ToStringBuilder sb = new ToStringBuilder(this);
		sb.append("id", id);
		sb.append("version", version);
		sb.append("tagCriteriaRef", tagCriteriaRef);
		sb.append("activityRef", activityRef);
		sb.append("rationale", rationale);
		sb.append("rubric", rubric);
		sb.append("exportString", exportString);
		sb.append("visible", visible);
		sb.append("locked", locked);
		return sb.toString();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LinkImpl)) {
			return false;
		}
		LinkImpl other = (LinkImpl) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(tagCriteriaRef,
				other.getTagCriteriaRef()).append(activityRef, other.getActivityRef())
				.isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(11, 13).append(tagCriteriaRef).append(activityRef)
				.toHashCode();
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference() {
		StringBuffer sb = new StringBuffer(TaggingManager.REFERENCE_ROOT);
		sb.append(Entity.SEPARATOR);
		sb.append(TaggingManager.LINK_REF);
		sb.append(Entity.SEPARATOR);
		sb.append(getTagCriteriaRef());
		sb.append(Entity.SEPARATOR);
		sb.append(getId());
		return sb.toString();
	}

	public String getUrl(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public Element toXml(Document arg0, Stack arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
