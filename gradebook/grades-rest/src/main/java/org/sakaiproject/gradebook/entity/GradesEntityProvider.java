/**
 * Copyright 2013 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.sakaiproject.gradebook.entity;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.azeckoski.reflectutils.ConversionUtils;
import org.azeckoski.reflectutils.transcoders.JSONTranscoder;
import org.azeckoski.reflectutils.transcoders.XMLTranscoder;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.EntityView.Method;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Redirectable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

import org.sakaiproject.gradebook.logic.ExternalLogic;

/**
 * Grades REST handler - processes and handles everything related to grades in Sakai
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com) (azeckoski @ vt.edu) (azeckoski @ unicon.net)
 */
public class GradesEntityProvider extends AbstractEntityProvider implements EntityProvider,
Resolvable, Outputable, Inputable, Describeable, ActionsExecutable, Redirectable, RequestAware {

    public static String PREFIX = "grades";

    private ExternalLogic externalLogic;
    public void setExternalLogic(ExternalLogic externalLogic) {
        this.externalLogic = externalLogic;
    }

    protected RequestGetter requestGetter;
    public void setRequestGetter(RequestGetter requestGetter) {
        this.requestGetter = requestGetter;
    }

    // custom actions

    @EntityCustomAction(action = "courses", viewKey = EntityView.VIEW_LIST)
    public Object getInstructorCourses(EntityView view) {
        String userId = externalLogic.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
                    "Only logged in users can access instructor courses listings");
        }
        String courseId = view.getPathSegment(2);
        List<Course> courses = externalLogic.getCoursesForInstructor(courseId);
        if (courses.isEmpty()) {
            throw new SecurityException(
                    "Only instructors can access instructor courses listings");
        }
        Object toEncode;
        if (courseId != null) {
            // get a single course
            Course c = courses.get(0);
            toEncode = c;
        } else {
            toEncode = courses;
        }
        return toEncode;
    }

    @EntityCustomAction(action = "students", viewKey = EntityView.VIEW_LIST)
    public List<Student> getCourseStudents(EntityView view) {
        String courseId = view.getPathSegment(2);
        if (courseId == null) {
            throw new IllegalArgumentException(
                    "valid courseId must be included in the URL /grades/students/{courseId}");
        }
        String userId = externalLogic.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
                    "Only logged in users can access student enrollment listings");
        }
        if (!externalLogic.isUserAdmin(userId) && !externalLogic.isUserInstructor(userId)) {
            throw new SecurityException("Only instructors can access course students listing");
        }
        List<Student> students = externalLogic.getStudentsForCourse(courseId);
        return students;
    }

    @EntityCustomAction(action = "gradebook", viewKey = EntityView.VIEW_LIST)
    public Gradebook getCourseGradebook(EntityView view) {
        String courseId = view.getPathSegment(2);
        if (courseId == null) {
            throw new IllegalArgumentException(
                    "valid courseId must be included in the URL /grades/gradebook/{courseId}");
        }
        String userId = externalLogic.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
                    "Only logged in users can access instructor courses listings");
        }
        if (!externalLogic.isUserAdmin(userId) && !externalLogic.isUserInstructor(userId)) {
            throw new SecurityException("Only instructors can access course gradebook");
        }
        Gradebook gradebook = externalLogic.getCourseGradebook(courseId, null);
        return gradebook;
    }

    @EntityCustomAction(action = "gradeitem", viewKey = "")
    public GradebookItem handleGradeItem(EntityView view) {
        String courseId = view.getPathSegment(2);
        if (courseId == null) {
            throw new IllegalArgumentException(
                    "valid courseId must be included in the URL /grades/gradeitem/{courseId}");
        }
        String userId = externalLogic.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException(
                    "Only logged in users can access instructor courses listings");
        }
        if (!externalLogic.isUserAdmin(userId) && !externalLogic.isUserInstructor(userId)) {
            throw new SecurityException("Only instructors can access course gradebook");
        }
        GradebookItem gbItemOut;
        if (Method.GET.toString().equalsIgnoreCase(view.getMethod())) {
            String gradeItemName = view.getPathSegment(3);
            if (gradeItemName == null) {
                throw new IllegalArgumentException(
                        "valid gbItemName must be included in the URL /grades/gradeitem/{courseId}/{gradeItemName}");
            }
            Gradebook gb = externalLogic.getCourseGradebook(courseId, gradeItemName);
            gbItemOut = gb.items.get(0);
        } else if (Method.POST.toString().equalsIgnoreCase(view.getMethod())
                || Method.PUT.toString().equalsIgnoreCase(view.getMethod())) {
            ServletRequest request = requestGetter.getRequest();
            if (request == null) {
                throw new IllegalStateException("Cannot get request to read data from");
            }
            String inputData;
            try {
                inputData = readerToString(request.getReader());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read the data from the request: " + e);
            }
            if (inputData == null || "".equals(inputData)) {
                throw new IllegalStateException("Must include the grade item and grades data for input (sent nothing)");
            }
            Map<String, Object> input;
            if (Formats.JSON.equals(view.getFormat())) {
                input = new JSONTranscoder().decode(inputData);
            } else {
                input = new XMLTranscoder().decode(inputData);
            }
            // loop through and get the data out and put it into a gradeitem
            ConversionUtils cvu = ConversionUtils.getInstance();
            String gbItemName = (String) input.get("name");
            GradebookItem gbItemIn = new GradebookItem(courseId, gbItemName);
            gbItemIn.pointsPossible = cvu.convert(input.get("pointsPossible"), Double.class);
            gbItemIn.dueDate = cvu.convert(input.get("dueDate"), Date.class);
            gbItemIn.eid = cvu.convert(input.get("externalID"), String.class);
            @SuppressWarnings("unchecked")
            List<Object> scores = cvu.convert(input.get("scores"), List.class);
            if (scores != null) {
                for (Object o : scores) {
                    GradebookItemScore score = cvu.convert(o, GradebookItemScore.class);
                    gbItemIn.scores.add( score );
                }
            }
            gbItemOut = externalLogic.saveGradebookItem(gbItemIn);
        } else {
            throw new EntityException("Method ("+view.getMethod()+") not supported", "grades/gradeitem", HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return gbItemOut;
    }

    public static String readerToString(BufferedReader br) {
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to get data from stream: " + e.getMessage(), e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                // ignore
            }
        }
        return sb.toString();
    }


    // standard methods
    // NOTE: this provider does not allow entity creation or deletion or list lookup because we are really dealing with gradebooks here

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.EntityProvider#getEntityPrefix()
     */
    public String getEntityPrefix() {
        return PREFIX;
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable#getEntity(org.sakaiproject.entitybroker.EntityReference)
     */
    public Object getEntity(EntityReference ref) {
        if (ref.getId() == null) {
            return new Gradebook(null);
        }
        Gradebook entity = externalLogic.getCourseGradebook(ref.getId(), null);
        if (entity != null) {
            return entity;
        }
        throw new IllegalArgumentException("Invalid id:" + ref.getId());
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable#getSampleEntity()
     */
    public Object getSampleEntity() {
        return new Gradebook(null);
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable#getHandledOutputFormats()
     */
    public String[] getHandledOutputFormats() {
        return new String[] { Formats.XML, Formats.JSON };
    }

    /* (non-Javadoc)
     * @see org.sakaiproject.entitybroker.entityprovider.capabilities.Inputable#getHandledInputFormats()
     */
    public String[] getHandledInputFormats() {
        return new String[] { Formats.HTML, Formats.XML, Formats.JSON };
    }

}
