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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/QTIItemBuilder.java $
 * $Id: QTIItemBuilder.java 227 2011-01-08 18:26:55Z drchuck $
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

import java.util.List;
import java.util.Vector;

import org.jdom.Element;

public class QTIItemBuilder {

  private Element item;
  private String ident;
  private String title;
  
  private Element item_metadata;
  private Element presentation;
  private List<Element> item_feedbacks;
  private List<Element> resp_processings;
  
  public
  QTIItemBuilder() {
    item_feedbacks=new Vector<Element>();
    resp_processings=new Vector<Element>();
  }
  
  public Element getItem() {
    return item;
  }

  public String getIdent() {
    return ident;
  }

  public String getTitle() {
    return title;
  }

  public Element getItem_metadata() {
    return item_metadata;
  }

  public Element getPresentation() {
    return presentation;
  }

  public List<Element> getItem_feedbacks() {
    return item_feedbacks;
  }

  public List<Element> getResp_processings() {
    return resp_processings;
  }

  public QTIItemBuilder
  withIdent(String the_ident) {
    ident=the_ident;
    return this;
  }
  
  public QTIItemBuilder
  withTitle(String the_title) {
    title=the_title;
    return this;
  }
  
  public QTIItemBuilder
  withItem(Element the_item) {
    item=the_item;
    return this;
  }
  
  public QTIItemBuilder
  withMetadata(Element the_md) {
    item_metadata=the_md;
    return this;
  }
  
  public QTIItemBuilder
  withPresentation(Element the_pres) {
    presentation=the_pres;
    return this;
  }
  
  public QTIItemBuilder
  withItemFeedback(Element the_feedback) {
    item_feedbacks.add(the_feedback);
    return this;
  }
  
  public QTIItemBuilder
  withResponseProcessing(Element the_resp) {
    resp_processings.add(the_resp);
    return this;
  }
  
  public QTIItem 
  build() {
   return QTIItem.newQTIItem(this); 
  }
  
}
