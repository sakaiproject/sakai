/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.services.assessment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.assessment.facade.ItemHashUtil;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

/**
 * Fixed questions and the random candidates that do not match their content.
 * Owns its collections so preparing a draw never changes a section's item set.
 */
public final class QuestionPoolDraw {
    private final List<ItemDataIfc> fixedItems;
    private final List<ItemDataIfc> eligibleRandomItems;

    public QuestionPoolDraw(List<? extends ItemDataIfc> fixedItems, List<? extends ItemDataIfc> randomItems) {
        this(fixedItems, randomItems, item -> {
            String hash = item.getHash();
            return StringUtils.isNotBlank(hash) ? hash
                    : ComponentManager.get(ItemHashUtil.class).hashItemUnchecked(item);
        });
    }

    public QuestionPoolDraw(List<? extends ItemDataIfc> fixedItems, List<? extends ItemDataIfc> randomItems,
                            Function<ItemDataIfc, String> hashResolver) {
        this.fixedItems = List.copyOf(fixedItems);
        Set<Long> fixedIds = new HashSet<>();
        Set<String> fixedHashes = new HashSet<>();
        for (ItemDataIfc item : fixedItems) {
            if (item.getItemId() != null) {
                fixedIds.add(item.getItemId());
            }
            String hash = hashResolver.apply(item);
            if (StringUtils.isNotBlank(hash)) {
                fixedHashes.add(hash);
            }
        }
        this.eligibleRandomItems = new ArrayList<>();
        for (ItemDataIfc item : randomItems) {
            if (!fixedIds.contains(item.getItemId())
                    && (fixedItems.isEmpty() || !fixedHashes.contains(hashResolver.apply(item)))) {
                this.eligibleRandomItems.add(item);
            }
        }
    }

    public static QuestionPoolDraw fromSection(SectionDataIfc section) {
        String authorType = section.getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE);
        Integer type = SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.toString().equals(authorType)
                ? SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL : SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL;
        return fromItems(section.getItemArray(), type);
    }

    public static QuestionPoolDraw fromItems(List<? extends ItemDataIfc> items, Integer authorType) {
        List<ItemDataIfc> fixed = new ArrayList<>();
        List<ItemDataIfc> random = new ArrayList<>();
        for (ItemDataIfc item : items) {
            if (SectionDataIfc.FIXED_AND_RANDOM_DRAW_FROM_QUESTIONPOOL.equals(authorType)
                    && Boolean.TRUE.equals(item.getIsFixed())) {
                fixed.add(item);
            } else {
                random.add(item);
            }
        }
        return new QuestionPoolDraw(fixed, random);
    }

    public List<ItemDataIfc> getFixedItems() {
        return new ArrayList<>(fixedItems);
    }

    public List<ItemDataIfc> getEligibleRandomItems() {
        return new ArrayList<>(eligibleRandomItems);
    }

    public List<ItemDataIfc> getAllItems() {
        List<ItemDataIfc> items = getFixedItems();
        items.addAll(eligibleRandomItems);
        return items;
    }

    public boolean isValid(int count) {
        return count > 0 && count <= eligibleRandomItems.size();
    }

    /** Selects fixed questions and exactly count random candidates; zero selects only fixed questions. */
    public List<ItemDataIfc> select(int count) {
        return select(count, getEligibleRandomItems());
    }

    public List<ItemDataIfc> select(int count, long seed) {
        List<ItemDataIfc> random = getEligibleRandomItems();
        Collections.shuffle(random, new Random(seed));
        return select(count, random);
    }

    private List<ItemDataIfc> select(int count, List<ItemDataIfc> random) {
        if (count < 0 || count > eligibleRandomItems.size()) {
            throw new IllegalArgumentException("Requested " + count + " random questions, but "
                    + eligibleRandomItems.size() + " are eligible after excluding fixed questions");
        }
        List<ItemDataIfc> selected = getFixedItems();
        selected.addAll(random.subList(0, count));
        return selected;
    }
}
