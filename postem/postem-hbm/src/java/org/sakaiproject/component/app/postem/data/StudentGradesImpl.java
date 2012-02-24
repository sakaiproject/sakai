/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.component.app.postem.data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;

public class StudentGradesImpl implements StudentGrades, Comparable,
		Serializable {
	protected Gradebook gradebook;

	protected String username;

	protected List grades = new ArrayList();

	protected DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm");

	protected Timestamp lastChecked;

	protected Long id;

	protected Integer lockId;

	public StudentGradesImpl() {

	}

	public StudentGradesImpl(String username, List grades) {
	    // ensure the usernames are trimmed and lowercase
		this.username = username.trim().toLowerCase();
		this.grades = grades;
	}

	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public Gradebook getGradebook() {
		return gradebook;
	}

	public void setGradebook(Gradebook gradebook) {
		this.gradebook = gradebook;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username.trim();
	}

	public List getGrades() {
		return grades;
	}

	public void setGrades(List grades) {
		this.grades = grades;
	}

	public String getCheckDateTime() {
		if (lastChecked == null) {
			return "never";
		}
		return dateFormat.format((Date) lastChecked);
	}

	public Timestamp getLastChecked() {
		return lastChecked;
	}

	public void setLastChecked(Timestamp lastChecked) {
		this.lastChecked = lastChecked;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int compareTo(Object other) {
		if (this == other)
			return 0;
		final StudentGrades that = (StudentGrades) other;

		return this.getUsername().compareTo(that.getUsername());
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof StudentGrades))
			return false;
		final StudentGrades that = (StudentGrades) other;

		return this.getUsername().equals(that.getUsername());
	}

	public int hashCode() {
		return getUsername().hashCode();
	}

	public boolean getReadAfterUpdate() {
		if (lastChecked == null) {
			return false;
		}
		return getLastChecked().after(gradebook.getLastUpdated());
	}

	/**
	 * Formats the grades for display, independently of the JSF display. If a
	 * {@link Template} exists for the parent gradebook, that template's
	 * fillGrades method is used. Otherwise, the grades are formatted into a plain
	 * old table.
	 * <p>
	 * This is a bad method for including display code within it; however, I do
	 * this for a simple reason: we're already including display code at this
	 * level via the template.
	 * <p>
	 * The prettier eventual solution will be to inject a default template via the
	 * controller, or possibly in the manager class (using a defaultTemplate
	 * property). This works for the quick and dirty now.
	 */
	public String formatGrades() {
		if (gradebook.getTemplate() == null) {

			List h2 = new ArrayList(gradebook.getHeadings());

			StringBuilder gradeBuffer = new StringBuilder();
			gradeBuffer.append("<table class=\"itemSummary\">");

			if (h2.size() != 0) {
				gradeBuffer.append("<tr><th scope=\"row\">" + StringEscapeUtils.escapeHtml(h2.get(0).toString()) + "</th><td>");
				h2.remove(0);
				gradeBuffer.append(StringEscapeUtils.escapeHtml(getUsername()));
				gradeBuffer.append("</td></tr>");
				Iterator ii = h2.iterator();
				Iterator jj = grades.iterator();

				while (ii.hasNext()) {
					gradeBuffer.append("<tr><th scope=\"row\">");
					gradeBuffer.append(StringEscapeUtils.escapeHtml((String) ii.next()));
					gradeBuffer.append("</th><td>");
					gradeBuffer.append(StringEscapeUtils.escapeHtml((String) jj.next()));
					gradeBuffer.append("</td></tr>");
				}
			} else {
				gradeBuffer.append("<tr><td>");
				gradeBuffer.append(StringEscapeUtils.escapeHtml(getUsername()));
				gradeBuffer.append("</td></tr>");
				Iterator jj = grades.iterator();
				while (jj.hasNext()) {
					gradeBuffer.append("<tr><td>");
					gradeBuffer.append(StringEscapeUtils.escapeHtml((String) jj.next()));
					gradeBuffer.append("</td></tr>");
				}
			}
			gradeBuffer.append("</table>");
			return gradeBuffer.toString();
		} else {
			return gradebook.getTemplate().fillGrades(this);
		}
	}

	public String getGradesRow() {
		StringBuilder gradeBuffer = new StringBuilder();
		// gradeBuffer.append("<table><tr>");
		int totalWidth = 0;

		Iterator jj = grades.iterator();
		int ii = 0;
		while (jj.hasNext()) {
			String current = (String) jj.next();
			String width = gradebook.getProperWidth(ii);
			int iwidth = Integer.parseInt(width.substring(0, width.length() - 2));
			totalWidth += iwidth;
			/*gradeBuffer.append("<td width='");
			gradeBuffer.append(width);
			gradeBuffer.append("' style='min-width: ");
			gradeBuffer.append(width);
			gradeBuffer.append("; width: ");
			gradeBuffer.append(width);
			gradeBuffer.append(";' >");*/
			gradeBuffer.append("<td style=\"padding:0.6em;\">");
			gradeBuffer.append(StringEscapeUtils.escapeHtml(current));
			gradeBuffer.append("</td>");
			ii++;
		}
		/*StringBuilder newBuffer = new StringBuilder();
		newBuffer.append("<table width='");
		newBuffer.append(totalWidth);
		newBuffer.append("px' style='min-width: ");
		newBuffer.append(totalWidth);
		newBuffer.append("px; width: ");
		newBuffer.append(totalWidth);
		newBuffer.append("px;' ><tr>");
		newBuffer.append(gradeBuffer);

		newBuffer.append("</tr></table>");
		newBuffer.append("</tr>");*/
		return gradeBuffer.toString();
	}

}
