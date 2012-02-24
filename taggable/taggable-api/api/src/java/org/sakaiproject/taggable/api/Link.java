/**********************************************************************************
 * $URL: https://source.sakaiproject.org/contrib/syracuse/taggable/branches/oncourse_osp_enhancements/taggable-api/api/src/java/org/sakaiproject/taggable/api/Link.java $
 * $Id: Link.java 45892 2008-02-22 19:54:48Z chmaurer@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.taggable.api;

import org.sakaiproject.entity.api.Entity;

/**
 * A link between an activity and a tagCriteria.
 * 
 * @author The Sakai Foundation.
 */
public interface Link extends Entity {

	/**
	 * Value indicating that the link is exportable for an NCATE report.
	 */
	public static final int EXPORT_NCATE = 1;

	/**
	 * @return A reference for the activity.
	 */
	public String getActivityRef();

	/**
	 * @return An integer value indicating which reports this link can be
	 *         exported for.
	 */
	public int getExportString();

	/**
	 * @return The tagCriteriaRef.
	 */
	public String getTagCriteriaRef();

	/**
	 * @return The rationale for this Link.
	 */
	public String getRationale();

	/**
	 * @return The rubric for this link.
	 */
	public String getRubric();

	/**
	 * @return True if this link can be exported for a report, false otherwise.
	 */
	public boolean isExportable();

	/**
	 * @param reportMask
	 *            The type of report to check.
	 * @return True if this link can be exported for the given report, false
	 *         otherwise.
	 */
	public boolean isExportable(int reportMask);

	/**
	 * @return True if this link is locked, false otherwise.
	 */
	public boolean isLocked();

	/**
	 * @return True if this link is visible to users with the appropriate
	 *         permission, false otherwise.
	 */
	public boolean isVisible();

	/**
	 * @param reportMask
	 *            A report value to toggle in the export string.
	 */
	public void setExportFlag(int reportMask);

	/**
	 * @param tagCriteriaRef
	 *            The tagCriteriaRef.
	 */
	public void setTagCriteriaRef(String tagCriteriaRef);

	/**
	 * @param locked
	 *            True if this link is locked, false otherwise.
	 */
	public void setLocked(boolean locked);

	/**
	 * @param rationale
	 *            The rationale for this Link.
	 */
	public void setRationale(String rationale);

	/**
	 * @param rubric
	 *            The rubric for this link.
	 */
	public void setRubric(String rubric);

	/**
	 * @param visible
	 *            True if this link is visible to users with the appropriate
	 *            permission, false otherwise.
	 */
	public void setVisible(boolean visible);
}
