/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.citation.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
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
import org.sakaiproject.citation.cover.ConfigurationService;
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
import org.sakaiproject.cheftool.VmServlet;

/**
 * 
 */
public class CitationListAccessServlet implements HttpAccess
{
	public static final String LIST_TEMPLATE = "/vm/citationList.vm";
	
	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("citations");
	
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
		if(org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS_SEL.equals(subtype) ||
				org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS_ALL.equals(subtype))
		{
			handleExportRequest(req, res, ref,
					org.sakaiproject.citation.api.CitationService.RIS_FORMAT,
					subtype);
			
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
	
	protected void handleExportRequest(HttpServletRequest req, HttpServletResponse res,
			Reference ref, String format, String subtype) 
			throws EntityNotDefinedException, EntityAccessOverloadException 
	{
		if(org.sakaiproject.citation.api.CitationService.RIS_FORMAT.equals(format))
		{
			String collectionId = req.getParameter("collectionId");
			List<String> citationIds = new java.util.ArrayList<String>();
			CitationCollection collection = null;
			try 
			{
				collection = CitationService.getCollection(collectionId);
			} 
			catch (IdUnusedException e) 
			{
				throw new EntityNotDefinedException(ref.getReference());
			}
			
			// decide whether to export selected or entire list
			if( org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS_SEL.equals(subtype) )
			{
				// export selected
				String[] paramCitationIds = req.getParameterValues("citationId");
				
				if( paramCitationIds == null || paramCitationIds.length < 1 )
				{
					// none selected - do not continue
					return;
				}
				citationIds.addAll(Arrays.asList(paramCitationIds));
			}
			else
			{
				// export all
				
				// iterate through ids
				List<Citation> citations = collection.getCitations();
				
				if( citations == null || citations.size() < 1 )
				{
					// no citations to export - do not continue
					return;
				}
				
				for( Citation citation : citations )
				{
					citationIds.add( citation.getId() );
				}
			}
			
			// We need to write to a temporary stream for better speed, plus
			// so we can get a byte count. Internet Explorer has problems
			// if we don't make the setContentLength() call.
			StringBuilder buffer = new StringBuilder(4096);
			// StringBuilder contentType = new StringBuilder();

			try 
			{
				collection.exportRis(buffer, citationIds);
			} 
			catch (IOException e) 
			{
				throw new EntityAccessOverloadException(ref.getReference());
			}

			// Set the mime type for a RIS file
			res.addHeader("Content-Disposition", "attachment; filename=\"citations.RIS\"");
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
    		ContentResource resource = (ContentResource) ref.getEntity(); // ContentHostingService.getResource(ref.getId());
    		ResourceProperties properties = resource.getProperties();
   
    		String title = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
    		String description = properties.getProperty( ResourceProperties.PROP_DESCRIPTION );
    		
     		String citationCollectionId = new String( resource.getContent() );
    		CitationCollection collection = CitationService.getCollection(citationCollectionId);

    		res.setContentType("text/html; charset=UTF-8");
    		PrintWriter out = res.getWriter();
    		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
					+ "<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
					+ "<title>"
					+ rb.getString("list.title") + ": "
					+ title
					+ "</title>\n"
					+ "<link href=\"/library/skin/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
					+ "<link href=\"/library/skin/default/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
					+ "<script type=\"text/javascript\" src=\"/library/js/jquery.js\"></script>\n"
					+ "<script type=\"text/javascript\" src=\"/sakai-citations-tool/js/citationscript.js\"></script>\n"
    				+ "</head>\n<body>" );

    		List<Citation> citations = collection.getCitations();
    		
    		out.println("<div class=\"portletBody\">\n\t<div class=\"indnt1\">");
    		out.println("\t<h3>" + rb.getString("list.title") + ": " + title + "</h3>");
    		if( description != null && !description.trim().equals("") )
    		{
    			out.println("\t<p>" + description + "</p>");
    		}
    		if( citations.size() > 0 )
    		{
    			Object[] args = { ConfigurationService.getSiteConfigOpenUrlLabel() };
    			out.println("\t<p class=\"instruction\">" + rb.getFormattedMessage("cite.subtitle", args) + "</p>");
    		}
    		out.println("\t<table class=\"listHier lines nolines\" summary=\"citations table\" cellpadding=\"0\" cellspacing=\"0\">");
    		out.println("\t<tbody>");
    		out.println("\t<tr><th colspan=\"2\">");
    		out.println("\t\t<div class=\"viewNav\" style=\"padding: 0pt;\"><strong>" + rb.getString("listing.title") + "</strong> (" + collection.size() + ")" );
    		out.println("\t\t</div>");
    		out.println("\t</th></tr>");


    		if( citations.size() > 0 )
    		{
    			out.println("\t<tr class=\"exclude\"><td colspan=\"2\">");
    			out.println("\t\t<div class=\"itemAction\">");
    			out.println("\t\t\t<a href=\"#\" onclick=\"showAllDetails( '" + rb.getString("link.hide.results") + "' ); return false;\">" + rb.getString("link.show.readonly") + "</a> |" );
    			out.println("\t\t\t<a href=\"#\" onclick=\"hideAllDetails( '" + rb.getString("link.show.readonly") + "' ); return false;\">" + rb.getString("link.hide.results") + "</a>" );
    			out.println("\t\t</div>\n\t</td></tr>");
    		}

    		for( Citation citation : citations )
    		{
    			String escapedId = citation.getId().replace( '-', 'x' );
    			
    			// toggle image
    			out.println("\t\t<tr>");
    			out.println("\t\t\t<td class=\"attach\">");
    			out.println("\t\t\t\t<img onclick=\"toggleDetails( '" + escapedId + "', '" + rb.getString("link.show.readonly") + "', '" + rb.getString("link.hide.results") + "' );\"" );
    			out.println("\t\t\t\tid=\"toggle_" + escapedId + "\" class=\"toggleIcon\"" );
    			out.println("\t\t\t\tstyle=\"cursor: pointer;\" src=\"/library/image/sakai/expand.gif?panel=Main\"");
    			out.println("\t\t\t\talt=\"" + rb.getString("link.show.readonly") + "\" align=\"top\"" );
    			out.println("\t\t\t\tborder=\"0\" height=\"13\" width=\"13\" />" );
    			out.println("\t\t\t</td>");
    			
    			// preferred URL?
    			String href = citation.hasPreferredUrl() ? citation.getCustomUrl(citation.getPreferredUrlId()) : citation.getOpenurl();
    			
    			out.println("\t\t<td headers=\"details\">");
    			out.println("\t\t\t<a href=\"" + href + "\" target=\"_blank\">" + Validator.escapeHtml( (String)citation.getCitationProperty( Schema.TITLE ) ) + "</a><br />");
    			out.println("\t\t\t\t" + Validator.escapeHtml( citation.getCreator() ) );
    			out.println("\t\t\t\t" + Validator.escapeHtml( citation.getSource() ) );
    			out.println("\t\t\t<div class=\"itemAction\">");
    			if( citation.hasCustomUrls() )
    			{
    				List<String> customUrlIds = citation.getCustomUrlIds();
    				for( String urlId : customUrlIds )
    				{
        			if (!citation.hasPreferredUrl() || 
        			    (citation.hasPreferredUrl() && (!citation.getPreferredUrlId().equals(urlId))))
        			{
      					String urlLabel = ( citation.getCustomUrlLabel( urlId ) == null ||
      							citation.getCustomUrlLabel( urlId ).trim().equals("") ) ? rb.getString( "nullUrlLabel.view" ) : Validator.escapeHtml( citation.getCustomUrlLabel( urlId ) );
              
    					  out.println("\t\t\t\t<a href=\"" + citation.getCustomUrl( urlId ).toString() + "\" target=\"_blank\">" + urlLabel + "</a>");
    	    			out.println("\t\t\t\t |");
    	    		}
    				}
    			}
    			out.println("\t\t\t\t<a href=\"" + citation.getOpenurl() + "\" target=\"_blank\">" + ConfigurationService.getSiteConfigOpenUrlLabel() + "</a>");
    			/* not using view citation link - using toggle triangle
    			out.println("\t\t\t\t<a id=\"link_" + escapedId + "\" href=\"#\" onclick=\"viewFullCitation('" + escapedId + "'); return false;\">"
    					+ rb.getString( "action.view" ) + "</a>" );
    			*/
    			out.println("\t\t\t</div>");

    			// show detailed info
    			out.println("\t\t<div id=\"details_" + escapedId + "\" class=\"citationDetails\" style=\"display: none;\">");
       			out.println("\t\t\t<table class=\"listHier lines nolines\" style=\"margin-left: 2em;\" cellpadding=\"0\" cellspacing=\"0\">");
	     			
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
    					// don't want to repeat authors
    					if( !Schema.CREATOR.equals(field.getIdentifier()) )
    					{
    						List values = (List) citation.getCitationProperty(field.getIdentifier());
    						Iterator valueIt = values.iterator();
    						boolean first = true;
    						while(valueIt.hasNext())
    						{
    							String value = (String) valueIt.next();
    							if( value != null && !value.trim().equals("") )
    							{
    								if(first)
    								{
    									String label = rb.getString(schema.getIdentifier() + "." + field.getIdentifier(), field.getIdentifier());
    									out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\"><strong>" + label + "</strong></td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>");
    								}
    								else
    								{
    									out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\">&nbsp;</td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>\n");
    								}
    							}	
    							first = false;
    						}
    					}
    				}
    				else
    				{
    					String value = (String) citation.getCitationProperty(field.getIdentifier());
    					if(value != null && ! value.trim().equals(""))
    					{
 							String label = rb.getString(schema.getIdentifier() + "." + field.getIdentifier(), field.getIdentifier());
 							/* leaving out "Find It!" link for now as we're not using it anywhere else anymore
 							if(Schema.TITLE.equals(field.getIdentifier()))
 							{
 								value += " [<a href=\"" + citation.getOpenurl() + "\" target=\"_blank\">" + openUrlLabel + "</a>]";
 							}
 							*/
 							
 							// don't want to repeat titles
 							if( !Schema.TITLE.equals(field.getIdentifier()) )
 							{
 								out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\"><strong>" + label + "</strong></td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>");
 							}

    					}
    				}
    			}
      			out.println("\t\t\t</table>");
       		    out.println("\t\t</div>");
       		    out.println("\t\t</td>");
       		    out.println("\t\t</tr>");
    		}
    		
    		if( citations.size() > 0 )
    		{
    			out.println("\t<tr class=\"exclude\"><td colspan=\"2\">");
    			out.println("\t\t<div class=\"itemAction\">");
    			out.println("\t\t\t<a href=\"#\" onclick=\"showAllDetails( '" + rb.getString("link.hide.results") + "' ); return false;\">" + rb.getString("link.show.readonly") + "</a> |" );
    			out.println("\t\t\t<a href=\"#\" onclick=\"hideAllDetails( '" + rb.getString("link.show.readonly") + "' ); return false;\">" + rb.getString("link.hide.results") + "</a>" );
    			out.println("\t\t</div>\n\t</td></tr>");
        		
        		out.println("\t<tr><th colspan=\"2\">");
        		out.println("\t\t<div class=\"viewNav\" style=\"padding: 0pt;\"><strong>" + rb.getString("listing.title") + "</strong> (" + collection.size() + ")" );
        		out.println("\t\t</div>");
        		out.println("\t</th></tr>");
        		out.println("\t</tbody>");
        		out.println("\t</table>");
        		out.println("</div></div>");
        		out.println("</body></html>");
    		}
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

