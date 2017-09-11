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

package org.sakaiproject.tool.gradebook.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.GradebookAssignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.springframework.orm.hibernate4.HibernateCallback;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GradebookCalculationImpl extends GradebookManagerHibernateImpl implements GradebookManager 
{
	@Override
	public List<CourseGradeRecord> getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids)
	{
		HibernateCallback<List<CourseGradeRecord>> hc = session -> {
            if(studentUids == null || studentUids.size() == 0)
            {
                log.debug("Returning no grade records for an empty collection of student UIDs in GradebookCalculationImpl.getPointsEarnedCourseGradeRecords(CourseGrade, Collection).");
                return new ArrayList<>();
            }

			Long gradebookId = courseGrade.getGradebook().getId();

			// get all of the AssignmentGradeRecords here to avoid repeated db calls
			Map<String, List<AssignmentGradeRecord>> studentIdGradeRecordsMap =
					getGradeRecordMapForStudents(gradebookId, studentUids);

			// get all of the counted assignments
			List<GradebookAssignment> countedAssigns = getCountedAssignments(session, gradebookId);

            return getPointsEarnedCourseGradeRecords(session, courseGrade, studentUids,
                    countedAssigns, studentIdGradeRecordsMap);
        };
		return getHibernateTemplate().execute(hc);
	}

	@Override
	public List<CourseGradeRecord> getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids, final Collection assignments, final Map gradeRecordMap)
	{
		HibernateCallback<List<CourseGradeRecord>> hc = session -> {
            if(studentUids == null || studentUids.size() == 0)
            {
                if(log.isDebugEnabled()) log.debug("Returning no grade records for an empty collection of student UIDs in GradebookCalculationImpl.getPointsEarnedCourseGradeRecords");
                return new ArrayList<>();
            }

            // let's make the grade map more manageable here.  it starts out as
            // Map of studentId --> Map of assignment id --> corresponding AssignmentGradeRecord
            Map<String, List<AssignmentGradeRecord>> studentIdGradeRecordsMap = new HashMap<String, List<AssignmentGradeRecord>>();
            if (gradeRecordMap != null) {
                for (Iterator stIter = studentUids.iterator(); stIter.hasNext();) {
                    String studentUid = (String)stIter.next();
                    Map<Long, AssignmentGradeRecord> studentMap = (Map<Long, AssignmentGradeRecord>)gradeRecordMap.get(studentUid);
                    if (studentMap != null) {
                        List<AssignmentGradeRecord> studentGradeRecs =
                            new ArrayList<AssignmentGradeRecord>(studentMap.values());
                        studentIdGradeRecordsMap.put(studentUid, studentGradeRecs);
                    }
                }
            }

            return getPointsEarnedCourseGradeRecords(session, courseGrade, studentUids, assignments, studentIdGradeRecordsMap);
        };
		return getHibernateTemplate().execute(hc);
	}
	
	/**
	 * 
	 * @param session
	 * @param courseGrade
	 * @param studentUids
	 * @param studentIdGradeRecordsMap map of studentId --> List of that student's AssignmentGradeRecords
	 * @return a list of the CourseGradeRecords with nonpersisted fields populated for the given students and
	 * associated AssignmentGradeRecords
	 */
	private List<CourseGradeRecord> getPointsEarnedCourseGradeRecords(Session session, 
	        CourseGrade courseGrade, Collection<String> studentUids, Collection<GradebookAssignment> assignments,
	        Map<String, List<AssignmentGradeRecord>> studentIdGradeRecordsMap) {

	    List<CourseGradeRecord> courseGradeRecs = new ArrayList<CourseGradeRecord>();

	    if (studentUids != null && !studentUids.isEmpty()) {
	        
	        int gbGradeType = getGradebook(courseGrade.getGradebook().getId()).getGrade_type();

	        Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
	        q.setLong("gradableObjectId", courseGrade.getId().longValue());
	        courseGradeRecs = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

	        Long gradebookId = courseGrade.getGradebook().getId();
	        Gradebook gradebook = getGradebook(gradebookId);
	        List cates = getCategories(gradebookId);
	        
	        List<GradebookAssignment> countedAssigns = new ArrayList<GradebookAssignment>();
	        // let's filter the passed assignments to make sure they are all counted
	        if (assignments != null) {
	            for (GradebookAssignment assign : assignments) {
	                if (assign.isIncludedInCalculations()) {
	                    countedAssigns.add(assign);
	                }
	            }
	        }

	        // no need to calculate anything for non-calculating gradebook
	        if (gbGradeType != GradebookService.GRADE_TYPE_LETTER)
	        {
                Map<String, Set<GradebookAssignment>> visibleExternals =
                    getVisibleExternalAssignments(courseGrade.getGradebook(), studentUids, countedAssigns);

	            for(CourseGradeRecord cgr : courseGradeRecs) 
	            {
                    // Filter out external activities that are not visible to this student, considering them "uncounted"
                    List<GradebookAssignment> studentCountedAssigns = new ArrayList<GradebookAssignment>();
                    String studentId = cgr.getStudentId();
                    for (GradebookAssignment a : countedAssigns) {
                        if (!a.isExternallyMaintained() ||
                                (visibleExternals.containsKey(studentId) && visibleExternals.get(studentId).contains(a)))
                        {
                            studentCountedAssigns.add(a);
                        }
                    }

	                //double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
	                List<AssignmentGradeRecord> studentGradeRecs;
	                if (studentIdGradeRecordsMap == null) {
	                    studentGradeRecs = new ArrayList<AssignmentGradeRecord>();
	                } else {
	                    studentGradeRecs = studentIdGradeRecordsMap.get(cgr.getStudentId());
	                }

	                applyDropScores(studentGradeRecs);

	                List totalEarned = getTotalPointsEarnedInternal(cgr.getStudentId(), gradebook, cates, studentGradeRecs, studentCountedAssigns);
	                double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
	                double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();	                
	                double totalPointsPossible = getTotalPointsInternal(gradebook, cates, cgr.getStudentId(), studentGradeRecs, studentCountedAssigns, false);
	                cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
	                if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
	            }
	        }
	    }
	    return courseGradeRecs;
	}
	
	@Override
	List getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List categories,
	        final List<AssignmentGradeRecord> gradeRecs, List<GradebookAssignment> countedAssigns)
	{
		int gbGradeType = gradebook.getGrade_type();
		if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
			return new ArrayList();
		}
		
		if (gradeRecs == null || countedAssigns == null) {
            if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for " +
                    "studentId=" + studentId + " returning 0 because null gradeRecs or countedAssigns");
            List returnList = new ArrayList();
            returnList.add(new Double(0));
            returnList.add(new Double(0));
            returnList.add(new Double(0)); // 3rd one is for the pre-adjusted course grade
            return returnList;
        }


		double totalPointsEarned = 0;
		BigDecimal literalTotalPointsEarned = new BigDecimal(0d);

		Map cateScoreMap = new HashMap();
		Map cateTotalScoreMap = new HashMap();

		Set assignmentsTaken = new HashSet();
		for (AssignmentGradeRecord gradeRec : gradeRecs)
		{
			if(gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("") && !gradeRec.getDroppedFromGrade())
			{
				GradebookAssignment go = gradeRec.getAssignment();
				if (go.isIncludedInCalculations() && countedAssigns.contains(go))
				{
					Double pointsEarned = new Double(gradeRec.getPointsEarned());
					//if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
					//{
						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
						{
							totalPointsEarned += pointsEarned.doubleValue();
							literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
							assignmentsTaken.add(go.getId());
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
						{
							totalPointsEarned += pointsEarned.doubleValue();
							literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
							assignmentsTaken.add(go.getId());
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
						{
							for(int i=0; i<categories.size(); i++)
							{
								Category cate = (Category) categories.get(i);
								if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
								{
									assignmentsTaken.add(go.getId());
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
									if(cateScoreMap.get(cate.getId()) != null)
									{
										cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + pointsEarned.doubleValue()));
									}
									else
									{
										cateScoreMap.put(cate.getId(), new Double(pointsEarned));
									}
									break;
								}
							}
						}
					}
					
						
					
				
			}			
		}

		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
		{
			Iterator assgnsIter = countedAssigns.iterator();
			while (assgnsIter.hasNext()) 
			{
				GradebookAssignment asgn = (GradebookAssignment)assgnsIter.next();
				if(assignmentsTaken.contains(asgn.getId()))
				{
					for(int i=0; i<categories.size(); i++)
					{
						Category cate = (Category) categories.get(i);
						if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()) && !asgn.isExtraCredit())
						{

							if(cateTotalScoreMap.get(cate.getId()) == null)
							{								
								cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
							}
							else
							{								
								cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));							
							}

						}
					}
				}
			}
		}

		if(assignmentsTaken.isEmpty())
			totalPointsEarned = -1;

		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
		{
			for(int i=0; i<categories.size(); i++)
			{
				Category cate = (Category) categories.get(i);
				if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
				{
					totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
				}
			}
		}

		if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
		List returnList = new ArrayList();
		returnList.add(new Double(totalPointsEarned));
		returnList.add(new Double((new BigDecimal(literalTotalPointsEarned.doubleValue(), GradebookService.MATH_CONTEXT)).doubleValue()));
		return returnList;
	}

	@Override
	public double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, List<AssignmentGradeRecord> studentGradeRecs, List<GradebookAssignment> countedAssigns, boolean literalTotal)
	{
		int gbGradeType = gradebook.getGrade_type();
		if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
			return -1;
		}
		
		if (studentGradeRecs == null || countedAssigns == null) {
            if (log.isDebugEnabled()) log.debug("Returning 0 from getTotalPointsInternal " +
                    "since studentGradeRecs or countedAssigns was null");
            return 0;
        }
		
		double totalPointsPossible = 0;

        HashSet<GradebookAssignment> countedSet = new HashSet<GradebookAssignment>(countedAssigns);

		// we need to filter this list to identify only "counted" grade recs
        List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<AssignmentGradeRecord>();
        for (AssignmentGradeRecord gradeRec : studentGradeRecs) {
            GradebookAssignment assign = gradeRec.getAssignment();
            boolean extraCredit = assign.isExtraCredit();
            if(gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY && assign.getCategory() != null && assign.getCategory().isExtraCredit())
            	extraCredit = true;
            
            if (assign.isCounted() && !assign.getUngraded() && !assign.isRemoved() && countedSet.contains(assign) &&
                    assign.getPointsPossible() != null && assign.getPointsPossible() > 0 && !gradeRec.getDroppedFromGrade() && !extraCredit) {
                countedGradeRecs.add(gradeRec);
            }
        }

		Set assignmentsTaken = new HashSet();
		Set categoryTaken = new HashSet();
		for (AssignmentGradeRecord gradeRec : countedGradeRecs)
		{
		    if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("")) 
		    {
		        Double pointsEarned = new Double(gradeRec.getPointsEarned());
		        GradebookAssignment go = gradeRec.getAssignment();
		        if (pointsEarned != null) 
		        {
		            if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
		            {
		                assignmentsTaken.add(go.getId());
		            }
		            else if ((gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY || gradebook
		            		.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
		            		&& go != null && categories != null)
		            {
//		                assignmentsTaken.add(go.getId());
//		            }
//		            else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
//		            {
		                for(int i=0; i<categories.size(); i++)
		                {
		                    Category cate = (Category) categories.get(i);
		                    if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()) && ((cate.isExtraCredit()!=null && !cate.isExtraCredit()) || cate.isExtraCredit()==null))
		                    {
		                        assignmentsTaken.add(go.getId());
		                        categoryTaken.add(cate.getId());
		                        break;
		                    }
		                }
		            }
		        }
		    }
		}

		if(!assignmentsTaken.isEmpty())
		{
			if(!literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
			{
				for(int i=0; i<categories.size(); i++)
				{
					Category cate = (Category) categories.get(i);
					if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
					{
						totalPointsPossible += cate.getWeight().doubleValue();
					}
				}
				return totalPointsPossible;
			}
			Iterator assignmentIter = countedAssigns.iterator();
			while (assignmentIter.hasNext()) 
			{
				GradebookAssignment asn = (GradebookAssignment) assignmentIter.next();
				if(asn != null)
				{
					Double pointsPossible = asn.getPointsPossible();

					if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
					{
						totalPointsPossible += pointsPossible.doubleValue();
					}
					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
					{
						totalPointsPossible += pointsPossible.doubleValue();
					}else if(literalTotal && gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && assignmentsTaken.contains(asn.getId()))
					{
						totalPointsPossible += pointsPossible.doubleValue();
					}
				}
			}
		}
		else
			totalPointsPossible = -1;

		return totalPointsPossible;
	}
	
	@Override
    public void applyDropScores(Collection<AssignmentGradeRecord> gradeRecords) {
        super.applyDropScores(gradeRecords);
    }
}
