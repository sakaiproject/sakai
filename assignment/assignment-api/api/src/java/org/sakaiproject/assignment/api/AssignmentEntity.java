package org.sakaiproject.assignment.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by enietzel on 3/6/17.
 */
@Slf4j
public class AssignmentEntity implements Entity {

    @Setter private AssignmentService assignmentService;
    @Setter private EntityManager entityManager;
    @Setter private ServerConfigurationService serverConfigurationService;

    private String assignmentId;
    private Assignment assignment;
    private Reference reference;

    public void initEntity(String assignmentId) {
        Objects.requireNonNull(assignmentId, "Assignment id cannot be null");
        this.assignmentId = assignmentId;
        try {
            assignment = assignmentService.getAssignment(assignmentId);
        } catch (IdUnusedException | PermissionException e) {
            log.warn("Could not fetch assignment with id = {}", reference.getId(), e);
        }

        if (assignment == null) {
            throw new RuntimeException("Cannot instantiate AssignmentEntity without an assignment...");
        }
        reference = entityManager.newReference(AssignmentReferenceReckoner.reckoner().context(assignment.getContext()).id(assignment.getId()).subtype("a").reckon().getReference());
    }

    @Override
    public String getUrl() {
        return reference.getUrl();
    }

    @Override
    public String getReference() {
        return reference.getReference();
    }

    @Override
    public String getUrl(String rootProperty) {
        return null;
    }

    @Override
    public String getReference(String rootProperty) {
        return getReference();
    }

    @Override
    public String getId() {
        return reference.getId();
    }

    @Override
    public ResourceProperties getProperties() {
        return reference.getProperties();
    }

    @Override
    public Element toXml(Document doc, Stack<Element> stack) {
        String xml = assignmentService.getXmlAssignment(assignment);
        Document document = null;
        try (ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes("UTF-8"))) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = builder.parse(in);
        } catch (UnsupportedEncodingException e) {
            log.error("Could not read assignment XML input stream", e);
        } catch (ParserConfigurationException e) {
            log.error("Could not get instance an of DocumentBuilder", e);
        } catch (SAXException e) {
            log.error("Could not parse assignment xml", e);
        } catch (IOException e) {
            log.error("IO error", e);
        }
        if (document != null) {
            return document.getDocumentElement();
        }
        return null;
    }
}
