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
<!-- ATTACHMENTS -->

<h:outputText value="#{printMessages.attachments} " escape="false" rendered="#{not empty itemAttachmentList && delivery.fromPrint}"/>

<div class="accordion my-3" id="attachmentsAccordion">

  <h:dataTable value="#{itemAttachmentList}" var="attach" border="0" styleClass="w-100 table table-borderless m-0 p-0" columnClasses="p-0" cellpadding="0" cellspacing="0">
    <h:column>
      
      <div class="accordion-item border rounded">
        
        <span class="accordion-header d-flex align-items-center px-3 py-2 m-0" style="border-radius: inherit;">
          
          <a href="<h:outputText value="#{attach.location}"/>" 
             download="<h:outputText value="#{attach.filename}"/>" 
             aria-label="<h:outputText value="#{commonMessages.action_download}"/>" 
             class="btn btn-secondary me-3 bi bi-download"></a>

          <div class="flex-grow-1">
            <h:panelGroup rendered="#{!delivery.fromPrint}">
              <%@ include file="/jsf/shared/mimeicon.jsp" %>
            </h:panelGroup>
            <f:verbatim>&nbsp;</f:verbatim>
            
            <h:outputLink value="#{attach.location}" target="_blank" styleClass="text-decoration-none">
              <h:outputText value="#{attach.filename}" />
            </h:outputLink>
            
            <h:panelGroup rendered="#{!attach.isLink}">
              <span class='text-muted small'>(<h:outputText value="#{attach.fileSize}"/> <h:outputText value="#{generalMessages.kb}"/>)</span>
            </h:panelGroup>
          </div>

          <h:panelGroup rendered="#{attach.isMedia && !delivery.fromPrint}">
            <button class="accordion-button collapsed w-auto p-2 ms-3 rounded gap-3" type="button" data-bs-toggle="collapse" 
                    data-bs-target="[id='collapse-<h:outputText value="#{attach.encodedResourceId}"/>']" 
                    aria-expanded="false" 
                    aria-controls="collapse-<h:outputText value="#{attach.encodedResourceId}"/>">
              <h:outputText value="#{commonMessages.action_preview}"/>
            </button>
          </h:panelGroup>
          
        </span>

        <h:panelGroup rendered="#{attach.isMedia && !delivery.fromPrint}">
          
          <div id="collapse-<h:outputText value="#{attach.encodedResourceId}"/>" class="accordion-collapse collapse" data-bs-parent="#attachmentsAccordion">
            
            <div class="accordion-body border-top p-3">
              
              <h:panelGroup rendered="#{attach.isInlineVideo}">
                <embed src="<h:outputText value="#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}"/>" 
                       volume="50" 
                       autostart="false"
                       style="width: 100%; max-width: 400px; height: auto; max-height: 350px;" />
              </h:panelGroup>
              
              <h:panelGroup rendered="#{attach.isInlineImage}">
                <img src="<h:outputText value="#{delivery.protocol}/samigo-app/servlet/ShowAttachmentMedia?resourceId=#{attach.encodedResourceId}&amp;mimeType=#{attach.mimeType}&amp;filename=#{attach.filename}"/>" 
                     style="width: 100%; max-width: 400px; height: auto; max-height: 350px;" />
              </h:panelGroup>
                
            </div>
            
          </div>

        </h:panelGroup>

        <h:outputText value="#{attach.filename}" rendered="#{delivery.fromPrint}"/>

      </div>

    </h:column>
  </h:dataTable>

</div>
