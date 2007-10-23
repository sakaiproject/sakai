/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007 The Sakai Foundation.
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

package org.sakaiproject.assignment.impl.conversion.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.conversion.SchemaConversionHandler;

public class CombineDuplicateSubmissionsConversionHandler implements SchemaConversionHandler 
{
	private static final Log log = LogFactory.getLog(CombineDuplicateSubmissionsConversionHandler.class);

	public boolean convertSource(String id, Object source, PreparedStatement updateRecord) throws SQLException 
	{
		List<String> xml = (List<String>) source;
		SortedSet<String> identifiers = new TreeSet<String>();

		List<AssignmentSubmissionAccess> saxlist = new ArrayList<AssignmentSubmissionAccess>();
		for(int i = 0; i < xml.size(); i++)
		{
			AssignmentSubmissionAccess sax = new AssignmentSubmissionAccess();
			saxlist.add(sax);
			try
			{
				sax.parse(xml.get(i));
				identifiers.add(sax.getId());
			}
			catch (Exception e1)
			{
				log.warn("Failed to parse " + id + "[" + xml + "]", e1);
				// return false;
			}
		}
		
		for(int i = saxlist.size() - 1; i > 0; i--)
		{
			saxlist.set(i - 1, combineItems(saxlist.get(i), saxlist.get(i - 1)));
		}
		
		if (saxlist.size() > 0) {
			AssignmentSubmissionAccess result = saxlist.get(0);
			
			String xml0 = result.toXml();
			String id0 = result.getId();
			
			log.info("updating \"" + id0 + " (revising XML as follows:\n" + xml0);
			
			updateRecord.setString(1, xml0);
			updateRecord.setString(2, id0);
			return true;
		}
		else {
			return false;
		}
	}

	protected AssignmentSubmissionAccess combineItems(AssignmentSubmissionAccess item1, AssignmentSubmissionAccess item2) 
	{
		AssignmentSubmissionAccess keepItem=item1;
		AssignmentSubmissionAccess removeItem=item2;

		// for normal assignment
		//it is student-generated	(submitted==TRUE && dateSubmittted==SOME_TIMESTAMP) or submitted=false),
		//or it is instructor generated	(submitted==TRUE && dateSubmitted==null)
		if("true".equals(item1.getSubmitted()) && item1.getDatesubmitted() != null
			&& !("true".equals(item2.getSubmitted()) && item2.getDatesubmitted() != null))
		{
			// item1 is student submission
			keepItem = item1;
			removeItem = item2;
		}
		else if("true".equals(item2.getSubmitted()) && item2.getDatesubmitted() != null
				&& !("true".equals(item1.getSubmitted()) && item1.getDatesubmitted() != null))
		{
			// item2 is student submission
			keepItem = item2;
			removeItem = item1;
		}
		else
		{
			// if there is no student submission, just duplicate instructor record
			if (StringUtil.trimToNull(item1.getFeedbacktext()) != null || StringUtil.trimToNull(item1.getFeedbackcomment()) != null || StringUtil.trimToNull(item1.getGrade()) != null)
			{
				// item 1 has some grading data
				keepItem = item1;
				removeItem = item2;
			}
			else if (StringUtil.trimToNull(item2.getFeedbacktext()) != null || StringUtil.trimToNull(item2.getFeedbackcomment()) != null || StringUtil.trimToNull(item2.getGrade()) != null)
			{
				// item 2 has some grading data
				keepItem = item2;
				removeItem = item1;
			}
			else
			{
				// if none of them contains useful information, randomly pick one to keep
				//keepItem = item1;
				//removeItem = item2;
			}
		}

		// need to verify whether student or instructor data 
		// takes precedence if both exist
		if(keepItem.getDatereturned() == null)
		{
			keepItem.setDatereturned(removeItem.getDatereturned());
		}
		if(keepItem.getDatesubmitted() == null)
		{
			keepItem.setDatesubmitted(removeItem.getDatesubmitted());
		}
		
		List feedbackattachments = keepItem.getFeedbackattachments();
		if(feedbackattachments == null || feedbackattachments.isEmpty())
		{
			keepItem.setFeedbackattachments(removeItem.getFeedbackattachments());
		}
		if(keepItem.getFeedbackcomment() == null)
		{
			keepItem.setFeedbackcomment(removeItem.getFeedbackcomment());
		}
		if(keepItem.getFeedbackcomment_html() == null)
		{
			keepItem.setFeedbackcomment_html(removeItem.getFeedbackcomment_html());
		}
		if(keepItem.getFeedbacktext() == null)
		{
			keepItem.setFeedbacktext(keepItem.getFeedbacktext());
		}
		if(keepItem.getFeedbacktext_html() == null)
		{
			keepItem.setFeedbacktext_html(removeItem.getFeedbacktext_html());
		}
		if(keepItem.getGrade() == null)
		{
			keepItem.setGrade(removeItem.getGrade());
		}
		if(keepItem.getGraded() == null)
		{
			keepItem.setGraded(removeItem.getGraded());
		}
		if(keepItem.getGradereleased() == null)
		{
			keepItem.setGradereleased(removeItem.getGradereleased());
		}
		if(keepItem.getReturned() == null)
		{
			keepItem.setReturned(removeItem.getReturned());
		}
		if(keepItem.getReviewReport() == null)
		{
			keepItem.setReviewReport(removeItem.getReviewReport());
		}
		if(keepItem.getReviewScore() == null)
		{
			keepItem.setReviewScore(removeItem.getReviewScore());
		}
		if(keepItem.getReviewStatus() == null)
		{
			keepItem.setReviewStatus(removeItem.getReviewStatus());
		}
		if(keepItem.getScaled_grade() == null)
		{
			keepItem.setScaled_grade(removeItem.getScaled_grade());
		}
		// what to do with properties????
		/// for now, we dump all the properties of the removeItem
//		if(keepItem.getSerializableProperties() == null)
//		{
//			keepItem.setSerializableProperties(removeItem.getSerializableProperties());
//		}
		

		return keepItem;
	}

	public Object getSource(String id, ResultSet rs) throws SQLException 
	{
		List<String> xml = new ArrayList<String>();
		while (rs.next())
		{
			xml.add(rs.getString(1));
		}
		
		return xml;
	}

	public Object getValidateSource(String id, ResultSet rs)
			throws SQLException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void validate(String id, Object source, Object result)
			throws Exception 
	{
		
	}

}
