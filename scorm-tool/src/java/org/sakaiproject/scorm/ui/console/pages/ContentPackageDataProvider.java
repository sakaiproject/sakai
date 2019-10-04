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
package org.sakaiproject.scorm.ui.console.pages;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.wicket.util.Utils;

/**
 * @author bjones86
 */
public class ContentPackageDataProvider extends SortableDataProvider<ContentPackage, String>
{
    private List<ContentPackage> contentPackages;

    public ContentPackageDataProvider( List<ContentPackage> contentPackages )
    {
        Objects.requireNonNull( contentPackages );
        this.contentPackages = contentPackages;
        setSort( "title", SortOrder.ASCENDING );
    }

    @Override
    public Iterator<? extends ContentPackage> iterator( long first, long count )
    {
        SortParam sort = getSort();
        Comparator propertyComparator = Utils.getPropertyComparator(sort);
        Collections.sort( contentPackages, (sort.isAscending() ? propertyComparator : propertyComparator.reversed()) );
        return contentPackages.subList( (int) first, (int) first + (int) count ).iterator();
    }

    @Override
    public long size()
    {
        return contentPackages.size();
    }

    @Override
    public IModel<ContentPackage> model( ContentPackage contentPackage )
    {
        return new Model( (Serializable) contentPackage );
    }
}
