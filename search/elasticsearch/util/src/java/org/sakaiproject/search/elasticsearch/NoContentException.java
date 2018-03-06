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
package org.sakaiproject.search.elasticsearch;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/13/13
 * Time: 9:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoContentException extends Exception {
    private String id;
    private String reference;
    private String siteId;
    public NoContentException(String id, String reference, String siteId) {
        this.id = id;
        this.reference = reference;
        this.siteId = siteId;
    }

    public String getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }
}
