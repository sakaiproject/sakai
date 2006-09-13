<%-- $Id: displayFileUploadAnswer.jsp 6874 2006-03-22 17:01:47Z hquinn@stanford.edu $
include file for displaying file upload questions
should be included in file importing DeliveryMessages
--%>
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->

<h:panelGroup>
  <h:dataTable value="#{description.itemGradingArrayList}" var="itemGradingArrayList" cellpadding="10">
    <h:column>
      <h:outputText value="#{description.answer}" escape="false" rendered="#{itemGradingArrayList.mediaSize == 0}" />
	  <h:dataTable value="#{itemGradingArrayList.mediaArray}" var="media">
	    <h:column>
      <h:outputText escape="false" value="
         <embed src=\"/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}&fromLink=true\"
                volume=\"50\" height=\"25\" width=\"300\" autostart=\"false\"/>
         " />

      <f:verbatim><br /></f:verbatim>
      <h:outputText value="#{msg.open_bracket}"/>
      <h:outputText value="#{media.duration} sec, recorded on " rendered="#{!media.durationIsOver}" />
      <h:outputText value="#{question.duration} sec, recorded on " rendered="#{media.durationIsOver}" />
	  <h:outputText value="#{media.createdDate}">
        <f:convertDateTime pattern="#{msg.grading_date_no_time_format}" />
      </h:outputText>
      <h:outputText value="#{msg.close_bracket}"/>
      <f:verbatim><br /></f:verbatim>
	  
	  <div>
      <h:outputText value="#{msg.can_you_hear_1}"  escape="false"/>
      <h:outputLink value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}&setMimeType=false">
        <h:outputText value=" #{msg.can_you_hear_2} " escape="false" />
      </h:outputLink>
      <h:outputText value="#{msg.can_you_hear_3}"  escape="false"/>
      </div>

     </h:column>
  </h:dataTable>
       </h:column>
  </h:dataTable>
</h:panelGroup>
