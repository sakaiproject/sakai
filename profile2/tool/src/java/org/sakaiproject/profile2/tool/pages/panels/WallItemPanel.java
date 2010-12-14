package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.model.WallItem;
import org.sakaiproject.profile2.tool.components.ProfileImageRenderer;
import org.sakaiproject.profile2.tool.pages.ViewProfile;
import org.sakaiproject.profile2.util.ProfileUtils;

/**
 * Wall item container.
 */
public class WallItemPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	public WallItemPanel(String id, WallItem wallItem) {
		super(id);

		// image wrapper, links to profile
		Link<String> wallItemPhoto = new Link<String>(
				"wallItemPhotoWrap", new Model<String>(wallItem
						.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}
		};

		// image
		wallItemPhoto.add(new ProfileImageRenderer("wallItemPhoto",
				wallItem.getCreatorUuid()));
		add(wallItemPhoto);

		// name and link to profile
		Link<String> wallItemProfileLink = new Link<String>(
				"wallItemProfileLink", new Model<String>(wallItem
						.getCreatorUuid())) {

			private static final long serialVersionUID = 1L;

			public void onClick() {
				setResponsePage(new ViewProfile(getModelObject()));
			}

		};
		wallItemProfileLink.add(new Label("wallItemName", wallItem
				.getCreatorName()));
		add(wallItemProfileLink);

		add(new Label("wallItemText", wallItem.getText()));
		add(new Label("wallItemDate", ProfileUtils
				.convertDateToString(wallItem.getDate(),
						"dd MMMMM, kk:mm")));
	}

}
