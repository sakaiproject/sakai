<%-- $Id$
include file for displaying file upload questions
should be included in file importing DeliveryMessages
--%>
<!--
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
-->

      <%-- media list, note that question is ItemContentsBean --%>
  <h:dataTable value="#{description.itemGradingArrayList}" var="itemGradingArrayList">
    <h:column>
	  <h:outputText value="#{description.answer}" escape="false" rendered="#{itemGradingArrayList.mediaSize == 0}" />
      <h:dataTable value="#{itemGradingArrayList.mediaArray}" var="media">
        <h:column>
          <h:outputLink title="#{evaluationMessages.t_fileUpload}" value="/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}" target="new_window">
             <h:outputText value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="#{evaluationMessages.open_bracket}"/>
         	<h:outputText value="#{media.fileSizeKBFormat} #{generalMessages.kb}"/>
         <h:outputText value="#{evaluationMessages.close_bracket}"/>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>


