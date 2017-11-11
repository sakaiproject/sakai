/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.component.gradebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.service.gradebook.shared.GradebookExistsException;
import org.sakaiproject.service.gradebook.shared.GradebookFrameworkService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.GradingScaleDefinition;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradePointsMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingScale;
import org.sakaiproject.tool.gradebook.LetterGradeMapping;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.LetterGradePlusMinusMapping;
import org.sakaiproject.tool.gradebook.PassNotPassMapping;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradebookFrameworkServiceImpl extends BaseHibernateManager implements GradebookFrameworkService {

	public static final String UID_OF_DEFAULT_GRADING_SCALE_PROPERTY = "uidOfDefaultGradingScale";
	
	public static final String PROP_COURSE_POINTS_DISPLAYED = "gradebook.coursepoints.displayed";
	public static final String PROP_COURSE_GRADE_DISPLAYED = "gradebook.coursegrade.displayed";
	public static final String PROP_ASSIGNMENTS_DISPLAYED = "gradebook.assignments.displayed";

	public void addGradebook(final String uid, final String name) {
        if(isGradebookDefined(uid)) {
            log.warn("You can not add a gradebook with uid=" + uid + ".  That gradebook already exists.");
            throw new GradebookExistsException("You can not add a gradebook with uid=" + uid + ".  That gradebook already exists.");
        }
        if (log.isDebugEnabled()) log.debug("Adding gradebook uid=" + uid + " by userUid=" + getUserUid());

        createDefaultLetterGradeMapping(getHardDefaultLetterMapping());
        
        getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            // Get available grade mapping templates.
            List gradingScales = session.createQuery("from GradingScale as gradingScale where gradingScale.unavailable=false").list();

            // The application won't be able to run without grade mapping
            // templates, so if for some reason none have been defined yet,
            // do that now.
            if (gradingScales.isEmpty()) {
                if (log.isInfoEnabled()) log.info("No Grading Scale defined yet. This is probably because you have upgraded or you are working with a new database. Default grading scales will be created. Any customized system-wide grade mappings you may have defined in previous versions will have to be reconfigured.");
                gradingScales = GradebookFrameworkServiceImpl.this.addDefaultGradingScales(session);
            }

            // Create and save the gradebook
            Gradebook gradebook = new Gradebook(name);
            gradebook.setUid(uid);
            session.save(gradebook);

            // Create the course grade for the gradebook
            CourseGrade cg = new CourseGrade();
            cg.setGradebook(gradebook);
            session.save(cg);

            // According to the specification, Display GradebookAssignment Grades is
            // on by default, and Display course grade is off. But can be overridden via properties


              Boolean propAssignmentsDisplayed = serverConfigurationService.getBoolean(PROP_ASSIGNMENTS_DISPLAYED,true);
              gradebook.setAssignmentsDisplayed(propAssignmentsDisplayed);

              Boolean propCourseGradeDisplayed = serverConfigurationService.getBoolean(PROP_COURSE_GRADE_DISPLAYED,false);
              gradebook.setCourseGradeDisplayed(propCourseGradeDisplayed);

               Boolean propCoursePointsDisplayed = serverConfigurationService.getBoolean(PROP_COURSE_POINTS_DISPLAYED,false);
               gradebook.setCoursePointsDisplayed(propCoursePointsDisplayed);

            String defaultScaleUid = GradebookFrameworkServiceImpl.this.getPropertyValue(UID_OF_DEFAULT_GRADING_SCALE_PROPERTY);

            // Add and save grade mappings based on the templates.
            GradeMapping defaultGradeMapping = null;
            Set gradeMappings = new HashSet();
            for (Iterator iter = gradingScales.iterator(); iter.hasNext();) {
                GradingScale gradingScale = (GradingScale)iter.next();
                GradeMapping gradeMapping = new GradeMapping(gradingScale);
                gradeMapping.setGradebook(gradebook);
                session.save(gradeMapping);
                gradeMappings.add(gradeMapping);
                if (gradingScale.getUid().equals(defaultScaleUid)) {
                    defaultGradeMapping = gradeMapping;
                }
            }

            // Check for null default.
            if (defaultGradeMapping == null) {
                defaultGradeMapping = (GradeMapping)gradeMappings.iterator().next();
                if (log.isWarnEnabled()) log.warn("No default GradeMapping found for new Gradebook=" + gradebook.getUid() + "; will set default to " + defaultGradeMapping.getName());
            }
            gradebook.setSelectedGradeMapping(defaultGradeMapping);

            // The Hibernate mapping as of Sakai 2.2 makes this next
            // call meaningless when it comes to persisting changes at
            // the end of the transaction. It is, however, needed for
            // the mappings to be seen while the transaction remains
            // uncommitted.
            gradebook.setGradeMappings(gradeMappings);

            gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
            gradebook.setCategory_type(GradebookService.CATEGORY_TYPE_NO_CATEGORY);

            //SAK-29740 make backwards compatible
            gradebook.setCourseLetterGradeDisplayed(true);

            gradebook.setCourseAverageDisplayed(true);

            // Update the gradebook with the new selected grade mapping
            session.update(gradebook);

            return null;

        });
	}

    private List addDefaultGradingScales(Session session) throws HibernateException {
    	List gradingScales = new ArrayList();

    	// Base the default set of templates on the old
    	// statically defined GradeMapping classes.
    	GradeMapping[] oldGradeMappings = {
    		new LetterGradeMapping(),
    		new LetterGradePlusMinusMapping(),
    		new PassNotPassMapping(),
    		new GradePointsMapping()
    	};

    	for (int i = 0; i < oldGradeMappings.length; i++) {
    		GradeMapping sampleMapping = oldGradeMappings[i];
    		sampleMapping.setDefaultValues();
			GradingScale gradingScale = new GradingScale();
			String uid = sampleMapping.getClass().getName();
			uid = uid.substring(uid.lastIndexOf('.') + 1);
			gradingScale.setUid(uid);
			gradingScale.setUnavailable(false);
			gradingScale.setName(sampleMapping.getName());
			gradingScale.setGrades(new ArrayList(sampleMapping.getGrades()));
			gradingScale.setDefaultBottomPercents(new HashMap(sampleMapping.getGradeMap()));
			session.save(gradingScale);
			if (log.isInfoEnabled()) log.info("Added Grade Mapping " + gradingScale.getUid());
			gradingScales.add(gradingScale);
		}
		setDefaultGradingScale("LetterGradePlusMinusMapping");
		session.flush();
		return gradingScales;
	}

	public void setAvailableGradingScales(final Collection gradingScaleDefinitions) {
        getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            mergeGradeMappings(gradingScaleDefinitions, session);
            return null;
        });
	}

	

	@Override
	public void saveGradeMappingToGradebook(final String scaleUuid, final String gradebookUid) {
		getHibernateTemplate().execute(session -> {

            List gradingScales = session.createQuery("from GradingScale as gradingScale where gradingScale.unavailable=false").list();

            for (Iterator iter = gradingScales.iterator(); iter.hasNext(); ) {
                GradingScale gradingScale = (GradingScale) iter.next();
                if (gradingScale.getUid().equals(scaleUuid)){
                    GradeMapping gradeMapping = new GradeMapping(gradingScale);
                    Gradebook gradebookToSet = getGradebook(gradebookUid);
                    gradeMapping.setGradebook(gradebookToSet);
                    session.save(gradeMapping);
                }
            }
            session.flush();
            return null;
        });
	}

	@Override
	public List<GradingScale> getAvailableGradingScales() {

		return getHibernateTemplate().execute(session -> {
            // Get available grade mapping templates.
            List<GradingScale> gradingScales = session.createQuery("from GradingScale as gradingScale where gradingScale.unavailable=false").list();

            // The application won't be able to run without grade mapping
            // templates, so if for some reason none have been defined yet,
            // do that now.
            if (gradingScales.isEmpty()) {
                if (log.isInfoEnabled()) log.info("No Grading Scale defined yet. This is probably because you have upgraded or you are working with a new database. Default grading scales will be created. Any customized system-wide grade mappings you may have defined in previous versions will have to be reconfigured.");
                gradingScales = GradebookFrameworkServiceImpl.this.addDefaultGradingScales(session);
            }
            return gradingScales;
        });
	}
	
	@Override
	public List<GradingScaleDefinition> getAvailableGradingScaleDefinitions() {
		List<GradingScale> gradingScales = this.getAvailableGradingScales();
		
		List<GradingScaleDefinition> rval = new ArrayList<>();
		for(GradingScale gradingScale: gradingScales) {
			rval.add(gradingScale.toGradingScaleDefinition());
		}
		return rval;
	}

	public void setDefaultGradingScale(String uid) {
		setPropertyValue(UID_OF_DEFAULT_GRADING_SCALE_PROPERTY, uid);
	}

	private void copyDefinitionToScale(GradingScaleDefinition bean, GradingScale gradingScale) {
		gradingScale.setUnavailable(false);
		gradingScale.setName(bean.getName());
		gradingScale.setGrades(bean.getGrades());
		Map defaultBottomPercents = new HashMap();
		Iterator gradesIter = bean.getGrades().iterator();
		Iterator defaultBottomPercentsIter = bean.getDefaultBottomPercentsAsList().iterator();
		while (gradesIter.hasNext() && defaultBottomPercentsIter.hasNext()) {
			String grade = (String)gradesIter.next();
			Double value = (Double)defaultBottomPercentsIter.next();
			defaultBottomPercents.put(grade, value);
		}
		gradingScale.setDefaultBottomPercents(defaultBottomPercents);
	}

	private void mergeGradeMappings(Collection gradingScaleDefinitions, Session session) throws HibernateException {
		Map newMappingDefinitionsMap = new HashMap();
		HashSet uidsToSet = new HashSet();
		for (Iterator iter = gradingScaleDefinitions.iterator(); iter.hasNext(); ) {
			GradingScaleDefinition bean = (GradingScaleDefinition)iter.next();
			newMappingDefinitionsMap.put(bean.getUid(), bean);
			uidsToSet.add(bean.getUid());
		}

		// Until we move to Hibernate 3 syntax, we need to update one record at a time.
		Query q;
		List gmtList;

		// Toggle any scales that are no longer specified.
		q = session.createQuery("from GradingScale as gradingScale where gradingScale.uid not in (:uidList) and gradingScale.unavailable=false");
		q.setParameterList("uidList", uidsToSet);
		gmtList = q.list();
		for (Iterator iter = gmtList.iterator(); iter.hasNext(); ) {
			GradingScale gradingScale = (GradingScale)iter.next();
			gradingScale.setUnavailable(true);
			session.update(gradingScale);
			if (log.isInfoEnabled()) log.info("Set Grading Scale " + gradingScale.getUid() + " unavailable");
		}

		// Modify any specified scales that already exist.
		q = session.createQuery("from GradingScale as gradingScale where gradingScale.uid in (:uidList)");
		q.setParameterList("uidList", uidsToSet);
		gmtList = q.list();
		for (Iterator iter = gmtList.iterator(); iter.hasNext(); ) {
			GradingScale gradingScale = (GradingScale)iter.next();
			copyDefinitionToScale((GradingScaleDefinition)newMappingDefinitionsMap.get(gradingScale.getUid()), gradingScale);
			uidsToSet.remove(gradingScale.getUid());
			session.update(gradingScale);
			if (log.isInfoEnabled()) log.info("Updated Grading Scale " + gradingScale.getUid());
		}

		// Add any new scales.
		for (Iterator iter = uidsToSet.iterator(); iter.hasNext(); ) {
			String uid = (String)iter.next();
			GradingScale gradingScale = new GradingScale();
			gradingScale.setUid(uid);
			GradingScaleDefinition bean = (GradingScaleDefinition)newMappingDefinitionsMap.get(uid);
			copyDefinitionToScale(bean, gradingScale);
			session.save(gradingScale);
			if (log.isInfoEnabled()) log.info("Added Grading Scale " + gradingScale.getUid());
		}
		session.flush();
	}


	public void deleteGradebook(final String uid)
		throws GradebookNotFoundException {
        if (log.isDebugEnabled()) log.debug("Deleting gradebook uid=" + uid + " by userUid=" + getUserUid());
        final Long gradebookId = getGradebook(uid).getId();

        // Worse of both worlds code ahead. We've been quick-marched
        // into Hibernate 3 sessions, but we're also having to use classic query
        // parsing -- which keeps us from being able to use either Hibernate's new-style
        // bulk delete queries or Hibernate's old-style session.delete method.
        // Instead, we're stuck with going through the Spring template for each
        // deletion one at a time.
        HibernateTemplate hibTempl = getHibernateTemplate();
        // int numberDeleted = hibTempl.bulkUpdate("delete GradingEvent as ge where ge.gradableObject.gradebook.id=?", gradebookId);
        // log.warn("GradingEvent numberDeleted=" + numberDeleted);

        List toBeDeleted;
        int numberDeleted;

        toBeDeleted = hibTempl.findByNamedParam("from GradingEvent as ge where ge.gradableObject.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isDebugEnabled()) log.debug("Deleted " + numberDeleted + " grading events");

        toBeDeleted = hibTempl.findByNamedParam("from AbstractGradeRecord as gr where gr.gradableObject.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isDebugEnabled()) log.debug("Deleted " + numberDeleted + " grade records");

        toBeDeleted = hibTempl.findByNamedParam("from GradableObject as go where go.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isDebugEnabled()) log.debug("Deleted " + numberDeleted + " gradable objects");

        Gradebook gradebook = (Gradebook)hibTempl.load(Gradebook.class, gradebookId);
        gradebook.setSelectedGradeMapping(null);

        toBeDeleted = hibTempl.findByNamedParam("from GradeMapping as gm where gm.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isDebugEnabled()) log.debug("Deleted " + numberDeleted + " grade mappings");

        hibTempl.delete(gradebook);
        hibTempl.flush();
        hibTempl.clear();
	}
	
	private void createDefaultLetterGradeMapping(final Map gradeMap)
	{
		if(getDefaultLetterGradePercentMapping() == null)
		{	
			Set keySet = gradeMap.keySet();

			if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
				throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.createDefaultLetterGradePercentMapping");

			if(validateLetterGradeMapping(gradeMap) == false)
				throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.createDefaultLetterGradePercentMapping");

			HibernateCallback hc = session -> {
                LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
                session.save(lgpm);
                Map saveMap = new HashMap();
                for(Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();)
                {
                    String key = (String) iter.next();
                    saveMap.put(key, gradeMap.get(key));
                }
                if (lgpm != null)
                {
                    lgpm.setGradeMap(saveMap);
                    lgpm.setMappingType(1);
                    session.update(lgpm);
                }
                return null;
            };
			getHibernateTemplate().execute(hc);
		}
	}
	
  private Map getHardDefaultLetterMapping()
  {
  	Map gradeMap = new HashMap();
		gradeMap.put("A+", Double.valueOf(100));
		gradeMap.put("A", Double.valueOf(95));
		gradeMap.put("A-", Double.valueOf(90));
		gradeMap.put("B+", Double.valueOf(87));
		gradeMap.put("B", Double.valueOf(83));
		gradeMap.put("B-", Double.valueOf(80));
		gradeMap.put("C+", Double.valueOf(77));
		gradeMap.put("C", Double.valueOf(73));
		gradeMap.put("C-", Double.valueOf(70));
		gradeMap.put("D+", Double.valueOf(67));
		gradeMap.put("D", Double.valueOf(63));
		gradeMap.put("D-", Double.valueOf(60));
		gradeMap.put("F", Double.valueOf(0.0));
		
		return gradeMap;
  }
}
