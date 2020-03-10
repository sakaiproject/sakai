<f:view>
<sakai:view title="#{msgs.delete_room_confirm_title}">
  <script>includeLatestJQuery("deleteRoomMessagesConfirm.jsp");</script>
  <script>
    $(document).ready( function () {
          // Assign the current class to the tab in the template
          var menuLink = $('#chatDeleteRoomMessagesForm\\:chatManageLink, #chatDeleteRoomMessagesForm\\:chatChangeRoomLink');
          menuLink.addClass('current');
          // Remove the link of the current option
          menuLink.html(menuLink.find('a').text());
    });
  </script>
  <h:form styleClass="portletBody" id="chatDeleteRoomMessagesForm">
    <%@ include file="chatMenu.jsp" %>
    <h:outputText value="#{msgs.delete_room_messages_confirm_alert}" styleClass="sak-banner-warn" />
    <h:messages rendered="#{!empty facesContext.maximumSeverity}" />
	<sakai:panel_edit>
		<h:outputLabel for="title" value="#{msgs.channel_title_colon}" />
		<h:outputText id="title" value="#{ChatTool.currentChannelEdit.chatChannel.title}" />

		<h:outputLabel for="desc" value="#{msgs.channel_description_colon}" />
		<h:outputText id="desc" value="#{ChatTool.currentChannelEdit.chatChannel.description}" />

	</sakai:panel_edit>
	
	<sakai:button_bar>
	    <h:commandButton id="delete"
	        action="#{ChatTool.processActionDeleteRoomMessages}"
	        value="#{msgs['gen.delete']}"
	        styleClass="active" />
	    <h:commandButton id="cancel"
	        action="#{ChatTool.processActionDeleteRoomMessagesCancel}"
	        value="#{msgs['gen.cancel']}" />
	</sakai:button_bar>
	
  </h:form>
</sakai:view>
</f:view>
