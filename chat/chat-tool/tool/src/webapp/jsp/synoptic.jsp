<f:view>
   <sakai:view title="#{msgs['custom.chatroom']}">
      <h:form>
      
         <sakai:tool_bar>
            <h:commandLink action="#{ChatTool.processActionSynopticOptions}" rendered="#{ChatTool.maintainer}">
               <h:outputText value="#{msgs.manage_tool}" />
            </h:commandLink>
         </sakai:tool_bar>
         
         <sakai:messages />
         
         <t:dataList id="chatSynoptic" var="message"
				value="#{ChatTool.synopticMessages}" layout="simple">
		
				<f:verbatim>
				<h3 class="testPanelHeader">
				</f:verbatim>
					<h:outputText value="#{message.restrictedBody}" />
				<f:verbatim>
				</h3>
				<p class="textPanelFooter">
				</f:verbatim>
					<h:outputText value="(#{message.chatMessage.chatChannel.title} - #{message.owner} - #{message.dateTime})" />
				<f:verbatim>
				</p>
				</f:verbatim>

			</t:dataList>
			
		</h:form>
	</sakai:view>
</f:view>