/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
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
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;

public class GradebookImpl implements Gradebook, Comparable, Serializable {

	protected String title;

	protected String creator;

	protected Timestamp created;

	protected String lastUpdater;

	protected DateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm");

	protected Timestamp lastUpdated;

	protected String context;

	protected Set students = new TreeSet();

	protected Template template;

	protected List headings = new ArrayList();

	protected Long id;

	protected Integer lockId;

	protected Boolean released = new Boolean(false);

	protected Boolean releaseStatistics = new Boolean(false);

	private static String units = "px";

	public GradebookImpl() {

	}

	public GradebookImpl(String title, String creator, String context,
			List headings, SortedSet students, Template template) {
		Timestamp now = new Timestamp(new Date().getTime());
		this.title = title;
		this.creator = creator;
		this.created = now;
		this.lastUpdater = creator;
		this.lastUpdated = now;
		this.context = context;
		if (headings != null) {
			this.headings = headings;
		} else {
			this.headings = new ArrayList();
		}
		if (students != null) {
			this.students = students;
		} else {
			this.students = new TreeSet();
		}
		this.template = template;
	}

	public Integer getLockId() {
		return lockId;
	}

	public void setLockId(Integer lockId) {
		this.lockId = lockId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public String getLastUpdater() {
		return lastUpdater;
	}

	public void setLastUpdater(String lastUpdater) {
		this.lastUpdater = lastUpdater;
	}

	public String getUpdatedDateTime() {
		return dateFormat.format((Date) lastUpdated);

	}

	public Timestamp getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Timestamp lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public Set getStudents() {
		return students;
	}

	public void setStudents(Set students) {
		if (students == null) {
			this.students = new TreeSet();
		} else {
			this.students = new TreeSet(students);
		}
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public List getHeadings() {
		return headings;
	}

	public void setHeadings(List headings) {
		if (headings == null) {
			this.headings = new ArrayList();
		} else {
			this.headings = headings;
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getReleased() {
		return released;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	public boolean getRelease() {
		return released.booleanValue();
	}

	public void setRelease(boolean release) {
		this.released = new Boolean(release);
	}

	public Boolean getReleaseStatistics() {
		return releaseStatistics;
	}

	public void setReleaseStatistics(Boolean releaseStatistics) {
		this.releaseStatistics = releaseStatistics;
	}

	public boolean getReleaseStats() {
		return releaseStatistics.booleanValue();
	}

	public void setReleaseStats(boolean releaseStats) {
		this.releaseStatistics = new Boolean(releaseStats);
	}

	public String getHeadingsRow() {
		List h2 = new ArrayList(headings);
		h2.remove(0);
		StringBuffer headingBuffer = new StringBuffer();
		// headingBuffer.append("<table><tr>");
		int totalWidth = 0;

		Iterator jj = h2.iterator();
		int ii = 0;
		while (jj.hasNext()) {
			String current = (String) jj.next();
			String width = getProperWidth(ii);
			int iwidth = Integer.parseInt(width.substring(0, width.length() - 2));
			totalWidth += iwidth;
			headingBuffer.append("<td width='");
			headingBuffer.append(width);
			headingBuffer.append("' style='min-width: ");
			headingBuffer.append(width);
			headingBuffer.append("; width: ");
			headingBuffer.append(width);
			headingBuffer.append(";' >");
			headingBuffer.append(current);
			headingBuffer.append("</td>");
			ii++;
		}
		StringBuffer newBuffer = new StringBuffer();
		newBuffer.append("<table width='");
		newBuffer.append(totalWidth);
		newBuffer.append("px' style='min-width: ");
		newBuffer.append(totalWidth);
		newBuffer.append("px; width: ");
		newBuffer.append(totalWidth);
		newBuffer.append("px;' ><tr>");
		newBuffer.append(headingBuffer);

		newBuffer.append("</tr></table>");
		return newBuffer.toString();
	}

	public int compareTo(Object other) {
		if (this == other)
			return 0;
		final Gradebook that = (Gradebook) other;

		return this.getTitle().compareTo(that.getTitle());
	}

	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof Gradebook))
			return false;
		final Gradebook that = (Gradebook) other;

		return this.getTitle().equals(that.getTitle());
	}

	public int hashCode() {
		return getTitle().hashCode();
	}

	public boolean hasStudent(String username) {
		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			if (((StudentGrades) iter.next()).getUsername().equals(username)) {
				return true;
			}
		}
		return false;
	}

	public boolean getHasGrades() {
		if (students != null && students.size() != 0) {
			return true;
		}
		return false;
	}

	public boolean getHasTemplate() {
		if (template != null) {
			return true;
		}
		return false;
	}

	public String getProperWidth(int column) {
		int maxWidth = 50;
		int tops = 150;
		try {
			List h2 = new ArrayList(headings);
			h2.remove(0);
			int hchars = ((String) h2.get(column)).length();
			int hwidth = hchars * 10;
			if (hwidth >= tops) {
				maxWidth = tops;
				return "" + maxWidth + units;
			}
			if (hwidth >= maxWidth) {
				maxWidth = hwidth;
			}
		} catch (Exception exception) {
		}
		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades sg = (StudentGrades) iter.next();
			try {
				int chars = ((String) sg.getGrades().get(column)).length();
				int width = chars * 10;
				if (width >= tops) {
					maxWidth = tops;
					return "" + maxWidth + units;
				}
				if (width >= maxWidth) {
					maxWidth = width;
				}
			} catch (Exception exception) {
			}
		}
		return "" + maxWidth + units;
	}

	public List getRawData(int column) {

		List rawData = new ArrayList();

		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades sg = (StudentGrades) iter.next();
			try {
				rawData.add(new Pair(sg.getUsername(), sg.getGrades().get(column)));
			} catch (IndexOutOfBoundsException exception) {
				rawData.add(new Pair(sg.getUsername(), ""));
			}

		}

		return rawData;
	}

	public List getAggregateData(int column) throws Exception {
		List aggregateData = new ArrayList();
		SummaryStatistics stats = SummaryStatistics.newInstance();
		int blanks = 0;

		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades sg = (StudentGrades) iter.next();
			try {
				String value = (String) sg.getGrades().get(column);
				if ("".equals(value.trim())) {
					// TODO: do blanks count as zeros for stats?
					// stats.addValue(0);
					blanks++;
				} else {
					stats.addValue(Double.parseDouble(value));
				}
			} catch (IndexOutOfBoundsException exception) {
				blanks++;
			}

		}
		aggregateData.add(new Pair("Average", new Double(stats.getMean())));
		aggregateData.add(new Pair("Std. Dev.", new Double(stats
				.getStandardDeviation())));
		aggregateData.add(new Pair("Highest", new Double(stats.getMax())));
		aggregateData.add(new Pair("Lowest", new Double(stats.getMin())));
		aggregateData.add(new Pair("Range", new Double(stats.getMax()
				- stats.getMin())));
		aggregateData.add(new Pair("N=count(non-blank)", new Double(stats.getN())));
		aggregateData.add(new Pair("count(blank)", new Integer(blanks)));

		return aggregateData;
	}

	public StudentGrades studentGrades(String username) {
		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades current = (StudentGrades) iter.next();
			if (current.getUsername().equals(username)) {
				return current;
			}
		}
		return null;
	}

	public TreeMap getStudentMap() {
		TreeMap studentMap = new TreeMap();

		Iterator iter = getStudents().iterator();
		while (iter.hasNext()) {
			StudentGrades ss = (StudentGrades) iter.next();
			studentMap.put(ss.getUsername(), ss.getUsername());
		}
		return studentMap;
	}

}
