/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
 * $URL$
 * $Id$
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

import lombok.extern.slf4j.Slf4j;

import org.jdom.Element;
import org.jdom.Namespace;

@Slf4j
public class LtiParser extends AbstractParser implements ContentParser {
  private static final Namespace LT_NS = Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsbasiclti_v1p0");
  
  private static final String FILE="file";
  private static final String HREF="href";
  private static final String TEXT="text";
  private static final String TITLE="title";
  private static final String TEXTTYPE="texttype";
  
  public void 
  parseContent(DefaultHandler the_handler,
               CartridgeLoader the_cartridge, 
               Element the_resource,
               boolean isProtected) throws ParseException {
    try {
      //ok, so we're looking at a discussion topic here...
      Element blti = getXML(the_cartridge, ((Element)the_resource.getChildren(FILE, the_handler.getNs().cc_ns()).get(0)).getAttributeValue(HREF));
      log.info("blti="+blti);
      Namespace topicNs = the_handler.getNs().blti_ns();
      the_handler.startLti(blti.getChildText(TITLE, topicNs),
                                  blti.getChild(TEXT, topicNs).getAttributeValue(TEXTTYPE),
                                  blti.getChildText(TEXT, topicNs),
                                  isProtected);
      the_handler.setLtiXml(blti);
      the_handler.endLti();
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }  
}
