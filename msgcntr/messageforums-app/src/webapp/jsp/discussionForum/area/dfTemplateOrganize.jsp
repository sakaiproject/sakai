<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messageforums.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.cdfm_default_template_organize}" toolCssHref="/messageforums-tool/css/msgcntr.css">           
       		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
   <f:verbatim>
      <script language="javascript">
      
         function updateForums(forumIndexChanged)
         {
         	var numForums = 0;
         	
         	while(document.getElementById("revise:forums:" + 
         		numForums +":forumIndex") != null) {
         		numForums++;
         	}
         	
         	var indexSet = [], i;
         	for(i = 0; i < numForums; i++) 
         		indexSet[i] = false;
         		
         	for(i = 0; i < numForums; i++) {
         		var sel = document.getElementById("revise:forums:" + i +":forumIndex");
         		sel.className="selChanged";
         		indexSet[sel.selectedIndex] = true;
         	}
         	var oldIndex = -1;
         	for(i = 0; i < numForums; i++) {
         		if(!indexSet[i]) {
					oldIndex = i;
					break;
				}
         	}
         	
			var newIndex = forumIndexChanged.selectedIndex;
			
         	for(i = 0; i < numForums; i++) {
         		var sel = document.getElementById("revise:forums:" + i +":forumIndex");
         		
         		if(sel.id != forumIndexChanged.id) {
					var val = sel.selectedIndex;
					var valIndex = val;
					if(newIndex > oldIndex) {
						if(val > oldIndex)
							valIndex--;
						if(val > newIndex)
							valIndex++;
					} else {
						if(val < oldIndex)
							valIndex++;
						if(val < newIndex)
							valIndex--;
					}
					sel.selectedIndex = valIndex;
         		}
         	}
         }
         
         function updateTopics(forumIndex, topicIndexChanged)
         {
         	var numTopics = 0;
         	
         	while(document.getElementById("revise:forums:" + 
         		forumIndex + ":topics:" + numTopics + ":topicIndex") != null) {
         		numTopics++;
         	}
         	
         	var indexSet = [], i;
         	for(i = 0; i < numTopics; i++) 
         		indexSet[i] = false;
         		
         	for(i = 0; i < numTopics; i++) {
         		var sel = document.getElementById("revise:forums:" + 
         					forumIndex + ":topics:" + i +":topicIndex");
				sel.className="selChanged";   
         		indexSet[sel.selectedIndex] = true;
         	}
         	var oldIndex = -1;
         	for(i = 0; i < numTopics; i++) {
         		if(!indexSet[i]) {
					oldIndex = i;
					break;
				}
         	}
         	
			var newIndex = topicIndexChanged.selectedIndex;
			
         	for(i = 0; i < numTopics; i++) {
         		var sel = document.getElementById("revise:forums:" + 
         					forumIndex + ":topics:" + i +":topicIndex");
         		
         		if(sel.id != topicIndexChanged.id) {
					var val = sel.selectedIndex;
					var valIndex = val;
					if(newIndex > oldIndex) {
						if(val > oldIndex)
							valIndex--;
						if(val > newIndex)
							valIndex++;
					} else {
						if(val < oldIndex)
							valIndex++;
						if(val < newIndex)
							valIndex--;
					}
					sel.selectedIndex = valIndex;
         		}
         	}
         }
         
         //because we don't know which order
         var forumIndexIterator = 0;
         
      </script>
   </f:verbatim>
      <h:form id="revise">
		
			<!--jsp/discussionForum/area/dfTemplateOrganize.jsp-->
        <sakai:tool_bar_message value="#{msgs.cdfm_default_template_organize}" />
		 		<div class="instruction">
		  		  <h:outputText id="instruction" value="#{msgs.cdfm_default_template_organize_instruction}"/>
				</div>
			<h:dataTable id="forums" binding="#{ForumTool.forumTable}" value="#{ForumTool.forums}" width="100%" var="forum" cellpadding="0" cellspacing="0" >
    <h:column rendered="#{! forum.nonePermission}">
					<h:panelGroup style="display:block;width:90%;padding:.5em" styleClass="forumHeader">
					<h:selectOneMenu id="forumIndex" value="#{forum.forum.sortIndex}" onchange="updateForums(this);" style="margin-right:1em">
	            <f:selectItems value="#{ForumTool.forumSelectItems}"/>
	         </h:selectOneMenu>
					<h:outputText id="forumTitle" value="#{forum.forum.title}" style="font-weight:bold;font-size:1.3em;"/>
					</h:panelGroup>
						<%--//designNote: need a rendered atttrib for the folowing predicated on the existence of topics in this forum--%>
					<h:dataTable id="topics" rendered="#{!empty forum.forum.topics}" value="#{forum.forum.topics}" var="topic" width="100%" cellspacing="0" cellpadding="0">
		   <h:column>
							<h:panelGroup style="display:block;width:90%;padding:.2em;margin:.2em 0 .2em .5em" styleClass="topicBloc">
								<h:selectOneMenu id="topicIndex" value="#{topic.sortIndex}" onchange="updateTopics(#{ForumTool.forumTable.rowIndex}, this);" style="margin-left:.3em;margin-right:1em">
		            <f:selectItems value="#{forum.topicSelectItems}"/>
		         </h:selectOneMenu>
								<h:outputText id="topicTitle" value="#{topic.title}" style="font-weight:bold;font-size:1.2em;"/>
							</h:panelGroup>
		   </h:column>
        </h:dataTable>			
   </h:column>
 </h:dataTable>
		
		
        <div class="act">
          <h:commandButton action="#{ForumTool.processActionSaveTemplateOrganization}" 
                           onclick="form.submit;" value="#{msgs.cdfm_button_bar_save_setting}" 
                           accesskey="s" 
						   styleClass="active"/>
          <h:commandButton action="#{ForumTool.processActionHome}" 
                           value="#{msgs.cdfm_button_bar_cancel}"
                           accesskey="x" />
       </div>
	  </h:form>
    </sakai:view>
</f:view>




