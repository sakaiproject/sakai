/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.citation.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationHelper;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.citation.cover.CitationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * 
 */
public class CitationListAccessServlet implements HttpAccess
{
	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("citation_mgr");
	
	/** Our logger. */
	private static Log m_log = LogFactory.getLog(CitationListAccessServlet.class);

	/**
	 * Handle an HTTP request for access. The request and response objects are provider.<br />
	 * The access is for the referenced entity.<br />
	 * Use the response object to send the headers, length, content type, and bytes of the response in whatever manner needed.<br />
	 * Make the response ONLY if it is permitted and exists and otherwise valid. Use the exceptions for any error handling.
	 * 
	 * @param req
	 *        The request object.
	 * @param res
	 *        The response object.
	 * @param ref
	 *        The entity reference
	 * @param copyrightAcceptedRefs
	 *        The collection (entity reference String) of entities that the end user in this session have already accepted the copyright for.
	 * @throws EntityPermissionException
	 *         Throw this if the current user does not have permission for the access.
	 * @throws EntityNotDefinedException
	 *         Throw this if the ref is not supported or the entity is not available for access in any way.
	 * @throws EntityAccessOverloadException
	 *         Throw this if you are rejecting an otherwise valid request because of some sort of server resource shortage or limit.
	 * @throws EntityCopyrightException
	 *         Throw this if you are rejecting an otherwise valid request because the user needs to agree to the copyright and has not yet done so.
	 */
	public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
			throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
	{
		String subtype = ref.getSubType();
		if(org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS.equals(subtype))
		{
			handleExportRequest(req, res, ref, org.sakaiproject.citation.api.CitationService.RIS_FORMAT);
			
		}
		else if(org.sakaiproject.citation.api.CitationService.REF_TYPE_VIEW_LIST.equals(subtype))
		{
			handleViewRequest(req, res, ref);
		}
		else
		{
			throw new EntityNotDefinedException(ref.getReference());
		}

	}	// handleAccess
	
	protected void handleExportRequest(HttpServletRequest req, HttpServletResponse res, Reference ref, String format) 
			throws EntityNotDefinedException, EntityAccessOverloadException 
	{
		if(org.sakaiproject.citation.api.CitationService.RIS_FORMAT.equals(format))
		{
			String collectionId = req.getParameter("collectionId");
			String[] citationIds = req.getParameterValues("citationId");
			List citations = new Vector();
			citations.addAll(Arrays.asList(citationIds));
			
			CitationCollection collection = null;
			try 
			{
				collection = CitationService.getCollection(collectionId);
			} 
			catch (IdUnusedException e) 
			{
				throw new EntityNotDefinedException(ref.getReference());
			}
			
			// We need to write to a temporary stream for better speed, plus
			// so we can get a byte count. Internet Explorer has problems
			// if we don't make the setContentLength() call.
			StringBuffer buffer = new StringBuffer(4096);
			// StringBuffer contentType = new StringBuffer();

			try 
			{
				collection.exportRis(buffer, citations);
			} 
			catch (IOException e) 
			{
				throw new EntityAccessOverloadException(ref.getReference());
			}

			// Set the mime type for a PDF
			res.addHeader("Content-Disposition", "inline; filename=\"citations.RIS\"");
			res.setContentType("application/x-Research-Info-Systems");
			res.setContentLength(buffer.length());

			if (buffer.length() > 0)
			{
				// Increase the buffer size for more speed.
				res.setBufferSize(buffer.length());
			}

			OutputStream out = null;
			try
			{
				out = res.getOutputStream();
				if (buffer.length() > 0)
				{
					out.write(buffer.toString().getBytes());
				}
				out.flush();
				out.close();
			}
			catch (Throwable ignore)
			{
			}
			finally
			{
				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (Throwable ignore)
					{
					}
				}
			}

		}
		
	}	// handleExportRequest

	protected void handleViewRequest(HttpServletRequest req, HttpServletResponse res, Reference ref) 
			throws EntityPermissionException, EntityAccessOverloadException, EntityNotDefinedException
	{
		if(! ContentHostingService.allowGetResource(ref.getId()))
		{
			String user = "";
			if(req.getUserPrincipal() != null)
			{
				user = req.getUserPrincipal().getName();
			}
			throw new EntityPermissionException(user, ContentHostingService.EVENT_RESOURCE_READ, ref.getReference());
		}

        try
        {
        	String openUrlLabel = CitationService.getOpenUrlLabel();
       		Object[] openUrlLabelArray = {openUrlLabel};
        	
    		ContentResource resource = (ContentResource) ref.getEntity(); // ContentHostingService.getResource(ref.getId());
    		ResourceProperties properties = resource.getProperties();
   
    		String title = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
    		
     		byte[] content = resource.getContent();
    		
     		String citationCollectionId = properties.getProperty(CitationHelper.PROP_CITATION_COLLECTION);
    		CitationCollection collection = CitationService.getCollection(citationCollectionId);

    		res.setContentType("text/html; charset=UTF-8");
    		PrintWriter out = res.getWriter();
    		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
					+ "<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
					+ "<style type=\"text/css\">body{margin:0px;padding:1em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:80%;}</style>\n"
					+ "<title>"
					+ rb.getString("list.title") + " "
					+ title
					+ "</title>" + "</head>\n<body>");
    		
    		out.println("<div id=\"div_index\">\n");
    		out.println("<h3>" + rb.getString("list.title") + " " + title + "</h3>");
    		if(content != null && content.length > 0)
    		{
    			out.println("<div>\n" + (new String(content)) + "\n</div>\n");
    		}
 			out.println("<div>\n<h4>" + rb.getFormattedMessage("cite.subtitle", openUrlLabelArray) + "</h4>\n");
    		List citations = collection.getCitations();
    		Iterator citationIt = citations.iterator();
    		while(citationIt.hasNext())
    		{
    			Citation citation = (Citation) citationIt.next();
    			out.println("<p>\n<a href=\"#cite_details\" onclick=\"showCitation('" + citation.getId() + "');\" >" + citation.getDisplayName() + "</a>");
    			out.println(citation.getCreator());
    			out.println(citation.getSource());
    			out.println("<a href=\"" + citation.getOpenurl() + "\" target=\"_blank\">" + openUrlLabel + "</a>\n</p>\n");
    		}
    		out.println("</div>\n");
       		out.println("</div>\n");
       		out.println("<br />\n<br />\n<br />\n");
   			out.println("<a name=\"cite_details\" id=\"cite_details\"></a>\n");
   		
    		citationIt = citations.iterator();
       		while(citationIt.hasNext())
    		{
    			Citation citation = (Citation) citationIt.next();
    			out.println("<div id=\"div_" + citation.getId() + "\" style=\"display:none\">\n");
     			out.println("<h3>" + rb.getString("cite.title") + citation.getDisplayName() + "</h3>\n");
       			out.println("<table cellpadding=\"10\">\n");
       		     			
    			Schema schema = citation.getSchema();
    			if(schema == null)
    			{
    				m_log.warn("CLAS.handleViewRequest() Schema is null: " + citation);
    				continue;
    			}
    			List fields = schema.getFields();
    			Iterator fieldIt = fields.iterator();
    			
    			while(fieldIt.hasNext())
    			{
    				Field field = (Field) fieldIt.next();
    				if(field.isMultivalued())
    				{
    					List values = (List) citation.getCitationProperty(field.getIdentifier());
    					Iterator valueIt = values.iterator();
    					boolean first = true;
    					while(valueIt.hasNext())
    					{
    						String value = (String) valueIt.next();
    						if(first)
    						{
     							String label = rb.getString(schema.getIdentifier() + "." + field.getIdentifier(), field.getIdentifier());
         		       			out.println("<tr valign=\"top\">\n<td>\n" + label + "\n</td>\n<td>\n" + value + "\n</td>\n</tr>\n");
    						}
    						else
    						{
        		       			out.println("<tr valign=\"top\">\n<td>\n&nbsp;\n</td>\n<td>\n" + value + "\n</td>\n</tr>\n");
    						}
    							
    						first = false;
    					}
    				}
    				else
    				{
    					String value = (String) citation.getCitationProperty(field.getIdentifier());
    					if(value != null && ! value.trim().equals(""))
    					{
 							String label = rb.getString(schema.getIdentifier() + "." + field.getIdentifier(), field.getIdentifier());
 							if(Schema.TITLE.equals(field.getIdentifier()))
 							{
 								value += " [<a href=\"" + citation.getOpenurl() + "\" target=\"_blank\">" + openUrlLabel + "</a>]";
 							}
   		       				out.println("<tr valign=\"top\">\n<td>\n" + label + "</td>\n<td>\n" + value + "</td>\n</tr>\n");

    					}
    				}
    			}
	       		
      			out.println("</table>\n");
       		    out.println("</div>\n");
     			
    		}
    		
			out.println("<script type=\"text/javascript\">\nfunction showCitation(citationId)\n{\n");
			out.println("var now_showing = document.getElementById(\"div_showing\").value;");
			out.println("if(now_showing != \"\")\n{\n");
			out.println("var hide_me = document.getElementById(now_showing);");
			out.println("if(hide_me)\n{\nhide_me.style.display=\"none\";\n}\n}\n");
			out.println("var show_me = document.getElementById(\"div_\" + citationId);");
			out.println("if(show_me)\n{\nshow_me.style.display=\"block\";\n}\n");
			out.println("document.getElementById(\"div_showing\").value = \"div_\" + citationId;\n");
			out.println("}\n</script>\n");
			out.println("<input type=\"hidden\" id=\"div_showing\" value=\"\" />\n");
			out.println("</body>\n</html>\n");
			
        }
        catch (IOException e)
        {
        	throw new EntityAccessOverloadException(ref.getReference());
        }
        catch (ServerOverloadException e)
        {
        	throw new EntityAccessOverloadException(ref.getReference());
        }
        catch (IdUnusedException e)
        {
        	throw new EntityNotDefinedException(ref.getReference());
        }

	}
}

