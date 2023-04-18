/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
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

import org.sakaiproject.cardgame.api.CardGameService;
import org.sakaiproject.cardgame.api.model.CardGameStatItem;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.comparator.UserSortNameComparator;
import org.sakaiproject.webapi.beans.CardGameUserRestBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class CardGameController extends AbstractSakaiApiController {


    private static final int MIN_ATTEMPTS_DEFAULT = 5;
    private static final double MIN_HIT_RATIO_DEFAULT = 0.5;
    private static final boolean SHOW_OFFICIAL_PHOTO_DEFAULT = true;
    private static final String[] ALLOWED_ROLE_IDS_DEFAULT = new String[] { "access", "Student" };


    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private CardGameService cardGameService;


    @GetMapping(value = "/sites/{siteId}/card-game/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getConfig(@PathVariable String siteId) {
        checkSakaiSession();
        checkSite(siteId);

        HashMap<String, Object> config = new HashMap<>();
        config.put("minAttempts", serverConfigurationService.getInt("cardgame.minAttempts", MIN_ATTEMPTS_DEFAULT));
        config.put("minHitRatio", serverConfigurationService.getDouble("cardgame.minHitRatio", MIN_HIT_RATIO_DEFAULT));
        config.put("showOfficialPhoto", serverConfigurationService.getBoolean("cardgame.showOfficialPhoto",
                SHOW_OFFICIAL_PHOTO_DEFAULT));

        return config;
    }

    @GetMapping(value = "/sites/{siteId}/card-game/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CardGameUserRestBean> getUsers(@PathVariable String siteId) {
        Session session = checkSakaiSession();
        Site site = checkSite(siteId);

        String currentUserId = session.getUserId();
        HashMap<String, CardGameStatItem> statItems = cardGameService.findStatItemByPlayerId(currentUserId).stream()
                .collect(Collectors.toMap(statItem -> statItem.getUserId(), statItem -> statItem, (prev, next) -> next, HashMap::new));

        Set<String> userIds = site.getMembers().stream()
                .filter(member -> Arrays.stream(getAllowedRoles())
                        .anyMatch(allowedRole -> allowedRole.equals(member.getRole().getId())))
                .map(member -> member .getUserId())
                .collect(Collectors.toSet());

        return userDirectoryService.getUsers(userIds).stream()
                .sorted(new UserSortNameComparator(true, true))
                .map(user -> CardGameUserRestBean.of(user, statItems.get(user.getId())))
                .collect(Collectors.toList());
    }

    @PutMapping(value = "/sites/{siteId}/card-game/users/{userId}/checkResult", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> addHitOrMiss(@PathVariable String siteId, @PathVariable String userId,
            @RequestParam(required = true) boolean correct) {
        String currentUserId = checkSakaiSession().getUserId();
        checkSite(siteId);

        if (correct) {
            log.debug("{} made hit  for {}", currentUserId, userId);
            cardGameService.addHit(currentUserId, userId);
        } else {
            log.debug("{} made miss for {}", currentUserId, userId);
            cardGameService.addMiss(currentUserId, userId);
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/sites/{siteId}/card-game/users/{userId}/markAsLearned", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> markUserAsLearned(@PathVariable String siteId, @PathVariable String userId) {
        String currentUserId = checkSakaiSession().getUserId();
        checkSite(siteId);

        log.debug("Marking user {} as learned", userId);
        cardGameService.markUserAsLearnedForPlayer(currentUserId, userId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/sites/{siteId}/card-game/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> reset() {

        String currentUserId = checkSakaiSession().getUserId();

        log.debug("Resetting game for user {}", currentUserId);
        cardGameService.resetGameForPlayer(currentUserId);

        return ResponseEntity.ok().build();
    }


    private String[] getAllowedRoles() {
        String[] configuredAllowedRoles = serverConfigurationService.getStrings("cardgame.allowedRoles");

        return configuredAllowedRoles != null ? configuredAllowedRoles : ALLOWED_ROLE_IDS_DEFAULT;
    }

}
