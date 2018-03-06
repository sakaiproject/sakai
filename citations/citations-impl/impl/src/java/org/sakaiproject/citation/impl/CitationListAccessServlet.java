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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.citation.api.Citation;
import org.sakaiproject.citation.api.CitationCollection;
import org.sakaiproject.citation.api.CitationCollectionOrder;
import org.sakaiproject.citation.api.Schema;
import org.sakaiproject.citation.api.Schema.Field;
import org.sakaiproject.citation.cover.CitationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * 
 */
@Slf4j
public class CitationListAccessServlet implements HttpAccess
{
	public static final String LIST_TEMPLATE = "/vm/citationList.vm";
	
	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("citations");

	private static final Collection<String> specialKeys = new HashSet<String>();
	static {
		specialKeys.add("edition");
		specialKeys.add("note");
		specialKeys.add("notes");
	};

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
		
		// SAK-22299. Build a pseudo content hosting event so that sitestats picks it up in its special resource area.
		Event e = EventTrackingService.newEvent(ContentHostingService.EVENT_RESOURCE_READ, "/content" + ref.getId(), false);
		EventTrackingService.post(e);

	}	// handleAccess
	
	protected void handleExportRequest(HttpServletRequest req, HttpServletResponse res,
			Reference ref, String format, String subtype) 
			throws EntityNotDefinedException, EntityAccessOverloadException, EntityPermissionException 
	{
		SessionManager sessionManager = ComponentManager.get(SessionManager.class);
		org.sakaiproject.content.api.ContentHostingService contentHostingService = ComponentManager.get(org.sakaiproject.content.api.ContentHostingService.class);
		if(! ContentHostingService.allowGetResource(req.getParameter("resourceId")))
		{
			String url = (req.getRequestURL()).toString();
			String user = "";
			if(req.getUserPrincipal() != null)
			{
				user = req.getUserPrincipal().getName();
			}
			throw new EntityPermissionException(user, ContentHostingService.EVENT_RESOURCE_READ, ref.getReference());
		}			
		
		String fileName = req.getParameter("resourceDisplayName");
		if(fileName == null || fileName.trim().equals("")) {
			fileName = rb.getString("export.default.filename");
		}
		
		if(org.sakaiproject.citation.api.CitationService.RIS_FORMAT.equals(format))
		{
			String citationCollectionId = null;
			ContentResource resource = null;
			try {
				resource = contentHostingService.getResource(req.getParameter("resourceId"));
				citationCollectionId = new String(resource.getContent());
			}
			catch (PermissionException e) {
				throw new EntityPermissionException(sessionManager.getCurrentSessionUserId(), "handleExportRequest", ref.getReference());
			}
			catch (IdUnusedException e) {
				throw new EntityNotDefinedException(ref.getReference());
			}
			catch (TypeException e) {
				throw new IllegalStateException("Resource Mismatch: " + ref.getReference(), e);
			}
			catch (ServerOverloadException e){
				throw new EntityAccessOverloadException(ref.getReference());
			}

			List<String> citationIds = new java.util.ArrayList<String>();
			CitationCollection collection = null;
			try 
			{
				collection = CitationService.getCollection(citationCollectionId);
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
					try {
						res.sendError(HttpServletResponse.SC_BAD_REQUEST, rb.getString("export.none_selected"));
					} catch (IOException e) {
						log.warn("export-selected request received with not citations selected. citationCollectionId: {}", citationCollectionId);
					}

					return;
				}
				citationIds.addAll(Arrays.asList(paramCitationIds));
				
				fileName = rb.getFormattedMessage("export.filename.selected.ris", fileName);
			}
			else
			{
				// export all
				
				// iterate through ids
				List<Citation> citations = collection.getCitations();
				
				if( citations == null || citations.size() < 1 )
				{
					// no citations to export - do not continue 
					try {
						res.sendError(HttpServletResponse.SC_NO_CONTENT, rb.getString("export.empty_collection"));
					} catch (IOException e) {
						log.warn("export-all request received for empty citation collection. citationCollectionId: {}", citationCollectionId);
					}
					
					return;
				}
				
				for( Citation citation : citations )
				{
					citationIds.add( citation.getId() );
				}
				fileName = rb.getFormattedMessage("export.filename.all.ris", fileName);
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
			res.addHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
			//res.addHeader("Content-Disposition", "attachment; filename=\"citations.RIS\"");
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
        try
        {
        	// We get the resource as this checks permissions and throws exceptions if the user doesn't have access
    		ContentResource resource = ContentHostingService.getResource(ref.getId());
    		
    		if (!CitationService.CITATION_LIST_ID.equals(resource.getResourceType())) {
    			// Don't do anything unless it's a citation list
    			throw new EntityNotDefinedException("Couldn't find citation list");
    		}
    		
    		if (resource.getContentLength() > 1024) {
    			// Only convert small byte arrays to string.
    			throw new EntityAccessOverloadException(ref.getId());
    		}
    		
    		ResourceProperties properties = resource.getProperties();
   
    		String title = properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
    		String introduction = properties.getProperty( org.sakaiproject.citation.api.CitationService.PROP_INTRODUCTION );
    		
     		String citationCollectionId = new String( resource.getContent() );
	        org.sakaiproject.citation.api.CitationService citationService = (org.sakaiproject.citation.api.CitationService) ComponentManager.get(org.sakaiproject.citation.api.CitationService.class);
	        CitationCollection collection = citationService.getUnnestedCitationCollection(citationCollectionId);
	        CitationCollection fullCollection = CitationService.getCollection(citationCollectionId);

    		res.setContentType("text/html; charset=UTF-8");
    		PrintWriter out = res.getWriter();
    		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
					+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
					+ "<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
					+ "<title>"
					+ rb.getString("list.title") + ": "
					+ Validator.escapeHtml(title)
					+ "</title>\n"
					+ "<link href=\"/library/skin/tool_base.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
					+ "<link href=\"/library/skin/" + ServerConfigurationService.getString("skin.default") + "/tool.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
					+ "<link href=\"/sakai-citations-tool/css/citations.css\" type=\"text/css\" rel=\"stylesheet\" media=\"all\" />\n"
					+ "<script type=\"text/javascript\" src=\"/library/webjars/jquery/1.12.4/jquery.min.js\"></script>\n"
					+ "<script type=\"text/javascript\" src=\"/sakai-citations-tool/js/citationscript.js\"></script>\n"
					+ "<script type=\"text/javascript\" src=\"/sakai-citations-tool/js/view_nested_citations.js\"></script>\n"
					+ "<script type=\"text/javascript\" src=\"/sakai-citations-tool/js/jquery.googlebooks.thumbnails.js\"></script>\n"
    				+ "</head>\n<body>" );

    		List<Citation> citations = collection.getCitations();
    		String contentCollectionId = resource.getContainingCollection().getId();


    		String exportParams =  "?resourceDisplayName=" + resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME) + "&resourceId=" + resource.getId();
    		String exportUrlAll = fullCollection.getUrl(org.sakaiproject.citation.api.CitationService.REF_TYPE_EXPORT_RIS_ALL) + exportParams;
    		String displayDate = null;
    		try {
    			displayDate = new SimpleDateFormat("dd/MM/yyyy", rb.getLocale()).format(properties.getDateProperty(ResourceProperties.PROP_MODIFIED_DATE));
    		} catch (EntityPropertyNotDefinedException e) {
    			log.warn("CitationListAccessServlet.handleViewRequest() : Property name requested is not defined - for contentCollectionId {}", contentCollectionId);
    		} catch (EntityPropertyTypeException e) {
    			log.warn("CitationListAccessServlet.handleViewRequest() : Named property found does not match the type of access requested - for contentCollectionId {}", contentCollectionId);
    		}

		out.println("<div class=\"portletBody\">\n\t<div class=\"listWidth citationList\">");
		boolean isPrintView = req.getParameter("printView")!=null;
		out.println("\t<div style=\"width: 100%; height: 90px; line-height: 90px; background-color:" +
					ServerConfigurationService.getString("official.institution.background.colour") +"; \">" +
					"<div class=\"banner\"><h1 style=\" margin-left:15px; color:" + ServerConfigurationService.getString("official.institution.text.colour") + ";\">" +
					Validator.escapeHtml(title) + "</h1></div> " +
					"<div class=\"bannerLinks\">"  +
					(!isPrintView ? "<a class=\"export\" href=" + exportUrlAll + ">Export</a>"  + "<a class=\"print\" target=\"_blank\" href=" + req.getRequestURL() + "?printView" + ">Print</a>"  : "") +
					"<div class=\"lastUpdated\">Last updated: " +  displayDate + "</div>" + "</div>" + "</div>");
    		out.println("<div style=\"clear:both;\"></div>");
    		if( introduction != null && !introduction.trim().equals("") )
    		{
    			out.println("\t<div class='descriptionView'>" + introduction + "</div>");
    		}

    		// nested sections
    		displayNestedSections(title, citationCollectionId, citationService, collection, fullCollection, out, contentCollectionId);

			// unnested citations
    		displayCitations(out, citations, collection, false, citationCollectionId, title, contentCollectionId);

    		// logos
    		String[] logos = ServerConfigurationService.getStrings("citations.logo");
    		String logoHTML = "";
    		if (logos != null){
    			logoHTML = "<div class='logos'>";
    			for (String logo : logos) {
    				logoHTML = logoHTML + "<img src='" + logo + "' width='100' height='100'>";
    			}
    			logoHTML = logoHTML + "</div>";
    			out.println(logoHTML);
    		}

    		if( citations.size() > 0 )
    		{
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
        } catch (PermissionException e) {
        	throw new EntityPermissionException(e.getUser(), ContentHostingService.EVENT_RESOURCE_READ, ref.getReference());
        } catch (TypeException e) {
        	// This doesn't seem right but it's probably the best fit.
        	throw new EntityNotDefinedException(ref.getReference());
        }
	}

	private void displayCitations(PrintWriter out, List<Citation> citations, CitationCollection collection, boolean isNested, String citationCollectionId, String title, String contentCollectionId) {

		out.println("\t<table class=\"listHier lines nolines\" summary=\"citations table\" cellpadding=\"0\" cellspacing=\"0\">");
		out.println("\t<tbody>");
		if( citations.size() > 0 && !isNested ) {
			out.println("\t<tr><th colspan=\"2\">");
			out.println("\t\t<div class=\"viewNav\" style=\"padding: 0pt;\"><strong>" +
					rb.getString("listing.title") + "</strong> (" + collection.size() + ")");
			out.println("\t\t</div>");
			out.println("\t</th></tr>");
		}

		if( citations.size() > 0 && !isNested )
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
			String href = null;
			try {
				href = citation.hasPreferredUrl() ? citation.getCustomUrl(citation.getPreferredUrlId()) : citation.getOpenurl();
			} catch (IdUnusedException e)
			{
//				throw new EntityNotDefinedException(citation.getReference());
			}

			out.println("\t\t<td headers=\"details\">");
			out.println("\t\t\t<div class=\"detailsDiv\"><div style=\"padding:5px;\"><div class=\"imgDiv\" style=\"padding-right:5px;\"><a href=\"" + Validator.escapeHtml(href)
					+ "\"><img src=\"/sakai-citations-tool/image/sakai/book-placeholder.png\" data-isbn=\"" + citation.getCitationProperty("isnIdentifier")
					+ "\" class=\"googleBookCover\"></a></div><div style=\"float:left;\"><div><a href=\"" + Validator.escapeHtml(href) + "\" target=\"_blank\">"
					+ Validator.escapeHtml( (String)citation.getCitationProperty( Schema.TITLE, true ) ) + "</a></div>");
			out.println("\t\t\t\t<div class=\"creatorDiv\">" + Validator.escapeHtml( citation.getCreator() )  + "</div>");
			out.println("\t\t\t\t<div class=\"sourceDiv\">" + Validator.escapeHtml( citation.getSource() )  + "</div>");

			out.println("\t\t\t<div><table class=\"listHier lines nolines\" cellpadding=\"0\" cellspacing=\"0\">");

			Schema schema = citation.getSchema();
			if(schema == null)	{
				log.warn("CLAS.handleViewRequest() Schema is null: {}", citation);
				continue;
			}
			List fields = schema.getFields();
			Iterator fieldIt = fields.iterator();

			while(fieldIt.hasNext())
			{
				Field field = (Field) fieldIt.next();
				if(specialKeys.contains(field.getIdentifier())) {

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
										out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\"><strong>" + label + ":</strong></td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>");
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
						String value = (String) citation.getCitationProperty(field.getIdentifier(), true);
						if(value != null && ! value.trim().equals(""))
						{
							String label = rb.getString(schema.getIdentifier() + "." + field.getIdentifier(), field.getIdentifier());
							// don't want to repeat titles
							if( !Schema.TITLE.equals(field.getIdentifier()) )
							{
								out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\"><strong>" + label + "</strong></td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>");
							}

						}
					}
				}
			}
			out.println("\t\t\t</table></div></div>");

			// rhs links
			out.println("\t\t\t<div>");
			if( citation.hasCustomUrls() || citation.getCitationProperty("otherIds") instanceof Vector){
				out.println("\t\t\t<div class=\"itemAction links\" style=\"width:20%\">");
			}
			if( citation.hasCustomUrls() )
			{
				List<String> customUrlIds = citation.getCustomUrlIds();
				for( String urlId : customUrlIds )
				{
					if (!citation.hasPreferredUrl() ||
							(citation.hasPreferredUrl() && (!citation.getPreferredUrlId().equals(urlId))))
					{
						String urlLabel = null;
						try {
							urlLabel = ( citation.getCustomUrlLabel( urlId ) == null ||
									citation.getCustomUrlLabel( urlId ).trim().equals("") ) ? rb.getString( "nullUrlLabel.view" ) : Validator.escapeHtml(citation.getCustomUrlLabel(urlId));
						} catch (IdUnusedException e) {
							log.error(e.getMessage(), e);
						}

						try {
							out.println("\t\t\t\t<a href=\"" + Validator.escapeHtml(citation.getCustomUrl( urlId )) + "\" target=\"_blank\">" + urlLabel + "</a>");
						} catch (IdUnusedException e) {
							log.error(e.getMessage(), e);
						}
						out.println("\t\t\t\t |");
					}
				}
			} else {
				// We only want to show the open url if no custom urls have been specified.
				if (citation.getCitationProperty("otherIds") instanceof Vector) {
					out.println("\t\t\t\t<a href=\"" + ((Vector) citation.getCitationProperty("otherIds")).get(0) + "\" target=\"_blank\">"
							+ "Find it" + " on SOLO" + "</a>");
				}
			}
			if( citation.hasCustomUrls() || citation.getCitationProperty("otherIds") instanceof Vector){
				out.println("\t\t\t</div>");
			}
			// TODO This doesn't need any Inline HTTP Transport.
			out.println("\t\t\t\t<span class=\"Z3988\" title=\""+ citation.getOpenurlParameters().substring(1).replace("&", "&amp;")+ "\"></span>");
			out.println("\t\t\t</div></div>");


			out.println("\t\t\t</div></div>");

			out.println("\t\t\t<div><table class=\"listHier lines nolines\" cellpadding=\"0\" cellspacing=\"0\">");
			out.println("\t\t\t</table></div>");

			// show detailed info
			out.println("\t\t<div id=\"details_" + escapedId + "\" class=\"citationDetails\" style=\"display: none;\">");
			out.println("\t\t\t<table class=\"listHier lines nolines\" cellpadding=\"0\" cellspacing=\"0\">");

			out.println("\t\t<div class=\"availabilityHeader\"></div>");

			out.println("\t\t</td>");
			fields = schema.getFields();
			fieldIt = fields.iterator();

			while(fieldIt.hasNext())
			{
				Field field = (Field) fieldIt.next();

				if(!specialKeys.contains(field.getIdentifier())) {
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
										out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\"><strong>" + label + ":</strong></td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>");
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

							// don't want to repeat titles
							if( !Schema.TITLE.equals(field.getIdentifier()) )
							{
								out.println("\t\t\t\t<tr>\n\t\t\t\t\t<td class=\"attach\"><strong>" + label + ":</strong></td>\n\t\t\t\t\t<td>" + Validator.escapeHtml(value) + "</td>\n\t\t\t\t</tr>");
							}
						}
					}
				}
			}
			out.println("\t\t\t</table>");
			out.println("\t\t</div>");


			out.println("\t\t</td>");
			out.println("\t\t</tr>");
		}

		if( citations.size() > 0 ) {
			if( !isNested ) {
				out.println("\t<tr class=\"exclude\"><td colspan=\"2\">");
				out.println("\t\t<div class=\"itemAction\">");
				out.println("\t\t\t<a href=\"#\" onclick=\"showAllDetails( '" + rb.getString("link.hide.results") + "' ); return false;\">" + rb.getString("link.show.readonly") + "</a> |");
				out.println("\t\t\t<a href=\"#\" onclick=\"hideAllDetails( '" + rb.getString("link.show.readonly") + "' ); return false;\">" + rb.getString("link.hide.results") + "</a>");
				out.println("\t\t</div>\n\t</td></tr>");

				out.println("\t<tr><th colspan=\"2\">");
				out.println("\t\t<div class=\"viewNav\" style=\"padding: 0pt;\"><strong>" + rb.getString("listing.title") + "</strong> (" + collection.size() + ")");
				out.println("\t\t</div>");
				out.println("\t</th></tr>");
			}
			out.println("\t</tbody>");
			out.println("\t</table>");
		}
	}

	private void displayNestedSections(String title, String citationCollectionId, org.sakaiproject.citation.api.CitationService citationService, CitationCollection collection, CitationCollection fullCollection, PrintWriter out, String contentCollectionId) throws IdUnusedException {

		CitationCollectionOrder nestedCollection = citationService.getNestedCollection(citationCollectionId);
		int nestedSectionsSize = nestedCollection.getChildren().size();

		out.println("<ol class='serialization viewCitations h1NestedLevel' style='padding:0;'>");

		// h1 sections
		if (nestedSectionsSize > 0) {
			for (CitationCollectionOrder nestedSection : nestedCollection.getChildren()) {
				String editorDivId = "sectionInlineEditor" + nestedSection.getLocation();
				String linkId = "link" + nestedSection.getLocation();
				String linkClick = "linkClick" + nestedSection.getLocation();
				String toggleImgDiv = "toggleImgDiv" + nestedSection.getLocation();
				String toggleImg = "toggleImg" + nestedSection.getLocation();
				String addSubsectionId = "addSubsection" + nestedSection.getLocation();
				int citationNo = nestedSection.getCountCitations();
				out.println("<li id = '" + linkId + "' class='h1Editor accordionH1 " + (nestedSection.getChildren().size() > 0 ? " hasSections" : "") + "' data-sectiontype='" + nestedSection.getSectiontype() + "'>" +
						"<div id='" + linkClick +"' style='width:100%; float:left; '><div id='" + toggleImgDiv + "'>" +
						(nestedSection.getChildren().size() > 0 ? "<img border='0' width='16' height='16' align='top' alt='Citation View' " +
						"src='/library/image/sakai/white-arrow-right.gif' class='toggleIcon accordionArrow' id='" + toggleImg + "'>" : "") + "</div>" +
						"<div id = '" + editorDivId + "' class='editor accordionDiv'>" +
						(nestedSection.getValue()!=null ? nestedSection.getValue() : "") + (citationNo!=0 ? " (" + citationNo + " citations)" : "") + "</div></div>");

				// h2 sections
				if (nestedSection.getChildren().size() > 0) {
					out.println("<ol id = '" + addSubsectionId  + "' class='h2NestedLevel' style='clear:both;'>");
					for (CitationCollectionOrder h2Section : nestedSection.getChildren()) {
						editorDivId = "sectionInlineEditor" + h2Section.getLocation();
						linkId = "link" + h2Section.getLocation();
						linkClick = "linkClick" + h2Section.getLocation();
						toggleImgDiv = "toggleImgDiv" + h2Section.getLocation();
						toggleImg = "toggleImg" + h2Section.getLocation();
						addSubsectionId = "addSubsection" + h2Section.getLocation();


						if (h2Section.getSectiontype().toString().equals("HEADING2")){

							out.println("<li id = '" + linkId + "' class='h2Section " +
									(h2Section.getChildren().size() > 0 ? " hasSections" : "") + "' data-location='"
									+ h2Section.getLocation() + "' data-sectiontype='" +
									h2Section.getSectiontype() + "' style='background: #cef none repeat scroll 0 0;'>" +
									"<div id='" + linkClick +"' style='width:100%;'><div id='" + toggleImgDiv + "'>" +
									(h2Section.getChildren().size() > 0 ? "<img border='0' width='16' height='16' align='top' alt='Citation View' " +
											"src='/library/image/sakai/collapse.gif' class='toggleIcon accordionArrow' id='" + toggleImg + "'>" : "") + "</div>" +
									"<div id = '" + editorDivId + "' class='editor h2Editor' style='min-height:30px; padding:5px;'>" +
									(h2Section.getValue()!=null ? h2Section.getValue() : "") + "</div></div>");

							// h3 sections
							if (h2Section.getChildren().size() > 0) {
								out.println("<ol id='" + addSubsectionId + "' class='h3NestedLevel' style='padding-top:0px;'>");
								for (CitationCollectionOrder h3Section : h2Section.getChildren()) {
									editorDivId = "sectionInlineEditor" + h3Section.getLocation();
									linkId = "link" + h3Section.getLocation();
									linkClick = "linkClick" + h3Section.getLocation();
									toggleImgDiv = "toggleImgDiv" + h3Section.getLocation();
									toggleImg = "toggleImg" + h3Section.getLocation();
									addSubsectionId = "addSubsection" + h3Section.getLocation();

									if (h3Section.getSectiontype().toString().equals("HEADING3")){

										out.println("<li id = '" + linkId + "' class='h3Section " +
												(h3Section.getChildren().size() > 0 ? " hasSections" : "") + " ' data-location='" + h3Section.getLocation() + "' data-sectiontype='" +
												h3Section.getSectiontype() + "'>" +
												"<div id='" + linkClick +"' style='width:100%;'><div id='" + toggleImgDiv + "'>" +
												(h3Section.getChildren().size() > 0 ? "<img border='0' width='16' height='16' align='top' alt='Citation View' " +
														"src='/library/image/sakai/collapse.gif' class='toggleIcon accordionArrow' id='" + toggleImg + "'>" : "") + "</div>" +
												"<div style='' id = '" + editorDivId + "' class='editor h3Editor' " +
												"style='padding-left:20px; '>" +
												"<div style=''>" + (h3Section.getValue()!=null ? h3Section.getValue() : "") + "</div></div></div>");

										//  nested citations
										if (h3Section.getChildren().size() > 0) {
											out.println("<ol id='" + addSubsectionId + "' class='h4NestedLevel' style='padding-top:0;'>");
											List h3Citations = new ArrayList();
											for (CitationCollectionOrder nestedCitation : h3Section.getChildren()) {
												if (nestedCitation.getSectiontype().toString().equals("CITATION")){
													Citation c = fullCollection.getCitation(nestedCitation.getCitationid());
													if (c != null) {
														h3Citations.add(c);
													}
												}
												// h3 description
												else if (nestedCitation.getSectiontype().toString().equals("DESCRIPTION")) {
													out.println("<li id = '" + linkId + "' class='h3Section' data-location='" + nestedCitation.getLocation() + "' data-sectiontype='" +
															nestedCitation.getSectiontype() + "' style='background: #cef none repeat scroll 0 0;'>" +
															"<div id = '" + editorDivId + "' class='editor description' style='min-height:30px; padding:5px;'>" +
															nestedCitation.getValue() + "</div></li>");
												}
											}
											if (h3Citations!=null && !h3Citations.isEmpty()){
												displayCitations(out, h3Citations, collection, true, citationCollectionId, title, contentCollectionId);
											}
											out.println("</ol>");
										}
									}
									else if (h3Section.getSectiontype().toString().equals("CITATION")) {
										out.println("<ol id='" + addSubsectionId + "' class='h4NestedLevel' style='padding-top:0;'>");
										List h3Citations = new ArrayList();
										Citation c = fullCollection.getCitation(h3Section.getCitationid());
										if (c != null) {
											h3Citations.add(c);
										}
										displayCitations(out, h3Citations, collection, true, citationCollectionId, title, contentCollectionId);
										out.println("</ol>");
									}
									// h2 description
									else if (h3Section.getSectiontype().toString().equals("DESCRIPTION")) {
										out.println("<li id = '" + linkId + "' class='h3Section' data-location='" + h3Section.getLocation() + "' data-sectiontype='" +
												h3Section.getSectiontype() + "' style='background: #cef none repeat scroll 0 0;'>" +
												"<div id = '" + editorDivId + "' class='editor description' style='min-height:30px; padding:5px;'>" +
												h3Section.getValue() + "</div></li>");
									}
								} // h3 section iteration
								out.println("</ol>"); // end ol for h3 section
							} else {
								out.println("<ol class='h3NestedLevel'></ol>");
							}  // end of h3 sections
						}
						else if (h2Section.getSectiontype().toString().equals("CITATION")) {
							out.println("<ol id='" + addSubsectionId + "' class='h4NestedLevel' style='padding-top:0;'>");
							List h3Citations = new ArrayList();
							Citation c = fullCollection.getCitation(h2Section.getCitationid());
							if (c != null) {
								h3Citations.add(c);
							}
							displayCitations(out, h3Citations, collection, true, citationCollectionId, title, contentCollectionId);
							out.println("</ol>");
						}
						// h1 description
						else if (h2Section.getSectiontype().toString().equals("DESCRIPTION")) {
							out.println("<li id = '" + linkId + "' class='h2Section' data-location='" + h2Section.getLocation() + "' data-sectiontype='" +
									h2Section.getSectiontype() + "' style='background: #cef none repeat scroll 0 0;'>" +
									"<div id = '" + editorDivId + "' class='editor description' style='min-height:30px; padding:5px;'>" +
									h2Section.getValue() + "</div></li>");
						}
					} // end of h2 sections
					out.println("</ol>");
				} // end of h2 sections
				out.println("</li>");
			}  // end of h1 sections
		} // end of h1 sections
		out.println("</ol>");
	}
}

