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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/WebLinkParser.java $
 * $Id: WebLinkParser.java 227 2011-01-08 18:26:55Z drchuck $
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

import org.jdom.Element;
import org.jdom.Namespace;

public class WebLinkParser extends AbstractParser implements ContentParser {

  private static final String FILE="file";
  private static final String HREF="href";
  private static final String URL="url";
  private static final String TITLE="title";
  private static final String TARGET="target";
  private static final String WINDOW_FEATURES="windowFeatures";
  
  public void 
  parseContent(DefaultHandler the_handler,
               CartridgeLoader the_cartridge, 
               Element the_resource,
               boolean isProtected) throws ParseException {
    try {
      //ok, so we're looking at a web link here...
      Element link = getXML(the_cartridge, ((Element)the_resource.getChildren(FILE, the_handler.getNs().cc_ns()).get(0)).getAttributeValue(HREF));
      Namespace linkNs = the_handler.getNs().link_ns();
      Element urlElement = link.getChild(URL, linkNs);
      
      the_handler.startWebLink(link.getChildText(TITLE, linkNs),
                               urlElement.getAttributeValue(HREF),
                               urlElement.getAttributeValue(TARGET),
                               urlElement.getAttributeValue(WINDOW_FEATURES),
                               isProtected);
      the_handler.setWebLinkXml(link);                         
      the_handler.endWebLink();
    } catch (IOException e) {
      throw new ParseException(e);
    }
  }
}
