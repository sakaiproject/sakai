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

package org.sakaiproject.progress.impl;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;
//import org.apache.commons.math.stat.descriptive.SummaryStatistics;

import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.gradebook.GradingScale;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.progress.api.IGradebookService;
import org.sakaiproject.progress.api.Template;
import lombok.Setter;


public class GradebookServiceImpl implements IGradebookService, Comparable, Serializable {

	@Setter
	private GradebookService gradebookService;

	protected Gradebook gradebook;
	protected String gradebookUid;
	protected Long gradebookId;

	public Gradebook getGradebook(final String uid) throws GradebookNotFoundException {
		gradebook = (Gradebook)gradebookService.getGradebook(uid);
		gradebookUid=gradebook.getUid();
		gradebookId=gradebook.getId();

		return gradebook;
	}
	@Override
	public int compareTo(Object o) {
		return 0;
	}

	@Override
	public String getTitle() {
		return gradebook.getName();
	}

	@Override
	public void setTitle(String title) {

	}

	@Override
	public String getCreator() {
		return null;
	}

	@Override
	public void setCreator(String creator) {

	}

	@Override
	public String getCreatorEid() {
		return null;
	}

	@Override
	public void setCreatorEid(String creatorUserId) {

	}

	@Override
	public Timestamp getCreated() {
		return null;
	}

	@Override
	public void setCreated(Timestamp created) {

	}

	@Override
	public String getLastUpdater() {
		return null;
	}

	@Override
	public void setLastUpdater(String lastUpdater) {

	}

	@Override
	public String getLastUpdaterEid() {
		return null;
	}

	@Override
	public void setLastUpdaterEid(String lastUpdaterUserId) {

	}

	@Override
	public String getUpdatedDateTime() {
		return null;
	}

	@Override
	public Timestamp getLastUpdated() {
		return null;
	}

	@Override
	public void setLastUpdated(Timestamp lastUpdated) {

	}

	@Override
	public String getContext() {
		return null;
	}

	@Override
	public void setContext(String context) {

	}

	@Override
	public Set getStudents() {
		return null;
	}

	@Override
	public void setStudents(Set students) {

	}

	@Override
	public Template getTemplate() {
		return null;
	}

	@Override
	public void setFileReference(String fileReference) {

	}

	@Override
	public String getFileReference() {
		return null;
	}

	@Override
	public List getHeadings() {
		return null;
	}

	@Override
	public void setHeadings(List headings) {

	}

	@Override
	public Long getId() {
		return null;
	}

	@Override
	public void setId(Long id) {

	}

	@Override
	public Boolean getReleased() {
		return null;
	}

	@Override
	public void setReleased(Boolean released) {

	}

	@Override
	public String getHeadingsRow() {
		return null;
	}

	@Override
	public TreeMap getStudentMap() {
		return null;
	}

	@Override
	public boolean hasStudent(String username) {
		return false;
	}

	@Override
	public boolean getRelease() {
		return false;
	}

	@Override
	public void setRelease(boolean release) {

	}

	@Override
	public Boolean getReleaseStatistics() {
		return null;
	}

	@Override
	public void setReleaseStatistics(Boolean releaseStatistics) {

	}

	@Override
	public boolean getReleaseStats() {
		return false;
	}

	@Override
	public void setReleaseStats(boolean releaseStats) {

	}

	@Override
	public String getProperWidth(int column) {
		return null;
	}

	@Override
	public List getRawData(int column) {
		return null;
	}

	@Override
	public List getAggregateData(int column) throws Exception {
		return null;
	}

	@Override
	public String getFirstUploadedUsername() {
		return null;
	}

	@Override
	public void setFirstUploadedUsername(String username) {

	}

	@Override
	public List getUsernames() {
		return null;
	}

	@Override
	public void setUsernames(List<String> usernames) {

	}
}
