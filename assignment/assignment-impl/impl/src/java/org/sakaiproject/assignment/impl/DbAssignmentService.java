/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.util.BaseDbSingleStorage;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * DbAssignmentService is the database-storing service class for Assignments.
 * </p>
 */
public class DbAssignmentService extends BaseAssignmentService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(DbAssignmentService.class);

	/** The name of the db table holding assignment objects. */
	protected String m_assignmentsTableName = "ASSIGNMENT_ASSIGNMENT";

	/** The name of the db table holding assignment content objects. */
	protected String m_contentsTableName = "ASSIGNMENT_CONTENT";

	/** The name of the db table holding assignment submission objects. */
	protected String m_submissionsTableName = "ASSIGNMENT_SUBMISSION";

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_locksInDb = true;

	/** Extra fields to store in the db with the XML. */
	protected static final String[] FIELDS = { "CONTEXT"};
	
	/** Extra fields to store in the db with the XML in ASSIGNMENT_SUBMISSION table */
	protected static final String[] SUBMISSION_FIELDS = { "CONTEXT", "SUBMITTER_ID", "SUBMIT_TIME", "SUBMITTED", "GRADED"};

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/**
	 * Configuration: set the table name for assignments.
	 * 
	 * @param path
	 *        The table name for assignments.
	 */
	public void setAssignmentTableName(String name)
	{
		m_assignmentsTableName = name;
	}

	/**
	 * Configuration: set the table name for contents.
	 * 
	 * @param path
	 *        The table name for contents.
	 */
	public void setContentTableName(String name)
	{
		m_contentsTableName = name;
	}

	/**
	 * Configuration: set the table name for submissions.
	 * 
	 * @param path
	 *        The table name for submissions.
	 */
	public void setSubmissionTableName(String name)
	{
		m_submissionsTableName = name;
	}

	/**
	 * Configuration: set the locks-in-db
	 * 
	 * @param value
	 *        The locks-in-db value.
	 */
	public void setLocksInDb(String value)
	{
		m_locksInDb = new Boolean(value).booleanValue();
	}

	/** Set if we are to run the to-context conversion. */
	protected boolean m_convertToContext = false;

	/**
	 * Configuration: run the to-context conversion
	 * 
	 * @param value
	 *        The locks-in-db value.
	 */
	public void setConvertToContext(String value)
	{
		m_convertToContext = new Boolean(value).booleanValue();
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_assignment");
			}

			super.init();

			M_log.info("init: assignments table: " + m_assignmentsTableName + " contents table: " + m_contentsTableName
					+ " submissions table: " + m_submissionsTableName + " locks-in-db" + m_locksInDb);

			// convert?
			if (m_convertToContext)
			{
				m_convertToContext = false;
				convertToContext();
			}
		}
		catch (Throwable t)
		{
			M_log.warn(this + ".init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseAssignmentService extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object for Assignments.
	 * 
	 * @return The new storage object for Assignments.
	 */
	protected AssignmentStorage newAssignmentStorage()
	{
		return new DbCachedAssignmentStorage(new AssignmentStorageUser());

	} // newAssignmentStorage

	/**
	 * Construct a Storage object for AssignmentsContents.
	 * 
	 * @return The new storage object for AssignmentContents.
	 */
	protected AssignmentContentStorage newContentStorage()
	{
		return new DbCachedAssignmentContentStorage(new AssignmentContentStorageUser());

	} // newContentStorage

	/**
	 * Construct a Storage object for AssignmentSubmissions.
	 * 
	 * @return The new storage object for AssignmentSubmissions.
	 */
	protected AssignmentSubmissionStorage newSubmissionStorage()
	{
		return new DbCachedAssignmentSubmissionStorage(new AssignmentSubmissionStorageUser());

	} // newSubmissionStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementations
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentStorage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Covers for the BaseDbSingleStorage, providing Assignment and AssignmentEdit parameters
	 */
	protected class DbCachedAssignmentStorage extends BaseDbSingleStorage implements AssignmentStorage
	{
		/**
		 * Construct.
		 * 
		 * @param assignment
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbCachedAssignmentStorage(AssignmentStorageUser assignment)
		{
			super(m_assignmentsTableName, "ASSIGNMENT_ID", FIELDS, m_locksInDb, "assignment", assignment, m_sqlService);

		} // DbCachedAssignmentStorage

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public Assignment get(String id)
		{
			return (Assignment) super.getResource(id);
		}

		public List getAll(String context)
		{
			return super.getAllResourcesWhere(FIELDS[0], context);
		}

		public AssignmentEdit put(String id, String context)
		{
			// pack the context in an array
			Object[] others = new Object[1];
			others[0] = context;
			return (AssignmentEdit) super.putResource(id, others);
		}

		public AssignmentEdit edit(String id)
		{
			return (AssignmentEdit) super.editResource(id);
		}

		public void commit(AssignmentEdit edit)
		{
			super.commitResource(edit);
		}

		public void cancel(AssignmentEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(AssignmentEdit edit)
		{
			super.removeResource(edit);
		}

	} // DbCachedAssignmentStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentContentStorage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Covers for the BaseDbSingleStorage, providing AssignmentContent and AssignmentContentEdit parameters
	 */
	protected class DbCachedAssignmentContentStorage extends BaseDbSingleStorage implements AssignmentContentStorage
	{
		/**
		 * Construct.
		 * 
		 * @param content
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbCachedAssignmentContentStorage(AssignmentContentStorageUser content)
		{
			super(m_contentsTableName, "CONTENT_ID", FIELDS, m_locksInDb, "content", content, m_sqlService);

		} // DbCachedAssignmentContentStorage

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public AssignmentContent get(String id)
		{
			return (AssignmentContent) super.getResource(id);
		}

		public List getAll(String context)
		{
			return super.getAllResourcesWhere(FIELDS[0], context);
		}

		public AssignmentContentEdit put(String id, String context)
		{
			// pack the context in an array
			Object[] others = new Object[1];
			others[0] = context;
			return (AssignmentContentEdit) super.putResource(id, others);
		}

		public AssignmentContentEdit edit(String id)
		{
			return (AssignmentContentEdit) super.editResource(id);
		}

		public void commit(AssignmentContentEdit edit)
		{
			super.commitResource(edit);
		}

		public void cancel(AssignmentContentEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(AssignmentContentEdit edit)
		{
			super.removeResource(edit);
		}

	} // DbCachedAssignmentContentStorage

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AssignmentSubmissionStorage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Covers for the BaseDbSingleStorage, providing AssignmentSubmission and AssignmentSubmissionEdit parameters
	 */
	protected class DbCachedAssignmentSubmissionStorage extends BaseDbSingleStorage implements AssignmentSubmissionStorage
	{
		/*FIELDS: "CONTEXT", "SUBMITTER_ID", "SUBMIT_TIME", "SUBMITTED", "GRADED"*/
		
		/**
		 * Construct.
		 * 
		 * @param submission
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbCachedAssignmentSubmissionStorage(AssignmentSubmissionStorageUser submission)
		{
			super(m_submissionsTableName, "SUBMISSION_ID", SUBMISSION_FIELDS, m_locksInDb, "submission", submission, m_sqlService);

		} // DbCachedAssignmentSubmissionStorage

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public AssignmentSubmission get(String id)
		{
			return (AssignmentSubmission) super.getResource(id);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public AssignmentSubmission get (String assignmentId, String userId)
		{
			Entity entry = null;

			// get the user from the db 
			// need to construct the query here instead of relying on the SingleStorage client
			String sql = "select XML from " + m_submissionsTableName + " where (" + SUBMISSION_FIELDS[0] + " = ? AND "+  SUBMISSION_FIELDS[1] + " = ?)";

			Object fields[] = new Object[2];
			fields[0] = caseId(assignmentId);
			fields[1] = caseId(userId);
			List xml = m_sql.dbRead(sql, fields, null);
			if (!xml.isEmpty())
			{
				// create the Resource from the db xml
				entry = readResource((String) xml.get(0));
				return (AssignmentSubmission) entry;
			}
			else
			{
				return null;
			}
		}
		
		/**
		 * Helper method to exclude inactive site members from submissions count
		 * @param sqlWhere where clause from sql query 
		 * @param assignmentId assignment id relating to query
		 * @return
		 */
		private int getSubmissionsCountWhere(String sqlWhere, String assignmentId) {
			int count = 0;
			Site site = null;
			try {
				site = SiteService.getSite(AssignmentService.getAssignment(assignmentId).getContext());
				
				List l = super.getSelectedResourcesWhere(sqlWhere);
				for (Object o : l) {
					AssignmentSubmission assignmentSubmission = (AssignmentSubmission)o;
					Member member = site.getMember(assignmentSubmission.getSubmitterIdString());
					if(member != null && member.isActive()) {
						count++;
					}
				}
			} catch (Throwable t) {
				M_log.warn(this + ".getSubmissionsCountWhere(): ", t);
				throw new IllegalArgumentException(t);
			}
			
			return count;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public int getSubmittedSubmissionsCount(String assignmentId)
		{
			return getSubmissionsCountWhere("where context='" + assignmentId + "' AND " + SUBMISSION_FIELDS[2] + " IS NOT NULL AND " + SUBMISSION_FIELDS[3] + "='" + Boolean.TRUE.toString() + "'", assignmentId);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public int getUngradedSubmissionsCount(String assignmentId)
		{
			return getSubmissionsCountWhere("where context='" + assignmentId + "' AND " + SUBMISSION_FIELDS[2] + " IS NOT NULL AND " + SUBMISSION_FIELDS[3] + "='" + Boolean.TRUE.toString() + "' AND " + SUBMISSION_FIELDS[4] + "='" + Boolean.FALSE.toString() + "'", assignmentId );
		}
		

		public List getAll(String context)
		{
			return super.getAllResourcesWhere(SUBMISSION_FIELDS[0], context);
		}

		public AssignmentSubmissionEdit put(String id, String assignmentId, String submitterId, String submitTime, String submitted, String graded)
		{
			// pack the context in an array
			Object[] others = new Object[5];
			others[0] = assignmentId;
			others[1] = submitterId;
			others[2] = submitTime;
			others[3] = submitted;
			others[4] = graded;
			
			return (AssignmentSubmissionEdit) super.putResource(id, others);
		}

		public AssignmentSubmissionEdit edit(String id)
		{
			return (AssignmentSubmissionEdit) super.editResource(id);
		}

		public void commit(AssignmentSubmissionEdit edit)
		{
			super.commitResource(edit);
		}

		public void cancel(AssignmentSubmissionEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(AssignmentSubmissionEdit edit)
		{
			super.removeResource(edit);
		}

	} // DbCachedAssignmentSubmissionStorage

	/**
	 * fill in the context field for any record missing it
	 */
	protected void convertToContext()
	{
		M_log.info(this + " convertToContext");

		try
		{
			// get a connection
			final Connection connection = m_sqlService.borrowConnection();
			boolean wasCommit = connection.getAutoCommit();
			connection.setAutoCommit(false);

			// read all assignment records
			String sql = "select XML from ASSIGNMENT_ASSIGNMENT where CONTEXT is null";
			m_sqlService.dbRead(connection, sql, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// create the Resource from the db xml
						String xml = result.getString(1);

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("assignment"))
						{
							M_log.warn(this + " convertToContext(): XML root element not assignment: " + root.getTagName());
							return null;
						}
						Assignment a = new BaseAssignment(root);
						// context is context
						String context = a.getContext();
						String id = a.getId();

						// update
						String update = "update ASSIGNMENT_ASSIGNMENT set CONTEXT = ? where ASSIGNMENT_ID = ?";
						Object fields[] = new Object[2];
						fields[0] = context;
						fields[1] = id;
						boolean ok = m_sqlService.dbWrite(connection, update, fields);

						M_log.info(this + " convertToContext: assignment id: " + id + " context: " + context + " ok: " + ok);

						return null;
					}
					catch (SQLException ignore)
					{
						M_log.warn(this + ":convertToContext " + ignore.getMessage());
						return null;
					}
				}
			});

			// read all content records
			sql = "select XML from ASSIGNMENT_CONTENT where CONTEXT is null";
			m_sqlService.dbRead(connection, sql, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// create the Resource from the db xml
						String xml = result.getString(1);

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("content"))
						{
							M_log.warn(this + " convertToContext(): XML root element not content: " + root.getTagName());
							return null;
						}
						AssignmentContent c = new BaseAssignmentContent(root);
						// context is creator
						String context = c.getCreator();
						String id = c.getId();

						// update
						String update = "update ASSIGNMENT_CONTENT set CONTEXT = ? where CONTENT_ID = ?";
						Object fields[] = new Object[2];
						fields[0] = context;
						fields[1] = id;
						boolean ok = m_sqlService.dbWrite(connection, update, fields);

						M_log.info(this + " convertToContext: content id: " + id + " context: " + context + " ok: " + ok);

						return null;
					}
					catch (SQLException ignore)
					{
						M_log.warn(this + ":convertToContext SqlReader " + ignore.getMessage());
						return null;
					}
				}
			});

			// read all submission records
			sql = "select XML from ASSIGNMENT_SUBMISSION where CONTEXT is null";
			m_sqlService.dbRead(connection, sql, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// create the Resource from the db xml
						String xml = result.getString(1);

						// read the xml
						Document doc = Xml.readDocumentFromString(xml);

						// verify the root element
						Element root = doc.getDocumentElement();
						if (!root.getTagName().equals("submission"))
						{
							M_log.warn(this + " convertToContext(): XML root element not submission: " + root.getTagName());
							return null;
						}
						AssignmentSubmission s = new BaseAssignmentSubmission(root);
						// context is assignment id
						String context = s.getAssignmentId();
						String id = s.getId();

						// update
						String update = "update ASSIGNMENT_SUBMISSION set CONTEXT = ? where SUBMISSION_ID = ?";
						Object fields[] = new Object[2];
						fields[0] = context;
						fields[1] = id;
						boolean ok = m_sqlService.dbWrite(connection, update, fields);

						M_log.info(this + " convertToContext: submission id: " + id + " context: " + context + " ok: " + ok);

						return null;
					}
					catch (SQLException ignore)
					{
						M_log.warn(this + ":convertToContext:SqlReader " + ignore.getMessage());
						return null;
					}
				}
			});

			connection.commit();
			connection.setAutoCommit(wasCommit);
			m_sqlService.returnConnection(connection);
		}
		catch (Throwable t)
		{
			M_log.warn(this + " convertToContext: failed: " + t);
		}

		// TODO:
		M_log.info(this + " convertToContext: done");
	}
}
