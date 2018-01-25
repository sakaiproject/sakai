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


package org.sakaiproject.assignment.impl.conversion;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.util.conversion.SchemaConversionHandler;

@Slf4j
public class CombineDuplicateSubmissionsConversionHandler implements SchemaConversionHandler 
{
	// db driver
	private String m_dbDriver = null;
	/**
	 * {@inheritDoc}
	 */
	public String getDbDriver()
	{
		return m_dbDriver;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setDbDriver(String dbDriver)
	{
		m_dbDriver = dbDriver;
	}
	
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
				log.warn("Failed to parse {}[{}]{}", id, xml, e1);
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
			String submitTime0 = result.getDatesubmitted();
			String submitted0 = result.getSubmitted();
			String graded0 = result.getGraded();
			String id0 = result.getId();
			
			log.info("updating \"{} revising XML", id0);
			
			if (getDbDriver().indexOf("mysql") != -1)
			{
				// see http://bugs.sakaiproject.org/jira/browse/SAK-1737
				// MySQL setCharacterStream() is broken and truncates UTF-8
				// international characters sometimes. So use setBytes()
				// instead (just for MySQL).
				try
				{
					updateRecord.setBytes(1, xml0.getBytes("UTF-8"));
				}
				catch (UnsupportedEncodingException e)
				{
					log.info("{}{}", e.getMessage(), xml0);
				}
			}
			else
			{
				updateRecord.setCharacterStream(1, new StringReader(xml0), xml0.length());
			}
			
			updateRecord.setString(2, submitTime0);
			updateRecord.setString(3, submitted0);
			updateRecord.setString(4, graded0);
			updateRecord.setString(5, id0);
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
		
		boolean usePreviousRecords = false;

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
		else if("true".equals(item2.getSubmitted()) && item2.getDatesubmitted() != null
				&& ("true".equals(item1.getSubmitted()) && item1.getDatesubmitted() != null))
		{
			// both are valid in terms of submission status and submit date
			Integer t1 = getIntegerObject(item1.getDatesubmitted());
			Integer t2 = getIntegerObject(item2.getDatesubmitted());
			if (t1 != null && t2 != null)
			{
				String grade1= StringUtils.trimToNull(item1.getGrade());
				String grade2 = StringUtils.trimToNull(item2.getGrade());
				if (item1.getGradereleased().equalsIgnoreCase(Boolean.TRUE.toString()) && item2.getGradereleased().equalsIgnoreCase(Boolean.TRUE.toString()))
				{
					// if both grades has been released
					if (nonDefaultGrade(grade1) && nonDefaultGrade(grade2) && !grade1.equals(grade2))
					{
						// if both grades are release and are different, set the previous one as previous grade and keep the later one as the current grade
						// to-do: don't have a good way to modify all previous properties right now
						usePreviousRecords = true;
						if (t1.intValue() < t2.intValue())
						{
							// keep the later one
							keepItem = item2;
							removeItem = item1;
						}
						else
						{
							keepItem = item1;
							removeItem = item2;
						}
					}
					else if (nonDefaultGrade(grade1) && !nonDefaultGrade(grade2))
					{
						keepItem = item1;
						removeItem = item2;
					}
					else if (!nonDefaultGrade(grade1) && nonDefaultGrade(grade2))
					{
						keepItem = item2;
						removeItem = item1;
					}
					else
					{
						// both are default grade
					}
				}
				else if (item1.getGradereleased().equalsIgnoreCase(Boolean.TRUE.toString()))
				{
					// keep the released grade one
					keepItem = item1;
					removeItem = item2;
				}
				else if (item2.getGradereleased().equalsIgnoreCase(Boolean.TRUE.toString()))
				{
					// keep the released grade one
					keepItem = item2;
					removeItem = item1;
				}
				else
				{
					// neither been released
				}
					
			}
			else if (t1 != null)
			{
				// keep whichever is not null
				keepItem = item1;
				removeItem = item2;
			}
			else
			{
				// keep whichever is not null
				keepItem = item2;
				removeItem = item1;
			}
		}	
		else
		{
			// if there is no student submission, just duplicate instructor record
			if (StringUtils.trimToNull(item1.getFeedbacktext()) != null || StringUtils.trimToNull(item1.getFeedbackcomment()) != null || StringUtils.trimToNull(item1.getGrade()) != null)
			{
				// item 1 has some grading data
				keepItem = item1;
				removeItem = item2;
			}
			else if (StringUtils.trimToNull(item2.getFeedbacktext()) != null || StringUtils.trimToNull(item2.getFeedbackcomment()) != null || StringUtils.trimToNull(item2.getGrade()) != null)
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
		if(keepItem.getDatereturned() == null && removeItem.getDatereturned() != null)
		{
			keepItem.setDatereturned(removeItem.getDatereturned());
		}
		if(keepItem.getDatesubmitted() == null && removeItem.getDatesubmitted() != null)
		{
			keepItem.setDatesubmitted(removeItem.getDatesubmitted());
		}
		
		// in case we need to update the previous gradeing info inside properties
		if (usePreviousRecords)
		{
			log.info("need to update previous grading information keepItem id={} removeItem id={}", keepItem.getId(), removeItem.getId());
			
			Map<String, Object> propertiesMap = keepItem.saxSerializableProperties.getSerializableProperties();
			
			// the properties definition copied from ResourceProperties.java file 
			/** Property for assignment submission's previous grade (user settable). [String] */
			String PROP_SUBMISSION_PREVIOUS_GRADES = "CHEF:submission_previous_grades";

			/** Property for assignment submission's scaled previous grade (user settable). [String] */
			String PROP_SUBMISSION_SCALED_PREVIOUS_GRADES = "CHEF:submission_scaled_previous_grades";

			/** Property for assignment submission's previous inline feedback text (user settable). [String] */
			String PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT = "CHEF:submission_previous_feedback_text";

			/** Property for assignment submission's previous feedback comment (user settable). [String] */
			String PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT = "CHEF:submission_previous_feedback_comment";
			
			// the properties definition from AssignmentAction.java
			/** property for previous feedback attachments **/
			String PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS = "prop_submission_previous_feedback_attachments";
			String previousGrades = combineGrades((String) propertiesMap.get(PROP_SUBMISSION_PREVIOUS_GRADES), removeItem.getGrade(), "graded on " + removeItem.getDatereturned());
			if (previousGrades != null)
			{
				propertiesMap.put(PROP_SUBMISSION_PREVIOUS_GRADES, previousGrades);
			}
			
			String previousFeedbackText = combinePropertyWithText((String) propertiesMap.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT), removeItem.getFeedbacktext(), "graded on " + removeItem.getDatereturned());
			if (previousFeedbackText != null)
			{
				propertiesMap.put(PROP_SUBMISSION_PREVIOUS_FEEDBACK_TEXT, previousFeedbackText);
			}
			
			String previousFeedbackComment = combinePropertyWithText((String) propertiesMap.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT), removeItem.getFeedbackcomment(), "graded on " + removeItem.getDatereturned());
			if (previousFeedbackComment != null)
			{
				propertiesMap.put(PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT, previousFeedbackComment);
			}
			
			String previousAttachments = (String) propertiesMap.get(PROP_SUBMISSION_PREVIOUS_FEEDBACK_COMMENT);
			List<String> attachments = removeItem.getFeedbackattachments();
			if (attachments != null && attachments.size() > 0)
			{
				if (previousAttachments == null)
				{
					previousAttachments = "";
				}
				
				// add the attachments
				for (int k =0; k < attachments.size(); k++)
				{
					String nAttachment = (String) attachments.get(k);
					if (previousAttachments.indexOf(nAttachment) == -1)
					{
						previousAttachments = previousAttachments.concat(",").concat(nAttachment);
					}
				}
				if (StringUtils.trimToNull(previousAttachments) != null)
				{
					propertiesMap.put(PROP_SUBMISSION_PREVIOUS_FEEDBACK_ATTACHMENTS, StringUtils.trimToNull(previousAttachments));
				}
			}
			// reset the properties
			keepItem.saxSerializableProperties.setSerializableProperties(propertiesMap);
		}
		
		// feedback attachments
		keepItem.setFeedbackattachments(combineAttachments(keepItem.getFeedbackattachments(), removeItem.getFeedbackattachments()));
		
		// submitted attachments
		keepItem.setSubmittedattachments(combineAttachments(keepItem.getSubmittedattachments(), removeItem.getSubmittedattachments()));
		
		if (removeItem.getDatesubmitted() != null)
		{
			//submission text
			keepItem.setSubmittedtext(combineText(keepItem.getSubmittedtext(), removeItem.getSubmittedtext(), "submitted on " + removeItem.getDatesubmitted()));
			
			//submission_text_html
			keepItem.setSubmittedtext_html(combineText(keepItem.getSubmittedtext_html(), removeItem.getSubmittedtext_html(), "submitted on " + removeItem.getDatesubmitted()));
		}
		
		// feedback comment
		keepItem.setFeedbackcomment(combineText(keepItem.getFeedbackcomment(), removeItem.getFeedbackcomment(), "commented on " + removeItem.getDatereturned()));
		
		// feedback_comment_html
		keepItem.setFeedbackcomment_html(combineText(keepItem.getFeedbackcomment_html(), removeItem.getFeedbackcomment_html(), "commented on " + removeItem.getDatereturned()));

		// feedback text
		keepItem.setFeedbacktext(combineText(keepItem.getFeedbacktext(), removeItem.getFeedbacktext(), "commented on " + removeItem.getDatereturned()));

		// feedback_text_html
		keepItem.setFeedbacktext_html(combineText(keepItem.getFeedbacktext_html(), removeItem.getFeedbacktext_html(), "commented on " + removeItem.getDatereturned()));
		
		// review
		if(keepItem.getReviewReport() == null && removeItem.getReviewReport() != null)
		{
			keepItem.setReviewReport(removeItem.getReviewReport());
		}
		if(keepItem.getReviewScore() == null && removeItem.getReviewScore() != null)
		{
			keepItem.setReviewScore(removeItem.getReviewScore());
		}
		if(keepItem.getReviewStatus() == null && removeItem.getReviewStatus() != null)
		{
			keepItem.setReviewStatus(removeItem.getReviewStatus());
		}
		// what to do with properties????
		/// for now, we dump all the properties of the removeItem
//		if(keepItem.getSerializableProperties() == null)
//		{
//			keepItem.setSerializableProperties(removeItem.getSerializableProperties());
//		}
		

		return keepItem;
	}

	/**
	 * is this grade with a non default value?
	 * @param grade
	 * @return
	 */
	private boolean nonDefaultGrade(String grade)
	{
		// if the grade is not of the following pattern, consider it is useful
		boolean rv = false;
		if (grade == null)
		{
		}
		else if ("00".equals(grade))
		{
		}	
		else if ("0".equals(grade))
		{
		}	
		else if ("no grade".equals(grade))
		{
		}
		else if ("ungraded".equals(grade))
		{
		}
		else if ("Fail".equals(grade))
		{
		}
		else
		{
			rv = true;
		}
		
		return rv;
	}
	
	/**
	 * Whether both grades
	 * @param item1
	 * @param item2
	 * @return
	 */
	private boolean bothGradesReleasedAndDifferent(AssignmentSubmissionAccess item1, AssignmentSubmissionAccess item2)
	{
		// if both grades have been released and are different
		if (item1.getGradereleased().equalsIgnoreCase(Boolean.TRUE.toString()) && item2.getGradereleased().equalsIgnoreCase(Boolean.TRUE.toString()))
		{
			String grade1 = item1.getGrade();
			String grade2 = item2.getGrade();
			if (nonDefaultGrade(grade1) && nonDefaultGrade(grade2) && !grade1.equals(grade2))
			{
				// both grades are not of default grade, and also different
				// need to keep the record
				return true;
			}
		}
		return false;
	}
	
	/**
	 * combine text, both are Base64 encoded
	 * @param text
	 * @param rText
	 * @param date
	 * @return
	 */
	private String combineText(String text, String rText, String date) {
		return combine(text, true, rText, true, date);
	}
	
	/**
	 * combine plain text with Base64 encoded text
	 * @param text
	 * @param rText
	 * @param date
	 * @return
	 */
	private String combinePropertyWithText(String text, String rText, String date) {
		return combine(text, false, rText, true, date);
	}
	
	/**
	 * combine grades, both are not encoded
	 * @param text
	 * @param rText
	 * @param date
	 * @return
	 */
	private String combineGrades(String text, String rText, String date) {
		return combine(text, false, rText, false, date);
	}

	private String combine(String text, boolean textEncoded, String rText, boolean rTextEncoded, String date) {
		text = StringUtils.trimToNull(text);
		rText = StringUtils.trimToNull(rText);
		if(rText != null && date != null)
		{
			String decodedRText = rText;
			if (rTextEncoded)
			{
				try
				{
					decodedRText = new String(Base64.decodeBase64(rText.getBytes("UTF-8")));
				}catch (java.io.UnsupportedEncodingException ignore)
				{
					// ignore
					decodedRText = rText;
					log.warn("{}:combine {}", this, ignore.getMessage());
				}
			}
			
			if (text == null)
			{
				// use the rText instead
				text = decodedRText;
			}
			else
			{
				try
				{
					String decodedText = text;
					if (textEncoded)
					{
						try
						{
							decodedText = new String(Base64.decodeBase64(text.getBytes("UTF-8")));
						}
						catch (UnsupportedEncodingException e)
						{
							decodedText = text;
							log.warn("{}:combine {}", this, e.getMessage());
						}
					}
					
					if (decodedText.indexOf((decodedRText)) == -1)
					{
						String decoded= decodedText + "<p>" + date + ":</p><p>" + decodedRText + "</p>";
						if (textEncoded)
						{
							// return encoded
							try
							{
								text = new String(Base64.encodeBase64(decoded.getBytes()),"UTF-8");
							}
							catch (java.io.UnsupportedEncodingException e)
							{
								// ignore
								log.warn("{}:combine 2 {}", this, e.getMessage());
							}
						}
						else
						{
							// return plain
							text = decoded;
						}
					}
				}catch (Exception ee)
				{
					// ignore
					log.warn(" Combine: {}", ee.getMessage());
				}
			}
		}
		return text;
	}

	/**
	 * coombine attachments
	 * @param attachments
	 * @param rAttachments
	 * @return
	 */
	private List<String> combineAttachments(List<String> attachments, List<String> rAttachments) {
		if(rAttachments != null && !rAttachments.isEmpty())
		{
			if (attachments == null || attachments.isEmpty())
			{
				// if keepItem's attachment is empty, use the removeItem's instead
				attachments = rAttachments;
			}
			else
			{
				// find the missing attachment from removeItem, and add them to keepItem
				for(int i=0; i<rAttachments.size();i++)
				{
					String rAttachment = rAttachments.get(i);
					if (!attachments.contains(rAttachment))
					{
						// add this attachment from removeItem
						attachments.add(rAttachment);
					}
				}
			}
		}
		return attachments;
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
	
	/**
	 * get Integer based on passed string. Truncate the String if necessary
	 * @param timeString
	 * @return
	 */
	private Integer getIntegerObject(String timeString)
	{
		Integer rv = null;
		
		int max_length = Integer.valueOf(Integer.MAX_VALUE).toString().length();
		if (timeString.length() > max_length)
		{
			timeString = timeString.substring(0, max_length);
		}
		
		try
		{
			rv = Integer.parseInt(timeString);
		}
		catch (Exception e)
		{
			// ignore
			log.warn("{}:getIntegerObject {}", this, e.getMessage());
		}
		return rv;
	}
}

