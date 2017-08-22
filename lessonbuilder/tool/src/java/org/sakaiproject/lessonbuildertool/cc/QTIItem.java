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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/QTIItem.java $
 * $Id: QTIItem.java 227 2011-01-08 18:26:55Z drchuck $
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

/*
 * This class has been created as both QTI assessments and QTI item banks can appear in the same cartridge.
 * This means that defaulthandler would need one set of methods to add an item to an assessment, and then another
 * set of methods to add an item to a question bank. One cannot simply subclass methods as then things parsed in a question
 * bank could fire events in an assessment(!)
 * 
 * However implementing two lots of methods that do roughly the same thing feels wrong, hence the parser will create
 * this QTIItem, and then add it where it needs to go. Users will then need to extract the items and do something with
 * them.
 */

public class QTIItem {

  private Element item;
  private String ident;
  private String title;
  
  private Element item_metadata;
  private Element presentation;
  private List<Element> item_feedbacks;
  private List<Element> resp_processings;
  
  private QTIItem() {}
  
  public Element getItem() {
    return (Element)item.clone();
  }

  public String getIdent() {
    return ident;
  }

  public String getTitle() {
    return title;
  }

  public Element getItem_metadata() {
    return (Element)item_metadata.clone();
  }

  public Element getPresentation() {
    return (Element)presentation.clone();
  }

  public List<Element> getItem_feedbacks() {
    return new Vector<Element>(item_feedbacks);
  }

  public List<Element> getResp_processings() {
    return new Vector<Element>(resp_processings);
  }

  public String 
  toString() {
    StringBuffer sb=new StringBuffer("QTI Item: ");
    sb.append(ident);
    sb.append(": title: ");
    sb.append(title);
    return sb.toString();
  }
  
  public static QTIItem
  newQTIItem(QTIItemBuilder builder) {
    QTIItem result=new QTIItem();
    result.ident=builder.getIdent();
    result.title=builder.getTitle();
    if (builder.getItem()!=null) {
      result.item=(Element)builder.getItem().clone();
    }
    if (builder.getItem_metadata()!=null) {
      result.item_metadata=(Element)builder.getItem_metadata().clone();
    }
    if (builder.getPresentation()!=null) {
      result.presentation=(Element)builder.getPresentation().clone();
    }
    result.item_feedbacks=new Vector<Element>(builder.getItem_feedbacks());
    result.resp_processings=new Vector<Element>(builder.getResp_processings());
    return result;
  }
  
}
