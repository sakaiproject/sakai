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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.collections.Sets;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.assessment.data.dao.assessment.Answer;
import org.sakaiproject.tool.assessment.data.dao.assessment.AnswerFeedback;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemAttachment;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemMetaData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTag;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemText;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemTextAttachment;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerFeedbackIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemMetaDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;
import org.sakaiproject.tool.assessment.data.ifc.shared.TypeIfc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dmccallum on 11/3/16.
 */
public class ItemHashUtilTest {

    private ItemHashUtil itemHashUtil = new ItemHashUtil();

    @Mock
    private ContentHostingService contentHostingService;
    @Mock
    private ServerConfigurationService serverConfigurationService;
    private Field serverConfigurationServiceCache;
    private ServerConfigurationService originalServerConfigurationService;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
        injectDependencies();
        overrideServerConfigurationServiceCover();
    }

    @After
    public void tearDown() throws IllegalAccessException {
        restoreServerConfigurationServiceCover();
    }

    private void injectDependencies() throws NoSuchFieldException, IllegalAccessException {
        overrideServerConfigurationServiceCover();
        itemHashUtil.setContentHostingService(contentHostingService);
    }

    private void overrideServerConfigurationServiceCover() throws NoSuchFieldException, IllegalAccessException {
        serverConfigurationServiceCache = org.sakaiproject.component.cover.ServerConfigurationService.class.getDeclaredField("m_instance");
        serverConfigurationServiceCache.setAccessible(true);
        originalServerConfigurationService = (ServerConfigurationService)serverConfigurationServiceCache.get(null);
        serverConfigurationServiceCache.set(null, serverConfigurationService);
    }

    private void restoreServerConfigurationServiceCover() throws IllegalAccessException {
        serverConfigurationServiceCache.set(null, originalServerConfigurationService);
    }


    // Only need a small number of "top level" testHashItem*() checks on the final hash value b/c rest of the tests in this class
    // verify all the variations on how the hash *base* should be constructed. Do at least need to do enough to verify
    // that all hasebase functions are invoked, though.

    @Test
    public void testHashItemGeneratesSha256OfHashBase() throws IOException, NoSuchAlgorithmException, ServerOverloadException {

        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        item.setInstruction(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[0])));
        item.setCorrectItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[1])));
        item.setInCorrectItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[2])));
        item.setGeneralItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[3])));
        item.setDescription(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[4])));

        // just the "first two" metadata fields should be sufficient to prove metadata is being included
        final ItemMetaDataIfc metaData1 = newItemMetaData(item, ItemMetaDataIfc.RANDOMIZE, 11);
        final ItemMetaDataIfc metaData2 = newItemMetaData(item, ItemMetaDataIfc.REQUIRE_ALL_OK, 12);
        item.setItemMetaDataSet(Sets.newSet(metaData1,metaData2));

        final ItemAttachment attachment = new ItemAttachment(1L, item, idForContentResource(CONTENT_RESOURCES[5]), CONTENT_RESOURCES[5][CR_NAME_IDX], null, Long.MAX_VALUE - 1, null, null, null, null, null, null, null, null);
        item.setItemAttachmentSet(Sets.newSet(attachment));

        final Pair<Answer,String> answer = answerAndExpectedHashBaseFor(item, 1L, true, "Label 1", CONTENT_RESOURCES[6], CONTENT_RESOURCES[7], CONTENT_RESOURCES[8], CONTENT_RESOURCES[9]);
        final ItemText itemText = new ItemText(item, 1L, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[10])), Sets.newSet(answerFrom(answer)));
        answerFrom(answer).setItemText(itemText);
        item.setItemTextSet(Sets.newSet(itemText));

        final ItemTag itemTag = new ItemTag(item, "tag1", "taglabel1", "tagcollection1", "tagcollectionname1");
        item.setItemTagSet(Sets.newSet(itemTag));

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 12).forEach(
                i -> expectResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        final StringBuilder expectedHashBase = new StringBuilder(labeled("TypeId","" + TypeIfc.FILL_IN_BLANK))
                .append(labeled("ItemText",renderBlanks(resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[10])))))
                .append(labeled("Instruction",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[0]))))
                .append(labeled("CorrectItemFeedback",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[1]))))
                .append(labeled("IncorrectItemFeedback",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[2]))))
                .append(labeled("GeneralCorrectItemFeedback",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[3]))))
                .append(labeled("Description",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[4]))))
                .append(expectedContentResourceHash1(CONTENT_RESOURCES[5]))
                .append(stringFrom(answer))
                .append(labeled("RANDOMIZE",resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[11])))) // this specific MD field not actually treated as resource doc
                .append(labeled("REQUIRE_ALL_OK",resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[12])))); // this specific MD field not actually treated as resource doc

        // nulls for the other 14 "important" metadata keys
        //IntStream.rangeClosed(0, 13).forEach(
        //        i -> expectedHashBase.append("null")
        //);

        // convenient for debugging where the hash goes off the rails by looking at where the underlying "base" string goes off the rails...
        assertThat(itemHashUtil.hashBaseForItem(item).toString(), equalTo(expectedHashBase.toString()));

        final String expectedHash = sha256(bytes(expectedHashBase.toString()));
        final String actualHash1 = itemHashUtil.hashItem(item);
        final String actualHash2 = itemHashUtil.hashItem(item);
        assertThat(actualHash1, equalTo(expectedHash));
        assertThat("Hash is not stable", actualHash2, equalTo(expectedHash)); // believe it or not, this failed at one point (test bug w/r/t stream mgmt)
    }

    /**
     * Same as {@link #testHashItemGeneratesSha256OfHashBase()} but verifies that a slightly different hash base is
     * used if the item type is {@link TypeIfc#EXTENDED_MATCHING_ITEMS}
     */
    @Test
    public void testHashItemGeneratesSha256OfHashBaseForExtendedMatchingItem() throws IOException, NoSuchAlgorithmException, ServerOverloadException {
        final ItemData item = newExtendedMatchingItem();

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 18).forEach(
                i -> expectResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        ArrayList<String[]> contentResourceDefs1 = new ArrayList<>();
        contentResourceDefs1.add(CONTENT_RESOURCES[18]);
        contentResourceDefs1.add(CONTENT_RESOURCES[8]);

        ArrayList<String[]> contentResourceDefs2 = new ArrayList<>();
        contentResourceDefs2.add(CONTENT_RESOURCES[14]); // hash of first ItemTextAttachment contents in first "combination" ItemText
        contentResourceDefs2.add(CONTENT_RESOURCES[13]); // hash of second ItemTextAttachent contents in first "combination" ItemText

        ArrayList<String[]> contentResourceDefs3 = new ArrayList<>();
        contentResourceDefs3.add(CONTENT_RESOURCES[16]); // hash of first ItemTextAttachment contents in second "combination" ItemText
        contentResourceDefs3.add(CONTENT_RESOURCES[17]); // hash of second ItemTextAttachent contents in second "combination" ItemText




        final StringBuilder expectedHashBase = new StringBuilder(labeled("TypeId","" + TypeIfc.EXTENDED_MATCHING_ITEMS))
                .append(labeled("ThemeText", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[0])))) // themeText
                .append(labeled("LeadInText", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[1])))) // leadInText
                .append(labeled("CorrectItemFeedback", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[2])))) // correct feedback
                .append(labeled("IncorrectItemFeedback", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[3])))) // incorrect feedback
                .append(labeled("GeneralCorrectItemFeedback", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[4])))) // general feedback
                .append(labeled("Description", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[5])))) // description
                // 18 then 8 for same reason 13 and 14 are reversed below
                .append(expectedContentResourceHashAttachments(contentResourceDefs1)) // first and second attachment
                .append(labeled("EmiLabel", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[9])))) // first 'options' answer label
                .append(labeled("EmiLabel", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[10])))) // second 'options' answer label
                .append(labeled("EmiCorrectOptionLabels", "Answer Label 3Answer Label 5")) //correct Answer labels for first "combination" ItemText
                .append(labeled("EmiSequence", "" + Long.MAX_VALUE)) // sequence value for first "combination" ItemText
                .append(labeled("EmiText", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[12])))) // text for first "combination" ItemText
                // 14 then 13 b/c itemattachments and itemtextattachments are sorted into the hash base by *their*
                // hashes, rather than by, say, resourceId. as noted in ItemFacadeQueries.hashBaseForResourceIds() this
                // a bit unfortunate but is really our only means to ensure a consistent sort order for attachments (at
                // least assuming the hashing implementation itself remains fixed, which it has to be for the overall
                // item hashing to be stable).
                .append(expectedContentResourceHashAttachments(contentResourceDefs2))
                .append(labeled("EmiCorrectOptionLabels", "Answer Label 6Answer Label 8")) //correct Answer labels for second "combination" ItemText
                .append(labeled("EmiSequence", "" + Long.MAX_VALUE)) // sequence value for second "combination" ItemText
                .append(labeled("EmiText", resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[15])))) // text for second "combination" ItemText
                .append(expectedContentResourceHashAttachments(contentResourceDefs3))
                .append(labeled("RANDOMIZE", resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[6])))) // this specific MD field not actually treated as resource doc
                .append(labeled("REQUIRE_ALL_OK", resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[7])))); // this specific MD field not actually treated as resource doc

        // nulls for the other 14 "important" metadata keys
        //IntStream.rangeClosed(0, 13).forEach(
        //        i -> expectedHashBase.append("null")
        //);

        final String expectedHash = sha256(bytes(expectedHashBase.toString()));

        // convenient for debugging where the hash goes off the rails by looking at where the underlying "base" string goes off the rails...
        assertThat(itemHashUtil.hashBaseForItem(item).toString(), equalTo(expectedHashBase.toString()));

        final String actualHash1 = itemHashUtil.hashItem(item);
        final String actualHash2 = itemHashUtil.hashItem(item);
        assertThat(actualHash1, equalTo(expectedHash));
        assertThat("Hash is not stable", actualHash2, equalTo(expectedHash)); // believe it or not, this failed at one point (test bug w/r/t stream mgmt)

    }


   @Test
    public void testHashBaseForItemCorePropertiesNormalizesResourceUrls()
            throws NoSuchAlgorithmException, IOException, ServerOverloadException, IdUnusedException, TypeException,
            PermissionException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);
        final ItemText itemText = new ItemText(item, 1L, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[0])), null);
        item.setItemTextSet(Sets.newSet(itemText));
        item.setInstruction(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[1])));
        item.setCorrectItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[2])));
        item.setInCorrectItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[3])));
        item.setGeneralItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[4])));
        item.setDescription(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[5])));

        expectServerUrlLookup();
        expectResourceLookup(CONTENT_RESOURCES[0]);
        expectResourceLookup(CONTENT_RESOURCES[1]);
        expectResourceLookup(CONTENT_RESOURCES[2]);
        expectResourceLookup(CONTENT_RESOURCES[3]);
        expectResourceLookup(CONTENT_RESOURCES[4]);
        expectResourceLookup(CONTENT_RESOURCES[5]);

        final StringBuilder expectedHashBase = new StringBuilder(labeled("TypeId" , "" + TypeIfc.FILL_IN_BLANK))
                .append(labeled("ItemText",renderBlanks(resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[0])))))
                .append(labeled("Instruction",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[1]))))
                .append(labeled("CorrectItemFeedback",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[2]))))
                .append(labeled("IncorrectItemFeedback",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[3]))))
                .append(labeled("GeneralCorrectItemFeedback",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[4]))))
                .append(labeled("Description",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[5]))));

        StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemCoreProperties(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemCorePropertiesPreservesNullsLiterally()
            throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        // have to explicitly initialize this collection, else NPE in getText()
        final ItemText itemText = new ItemText(item, 1L, null, null);
        item.setItemTextSet(Sets.newSet(itemText));

        final StringBuilder expectedHashBase = new StringBuilder("TypeId:"+TypeIfc.FILL_IN_BLANK + "::")
                .append("ItemText:null::");
                //.append("null")
                //.append("null")
                //.append("null")
                //.append("null")
                //.append("null")


        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemCoreProperties(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemAnswersNormalizesEmbeddedResourceUrls()
            throws NoSuchAlgorithmException, IOException, ServerOverloadException, IdUnusedException, TypeException,
            PermissionException {

        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        final Pair<Answer,String> answer1 = answerAndExpectedHashBaseFor(item, 1L, true, "Label 1", CONTENT_RESOURCES[0], CONTENT_RESOURCES[1], CONTENT_RESOURCES[2], CONTENT_RESOURCES[3]);
        final Pair<Answer,String> answer2 = answerAndExpectedHashBaseFor(item, 2L, false, "Label 2", CONTENT_RESOURCES[4], CONTENT_RESOURCES[5], CONTENT_RESOURCES[6], CONTENT_RESOURCES[7]);
        final Pair<Answer,String> answer3 = answerAndExpectedHashBaseFor(item, 3L, true, "Label 3", CONTENT_RESOURCES[8], CONTENT_RESOURCES[9], CONTENT_RESOURCES[10], CONTENT_RESOURCES[11]);
        final Pair<Answer,String> answer4 = answerAndExpectedHashBaseFor(item, 4L, false, "Label 4", CONTENT_RESOURCES[12], CONTENT_RESOURCES[13], CONTENT_RESOURCES[14], CONTENT_RESOURCES[15]);

        final ItemText itemText1 = new ItemText(item, 1L, null, Sets.newSet(answerFrom(answer1), answerFrom(answer2)));
        answerFrom(answer1).setItemText(itemText1);
        answerFrom(answer2).setItemText(itemText1);

        final ItemText itemText2 = new ItemText(item, 2L, null, Sets.newSet(answerFrom(answer3), answerFrom(answer4)));
        answerFrom(answer3).setItemText(itemText2);
        answerFrom(answer4).setItemText(itemText2);

        item.setItemTextSet(Sets.newSet(itemText1, itemText2));

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(stringFrom(answer1))
                .append(stringFrom(answer2))
                .append(stringFrom(answer3))
                .append(stringFrom(answer4));

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 15).forEach(
                i -> expectResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAnswers(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemAnswersPreservesNullsLiterally()
            throws IOException, NoSuchAlgorithmException, ServerOverloadException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        // sequence, at least, is required, else ordering is completely non-deterministic
        final Pair<Answer,String> answer1 = answerAndExpectedHashBaseFor(item, 1L, null, null, null, null, null, null);
        final Pair<Answer,String> answer2 = answerAndExpectedHashBaseFor(item, 2L, null, null, null, null, null, null);

        final ItemText itemText1 = new ItemText(item, 1L, null, Sets.newSet(answerFrom(answer1), answerFrom(answer2)));
        answerFrom(answer1).setItemText(itemText1);
        answerFrom(answer2).setItemText(itemText1);

        item.setItemTextSet(Sets.newSet(itemText1));

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(stringFrom(answer1))
                .append(stringFrom(answer2));

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAnswers(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    // Generate the Answer and expected hash bas in one function b/c we need the full *ContentResource arrays to
    // predict the expected hash base and there's just so many of them, a call to a dedicated hash base predictor
    // function is just too much duplication
    private ImmutablePair<Answer, String> answerAndExpectedHashBaseFor(ItemDataIfc item, long sequence, Boolean isCorrect,
                                                                       String label, String[] textContentResource,
                                                                       String[] correctFeedbackContentResource,
                                                                       String[] incorrectFeedbackContentResource,
                                                                       String[] generalFeedbackContentResource)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {

        final Answer answer = new Answer();
        answer.setItem(item);
        answer.setSequence(sequence);
        answer.setIsCorrect(isCorrect);
        answer.setLabel(label);
        answer.setText(resourceDocTemplate1(fullUrlForContentResource(textContentResource)));
        final AnswerFeedback correctAnswerFeedback =
                new AnswerFeedback(answer, AnswerFeedbackIfc.CORRECT_FEEDBACK, resourceDocTemplate1(fullUrlForContentResource(correctFeedbackContentResource)));
        final AnswerFeedback incorrectAnswerFeedback =
                new AnswerFeedback(answer, AnswerFeedbackIfc.INCORRECT_FEEDBACK, resourceDocTemplate1(fullUrlForContentResource(incorrectFeedbackContentResource)));
        final AnswerFeedback generalAnswerFeedback =
                new AnswerFeedback(answer, AnswerFeedbackIfc.GENERAL_FEEDBACK, resourceDocTemplate1(fullUrlForContentResource(generalFeedbackContentResource)));
        answer.setAnswerFeedbackSet(Sets.newSet(correctAnswerFeedback, incorrectAnswerFeedback, generalAnswerFeedback));
        String getIsCorrect = "" + isCorrect;
        if (getIsCorrect.equals("null")){
            getIsCorrect = null;
        }
        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled("ItemTextAnswer",resourceDocTemplate1(expectedContentResourceHash1(textContentResource))))
                .append(labeled("CorrectAnswerFeedback",resourceDocTemplate1(expectedContentResourceHash1(correctFeedbackContentResource))))
                .append(labeled("InCorrectAnswerFeedback",resourceDocTemplate1(expectedContentResourceHash1(incorrectFeedbackContentResource))))
                .append(labeled("GeneralAnswerFeedback",resourceDocTemplate1(expectedContentResourceHash1(generalFeedbackContentResource))))
                .append(labeled("AnswerSequence","" + sequence))
                .append(labeled("AnswerLabel",label))
                .append(labeled("AnswerIsCorrect",getIsCorrect));

        return new ImmutablePair<>(answer, expectedHashBase.toString());
    }

    private <R> Answer answerFrom(Pair<Answer,R> pair) {
        return pair.getLeft();
    }

    private <L> String stringFrom(Pair<L,String> pair) {
        return pair.getRight();
    }

    @Test
    public void testHashBaseForItemAnswersIncludesSimpleAnswerOptionsForExtendedMatchingItems() throws IOException, NoSuchAlgorithmException, ServerOverloadException {
        final ItemData item = newExtendedMatchingItem();

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 18).forEach(
                i -> expectResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        ArrayList<String[]> contentResourceDefs1 = new ArrayList<>();
        contentResourceDefs1.add(CONTENT_RESOURCES[14]); // hash of first ItemTextAttachment contents in first "combination" ItemText
        contentResourceDefs1.add(CONTENT_RESOURCES[13]); // hash of second ItemTextAttachent contents in first "combination" ItemText


        ArrayList<String[]> contentResourceDefs2 = new ArrayList<>();
        contentResourceDefs2.add(CONTENT_RESOURCES[16]); // hash of first ItemTextAttachent contents in second "combination" ItemText
        contentResourceDefs2.add(CONTENT_RESOURCES[17]); // hash of second ItemTextAttachent contents in second "combination" ItemText


        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled("EmiLabel",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[9])))) // first 'options' Answer label
                .append(labeled("EmiLabel",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[10])))) // second 'options' Answer label
                .append(labeled("EmiCorrectOptionLabels","Answer Label 3Answer Label 5")) //correct Answer labels for first "combination" ItemText
                .append(labeled("EmiSequence",""+Long.MAX_VALUE)) // sequence value for first "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[12])))) // text for first "combination" ItemText
                // 14 then 13 b/c itemattachments and itemtextattachments are sorted into the hash base by *their*
                // hashes, rather than by, say, resourceId. as noted in ItemFacadeQueries.hashBaseForResourceIds() this
                // a bit unfortunate but is really our only means to ensure a consistent sort order for attachments (at
                // least assuming the hashing implementation itself remains fixed, which it has to be for the overall
                // item hashing to be stable).
                .append(expectedContentResourceHashAttachments(contentResourceDefs1))
                .append(labeled("EmiCorrectOptionLabels","Answer Label 6Answer Label 8")) //correct Answer labels for second "combination" ItemText
                .append(labeled("EmiSequence",""+Long.MAX_VALUE)) // sequence value for second "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[15])))) // text for second "combination" ItemText
                .append(expectedContentResourceHashAttachments(contentResourceDefs2));

        StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAnswers(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemAnswersIncludesAnswerOptionsRichTextInsteadOfAnswerOptionLabelsForExtendedMatchingItems() throws IOException, NoSuchAlgorithmException, ServerOverloadException {
        final ItemData item = newExtendedMatchingItem();

        // the key distinguisher between this test and
        // testHashBaseForItemAnswersIncludesSimpleAnswerOptionsForExtendedMatchingItems()
        item.setAnswerOptionsSimpleOrRich(ItemDataIfc.ANSWER_OPTIONS_RICH);

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 18).forEach(
                i -> expectResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        ArrayList<String[]> contentResourceDefs1 = new ArrayList<>();
        contentResourceDefs1.add(CONTENT_RESOURCES[14]); // hash of first ItemTextAttachment contents in first "combination" ItemText
        contentResourceDefs1.add(CONTENT_RESOURCES[13]); // hash of second ItemTextAttachent contents in first "combination" ItemText


        ArrayList<String[]> contentResourceDefs2 = new ArrayList<>();
        contentResourceDefs2.add(CONTENT_RESOURCES[16]); // hash of first ItemTextAttachent contents in second "combination" ItemText
        contentResourceDefs2.add(CONTENT_RESOURCES[17]); // hash of second ItemTextAttachent contents in second "combination" ItemText

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled("EmiAnswerOptionsRichText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[11])))) // rich text
                .append(labeled("EmiCorrectOptionLabels","Answer Label 3Answer Label 5")) //correct Answer labels for first "combination" ItemText
                .append(labeled("EmiSequence",""+Long.MAX_VALUE)) // sequence value for first "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[12])))) // text for first "combination" ItemText
                // 14 then 13 b/c itemattachments and itemtextattachments are sorted into the hash base by *their*
                // hashes, rather than by, say, resourceId. as noted in ItemFacadeQueries.hashBaseForResourceIds() this
                // a bit unfortunate but is really our only means to ensure a consistent sort order for attachments (at
                // least assuming the hashing implementation itself remains fixed, which it has to be for the overall
                // item hashing to be stable).
                .append(expectedContentResourceHashAttachments(contentResourceDefs1))
                .append(labeled("EmiCorrectOptionLabels","Answer Label 6Answer Label 8")) //correct Answer labels for second "combination" ItemText
                .append(labeled("EmiSequence",""+Long.MAX_VALUE)) // sequence value for second "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[15])))) // text for second "combination" ItemText
                .append(expectedContentResourceHashAttachments(contentResourceDefs2));

        StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAnswers(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemAnswersSkipsMissingResourcesForSimpleExtendedMatchingItems() throws IdUnusedException, PermissionException, IOException, TypeException, ServerOverloadException, NoSuchAlgorithmException {
        final ItemData item = newExtendedMatchingItem();

        expectServerUrlLookup();

        failResourceLookup(CONTENT_RESOURCES[9]); //first options answer label
        expectResourceLookupUnchecked (CONTENT_RESOURCES[10]); //second options answer label

        failResourceLookup(CONTENT_RESOURCES[12]); // first answer combo item text text
        failResourceLookup(CONTENT_RESOURCES[13]); // first ItemTextAttachment contents in first "combination" ItemText
        expectResourceLookupUnchecked(CONTENT_RESOURCES[14]); // second ItemTextAttachment contents in first "combination" ItemText

        expectResourceLookupUnchecked(CONTENT_RESOURCES[15]); // second answer combo item text text
        failResourceLookup(CONTENT_RESOURCES[16]); // first ItemTextAttachment contents in second "combination" ItemText
        expectResourceLookupUnchecked(CONTENT_RESOURCES[17]); // second ItemTextAttachment contents in second "combination" ItemText

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled("EmiLabel",resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[9])))) // first 'options' Answer label
                .append(labeled("EmiLabel",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[10])))) // second 'options' Answer label
                .append(labeled("EmiCorrectOptionLabels","Answer Label 3Answer Label 5")) //correct Answer labels for first "combination" ItemText
                .append(labeled("EmiSequence","" + Long.MAX_VALUE)) // sequence value for first "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[12])))) // text for first "combination" ItemText
                .append(expectedContentResourceHash1(CONTENT_RESOURCES[14])) // hash of second ItemTextAttachment contents in first "combination" ItemText
                .append(labeled("EmiCorrectOptionLabels","Answer Label 6Answer Label 8")) //correct Answer labels for second "combination" ItemText
                .append(labeled("EmiSequence","" + Long.MAX_VALUE)) // sequence value for second "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[15])))) // text for second "combination" ItemText
                .append(expectedContentResourceHash1(CONTENT_RESOURCES[17])); // hash of second ItemTextAttachent contents in second "combination" ItemText


        StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAnswers(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemAnswersSkipsMissingResourcesForRichExtendedMatchingItems() throws IdUnusedException, PermissionException, IOException, TypeException, ServerOverloadException, NoSuchAlgorithmException {
        // same as testHashBaseForItemAnswersSkipsMissingResourcesForSimpleExtendedMatchingItems() but has one
        // additional field to check
        final ItemData item = newExtendedMatchingItem();

        // the key distinguisher between this test and
        // testHashBaseForItemAnswersSkipsMissingResourcesForSimpleExtendedMatchingItems()
        item.setAnswerOptionsSimpleOrRich(ItemDataIfc.ANSWER_OPTIONS_RICH);

        expectServerUrlLookup();

        failResourceLookup(CONTENT_RESOURCES[11]); // rich text

        failResourceLookup(CONTENT_RESOURCES[12]); // first answer combo item text text
        failResourceLookup(CONTENT_RESOURCES[13]); // first ItemTextAttachment contents in first "combination" ItemText
        expectResourceLookupUnchecked(CONTENT_RESOURCES[14]); // second ItemTextAttachment contents in first "combination" ItemText

        expectResourceLookupUnchecked(CONTENT_RESOURCES[15]); // second answer combo item text text
        failResourceLookup(CONTENT_RESOURCES[16]); // first ItemTextAttachment contents in second "combination" ItemText
        expectResourceLookupUnchecked(CONTENT_RESOURCES[17]); // second ItemTextAttachment contents in second "combination" ItemText

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled("EmiAnswerOptionsRichText",resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[11])))) // rich text
                .append(labeled("EmiCorrectOptionLabels","Answer Label 3Answer Label 5")) //correct Answer labels for first "combination" ItemText
                .append(labeled("EmiSequence","" + Long.MAX_VALUE)) // sequence value for first "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[12])))) // text for first "combination" ItemText
                .append(expectedContentResourceHash1(CONTENT_RESOURCES[14])) // hash of second ItemTextAttachment contents in first "combination" ItemText
                .append(labeled("EmiCorrectOptionLabels","Answer Label 6Answer Label 8")) //correct Answer labels for second "combination" ItemText
                .append(labeled("EmiSequence","" + Long.MAX_VALUE)) // sequence value for second "combination" ItemText
                .append(labeled("EmiText",resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[15])))) // text for second "combination" ItemText
                .append(expectedContentResourceHash1(CONTENT_RESOURCES[17])); // hash of second ItemTextAttachent contents in second "combination" ItemText


        StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAnswers(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    // setting up EMI items is such an elaborate process, we just give up and create an elaborate factory
    // method for those things. means test methods know more about what goes on in this process than
    // they really should, but without this method, code duplication becomes an even more serious problem
    private ItemData newExtendedMatchingItem() {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.EXTENDED_MATCHING_ITEMS);

        // the ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE makes ItemText behave specially for EMI items
        // (for "simple" answer options, anyway). In particular, it results in the answer *label* being treated as
        // a resource document
        final Set<ItemTextIfc> itemTextSet = Sets.newSet();
        final ItemText themeText = new ItemText(item, ItemTextIfc.EMI_THEME_TEXT_SEQUENCE, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[0])), Sets.newSet());
        final ItemText leadInText = new ItemText(item, ItemTextIfc.EMI_LEAD_IN_TEXT_SEQUENCE, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[1])), Sets.newSet());
        itemTextSet.add(themeText);
        itemTextSet.add(leadInText);

        // no instruction property for EMI
        item.setCorrectItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[2])));
        item.setInCorrectItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[3])));
        item.setGeneralItemFeedback(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[4])));
        item.setDescription(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[5])));

        // just the "first two" metadata fields should be sufficient to prove metadata is being included
        final ItemMetaDataIfc metaData1 = newItemMetaData(item, ItemMetaDataIfc.RANDOMIZE, 6);
        final ItemMetaDataIfc metaData2 = newItemMetaData(item, ItemMetaDataIfc.REQUIRE_ALL_OK, 7);
        item.setItemMetaDataSet(Sets.newSet(metaData1,metaData2));

        final ItemAttachment attachment1 = new ItemAttachment(1L, item, idForContentResource(CONTENT_RESOURCES[8]), CONTENT_RESOURCES[8][CR_NAME_IDX], null, Long.MAX_VALUE - 1, null, null, null, null, null, null, null, null);
        final ItemAttachment attachment2 = new ItemAttachment(2L, item, idForContentResource(CONTENT_RESOURCES[18]), CONTENT_RESOURCES[18][CR_NAME_IDX], null, Long.MAX_VALUE - 1, null, null, null, null, null, null, null, null);
        item.setItemAttachmentSet(Sets.newSet(attachment1,attachment2));

        // then we need to have "EMI question answer combinations". Each "combination" is implemented as any ItemText
        // having a sequence property >= zero.
        final Answer answer1 = new Answer();
        answer1.setItem(item);
        answer1.setSequence(1L);
        answer1.setIsCorrect(true);
        answer1.setLabel(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[9])));
        answer1.setText("Answer Text 1"); // completely ignored, actually

        final Answer answer2 = new Answer();
        answer2.setItem(item);
        answer2.setSequence(2L);
        answer2.setIsCorrect(true);
        answer2.setLabel(resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[10])));
        answer2.setText("Answer Text 2"); // completely ignored, actually

        final ItemText answerOptions = new ItemText(item, ItemTextIfc.EMI_ANSWER_OPTIONS_SEQUENCE, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[11])), Sets.newSet(answer1, answer2));
        answer1.setItemText(answerOptions);
        answer2.setItemText(answerOptions);
        itemTextSet.add(answerOptions);

        // now more ItemTexts and Answers to act as answer *combinations*, as distinct from answer *options*. there are
        // mulitple combinations, but only one options. an ItemText is a combination if the question type is EMI and
        // the ItemText sequence number is greater than zero (we use Long.MAX_VALUE here)
        final Answer answer3 = new Answer();
        answer3.setItem(item);
        answer3.setSequence(3L);
        answer3.setIsCorrect(true); // should be included in hash
        answer3.setLabel("Answer Label 3"); // not interpreted as a resource doc
        answer3.setText("Answer Text 3"); // completely ignored, actually
        final Answer answer4 = new Answer();
        answer4.setItem(item);
        answer4.setSequence(4L);
        answer4.setIsCorrect(false); // should be excluded from hash
        answer4.setLabel("Answer Label 4"); // not interpreted as a resource doc
        answer4.setText("Answer Text 4"); // completely ignored, actually
        final Answer answer5 = new Answer();
        answer5.setItem(item);
        answer5.setSequence(5L);
        answer5.setIsCorrect(true); // should be included in hash
        answer5.setLabel("Answer Label 5"); // not interpreted as a resource doc
        answer5.setText("Answer Text 5"); // completely ignored, actually

        final ItemText answerCombination1 = new ItemText(item, Long.MAX_VALUE, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[12])), Sets.newSet(answer3,answer4,answer5));
        answer3.setItemText(answerCombination1);
        answer4.setItemText(answerCombination1);
        answer5.setItemText(answerCombination1);

        final ItemTextAttachmentIfc itemTextAttachment1 =
                new ItemTextAttachment(1L, answerOptions, idForContentResource(CONTENT_RESOURCES[13]),
                        CONTENT_RESOURCES[13][CR_NAME_IDX], "text/text", 1024L, "Item Text Attachment Description 1",
                        "Location 1", false, AttachmentIfc.ACTIVE_STATUS, "admin", new Date(), "admin", new Date());
        final ItemTextAttachmentIfc itemTextAttachment2 =
                new ItemTextAttachment(2L, answerOptions, idForContentResource(CONTENT_RESOURCES[14]),
                        CONTENT_RESOURCES[14][CR_NAME_IDX], "text/text", 1024L, "Item Text Attachment Description 2",
                        "Location 2", false, AttachmentIfc.ACTIVE_STATUS, "admin", new Date(), "admin", new Date());
        final Set<ItemTextAttachmentIfc> itemTextAttachmentSet1 = Sets.newSet(itemTextAttachment1, itemTextAttachment2);
        answerCombination1.setItemTextAttachmentSet(itemTextAttachmentSet1);
        itemTextSet.add(answerCombination1);


        final Answer answer6 = new Answer();
        answer6.setItem(item);
        answer6.setSequence(6L);
        answer6.setIsCorrect(true); // should be included in hash
        answer6.setLabel("Answer Label 6"); // not interpreted as a resource doc
        answer6.setText("Answer Text 6"); // completely ignored, actually
        final Answer answer7 = new Answer();
        answer7.setItem(item);
        answer7.setSequence(7L);
        answer7.setIsCorrect(false); // should be excluded from hash
        answer7.setLabel("Answer Label 7"); // not interpreted as a resource doc
        answer7.setText("Answer Text 7"); // completely ignored, actually
        final Answer answer8 = new Answer();
        answer8.setItem(item);
        answer8.setSequence(8L);
        answer8.setIsCorrect(true); // should be included in hash
        answer8.setLabel("Answer Label 8"); // not interpreted as a resource doc
        answer8.setText("Answer Text 8"); // completely ignored, actually

        final ItemText answerCombination2 = new ItemText(item, Long.MAX_VALUE, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[15])), Sets.newSet(answer6,answer7,answer8));
        answer6.setItemText(answerCombination1);
        answer7.setItemText(answerCombination1);
        answer8.setItemText(answerCombination1);

        final ItemTextAttachmentIfc itemTextAttachment3 =
                new ItemTextAttachment(3L, answerOptions, idForContentResource(CONTENT_RESOURCES[16]),
                        CONTENT_RESOURCES[16][CR_NAME_IDX], "text/text", 1024L, "Item Text Attachment Description 3",
                        "Location 3", false, AttachmentIfc.ACTIVE_STATUS, "admin", new Date(), "admin", new Date());
        final ItemTextAttachmentIfc itemTextAttachment4 =
                new ItemTextAttachment(4L, answerOptions, idForContentResource(CONTENT_RESOURCES[17]),
                        CONTENT_RESOURCES[17][CR_NAME_IDX], "text/text", 1024L, "Item Text Attachment Description 4",
                        "Location 4", false, AttachmentIfc.ACTIVE_STATUS, "admin", new Date(), "admin", new Date());
        final Set<ItemTextAttachmentIfc> itemTextAttachmentSet2 = Sets.newSet(itemTextAttachment3, itemTextAttachment4);
        answerCombination2.setItemTextAttachmentSet(itemTextAttachmentSet2);
        itemTextSet.add(answerCombination2);

        item.setItemTextSet(itemTextSet);

        final ItemTag itemTag = new ItemTag(item, "tag1", "taglabel1", "tagcollection1", "tagcollectionname1");
        item.setItemTagSet(Sets.newSet(itemTag));

        return item;
    }

    @Test
    public void testHashBaseForItemAttachments() throws IOException, NoSuchAlgorithmException,
            IdUnusedException, PermissionException, TypeException, ServerOverloadException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        final ItemAttachment attachment1 = new ItemAttachment(1L, item, idForContentResource(CONTENT_RESOURCES[0]), CONTENT_RESOURCES[0][CR_NAME_IDX], null, Long.MAX_VALUE - 1, null, null, null, null, null, null, null, null);
        final ItemAttachment attachment2 = new ItemAttachment(2L, item, idForContentResource(CONTENT_RESOURCES[1]), CONTENT_RESOURCES[1][CR_NAME_IDX], null, Long.MAX_VALUE, null, null, null, null, null, null, null, null);

        // this assertion and the intentional backward add to the attachment set are *mild* attempt at ensuring
        // they're sorted in lexical order before being added to the hash base
        assertThat(attachment1.getResourceId(), lessThan(attachment2.getResourceId()));
        item.setItemAttachmentSet(Sets.newSet(attachment1, attachment2));

        // for a straight-up attachment (as opposed to an inlined HTML doc containing question or answer text),
        // we hash the attachment contents, not a doc referring to the attachment.
        ArrayList<String[]> contentResourceDefs1 = new ArrayList<>();
        contentResourceDefs1.add(CONTENT_RESOURCES[0]);
        contentResourceDefs1.add(CONTENT_RESOURCES[1]);

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(expectedContentResourceHashAttachments(contentResourceDefs1));

        expectServerUrlLookup();
        expectResourceLookup(CONTENT_RESOURCES[0]);
        expectResourceLookup(CONTENT_RESOURCES[1]);

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAttachments(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    // Little bit of paranoia on this one b/c we want to verify that a single missing resource doesn't result
    // in an edge case where we return literally null or the string "null" as the hash base for the item. we
    // want the empty string in that case since the intention is for the resource to simply be elided from
    // the hash base.
    @Test
    public void testHashBaseForItemAttachmentsSkipSingleMissingResources() throws IOException, IdUnusedException,
            PermissionException, TypeException, ServerOverloadException, NoSuchAlgorithmException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        final ItemAttachment attachment1 = new ItemAttachment(1L, item, idForContentResource(CONTENT_RESOURCES[0]), CONTENT_RESOURCES[0][CR_NAME_IDX], null, Long.MAX_VALUE - 1, null, null, null, null, null, null, null, null);
        item.setItemAttachmentSet(Sets.newSet(attachment1));

        expectServerUrlLookup();
        failResourceLookup(CONTENT_RESOURCES[0]);

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAttachments(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(""));
    }

    @Test
    public void testHashBaseForItemAttachmentsSkipsMissingResources() throws IOException, IdUnusedException,
            PermissionException, TypeException, ServerOverloadException, NoSuchAlgorithmException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);

        final ItemAttachment attachment1 = new ItemAttachment(1L, item, idForContentResource(CONTENT_RESOURCES[0]), CONTENT_RESOURCES[0][CR_NAME_IDX], null, Long.MAX_VALUE - 1, null, null, null, null, null, null, null, null);
        final ItemAttachment attachment2 = new ItemAttachment(2L, item, idForContentResource(CONTENT_RESOURCES[1]), CONTENT_RESOURCES[1][CR_NAME_IDX], null, Long.MAX_VALUE, null, null, null, null, null, null, null, null);
        item.setItemAttachmentSet(Sets.newSet(attachment1, attachment2));

        // for a straight-up attachment (as opposed to an inlined HTML doc containing question or answer text),
        // we hash the attachment contents, not a doc referring to the attachment.
        final StringBuilder expectedHashBase = new StringBuilder()
                .append(expectedContentResourceHash1(CONTENT_RESOURCES[0]));

        expectServerUrlLookup();
        expectResourceLookup(CONTENT_RESOURCES[0]);
        failResourceLookup(CONTENT_RESOURCES[1]);

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemAttachments(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    @Test
    public void testHashBaseForItemMetadata() throws IOException, NoSuchAlgorithmException, ServerOverloadException {
        // use a html doc for each (except for IMAGE_MAP_SRC which is an extra special case), even tho code only
        // assumes a small subset are actually html docs. easy way of verifying whether or not individual properties
        // are or are not routed through resource hashing routines.

        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);
        final ItemMetaDataIfc metaData1 = newItemMetaData(item, ItemMetaDataIfc.RANDOMIZE, 0);
        final ItemMetaDataIfc metaData2 = newItemMetaData(item, ItemMetaDataIfc.REQUIRE_ALL_OK, 1);
        final ItemMetaDataIfc metaData3 = new ItemMetaData(item, ItemMetaDataIfc.IMAGE_MAP_SRC, serverRelativeUrlForContentResource(CONTENT_RESOURCES[2]));
        final ItemMetaDataIfc metaData4 = newItemMetaData(item, ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB, 3);
        final ItemMetaDataIfc metaData5 = newItemMetaData(item, ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB, 4);
        final ItemMetaDataIfc metaData6 = newItemMetaData(item, ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB, 5);
        final ItemMetaDataIfc metaData7 = newItemMetaData(item, ItemMetaDataIfc.MCMS_PARTIAL_CREDIT, 6);
        final ItemMetaDataIfc metaData8 = newItemMetaData(item, ItemMetaDataIfc.FORCE_RANKING, 7);
        final ItemMetaDataIfc metaData9 = newItemMetaData(item, ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH, 8);
        final ItemMetaDataIfc metaData10 = newItemMetaData(item, ItemMetaDataIfc.ADD_COMMENT_MATRIX, 9);
        final ItemMetaDataIfc metaData11 = newItemMetaData(item, ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD, 10);
        final ItemMetaDataIfc metaData12 = newItemMetaData(item, ItemMetaDataIfc.PREDEFINED_SCALE, 11);
        final ItemMetaDataIfc metaData13 = newItemMetaData(item, ItemMetaDataIfc.TIMEALLOWED, 12);
        final ItemMetaDataIfc metaData14 = newItemMetaData(item, ItemMetaDataIfc.NUMATTEMPTS, 13);
        final ItemMetaDataIfc metaData15 = newItemMetaData(item, ItemMetaDataIfc.SCALENAME, 14);
        final ItemMetaDataIfc metaData16 = newItemMetaData(item, ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX, 15);
        item.setItemMetaDataSet(Sets.newSet(metaData1,metaData2,metaData3,metaData4,metaData5,metaData6,metaData7,
                metaData8,metaData9,metaData10,metaData11,metaData12,metaData12,metaData13,metaData14,metaData15,
                metaData16));

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 15).forEach(
                i -> expectResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled(ItemMetaDataIfc.RANDOMIZE,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[0]))))
                .append(labeled(ItemMetaDataIfc.REQUIRE_ALL_OK,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[1]))))
                .append(labeled(ItemMetaDataIfc.IMAGE_MAP_SRC,expectedContentResourceHash1(CONTENT_RESOURCES[2])))
                .append(labeled(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[3]))))
                .append(labeled(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[4]))))
                .append(labeled(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[5]))))
                .append(labeled(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[6]))))
                .append(labeled(ItemMetaDataIfc.FORCE_RANKING,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[7]))))
                .append(labeled(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[8]))))
                .append(labeled(ItemMetaDataIfc.ADD_COMMENT_MATRIX,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[9]))))
                .append(labeled(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD,resourceDocTemplate1(expectedContentResourceHash1(CONTENT_RESOURCES[10]))))
                .append(labeled(ItemMetaDataIfc.PREDEFINED_SCALE,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[11]))))
                .append(labeled(ItemMetaDataIfc.TIMEALLOWED,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[12]))))
                .append(labeled(ItemMetaDataIfc.NUMATTEMPTS,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[13]))))
                .append(labeled(ItemMetaDataIfc.SCALENAME,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[14]))))
                .append(labeled(ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[15]))));


        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemMetadata(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));

    }


    @Test
    public void testHashBaseForItemMetadataSkipsMissingResources() throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);
        final ItemMetaDataIfc metaData1 = newItemMetaData(item, ItemMetaDataIfc.RANDOMIZE, 0);
        final ItemMetaDataIfc metaData2 = newItemMetaData(item, ItemMetaDataIfc.REQUIRE_ALL_OK, 1);
        final ItemMetaDataIfc metaData3 = new ItemMetaData(item, ItemMetaDataIfc.IMAGE_MAP_SRC, serverRelativeUrlForContentResource(CONTENT_RESOURCES[2]));
        final ItemMetaDataIfc metaData4 = newItemMetaData(item, ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB, 3);
        final ItemMetaDataIfc metaData5 = newItemMetaData(item, ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB, 4);
        final ItemMetaDataIfc metaData6 = newItemMetaData(item, ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB, 5);
        final ItemMetaDataIfc metaData7 = newItemMetaData(item, ItemMetaDataIfc.MCMS_PARTIAL_CREDIT, 6);
        final ItemMetaDataIfc metaData8 = newItemMetaData(item, ItemMetaDataIfc.FORCE_RANKING, 7);
        final ItemMetaDataIfc metaData9 = newItemMetaData(item, ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH, 8);
        final ItemMetaDataIfc metaData10 = newItemMetaData(item, ItemMetaDataIfc.ADD_COMMENT_MATRIX, 9);
        final ItemMetaDataIfc metaData11 = newItemMetaData(item, ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD, 10);
        final ItemMetaDataIfc metaData12 = newItemMetaData(item, ItemMetaDataIfc.PREDEFINED_SCALE, 11);
        final ItemMetaDataIfc metaData13 = newItemMetaData(item, ItemMetaDataIfc.TIMEALLOWED, 12);
        final ItemMetaDataIfc metaData14 = newItemMetaData(item, ItemMetaDataIfc.NUMATTEMPTS, 13);
        final ItemMetaDataIfc metaData15 = newItemMetaData(item, ItemMetaDataIfc.SCALENAME, 14);
        final ItemMetaDataIfc metaData16 = newItemMetaData(item, ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX, 15);
        item.setItemMetaDataSet(Sets.newSet(metaData1,metaData2,metaData3,metaData4,metaData5,metaData6,metaData7,
                metaData8,metaData9,metaData10,metaData11,metaData12,metaData12,metaData13,metaData14,metaData15,
                metaData16));

        expectServerUrlLookup();
        IntStream.rangeClosed(0, 15).forEach(
                i -> failResourceLookupUnchecked(CONTENT_RESOURCES[i])
        );

        final StringBuilder expectedHashBase = new StringBuilder()
                .append(labeled(ItemMetaDataIfc.RANDOMIZE,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[0]))))
                .append(labeled(ItemMetaDataIfc.REQUIRE_ALL_OK,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[1]))))
                .append(labeled(ItemMetaDataIfc.IMAGE_MAP_SRC,serverRelativeUrlForContentResource(CONTENT_RESOURCES[2])))
                .append(labeled(ItemMetaDataIfc.CASE_SENSITIVE_FOR_FIB,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[3]))))
                .append(labeled(ItemMetaDataIfc.MUTUALLY_EXCLUSIVE_FOR_FIB,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[4]))))
                .append(labeled(ItemMetaDataIfc.IGNORE_SPACES_FOR_FIB,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[5]))))
                .append(labeled(ItemMetaDataIfc.MCMS_PARTIAL_CREDIT,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[6]))))
                .append(labeled(ItemMetaDataIfc.FORCE_RANKING,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[7]))))
                .append(labeled(ItemMetaDataIfc.MX_SURVEY_RELATIVE_WIDTH,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[8]))))
                .append(labeled(ItemMetaDataIfc.ADD_COMMENT_MATRIX,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[9]))))
                .append(labeled(ItemMetaDataIfc.MX_SURVEY_QUESTION_COMMENTFIELD,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[10]))))
                .append(labeled(ItemMetaDataIfc.PREDEFINED_SCALE,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[11]))))
                .append(labeled(ItemMetaDataIfc.TIMEALLOWED,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[12]))))
                .append(labeled(ItemMetaDataIfc.NUMATTEMPTS,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[13]))))
                .append(labeled(ItemMetaDataIfc.SCALENAME,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[14]))))
                .append(labeled(ItemMetaDataIfc.ADD_TO_FAVORITES_MATRIX,resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[15]))));

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemMetadata(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }

    private String labeled(String label, String text){
        if(StringUtils.isNotEmpty(text)) {
            return label + ":" + text + "::";
        }else{
            return "";
        }
    }

    @Test
    public void testHashBaseForItemMetadataSkipsUnimportantKeys() throws NoSuchAlgorithmException, IOException, ServerOverloadException {
        final ItemData item = new ItemData();
        item.setTypeId(TypeIfc.FILL_IN_BLANK);
        final ItemMetaDataIfc metaData1 = newItemMetaData(item, ItemMetaDataIfc.RANDOMIZE, 0);
        final ItemMetaDataIfc metaData2 = newItemMetaData(item, ItemMetaDataIfc.OBJECTIVE, 1); // this one should be ignored
        final ItemMetaDataIfc metaData3 = newItemMetaData(item, ItemMetaDataIfc.REQUIRE_ALL_OK, 2);
        item.setItemMetaDataSet(Sets.newSet(metaData1, metaData2, metaData3));

        final StringBuilder expectedHashBase = new StringBuilder()
                .append("RANDOMIZE:" + resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[0])) +  "::")
                .append("REQUIRE_ALL_OK:" + resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[2]))+ "::");

        // nulls for the other 14 "important" keys
        IntStream.rangeClosed(0, 13).forEach(
                i -> expectedHashBase.append("")
        );

        final StringBuilder actualHashBase = new StringBuilder();
        itemHashUtil.hashBaseForItemMetadata(item, actualHashBase);
        assertThat(actualHashBase.toString(), equalTo(expectedHashBase.toString()));
    }


    private ItemMetaDataIfc newItemMetaData(ItemData item, String key, int contentResourceIndex) {
        return new ItemMetaData(item, key, resourceDocTemplate1(fullUrlForContentResource(CONTENT_RESOURCES[contentResourceIndex])));
    }

    @Test
    public void testNormalizeResourceUrlsReplacesSingleResourceReference()
            throws NoSuchAlgorithmException, IOException, ServerOverloadException, IdUnusedException, TypeException, PermissionException {
        final String[] contentResourceDef = CONTENT_RESOURCES[0];

        final String originalResourceReferencingDoc = resourceDocTemplate1(fullUrlForContentResource(contentResourceDef));
        ArrayList<String[]> contentResourceDefs = new ArrayList<>();
        contentResourceDefs.add(contentResourceDef);
        final String expectedResourceReferencingDoc = "Label:" + resourceDocTemplate1(expectedContentResourceHash(contentResourceDefs)) + "::";

        expectServerUrlLookup();
        expectResourceLookup(contentResourceDef);
        final String actualParsedDoc = itemHashUtil.normalizeResourceUrls("Label",originalResourceReferencingDoc);
        assertThat(actualParsedDoc, equalTo(expectedResourceReferencingDoc));
    }

    @Test
    public void testNormalizeResourceUrlsReplacesTwoResourceReferences()
            throws NoSuchAlgorithmException, IOException, ServerOverloadException, IdUnusedException, TypeException, PermissionException {
        final String[] contentResourceDef1 = CONTENT_RESOURCES[0];
        final String[] contentResourceDef2 = CONTENT_RESOURCES[1];

        final String originalResourceReferencingDoc = resourceDocTemplate2(
                fullUrlForContentResource(contentResourceDef1),
                fullUrlForContentResource(contentResourceDef2));
        ArrayList<String[]> contentResourceDefs1 = new ArrayList<>();
        contentResourceDefs1.add(contentResourceDef1);
        ArrayList<String[]> contentResourceDefs2 = new ArrayList<>();
        contentResourceDefs2.add(contentResourceDef2);
        final String expectedResourceReferencingDoc = "Label:" + resourceDocTemplate1(
                expectedContentResourceHash(contentResourceDefs1)) + resourceDocTemplate1b(
                expectedContentResourceHash(contentResourceDefs2)) + "::";

        expectServerUrlLookup();
        expectResourceLookup(contentResourceDef1);
        expectResourceLookup(contentResourceDef2);
        final String actualParsedDoc = itemHashUtil.normalizeResourceUrls("Label",originalResourceReferencingDoc);
        assertThat(actualParsedDoc, equalTo(expectedResourceReferencingDoc));
    }

    @Test
    public void testNormalizeResourceUrlsSkipsMissingResources() throws IOException,
            NoSuchAlgorithmException, IdUnusedException, PermissionException, TypeException, ServerOverloadException {
        final String[] contentResourceDef1 = CONTENT_RESOURCES[0];
        final String[] contentResourceDef2 = CONTENT_RESOURCES[1];

        final String originalResourceReferencingDoc = resourceDocTemplate2(
                fullUrlForContentResource(contentResourceDef1),
                fullUrlForContentResource(contentResourceDef2));
        ArrayList<String[]> contentResourceDefs = new ArrayList<>();
        contentResourceDefs.add(contentResourceDef2);
        final String expectedResourceReferencingDoc = "Label:" + resourceDocTemplate2(
                fullUrlForContentResource(contentResourceDef1),
                expectedContentResourceHash(contentResourceDefs)) + "::";

        expectServerUrlLookup();
        failResourceLookup(contentResourceDef1);
        expectResourceLookup(contentResourceDef2);

        final String actualParsedDoc = itemHashUtil.normalizeResourceUrls("Label",originalResourceReferencingDoc);
        assertThat(actualParsedDoc, equalTo(expectedResourceReferencingDoc));
    }

    @Test
    public void testNormalizeMetadataUrlReplacesResourceReference() throws IOException, NoSuchAlgorithmException, IdUnusedException, PermissionException, TypeException, ServerOverloadException {
        final String[] contentResourceDef = CONTENT_RESOURCES[0];

        final String originalResourceReferencingMetadataValue = serverRelativeUrlForContentResource(contentResourceDef);
        ArrayList<String[]> contentResourceDefs = new ArrayList<>();
        contentResourceDefs.add(contentResourceDef);
        final String expectedResourceReferencingMetadataValue = "Label:" + expectedContentResourceHash(contentResourceDefs) + "::";

        expectResourceLookup(contentResourceDef);
        final String actualParsedValue = itemHashUtil.normalizeMetadataUrl("Label",originalResourceReferencingMetadataValue);
        assertThat(actualParsedValue, equalTo(expectedResourceReferencingMetadataValue));
    }

    @Test
    public void testNormalizeMetadataUrlSkipsMissingResources() throws IdUnusedException, PermissionException, IOException, TypeException, ServerOverloadException, NoSuchAlgorithmException {
        final String[] contentResourceDef = CONTENT_RESOURCES[0];

        final String originalResourceReferencingMetadataValue = serverRelativeUrlForContentResource(contentResourceDef);
        final String expectedResourceReferencingMetadataValue = "Label:" + originalResourceReferencingMetadataValue + "::";

        failResourceLookup(contentResourceDef);

        final String actualParsedValue = itemHashUtil.normalizeMetadataUrl("Label", originalResourceReferencingMetadataValue);
        assertThat(actualParsedValue, equalTo(expectedResourceReferencingMetadataValue));
    }

    @Test
    public void testCanonicalSha256() throws IOException, NoSuchAlgorithmException {
        String ours = sha256(bytes("hashBase"));
        String theirs = itemHashUtil.hashString("hashBase");
        // from http://www.xorbin.com/tools/sha256-hash-calculator and
        // http://tomeko.net/online_tools/hex_to_base64.php
        String wellKnown = "yStr7Sx/4kg3bIjdI3CUTsihx64LU5huQVnIeItLPrI=";
        assertThat(theirs, equalTo(ours));
        assertThat(theirs, equalTo(wellKnown));
    }

    private void expectResourceLookupUnchecked(String[] resourceDef) {
        try {
            expectResourceLookup(resourceDef);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ContentResource expectResourceLookup(final String[] resourceDef) throws IdUnusedException, TypeException,
            PermissionException, ServerOverloadException, UnsupportedEncodingException {
        final ContentResource resource = mock(ContentResource.class);
        when(contentHostingService.getResource(idForContentResource(resourceDef))).thenReturn(resource);
        when(resource.getContentLength()).thenReturn(Long.parseLong(resourceDef[CR_SIZE_IDX]));
        when(resource.streamContent()).thenAnswer(invocation -> inputStreamForContentResource(resourceDef));
        return resource;
    }

    private void failResourceLookupUnchecked(String[] resourceDef) {
        try {
            failResourceLookup(resourceDef);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void failResourceLookup(String[] resourceDef) throws IdUnusedException, TypeException,
            PermissionException, ServerOverloadException, UnsupportedEncodingException {
        when(contentHostingService.getResource(idForContentResource(resourceDef))).thenThrow(IdUnusedException.class);
    }

    private void expectServerUrlLookup() {
        when(serverConfigurationService.getServerUrl()).thenReturn(SERVER_BASE_URL);
    }

    private InputStream inputStreamForContentResource(String[] contentResource) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(bytesForContentResource(contentResource));
    }

    private byte[] bytesForContentResource(String[] contentResource) throws UnsupportedEncodingException {
        return bytes(contentResource[CR_CONTENT_IDX]);
    }

    private byte[] bytes(String fromString) throws UnsupportedEncodingException {
        return fromString.getBytes("UTF-8");
    }

    private byte[] bytes(long fromLong) throws UnsupportedEncodingException {
        // yes, we know this is not the same as ByteBuffer.put(long)... but it matches what's
        // going on in the actual hash operations in ItemFacadeQueries where we encode as bytes
        // the string representation of the long
        return (""+fromLong).getBytes("UTF-8");
    }

    private String sha256(byte[] hashBase) throws NoSuchAlgorithmException {
        return hash(hashBase, "SHA-256");
    }

    private String hash(byte[] hashBase, String algorithm) throws NoSuchAlgorithmException {
        if ( hashBase == null ) { return null; }
        final MessageDigest algo = MessageDigest.getInstance(algorithm);
        final byte[] digest = algo.digest(hashBase);
        return Base64.encodeBase64String(digest);
    }

    private byte[] hashBaseBytesForContentResource(String[] contentResource) throws UnsupportedEncodingException {
        final byte[] contentBytes = bytes(contentResource[CR_CONTENT_IDX]);
        final byte[] lengthBytes = bytes(Long.parseLong(contentResource[CR_SIZE_IDX]));
        return ByteBuffer.allocate(contentBytes.length + lengthBytes.length)
                .put(contentBytes)
                .put(lengthBytes)
                .array();
    }

    private String expectedContentResourceHash1(String[] contentResource)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if ( contentResource == null ) { return null; }
        final byte[] hashBaseBytes = hashBaseBytesForContentResource(contentResource);
        return "Resources:" + sha256(hashBaseBytes) + "::";
    }

    private String expectedContentResourceHash(ArrayList<String[]> contentResources)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Iterator<String[]> it = contentResources.iterator();
        StringBuilder answer = new StringBuilder();

        while (it.hasNext()) {
            String[] contentResource = it.next();
            if (contentResource != null) {
                answer.append("Resources:" + sha256(hashBaseBytesForContentResource(contentResource)) + "::");
            }
        }
        return answer.toString();
    }

    private String expectedContentResourceHashAttachments(ArrayList<String[]> contentResources)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Iterator<String[]> it = contentResources.iterator();
        StringBuilder answer = new StringBuilder();
        if(contentResources.size()>0){
            answer.append("Resources:");
        }
        while (it.hasNext()) {
            String[] contentResource = it.next();
            if (contentResource != null) {
                answer.append(sha256(hashBaseBytesForContentResource(contentResource)));
            }
        }
        if(contentResources.size()>0){
            answer.append("::");
        }
        return answer.toString();
    }

    private String fullUrlForContentResource(String[] contentResource) {
        return contentResource == null ? null : SERVER_BASE_URL + serverRelativeUrlForContentResource(contentResource);
    }

    private String serverRelativeUrlForContentResource(String[] contentResource) {
        return contentResource == null ? null : CONTENT_BASE_PATH + idForContentResource(contentResource);
    }

    private String idForContentResource(String[] contentResource) {
        return contentResource[CR_GROUP_IDX] + "/" + contentResource[CR_NAME_IDX];
    }

    // copy/paste of some special handling for fill-in-the-blank question text in ItemData.getText()
    private String renderBlanks(String text) {
        return text.replaceAll("\\{","__").replaceAll("\\}","__");
    }

    private String resourceDocTemplate1(Object... interpolations) {
        return resourceDocTemplate(HTML_DOC_TEMPLATE_1, interpolations);
    }

    private String resourceDocTemplate1b(Object... interpolations) {
        return resourceDocTemplate(HTML_DOC_TEMPLATE_1b, interpolations);
    }

    private String resourceDocTemplate2(Object... interpolations) {
        return resourceDocTemplate(HTML_DOC_TEMPLATE_2, interpolations);
    }

    private String resourceDocTemplate(String template, Object... interpolations) {
        return (ArrayUtils.isEmpty(interpolations) || Arrays.equals(interpolations, new Object[] {null}))
                ? null : String.format(template, interpolations);
    }

    private static final String CONTENT_BASE_PATH = "/access/content";
    private static final String SERVER_BASE_URL = "http://localhost:8081";
    private static final int CR_GROUP_IDX = 0;
    private static final int CR_NAME_IDX = 1;
    private static final int CR_SIZE_IDX = 2;
    private static final int CR_CONTENT_IDX = 3;
    // each entry these have to sort lexically by its first nested element (a group+context ID)
    private static final String[][] CONTENT_RESOURCES = {
            {
                    "/group/contextId000",
                    "foo.png",
                    "2",
                    "foo content foo content foo content"
            },
            {
                    "/group/contextId001",
                    "bar.png",
                    "4",
                    "bar content bar content bar content"
            },
            {
                    "/group/contextId002",
                    "baz.png",
                    "8",
                    "baz content baz content baz content"
            },
            {
                    "/group/contextId003",
                    "bap.png",
                    "16",
                    "bap content bap content bap content"
            },
            {
                    "/group/contextId004",
                    "bam.png",
                    "32",
                    "bam content bam content bam content"
            },
            {
                    "/group/contextId005",
                    "bat.png",
                    "64",
                    "bat content bat content bat content"
            },
            {
                    "/group/contextId006",
                    "baq.png",
                    "128",
                    "baq content baq content baq content"
            },
            {
                    "/group/contextId007",
                    "baw.png",
                    "256",
                    "baw content baw content baw content"
            },
            {
                    "/group/contextId008",
                    "bas.png",
                    "512",
                    "bas content bas content bas content"
            },
            {
                    "/group/contextId009",
                    "bad.png",
                    "1024",
                    "bad content bad content bad content"
            },
            {
                    "/group/contextId010",
                    "baf.png",
                    "2048",
                    "baf content baf content baf content"
            },
            {
                    "/group/contextId011",
                    "bag.png",
                    "4096",
                    "bag content bag content bag content"
            },
            {
                    "/group/contextId012",
                    "bah.png",
                    "8192",
                    "bah content bah content bah content"
            },
            {
                    "/group/contextId013",
                    "baj.png",
                    "16384",
                    "baj content baj content baj content"
            },
            {
                    "/group/contextId014",
                    "bak.png",
                    "32768",
                    "bak content bak content bak content"
            },
            {
                    "/group/contextId015",
                    "bal.png",
                    "65536",
                    "bal content bal content bal content"
            },
            {
                    "/group/contextId016",
                    "ban.png",
                    "131072",
                    "ban content ban content ban content"
            },
            {
                    "/group/contextId017",
                    "bao.png",
                    "262144",
                    "bao content bao content bao content"
            },
            {
                    "/group/contextId018",
                    "bau.png",
                    "262144",
                    "bau content bau content bau content"
            }
    };

    private static final String HTML_DOC_TEMPLATE_1 = "<p>I {response} these images:</p>\n" +
            "\n" +
            "<p><img alt=\"\" height=\"760\" src=\"%s\" width=\"1082\" /></p>";
    private static final String HTML_DOC_TEMPLATE_1b = "\n" +
            "<p><img alt=\"\" height=\"760\" src=\"%s\" width=\"1082\" /></p>";
    private static final String HTML_DOC_TEMPLATE_2 = "<p>I {response} these images:</p>\n" +
            "\n" +
            "<p><img alt=\"\" height=\"760\" src=\"%s\" width=\"1082\" /></p>" +
            "\n" +
            "<p><img alt=\"\" height=\"760\" src=\"%s\" width=\"1082\" /></p>";
}
