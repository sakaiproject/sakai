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
import org.hibernate.query.Query;
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
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradebookFrameworkServiceImpl extends BaseHibernateManager implements GradebookFrameworkService {

	public static final String UID_OF_DEFAULT_GRADING_SCALE_PROPERTY = "uidOfDefaultGradingScale";
	
	public static final String PROP_COURSE_POINTS_DISPLAYED = "gradebook.coursepoints.displayed";
	public static final String PROP_COURSE_GRADE_DISPLAYED = "gradebook.coursegrade.displayed";
	public static final String PROP_ASSIGNMENTS_DISPLAYED = "gradebook.assignments.displayed";
	public static final String PROP_ASSIGNMENT_STATS_DISPLAYED = "gradebook.stats.assignments.displayed";
	public static final String PROP_COURSE_GRADE_STATS_DISPLAYED = "gradebook.stats.coursegrade.displayed";

	@Override
	public void addGradebook(final String uid, final String name) {
        if(isGradebookDefined(uid)) {
			log.warn("You can not add a gradebook with uid={}. That gradebook already exists.", uid);
            throw new GradebookExistsException("You can not add a gradebook with uid=" + uid + ".  That gradebook already exists.");
        }
        if (log.isDebugEnabled()) {
			log.debug("Adding gradebook uid={} by userUid={}", uid, getUserUid());
		}

        createDefaultLetterGradeMapping(getHardDefaultLetterMapping());
        
        getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            // Get available grade mapping templates.
			List<GradingScale> gradingScales = session
					.createQuery("from GradingScale as gradingScale where gradingScale.unavailable=false").list();

            // The application won't be able to run without grade mapping
            // templates, so if for some reason none have been defined yet,
            // do that now.
            if (gradingScales.isEmpty()) {
                if (log.isInfoEnabled()) {
					log.info("No Grading Scale defined yet. This is probably because you have upgraded or you are working with a new database. Default grading scales will be created. Any customized system-wide grade mappings you may have defined in previous versions will have to be reconfigured.");
				}
                gradingScales = GradebookFrameworkServiceImpl.this.addDefaultGradingScales(session);
            }

            // Create and save the gradebook
			final Gradebook gradebook = new Gradebook();
			gradebook.setName(name);
            gradebook.setUid(uid);
            session.save(gradebook);

            // Create the course grade for the gradebook
            final CourseGrade cg = new CourseGrade();
            cg.setGradebook(gradebook);
            session.save(cg);

            // According to the specification, Display GradebookAssignment Grades is
            // on by default, and Display course grade is off. But can be overridden via properties


              final Boolean propAssignmentsDisplayed = this.serverConfigurationService.getBoolean(PROP_ASSIGNMENTS_DISPLAYED,true);
              gradebook.setAssignmentsDisplayed(propAssignmentsDisplayed);

              final Boolean propCourseGradeDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_GRADE_DISPLAYED,false);
              gradebook.setCourseGradeDisplayed(propCourseGradeDisplayed);

               final Boolean propCoursePointsDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_POINTS_DISPLAYED,false);
               gradebook.setCoursePointsDisplayed(propCoursePointsDisplayed);

            final String defaultScaleUid = GradebookFrameworkServiceImpl.this.getPropertyValue(UID_OF_DEFAULT_GRADING_SCALE_PROPERTY);

            // Add and save grade mappings based on the templates.
            GradeMapping defaultGradeMapping = null;
			final Set<GradeMapping> gradeMappings = new HashSet<>();
			for (final GradingScale gradingScale : gradingScales) {
                final GradeMapping gradeMapping = new GradeMapping(gradingScale);
                gradeMapping.setGradebook(gradebook);
                session.save(gradeMapping);
                gradeMappings.add(gradeMapping);
                if (gradingScale.getUid().equals(defaultScaleUid)) {
                    defaultGradeMapping = gradeMapping;
                }
            }

            // Check for null default.
            if (defaultGradeMapping == null) {
				defaultGradeMapping = gradeMappings.iterator().next();
                if (log.isWarnEnabled()) {
					log.warn("No default GradeMapping found for new Gradebook={}; will set default to {}",
							gradebook.getUid(), defaultGradeMapping.getName());
				}
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

            // SAK-33855 turn on stats for new gradebooks
            final Boolean propAssignmentStatsDisplayed = this.serverConfigurationService.getBoolean(PROP_ASSIGNMENT_STATS_DISPLAYED, true);
            gradebook.setAssignmentStatsDisplayed(propAssignmentStatsDisplayed);

            final Boolean propCourseGradeStatsDisplayed = this.serverConfigurationService.getBoolean(PROP_COURSE_GRADE_STATS_DISPLAYED, true);
            gradebook.setCourseGradeStatsDisplayed(propCourseGradeStatsDisplayed);

            // Update the gradebook with the new selected grade mapping
            session.update(gradebook);

            return null;

        });
	}

    private List addDefaultGradingScales(final Session session) throws HibernateException {
		final List<GradingScale> gradingScales = new ArrayList<>();

    	// Base the default set of templates on the old
    	// statically defined GradeMapping classes.
    	final GradeMapping[] oldGradeMappings = {
    		new LetterGradeMapping(),
    		new LetterGradePlusMinusMapping(),
    		new PassNotPassMapping(),
    		new GradePointsMapping()
    	};

    	for (final GradeMapping sampleMapping : oldGradeMappings) {
    		sampleMapping.setDefaultValues();
			final GradingScale gradingScale = new GradingScale();
			String uid = sampleMapping.getClass().getName();
			uid = uid.substring(uid.lastIndexOf('.') + 1);
			gradingScale.setUid(uid);
			gradingScale.setUnavailable(false);
			gradingScale.setName(sampleMapping.getName());
			gradingScale.setGrades(new ArrayList<>(sampleMapping.getGrades()));
			gradingScale.setDefaultBottomPercents(new HashMap<>(sampleMapping.getGradeMap()));
			session.save(gradingScale);
			if (log.isInfoEnabled()) {
				log.info("Added Grade Mapping " + gradingScale.getUid());
			}
			gradingScales.add(gradingScale);
		}
		setDefaultGradingScale("LetterGradePlusMinusMapping");
		session.flush();
		return gradingScales;
	}

	@Override
	public void setAvailableGradingScales(final Collection gradingScaleDefinitions) {
        getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            mergeGradeMappings(gradingScaleDefinitions, session);
            return null;
        });
	}

	

	@Override
	public void saveGradeMappingToGradebook(final String scaleUuid, final String gradebookUid) {
		getHibernateTemplate().execute(session -> {

			final List<GradingScale> gradingScales = session
					.createQuery("from GradingScale as gradingScale where gradingScale.unavailable=false").list();

			for (final GradingScale gradingScale : gradingScales) {
                if (gradingScale.getUid().equals(scaleUuid)){
                    final GradeMapping gradeMapping = new GradeMapping(gradingScale);
                    final Gradebook gradebookToSet = getGradebook(gradebookUid);
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
                if (log.isInfoEnabled()) {
					log.info("No Grading Scale defined yet. This is probably because you have upgraded or you are working with a new database. Default grading scales will be created. Any customized system-wide grade mappings you may have defined in previous versions will have to be reconfigured.");
				}
                gradingScales = GradebookFrameworkServiceImpl.this.addDefaultGradingScales(session);
            }
            return gradingScales;
        });
	}

	@Override
	public List<GradingScaleDefinition> getAvailableGradingScaleDefinitions() {
		final List<GradingScale> gradingScales = getAvailableGradingScales();

		final List<GradingScaleDefinition> rval = new ArrayList<>();
		for(final GradingScale gradingScale: gradingScales) {
			rval.add(gradingScale.toGradingScaleDefinition());
		}
		return rval;
	}

	@Override
	public void setDefaultGradingScale(final String uid) {
		setPropertyValue(UID_OF_DEFAULT_GRADING_SCALE_PROPERTY, uid);
	}

	private void copyDefinitionToScale(final GradingScaleDefinition bean, final GradingScale gradingScale) {
		gradingScale.setUnavailable(false);
		gradingScale.setName(bean.getName());
		gradingScale.setGrades(bean.getGrades());
		final Map<String, Double> defaultBottomPercents = new HashMap<>();
		final Iterator gradesIter = bean.getGrades().iterator();
		final Iterator defaultBottomPercentsIter = bean.getDefaultBottomPercentsAsList().iterator();
		while (gradesIter.hasNext() && defaultBottomPercentsIter.hasNext()) {
			final String grade = (String)gradesIter.next();
			final Double value = (Double)defaultBottomPercentsIter.next();
			defaultBottomPercents.put(grade, value);
		}
		gradingScale.setDefaultBottomPercents(defaultBottomPercents);
	}

	private void mergeGradeMappings(final Collection<GradingScaleDefinition> gradingScaleDefinitions,
			final Session session) throws HibernateException {
		final Map<String, GradingScaleDefinition> newMappingDefinitionsMap = new HashMap<>();
		final HashSet<String> uidsToSet = new HashSet<>();
		for (final GradingScaleDefinition bean : gradingScaleDefinitions) {
			newMappingDefinitionsMap.put(bean.getUid(), bean);
			uidsToSet.add(bean.getUid());
		}

		// Until we move to Hibernate 3 syntax, we need to update one record at a time.
		// Toggle any scales that are no longer specified.
		Query q = session.createQuery(
				"from GradingScale as gradingScale where gradingScale.uid not in (:uidList) and gradingScale.unavailable=false");
		q.setParameterList("uidList", uidsToSet);
		List<GradingScale> gmtList = q.list();
		for (final GradingScale gradingScale : gmtList) {
			gradingScale.setUnavailable(true);
			session.update(gradingScale);
			if (log.isInfoEnabled()) {
				log.info("Set Grading Scale " + gradingScale.getUid() + " unavailable");
			}
		}

		// Modify any specified scales that already exist.
		q = session.createQuery("from GradingScale as gradingScale where gradingScale.uid in (:uidList)");
		q.setParameterList("uidList", uidsToSet);
		gmtList = q.list();
		for (final GradingScale gradingScale : gmtList) {
			copyDefinitionToScale(newMappingDefinitionsMap.get(gradingScale.getUid()), gradingScale);
			uidsToSet.remove(gradingScale.getUid());
			session.update(gradingScale);
			if (log.isInfoEnabled()) {
				log.info("Updated Grading Scale " + gradingScale.getUid());
			}
		}

		// Add any new scales.
		for (final String uid : uidsToSet) {
			final GradingScale gradingScale = new GradingScale();
			gradingScale.setUid(uid);
			final GradingScaleDefinition bean = newMappingDefinitionsMap.get(uid);
			copyDefinitionToScale(bean, gradingScale);
			session.save(gradingScale);
			if (log.isInfoEnabled()) {
				log.info("Added Grading Scale " + gradingScale.getUid());
			}
		}
		session.flush();
	}


	@Override
	public void deleteGradebook(final String uid) throws GradebookNotFoundException {
			log.debug("Deleting gradebook uid={} by userUid={}", uid, getUserUid());

        final Long gradebookId = getGradebook(uid).getId();

        // Worse of both worlds code ahead. We've been quick-marched
        // into Hibernate 3 sessions, but we're also having to use classic query
        // parsing -- which keeps us from being able to use either Hibernate's new-style
        // bulk delete queries or Hibernate's old-style session.delete method.
        // Instead, we're stuck with going through the Spring template for each
        // deletion one at a time.
        final HibernateTemplate hibTempl = getHibernateTemplate();
        // int numberDeleted = hibTempl.bulkUpdate("delete GradingEvent as ge where ge.gradableObject.gradebook.id=?", gradebookId);
        // log.warn("GradingEvent numberDeleted=" + numberDeleted);

        List toBeDeleted;
        int numberDeleted;

        toBeDeleted = hibTempl.findByNamedParam("from GradingEvent as ge where ge.gradableObject.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        log.debug("Deleted {} grading events", numberDeleted);

        toBeDeleted = hibTempl.findByNamedParam("from Comment as gc where gc.gradableObject.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        log.debug("Deleted {} grade comments", numberDeleted);

        toBeDeleted = hibTempl.findByNamedParam("from AbstractGradeRecord as gr where gr.gradableObject.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isDebugEnabled()) {
			log.debug("Deleted {} grade records", numberDeleted);
		}

        toBeDeleted = hibTempl.findByNamedParam("from GradableObject as go where go.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        log.debug("Deleted {} gradable objects", numberDeleted);

        toBeDeleted = hibTempl.findByNamedParam("from Category as cg where cg.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        log.debug("Deleted {} gradable categories", numberDeleted);

        final Gradebook gradebook = hibTempl.load(Gradebook.class, gradebookId);
        gradebook.setSelectedGradeMapping(null);

        toBeDeleted = hibTempl.findByNamedParam("from GradeMapping as gm where gm.gradebook.id = :gradebookid", "gradebookid", gradebookId);
        numberDeleted = toBeDeleted.size();
        hibTempl.deleteAll(toBeDeleted);
        if (log.isDebugEnabled()) {
			log.debug("Deleted {} grade mappings", numberDeleted);
		}

        hibTempl.delete(gradebook);
        hibTempl.flush();
        hibTempl.clear();
	}
	
	private void createDefaultLetterGradeMapping(final Map gradeMap)
	{
		if(getDefaultLetterGradePercentMapping() == null)
		{
			final Set keySet = gradeMap.keySet();

			if(keySet.size() != GradebookService.validLetterGrade.length) {
				throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.createDefaultLetterGradePercentMapping");
			}

			if (!validateLetterGradeMapping(gradeMap)) {
				throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.createDefaultLetterGradePercentMapping");
			}

			final HibernateCallback hc = session -> {
                final LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
                session.save(lgpm);
                final Map saveMap = new HashMap();
                for(final Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();)
                {
                    final String key = (String) iter.next();
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

	private Map<String, Double> getHardDefaultLetterMapping()
  {
		final Map<String, Double> gradeMap = new HashMap<>();
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
