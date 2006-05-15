package org.sakaiproject.component.app.postem.data;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.FetchMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.HibernateTemplate;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class GradebookManagerImpl extends HibernateDaoSupport implements
		GradebookManager, Serializable {

	public static final String TITLE = "title";

	public static final String CONTEXT = "context";

	public static final String STUDENTS = "students";

	public static final String ID = "id";
	
	public static final String RELEASED = "released";

	public Gradebook createGradebook(String title, String creator,
			String context, List headings, SortedSet students, Template template) {
		if (title == null || creator == null || context == null
				|| headings == null || students == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {

			Gradebook grades = new GradebookImpl(title, creator, context,
					headings, students, template);
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
			Gradebook grades = new GradebookImpl("", creator, context, null,
					null, null);
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
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

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
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

					session.delete(student);
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
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

					Criteria crit = session.createCriteria(GradebookImpl.class)
							.add(Expression.eq(TITLE, title)).add(
									Expression.eq(CONTEXT, context))
							.setFetchMode(STUDENTS, FetchMode.EAGER);

					Gradebook gradebook = (Gradebook) crit.uniqueResult();

					return gradebook;
				}
			};
			return (Gradebook) getHibernateTemplate().execute(hcb);
		}

	}

	public SortedSet getGradebooksByContext(final String context) {
		if (context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

					Criteria crit = session.createCriteria(GradebookImpl.class)
							.add(Expression.eq(CONTEXT, context));

					List gbs = crit.list();
					SortedSet gradebooks = new TreeSet();

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
	
	public SortedSet getReleasedGradebooksByContext(final String context) {
		if (context == null) {
			throw new IllegalArgumentException("Null Argument");
		} else {
			HibernateCallback hcb = new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {

					Criteria crit = session.createCriteria(GradebookImpl.class)
							.add(Expression.eq(CONTEXT, context)).add(Expression.eq(RELEASED, new Boolean(true)));

					List gbs = crit.list();
					SortedSet gradebooks = new TreeSet();

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
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					// get syllabi in an eager fetch mode
					Criteria crit = session.createCriteria(Gradebook.class)
							.add(Expression.eq(ID, gradebook.getId()))
							.setFetchMode(STUDENTS, FetchMode.EAGER);

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
			/*
			 * Iterator iter = gradebook.getStudents().iterator(); while
			 * (iter.hasNext()) { temp.saveOrUpdate((StudentGradesImpl)
			 * iter.next()); }
			 */
		}
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

}
