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

package org.sakaiproject.component.app.postem;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Comparator;
import java.util.ArrayList;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.api.app.postem.data.Heading;
import org.sakaiproject.api.app.postem.data.StudentGradeData;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;
import org.sakaiproject.component.app.postem.data.GradebookImpl;
import org.sakaiproject.component.app.postem.data.HeadingImpl;
import org.sakaiproject.component.app.postem.data.StudentGradeDataImpl;
import org.sakaiproject.component.app.postem.data.StudentGradesImpl;
import org.sakaiproject.component.app.postem.data.TemplateImpl;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class GradebookManagerImpl extends HibernateDaoSupport implements
		GradebookManager, Serializable {

	public static final String TITLE = "title";

	public static final String CONTEXT = "context";

	public static final String STUDENTS = "students";

	public static final String ID = "id";

	public static final String RELEASED = "released";

	public Gradebook createGradebook(String title, String creator,
			String context, List<Heading> headings, SortedSet students, Template template) {
		if (title == null || creator == null || context == null || headings == null
				|| students == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {

			Gradebook grades = new GradebookImpl(title, creator, context, headings,
					students, template);
			Iterator si = students.iterator();
			while (si.hasNext()) {
				((StudentGradesImpl) si.next()).setGradebook(grades);
			}
			saveGradebook(grades);
			return grades;
		}

	}

	public Gradebook createEmptyGradebook(String creator, String context) {
		if (creator == null || context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			Gradebook grades = new GradebookImpl("", creator, context, null, null,
					null);
			// saveGradebook(grades);

			return grades;
		}

	}

	public StudentGrades createStudentGradesInGradebook(String username,
			List grades, Gradebook gradebook) {
		if (username == null || grades == null || gradebook == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			StudentGrades student = new StudentGradesImpl(username, grades);
			student.setGradebook(gradebook);
			gradebook.getStudents().add(student);
			return student;
		}
	}

	public StudentGrades createStudentGrades(String username, List grades) {
		return (StudentGrades) new StudentGradesImpl(username, grades);
	}

	public Template createTemplate(String template) {
		Template temp = new TemplateImpl();
		temp.setTemplateCode(template);
		return temp;
	}

	public void deleteGradebook(final Gradebook gradebook) {
		if (gradebook != null) {
			Iterator si = gradebook.getStudents().iterator();
			while (si.hasNext()) {
				deleteStudentGrades((StudentGrades) si.next());
			}
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,
						SQLException {

					session.delete(gradebook);
					return null;
				}
			};
			getHibernateTemplate().execute(hcb);

		}

	}

	public void deleteStudentGrades(final StudentGrades student) {
		if (student != null) {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,
						SQLException {

					session.delete(student);
					return null;
				}
			};
			getHibernateTemplate().execute(hcb);

		}
	}
	
	public void deleteHeading(final Heading heading) {
		if (heading != null) {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,
						SQLException {

					session.delete(heading);
					return null;
				}
			};
			getHibernateTemplate().execute(hcb);

		}
	}

	public Gradebook getGradebookByTitleAndContext(final String title,
			final String context) {
		if (title == null || context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,
						SQLException {

					Criteria crit = session.createCriteria(GradebookImpl.class).add(
							Expression.eq(TITLE, title)).add(Expression.eq(CONTEXT, context))
							.setFetchMode(STUDENTS, FetchMode.EAGER);

					Gradebook gradebook = (Gradebook) crit.uniqueResult();

					return gradebook;
				}
			};
			return (Gradebook) getHibernateTemplate().execute(hcb);
		}

	}

	public SortedSet getGradebooksByContext(final String context, final String sortBy, final boolean ascending) {
		if (context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			List gbList = getHibernateTemplate().find("from GradebookImpl as gb where gb.context=?",
		    		context);
			
			Comparator gbComparator = determineComparator(sortBy, ascending);
			
			SortedSet gradebooks = new TreeSet(gbComparator);

			Iterator gbIterator = gbList.iterator();

			while (gbIterator.hasNext()) {
				gradebooks.add((Gradebook) gbIterator.next());

			}

			return gradebooks;
			
		}
	}

	public SortedSet getReleasedGradebooksByContext(final String context, final String sortBy, final boolean ascending) {
		if (context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,
						SQLException {

					Criteria crit = session.createCriteria(GradebookImpl.class).add(
							Expression.eq(CONTEXT, context)).add(
							Expression.eq(RELEASED, new Boolean(true)));

					List gbs = crit.list();
					
					Comparator gbComparator = determineComparator(sortBy, ascending);
					
					SortedSet gradebooks = new TreeSet(gbComparator);

					Iterator gbIterator = gbs.iterator();

					while (gbIterator.hasNext()) {
						gradebooks.add((Gradebook) gbIterator.next());

					}

					return gradebooks;
				}
			};

			return (SortedSet) getHibernateTemplate().execute(hcb);
		}
	}

	public SortedSet getStudentGradesForGradebook(final Gradebook gradebook)
			throws IllegalArgumentException {
		if (gradebook == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException,
						SQLException {
					// get syllabi in an eager fetch mode
					Criteria crit = session.createCriteria(Gradebook.class).add(
							Expression.eq(ID, gradebook.getId())).setFetchMode(STUDENTS,
							FetchMode.EAGER);

					Gradebook grades = (Gradebook) crit.uniqueResult();

					if (grades != null) {
						return grades.getStudents();
					}
					return new TreeSet();
				}
			};
			return (SortedSet) getHibernateTemplate().execute(hcb);
		}
	}

	public void saveGradebook(Gradebook gradebook)
			throws IllegalArgumentException {
		if (gradebook == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateTemplate temp = getHibernateTemplate();
			temp.saveOrUpdate(gradebook);
		}
	}
	
	public void saveGradebook(final Gradebook gradebook, final List<String> headingTitles, final Map usernameGradesListMap) {
		HibernateCallback hc = new HibernateCallback() {
    		public Object doInHibernate(Session session) throws HibernateException {
    			Long gradebookId = gradebook.getId();
    			
    			if (gradebookId == null) {
    				gradebookId = (Long)session.save(gradebook);
    			}
    			
    			if (headingTitles != null && !headingTitles.isEmpty()) {
    				List headingList = new ArrayList();
    				int location = 0;
    				for (Iterator titleIter = headingTitles.iterator(); titleIter.hasNext();)
    				{
    					String title = (String) titleIter.next();
    					Heading newHeading = new HeadingImpl(gradebookId, title, location);
    					headingList.add(newHeading);
    					location++;
    				}
    				
    				gradebook.setHeadings(headingList);
    			}
    			
    			if (usernameGradesListMap != null && !usernameGradesListMap.isEmpty()) {
    				// first, we need to create the StudentGrades objects - we need the ids of these
    				// to save the grades
    				Set<StudentGrades> studentSet = new TreeSet();
    				for (Iterator usernameIter = usernameGradesListMap.keySet().iterator(); usernameIter.hasNext();) {
    					String username = (String) usernameIter.next();
    					StudentGrades student = new StudentGradesImpl(username, null);
    					student.setGradebook(gradebook);
    					studentSet.add(student);
    				}
    				
    				gradebook.setStudents(studentSet);
    			}
    			
    			session.saveOrUpdate(gradebook);
				
				return gradebookId;
    		}
    	};
    	
    	Long gradebookId = (Long)getHibernateTemplate().execute(hc);
    	
    	// now, we need to populate the student grade data since we now have student id values
    	Gradebook gbWithStudents = getGradebookByIdWithStudents(gradebookId);
    	if (gbWithStudents != null) {
    		Set<StudentGrades> studentGrades = gbWithStudents.getStudents();
			Set<StudentGrades> studentsWithGrades = new TreeSet();
			if (studentGrades != null) {
				for (Iterator studentIter = studentGrades.iterator(); studentIter.hasNext();) {
					StudentGrades student = (StudentGrades) studentIter.next();
					List<StudentGradeData> gradesList = new ArrayList();
					if (student != null) {
						String username = student.getUsername();
						Long studentId = student.getId();
						List<String> listFromMap = (List)usernameGradesListMap.get(username);

						if (listFromMap != null) {
							int location = 0;
							for (Iterator gradesIter = listFromMap.iterator(); gradesIter.hasNext();) {
								String gradeEntry = (String) gradesIter.next();
								StudentGradeData gradeData = new StudentGradeDataImpl(studentId, gradeEntry, location);
								gradesList.add(gradeData);
								location++;
							}
						}
					}
					
					student.setGrades(gradesList);
					studentsWithGrades.add(student);
				}
				
				gbWithStudents.setStudents(studentsWithGrades);
			}
    	}
    	
    	getHibernateTemplate().saveOrUpdate(gbWithStudents);
	}

	public void updateGrades(Gradebook gradebook, List headings,
			SortedSet students) {
		gradebook.setHeadings(headings);
		gradebook.setStudents(students);
		getHibernateTemplate().saveOrUpdate(gradebook);
	}

	public void updateTemplate(Gradebook gradebook, String template) {
		gradebook.setTemplate(createTemplate(template));
		getHibernateTemplate().saveOrUpdate(gradebook);
	}
	
	public Gradebook getGradebookByIdWithHeadingsStudentsAndGrades(final Long gradebookId) {
		if (gradebookId == null) {
	        throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithHeadingsStudentsAndGrades");
	       }

	      HibernateCallback hcb = new HibernateCallback() {
	        public Object doInHibernate(Session session) throws HibernateException, SQLException {
	          Query q = session.getNamedQuery("findGradebookByIdWithHeadingsStudentsAndGrades");
	          q.setParameter("id", gradebookId, Hibernate.LONG);

	          return (Gradebook) q.uniqueResult();
	        }
	      };   

	      return (Gradebook) getHibernateTemplate().execute(hcb); 
	}
	
	public Gradebook getGradebookByIdWithStudents(final Long gradebookId) {
		if (gradebookId == null) {
	        throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithStudents");
	       }

	      HibernateCallback hcb = new HibernateCallback() {
	        public Object doInHibernate(Session session) throws HibernateException, SQLException {
	          Query q = session.getNamedQuery("findGradebookByIdWithStudents");
	          q.setParameter("id", gradebookId, Hibernate.LONG);

	          return (Gradebook) q.uniqueResult();
	        }
	      };   

	      return (Gradebook) getHibernateTemplate().execute(hcb); 
	}
	
	public Gradebook getGradebookByIdWithHeadingsAndStudents(final Long gradebookId) {
		if (gradebookId == null) {
	        throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithStudents");
	       }

	      HibernateCallback hcb = new HibernateCallback() {
	        public Object doInHibernate(Session session) throws HibernateException, SQLException {
	          Query q = session.getNamedQuery("findGradebookByIdWithHeadingsAndStudents");
	          q.setParameter("id", gradebookId, Hibernate.LONG);

	          return (Gradebook) q.uniqueResult();
	        }
	      };   

	      return (Gradebook) getHibernateTemplate().execute(hcb); 
	}
	
	public Gradebook getGradebookByIdWithHeadings(final Long gradebookId) {
		if (gradebookId == null) {
	        throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithHeadings");
	       }

	      HibernateCallback hcb = new HibernateCallback() {
	        public Object doInHibernate(Session session) throws HibernateException, SQLException {
	          Query q = session.getNamedQuery("findGradebookByIdWithHeadings");
	          q.setParameter("id", gradebookId, Hibernate.LONG);

	          return (Gradebook) q.uniqueResult();
	        }
	      };   

	      return (Gradebook) getHibernateTemplate().execute(hcb); 
	}
	
	private Comparator determineComparator(String sortBy, boolean ascending) {
		if (ascending) {
			if (sortBy.equals(Gradebook.SORT_BY_CREATOR)) {
				return GradebookImpl.CreatorAscComparator;
			} else if (sortBy.equals(Gradebook.SORT_BY_MOD_BY)) {
				return GradebookImpl.ModByAscComparator;
			} else if (sortBy.equals(Gradebook.SORT_BY_MOD_DATE)) {
				return GradebookImpl.ModDateAscComparator;
			} else if (sortBy.equals(Gradebook.SORT_BY_RELEASED)) {
				return GradebookImpl.ReleasedAscComparator;
			} else {
				return GradebookImpl.TitleAscComparator;
			}
		} else {
			if (sortBy.equals(Gradebook.SORT_BY_CREATOR)) {
				return GradebookImpl.CreatorDescComparator;
			} else if (sortBy.equals(Gradebook.SORT_BY_MOD_BY)) {
				return GradebookImpl.ModByDescComparator;
			} else if (sortBy.equals(Gradebook.SORT_BY_MOD_DATE)) {
				return GradebookImpl.ModDateDescComparator;
			} else if (sortBy.equals(Gradebook.SORT_BY_RELEASED)) {
				return GradebookImpl.ReleasedDescComparator;
			} else {
				return GradebookImpl.TitleDescComparator;
			}	
		}
	}
	
	public boolean titleExistsInContext(String title, String context) {
		List gbList = getHibernateTemplate().find("from GradebookImpl as gb where gb.context=? and title=?", new Object[] {context, title});
		return gbList != null && gbList.size() > 0;
	}
	
	public Map<String, List> createUsernameGradesListMap(List gradesLists) {
		Map<String, List> usernameGradesListMap = new HashMap();
		if (gradesLists == null || gradesLists.isEmpty()) {
			return usernameGradesListMap;
		}
		
		for (Iterator listIter = gradesLists.iterator(); listIter.hasNext();) {
			List studentList = (ArrayList) listIter.next();
			String username = ((String)studentList.remove(0)).trim();
			usernameGradesListMap.put(username, studentList);
		}
		
		return usernameGradesListMap;
	}
	
	public StudentGrades populateGradesForStudent(StudentGrades student) {
		if (student == null) {
			throw new IllegalArgumentException("Null StudentGrades object passed to getStudentWithGrades");
		}
		
		Long studentId = student.getId();
		if (studentId == null)
			return student;
		
		List studentGrades = getHibernateTemplate().find("from StudentGradeDataImpl as grades where grades.studentId=?", studentId);
		student.setGrades(studentGrades);
		
		return student;
	}
	
	public void updateStudent(StudentGrades student) throws IllegalArgumentException {
		if (student == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateTemplate temp = getHibernateTemplate();
			temp.saveOrUpdate(student);
		}
}

}
