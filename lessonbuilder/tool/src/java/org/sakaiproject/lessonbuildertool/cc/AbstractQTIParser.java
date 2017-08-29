/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.cc;

/***********
 * This code is based on a reference implementation done for the IMS Consortium.
 * The copyright notice for that implementation is included below. 
 * All modifications are covered by the following copyright notice.
 *
 * Copyright (c) 2011 Rutgers, the State University of New Jersey
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
 */

/**********************************************************************************
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/AbstractQTIParser.java $
 * $Id: AbstractQTIParser.java 227 2011-01-08 18:26:55Z drchuck $
 **********************************************************************************
 *
 * Copyright (c) 2010 IMS GLobal Learning Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. 
 *
 **********************************************************************************/

import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;

public abstract class AbstractQTIParser extends AbstractParser {

  private static final Namespace QTI_NS=Namespace.getNamespace("qti","http://www.imsglobal.org/xsd/ims_qtiasiv1p2");
  
  private static final String QTI_IDENT         ="ident";
  private static final String QTI_MD            ="qtimetadata";
  private static final String QTI_MD_FIELD      ="qtimetadatafield";
  private static final String QTI_MD_FIELDLABEL ="fieldlabel";
  private static final String QTI_MD_FIELDENTRY ="fieldentry";
  
  private static final String QTI_ITEM_MD       ="itemmetadata";
  private static final String QTI_PRESENTATION  ="presentation";
  private static final String QTI_ITEM_RESP     ="resprocessing";
  private static final String QTI_ITEM_FEEDBACK="itemfeedback";
  
  private static final String IDENT="ident";
  private static final String TITLE="title";
  
/*  public void
  setIdent(Element qti,
           DefaultHandler handler) {
    handler.setIdent(qti.getAttributeValue(IDENT));
  }
  */
  public void
  processQTIMetadata(Element the_md,
                     DefaultHandler the_handler) {
    if (the_md.getChild(QTI_MD, QTI_NS)!=null) {
      the_handler.startQTIMetadata();
      List md_fields=the_md.getChild(QTI_MD, QTI_NS).getChildren(QTI_MD_FIELD,QTI_NS);
      the_handler.addQTIMetadataXml(the_md);
      for (Iterator iter=md_fields.iterator(); iter.hasNext();) {
        Element md=(Element)iter.next();
        the_handler.addQTIMetadataField(md.getChildText(QTI_MD_FIELDLABEL, QTI_NS),
                                        md.getChildText(QTI_MD_FIELDENTRY, QTI_NS));
      }
      the_handler.endQTIMetadata();
    }
  }
  /*
  public void
  processQTIItemMetadata(Element the_md,
                         DefaultHandler the_handler) {
    if (the_md.getChild(QTI_MD, QTI_NS)!=null) {
      the_handler.startQTIItemMetadata();
      List md_fields=the_md.getChild(QTI_MD, QTI_NS).getChildren(QTI_MD_FIELD,QTI_NS);
      the_handler.setQTIItemMetadataXml(the_md);
      for (Iterator iter=md_fields.iterator(); iter.hasNext();) {
        Element md=(Element)iter.next();
        the_handler.addQTIItemMetadataField(md.getChildText(QTI_MD_FIELDLABEL, QTI_NS),
                                            md.getChildText(QTI_MD_FIELDENTRY, QTI_NS));
      }
      the_handler.endQTIItemMetadata();
    }
  }
*/  
  public QTIItem
  processItem(Element the_item) {
    QTIItemBuilder builder=new QTIItemBuilder();
    builder=builder.withIdent(the_item.getAttributeValue(IDENT));
    builder=builder.withTitle(the_item.getAttributeValue(TITLE));
    builder=builder.withItem(the_item);
    Element item_metadata = the_item.getChild(QTI_ITEM_MD, QTI_NS);
    if (item_metadata!=null) {
      builder=builder.withMetadata(item_metadata);
    }
    Element item_presentation = the_item.getChild(QTI_PRESENTATION, QTI_NS);
    if (item_presentation!=null) {
      builder=builder.withPresentation(item_presentation);
    }
    List item_feedbacks=the_item.getChildren(QTI_ITEM_FEEDBACK, QTI_NS);
    if (item_feedbacks!=null) {
      for (Iterator iter=item_feedbacks.iterator();iter.hasNext();) {
        builder=builder.withItemFeedback((Element)iter.next());   
      }
    }
    List item_reps=the_item.getChildren(QTI_ITEM_RESP, QTI_NS);
    if (item_reps!=null) {
      for (Iterator iter=item_reps.iterator();iter.hasNext();) {
        builder=builder.withResponseProcessing((Element)iter.next());
      }
    }
    return builder.build();
  }
}
