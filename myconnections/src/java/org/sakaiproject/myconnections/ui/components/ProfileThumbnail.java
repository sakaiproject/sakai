package org.sakaiproject.myconnections.ui.components;

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
