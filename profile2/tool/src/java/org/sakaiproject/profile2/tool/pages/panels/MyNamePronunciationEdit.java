package org.sakaiproject.profile2.tool.pages.panels;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.profile2.logic.ProfileLogic;
import org.sakaiproject.profile2.logic.ProfileWallLogic;
import org.sakaiproject.profile2.logic.SakaiProxy;
import org.sakaiproject.profile2.model.UserProfile;
import org.sakaiproject.profile2.util.ProfileConstants;

@Slf4j
public class MyNamePronunciationEdit extends Panel {

    @SpringBean(name="org.sakaiproject.profile2.logic.SakaiProxy")
    private SakaiProxy sakaiProxy;

    @SpringBean(name="org.sakaiproject.profile2.logic.ProfileWallLogic")
    private ProfileWallLogic wallLogic;

    @SpringBean(name="org.sakaiproject.profile2.logic.ProfileLogic")
    private ProfileLogic profileLogic;

    private final HiddenField audioBase64;

    public MyNamePronunciationEdit(final String id, final UserProfile userProfile) {
        super(id);

        log.debug("MyNamePronunciationEdit()");

        final Component thisPanel = this;
        final String userId = userProfile.getUserUuid();

        //heading
        add(new Label("heading", new ResourceModel("heading.name.pronunciation.edit")));

        //setup form
        Form form = new Form("form", new Model(userProfile));
        form.setOutputMarkupId(true);

        //form submit feedback
        final Label formFeedback = new Label("formFeedback");
        formFeedback.setOutputMarkupPlaceholderTag(true);
        form.add(formFeedback);

        //add warning message if superUser and not editing own profile
        Label editWarning = new Label("editWarning");
        editWarning.setVisible(false);
        if(sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
            editWarning.setDefaultModel(new StringResourceModel("text.edit.other.warning", null, new Object[]{userProfile.getDisplayName()}));
            editWarning.setEscapeModelStrings(false);
            editWarning.setVisible(true);
        }
        form.add(editWarning);

        //phoneticPronunciation
        WebMarkupContainer phoneticContainer = new WebMarkupContainer("phoneticContainer");
        phoneticContainer.add(new Label("phoneticLabel", new ResourceModel("profile.phonetic")));
        TextField phonetic = new TextField("phoneticPronunciation", new PropertyModel(userProfile, "phoneticPronunciation"));
        phonetic.setOutputMarkupId(true);
        phoneticContainer.add(phonetic);
        form.add(phoneticContainer);

        //pronunciationExamples
        WebMarkupContainer pronunciationExamples = new WebMarkupContainer("pronunciationExamples");
        String href = sakaiProxy.getNamePronunciationExamplesLink();
        if (StringUtils.isBlank(href)) {
            WebMarkupContainer examplesLink = new WebMarkupContainer("examplesLink");
            examplesLink.setVisible(false);
            pronunciationExamples.add(examplesLink);
        } else {
            ExternalLink examplesLink = new ExternalLink("examplesLink", href, new ResourceModel("profile.phonetic.examples.link.label").getObject());
            examplesLink.add(new AttributeModifier("target", "_blank"));
            pronunciationExamples.add(examplesLink);
        }
        form.add(pronunciationExamples);

        //nameRecording
        WebMarkupContainer nameRecordingContainer = new WebMarkupContainer("nameRecordingContainer");
        nameRecordingContainer.add(new Label("nameRecordingLabel", new ResourceModel("profile.name.recording")));
        audioBase64 = new HiddenField("audioBase64", Model.of());
        nameRecordingContainer.add(audioBase64);
        form.add(nameRecordingContainer);

        //audioPlayer
        WebMarkupContainer audioPlayer = new WebMarkupContainer("audioPlayer");
        final String slash = Entity.SEPARATOR;
        final StringBuilder path = new StringBuilder();
        path.append(slash);
        path.append("direct");
        path.append(slash);
        path.append("profile");
        path.append(slash);
        path.append(userProfile.getUserUuid());
        path.append(slash);
        path.append("pronunciation");
        path.append("?v=");
        path.append(RandomStringUtils.random(8, true, true));
        audioPlayer.add(new AttributeModifier("src", path.toString()));
        if (profileLogic.getUserNamePronunciation(userProfile.getUserUuid()) == null) audioPlayer.setVisible(false);
        nameRecordingContainer.add(audioPlayer);

        Label namePronunciationDuration = new Label("namePronunciationDuration", sakaiProxy.getNamePronunciationDuration());
        form.add(namePronunciationDuration);

        //Delete recording link
        AjaxLink clearRecordingLink = new AjaxLink("clearExistingRecordingLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                String userId = sakaiProxy.getCurrentUserId();
                String path = profileLogic.getUserNamePronunciationResourceId(userId);
                sakaiProxy.removeResource(path);
                log.info("Pronunciation recording removed for the user {}. ", userId );
                //repaint panel
                Component newPanel = new MyNamePronunciationDisplay(id, userProfile);
                newPanel.setOutputMarkupId(true);
                thisPanel.replaceWith(newPanel);
                if(target != null) {
                    target.add(newPanel);
                    target.appendJavaScript("setMainFrameHeight(window.name);");
                }
            }

            @Override
            protected void updateAjaxAttributes( AjaxRequestAttributes attributes )
            {
                super.updateAjaxAttributes( attributes );
         
                AjaxCallListener ajaxCallListener = new AjaxCallListener();
                ajaxCallListener.onPrecondition( "return confirm('" + new ResourceModel("profile.phonetic.clear.confirmation").getObject() + "');" );
                attributes.getAjaxCallListeners().add( ajaxCallListener );
            }
        };

        if (profileLogic.getUserNamePronunciation(userProfile.getUserUuid()) == null) clearRecordingLink.setVisible(false);
        clearRecordingLink.add(new Label("clearExistingRecordingLabel", new ResourceModel("profile.phonetic.clear.recording.label")));
        clearRecordingLink.add(new AttributeModifier("title",  new ResourceModel("profile.phonetic.clear.recording.label")));
        nameRecordingContainer.add(clearRecordingLink);

        //submit button
        AjaxFallbackButton submitButton = new AjaxFallbackButton("submit", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                if(save(form)) {
                    //post update event
                    sakaiProxy.postEvent(ProfileConstants.EVENT_PROFILE_NAME_PRONUN_UPDATE, "/profile/"+userId, true);

                    //post to wall if enabled
                    if (sakaiProxy.isWallEnabledGlobally() && !sakaiProxy.isSuperUserAndProxiedToUser(userId)) {
                        wallLogic.addNewEventToWall(ProfileConstants.EVENT_PROFILE_NAME_PRONUN_UPDATE, sakaiProxy.getCurrentUserId());
                    }

                    //repaint panel
                    Component newPanel = new MyNamePronunciationDisplay(id, userProfile);
                    newPanel.setOutputMarkupId(true);
                    thisPanel.replaceWith(newPanel);
                    if(target != null) {
                        target.add(newPanel);
                        target.appendJavaScript("setMainFrameHeight(window.name);");
                    }

                } else {
                    formFeedback.setDefaultModel(new ResourceModel("error.profile.save.info.failed"));
                    formFeedback.add(new AttributeModifier("class", true, new Model<>("save-failed-error")));
                    target.add(formFeedback);
                }
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes){
                super.updateAjaxAttributes(attributes);
                AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
                    @Override
                    public CharSequence getBeforeHandler(Component component) {
                        return "doUpdateCK()";
                    }
                };
                attributes.getAjaxCallListeners().add(myAjaxCallListener);
            }
        };
        form.add(submitButton);

        //cancel button
        AjaxFallbackButton cancelButton = new AjaxFallbackButton("cancel", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form form) {
                Component newPanel = new MyNamePronunciationDisplay(id, userProfile);
                newPanel.setOutputMarkupId(true);
                thisPanel.replaceWith(newPanel);
                if(target != null) {
                    target.add(newPanel);
                    target.appendJavaScript("setMainFrameHeight(window.name);");
                }
            }
        };
        cancelButton.setDefaultFormProcessing(false);
        form.add(cancelButton);

        //add form to page
        add(form);
    }

    private boolean save(Form form) {
        //get the backing model
        UserProfile userProfile = (UserProfile) form.getModelObject();

        //get userId from the UserProfile (because admin could be editing), then get existing SakaiPerson for that userId
        String userId = userProfile.getUserUuid();
        SakaiPerson sakaiPerson = sakaiProxy.getSakaiPerson(userId);

        sakaiPerson.setPhoneticPronunciation(userProfile.getPhoneticPronunciation());
        if (audioBase64.getDefaultModelObject() != null) {
            String audioStr = audioBase64.getDefaultModelObject().toString().split(",")[1];
            String path = profileLogic.getUserNamePronunciationResourceId(userId);
            byte[] bytes = Base64.getDecoder().decode(audioStr.getBytes(StandardCharsets.UTF_8));
            sakaiProxy.removeResource(path);
            sakaiProxy.saveFile(path, userId, userId+".ogg", "audio/ogg", bytes);
        }

        if(profileLogic.saveUserProfile(sakaiPerson)) {
            log.info("Saved SakaiPerson for: " + userId );
            return true;
        } else {
            log.info("Couldn't save SakaiPerson for: " + userId);
            return false;
        }
    }
}
