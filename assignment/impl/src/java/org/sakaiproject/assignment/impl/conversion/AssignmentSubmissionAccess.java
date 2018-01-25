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

package org.sakaiproject.assignment.impl.conversion;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.entity.api.serialize.EntityParseException;
import org.sakaiproject.entity.api.serialize.SerializableEntity;
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
@Slf4j
public class AssignmentSubmissionAccess implements SerializableSubmissionAccess,
		SerializableEntity
{
	private SAXParserFactory parserFactory;
	protected SAXSerializablePropertiesAccess saxSerializableProperties = new SAXSerializablePropertiesAccess();

	protected String id = null;

	protected String submitTime = null;

	protected List<String> submitterIds = new ArrayList<String>();

	protected String grade = null;
	
	protected int m_factor;

	protected String assignment = null;

	protected String context = null;

	protected String datereturned = null;

	protected String feedbackcomment = null;

	protected String feedbackcomment_html = null;

	protected String feedbacktext = null;

	protected String feedbacktext_html = null;

	protected String graded = null;
	
	protected String gradedBy = null;

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

	protected List<String> grades = new ArrayList<String>();

	protected String datesubmitted = null;

	protected String submittedtext;

	protected String submittedtext_html;
	
        protected String submitterid = null;
        
	protected List<String> submitters = new ArrayList<String>();


	public AssignmentSubmissionAccess()
	{
		
	}

	/**
	 * Helper method to add elements or attributes to a list
	 * @param attributeName Name of the attribute or element value to add
	 * @param list The list to add to add elements to
	 * @param attributes A object of Element or Attributes that will be used as a source
	 * @param dereference Whether or not it needs to be created as a reference from entitybroker
	 */
	protected void addElementsToList(String attributeName, List list, Object attributes, boolean dereference) {
		int x=0;
		String tempString = null;
		//Can handle either values coming as an Element or Attributes
		if (attributes instanceof Element) {
			tempString = ((Element) attributes).getAttribute(attributeName+x);
		}
		else if (attributes instanceof Attributes) {
			tempString = ((Attributes) attributes).getValue(attributeName+x);
		}
		tempString = StringUtils.trimToNull(tempString);
		while (tempString != null)
		{
			/* This method doesn't seem to deference
			Reference tempReference;
			if (dereference==true) {
				tempReference = m_entityManager.newReference(tempString);
				list.add(tempReference);
			}
			else {
				list.add(tempString);
			}
			*/
			list.add(tempString);
			x++;
			if (attributes instanceof Element) {
				tempString = ((Element) attributes).getAttribute(attributeName+x);
			}
			else if (attributes instanceof Attributes) {
				tempString = ((Attributes) attributes).getValue(attributeName+x);
			}
			tempString = StringUtils.trimToNull(tempString);
		} 
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
		submission.setAttribute("scaled_factor", String.valueOf(this.m_factor));
		submission.setAttribute("assignment", this.assignment);
		submission.setAttribute("datesubmitted", this.datesubmitted);
		submission.setAttribute("datereturned", this.datereturned);
		submission.setAttribute("lastmod", this.lastmod);
		submission.setAttribute("submitted", this.submitted);
		submission.setAttribute("submitterid", this.submitterid);
		submission.setAttribute("returned",this.returned);
		submission.setAttribute("graded", this.graded);
		submission.setAttribute("gradedBy", this.gradedBy);
		submission.setAttribute("gradereleased", this.gradereleased);
		submission.setAttribute("pledgeflag", this.pledgeflag);

		// SAVE THE SUBMITTERS
		for (int x = 0; x < this.submitters.size(); x++)
		{
			attributeString = "submitter" + x;
			itemString = (String) this.submitters.get(x);
			if (itemString != null)
			{
				submission.setAttribute(attributeString, itemString);
			}
		}

		// SAVE GRADE OVERRIDES
		for (int x = 0; x < this.grades.size(); x++) {
		    attributeString = "grade" + x;
		    itemString = (String) this.grades.get(x);
		    if (itemString != null) {
		        submission.setAttribute(attributeString, itemString);
		    }
		}

		// SAVE THE FEEDBACK ATTACHMENTS
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
		
		for(Map.Entry<String, Object> entry : props.entrySet())
		{
			String key = entry.getKey();
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
						log.warn(".toXml: in list not string: {}", key);
					}
				}
			}
			else
			{
				log.warn(".toXml: not a string, not a value: {}", key);
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
			log.warn("{}:parse {}", this, e.getMessage());
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
					String enc = StringUtils.trimToNull(attributes.getValue("enc"));
					String value = null;
					if ("BASE64".equalsIgnoreCase(enc))
					{
						String charset = StringUtils.trimToNull(attributes
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
							log.warn("construct(el): value set not a list: {}", name);
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
					setAssignment(StringUtils.trimToNull(attributes.getValue("assignment")));
					setContext(StringUtils.trimToNull(attributes.getValue("context")));
					setDatereturned(StringUtils.trimToNull(attributes.getValue("datereturned")));
					setDatesubmitted(StringUtils.trimToNull(attributes.getValue("datesubmitted")));
					setFeedbackcomment(StringUtils.trimToNull(attributes.getValue("feedbackcomment")));
					if (StringUtils.trimToNull(attributes.getValue("feedbackcomment-html"))  != null)
					{
						setFeedbackcomment_html(StringUtils.trimToNull(attributes.getValue("feedbackcomment-html")));
					}
					else if (StringUtils.trimToNull(attributes.getValue("feedbackcomment-formatted"))  != null)
					{
						setFeedbackcomment_html(StringUtils.trimToNull(attributes.getValue("feedbackcomment-formatted")));
					}
					setFeedbacktext(StringUtils.trimToNull(attributes.getValue("feedbacktext")));
					
					if (StringUtils.trimToNull(attributes.getValue("feedbacktext-html")) != null)
					{
						setFeedbacktext_html(StringUtils.trimToNull(attributes.getValue("feedbacktext-html")));
					}
					else if (StringUtils.trimToNull(attributes.getValue("feedbacktext-formatted")) != null)
					{
						setFeedbacktext_html(StringUtils.trimToNull(attributes.getValue("feedbacktext-formatted")));
					}
					
					// get number of decimals
					String factor = StringUtils.trimToNull(attributes.getValue("scaled_factor"));
					if (factor == null) {
						factor = String.valueOf(AssignmentConstants.DEFAULT_SCALED_FACTOR);
					}
					m_factor = Integer.valueOf(factor);
					
					// get grade
					String grade = StringUtils.trimToNull(attributes.getValue("scaled_grade"));
					if (grade == null)
					{
						grade = StringUtils.trimToNull(attributes.getValue("grade"));
						if (grade != null)
						{
							try
							{
								Integer.parseInt(grade);
								// for the grades in points, multiple those by factor
								grade = grade + factor.substring(1);
							}
							catch (Exception e)
							{
								log.warn("{}:parse grade {}", this, e.getMessage());
							}
						}
					}
					setGrade(grade);
					
					setGraded(StringUtils.trimToNull(attributes.getValue("graded")));
					setGradedBy(StringUtils.trimToNull(attributes.getValue("gradedBy")));
					setGradereleased(StringUtils.trimToNull(attributes.getValue("gradereleased")));
					setLastmod(StringUtils.trimToNull(attributes.getValue("lastmod")));

					addElementsToList("feedbackattachment",feedbackattachments,attributes,false);
					addElementsToList("submittedattachment",submittedattachments,attributes,false);

					setPledgeflag(StringUtils.trimToNull(attributes.getValue("pledgeflag")));
					setReturned(StringUtils.trimToNull(attributes.getValue("returned")));
					setReviewReport(StringUtils.trimToNull(attributes.getValue("reviewReport")));
					setReviewScore(StringUtils.trimToNull(attributes.getValue("reviewScore")));
					setReviewStatus(StringUtils.trimToNull(attributes.getValue("reviewStatus")));
					setSubmitted(StringUtils.trimToNull(attributes.getValue("submitted")));
					
					// submittedtext and submittedtext_html are base-64
					setSubmittedtext(StringUtils.trimToNull(attributes.getValue("submittedtext")));
					setSubmittedtext_html(StringUtils.trimToNull(attributes.getValue("submittedtext-html")));
					setSubmitterId(StringUtils.trimToNull(attributes.getValue("submitterid")));

					addElementsToList("submitter",submitters,attributes,false);
					// for backward compatibility of assignments without submitter ids
					if (getSubmitterId() == null && submitters.size() > 0) {
						setSubmitterId(submitters.get(0));
					}
					addElementsToList("grade",grades,attributes,false);
				}
			}
		});	
	}

	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getId()
	 */
	public String getId() 
	{
		return id;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setId(java.lang.String)
	 */
	public void setId(String id) 
	{
		this.id = id;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getGrade()
	 */
	public String getGrade() 
	{
		return grade;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setGrade(java.lang.String)
	 */
	public void setGrade(String grade) 
	{
		this.grade = grade;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getAssignment()
	 */
	public String getAssignment() 
	{
		return assignment;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setAssignment(java.lang.String)
	 */
	public void setAssignment(String assignment) 
	{
		this.assignment = assignment;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getContext()
	 */
	public String getContext() 
	{
		return context;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setContext(java.lang.String)
	 */
	public void setContext(String context) 
	{
		this.context = context;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getDatereturned()
	 */
	public String getDatereturned() 
	{
		return datereturned;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setDatereturned(java.lang.String)
	 */
	public void setDatereturned(String datereturned) 
	{
		this.datereturned = datereturned;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getFeedbackcomment()
	 */
	public String getFeedbackcomment() 
	{
		return feedbackcomment;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setFeedbackcomment(java.lang.String)
	 */
	public void setFeedbackcomment(String feedbackcomment) 
	{
		this.feedbackcomment = feedbackcomment;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getFeedbackcomment_html()
	 */
	public String getFeedbackcomment_html() 
	{
		return feedbackcomment_html;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setFeedbackcomment_html(java.lang.String)
	 */
	public void setFeedbackcomment_html(String feedbackcomment_html) 
	{
		this.feedbackcomment_html = feedbackcomment_html;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getFeedbacktext()
	 */
	public String getFeedbacktext() 
	{
		return feedbacktext;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setFeedbacktext(java.lang.String)
	 */
	public void setFeedbacktext(String feedbacktext) 
	{
		this.feedbacktext = feedbacktext;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getFeedbacktext_html()
	 */
	public String getFeedbacktext_html() 
	{
		return feedbacktext_html;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setFeedbacktext_html(java.lang.String)
	 */
	public void setFeedbacktext_html(String feedbacktext_html) 
	{
		this.feedbacktext_html = feedbacktext_html;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getGraded()
	 */
	public String getGraded() 
	{
		return graded;
	}

	public String getGradedBy(){
		return gradedBy;
	}

	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setGraded(java.lang.String)
	 */
	public void setGraded(String graded) 
	{
		this.graded = graded;
	}

	public void setGradedBy(String gradedBy){
		this.gradedBy = gradedBy;
	}

	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getGradereleased()
	 */
	public String getGradereleased() 
	{
		return gradereleased;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setGradereleased(java.lang.String)
	 */
	public void setGradereleased(String gradereleased) 
	{
		this.gradereleased = gradereleased;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getLastmod()
	 */
	public String getLastmod() 
	{
		return lastmod;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setLastmod(java.lang.String)
	 */
	public void setLastmod(String lastmod) 
	{
		this.lastmod = lastmod;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getPledgeflag()
	 */
	public String getPledgeflag() 
	{
		return pledgeflag;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setPledgeflag(java.lang.String)
	 */
	public void setPledgeflag(String pledgeflag) 
	{
		this.pledgeflag = pledgeflag;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getReturned()
	 */
	public String getReturned() 
	{
		return returned;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setReturned(java.lang.String)
	 */
	public void setReturned(String returned) 
	{
		this.returned = returned;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getReviewReport()
	 */
	public String getReviewReport() 
	{
		return reviewReport;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setReviewReport(java.lang.String)
	 */
	public void setReviewReport(String reviewReport) 
	{
		this.reviewReport = reviewReport;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getReviewScore()
	 */
	public String getReviewScore() 
	{
		return reviewScore;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setReviewScore(java.lang.String)
	 */
	public void setReviewScore(String reviewScore) 
	{
		this.reviewScore = reviewScore;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getReviewStatus()
	 */
	public String getReviewStatus() 
	{
		return reviewStatus;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setReviewStatus(java.lang.String)
	 */
	public void setReviewStatus(String reviewStatus) 
	{
		this.reviewStatus = reviewStatus;
	}

	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getSubmitted()
	 */
	public String getSubmitted() 
	{
		return submitted;
	}

        public String getSubmitterId() {
            return submitterid;
        }

	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setSubmitted(java.lang.String)
	 */
	public void setSubmitted(String submitted) 
	{
		this.submitted = submitted;
	}


        public void setSubmitterId(String id) {
            this.submitterid = id;
        }
        
	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getSubmittedattachments()
	 */
	public List<String> getSubmittedattachments() 
	{
		return submittedattachments;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setSubmittedattachments(java.util.List)
	 */
	public void setSubmittedattachments(List<String> submittedattachments) 
	{
		this.submittedattachments = submittedattachments;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getFeedbackattachments()
	 */
	public List<String> getFeedbackattachments() 
	{
		return feedbackattachments;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setFeedbackattachments(java.util.List)
	 */
	public void setFeedbackattachments(List<String> feedbackattachments) 
	{
		this.feedbackattachments = feedbackattachments;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getDatesubmitted()
	 */
	public String getDatesubmitted() 
	{
		return datesubmitted;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setDatesubmitted(java.lang.String)
	 */
	public void setDatesubmitted(String datesubmitted) 
	{
		this.datesubmitted = datesubmitted;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getSubmittedtext()
	 */
	public String getSubmittedtext() 
	{
		return submittedtext;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setSubmittedtext(java.lang.String)
	 */
	public void setSubmittedtext(String submittedtext) 
	{
		this.submittedtext = submittedtext;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getSubmittedtext_html()
	 */
	public String getSubmittedtext_html() 
	{
		return submittedtext_html;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setSubmittedtext_html(java.lang.String)
	 */
	public void setSubmittedtext_html(String submittedtext_html) 
	{
		this.submittedtext_html = submittedtext_html;
	}


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#getSubmitters()
	 */
	public List<String> getSubmitters() 
	{
		return submitters;
	}

        public List<String>getGrades() { return grades; }
        public void setGrades(List<String> gr) { this.grades = gr; }


	/* (non-Javadoc)
	 * @see SerializableSubmissionAccess#setSubmitters(java.util.List)
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

