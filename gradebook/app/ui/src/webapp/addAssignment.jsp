<f:view>
		<%
		  String thisId = request.getParameter("panel");
		  if (thisId == null) 
		  {
	    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
		  }
		%>
	<script type="text/javascript">
		var thisId = "<%= thisId %>";
		var MAX_NEW_ITEMS = <h:outputText value="#{msgs.add_assignment_max_bulk_items}" />;
	</script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/multiItemAdd.js"></script>
<%--  Commented out due to prototype already being embedded
		so including will break calendar widget
  	<script type="text/javascript" src="/library/js/jquery.js"></script> --%>

   <div class="portletBody">
	<h:form id="gbForm">

		<t:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@ include file="/inc/appMenu.jspf"%>
			
			<%@ include file="/inc/breadcrumb.jspf" %>
		</t:aliasBean>

		<sakai:flowState bean="#{addAssignmentBean}" />

		<p class="instruction"><h:outputText value="#{msgs.add_assignment_instruction}" /></p>

<%-- Commented out per SAK-12285
		<p>
		<h:outputText value="#{msgs.add_assignment_selector1}" />
			<h:selectOneMenu id="numItems" value="">
				<f:selectItems value="#{addAssignmentBean.addItemSelectList}" />
			</h:selectOneMenu>
			<h:outputText value="#{msgs.add_assignment_selector2}" />
		</p>
--%>
		<t:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
<%--			<%@ include file="/inc/assignmentEditing.jspf"%> --%>
			<%@ include file="/inc/globalMessages.jspf"%>

			<%-- Allows bulk creation of Gradebook Items --%>
			<%@ include file="/inc/bulkNewItems.jspf" %>
		</t:aliasBean>

		<%-- Calls a javascript function to add another Add Gradebook Item Pane --%>
		<h:outputLink id="addSecond" value="#" styleClass="addSecond act" >
			<h:outputText value="#{msgs.add_assignment_add_pane}" />
		</h:outputLink>

		<%-- Keeps track of how many panes are displayed --%>
		<h:inputHidden id="numTotalItems" value="#{addAssignmentBean.numTotalItems}" />
		
		<p class="act calendarPadding">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.add_assignment_submit}"
				action="#{addAssignmentBean.saveNewAssignment}"/>
			<h:commandButton
				value="#{msgs.add_assignment_cancel}"
				action="overview" immediate="true"/>
		</p>
 		
 		<%-- Need to attach listeners, must be here --%>
 		<script type="text/javascript">
 			// Add functionality for Add another link on page
			Event.observe(
				"gbForm:addSecond",
				"click",
				function(event){
					addItemScreen();
		
					adjustNumBulkItems(1);
				}
			);

			// Add functionality to Add item drop down
			// Commented out per SAK-12285
//			Event.observe(
//				"gbForm:numItems",
//				"change",
//				function(event){
//					addMultipleItems(this);

					// since DOM changed, resize
//					setMainFrameHeightNow(thisId, 'grow');
//				}
//			);

			// adds X remove icon to first pane if more than 2 are displayed
		 	if (getNumTotalItem() >= 2) {
 				addDelX();
		 	}
 			    
		    setMainFrameHeight('<%= thisId %>');
  		</script>
  	</h:form>
  </div>
</f:view>
