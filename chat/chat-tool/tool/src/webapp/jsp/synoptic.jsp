<f:view>
   <sakai:view title="#{msgs['custom.chatroom']}">
      <h:form>     
         <sakai:tool_bar>
            <h:commandLink action="#{ChatTool.processActionSynopticOptions}" rendered="#{ChatTool.maintainer}">
               <h:outputText value="#{ChatTool.accessibleOptionsLink}" escape="false"/>
            </h:commandLink>
         </sakai:tool_bar>         
         <sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />         
		<ul id="_id1:chatSynoptic" class="synopticList">
		<c:forEach items="${ChatTool.synopticMessages}" var="message">  		
			<li>
        	<t:htmlTag value="h3" styleClass="textPanelHeader"><c:out value="${message.restrictedBody}" /></t:htmlTag>
        	<t:htmlTag value="span" styleClass="textPanelFooter" style="display:block"><c:out value="(${message.chatMessage.chatChannel.title} - ${message.owner} - ${message.dateTime})" /></t:htmlTag>
			</li>
		</c:forEach></ul>
		</h:form>
	</sakai:view>
</f:view>