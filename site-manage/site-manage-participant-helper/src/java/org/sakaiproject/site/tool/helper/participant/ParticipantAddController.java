/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.site.tool.helper.participant.impl.ParticipantDisplayResolver;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantMessage;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantNotificationOption;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantRoleMode;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantStatus;
import org.sakaiproject.site.tool.helper.participant.impl.ParticipantWizardSnapshot;
import org.sakaiproject.site.tool.helper.participant.impl.SiteAddParticipantHandler;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import lombok.Getter;
import lombok.Setter;

/** HTTP boundary for the server-owned participant-add wizard. */
@Controller
public class ParticipantAddController {

    private final SiteAddParticipantHandler handler;
    private final ParticipantDisplayResolver displayResolver;
    private final MessageSource messageSource;

    public ParticipantAddController(SiteAddParticipantHandler handler, ParticipantDisplayResolver displayResolver,
            MessageSource messageSource) {
        this.handler = handler;
        this.displayResolver = displayResolver;
        this.messageSource = messageSource;
    }

    /** Restores the request-scoped operation before every wizard endpoint. */
    @ModelAttribute
    public void initializeOperation() {
        handler.beginStep();
    }

    @GetMapping("/")
    public String startNewOperation() {
        handler.startNewOperation();
        return "redirect:/add";
    }

    @GetMapping("/add")
    public String add(Model model) {
        model.addAttribute("addForm", createAddForm(handler.snapshot()));
        return renderAdd(model);
    }

    @PostMapping("/add")
    public String submitAdd(@ModelAttribute AddForm form, Model model) {
        if (handler.submitAdd(form.getCsrfToken(), form.getOfficialAccountParticipant(),
                form.getNonOfficialAccountParticipant(), ParticipantStatus.fromFormValue(form.getStatusChoice()))) {
            return "redirect:/roles";
        }
        model.addAttribute("addForm", createAddForm(handler.snapshot()));
        return renderAdd(model);
    }

    @GetMapping("/roles")
    public String roles(Model model) {
        if (!handler.hasParticipants()) {
            return "redirect:/add";
        }
        ParticipantWizardSnapshot snapshot = handler.snapshot();
        RoleForm form = new RoleForm();
        form.setCsrfToken(handler.getCsrfToken());
        form.setRoleChoice(snapshot.roleChoice());
        form.setSameRoleChoice(snapshot.sameRoleChoice());
        form.setIndividualRoles(snapshot.participants().stream().map(participant -> participant.getRole()).toList());
        model.addAttribute("roleForm", form);
        return renderRoles(model, snapshot);
    }

    @PostMapping("/roles")
    public String submitRoles(@ModelAttribute RoleForm form, Model model) {
        if (!handler.hasParticipants()) {
            return "redirect:/add";
        }
        if (handler.submitRoles(form.getCsrfToken(), ParticipantRoleMode.fromFormValue(form.getRoleChoice()), form.getSameRoleChoice(),
                form.getIndividualRoles())) {
            return "redirect:/confirm";
        }
        model.addAttribute("roleForm", form);
        return renderRoles(model, handler.snapshot());
    }

    @PostMapping("/roles/back")
    public String backFromRoles() {
        handler.clearStepMessages();
        return "redirect:/add";
    }

    @GetMapping("/confirm")
    public String confirm(Model model) {
        if (!handler.hasParticipants()) {
            return "redirect:/add";
        }
        ParticipantWizardSnapshot snapshot = handler.snapshot();
        ConfirmForm form = new ConfirmForm();
        form.setCsrfToken(handler.getCsrfToken());
        form.setEmailNotiChoice(snapshot.emailNotiChoice());
        model.addAttribute("confirmForm", form);
        return renderConfirm(model, snapshot);
    }

    @PostMapping("/confirm")
    public Object submitConfirm(@ModelAttribute ConfirmForm form, Model model) {
        if (handler.finish(form.getCsrfToken(), ParticipantNotificationOption.fromFormValue(form.getEmailNotiChoice()))) {
            return doneRedirect(handler.getDoneUrl());
        }
        model.addAttribute("confirmForm", form);
        return renderConfirm(model, handler.snapshot());
    }

    @PostMapping("/confirm/back")
    public String backFromConfirm(@ModelAttribute ConfirmForm form) {
        handler.saveNotificationChoice(form.getCsrfToken(),
                ParticipantNotificationOption.fromFormValue(form.getEmailNotiChoice()));
        handler.clearStepMessages();
        return "redirect:/roles";
    }

    @PostMapping("/cancel")
    public RedirectView cancel() {
        return doneRedirect(handler.cancel());
    }

    private String renderAdd(Model model) {
        model.addAttribute("allowAddParticipant", handler.canAddParticipant());
        model.addAttribute("allowNonOfficialAccount", handler.allowsNonOfficialAccounts());
        model.addAttribute("showStatusChoice", handler.showsStatusChoice());
        model.addAttribute("showCourseInstructions", handler.showsCourseInstructions());
        return render(model, "add", 1);
    }

    private AddForm createAddForm(ParticipantWizardSnapshot snapshot) {
        AddForm form = new AddForm();
        form.setCsrfToken(handler.getCsrfToken());
        form.setOfficialAccountParticipant(snapshot.officialAccountParticipant());
        form.setNonOfficialAccountParticipant(snapshot.nonOfficialAccountParticipant());
        form.setStatusChoice(snapshot.statusChoice());
        return form;
    }

    private String renderRoles(Model model, ParticipantWizardSnapshot snapshot) {
        model.addAttribute("roles", handler.getRoles());
        model.addAttribute("participants", snapshot.participants());
        model.addAttribute("participantDisplays", displayResolver.displays(snapshot.participants()));
        return render(model, "roles", 2);
    }

    private String renderConfirm(Model model, ParticipantWizardSnapshot snapshot) {
        model.addAttribute("participants", snapshot.participants());
        model.addAttribute("active", snapshot.active());
        return render(model, "confirm", 3);
    }

    private String render(Model model, String view, int step) {
        model.addAttribute("siteTitle", handler.getSiteTitle());
        model.addAttribute("step", step);
        List<ParticipantMessageView> messages = handler.getMessages().stream()
                .map(message -> new ParticipantMessageView(
                        messageSource.getMessage(message.getCode(), message.getArgs(), LocaleContextHolder.getLocale()),
                        message.getSeverity()))
                .toList();
        model.addAttribute("messages", messages);
        return view;
    }

    private RedirectView doneRedirect(String doneUrl) {
        RedirectView redirectView = new RedirectView(doneUrl, false);
        redirectView.setExposeModelAttributes(false);
        return redirectView;
    }

    public record ParticipantMessageView(String text, ParticipantMessage.Severity severity) {

        public String bannerClass() {
            return switch (severity) {
                case ERROR -> "sak-banner-error";
                case WARNING -> "sak-banner-warn";
                case INFO -> "sak-banner-info";
            };
        }
    }

    @Getter
    @Setter
    public static class AddForm {
        private String csrfToken;
        private String officialAccountParticipant;
        private String nonOfficialAccountParticipant;
        private String statusChoice = "active";
    }

    @Getter
    @Setter
    public static class RoleForm {
        private String csrfToken;
        private String roleChoice = "sameRole";
        private String sameRoleChoice;
        private List<String> individualRoles = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class ConfirmForm {
        private String csrfToken;
        private String emailNotiChoice = Boolean.FALSE.toString();
    }
}
