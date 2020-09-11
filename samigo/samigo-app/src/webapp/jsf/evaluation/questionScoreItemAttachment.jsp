<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
**********************************************************************************/
--%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<!-- ATTACHMENTS -->
<%-- Similar to /jsf/delivery/item/attachment.jsp: attachment.jsp expects 'question' is an instance of ItemContentsBean, while questionScore expects 'question' is a PublishedItemData instance --%>
<t:aliasBean alias="#{itemAttachmentList}" value="#{question.itemAttachmentList}" >
  <%@ include file="/jsf/shared/itemAttachmentList.jsp" %>
</t:aliasBean>
