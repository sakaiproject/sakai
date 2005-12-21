<h4><div style="width:100%">
	<div   style="float:left;width:49%;" >View &nbsp;&nbsp;
	<h:selectOneMenu  onchange="this.form.submit();"  valueChangeListener="#{ForumTool.processValueChangeForMessageView}" value="#{ForumTool.selectedMessageView}">
		<f:selectItem itemValue="dfAllMessages" itemLabel="#{msgs.msg_view_all}" />
		<f:selectItem itemValue="dfThreadedView" itemLabel="#{msgs.msg_view_threaded}" />
		<f:selectItem itemValue="expand" itemLabel="#{msgs.msg_view_expanded}" />
		<f:selectItem itemValue="collapse" itemLabel="#{msgs.msg_view_collapsed}" />
		<f:selectItem itemValue="dfUnreadView" itemLabel="#{msgs.msg_view_unread}" />
		<%--<f:selectItem itemValue="label" itemLabel="#{msgs.msg_view_bylabel}" />--%>
	</h:selectOneMenu>
	</div><div  style="float:right;text-align:right;width:49%;">
	<h:inputText  value="#{ForumTool.searchText}" />&nbsp;&nbsp;&nbsp;&nbsp;
    <h:commandButton  value="Search" action="#{ForumTool.processActionSearch}" onkeypress="document.forms[0].submit;"/>
	</div></div>
</h4>
