/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.jcr.api;

import javax.jcr.Session;

/**
 * This Interface defines some of the item names that are defined in the
 * jcr spec 1.0, using the default prefixes 'jcr', 'nt' and 'mix'. Please note
 * that those prefixes can by redefined by an application using the
 * {@link Session#setNamespacePrefix(String, String)} method. As a result, the
 * constants may not refer to the respective items.
 */
public interface JCRConstants {
    /**
     * jcr:autoCreated
     */
    public static final String JCR_AUTOCREATED = "jcr:autoCreated";
    /**
     * jcr:baseVersion
     */
    public static final String JCR_BASEVERSION = "jcr:baseVersion";
    /**
     * jcr:child
     */
    public static final String JCR_CHILD = "jcr:child";
    /**
     * jcr:childNodeDefinition
     */
    public static final String JCR_CHILDNODEDEFINITION = "jcr:childNodeDefinition";
    /**
     * jcr:content
     */
    public static final String JCR_CONTENT = "jcr:content";
    /**
     * jcr:created
     */
    public static final String JCR_CREATED = "jcr:created";
    /**
     * jcr:data
     */
    public static final String JCR_DATA = "jcr:data";
    /**
     * jcr:defaultPrimaryType
     */
    public static final String JCR_DEFAULTPRIMARYTYPE = "jcr:defaultPrimaryType";
    /**
     * jcr:defaultValues
     */
    public static final String JCR_DEFAULTVALUES = "jcr:defaultValues";
    /**
     * jcr:encoding
     */
    public static final String JCR_ENCODING = "jcr:encoding";
    /**
     * jcr:frozenMixinTypes
     */
    public static final String JCR_FROZENMIXINTYPES = "jcr:frozenMixinTypes";
    /**
     * jcr:frozenNode
     */
    public static final String JCR_FROZENNODE = "jcr:frozenNode";
    /**
     * jcr:frozenPrimaryType
     */
    public static final String JCR_FROZENPRIMARYTYPE = "jcr:frozenPrimaryType";
    /**
     * jcr:frozenUuid
     */
    public static final String JCR_FROZENUUID = "jcr:frozenUuid";
    /**
     * jcr:hasOrderableChildNodes
     */
    public static final String JCR_HASORDERABLECHILDNODES = "jcr:hasOrderableChildNodes";
    /**
     * jcr:isCheckedOut
     */
    public static final String JCR_ISCHECKEDOUT = "jcr:isCheckedOut";
    /**
     * jcr:isMixin
     */
    public static final String JCR_ISMIXIN = "jcr:isMixin";
    /**
     * jcr:language
     */
    public static final String JCR_LANGUAGE = "jcr:language";
    /**
     * jcr:lastModified
     */
    public static final String JCR_LASTMODIFIED = "jcr:lastModified";
    /**
     * jcr:lockIsDeep
     */
    public static final String JCR_LOCKISDEEP = "jcr:lockIsDeep";
    /**
     * jcr:lockOwner
     */
    public static final String JCR_LOCKOWNER = "jcr:lockOwner";
    /**
     * jcr:mandatory
     */
    public static final String JCR_MANDATORY = "jcr:mandatory";
    /**
     * jcr:mergeFailed
     */
    public static final String JCR_MERGEFAILED = "jcr:mergeFailed";
    /**
     * jcr:mimeType
     */
    public static final String JCR_MIMETYPE = "jcr:mimeType";
    /**
     * jcr:mixinTypes
     */
    public static final String JCR_MIXINTYPES = "jcr:mixinTypes";
    /**
     * jcr:multiple
     */
    public static final String JCR_MULTIPLE = "jcr:multiple";
    /**
     * jcr:name
     */
    public static final String JCR_NAME = "jcr:name";
    /**
     * jcr:nodeTypeName
     */
    public static final String JCR_NODETYPENAME = "jcr:nodeTypeName";
    /**
     * jcr:onParentVersion
     */
    public static final String JCR_ONPARENTVERSION = "jcr:onParentVersion";
    /**
     * jcr:predecessors
     */
    public static final String JCR_PREDECESSORS = "jcr:predecessors";
    /**
     * jcr:primaryItemName
     */
    public static final String JCR_PRIMARYITEMNAME = "jcr:primaryItemName";
    /**
     * jcr:primaryType
     */
    public static final String JCR_PRIMARYTYPE = "jcr:primaryType";
    /**
     * jcr:propertyDefinition
     */
    public static final String JCR_PROPERTYDEFINITION = "jcr:propertyDefinition";
    /**
     * jcr:protected
     */
    public static final String JCR_PROTECTED = "jcr:protected";
    /**
     * jcr:requiredPrimaryTypes
     */
    public static final String JCR_REQUIREDPRIMARYTYPES = "jcr:requiredPrimaryTypes";
    /**
     * jcr:requiredType
     */
    public static final String JCR_REQUIREDTYPE = "jcr:requiredType";
    /**
     * jcr:rootVersion
     */
    public static final String JCR_ROOTVERSION = "jcr:rootVersion";
    /**
     * jcr:sameNameSiblings
     */
    public static final String JCR_SAMENAMESIBLINGS = "jcr:sameNameSiblings";
    /**
     * jcr:statement
     */
    public static final String JCR_STATEMENT = "jcr:statement";
    /**
     * jcr:successors
     */
    public static final String JCR_SUCCESSORS = "jcr:successors";
    /**
     * jcr:supertypes
     */
    public static final String JCR_SUPERTYPES = "jcr:supertypes";
    /**
     * jcr:system
     */
    public static final String JCR_SYSTEM = "jcr:system";
    /**
     * jcr:uuid
     */
    public static final String JCR_UUID = "jcr:uuid";
    /**
     * jcr:valueConstraints
     */
    public static final String JCR_VALUECONSTRAINTS = "jcr:valueConstraints";
    /**
     * jcr:versionHistory
     */
    public static final String JCR_VERSIONHISTORY = "jcr:versionHistory";
    /**
     * jcr:versionLabels
     */
    public static final String JCR_VERSIONLABELS = "jcr:versionLabels";
    /**
     * jcr:versionStorage
     */
    public static final String JCR_VERSIONSTORAGE = "jcr:versionStorage";
    /**
     * jcr:versionableUuid
     */
    public static final String JCR_VERSIONABLEUUID = "jcr:versionableUuid";

    /**
     * Pseudo property jcr:path used with query results
     */
    public static final String JCR_PATH = "jcr:path";
    /**
     * Pseudo property jcr:score used with query results
     */
    public static final String JCR_SCORE = "jcr:score";

    /**
     * mix:lockable
     */
    public static final String MIX_LOCKABLE = "mix:lockable";
    /**
     * mix:referenceable
     */
    public static final String MIX_REFERENCEABLE = "mix:referenceable";
    /**
     * mix:versionable
     */
    public static final String MIX_VERSIONABLE = "mix:versionable";
    /**
     * nt:base
     */
    public static final String NT_BASE = "nt:base";
    /**
     * nt:childNodeDefinition
     */
    public static final String NT_CHILDNODEDEFINITION = "nt:childNodeDefinition";
    /**
     * nt:file
     */
    public static final String NT_FILE = "nt:file";
    /**
     * nt:folder
     */
    public static final String NT_FOLDER = "nt:folder";
    /**
     * nt:frozenNode
     */
    public static final String NT_FROZENNODE = "nt:frozenNode";
    /**
     * nt:hierarchyNode
     */
    public static final String NT_HIERARCHYNODE = "nt:hierarchyNode";
    /**
     * nt:linkedFile
     */
    public static final String NT_LINKEDFILE = "nt:linkedFile";
    /**
     * nt:nodeType
     */
    public static final String NT_NODETYPE = "nt:nodeType";
    /**
     * nt:propertyDefinition
     */
    public static final String NT_PROPERTYDEFINITION = "nt:propertyDefinition";
    /**
     * nt:query
     */
    public static final String NT_QUERY = "nt:query";
    /**
     * nt:resource
     */
    public static final String NT_RESOURCE = "nt:resource";
    /**
     * nt:unstructured
     */
    public static final String NT_UNSTRUCTURED = "nt:unstructured";
    /**
     * nt:version
     */
    public static final String NT_VERSION = "nt:version";
    /**
     * nt:versionHistory
     */
    public static final String NT_VERSIONHISTORY = "nt:versionHistory";
    /**
     * nt:versionLabels
     */
    public static final String NT_VERSIONLABELS = "nt:versionLabels";
    /**
     * nt:versionedChild
     */
    public static final String NT_VERSIONEDCHILD = "nt:versionedChild";
    
    public static final String MIX_SAKAIPROPERTIES = "sakaijcr:properties-mix";
}
