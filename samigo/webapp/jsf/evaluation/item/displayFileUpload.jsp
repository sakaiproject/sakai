<%-- $Id: displayFileUpload.jsp,v 1.7 2005/06/12 23:08:05 daisyf.stanford.edu Exp $
include file for displaying file upload questions
should be included in file importing DeliveryMessages
--%>
<h:outputText value="#{question.text}"  escape="false"/>
<f:verbatim><br /></f:verbatim>

      <%-- media list, note that question is ItemContentsBean --%>
      <h:dataTable value="#{question.mediaArray}" var="media">
        <h:column>
          <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputLink value="/samigo/servlet/ShowMedia?mediaId=#{media.mediaId}" target="new_window">
             <h:outputText escape="false" value="#{media.filename}" />
          </h:outputLink>
        </h:column>
        <h:column>
         <h:outputText value="("/>
         <h:outputText value="#{media.createdDate}">
           <f:convertDateTime pattern="MM/dd/yyyy" />
         </h:outputText>
         <h:outputText value=")"/>
        </h:column>
      </h:dataTable>


