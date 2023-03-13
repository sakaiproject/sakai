/*
 * Copyright (c) 2023, The Apereo Foundation
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
 *
 */
package org.sakaiproject.tool.assessment.util;

import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SectionDataIfc;

public class ItemCancellationUtil {


    public static boolean isCancelled(ItemDataIfc item) {
        int cancellation = item.getCancellation().intValue();
        return cancellation == ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED
                || cancellation == ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED;
    }

    public static boolean isCancellationPending(int cancellation) {
        return cancellation == ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL
                || cancellation == ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL;
    }

    public static boolean isCancellationPending(ItemDataIfc item) {
        int cancellation = item.getCancellation().intValue();
        return cancellation != ItemDataIfc.ITEM_NOT_CANCELED;
    }

    public static boolean isCancelledOrCancellationPending(ItemDataIfc item) {
        return isCancelled(item) || isCancellationPending(item);
    }

    public static boolean isCancellable(ItemDataIfc item) {
        return !isCancelled(item);
    }

    public static boolean isRandomItem(ItemDataIfc item) {
        int sectionAuthorType = Integer.parseInt(item.getSection().getSectionMetaDataByLabel(SectionDataIfc.AUTHOR_TYPE));

        return SectionDataIfc.RANDOM_DRAW_FROM_QUESTIONPOOL.equals(sectionAuthorType);
    }
}
