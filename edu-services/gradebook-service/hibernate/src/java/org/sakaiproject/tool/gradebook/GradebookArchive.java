/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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

package org.sakaiproject.tool.gradebook;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

/**
 * Models a gradebook and all of its dependent objects, which can all be
 * serialized as xml for archiving.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
@Slf4j
public class GradebookArchive {
    private Gradebook gradebook;
    private GradeMapping selectedGradeMapping;
    private Collection gradeMappings;
    private CourseGrade courseGrade;
    private Collection assignments;

    public GradebookArchive() {
        // Allows for creating the archive, then populating it via readArchive()
    }

	/**
	 * @param gradebook
	 * @param selectedGradeMapping
	 * @param gradeMappings
	 * @param courseGrade
	 * @param assignments
	 */
	public GradebookArchive(Gradebook gradebook,
			GradeMapping selectedGradeMapping, Collection gradeMappings,
			CourseGrade courseGrade, Collection assignments) {
		super();
		this.gradebook = gradebook;
		this.selectedGradeMapping = selectedGradeMapping;
		this.gradeMappings = gradeMappings;
		this.courseGrade = courseGrade;
		this.assignments = assignments;
	}

    /**
     * Serializes this gradebook archive into an xml document
     */
    public String archive() {
        if(log.isDebugEnabled()) log.debug("GradebookArchive.archive() called");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(baos));
        encoder.writeObject(this);
        encoder.flush();
        String xml = baos.toString();
        if(log.isDebugEnabled()) log.debug("GradebookArchive.archive() finished");
        return xml;
    }

    /**
     * Read a gradebook archive from an xml input stream.
     *
     * @param xml The input stream containing the serialized gradebook archive
     * @return A gradebook archive object modeling the data in the xml stream
     */
    public void readArchive(String xml) {
        ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
        XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(in));
        GradebookArchive archive = (GradebookArchive)decoder.readObject();
        decoder.close();
        this.gradebook = archive.getGradebook();
        this.courseGrade = archive.getCourseGrade();
        this.assignments = archive.getAssignments();
    }

    public Collection getAssignments() {
		return assignments;
	}
	public void setAssignments(Collection assignments) {
		this.assignments = assignments;
	}
	public CourseGrade getCourseGrade() {
		return courseGrade;
	}
	public void setCourseGrade(CourseGrade courseGrade) {
		this.courseGrade = courseGrade;
	}
	public Gradebook getGradebook() {
		return gradebook;
	}
	public void setGradebook(Gradebook gradebook) {
		this.gradebook = gradebook;
	}
	public Collection getGradeMappings() {
		return gradeMappings;
	}
	public void setGradeMappings(Collection gradeMappings) {
		this.gradeMappings = gradeMappings;
	}
	public GradeMapping getSelectedGradeMapping() {
		return selectedGradeMapping;
	}
	public void setSelectedGradeMapping(GradeMapping selectedGradeMapping) {
		this.selectedGradeMapping = selectedGradeMapping;
	}
}



