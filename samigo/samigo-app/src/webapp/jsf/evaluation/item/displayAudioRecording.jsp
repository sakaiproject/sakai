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

<h:outputText value="#{question.text}"  escape="false"/>
<f:verbatim><br /></f:verbatim>

  <h:dataTable value="#{question.mediaArray}" var="media">
    <h:column>

      <h:outputText escape="false" value="
         <embed src=\"/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}\"
                volume=\"50\" height=\"25\" width=\"300\" autostart=\"false\"/>
         " />

      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{evaluationMessages.open_bracket}"/>
      <h:outputText value="#{media.duration} sec, recorded on " rendered="#{!media.durationIsOver}" />
      <h:outputText value="#{question.duration} sec, recorded on " rendered="#{media.durationIsOver}" />
      <h:outputText value="#{media.createdDate}">
        <f:convertDateTime pattern="#{evaluationMessages.grading_date_no_time_format}" />
      </h:outputText>
      <h:outputText value="#{evaluationMessages.close_bracket}"/>
      <f:verbatim><br /></f:verbatim>    
	  
	  <div>
      <h:outputText value="#{evaluationMessages.can_you_hear_1}"  escape="false"/>
      <h:outputLink value="/samigo-app/servlet/ShowMedia?mediaId=#{media.mediaId}&setMimeType=false">
        <h:outputText value=" #{evaluationMessages.can_you_hear_2} " escape="false" />
      </h:outputLink>
      <h:outputText value="#{evaluationMessages.can_you_hear_3}"  escape="false"/>
      </div>
	  
	</h:column>
  </h:dataTable>
