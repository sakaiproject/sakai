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
 *       http://www.osedu.org/licenses/ECL-2.0
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingCategoryNameException;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Comment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookProperty;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.LetterGradePercentMapping;
import org.sakaiproject.tool.gradebook.Permission;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.EventTrackingService;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Provides methods which are shared between service business logic and application business
 * logic, but not exposed to external callers.
 */
public abstract class BaseHibernateManager extends HibernateDaoSupport {
	private static final Log log = LogFactory.getLog(BaseHibernateManager.class);

    // Oracle will throw a SQLException if we put more than this into a
    // "WHERE tbl.col IN (:paramList)" query.
    public static int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;

    protected SectionAwareness sectionAwareness;
    protected Authn authn;
    protected EventTrackingService eventTrackingService;

    // Local cache of static-between-deployment properties.
    protected Map propertiesMap = new HashMap();

    public Gradebook getGradebook(String uid) throws GradebookNotFoundException {
    	List list = getHibernateTemplate().find("from Gradebook as gb where gb.uid=?",
    		uid);
		if (list.size() == 1) {
			return (Gradebook)list.get(0);
		} else {
            throw new GradebookNotFoundException("Could not find gradebook uid=" + uid);
        }
    }

    public boolean isGradebookDefined(String gradebookUid) {
        String hql = "from Gradebook as gb where gb.uid=?";
        return getHibernateTemplate().find(hql, gradebookUid).size() == 1;
    }

    protected List getAssignments(Long gradebookId, Session session) throws HibernateException {
        List assignments = session.createQuery(
        	"from Assignment as asn where asn.gradebook.id=? and asn.removed=false").
        	setLong(0, gradebookId.longValue()).
        	list();
        return assignments;
    }

    protected List getCountedStudentGradeRecords(Long gradebookId, String studentId, Session session) throws HibernateException {
        return session.createQuery(
        	"select agr from AssignmentGradeRecord as agr, Assignment as asn where agr.studentId=? and agr.gradableObject=asn and asn.removed=false and asn.notCounted=false and asn.gradebook.id=?" +
        	" and asn.ungraded=false").
        	setString(0, studentId).
        	setLong(1, gradebookId.longValue()).
        	list();
    }

    /**
     */
    public CourseGrade getCourseGrade(Long gradebookId) {
        return (CourseGrade)getHibernateTemplate().find(
                "from CourseGrade as cg where cg.gradebook.id=?",
                gradebookId).get(0);
    }

    /**
     * Gets the course grade record for a student, or null if it does not yet exist.
     *
     * @param studentId The student ID
     * @param session The hibernate session
     * @return A List of grade records
     *
     * @throws HibernateException
     */
    protected CourseGradeRecord getCourseGradeRecord(Gradebook gradebook,
            String studentId, Session session) throws HibernateException {
        return (CourseGradeRecord)session.createQuery(
        	"from CourseGradeRecord as cgr where cgr.studentId=? and cgr.gradableObject.gradebook=?").
        	setString(0, studentId).
        	setEntity(1, gradebook).
        	uniqueResult();
    }

    public String getGradebookUid(Long id) {
        return ((Gradebook)getHibernateTemplate().load(Gradebook.class, id)).getUid();
    }

	protected Set getAllStudentUids(String gradebookUid) {
		List enrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
        Set studentUids = new HashSet();
        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
            studentUids.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
        }
        return studentUids;
	}

	protected Map getPropertiesMap() {

		return propertiesMap;
	}

	public String getPropertyValue(final String name) {
		String value = (String)propertiesMap.get(name);
		if (value == null) {
			List list = getHibernateTemplate().find("from GradebookProperty as prop where prop.name=?",
				name);
			if (!list.isEmpty()) {
				GradebookProperty property = (GradebookProperty)list.get(0);
				value = property.getValue();
				propertiesMap.put(name, value);
			}
		}
		return value;
	}
	public void setPropertyValue(final String name, final String value) {
		GradebookProperty property;
		List list = getHibernateTemplate().find("from GradebookProperty as prop where prop.name=?",
			name);
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

	protected Assignment getAssignmentWithoutStats(String gradebookUid, String assignmentName, Session session) throws HibernateException {
		return (Assignment)session.createQuery(
			"from Assignment as asn where asn.name=? and asn.gradebook.uid=? and asn.removed=false").
			setString(0, assignmentName).
			setString(1, gradebookUid).
			uniqueResult();
	}
	
	protected Assignment getAssignmentWithoutStats(String gradebookUid, Long assignmentId, Session session) throws HibernateException {
		return (Assignment)session.createQuery(
			"from Assignment as asn where asn.id=? and asn.gradebook.uid=? and asn.removed=false").
			setLong(0, assignmentId).
			setString(1, gradebookUid).
			uniqueResult();
	}

	protected void updateAssignment(Assignment assignment, Session session)
		throws ConflictingAssignmentNameException, HibernateException {
		// Ensure that we don't have the assignment in the session, since
		// we need to compare the existing one in the db to our edited assignment
		session.evict(assignment);

		Assignment asnFromDb = (Assignment)session.load(Assignment.class, assignment.getId());
		List conflictList = ((List)session.createQuery(
				"select go from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false and go.id != ?").
				setString(0, assignment.getName()).
				setEntity(1, assignment.getGradebook()).
				setLong(2, assignment.getId().longValue()).list());
		int numNameConflicts = conflictList.size();
		if(numNameConflicts > 0) {
			throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
		}

		session.evict(asnFromDb);
		session.update(assignment);
	}

    protected AssignmentGradeRecord getAssignmentGradeRecord(Assignment assignment, String studentUid, Session session) throws HibernateException {
		return (AssignmentGradeRecord)session.createQuery(
			"from AssignmentGradeRecord as agr where agr.studentId=? and agr.gradableObject.id=?").
			setString(0, studentUid).
			setLong(1, assignment.getId().longValue()).
			uniqueResult();
	}

    public Long createAssignment(final Long gradebookId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased) throws ConflictingAssignmentNameException, StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
                List conflictList = ((List)session.createQuery(
                        "select go from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false").
                        setString(0, name).
                        setEntity(1, gb).list());
            		int numNameConflicts = conflictList.size();
                if(numNameConflicts > 0) {
                    throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
                }

                   Assignment asn = new Assignment();
                   asn.setGradebook(gb);
                   asn.setName(name.trim());
                   asn.setPointsPossible(points);
                   asn.setDueDate(dueDate);
             			 asn.setUngraded(false);
                   if (isNotCounted != null) {
                       asn.setNotCounted(isNotCounted.booleanValue());
                   }

                   if(isReleased!=null){
                       asn.setReleased(isReleased.booleanValue());
                   }

                   // Save the new assignment
                   Long id = (Long)session.save(asn);

                   return id;
               }
           };

           return (Long)getHibernateTemplate().execute(hc);
    }

    public void updateGradebook(final Gradebook gradebook) throws StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                // Get the gradebook and selected mapping from persistence
                Gradebook gradebookFromPersistence = (Gradebook)session.load(
                        gradebook.getClass(), gradebook.getId());
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
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public boolean isExplicitlyEnteredCourseGradeRecords(final Long gradebookId) {
        final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
        if (studentUids.isEmpty()) {
            return false;
        }

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Integer total;
                if (studentUids.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
                    Query q = session.createQuery(
                            "select cgr from CourseGradeRecord as cgr where cgr.enteredGrade is not null and cgr.gradableObject.gradebook.id=:gradebookId and cgr.studentId in (:studentUids)");
                    q.setLong("gradebookId", gradebookId.longValue());
                    q.setParameterList("studentUids", studentUids);
                    List totalList = (List)q.list();
                    total = Integer.valueOf(totalList.size());
                    if (log.isDebugEnabled()) log.debug("total number of explicitly entered course grade records = " + total);
                } else {
                    total = Integer.valueOf(0);
                    Query q = session.createQuery(
                            "select cgr.studentId from CourseGradeRecord as cgr where cgr.enteredGrade is not null and cgr.gradableObject.gradebook.id=:gradebookId");
                    q.setLong("gradebookId", gradebookId.longValue());
                    for (Iterator iter = q.list().iterator(); iter.hasNext(); ) {
                        String studentId = (String)iter.next();
                        if (studentUids.contains(studentId)) {
                            total = Integer.valueOf(1);
                            break;
                        }
                    }
                }
                return total;
            }
        };
        return ((Integer)getHibernateTemplate().execute(hc)).intValue() > 0;
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

    protected EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public void postEvent(String message,String objectReference){        
       eventTrackingService.postEvent(message,objectReference);
    }

    public Long createCategory(final Long gradebookId, final String name, final Double weight, final int drop_lowest) 
    throws ConflictingCategoryNameException, StaleObjectModificationException {
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
    			List conflictList = ((List)session.createQuery(
    					"select ca from Category as ca where ca.name = ? and ca.gradebook = ? and ca.removed=false ").
    					setString(0, name).
    					setEntity(1, gb).list());
    			int numNameConflicts = conflictList.size();
    			if(numNameConflicts > 0) {
    				throw new ConflictingCategoryNameException("You can not save multiple catetories in a gradebook with the same name");
    			}
    			if(weight > 1 || weight < 0)
    			{
    				throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in createCategory of BaseHibernateManager");
    			}

    			Category ca = new Category();
    			ca.setGradebook(gb);
    			ca.setName(name);
    			ca.setWeight(weight);
    			ca.setDrop_lowest(drop_lowest);
    			ca.setRemoved(false);

    			Long id = (Long)session.save(ca);

    			return id;
    		}
    	};

    	return (Long)getHibernateTemplate().execute(hc);
    }

    public List getCategories(final Long gradebookId) throws HibernateException {
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			List categories = session.createQuery(
					"from Category as ca where ca.gradebook=? and ca.removed=false").
					setLong(0, gradebookId.longValue()).
					list();
    			return categories;
    		}
    	};
    	return (List) getHibernateTemplate().execute(hc);
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
    
    public Long createAssignmentForCategory(final Long gradebookId, final Long categoryId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased)
    throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException
    {
    	if(gradebookId == null || categoryId == null)
    	{
    		throw new IllegalArgumentException("gradebookId or categoryId is null in BaseHibernateManager.createAssignmentForCategory");
    	}
    	
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
    			Category cat = (Category)session.load(Category.class, categoryId);
    			List conflictList = ((List)session.createQuery(
    					"select go from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false").
    					setString(0, name).
    					setEntity(1, gb).list());
    			int numNameConflicts = conflictList.size();
    			if(numNameConflicts > 0) {
    				throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
    			}

    			Assignment asn = new Assignment();
    			asn.setGradebook(gb);
    			asn.setCategory(cat);
    			asn.setName(name.trim());
    			asn.setPointsPossible(points);
    			asn.setDueDate(dueDate);
    			asn.setUngraded(false);
    			if (isNotCounted != null) {
    				asn.setNotCounted(isNotCounted.booleanValue());
    			}

    			if(isReleased!=null){
    				asn.setReleased(isReleased.booleanValue());
    			}

    			Long id = (Long)session.save(asn);

    			return id;
    		}
    	};

    	return (Long)getHibernateTemplate().execute(hc);
    }
    
    public List getAssignmentsForCategory(final Long categoryId) throws HibernateException{
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			List assignments = session.createQuery(
					"from Assignment as assign where assign.category=? and assign.removed=false").
					setLong(0, categoryId.longValue()).
					list();
    			return assignments;
    		}
    	};
    	return (List) getHibernateTemplate().execute(hc);
    }
    
    public Category getCategory(final Long categoryId) throws HibernateException{
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			return session.createQuery(
    			"from Category as cat where cat.id=?").
    			setLong(0, categoryId.longValue()).
    			uniqueResult();
    		}
    	};
    	return (Category) getHibernateTemplate().execute(hc);
    }
    
    public void updateCategory(final Category category) throws ConflictingCategoryNameException, StaleObjectModificationException{
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			session.evict(category);
    			Category persistentCat = (Category)session.load(Category.class, category.getId());
    			List conflictList = ((List)session.createQuery(
    			"select ca from Category as ca where ca.name = ? and ca.gradebook = ? and ca.id != ? and ca.removed=false").
    			setString(0, category.getName()).
    			setEntity(1, category.getGradebook()).
    			setLong(2, category.getId().longValue()).list());
    			int numNameConflicts = conflictList.size();
    			if(numNameConflicts > 0) {
    				throw new ConflictingCategoryNameException("You can not save multiple category in a gradebook with the same name");
    			}
    			if(category.getWeight().doubleValue() > 1 || category.getWeight().doubleValue() < 0)
    			{
    				throw new IllegalArgumentException("weight for category is greater than 1 or less than 0 in updateCategory of BaseHibernateManager");
    			}
    			session.evict(persistentCat);
    			session.update(category);
    			return null;
    		}
    	};
    	try {
    		getHibernateTemplate().execute(hc);
    	} catch (Exception e) {
    		throw new StaleObjectModificationException(e);
    	}
    }
    
    public void removeCategory(final Long categoryId) throws StaleObjectModificationException{
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Category persistentCat = (Category)session.load(Category.class, categoryId);

    			List assigns = getAssignmentsForCategory(categoryId);
    			for(Iterator iter = assigns.iterator(); iter.hasNext();)
    			{
    				Assignment assignment = (Assignment) iter.next();
    				assignment.setCategory(null);
    				updateAssignment(assignment, session);
    			}

    			persistentCat.setRemoved(true);
    			session.update(persistentCat);
    			return null;
    		}
    	};
    	try {
    		getHibernateTemplate().execute(hc);
    	} catch (Exception e) {
    		throw new StaleObjectModificationException(e);
    	}
    }
    
    public LetterGradePercentMapping getDefaultLetterGradePercentMapping()
    {
    	HibernateCallback hc = new HibernateCallback() 
    	{
    		public Object doInHibernate(Session session) throws HibernateException 
    		{
    			List defaultMapping = (session.createQuery(
    			"select lgpm from LetterGradePercentMapping as lgpm where lgpm.mappingType = 1")).list();
    			if(defaultMapping == null || defaultMapping.size() == 0) 
    			{
    				log.info("Default letter grade mapping hasn't been created in DB in BaseHibernateManager.getDefaultLetterGradePercentMapping");
    				return null;
    			}
    			if(defaultMapping.size() > 1) 
    			{
    				log.error("Duplicate default letter grade mapping was created in DB in BaseHibernateManager.getDefaultLetterGradePercentMapping");
    				return null;
    			}

    			return ((LetterGradePercentMapping) defaultMapping.get(0));

    		}
    	};

    	return (LetterGradePercentMapping) getHibernateTemplate().execute(hc);
    }
    
    public void createOrUpdateDefaultLetterGradePercentMapping(final Map gradeMap)
    {
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
    
    private void updateDefaultLetterGradePercentMapping(final Map gradeMap, final LetterGradePercentMapping lgpm)
    {
  		Set keySet = gradeMap.keySet();

  		if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
  			throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.updateDefaultLetterGradePercentMapping");

  		if(validateLetterGradeMapping(gradeMap) == false)
  			throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.updateDefaultLetterGradePercentMapping");

  		HibernateCallback hcb = new HibernateCallback()
  		{
  			public Object doInHibernate(Session session) throws HibernateException,
  			SQLException
  			{
  				Map saveMap = new HashMap();
  				for(Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();)
  				{
  					String key = (String) iter.next();
  					saveMap.put(key, gradeMap.get(key));
  				}
  				lgpm.setGradeMap(saveMap);
  				session.update(lgpm);          
  				return null;
  			}
  		}; 
  		getHibernateTemplate().execute(hcb);
    }
    
    public void createDefaultLetterGradePercentMapping(final Map gradeMap)
    {
    	if(getDefaultLetterGradePercentMapping() != null)
    		throw new IllegalArgumentException("gradeMap has already been created in BaseHibernateManager.createDefaultLetterGradePercentMapping");
    	
    	if(gradeMap == null)
    		throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.createDefaultLetterGradePercentMapping");
    	
    	Set keySet = gradeMap.keySet();
    	
    	if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
    		throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.createDefaultLetterGradePercentMapping");
    	
    	if(validateLetterGradeMapping(gradeMap) == false)
    		throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.createDefaultLetterGradePercentMapping");
    	
      HibernateCallback hcb = new HibernateCallback()
      {
        public Object doInHibernate(Session session) throws HibernateException,
            SQLException
        {
        	LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
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
            session.save(lgpm);          
          }           
          return null;
        }
      }; 
      getHibernateTemplate().execute(hcb);
    }
    
    public LetterGradePercentMapping getLetterGradePercentMapping(final Gradebook gradebook)
    {
    	HibernateCallback hc = new HibernateCallback() 
    	{
    		public Object doInHibernate(Session session) throws HibernateException 
    		{
    			LetterGradePercentMapping mapping = (LetterGradePercentMapping) ((session.createQuery(
    			"from LetterGradePercentMapping as lgpm where lgpm.gradebookId=:gradebookId and lgpm.mappingType=2")).
    			setLong("gradebookId", gradebook.getId().longValue())).
    			uniqueResult();
    			if(mapping == null ) 
    			{
    				LetterGradePercentMapping lgpm = getDefaultLetterGradePercentMapping();
    				LetterGradePercentMapping returnLgpm = new LetterGradePercentMapping();
    				returnLgpm.setGradebookId(gradebook.getId());
    				returnLgpm.setGradeMap(lgpm.getGradeMap());
    				returnLgpm.setMappingType(2);
    				return returnLgpm;
    			}
    			return mapping;

    		}
    	};

    	return (LetterGradePercentMapping) getHibernateTemplate().execute(hc);
    }
    
    /**
     * this method is different with getLetterGradePercentMapping - 
     * it returns null if no mapping exists for gradebook instead of
     * returning default mapping.
     */
    private LetterGradePercentMapping getLetterGradePercentMappingForGradebook(final Gradebook gradebook)
    {
    	HibernateCallback hc = new HibernateCallback() 
    	{
    		public Object doInHibernate(Session session) throws HibernateException 
    		{
    			LetterGradePercentMapping mapping = (LetterGradePercentMapping) ((session.createQuery(
    			"from LetterGradePercentMapping as lgpm where lgpm.gradebookId=:gradebookId and lgpm.mappingType=2")).
    			setLong("gradebookId", gradebook.getId().longValue())).
    			uniqueResult();
    			return mapping;

    		}
    	};

    	return (LetterGradePercentMapping) getHibernateTemplate().execute(hc);
    }
    
    public void saveOrUpdateLetterGradePercentMapping(final Map gradeMap, final Gradebook gradebook)
    {
    	if(gradeMap == null)
    		throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");

    	LetterGradePercentMapping lgpm = getLetterGradePercentMappingForGradebook(gradebook);

    	if(lgpm == null)
    	{
    		Set keySet = gradeMap.keySet();

    		if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
    			throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");

    		if(validateLetterGradeMapping(gradeMap) == false)
    			throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.saveOrUpdateLetterGradePercentMapping");

    		HibernateCallback hcb = new HibernateCallback()
    		{
    			public Object doInHibernate(Session session) throws HibernateException,
    			SQLException
    			{
    				LetterGradePercentMapping lgpm = new LetterGradePercentMapping();
    				if (lgpm != null)
    				{                    
    					Map saveMap = new HashMap();
    					for (Iterator gradeIter = gradeMap.keySet().iterator(); gradeIter.hasNext();) {
    						String letterGrade = (String)gradeIter.next();
    						Double value = (Double)gradeMap.get(letterGrade);
    						saveMap.put(letterGrade, value);
    					}
    					
    					lgpm.setGradeMap(saveMap);
    					lgpm.setGradebookId(gradebook.getId());
    					lgpm.setMappingType(2);
    					session.save(lgpm);
    				}
    				return null;
    			}
    		}; 
    		getHibernateTemplate().execute(hcb);
    	}
    	else
    	{
    		udpateLetterGradePercentMapping(gradeMap, gradebook);
    	}
    }
    
    private void udpateLetterGradePercentMapping(final Map gradeMap, final Gradebook gradebook)
    {
      HibernateCallback hcb = new HibernateCallback()
      {
      	public Object doInHibernate(Session session) throws HibernateException, SQLException
      	{
      		LetterGradePercentMapping lgpm = getLetterGradePercentMapping(gradebook);

      		if( lgpm == null)
      			throw new IllegalArgumentException("LetterGradePercentMapping is null in BaseHibernateManager.updateLetterGradePercentMapping");

      		if(gradeMap == null)
      			throw new IllegalArgumentException("gradeMap is null in BaseHibernateManager.updateLetterGradePercentMapping");

      		Set keySet = gradeMap.keySet();

      		if(keySet.size() != GradebookService.validLetterGrade.length) //we only consider letter grade with -/+ now.
      			throw new IllegalArgumentException("gradeMap doesn't have right size in BaseHibernateManager.udpateLetterGradePercentMapping");

      		if(validateLetterGradeMapping(gradeMap) == false)
      			throw new IllegalArgumentException("gradeMap contains invalid letter in BaseHibernateManager.udpateLetterGradePercentMapping");

        	Map saveMap = new HashMap();
        	for(Iterator iter = gradeMap.keySet().iterator(); iter.hasNext();)
        	{
        		String key = (String) iter.next();
        		saveMap.put(key, gradeMap.get(key));
        	}

        	lgpm.setGradeMap(saveMap);
      		session.save(lgpm);
      		
      		return null;
      	}
      }; 
      getHibernateTemplate().execute(hcb);
    }

    protected boolean validateLetterGradeMapping(Map gradeMap)
    {
    	Set keySet = gradeMap.keySet();

    	for(Iterator iter = keySet.iterator(); iter.hasNext(); )
    	{
    		String key = (String) iter.next();
    		boolean validLetter = false;
    		for(int i=0; i<GradebookService.validLetterGrade.length; i++)
    		{
    			if(key.equalsIgnoreCase(GradebookService.validLetterGrade[i]))
    			{
    				validLetter = true;
    				break;
    			}
    		}
    		if(validLetter == false)
    			return false;
    	}
    	return true;
    }
    
    public Long createUngradedAssignment(final Long gradebookId, final String name, 
    		final Date dueDate, final Boolean isNotCounted, final Boolean isReleased)
    throws ConflictingAssignmentNameException, StaleObjectModificationException
    {
    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
    			List conflictList = ((List)session.createQuery(
    			"select go from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false").
    			setString(0, name).
    			setEntity(1, gb).list());
    			int numNameConflicts = conflictList.size();
    			if(numNameConflicts > 0) {
    				throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
    			}

    			Assignment asn = new Assignment();
    			asn.setGradebook(gb);
    			asn.setName(name.trim());
    			asn.setDueDate(dueDate);
    			asn.setUngraded(true);
    			if (isNotCounted != null) {
    				asn.setNotCounted(isNotCounted.booleanValue());
    			}

    			if(isReleased!=null){
    				asn.setReleased(isReleased.booleanValue());
    			}

    			Long id = (Long)session.save(asn);

    			return id;
    		}
    	};
    	return (Long)getHibernateTemplate().execute(hc);
    }

    public Long createUngradedAssignmentForCategory(final Long gradebookId, final Long categoryId, 
    		final String name, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased)
    throws ConflictingAssignmentNameException, StaleObjectModificationException, IllegalArgumentException
    {
    	if(gradebookId == null || categoryId == null)
    	{
    		throw new IllegalArgumentException("gradebookId or categoryId is null in BaseHibernateManager.createUngradedAssignmentForCategory");
    	}

    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
    			Category cat = (Category)session.load(Category.class, categoryId);
    			List conflictList = ((List)session.createQuery(
    			"select go from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false").
    			setString(0, name).
    			setEntity(1, gb).list());
    			int numNameConflicts = conflictList.size();
    			if(numNameConflicts > 0) {
    				throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
    			}

    			Assignment asn = new Assignment();
    			asn.setGradebook(gb);
    			asn.setCategory(cat);
    			asn.setName(name.trim());
    			asn.setDueDate(dueDate);
    			asn.setUngraded(true);
    			if (isNotCounted != null) {
    				asn.setNotCounted(isNotCounted.booleanValue());
    			}

    			if(isReleased!=null){
    				asn.setReleased(isReleased.booleanValue());
    			}

    			Long id = (Long)session.save(asn);

    			return id;
    		}
    	};

    	return (Long)getHibernateTemplate().execute(hc);
    }

    public Long addPermission(final Long gradebookId, final String userId, final String function, final Long categoryId, final String groupId)
    throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null || function == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.addPermission");
    	if(!function.equalsIgnoreCase(GradebookService.gradePermission) && !function.equalsIgnoreCase(GradebookService.viewPermission))
    		throw new IllegalArgumentException("Function is not grade or view in BaseHibernateManager.addPermission");

    	HibernateCallback hc = new HibernateCallback() 
    	{
    		public Object doInHibernate(Session session) throws HibernateException 
    		{
    			Permission permission = new Permission();
    			permission.setCategoryId(categoryId);
    			permission.setGradebookId(gradebookId);
    			permission.setGroupId(groupId);
    			permission.setFunction(function);
    			permission.setUserId(userId);

    			Long permissionId = (Long) session.save(permission);

    			return permissionId;
    		}
    	};

    	return (Long)getHibernateTemplate().execute(hc);
    }

    public List getPermissionsForGB(final Long gradebookId) throws IllegalArgumentException
    {
    	if(gradebookId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForGB");

    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId");
    			q.setLong("gradebookId", gradebookId);

    			return q.list();
    		}
    	};
    	return (List)getHibernateTemplate().execute(hc);
    }

    public void updatePermission(Collection perms)
    {
    	for(Iterator iter = perms.iterator(); iter.hasNext(); )
    	{
    		Permission perm = (Permission) iter.next();
    		if(perm != null)
    			updatePermission(perm);
    	}
    }

    public void updatePermission(final Permission perm) throws IllegalArgumentException
    {
    	if(perm == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.updatePermission");
    	if(perm.getId() == null)
    		throw new IllegalArgumentException("Object is not persistent in BaseHibernateManager.updatePermission");

    	HibernateCallback hc = new HibernateCallback() 
    	{
    		public Object doInHibernate(Session session) throws HibernateException 
    		{
    			session.update(perm);

    			return null;
    		}
    	};

    	getHibernateTemplate().execute(hc);
    }
    
    public void deletePermission(final Permission perm) throws IllegalArgumentException
    {
    	if(perm == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.deletePermission");
    	if(perm.getId() == null)
    		throw new IllegalArgumentException("Object is not persistent in BaseHibernateManager.deletePermission");
    	
    	HibernateCallback hc = new HibernateCallback() 
    	{
    		public Object doInHibernate(Session session) throws HibernateException 
    		{
    			session.delete(perm);

    			return null;
    		}
    	};

    	getHibernateTemplate().execute(hc);
    }

    public List getPermissionsForUser(final Long gradebookId, final String userId) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUser");

    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId");
    			q.setLong("gradebookId", gradebookId);
    			q.setString("userId", userId);

    			return q.list();
    		}
    	};
    	return (List)getHibernateTemplate().execute(hc);    	
    }

    public List getPermissionsForUserForCategory(final Long gradebookId, final String userId, final List cateIds) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForCategory");

    	if(cateIds != null && cateIds.size() > 0)
    	{
    		HibernateCallback hc = new HibernateCallback() {
    			public Object doInHibernate(Session session) throws HibernateException {
    				Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.categoryId in (:cateIds)");
    				q.setLong("gradebookId", gradebookId);
    				q.setString("userId", userId);
    				q.setParameterList("cateIds", cateIds);

    				return q.list();
    			}
    		};
    		return (List)getHibernateTemplate().execute(hc);
    	}
    	else
    	{
    		return null;
    	}
    }

    public List getPermissionsForUserAnyCategory(final Long gradebookId, final String userId) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyCategory");

    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.categoryId is null");
    			q.setLong("gradebookId", gradebookId);
    			q.setString("userId", userId);

    			return q.list();
    		}
    	};
    	return (List)getHibernateTemplate().execute(hc);
    }

    public List getPermissionsForUserAnyGroup(final Long gradebookId, final String userId) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroup");

    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.groupId is null");
    			q.setLong("gradebookId", gradebookId);
    			q.setString("userId", userId);

    			return q.list();
    		}
    	};
    	return (List)getHibernateTemplate().execute(hc);    	
    }
    
    public List getPermissionsForUserAnyGroupForCategory(final Long gradebookId, final String userId, final List cateIds) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");

    	if(cateIds != null && cateIds.size() > 0)
    	{
    		HibernateCallback hc = new HibernateCallback() {
    			public Object doInHibernate(Session session) throws HibernateException {
    				Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.categoryId in (:cateIds) and perm.groupId is null");
    				q.setLong("gradebookId", gradebookId);
    				q.setString("userId", userId);
    				q.setParameterList("cateIds", cateIds);

    				return q.list();
    			}
    		};
    		return (List)getHibernateTemplate().execute(hc);
    	}
    	else
    	{
    		return null;
    	}    	
    }
    
    public List getPermissionsForGBForCategoryIds(final Long gradebookId, final List cateIds) throws IllegalArgumentException
    {
    	if(gradebookId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");

    	if(cateIds != null && cateIds.size() > 0)
    	{
    		HibernateCallback hc = new HibernateCallback() {
    			public Object doInHibernate(Session session) throws HibernateException {
    				Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.categoryId in (:cateIds)");
    				q.setLong("gradebookId", gradebookId);
    				q.setParameterList("cateIds", cateIds);

    				return q.list();
    			}
    		};
    		return (List)getHibernateTemplate().execute(hc);
    	}
    	else
    	{
    		return null;
    	}    	
    }

    public List getPermissionsForUserAnyGroupAnyCategory(final Long gradebookId, final String userId) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserAnyGroupForCategory");

    	HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.categoryId is null and perm.groupId is null");
    			q.setLong("gradebookId", gradebookId);
    			q.setString("userId", userId);

    			return q.list();
    		}
    	};
    	return (List)getHibernateTemplate().execute(hc);
    }

    public List getPermissionsForUserForGoupsAnyCategory(final Long gradebookId, final String userId, final List groupIds) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForGoupsAnyCategory");
    	
    	if (groupIds != null && groupIds.size() > 0) {
	    	HibernateCallback hc = new HibernateCallback() {
	    		public Object doInHibernate(Session session) throws HibernateException {
	    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.categoryId is null and perm.groupId in (:groupIds) ");
	    			q.setLong("gradebookId", gradebookId);
	    			q.setString("userId", userId);
	    			q.setParameterList("groupIds", groupIds);
	
	    			return q.list();
	    		}
	    	};
	    	return (List)getHibernateTemplate().execute(hc);
    	} else {
    		return null;
    	}
    }
    
    public List getPermissionsForUserForGroup(final Long gradebookId, final String userId, final List groupIds) throws IllegalArgumentException
    {
    	if(gradebookId == null || userId == null)
    		throw new IllegalArgumentException("Null parameter(s) in BaseHibernateManager.getPermissionsForUserForGroup");
    	
    	if (groupIds != null && groupIds.size() > 0) {
	    	HibernateCallback hc = new HibernateCallback() {
	    		public Object doInHibernate(Session session) throws HibernateException {
	    			Query q = session.createQuery("from Permission as perm where perm.gradebookId=:gradebookId and perm.userId=:userId and perm.groupId in (:groupIds) ");
	    			q.setLong("gradebookId", gradebookId);
	    			q.setString("userId", userId);
	    			q.setParameterList("groupIds", groupIds);
	
	    			return q.list();
	    		}
	    	};
	    	return (List)getHibernateTemplate().execute(hc);  
    	} else {
    		return null;
    	}
    }
    
    public boolean isAssignmentDefined(Long gradableObjectId) {
        String hql = "from Assignment as asn where asn.id=? and removed=false";
        return getHibernateTemplate().find(hql, gradableObjectId).size() == 1;
    }
    
    /**
     * 
     * @param gradableObjectId
     * @return the Assignment object with the given id
     */
    public Assignment getAssignment(Long gradableObjectId) {
        return (Assignment)getHibernateTemplate().load(Assignment.class, gradableObjectId);
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
    		Double pointsPossible = agr.getAssignment().getPointsPossible();
    		agr.setDateRecorded(agr.getDateRecorded());
    		agr.setGraderId(agr.getGraderId());
    		if (agr != null) {
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
		return Double.valueOf(equivPoints.doubleValue());
    }
    
    public List getComments(final Assignment assignment, final Collection studentIds) {
    	if (studentIds.isEmpty()) {
    		return new ArrayList();
    	}
        return (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
            	List comments;
            	if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
            		Query q = session.createQuery(
            			"from Comment as c where c.gradableObject=:go and c.studentId in (:studentIds)");
                    q.setParameter("go", assignment);
                    q.setParameterList("studentIds", studentIds);
                    comments = q.list();
            	} else {
            		comments = new ArrayList();
            		Query q = session.createQuery("from Comment as c where c.gradableObject=:go");
            		q.setParameter("go", assignment);
            		List allComments = q.list();
            		for (Iterator iter = allComments.iterator(); iter.hasNext(); ) {
            			Comment comment = (Comment)iter.next();
            			if (studentIds.contains(comment.getStudentId())) {
            				comments.add(comment);
            			}
            		}
            	}
                return comments;
            }
        });
    }
    
    protected void finalizeNullGradeRecords(final Gradebook gradebook) {
    	final Set<String> studentUids = getAllStudentUids(gradebook.getUid());
		final Date now = new Date();
		final String graderId = getAuthn().getUserUid();
    	getHibernateTemplate().execute(new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			List<Assignment> countedAssignments = session.createQuery(
    				"from Assignment as asn where asn.gradebook.id=:gb and asn.removed=false and asn.notCounted=false and asn.ungraded=false").
    				setLong("gb", gradebook.getId().longValue()).list();
    			for (Assignment assignment : countedAssignments) {
    				List<AssignmentGradeRecord> scoredGradeRecords = session.createQuery(
    					"from AssignmentGradeRecord as agr where agr.gradableObject.id=:go").
    					setLong("go", assignment.getId()).list();
    				Map<String, AssignmentGradeRecord> studentToGradeRecordMap = new HashMap<String, AssignmentGradeRecord>();
    				for (AssignmentGradeRecord scoredGradeRecord : scoredGradeRecords) {
    					studentToGradeRecordMap.put(scoredGradeRecord.getStudentId(), scoredGradeRecord);
    				}
    				for (String studentUid : studentUids) {
    					AssignmentGradeRecord gradeRecord = studentToGradeRecordMap.get(studentUid);
   						if (gradeRecord != null) {
   							if (gradeRecord.getPointsEarned() == null) {
   								gradeRecord.setPointsEarned(Double.valueOf(0));
   							} else {
   								continue;
   							}
   						} else {
   							gradeRecord = new AssignmentGradeRecord(assignment, studentUid, Double.valueOf(0));
   						}
						gradeRecord.setGraderId(graderId);
						gradeRecord.setDateRecorded(now);
						session.saveOrUpdate(gradeRecord);
						session.save(new GradingEvent(assignment, graderId, studentUid, gradeRecord.getPointsEarned()));
    				}
    			}
    			return null;
    		}
    	});
    }
}
