/**
 * Copyright (c) 2003-2020 The Apereo Foundation
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
/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.producers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.poll.logic.ExternalLogic;
import org.sakaiproject.poll.logic.PollListManager;
import org.sakaiproject.poll.model.Option;
import org.sakaiproject.poll.model.Poll;
import org.sakaiproject.poll.tool.constants.NavigationConstants;
import org.sakaiproject.poll.tool.params.OptionBatchViewParameters;
import org.sakaiproject.poll.tool.params.PollViewParameters;
import org.sakaiproject.poll.tool.params.VoteBean;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

@Slf4j
@Setter
public class PollOptionBatchProducer implements ViewComponentProducer,ViewParamsReporter, NavigationCaseReporter, ActionResultInterceptor {

    public static final String VIEW_ID = "pollOptionBatch";

    private VoteBean voteBean;
    private MessageLocator messageLocator;
    private LocaleGetter localeGetter;
    private PollListManager pollListManager;
    private TargettedMessageList targettedMessageList;
    private ExternalLogic externalLogic;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker arg2) {

        String locale = localeGetter.get().toString();
        Map<String, String> langMap = new HashMap<String, String>();
        langMap.put("lang", locale);
        langMap.put("xml:lang", locale);

        UIOutput.make(tofill, "polls-html", null).decorate(new UIFreeAttributeDecorator(langMap));

        // Menu links
        UIBranchContainer actions = UIBranchContainer.make(tofill,"actions:",Integer.toString(0));
        UIInternalLink.make(actions, NavigationConstants.NAVIGATE_MAIN, UIMessage.make(NavigationConstants.NAVIGATE_MAIN_MESSAGE), new SimpleViewParameters(PollToolProducer.VIEW_ID));
        if (this.isAllowedPollAdd()) {
            UIInternalLink.make(actions, NavigationConstants.NAVIGATE_ADD, UIMessage.make(NavigationConstants.NAVIGATE_ADD_MESSAGE), new PollViewParameters(AddPollProducer.VIEW_ID, "New 0"));
        }
        if (this.isSiteOwner()) {
            UIInternalLink.make(actions, NavigationConstants.NAVIGATE_PERMISSIONS, UIMessage.make(NavigationConstants.NAVIGATE_PERMISSIONS_MESSAGE), new SimpleViewParameters(PermissionsProducer.VIEW_ID));
        }

        if (targettedMessageList.size() > 0) {
            for (int i = 0; i < targettedMessageList.size(); i ++ ) {
                UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:");
                String output;
                if (targettedMessageList.messageAt(i).args != null ) {
                    output = messageLocator.getMessage(targettedMessageList.messageAt(i).acquireMessageCode(),targettedMessageList.messageAt(i).args[0]);
                } else {
                    output = messageLocator.getMessage(targettedMessageList.messageAt(i).acquireMessageCode());
                }
                UIOutput.make(errorRow,"error", output);
            }
        }

        Option option = new Option();
        Poll poll = null;
        OptionBatchViewParameters aivp = (OptionBatchViewParameters) viewparams;
        if (aivp.pollId != null) {
            option.setPollId(Long.valueOf(aivp.pollId));
            poll = pollListManager.getPollById(Long.valueOf(aivp.pollId));
        } else { 
            option.setPollId(voteBean.getPoll().getPollId());
        }
        UIMessage.make(tofill,"new-option-batch-title","new_option_batch_title");

        if (poll == null) {
            log.warn("no poll found");
            return;
        }

        UIOutput.make(tofill,"poll_text",poll.getText());
        UIOutput.make(tofill,"poll-question",messageLocator.getMessage("new_poll_question"));
        UIForm form = UIForm.make(tofill,"opt-form");
        UIOutput.make(form, "batchUploadLabel", messageLocator.getMessage("new_poll_option_batch_info"));
        UIOutput.make(form, "batchUpload");
        form.parameters.add(new UIELBinding("#{option.pollId}", poll.getPollId()));
        UICommand save =  UICommand.make(form, "submit-new-option-batch", messageLocator.getMessage("new_poll_submit"), "#{pollToolBean.processActionAddOptionBatch}");
        save.parameters.add(new UIELBinding("#{pollToolBean.submissionStatus}", "batch"));
        save.parameters.add(new UIELBinding("#{option.status}", "batch"));
        UICommand cancel = UICommand.make(form, "cancel",messageLocator.getMessage("new_poll_cancel"),"#{pollToolBean.cancel}");
        cancel.parameters.add(new UIELBinding("#{option.status}", "cancel"));

    }

    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("batch", new PollViewParameters(AddPollProducer.VIEW_ID)));
        togo.add(new NavigationCase("cancel", null));
        return togo;
    }

    public ViewParameters getViewParameters() {
        return new OptionBatchViewParameters();
    }

    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
        log.debug("checking interceptActionResult...");

        if (result.resultingView instanceof OptionBatchViewParameters) {
            OptionBatchViewParameters optvp = (OptionBatchViewParameters) result.resultingView;
            log.debug("OptionBatchViewParameters: {} ",  optvp.pollId);
            String retVal = (String) actionReturn;
            log.debug("retval is {}.", retVal);
            if (retVal == null) {
                return;
            }

            String viewId = AddPollProducer.VIEW_ID;
            result.resultingView = new PollViewParameters(viewId, optvp.pollId);
        }
    }

    private boolean isAllowedPollAdd() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation(PollListManager.PERMISSION_ADD, externalLogic.getCurrentLocationReference());
    }

    private boolean isSiteOwner() {
        return externalLogic.isUserAdmin() || externalLogic.isAllowedInLocation("site.upd", externalLogic.getCurrentLocationReference());
    }

}

