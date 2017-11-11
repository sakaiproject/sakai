/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.facade;

import java.util.Map;


public class BackfillItemHashResult {

    private long elapsedTime;
    private int totalItems;
    private int totalItemsNeedingBackfill;
    private int itemsReadCount;
    private int itemUpdatedCount;
    private int batchSize;
    private Map<Long, Throwable> hashingErrorsByItemId;
    private Map<Integer, Throwable> otherErrorsByBatchNumber;

    public BackfillItemHashResult() {}

    public BackfillItemHashResult(long elapsedTime, int totalItems, int totalItemsNeedingBackfill, int itemsReadCount,
                                  int itemUpdatedCount, int batchSize, Map<Long, Throwable> hashingErrorsByItemId,
                                  Map<Integer, Throwable> otherErrorsByBatchNumber) {
        this.elapsedTime = elapsedTime;
        this.totalItems = totalItems;
        this.totalItemsNeedingBackfill = totalItemsNeedingBackfill;
        this.itemsReadCount = itemsReadCount;
        this.itemUpdatedCount = itemUpdatedCount;
        this.batchSize = batchSize;
        this.hashingErrorsByItemId = hashingErrorsByItemId;
        this.otherErrorsByBatchNumber = otherErrorsByBatchNumber;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalItemsNeedingBackfill() {
        return totalItemsNeedingBackfill;
    }

    public void setTotalItemsNeedingBackfill(int totalItemsNeedingBackfill) {
        this.totalItemsNeedingBackfill = totalItemsNeedingBackfill;
    }

    public int getItemsReadCount() {
        return itemsReadCount;
    }

    public void setItemsReadCount(int itemsReadCount) {
        this.itemsReadCount = itemsReadCount;
    }

    public int getItemUpdatedCount() {
        return itemUpdatedCount;
    }

    public void setItemUpdatedCount(int itemUpdatedCount) {
        this.itemUpdatedCount = itemUpdatedCount;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public Map<Long, Throwable> getHashingErrorsByItemId() {
        return hashingErrorsByItemId;
    }

    public void setHashingErrorsByItemId(Map<Long, Throwable> hashingErrorsByItemId) {
        this.hashingErrorsByItemId = hashingErrorsByItemId;
    }

    public Map<Integer, Throwable> getOtherErrorsByBatchNumber() {
        return otherErrorsByBatchNumber;
    }

    public void setOtherErrorsByBatchNumber(Map<Integer, Throwable> otherErrorsByBatchNumber) {
        this.otherErrorsByBatchNumber = otherErrorsByBatchNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackfillItemHashResult)) return false;

        BackfillItemHashResult that = (BackfillItemHashResult) o;

        if (elapsedTime != that.elapsedTime) return false;
        if (totalItems != that.totalItems) return false;
        if (totalItemsNeedingBackfill != that.totalItemsNeedingBackfill) return false;
        if (itemsReadCount != that.itemsReadCount) return false;
        if (itemUpdatedCount != that.itemUpdatedCount) return false;
        if (batchSize != that.batchSize) return false;
        if (hashingErrorsByItemId != null ? !hashingErrorsByItemId.equals(that.hashingErrorsByItemId) : that.hashingErrorsByItemId != null)
            return false;
        return otherErrorsByBatchNumber != null ? otherErrorsByBatchNumber.equals(that.otherErrorsByBatchNumber) : that.otherErrorsByBatchNumber == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (elapsedTime ^ (elapsedTime >>> 32));
        result = 31 * result + totalItems;
        result = 31 * result + totalItemsNeedingBackfill;
        result = 31 * result + itemsReadCount;
        result = 31 * result + itemUpdatedCount;
        result = 31 * result + batchSize;
        result = 31 * result + (hashingErrorsByItemId != null ? hashingErrorsByItemId.hashCode() : 0);
        result = 31 * result + (otherErrorsByBatchNumber != null ? otherErrorsByBatchNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BackfillItemHashResult{" +
                "elapsedTime=" + elapsedTime +
                ", totalItems=" + totalItems +
                ", totalItemsNeedingBackfill=" + totalItemsNeedingBackfill +
                ", itemsReadCount=" + itemsReadCount +
                ", itemUpdatedCount=" + itemUpdatedCount +
                ", batchSize=" + batchSize +
                ", hashingErrorsByItemId=" + hashingErrorsByItemId +
                ", otherErrorsByBatchNumber=" + otherErrorsByBatchNumber +
                '}';
    }
}
