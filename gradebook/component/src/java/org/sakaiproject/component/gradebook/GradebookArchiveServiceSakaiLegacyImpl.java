/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.component.gradebook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookArchiveService;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.legacy.resource.Entity;
import org.sakaiproject.service.legacy.resource.EntityProducer;
import org.sakaiproject.service.legacy.resource.Reference;
import org.sakaiproject.service.legacy.resource.ResourceProperties;
import org.sakaiproject.service.legacy.resource.cover.EntityManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookArchive;
import org.sakaiproject.tool.gradebook.business.GradeManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;
import org.sakaiproject.util.java.StringUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <b>The gradebook archive service is currently not implemented for sakai2</b>
 *
 * Archives gradebooks as xml and creates new gradebooks from xml.  This
 * implementation gets its data from (presumably) hibernate managed services.
 * Since the objects returned from hibernate are dynamic proxies, they are not
 * encoded and decoded from xml properly.  So, we take each collection and
 * "de-proxy" them before proceeding with the xml serialization.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradebookArchiveServiceSakaiLegacyImpl implements GradebookArchiveService, EntityProducer {
    private static Log log = LogFactory.getLog(GradebookArchiveServiceSakaiLegacyImpl.class);

    private ContextManagement contextManagement;
    private GradebookService gradebookService;
    private GradebookManager gradebookManager;
    private GradeManager gradeManager;
    
    protected String SERVICE_NAME = "gradebook"; 

	/**
	 * @see org.sakaiproject.service.legacy.resource.EntityProducer#getLabel()
	 */
	public String getLabel() {
        return "gradebook";
	}

    /**
     * Registers this service with sakai so it will be called upon site import
     * and export.
     */
    public void init() {
        //if(log.isInfoEnabled()) log.info("Registering gradebook archive service with sakai as id=" + GradebookArchiveService.class.getName());
        EntityManager.registerEntityProducer(this);
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willImport()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(Entity.SEPARATOR + SERVICE_NAME))
		{
			String id = null;
			String context = null;

			// we will get null, gradebook, context, id
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				context = parts[2];
			}
			if (parts.length > 3)
			{
				id = parts[3];
			}

			ref.set(SERVICE_NAME, null, id, null, context);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		// double check that it's mine
		if (SERVICE_NAME != ref.getType()) return null;

		return "Gradebook: " + ref.getContext() + " / " + ref.getId();
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
	public Collection getEntityRealms(Reference ref)
	{
		// double check that it's mine
		if (SERVICE_NAME != ref.getType()) return null;

		Collection rv = new Vector();

		try
		{
			// site
			ref.addSiteContextRealm(rv);

			// specific
			rv.add(ref.getReference());
		}
		catch (Throwable e)
		{
		}
		
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * @see org.sakaiproject.service.legacy.resource.EntityProducer#archive(java.lang.String, org.w3c.dom.Document, java.util.Stack, java.lang.String, org.sakaiproject.service.legacy.resource.ReferenceVector)
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments) {
        if(log.isDebugEnabled()) log.debug("archiving gradebook for " + siteId);

        // Keep a log of messages relating to this archive attempt
        StringBuffer logMessages = new StringBuffer();

        GradebookArchive archive = createGradebookArchive(siteId, logMessages);
        if(archive == null) {
            return logMessages.toString();
        }

        Element element = doc.createElement(GradebookArchiveService.class.getName());
        ((Element) stack.peek()).appendChild(element);

        String xml = archive.archive();
        if(log.isDebugEnabled()) log.debug("Adding element to doc: " + element);
        if(log.isDebugEnabled()) log.debug("doc: " + doc);

        element.appendChild(element.getOwnerDocument().createCDATASection(xml));

        logMessages.append("Gradebook uid=" + archive.getGradebook().getUid() + " has been archived\n");
        return logMessages.toString();
	}


	/**
     * @param siteId
     * @param logMessages
     * @return
     */
    private GradebookArchive createGradebookArchive(String siteId, StringBuffer logMessages) {
        // Get the gradebook for this context/site (as of sakai2, they are the same thing)
        Gradebook gradebook;
        Set gradeMappings = new HashSet();
        GradeMapping selectedGradeMapping;
        CourseGrade courseGrade;
        Collection concreteAssignments;

        try {
            gradebook = gradebookManager.getGradebook(siteId);
        } catch (GradebookNotFoundException gbnfe) {
            logMessages.append("Error: " + gbnfe + "\n");
            return null;
        }

        // Ensure that we have the full maps in memory
        for(Iterator iter = gradebook.getGradeMappings().iterator(); iter.hasNext();) {
            GradeMapping mapping = (GradeMapping)iter.next();
            Map concreteMap = new HashMap(mapping.getGradeMap());
            mapping.setGradeMap(concreteMap);
            gradeMappings.add(mapping);
        }
        gradebook.setGradeMappings(gradeMappings);

        selectedGradeMapping = gradebook.getSelectedGradeMapping();
        Map concreteMap = new HashMap(selectedGradeMapping.getGradeMap());
        selectedGradeMapping.setGradeMap(concreteMap);


        courseGrade = gradeManager.getCourseGrade(gradebook.getId());

        Collection assignments = gradeManager.getAssignments(gradebook.getId());
        concreteAssignments = new ArrayList();
        for(Iterator iter = assignments.iterator(); iter.hasNext();) {
            Assignment asn = (Assignment)iter.next();
            concreteAssignments.add(asn);
        }

        return new GradebookArchive(gradebook, selectedGradeMapping, gradeMappings, courseGrade, concreteAssignments);
    }

    /**
	 * @see org.sakaiproject.service.legacy.resource.EntityProducer#merge(java.lang.String, org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.HashMap, java.util.Set)
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId,
            Map attachmentNames, Map userIdTrans, Set userListAllowImport) {

        if(log.isDebugEnabled()) log.debug("merging gradebook into " + siteId);

        // Keep a log of messages relating to this archive attempt
        StringBuffer logMessages = new StringBuffer();

        // Get the gradebook for this context.  If it exists, then throw an error
        if(gradebookService.gradebookExists(siteId)) {
            logMessages.append("A gradebook already exists in this site.");
            return logMessages.toString();
        }

        // Read the gradebook archive object from the xml document root
        GradebookArchive archive = new GradebookArchive();
        if(log.isDebugEnabled()) log.debug("root=" + root);
        if(log.isDebugEnabled()) log.debug("first child = " + root.getFirstChild());
        if(log.isDebugEnabled()) log.debug("child value = " + root.getFirstChild().getNodeValue());
        archive.readArchive(root.getFirstChild().getNodeValue());

        // Create the gradebook for this site
        Gradebook gradebook = archive.getGradebook();
        gradebookService.addGradebook(siteId, siteId);
        logMessages.append("Addes gradebook uid=" + siteId + "\n");

        Gradebook persistentGradebook;
        try {
			// Use the archived course grade
			persistentGradebook = gradebookManager.getGradebook(siteId);
		} catch (GradebookNotFoundException e1) {
            log.error("The gradebook for this site does not exist.  There was a horrible problem, cause I just added it!");
            return null;
		}

        // Add the assignments
        for(Iterator iter = archive.getAssignments().iterator(); iter.hasNext();) {
            Assignment asn = (Assignment)iter.next();
            try {
                gradeManager.createAssignment(persistentGradebook.getId(), asn.getName(), asn.getPointsPossible(), asn.getDueDate());
                logMessages.append("Adding " + asn.getName() + " to gradebook " + persistentGradebook.getUid() + "\n");
            } catch (Exception ex) {
                log.error(ex);
            }
        }
        if(log.isDebugEnabled()) log.debug("finished merging gradebook for " + siteId);
        return logMessages.toString();
	}

	/**
	 * @see org.sakaiproject.service.legacy.resource.EntityProducer#importEntities(java.lang.String, java.lang.String, java.util.List)
	 */
	public void importEntities(String fromContext, String toContext, List resourceIds) {

        if(log.isDebugEnabled()) log.debug("copying gradebook from " + fromContext + " to " + toContext + ".  This is not yet supported");

        // Get the gradebook for the "from" context

        // Create a gradebook archive object

        // Add all of the contents of the gradebook archive to a new gradebook in the "to" context

        if(log.isDebugEnabled()) log.debug("finished copying gradebook from " + fromContext + " to " + toContext);
	}

	/**
	 * @see org.sakaiproject.service.gradebook.shared.GradebookArchiveService#createArchive(java.lang.String, org.w3c.dom.Document)
	 */
	public String createArchive(String gradebookUid, Document doc) {
        Stack stack = new Stack();
        Element element = doc.createElement("SakaiArchiveRootNode");
        stack.push(element);
        doc.appendChild(element);
        return archive(gradebookUid, doc, stack, null, null);
	}

    /**
     * @see org.sakaiproject.service.gradebook.shared.GradebookArchiveService#createGradebookFromArchive(java.lang.String, org.w3c.dom.Document)
     */
    public String createGradebookFromArchive(String context, Document doc) {
        Element gradebookElement;
        try {
            gradebookElement = (Element)doc.getElementsByTagName(GradebookArchiveService.class.getName()).item(0);
        } catch (Exception e) {
            log.error("There was no gradebook element in the document: " + e);
            return null;
        }
        if(log.isDebugEnabled()) log.debug("gradebookElement = " + gradebookElement);
        return merge(context, gradebookElement, null, null, null, null, null);
    }


    public void setContextManagement(ContextManagement contextManagement) {
        this.contextManagement = contextManagement;
    }
    public void setGradeManager(
			GradeManager gradeManager) {
		this.gradeManager = gradeManager;
	}
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}
	public void setGradebookService(GradebookService gradebookService) {
		this.gradebookService = gradebookService;
	}
}



