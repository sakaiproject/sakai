/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.mvc;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.poll.api.importformat.PollImportCsvFormat;
import org.sakaiproject.poll.api.service.PollImportException;
import org.sakaiproject.poll.api.service.PollsService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.springframework.context.MessageSource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping
@Slf4j
public class PollImportController {

    private static final long MAX_IMPORT_FILE_BYTES = 1024 * 1024;

    private final MessageSource messageSource;
    private final ToolManager toolManager;
    private final SessionManager sessionManager;
    private final PollsService pollsService;
    private final UserDirectoryService userDirectoryService;

    public PollImportController(MessageSource messageSource,
                                ToolManager toolManager,
                                SessionManager sessionManager,
                                PollsService pollsService,
                                UserDirectoryService userDirectoryService) {
        this.messageSource = messageSource;
        this.toolManager = toolManager;
        this.sessionManager = sessionManager;
        this.pollsService = pollsService;
        this.userDirectoryService = userDirectoryService;
    }

    @GetMapping("/pollImport")
    public String showImport(Model model, Locale locale) {
        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        if (!pollsService.isAllowedPollAdd(currentSiteId)) {
            return "redirect:/votePolls";
        }

        populateModel(model, locale);
        return "polls/import";
    }

    @GetMapping("/pollImport/sample")
    public Object downloadSample(Locale locale) {
        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        if (!pollsService.isAllowedPollAdd(currentSiteId)) {
            return "redirect:/votePolls";
        }

        String csv = PollImportCsvFormat.buildSampleCsv(buildImportColumnHeaders(locale));
        String filename = messageSource.getMessage("poll_import_sample_filename", null, locale);

        ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping(value = "/pollImport", consumes = "multipart/form-data")
    public String importPolls(@RequestParam(required = false) String pollUploadedText,
                              @RequestParam(value = "pollUploadFile", required = false) MultipartFile pollUploadFile,
                              RedirectAttributes redirectAttributes,
                              Locale locale,
                              Model model) {
        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        if (!pollsService.isAllowedPollAdd(currentSiteId)) {
            redirectAttributes.addFlashAttribute("alert", messageSource.getMessage("new_poll_noperms", null, locale));
            return "redirect:/votePolls";
        }

        try {
            String uploadedFileText = readUploadedFile(pollUploadFile, locale);
            if (StringUtils.isAllBlank(pollUploadedText, uploadedFileText)) {
                return showImportError(model, messageSource.getMessage("poll_import_error_inputrequired", null, locale), pollUploadedText, locale);
            }

            List<String> contents = new ArrayList<>();
            if (StringUtils.isNotBlank(pollUploadedText)) {
                contents.add(pollUploadedText);
            }
            if (StringUtils.isNotBlank(uploadedFileText)) {
                contents.add(uploadedFileText);
            }

            String currentUserId = sessionManager.getCurrentSessionUserId();

            pollsService.importPollsFromCsv(contents, currentSiteId, currentUserId);

            redirectAttributes.addFlashAttribute("success", messageSource.getMessage("poll_import_success", null, locale));
            return "redirect:/votePolls";
        } catch (PollImportException e) {
            return showImportError(model, messageSource.getMessage(e.getError().getMessageKey(), null, locale), pollUploadedText, locale);
        } catch (IllegalArgumentException e) {
            return showImportError(model, e.getMessage(), pollUploadedText, locale);
        }
    }

    private void populateModel(Model model, Locale locale) {
        String currentSiteId = toolManager.getCurrentPlacement().getContext();
        model.addAttribute("canAdd", pollsService.isAllowedPollAdd(currentSiteId));
        model.addAttribute("isSiteOwner", pollsService.isSiteOwner(currentSiteId));
        List<PollGroupInfo> groups = pollsService.getSiteGroups(currentSiteId).stream()
                .map(group -> toGroupInfo(group, locale))
                .toList();
        model.addAttribute("groups", groups);
        model.addAttribute("importExample", PollImportCsvFormat.buildSampleCsv(buildImportColumnHeaders(locale)));
    }

    private PollGroupInfo toGroupInfo(Group group, Locale locale) {
        List<String> memberUserIds = group.getMembers().stream()
                .map(member -> member.getUserId())
                .toList();
        List<User> members = new ArrayList<>(userDirectoryService.getUsers(memberUserIds));
        Collections.sort(members, new UserSortNameComparator(locale));
        StringJoiner joiner = new StringJoiner(", ");
        members.forEach(user -> joiner.add(user.getDisplayName()));
        return new PollGroupInfo(group.getTitle(), joiner.toString());
    }

    private List<String> buildImportColumnHeaders(Locale locale) {
        return PollImportCsvFormat.buildColumnHeaders(
                key -> messageSource.getMessage(key, null, locale));
    }

    private String showImportError(Model model, String errorMessage, String pollUploadedText, Locale locale) {
        model.addAttribute("errorMessage", errorMessage);
        populateModel(model, locale);
        model.addAttribute("pollUploadedText", pollUploadedText);
        return "polls/import";
    }

    private String readUploadedFile(MultipartFile file, Locale locale) {
        if (file == null || file.isEmpty()) {
            return StringUtils.EMPTY;
        }

        if (file.getSize() > MAX_IMPORT_FILE_BYTES) {
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_file", null, locale));
        }

        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                builder.append(buffer, 0, count);
            }
            return builder.toString();
        } catch (IOException e) {
            log.warn("Unable to read imported poll file {}", file.getOriginalFilename(), e);
            throw new IllegalArgumentException(messageSource.getMessage("poll_import_error_file", null, locale), e);
        }
    }

    private record PollGroupInfo(String title, String members) { }
}
