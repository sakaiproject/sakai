/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/DiscussionParser.java $
 * $Id: DiscussionParser.java 227 2011-01-08 18:26:55Z drchuck $
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

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

public class DiscussionParser extends AbstractParser implements ContentParser {
  
  private static final Namespace DT_NS = Namespace.getNamespace("dt", "http://www.imsglobal.org/xsd/imsdt_v1p0");
  
  private static final String ATTACHMENT_QUERY = "attachments/attachment";
  
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
      // none of this is used
      if (false) {
    try {
      //ok, so we're looking at a discussion topic here...
      Element discussion = getXML(the_cartridge, ((Element)the_resource.getChildren(FILE, the_handler.getNs().cc_ns()).get(0)).getAttributeValue(HREF));
      Namespace topicNs = the_handler.getNs().topic_ns();
      the_handler.startDiscussion(discussion.getChildText(TITLE, topicNs),
                                  discussion.getChild(TEXT, topicNs).getAttributeValue(TEXTTYPE),
                                  discussion.getChildText(TEXT, topicNs),
                                  isProtected);
      the_handler.setDiscussionXml(discussion);
      //discussion may have attachments
      XPath path = XPath.newInstance(ATTACHMENT_QUERY);
      if (!topicNs.equals(Namespace.NO_NAMESPACE))
	  path.addNamespace(topicNs); 
      for (Iterator iter=path.selectNodes(discussion).iterator(); iter.hasNext();) {
        the_handler.addAttachment(((Element)iter.next()).getAttributeValue(HREF));
      }
      the_handler.endDiscussion();
    } catch (IOException e) {
      throw new ParseException(e);
    } catch (JDOMException je) {
      throw new ParseException(je);
    }
  }  
  }
}
