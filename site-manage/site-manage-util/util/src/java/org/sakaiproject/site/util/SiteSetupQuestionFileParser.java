/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.site.util;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.sitemanage.api.model.*;

/**
 * This parser is mainly to parse the questions.xml file:
 * @author zqian
 *
 */
@Slf4j
public class SiteSetupQuestionFileParser
{

	private static org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService questionService = (org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService) ComponentManager
	.get(org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService.class);
	
	/* Here is a template of the question.xml file:
	   <?xml version="1.0" encoding="UTF-8" ?> 
	 	<SiteSetupQuestions>
			<site type="project">
				<header>Please answer the following to help us understand how CTools is being used for this project site.</header> 
				<url><a href="http://www.google.com" target="_blank">More info</a></url>
				<question required="true" multiple_answers="false">
	  				<q>In what capacity are you creating this site?</q> 
	 				<answer>Student</answer> 
	  				<answer>Faculty</answer> 
	  				<answer>Staff</answer> 
	  			</question>
				<question required="false" multiple_answers="false">
	  				<q>The primary use for this project site will be:</q> 
	  				<answer>Learning"</answer> 
	 				<answer>Research"</answer> 
	  				<answer>Administrative</answer> 
	  				<answer>Personal</answer> 
	  				<answer>Student group/organization</answer> 
	  				<answer fillin_blank="true">Other</answer> 
	  			</question>
  			</site>
			<site type="course">
				<header>Please answer the following to help us understand how CTools is being used for this course site.</header> 
				<question required="true" multiple_answers="false">
	  				<q>The primary use for this project site will be:</q> 
	  				<answer>Student</answer> 
	  				<answer>Faculty</answer> 
	  				<answer>Staff</answer> 
	  			</question>
	  		</site>
	  	</SiteSetupQuestions>
	 */
	private static ContentHostingService contentHostingService = (ContentHostingService) ComponentManager.get("org.sakaiproject.content.api.ContentHostingService");
	
	protected static String m_adminSiteName = "setupQuestionsAdmin";
	protected static String m_configFolder = "config";
	protected static String m_configBackupFolder = "configBackup";
	protected static String m_configXml = "questions.xml";
	
	protected static SiteSetupQuestionMap m_siteSetupQuestionMap;
	
	public String getAdminSiteName() {
		return m_adminSiteName;
	}

	public void setAdminSiteName(String siteName) {
		m_adminSiteName = siteName;
	}
	    
	/**
	 * the reference to config folder
	 * @return
	 */
	public static String getConfigFolderReference()
	{
		String configFolderRef = null;
		if(StringUtils.trimToNull(m_adminSiteName) != null && StringUtils.trimToNull(m_configFolder) != null)
		{
			configFolderRef = "/content/group/" + m_adminSiteName + "/" + m_configFolder + "/";
		}
		return configFolderRef;
	}
	
	/**
	 * the reference to config backup folder
	 * @return
	 */
	public static String getConfigBackupFolderReference()
	{
		String configBackupFolderRef = null;
		if(StringUtils.trimToNull(m_adminSiteName) != null && StringUtils.trimToNull(m_configBackupFolder) != null)
		{
			configBackupFolderRef = "/content/group/" + m_adminSiteName + "/" + m_configBackupFolder + "/";
		}
		return configBackupFolderRef;
	}
	
	/**
	 * Is the configuration XML file provided and readable
	 * @return true If the XML file is provided and readable, false otherwise
	 */
	public static boolean isConfigurationXmlAvailable()
	{
		try
	    {
			String x = "";
			if (m_configXml == null)
			{
				return false;
			}
			return exists(m_configXml);
	    }
		catch (Exception exception)
		{
			log.warn("Unexpected exception: " + exception);
		}
		return false;
	}
	
	//
	  /**
	   * Does the specified configuration/hierarchy resource exist?
	   * @param resourceName Resource name
	   * @return true If we can access this content
	   */
	  protected static  boolean exists(String resourceName)
	  {
	    String configFolderRef  = getConfigFolderReference();


     	if (StringUtils.trimToNull(configFolderRef) != null && StringUtils.trimToNull(resourceName)!=null)
    	{
     		String referenceName = configFolderRef + resourceName;

     		Reference reference = EntityManager.newReference(referenceName);
    		if (reference == null) return false;

    		enableSecurityAdvisor();
    		ContentResource resource= null;
    		try
    		{
    			resource = contentHostingService.getResource(reference.getId());
    			// as a remind for newly added configuration file
    			log.info("exists(): find new resource " + reference.getId());
    		}
    		catch (Exception ee)
    		{
    			// the configuration xml file are added, and get read immediately and moved to the config backup folder afterwards. Its contents are stored into database
    			// so it is normal to find the the configuration xml missing from the original config folder
    			// this exception is as expected, don't put it into log file
    		}
    		popSecurityAdvisor();

        return (resource != null);
    	}
     	
	    return false;
	  }
	  
	/**
	* Establish a security advisor to allow the "embedded" azg work to occur
	* with no need for additional security permissions.
	*/
	protected static void enableSecurityAdvisor()
	{
		// put in a security advisor so we can create citationAdmin site without need
		// of further permissions
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	* remove recent SecurityAdvisor
	*/
	protected static void popSecurityAdvisor()
	{
		// remove recent the security advisor
		SecurityService.popAdvisor();
	}
	
	  /**
	   * Update configuration data from an XML resource
	   * @param configFileRef XML configuration reference (/content/...)
	   */
	  public static SiteSetupQuestionMap updateConfig()
	  {
		String configFolder = getConfigFolderReference();
		Reference configFolderReference = EntityManager.newReference(configFolder);
		
		String configBackupFolder = getConfigBackupFolderReference();
		Reference configBackupolderReference = EntityManager.newReference(configBackupFolder);
		
	    Reference ref = EntityManager.newReference(configFolder + m_configXml);
	    Reference refBackup = EntityManager.newReference(configBackupFolder + m_configXml);

	    if (ref != null)
	    {
	      try
	      {
	        /*
	         * Fetch configuration details from our XML resource
	         */
	        enableSecurityAdvisor();

	        ContentResource resource = contentHostingService.getResource(ref.getId());
	        if (resource != null)
	        {
	        	// step 0: set all existing questions to be non-current and remove all SiteTypeQuestions
	        	List<SiteSetupQuestion> questions = questionService.getAllSiteQuestions();
	        	if (questions != null && !questions.isEmpty())
	        	{
	        		for (SiteSetupQuestion question:questions)
	        		{
	        			if (question.getCurrent().equals("true"))
	        			{
		        			question.setCurrent("false");
		        			questionService.saveSiteSetupQuestion(question);
	        			}
	        		}
	        	}
	        	questionService.removeAllSiteTypeQuestions();
	        	
	        	// Step 1: read in questions and answers
	        	m_siteSetupQuestionMap = populateConfig(ref.getReference(), resource.streamContent());
	          
	        	// Step 2: make a copy of current question file
	        	// make sure the back folder exists
	        	if (!contentHostingService.isAvailable(configBackupolderReference.getId()))
	        	{
	        		try
	        		{
	        			ContentCollectionEdit fEdit = contentHostingService.addCollection(configBackupolderReference.getId(), m_configBackupFolder);
	        			contentHostingService.commitCollection(fEdit);
	        		}
	        		catch (Exception ee)
	        		{
	        			log.warn("SiteSetupQuestionMap.updateConfig: Problem of adding backup collection " + configBackupolderReference.getId() + ee.getMessage());
	        		}
	        	}
	          
	        	if (contentHostingService.isAvailable(configBackupolderReference.getId()))
	        	{
	        		try
	        		{
	        			contentHostingService.copy(ref.getId(), refBackup.getId());
	        		}
	        		catch (Exception ee)
	        		{
	        			log.warn("SiteSetupQuestionMap.updateConfig: Problem of backing up question.xml file " + ee.getMessage());
	        		}
	        	}
	          
	        	// Step 3: remove question file
	        	try
	        	{
	        		ContentResourceEdit rEdit = contentHostingService.editResource(ref.getId());
	        		contentHostingService.removeResource(rEdit);
	        	}
	        	catch (Exception ee)
	        	{
	        		log.warn("SiteSetupQuestionMap.updateConfig: Problem of removing resource " + ref.getId() + ee.getMessage());
	        	} 
	        }
	        // remove recent the security advisor
	        popSecurityAdvisor();
	      }
	      catch (PermissionException e)
	      {
	        log.warn("Exception: " + e + ", continuing");
	      }
	      catch (IdUnusedException e)
	      {
	        log.info("configuration XML is missing ("
	              +    m_configXml
	              +    "); Citations ConfigurationService will watch for its creation");
	      }
	      catch (TypeException e)
	      {
	        log.warn("Exception: " + e + ", continuing");
	      }
	      catch (ServerOverloadException e)
	      {
	        log.warn("Exception: " + e + ", continuing");
	      }
	    }
	    
	    return m_siteSetupQuestionMap;
	  }
	  
	  /**
	   * Populate cached values from a configuration XML resource.  We always try
	   * to parse the resource, regardless of any prior success or failure.
	   *
	   * @param configurationXml Configuration resource name (this doubles as a
	   *                         unique key into the configuration cache)
	   */
	  protected static SiteSetupQuestionMap populateConfig(String configurationXml, InputStream stream)
	  {
	    org.w3c.dom.Document  document;
	    String                value;

	    /*
	     * Parse the XML - if that fails, give up now
	     */
	    if ((document = parseXmlFromStream(stream)) == null)
	    {
	      return null;
	    }
	    
	    SiteSetupQuestionMap m = new SiteSetupQuestionMap();
	    
	    Element rootElement = document.getDocumentElement();
		NodeList childList = rootElement.getChildNodes();
	    // root element should be "SiteSetupQuestions"
	    if (childList == null || childList.getLength() == 0)
		{
			log.warn("Cannot find elements in SiteSetupQuestions");
		}
		else
		{
			for (int i = 0; i < childList.getLength(); i++)
			{
				Node currentNode = childList.item(i);
				switch (currentNode.getNodeType())
				{
					case Node.TEXT_NODE:
						break;
					case Node.ELEMENT_NODE:
						if (currentNode.hasAttributes())
						{
							NamedNodeMap nNMap = currentNode.getAttributes();
							String siteType = nNMap.getNamedItem("type") != null ? nNMap.getNamedItem("type").getNodeValue():null;
							if (siteType != null)
							{
								// add the site type into the question list
								SiteTypeQuestions siteTypeQuestions = questionService.newSiteTypeQuestions();
								siteTypeQuestions.setSiteType(siteType);
								
								NodeList qSetList = currentNode.getChildNodes();
								for (int i2 = 0; i2 < qSetList.getLength(); i2++)
								{
									Node qNode = qSetList.item(i2);
									switch (qNode.getNodeType())
									{
										case Node.TEXT_NODE:
											break;
										case Node.ELEMENT_NODE:
											if (qNode.getNodeName().equals("header"))
											{
												siteTypeQuestions.setInstruction(qNode.getTextContent());
											}
											else if (qNode.getNodeName().equals("url"))
											{
												NodeList qList = qNode.getChildNodes();
												for (int i3 = 0; i3 < qList.getLength(); i3++)
												{
													Node qDetailNode = qList.item(i3);
													switch (qDetailNode.getNodeType())
													{
														case Node.TEXT_NODE:
															break;
														case Node.ELEMENT_NODE:
															if (qDetailNode.getNodeName().equals("a"))
															{
																if (qDetailNode.hasAttributes())
																{
																	// attributes
																	NamedNodeMap qDetailMap = qDetailNode.getAttributes();
																	if (qDetailMap.getNamedItem("href") != null)
																	{
																		siteTypeQuestions.setUrl(qDetailMap.getNamedItem("href").getNodeValue());
																	}
																	else if (qDetailMap.getNamedItem("target") != null)
																	{
																		siteTypeQuestions.setUrlTarget(qDetailMap.getNamedItem("target").getNodeValue());
																	}
																}
																siteTypeQuestions.setUrlLabel(qDetailNode.getTextContent());
															}
													}
												}
											}
											else if (qNode.getNodeName().equals("question"))
											{
												SiteSetupQuestion q = questionService.newSiteSetupQuestion();
												if (qNode.hasAttributes())
												{
													// attributes
													NamedNodeMap qMap = qNode.getAttributes();
													if (qMap.getNamedItem("required") != null)
													{
														q.setRequired(Boolean.valueOf(qMap.getNamedItem("required").getNodeValue()));
													}
													else
													{
														q.setRequired(false);
													}
													if (qMap.getNamedItem("multiple_answers") != null)
													{
														q.setIsMultipleAnswers(Boolean.valueOf(qMap.getNamedItem("multiple_answers").getNodeValue()));
													}
													else
													{
														q.setIsMultipleAnswers(false);
													}
													
													NodeList qList = qNode.getChildNodes();
													for (int i3 = 0; i3 < qList.getLength(); i3++)
													{
														Node qDetailNode = qList.item(i3);
														switch (qDetailNode.getNodeType())
														{
															case Node.TEXT_NODE:
																break;
															case Node.ELEMENT_NODE:
																if (qDetailNode.getNodeName().equals("q"))
																{
																	q.setQuestion(qDetailNode.getTextContent());
																}
																else if (qDetailNode.getNodeName().equals("answer"))
																{
																	SiteSetupQuestionAnswer answer = questionService.newSiteSetupQuestionAnswer();
																	if (qDetailNode.hasAttributes())
																	{
																		// attributes
																		NamedNodeMap qDetailMap = qDetailNode.getAttributes();
																		if (qDetailMap.getNamedItem("fillin_blank") != null)
																		{
																			answer.setIsFillInBlank(Boolean.valueOf(qDetailMap.getNamedItem("fillin_blank").getNodeValue()));
																		}
																		else
																		{
																			answer.setIsFillInBlank(false);
																		}
																	}
																	answer.setAnswer(qDetailNode.getTextContent());
																	// save answer
																	questionService.saveSiteSetupQuestionAnswer(answer);
																	q.addAnswer(answer);
																}
																break;
														}
													}
												}
												
												// mark this question as currently used
												q.setCurrent("true");
												
												// save question
												questionService.saveSiteSetupQuestion(q);
												siteTypeQuestions.addQuestion(q);
											}
											
											break;
									}
							}
							// save siteTypeQuestions
							questionService.saveSiteTypeQuestions(siteTypeQuestions);
								
						}
					}
					break;
				}
			}
		}

	    // what to do?
	    
	    
	    return m;
	  }

	  /**
	   * Lookup and save one dynamic configuration parameter
	   * @param Configuration XML
	   * @param parameterMap Parameter name=value pairs
	   * @param name Parameter name
	   */
	  protected void saveParameter(org.w3c.dom.Document document,
	                             Map parameterMap, String name)
	  {
	    String value;

	    if ((value = getText(document, name)) != null)
	    {
	      parameterMap.put(name, value);
	    }
	  }
	  
	  /*
	   * XML helpers
	   */

	  /**
	   * Parse an XML resource
	   * @param filename The filename (or URI) to parse
	   * @return DOM Document (null if parse fails)
	   */
	  protected static Document parseXmlFromStream(InputStream stream)
	  {
	    try
	    {
	      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
	      builderFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
	      builderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
	      builderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
	      DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

	      if (documentBuilder != null)
	      {
	        return documentBuilder.parse(stream);
	      }
	    }
	    catch (Exception exception)
	    {
	      log.warn("XML parse on \"" + stream + "\" failed: " + exception);
	    }
	    return null;
	  }

	  // xml helper
	  /**
	   * Get a DOM Document builder.
	   * @return The DocumentBuilder
	   * @throws DomException
	   */
	  protected static DocumentBuilder getXmlDocumentBuilder()
	  {
	    try
	    {
	      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	      factory.setNamespaceAware(false);

	      return factory.newDocumentBuilder();
	    }
	    catch (Exception exception)
	    {
	      log.warn("Failed to get XML DocumentBuilder: " + exception);
	    }
	    return null;
	  }

	  /**
	   * "Normalize" XML text node content to create a simple string
	   * @param original Original text
	   * @param update Text to add to the original string (a space separates the two)
	   * @return Concatenated contents (trimmed)
	   */
	  protected String normalizeText(String original, String update)
	  {
	    StringBuilder  result;

	    if (original == null)
	    {
	      return (update == null) ? "" : update.trim();
	    }

	    result = new StringBuilder(original.trim());
	    result.append(' ');
	    result.append(update.trim());

	    return result.toString();
	  }
	  
	  /**
	   * Get the text associated with this element
	   * @param root The document containing the text element
	   * @return Text (trimmed of leading/trailing whitespace, null if none)
	   */
	  protected String getText(Document root, String elementName)
	  {
	    return getText(root.getDocumentElement(), elementName);
	  }

	  /**
	   * Get the text associated with this element
	   * @param root The root node of the text element
	   * @return Text (trimmed of leading/trailing whitespace, null if none)
	   */
	  protected String getText(Element root, String elementName)
	  {
	    NodeList  nodeList;
	    Node      parent;
	    String    text;

	    nodeList = root.getElementsByTagName(elementName);
	    if (nodeList.getLength() == 0)
	    {
	      return null;
	    }

	    text = null;
	    parent = (Element) nodeList.item(0);

	    for (Node child = parent.getFirstChild();
	              child != null;
	              child = child.getNextSibling())
	    {
	      switch (child.getNodeType())
	      {
	        case Node.TEXT_NODE:
	          text = normalizeText(text, child.getNodeValue());
	          break;

	        default:
	          break;
	      }
	    }
	    return text == null ? text : text.trim();
	  }


}
