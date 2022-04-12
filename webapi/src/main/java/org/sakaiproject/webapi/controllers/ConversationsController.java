/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.controllers;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.conversations.api.ConversationsPermissionsException;
import org.sakaiproject.conversations.api.Permissions;
import org.sakaiproject.conversations.api.Reaction;
import org.sakaiproject.conversations.api.beans.CommentTransferBean;
import org.sakaiproject.conversations.api.beans.PostTransferBean;
import org.sakaiproject.conversations.api.beans.TopicTransferBean;
import org.sakaiproject.conversations.api.model.ConvStatus;
import org.sakaiproject.conversations.api.model.Settings;
import org.sakaiproject.conversations.api.model.Tag;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.webapi.beans.ConversationsRestBean;
import org.sakaiproject.webapi.beans.SimpleGroup;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 */
@Slf4j
@RestController
public class ConversationsController extends AbstractSakaiApiController {

	@Resource
	private ConversationsService conversationsService;

	@Resource
	private EntityManager entityManager;

	@Resource
	private SecurityService securityService;

	@Resource(name = "org.sakaiproject.component.api.ServerConfigurationService")
	private ServerConfigurationService serverConfigurationService;

	@Resource
	private SiteService siteService;

	@Resource
	private UserDirectoryService userDirectoryService;

	@GetMapping(value = "/sites/{siteId}/conversations", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel getSiteConversations(@PathVariable String siteId) throws ConversationsPermissionsException {

		String currentUserId = checkSakaiSession().getUserId();

        Site site;
        try {
            site = siteService.getSite(siteId);
            String siteRef = "/site/" + siteId;
            ConversationsRestBean bean = new ConversationsRestBean();
            bean.userId = currentUserId;
            bean.siteId = siteId;
            bean.groups = site.getGroups().stream().map(SimpleGroup::new).collect(Collectors.toList());
            bean.topics = conversationsService.getTopicsForSite(siteId).stream()
                .map(tb -> entityModelForTopicBean(tb)).collect(Collectors.toList());
            Settings settings = conversationsService.getSettingsForSite(siteId);
            bean.canUpdatePermissions = securityService.unlock(SiteService.SECURE_UPDATE_SITE, siteRef);

            if (!settings.getSiteLocked()
                || securityService.unlock(Permissions.MODERATE.label, siteRef)) {
                bean.canEditTags = securityService.unlock(Permissions.TAG_CREATE.label, siteRef);
                bean.canCreateTopic = securityService.unlock(Permissions.TOPIC_CREATE.label, siteRef);
            }
            bean.canViewSiteStatistics = securityService.unlock(Permissions.VIEW_STATISTICS.label, siteRef);
            bean.canPin = settings.getAllowPinning() && securityService.unlock(Permissions.TOPIC_PIN.label, siteRef);
            bean.isInstructor = securityService.unlock(Permissions.ROLETYPE_INSTRUCTOR.label, siteRef);
            bean.canViewAnonymous = securityService.unlock(Permissions.VIEW_ANONYMOUS.label, siteRef);
            bean.settings = settings;

            ConvStatus convStatus = conversationsService.getConvStatusForSiteAndUser(siteId, currentUserId);
            bean.showGuidelines = settings.getRequireGuidelinesAgreement() && !convStatus.getGuidelinesAgreed();
            bean.tags = conversationsService.getTagsForSite(siteId);

            List<Link> links = new ArrayList<>();
            if (bean.canViewSiteStatistics) links.add(Link.of("/api/sites/" + siteId + "/conversations/stats", "stats"));
            return EntityModel.of(bean, links);

        } catch (Exception e) {
            log.error("Failed to load data fully", e);
        }

        return null;
    }

	@PostMapping(value = "/sites/{siteId}/conversations/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getSiteStats(@PathVariable String siteId, @RequestBody Map<String, Object> options) throws ConversationsPermissionsException {

		checkSakaiSession();

        String interval = (String) options.get("interval");
        Instant from = interval.equals("WEEK") ? Instant.now().minus(7, ChronoUnit.DAYS) : null;
        Instant to = interval.equals("THIS_WEEK") ? Instant.now(): null;

        return conversationsService.getSiteStats(siteId, from, to, (Integer) options.get("page"), (String) options.get("sort"));
    }

	@PostMapping(value = "/sites/{siteId}/topics", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel createTopic(@PathVariable String siteId, @RequestBody TopicTransferBean topicBean) throws ConversationsPermissionsException {

		checkSakaiSession();

        topicBean.siteId = siteId;
        return entityModelForTopicBean(conversationsService.saveTopic(topicBean));
    }

	@PutMapping(value = "/sites/{siteId}/topics/{topicId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel updateTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody TopicTransferBean topicBean) throws ConversationsPermissionsException {

		checkSakaiSession();

        topicBean.id = topicId;
        topicBean.siteId = siteId;
        return entityModelForTopicBean(conversationsService.saveTopic(topicBean));
    }

	@DeleteMapping(value = "/sites/{siteId}/topics/{topicId}")
    public ResponseEntity deleteTopic(@PathVariable String topicId) throws ConversationsPermissionsException, UserNotDefinedException {

		checkSakaiSession();
        if (conversationsService.deleteTopic(topicId)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/pinned")
    public ResponseEntity pinTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean pinned) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.pinTopic(topicId, pinned);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/bookmarked")
    public ResponseEntity bookmarkTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean bookmarked) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.bookmarkTopic(topicId, bookmarked);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/hidden")
    public ResponseEntity hideTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean hidden) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.hideTopic(topicId, hidden);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/locked", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel lockTopic(@PathVariable String siteId, @PathVariable String topicId, @RequestBody Boolean locked) throws ConversationsPermissionsException {

		checkSakaiSession();

        return entityModelForTopicBean(conversationsService.lockTopic(topicId, locked));
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/reactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Reaction, Integer> postTopicReactions(@PathVariable String topicId, @RequestBody Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

		checkSakaiSession();

        return conversationsService.saveTopicReactions(topicId, reactions);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/markpostsviewed")
    public ResponseEntity markPostsViewed(@PathVariable String topicId, @RequestBody Set<String> postIds) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.markPostsViewed(postIds, topicId);
        return new ResponseEntity(HttpStatus.OK);
    }


    private EntityModel entityModelForTopicBean(TopicTransferBean topicBean) {

        List<Link> links = new ArrayList<>();
        links.add(Link.of(topicBean.url, "self"));
        links.add(Link.of(topicBean.url + "/bookmarked", "bookmark"));
        links.add(Link.of(topicBean.url + "/posts/markpostsviewed", "markpostsviewed"));
        if (topicBean.canPin) links.add(Link.of(topicBean.url + "/pinned", "pin"));
        if (topicBean.canPost) links.add(Link.of(topicBean.url + "/posts", "post"));
        if (topicBean.canDelete) links.add(Link.of(topicBean.url, "delete"));
        if (topicBean.canReact) links.add(Link.of(topicBean.url + "/reactions", "react"));
        if (topicBean.canModerate) links.add(Link.of(topicBean.url + "/locked", "lock"));
        if (topicBean.canModerate) links.add(Link.of(topicBean.url + "/hidden", "hide"));
        return EntityModel.of(topicBean, links);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel createPost(@PathVariable String siteId, @PathVariable String topicId, @RequestBody PostTransferBean postBean) throws ConversationsPermissionsException {

		checkSakaiSession();
        postBean.siteId = siteId;
        postBean.topic = topicId;
        return entityModelForPostBean(conversationsService.savePost(postBean));
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/posts", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EntityModel> getTopicPosts(@PathVariable String siteId, @PathVariable String topicId) throws ConversationsPermissionsException {

		checkSakaiSession();
        return conversationsService.getPostsByTopicId(siteId, topicId).stream()
            .map(pb -> entityModelForPostBean(pb)).collect(Collectors.toList());
    }

	@PutMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel updatePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody PostTransferBean postBean) throws ConversationsPermissionsException {

		checkSakaiSession();

        postBean.siteId = siteId;
        postBean.id = postId;
        return entityModelForPostBean(conversationsService.savePost(postBean));
    }

	@DeleteMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}")
    public ResponseEntity deletePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId) throws ConversationsPermissionsException {

		checkSakaiSession();

        if (conversationsService.deletePost(siteId, topicId, postId, true)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/upvote")
    public ResponseEntity upvotePost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId) throws ConversationsPermissionsException {

		checkSakaiSession();
        conversationsService.upvotePost(siteId, topicId, postId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@GetMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/unupvote")
    public ResponseEntity unUpvotePost(@PathVariable String siteId, @PathVariable String postId) throws ConversationsPermissionsException {

		checkSakaiSession();
        conversationsService.unUpvotePost(siteId, postId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/reactions", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Reaction, Integer> postPostReactions(@PathVariable String postId, @RequestBody Map<Reaction, Boolean> reactions) throws ConversationsPermissionsException {

		checkSakaiSession();

        return conversationsService.savePostReactions(postId, reactions);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/locked", produces = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel lockPost(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody Boolean locked) throws ConversationsPermissionsException {

		checkSakaiSession();

        return entityModelForPostBean(conversationsService.lockPost(siteId, topicId, postId, locked));
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/hidden")
    public ResponseEntity hidePost(@PathVariable String siteId, @PathVariable String postId, @RequestBody Boolean hidden) throws ConversationsPermissionsException {

		checkSakaiSession();

        conversationsService.hidePost(postId, hidden, siteId);
        return new ResponseEntity(HttpStatus.OK);
    }

    private EntityModel entityModelForPostBean(PostTransferBean postBean) {

        List<Link> links = new ArrayList<>();
        links.add(Link.of(postBean.url, "self"));
        if (postBean.canDelete) links.add(Link.of(postBean.url, "delete"));
        if (postBean.canReact) links.add(Link.of(postBean.url + "/reactions", "react"));
        if (postBean.canModerate) links.add(Link.of(postBean.url + "/locked", "lock"));
        if (postBean.canModerate) links.add(Link.of(postBean.url + "/hidden", "hide"));
        return EntityModel.of(postBean, links);
    }

	@PostMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentTransferBean createComment(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @RequestBody CommentTransferBean commentBean) throws ConversationsPermissionsException  {

		checkSakaiSession();
        commentBean.post = postId;
        commentBean.siteId = siteId;
        return conversationsService.saveComment(commentBean);
    }

	@PutMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public CommentTransferBean updateComment(@PathVariable String siteId, @PathVariable String topicId, @PathVariable String postId, @PathVariable String commentId, @RequestBody CommentTransferBean commentBean) throws ConversationsPermissionsException  {

		checkSakaiSession();

        commentBean.id = commentId;
        commentBean.post = postId;
        commentBean.siteId = siteId;
        return conversationsService.saveComment(commentBean);
    }

	@DeleteMapping(value = "/sites/{siteId}/topics/{topicId}/posts/{postId}/comments/{commentId}")
    public ResponseEntity deleteComment(@PathVariable String siteId, @PathVariable String commentId) throws ConversationsPermissionsException  {

		checkSakaiSession();

        if (conversationsService.deleteComment(siteId, commentId)) {
            return new ResponseEntity(HttpStatus.OK);
        } else {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	@PostMapping(value = "/sites/{siteId}/conversations/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tag> createTags(@PathVariable String siteId, @RequestBody List<Tag> tags) throws ConversationsPermissionsException {

		checkSakaiSession();
        return conversationsService.createTags(tags);
    }

	@GetMapping(value = "/sites/{siteId}/conversations/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Tag> getTagsForSite(@PathVariable String siteId) throws ConversationsPermissionsException {

		checkSakaiSession();
        return conversationsService.getTagsForSite(siteId);
    }

	@PutMapping(value = "/sites/{siteId}/conversations/tags/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateTag(@PathVariable String siteId, @PathVariable Long tagId, @RequestBody Tag tag) throws ConversationsPermissionsException {

		checkSakaiSession();

        tag.setId(tagId);
        conversationsService.saveTag(tag);
        return new ResponseEntity(HttpStatus.OK);
    }

	@DeleteMapping(value = "/sites/{siteId}/conversations/tags/{tagId}")
    public ResponseEntity deleteTag(@PathVariable Long tagId) throws ConversationsPermissionsException  {

		checkSakaiSession();

        conversationsService.deleteTag(tagId);
        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/settings/guidelines")
    public ResponseEntity saveSetting(@PathVariable String siteId, @RequestBody String guidelines) throws ConversationsPermissionsException {

		checkSakaiSession();

        Settings settings = conversationsService.getSettingsForSite(siteId);
        settings.setGuidelines(guidelines);
        conversationsService.saveSettings(settings);

        return new ResponseEntity(HttpStatus.OK);
    }

	@PostMapping(value = "/sites/{siteId}/conversations/settings/{setting}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity saveSetting(@PathVariable String siteId, @PathVariable String setting, @RequestBody Boolean on) throws ConversationsPermissionsException {

		checkSakaiSession();

        Settings settings = conversationsService.getSettingsForSite(siteId);

        switch (setting) {
            case "allowPinning":
                settings.setAllowPinning(on);
                break;
            case "allowUpvoting":
                settings.setAllowUpvoting(on);
                break;
            case "allowAnonPosting":
                settings.setAllowAnonPosting(on);
                break;
            case "allowReactions":
                settings.setAllowReactions(on);
                break;
            case "allowBookmarking":
                settings.setAllowBookmarking(on);
                break;
            case "requireGuidelinesAgreement":
                settings.setRequireGuidelinesAgreement(on);
                break;
            case "siteLocked":
                settings.setSiteLocked(on);
                break;
            default:
        }

        conversationsService.saveSettings(settings);

        return new ResponseEntity(HttpStatus.OK);
    }

	@GetMapping(value = "/sites/{siteId}/conversations/agree")
    public ResponseEntity agreeToGuidelines(@PathVariable String siteId) throws ConversationsPermissionsException {

		String currentUserId = checkSakaiSession().getUserId();
        ConvStatus convStatus = conversationsService.getConvStatusForSiteAndUser(siteId, currentUserId);
        convStatus.setGuidelinesAgreed(true);
        conversationsService.saveConvStatus(convStatus);
        return new ResponseEntity(HttpStatus.OK);
    }
}
