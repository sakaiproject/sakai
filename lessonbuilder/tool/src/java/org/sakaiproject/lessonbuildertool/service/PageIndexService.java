/*
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.ToolConfiguration;

/**
 * Builds the canonical Lessons page index for a site.
 *
 * <p>Pages reachable from a current Lessons placement are active. Non-student pages
 * left over after walking that graph are removed pages. Keeping this calculation in
 * a request-independent service ensures that import, recovery, deletion, and UI code
 * all use the same definition.</p>
 */
@Slf4j
@Setter
public class PageIndexService {

    private SimplePageToolDao simplePageToolDao;

    public PageIndex getPageIndex(String siteId) {
        List<SimplePage> sitePages = simplePageToolDao.getSitePages(siteId);
        if (sitePages == null) {
            sitePages = Collections.emptyList();
        }

        Map<Long, SimplePage> remainingPages = sitePages.stream().collect(Collectors.toMap(
                SimplePage::getPageId,
                page -> page,
                (first, duplicate) -> first,
                LinkedHashMap::new));

        List<SimplePageItem> topLevelItems = getActiveTopLevelItems(siteId, sitePages);

        Set<Long> topLevelPageIds = topLevelItems.stream()
                .map(SimplePageItem::getSakaiId)
                .map(this::parsePageId)
                .filter(pageId -> pageId != null)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        PageTraversal traversal = new PageTraversal(siteId, topLevelPageIds, remainingPages);
        for (SimplePageItem topLevelItem : topLevelItems) {
            traversal.addPageTree(topLevelItem, true, 0);
        }

        List<SimplePage> removedPages = traversal.remainingPages.values().stream()
                .filter(page -> page.getOwner() == null)
                .collect(Collectors.toList());

        return new PageIndex(
                List.copyOf(traversal.activePages),
                List.copyOf(removedPages),
                Set.copyOf(traversal.sharedPageIds));
    }

    public Set<Long> getRemovedPageIds(String siteId) {
        return getPageIndex(siteId).removedPages().stream()
                .map(SimplePage::getPageId)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Returns the page IDs in a Lessons page tree, including the requested root.
     * Both persisted parent relationships and PAGE items are followed so retained
     * trees can be copied or restored even when their original placement is gone.
     */
    public Set<Long> getPageTreeIds(String siteId, Long rootPageId) {
        if (rootPageId == null) {
            return Collections.emptySet();
        }

        List<SimplePage> sitePages = simplePageToolDao.getSitePages(siteId);
        if (sitePages == null || sitePages.isEmpty()) {
            return Collections.emptySet();
        }

        Map<Long, SimplePage> pagesById = sitePages.stream().collect(Collectors.toMap(
                SimplePage::getPageId,
                page -> page,
                (first, duplicate) -> first,
                LinkedHashMap::new));
        if (!pagesById.containsKey(rootPageId)) {
            return Collections.emptySet();
        }

        Map<Long, List<Long>> childrenByParent = new LinkedHashMap<>();
        for (SimplePage page : sitePages) {
            if (page.getParent() != null) {
                childrenByParent.computeIfAbsent(page.getParent(), ignored -> new ArrayList<>())
                        .add(page.getPageId());
            }
        }

        Set<Long> activeTopLevelPageIds = getActiveTopLevelItems(siteId, sitePages).stream()
                .map(SimplePageItem::getSakaiId)
                .map(this::parsePageId)
                .filter(pageId -> pageId != null)
                .collect(Collectors.toSet());

        Set<Long> pageTreeIds = new LinkedHashSet<>();
        Deque<Long> pagesToVisit = new ArrayDeque<>();
        pagesToVisit.add(rootPageId);
        while (!pagesToVisit.isEmpty()) {
            Long pageId = pagesToVisit.removeFirst();
            if (!rootPageId.equals(pageId) && activeTopLevelPageIds.contains(pageId)) {
                continue;
            }
            if (!pageTreeIds.add(pageId)) {
                continue;
            }

            pagesToVisit.addAll(childrenByParent.getOrDefault(pageId, Collections.emptyList()));

            List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
            if (items == null) {
                continue;
            }
            for (SimplePageItem item : items) {
                if (item.getType() != SimplePageItem.PAGE) {
                    continue;
                }
                Long referencedPageId = parsePageId(item.getSakaiId());
                if (referencedPageId != null && pagesById.containsKey(referencedPageId)) {
                    pagesToVisit.addLast(referencedPageId);
                }
            }
        }

        return Collections.unmodifiableSet(pageTreeIds);
    }

    private List<SimplePageItem> getActiveTopLevelItems(String siteId, List<SimplePage> sitePages) {
        if (sitePages.isEmpty()) {
            return Collections.emptyList();
        }

        List<ToolConfiguration> activePlacements = simplePageToolDao.getSiteTools(siteId);
        if (activePlacements == null || activePlacements.isEmpty()) {
            return Collections.emptyList();
        }

        List<SimplePageItem> topLevelItems = simplePageToolDao.getOrderedTopLevelPageItems(siteId);
        return topLevelItems == null ? Collections.emptyList() : topLevelItems;
    }

    private final class PageTraversal {

        private final String siteId;
        private final Set<Long> topLevelPageIds;
        private final Map<Long, SimplePage> remainingPages;
        private final Map<Long, SimplePage> visitedPages = new LinkedHashMap<>();
        private final List<PageNode> activePages = new ArrayList<>();
        private final Set<Long> sharedPageIds = new LinkedHashSet<>();

        private PageTraversal(String siteId, Set<Long> topLevelPageIds, Map<Long, SimplePage> remainingPages) {
            this.siteId = siteId;
            this.topLevelPageIds = topLevelPageIds;
            this.remainingPages = remainingPages;
        }

        private void addPageTree(SimplePageItem pageItem, boolean topLevel, int level) {

            Long pageId = parsePageId(pageItem.getSakaiId());
            if (pageId == null || pageId == 0L) {
                return;
            }

            SimplePage page = remainingPages.get(pageId);
            if (page == null) {
                page = visitedPages.get(pageId);
            }
            if (page == null) {
                log.warn("Lessons page item {} references missing page {}", pageItem.getId(), pageId);
                return;
            }
            if (!siteId.equals(page.getSiteId())) {
                log.warn("Lessons page item {} in site {} references page {} from another site",
                        pageItem.getId(), siteId, pageId);
                return;
            }

            activePages.add(new PageNode(page, pageItem, level, topLevel));

            if (visitedPages.containsKey(pageId) || (topLevelPageIds.contains(pageId) && !topLevel)) {
                sharedPageIds.add(pageId);
                return;
            }

            remainingPages.remove(pageId);
            visitedPages.put(pageId, page);

            List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
            if (items == null) {
                return;
            }

            List<SimplePageItem> nextPages = new ArrayList<>();
            for (SimplePageItem item : items) {
                if (item.getType() != SimplePageItem.PAGE) {
                    continue;
                }
                if (item.getNextPage()) {
                    nextPages.add(item);
                } else {
                    addPageTree(item, false, level + 1);
                }
            }

            for (SimplePageItem nextPage : nextPages) {
                addPageTree(nextPage, false, level);
            }
        }
    }

    private Long parsePageId(String pageId) {
        try {
            return Long.valueOf(pageId);
        } catch (NumberFormatException e) {
            log.warn("Lessons page item has invalid page id {}", pageId);
            return null;
        }
    }

    public record PageNode(SimplePage page, SimplePageItem pageItem, int level, boolean topLevel) {
    }

    public record PageIndex(List<PageNode> activePages, List<SimplePage> removedPages, Set<Long> sharedPageIds) {
    }
}
