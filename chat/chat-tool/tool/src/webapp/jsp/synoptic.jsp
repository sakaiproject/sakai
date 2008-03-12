<f:view>
   <sakai:view title="#{msgs['custom.chatroom']}">
      <h:form>
      
         <sakai:tool_bar>
            <h:commandLink action="#{ChatTool.processActionSynopticOptions}" rendered="#{ChatTool.maintainer}">
               <h:outputText value="#{msgs.manage_tool}" />
            </h:commandLink>
         </sakai:tool_bar>
         
         <sakai:messages />
         
		<t:dataList id="chatSynoptic" var="message" value="#{ChatTool.synopticMessages}" layout="unorderedList" styleClass="synopticList">
        	<t:htmlTag value="h3" styleClass="textPanelHeader">
				<h:outputText value="#{message.restrictedBody}"/>
			</t:htmlTag>
	        <h:outputText value="(#{message.chatMessage.chatChannel.title} - #{message.owner} - #{message.dateTime})" styleClass="textPanelFooter" style="display:block"/>
        </t:dataList>
			
		</h:form>
	</sakai:view>
</f:view>