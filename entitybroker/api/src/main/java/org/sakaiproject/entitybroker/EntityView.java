/**
 * $Id$
 * $URL$
 * EntityView.java - entity-broker - Apr 10, 2008 6:26:47 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
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
 */

package org.sakaiproject.entitybroker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.Template;


/**
 * Defines an entity view (a specific way to looking at entity data, e.g. LIST of entities, SHOW a single entity, 
 * UPDATE an entity, DELETE an entity, create a NEW entity),
 * each view has a unique view key constant related to it (e.g. {@link #VIEW_LIST}) <br/>
 * The view contains all the known information about a view request including
 * the entity prefix, reference, full path and segments, format (extension),
 * method (POST, GET, etc.), and view key (type). The Entity View can
 * generate the URL for this view based on the data it contains. <br/>
 * Views use URL templates which can be controlled via custom templates if desired. <br/>
 * <b>NOTE:</b> For those using custom actions, the view is typically LIST if you are returning data
 * of an unspecified size. If you want to perform write operations (POST, UPDATE, DELETE), you will need
 * to use the appropriate view constant. See the docs on each constant for more detail.
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class EntityView implements Cloneable, Serializable {
    public final static long serialVersionUID = 1l;

    public static final char SEPARATOR = '/';
    public static final char PERIOD = '.';

    public static final String PREFIX = "prefix";
    public static final String ID = "id";
    public static final String DIRECT = "direct";
    public static final String DIRECT_PREFIX = SEPARATOR + DIRECT;

    /**
     * Represents HTTP methods (GET, POST, etc.)
     */
    public static enum Method { POST, GET, PUT, DELETE, HEAD }

    /**
     * Defines the view for the "list" (index) or collection operation,
     * access a list/collection of all entities of a type (possibly filtered by search params),
     * represents a {@link Method#GET} to the entity space/collection,
     * also indicates an action related to reading a collection of entities
     */
    public static final String VIEW_LIST = "list";
    /**
     * Defines the view for the "show" (read) operation,
     * access data or a view of an entity,
     * represents a {@link Method#GET} to a specific entity,
     * also indicates an action related to reading a specific entity
     */
    public static final String VIEW_SHOW = "show";
    /**
     * Defines the view for the "new" (create) operation,
     * create a new record or access a form for creating a new record,
     * represents a {@link Method#POST} (or PUT) to the entity space/collection,
     * also indicates an action related to writing a collection of entities
     */
    public static final String VIEW_NEW  = "new";
    /**
     * Defines the view for the "edit" (update) operation,
     * update an entity or access a form for updating an entity,
     * represents a {@link Method#PUT} (or POST) to a specific entity,
     * also indicates an action related to writing a specific entity
     */
    public static final String VIEW_EDIT = "edit";
    /**
     * Defines the view for the "delete" (destroy) operation,
     * remove an entity or access a form for removing an entity,
     * represents a {@link Method#DELETE} to a specific entity
     */
    public static final String VIEW_DELETE = "delete";


    private String originalEntityURL;
    /**
     * Special use only, 
     * normally you should use {@link #toString()} or {@link #getEntityURL(String, String)}
     * 
     * @return the original entity URL which was used to create this entity view,
     * includes the optional pieces from the URL, will be null if this was created
     * without using a constructor that takes an entityUrl
     */
    public String getOriginalEntityUrl() {
        return originalEntityURL;
    }
    protected void setOriginalEntityURL(String entityUrl) {
        checkEntityURL(entityUrl);
        this.originalEntityURL = entityUrl;
    }

    private String extension;
    /**
     * The extension for this view which defines the type of data that will be returned for this view,
     * examples: html, xml, json
     * <b>NOTE:</b> you should assume html return format when this is null
     * 
     * @return the extension for this view if there is one,
     * this will be null if there was no extension in the original entityUrl
     */
    public String getExtension() {
        return extension;
    }
    public void setExtension(String extension) {
        this.extension = extension;
    }
    /**
     * @return the format (from {@link Formats}) that is being used for this view,
     * will return {@link Formats#HTML} if none set
     */
    public String getFormat() {
        String format = extension;
        if (format == null) {
            format = Formats.HTML;
        }
        return format;
    }

    private Method method = Method.GET;
    /**
     * @return the method (GET, POST, etc.) (from {@link Method}) being used for this view,
     * defaults to GET if none was set explicitly
     */
    public String getMethod() {
        return method.name();
    }
    public void setMethod(Method method) {
        if (method != null) {
            this.method = method;
        }
    }

    private String viewKey;
    /**
     * @return the key which uniquely identifies the view we are associated with,
     * uses the constants like {@link #VIEW_LIST} and {@link #VIEW_NEW}
     */
    public String getViewKey() {
        return viewKey;
    }
    public void setViewKey(String viewKey) {
        TemplateParseUtil.validateTemplateKey(viewKey);
        this.viewKey = viewKey;
    }

    private EntityReference entityReference;
    /**
     * @return the entity reference object which indicates which entity this view related to
     */
    public EntityReference getEntityReference() {
        return entityReference;
    }
    /**
     * Allows for easy chained construction of EntityViews by setting an EntityReference,
     * does not set the viewkey or extension unless they are unset, maintains current extension
     */
    public EntityView setEntityReference(EntityReference ref) {
        if (ref == null) {
            throw new IllegalArgumentException("ref cannot be null");
        }
        if (this.pathSegments == null) {
            this.pathSegments = new HashMap<String, String>();
        }
        this.pathSegments.put(PREFIX, ref.getPrefix());
        if (ref.getId() != null) {
            this.pathSegments.put(ID, ref.getId());
        }
        if (this.viewKey == null) {
            String viewKey = VIEW_LIST;
            if (ref.getId() != null) {
                viewKey = VIEW_SHOW;
            }
            setViewKey(viewKey);
        } else {
            // fix up the viewKey so that it makes sense
            if (VIEW_SHOW.equals(this.viewKey)
                    && ref.getId() == null) {
                this.viewKey = VIEW_LIST;
            }
        }
        this.entityReference = ref;
        return this;
    }

    /**
     * Contains all path segments for this entity reference,
     * e.g. /prefix/id/thing would cause this to contain 3 segments
     * for the prefix, id, and thing as long as one of the parse templates
     * supported that path
     */
    private Map<String, String> pathSegments;

    /**
     * Contains the parsing templates for this entity reference
     */
    private List<Template> parseTemplates;
    public List<Template> getParseTemplates() {
        if (parseTemplates == null) {
            parseTemplates = new ArrayList<Template>( TemplateParseUtil.defaultTemplates );
        }
        return parseTemplates;
    }
    /**
     * Cache the parsed templates for this EB
     */
    private List<PreProcessedTemplate> anazlyzedTemplates;
    public List<PreProcessedTemplate> getAnazlyzedTemplates() {
        return anazlyzedTemplates;
    }


    public EntityView() {
        loadParseTemplates(null);
    }

    /**
     * Constructor which takes an entity URL path,
     * (should not include anything but the path, for example:
     * http://server/webapp/myprefix/edit/3/stuff.xml?system=down
     * would yield the path: /myprefix/edit/3/stuff.xml<br/>
     * <b>NOTE:</b> this is the most common way to construct an entity view object
     * 
     * @param entityURL a URL path which goes to a specific entity view,
     * consists of path segments defined by path templates and includes an option extension
     */
    public EntityView(String entityURL) {
        this();
        parseEntityURL(entityURL);
    }

    /**
     * Turn this viewKey and map of segments (key -> value pairs) into an entity view object
     * 
     * @param viewKey a key which uniquely identifies a view, 
     * from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
     * @param segments a map of replaceable keys (e.g. {@link #PREFIX}) to values,
     * the replaceable variable names -> values (e.g. "prefix" -> "myPrefix"),
     * must contain at LEAST a key for the prefix (use constant {@link #PREFIX}) which is not set to null
     * @param extension (optional) format extension related to this view (e.g. xml), do not include the period,
     * leave this null for no extension
     */
    public EntityView(String viewKey, Map<String, String> segments, String extension) {
        this();
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("segments map cannot be null or empty");
        }
        TemplateParseUtil.validateTemplateKey(viewKey);
        populateInternals(viewKey, segments, extension);
    }

    /**
     * Construct an entity view based on a reference, view, and format extension
     * 
     * @param ref an EntityReference object which represents a unique entity reference
     * @param viewKey (optional) a key which uniquely identifies a view, 
     * from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
     * @param extension (optional) format extension related to this view (e.g. xml), do not include the period,
     * leave this null for no extension
     */
    public EntityView(EntityReference ref, String viewKey, String extension) {
        setEntityReference(ref);
        this.pathSegments = new HashMap<String, String>();
        this.pathSegments.put(PREFIX, ref.getPrefix());
        if (viewKey == null) {
            if (this.entityReference.getId() == null) {
                viewKey = VIEW_LIST;
            } else {
                this.pathSegments.put(ID, ref.getId());
                viewKey = VIEW_SHOW;
            }
        } else {
            if (VIEW_DELETE.equals(viewKey)) {
                setMethod(Method.DELETE);
            } else if (VIEW_EDIT.equals(viewKey)) {
                setMethod(Method.PUT);
            } else if (VIEW_NEW.equals(viewKey)) {
                setMethod(Method.POST);
            }
        }
        setViewKey(viewKey);
        setExtension(extension);
    }

    /**
     * Populates the internal values based on the view key, map of segments, and extension
     */
    protected void populateInternals(String viewKey, Map<String, String> segments, String extension) {
        setViewKey(viewKey);
        if (VIEW_DELETE.equals(viewKey)) {
            setMethod(Method.DELETE);
        } else if (VIEW_EDIT.equals(viewKey)) {
            setMethod(Method.PUT);
        } else if (VIEW_NEW.equals(viewKey)) {
            setMethod(Method.POST);
        } else {
            setMethod(Method.GET);
        }
        this.extension = extension;
        this.pathSegments = new HashMap<String, String>();
        this.pathSegments.putAll(segments);

        String prefix = null;
        String id = null;
        for (Entry<String, String> se : segments.entrySet()) {
            if (PREFIX.equals(se.getKey())) {
                prefix = se.getValue();
            } else if (ID.equals(se.getKey())) {
                id = se.getValue();
            }
        }
        if (prefix == null) {
            throw new IllegalArgumentException("Cannot identify an entity space for this view," +
            "there appears to be no prefix that was parsed from the url");
        }
        this.entityReference = new EntityReference(prefix, id == null ? "" : id);
    }

    // METHODS

    /**
     * Used to build this object after it has already been created (typically so custom templates can be inserted)
     * @param entityURL a URL path which goes to a specific entity view,
     * consists of path segments defined by path templates and includes an option extension
     */
    public void parseEntityURL(String entityURL) {
        this.originalEntityURL = entityURL;
        checkEntityURL(entityURL);
        ProcessedTemplate parsed = TemplateParseUtil.parseTemplate(entityURL, anazlyzedTemplates);

        if (parsed == null) {
            throw new IllegalArgumentException("Could not parse entityURL against any known templates: " + entityURL);
        }

        populateInternals(parsed.templateKey, new HashMap<String, String>(parsed.segmentValues), parsed.extension);
    }

    /**
     * Override this method if creating a custom {@link EntityView} object
     * 
     * @param templates a list of template constants -> parse templates,
     * the array which defines the set of template keys is {@link #PARSE_TEMPLATE_KEYS}<br/>
     * Rules for parse templates:
     * 1) "{","}", and {@link #SEPARATOR} are special characters and must be used as indicated only
     * 2) Must begin with a {@link #SEPARATOR}, must not end with a {@link #SEPARATOR}
     * 3) must begin with "/{prefix}" (use the {@link #SEPARATOR} and {@link #PREFIX} constants)
     * 3) each {var} can only be used once in a template
     * 4) {var} can never touch each other (i.e /{var1}{var2}/{id} is invalid)
     * 5) each {var} can only have the chars from {@link TemplateParseUtil#VALID_VAR_CHARS}
     * 6) parse templates can only have the chars from {@link TemplateParseUtil#VALID_TEMPLATE_CHARS}
     * 7) Empty braces ({}) cannot appear in the template
     */
    public void loadParseTemplates(List<Template> templates) {
        if (parseTemplates == null) {
            parseTemplates = new ArrayList<Template>();
        } else {
            parseTemplates.clear();
        }
        if (templates == null || templates.isEmpty()) {
            // just load in the already processed and made templates and preproced templates for efficiency
            parseTemplates.addAll(TemplateParseUtil.defaultTemplates);
            anazlyzedTemplates = new ArrayList<PreProcessedTemplate>(TemplateParseUtil.defaultPreprocessedTemplates);
        } else {
            for (Template t : templates) {
                TemplateParseUtil.validateTemplateKey(t.templateKey);
                TemplateParseUtil.validateTemplate(t.template);
                parseTemplates.add(t);
            }
            // now add in the default templates that are not already there
            for (Template t : TemplateParseUtil.defaultTemplates) {
                if (! parseTemplates.contains(t)) {
                    parseTemplates.add(t);
                }
            }
        }
        anazlyzedTemplates = TemplateParseUtil.preprocessTemplates(parseTemplates);
    }

    /**
     * Special efficiency method to reduce reloading of custom templates,
     * do not use this unless you wrote it or REALLY know what you are doing
     */
    public void preloadParseTemplates(List<PreProcessedTemplate> preprocessedTemplates) {
        // remake the internal lists and copy in the preprocessed templates
        parseTemplates = new ArrayList<Template>();
        anazlyzedTemplates = new ArrayList<PreProcessedTemplate>();
        if (preprocessedTemplates == null || preprocessedTemplates.isEmpty()) {
            // just load in the already processed and made templates and preproced templates for efficiency
            parseTemplates.addAll(TemplateParseUtil.defaultTemplates);
            anazlyzedTemplates = new ArrayList<PreProcessedTemplate>(TemplateParseUtil.defaultPreprocessedTemplates);
        } else {
            // load in the ones that were provided
            for (PreProcessedTemplate preProcessedTemplate : preprocessedTemplates) {
                anazlyzedTemplates.add(preProcessedTemplate);
                parseTemplates.add( new Template(preProcessedTemplate.templateKey, preProcessedTemplate.template) );
            }
        }
    }

    /**
     * @return the entity URL of the internal reference based on the
     * internal viewKey and extension, defaults to {@link TemplateParseUtil#TEMPLATE_SHOW} or 
     * the {@link TemplateParseUtil#TEMPLATE_LIST} one if there is no id,
     * example: /prefix if there is no id or /prefix/id if there is an id
     * @throws IllegalArgumentException if there is not enough information to generate a URL
     */
    @Override
    public String toString() {
        return getEntityURL();
    }

    /**
     * @return the entity URL of the internal reference based on the
     * internal viewKey and extension, defaults to {@link TemplateParseUtil#TEMPLATE_SHOW} or 
     * the {@link TemplateParseUtil#TEMPLATE_LIST} one if there is no id,
     * example: /prefix if there is no id or /prefix/id if there is an id
     * @throws IllegalArgumentException if there is not enough information to generate a URL
     */
    public String getEntityURL() {
        String URL = getEntityURL(this.viewKey, this.extension);
        return URL;
    }

    /**
     * Get an entityUrl by merging a specific template with the data in this EB object
     * 
     * @param viewKey a key which uniquely identifies a view, 
     * from the set of template keys {@link #PARSE_TEMPLATE_KEYS}
     * @param extension an optional extension related to this view (e.g. xml), 
     * do not include the period, leave this null for no extension
     * @return the entityUrl which goes to this view
     * @throws IllegalArgumentException if the viewKey is invalid OR there is not enough information 
     * in the path segments to generate the requested URL
     */
    public String getEntityURL(String viewKey, String extension) {
        TemplateParseUtil.validateTemplateKey(viewKey);
        // correctly set the viewKey if none is set
        if (viewKey == null) {
            if (entityReference == null || pathSegments == null || pathSegments.isEmpty()) {
                throw new IllegalArgumentException("There is no entity reference information or path segments in this view to process into a URL");
            }
            if (entityReference.getId() == null || pathSegments.size() == 1) {
                viewKey = VIEW_LIST;
            } else {
                viewKey = VIEW_SHOW;
            }
        } else {
            if (VIEW_NEW.equals(viewKey) || VIEW_LIST.equals(viewKey)) {
                // stay as is
            } else {
                // check if it should reset to list only
                if (entityReference.getId() == null || pathSegments.size() == 1) {
                    viewKey = VIEW_LIST;
                }
            }
        }
        String template = getParseTemplate(viewKey);
        if (template == null) {
            throw new IllegalStateException("parseTemplates contains no template for key: " + viewKey);
        }
        String url = TemplateParseUtil.mergeTemplate(template, pathSegments);
        if (extension != null && ! "".equals(extension)) {
            url += PERIOD + extension;
        }
        return url;
    }

    /**
     * Gets the parsed values of path segment variables
     * @param parseVariable a path segment variable (the thing that gets replaced in the parse template)
     * @return the value of this parse variable or null if no value exists
     */
    public String getPathSegment(String parseVariable) {
        return pathSegments.get(parseVariable);
    }

    /**
     * Get a segment value by position from the encoded URL for this view<br/>
     * Position 0 is always the prefix <br/>
     * Example: /user/aaronz/promote/stuff.xml <br/>
     * position 0: 'user' <br/>
     * position 1: 'aaronz' <br/>
     * position 2: 'promote' <br/>
     * position 3: 'stuff' <br/>
     * position 4: null <br/>
     * @param position the position number in the path segments, 0 is always the prefix
     * @return the value at the given path position OR null if there is nothing at that position
     */
    public String getPathSegment(int position) {
        String segment = null;
        String[] segments = getPathSegments();
        if (segments.length > 0) {
            if (position < segments.length) {
                segment = segments[position];
            }
        }
        return segment;
    }

    /**
     * Get all the path segments for the encoded URL for this view<br/>
     * Example: /user/aaronz/promote/stuff.xml <br/>
     * segments = {"user","aaronz","promote","stuff"}
     * @return an array of path segments
     */
    public String[] getPathSegments() {
        String url = getOriginalEntityUrl();
        if (url == null) {
            url = getEntityURL();
        }
        String[] segments = new String[0];
        if (url != null) {
            url = TemplateParseUtil.findExtension(url)[1];
            if (url.charAt(0) == SEPARATOR) {
                url = url.substring(1);
            }
            segments = url.split(SEPARATOR+"");
        }
        return segments;
    }

    /**
     * @param templateKey a key from the set of template keys {@link #PARSE_TEMPLATE_KEYS},
     * should match with the viewKey
     * @return the template being used by this entity view for this key or null if none found
     */
    public String getParseTemplate(String templateKey) {
        TemplateParseUtil.validateTemplateKey(templateKey);
        String template = null;
        for (Template t : getParseTemplates()) {
            if (templateKey.equals(t.templateKey)) {
                template = t.template;
            }
        }
        return template;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return copy(this);
    }

    /**
     * @return a copy of this object
     * @see #copy(EntityView)
     */
    public EntityView copy() {
        return copy(this);
    }

    // STATIC METHODS

    /**
     * Check if an entityUrl is basically valid
     * @param entityURL
     * @throws IllegalArgumentException if the entityUrl is not even basically valid
     */
    protected static void checkEntityURL(String entityURL) {
        if (entityURL == null 
                || "".equals(entityURL)
                || SEPARATOR != entityURL.charAt(0) )
            throw new IllegalArgumentException("Invalid entity Url for EntityBroker: "
                    + entityURL + " - these begin with " + SEPARATOR + " and cannot be null");      
    }

    /**
     * Makes a copy of an EntityView which can be changed independently
     * @param ev any EntityView
     * @return the copy
     * @throws IllegalArgumentException if the input is null OR not completely constructed
     */
    public static EntityView copy(EntityView ev) {
        if (ev == null) {
            throw new IllegalArgumentException("input entity view must not be null");
        }
        if (ev.viewKey == null || ev.entityReference == null) {
            throw new IllegalArgumentException("input entity view must be completely constructed");         
        }
        EntityView togo = new EntityView();
        EntityReference ref = ev.getEntityReference();
        togo.setEntityReference( new EntityReference(ref.getPrefix(), ref.getId() == null ? "" : ref.getId()) );
        togo.preloadParseTemplates( ev.getAnazlyzedTemplates() );
        togo.setExtension( ev.getExtension() );
        togo.setViewKey( ev.getViewKey() );
        return togo;
    }

    /**
     * Translate a viewkey into an http method
     * @param viewKey 
     * @return the method which matches this viewkey
     */
    public static Method translateViewKeyToMethod(String viewKey) {
        Method m = Method.GET;
        if (viewKey.equals(VIEW_DELETE)) {
            m = Method.DELETE;
        } else if (viewKey.equals(VIEW_EDIT)) {
            m = Method.PUT;
        } else if (viewKey.equals(VIEW_NEW)) {
            m = Method.POST;
        }
        return m;
    }

}
