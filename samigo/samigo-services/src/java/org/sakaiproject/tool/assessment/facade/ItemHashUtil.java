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

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.springframework.orm.hibernate4.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * For shared {@code Item} hashing functionality shared between "query" classes for items of
 * any scope, e.g. published and un-published.
 */
@Slf4j
class ItemHashUtil {

    static final String TOTAL_ITEM_COUNT_HQL = "total.item.count.hql";
    static final String TOTAL_HASH_BACKFILLABLE_ITEM_COUNT_HQL = "total.hash.backfillable.item.count.hql";
    static final String ALL_HASH_BACKFILLABLE_ITEM_IDS_HQL = "all.backfillable.item.ids.hql";
    static final String ITEMS_BY_ID_HQL = "items.by.id.hql";
    static final String ID_PARAMS_PLACEHOLDER = "{ID_PARAMS}";

    private ContentHostingService contentHostingService;
    private PlatformTransactionManager transactionManager;

    /**
     * Bit of a hack to allow reuse between {@link ItemFacadeQueries} and {@link PublishedItemFacadeQueries}.
     * Arguments are rather arbitrary extension points to support what we happen to <em>know</em> are the differences
     * between item and published item processing, as well as the common utilities/service dependencies.
     *
     * @param batchSize
     * @param hqlQueries
     * @param concreteType
     * @param hashAndAssignCallback
     * @param hibernateTemplate
     * @return
     */
    BackfillItemHashResult backfillItemHashes(int batchSize,
                                                       Map<String,String> hqlQueries,
                                                       Class<? extends ItemDataIfc> concreteType,
                                                       Function<ItemDataIfc,ItemDataIfc> hashAndAssignCallback,
                                                       HibernateTemplate hibernateTemplate) {

        final long startTime = System.currentTimeMillis();
        log.debug("Hash backfill starting for items of type [" + concreteType.getSimpleName() + "]");

        if ( batchSize <= 0 ) {
            batchSize = 100;
        }
        final int flushSize = batchSize;

        final AtomicInteger totalItems = new AtomicInteger(0);
        final AtomicInteger totalItemsNeedingBackfill = new AtomicInteger(0);
        final AtomicInteger batchNumber = new AtomicInteger(0);
        final AtomicInteger recordsRead = new AtomicInteger(0);
        final AtomicInteger recordsUpdated = new AtomicInteger(0);
        final Map<Long, Throwable> hashingErrors = new TreeMap<>();
        final Map<Integer, Throwable> otherErrors = new TreeMap<>();
        final List<Long> batchElapsedTimes = new ArrayList<>();
        // always needed as *printable* average per-batch timing value, so just store as string. and cache at this
        // scope b/c we sometimes need to print a single calculation multiple times, e.g. in last batch and
        // at method exit
        final AtomicReference<String> currentAvgBatchElapsedTime = new AtomicReference<>("0.00");
        final AtomicBoolean areMoreItems = new AtomicBoolean(true);

        // Get the item totals up front since a) we know any questions created while the job is running will be
        // assigned hashes and thus won't need to be handled by the job and b) makes bookkeeping within the job much
        // easier
        hibernateTemplate.execute(session -> {
            session.setDefaultReadOnly(true);
            totalItems.set(countItems(hqlQueries, session));
            totalItemsNeedingBackfill.set(countItemsNeedingHashBackfill(hqlQueries, session));
            log.debug("Hash backfill required for [" + totalItemsNeedingBackfill + "] of [" + totalItems
                    + "] items of type [" + concreteType.getSimpleName() + "]");
            return null;
        });

        while (areMoreItems.get()) {
            long batchStartTime = System.currentTimeMillis();
            batchNumber.getAndIncrement();
            final AtomicInteger itemsHashedInBatch = new AtomicInteger(0);
            final AtomicInteger itemsReadInBatch = new AtomicInteger(0);
            final AtomicReference<Throwable> failure = new AtomicReference<>(null);

            // Idea here is a) avoid very long running transactions and b) avoid reading all items into memory
            // and c) avoid weirdness, e.g. duplicate results, when paginating complex hibernate objects. So
            // there's a per-batch transaction, and each batch re-runs the same two item lookup querys, one to
            // get the list of IDs for the next page of items, and one to resolve those IDs to items
            try {
                new TransactionTemplate(transactionManager, requireNewTransaction()).execute(status -> {
                    hibernateTemplate.execute(session -> {
                        List<ItemDataIfc> itemsInBatch = null;
                        try { // resource cleanup block
                            session.setFlushMode(FlushMode.MANUAL);
                            try { // initial read block (failures here are fatal)

                            // set up the actual result set for this batch of items. use error count to skip over failed items
                            final List<Long> itemIds = itemIdsNeedingHashBackfill(hqlQueries, flushSize, hashingErrors.size(), session);
                            itemsInBatch = itemsById(itemIds, hqlQueries, session);

                            } catch (RuntimeException e) {
                                // Panic on failure to read counts and/or the actual items in the batch.
                                // Otherwise would potentially loop indefinitely since this design has no way way to
                                // skip this page of results.
                                log.error("Failed to read batch of hashable items. Giving up at record [" + recordsRead
                                        + "] of [" + totalItemsNeedingBackfill + "] Type: [" + concreteType.getSimpleName()
                                        + "]", e);
                                areMoreItems.set(false); // force overall loop to exit
                                throw e; // force txn to give up
                            }

                            for ( ItemDataIfc item : itemsInBatch ) {
                                recordsRead.getAndIncrement();
                                itemsReadInBatch.getAndIncrement();

                                // Assign the item's hash/es
                                try {
                                    log.debug("Backfilling hash for item [" + recordsRead + "] of ["
                                            + totalItemsNeedingBackfill + "] Type: [" + concreteType.getSimpleName()
                                            + "] ID: [" + item.getItemId() + "]");
                                    hashAndAssignCallback.apply(item);
                                    itemsHashedInBatch.getAndIncrement();
                                } catch (Throwable t) {
                                    // Failures considered ignorable here... probably some unexpected item state
                                    // that prevented hash calculation.
                                    //
                                    // Re the log statement... yes, the caller probably logs exceptions, but likely
                                    // without stack traces, and we'd like to advertise failures as quickly as possible,
                                    // so we go ahead and emit an error log here.
                                    log.error("Item hash calculation failed for item [" + recordsRead + "] of ["
                                            + totalItemsNeedingBackfill + "] Type: [" + concreteType.getSimpleName()
                                            + "] ID: [" + (item == null ? "?" : item.getItemId()) + "]", t);
                                    hashingErrors.put(item.getItemId(), t);
                                }

                            }
                            if (itemsHashedInBatch.get() > 0) {
                                session.flush();
                                recordsUpdated.getAndAdd(itemsHashedInBatch.get());
                            }
                            areMoreItems.set(itemsInBatch.size() >= flushSize);

                        } finally {
                            quietlyClear(session); // potentially very large, so clear aggressively
                        }
                        return null;
                    }); // end session
                    return null;
                }); // end transaction
            } catch ( Throwable t ) {
                // We're still in the loop over all batches, but something caused the current batch (and its
                // transaction) to exit abnormally. Logging of both success and failure cases is quite detailed,
                // and needs the same timing calcs, so is consolidated into the  'finally' block below.
                failure.set(t);
                otherErrors.put(batchNumber.get(), t);
            } finally {
                // Detailed batch-level reporting
                final long batchElapsed = (System.currentTimeMillis() - batchStartTime);
                batchElapsedTimes.add(batchElapsed);
                currentAvgBatchElapsedTime.set(new DecimalFormat("#.00")
                        .format(batchElapsedTimes.stream().collect(Collectors.averagingLong(l -> l))));
                if (failure.get() == null) {
                    log.debug("Item hash backfill batch flushed to database. Type: ["
                            + concreteType.getSimpleName() + "] Batch number: ["
                            + batchNumber + "] Items attempted in batch: [" + itemsReadInBatch
                            + "] Items succeeded in batch: [" + itemsHashedInBatch
                            + "] Total items attempted: [" + recordsRead + "] Total items succeeded: ["
                            + recordsUpdated + "] Total attemptable items: [" + totalItemsNeedingBackfill
                            + "] Elapsed batch time: [" + batchElapsed
                            + "ms] Avg time/batch: ["
                            + currentAvgBatchElapsedTime + "ms]");
                } else {
                    // yes, caller probably logs exceptions later, but probably without stack traces, and we'd
                    // like to advertise failures as quickly as possible, so we go ahead and emit an error log
                    // here.
                    log.error("Item hash backfill failed. Type: ["
                            + concreteType.getSimpleName() + "] Batch number: ["
                            + batchNumber + "] Items attempted in batch: [" + itemsReadInBatch
                            + "] Items flushable (but failed) in batch: [" + itemsHashedInBatch
                            + "] Total items attempted: [" + recordsRead + "] Total items succeeded: ["
                            + recordsUpdated + "] Total attemptable items: [" + totalItemsNeedingBackfill
                            + "] Elapsed batch time: [" + batchElapsed
                            + "ms] Avg time/batch: ["
                            + currentAvgBatchElapsedTime + "ms]", failure.get());
                }
            }
        } // end loop over all batches

        final long elapsedTime = System.currentTimeMillis() - startTime;
        log.debug("Hash backfill completed for items of type [" + concreteType.getSimpleName()
                + "]. Total items attempted: [" + recordsRead
                + "] Total items succeeded: [" + recordsUpdated + "] Target attemptable items: ["
                + totalItemsNeedingBackfill + "] Total elapsed time: [" + elapsedTime
                + "ms] Total batches: [" + batchNumber + "] Avg time/batch: ["
                + currentAvgBatchElapsedTime + "ms]");

        return new BackfillItemHashResult(elapsedTime, totalItems.get(), totalItemsNeedingBackfill.get(),
                recordsRead.get(), recordsUpdated.get(), flushSize, hashingErrors, otherErrors);
    }

    private int countItems(Map<String, String> hqlQueries, Session session) {
        @SuppressWarnings("unchecked")
        final List<Integer> totalItemsResult = session
                .createQuery(hqlQueries.get(TOTAL_ITEM_COUNT_HQL))
                .setReadOnly(true).list();
        return totalItemsResult.get(0);
    }

    private int countItemsNeedingHashBackfill(Map<String, String> hqlQueries, Session session) {
        @SuppressWarnings("unchecked")
        final List<Integer> totalItemsNeedingBackfillResult = session
                .createQuery(hqlQueries.get(TOTAL_HASH_BACKFILLABLE_ITEM_COUNT_HQL))
                .setReadOnly(true).list();
        return totalItemsNeedingBackfillResult.get(0);
    }

    private List<Long> itemIdsNeedingHashBackfill(Map<String, String> hqlQueries, int pageSize, int pageStart, Session session) {
        return session
                .createQuery(hqlQueries.get(ALL_HASH_BACKFILLABLE_ITEM_IDS_HQL))
                .setFirstResult(pageStart)
                .setMaxResults(pageSize)
                .list();
    }

    private List<ItemDataIfc> itemsById(List<Long> itemIds, Map<String, String> hqlQueries, Session session) {
        if ( itemIds == null || itemIds.isEmpty() ) {
            return new ArrayList<>(0);
            }

        final String paramPlaceholders = StringUtils.repeat("?", ",", itemIds.size());
        final Query query = session.createQuery(hqlQueries.get(ITEMS_BY_ID_HQL).replace(ID_PARAMS_PLACEHOLDER, paramPlaceholders));
        final AtomicInteger position = new AtomicInteger(0);
        itemIds.forEach(id -> query.setParameter(position.getAndIncrement(), id));
        return query.list();
    }

    private TransactionDefinition requireNewTransaction() {
        return new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    private void quietlyClear(Session session) {
        if ( session != null ) {
            try {
                session.clear();
            } catch (Exception e) {
                // nothing to do
            }
        }
    }


    String hashItem(ItemDataIfc item) throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        StringBuilder hashBase = hashBaseForItem(item);
        return hashString(hashBase.toString());
    }

    String hashItemUnchecked(ItemDataIfc item) {
        try {
            return hashItem(item);
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new RuntimeException(e);
        }
    }

    StringBuilder hashBaseForItem(ItemDataIfc item) throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        StringBuilder hashBase = new StringBuilder();

        hashBaseForItemCoreProperties(item, hashBase);
        hashBaseForItemAttachments(item, hashBase);
        hashBaseForItemAnswers(item, hashBase);
        hashBaseForItemMetadata(item, hashBase);

        return hashBase;
    }

    StringBuilder hashBaseForItemCoreProperties(ItemDataIfc item, StringBuilder into)
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {

        into.append("TypeId:" + item.getTypeId() + "::");

        if (item.getTypeId() == TypeIfc.EXTENDED_MATCHING_ITEMS) {
            into.append(normalizeResourceUrls("ThemeText", item.getThemeText()))
                    .append(normalizeResourceUrls("LeadInText",item.getLeadInText()));
        } else {
            into.append(normalizeResourceUrls("ItemText",item.getText()))
                    .append(normalizeResourceUrls("Instruction",item.getInstruction()));
        }
        return into.append(normalizeResourceUrls("CorrectItemFeedback",item.getCorrectItemFeedback()))
                .append(normalizeResourceUrls("IncorrectItemFeedback",item.getInCorrectItemFeedback()))
                .append(normalizeResourceUrls("GeneralCorrectItemFeedback",item.getGeneralItemFeedback()))
                .append(normalizeResourceUrls("Description",item.getDescription()));

    }

    StringBuilder hashBaseForItemAttachments(ItemDataIfc item, StringBuilder into)
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {

        final AssessmentService service = new AssessmentService();
        final List<String> attachmentResourceIds = service.getItemResourceIdList(item);

        if ( attachmentResourceIds == null ) {
            return into;
        }

        return hashBaseForResourceIds(attachmentResourceIds, into);
    }

    StringBuilder hashBaseForItemAnswers(ItemDataIfc item, StringBuilder into)
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        if (item.getTypeId() == TypeIfc.EXTENDED_MATCHING_ITEMS){  //EMI question is different

            if (item.getIsAnswerOptionsSimple()){
                for ( AnswerIfc answerIfc : item.getEmiAnswerOptions() ) {
                    into.append(normalizeResourceUrls("EmiLabel",answerIfc.getLabel()));
                }
            }
            if (item.getIsAnswerOptionsRich()){
                into.append(normalizeResourceUrls("EmiAnswerOptionsRichText",item.getEmiAnswerOptionsRichText()));
            }

            for ( ItemTextIfc itemTextIfc : item.getEmiQuestionAnswerCombinations() ) {
                into.append(normalizeResourceUrls("EmiCorrectOptionLabels" , itemTextIfc.getEmiCorrectOptionLabels()));
                into.append(normalizeResourceUrls("EmiSequence" , Long.toString(itemTextIfc.getSequence())));
                into.append(normalizeResourceUrls("EmiText" , itemTextIfc.getText()));
                if (itemTextIfc.getHasAttachment() && itemTextIfc.isEmiQuestionItemText()){
                    final List<String> itemTextAttachmentIfcList =
                            itemTextIfc
                                    .getItemTextAttachmentSet()
                                    .stream()
                                    .map(AttachmentIfc::getResourceId)
                                    .collect(Collectors.toList());
                    into = hashBaseForResourceIds(itemTextAttachmentIfcList, into);
                }
            }
        }else{
            //We use the itemTextArraySorted and answerArraySorted to be sure we retrieve the same order.
            final List<ItemTextIfc> itemTextArraySorted = item.getItemTextArraySorted();
            for ( ItemTextIfc itemTextIfc : itemTextArraySorted ) {
                if ((item.getTypeId().equals(TypeIfc.MATCHING))||(item.getTypeId().equals(TypeIfc.MATRIX_CHOICES_SURVEY))||(item.getTypeId().equals(TypeIfc.CALCULATED_QUESTION))||(item.getTypeId().equals(TypeIfc.IMAGEMAP_QUESTION))) {
                    into.append(normalizeResourceUrls("ItemTextAnswer", itemTextIfc.getText()));
                }
                if ((item.getTypeId() != TypeIfc.AUDIO_RECORDING)&&(item.getTypeId() != TypeIfc.FILE_UPLOAD)) {
                    final List<AnswerIfc> answerArraySorted = itemTextIfc.getAnswerArraySorted();
                    for (AnswerIfc answerIfc : answerArraySorted) {
                        String getIsCorrect = "" + answerIfc.getIsCorrect();
                                if (getIsCorrect.equals("null")){
                                    getIsCorrect = null;
                                }

                        into.append(normalizeResourceUrls("ItemTextAnswer", answerIfc.getText()))
                                .append(normalizeResourceUrls("CorrectAnswerFeedback", answerIfc.getCorrectAnswerFeedback()))
                                .append(normalizeResourceUrls("InCorrectAnswerFeedback", answerIfc.getInCorrectAnswerFeedback()))
                                .append(normalizeResourceUrls("GeneralAnswerFeedback", answerIfc.getGeneralAnswerFeedback()))
                                .append(normalizeResourceUrls("AnswerSequence" , "" + answerIfc.getSequence() ))
                                .append(normalizeResourceUrls("AnswerLabel" ,  answerIfc.getLabel()))
                                .append(normalizeResourceUrls("AnswerIsCorrect" , getIsCorrect));
                    }
                }
            }
        }
        return into;
    }

    StringBuilder hashBaseForResourceIds(List<String> resourceIdList, StringBuilder into)
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        // Sort the hashes, not the resources, b/c the only reasonable option for sorting resources
        // is the resourceId field, but that is unreliable because a resource rename shouldn't have
        // an impact on question hashing.
        final List<String> hashes = new ArrayList<>(resourceIdList.size());
        for ( String resourceId : resourceIdList ) {
            ContentResource file = null;
            try {
                contentHostingService.checkResource(resourceId);
                file = contentHostingService.getResource(resourceId);
            } catch (Exception e) {
                // nothing to do, resource does not exist or we don't have access to it
                log.debug("Failed to access resource by id " + resourceId, e);
            }
            if ( file != null ) {
                // The 1L means "hash the first KB". The hash will also include the size of the entire file as a
                // stringified long. We do this b/c we suppose than a file where the first KB hash and the length are
                // the same are very likely the same file from a content perspective. We only hash the first KB for
                // performance.
                final String hash = hashResource(file, 1L);
                if ( hash != null ) {
                    hashes.add(hash);
                }
            }
        }
        if (hashes.size()>0) {
            return into.append("Resources:" + hashes.stream().sorted().collect(Collectors.joining()) + "::");
        }else{
            return into;
        }
    }

    StringBuilder hashBaseForItemMetadata(ItemDataIfc item, StringBuilder into)
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        return into.append(normalizeMetadataUrl(ItemMetaDataIfc.RANDOMIZE,item.getItemMetaDataByLabel(ItemMetaDataIfc.RANDOMIZE)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.REQUIRE_ALL_OK,item.getItemMetaDataByLabel(ItemMetaDataIfc.REQUIRE_ALL_OK)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.IMAGE_MAP_SRC,item.getItemMetaDataByLabel(ItemMetaDataIfc.IMAGE_MAP_SRC)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB,item.getItemMetaDataByLabel(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB,item.getItemMetaDataByLabel(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB,item.getItemMetaDataByLabel(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT,item.getItemMetaDataByLabel(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.FORCE_RANKING,item.getItemMetaDataByLabel(ItemMetaDataIfc.FORCE_RANKING)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH,item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.ADD_COMMENT_MATRIX,item.getItemMetaDataByLabel(ItemMetaDataIfc.ADD_COMMENT_MATRIX)))
                .append(normalizeResourceUrls(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD,item.getItemMetaDataByLabel(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.PREDEFINED_SCALE,item.getItemMetaDataByLabel(ItemMetaDataIfc.PREDEFINED_SCALE)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.TIMEALLOWED,item.getItemMetaDataByLabel(ItemMetaDataIfc.TIMEALLOWED)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.NUMATTEMPTS,item.getItemMetaDataByLabel(ItemMetaDataIfc.NUMATTEMPTS)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.SCALENAME,item.getItemMetaDataByLabel(ItemMetaDataIfc.SCALENAME)))
                .append(normalizeMetadataUrl(ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX,item.getItemMetaDataByLabel(ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX)));

    }

    String hashResource(ContentResource cr, long lengthInKBToHash)
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        if ( cr == null ) {
            return null;
        }
        final String algorithm = "SHA-256";

        // compute the digest using the MD5 algorithm
        final MessageDigest md = MessageDigest.getInstance(algorithm);
        //To improve performance, we will only hash some bytes of the file.
        if (lengthInKBToHash<=0L){
            lengthInKBToHash = Long.MAX_VALUE;
        }

        final InputStream fis =  cr.streamContent();
        try {
            final byte[] buffer = new byte[1024];
            int numRead;
            long lengthToRead = 0;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    md.update(buffer, 0, numRead);
                    lengthToRead += 1;
                }
            } while ((numRead != -1) && (lengthToRead < lengthInKBToHash));
        } finally {
            if ( fis != null ) {
                try {
                    fis.close();
                } catch ( Exception e ) {
                    //nothing to do
                }
            }
        }

        // Include the file length as a disambiguator for files which otherwise happen to contain the same bytes in the
        // lengthInKBToHash range. We don't include the file name in the hash base because this might be a renamed copy
        // of a file, in which case the name is a spurious disambiguator for our purposes.
        md.update((""+cr.getContentLength()).getBytes("UTF-8"));
        byte[] digest = md.digest();

        return Base64.encodeBase64String(digest);
    }

    String hashString(String textToHash) throws IOException, NoSuchAlgorithmException {
        // This code is copied from org.sakaiproject.user.impl.PasswordService.hash()
        final String algorithm = "SHA-256";

        // compute the digest using the SHA-256 algorithm
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] digest = md.digest(textToHash.getBytes("UTF-8"));

        final String rv = Base64.encodeBase64String(digest);
        return rv;
    }

    String normalizeResourceUrls(String label, String textToParse) throws IOException, NoSuchAlgorithmException, ServerOverloadException {

        if (StringUtils.isNotEmpty(textToParse)) {

            String siteContentPath = "/access/content/";
            String startSrc = "src=\"";
            String endSrc = "\"";

            //search for all the substrings that are potential links to resources
            //if contains "..getServerUrl()/access/content/" then it's a standard site content file
            if (textToParse != null) {
                int beginIndex = textToParse.indexOf(startSrc);
                if (beginIndex > 0) {
                    String sakaiSiteResourcePath = ServerConfigurationService.getServerUrl() + siteContentPath;
                    // have to loop because there may be more than one site of origin for the content
                    while (beginIndex > 0) {
                        int correctionIndex = 0;
                        beginIndex = beginIndex + startSrc.length();
                        int endIndex = textToParse.indexOf(endSrc, beginIndex);
                        String resourceURL = textToParse.substring(beginIndex, endIndex);
                        //GET THE RESOURCE or at least check if valid
                        //if contains "..getServerUrl()/access/content/" then it's a standard site content file
                        if (resourceURL.contains(sakaiSiteResourcePath)) {
                            String cleanResourceURL = resourceURL.substring(sakaiSiteResourcePath.length() - 1);
                            final String resourceHash = hashBaseForResourceIds(Arrays.asList(cleanResourceURL), new StringBuilder()).toString();
                            if (StringUtils.isNotEmpty(resourceHash)) {
                                textToParse = textToParse.replace(resourceURL, resourceHash);
                                correctionIndex = resourceHash.length() - resourceURL.length();
                            } // else just leave the URL unmolested if we can't resolve it to a readable resource
                        }
                        beginIndex = textToParse.indexOf(startSrc, endIndex + correctionIndex);
                    } // end while
                }

            }
            return label + ":" + textToParse + "::";
        }else{
            return "";
        }
    }

    String normalizeMetadataUrl(String label, String textToParse) throws IOException, NoSuchAlgorithmException, ServerOverloadException {
        String siteContentPath = "/access/content/";
        if (textToParse != null) {
            //GET THE RESOURCE or at least check if valid
            //if contains "/access/content/" then it's a standard site content file
            if (textToParse.startsWith(siteContentPath)){
                final String resourceId = textToParse.substring(siteContentPath.length()-1);
                final String resourceHash = hashBaseForResourceIds(Arrays.asList(resourceId), new StringBuilder()).toString();
                if (StringUtils.isNotEmpty(resourceHash)) {
                    textToParse = resourceHash;
                } // else just leave the URL unmolested if we can't resolve it to a readable resource
            }
            return label + ":" + textToParse + "::";
        }else{
            return "";
        }

    }

    public void setContentHostingService(ContentHostingService contentHostingService) {
        this.contentHostingService = contentHostingService;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

}
