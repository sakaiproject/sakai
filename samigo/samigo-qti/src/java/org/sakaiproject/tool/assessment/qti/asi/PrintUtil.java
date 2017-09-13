/**
 * Copyright (c) 2005-2014 The Apereo Foundation
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
package org.sakaiproject.tool.assessment.qti.asi;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.sakaiproject.tool.assessment.data.ifc.assessment.AnswerIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemDataIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextAttachmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.ItemTextIfc;

public class PrintUtil {

    private static StringBuilder buf = new StringBuilder();

    public static String printItem(ItemDataIfc item) {
        pNewItem(item);
//	p("ItemId", item.getItemId());// item/@ident
        p("ThemeText", item.getThemeText());// item/@label
        p("IsAnswerOptionsSimple", item.getIsAnswerOptionsSimple());// item/presentation/@label
        p("LeadInText", item.getLeadInText());// item/presentation/flow/material/mattext
        p("Sequence", item.getSequence());// item/presentation/flow/response_lid/@ident
        p("Score", item.getScore());// item/resprocessing/outcomes/decvar/@maxvalue
        p("Discount", item.getDiscount());// item/resprocessing/outcomes/decvar/@minvalue
        p("AnswerKey", item.getAnswerKey());
        p("AnswerOptionsRichCount", item.getAnswerOptionsRichCount());
//      p("CorrectItemFeedback", item.getCorrectItemFeedback());
//	p("CreatedBy", item.getCreatedBy());
//	p("CreatedDate", item.getCreatedDate());
//      p("Description", item.getDescription());
//      p("Duration", item.getDuration());
        p("EmiAnswerOptionLabels", item.getEmiAnswerOptionLabels());
        p("EmiAnswerOptionsRichText", item.getEmiAnswerOptionsRichText());
//      p("GeneralItemFeedback", item.getGeneralItemFeedback());
//	p("Grade", item.getGrade());
//	p("HasRationale", item.getHasRationale());
//	p("Hint", item.getHint());
//	p("InCorrectItemFeedback", item.getInCorrectItemFeedback());
//	p("Instruction", item.getInstruction());
//	p("IsTrue", item.getIsTrue());
//	p("ItemIdString", item.getItemIdString());
//	p("LastModifiedBy", item.getLastModifiedBy());
//	p("LastModifiedDate", item.getLastModifiedDate());
        p("NumberOfCorrectEmiOptions", item.getNumberOfCorrectEmiOptions());
        p("PartialCreditFlag", item.getPartialCreditFlag());
        p("Status", item.getStatus());
//	p("Text", item.getText());
//	p("TriesAllowed", item.getTriesAllowed());
//	p("Type", item.getType());
//	p("TypeId", item.getTypeId());
//	pNewData("EmiAnswerOptions");///
//	for (AnswerIfc answer : item.getEmiAnswerOptions()) {
//          p("CorrectAnswerFeedback", answer.getCorrectAnswerFeedback());
//          p("Discount", answer.getDiscount());
//          p("GeneralAnswerFeedback", answer.getGeneralAnswerFeedback());
//          p("Grade", answer.getGrade());
//          p("Id", answer.getId());
//          p("InCorrectAnswerFeedback", answer.getInCorrectAnswerFeedback());
//          p("IsCorrect", answer.getIsCorrect());
//          p("Label", answer.getLabel());
//          p("PartialCredit", answer.getPartialCredit());
//          p("Score", answer.getScore());
//          p("Sequence", answer.getSequence());
//          p("Text", answer.getText());
//          p("ItemText", answer.getItemText());
//          pNewData("AnswerFeedbackSet");
//          for(AnswerFeedbackIfc af: answer.getAnswerFeedbackSet()){
//              p("", af.getId());
//              p("", af.getText());
//              p("", af.getTypeId());
//          }
//          pEndData();//AnswerFeedbackSet
//	}
//	pEndData();//EmiAnswerOptions
//	pNewData("EmiQuestionAnswerCombinations");
//	for(ItemTextIfc it: item.getEmiQuestionAnswerCombinations()){
//          p(it.getId().toString(), it);
//	}
//	pEndData();//EmiQuestionAnswerCombinations
        pa(item.getItemAttachmentSet());

//	pNewData("ItemFeedbackSet");
//	for(ItemFeedbackIfc ifb: item.getItemFeedbackSet()){
//	p("Id", ifb.getId());
//	p("Text", ifb.getText());
//	p("TypeId", ifb.getTypeId());
//	}
//	pEndData();//ItemFeedbackSet
//	pNewData("ItemMetaDataSet");//handled by global Item?
//	for(ItemMetaDataIfc im: item.getItemMetaDataSet()){
//	//p("Id", im.getId());
//	p("Label", im.getLabel());
//	p("Entry", im.getEntry());
//	}
//	pEndData();//ItemMetaDataSet
        pt(item.getItemTextArraySorted());

        pEndData();// item
        String data = buf.toString();
        buf.setLength(0);
        return data;
    }

    private static void pt(Collection<ItemTextIfc> itemTextSet) {
        if (itemTextSet == null) {
            return;
        }
        pNewData("ItemTextSet");
        for (ItemTextIfc it : itemTextSet) {
            p(String.valueOf(it.getId()), it);
        }
        pEndData();// ItemTextSet
    }

    private static void pa(Set<ItemAttachmentIfc> attachSet) {
        if (attachSet == null) {
            return;
        }
        pNewData("ItemAttachmentSet");
        for (ItemAttachmentIfc ia : attachSet) {
//          p("AttachmentId", ia.getAttachmentId());
            p("AttachmentType", ia.getAttachmentType());
//          p("CreatedBy", ia.getCreatedBy());
//          p("CreatedDate", ia.getCreatedDate());
            p("Description", ia.getDescription());
            p("Filename", ia.getFilename());
            p("FileSize", ia.getFileSize());
            p("IsLink", ia.getIsLink());
//          p("LastModifiedBy", ia.getLastModifiedBy());
//          p("LastModifiedDate", ia.getLastModifiedDate());
            p("Location", ia.getLocation());
            p("MimeType", ia.getMimeType());
            p("ResourceId", ia.getResourceId());
            p("Status", ia.getStatus());
        }
        pEndData();// ItemAttachmentSet
    }

    private static void p(String label, ItemTextIfc itemText) {
        pNewData(label);
        p("EmiCorrectOptionLabels", itemText.getEmiCorrectOptionLabels());
//	p("Id", itemText.getId());
        p("RequiredOptionsCount", itemText.getRequiredOptionsCount());
        p("Sequence", itemText.getSequence());
        p("Text", itemText.getText());
        p("HasAttachment", itemText.getHasAttachment());
        p("isEmiQuestionItemText", itemText.isEmiQuestionItemText());
        pan(itemText.getAnswerSet());
        patt(itemText.getItemTextAttachmentSet());

        pEndData();// ItemTextIfc
    }

    private static void pan(Set<AnswerIfc> answerSet) {
        if (answerSet == null) {
            return;
        }
        pNewData("AnswerSet");
        for (AnswerIfc answer : answerSet) {
            pNewData(answer.getSequence().toString());
//          p("Id", answer.getId());
            p("Sequence", answer.getSequence());
            p("Label", answer.getLabel());
            p("Text", answer.getText());

            p("IsCorrect", answer.getIsCorrect());
            p("Score", answer.getScore());
            p("Discount", answer.getDiscount());

            // p("CorrectAnswerFeedback", answer.getCorrectAnswerFeedback());
            // p("GeneralAnswerFeedback", answer.getGeneralAnswerFeedback());
            // p("Grade", answer.getGrade());
            // p("InCorrectAnswerFeedback",
            // answer.getInCorrectAnswerFeedback());
            // p("PartialCredit", answer.getPartialCredit());
            // p("ItemText", answer.getItemText());// Bad circular ref
            // pNewData("AnswerFeedbackSet");
            // for(AnswerFeedbackIfc af: answer.getAnswerFeedbackSet()){
            // p("", af.getId());
            // p("", af.getText());
            // p("", af.getTypeId());
            // }
            // pEndData();//AnswerFeedbackSet
            pEndData();// Answer
        }
        pEndData();// AnswerSet
    }

    private static void patt(Set<ItemTextAttachmentIfc> attachSet) {
        if (attachSet == null) {
            return;
        }
        pNewData("ItemTextAttachmentSet");
        for (ItemTextAttachmentIfc ita : attachSet) {
//          p("AttachmentId", ita.getAttachmentId());
            p("AttachmentType", ita.getAttachmentType());
//          p("CreatedBy", ita.getCreatedBy());
//          p("CreatedDate", ita.getCreatedDate());
            p("Description", ita.getDescription());
            p("Filename", ita.getFilename());
            p("FileSize", ita.getFileSize());
            p("IsLink", ita.getIsLink());
//          p("LastModifiedBy", ita.getLastModifiedBy());
//          p("LastModifiedDate", ita.getLastModifiedDate());
            p("Location", ita.getLocation());
            p("MimeType", ita.getMimeType());
            p("ResourceId", ita.getResourceId());
            p("Status", ita.getStatus());
        }
        pEndData();// ItemTextAttachmentSet
    }

    private static int pTab = 0;

    private static void pNewItem(ItemDataIfc item) {
        pTab = 0;
        p("************ " + item.getItemIdString() + ": " + item.getThemeText()
                + " **************");
    }

    private static void pNewData(String text) {
        p("----- " + text + " -----");
        pTab++;
    }

    private static void pEndData() {
        pTab--;
        p("----- End -----");
    }

    @SuppressWarnings("unused")
    private static void p(String label, Object text) {
        p(label, (text == null ? "Object" : text.getClass().getSimpleName()),
                (text == null ? "null" : text.toString()));
    }

    private static void p(String label, String text) {
        p(label, "String", text);
    }

    private static void p(String label, Boolean text) {
        p(label, "Boolean", String.valueOf(text));
    }

    private static void p(String label, Integer text) {
        p(label, "Integer", String.valueOf(text));
    }

    private static void p(String label, Long text) {
        p(label, "Long", String.valueOf(text));
    }

    private static void p(String label, Float text) {
        p(label, "Float", String.valueOf(text));
    }

    private static void p(String label, Double text) {
        p(label, "Double", String.valueOf(text));
    }

    private static void p(String label, Date text) {
        p(label, "Date", String.valueOf(text));
    }

    private static void p(String label, String type, String text) {
        p(label + "(" + type + "): " + text);
    }

    private static void p(String text) {
        for (int i = 0; i < pTab; i++) {
            buf.append("\t");
        }
        buf.append(text);
        buf.append("\n");
    }
}
