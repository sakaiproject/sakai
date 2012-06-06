/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.pages;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.components.ErrorLevelsFeedbackMessageFilter;
import org.sakaiproject.profile2.tool.components.GalleryImageRenderer;

/**
 * View-only version of MyPicture.
 */
public class ViewPicture extends BasePage {

	public ViewPicture(GalleryImage galleryImage) {
		configureFeedback();

		Label galleryImageHeading = new Label("galleryImageHeading",
				new Model<String>(galleryImage.getDisplayName()));
		add(galleryImageHeading);

		Form galleryImageForm = new Form("galleryImageForm");
		galleryImageForm.setOutputMarkupId(true);
		add(galleryImageForm);

		GalleryImageRenderer galleryImageRenderer = new GalleryImageRenderer(
				"galleryImageRenderer", galleryImage.getMainResource());
		galleryImageForm.add(galleryImageRenderer);
	}
	
	private void configureFeedback() {

		// activate feedback panel
		final FeedbackPanel feedbackPanel = new FeedbackPanel("feedback");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setVisible(false);
		
		add(feedbackPanel);

		// don't show filtered feedback errors in feedback panel
		int[] filteredErrorLevels = new int[] { FeedbackMessage.ERROR };
		feedbackPanel.setFilter(new ErrorLevelsFeedbackMessageFilter(
				filteredErrorLevels));
	}
}
