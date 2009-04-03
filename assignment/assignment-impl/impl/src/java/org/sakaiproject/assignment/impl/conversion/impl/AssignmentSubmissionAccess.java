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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.assignment.impl.conversion.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.impl.conversion.api.SerializableSubmissionAccess;
import org.sakaiproject.assignment.impl.conversion.impl.SAXSerializablePropertiesAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 */
public class AssignmentSubmissionAccess implements SerializableSubmissionAccess,
		SerializableEntity
{
	protected static final Log log = LogFactory
			.getLog(AssignmentSubmissionAccess.class);

	private SAXParserFactory parserFactory;
	protected SAXSerializablePropertiesAccess saxSerializableProperties = new SAXSerializablePropertiesAccess();

	protected String id = null;

	protected String submitTime = null;

	protected List<String> submitterIds = new ArrayList<String>();

	protected String grade = null;

	protected String assignment = null;

	protected String context = null;

	protected String datereturned = null;

	protected String feedbackcomment = null;

	protected String feedbackcomment_html = null;

	protected String feedbacktext = null;

	protected String feedbacktext_html = null;

	protected String graded = null;

	protected String gradereleased = null;

	protected String lastmod = null;

	protected String pledgeflag = null;

	protected String returned = null;

	protected String reviewReport = null;

	protected String reviewScore = null;

	protected String reviewStatus = null;

	protected String submitted = null;

	protected List<String> submittedattachments = new ArrayList<String>();
	
	protected List<String> feedbackattachments = new ArrayList<String>();

	protected String datesubmitted = null;

	protected String submittedtext;

	protected String submittedtext_html;
	
	protected List<String> submitters = new ArrayList<String>();


	public AssignmentSubmissionAccess()
	{
		
	}

	public String toXml()
	{
		Document doc = Xml.createDocument();
		Element submission = doc.createElement("submission");
		doc.appendChild(submission);

		String numItemsString = null;
		String attributeString = null;
		String itemString = null;
		
		submission.setAttribute("reviewScore",this.reviewScore);
		submission.setAttribute("reviewReport",this.reviewReport);
		submission.setAttribute("reviewStatus",this.reviewStatus);
		
		
		submission.setAttribute("id", this.id);
		submission.setAttribute("context", this.context);
		submission.setAttribute("scaled_grade", this.grade);
		submission.setAttribute("assignment", this.assignment);
		submission.setAttribute("datesubmitted", this.datesubmitted);
		submission.setAttribute("datereturned", this.datereturned);
		submission.setAttribute("lastmod", this.lastmod);
		submission.setAttribute("submitted", this.submitted);
		submission.setAttribute("returned",this.returned);
		submission.setAttribute("graded", this.graded);
		submission.setAttribute("gradereleased", this.gradereleased);
		submission.setAttribute("pledgeflag", this.pledgeflag);

		// SAVE THE SUBMITTERS
		numItemsString = "" + this.submitters.size();
		submission.setAttribute("numberofsubmitters", numItemsString);
		for (int x = 0; x < this.submitters.size(); x++)
		{
			attributeString = "submitter" + x;
			itemString = (String) this.submitters.get(x);
			if (itemString != null)
			{
				submission.setAttribute(attributeString, itemString);
			}
		}

		// SAVE THE FEEDBACK ATTACHMENTS
		numItemsString = "" + this.feedbackattachments.size();
		submission.setAttribute("numberoffeedbackattachments", numItemsString);
		for (int x = 0; x < this.feedbackattachments.size(); x++)
		{
			attributeString = "feedbackattachment" + x;
			itemString = this.feedbackattachments.get(x);
			if(itemString != null)
			{
				submission.setAttribute(attributeString, itemString);
			}
		}
		// SAVE THE SUBMITTED ATTACHMENTS
		numItemsString = "" + this.submittedattachments.size();
		submission.setAttribute("numberofsubmittedattachments", numItemsString);
		for (int x = 0; x < this.submittedattachments.size(); x++)
		{
			attributeString = "submittedattachment" + x;
			itemString = this.submittedattachments.get(x);
			if (itemString != null)
			{
				submission.setAttribute(attributeString, itemString);
			}
		}
		
		submission.setAttribute("submittedtext", this.submittedtext);
		submission.setAttribute("submittedtext-html", this.submittedtext_html);
		submission.setAttribute("feedbackcomment", this.feedbackcomment);
		submission.setAttribute("feedbackcomment-html", this.feedbackcomment_html);
		submission.setAttribute("feedbacktext", this.feedbacktext);
		submission.setAttribute("feedbacktext-html", this.feedbacktext_html);

		// SAVE THE PROPERTIES
		Element properties = doc.createElement("properties");
		submission.appendChild(properties);
		
		Map<String, Object> props = this.saxSerializableProperties.getSerializableProperties();
		
		for(String key : props.keySet())
		{
			Object value = props.get(key);
			if (value instanceof String)
			{
				Element propElement = doc.createElement("property");
				properties.appendChild(propElement);
				propElement.setAttribute("name", key);

				// encode to allow special characters in the value
				Xml.encodeAttribute(propElement, "value", (String) value);
				propElement.setAttribute("enc", "BASE64");
			}
			else if (value instanceof List)
			{
				for (Object val : (List) value)
				{
					if(val == null)
					{
						continue;
					}
					if (val instanceof String)
					{
						Element propElement = doc.createElement("property");
						properties.appendChild(propElement);
						propElement.setAttribute("name", key);
						Xml.encodeAttribute(propElement, "value", (String) val);
						propElement.setAttribute("enc", "BASE64");
						propElement.setAttribute("list", "list");
					}
					else
					{
						log.warn(".toXml: in list not string: " + key);
					}
				}
			}
			else
			{
				log.warn(".toXml: not a string, not a value: " + key);
			}
		}
		
		return Xml.writeDocumentToString(doc);
		
	}

	/**
	 * @param xml
	 * @throws EntityParseException
	 */
	public void parse(String xml) throws Exception
	{
		Reader r = new StringReader(xml);
		InputSource ss = new InputSource(r);

		SAXParser p = null;
		if (parserFactory == null)
		{
			parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(false);
			parserFactory.setValidating(false);
		}
		try
		{
			p = parserFactory.newSAXParser();
		}
		catch (ParserConfigurationException e)
		{
			log.warn(this + ":parse " + e.getMessage());
			throw new SAXException("Failed to get a parser ", e);
		}
		final Map<String, Object> props = new HashMap<String, Object>();
		saxSerializableProperties.setSerializableProperties(props);
		
		p.parse(ss, new DefaultHandler()
		{

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
			 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
			 */
			@Override
			public void startElement(String uri, String localName, String qName,
					Attributes attributes) throws SAXException
			{

				if ("property".equals(qName))
				{

					String name = attributes.getValue("name");
					String enc = StringUtil.trimToNull(attributes.getValue("enc"));
					String value = null;
					if ("BASE64".equalsIgnoreCase(enc))
					{
						String charset = StringUtil.trimToNull(attributes
								.getValue("charset"));
						if (charset == null) charset = "UTF-8";

						value = Xml.decode(charset, attributes.getValue("value"));
					}
					else
					{
						value = attributes.getValue("value");
					}

					// deal with multiple valued lists
					if ("list".equals(attributes.getValue("list")))
					{
						// accumulate multiple values in a list
						Object current = props.get(name);

						// if we don't have a value yet, make a list to
						// hold
						// this one
						if (current == null)
						{
							List values = new ArrayList();
							props.put(name, values);
							values.add(value);
						}

						// if we do and it's a list, add this one
						else if (current instanceof List)
						{
							((List) current).add(value);
						}

						// if it's not a list, it's wrong!
						else
						{
							log.warn("construct(el): value set not a list: " + name);
						}
					}
					else
					{
						props.put(name, value);
					}
				}
				else if ("submission".equals(qName))
				{
					setId( attributes.getValue("id") );
					setAssignment(StringUtil.trimToNull(attributes.getValue("assignment")));
					setContext(StringUtil.trimToNull(attributes.getValue("context")));
					setDatereturned(StringUtil.trimToNull(attributes.getValue("datereturned")));
					setDatesubmitted(StringUtil.trimToNull(attributes.getValue("datesubmitted")));
					setFeedbackcomment(StringUtil.trimToNull(attributes.getValue("feedbackcomment")));
					if (StringUtil.trimToNull(attributes.getValue("feedbackcomment-html"))  != null)
					{
						setFeedbackcomment_html(StringUtil.trimToNull(attributes.getValue("feedbackcomment-html")));
					}
					else if (StringUtil.trimToNull(attributes.getValue("feedbackcomment-formatted"))  != null)
					{
						setFeedbackcomment_html(StringUtil.trimToNull(attributes.getValue("feedbackcomment-formatted")));
					}
					setFeedbacktext(StringUtil.trimToNull(attributes.getValue("feedbacktext")));
					
					if (StringUtil.trimToNull(attributes.getValue("feedbacktext-html")) != null)
					{
						setFeedbacktext_html(StringUtil.trimToNull(attributes.getValue("feedbacktext-html")));
					}
					else if (StringUtil.trimToNull(attributes.getValue("feedbacktext-formatted")) != null)
					{
						setFeedbacktext_html(StringUtil.trimToNull(attributes.getValue("feedbacktext-formatted")));
					}
						
					
					// get grade
					String grade = StringUtil.trimToNull(attributes.getValue("scaled_grade"));
					if (grade == null)
					{
						grade = StringUtil.trimToNull(attributes.getValue("grade"));
						if (grade != null)
						{
							try
							{
								Integer.parseInt(grade);
								// for the grades in points, multiple those by 10
								grade = grade + "0";
							}
							catch (Exception e)
							{
								log.warn(this + ":parse grade " + e.getMessage());
							}
						}
					}
					setGrade(grade);
					
					setGraded(StringUtil.trimToNull(attributes.getValue("graded")));
					setGradereleased(StringUtil.trimToNull(attributes.getValue("gradereleased")));
					setLastmod(StringUtil.trimToNull(attributes.getValue("lastmod")));
					String numberoffeedbackattachments = StringUtil.trimToNull(attributes.getValue("numberoffeedbackattachments"));
					int feedbackAttachmentCount = 0;
					try
					{
						feedbackAttachmentCount = Integer.parseInt(numberoffeedbackattachments);
					}
					catch (Exception e)
					{
						// use 0 for feedbackAttachmentCount
						log.warn(this + ":parse feedbackAttachmentCount " + e.getMessage());
					}
					for(int i = 0; i < feedbackAttachmentCount; i++)
					{
						String feedbackattachment = StringUtil.trimToNull(attributes.getValue("feedbackattachment" + i));
						if(feedbackattachment != null)
						{
							feedbackattachments.add(feedbackattachment);
						}
					}
					String numberofsubmittedattachments = StringUtil.trimToNull(attributes.getValue("numberofsubmittedattachments"));
					int submittedAttachmentCount = 0;
					try
					{
						submittedAttachmentCount = Integer.parseInt(numberofsubmittedattachments);
					}
					catch (Exception e)
					{
						// use 0 for submittedAttachmentCount
						log.warn(this + ":parse submittedAttachmentCount " + e.getMessage());
					}
					for(int i = 0; i < submittedAttachmentCount; i++)
					{
						String submittedattachment = StringUtil.trimToNull(attributes.getValue("submittedattachment" + i));
						if(submittedattachment != null)
						{
							submittedattachments.add(submittedattachment);
						}
					}
					setPledgeflag(StringUtil.trimToNull(attributes.getValue("pledgeflag")));
					setReturned(StringUtil.trimToNull(attributes.getValue("returned")));
					setReviewReport(StringUtil.trimToNull(attributes.getValue("reviewReport")));
					setReviewScore(StringUtil.trimToNull(attributes.getValue("reviewScore")));
					setReviewStatus(StringUtil.trimToNull(attributes.getValue("reviewStatus")));
					setSubmitted(StringUtil.trimToNull(attributes.getValue("submitted")));
					
					// submittedtext and submittedtext_html are base-64
					setSubmittedtext(StringUtil.trimToNull(attributes.getValue("submittedtext")));
					setSubmittedtext_html(StringUtil.trimToNull(attributes.getValue("submittedtext-html")));
					
					String numberofsubmitters = StringUtil.trimToNull(attributes.getValue("numberofsubmitters"));
					int submitterCount = 0;
					try
					{
						submitterCount = Integer.parseInt(numberofsubmitters);
					}
					catch (Exception e)
					{
						// use 0 for submittedAttachmentCount
						log.warn(this + ":Parse " + e.getMessage());
					}
					for(int i = 0; i < submitterCount; i++)
					{
						String submitter = StringUtil.trimToNull(attributes.getValue("submitter" + i));
						if(submitter != null)
						{
							submitters.add(submitter);
						}
					}
				}
			}
		});	
	}

	/**
	 * @param sax2
	 * @throws Exception
	 */
	public void check(SerializableSubmissionAccess sax2) throws Exception
	{
		StringBuilder sb = new StringBuilder();
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getId()
	 */
	public String getId() 
	{
		return id;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setId(java.lang.String)
	 */
	public void setId(String id) 
	{
		this.id = id;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getGrade()
	 */
	public String getGrade() 
	{
		return grade;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setGrade(java.lang.String)
	 */
	public void setGrade(String grade) 
	{
		this.grade = grade;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getAssignment()
	 */
	public String getAssignment() 
	{
		return assignment;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setAssignment(java.lang.String)
	 */
	public void setAssignment(String assignment) 
	{
		this.assignment = assignment;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getContext()
	 */
	public String getContext() 
	{
		return context;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setContext(java.lang.String)
	 */
	public void setContext(String context) 
	{
		this.context = context;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getDatereturned()
	 */
	public String getDatereturned() 
	{
		return datereturned;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setDatereturned(java.lang.String)
	 */
	public void setDatereturned(String datereturned) 
	{
		this.datereturned = datereturned;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getFeedbackcomment()
	 */
	public String getFeedbackcomment() 
	{
		return feedbackcomment;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setFeedbackcomment(java.lang.String)
	 */
	public void setFeedbackcomment(String feedbackcomment) 
	{
		this.feedbackcomment = feedbackcomment;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getFeedbackcomment_html()
	 */
	public String getFeedbackcomment_html() 
	{
		return feedbackcomment_html;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setFeedbackcomment_html(java.lang.String)
	 */
	public void setFeedbackcomment_html(String feedbackcomment_html) 
	{
		this.feedbackcomment_html = feedbackcomment_html;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getFeedbacktext()
	 */
	public String getFeedbacktext() 
	{
		return feedbacktext;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setFeedbacktext(java.lang.String)
	 */
	public void setFeedbacktext(String feedbacktext) 
	{
		this.feedbacktext = feedbacktext;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getFeedbacktext_html()
	 */
	public String getFeedbacktext_html() 
	{
		return feedbacktext_html;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setFeedbacktext_html(java.lang.String)
	 */
	public void setFeedbacktext_html(String feedbacktext_html) 
	{
		this.feedbacktext_html = feedbacktext_html;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getGraded()
	 */
	public String getGraded() 
	{
		return graded;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setGraded(java.lang.String)
	 */
	public void setGraded(String graded) 
	{
		this.graded = graded;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getGradereleased()
	 */
	public String getGradereleased() 
	{
		return gradereleased;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setGradereleased(java.lang.String)
	 */
	public void setGradereleased(String gradereleased) 
	{
		this.gradereleased = gradereleased;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getLastmod()
	 */
	public String getLastmod() 
	{
		return lastmod;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setLastmod(java.lang.String)
	 */
	public void setLastmod(String lastmod) 
	{
		this.lastmod = lastmod;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getPledgeflag()
	 */
	public String getPledgeflag() 
	{
		return pledgeflag;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setPledgeflag(java.lang.String)
	 */
	public void setPledgeflag(String pledgeflag) 
	{
		this.pledgeflag = pledgeflag;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getReturned()
	 */
	public String getReturned() 
	{
		return returned;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setReturned(java.lang.String)
	 */
	public void setReturned(String returned) 
	{
		this.returned = returned;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getReviewReport()
	 */
	public String getReviewReport() 
	{
		return reviewReport;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setReviewReport(java.lang.String)
	 */
	public void setReviewReport(String reviewReport) 
	{
		this.reviewReport = reviewReport;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getReviewScore()
	 */
	public String getReviewScore() 
	{
		return reviewScore;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setReviewScore(java.lang.String)
	 */
	public void setReviewScore(String reviewScore) 
	{
		this.reviewScore = reviewScore;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getReviewStatus()
	 */
	public String getReviewStatus() 
	{
		return reviewStatus;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setReviewStatus(java.lang.String)
	 */
	public void setReviewStatus(String reviewStatus) 
	{
		this.reviewStatus = reviewStatus;
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getSubmitted()
	 */
	public String getSubmitted() 
	{
		return submitted;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setSubmitted(java.lang.String)
	 */
	public void setSubmitted(String submitted) 
	{
		this.submitted = submitted;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getSubmittedattachments()
	 */
	public List<String> getSubmittedattachments() 
	{
		return submittedattachments;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setSubmittedattachments(java.util.List)
	 */
	public void setSubmittedattachments(List<String> submittedattachments) 
	{
		this.submittedattachments = submittedattachments;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getFeedbackattachments()
	 */
	public List<String> getFeedbackattachments() 
	{
		return feedbackattachments;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setFeedbackattachments(java.util.List)
	 */
	public void setFeedbackattachments(List<String> feedbackattachments) 
	{
		this.feedbackattachments = feedbackattachments;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getDatesubmitted()
	 */
	public String getDatesubmitted() 
	{
		return datesubmitted;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setDatesubmitted(java.lang.String)
	 */
	public void setDatesubmitted(String datesubmitted) 
	{
		this.datesubmitted = datesubmitted;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getSubmittedtext()
	 */
	public String getSubmittedtext() 
	{
		return submittedtext;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setSubmittedtext(java.lang.String)
	 */
	public void setSubmittedtext(String submittedtext) 
	{
		this.submittedtext = submittedtext;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getSubmittedtext_html()
	 */
	public String getSubmittedtext_html() 
	{
		return submittedtext_html;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setSubmittedtext_html(java.lang.String)
	 */
	public void setSubmittedtext_html(String submittedtext_html) 
	{
		this.submittedtext_html = submittedtext_html;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#getSubmitters()
	 */
	public List<String> getSubmitters() 
	{
		return submitters;
	}


	/* (non-Javadoc)
	 * @see org.sakaiproject.assignment.impl.conversion.impl.SerializableSubmissionAccess#setSubmitters(java.util.List)
	 */
	public void setSubmitters(List<String> submitters) 
	{
		this.submitters = submitters;
	}


	public SerializableEntity getSerializableProperties() 
	{
		return saxSerializableProperties;
	}

}

