package org.sakaiproject.sitemanage.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.MessageHandler;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.util.Participant;
import org.sakaiproject.site.util.SiteParticipantHelper;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.util.RequestFilter;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.sitemanage.api.SiteParticipantProvider;
import org.sakaiproject.util.ResourceLoader;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SiteParticipantProviderImpl implements SiteParticipantProvider {

	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SiteParticipantProviderImpl.class);
	

	/** portlet configuration parameter values* */
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("printParticipant");
	
    // create transformerFactory object needed by generatePDF
    private TransformerFactory transformerFactory = null;

    // create DocumentBuilder object needed by print PDF
	DocumentBuilder docBuilder =  null;
	
	public void init()
	{
		transformerFactory = TransformerFactory.newInstance();
		try
		{
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		}
		catch (ParserConfigurationException e)
		{
			M_log.warn(this + " cannot get DocumentBuilder " + e.getMessage());
		}
		
		// register as an entity producer
		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);
	}
	// XML Node/Attribute Names
	protected static final String PARTICIPANTS_NODE_NAME = "PARTICIPANTS";
	protected static final String SITE_TITLE_NODE_NAME = "SITE_TITLE";
	protected static final String PARTICIPANT_NODE_NAME = "PARTICIPANT";
	protected static final String PARTICIPANT_NAME_NODE_NAME = "NAME";
	protected static final String PARTICIPANT_SECTIONS_NODE_NAME = "SECTIONS";
	protected static final String PARTICIPANT_SECTION_NODE_NAME = "SECTION";
	protected static final String PARTICIPANT_ID_NODE_NAME = "ID";
	protected static final String PARTICIPANT_CREDIT_NODE_NAME = "CREDIT";
	protected static final String PARTICIPANT_ROLE_NODE_NAME = "ROLE";
	protected static final String PARTICIPANT_STATUS_NODE_NAME = "STATUS";
	
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willImport()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref,
					Collection copyrightAcceptedRefs) throws EntityPermissionException, EntityNotDefinedException
			{
		
				if (SessionManager.getCurrentSessionUserId() == null)
				{
					// fail the request, user not logged in yet.
				}
				else
				{
					String siteId = ref.getId();
					// for logged in user, print pdf
					print_participant(siteId);
				}
			}
		};
	}
	
	/**
	 * generate PDF file containing all site participant
	 * @param data
	 */
	public void print_participant(String siteId)
	{
		
		HttpServletResponse res = (HttpServletResponse) ThreadLocalManager.get(RequestFilter.CURRENT_HTTP_RESPONSE);
		
		ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();

		res.addHeader("Content-Disposition", "inline; filename=\"participants.pdf\"");
		res.setContentType("application/pdf");
		
		Document document = docBuilder.newDocument();
		
		// get the participant xml document
		generateParticipantXMLDocument(document, siteId);

		generatePDF(document, outByteStream);
		res.setContentLength(outByteStream.size());
		if (outByteStream.size() > 0)
		{
			// Increase the buffer size for more speed.
			res.setBufferSize(outByteStream.size());
		}

		/*
		// output xml for debugging purpose
		try
		{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        Transformer transformer = transformerFactory.newTransformer();
	        DOMSource source = new DOMSource(document);
	        StreamResult result =  new StreamResult(System.out);
	        transformer.transform(source, result);
		}
		catch (Exception e)
		{
			
		}*/
        
		OutputStream out = null;
		try
		{
			out = res.getOutputStream();
			if (outByteStream.size() > 0)
			{
				outByteStream.writeTo(out);
			}
			res.setHeader("Refresh", "0");

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
	
	/**
	 * Generate participant document
	 * @param doc
	 * @param siteId
	 */
	protected void generateParticipantXMLDocument(Document doc, String siteId)
	{

		List<String> providerCourseList = SiteParticipantHelper.getProviderCourseList(siteId);
		Collection<Participant> participants = SiteParticipantHelper.prepareParticipants(siteId, providerCourseList);
		
		// Create Root Element
		Element root = doc.createElement(PARTICIPANTS_NODE_NAME);

		String siteTitle = "";
		
		if (siteId != null)
		{
			try
			{
				Site site = SiteService.getSite(siteId);
				siteTitle = site.getTitle();
	    		
				// site title
				writeStringNodeToDom(doc, root, SITE_TITLE_NODE_NAME, rb.getFormattedMessage("participant_pdf_title", new String[] {siteTitle}));
			}
			catch (Exception e)
			{
				M_log.warn(this + ":generateParticipantXMLDocument: Cannot find site with id =" + siteId);
			}
		}

		// Add the Root Element to Document
		doc.appendChild(root);


		if (participants != null)
		{
		
			// Go through all the time ranges (days)
			for (Iterator<Participant> iParticipants = participants.iterator(); iParticipants.hasNext();)
			{
				Participant participant = iParticipants.next();
				// Create Participant Element
				Element participantNode = doc.createElement(PARTICIPANT_NODE_NAME);
				
				// participant name
				String participantName= participant.getName();
				if (participant.getDisplayId() != null)
				{
					participantName +="( " +  participant.getDisplayId() + " )";
				}
				writeStringNodeToDom(doc, participantNode, PARTICIPANT_NAME_NODE_NAME, StringUtil.trimToZero(participantName));

				// sections
				Element sectionsNode = doc.createElement(PARTICIPANT_SECTIONS_NODE_NAME);
				for ( Iterator iSections = participant.getSectionEidList().iterator(); iSections.hasNext();)
				{
					String section = (String) iSections.next();
					writeStringNodeToDom(doc, sectionsNode, PARTICIPANT_SECTION_NODE_NAME, StringUtil.trimToZero(section));
				}
				participantNode.appendChild(sectionsNode);

				// registration id
				writeStringNodeToDom(doc, participantNode, PARTICIPANT_ID_NODE_NAME, StringUtil.trimToZero(participant.getRegId()));
				
				// credit
				writeStringNodeToDom(doc, participantNode, PARTICIPANT_CREDIT_NODE_NAME, StringUtil.trimToZero(participant.getCredits()));

				// role id
				writeStringNodeToDom(doc, participantNode, PARTICIPANT_ROLE_NODE_NAME, StringUtil.trimToZero(participant.getRole()));
				
				// status
				writeStringNodeToDom(doc, participantNode, PARTICIPANT_STATUS_NODE_NAME, StringUtil.trimToZero(participant.active?rb.getString("sitegen.siteinfolist.active"):rb.getString("sitegen.siteinfolist.inactive")));
			
				// add participant node to participants node
				root.appendChild(participantNode);
			}
		}
	}
	
	/**
	 * Utility routine to write a string node to the DOM.
	 */
	protected Element writeStringNodeToDom(Document doc, Element parent, String nodeName, String nodeValue)
	{
		if (nodeValue != null)
		{
			Element name = doc.createElement(nodeName);
			name.appendChild(doc.createTextNode(nodeValue));
			parent.appendChild(name);
			return name;
		}

		return null;
	}
	/**
	 * Takes a DOM structure and renders a PDF
	 * 
	 * @param doc
	 *        DOM structure
	 * @param xslFileName
	 *        XSL file to use to translate the DOM document to FOP
	 */
	protected void generatePDF(Document doc, OutputStream streamOut)
	{
		String xslFileName = "participants-all-attrs.xsl";
		Driver driver = new Driver();

		org.apache.avalon.framework.logger.Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_ERROR);
		MessageHandler.setScreenLogger(logger);
		driver.setLogger(logger);

		driver.setOutputStream(streamOut);
		driver.setRenderer(Driver.RENDER_PDF);

		try
		{
			InputStream in = getClass().getClassLoader().getResourceAsStream(xslFileName);
			Transformer transformer = transformerFactory.newTransformer(new StreamSource(in));

			Source src = new DOMSource(doc);
         
			// Kludge: Xalan in JDK 1.4/1.5 does not properly resolve java classes 
			// (http://xml.apache.org/xalan-j/faq.html#jdk14)
			// Clean this up in JDK 1.6 and pass ResourceBundle/ArrayList parms
			//transformer.setParameter("dayNames0", dayNames[0]);
			transformer.transform(src, new SAXResult(driver.getContentHandler()));
		}

		catch (TransformerException e)
		{
			e.printStackTrace();
			M_log.warn(this+".generatePDF(): " + e);
			return;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans, Set userListAllowImport)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String arg3,
		      List attachments)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids) 
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "siteparticipant";
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Entity getEntityReference(Reference ref)
	{
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{

			String id = null;

			// we will get null, service, site id
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				id = parts[2];
			}

			ref.set(APPLICATION_ID, null, id, null, null);

			return true;
		}

		return false;
	}
}
