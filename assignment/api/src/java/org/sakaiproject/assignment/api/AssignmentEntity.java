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
        if (assignment != null && StringUtils.isNotBlank(assignment.getId())) {
            // if assignment has an id assume its been persisted
            this.assignment = assignment;
            this.assignmentId = assignment.getId();
            reference = entityManager.newReference(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
        } else {
            log.warn("Can not initialize entity with assignment {}", assignment);
        }
    }

    public void initEntity(String assignmentId) {
        if (StringUtils.isNotBlank(assignmentId)) {
            try {
                assignment = assignmentService.getAssignment(assignmentId);
                this.assignmentId = assignmentId;
                reference = entityManager.newReference(AssignmentReferenceReckoner.reckoner().assignment(assignment).reckon().getReference());
            } catch (Exception e) {
                log.warn("Could not initialize entity with assignment id {}", assignmentId, e);
            }
        } else {
            log.warn("Can not initialize entity with assignment id {}", assignmentId);
        }
    }

    private String getAccessPoint(boolean relative) {
        return (relative ? "" : serverConfigurationService.getAccessUrl());
    }

    @Override
    public String getUrl() {
        return (reference != null) ? getAccessPoint(false) + reference.getReference() : null;
    }

    @Override
    public String getReference() {
        return (reference != null) ? reference.getReference() : null;
    }

    @Override
    public String getUrl(String rootProperty) {
        return getUrl();
    }

    @Override
    public String getReference(String rootProperty) {
        return getReference();
    }

    @Override
    public String getId() {
        return (reference != null) ? reference.getId() : null;
    }

    public String getTitle() {
        return (assignment != null) ? assignment.getTitle() : null;
    }

    @Override
    public ResourceProperties getProperties() {
        return (reference != null) ? reference.getProperties() : null;
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
