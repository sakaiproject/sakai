/**
 * $Id$
 * $URL$
 * BrowseEntity.java - entity-broker - Aug 3, 2008 6:03:53 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.sakaiproject.entitybroker.EntityView;

/**
 * This is an object to hold data about a browseable entity type
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class BrowseEntity {

    private String prefix;
    private String title;
    private String description;
    private String parentPrefix;
    private List<String> nestedPrefixes;
    private List<String> entityAccessViews;

    /**
     * Minimal constructor
     * @param prefix an entity prefix
     */
    public BrowseEntity(String prefix) {
        this(prefix, null, null, null, null);
    }

    /**
     * Constructor for types with no children or access provider
     * @param prefix an entity prefix
     * @param title a display title
     * @param description (optional) display description
     */
    public BrowseEntity(String prefix, String title, String description) {
        this(prefix, title, description, null, null);
    }

    /**
     * Constructor for complete types with most optional information,
     * all optionals can be null
     * @param prefix an entity prefix
     * @param title a display title
     * @param description (optional) display description
     * @param nestedPrefixes (optional) the list of nested (children) prefixes
     * @param entityViewKeys (optional) the list of handled views for the access provider
     */
    public BrowseEntity(String prefix, String title, String description, String[] nestedPrefixes, String[] entityViewKeys) {
        this.prefix = prefix;
        setTitleDesc(title, description);
        setNestedPrefixes(nestedPrefixes);
        setEntityViewKeys(entityViewKeys);
    }

    public void setTitleDesc(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public void setNestedPrefixes(String[] nestedPrefixes) {
        if (nestedPrefixes != null && nestedPrefixes.length > 0) {
            for (int i = 0; i < nestedPrefixes.length; i++) {
                addNestedPrefix(nestedPrefixes[i]);
            }
        } else {
            this.nestedPrefixes = null;
        }
    }

    public void setEntityViewKeys(String[] entityViewKeys) {
        if (entityViewKeys != null && entityViewKeys.length > 0) {
            for (int i = 0; i < entityViewKeys.length; i++) {
                addEntityViewKeys(entityViewKeys[i]);
            }
        } else {
            this.entityAccessViews = null;
        }
    }

    /**
     * Adds a nested prefix which is a child of this entity type
     * @param prefix an entity prefix
     */
    public void addNestedPrefix(String prefix) {
        if (this.nestedPrefixes == null) {
            this.nestedPrefixes = new Vector<String>();
        }
        if (! this.nestedPrefixes.contains(prefix)) {
            this.nestedPrefixes.add(prefix);
        }
    }

    /**
     * Adds a view key which is known to be supported by this entity types access provider
     * @param entityViewKey an entity view key (e.g. {@link EntityView#VIEW_NEW})
     */
    public void addEntityViewKeys(String entityViewKey) {
        if (this.entityAccessViews == null) {
            this.entityAccessViews = new Vector<String>();
        }
        if (! this.entityAccessViews.contains(entityViewKey)) {
            this.entityAccessViews.add(entityViewKey);
        }
    }

    public void setParentPrefix(String parentPrefix) {
        this.parentPrefix = parentPrefix;
    }

    /**
     * @return the entity prefix for this browseable entity type
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return the display title for this entity type (will not be null, may just be the prefix)
     */
    public String getTitle() {
        String title = this.title;
        if (title == null) {
            title = prefix;
        }
        return title;
    }

    /**
     * @return the optional description of this type of entity (may be null)
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the prefix for the parent entity type for this type of entity OR null if this is a root type
     */
    public String getParentPrefix() {
        return parentPrefix;
    }

    /**
     * Gets all the nested children prefixes for this entity type (if there are any)
     * 
     * @return the list of nested (children) prefixes for this entity type OR null if there are none (will not be empty list)
     */
    public List<String> getNestedPrefixes() {
        return nestedPrefixes;
    }
    
    /**
     * Get the list of entity access views (e.g. {@link EntityView#VIEW_NEW}) that are known to be handled by this entity type
     * @return the list of view keys OR empty if none handled OR null if the handled views are unknown
     */
    public List<String> getEntityAccessViews() {
        return entityAccessViews;
    }


    @Override
    public boolean equals(Object obj) {
        if (null == obj)
            return false;
        if (!(obj instanceof BrowseEntity))
            return false;
        else {
            BrowseEntity castObj = (BrowseEntity) obj;
            if (null == this.prefix || null == castObj.prefix)
                return false;
            else
                return (this.prefix.equals(castObj.prefix));
        }
    }

    @Override
    public int hashCode() {
        String hashStr = this.getClass().getName() + ":" + this.prefix.hashCode();
        return hashStr.hashCode();
    }

    @Override
    public String toString() {
        return "BE::prefix="+prefix+":title="+title+":nested="+nestedPrefixes+":views="+entityAccessViews;
    }

    public static class PrefixComparator implements Comparator<BrowseEntity>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(BrowseEntity o1, BrowseEntity o2) {
            return o1.prefix.compareTo(o2.prefix);
        }
    }

    public static class TitleComparator implements Comparator<BrowseEntity>, Serializable {
        public final static long serialVersionUID = 1l;
        public int compare(BrowseEntity o1, BrowseEntity o2) {
            return o1.getTitle().compareTo(o2.getTitle());
        }
    }

}
