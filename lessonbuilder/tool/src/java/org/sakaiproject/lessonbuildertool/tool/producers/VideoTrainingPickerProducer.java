/**
 * $URL: $
 * $Id: $
 *
 * Copyright (c) 2024 Sakai Foundation
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

package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.List;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.service.VideoTrainingEntity;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Creates a list of Video Training Module videos for the user to choose from.
 * Their choice will be added to the end of the list of items on this page.
 * 
 * @author Sakai Team
 */
public class VideoTrainingPickerProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {
	
	public static final String VIEW_ID = "VideoTrainingPicker";

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private VideoTrainingEntity videoTrainingEntity;
	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public void setVideoTrainingEntity(VideoTrainingEntity videoTrainingEntity) {
		this.videoTrainingEntity = videoTrainingEntity;
	}

	public String getViewID() {
		return VIEW_ID;
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		if (((GeneralViewParameters) viewparams).getSendingPage() != -1) {
			try {
				simplePageBean.updatePageObject(((GeneralViewParameters) viewparams).getSendingPage());
			} catch (Exception e) {
				return;
			}
		}

		UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
			.decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));

		Long itemId = ((GeneralViewParameters) viewparams).getItemId();
		simplePageBean.setItemId(itemId);

		UIForm form = UIForm.make(tofill, "video-training-picker");
		Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
		if (sessionToken != null) {
			UIInput.make(form, "csrf", "simplePageBean.csrfToken", sessionToken.toString());
		}

		if (simplePageBean == null || videoTrainingEntity == null) {
			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.video-training.not.available"));
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
			return;
		}

		if (simplePageBean.canEditPage()) {

			SimplePage page = simplePageBean.getCurrentPage();
			String currentItem = null;

			if (itemId != null && itemId != -1) {
				SimplePageItem i = simplePageToolDao.findItem(itemId);
				if (i == null) {
					return;
				}
				if (i.getPageId() != page.getPageId()) {
					return;
				}
				currentItem = i.getSakaiId();
			}

			List<LessonEntity> videos = videoTrainingEntity.getEntitiesInSite(simplePageBean);

			if (videos == null || videos.isEmpty()) {
				UIOutput.make(tofill, "error-div");
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.video-training.no-videos"));
				UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
				return;
			}

			// If addVideoTraining set duplicatedSakaiId and return to the list,
			// show a warning message in the picker page instead of an error.
			if (simplePageBean.isSkipDuplicateCheck()) {
				UIOutput.make(tofill, "warning-div");

				String duplicatedId = simplePageBean.getDuplicatedSakaiId();
				String duplicatedTitle = videos.stream()
					.filter(v -> v.getReference().equals(duplicatedId))
					.map(LessonEntity::getTitle)
					.findFirst()
					.orElse(duplicatedId);

				String msg = messageLocator.getMessage("simplepage.duplicate.item",
					new Object[] {duplicatedTitle});

				UIOutput.make(tofill, "warning", msg);
			}

			ArrayList<String> values = new ArrayList<String>();
			for (LessonEntity video : videos) {
				values.add(video.getReference());
			}

			if (simplePageBean != null && simplePageBean.isSkipDuplicateCheck() && simplePageBean.getDuplicatedSakaiId() != null) {
				String dup = simplePageBean.getDuplicatedSakaiId();
				if (values.contains(dup)) {
					currentItem = dup;
				}
			}

			if (currentItem == null && !values.isEmpty()) {
				currentItem = values.get(0);
			}

			UISelect select = UISelect.make(form, "video-span", values.toArray(new String[1]), "#{simplePageBean.selectedEntity}", currentItem);

			for (LessonEntity video : videos) {
				int index = videos.indexOf(video);
				UIBranchContainer row = UIBranchContainer.make(form, "video:", String.valueOf(index));
				UISelectChoice.make(row, "select", select.getFullID(), index);

				String finalUrl = video.getUrl();

				if (video instanceof VideoTrainingEntity) {
					VideoTrainingEntity videoTraining = (VideoTrainingEntity) video;
					finalUrl = videoTraining.getPortalUrl();
					String thumbnailUrl = videoTraining.getThumbnailUrl();
                    if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                        boolean isHlsJpg = thumbnailUrl.contains("thumbnail.jpg");
                        boolean isVideoThumb = videoTraining.isThumbnailVideo() && !isHlsJpg;

                        if (isVideoThumb) {
                            UIOutput.make(row, "thumbnail-video", "").decorate(new UIFreeAttributeDecorator("src", thumbnailUrl))
                                .decorate(new UIFreeAttributeDecorator("style", "display:block;"));
                            UIOutput.make(row, "thumbnail-image", "").decorate(new UIFreeAttributeDecorator("style", "display:none;"));
                        } else {
                            UIOutput.make(row, "thumbnail-image", "").decorate(new UIFreeAttributeDecorator("src", thumbnailUrl))
                                .decorate(new UIFreeAttributeDecorator("alt", video.getTitle()))
                                .decorate(new UIFreeAttributeDecorator("style", "display:block;"));
                            UIOutput.make(row, "thumbnail-video", "").decorate(new UIFreeAttributeDecorator("style", "display:none;"));
                        }
                        UIOutput.make(row, "thumbnail-placeholder", "").decorate(new UIFreeAttributeDecorator("style", "display:none;"));
                    } else {
                        UIOutput.make(row, "thumbnail-placeholder", "").decorate(new UIFreeAttributeDecorator("style", "display:flex;"));
                        UIOutput.make(row, "thumbnail-video", "").decorate(new UIFreeAttributeDecorator("style", "display:none;"));
                        UIOutput.make(row, "thumbnail-image", "").decorate(new UIFreeAttributeDecorator("style", "display:none;"));
                    }
				}

				UILink.make(row, "link", video.getTitle(), finalUrl);
			}

			UIInput.make(form, "item-id", "#{simplePageBean.itemId}");
			UIInput.make(form, "add-before", "#{simplePageBean.addBefore}", ((GeneralViewParameters) viewparams).getAddBefore());

			UICommand.make(form, "submit", messageLocator.getMessage("simplepage.chooser.select"), "#{simplePageBean.addVideoTraining}");
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancelVideoTraining}");
		} else {
			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.permissions.general")).
				decorate(new UIFreeAttributeDecorator("role", "alert"));
			UICommand.make(form, "cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
		}
	}


	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> nav = new ArrayList<NavigationCase>();
		nav.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		nav.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		nav.add(new NavigationCase("failure", new SimpleViewParameters(VideoTrainingPickerProducer.VIEW_ID)));
		nav.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return nav;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}
}
