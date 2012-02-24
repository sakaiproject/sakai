/**
 * $Id$
 * $URL$
 * RESTfulEntityProviderMock.java - entity-broker - Apr 9, 2008 10:31:13 AM - azeckoski
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

package org.sakaiproject.entitybroker.mocks;

import java.util.HashMap;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CRUDable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.OutputSerializable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;


/**
 * Stub class to make it possible to test the {@link OutputSerializable} capabilities, will perform like the
 * actual class so it can be reliably used for testing<br/> 
 * Will perform all {@link CRUDable} operations<br/>
 * Returns {@link MyEntity} objects<br/>
 * Allows for testing {@link Resolvable} and {@link CollectionResolvable} as well, returns 3 {@link MyEntity} objects 
 * if no search restrictions, 1 if "stuff" property is set, none if other properties are set
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SerializeableEntityProviderMock extends CRUDableEntityProviderMock implements OutputSerializable {

    public SerializeableEntityProviderMock(String prefix, String[] ids) {
        super(prefix, ids);
    }

    public Object makeSerializableObject(EntityReference ref, Object entity) {
        MyEntity me = (MyEntity) entity;
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("identity", me.getId());
        map.put("stuff", me.getStuff());
        map.put("xtra", me.extra);
        map.put("name", "aaronz");
        return map;
    }

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }

}
