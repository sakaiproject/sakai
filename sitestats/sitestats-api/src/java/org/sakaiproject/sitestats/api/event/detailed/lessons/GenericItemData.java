/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.api.event.detailed.lessons;

/**
 * Catch-all generic item data object, should be eventually phased out once all item types are explicitly handled
 * @author bjones86
 * @author plukasew
 */
public class GenericItemData implements LessonsData
{
    // Member variables
    public final String title;
    public final PageData parentPage;

    public static final DeletedItem DELETED_ITEM = new GenericItemData.DeletedItem();

    /**
     * Constructor
     * @param title the title of the item
     * @param parentPage the page the item is on
     */
    public GenericItemData( String title, PageData parentPage )
    {
        this.title          = title;
        this.parentPage     = parentPage;
    }

	// a deleted item (no further info is available)
	public static final class DeletedItem implements LessonsData { }
}
