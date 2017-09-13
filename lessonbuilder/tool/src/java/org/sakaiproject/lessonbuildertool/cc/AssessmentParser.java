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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/AssessmentParser.java $
 * $Id: AssessmentParser.java 227 2011-01-08 18:26:55Z drchuck $
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

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.jdom.Element;
import org.jdom.Namespace;


public class AssessmentParser extends AbstractQTIParser implements ContentParser {

  private static final String QTI_COMMENT="qticomment";
  private static final Namespace QTI_NS=Namespace.getNamespace("qti","http://www.imsglobal.org/xsd/ims_qtiasiv1p2");
  private static final String ASSESSMENT="assessment";
  private static final String SECTION="section";
  private static final String ITEM="item";
  private static final String IDENT="ident";
  private static final String TITLE="title";
  private static final String FILE="file";
  private static final String HREF="href";
  
  public void 
  parseContent(DefaultHandler handler,
               CartridgeLoader the_cartridge, 
               Element the_resource,
               boolean isProtected) throws ParseException {

      // this is now handled by the import object method for the appropriate testing engine.
      // I've seen this generate a spurious error, so don't do it
      if (false) {
    try {
      String href=((Element)the_resource.getChildren(FILE, handler.getNs().cc_ns()).get(0)).getAttributeValue(HREF);
      Element qti=getXML(the_cartridge,href);
      handler.startAssessment(href, isProtected);
      Element assessment=qti.getChild(ASSESSMENT, QTI_NS);
      handler.setAssessmentDetails(assessment.getAttributeValue(IDENT), 
                                   assessment.getAttributeValue(TITLE));
      handler.setAssessmentXml(qti);
      String qti_comment=assessment.getChildText(QTI_COMMENT, QTI_NS);
      if (qti_comment!=null) {
        handler.setQTIComment(qti_comment);
      }
      processQTIMetadata(assessment, handler);
      List items=assessment.getChild(SECTION, QTI_NS).getChildren(ITEM, QTI_NS);
      for (Iterator iter=items.iterator(); iter.hasNext();) {
        handler.addAssessmentItem(processItem((Element)iter.next()));
      }      
    } catch (IOException e) {
      throw new ParseException(e.getMessage(), e);
    } 
      }
    handler.endAssessment();
  }  
}
