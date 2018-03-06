/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
 * $URL: http://ims-dev.googlecode.com/svn/trunk/cc/IMS_CCParser_v1p0/src/main/java/org/imsglobal/cc/DefaultHandler.java $
 * $Id: DefaultHandler.java 227 2011-01-08 18:26:55Z drchuck $
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

import org.jdom.Element;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;

public class DefaultHandler implements AssessmentHandler, DiscussionHandler, AuthorizationHandler,
                                       MetadataHandler, LearningApplicationResourceHandler, 
                                       QuestionBankHandler, LtiHandler,
                                       WebContentHandler, WebLinkHandler {

  Ns ns = null;
  public void setNs(Ns n) {
      ns = n;
  }

  public Ns getNs() {
      return ns;
  }

  SimplePageBean simplePageBean = null;
  
  public SimplePageBean getSimplePageBean() {
      return simplePageBean;
  }

  public void endAssessment() {}

  public void setAssessmentXml(Element the_xml) {}

  public void setPresentationXml(Element the_xml) {}

  public void setQTIComment(String the_comment) {}

  public void setQTICommentXml(Element the_xml) {}

  public void setSection(String ident, String title) {}

  public void setSectionXml(Element the_xml) {}

  public void startAssessment(String the_file_name, boolean isProtected) {}

  //public void addQTIItemFeedbackXml(Element the_feedback) {}

 // public void addQTIItemResponseProcessingXml(Element the_resp) {}

  public void addQTIMetadataField(String label, String entry) {}

  public void addQTIMetadataXml(Element the_md) {}

  //public void endQTIItem() {}

  public void endQTIMetadata() {}

  public void setQuestionBankDetails(String id) {}

  //public void setQTIItemMetadataXml(Element the_md) {}

  //public void setQTIItemPresentationXml(Element the_md) {}

  //public void setQTIItemXml(Element xmltext) {}

  //public void startQTIItem(String id, String title) {}

  public void startQTIMetadata() {}

  public void endCCFolder() {}

  public void endCCItem() {}

  public void endDependency() {}

  public void endManifest() {}

  public void endResource() {}

  public void setCCItemXml(Element the_xml, Element resource, AbstractParser parser, CartridgeLoader loader, boolean nopage) {}

  public void setManifestXml(Element the_xml) {}

  public void setResourceXml(Element the_xml) {}

  public void startCCFolder(Element folder) {}

  public void startCCItem(String the_id, String the_title) {}

  public void startDependency(String source, String target) {}

  public void startManifest() {}

  public void startResource(String id, boolean isProtected) {}

  public void addAttachment(String attachment_path) {}

  public void startDiscussion(String topic_name, String text_type, String text,
      boolean isProtected) {}

  public void endDiscussion() {}

  public void startLti(String topic_name, String text_type, String text,
      boolean isProtected) {}

  public void endLti() {}

  public void setAuthorizationService(String cartridgeId, String webservice_url) {}

  public void setAuthorizationServiceXml(Element the_node) {}

  public void endAuthorization() {}

  public void startAuthorization(boolean isCartridgeScope,
      boolean isResourceScope, boolean isImportScope) {}

  public void checkCurriculum(Element the_md) {}

  public void setManifestMetadataXml(Element the_md) {}

  public void endManifestMetadata() {}

  public void setResourceMetadataXml(Element the_md) {}

  public void startManifestMetadata(String schema, String schema_version) {}

  public void addFile(Element elem) {}

  public void preProcessFile(String the_file_id) {}

  public void addFile(String the_file) {}

  public void endLearningApplicationResource() {}

  public void startLearningApplicationResource(String entry_point,
      boolean isProtected) {}

  public void endQuestionBank() {}

  public void setQuestionBankXml(Element the_xml) {}

  public void startQuestionBank(String the_file_name, boolean isProtected) {}

  public void endWebContent() {}

  public void startWebContent(String entry_point, boolean isProtected) {}

  public void endWebLink() {}

  public void setWebLinkXml(Element the_xml) {}

  public void startWebLink(String the_title, String the_url, String the_target,
      String the_window_features, boolean isProtected) {}

  public void setAssessmentDetails(String the_ident, String the_title) {}

  public void addAssessmentItem(QTIItem the_item) {}

  public void addQuestionBankItem(QTIItem the_item) {}

  public void setDiscussionXml(Element the_element) {}

  public void setLtiXml(Element the_element) {}
  
}
