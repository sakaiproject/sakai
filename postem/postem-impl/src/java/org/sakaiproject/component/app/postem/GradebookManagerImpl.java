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

package org.sakaiproject.component.app.postem;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.FetchMode;
import org.hibernate.query.Query;

import org.sakaiproject.api.app.postem.data.Gradebook;
import org.sakaiproject.api.app.postem.data.GradebookManager;
import org.sakaiproject.api.app.postem.data.StudentGrades;
import org.sakaiproject.api.app.postem.data.Template;
import org.sakaiproject.component.app.postem.data.GradebookImpl;
import org.sakaiproject.component.app.postem.data.StudentGradesImpl;
import org.sakaiproject.component.app.postem.data.TemplateImpl;

import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class GradebookManagerImpl extends HibernateDaoSupport implements GradebookManager, Serializable {

    public static final String TITLE = "title";

    public static final String CONTEXT = "context";

    public static final String STUDENTS = "students";

    public static final String ID = "id";

    public static final String RELEASED = "released";

    public Gradebook createGradebook(String title, String creator, String context, List headings, SortedSet students, Template template, String fileReference) {
        if (title == null || creator == null || context == null || headings == null || students == null) {
            throw new IllegalArgumentException("Null Argument");

        } else {
            Gradebook grades = new GradebookImpl(title, creator, context, headings, students, template);
            grades.setFileReference(fileReference);
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
            Gradebook grades = new GradebookImpl("", creator, context, null, null,null);
            // saveGradebook(grades);
            return grades;
        }

    }

    public StudentGrades createStudentGradesInGradebook(String username, List grades, Gradebook gradebook) {
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
            getHibernateTemplate().delete(getHibernateTemplate().merge(gradebook));
        }
    }

    public void deleteStudentGrades(final StudentGrades student) {
        if (student != null) {
            getHibernateTemplate().delete(getHibernateTemplate().merge(student));
        }
    }

    public Gradebook getGradebookByTitleAndContext(final String title, final String context) {
        if (title == null || context == null) {
            throw new IllegalArgumentException("Null Argument");

        } else {
            HibernateCallback<Gradebook> hcb = session -> {
                Query<GradebookImpl> q = session.createQuery(
                    "from GradebookImpl g left join fetch g.students where g.title = :title and g.context = :context",
                    GradebookImpl.class);
                q.setParameter("title", title);
                q.setParameter("context", context);
                return q.uniqueResult();
            };
            return getHibernateTemplate().execute(hcb);
        }
    }

    public SortedSet getGradebooksByContext(final String context, final String sortBy, final boolean ascending) {
        if (context == null) {
            throw new IllegalArgumentException("Null Argument");

        } else {
            HibernateCallback<SortedSet> hcb = session -> {
                Query<GradebookImpl> q = session.createQuery(
                    "from GradebookImpl g where g.context = :context", GradebookImpl.class);
                q.setParameter("context", context);
                List<GradebookImpl> gbs = q.list();
                Comparator gbComparator = determineComparator(sortBy, ascending);
                SortedSet gradebooks = new TreeSet(gbComparator);
                gradebooks.addAll(gbs);
                return gradebooks;
            };

            return (SortedSet) getHibernateTemplate().execute(hcb);
        }
    }

    public SortedSet getReleasedGradebooksByContext(final String context, final String sortBy, final boolean ascending) {
        if (context == null) {
            throw new IllegalArgumentException("Null Argument");

        } else {
            HibernateCallback<SortedSet> hcb = session -> {
                Query<GradebookImpl> q = session.createQuery(
                    "from GradebookImpl g where g.context = :context and g.released = :released",
                    GradebookImpl.class);
                q.setParameter("context", context);
                q.setParameter("released", Boolean.TRUE);
                List<GradebookImpl> gbs = q.list();
                Comparator gbComparator = determineComparator(sortBy, ascending);
                SortedSet gradebooks = new TreeSet(gbComparator);
                gradebooks.addAll(gbs);
                return gradebooks;
            };

            return (SortedSet) getHibernateTemplate().execute(hcb);
        }
    }

    public SortedSet getStudentGradesForGradebook(final Gradebook gradebook) throws IllegalArgumentException {
        if (gradebook == null) {
            throw new IllegalArgumentException("Null Argument");

        } else {
            HibernateCallback<SortedSet> hcb = session -> {
                Query<GradebookImpl> q = session.createQuery(
                    "from GradebookImpl g left join fetch g.students where g.id = :id",
                    GradebookImpl.class);
                q.setParameter("id", gradebook.getId());
                GradebookImpl grades = q.uniqueResult();
                if (grades != null) {
                    return (SortedSet) grades.getStudents();
                }
                return new TreeSet();
            };
            return (SortedSet) getHibernateTemplate().execute(hcb);
        }
    }

    public void saveGradebook(Gradebook gradebook) throws IllegalArgumentException {
        if (gradebook == null) {
            throw new IllegalArgumentException("Null Argument");
        } else {
            getHibernateTemplate().merge(gradebook);
        }
    }

    public void updateGrades(Gradebook gradebook, List headings, SortedSet students) {
        gradebook.setHeadings(headings);
        gradebook.setStudents(students);
        getHibernateTemplate().merge(gradebook);
    }

    public void updateTemplate(Gradebook gradebook, String template, String fileReference) {
        gradebook.setFileReference(fileReference);
        gradebook.setTemplate(createTemplate(template));
        getHibernateTemplate().merge(gradebook);
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

    public Gradebook getGradebookByIdWithHeadingsAndStudents(final Long gradebookId) {
        if (gradebookId == null) {
            throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithStudents");
        }
        HibernateCallback<Gradebook> hcb = session -> {
            Query<GradebookImpl> q = session.createQuery(
                "from GradebookImpl g left join fetch g.headings left join fetch g.students where g.id = :id",
                GradebookImpl.class);
            q.setParameter("id", gradebookId);
            return q.uniqueResult();
        };
        return getHibernateTemplate().execute(hcb);
    }

    public Gradebook getGradebookByIdWithHeadings(final Long gradebookId) {
        if (gradebookId == null) {
            throw new IllegalArgumentException("Null gradebookId passed to getGradebookByIdWithHeadings");
        }
        HibernateCallback<Gradebook> hcb = session -> {
            Query<GradebookImpl> q = session.createQuery(
                "from GradebookImpl g left join fetch g.headings where g.id = :id",
                GradebookImpl.class);
            q.setParameter("id", gradebookId);
            return q.uniqueResult();
        };
        return (Gradebook) getHibernateTemplate().execute(hcb);
    }

    public StudentGrades getStudentByGBAndUsername(final Gradebook gradebook, final String username) {
        if (gradebook == null || username == null) {
            throw new IllegalArgumentException("Null gradebookId or username passed to getStudentByGBIdAndUsername");
        }
        HibernateCallback<StudentGrades> hcb = session -> {
            Query<StudentGradesImpl> q = session.createQuery(
                "from StudentGradesImpl s where s.gradebook = :gradebook and lower(s.username) = lower(:username)",
                StudentGradesImpl.class);
            q.setParameter("gradebook", gradebook);
            q.setParameter("username", username);
            return q.uniqueResult();
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

    public List getUsernamesInGradebook(final Gradebook gradebook) {
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
