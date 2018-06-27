      
  <!-- THEME TEXT -->
  	<f:verbatim><h3></f:verbatim>
      <h:outputText styleClass="questionBlock" escape="false" value="#{question.itemData.themeText}" />
      <f:verbatim></h3><br/></f:verbatim>

   <!-- SIMPLE TEXT - EMI SIMPLE TEXT OPTIONS-->
      <h:dataTable value="#{question.itemData.emiAnswerOptions}" var="option"  rendered="#{question.itemData.isAnswerOptionsSimple}" cellpadding="3">
        <h:column> 
            <h:outputText styleClass="questionBlock"  escape="false" value="#{option.label}. " /> 
        </h:column>
        <h:column> 
            <h:outputText styleClass="questionBlock" escape="false" value=" #{option.text}" /> 
        </h:column>
      </h:dataTable>      

  <!-- RICH TEXT - EMI RICH ANSWER OPTIONS-->
  <h:outputText styleClass="questionBlock" value="#{question.itemData.emiAnswerOptionsRichText}"  escape="false" rendered="#{question.itemData.isAnswerOptionsRich}"/>
      
  <!-- ATTACHMENTS BELOW - EMI RICH ANSWER OPTIONS-->
  <h:dataTable value="#{question.itemData.itemAttachmentList}" var="attach"  rendered="#{question.itemData.isAnswerOptionsRich}" cellpadding="4">
    <h:column>
      <%@ include file="/jsf/shared/mimeicon.jsp" %>
    </h:column>
    <h:column>
      <h:outputLink value="#{attach.location}" target="new_window">
         <h:outputText escape="false" value="#{attach.filename}" />
      </h:outputLink>
    </h:column>
    <h:column>
      <h:outputText escape="false" value="#{attach.fileSize} #{generalMessages.kb}" rendered="#{!attach.isLink}"/>
    </h:column>
  </h:dataTable>
  <!-- ATTACHMENTS ABOVE - EMI RICH ANSWER OPTIONS-->
      
  <!-- LEAD IN TEXT -->
      <f:verbatim><h3></f:verbatim>
      <h:outputText escape="false" value="#{question.itemData.leadInText}" />
      <f:verbatim></h3><br/></f:verbatim>

  <!-- EMI ITEMS -->
      <h:dataTable value="#{question.itemData.emiQuestionAnswerCombinations}" var="item" cellspacing="0" cellpadding="4">

        <h:column> 
         <h:panelGroup rendered="#{(item.text != null && item.text ne '')}">
          <h:outputText escape="false" value="#{item.sequence}. #{item.text}" />                 
         </h:panelGroup>         
        </h:column>
        <h:column>         
            <h:outputText escape="false" value="____"/>			
        </h:column>

      </h:dataTable>
      
      <f:verbatim><br/><br/></f:verbatim>
	  
  <%-- answerBlock --%>
  <h:panelGroup styleClass="answerBlock" rendered="#{printSettings.showKeys || printSettings.showKeysFeedback}">
  	<h:outputLabel value="#{printMessages.answer_point}: "/>
  	<h:outputText value="#{question.itemData.score}">
        <f:convertNumber maxFractionDigits="2"/>
    </h:outputText>
    <h:outputText escape="false" value=" #{authorMessages.points_lower_case}" />
  	<h:outputText value="<br />" escape="false" />
    <h:outputLabel value="#{printMessages.answer_key}: "/>
    <h:outputText escape="false" value="#{question.key}" />
    <h:outputText value="<br />" escape="false" />
  </h:panelGroup>
	  
  