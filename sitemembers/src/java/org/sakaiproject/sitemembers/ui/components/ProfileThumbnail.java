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
package org.sakaiproject.sitemembers.ui.components;

import java.util.concurrent.TimeUnit;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

/**
 * Renders a user's profile thumbnail via the direct entity URL. The image is rendered as a background image so it can be styled easier (ie
 * made round).
 *
 * Attach to an 'a' tag and provide the user uuid in the model.
 *
 * e.g. <code>
 * &lt;a wicket:id="photo" /&gt;
 * </code> <br />
 * <code>
 * add(new ProfileThumbnail("photo", new Model<String>(userUuid)));
 * </code>
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */
public class ProfileThumbnail extends WebComponent {

    private static final long serialVersionUID = 1L;

    public ProfileThumbnail(final String id, final IModel<String> model) {
        super(id, model);
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);
        checkComponentTag(tag, "a");

        final String userUuid = this.getDefaultModelObjectAsString();

        // image url, cached for a minute
        final String imageUrl = "/direct/profile/" + userUuid + "/image/thumb" + "?t="
                + TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());

        // output image
        tag.put("style", "background-image:url(" + imageUrl + ")");
    }
}
