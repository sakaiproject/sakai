/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.assignment.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    public void initEntity(Assignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        if (StringUtils.isNotBlank(assignment.getId())) {
            // if assignment has an id assume its been persisted
            this.assignment = assignment;
            this.assignmentId = assignment.getId();
        }
        reference = entityManager.newReference(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
    }

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
        reference = entityManager.newReference(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
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

    public String getTitle() {
        return assignment.getTitle();
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
