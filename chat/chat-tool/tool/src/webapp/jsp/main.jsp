<f:view>
    <sakai:view title="#{ChatTool.enterTool}">
		<!-- This page doesn't really matter as it will redirect the user to the "select a room" page or the "chat room" page  -->
    </sakai:view>
</f:view>
<%--

JSPs needed
	-test permissions
	-add/edit room
	-delete room confirm
	-make sure interface interacts correctly when deleting a room. (no crash), maybe message to interface saying that
		the room has been deleted.
	-delete message confirm
	-tool preferences -- select initial view (select room/ specific room)
	-Room
		-hit enter to submit a new message
		-having a return in the message causes it to not show up in the monitor
		

	-messageAdaptorComponents has a link to the ChatService to allow for searching of the chat tool messages
		That had to be knocked out because the ChatService isn't being used any more
	-chat synoptic is broken now
	
--%>
