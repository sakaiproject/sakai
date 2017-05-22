/**********************************************************************************
 *
 * Copyright (c) 2016 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tags.api;

/**
 * The interface for the tags service.
 */
public interface TagService {

    public void init();

    public void destroy();


    /**
     * Return the tags sub-service.
     */
    public Tags getTags();

    /**
     * Return the collections sub-service
     */
    public TagCollections getTagCollections();

    /**
     * Return an I18N translator for a given file and locale.
     */
    public I18n getI18n(ClassLoader loader, String resourceBase);

    /**
     * Return if the service is enabled or not.
     */
    public Boolean getServiceActive ();

    /**
     * Return the max size of the pages
     */
    public int getMaxPageSize();

}
