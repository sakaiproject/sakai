<%-- $Id$
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

<h:outputText value="#{question.text}"  escape="false"/>
<f:verbatim><br /></f:verbatim>

      <%-- media list, note that question is ItemContentsBean --%>
  <h:dataTable value="#{description.itemGradingArrayList}" var="itemGradingArrayList">
    <h:column>
      <h:dataTable value="#{itemGradingArrayList.mediaArray}" var="media">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink title="#{msg.t_fileUpload}" value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}&sam_fileupload_siteId=#{delivery.siteId}&createdBy=#{question.itemData.createdBy}" target="new_window">
             <h:outputText escape="false" value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="#{msg.open_bracket}"/>
         <h:outputText value="#{media.createdDate}">
           <f:convertDateTime pattern="#{msg.grading_date_no_time_format}" />
         </h:outputText>
         <h:outputText value="#{msg.close_bracket}"/>
        </h:column>
      </h:dataTable>
    </h:column>
  </h:dataTable>


