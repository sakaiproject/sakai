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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Query;
import org.hibernate.criterion.Expression;

import org.sakaiproject.progress.api.GradebookManager;
import org.sakaiproject.progress.api.IGradebookService;
import org.sakaiproject.progress.api.StudentGrades;
import org.sakaiproject.progress.api.Template;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

public class GradebookManagerImpl extends HibernateDaoSupport implements
		GradebookManager, Serializable {

	public static final String TITLE = "title";

	public static final String CONTEXT = "context";

	public static final String STUDENTS = "students";

	public static final String ID = "id";

	public static final String RELEASED = "released";

	public IGradebookService createGradebook(String title, String creator,
											 String context, List headings, SortedSet students, Template template, String fileReference) {
		if (title == null || creator == null || context == null || headings == null
				|| students == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {

			IGradebookService grades = new GradebookServiceImpl(title, creator, context, headings,
					students, template);

            grades.setFileReference(fileReference);
			Iterator si = students.iterator();
			while (si.hasNext()) {
				((StudentGradesImpl) si.next()).setGradebook(grades);
			}
			saveGradebook(grades);
			return grades;
		}

	}

	public IGradebookService createEmptyGradebook(String creator, String context) {
		if (creator == null || context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			IGradebookService grades = new GradebookServiceImpl("", creator, context, null, null,
					null);
			// saveGradebook(grades);

			return grades;
		}

	}

	public StudentGrades createStudentGradesInGradebook(String username,
														List grades, IGradebookService gradebook) {
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

	public void deleteGradebook(final IGradebookService gradebook) {
		if (gradebook != null) {
			getHibernateTemplate().delete(getHibernateTemplate().merge(gradebook));
		}
	}

	public void deleteStudentGrades(final StudentGrades student) {
		if (student != null) {
			getHibernateTemplate().delete(getHibernateTemplate().merge(student));
		}
	}

	public IGradebookService getGradebookByTitleAndContext(final String title,
			final String context) {
		if (title == null || context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = session -> {
                Criteria crit = session.createCriteria(GradebookServiceImpl.class).add(
                        Expression.eq(TITLE, title)).add(Expression.eq(CONTEXT, context))
                        .setFetchMode(STUDENTS, FetchMode.EAGER);

				IGradebookService gradebook = (IGradebookService) crit.uniqueResult();

                return gradebook;
            };
			return (IGradebookService) getHibernateTemplate().execute(hcb);
		}

	}

	public SortedSet getGradebooksByContext(final String context, final String sortBy, final boolean ascending) {
		if (context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = session -> {

                Criteria crit = session.createCriteria(GradebookServiceImpl.class).add(Expression.eq(CONTEXT, context));

                List gbs = crit.list();

                Comparator gbComparator = determineComparator(sortBy, ascending);

                SortedSet gradebooks = new TreeSet(gbComparator);

                Iterator gbIterator = gbs.iterator();

                while (gbIterator.hasNext()) {
                    gradebooks.add((IGradebookService) gbIterator.next());

                }

                return gradebooks;
            };

			return (SortedSet) getHibernateTemplate().execute(hcb);
		}
	}

	public SortedSet getReleasedGradebooksByContext(final String context, final String sortBy, final boolean ascending) {
		if (context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = session -> {

                Criteria crit = session.createCriteria(GradebookServiceImpl.class).add(
                        Expression.eq(CONTEXT, context)).add(
                        Expression.eq(RELEASED, new Boolean(true)));

                List gbs = crit.list();

                Comparator gbComparator = determineComparator(sortBy, ascending);

                SortedSet gradebooks = new TreeSet(gbComparator);

                Iterator gbIterator = gbs.iterator();

                while (gbIterator.hasNext()) {
                    gradebooks.add((IGradebookService) gbIterator.next());

                }

                return gradebooks;
            };

			return (SortedSet) getHibernateTemplate().execute(hcb);
		}
	}

	public SortedSet getStudentGradesForGradebook(final IGradebookService gradebook)
			throws IllegalArgumentException {
		if (gradebook == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = session -> {
                // get syllabi in an eager fetch mode
                Criteria crit = session.createCriteria(IGradebookService.class).add(
                        Expression.eq(ID, gradebook.getId())).setFetchMode(STUDENTS,
                        FetchMode.EAGER);

				IGradebookService grades = (IGradebookService) crit.uniqueResult();

                if (grades != null) {
                    return grades.getStudents();
                }
                return new TreeSet();
            };
			return (SortedSet) getHibernateTemplate().execute(hcb);
		}
	}

	public void saveGradebook(IGradebookService gradebook)
			throws IllegalArgumentException {
		if (gradebook == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			getHibernateTemplate().merge(gradebook);
		}
	}

	public void updateGrades(IGradebookService gradebook, List headings,
			SortedSet students) {
		gradebook.setHeadings(headings);
		gradebook.setStudents(students);
		getHibernateTemplate().merge(gradebook);
	}

	public void updateTemplate(IGradebookService gradebook, String template, String fileReference) {
		gradebook.setFileReference(fileReference);
		gradebook.setTemplate(createTemplate(template));
		getHibernateTemplate().merge(gradebook);
	}
	
	private Comparator determineComparator(String sortBy, boolean ascending) {
		if (ascending) {
			if (sortBy.equals(IGradebookService.SORT_BY_CREATOR)) {
				return GradebookServiceImpl.CreatorAscComparator;
			} else if (sortBy.equals(IGradebookService.SORT_BY_MOD_BY)) {
				return GradebookServiceImpl.ModByAscComparator;
			} else if (sortBy.equals(IGradebookService.SORT_BY_MOD_DATE)) {
				return GradebookServiceImpl.ModDateAscComparator;
			} else if (sortBy.equals(IGradebookService.SORT_BY_RELEASED)) {
				return GradebookServiceImpl.ReleasedAscComparator;
			} else {
				return GradebookServiceImpl.TitleAscComparator;
			}
		} else {
			if (sortBy.equals(IGradebookService.SORT_BY_CREATOR)) {
				return GradebookServiceImpl.CreatorDescComparator;
			} else if (sortBy.equals(IGradebookService.SORT_BY_MOD_BY)) {
				return GradebookServiceImpl.ModByDescComparator;
			} else if (sortBy.equals(IGradebookService.SORT_BY_MOD_DATE)) {
				return GradebookServiceImpl.ModDateDescComparator;
			} else if (sortBy.equals(IGradebookService.SORT_BY_RELEASED)) {
				return GradebookServiceImpl.ReleasedDescComparator;
			} else {
				return GradebookServiceImpl.TitleDescComparator;
			}	
		}
	}
	
	public IGradebookService getGradebookByIdWithHeadingsAndStudents(final Long gradebookId) {
		if (gradebookId == null) {
	        throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithStudents");
	       }

		HibernateCallback hcb = session -> {

            Criteria crit = session.createCriteria(GradebookServiceImpl.class).add(Expression.eq(ID, gradebookId));

			IGradebookService gradebook = (IGradebookService)crit.uniqueResult();
            getHibernateTemplate().initialize(gradebook.getHeadings());
            getHibernateTemplate().initialize(gradebook.getStudents());

            return gradebook;
        };

	      return (IGradebookService) getHibernateTemplate().execute(hcb);
	
	}
	
	public IGradebookService getGradebookByIdWithHeadings(final Long gradebookId) {
		if (gradebookId == null) {
	        throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithHeadings");
	       }

		HibernateCallback hcb = session -> {

            Criteria crit = session.createCriteria(GradebookServiceImpl.class).add(Expression.eq(ID, gradebookId));

			IGradebookService gradebook = (IGradebookService)crit.uniqueResult();
            getHibernateTemplate().initialize(gradebook.getHeadings());

            return gradebook;
        };

	      return (IGradebookService) getHibernateTemplate().execute(hcb);
	
	}
	
	public StudentGrades getStudentByGBAndUsername(final IGradebookService gradebook, final String username) {
		if (gradebook == null || username == null) {
	        throw new IllegalArgumentException("Null gradebookId or username passed to getStudentByGBIdAndUsername");
	       }

		HibernateCallback hcb = session -> {
            gradebook.setStudents(null);
            Criteria crit = session.createCriteria(StudentGradesImpl.class).add(
                    Expression.eq("gradebook", gradebook)).add(Expression.eq("username", username).ignoreCase());

            StudentGrades student = (StudentGrades)crit.uniqueResult();

            return student;
        };

	      return (StudentGrades) getHibernateTemplate().execute(hcb); 
	}
	
	public void updateStudent(StudentGrades student) throws IllegalArgumentException {
		if (student == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			getHibernateTemplate().merge(student);
		}
	}
	
	public List getUsernamesInGradebook(final IGradebookService gradebook) {
		if (gradebook == null) {
	        throw new IllegalArgumentException("Null gradebook passed to getUsernamesInGradebook");
	       }

		HibernateCallback hcb = session -> {
          Query q = session.getNamedQuery("findUsernamesInGradebook");
          q.setParameter("gradebook", gradebook);
          return q.list();
        };
	        
	    return (List) getHibernateTemplate().execute(hcb);  
	}

}
