package org.sakaiproject.meetings.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import javax.annotation.Resource;

import org.sakaiproject.meetings.api.persistence.Meeting;
import org.sakaiproject.meetings.api.MeetingsService;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.meetings.api.beans.MeetingTransferBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MeetingsEntityProducer implements EntityProducer, EntityTransferrer, ContextObserver {

    private static final String ARCHIVE_VERSION = "1.0.8"; // in case new features are added in future exports
    private static final String VERSION_ATTR = "version";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";
    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "meetings";
    public static final String APPLICATION_ID = "sakai.meetings-tool";
    public static final String APPLICATION = "meetings-tool";
    public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";

    @Resource private MeetingsService meetingManager;
    @Resource private EntityManager entityManager;
    @Resource private ServerConfigurationService serverConfigurationService;
    @Resource private SiteService siteService;

    public void init() {

        log.debug(APPLICATION + " init()");

        try {
            entityManager.registerEntityProducer(this, REFERENCE_ROOT);
        } catch (Exception e) {
            log.warn("Error registering " + APPLICATION + " Entity Producer", e);
        }
    }

    protected String serviceName() {
       return MeetingsEntityProducer.class.getName();
    }


    /**
     * {@inheritDoc}
     */
    public HttpAccess getHttpAccess() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Collection getEntityAuthzGroups(Reference ref, String userId) {
       return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getEntityUrl(Reference ref) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Entity getEntity(Reference ref) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceProperties getEntityResourceProperties(Reference ref) {
        return null;
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
    public boolean parseEntityReference(String reference, Reference ref)
    {
        // not for the moment
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sakaiproject.service.legacy.entity.ResourceService#merge(java.lang.String,
     *      org.w3c.dom.Element, java.lang.String, java.lang.String, java.util.Map, java.util.HashMap,
     *      java.util.Set)
     */
    public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
            Set userListAllowImport) {
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sakaiproject.service.legacy.entity.ResourceService#archive(java.lang.String,
     *      org.w3c.dom.Document, java.util.Stack, java.lang.String,
     *      org.sakaiproject.service.legacy.entity.ReferenceVector)
     */
    public String archive(String siteId, Document doc, Stack stack, String arg3, List attachments) {
        //prepare the buffer for the results log
        StringBuilder results = new StringBuilder();

        try {
            Site site = siteService.getSite(siteId);
            // start with an element with our very own (service) name
            Element element = doc.createElement(serviceName());
            element.setAttribute(VERSION_ATTR, ARCHIVE_VERSION);
            ((Element) stack.peek()).appendChild(element);
            stack.push(element);

            Element linktool = doc.createElement(APPLICATION);
            Collection<ToolConfiguration> tools = site.getTools(myToolIds());
            if (tools != null && !tools.isEmpty()) {
                for (ToolConfiguration config: tools) {
                    element = doc.createElement(APPLICATION);

                    Attr attr = doc.createAttribute("toolid");
                    attr.setValue(config.getToolId());
                    element.setAttributeNode(attr);

                    attr = doc.createAttribute("name");
                    attr.setValue(config.getContainingPage().getTitle());
                    element.setAttributeNode(attr);

                    Properties props = config.getConfig();
                    if (props == null)
                        continue;

                    String url = props.getProperty("url", null);
                    if (url == null && props != null) {
                        String urlProp = props.getProperty("urlProp", null);
                        if (urlProp != null) {
                            url = serverConfigurationService.getString(urlProp);
                        }
                    }

                    attr = doc.createAttribute("url");
                    attr.setValue(url);
                    element.setAttributeNode(attr);

                    String height = "600";
                    String heights =  props.getProperty("height", "600");
                    if (heights != null) {
                        heights = heights.trim();
                        if (heights.endsWith("px"))
                            heights = heights.substring(0, heights.length()-2).trim();
                        height = heights;
                    }

                    attr = doc.createAttribute("height");
                    attr.setValue(height);
                    element.setAttributeNode(attr);

                    linktool.appendChild(element);
                }

                results.append("archiving " + getLabel() + ": (" + tools.size() + ") " + APPLICATION + " instances archived successfully.\n");

            } else {
                results.append("archiving " + getLabel()
                   + ": no " + APPLICATION + " tools.\n");
            }

            ((Element) stack.peek()).appendChild(linktool);
            stack.push(linktool);

            stack.pop();
        } catch (Exception any) {
            log.warn("archive: exception archiving service: " + serviceName());
        }

        stack.pop();

        return results.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sakaiproject.service.legacy.entity.ResourceService#getLabel()
     */
    public String getLabel() {
      return APPLICATION;
    }

    /**
     * {@inheritDoc}
     */
    public boolean willArchiveMerge() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean willImport() {
        return true;
    }


    //EntityTransferrer implementation
    /**
     * {@inheritDoc}
     */
    public String[] myToolIds() {
        return new String[] { APPLICATION_ID, "sakai.meetings" };
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions) {

        log.debug("transferCopyEntities");
        try {
            meetingManager.getSiteMeetings(fromContext).forEach(m -> {
                m.id = null;
                m.siteId = toContext;
                meetingManager.databaseStoreMeeting(m);
            });
        } catch( Exception e) {
            log.debug("Exception occurred " + e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> transferCopyEntities(String fromContext, String toContext, List<String> ids, List<String> transferOptions, boolean cleanup) {

        try {
            if (cleanup == true) {
                meetingManager.deleteMeetingsBySiteId(toContext);
            }

            transferCopyEntities(fromContext, toContext, ids, transferOptions);

        } catch (Exception e) {
            log.info("WebContent transferCopyEntities Error" + e);
        }
        return null;
    }

    /// ContextObserver implementation
    public void contextCreated(String context, boolean toolPlacement) {
    }

    public void contextUpdated(String context, boolean toolPlacement) {
    }

    public void contextDeleted(String context, boolean toolPlacement) {
        //Delete meetings
        try {
            meetingManager.deleteMeetingsBySiteId(context);
        } catch (Exception e) {
            log.info(APPLICATION + " contextDeleted Error: " + e);
        }
    }
}
