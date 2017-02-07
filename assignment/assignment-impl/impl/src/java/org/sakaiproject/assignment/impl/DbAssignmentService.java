/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.assignment.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.assignment.api.AssignmentContentEdit;
import org.sakaiproject.assignment.api.AssignmentEdit;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentSubmissionEdit;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.user.api.User;
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
	private static Logger M_log = LoggerFactory.getLogger(DbAssignmentService.class);

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

	/** Oracle in clause limit */
	protected static final int MAX_IN_CLAUSE_SIZE = 1000;

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
		m_locksInDb = Boolean.valueOf(value).booleanValue();
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
		m_convertToContext = Boolean.valueOf(value).booleanValue();
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
		m_autoDdl = Boolean.valueOf(value).booleanValue();
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
	public AssignmentStorage newAssignmentStorage()
	{
		return new DbCachedAssignmentStorage(new AssignmentStorageUser());

	} // newAssignmentStorage

	/**
	 * Construct a Storage object for AssignmentsContents.
	 * 
	 * @return The new storage object for AssignmentContents.
	 */
	public AssignmentContentStorage newContentStorage()
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
		 * {@inheritDoc}
		 */
		public Map<User, AssignmentSubmission> getUserSubmissionMap(Assignment assignment, List<User> users)
		{
			if (assignment == null || assignment.isGroup())
			{
				throw new IllegalArgumentException("getSubmissionForUsers invoked with null of group assignment");
			}

			Map<User, AssignmentSubmission> userSubmissionMap = new HashMap<>();

			if (CollectionUtils.isEmpty(users))
			{
				return userSubmissionMap;
			}

			// Work in batches of 1000 users (due to Oracle's in clause limit)
			int minUser = 0;
			int maxUser = Math.min(users.size(), MAX_IN_CLAUSE_SIZE);
			while (minUser < users.size())
			{
				List<User> userSublist = users.subList(minUser, maxUser);

				/* 
				 * Build a query like: 
				 * select XML from assignment_submission where (CONTEXT = ? AND SUBMITTER_ID in (?, ?, ...));
				 */
				// The sql string
				StringBuilder sql = new StringBuilder();
				// fields are the values to be passed in. 1st param is assignment ID, the rest are userIDs
				String fields[] = new String[1 + userSublist.size()];

				String param = "?";
				sql.append("select XML from ").append(m_submissionsTableName)
					.append(" where (").append(SUBMISSION_FIELDS[0]).append(" = ").append(param).append(" AND ")
					.append(SUBMISSION_FIELDS[1]).append(" in (");
				fields[0] = caseId(assignment.getId());

				// This map will be useful to retrieve Users from submission.getSubmitterId()
				Map<String, User> userIdUserMap = new HashMap<>();
				for (int i = 0; i < userSublist.size(); i++)
				{
					User u = userSublist.get(i);
					String userId = u.getId();
					userIdUserMap.put(userId, u);

					sql.append(param);
					// compiler optimizes this (first iteration appends "?", subsequent iterations append ",?")
					param = ",?";
					// first field is assignmentId, all userId fields' indices are shifted up 1
					fields[i + 1] = userId;
				}
				// append "))" to close "in (" and "where("
				sql.append("))");

				List xmlResources = m_sql.dbRead(sql.toString(), fields, null);
				for (Object xml : xmlResources)
				{
					AssignmentSubmission submission = (AssignmentSubmission) readResource((String) xml);
					String submitterId = submission.getSubmitterId();
					User u = userIdUserMap.get(submitterId);
					if (u == null)
					{
						M_log.warn("getUserSubmissionMap() - submission's submitterId not found in the original user list");
					}
					else
					{
						userSubmissionMap.put(u, submission);
					}
				}

				minUser += MAX_IN_CLAUSE_SIZE;
				maxUser = Math.min(users.size(), minUser + MAX_IN_CLAUSE_SIZE);
			}

			return userSubmissionMap;
		}
		
		/**
		 * Helper method to exclude inactive site members from submissions count
		 * @param sqlWhere where clause from sql query 
		 * @param assignmentId assignment id relating to query
		 * @return
		 */
		private int getSubmissionsCountWhere(String queryType, String assignmentRef) {
			int count = 0;
			Site site = null;
			Collection asgGroups = null;
			String sqlWhere = null;
			
			try {
				Assignment a = getAssignment(assignmentRef);
				
				// is this a non-electronice submission type assignment
				boolean isNonElectronicSubmission = a.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION == a.getContent().getTypeOfSubmission();
				
				if ("submitted".equals(queryType))
				{
					if (isNonElectronicSubmission)
					{
						sqlWhere ="where context='" + assignmentId(assignmentRef) + "' AND " + SUBMISSION_FIELDS[3] + "='" + Boolean.TRUE.toString() + "'";
					}
					else
					{
						sqlWhere ="where context='" + assignmentId(assignmentRef) + "' AND " + SUBMISSION_FIELDS[2] + " IS NOT NULL AND " + SUBMISSION_FIELDS[3] + "='" + Boolean.TRUE.toString() + "'";
					}
					
				}
				else if ("ungraded".equals(queryType))
				{
					if (isNonElectronicSubmission)
					{
						sqlWhere = "where context='" + assignmentId(assignmentRef) + "' AND " + SUBMISSION_FIELDS[3] + "='" + Boolean.TRUE.toString() + "' AND " + SUBMISSION_FIELDS[4] + "='" + Boolean.FALSE.toString() + "'";
					}
					else
					{
						sqlWhere ="where context='" + assignmentId(assignmentRef) + "' AND " + SUBMISSION_FIELDS[2] + " IS NOT NULL AND " + SUBMISSION_FIELDS[3] + "='" + Boolean.TRUE.toString() + "' AND " + SUBMISSION_FIELDS[4] + "='" + Boolean.FALSE.toString() + "'";
					}
				}
				
				if (a.getAccess().equals(Assignment.AssignmentAccess.GROUPED))
				{
					asgGroups = a.getGroups();
				}
				
				site = siteService.getSite(a.getContext());
				List l = super.getSelectedResourcesWhere(sqlWhere);
				
				if (a.isGroup()) {
                                    for (Object o : l) {
                                        AssignmentSubmission assignmentSubmission = (AssignmentSubmission)o;
                                        Group _gg = site.getGroup(assignmentSubmission.getSubmitterId());
                                        if (_gg != null) count++;
                                    }
				} else {
				// check whether the submitter is an active member of the site
				for (Object o : l) {
					AssignmentSubmission assignmentSubmission = (AssignmentSubmission)o;
					String userId = assignmentSubmission.getSubmitterIdString();
					Member member = site != null ? site.getMember(userId) : null;
					if(member != null && member.isActive()) 
					{	
						if (asgGroups != null)
						{
							// for group based assignment: check whether member is in any group if the assignment is for groups
							boolean inGroup = false;
							for (Iterator iAsgGroups=asgGroups.iterator(); site!=null && !inGroup && iAsgGroups.hasNext();)
							{
								String groupId = (String) iAsgGroups.next();
								try
								{
									Group group = site.getGroup(groupId);
									if ( group != null && group.getUserRole(userId) != null)
									{
										// in one of the group, hence increase the count
										inGroup = true;
										count++;
									}
								}
								catch (Exception ee)
								{
									M_log.warn(this + " getSubmissionsCountWhere " + ee.getMessage() + " sqlWhere = " + sqlWhere + " assignmentRef=" + assignmentRef);
								}
							}
						}
						else
						{
							// for site based assignment
							count++;
						}
					}
				}
				}
			} catch (Exception e)
			{
				M_log.warn(this + ".getSubmissionsCountWhere(): assignmentRef=" + assignmentRef + " " + e.getMessage());
			} catch (Throwable t) {
				M_log.warn(this + ".getSubmissionsCountWhere(): ", t);
				throw new IllegalArgumentException(t);
			}
			
			return count;
		}
		
		/**
		 * {@inheritDoc}
		 */
		public int getSubmittedSubmissionsCount(String assignmentRef)
		{
			return getSubmissionsCountWhere("submitted", assignmentRef);
		}
		
		/**
		 * {@inheritDoc}
		 */
		public int getUngradedSubmissionsCount(String assignmentRef)
		{
			return getSubmissionsCountWhere("ungraded", assignmentRef );
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
