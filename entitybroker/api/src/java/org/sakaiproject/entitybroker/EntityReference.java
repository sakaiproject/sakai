/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
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
 **/

package org.sakaiproject.entitybroker;

import java.io.Serializable;

import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;

/**
 * The class of all Entity references handled by the EntityBroker system. This provides the
 * entity prefix, which uniquely identifies the {@link EntityProvider}
 * responsible for handling the Entity. It optionally provides the entity id which
 * uniquely identifies an entity locally within the prefix space. 
 * It also provides for all parsing methods for
 * entity references (always of the form /prefix or /prefix/id)
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityReference implements Cloneable, Serializable {
    public final static long serialVersionUID = 1l;

    // NOTE: these are transient for serialization - ignore PMD SE_TRANSIENT_FIELD_NOT_RESTORED
    public static transient final char SEPARATOR = EntityView.SEPARATOR;
    public static transient final char PERIOD = EntityView.PERIOD;

    protected transient String originalReference;
    /**
     * This is a special method and should not normally be used,
     * use {@link #toString()} or {@link #getReference()}
     * @return the reference string used to create this object or
     * null if this was created using a constructor which does not supply a reference
     */
    public String getOriginalReference() {
        return originalReference;
    }
    protected void setOriginalReference(String reference) {
        checkReference(reference);
        this.originalReference = reference;
    }

    /**
     * An entity prefix, should match with the prefix handled in an {@link EntityProvider},
     * uniquely identifies an entity space or entity type<br/>
     * <b>WARNING:</b> use the {@link #getPrefix()} method rather than referring to this directly
     */
    public String prefix;
    /**
     * @return the entity prefix (uniquely identifies an entity space or entity type),
     * this should never be null
     */
    public String getPrefix() {
        return makeEntityPrefix();
    }

    private String entityId;
    /**
     * A local entity id, represents an entity uniquely in a tool/webapp, 
     * could match with the actual id of a model data object,
     * this will be null if this reference refers to an entity space only
     * 
     * @return the entity id (locally unique id for an entity of this entity type)
     * or null if this this reference refers to an entity space only
     */
    public String getId() {
        return makeEntityId();
    }


    // CONSTRUCTORS

    private transient boolean empty = true;
    /**
     * @return true if this object contains no reference data, false if it contains a valid reference
     */
    public boolean isEmpty() {
        return empty;
    }

    public EntityReference() { }

    /**
     * Constructor which takes an entity reference,
     * this is the most common way to construct an entity reference object
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional path segments
     */
    public EntityReference(String reference) {
        // OVERRIDE THIS when creating your own EntityReference
        this();
        checkReference(reference);
        this.originalReference = reference;
        this.prefix = findPrefix(reference);
        this.entityId = findId(reference);
        empty = false;
    }

    /**
     * Convenience constructor for when you know the prefix and/or id
     * 
     * @param prefix the entity prefix
     * @param id the local entity id (can be empty string if there is no id)
     */
    public EntityReference(String prefix, String id) {
        // OVERRIDE THIS when creating your own EntityReference
        this();
        checkPrefixId(prefix, id);

        this.prefix = prefix;
        if ("".equals(id)) { id = null; }
        this.entityId = id;
        empty = false;
    }


    // METHODS

    /**
     * Override this if you are making a new class to define your entity reference
     * 
     * @param spaceOnly if this is true then only return the entity space reference (e.g. /prefix),
     * otherwise return the full reference (e.g. /prefix/id)
     * @return an entity reference string
     */
    protected String makeEntityReference(boolean spaceOnly) {
        // OVERRIDE THIS when creating your own EntityReference
        if (getPrefix() == null) {
            throw new IllegalStateException("prefix is null, cannot generate the string reference");
        }
        String ref = null;
        if (spaceOnly || getId() == null) {
            ref = SEPARATOR + getPrefix();
        } else {
            ref = SEPARATOR + getPrefix() + SEPARATOR + getId();
        }
        return ref;
    }

    /**
     * Override this if you are making a new class to define your entity reference,
     * called by public {@link #getPrefix()} method
     * 
     * @return the prefix for the current entity reference
     */
    protected String makeEntityPrefix() {
        prefix = "".equals(prefix) ? null : prefix; // fix empty prefix to null
        return prefix;
    }

    /**
     * Override this if you are making a new class to define your entity reference,
     * called by public {@link #getId()} method
     * 
     * @return the prefix for the current entity reference
     */
    protected String makeEntityId() {
        entityId = "".equals(entityId) ? null : entityId; // fix empty id to null
        return entityId;
    }


    /**
     * Get the string reference for this entity reference object,
     * same as calling {@link #toString()}
     * @return the full entity reference
     */
    public String getReference() {
        return this.toString();
    }

    /**
     * Get the space reference for this entity reference,
     * this ignored any id and only returns the reference to the entity space
     * @return the entity space reference (e.g. /myPrefix)
     */
    public String getSpaceReference() {
        return makeEntityReference(true);
    }

    /**
     * @return the string version of this entity reference,
     * example: /prefix if there is no id or /prefix/id if there is an id
     */
    @Override
    public String toString() {
        return makeEntityReference(false);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return copy(this);
    }

    /**
     * @return a copy of this reference
     * @see #copy(EntityReference)
     */
    public EntityReference copy() {
        return copy(this);
    }

    // STATIC METHODS

    /**
     * Get the entity prefix based on an entity reference string,
     * <b>WARNING:</b> this is meant for internal use,
     * use {@link EntityReference#EntityReference(String)} and
     * the methods in {@link EntityBroker} to parse references
     * 
     * @param reference an entity reference or entity URL
     * @return the entity prefix
     */
    public static String getPrefix(String reference) {
        return findPrefix(reference);
    }

    /**
     * Will convert a reference into an id (even if it is not a reference)
     * @param reference an entity reference (e.g. /user/aaronz)
     * @return the id from the reference (e.g. aaronz), preserves null
     */
    public static String getIdFromRef(String reference) {
        String id = null;
        if (reference != null) {
            if (reference.startsWith("/")) {
                // assume the form of "/user/userId"
                id = new EntityReference(reference).getId();
            } else {
                // otherwise assume this is the id
                id = reference;
            }
        }
        return id;
    }

    /**
     * Get the id value out of a reference by the key that preceeds it
     * @param reference an entity reference (e.g. /site/siteId/group/groupId)
     * @param key the key to get the id from (e.g. 'group' yields groupId)
     * @return the id value OR null if no key is found or no id is available
     */
    public static String getIdFromRefByKey(String reference, String key) {
        String id = null;
        if (reference != null) {
            String[] parts = reference.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (key.equals(parts[i])) {
                    if (parts.length > i+1) {
                        id = parts[i+1];
                    }
                }
            }
        }
        return id;
    }

    /**
     * Get the entity prefix based on an entity reference
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional id
     * @return the entity prefix
     */
    protected static String findPrefix(String reference) {
        int spos = getSeparatorPos(reference, 1);
        if (spos == -1) {
            // trim off the extension from the end
            spos = reference.lastIndexOf(PERIOD);
        }
        return spos == -1 ? reference.substring(1) : reference.substring(1, spos);
    }

    /**
     * Get the local entity id based on a full entity reference
     * 
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional id
     * @return the local entity id or null if none can be found
     */
    protected static String findId(String reference) {
        String id = null;
        int spos = getSeparatorPos(reference, 1);
        if (spos != -1) {
            int spos2 = getSeparatorPos(reference, 2);
            if (spos2 == -1) {
                // trim off the extension from the end if it seems valid
                if (TemplateParseUtil.findExtension(reference)[2] != null) {
                    spos2 = reference.lastIndexOf(PERIOD);
                }
            }
            id = spos2 == -1 ? reference.substring(spos + 1) : reference.substring(spos + 1, spos2);
        }
        return id;
    }

    /**
     * @param reference a globally unique reference to an entity, 
     * consists of the entity prefix and optional id
     * @param number this is the separator to get,
     * 0 would return the first one found, 1 would return the second
     * @return the location of the separator between the entity and the id or -1 if none found
     */
    protected static int getSeparatorPos(String reference, int number) {
        checkReference(reference);
        int position = 0;
        for (int i = 0; i < number; i++) {
            position = reference.indexOf(SEPARATOR, position+1);
            if (position < 0) {
                break;
            }
        }
        return position;
    }

    /**
     * Check if a reference is basically valid
     * @param reference
     * @throws IllegalArgumentException if the reference is not even basically valid
     */
    protected static void checkReference(String reference) {
        if (reference == null 
                || "".equals(reference)
                || SEPARATOR != reference.charAt(0) )
            throw new IllegalArgumentException("Invalid entity reference for EntityBroker: "
                    + reference + " - these begin with /prefix, e.g. " + SEPARATOR + "myentity"
                    + SEPARATOR + "3 OR " + SEPARATOR + "myentity");
    }

    /**
     * Checks this prefix and id to see if they are valid format, throw exceptions if they aren't
     */
    protected static void checkPrefixId(String prefix, String id) {
        if (prefix == null || prefix.equals("") || id == null) {
            throw new IllegalArgumentException("prefix ("+prefix+") and id ("+id+") cannot be null (prefix cannot be empty) to get entity reference");
        }
        boolean prefixOK = prefix.matches(TemplateParseUtil.VALID_VAR_CHARS+"+");
        boolean idOK = id.matches(TemplateParseUtil.VALID_VAR_CHARS+"*");
        if (! prefixOK || ! idOK ) {
            throw new IllegalArgumentException("prefix ("+prefix+") and id ("+id+") must contain only valid chars: " + TemplateParseUtil.VALID_VAR_CHARS);
        }
    }

    /**
     * Creates a copy of an entity reference
     * @param ref an entity reference object
     * @return the new copy of the ref
     * @throws IllegalArgumentException if the ref is invalid OR null
     */
    public static EntityReference copy(EntityReference ref) {
        if (ref == null) {
            throw new IllegalArgumentException("input entity reference must not be null");
        }
        EntityReference copy = new EntityReference(ref.getPrefix(), ref.getId() == null ? "" : ref.getId());
        return copy;
    }

}
