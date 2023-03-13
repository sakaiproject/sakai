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

public class ItemCancellationUtil {


    public static boolean isCancelled(int cancellation) {
        return cancellation == ItemDataIfc.ITEM_DISTRIBUTED_CANCELLED
                || cancellation == ItemDataIfc.ITEM_TOTAL_SCORE_CANCELLED;
    }

    public static boolean isCancelled(Integer cancellation) {
        return isCancelled(cancellation.intValue());
    }

    public static boolean isCancelled(ItemDataIfc item) {
        return isCancelled(item.getCancellation());
    }

    public static boolean isCancellationPending(int cancellation) {
        return cancellation == ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL
                || cancellation == ItemDataIfc.ITEM_DISTRIBUTED_TO_CANCEL;
    }

    public static boolean isCancellationPending(Integer cancellation) {
        return isCancellationPending(cancellation.intValue());
    }

    public static boolean isCancellationPending(ItemDataIfc item) {
        return isCancellationPending(item.getCancellation());
    }

    public static boolean isCancelledOrCancellationPending(int cancellation) {
        return cancellation != ItemDataIfc.ITEM_NOT_CANCELED;
    }

    public static boolean isCancelledOrCancellationPending(Integer cancellation) {
        return isCancelledOrCancellationPending(cancellation.intValue());
    }

    public static boolean isCancelledOrCancellationPending(ItemDataIfc item) {
        return isCancelledOrCancellationPending(item.getCancellation());
    }

}
