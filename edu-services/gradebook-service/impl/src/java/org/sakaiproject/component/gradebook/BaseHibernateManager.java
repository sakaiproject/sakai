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
 /**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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
package org.sakaiproject.component.gradebook;

 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.stream.Collectors;

 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.StaleObjectStateException;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.hibernate.HibernateCriterionUtils;
 import org.sakaiproject.section.api.SectionAwareness;
 import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
 import org.sakaiproject.section.api.facade.Role;
 import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
 import org.sakaiproject.service.gradebook.shared.CommentDefinition;
 import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
 import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
 import org.sakaiproject.service.gradebook.shared.GradebookExternalAssessmentService;
 import org.sakaiproject.service.gradebook.shared.GradebookHelper;
 import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
 import org.sakaiproject.service.gradebook.shared.GradebookService;
 import org.sakaiproject.service.gradebook.shared.GraderPermission;
 import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
 import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
 import org.sakaiproject.tool.gradebook.GradebookAssignment;
 import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
 import org.sakaiproject.tool.gradebook.Category;
 import org.sakaiproject.tool.gradebook.Comment;
 import org.sakaiproject.tool.gradebook.CourseGrade;
 import org.sakaiproject.tool.gradebook.CourseGradeRecord;
 import org.sakaiproject.tool.gradebook.GradableObject;
 import org.sakaiproject.tool.gradebook.GradeMapping;
 import org.sakaiproject.tool.gradebook.Gradebook;
 import org.sakaiproject.tool.gradebook.GradebookProperty;
 import org.sakaiproject.tool.gradebook.GradingEvent;
 import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
 import org.sakaiproject.tool.gradebook.Permission;
 import org.sakaiproject.tool.gradebook.facades.Authn;
 import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
 import org.springframework.orm.hibernate4.HibernateCallback;
 import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

 import lombok.extern.slf4j.Slf4j;

 /**
 * Provides methods which are shared between service business logic and application business
 * logic, but not exposed to external callers.
 */
@Slf4j
public abstract class BaseHibernateManager extends HibernateDaoSupport {

    protected SectionAwareness sectionAwareness;
    protected Authn authn;
    protected EventTrackingService eventTrackingService;
    protected ServerConfigurationService serverConfigurationService;
    protected GradebookExternalAssessmentService externalAssessmentService;

    // Local cache of static-between-deployment properties.
    protected Map propertiesMap = new HashMap();

    public Gradebook getGradebook(String uid) throws GradebookNotFoundException {
    	List list = getHibernateTemplate().findByNamedParam("from Gradebook as gb where gb.uid = :uid", "uid", uid);
		if (list.size() == 1) {
			return (Gradebook)list.get(0);
		} else {
            throw new GradebookNotFoundException("Could not find gradebook uid=" + uid);
        }
    }

    public boolean isGradebookDefined(String gradebookUid) {
        return ((Long) getSessionFactory().getCurrentSession().createCriteria(Gradebook.class)
                .add(Restrictions.eq("uid", gradebookUid))
                .setProjection(Projections.rowCount())
                .uniqueResult()) == 1L;

    }

    protected List<GradebookAssignment> getAssignments(Long gradebookId) throws HibernateException {
        return getSessionFactory().getCurrentSession()
                .createQuery("from GradebookAssignment as asn where asn.gradebook.id = :gradebookid and asn.removed is false")
                .setLong("gradebookid", gradebookId)
                .list();
    }

    protected List getCountedStudentGradeRecords(Long gradebookId, String studentId) throws HibernateException {
        return getSessionFactory().getCurrentSession().createQuery(
        	"select agr from AssignmentGradeRecord as agr, GradebookAssignment as asn where agr.studentId = :studentid and agr.gradableObject = asn and asn.removed is false and asn.notCounted is false and asn.gradebook.id = :gradebookid and asn.ungraded is false")
        	.setString("studentid", studentId)
        	.setLong("gradebookid", gradebookId)
        	.list();
    }

    /**
     */
    public CourseGrade getCourseGrade(Long gradebookId) {
        return (CourseGrade) getSessionFactory().getCurrentSession().createQuery(
                "from CourseGrade as cg where cg.gradebook.id = :gradebookid")
                .setLong("gradebookid", gradebookId)
                .uniqueResult();
    }

    /**
     * Gets the course grade record for a student, or null if it does not yet exist.
     *
     * @param studentId The student ID
     * @return A List of grade records
     *
     * @throws HibernateException
     */
    protected CourseGradeRecord getCourseGradeRecord(Gradebook gradebook, String studentId) throws HibernateException {
        return (CourseGradeRecord) getSessionFactory().getCurrentSession()
                .createQuery("from CourseGradeRecord as cgr where cgr.studentId = :studentid and cgr.gradableObject.gradebook = :gradebook")
                .setString("studentid", studentId)
                .setEntity("gradebook", gradebook)
                .uniqueResult();
    }

    public String getGradebookUid(Long id) {
        return getHibernateTemplate().load(Gradebook.class, id).getUid();
    }

	protected Set<String> getAllStudentUids(String gradebookUid) {
		List<EnrollmentRecord> enrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
        return enrollments.stream().map(e -> e.getUser().getUserUid()).collect(Collectors.toSet());
	}

    public String getPropertyValue(final String name) {
		String value = (String)propertiesMap.get(name);
		if (value == null) {
			List<?> list = getHibernateTemplate().findByNamedParam("from GradebookProperty as prop where prop.name = :name", "name", name);
			if (!list.isEmpty()) {
				GradebookProperty property = (GradebookProperty)list.get(0);
				value = property.getValue();
				propertiesMap.put(name, value);
			}
		}
		return value;
	}

	public void setPropertyValue(final String name, final String value) {
		List<?> list = getHibernateTemplate().findByNamedParam("from GradebookProperty as prop where prop.name = :name", "name", name);
		GradebookProperty property;
		if (!list.isEmpty()) {
			property = (GradebookProperty)list.get(0);
		} else {
			property = new GradebookProperty(name);
		}
		property.setValue(value);
		getHibernateTemplate().saveOrUpdate(property);
		propertiesMap.put(name, value);
	}

	/**
	 * Oracle has a low limit on the maximum length of a parameter list
	 * in SQL queries of the form "WHERE tbl.col IN (:paramList)".
	 * Since enrollment lists can sometimes be very long, we've replaced
	 * such queries with full selects followed by filtering. This helper
	 * method filters out unwanted grade records. (Typically they're not
	 * wanted because they're either no longer officially enrolled in the
	 * course or they're not members of the selected section.)
	 */
	protected List filterGradeRecordsByStudents(Collection gradeRecords, Collection studentUids) {
		List filteredRecords = new ArrayList();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
			if (studentUids.contains(agr.getStudentId())) {
				filteredRecords.add(agr);
			}
		}
		return filteredRecords;
	}

	@Deprecated
	protected GradebookAssignment getAssignmentWithoutStats(String gradebookUid, String assignmentName) throws HibernateException {
		return (GradebookAssignment) getSessionFactory().getCurrentSession()
                .createQuery("from GradebookAssignment as asn where asn.name = :assignmentname and asn.gradebook.uid = :gradebookuid and asn.removed is false")
                .setString("assignmentname", assignmentName)
                .setString("gradebookuid", gradebookUid)
                .uniqueResult();
	}

	protected GradebookAssignment getAssignmentWithoutStats(String gradebookUid, Long assignmentId) throws HibernateException {
		return (GradebookAssignment) getSessionFactory().getCurrentSession()
                .createQuery("from GradebookAssignment as asn where asn.id = :assignmentid and asn.gradebook.uid = :gradebookuid and asn.removed is false")
                .setLong("assignmentid", assignmentId)
                .setString("gradebookuid", gradebookUid)
                .uniqueResult();
	}

	protected void updateAssignment(GradebookAssignment assignment) throws ConflictingAssignmentNameException, HibernateException {
		// Ensure that we don't have the assignment in the session, since
		// we need to compare the existing one in the db to our edited assignment
        Session session = getSessionFactory().getCurrentSession();
		session.evict(assignment);

		GradebookAssignment asnFromDb = (GradebookAssignment) session.load(GradebookAssignment.class, assignment.getId());

		Long count = (Long) session.createCriteria(GradableObject.class)
                .add(Restrictions.eq("name", assignment.getName()))
                .add(Restrictions.eq("gradebook", assignment.getGradebook()))
                .add(Restrictions.ne("id", assignment.getId()))
                .add(Restrictions.eq("removed", false))
                .setProjection(Projections.rowCount())
                .uniqueResult();
		if(count > 0) {
			throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
		}

		session.evict(asnFromDb);
		session.update(assignment);
	}

    protected AssignmentGradeRecord getAssignmentGradeRecord(GradebookAssignment assignment, String studentUid) throws HibernateException {
		return (AssignmentGradeRecord) getSessionFactory().getCurrentSession()
                .createQuery("from AssignmentGradeRecord as agr where agr.studentId = :studentid and agr.gradableObject.id = :assignmentid")
                .setString("studentid", studentUid)
                .setLong("assignmentid", assignment.getId())
                .uniqueResult();
	}

    public Long createAssignment(final Long gradebookId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted,
           final Boolean isReleased, final Boolean isExtraCredit) throws ConflictingAssignmentNameException, StaleObjectModificationException
    {
        return createNewAssignment(gradebookId, null, name, points, dueDate, isNotCounted, isReleased, isExtraCredit);
    }

    public Long createAssignmentForCategory(final Long gradebookId, final Long categoryId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased, final Boolean isExtraCredit)
    throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException
    {
    	if(gradebookId == null || categoryId == null)
    	{
    		throw new IllegalArgumentException("gradebookId or categoryId is null in BaseHibernateManager.createAssignmentForCategory");
    	}

    	return createNewAssignment(gradebookId, categoryId, name, points, dueDate, isNotCounted, isReleased, isExtraCredit);
    }

    private Long createNewAssignment(final Long gradebookId, final Long categoryId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted,
            final Boolean isReleased, final Boolean isExtraCredit) throws ConflictingAssignmentNameException, StaleObjectModificationException
    {
        GradebookAssignment asn = prepareNewAssignment(name, points, dueDate, isNotCounted, isReleased, isExtraCredit);

        return saveNewAssignment(gradebookId, categoryId, asn);
    }

    private GradebookAssignment prepareNewAssignment(String name, Double points, Date dueDate, Boolean isNotCounted, Boolean isReleased, Boolean isExtraCredit)
    {
        String validatedName = StringUtils.trimToNull(name);
        if (validatedName == null){
            throw new ConflictingAssignmentNameException("You cannot save an assignment without a name");
        }

        // name cannot contain these special chars as they are reserved for special columns in import/export
        GradebookHelper.validateGradeItemName(validatedName);

        GradebookAssignment asn = new GradebookAssignment();
        asn.setName(validatedName);
        asn.setPointsPossible(points);
        asn.setDueDate(dueDate);
        asn.setUngraded(false);
        if (isNotCounted != null)
        {
            asn.setNotCounted(isNotCounted.booleanValue());
        }
        if (isExtraCredit != null)
        {
            asn.setExtraCredit(isExtraCredit.booleanValue());
        }
        if (isReleased != null)
        {
            asn.setReleased(isReleased.booleanValue());
        }

        return asn;
    }

    private void loadAssignmentGradebookAndCategory(GradebookAssignment asn, Long gradebookId, Long categoryId)
    {
        Session session = getSessionFactory().getCurrentSession();
        Gradebook gb = (Gradebook) session.load(Gradebook.class, gradebookId);
        asn.setGradebook(gb);
        if (categoryId != null)
        {
            Category cat = (Category) session.load(Category.class, categoryId);
            asn.setCategory(cat);
        }
    }

    protected Long saveNewAssignment(final Long gradebookId, final Long categoryId, final GradebookAssignment asn) throws ConflictingAssignmentNameException
    {
        HibernateCallback<Long> hc = session -> {
            loadAssignmentGradebookAndCategory(asn, gradebookId, categoryId);

            if (assignmentNameExists(asn.getName(), asn.getGradebook()))
            {
                throw new ConflictingAssignmentNameException("You cannot save multiple assignments in a gradebook with the same name");
            }

            return (Long) session.save(asn);
        };

        return getHibernateTemplate().execute(hc);
    }

    public void updateGradebook(final Gradebook gradebook) throws StaleObjectModificationException {
        HibernateCallback hc = session -> {
            // Get the gradebook and selected mapping from persistence
            Gradebook gradebookFromPersistence = (Gradebook)session.load(gradebook.getClass(), gradebook.getId());
            GradeMapping mappingFromPersistence = gradebookFromPersistence.getSelectedGradeMapping();

            // If the mapping has changed, and there are explicitly entered
            // course grade records, disallow this update.
            if (!mappingFromPersistence.getId().equals(gradebook.getSelectedGradeMapping().getId())) {
                if(isExplicitlyEnteredCourseGradeRecords(gradebook.getId())) {
                    throw new IllegalStateException("Selected grade mapping can not be changed, since explicit course grades exist.");
                }
            }

            // Evict the persisted objects from the session and update the gradebook
            // so the new grade mapping is used in the sort column update
            //session.evict(mappingFromPersistence);
            for(Iterator iter = gradebookFromPersistence.getGradeMappings().iterator(); iter.hasNext();) {
                session.evict(iter.next());
            }
            session.evict(gradebookFromPersistence);
            try {
                session.update(gradebook);
                session.flush();
            } catch (StaleObjectStateException e) {
                throw new StaleObjectModificationException(e);
            }

            return null;
        };
        getHibernateTemplate().execute(hc);
    }

    public boolean isExplicitlyEnteredCourseGradeRecords(final Long gradebookId) {

        final List<String> studentUids = new ArrayList<>(getAllStudentUids(getGradebookUid(gradebookId)));

        if (studentUids.isEmpty()) {
            return false;
        }

        HibernateCallback<Number> hc = session -> (Number) session.createCriteria(CourseGradeRecord.class)
                .createAlias("gradableObject", "go")
                .createAlias("go.gradebook", "gb")
                .add(Restrictions.eq("gb.id", gradebookId))
                .add(Restrictions.isNotNull("enteredGrade"))
                .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentUids))
                .setProjection(Projections.rowCount())
                .uniqueResult();
        Number number = getHibernateTemplate().execute(hc);
        log.debug("total number of explicitly entered course grade records = {}", number);
        return number.intValue() > 0;
    }

	public Authn getAuthn() {
        return authn;
    }
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

    protected String getUserUid() {
        return authn.getUserUid();
    }

    protected SectionAwareness getSectionAwareness() {
        return sectionAwareness;
    }
    public void setSectionAwareness(SectionAwareness sectionAwareness) {
        this.sectionAwareness = sectionAwareness;
    }

    protected ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

    protected EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    protected GradebookExternalAssessmentService getGradebookExternalAssessmentService() {
        return externalAssessmentService;
    }

    public void setGradebookExternalAssessmentService(GradebookExternalAssessmentService externalAssessmentService) {
        this.externalAssessmentService = externalAssessmentService;
    }

    public void postEvent(String message,String objectReference){
       eventTrackingService.postEvent(message,objectReference);
    }


    public Long createCategory(final Long gradebookId, final String name, final Double weight, final Integer drop_lowest,
                               final Integer dropHighest, final Integer keepHighest, final Boolean is_extra_credit) {
        return createCategory(gradebookId, name, weight, drop_lowest, dropHighest, keepHighest, is_extra_credit, null);
    }

    public Long createCategory(final Long gradebookId, final String name, final Double weight, final Integer drop_lowest,
                               final Integer dropHighest, final Integer keepHighest, final Boolean is_extra_credit,
                               final Integer categoryOrder) throws ConflictingCategoryNameException, StaleObjectModificationException {

    	HibernateCallback<Long> hc = session -> {
            Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);

            Number numNameConflicts = (Number) session.createCriteria(Category.class)
                    .add(Restrictions.eq("name", name))
                    .add(Restrictions.eq("gradebook", gb))
                    .add(Restrictions.eq("removed", false))
                    .setProjection(Projections.rowCount())
                    .uniqueResult();

            if(numNameConflicts.intValue() > 0) {
                throw new ConflictingCategoryNameException("You can not save multiple catetories in a gradebook with the same name");
            }
            if(weight > 1 || weight < 0) {
                throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in createCategory of BaseHibernateManager");
            }
            if(((drop_lowest!=null && drop_lowest > 0) || (dropHighest!=null && dropHighest > 0)) && (keepHighest!=null && keepHighest > 0)) {
                throw new IllegalArgumentException("a combination of positive values for keepHighest and either drop_lowest or dropHighest occurred in createCategory of BaseHibernateManager");
            }

            Category ca = new Category();
            ca.setGradebook(gb);
            ca.setName(name);
            ca.setWeight(weight);
            ca.setDropLowest(drop_lowest);
            ca.setDropHighest(dropHighest);
            ca.setKeepHighest(keepHighest);
            //ca.setItemValue(itemValue);
            ca.setRemoved(false);
            ca.setExtraCredit(is_extra_credit);
            ca.setCategoryOrder(categoryOrder);

            Long id = (Long) session.save(ca);

            return id;
        };

    	return (Long)getHibernateTemplate().execute(hc);
    }

    public List getCategories(final Long gradebookId) throws HibernateException {
    	HibernateCallback<List<Category>> hc = session -> session
                .createQuery("from Category as ca where ca.gradebook.id = :gradebookid and ca.removed is false")
                .setLong("gradebookid", gradebookId)
                .list();

        return getHibernateTemplate().execute(hc);
    }

    public List getCategoriesWithAssignments(Long gradebookId) {
    	List categories = getCategories(gradebookId);
    	List categoriesWithAssignments = new ArrayList();
    	if (categories != null) {
    		for (Iterator catIter = categories.iterator(); catIter.hasNext();) {
    			Category category = (Category) catIter.next();
    			if (category != null) {
    				List assignments = getAssignmentsForCategory(category.getId());
    				category.setAssignmentList(assignments);
    				categoriesWithAssignments.add(category);
    			}
    		}
    	}

    	return categoriesWithAssignments;
    }

    public List<GradebookAssignment> getAssignmentsForCategory(final Long categoryId) throws HibernateException{
    	HibernateCallback<List<GradebookAssignment>> hc = session -> session
                .createQuery("from GradebookAssignment as assign where assign.category = :categoryid and assign.removed is false")
                .setLong("categoryid", categoryId)
                .list();
    	return getHibernateTemplate().execute(hc);
    }

    public Category getCategory(final Long categoryId) throws HibernateException{
    	HibernateCallback<Category> hc = session -> (Category) session
                .createQuery("from Category as cat where cat.id = :categoryid")
                .setLong("categoryid", categoryId)
                .uniqueResult();
    	return getHibernateTemplate().execute(hc);
    }

    public void updateCategory(final Category category) throws ConflictingCategoryNameException, StaleObjectModificationException {
        HibernateCallback hc = session -> {
            session.evict(category);
            Category persistentCat = (Category) session.load(Category.class, category.getId());
            Number numNameConflicts = (Number) session.createCriteria(Category.class)
                    .add(Restrictions.eq("name", category.getName()))
                    .add(Restrictions.eq("gradebook", category.getGradebook()))
                    .add(Restrictions.ne("id", category.getId()))
                    .add(Restrictions.eq("removed", false))
                    .setProjection(Projections.rowCount())
                    .uniqueResult();

            if (numNameConflicts.intValue() > 0) {
                throw new ConflictingCategoryNameException("You can not save multiple category in a gradebook with the same name");
            }
            if (category.getWeight().doubleValue() > 1 || category.getWeight().doubleValue() < 0) {
                throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in updateCategory of BaseHibernateManager");
            }
            session.evict(persistentCat);
            session.update(category);
            return null;
        };
        try {
    		getHibernateTemplate().execute(hc);
    	} catch (Exception e) {
    		throw new StaleObjectModificationException(e);
    	}
    }

    public void removeCategory(final Long categoryId) throws StaleObjectModificationException{
    	HibernateCallback hc = session -> {
            Category persistentCat = (Category)session.load(Category.class, categoryId);

            List assigns = getAssignmentsForCategory(categoryId);
            for(Iterator iter = assigns.iterator(); iter.hasNext();)
            {
                GradebookAssignment assignment = (GradebookAssignment) iter.next();
                assignment.setCategory(null);
                updateAssignment(assignment);
            }

            persistentCat.setRemoved(true);
            session.update(persistentCat);
            return null;
        };
    	try {
    		getHibernateTemplate().execute(hc);
    	} catch (Exception e) {
    		throw new StaleObjectModificationException(e);
    	}
    }

    public LetterGradePercentMapping getDefaultLetterGradePercentMapping() {
    	HibernateCallback<LetterGradePercentMapping> hc = session -> {
            List<LetterGradePercentMapping> mapping = session.createCriteria(LetterGradePercentMapping.class)
                    .add(Restrictions.eq("mappingType", 1))
                    .list();

            if(mapping.size() == 0)
            {
                log.info("Default letter grade mapping hasn't been created in DB in BaseHibernateManager.getDefaultLetterGradePercentMapping");
                return null;
            }
            if(mapping.size() > 1)
            {
                log.error("Duplicate default letter grade mapping was created in DB in BaseHibernateManager.getDefaultLetterGradePercentMapping");
                return null;
            }

            return mapping.get(0);
        };

    	return getHibernateTemplate().execute(hc);
    }

    public void createOrUpdateDefaultLetterGradePercentMapping(final Map gradeMap) {
    	if(gradeMap == null)
    		throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.createOrUpdateDefaultLetterGradePercentMapping");

    	LetterGradePercentMapping lgpm = getDefaultLetterGradePercentMapping();

    	if(lgpm != null)
    	{
    		updateDefaultLetterGradePercentMapping(gradeMap, lgpm);
    	}
    	else
    	{
    		createDefaultLetterGradePercentMapping(gradeMap);
    	}
    }

    private void updateDefaultLetterGradePercentMapping(final Map<String, Double> gradeMap, final LetterGradePercentMapping lgpm) {
  		Set<String> keySet = gradeMap.keySet();

  		if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
  			throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.updateDefaultLetterGradePercentMapping");

  		if(!validateLetterGradeMapping(gradeMap))
  			throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.updateDefaultLetterGradePercentMapping");

  		HibernateCallback<Void> hcb = session -> {
              Map<String, Double> saveMap = new HashMap<>(gradeMap);
              lgpm.setGradeMap(saveMap);
              session.update(lgpm);
              return null;
          };
  		getHibernateTemplate().execute(hcb);
    }

    public void createDefaultLetterGradePercentMapping(final Map<String, Double> gradeMap) {
    	if(getDefaultLetterGradePercentMapping() != null)
    		throw new IllegalArgumentException("gradeMap has already been created in BaseHibernateManager.createDefaultLetterGradePercentMapping");

    	if(gradeMap == null)
    		throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.createDefaultLetterGradePercentMapping");

    	Set<String> keySet = gradeMap.keySet();

    	if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
    		throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.createDefaultLetterGradePercentMapping");

    	if(!validateLetterGradeMapping(gradeMap))
    		throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.createDefaultLetterGradePercentMapping");

        HibernateCallback<Void> hcb = session -> {
            LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
            Map<String, Double> saveMap = new HashMap<>(gradeMap);
            if (lgpm != null) {
                lgpm.setGradeMap(saveMap);
                lgpm.setMappingType(1);
                session.save(lgpm);
            }
            return null;
        };
      getHibernateTemplate().execute(hcb);
    }

    public LetterGradePercentMapping getLetterGradePercentMapping(final Gradebook gradebook) {
    	HibernateCallback<LetterGradePercentMapping> hc = session -> {
            LetterGradePercentMapping mapping = (LetterGradePercentMapping) session
                    .createQuery("from LetterGradePercentMapping as lgpm where lgpm.gradebookId = :gradebookId and lgpm.mappingType = 2")
                    .setLong("gradebookId", gradebook.getId())
                    .uniqueResult();
            if(mapping == null ) {
                LetterGradePercentMapping lgpm = getDefaultLetterGradePercentMapping();
                LetterGradePercentMapping returnLgpm = new LetterGradePercentMapping();
                returnLgpm.setGradebookId(gradebook.getId());
                returnLgpm.setGradeMap(lgpm.getGradeMap());
                returnLgpm.setMappingType(2);
                return returnLgpm;
            }
            return mapping;
        };

    	return getHibernateTemplate().execute(hc);
    }

    /**
     * this method is different with getLetterGradePercentMapping -
     * it returns null if no mapping exists for gradebook instead of
     * returning default mapping.
     */
    private LetterGradePercentMapping getLetterGradePercentMappingForGradebook(final Gradebook gradebook) {
        HibernateCallback<LetterGradePercentMapping> hc = session -> {
            LetterGradePercentMapping mapping = (LetterGradePercentMapping) session
                    .createQuery("from LetterGradePercentMapping as lgpm where lgpm.gradebookId = :gradebookId and lgpm.mappingType = 2")
                    .setLong("gradebookId", gradebook.getId())
                    .uniqueResult();
            return mapping;
        };

    	return getHibernateTemplate().execute(hc);
    }

    public void saveOrUpdateLetterGradePercentMapping(final Map<String, Double> gradeMap, final Gradebook gradebook) {
    	if(gradeMap == null) {
            throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");
        }

    	LetterGradePercentMapping lgpm = getLetterGradePercentMappingForGradebook(gradebook);

    	if(lgpm == null) {
    		Set<String> keySet = gradeMap.keySet();

    		if(keySet.size() != GradebookService.validLetterGrade.length) { //we only consider letter grade with -/+ now.
                throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");
            }
    		if(!validateLetterGradeMapping(gradeMap)) {
                throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");
            }

    		HibernateCallback<Void> hcb = session -> {
                LetterGradePercentMapping lgpm1 = new LetterGradePercentMapping();
                Map<String, Double> saveMap = new HashMap<>(gradeMap);
                lgpm1.setGradeMap(saveMap);
                lgpm1.setGradebookId(gradebook.getId());
                lgpm1.setMappingType(2);
                session.save(lgpm1);
                return null;
            };
    		getHibernateTemplate().execute(hcb);
    	}
    	else
    	{
    		udpateLetterGradePercentMapping(gradeMap, gradebook);
    	}
    }

    private void udpateLetterGradePercentMapping(final Map<String, Double> gradeMap, final Gradebook gradebook) {
        HibernateCallback<Void> hcb = session -> {
            LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);

            if (lgpm == null) {
                throw new IllegalArgumentException("LetterGradePercentMapping is null in BaseHibernateManager.updateLetterGradePercentMapping");
            }
            if (gradeMap == null) {
                throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.updateLetterGradePercentMapping");
            }
            Set<String> keySet = gradeMap.keySet();

            if (keySet.size() != GradebookService.validLetterGrade.length) { //we only consider letter grade with -/+ now.
                throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.udpateLetterGradePercentMapping");
            }
            if (validateLetterGradeMapping(gradeMap) == false) {
                throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.udpateLetterGradePercentMapping");
            }
            Map<String, Double> saveMap = new HashMap<>(gradeMap);
            lgpm.setGradeMap(saveMap);
            session.save(lgpm);

            return null;
        };
        getHibernateTemplate().execute(hcb);
    }

    protected boolean validateLetterGradeMapping(Map<String, Double> gradeMap) {
    	for (String key : gradeMap.keySet()) {
    		boolean validLetter = false;
    		for (int i = 0; i < GradebookService.validLetterGrade.length; i++) {
    			if (key.equalsIgnoreCase(GradebookService.validLetterGrade[i])) {
    				validLetter = true;
    				break;
    			}
    		}
            if (!validLetter) {
                return false;
            }
    	}
    	return true;
    }

    public Long createUngradedAssignment(final Long gradebookId, final String name, final Date dueDate, final Boolean isNotCounted,
                                         final Boolean isReleased) throws ConflictingAssignmentNameException, StaleObjectModificationException {
    	HibernateCallback<Long> hc = session -> {
            Gradebook gb = (Gradebook) session.load(Gradebook.class, gradebookId);

            // trim the name before validation
            String trimmedName = StringUtils.trimToEmpty(name);

            if(assignmentNameExists(trimmedName, gb)) {
                throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
            }

            GradebookAssignment asn = new GradebookAssignment();
            asn.setGradebook(gb);
            asn.setName(trimmedName);
            asn.setDueDate(dueDate);
            asn.setUngraded(true);
            if (isNotCounted != null) {
                asn.setNotCounted(isNotCounted);
            }
            if (isReleased != null) {
                asn.setReleased(isReleased);
            }

            return (Long) session.save(asn);
        };
    	return getHibernateTemplate().execute(hc);
    }

    public Long createUngradedAssignmentForCategory(final Long gradebookId, final Long categoryId, final String name, final Date dueDate, final Boolean isNotCounted,
                                                    final Boolean isReleased) throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException {

        if (gradebookId == null || categoryId == null) {
            throw new IllegalArgumentException("gradebookId or categoryId is null in BaseHibernateManager.createUngradedAssignmentForCategory");
    	}
    	HibernateCallback<Long> hc = session -> {
            Gradebook gb = (Gradebook) session.load(Gradebook.class, gradebookId);
            Category cat = (Category) session.load(Category.class, categoryId);

            // trim the name before the validation
            String trimmedName = StringUtils.trimToEmpty(name);

            if (assignmentNameExists(trimmedName, gb)) {
                throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
            }

            GradebookAssignment asn = new GradebookAssignment();
            asn.setGradebook(gb);
            asn.setCategory(cat);
            asn.setName(trimmedName);
            asn.setDueDate(dueDate);
            asn.setUngraded(true);
            if (isNotCounted != null) {
                asn.setNotCounted(isNotCounted);
            }
            if (isReleased != null) {
                asn.setReleased(isReleased);
            }

            return (Long) session.save(asn);
        };

    	return getHibernateTemplate().execute(hc);
    }

    public Long addPermission(final Long gradebookId, final String userId, final String function, final Long categoryId,
                              final String groupId) throws IllegalArgumentException {

        if (gradebookId == null || userId == null || function == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.addPermission");
        }
        if (!function.equalsIgnoreCase(GradebookService.gradePermission) && !function.equalsIgnoreCase(GradebookService.viewPermission)) {
            throw new IllegalArgumentException("Function is not grade or view in BaseHibernateManager.addPermission");
        }
    	HibernateCallback<Long> hc = session -> {
            Permission permission = new Permission();
            permission.setCategoryId(categoryId);
            permission.setGradebookId(gradebookId);
            permission.setGroupId(groupId);
            permission.setFunction(function);
            permission.setUserId(userId);

            return (Long) session.save(permission);
        };

    	return getHibernateTemplate().execute(hc);
    }

    @Deprecated
    public List<Permission> getPermissionsForGB(final Long gradebookId) throws IllegalArgumentException {

        if (gradebookId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForGB");
        }
    	HibernateCallback<List<Permission>> hc = session -> session
                .createQuery("from Permission as perm where perm.gradebookId = :gradebookId")
                .setLong("gradebookId", gradebookId)
                .list();
    	return getHibernateTemplate().execute(hc);
    }

    @Deprecated
    public void updatePermission(Collection perms)
    {
    	for(Iterator iter = perms.iterator(); iter.hasNext(); )
    	{
    		Permission perm = (Permission) iter.next();
    		if(perm != null)
    			updatePermission(perm);
    	}
    }

    @Deprecated
    public void updatePermission(final Permission perm) throws IllegalArgumentException {
        if (perm == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.updatePermission");
        }
        if (perm.getId() == null) {
            throw new IllegalArgumentException("Object is not persistent in BaseHibernateManager.updatePermission");
        }

    	HibernateCallback<Void> hc = session -> {
            session.update(perm);
            return null;
        };

    	getHibernateTemplate().execute(hc);
    }

    @Deprecated
    public void deletePermission(final Permission perm) throws IllegalArgumentException {
        if (perm == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.deletePermission");
        }
        if (perm.getId() == null) {
            throw new IllegalArgumentException("Object is not persistent in BaseHibernateManager.deletePermission");
        }

        HibernateCallback<Void> hc = session -> {
            session.delete(perm);
            return null;
        };

    	getHibernateTemplate().execute(hc);
    }

    public List<Permission> getPermissionsForUser(final Long gradebookId, final String userId) throws IllegalArgumentException {
    	if(gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUser");
        }

    	HibernateCallback<List<Permission>> hc = session -> session
                .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId")
                .setLong("gradebookId", gradebookId)
                .setString("userId", userId)
                .list();

    	return getHibernateTemplate().execute(hc);
    }

    public List<Permission> getPermissionsForUserForCategory(final Long gradebookId, final String userId, final List cateIds) throws IllegalArgumentException {
    	if(gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForCategory");
        }

        if (cateIds != null && cateIds.size() > 0) {
    		HibernateCallback<List<Permission>> hc = session -> session
                    .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId and perm.categoryId in (:cateIds)")
                    .setLong("gradebookId", gradebookId)
                    .setString("userId", userId)
                    .setParameterList("cateIds", cateIds)
                    .list();
    		return getHibernateTemplate().execute(hc);
    	}
        return null;
    }

    public List<Permission> getPermissionsForUserAnyCategory(final Long gradebookId, final String userId) throws IllegalArgumentException {
        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyCategory");
        }

    	HibernateCallback<List<Permission>> hc = session -> session
                .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId and perm.categoryId is null and perm.function in (:functions)")
                .setLong("gradebookId", gradebookId)
                .setString("userId", userId)
                .setParameterList("functions", GraderPermission.getStandardPermissions())
                .list();

    	return getHibernateTemplate().execute(hc);
    }

    public List<Permission> getPermissionsForUserAnyGroup(final Long gradebookId, final String userId) throws IllegalArgumentException {
        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroup");
        }

    	HibernateCallback<List<Permission>> hc = session -> session
                .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId and perm.groupId is null and perm.function in (:functions)")
                .setLong("gradebookId", gradebookId)
                .setString("userId", userId)
                .setParameterList("functions", GraderPermission.getStandardPermissions())
                .list();

    	return getHibernateTemplate().execute(hc);
    }

    public List<Permission> getPermissionsForUserAnyGroupForCategory(final Long gradebookId, final String userId, final List cateIds) throws IllegalArgumentException {
        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");
        }

        if (cateIds != null && cateIds.size() > 0) {
            HibernateCallback<List<Permission>> hc = session -> session
                    .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId and perm.categoryId in (:cateIds) and perm.groupId is null")
                    .setLong("gradebookId", gradebookId)
                    .setString("userId", userId)
                    .setParameterList("cateIds", cateIds)
                    .list();

    		return getHibernateTemplate().execute(hc);
    	}
        return null;
    }

    public List<Permission> getPermissionsForGBForCategoryIds(final Long gradebookId, final List cateIds) throws IllegalArgumentException {
        if (gradebookId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");
        }
        if (cateIds != null && cateIds.size() > 0) {
    		HibernateCallback<List<Permission>> hc = session -> session
                    .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.categoryId in (:cateIds)")
                    .setLong("gradebookId", gradebookId)
                    .setParameterList("cateIds", cateIds)
                    .list();

    		return getHibernateTemplate().execute(hc);
    	}
        return null;
    }

    public List<Permission> getPermissionsForUserAnyGroupAnyCategory(final Long gradebookId, final String userId) throws IllegalArgumentException {
        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");
        }

    	HibernateCallback<List<Permission>> hc = session -> session
                .createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.categoryId is null and perm.groupId is null")
                .setLong("gradebookId", gradebookId)
                .setString("userId", userId)
                .list();

    	return getHibernateTemplate().execute(hc);
    }

    public List<Permission> getPermissionsForUserForGoupsAnyCategory(final Long gradebookId, final String userId, final List groupIds) throws IllegalArgumentException {
        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForGoupsAnyCategory");
        }
    	if (groupIds != null && groupIds.size() > 0) {
	    	HibernateCallback<List<Permission>> hc = session -> session
                    .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId and perm.categoryId is null and perm.groupId in (:groupIds)")
                    .setLong("gradebookId", gradebookId)
                    .setString("userId", userId)
                    .setParameterList("groupIds", groupIds)
                    .list();

	    	return getHibernateTemplate().execute(hc);
    	}
        return null;
    }

    public List getPermissionsForUserForGroup(final Long gradebookId, final String userId, final List groupIds) throws IllegalArgumentException {
        if (gradebookId == null || userId == null) {
            throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForGroup");
        }
    	if (groupIds != null && groupIds.size() > 0) {
	    	HibernateCallback<List<Permission>> hc = session -> session
                    .createQuery("from Permission as perm where perm.gradebookId = :gradebookId and perm.userId = :userId and perm.groupId in (:groupIds) ")
                    .setLong("gradebookId", gradebookId)
                    .setString("userId", userId)
                    .setParameterList("groupIds", groupIds)
                    .list();

	    	return getHibernateTemplate().execute(hc);
    	}
        return null;
    }

    public boolean isAssignmentDefined(Long gradableObjectId) {
        HibernateCallback<Number> hc = session -> (Number) session.createCriteria(GradebookAssignment.class)
                    .add(Restrictions.eq("id", gradableObjectId))
                    .add(Restrictions.eq("removed", false))
                    .setProjection(Projections.rowCount())
                    .uniqueResult();
        return getHibernateTemplate().execute(hc).intValue() == 1;
    }

    /**
     *
     * @param gradableObjectId
     * @return the GradebookAssignment object with the given id
     */
    public GradebookAssignment getAssignment(Long gradableObjectId) {
        return getHibernateTemplate().load(GradebookAssignment.class, gradableObjectId);
    }

    /**
     *
     * @param doublePointsPossible
     * @param doublePointsEarned
     * @return the % equivalent for the given points possible and points earned
     */
    protected Double calculateEquivalentPercent(Double doublePointsPossible, Double doublePointsEarned) {

    	if (doublePointsEarned == null || doublePointsPossible == null)
    		return null;

    	// scale to handle points stored as repeating decimals
    	BigDecimal pointsEarned = new BigDecimal(doublePointsEarned.toString());
    	BigDecimal pointsPossible = new BigDecimal(doublePointsPossible.toString());

    	// Avoid dividing by zero
    	if (pointsEarned.compareTo(BigDecimal.ZERO) == 0 || pointsPossible.compareTo(BigDecimal.ZERO) == 0) {
    		return new Double(0);
    	}

    	BigDecimal equivPercent = pointsEarned.divide(pointsPossible, GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
    	return Double.valueOf(equivPercent.doubleValue());

    }

    /**
     * Converts points to percentage for the given AssignmentGradeRecords
     * @param gradebook
     * @param studentRecordsFromDB
     * @return
     */
    protected List convertPointsToPercentage(Gradebook gradebook, List studentRecordsFromDB)
    {
    	List percentageList = new ArrayList();
    	for(int i=0; i < studentRecordsFromDB.size(); i++)
    	{
    		AssignmentGradeRecord agr = (AssignmentGradeRecord) studentRecordsFromDB.get(i);
    		if (agr != null) {
    			Double pointsPossible = agr.getAssignment().getPointsPossible();
    			if (pointsPossible == null || agr.getPointsEarned() == null) {
    				agr.setPercentEarned(null);
        			percentageList.add(agr);
    			} else {
        			agr.setDateRecorded(agr.getDateRecorded());
        			agr.setGraderId(agr.getGraderId());
        			agr.setPercentEarned(calculateEquivalentPercent(pointsPossible, agr.getPointsEarned()));
        			percentageList.add(agr);
    			}
    		}
    	}
    	return percentageList;
    }

    /**
     * Converts points to letter grade for the given AssignmentGradeRecords
     * @param gradebook
     * @param studentRecordsFromDB
     * @return
     */
    protected List convertPointsToLetterGrade(Gradebook gradebook, List studentRecordsFromDB)
    {
    	List letterGradeList = new ArrayList();
    	LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);
    	for(int i=0; i < studentRecordsFromDB.size(); i++)
    	{
    		AssignmentGradeRecord agr = (AssignmentGradeRecord) studentRecordsFromDB.get(i);
    		if(agr != null) {
        		Double pointsPossible = agr.getAssignment().getPointsPossible();
        		agr.setDateRecorded(agr.getDateRecorded());
        		agr.setGraderId(agr.getGraderId());
    			if (pointsPossible == null || agr.getPointsEarned() == null) {
    				agr.setLetterEarned(null);
        			letterGradeList.add(agr);
    			} else {
    				String letterGrade = lgpm.getGrade(calculateEquivalentPercent(pointsPossible, agr.getPointsEarned()));
        			agr.setLetterEarned(letterGrade);
        			letterGradeList.add(agr);
    			}
    		}
    	}
    	return letterGradeList;
    }

    protected Double calculateEquivalentPointValueForPercent(Double doublePointsPossible, Double doublePercentEarned) {
    	if (doublePointsPossible == null || doublePercentEarned == null)
    		return null;

    	BigDecimal pointsPossible = new BigDecimal(doublePointsPossible.toString());
		BigDecimal percentEarned = new BigDecimal(doublePercentEarned.toString());
		BigDecimal equivPoints = pointsPossible.multiply(percentEarned.divide(new BigDecimal("100"), GradebookService.MATH_CONTEXT));
		return equivPoints.doubleValue();
    }

    public List<Comment> getComments(final GradebookAssignment assignment, final Collection studentIds) {
    	if (studentIds.isEmpty()) {
    		return new ArrayList<>();
    	}
        HibernateCallback<List<Comment>> hc = session -> session.createCriteria(Comment.class)
                        .add(Restrictions.eq("gradableObject", assignment))
                        .add(HibernateCriterionUtils.CriterionInRestrictionSplitter("studentId", studentIds))
                        .list();
    	return getHibernateTemplate().execute(hc);
    }

    protected Map<String, Set<GradebookAssignment>> getVisibleExternalAssignments(Gradebook gradebook, Collection<String> studentIds, List<GradebookAssignment> assignments) {
        String gradebookUid = gradebook.getUid();
        Map<String, List<String>> allExternals = externalAssessmentService.getVisibleExternalAssignments(gradebookUid, studentIds);
        Map<String, GradebookAssignment> allRequested = new HashMap<String, GradebookAssignment>();

        for (GradebookAssignment a : assignments) {
            if (a.isExternallyMaintained()) {
                allRequested.put(a.getExternalId(), a);
            }
        }

        Map<String, Set<GradebookAssignment>> visible = new HashMap<String, Set<GradebookAssignment>>();
        for (String studentId : allExternals.keySet()) {
            if (studentIds.contains(studentId)) {
                Set<GradebookAssignment> studentAssignments = new HashSet<GradebookAssignment>();
                for (String assignmentId : allExternals.get(studentId)) {
                    if (allRequested.containsKey(assignmentId)) {
                        studentAssignments.add(allRequested.get(assignmentId));
                    }
                }
                visible.put(studentId, studentAssignments);
            }
        }
        return visible;
    }

	// NOTE: This should not be called in a loop. Anything for sets should use getVisibleExternalAssignments
	protected boolean studentCanView(String studentId, GradebookAssignment assignment) {
		if (assignment.isExternallyMaintained()) {
			try {
				String gbUid = assignment.getGradebook().getUid();
				String extId = assignment.getExternalId();

				if (externalAssessmentService.isExternalAssignmentGrouped(gbUid, extId)) {
					return externalAssessmentService.isExternalAssignmentVisible(gbUid, extId, studentId);
				}
			} catch (GradebookNotFoundException e) {
				if (log.isDebugEnabled()) { log.debug("Bogus graded assignment checked for course grades: " + assignment.getId()); }
			}
		}

		// We assume that the only disqualifying condition is that the external assignment
		// is grouped and the student is not a member of one of the groups allowed.
		return true;
	}

    protected void finalizeNullGradeRecords(final Gradebook gradebook) {
    	final Set<String> studentUids = getAllStudentUids(gradebook.getUid());
		final Date now = new Date();
		final String graderId = getAuthn().getUserUid();

        getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            List<GradebookAssignment> countedAssignments = session
                    .createQuery("from GradebookAssignment as asn where asn.gradebook.id = :gb and asn.removed is false and asn.notCounted is false and asn.ungraded is false")
                    .setLong("gb", gradebook.getId())
                    .list();

            Map<String, Set<GradebookAssignment>> visible = getVisibleExternalAssignments(gradebook, studentUids, countedAssignments);

            for (GradebookAssignment assignment : countedAssignments) {
                List<AssignmentGradeRecord> scoredGradeRecords = session
                        .createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.id = :go")
                        .setLong("go", assignment.getId())
                        .list();

                Map<String, AssignmentGradeRecord> studentToGradeRecordMap = new HashMap<>();
                for (AssignmentGradeRecord scoredGradeRecord : scoredGradeRecords) {
                    studentToGradeRecordMap.put(scoredGradeRecord.getStudentId(), scoredGradeRecord);
                }

                for (String studentUid : studentUids) {
                    // SAK-11485 - We don't want to add scores for those grouped activities
                    //             that this student should not see or be scored on.
                    if (assignment.isExternallyMaintained() && (!visible.containsKey(studentUid) || !visible.get(studentUid).contains(assignment))) {
                        continue;
                    }
                    AssignmentGradeRecord gradeRecord = studentToGradeRecordMap.get(studentUid);
                    if (gradeRecord != null) {
                        if (gradeRecord.getPointsEarned() == null) {
                            gradeRecord.setPointsEarned(0d);
                        } else {
                            continue;
                        }
                    } else {
                        gradeRecord = new AssignmentGradeRecord(assignment, studentUid, 0d);
                    }
                    gradeRecord.setGraderId(graderId);
                    gradeRecord.setDateRecorded(now);
                    session.saveOrUpdate(gradeRecord);
                    session.save(new GradingEvent(assignment, graderId, studentUid, gradeRecord.getPointsEarned()));
                }
            }
            return null;
        });
    }

    /**
     *
     * @param name the assignment name (will not be trimmed)
     * @param gradebook the gradebook to check
     * @return true if an assignment with the given name already exists in this gradebook.
     */
    protected boolean assignmentNameExists(String name, Gradebook gradebook)
    {
        Number count = (Number) getHibernateTemplate().execute(session -> session
                .createCriteria(GradableObject.class)
                .createAlias("gradebook", "gb")
                .add(Restrictions.eq("name", name))
                .add(Restrictions.eq("gb.uid", gradebook.getUid()))
                .add(Restrictions.eq("removed", false))
                .setProjection(Projections.rowCount())
                .uniqueResult());
        return count.intValue() > 0;
    }

	private Comment getInternalComment(String gradebookUid, Long assignmentId, String studentUid) {
		return (Comment) getHibernateTemplate().execute(session -> session
                .createQuery("from Comment as c where c.studentId = :studentId and c.gradableObject.gradebook.uid = :gradebookUid and c.gradableObject.id = :assignmentId and gradableObject.removed is false")
                .setString("studentId", studentUid)
                .setString("gradebookUid", gradebookUid)
                .setLong("assignmentId", assignmentId)
                .uniqueResult());
	}

	public CommentDefinition getAssignmentScoreComment(final String gradebookUid, final Long assignmentId, final String studentUid) throws GradebookNotFoundException, AssessmentNotFoundException {
		if (gradebookUid == null || assignmentId == null || studentUid == null) {
			throw new IllegalArgumentException("null parameter passed to getAssignmentScoreComment. Values are gradebookUid:" + gradebookUid + " assignmentId:" + assignmentId + " studentUid:"+ studentUid);
		}
		GradebookAssignment assignment = getAssignmentWithoutStats(gradebookUid, assignmentId);
		if (assignment == null) {
			throw new AssessmentNotFoundException("There is no assignmentId " + assignmentId + " for gradebookUid " + gradebookUid);
		}

		CommentDefinition commentDefinition = null;
        Comment comment = getInternalComment(gradebookUid, assignmentId, studentUid);
        if (comment != null) {
        	commentDefinition = new CommentDefinition();
        	commentDefinition.setAssignmentName(assignment.getName());
        	commentDefinition.setCommentText(comment.getCommentText());
        	commentDefinition.setDateRecorded(comment.getDateRecorded());
        	commentDefinition.setGraderUid(comment.getGraderId());
        	commentDefinition.setStudentUid(comment.getStudentId());
        }
		return commentDefinition;
	}

	public void setAssignmentScoreComment(final String gradebookUid, final Long assignmentId, final String studentUid, final String commentText) throws GradebookNotFoundException, AssessmentNotFoundException {
		getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            Comment comment = getInternalComment(gradebookUid, assignmentId, studentUid);
            if (comment == null) {
                comment = new Comment(studentUid, commentText, getAssignmentWithoutStats(gradebookUid, assignmentId));
            } else {
                comment.setCommentText(commentText);
            }
            comment.setGraderId(authn.getUserUid());
            comment.setDateRecorded(new Date());
            session.saveOrUpdate(comment);
            return null;
        });
	}

	public void updateGradeMapping(final Long gradeMappingId, final Map<String, Double> gradeMap){
		getHibernateTemplate().execute((HibernateCallback<Void>) session -> {
            GradeMapping gradeMapping = (GradeMapping)session.load(GradeMapping.class, gradeMappingId);
            gradeMapping.setGradeMap(gradeMap);
            session.update(gradeMapping);
            session.flush();
            return null;
        });
	}

	 /**
     * Get's all course grade overrides for a given gradebook
     *
     * @param gradebook The gradebook
     * @return A list of {@link CourseGradeRecord} that have overrides
     *
     * @throws HibernateException
     */
    protected List<CourseGradeRecord> getCourseGradeOverrides(Gradebook gradebook) throws HibernateException {
        return getHibernateTemplate().execute(session -> session
                .createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.gradebook = :gradebook and cgr.enteredGrade is not null")
                .setEntity("gradebook", gradebook)
                .list());
    }
}
